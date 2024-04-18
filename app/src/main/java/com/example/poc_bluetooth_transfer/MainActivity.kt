package com.example.poc_bluetooth_transfer
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.poc_bluetooth_transfer.databinding.ActivityMainBinding
import java.util.UUID


class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding

    // constants
    private val LOG_TAG_NAME: String = "LOCAL_TEST"
    private val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // default well-known SSP UUID

    // intent request codes
    private val REQUEST_ENABLE_BT: Int = 1
    private val REQUEST_BT_CONNECT: Int = 2

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root);

        binding.btnEnableBluetooth.setOnClickListener {
            enableBluetooth()
        }

        binding.btnDiscoverDevices.setOnClickListener {
            makeDiscoverable()
        }

        binding.btnScanDevices.setOnClickListener {
            scanDevices()
        }

        binding.btnGetPairedDevices.setOnClickListener {
            getPairedDevices()
        }

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        // request runtime permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BT_CONNECT)
            }
        }

        // init BT adapter
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }


    @SuppressLint("MissingPermission")
    private fun enableBluetooth() {
        // Enable the Bluetooth device
        if (bluetoothAdapter?.isEnabled == true) {
            Toast.makeText(this, "Bluetooth is already enabled!", Toast.LENGTH_SHORT).show()
            return
        }

        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        updateBluetoothStatus(true)
    }

    @SuppressLint("MissingPermission")
    private fun makeDiscoverable() {
        if (!bluetoothAdapter?.isDiscovering!!) {
            Toast.makeText(this, "Making the device discoverable...", Toast.LENGTH_SHORT).show()
            val requestCode = 1;
            val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
            startActivityForResult(discoverableIntent, requestCode)
        }

    }

    @SuppressLint("MissingPermission")
    private fun scanDevices() {
        Log.d("[Custom log]", "Starting to scan devices...")
        bluetoothAdapter?.startDiscovery()
        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)

        registerReceiver(receiver, filter)

    }

    @SuppressLint("MissingPermission")
    private fun getPairedDevices() {
        if (bluetoothAdapter?.isEnabled == true) {
            binding.textLog.text = "Paired devices:"
            val devices = bluetoothAdapter!!.bondedDevices
            for (device in devices) {
                binding.textLog.append("\nDevice: $device.name, $device")
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            Log.d("[Custom log]", "Action: $action")
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    if (deviceName != null) {
                        Log.d("[Custom log]", "Device name: $deviceName")
                    }
                    if (deviceHardwareAddress != null) {
                        Log.d("[Custom log]", "Device mac address: $deviceHardwareAddress")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("[Custom log]", "Discovery has started")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("[Custom log]", "Discovery finished")
                }
            }
        }
    }

    private fun updateBluetoothStatus(status: Boolean) {
        if (status) {
            binding.img.setImageResource(R.drawable.ic_bluetooth_on)
            binding.textStatus.setText("Bluetooth is enabled")
        } else {
            binding.img.setImageResource(R.drawable.ic_bluetooth_off)
            binding.textStatus.setText("Bluetooth is disabled")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver)
    }



}