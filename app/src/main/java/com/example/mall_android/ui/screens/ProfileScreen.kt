package com.example.mall_android.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mall_android.ApiClient
import com.example.mall_android.AuthStore
import com.example.mall_android.ui.theme.BrandBackground
import com.example.mall_android.ui.theme.BrandBorder
import com.example.mall_android.ui.theme.BrandPrimary
import com.example.mall_android.ui.theme.BrandSecondary
import com.example.mall_android.ui.theme.BrandSurface
import com.example.mall_android.ui.theme.BrandText
import com.example.mall_android.ui.theme.BrandTextSecondary

private data class MenuEntry(val title: String, val icon: ImageVector, val route: String, val badge: Int?)

private data class OrderStatEntry(val title: String, val icon: ImageVector)

@Composable
fun ProfileScreen(authStore: AuthStore, apiClient: ApiClient, navController: NavController) {
    val user = authStore.user
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        apiClient.getNotifications().onSuccess { unreadCount = it.unread }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // User header
            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (authStore.isLoggedIn()) {
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                (user?.nickname ?: user?.username ?: "U").take(1),
                                style = TextStyle(fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                user?.nickname ?: user?.username ?: "",
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandText)
                            )
                            if (!user?.phone.isNullOrEmpty()) {
                                Text(
                                    user.phone,
                                    style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary)
                                )
                            }
                        }
                        TextButton(onClick = { navController.navigate("profile-edit") }) {
                            Icon(Icons.Filled.Edit, "编辑", tint = BrandSecondary)
                            Spacer(Modifier.width(2.dp))
                            Text("编辑")
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(BrandBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, null, tint = Color.Gray)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("未登录", style = TextStyle(fontSize = 18.sp, color = BrandTextSecondary))
                        }
                        Button(onClick = { navController.navigate("auth") }) { Text("登录/注册") }
                    }
                }
            }

            // Order stats
            if (authStore.isLoggedIn()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        val statEntries = listOf(
                            OrderStatEntry("全部订单", Icons.Filled.ShoppingBag),
                            OrderStatEntry("待付款", Icons.Filled.AccountBalanceWallet),
                            OrderStatEntry("待发货", Icons.Filled.Payment),
                            OrderStatEntry("待收货", Icons.Filled.DirectionsCar)
                        )
                        statEntries.forEach { entry ->
                            OrderStatItem(title = entry.title, icon = entry.icon) {
                                navController.navigate("orders")
                            }
                        }
                    }
                }
            }

            // Menu items
            val menuItems = if (authStore.isLoggedIn()) {
                listOf(
                    MenuEntry("全部订单", Icons.Filled.ShoppingBag, "orders", null),
                    MenuEntry("我的地址", Icons.Filled.LocationOn, "addresses", null),
                    MenuEntry("售后服务", Icons.Filled.Replay, "aftersales", null),
                    MenuEntry("消息通知", Icons.Filled.Notifications, "notifications", unreadCount)
                )
            } else {
                listOf(
                    MenuEntry("消息通知", Icons.Filled.Notifications, "notifications", null)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = BrandSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    menuItems.forEachIndexed { i, item ->
                        ListItem(
                            headlineContent = { Text(item.title) },
                            leadingContent = { Icon(item.icon, null, tint = BrandSecondary, modifier = Modifier.size(24.dp)) },
                            trailingContent = {
                                Row {
                                    if (item.badge != null && item.badge!! > 0) {
                                        Surface(color = BrandPrimary, shape = RoundedCornerShape(10.dp)) {
                                            Text(
                                                item.badge.toString(),
                                                style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Icon(Icons.Filled.KeyboardArrowRight, null, tint = Color.Gray)
                                }
                            },
                            modifier = Modifier.clickable { navController.navigate(item.route) }
                        )
                        if (i < menuItems.size - 1) Divider(color = BrandBorder)
                    }
                }
            }

            if (authStore.isLoggedIn()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("退出登录") },
                        leadingContent = { Icon(Icons.Filled.Logout, null, tint = Color.Red, modifier = Modifier.size(24.dp)) },
                        modifier = Modifier.clickable {
                            authStore.logout()
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = BrandSecondary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(title, style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
    }
}
