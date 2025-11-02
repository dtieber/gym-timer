package com.dti.gymtimer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun GymTimerApp(viewModel: GymTimerViewModel) {
    val remainingTime by viewModel.remainingTime.collectAsState()
    val alarmRinging by viewModel.alarmRinging.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        color = Color(0xFF101010)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (alarmRinging) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFFFC107))
                        .clickable { viewModel.resetTimer() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⏰ Alarm ringing – tap to stop",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Text(
                text = formatTime(remainingTime),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clickable { viewModel.toggleTimer() }
                    .padding(bottom = 40.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(onClick = { viewModel.startTimer(60) }) {
                        Text(
                            "1:00",
                            fontSize = 20.sp
                        )
                    }
                    Button(onClick = { viewModel.startTimer(90) }) {
                        Text(
                            "1:30",
                            fontSize = 20.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(onClick = { viewModel.startTimer(120) }) {
                        Text(
                            "2:00",
                            fontSize = 20.sp
                        )
                    }
                    Button(onClick = { viewModel.addSeconds(10) }) {
                        Text(
                            "+10s",
                            fontSize = 20.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Button(
                        onClick = { viewModel.resetTimer() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Reset", fontSize = 20.sp, color = Color.White)
                    }
                }
            }
        }
    }
}


fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", m, s)
}
