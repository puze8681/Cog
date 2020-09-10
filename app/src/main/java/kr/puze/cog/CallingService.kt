package kr.puze.cog

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_call.view.*
import kotlin.math.roundToInt

@SuppressLint("ClickableViewAccessibility")
class CallingService: Service() {

    var callDate: String = ""
    var callName: String = ""
    var callNumber: String = ""
    var callMoney: Int = 0
    var callPay: Int = 0
    var callCount: Int = 0
    lateinit var windowManager: WindowManager
    lateinit var params: WindowManager.LayoutParams
    lateinit var rootView: View
    lateinit var dateView: TextView
    lateinit var nameView: TextView
    lateinit var numberView: TextView
    lateinit var moneyView: TextView
    lateinit var payView: TextView
    lateinit var countView: TextView
    lateinit var buttonView: TextView

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay as Display
        val width = ((display.width) * 0.9).roundToInt()

        params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        rootView = layoutInflater.inflate(R.layout.dialog_call, null)
        dateView = rootView.text_date_dialog_call
        nameView = rootView.text_name_dialog_call
        numberView = rootView.text_number_dialog_call
        moneyView = rootView.text_money_dialog_call
        payView = rootView.text_pay_dialog_call
        countView = rootView.text_count_dialog_call
        buttonView = rootView.button_dialog_call
        buttonView.setOnClickListener { removePopup() }

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
            dateView.text = callDate
            nameView.text = callName
            numberView.text = callNumber
            moneyView.text = "$callMoney"
            payView.text = "$callPay"
            countView.text = "$callCount"
        }

        return START_REDELIVER_INTENT
    }

    private fun setExtra(intent: Intent?){
        if(intent == null){
            removePopup()
            return
        }
        callDate = intent.getStringExtra("date")
        callName = intent.getStringExtra("name")
        callNumber = intent.getStringExtra("number")
        callMoney = intent.getIntExtra("money", 0)
        callPay = intent.getIntExtra("pay", 0)
        callCount = intent.getIntExtra("count", 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        removePopup()
    }

    fun removePopup(){
        if(rootView != null && windowManager != null) windowManager.removeView(rootView)
    }
}