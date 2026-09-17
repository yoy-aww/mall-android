package com.example.mall_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import kotlinx.coroutines.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mall_android.ApiClient
import com.example.mall_android.AuthStore
import com.example.mall_android.model.Order
import com.example.mall_android.ui.components.EmptyView
import com.example.mall_android.ui.components.LoadingView
import com.example.mall_android.ui.components.StatusChip
import com.example.mall_android.ui.theme.BrandBorder
import com.example.mall_android.ui.theme.BrandPrice
import com.example.mall_android.ui.theme.BrandSurface
import com.example.mall_android.ui.theme.BrandText
import com.example.mall_android.ui.theme.BrandTextSecondary

@Composable
fun OrdersScreen(apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var filter by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(filter) {
        loading = true
        val result = withContext(Dispatchers.IO) { apiClient.getMyOrders(filter.ifEmpty { null }) }
        result.onSuccess { orders = it.list }
        result.onFailure { orders = emptyList() }
        loading = false
    }

    val filters = listOf(
        "" to "全部", "pending" to "待付款", "paid" to "已付款",
        "shipped" to "待收货", "completed" to "已完成", "cancelled" to "已取消"
    )

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Filled.ArrowBack, "返回") } },
            title = { Text("我的订单", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Filter tabs
            LazyRow(
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
                                Divider(color = BrandBorder)
                                Spacer(Modifier.height(8.dp))
                                order.items.take(2).forEach { item ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.productName, style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f))
                                        Text("×${item.quantity}", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                                        Spacer(Modifier.width(8.dp))
                                        Text("¥${String.format("%.2f", item.price)}", style = TextStyle(fontSize = 13.sp, color = BrandPrice))
                                    }
                                }
                                if (order.items.size > 2) {
                                    Text("等${order.items.size}件商品", style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                                }
                                Spacer(Modifier.height(8.dp))
                                Divider(color = BrandBorder)
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
