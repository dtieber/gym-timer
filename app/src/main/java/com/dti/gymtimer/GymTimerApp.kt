package com.dti.gymtimer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dti.gymtimer.ui.theme.AppDanger
import com.dti.gymtimer.ui.theme.AppDisabledContainer
import com.dti.gymtimer.ui.theme.AppDisabledContent
import com.dti.gymtimer.ui.theme.AppMuted
import com.dti.gymtimer.ui.theme.AppOnSurface
import com.dti.gymtimer.ui.theme.AppPrimary
import com.dti.gymtimer.ui.theme.AppSuccess
import com.dti.gymtimer.ui.theme.AppSuccessContainer
import com.dti.gymtimer.ui.theme.AppSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GymTimerApp(viewModel: GymTimerViewModel) {
    val remainingTime by viewModel.remainingTime.collectAsState()
    val alarmRinging by viewModel.alarmRinging.collectAsState()
    val currentSet by viewModel.currentSet.collectAsState()
    val workoutStartMs by viewModel.workoutStartTimeMs.collectAsState()

    val showStopConfirm = remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 56.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top area: set chips
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..5).forEach { set ->
                        val isCurrent = currentSet == set
                        val isNext = currentSet != null && (currentSet!! % 5 + 1) == set

                        val bgColor = when {
                            isCurrent -> AppSuccess
                            isNext -> AppSuccessContainer
                            else -> Color.DarkGray
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 56.dp, height = 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .clickable { viewModel.selectSet(set) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$set",
                                color = AppOnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            // Middle area: timer, alarm banner directly under timer, and quick controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleTimer() }
                        .padding(bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatTimeMS(remainingTime),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppOnSurface,
                        textAlign = TextAlign.Center
                    )
                }

                if (alarmRinging) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(color = Color(0xFFFFC107))
                            .zIndex(1f)
                            .clickable { viewModel.resetTimer() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "⏰ Alarm ringing — tap to stop",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = if (remainingTime > 0) "Tap to pause/resume" else "Tap to start",
                    fontSize = 12.sp,
                    color = AppMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallTimerButton("1:00") { viewModel.startTimer(60) }
                        SmallTimerButton("1:30") { viewModel.startTimer(90) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallTimerButton("2:00") { viewModel.startTimer(120) }
                        SmallTimerButton("+10s") { viewModel.addSeconds(10) }
                    }
                    Row {
                        Button(
                            onClick = { viewModel.resetTimer() },
                            colors = ButtonDefaults.buttonColors(containerColor = AppDanger)
                        ) {
                            Text("Reset", color = AppOnSurface)
                        }
                    }
                }
            }

            // Bottom area: workout start + start/stop buttons side-by-side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 36.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Workout start",
                        fontSize = 12.sp,
                        color = AppMuted
                    )
                    Text(
                        text = formatClockTime(workoutStartMs),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppOnSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                val isSet = workoutStartMs != null
                val btnWidth = 96.dp

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.setWorkoutStartTimeToNow() },
                        enabled = !isSet,
                        modifier = Modifier.width(btnWidth),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isSet) AppPrimary else AppDisabledContainer,
                            contentColor = if (!isSet) AppOnSurface else AppDisabledContent
                        )
                    ) {
                        Text("Start")
                    }

                    Button(
                        onClick = { showStopConfirm.value = true },
                        enabled = isSet,
                        modifier = Modifier.width(btnWidth),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSet) AppDanger else AppDisabledContainer,
                            contentColor = if (isSet) AppOnSurface else AppDisabledContent
                        )
                    ) {
                        Text("Stop")
                    }
                }
            }
        }

        if (showStopConfirm.value) {
            AlertDialog(
                onDismissRequest = { showStopConfirm.value = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.resetWorkoutStartTime()
                        showStopConfirm.value = false
                    }) {
                        Text("Yes", color = AppOnSurface)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStopConfirm.value = false }) {
                        Text("No", color = AppMuted)
                    }
                },
                title = { Text("Stop workout?", color = AppOnSurface) },
                text = {
                    Text(
                        "Are you sure you want to stop and reset the workout start time?",
                        color = AppMuted
                    )
                },
                containerColor = Color(0xFF212121),
                tonalElevation = 4.dp
            )
        }
    }
}

@Composable
private fun SmallTimerButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(44.dp)
            .defaultMinSize(minWidth = 100.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
    ) {
        Text(label, color = AppOnSurface)
    }
}

fun formatTimeMS(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format(Locale.ENGLISH, "%02d:%02d", m, s)
}

fun formatClockTime(ms: Long?): String {
    if (ms == null) return "--:--:--"
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(ms))
}
