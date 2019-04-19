package com.urishaked.badgemaster

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Button

import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Logger
import com.vikramezhil.droidspeech.DroidSpeech
import com.vikramezhil.droidspeech.OnDSListener


val magicManufacturerId = 0xf00d;
val colors = mapOf<String, UByteArray>(
    "read" to ubyteArrayOf(0xffu, 0u, 0u),
    "red" to ubyteArrayOf(0xffu, 0u, 0u),
    "green" to ubyteArrayOf(0u, 0xffu, 0u),
    "blue" to ubyteArrayOf(0u, 0u, 0xffu),
    "white" to ubyteArrayOf(0xffu, 0xffu, 0xffu),
    "yellow" to ubyteArrayOf(0xffu, 0xffu, 0u),
    "purple" to ubyteArrayOf(0xffu, 0u, 0xffu)
);

class MainActivity : AppCompatActivity() {
    private val log = Logger.getLogger(MainActivity::class.java.name)
    private lateinit var bluetooth: BluetoothManager
    private val callback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            log.info("Advertising started")
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            log.severe("Failed advertising: " + errorCode);
            super.onStartFailure(errorCode)
        }
    }

    private var advertising = false;

    private fun sendColor(r: UByte, g: UByte, b: UByte) {
        val adv = this.bluetooth.adapter.getBluetoothLeAdvertiser();
        val advData = AdvertiseData.Builder()
            .addManufacturerData(magicManufacturerId, ubyteArrayOf(r, g, b).asByteArray())
            .build();

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build();

        if (this.advertising) {
            adv.stopAdvertising(callback);
        }
        adv.startAdvertising(settings, advData, null, callback);
        this.advertising = true;
    }

    private fun updateColor(colorStr: String) {
        log.info("Updating color, input=" + colorStr)
        for (candidate in colors.keys) {
            if (colorStr.toLowerCase().split(' ', ',', '.').contains(candidate)) {
                val color = colors.get(candidate);
                if (color != null) {
                    log.info("Set color to: " + color);
                    sendColor(color[0], color[1], color[2]);
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        bluetooth = getSystemService(android.content.Context.BLUETOOTH_SERVICE) as BluetoothManager;

        findViewById<Button>(R.id.buttonRed).setOnClickListener { view ->
            this.sendColor(255u, 0u, 0u)
        }

        findViewById<Button>(R.id.buttonGreen).setOnClickListener { view ->
            this.sendColor(0u, 255u, 0u)
        }

        findViewById<Button>(R.id.buttonBlue).setOnClickListener { view ->
            this.sendColor(0u, 0u, 255u)
        }

        findViewById<Button>(R.id.buttonBlack).setOnClickListener { view ->
            this.sendColor(0u, 0u, 0u)
        }

        val droidSpeech = DroidSpeech(this, null)
        droidSpeech.setOnDroidSpeechListener(object : OnDSListener {
            override fun onDroidSpeechSupportedLanguages(
                currentSpeechLanguage: String?,
                supportedSpeechLanguages: MutableList<String>?
            ) {
            }

            override fun onDroidSpeechError(errorMsg: String?) {
                log.severe("Speech recognition failed: " + errorMsg);
            }

            override fun onDroidSpeechClosedByUser() {
                log.info("Speech Closed by user");
            }

            override fun onDroidSpeechLiveResult(liveSpeechResult: String?) {
                if (liveSpeechResult != null) {
                    updateColor(liveSpeechResult);
                }
            }

            override fun onDroidSpeechFinalResult(finalSpeechResult: String?) {
                if (finalSpeechResult != null) {
                    updateColor(finalSpeechResult);
                }
            }

            override fun onDroidSpeechRmsChanged(rmsChangedValue: Float) {
            }
        })
        droidSpeech.startDroidSpeechRecognition();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
