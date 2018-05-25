/*
 * Created by Android Developer Medan on 5/25/18 11:47 AM
 * Copyright (c) 2018. All rights reserved.
 *
 * Last modified 5/25/18 11:47 AM
 */

package com.ysn.exampletextrecognition

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.SurfaceHolder
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private lateinit var mCameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startCameraSource()
    }

    private fun startCameraSource() {
        // Create the TextRecognizer
        val textRecognizer = TextRecognizer.Builder(this).build()
        if (!textRecognizer.isOperational) {
            Log.d(TAG, "Detector dependencies not loaded yet")
        } else {
            // Initialize camerasource to use high resolution and set AutoFocus on.
            mCameraSource = CameraSource.Builder(this, textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(2.0f)
                    .build()

            /**
             * Add call back to SurfaceView and check if camera a permission is granted.
             * If permission is granted we can start our camerasource and pass it to surfaceview
             */
            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
                    try {
                        if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), 1)
                            return
                        }
                        mCameraSource.start(surfaceView.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
                    /* nothing to do in here */
                }

                override fun surfaceDestroyed(surfaceHolder: SurfaceHolder?) {
                    mCameraSource.stop()
                }
            })

            // Set the TextRecognizer's Processor
            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {
                override fun release() {
                    /* nothing to do in here */
                }

                override fun receiveDetections(detections: Detector.Detections<TextBlock>?) {
                    val items = detections?.detectedItems
                    if (items?.size() != 0) {
                        text_view.post {
                            val stringBuilder = StringBuilder()
                            for (a in 0 until items?.size()!!) {
                                val item = items.valueAt(a)
                                stringBuilder.append(item.value)
                                stringBuilder.append("\n")
                            }
                            text_view.text = stringBuilder.toString()
                        }
                    }
                }

            })
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != 1) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                mCameraSource.start(surfaceView.holder)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}
