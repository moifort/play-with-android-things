package me.mottet.things

import android.app.Activity
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class MainActivity : Activity(), SensorEventListener {
    private var count = 0

    private lateinit var sensor: Bmx280SensorDriver

    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance()

        val mDisplay = RainbowHat.openDisplay()
        mDisplay.setEnabled(true)
        mDisplay.display("TIBO")

        val mLedstrip = RainbowHat.openLedStrip()
        mLedstrip.brightness = 0
        mLedstrip.brightness = 15
        val colors = IntArray(7)
        Arrays.fill(colors, Color.BLACK)
        mLedstrip.write(colors)
        mLedstrip.write(colors)

        sensor = RainbowHat.createSensorDriver()
        // Register the drivers with the framework
        sensor.registerTemperatureSensor()
        sensor.registerPressureSensor()
        sensor.registerHumiditySensor()
    }

    override fun onStart() {
        super.onStart()
        Thread.sleep(1000)
        val sensorManager = getSystemService(SensorManager::class.java)
        val temperature = sensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE)[0]
        sensorManager.registerListener(this, temperature, SensorManager.SENSOR_DELAY_NORMAL)
        val pressure = sensorManager.getDynamicSensorList(Sensor.TYPE_PRESSURE)[0]
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL)
        val humdity = sensorManager.getDynamicSensorList(Sensor.TYPE_RELATIVE_HUMIDITY)[0]
        sensorManager.registerListener(this, humdity, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensor.unregisterHumiditySensor()
        sensor.unregisterPressureSensor()
        sensor.unregisterTemperatureSensor()
        sensor.close()
    }

    override fun onAccuracyChanged(p0: Sensor, p1: Int) {
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (count++ < 100) return
        count = 0
        val value = event.values[0]
        when {
            event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                database.reference.child("weather").child("temperature").setValue(Temperature(value.toDouble(), System.currentTimeMillis()))
            }
//            event.sensor.type == Sensor.TYPE_PRESSURE -> Log.e("TIBO", "pressure $value")
//            event.sensor.type == Sensor.TYPE_RELATIVE_HUMIDITY -> Log.e("TIBO", "humdity $value")
        }

    }

}

data class Temperature(val value: Double, val timestamp: Long)
