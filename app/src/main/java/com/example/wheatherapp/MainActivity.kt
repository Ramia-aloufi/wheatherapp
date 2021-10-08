package com.example.wheatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var city = "Riyadh"
    var key = "d6d9e9befc1a0da26da340416425192e"
    private lateinit var errorButton: Button
    private lateinit var rlZip: RelativeLayout
    private lateinit var etZip: EditText
    private lateinit var btZip: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        floatingActionButton.setOnClickListener {
            this.recreate()
        }

        errorButton = findViewById(R.id.btError)

        errorButton.setOnClickListener {
            city = "Medinah"
            requestAPI()
        }

        rlZip = findViewById(R.id.rlZip)
        etZip = findViewById(R.id.etZip)
        btZip = findViewById(R.id.btZip)
        btZip.setOnClickListener {
            city = etZip.text.toString();
            etZip.text.clear()

            // Hide Keyboard
            val imm = ContextCompat.getSystemService(this, InputMethodManager::class.java)
            imm?.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
            rlZip.isVisible = false
        }
        requestAPI()
    }

    private fun requestAPI(){
        println("CITY: $city")
        CoroutineScope(IO).launch {
            updateStatus(-1)
            val data = async {
                fetchWeatherData()
            }.await()
            if(data.isNotEmpty()){
                updateWeatherData(data)
                updateStatus(0)
            }else{
                updateStatus(1)
            }
        }
    }

    private suspend fun updateWeatherData(result: String){
        withContext(Main){
            val jsonObj = JSONObject(result)
            val main = jsonObj.getJSONObject("main")
            val sys = jsonObj.getJSONObject("sys")
            val wind = jsonObj.getJSONObject("wind")
            val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
            val lastUpdate:Long = jsonObj.getLong("dt")
            val lastUpdateText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(lastUpdate*1000))
            val currentTemperature = main.getString("temp")
            val temp = try{
                currentTemperature.substring(0, currentTemperature.indexOf(".")) + "°C"
            }catch(e: Exception){
                currentTemperature + "°C"
            }
            val minTemperature = main.getString("temp_min")
            val tempMin = "Low: " + minTemperature.substring(0, minTemperature.indexOf("."))+"°C"
            val maxTemperature = main.getString("temp_max")
            val tempMax = "High: " + maxTemperature.substring(0, maxTemperature.indexOf("."))+"°C"
            val pressure = main.getString("pressure")
            val humidity = main.getString("humidity")

            val sunrise:Long = sys.getLong("sunrise")
            val sunset:Long = sys.getLong("sunset")
            val windSpeed = wind.getString("speed")
            val weatherDescription = weather.getString("description")

            val address = jsonObj.getString("name")+", "+sys.getString("country")

           addr.text = address
            addr.setOnClickListener {
                rlZip.isVisible = false
            }
            weatherDe.text =  lastUpdateText
            whethdes.text = weatherDescription.capitalize(Locale.getDefault())
            tem.text = temp
            tempMi.text = tempMin
            tempMa.text = tempMax
            sunrisetime.text = SimpleDateFormat("hh:mm a",Locale.ENGLISH).format(Date(sunrise*1000))
            sunsettime.text = SimpleDateFormat("hh:mm a",Locale.ENGLISH).format(Date(sunset*1000))
            tvwind.text = windSpeed
            tvpressure.text = pressure
            humiditytime.text = humidity
            refreshicon.setOnClickListener { requestAPI() }
        }
    }

    private fun fetchWeatherData(): String{
        var response = ""
        try {
            response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$key")
                .readText(Charsets.UTF_8)
        }catch (e: Exception){
            println("Error:fetchWeatherData $e")
        }
        return response
    }

    private suspend fun updateStatus(state: Int){
//        states: -1 = loading, 0 = loaded, 1 = error
        withContext(Main){
            when{
                state < 0 -> {
                    pbProgress.visibility = View.VISIBLE
                    rlMain.visibility = View.GONE
                   llErrorContainer.visibility = View.GONE
                }
                state == 0 -> {
                    pbProgress.visibility = View.GONE
                    rlMain.visibility = View.VISIBLE
                }
                state > 0 -> {
                    pbProgress.visibility = View.GONE
                    llErrorContainer.visibility = View.VISIBLE
                }
            }
        }
    }


}