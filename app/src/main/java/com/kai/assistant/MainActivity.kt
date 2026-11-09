package com.kai.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val Ink = Color(0xFF263B57)
private val Muted = Color(0xFF77869A)
private val Canvas = Color(0xFFF5F7FA)
private val Line = Color(0xFFE5EAF0)
private val BlueTint = Color(0xFFEDF2F8)
private val White = Color.White

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vault = ApiKeyVault(applicationContext)
        setContent { KaiApp(vault) }
    }
}

private enum class Tab(val label: String) {
    HOME("Overview"), CHAT("Chat"), TEAM("AI Team"), SETTINGS("Settings")
}

@Composable
private fun KaiApp(vault: ApiKeyVault) {
    var tab by remember { mutableStateOf(Tab.HOME) }
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Ink, onPrimary = White, background = Canvas, surface = White,
        onSurface = Ink, secondary = Muted
    )) {
        Scaffold(containerColor = Canvas, bottomBar = {
            NavigationBar(containerColor = White, tonalElevation = 4.dp) {
                Tab.values().forEach { item ->
                    val selected = tab == item
                    NavigationBarItem(
                        selected = selected, onClick = { tab = item },
                        icon = {
                            Icon(when (item) {
                                Tab.HOME -> Icons.Default.Home
                                Tab.CHAT -> Icons.Default.ChatBubbleOutline
                                Tab.TEAM -> Icons.Default.Groups
                                Tab.SETTINGS -> Icons.Default.Settings
                            }, contentDescription = item.label)
                        },
                        label = { Text(item.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink, selectedTextColor = Ink, indicatorColor = BlueTint,
                            unselectedIconColor = Muted, unselectedTextColor = Muted
                        )
                    )
                }
            }
        }) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    Tab.HOME -> HomeScreen(onChat = { tab = Tab.CHAT }, onSettings = { tab = Tab.SETTINGS })
                    Tab.CHAT -> ChatScreen(vault)
                    Tab.TEAM -> TeamScreen()
                    Tab.SETTINGS -> SettingsScreen(vault)
                }
            }
        }
    }
}

@Composable
private fun PageHeader(title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
        Text(title, fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(5.dp))
        Text(subtitle, fontSize = 13.sp, color = Muted)
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(17.dp), content = content)
    }
}

