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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController

@Composable
fun ProfileScreen(authStore: AuthStore, apiClient: ApiClient, navController: NavController) {
    val user = authStore.user
    var unreadCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        apiClient.getNotifications().onSuccess { unreadCount = it.unread }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("个人中心", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface))
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
                            modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BrandPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text((user?.nickname ?: user?.username ?: "U").take(1), style = TextStyle(fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user?.nickname ?: user?.username ?: "", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandText))
                            if (user?.phone?.isNotEmpty() == true) {
                                Text(user.phone, style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                            }
                        }
                        TextButton(onClick = { navController.navigate("profile-edit") }) {
                            Icon(Icons.Default.Edit, "编辑", tint = BrandSecondary)
                            Spacer(Modifier.width(2.dp))
                            Text("编辑")
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BrandBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.Gray)
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(12dp)),
                    colors = CardDefaults.cardColors(containerColor = BrandSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArchitecture = Arrangement.SpaceEvenly) {
                        OrderStatItem("全部订单", Icons.Default.ShoppingBag, null, { navController.navigate("orders") })
                        OrderStatItem("待付款", Icons.Default.AccountBalanceWallet, "pending", { navController.navigate("orders") })
                        OrderStatItem("待发货", Icons.Default.Payment, "paid", { navController.navigate("orders") })
                        OrderStatItem("待收货", Icons.Default.Truck, "shipped", { navController.navigate("orders") })
                    }
                }
            }

            // Menu items
            val menuItems = if (authStore.isLoggedIn()) {
                listOf(
                    MenuItem("全部订单", Icons.Default.ShoppingBag, "orders", null),
                    MenuItem("我的地址", Icons.Default.LocationOn, "addresses", null),
                    MenuItem("售后服务", Icons.Default.Replay, "aftersales", null),
                    MenuItem("消息通知", Icons.Default.Notifications, "notifications", unreadCount),
                )
            } else {
                listOf(
                    MenuItem("消息通知", Icons.Default.Notifications, "notifications", null),
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
                                            Text(item.badge.toString(), style = TextStyle(fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
                                }
                            },
                            modifier = if (i < menuItems.size - 1) Modifier.clip(RoundedCornerShape(0.dp)).navigationClick { navController.navigate(item.route) } else Modifier.clip(RoundedCornerShape(0.dp)).navigationClick { navController.navigate(item.route) }
                        )
                        if (i < menuItems.size - 1) Divider(color = BrandBorder)
                    }
                }
            }

            if (authStore.isLoggedIn()) {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp).clip(RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = BrandSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    ListItem(headlineContent = { Text("退出登录") }, leadingContent = { Icon(Icons.Default.Logout, null, tint = Color.Red, modifier = Modifier.size(24.dp)) }, modifier = Modifier.clip(RoundedCornerShape(0.dp)).navigationClick {
                        authStore.logout()
                        navController.navigateUp()
                    })
                }
            }
        }
    }
}

private data class MenuItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val route: String, val badge: Int?)

private data class OrderStatItem(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val status: String?, val onClick: () -> Unit)

@Composable
private fun RowScope.OrderStatItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, status: String?, onClick: () -> Unit) {
    Column(modifier = Modifier.clip(RoundedCornerShape(0.dp)).navigationClick { onClick() }, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = BrandSecondary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(4.dp))
        Text(title, style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
    }
}

@Composable
private fun Modifier.navigationClick(onClick: () -> Unit): Modifier = this.then(Modifier.clickable(onClick = onClick))
