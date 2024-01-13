package com.example.sec_lab7.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.health.connect.client.HealthConnectClient
import com.example.sec_lab7.R
import androidx.health.connect.client.records.StepsRecord
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MyAdapter(
    private val context: Context,
    private val arrayList: MutableList<StepsRecord>,
    private val viewModel: MainViewModel,
    private val mainFragment: MainFragment,
    private var healthConnectClient: HealthConnectClient
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val convertViewCopy = LayoutInflater.from(context).inflate(R.layout.row, parent, false)
        convertViewCopy.findViewById<TextView>(R.id.fromTime).text =
            arrayList[position].startTime.toString()
        convertViewCopy.findViewById<TextView>(R.id.countSteps).text =
            arrayList[position].count.toString()
        convertViewCopy.findViewById<FloatingActionButton>(R.id.btnRemove).setOnClickListener {
            val steps = arrayList[position]
            viewModel.removeSteps(healthConnectClient, steps.startTime.minusMillis(1), steps.endTime.plusMillis(1))
            mainFragment.updateUI()
        }

        return convertViewCopy
    }
}