@Composable
private fun HomeScreen(onChat: () -> Unit, onSettings: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("KAI", fontSize = 12.sp, color = Muted, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Good to see you.", fontSize = 25.sp, color = Ink, fontWeight = FontWeight.Bold)
                    Text("Your executive workspace", color = Muted, fontSize = 13.sp)
                }
                Box(Modifier.size(46.dp).background(BlueTint, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Kai", tint = Ink)
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Ink)) {
                Column(Modifier.padding(20.dp)) {
                    Text("YOUR AI CHIEF OF STAFF", color = Color(0xFFC9D5E3), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.3.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("What would you like to\nget done today?", color = White, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onChat, colors = ButtonDefaults.buttonColors(containerColor = White, contentColor = Ink),
                        shape = RoundedCornerShape(12.dp)) {
                        Text("Start a conversation")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
        item { Text("Workspace", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("AI employees", "04", "Specialist roles", Modifier.weight(1f))
                MetricCard("Connections", "01", "Local device", Modifier.weight(1f))
            }
        }
        item {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Ink)
                    Spacer(Modifier.width(10.dp))
                    Text("Private by design", fontWeight = FontWeight.SemiBold, color = Ink)
                }
                Spacer(Modifier.height(7.dp))
                Text("Your API key is encrypted on this device. Add a provider key in Settings to activate live AI responses.", color = Muted, fontSize = 13.sp)
                TextButton(onClick = onSettings) { Text("Configure AI provider") }
            }
        }
        item {
            Text("Quick actions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(8.dp))
            Panel {
                QuickAction(Icons.Default.Chat, "Ask Kai", "Think, plan, write or research", onChat)
                HorizontalDivider(color = Line)
                QuickAction(Icons.Default.Tune, "AI settings", "Connect your preferred model", onSettings)
            }
        }
        item { Text("Your workspace is ready for its next integrations.", color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun MetricCard(title: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = White)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Muted, fontSize = 12.sp)
            Text(value, color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun QuickAction(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, detail: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(BlueTint, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = Ink)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Muted, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Muted)
    }
}

@Composable
private fun TeamScreen() {
    val agents = listOf(
        Triple("Chief of Staff", "Plans priorities, coordinates work and reviews outcomes.", Icons.Default.WorkspacePremium),
        Triple("Research Analyst", "Finds information, compares options and prepares briefings.", Icons.Default.Search),
        Triple("Inbox Manager", "Organises messages and prepares replies for your review.", Icons.Default.Inbox),
        Triple("Operations Planner", "Turns goals into tasks, schedules and progress updates.", Icons.Default.AccountTree)
    )
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { PageHeader("Your AI team", "Specialist employees with clear responsibilities") }
        items(agents) { agent ->
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).background(BlueTint, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                        Icon(agent.third, contentDescription = null, tint = Ink)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(agent.first, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text("Specialist agent", color = Muted, fontSize = 11.sp)
                    }
                    Surface(color = BlueTint, shape = RoundedCornerShape(8.dp)) {
                        Text("Ready", Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = Ink, fontSize = 10.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(agent.second, color = Muted, fontSize = 13.sp)
            }
        }
        item { Text("Agent actions are coordinated through Kai chat in this MVP. Independent background execution is a future milestone.", color = Muted, fontSize = 12.sp) }
    }
}

@Composable
private fun ChatScreen(vault: ApiKeyVault) {
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Add an API key in Settings to enable live AI.") }
    val messages = remember { mutableStateListOf("Kai" to "Hello. I’m Kai, your executive assistant. I can help you plan, analyse and get work organised.") }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp)) {
        PageHeader("Conversation", "Your private executive workspace")
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(messages) { msg ->
                val mine = msg.first == "You"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    Surface(color = if (mine) Ink else White, shape = RoundedCornerShape(16.dp),
                        shadowElevation = 1.dp, modifier = Modifier.widthIn(max = 300.dp)) {
                        Text(msg.second, Modifier.padding(14.dp), color = if (mine) White else Ink, fontSize = 14.sp)
                    }
                }
            }
            if (busy) item { Text("Kai is thinking…", color = Muted, fontSize = 12.sp) }
        }
        Text(status, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 7.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f),
                placeholder = { Text("Message Kai…") }, shape = RoundedCornerShape(16.dp), maxLines = 4)
            Spacer(Modifier.width(8.dp))
            IconButton(enabled = input.isNotBlank() && !busy, onClick = {
                val prompt = input.trim()
                input = ""
                messages.add("You" to prompt)
                busy = true
                status = "Sending securely to your selected provider…"
                scope.launch {
                    val answer = withContext(Dispatchers.IO) {
                        runCatching {
                            val key = vault.getKey() ?: error("No API key saved. Open Settings to add one.")
                            AiClient.reply(vault.getProvider(), vault.getModel(), key, prompt)
                        }.getOrElse { "I couldn't complete that request: ${it.message ?: "Unknown error"}. Check your provider settings and connection." }
                    }
                    messages.add("Kai" to answer)
                    busy = false
                    status = "Connected through your selected AI provider."
                }
            }) { Icon(Icons.Default.Send, contentDescription = "Send message", tint = Ink) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(vault: ApiKeyVault) {
    val scope = rememberCoroutineScope()
    val providers = listOf("Groq", "Gemini", "OpenAI")
    var provider by remember { mutableStateOf(vault.getProvider()) }
    var model by remember { mutableStateOf(vault.getModel()) }
    var key by remember { mutableStateOf(vault.getKey().orEmpty()) }
    var visible by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Your key is stored encrypted on this device.") }
    var busy by remember { mutableStateOf(false) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PageHeader("Settings", "Connect the AI services you choose") }
        item {
            Panel {
                Text("AI provider", color = Ink, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = provider, onValueChange = {}, readOnly = true,
                        label = { Text("Provider") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        providers.forEach { option ->
                            DropdownMenuItem(text = { Text(option) }, onClick = {
                                provider = option
                                model = defaultModel(option)
                                expanded = false
                            })
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = model, onValueChange = { model = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Model") }, supportingText = { Text("Use a model enabled for your provider account.") },
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = key, onValueChange = { key = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("API key") }, placeholder = { Text("Paste your provider key here") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton(onClick = { visible = !visible }) {
                        Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (visible) "Hide key" else "Show key")
                    } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp), singleLine = true)
                Spacer(Modifier.height(5.dp))
                Text("Keys are encrypted with Android Keystore and saved only on this device. Never commit keys to GitHub.", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(14.dp))
                Button(onClick = {
                    if (key.isBlank()) { message = "Enter an API key first."; return@Button }
                    vault.save(provider, model, key.trim())
                    message = "API key saved securely on this device."
                }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink)) { Text("Save API key") }
                OutlinedButton(onClick = {
                    if (key.isBlank()) { message = "Enter an API key first."; return@OutlinedButton }
                    vault.save(provider, model, key.trim())
                    busy = true
                    message = "Testing connection…"
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            runCatching { AiClient.reply(provider, model, key.trim(), "Reply with exactly: Kai connection successful.") }
                        }
                        message = result.fold(
                            onSuccess = { "Connection successful. Kai can now use $provider." },
                            onFailure = { "Connection failed: ${it.message ?: "Check the key, model and internet connection."}" }
                        )
                        busy = false
                    }
                }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp)) {
                    if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Test connection")
                }
                Text(message, color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                TextButton(onClick = {
                    vault.clearKey()
                    key = ""
                    message = "Saved API key removed from this device."
                }) { Text("Remove saved key", color = Ink) }
            }
        }
        item {
            Panel {
                Text("Privacy & permissions", fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(Modifier.height(7.dp))
                Text("Kai does not request WhatsApp, email, contacts, microphone or calendar permissions in this MVP. Those integrations will be added only with clear permissions and provider-supported access.", color = Muted, fontSize = 12.sp)
            }
        }
        item {
            Panel {
                Text("About Kai", fontWeight = FontWeight.SemiBold, color = Ink)
                Text("Version 0.1.0 • Android MVP • Local-first", color = Muted, fontSize = 12.sp)
            }
        }
    }
}

private fun defaultModel(provider: String): String = when (provider) {
    "Gemini" -> "gemini-2.0-flash"
    "OpenAI" -> "gpt-4o-mini"
    else -> "llama-3.3-70b-versatile"
}
