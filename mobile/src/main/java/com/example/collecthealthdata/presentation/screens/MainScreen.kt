package com.example.collecthealthdata.presentation.screens

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.collecthealthdata.presentation.activity.AuthActivity
import com.example.collecthealthdata.presentation.activity.UserInputActivity
import com.example.collecthealthdata.data.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.jvm.java

@Composable
fun MainScreen(context: Context, user: User) {
    var isRecording by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // 전체 패딩 추가
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween // 왼쪽 & 오른쪽 정렬
        ) {
            // 왼쪽: 로그아웃 & 계정 삭제 버튼
            Row {
                Button(onClick = {
                    unDefinedFeature(context, "로그아웃")
                    //FirebaseAuth.getInstance().signOut()
                    //val intent = Intent(context, AuthActivity::class.java)
                    //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    //context.startActivity(intent)
                }) {
                    Text("로그아웃")
                }
                Spacer(modifier = Modifier.width(8.dp)) // 버튼 간격
                Button(onClick = {
                    unDefinedFeature(context,"계정 삭제")
                    /*val user = FirebaseAuth.getInstance().currentUser

                    user?.delete()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("AccountDelete", "계정 삭제 성공")

                                // SharedPreferences 초기화 (있다면)
                                val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                prefs.edit().clear().apply()

                                // 로그인 화면으로 이동
                                val intent = Intent(context, AuthActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            } else {
                                Log.w("AccountDelete", "계정 삭제 실패", task.exception)
                                Toast.makeText(context, "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }*/
                }) {
                    Text("계정 삭제")
                }
            }

        }

        // 화면 중앙 버튼들
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 50.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                val intent = Intent(context, UserInputActivity::class.java).apply {
                    putExtra("USER",user)
                }
                context.startActivity(intent)
            }) { Text("개인 정보 입력하기") }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!isRecording) {
                        isRecording = true
                        coroutineScope.launch {
                            recordWavFor10Seconds(context)
                            isRecording = false
                        }
                    }
                },
                enabled = !isRecording
            ) {
                Text(if (isRecording) "녹음 중..." else "10초간 녹음하기")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { /* 버튼 3 클릭 */ }) { Text("Button 3") }
        }
    }
}

fun unDefinedFeature(context:Context, text: String){
    Toast.makeText(context, "현재 $text 기능 비활성화중입니다. 개발자에게 문의해주세요",Toast.LENGTH_SHORT).show()
}

suspend fun recordWavFor10Seconds(context: Context): File = withContext(Dispatchers.IO) {
    val sampleRate = 44100
    val channelConfig = AudioFormat.CHANNEL_IN_MONO
    val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
    val audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate, channelConfig, audioFormat,
        bufferSize
    )

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "recorded_$timestamp.wav"
    val wavFile = File(context.cacheDir, fileName)
    val rawData = ByteArrayOutputStream()

    val buffer = ByteArray(bufferSize)
    audioRecord.startRecording()

    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < 10_000) {
        val read = audioRecord.read(buffer, 0, buffer.size)
        if (read > 0) rawData.write(buffer, 0, read)
    }

    audioRecord.stop()
    audioRecord.release()

    // WAV 파일로 저장
    saveAsWav(wavFile, rawData.toByteArray(), sampleRate, 1)

    Log.d("AudioRecorder", "WAV 저장 완료: ${wavFile.name}")
    return@withContext wavFile
}

fun saveAsWav(file: File, audioData: ByteArray, sampleRate: Int, channels: Int) {
    val totalDataLen = audioData.size + 36
    val byteRate = sampleRate * channels * 2

    val out = FileOutputStream(file)
    val header = ByteArray(44)

    // RIFF/WAVE header
    header[0] = 'R'.code.toByte()
    header[1] = 'I'.code.toByte()
    header[2] = 'F'.code.toByte()
    header[3] = 'F'.code.toByte()
    writeInt(header, 4, totalDataLen)
    header[8] = 'W'.code.toByte()
    header[9] = 'A'.code.toByte()
    header[10] = 'V'.code.toByte()
    header[11] = 'E'.code.toByte()
    header[12] = 'f'.code.toByte()
    header[13] = 'm'.code.toByte()
    header[14] = 't'.code.toByte()
    header[15] = ' '.code.toByte()
    writeInt(header, 16, 16) // PCM header length
    writeShort(header, 20, 1.toShort()) // audio format (1 = PCM)
    writeShort(header, 22, channels.toShort())
    writeInt(header, 24, sampleRate)
    writeInt(header, 28, byteRate)
    writeShort(header, 32, (channels * 2).toShort()) // block align
    writeShort(header, 34, 16.toShort()) // bits per sample
    header[36] = 'd'.code.toByte()
    header[37] = 'a'.code.toByte()
    header[38] = 't'.code.toByte()
    header[39] = 'a'.code.toByte()
    writeInt(header, 40, audioData.size)

    out.write(header)
    out.write(audioData)
    out.close()
}

fun writeInt(b: ByteArray, offset: Int, value: Int) {
    b[offset] = value.toByte()
    b[offset + 1] = (value shr 8).toByte()
    b[offset + 2] = (value shr 16).toByte()
    b[offset + 3] = (value shr 24).toByte()
}

fun writeShort(b: ByteArray, offset: Int, value: Short) {
    b[offset] = value.toByte()
    b[offset + 1] = (value.toInt() shr 8).toByte()
}
