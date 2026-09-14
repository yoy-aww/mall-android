package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AfterSalesScreen(apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    val scope = rememberCoroutineScope()
    var aftersales by remember { mutableStateOf<List<AfterSale>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refundableOrders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var selectedOrderId by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf("") }
    var applyDescription by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var applyError by remember { mutableStateOf("") }
    var applySuccess by remember { mutableStateOf("") }

    // 预定义原因选项
    val reasons = listOf("质量问题", "不喜欢/不想要", "买贵了/少优惠", "少发/漏发", "商品破损/污渍", "空包裹")

    LaunchedEffect(Unit) {
        apiClient.getAfterSales().onSuccess { aftersales = it }
        loading = false
        // 加载可退订单（paid/shipped/delivered/completed）
        apiClient.getMyOrders().onSuccess { paginated ->
            refundableOrders = paginated.list.filter { it.status in listOf("paid", "shipped", "delivered", "completed") }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("售后服务", fontWeight = FontWeight.Bold) },
            actions = {
                TextButton(onClick = {
                    if (!authStore.isLoggedIn()) navController.navigate("auth")
                    else {
                        selectedOrderId = ""
                        selectedReason = ""
                        applyDescription = ""
                        applyError = ""
                        showApplyDialog = true
                    }
                }) { Text("申请售后") }
            },
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

        if (showApplyDialog) {
            AlertDialog(
                onDismissRequest = { if (!submitting) showApplyDialog = false },
                title = { Text("申请售后", style = MaterialTextStyle(18.sp, FontWeight.Bold, BrandPrimaryDark)) },
                text = {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Text("选择订单", style = MaterialTextStyle(14.sp, FontWeight.Medium, BrandText))
                        Spacer(Modifier.height(6.dp))
                        if (refundableOrders.isEmpty()) {
                            Text(
                                "暂无可申请售后的订单（订单需为已付款/已发货/已签收/已完成状态）",
                                style = MaterialTextStyle(13.sp, FontWeight.Normal, BrandTextSecondary),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            refundableOrders.forEach { order ->
                                val checked = selectedOrderId == order.id
                                Surface(
                                    color = if (checked) BrandTagBg else BrandBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (checked) BrandPrimary else BrandBorder, RoundedCornerShape(8.dp))
                                        .clickable { selectedOrderId = order.id }
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, if (checked) BrandPrimary else BrandBorder, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (checked) Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(BrandPrimary))
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text("订单号：${order.id}", style = MaterialTextStyle(13.sp, FontWeight.Medium, BrandText))
                                            Text(
                                                "${order.items.size} 件商品  ¥${String.format("%.2f", order.totalAmount)}",
                                                style = MaterialTextStyle(12.sp, FontWeight.Normal, BrandTextSecondary)
                                            )
                                            Text(
                                                "状态：${when (order.status) {
                                                    "paid" -> "已付款"
                                                    "shipped" -> "已发货"
                                                    "delivered" -> "已签收"
                                                    "completed" -> "已完成"
                                                    else -> order.status
                                                }}",
                                                style = MaterialTextStyle(12.sp, FontWeight.Normal, BrandTextSecondary)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("原因", style = MaterialTextStyle(14.sp, FontWeight.Medium, BrandText))
                        Spacer(Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reasons.forEach { reason ->
                                val selected = selectedReason == reason
                                Surface(
                                    color = if (selected) BrandPrimary else BrandBackground,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.clickable { selectedReason = reason }
                                ) {
                                    Text(
                                        reason,
                                        style = MaterialTextStyle(12.sp, FontWeight.Normal, if (selected) Color.White else BrandText),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text("描述（选填）", style = MaterialTextStyle(14.sp, FontWeight.Medium, BrandText))
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = applyDescription,
                            onValueChange = { applyDescription = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("详细说明问题...") },
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                        if (applyError.isNotEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(applyError, style = MaterialTextStyle(12.sp, FontWeight.Normal, Color(0xFFE74C3C)))
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        applyError = ""
                        if (selectedOrderId.isEmpty()) { applyError = "请选择订单"; return@Button }
                        if (selectedReason.isEmpty()) { applyError = "请选择原因"; return@Button }
                        submitting = true
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                apiClient.createAfterSale(selectedOrderId, emptyList(), selectedReason, applyDescription).onSuccess {
                                    showApplyDialog = false
                                    applySuccess = "售后申请已提交"
                                    apiClient.getAfterSales().onSuccess { aftersales = it }
                                }.onFailure { applyError = "提交失败: ${it.message}" }
                            }
                            submitting = false
                        }
                    }, enabled = !submitting) {
                        if (submitting) CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        else Text("提交")
                    }
                },
                dismissButton = {
                    if (!submitting) TextButton(onClick = { showApplyDialog = false }) { Text("取消") }
                }
            )
        }
    }
}
