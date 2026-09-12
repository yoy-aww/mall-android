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

@Composable
fun OrdersScreen(apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(filter) {
        loading = true
        val uid = authStore.user?.id ?: ""
        val path = if (filter.isEmpty()) "/orders?userId=$uid" else "/orders?userId=$uid&status=$filter"
        apiClient.getMyOrders()
            .onSuccess { orders = it.list }
            .onFailure { orders = emptyList() }
        loading = false
    }

    val filters = listOf("" to "全部", "pending" to "待付款", "paid" to "已付款", "shipped" to "待收货", "completed" to "已完成", "cancelled" to "已取消")

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("我的订单", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Filter tabs
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filters) { (value, label) ->
                    FilterChip(
                        selected = filter == value,
                        onClick = { filter = value },
                        label = { Text(label, fontSize = 13.sp) },
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            if (loading) {
                LoadingView()
            } else if (orders.isEmpty()) {
                EmptyView("暂无订单")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                                .clickable { navController.navigate("order/${order.id}") },
                            colors = CardDefaults.cardColors(containerColor = BrandSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(order.id, style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary), modifier = Modifier.weight(1f))
                                    StatusChip(order.status)
                                }
                                Spacer(Modifier.height(8.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                order.items.take(2).forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.productName, style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f))
                                        Text("×${item.quantity}", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                                        Spacer(Modifier.width(8.dp))
                                        Text("¥${String.format("%.2f", item.price)}", style = TextStyle fontSize = 13.sp, color = BrandPrice))
                                    }
                                }
                                if (order.items.size > 2) {
                                    Text("等${order.items.size}件商品", style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                                }
                                Spacer(Modifier.height(8.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("合计：", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary), modifier = Modifier.weight(1f))
                                    Text("¥", style = TextStyle(fontSize = 12.sp, color = BrandPrice))
                                    Text(String.format("%.2f", order.totalAmount), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.End) {
                                    if (order.status == "pending") {
                                        OutlinedButton(onClick = { apiClient.cancelOrder(order.id) }) { Text("取消") }
                                        Spacer(Modifier.width(8.dp))
                                        Button(onClick = { navController.navigate("payment/${order.id}") }) { Text("去支付") }
                                    }
                                    if (order.status == "shipped") {
                                        Button(onClick = { apiClient.confirmDelivery(order.id) }) { Text("确认收货") }
                                    }
                                    if (order.status == "delivered") {
                                        Button(onClick = { apiClient.confirmOrder(order.id) }) { Text("确认完成") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
