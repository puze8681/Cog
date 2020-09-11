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
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private PrefUtil prefUtil;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG,"onReceive()");
        /** * http://mmarvick.github.io/blog/blog/lollipop-multiple-broadcastreceiver-call-state/ * 2번 호출되는 문제 해결 */
        prefUtil = new PrefUtil(context);
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (state.equals(mLastState)) { return; }
        else { mLastState = state; }
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            final String phone_number = PhoneNumberUtils.formatNumber(incomingNumber);
            getData(context, phone_number);
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

