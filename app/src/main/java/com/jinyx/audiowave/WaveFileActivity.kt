package com.jinyx.audiowave

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_wave.*

/**
 * @Author: Pen
 * @Create: 2023-02-23 14:23:12
 * @Signature: 五花马，千金裘；与尔同消万古愁
 */
class WaveFileActivity : AppCompatActivity() {

    private var readAudioThread: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave)

        feedAudioFile2WaveView()
    }

    private fun feedAudioFile2WaveView() {
        waveView1?.clear()
        waveView2?.clear()
        readAudioThread = Thread {
            try {
                val inputStream = assets.open("必杀技.wav")
                val waveCount = waveView1.waveCount
                val audioLen = inputStream.available()
                // 音频流一般是2个字节表示一个采样点
                val bufferStep = audioLen / waveCount / 2 * 2
                val buffer = ByteArray(bufferStep)
                var len: Int
                while ((inputStream.read(buffer).also { len = it }) != -1) {
                    if (len == bufferStep) {
                        // feed 的 数据，此处需要 copy 出来；避免线程调度，未绘制的时候；read 下一帧数据造成 buffer 中的内容改变，导致绘制错乱
                        feedAudio2WaveView(buffer.copyOfRange(0, buffer.size))
                    } else if (len > 0) {
                        feedAudio2WaveView(buffer.copyOfRange(0, len))
                    }
                    Thread.sleep(1)
                }
                runOnUiThread {
                    waveView1?.stop()
                    waveView2?.stop()
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

    override fun onDestroy() {
        readAudioThread?.interrupt()
        super.onDestroy()
    }

}