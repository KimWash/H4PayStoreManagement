package com.h4pay.store

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.h4pay.store.networking.getProdList
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

var prodList : JSONArray = getProdList().execute().get()

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val time = findViewById<TextView>(R.id.time)

        val wakeLock: PowerManager.WakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
            }

        val mHandler = Handler()
        val thread = Thread(Runnable{
            run{
                while (true){
                    mHandler.post(Runnable {
                        run{
                            val cal = Calendar.getInstance()
                            var min = cal.get(Calendar.MINUTE).toString()
                            var hour = cal.get(Calendar.HOUR).toString()
                            min = (if (min.toInt() < 10) "0" else "") + min
                            hour = (if (hour.toInt() < 10) "0" else "") + hour

                            time.setText("$hour : $min")
                        }
                    })
                    Thread.sleep(10000)
                }
            }
        })
        thread.start()

        val exchangeButton = findViewById<Button>(R.id.exchangeButton)
        exchangeButton.setOnClickListener {
            var exchange = Intent(this, exchangeActivity::class.java)
            startActivity(exchange)
        }

        val orderListButton = findViewById<Button>(R.id.orderListButton)
        orderListButton.setOnClickListener {
            var orderList = Intent(this, orderList::class.java)
            startActivity(orderList)
        }

        val makeNoticeButton = findViewById<Button>(R.id.makeNoticeButton)
        makeNoticeButton.setOnClickListener {
            var makeNotice = Intent(this, makeNotice::class.java)
            startActivity(makeNotice)
        }

        val csButton = findViewById<Button>(R.id.csButton)
        csButton.setOnClickListener {
            var csButton = Intent(this, customerSerivce::class.java)
            startActivity(csButton)
        }

        val callDeveloperButton = findViewById<Button>(R.id.callDeveloperButton)
        callDeveloperButton.setOnClickListener {
            var callDeveloper = Intent(this, callDeveloper::class.java)
            startActivity(callDeveloper)
        }
    }
}