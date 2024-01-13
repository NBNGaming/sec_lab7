package com.example.sec_lab7.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.sec_lab7.MainActivity
import com.example.sec_lab7.R
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    // Для DatePicker
    var myDay = 0
    var myMonth: Int = 0
    var myYear: Int = 0

    // Результат после выбора даты и времени
    lateinit var myDateTime: ZonedDateTime
    var myStepsCount: Long? = null

    private lateinit var viewModel: MainViewModel
    private lateinit var mainActivity: MainActivity

    private lateinit var listView: ListView

    private lateinit var healthConnectClient: HealthConnectClient
    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )
    private val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    private val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            updateUI()
        }
    }

    private suspend fun checkPermissionsAndRun() {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        if (granted.containsAll(PERMISSIONS)) {
            updateUI()
        } else {
            requestPermissions.launch(PERMISSIONS)
        }
    }

    private fun checkSDK() {
        val availabilityStatus = HealthConnectClient.getSdkStatus(mainActivity)
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE ||
            availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            return
        }
        healthConnectClient = HealthConnectClient.getOrCreate(mainActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        mainActivity = this.activity as MainActivity
        myDateTime = ZonedDateTime.now()
        checkSDK()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = mainActivity.findViewById(R.id.steps_list_view)

        val nameObserver = Observer<MutableList<StepsRecord>> { newList ->
            val adapter = MyAdapter(requireContext(), newList, viewModel, this, healthConnectClient)
            listView.adapter = adapter
        }

        viewModel.stepsListLive.observe(viewLifecycleOwner, nameObserver)

        mainActivity.findViewById<TextView>(R.id.selectedDateTime).setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                mainActivity,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            )
            datePickerDialog.show()
        }

        mainActivity.findViewById<Button>(R.id.addSteps).setOnClickListener {
            if (!addStepsBtnEnabled()) {
                return@setOnClickListener
            }
            viewModel.addSteps(healthConnectClient, myDateTime, myStepsCount!!)
            updateUI()
        }

        mainActivity.findViewById<SeekBar>(R.id.seekBar)
            .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    myStepsCount = progress.toLong() * 400
                    updateUI()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })

        viewLifecycleOwner.lifecycleScope.launch {
            checkPermissionsAndRun()
        }
        updateUI()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myYear = year
        myMonth = month + 1
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            mainActivity,
            this,
            calendar.get(Calendar.HOUR),
            0,
            DateFormat.is24HourFormat(mainActivity),
        )
        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val localDateTime = LocalDateTime.of(myYear, myMonth, myDay, hourOfDay, 0, 0)
        myDateTime = localDateTime.atZone(myDateTime.zone)
        updateUI()
    }

    fun updateUI() {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
        var title = formatter.format(myDateTime)
        if (myStepsCount != null) {
            title += " — $myStepsCount steps"
        }
        mainActivity.findViewById<TextView>(R.id.selectedDateTime).text = title
        mainActivity.findViewById<Button>(R.id.addSteps).isEnabled = addStepsBtnEnabled()
        val startTime = myDateTime.truncatedTo(ChronoUnit.DAYS)
        viewModel.getSteps(healthConnectClient, startTime, startTime.plusDays(1).minusSeconds(1))
    }

    private fun addStepsBtnEnabled(): Boolean {
        return myStepsCount != null && (myStepsCount ?: 0) > 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

}