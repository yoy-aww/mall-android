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
fun OrderDetailScreen(orderId: String, apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var order by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        apiClient.getOrder(orderId).onSuccess { order = it }
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("订单详情", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        val o = order ?: run { LoadingView(); return@Scaffold }
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Status
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatusChip(o.status)
                        Spacer(Modifier.height(8.dp))
                        Text("订单号：${o.id}", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                        Text("下单时间：${o.createdAt}", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                    }
                }
            }

            // Address
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("收货信息", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                        Spacer(Modifier.height(4.dp))
                        Text("${o.receiverName}  ${o.receiverPhone}", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                        Text(o.shippingAddress, style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                    }
                }
            }

            // Items
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("商品清单", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                        o.items.forEach { item ->
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(item.productName, style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f))
                                Text("×${item.quantity}", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                                Spacer(Modifier.width(8.dp))
                                Text("¥${String.format("%.2f", item.price)}", style = TextStyle(fontSize = 13.sp, color = BrandPrice, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Tracking
            if (o.tracking != null && o.tracking!!.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("物流单号", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                            Text(o.tracking, style = TextStyle(fontSize = 13.sp, color = BrandText))
                        }
                    }
                }
            }

            // Price
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row { Text("商品小计", style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f)); Text("¥${String.format("%.2f", o.subtotal)}", style = TextStyle(fontSize = 13.sp)) }
                        Spacer(Modifier.height(4.dp))
                        Row { Text("运费", style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f)); Text("¥${String.format("%.2f", o.shippingFee)}", style = TextStyle(fontSize = 13.sp)) }
                        Spacer(Modifier.height(8.dp))
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("实付金额", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            Text("¥", style = TextStyle(fontSize = 12.sp, color = BrandPrice))
                            Text(String.format("%.2f", o.totalAmount), style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                        }
                    }
                }
            }

            // Actions
            if (o.status == "pending" || o.status == "shipped" || o.status == "delivered") {
                item {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (o.status == "pending") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { apiClient.cancelOrder(o.id); navController.navigateUp() }, modifier = Modifier.weight(1f)) { Text("取消订单") }
                                Button(onClick = { navController.navigate("payment/${o.id}") }, modifier = Modifier.weight(1f)) { Text("去支付") }
                            }
                        }
                        if (o.status == "shipped") {
                            Button(onClick = { apiClient.confirmDelivery(o.id); navController.navigateUp() }, modifier = Modifier.fillMaxWidth()) { Text("确认收货") }
                        }
                        if (o.status == "delivered") {
                            Button(onClick = { apiClient.confirmOrder(o.id); navController.navigateUp() }, modifier = Modifier.fillMaxWidth()) { Text("确认完成") }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { navController.navigate("aftersales") }, modifier = Modifier.fillMaxWidth()) { Text("申请售后") }
                    }
                }
            }
        }
    }
}
