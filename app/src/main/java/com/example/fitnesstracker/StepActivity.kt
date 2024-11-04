package com.example.fitnesstracker

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.data.LocalField
import com.google.android.gms.fitness.request.LocalDataReadRequest
import com.google.android.material.slider.Slider
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class StepActivity : AppCompatActivity() {
    private lateinit var displayStep: TextView
    private lateinit var slider: Slider
    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_step)

        slider=findViewById(R.id.slider)
        displayStep=findViewById(R.id.textView7)
        lineChart = findViewById(R.id.lineChart)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val localRecordingClient = FitnessLocal.getLocalRecordingClient(this)
        val stepTracker=StepTracker(this)
        slider.setOnTouchListener { _, _ -> true }

        stepTracker.readTodayStepCount(localRecordingClient){stepData->runOnUiThread{
            displayStep.text=stepData
            if(stepData.toFloat()<=6000){
                slider.value=stepData.toFloat()
            }else{
                val default=6000
                slider.value=default.toFloat()

            }

        }}
        // Read weekly step data and set up the chart
        readWeeklyStepData(localRecordingClient)
    }
    private fun readWeeklyStepData(localRecordingClient: LocalRecordingClient) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)

        val readRequest = LocalDataReadRequest.Builder()
            .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        localRecordingClient.readData(readRequest)
            .addOnSuccessListener { response ->
                val entries = ArrayList<Entry>()
                var index = 0
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(LocalDataType.TYPE_STEP_COUNT_DELTA)

                    if (dataSet != null) {
                        val steps = dataSet.dataPoints
                            .map { dp -> dp.getValue(LocalField.FIELD_STEPS).asInt() } // Use asInt() for steps
                            .sum() // Sum the steps

                        entries.add(Entry(index.toFloat(), steps.toFloat())) // Convert steps to float
                        index++
                    }
                }
                setupChart(entries)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was an error reading step data", e)
            }
    }


    private fun setupChart(entries: List<Entry>) {
        val lineDataSet = LineDataSet(entries, "Weekly Steps") // Create a dataset
        lineDataSet.color = resources.getColor(R.color.teal) // Set the color for the line
//        lineDataSet.valueTextColor = resources.getColor(R.color.black) // Set text color
        lineDataSet.valueTextSize = 12f

        val lineData = LineData(lineDataSet) // Create LineData object
        lineChart.data = lineData // Set data to chart
        lineChart.invalidate() // Refresh the chart
    }
}