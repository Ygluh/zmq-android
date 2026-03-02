package com.example.android_notes.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.zmq.R
import org.zeromq.SocketType
import org.zeromq.ZContext
import java.nio.charset.StandardCharsets

class SocketsActivity : AppCompatActivity() {
    private val log_tag = "MY_LOG_TAG"
    private lateinit var tvSockets: TextView
    private lateinit var etServerIP: EditText
    private lateinit var btnStartClient: Button
    private lateinit var handler: Handler
    private var isRunning = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sockets)

        tvSockets = findViewById(R.id.tvSockets)
        etServerIP = findViewById(R.id.etServerIP)
        btnStartClient = findViewById(R.id.btnStartClient)
        handler = Handler(Looper.getMainLooper())

        etServerIP.setText("10.0.2.2")

        btnStartClient.setOnClickListener {
            if (!isRunning) {
                startClientThread()
            }
        }
    }

    private fun startClientThread() {
        isRunning = true
        btnStartClient.text = "Running..."
        btnStartClient.isEnabled = false

        Thread {
            val serverIP = etServerIP.text.toString()
            val context = ZContext()
            val socket = context.createSocket(SocketType.REQ)

            try {
                socket.connect("tcp://$serverIP:2223")
                Log.d(log_tag, "[CLIENT] Connected to server: $serverIP:2223")

                handler.post {
                    tvSockets.text = "Connected to $serverIP"
                }

                val request = "Hello from Android!"

                for (i in 1..10) {
                    socket.send(request.toByteArray(StandardCharsets.UTF_8), 0)
                    Log.d(log_tag, "[CLIENT] Sent: $request")

                    val finalI = i
                    handler.post {
                        tvSockets.text = "Sent #$finalI: $request"
                    }

                    val reply = socket.recv(0)
                    val replyString = String(reply, StandardCharsets.UTF_8)  // ✅ Тоже StandardCharsets
                    Log.d(log_tag, "[CLIENT] Received: $replyString")

                    handler.post {
                        tvSockets.append("\nReceived: $replyString")
                    }

                    Thread.sleep(1000)
                }

            } catch (e: Exception) {
                Log.e(log_tag, "[CLIENT] Error: ${e.message}")
                handler.post {
                    tvSockets.text = "Error: ${e.message}"
                }
            } finally {
                socket.close()
                context.close()
                isRunning = false
                handler.post {
                    btnStartClient.text = "Start Client"
                    btnStartClient.isEnabled = true
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}