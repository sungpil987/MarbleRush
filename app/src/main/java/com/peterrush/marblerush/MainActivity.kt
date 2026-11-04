package com.peterrush.marblerush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MarbleRushApp() }
    }
}

private data class Runner(val name: String, val color: Color, val x: Float, val y: Float, val speed: Float)
private data class Peg(val x: Float, val y: Float)

@Composable
private fun MarbleRushApp() {
    var seconds by remember { mutableIntStateOf(30) }
    var names by remember { mutableStateOf((1..12).joinToString(", ") { "MARBLE%02d".format(it) }) }
    var runners by remember { mutableStateOf(emptyList<Runner>()) }
    var active by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var winner by remember { mutableStateOf("") }
    val colors = listOf(Color(0xFFFF5D73), Color(0xFF42D6FF), Color(0xFFFFD166), Color(0xFF9B7BFF), Color(0xFF52E3A4), Color(0xFFFF9F43), Color(0xFFFF78C8), Color(0xFFB8E34B), Color(0xFF6C8CFF), Color(0xFFFF795E), Color(0xFF5DE0D0), Color(0xFFE5E7EB), Color(0xFFEC6A35), Color(0xFF8ED1FC), Color(0xFFC084FC), Color(0xFFF472B6))
    val pegs = remember { (0 until 9).map { i -> Peg(if (i % 2 == 0) 0.34f else 0.66f, 0.13f + i * 0.092f) } + listOf(Peg(0.5f, 0.31f), Peg(0.5f, 0.68f)) }

    fun startRace() {
        val list = names.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(16).ifEmpty { (1..12).map { "MARBLE%02d".format(it) } }
        runners = list.mapIndexed { i, name ->
            Runner(name.take(14), colors[i % colors.size], 0.12f + (i % 8) * 0.108f, 0.035f + Random.nextFloat() * 0.025f, Random.nextFloat() * 0.012f + 0.024f)
        }
        elapsed = 0f
        winner = ""
        complete = false
        active = true
    }

    LaunchedEffect(active) {
        if (active) {
            var time = 0f
            while (active && time < seconds) {
                delay(50)
                time = (time + 0.05f).coerceAtMost(seconds.toFloat())
                elapsed = time
                runners = runners.map { marble ->
                    if (marble.y >= 0.97f) marble else {
                        val peg = pegs.firstOrNull { abs(marble.y - it.y) < 0.025f && abs(marble.x - it.x) < 0.075f }
                        val drift = (Random.nextFloat() - 0.5f) * 0.018f
                        val bounce = if (peg != null) { if (marble.x <= peg.x) -0.055f else 0.055f } else 0f
                        val nextX = (marble.x + drift + bounce).coerceIn(0.07f, 0.93f)
                        val nextY = (marble.y + marble.speed * 0.05f).coerceAtMost(0.97f)
                        val nextSpeed = if (peg != null) (marble.speed * 0.985f).coerceAtLeast(0.018f) else marble.speed
                        marble.copy(x = nextX, y = nextY, speed = nextSpeed)
                    }
                }
                if (runners.isNotEmpty() && runners.all { it.y >= 0.97f }) break
            }
            if (active) {
                winner = runners.maxByOrNull { it.y }?.name ?: ""
                active = false
                complete = true
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF090D1A)).safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("MARBLE RUSH", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Black)
        Text("WHO WILL SURVIVE THE RUN?", color = Color(0xFF8FA4C8), fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60).forEach { t ->
                OutlinedButton(onClick = { if (!active) seconds = t }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = if (seconds == t) Color(0xFFFFD166) else Color.White)) {
                    Text(if (t == 30) "30초 스프린트" else "60초 챔피언십", fontWeight = FontWeight.Bold)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("참가자 이름", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = names, onValueChange = { names = it }, modifier = Modifier.fillMaxWidth(),
                enabled = !active, minLines = 2, maxLines = 3,
                placeholder = { Text("예: 피터, 철수, 영희", color = Color(0xFF9AAAC4)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 14.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White, disabledTextColor = Color(0xFFB7C5DF),
                    focusedBorderColor = Color(0xFF8B7CFF), unfocusedBorderColor = Color(0xFF66738D), disabledBorderColor = Color(0xFF66738D),
                    cursorColor = Color(0xFFFFD166), focusedContainerColor = Color(0xFF18243A),
                    unfocusedContainerColor = Color(0xFF18243A), disabledContainerColor = Color(0xFF18243A)
                ),
                shape = RoundedCornerShape(12.dp)
            )
            Text("쉼표(,)로 구분 · 최대 16명", color = Color(0xFF8FA4C8), fontSize = 11.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(if (active) "RACE IN PROGRESS" else if (complete) "RACE COMPLETE" else "RACE TRACK", color = Color.White, fontWeight = FontWeight.Bold)
            Text(((seconds - elapsed).coerceAtLeast(0f).toInt()).toString() + "s", color = Color(0xFFFFD166), fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111B30), RoundedCornerShape(18.dp))) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                val ballRadius = size.width * 0.026f
                drawLine(Color(0xFF263957), Offset(size.width * 0.04f, 0f), Offset(size.width * 0.04f, size.height), 8f)
                drawLine(Color(0xFF263957), Offset(size.width * 0.96f, 0f), Offset(size.width * 0.96f, size.height), 8f)
                for (i in 1..12) {
                    val y = size.height * i / 13f
                    drawLine(Color(0x223B5278), Offset(0f, y), Offset(size.width, y), 1f)
                }
                drawLine(Color(0xFFFFD166), Offset(0f, size.height * 0.975f), Offset(size.width, size.height * 0.975f), 3f)
                pegs.forEach { peg ->
                    val p = Offset(size.width * peg.x, size.height * peg.y)
                    drawCircle(Color(0xFF526887), ballRadius * 0.78f, p)
                    drawCircle(Color(0xFF9FB4D2), ballRadius * 0.34f, Offset(p.x - 2f, p.y - 2f))
                }
                runners.forEach { marble ->
                    val p = Offset(size.width * marble.x, size.height * marble.y)
                    drawCircle(Color.Black.copy(alpha = 0.4f), ballRadius * 1.12f, Offset(p.x + 2f, p.y + 3f))
                    drawCircle(marble.color, ballRadius, p)
                    drawCircle(Color.White.copy(alpha = 0.85f), ballRadius * 0.32f, Offset(p.x - ballRadius * 0.3f, p.y - ballRadius * 0.3f))
                }
            }
            if (runners.isEmpty()) Text("READY, SET, ROLL!", color = Color(0xFF8FA4C8), modifier = Modifier.align(Alignment.Center))
        }
        Button(onClick = { if (active) active = false else startRace() }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6757E8)), shape = RoundedCornerShape(14.dp)) {
            Text(if (active) "경기 중단" else if (complete) "다시 레이스!" else "레이스 시작!", fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        if (complete) Text("🏆 WINNER  " + winner, color = Color(0xFFFFD166), fontSize = 20.sp, fontWeight = FontWeight.Black)
    }
}
