package com.example.mall_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import com.example.mall_android.ui.components.LoadingView
import com.example.mall_android.ui.components.StatusChip
import com.example.mall_android.ui.theme.BrandBorder
import com.example.mall_android.ui.theme.BrandPrice
import com.example.mall_android.ui.theme.BrandSurface
import com.example.mall_android.ui.theme.BrandText
import com.example.mall_android.ui.theme.BrandTextSecondary

@Composable
fun OrderDetailScreen(orderId: String, apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var order by remember { mutableStateOf<Order?>(null) }

    LaunchedEffect(Unit) {
        apiClient.getOrder(orderId).onSuccess { order = it }
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Filled.ArrowBack, "返回") } },
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
                            Text(o.tracking!!, style = TextStyle(fontSize = 13.sp, color = BrandText))
                        }
                    }
                }
            }

            // Price
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Text("商品小计", style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f))
                            Text("¥${String.format("%.2f", o.subtotal)}", style = TextStyle(fontSize = 13.sp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Row {
                            Text("运费", style = TextStyle(fontSize = 13.sp), modifier = Modifier.weight(1f))
                            Text("¥${String.format("%.2f", o.shippingFee)}", style = TextStyle(fontSize = 13.sp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Divider(color = BrandBorder)
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
