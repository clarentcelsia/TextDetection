package com.example.textdetector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var textRecognizer: TextRecognizer
    lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Build detector
        textRecognizer = TextRecognizer.Builder(applicationContext).build()
        textRecognizer()

    }


    private fun cameraDetection() {
        detector.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {
                cameraSource.stop()
            }

            override fun surfaceCreated(p0: SurfaceHolder?) {
                try {
                    cameraPermission()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

        })
    }

    private fun textRecognizer() {
        if (!textRecognizer.isOperational) {
            Log.d("error detected: ", "Make sure you implement the text detector library")
        } else {
            cameraSource = CameraSource.Builder(applicationContext, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 720)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2F)
                .build()

            cameraDetection()

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                }

                override fun receiveDetections(p0: Detector.Detections<TextBlock>?) {
                    p0?.let {
                        val texts: SparseArray<TextBlock> = p0.detectedItems
                        if (texts.size() != 0) {
                            var stringBuilder = StringBuilder()
                            for (i in 0 until texts.size()) {
                                val item: TextBlock = texts.valueAt(i)
                                stringBuilder.append(item.value)
                                stringBuilder.append("\n")
                            }
                            displayText.text = stringBuilder.toString()
                        }
                    }
                }


            })


        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        cameraPermission()
                    }

                    try {
                        cameraSource.start(detector.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun cameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(Manifest.permission.CAMERA)
                    requestPermissions(permission, PERMISSION_CODE)
            }
            else{
                cameraSource.start(detector.holder)
            }
        }
    }

    companion object {
        const val PERMISSION_CODE = 121

    }
}