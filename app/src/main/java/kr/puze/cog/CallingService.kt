package kr.puze.cog

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_call.view.*
import kotlin.math.roundToInt


@SuppressLint("ClickableViewAccessibility")
class CallingService: Service() {

    var callName: String = ""
    var callNumber: String = ""
    var callLoanMoney: String = ""
    var callPayCount: String = ""
    var callPay: String = ""
    var callLoanCount: String = ""
    var callDate: ArrayList<String> = ArrayList()
    var callPayName: ArrayList<String> = ArrayList()

    lateinit var windowManager: WindowManager
    lateinit var params: WindowManager.LayoutParams
    lateinit var rootView: View
    lateinit var nameView: TextView
    lateinit var numberView: TextView
    lateinit var moneyView: TextView
    lateinit var payView: TextView
    lateinit var unpaidView: TextView
    lateinit var loanCountView: TextView
    lateinit var firstLoanDateView: TextView
    lateinit var loanDateView: TextView
    lateinit var payerView: TextView

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LOGTAG/SERVICE", "onCreate")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay as Display
        val width = ((display.width) * 0.9).roundToInt()

        params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            PixelFormat.TRANSLUCENT)

        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.dialog_call, null)
        nameView = rootView.text_name_dialog_call
        numberView = rootView.text_number_dialog_call
        moneyView = rootView.text_money_dialog_call
        payView = rootView.text_pay_dialog_call
        unpaidView = rootView.text_unpaid_dialog_call
        loanCountView = rootView.text_loan_count_dialog_call
        firstLoanDateView = rootView.text_first_loan_date_dialog_call
        loanDateView = rootView.text_loan_date_dialog_call
        payerView = rootView.text_payer_dialog_call

        setDraggable()
    }

    private fun setDraggable(){
        rootView.setOnTouchListener { v, event ->
            var initialX: Int = 0
            var initialY: Int = 0
            var initialTouchX: Float = 0f
            var initialTouchY: Float = 0f
            when(event.action){
                MotionEvent.ACTION_DOWN->{
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP->{
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE->{
                    params.x = initialX + (event.rawX - initialTouchX).roundToInt()
                    params.y = initialY + (event.rawY - initialTouchY).roundToInt()

                    if(rootView != null) windowManager.updateViewLayout(rootView, params)
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        windowManager.addView(rootView, params)
        setExtra(intent)

        if(!callDate.isNullOrEmpty() && !callName.isNullOrEmpty() && !callNumber.isNullOrEmpty()){
            var dateString = "대출: "
            for (i in callDate){
                dateString += "\n$i"
            }
            var payerString = "입금자명: "
            for (i in callPayName){
                payerString += "$i, "
            }

            nameView.text = callName
            numberView.text = callNumber
            moneyView.text = "대출금액: $callLoanMoney"
            payView.text = "$callPayCount 번($callPay 원)"
            unpaidView.text = "미납: ${callLoanMoney.toInt() - callPay.toInt()}"
            loanCountView.text = "대출횟수: $callLoanCount 번"
            firstLoanDateView.text = "첫대출: ${callDate[0]}"
            loanDateView.text = dateString
            payerView.text = payerString
        }

        return START_REDELIVER_INTENT
    }

    private fun setExtra(intent: Intent?){
        if(intent == null){
            removePopup()
            return
        }

        callName = intent.getStringExtra("name")
        callNumber = intent.getStringExtra("number")
        callLoanMoney = intent.getStringExtra("loanMoney")
        callPayCount = intent.getStringExtra("payCount")
        callPay = intent.getStringExtra("pay")
        callLoanCount = intent.getStringExtra("loanCount")
        callDate = intent.getStringArrayListExtra("date")
        callPayName = intent.getStringArrayListExtra("payName")
    }

    override fun onDestroy() {
        super.onDestroy()
        removePopup()
    }

    private fun removePopup(){
        if(rootView != null && windowManager != null) windowManager.removeView(rootView)
    }
}