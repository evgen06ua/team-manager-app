package com.example.teammanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teammanager.ui.theme.TeamManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TeamManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var status by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hello, test user",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        HorizontalDivider()

        Text(
            text = "Next match",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Racing vs Boca Juniors",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "12:00  ·  CDM",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        Text(
            text = "Are you coming?",
            fontSize = 15.sp
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { status = "See you on the pitch!" },
                modifier = Modifier.weight(1f)
            ) {
                Text("I am coming")
            }
            OutlinedButton(
                onClick = { status = "Absence noted." },
                modifier = Modifier.weight(1f)
            ) {
                Text("I am not coming")
            }
        }

        if (status.isNotEmpty()) {
            Text(
                text = status,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}