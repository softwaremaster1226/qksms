package com.android.QKSMS.feature.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.android.QKSMS.R
import com.android.QKSMS.common.util.Utils
import java.lang.RuntimeException

class RecordFragment: Fragment(), RecordClickListener {
    private lateinit var attaching: RecordClickListener

    private lateinit var mediaRecorder:MediaRecorder
    private var audioSavePathInDevice: String? = null
    private lateinit var record:ImageView
    private lateinit var recording_container:FrameLayout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_recording, container, false)
        record = view.findViewById(R.id.recording)
        recording_container = view.findViewById(R.id.recording_container)
        val animView:View = view.findViewById(R.id.anim)
        val scaleDown: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(
                animView,
                PropertyValuesHolder.ofFloat("scaleX", 1.5f),
                PropertyValuesHolder.ofFloat("scaleY", 1.5f))
        scaleDown.duration = 1000

        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatMode = ObjectAnimator.REVERSE

        recording_container.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    audioSavePathInDevice = Environment.getExternalStorageDirectory().absolutePath + "/Music/" + Utils.getCurrentTimeStamp() + ".mp3"
                    mediaRecorderReady()
                    mediaRecorder.prepare()
                    mediaRecorder.start()
                    scaleDown.start()
                    record.setImageDrawable(resources.getDrawable(R.drawable.ic_stop))
                }
                MotionEvent.ACTION_UP -> {
                    try {
                        mediaRecorder.stop()
                    } catch (e: RuntimeException) {
                        e.printStackTrace()
                    }

                    setRecord(Uri.parse(audioSavePathInDevice))
                    scaleDown.pause()
                    animView.scaleX = 1f
                    animView.scaleY = 1f
                    record.setImageDrawable(resources.getDrawable(R.drawable.ic_record))
                }
                else -> {
                    Log.d("--------------", "Else state")
                }
            }
            return@setOnTouchListener true
        }
        return view
    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        attaching = activity as RecordClickListener
    }

    private fun mediaRecorderReady() {
        if (activity?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) } != PackageManager.PERMISSION_GRANTED) {

            activity?.let { ActivityCompat.requestPermissions(it, Array(1){Manifest.permission.RECORD_AUDIO}, 1) }

        } else {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioSavePathInDevice)
            }
        }
    }

    override fun setRecord(uri: Uri) {
        uri.let {
            attaching.setRecord(it)
        }
    }
}