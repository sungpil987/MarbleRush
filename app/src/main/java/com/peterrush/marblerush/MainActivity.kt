package com.peterrush.marblerush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashFile = java.io.File(filesDir, "last_crash.txt")
        val previousCrash = if (crashFile.exists()) crashFile.readText() else null
        setContent {
            if (!previousCrash.isNullOrBlank()) CrashReportScreen(previousCrash)
            else MarbleRushApp()
        }
    }
}

private data class Runner(val name: String, val color: Color, var x: Float, var y: Float, var speed: Float)

@Composable
private fun MarbleRushApp() {
    var seconds by remember { mutableIntStateOf(30) }
    var names by remember { mutableStateOf((1..12).joinToString(", ") { "MARBLE%02d".format(it) }) }
    var runners by remember { mutableStateOf(emptyList<Runner>()) }
    var active by remember { mutableStateOf(false) }
    var complete by remember { mutableStateOf(false) }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var winner by remember { mutableStateOf("") }
    val colors = listOf(Color(0xFFFF5D73), Color(0xFF42D6FF), Color(0xFFFFD166), Color(0xFF9B7BFF), Color(0xFF52E3A4), Color(0xFFFF9F43), Color(0xFFFF78C8), Color(0xFFB8E34B), Color(0xFF6C8CFF), Color(0xFFFF795E), Color(0xFF5DE0D0), Color(0xFFE5E7EB))

    fun start() {
        val list = names.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(16).ifEmpty { (1..12).map { "MARBLE%02d".format(it) } }
        runners = list.mapIndexed { i, n -> Runner(n.take(12), colors[i % colors.size], Random.nextFloat() * .82f + .08f, .04f + Random.nextFloat() * .05f, Random.nextFloat() * .25f + .22f) }
        elapsed = 0f
        winner = ""
        complete = false
        active = true
    }

    LaunchedEffect(active) {
        while (active && elapsed < seconds) {
            delay(40)
            elapsed = (elapsed + .04f).coerceAtMost(seconds.toFloat())
            runners.forEach { r ->
                r.y += r.speed * .04f
                r.x += (Random.nextFloat() - .5f) * .055f
                if (r.x < .06f) r.x = .06f
                if (r.x > .94f) r.x = .94f
                if (Random.nextFloat() < .08f) r.speed = (r.speed + Random.nextFloat() * .12f).coerceAtMost(.7f)
            }
            if (runners.all { it.y >= .98f }) elapsed = seconds.toFloat()
        }
        if (active) {
            winner = runners.maxByOrNull { it.y }?.name ?: ""
            active = false
            complete = true
        }
    }

    Column(Modifier.fillMaxSize().background(Color(0xFF090D1A)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("MARBLE RUSH", color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Black)
        Text("WHO WILL SURVIVE THE RUN?", color = Color(0xFF8FA4C8), fontSize = 11.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60).forEach { t ->
                OutlinedButton(onClick = { if (!active) seconds = t }, modifier = Modifier.weight(1f)) {
                    Text(if (t == 30) "30초 스프린트" else "60초 챔피언십", color = if (seconds == t) Color(0xFFFFD166) else Color.White)
                }
            }
        }
        Text("참가자 이름 (쉼표로 구분, 최대 16명)", color = Color(0xFFB7C5DF), fontSize = 12.sp)
        OutlinedTextField(value = names, onValueChange = { names = it }, modifier = Modifier.fillMaxWidth(), enabled = !active, minLines = 2)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (active) "RACE IN PROGRESS" else if (complete) "RACE COMPLETE" else "RACE TRACK", color = Color.White, fontWeight = FontWeight.Bold)
            Text("${(seconds - elapsed).coerceAtLeast(0f).toInt()}s", color = Color(0xFFFFD166), fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        Box(Modifier.weight(1f).fillMaxWidth().background(Color(0xFF111B30), RoundedCornerShape(18.dp))) {
            Canvas(Modifier.fillMaxSize().padding(8.dp)) {
                drawLine(Color(0xFF263957), Offset(size.width * .04f, 0f), Offset(size.width * .04f, size.height), 8f)
                drawLine(Color(0xFF263957), Offset(size.width * .96f, 0f), Offset(size.width * .96f, size.height), 8f)
                for (i in 1..12) drawLine(Color(0x223B5278), Offset(0f, size.height * i / 13f), Offset(size.width, size.height * i / 13f), 1f)
                runners.forEach { r ->
                    val p = Offset(size.width * r.x, size.height * r.y.coerceAtMost(1f))
                    drawCircle(Color.Black.copy(alpha = .35f), size.width * .026f, Offset(p.x + 2f, p.y + 3f))
                    drawCircle(r.color, size.width * .022f, p)
                    drawCircle(Color.White.copy(alpha = .8f), size.width * .007f, Offset(p.x - size.width * .006f, p.y - size.width * .007f))
                }
            }
            if (runners.isEmpty()) Text("READY, SET, ROLL!", color = Color(0xFF8FA4C8), modifier = Modifier.align(Alignment.Center))
        }
        Button(onClick = { if (active) active = false else start() }, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6757E8)), shape = RoundedCornerShape(14.dp)) {
            Text(if (active) "경기 중단" else if (complete) "다시 레이스!" else "레이스 시작!", fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        if (complete) Text("🏆 WINNER  $winner", color = Color(0xFFFFD166), fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun CrashReportScreen(report: String) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF090D1A)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MARBLE RUSH 진단 화면", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text("이전 실행에서 오류가 발생했어. 아래 내용을 캡처해서 보내줘. 오류 원인을 확인해 고칠게.", color = Color(0xFFFFD166))
        SelectionContainer {
            Text(report, color = Color(0xFFE5E7EB), fontSize = 11.sp, modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()))
        }
    }
}
