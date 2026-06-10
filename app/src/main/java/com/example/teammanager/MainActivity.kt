package com.example.teammanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teammanager.ui.theme.TeamManagerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Match(
    val id: String = "",
    val opponent: String = "",
    val location: String = "",
    val matchDate: String = "",
    val status: String = ""
)

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        enableEdgeToEdge()
        setContent {
            TeamManagerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoot(modifier = Modifier.padding(innerPadding), auth = auth)
                }
            }
        }
    }
}

@Composable
fun AppRoot(modifier: Modifier = Modifier, auth: FirebaseAuth) {
    var screen by remember { mutableStateOf(if (auth.currentUser != null) "main" else "login") }
    when (screen) {
        "login" -> LoginScreen(modifier = modifier, auth = auth, onLoggedIn = { screen = "main" }, onGoRegister = { screen = "register" })
        "register" -> RegisterScreen(modifier = modifier, auth = auth, onRegistered = { screen = "main" }, onBack = { screen = "login" })
        "main" -> MainScreen(modifier = modifier, auth = auth, onLogout = { auth.signOut(); screen = "login" })
    }
}

@Composable
fun LoginScreen(modifier: Modifier = Modifier, auth: FirebaseAuth, onLoggedIn: () -> Unit, onGoRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("TeamManager", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Sign in to your team", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        if (error.isNotEmpty()) { Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }
        Button(onClick = {
            if (email.isBlank() || password.isBlank()) { error = "Please fill in all fields"; return@Button }
            auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { if (it.isSuccessful) onLoggedIn() else error = it.exception?.message ?: "Login failed" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onGoRegister) { Text("No account? Register here") }
    }
}

@Composable
fun RegisterScreen(modifier: Modifier = Modifier, auth: FirebaseAuth, onRegistered: () -> Unit, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(modifier = modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password (min 6 chars)") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        if (error.isNotEmpty()) { Text(error, color = MaterialTheme.colorScheme.error, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }
        Button(onClick = {
            if (email.isBlank() || password.isBlank()) { error = "Please fill in all fields"; return@Button }
            if (password.length < 6) { error = "Password must be at least 6 characters"; return@Button }
            auth.createUserWithEmailAndPassword(email.trim(), password).addOnCompleteListener { if (it.isSuccessful) onRegistered() else error = it.exception?.message ?: "Registration failed" }
        }, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("Back to Login") }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, auth: FirebaseAuth, onLogout: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var matches by remember { mutableStateOf(listOf<Match>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var opponent by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var matchDate by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("matches").addSnapshotListener { snap, _ ->
            if (snap != null) {
                matches = snap.documents.map { doc ->
                    Match(
                        id = doc.id,
                        opponent = doc.getString("opponent") ?: "",
                        location = doc.getString("location") ?: "",
                        matchDate = doc.getString("matchDate") ?: "",
                        status = doc.getString("status") ?: ""
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Match") },
            text = {
                Column {
                    OutlinedTextField(value = opponent, onValueChange = { opponent = it }, label = { Text("Opponent *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location *") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = matchDate, onValueChange = { matchDate = it }, label = { Text("Date & Time *") }, modifier = Modifier.fillMaxWidth())
                    if (error.isNotEmpty()) { Spacer(Modifier.height(4.dp)); Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (opponent.isBlank() || location.isBlank() || matchDate.isBlank()) { error = "All fields are required"; return@Button }
                    db.collection("matches").add(mapOf("opponent" to opponent, "location" to location, "matchDate" to matchDate, "status" to "upcoming", "createdBy" to (auth.currentUser?.uid ?: "")))
                    opponent = ""; location = ""; matchDate = ""; error = ""; showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false; error = "" }) { Text("Cancel") } }
        )
    }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Hello, ${auth.currentUser?.email?.substringBefore("@") ?: "player"}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            TextButton(onClick = onLogout) { Text("Log out") }
        }
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))
        Text("Upcoming Matches", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { showAddDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("+ Add Match") }
        Spacer(Modifier.height(12.dp))
        if (matches.isEmpty()) {
            Text("No matches yet. Add one!", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(matches) { match ->
                    MatchCard(match = match, onDelete = { db.collection("matches").document(match.id).delete() })
                }
            }
        }
    }
}

@Composable
fun MatchCard(match: Match, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    var availability by remember { mutableStateOf("") }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Delete Match") },
            text = { Text("Are you sure you want to delete ${match.opponent}?") },
            confirmButton = { Button(onClick = { onDelete(); showConfirm = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text(match.opponent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("${match.matchDate} · ${match.location}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showConfirm = true }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { availability = "coming" }, colors = if (availability == "coming") ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(), modifier = Modifier.weight(1f)) { Text("I'm coming", fontSize = 12.sp) }
                OutlinedButton(onClick = { availability = "notcoming" }, modifier = Modifier.weight(1f)) { Text("Not coming", fontSize = 12.sp) }
            }
            if (availability == "coming") Text("✓ You're in!", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            if (availability == "notcoming") Text("✗ Absence noted", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}