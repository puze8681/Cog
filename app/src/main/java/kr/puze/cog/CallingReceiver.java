package kr.puze.cog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kr.puze.cog.Data.CogData;
import www.okit.co.Utils.PrefUtil;

public class CallingReceiver extends BroadcastReceiver {
    public static final String TAG = "PHONE STATE";
    private static String mLastState;
    private PrefUtil prefUtil;

    public void onReceive(Context context, Intent intent) {
        prefUtil = new PrefUtil(context);

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            String savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;

            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }

            if (number != null && !number.isEmpty() && !number.equals("null")) {
                //onCallStateChanged(context, state, number);
                Log.d("TEST :","NUMBER =>"+number);
                getData(context, number);
            }
        }
    }

    private void getData(final Context context, String number){
        if(number.startsWith("+82")){
            number = number.replace("+82", "0");
        }
        number = number.replace(" ", "");
        number = number.replace("-", "");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Cogs");
        reference.child(prefUtil.getPhone()).child(number).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.w(TAG, "loadPost:onDataChange");
                if(dataSnapshot.exists()){
                    CogData cog = dataSnapshot.getValue(CogData.class);
                    Log.d("LOGTAG/RECEIVER", cog.getDate());
                    Log.d("LOGTAG/RECEIVER", cog.getName());
                    Log.d("LOGTAG/RECEIVER", cog.getNumber());
                    Log.d("LOGTAG/RECEIVER", String.valueOf(cog.getMoney()));
                    Log.d("LOGTAG/RECEIVER", String.valueOf(cog.getPay()));
                    Log.d("LOGTAG/RECEIVER", String.valueOf(cog.getCount()));
                    Log.d("LOGTAG/RECEIVER", dataSnapshot.getValue().toString());
                    Intent serviceIntent = new Intent(context, CallingService.class);
                    serviceIntent.putExtra("date", cog.getDate());
                    serviceIntent.putExtra("name", cog.getName());
                    serviceIntent.putExtra("number", cog.getNumber());
                    serviceIntent.putExtra("money", cog.getMoney());
                    serviceIntent.putExtra("pay", cog.getPay());
                    serviceIntent.putExtra("count", cog.getCount());
                    context.startService(serviceIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }
}

