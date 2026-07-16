package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.viewmodel.MainViewModel

@Composable
fun KeypadScreen(navController: NavController, viewModel: MainViewModel) {
    var enteredNumber by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Contact") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newContactName,
                        onValueChange = { newContactName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredNumber,
                        onValueChange = { enteredNumber = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newContactName.isNotBlank() && enteredNumber.isNotBlank()) {
                        viewModel.addContact(newContactName, enteredNumber)
                        showAddDialog = false
                        navController.popBackStack()
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = enteredNumber.ifEmpty { " " },
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(32.dp),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
        
        if (enteredNumber.isNotEmpty()) {
            TextButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add to contacts")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add to contacts")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        val padModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            
        Column(modifier = padModifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DialerKey("1", "") { enteredNumber += "1" }
                DialerKey("2", "ABC") { enteredNumber += "2" }
                DialerKey("3", "DEF") { enteredNumber += "3" }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DialerKey("4", "GHI") { enteredNumber += "4" }
                DialerKey("5", "JKL") { enteredNumber += "5" }
                DialerKey("6", "MNO") { enteredNumber += "6" }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DialerKey("7", "PQRS") { enteredNumber += "7" }
                DialerKey("8", "TUV") { enteredNumber += "8" }
                DialerKey("9", "WXYZ") { enteredNumber += "9" }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                DialerKey("*", "") { enteredNumber += "*" }
                DialerKey("0", "+") { enteredNumber += "0" }
                DialerKey("#", "") { enteredNumber += "#" }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(64.dp))
            FloatingActionButton(
                onClick = {
                    if (enteredNumber.isNotEmpty()) {
                        viewModel.makeActualCall(context, enteredNumber)
                        navController.popBackStack()
                    }
                },
                containerColor = Color(0xFF4CAF50),
                modifier = Modifier.size(72.dp),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (enteredNumber.isNotEmpty()) {
                    IconButton(onClick = { enteredNumber = enteredNumber.dropLast(1) }) {
                        Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DialerKey(number: String, letters: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = number, fontSize = 28.sp, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
        if (letters.isNotEmpty()) {
            Text(text = letters, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
