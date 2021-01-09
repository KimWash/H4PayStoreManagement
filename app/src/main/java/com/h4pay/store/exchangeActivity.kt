package com.h4pay.store

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.h4pay.store.networking.cancelOrder
import com.h4pay.store.networking.exchangeOrder
import com.h4pay.store.networking.orderLookupByID
import com.h4pay.store.recyclerAdapter.itemsRecycler
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*


class exchangeActivity : AppCompatActivity() {

    private val TAG = "exchangeActivity"
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val wakeLock: PowerManager.WakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag").apply {
                    acquire()
                }
            }

        viewManager = LinearLayoutManager(this)

        val edit = findViewById<EditText>(R.id.orderIdInput)
        val uid = findViewById<TextView>(R.id.lookup_uid)
        val orderid = findViewById<TextView>(R.id.lookup_orderid)
        val date = findViewById<TextView>(R.id.lookup_date)
        val expire = findViewById<TextView>(R.id.lookup_expire)
        val amount = findViewById<TextView>(R.id.lookup_amount)
        val exchanged = findViewById<TextView>(R.id. exchanged)
        val exchangeButton = findViewById<LinearLayout>(R.id.exchange_Button)
        val cancel = findViewById<LinearLayout>(R.id.exchange_cancel_button)
        exchangeButton.isVisible = false
        cancel.isVisible = false

        edit.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // 입력되는 텍스트에 변화가 있을 때
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(arg0: Editable) {
                // 입력이 끝났을 때
                edit.requestFocus();
                if (edit.text.length < 0 || edit.text.length > 21){
                    Toast.makeText(this@exchangeActivity, "올바른 주문번호가 아닙니다!", Toast.LENGTH_SHORT).show()
                }
                else if (edit.text.length == 21){
                    val f = NumberFormat.getInstance()
                    f.isGroupingUsed = false
                    val orderID = edit.getText().toString()
                    val res = orderLookupByID(orderID).execute().get()
                    //----------RecyclerView Init-----------
                    //----------RecyclerView Init-----------
                    var items = res.getJSONArray("item")
                    for (i in 0 until items.length()-1){
                        if (items.getJSONObject(i).getInt("amount") == 0){
                            items.remove(i)
                        }
                    }

                    viewAdapter = itemsRecycler(this@exchangeActivity, items)
                    val lm = LinearLayoutManager(this@exchangeActivity, LinearLayoutManager.HORIZONTAL, false)
                    val recyclerView = findViewById<RecyclerView>(R.id.itemsRecyclerView)

                    recyclerView.apply {
                        // use this setting to improve performance if you know that changes
                        // in content do not change the layout size of the RecyclerView
                        setHasFixedSize(true)

                        // use a linear layout manager
                        layoutManager = lm


                        // specify an viewAdapter (see also next example)
                        adapter = viewAdapter

                    }
                    //----------RecyclerView END-----------
                    recyclerView.isVisible = true
                    if (res != null){
                        edit.setText("")
                        uid.text = "사용자 ID " + res.getString("uid")
                        orderid.text = "주문번호 " +  f.format(res.get("orderid"))
                        val format = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.JAPANESE
                        )
                        val format2 = SimpleDateFormat(
                                "yyyy년 MM월 dd일 HH시 mm분 ss초", Locale.KOREAN
                        )
                        val moneyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());

                        format.timeZone = TimeZone.getTimeZone("UTC")
                        format2.timeZone = TimeZone.getTimeZone("UTC")
                        val pretifiedDate = format.parse(res.getString("date"))
                        val pretifiedExpire = format.parse(res.getString("expire"))
                        date.text = "주문 일시 ${format2.format(pretifiedDate)}"
                        expire.text = "사용 기한 ${format2.format(pretifiedExpire)}"
                        amount.text = "결제 금액 ${moneyFormat.format(res.getInt("amount"))}"

                        recyclerView.post{
                            edit.isFocusableInTouchMode = true;
                            edit.requestFocus()
                        }

                        Thread(Runnable{
                            Thread.sleep(1000)
                            runOnUiThread {
                                edit?.requestFocus()
                            }
                        }).start()

                        if (res.getBoolean("exchanged")){
                            exchanged.text = "교환됨"
                            exchanged.background = ContextCompat.getDrawable(this@exchangeActivity, R.drawable.rounded_red)
                            exchangeButton.isEnabled = false
                            edit.requestFocus()
                        }
                        else{
                            exchanged.text = "교환 안 됨"
                            exchanged.background = ContextCompat.getDrawable(this@exchangeActivity, R.drawable.rounded_green)
                            exchangeButton.isEnabled = true
                            exchangeButton.isVisible = true
                            cancel.isVisible = true
                            edit.requestFocus()

                            //------Cancel and Exchange Button OnClick Event----------

                            exchangeButton.setOnClickListener {

                                val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                                    when (which) {
                                        DialogInterface.BUTTON_POSITIVE -> {

                                            if (exchangeOrder(f.format(res.get("orderid"))).execute().get() == true) {
                                                Toast.makeText(this@exchangeActivity, "교환이 정상적으로 완료되었습니다!", Toast.LENGTH_SHORT).show()
                                                uid.text = ""
                                                orderid.text = ""
                                                date.text = ""
                                                expire.text = ""
                                                amount.text = ""
                                                exchanged.text = ""
                                                exchanged.background = null
                                                recyclerView.isVisible = false
                                                exchangeButton.isVisible = false
                                            }
                                            else{
                                                Toast.makeText(this@exchangeActivity, "교환에 실패했습니다.\n이미 교환되었거나 없는 주문번호입니다.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                        DialogInterface.BUTTON_NEGATIVE -> {
                                        }
                                    }
                                }


                                AlertDialog.Builder(this@exchangeActivity, R.style.AlertDialogTheme)
                                        .setTitle("확인")
                                        .setMessage("정말로 교환처리 하시겠습니까?")
                                        .setPositiveButton("예", dialogClickListener)
                                        .setNegativeButton("아니오", dialogClickListener)
                                        .show()


                                cancel.setOnClickListener {
                                    val dialogClickListener =
                                        DialogInterface.OnClickListener { _, which ->
                                            when (which) {
                                                DialogInterface.BUTTON_POSITIVE -> {
                                                    Log.e(TAG, "yes")
                                                    if (cancelOrder(f.format(res.get("orderid"))).execute()
                                                            .get() == true
                                                    ) {
                                                        Toast.makeText(
                                                            this@exchangeActivity,
                                                            "취소가 정상적으로 완료되었습니다.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        uid.text = ""
                                                        orderid.text = ""
                                                        date.text = ""
                                                        expire.text = ""
                                                        amount.text = ""
                                                        exchanged.text = ""
                                                        exchanged.background = null
                                                        recyclerView.isVisible = false
                                                    } else {
                                                        Toast.makeText(
                                                            this@exchangeActivity,
                                                            "취소에 실패했습니다.\n없는 주문번호입니다.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                                DialogInterface.BUTTON_NEGATIVE -> {
                                                    Log.e(TAG, "no")
                                                }
                                            }
                                        }

                                    AlertDialog.Builder(
                                        this@exchangeActivity,
                                        R.style.AlertDialogTheme
                                    )
                                        .setTitle("확인")
                                        .setMessage("정말로 취소처리 하시겠습니까?")
                                        .setPositiveButton("예", dialogClickListener)
                                        .setNegativeButton("아니오", dialogClickListener)
                                        .show()
                                }
                            }
                        }


                    }
                    else{
                        Toast.makeText(this@exchangeActivity, "주문 내역이 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        edit.setText("")
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                // 입력하기 전에
            }
        })


    }
}
