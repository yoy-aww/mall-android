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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController

@Composable
fun PaymentScreen(orderId: String, apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var order by remember { mutableStateOf<Order?>(null) }
    var paid by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apiClient.getOrder(orderId).onSuccess { order = it }
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("订单支付", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        val o = order
        if (o == null) {
            LoadingView()
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (paid) {
                    Icon(Icons.Default.CheckCircle, null, tint = BrandSuccess, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(20.dp))
                    Text("支付成功！", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandSuccess))
                    Spacer(Modifier.height(8.dp))
                    Text("订单号：${o.id}", style = TextStyle(fontSize = 14.sp, color = BrandTextSecondary))
                } else {
                    Text("应付金额", style = TextStyle(fontSize = 16.sp, color = BrandTextSecondary))
                    Spacer(Modifier.height(12.dp))
                    Text("¥", style = TextStyle(fontSize = 28.sp, color = BrandPrice))
                    Text(String.format("%.2f", o.totalAmount), style = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                    Spacer(Modifier.height(32.dp))

                    // Payment method
                    Surface(color = BrandSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { }, verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreditCard, null, tint = BrandSecondary)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("模拟支付", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium))
                                    Text("点击立即支付（测试用）", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                                }
                                Icon(Icons.Default.KeyboardArrowRight, null, tint = Color.Gray)
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            loading = true
                            apiClient.payOrder(orderId).onSuccess { paid = true }
                            .onFailure { loading = false }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !loading
                    ) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("立即支付", fontSize = 16.sp)
                    }

                    TextButton(onClick = { navController.navigate("orders") }) {
                        Text("稍后支付", color = BrandTextSecondary)
                    }
                }

                Spacer(Modifier.height(32.dp))
                Button(onClick = { navController.navigate("orders") }) { Text(if (paid) "查看订单" else "返回首页") }
            }
        }
    }
}
