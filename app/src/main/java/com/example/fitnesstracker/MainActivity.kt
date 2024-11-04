package com.example.fitnesstracker

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.android.gms.fitness.FitnessLocal
import com.google.android.gms.fitness.LocalRecordingClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    // UI components
    private lateinit var tvSteps: TextView
    private lateinit var bottomNavigationBar :BottomNavigationView
    private lateinit var stepTracker: StepTracker
    private lateinit var stepSlider: Slider
    private lateinit var Stepcard: CardView
    private val stepGoal = 6000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        tvSteps = findViewById(R.id.displayStep)
        bottomNavigationBar=findViewById(R.id.bottomNavigationView)
        stepSlider=findViewById(R.id.slider)
        Stepcard=findViewById(R.id.Gendercard)
        stepTracker = StepTracker(this)


        // Disable user interaction with the slider
        stepSlider.setOnTouchListener { _, _ -> true }


        val hasMinPlayServices = isGooglePlayServicesAvailable(this, LocalRecordingClient.LOCAL_RECORDING_CLIENT_MIN_VERSION_CODE)

        if(hasMinPlayServices != ConnectionResult.SUCCESS) {
            // Prompt user to update their device's Google Play services app and return
            GoogleApiAvailability.getInstance().getErrorDialog(this, hasMinPlayServices, 100)?.show()

            // Return as the app cannot proceed without updated services
            return
        }

        Stepcard.setOnClickListener{
            startActivity(Intent(this,StepActivity::class.java))
        }

        bottomNavigationBar.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.item_1 -> {
                    // Respond to navigation item 1 click
                    startActivity(Intent(this,BmiActivity::class.java))
                    true
                }
                R.id.item_2 -> {
                    // Respond to navigation item 2 click
                    val intent=Intent(this,StepActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val localRecordingClient = FitnessLocal.getLocalRecordingClient(this)

        stepTracker.subscribeToStepCount(localRecordingClient)
        // Read step data and display it in the UI
        stepTracker.readTodayStepCount(localRecordingClient) { stepData ->
            runOnUiThread {
                tvSteps.text = stepData // Display the step count data
//                updateSlider(stepData.toInt())
                if (stepData.isNotEmpty()) {
                    try {
                        val steps = stepData.toInt()
                        tvSteps.text = stepData  // Display the step count
                        updateSlider(steps)      // Update slider
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Invalid step data: $stepData")
                        tvSteps.text = "Invalid data"
                    }
                } else {
                    Log.w(TAG, "Step data is empty")
                    tvSteps.text = "No step data"
                }
            }
        }

    }
    private fun updateSlider(steps: Int) {
        // Calculate the percentage of the step goal achieved
        var percentage = (steps * 100) / stepGoal  // Calculate percentage completed

        // Ensure the percentage doesn't exceed 100%
        if (percentage > 100) {
            percentage = 100
        }

        stepSlider.value = percentage.toFloat()  // Update slider position
        stepSlider.setLabelFormatter { value: Float ->
            "${value.toInt()}%" // Display the value with a percentage symbol
        }
    }


}