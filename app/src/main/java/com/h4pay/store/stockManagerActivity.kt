package com.h4pay.store

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.h4pay.store.customDialogs.yesNoDialog
import com.h4pay.store.networking.cancelOrder
import com.h4pay.store.networking.getProdList
import com.h4pay.store.recyclerAdapter.RecyclerItemClickListener
import com.h4pay.store.recyclerAdapter.orderRecycler
import com.h4pay.store.recyclerAdapter.productRecycler
import org.json.JSONArray

class stockManagerActivity(): AppCompatActivity() {

    private val TAG = "stockManagerActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var productNameView:TextView
    private lateinit var productPriceView :TextView
    private lateinit var discountButton :Button
    private lateinit var inStockView:Switch



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stockmanager)
        loadUI()
        loadList() //RecyclerView Init
    }

    fun loadUI(){
        productNameView = findViewById(R.id.productName)
        productPriceView = findViewById(R.id.productPrice)
        discountButton = findViewById(R.id.discountApply)
        inStockView = findViewById(R.id.soldout)
        recyclerView = findViewById(R.id.productRecyclerView)
    }

    fun loadProduct(position: Int){
        val item = prodList.getJSONObject(position)
        val soldout = item.getBoolean("soldout")
        productNameView.text = item.getString("productName")
        productPriceView.text = item.getString("price")
        discountButton.setOnClickListener {
            //TODO: 할인 Dialog Display
        }
        inStockView.textOn = "재고 있음"
        inStockView.textOff = "품절"
        inStockView.isChecked = soldout


        inStockView.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, isChecked.toString())
            if (isChecked && !soldout){
                yesNoDialog(this@stockManagerActivity,"확인", "정말로 품절처리 하시겠습니까?", {
                    //TODO: 품절처리 Method
                },{
                    inStockView.isChecked = false
                })
            }else if(!isChecked && soldout){
                yesNoDialog(this@stockManagerActivity, "확인", "정말로 재고 있음 상태로 만드시겠습니까?", {
                    //TODO: InStock처리 Method
                },{inStockView.isChecked = true})
            }
        }
    }

    fun loadList(){
        //Load Data
        prodList = getProdList().execute().get()

        //RecyclerView Init
        viewManager = LinearLayoutManager(this)
        viewAdapter = productRecycler(this, prodList)
        recyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            adapter = viewAdapter
            layoutManager = viewManager

            addOnItemTouchListener(
                RecyclerItemClickListener(applicationContext, this,
                    object : RecyclerItemClickListener.OnItemClickListener {
                        @SuppressLint("SetTextI18n")
                        override fun onItemClick(view: View, position: Int) {
                            loadProduct(position)
                        }
                    }
                )
            )
        }
    }
}