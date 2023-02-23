package com.jinyx.audiowave

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_wave.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

/**
 * @Author: Pen
 * @Create: 2023-02-23 14:23:12
 * @Signature: 五花马，千金裘；与尔同消万古愁
 */
@RuntimePermissions
class WaveRecordActivity : AppCompatActivity() {

    private companion object {
        private const val SOURCE = MediaRecorder.AudioSource.MIC
        private const val SAMPLE = 16000
        private const val CHANNEL = AudioFormat.CHANNEL_IN_MONO
        private const val FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    private var audioRecord: AudioRecord? = null
    private var minBufferSize: Int = 0
    private var readAudioThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave)

        needPermissionWithPermissionCheck()
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun needPermission() {
        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE, CHANNEL, FORMAT)
        audioRecord = AudioRecord(SOURCE, SAMPLE, CHANNEL, FORMAT, minBufferSize)
        waveView1.clear()
        waveView2.clear()
        loopAudioRead()
    }

    private fun loopAudioRead() {
        readAudioThread = Thread {
            try {
                if (audioRecord != null && audioRecord!!.state == AudioRecord.STATE_INITIALIZED) {
                    audioRecord!!.startRecording()
                    while (audioRecord != null && audioRecord!!.state == AudioRecord.STATE_INITIALIZED
                        && audioRecord!!.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        val buffer = ByteArray(minBufferSize)
                        val readCount = audioRecord!!.read(buffer, 0, minBufferSize)
                        if (readCount == minBufferSize) {
                            feedAudio2WaveView(buffer)
                        } else if (readCount > 0) {
                            feedAudio2WaveView(buffer.copyOfRange(0, readCount))
                        }
                        Thread.sleep(1)
                    }
                } else {
                    toast("录音机启用失败")
                }
            } catch (e: Exception) { }
        }
        readAudioThread!!.start()
    }

    private fun feedAudio2WaveView(audio: ByteArray) {
        runOnUiThread {
            waveView1.feedAudioData(audio)
            waveView2.feedAudioData(audio)
        }
    }

    private fun toast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun onPermissionDenied() {
        toast("没有权限哦！亲~")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onBackPressed() {
        if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord?.stop()
            waveView1?.stop()
            waveView2?.stop()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        audioRecord?.release()
        audioRecord = null
        readAudioThread?.interrupt()
        readAudioThread = null
        super.onDestroy()
    }

}