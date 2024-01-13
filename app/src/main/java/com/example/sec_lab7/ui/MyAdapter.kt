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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
        val item = arrayList[position]
        val startTime = ZonedDateTime.ofInstant(item.startTime, item.startZoneOffset)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

        val convertViewCopy = LayoutInflater.from(context).inflate(R.layout.row, parent, false)
        convertViewCopy.findViewById<TextView>(R.id.fromTime).text =
            formatter.format(startTime)
        convertViewCopy.findViewById<TextView>(R.id.countSteps).text =
            item.count.toString()
        convertViewCopy.findViewById<FloatingActionButton>(R.id.btnRemove).setOnClickListener {
            viewModel.removeSteps(healthConnectClient, item.metadata.id)
            mainFragment.updateUI()
        }

        return convertViewCopy
    }
}