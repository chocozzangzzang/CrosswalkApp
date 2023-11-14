package com.example.crosswalk.cocotest

import android.Manifest
import android.R
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.crosswalk.databinding.ActivityBeaconBinding
import java.util.UUID


class BeaconActivity : AppCompatActivity() {

    private val TAG = "BleActivity"
    lateinit var binding : ActivityBeaconBinding

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private lateinit var bluetoothGatt: BluetoothGatt
    private lateinit var characteristic1: BluetoothGattCharacteristic
    private lateinit var characteristic2: BluetoothGattCharacteristic
    private lateinit var characteristic3: BluetoothGattCharacteristic
    private val handler = Handler()

    companion object {
        private val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
        private val CHARACTERISTIC_UUID:  UUID = UUID.fromString("b67b46cc-7009-4d7f-84fa-5ea76b78051d")
        private val CHARACTERISTIC_UUID2: UUID = UUID.fromString("c67b46cc-7009-4d7f-84fa-5ea76b78051d")
        private val CHARACTERISTIC_UUID3: UUID = UUID.fromString("d67b46cc-7009-4d7f-84fa-5ea76b78051d")
        private val MY_PERMISSIONS_REQUEST_BLUETOOTH = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeaconBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Bluetooth 권한 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    MY_PERMISSIONS_REQUEST_BLUETOOTH
                )
                binding.tvBluetoothStatus.text = "BLUETOOTH OK"
            } else {
                // BLE 통신 시작
                startBleCommunication()
            }
        } else {
            // BLE 통신 시작
            startBleCommunication()
        }
    }

    private fun startBleCommunication() {
        binding.tvBluetoothStatus.text = "BLE COMMUNICATION OK"
        if (bluetoothAdapter == null || !(bluetoothAdapter!!.isEnabled)) {
            Log.e(TAG, "Bluetooth not enabled")
            return
        }

        val deviceAddress = "00:00:00:00:00:02" // Replace with your BLE device address

        val device = bluetoothAdapter!!.getRemoteDevice(deviceAddress)
        if (ActivityCompat.checkSelfPermission(
                this@BeaconActivity,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.tvBluetoothStatus.text = "BLE COMMUNICATION NOT OK2"
            return
        }
        binding.tvBluetoothStatus.text = "BLE COMMUNICATION OK2"
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected to GATT server.")
                    if (ActivityCompat.checkSelfPermission(
                            this@BeaconActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                characteristic1 = service.getCharacteristic(CHARACTERISTIC_UUID)
                // 여기에서 필요한 BLE 동작 수행
                // 예: 데이터 읽기, 쓰기 등
                // 특성에 대한 알림 활성화
                if (ActivityCompat.checkSelfPermission(
                        this@BeaconActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    binding.tvBluetoothStatus.text = "GET BLE DATA NOT OK"
                    return
                }
                binding.tvBluetoothStatus.text = "GET BLE DATA OK"
                gatt.setCharacteristicNotification(characteristic1, true)
                // 특성의 디스크립터에 알림 활성화
                val descriptor1 = characteristic1.getDescriptor(UUID.fromString("b67b46cc-7009-4d7f-84fa-5ea76b78051d"))
                descriptor1.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                //gatt.writeDescriptor(descriptor1)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            // 특성이 변경되면 이 메서드가 호출됨
            val data = characteristic.value
            // 데이터를 여기에서 처리
            binding.tvBluetoothStatus21.text = String(data)
            Log.i(TAG, "Received data: ${String(data)}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 권한이 허용된 경우 BLE 통신 시작
                    startBleCommunication()
                } else {
                    // 권한이 거부된 경우 사용자에게 설명하거나 다른 조치를 취할 수 있음
                    // 예: 권한이 필요한 이유를 사용자에게 설명하는 다이얼로그 표시
                }
            }
        }
    }
}
