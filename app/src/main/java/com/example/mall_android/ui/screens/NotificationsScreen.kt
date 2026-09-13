package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun NotificationsScreen(apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var unread by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apiClient.getNotifications().onSuccess { result ->
            notifications = result.list
            unread = result.unread
        }
        loading = false
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("消息通知", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (unread > 0) {
                Button(onClick = {
                    apiClient.markAllNotificationsRead().onSuccess {
                        notifications = notifications.map { it.copy(read = 1) }
                        unread = 0
                    }
                }, modifier = Modifier.fillMaxWidth().height(40.dp)) {
                    Text("全部标为已读 (${unread}条未读)")
                }
                Spacer(Modifier.height(8.dp))
            }
            if (loading) {
                LoadingView()
            } else if (notifications.isEmpty()) {
                EmptyView("暂无消息")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(notifications) { notif ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                                .clickable { apiClient.markNotificationRead(notif.id).onSuccess {
                                    notifications = notifications.map { if (it.id == notif.id) it.copy(read = 1) else it }
                                    unread = notifications.count { it.read == 0 }
                                } },
                            colors = CardDefaults.cardColors(containerColor = if (notif.read == 0) BrandDiscountBg else BrandSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                if (notif.read == 0) {
                                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(BrandPrimary).padding(end = 8.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(notif.title, style = TextStyle(fontSize = 14.sp, fontWeight = if (notif.read == 0) FontWeight.Bold else FontWeight.Normal))
                                    Spacer(Modifier.height(4.dp))
                                    Text(notif.content, style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                                    Spacer(Modifier.height(4.dp))
                                    Text(notif.createdAt.take(16).replace("T", " "), style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
