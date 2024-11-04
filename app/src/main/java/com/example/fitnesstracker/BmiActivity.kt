package com.example.fitnesstracker

import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider

class BmiActivity : AppCompatActivity() {

    private lateinit var genderGroup:RadioGroup
    private lateinit var heightSlider:Slider
    private lateinit var weightSlider:Slider
    private lateinit var CalculateBmi:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bmi)

        genderGroup=findViewById(R.id.GenderGroup)
        heightSlider=findViewById(R.id.heightslider)
        weightSlider=findViewById(R.id.weightslider)
        CalculateBmi=findViewById(R.id.calBmi)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        CalculateBmi.setOnClickListener{
            val selectedGenderId = genderGroup.checkedRadioButtonId
            val selectedGender = when (selectedGenderId) {
                R.id.male -> "Male"
                R.id.female -> "Female"
                else -> "Not Selected"
            }
            if (selectedGender == "Not Selected") {
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
            }else{
                // Get height and weight values
                val height = heightSlider.value  // Height in cm
                val weight = weightSlider.value  // Weight in kg

                // Calculate BMI
                val heightInMeters = height / 100  // Convert height to meters
                val bmi = weight / (heightInMeters * heightInMeters)

                // Determine BMI category and suggestions
                val bmiCategory: String
                val suggestions: String

                when {
                    bmi < 18.5 -> {
                        bmiCategory = "Underweight"
                        suggestions = "You should focus on a balanced diet and healthy eating to gain weight."
                    }
                    bmi >= 18.5 && bmi < 24.9 -> {
                        bmiCategory = "Normal weight"
                        suggestions = "Great job! Maintain a healthy diet and stay active."
                    }
                    bmi >= 25 && bmi < 29.9 -> {
                        bmiCategory = "Overweight"
                        suggestions = "Consider a balanced diet and regular exercise to shed a few pounds."
                    }
                    else -> {
                        bmiCategory = "Obese"
                        suggestions = "You should consult a healthcare provider for advice on weight management."
                    }
                }

                // Show BMI result in a pop-up
                val bmiMessage = "Gender: $selectedGender\nHeight: $height cm\nWeight: $weight kg\n\nBMI: %.2f\nCategory: $bmiCategory\n\nSuggestions: $suggestions"
                    .format(bmi)

                val dialog = AlertDialog.Builder(this)
                    .setTitle("BMI Result")
                    .setMessage(bmiMessage)
                    .setPositiveButton("OK", null)
                    .create()
                dialog.show()
            }



        }
    }
}