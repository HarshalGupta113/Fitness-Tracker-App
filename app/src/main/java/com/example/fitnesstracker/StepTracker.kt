package com.example.fitnesstracker

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.gms.fitness.data.LocalDataSet
import com.google.android.gms.fitness.data.LocalDataType
import com.google.android.gms.fitness.data.LocalField
import com.google.android.gms.fitness.request.LocalDataReadRequest
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class StepTracker(private val context :Context) {
    fun subscribeToStepCount(localRecordingClient: LocalRecordingClient) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        localRecordingClient.subscribe(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener {
                Log.i(TAG, "Successfully subscribed to step count!")
            }
            .addOnFailureListener { e ->
                Snackbar.make((context as Activity).findViewById(android.R.id.content),e.localizedMessage,Snackbar.LENGTH_SHORT).show()
                Log.w(TAG, "There was an issue subscribing to step count.", e)
            }
    }

    // Read the step count data for the past week
    fun readTodayStepCount(localRecordingClient: LocalRecordingClient, onDataRead: (String) -> Unit) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault()) // Current time
        val startTime = endTime.toLocalDate().atStartOfDay(ZoneId.systemDefault()) // Start of today

        val readRequest = LocalDataReadRequest.Builder()
            .aggregate(LocalDataType.TYPE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        localRecordingClient.readData(readRequest)
            .addOnSuccessListener { response ->
                for (dataSet in response.buckets.flatMap { it.dataSets }) {
                    val stepData = dumpDataSet(dataSet) // Process data
                    onDataRead(stepData) // Pass the data to the callback
                }
            }
            .addOnFailureListener { e ->
                Snackbar.make((context as Activity).findViewById(android.R.id.content), e.localizedMessage, Snackbar.LENGTH_SHORT).show()
                Log.w(TAG, "There was an error reading step data", e)
            }

    }


    // Process the dataset and extract step data
    private fun dumpDataSet(dataSet: LocalDataSet): String {
        val stepData = StringBuilder()
        Log.i(TAG, "Data returned for Data type: ${dataSet.dataType.name}")
        for (dp in dataSet.dataPoints) {
            for (field in dp.dataType.fields) {
                val stepCount = "${dp.getValue(field)}"
                stepData.append(stepCount)
                Log.i(TAG, "\tField: ${field.name} Value: ${dp.getValue(field)}")
            }
        }
        return stepData.toString()
    }

    fun readDistanceData(localRecordingClient: LocalRecordingClient, onDataRead: (String) -> Unit) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)

        val readRequest = LocalDataReadRequest.Builder()
            .aggregate(LocalDataType.TYPE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        localRecordingClient.readData(readRequest)
            .addOnSuccessListener { response ->
                val distanceData = StringBuilder()
                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(LocalDataType.TYPE_DISTANCE_DELTA)

                    if (dataSet != null) {
                        val distance = dataSet.dataPoints
                            .map { dp -> dp.getValue(LocalField.FIELD_DISTANCE).asFloat() } // Convert each value to a float
                            .sum() // Sum the floats

                        // Use getStartTime(TimeUnit.MILLISECONDS) to get the start time
                        val startTime = bucket.getStartTime(TimeUnit.MILLISECONDS)

                        distanceData.append("Day: $startTime\nDistance: ${distance} meters\n")
                    }
                }
                onDataRead(distanceData.toString())
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was an error reading distance data", e)
            }
    }


    fun readCaloriesData(localRecordingClient: LocalRecordingClient, onDataRead: (String) -> Unit) {
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusWeeks(1)

        val readRequest = LocalDataReadRequest.Builder()
            .aggregate(LocalDataType.TYPE_CALORIES_EXPENDED)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .build()

        localRecordingClient.readData(readRequest)
            .addOnSuccessListener { response ->
                val caloriesData = StringBuilder()

                for (bucket in response.buckets) {
                    val dataSet = bucket.getDataSet(LocalDataType.TYPE_CALORIES_EXPENDED)

                    if (dataSet != null) {
                        val calories = dataSet.dataPoints
                            .map { dp -> dp.getValue(LocalField.FIELD_CALORIES).asFloat() } // Map to floats
                            .sum() // Sum the calories

                        // Use getStartTime(TimeUnit.MILLISECONDS) to get the correct time
                        val startTime = bucket.getStartTime(TimeUnit.MILLISECONDS)

                        caloriesData.append("Day: $startTime\nCalories: ${calories} kcal\n").trim()
                    } else {
                        Log.w(TAG, "No calories data available for this bucket.")
                    }
                }


                onDataRead(caloriesData.toString())
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was an error reading calories data", e)
            }
    }


}