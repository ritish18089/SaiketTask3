package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.data.models.CallHistory
import com.example.data.models.CallType
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTab(viewModel: MainViewModel, modifier: Modifier = Modifier, navController: NavController) {
    val history by viewModel.callHistory.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Missed", "Incoming", "Outgoing")

    Column(modifier = modifier.fillMaxSize()) {
        TopSearchBar(viewModel, navController, "Search history")
        
        ScrollableTabRow(
            selectedTabIndex = filters.indexOf(selectedFilter),
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            filters.forEachIndexed { index, filter ->
                Tab(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    text = { Text(filter) }
                )
            }
        }

        val filteredHistory = history.filter {
            when (selectedFilter) {
                "Missed" -> it.callType == CallType.MISSED
                "Incoming" -> it.callType == CallType.INCOMING
                "Outgoing" -> it.callType == CallType.OUTGOING
                else -> true
            }
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
            items(filteredHistory) { call ->
                CallHistoryItem(call, viewModel)
            }
        }
    }
}

@Composable
fun CallHistoryItem(call: CallHistory, viewModel: MainViewModel) {
    val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(call.timestamp))
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.makeActualCall(context, call.phoneNumber) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = call.contactName?.firstOrNull()?.uppercase() ?: "?",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = call.contactName ?: call.phoneNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (call.callType) {
                    CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                    CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                    CallType.MISSED -> Icons.AutoMirrored.Filled.CallMissed
                }
                val tint = if (call.callType == CallType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$dateString • ${call.durationSeconds}s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        IconButton(onClick = { viewModel.makeActualCall(context, call.phoneNumber) }) {
            Icon(Icons.Default.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
