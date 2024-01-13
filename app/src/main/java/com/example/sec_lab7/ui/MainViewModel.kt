package com.example.sec_lab7.ui

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZonedDateTime

class MainViewModel : ViewModel() {
    var stepsListLive: MutableLiveData<MutableList<StepsRecord>> = MutableLiveData()

    fun addSteps(healthConnectClient: HealthConnectClient, startTime: ZonedDateTime, count: Long) {
        val endTime = startTime.plusMinutes(59)

        viewModelScope.launch {
            try {
                val stepsRecord = StepsRecord(
                    count = count,
                    startTime = startTime.toInstant(),
                    endTime = endTime.toInstant(),
                    startZoneOffset = startTime.offset,
                    endZoneOffset = endTime.offset,
                )
                healthConnectClient.insertRecords(listOf(stepsRecord))
            } catch (e: Exception) {
                // Run error handling here
            }
        }
    }

    fun removeSteps(healthConnectClient: HealthConnectClient, startTime: Instant, endTime: Instant) {
        viewModelScope.launch {
            try {
                healthConnectClient.deleteRecords(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            } catch (e: Exception) {
                // Run error handling here
            }
        }
    }

    fun getSteps(healthConnectClient: HealthConnectClient, startTime: ZonedDateTime, endTime: ZonedDateTime) {
        val result = mutableListOf<StepsRecord>()

        viewModelScope.launch {
            try {
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime.toInstant(), endTime.toInstant())
                    )
                )
                for (stepRecord in response.records) {
                    result.add(stepRecord)
                }
                result.sortByDescending { it.startTime }
                stepsListLive.value = result
            } catch (e: Exception) {
                Log.e("", e.toString())
                // Run error handling here
            }
        }
    }
}