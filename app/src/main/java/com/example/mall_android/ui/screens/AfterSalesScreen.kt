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
fun AfterSalesScreen(apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var aftersales by remember { mutableStateOf<List<AfterSale>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apiClient.getAfterSales().onSuccess { aftersales = it }
        loading = false
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("售后服务", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        if (loading) { LoadingView() }
        else if (aftersales.isEmpty()) {
            EmptyView("暂无售后记录")
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(aftersales) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("订单号：${item.orderId}", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary), modifier = Modifier.weight(1f))
                                val statusColor = when (item.status) {
                                    "pending" -> StatusPending
                                    "approved" -> StatusPaid
                                    "rejected" -> StatusCancelled
                                    "completed" -> StatusCompleted
                                    else -> Color.Gray
                                }
                                Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                                    Text(when (item.status) {
                                        "pending" -> "审核中"
                                        "approved" -> "已同意"
                                        "rejected" -> "已拒绝"
                                        "completed" -> "已完成"
                                        else -> item.status
                                    }, style = TextStyle(fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Medium), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("原因：${item.reason}", style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium))
                            Spacer(Modifier.height(4.dp))
                            Text(item.description, style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                            if (item.items.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                item.items.forEach { it ->
                                    Text("${it.productName} ×${it.quantity}", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(item.createdAt.take(16).replace("T", " "), style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                            if (item.handleReason != null && item.handleReason!!.isNotEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Surface(color = BrandBackground, shape = RoundedCornerShape(8.dp)) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("商家处理", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSecondary))
                                        Text(item.handleReason, style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
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
