package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.lazy.LazyColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CheckoutScreen(addressId: String, apiClient: ApiClient, cartManager: CartManager, authStore: AuthStore, navController: NavController) {
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var selectedAddressId by remember { mutableStateOf("") }
    var shippingMethod by remember { mutableStateOf("standard") }
    var remark by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<OrderPreview?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }

    val cartItems = cartManager.items
    val isLoggedIn = authStore.isLoggedIn()
    val selectedAddress = remember(addresses, selectedAddressId) {
        addresses.firstOrNull { it.id == selectedAddressId }
    }

    // Load addresses (when login state changes)
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            val result = withContext(Dispatchers.IO) { apiClient.getAddresses() }
            result.onSuccess { list ->
                addresses = list
                // Default select: preferred addressId → first default → first item
                val preferred = list.firstOrNull { it.id == addressId }
                    ?: list.firstOrNull { it.isDefault == 1 }
                    ?: list.firstOrNull()
                if (preferred != null) selectedAddressId = preferred.id
            }
        }
    }

    // Reload when returning from address edit (nav stack change)
    LaunchedEffect(navController.currentBackStack.value) {
        if (isLoggedIn) {
            val result = withContext(Dispatchers.IO) { apiClient.getAddresses() }
            result.onSuccess { list ->
                addresses = list
                if (selectedAddressId.isEmpty()) {
                    val preferred = list.firstOrNull { it.id == addressId }
                        ?: list.firstOrNull { it.isDefault == 1 }
                        ?: list.firstOrNull()
                    if (preferred != null) selectedAddressId = preferred.id
                }
            }
        }
    }

    // Toast auto-dismiss
    LaunchedEffect(toast) {
        if (toast != null) {
            kotlinx.coroutines.delay(2500)
            toast = null
        }
    }

    // Preview order
    // Preview order (on cart items or shipping method change)
    LaunchedEffect(cartItems, shippingMethod) {
        if (cartItems.isNotEmpty()) {
            val result = withContext(Dispatchers.IO) {
                apiClient.previewOrder(cartItems, shippingMethod)
            }
            result.onSuccess { preview = it }
        }
    }

    Box {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
                    title = { Text("确认订单", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
                )
            }
        ) { padding ->
        if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", style = MaterialTextStyle(48.sp))
                    Spacer(Modifier.height(12.dp))
                    Text("购物车没有商品", style = MaterialTextStyle(14.sp, color = BrandTextSecondary))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.navigateUp() }) { Text("去挑选") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier  .padding(padding).fillMaxSize()) {

                // ===== 收货信息 =====
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("收货信息", style = MaterialTextStyle(17.sp, FontWeight.Bold, Color(0xFF333333)))
                            Spacer(Modifier.height(12.dp))

                            if (isLoggedIn && addresses.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8. dp))
                                        .border(
                                            width = 1.dp,
                                            color = BrandBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暂无收货地址", style = MaterialTextStyle(13.sp, color = BrandTextSecondary))
                                }
                            }

                            if (isLoggedIn) {
                                addresses.forEach { addr ->
                                    Spacer(Modifier.height(10.dp))
                                    val selected = addr.id == selectedAddressId
                                    Surface(
                                        color = if (selected) Color(0xFFF0F9F4) else BrandSurface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = if (selected) Color(0xFFB7EB8F) else Color(0xFFD9D9D9),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedAddressId = addr.id }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            // 第一行：标签 + 默认标
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(0xFFF5F5F5))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        addr.label.ifEmpty { "地址" },
                                                        style = MaterialTextStyle(14.sp, color = Color(0xFF666666))
                                                    )
                                                }
                                                if (addr.isDefault == 1) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(4.dp))
                                                            .background(Color(0xFFFF4D4F))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("默认", style = MaterialTextStyle(12.sp, color = Color.White))
                                                    }
                                                }
                                            }

                                            // 第二行：姓名 手机 地址
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                "${addr.receiverName} ${addr.receiverPhone} ${addr.province} ${addr.city} ${addr.address}",
                                                style = MaterialTextStyle(14.sp, color = Color(0xFF333333)),
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }

                                // + 新建地址（虚线按钮）
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFD9D9D9),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { navController.navigate("address-edit") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+ 新建地址", style = MaterialTextStyle(14.sp, color = Color(0xFF666666)))
                                }
                            } else {
                                // 未登录/无地址
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFAFAFA))
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暂无收货地址，请新建", style = MaterialTextStyle(14.sp, color = Color(0xFF999999)))
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFD9D9D9),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .clickable { navController.navigate("address-edit") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("+ 新建地址", style = MaterialTextStyle(14.sp, color = Color(0xFF666666)))
                                }
                            }
                        }
                    }
                }

                // ===== 配送方式 =====
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("配送方式", style = MaterialTextStyle(16.sp, FontWeight.Bold, BrandPrimaryDark))
                            Spacer(Modifier.height(10.dp))
                            ShipMethodRow(shippingMethod) { shippingMethod = it }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "普通快递 ¥8（满 ¥199 免邮）；顺丰特快 ¥15（不免邮）",
                                style = MaterialTextStyle(11.sp, color = BrandTextSecondary)
                            )
                        }
                    }
                }

                // ===== 商品清单 =====
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("商品清单", style = MaterialTextStyle(16.sp, FontWeight.Bold, BrandPrimaryDark))
                            cartItems.forEach { item ->
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.productName, style = MaterialTextStyle(13.sp, color = BrandText), modifier = Modifier.weight(1f), maxLines = 2)
                                    Text("×${item.quantity}", style = MaterialTextStyle(12.sp, color = BrandTextSecondary))
                                    Spacer(Modifier.width(8.dp))
                                    Text("¥${String.format("%.2f", item.price * item.quantity)}", style = MaterialTextStyle(14.sp, FontWeight.Bold, BrandPrice))
                                }
                            }
                        }
                    }
                }

                // ===== 备注 =====
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("备注", style = MaterialTextStyle(17.sp, FontWeight.Bold, Color(0xFF333333)))
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = remark,
                                onValueChange = { remark = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("选填，如有特殊需求请在此说明") },
                                maxLines = 3,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // ===== 金额汇总 =====
                if (preview != null) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row {
                                    Text("商品小计", style = MaterialTextStyle(13.sp, color = BrandTextSecondary), modifier = Modifier.weight(1f))
                                    Text("¥${String.format("%.2f", preview!!.subtotal)}", style = MaterialTextStyle(13.sp))
                                }
                                Spacer(Modifier.height(6. dp))
                                Row {
                                    Text("运费", style = MaterialTextStyle(13.sp, color = BrandTextSecondary), modifier = Modifier.weight(1f))
                                    Text(
                                        if (preview!!.free) "免邮" else "¥${String.format("%.2f", preview!!.shippingFee)}",
                                        style = MaterialTextStyle(13.sp, color = if (preview!!.free) BrandSuccess else BrandText)
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Divider(color = BrandBorder)
                                Spacer(Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("应付", style = MaterialTextStyle(15.sp, FontWeight.Bold, BrandText), modifier = Modifier.weight(1f))
                                    Text("¥", style = MaterialTextStyle(14.sp, color = BrandPrice))
                                    Text(String.format("%.2f", preview!!.total), style = MaterialTextStyle(20. sp, FontWeight.Bold, BrandPrice))
                                }
                            }
                        }
                    }
                }

                // ===== 提交订单 =====
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val addr = selectedAddress
                            if (addr == null) {
                                toast = if (isLoggedIn) "请先选择或新建收货地址" else "请先登录"
                                return@Button
                            }
                            loading = true
                            val shippingAddress = "${addr.province} ${addr.city} ${addr.address}".trim()
                            val scope = kotlinx.coroutines.MainScope()
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    apiClient.createOrder(
                                        items = cartItems,
                                        shippingAddress = shippingAddress,
                                        receiverName = addr.receiverName,
                                        receiverPhone = addr.receiverPhone,
                                        remark = remark.ifEmpty { null },
                                        shippingMethod = shippingMethod
                                    )
                                }
                                result.onSuccess { order ->
                                    cartManager.clearCart()
                                    navController.navigate("payment/${order.id}") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    loading = false
                                }.onFailure {
                                    toast = "下单失败：${it.message}"
                                    loading = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal = 12.dp),
                        enabled = !loading && preview != null && selectedAddress != null
                    ) {
                        when {
                            loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            preview == null -> Text("计算中...", fontSize = 16.sp)
                            else -> Text("提交订单", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

        // Toast 提示
        val currentToast = toast
        if (currentToast != null) {
            Box(
                modifier = Modifier
                    .padding(top = 60.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xEE333333))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(currentToast, style = MaterialTextStyle(14.sp, color = Color.White))
            }
        }
    }
}

@Composable
private fun ShipMethodRow(method: String, onMethodChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ShipOptionRow(selected = method == "standard", label = "标准快递 · ¥8（满 ¥199 免邮）") {
            onMethodChange("standard")
        }
        Spacer(Modifier.height(8. dp))
        ShipOptionRow(selected = method == "sfx", label = "顺丰特快 · ¥15") {
            onMethodChange("sfx")
        }
    }
}

@Composable
private fun ShipOptionRow(selected: Boolean, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(if (selected) BrandTagBg else BrandBackground)
            .border(
                width = if (selected) 1.dp else 1.dp,
                color = if (selected) BrandPrimary else BrandBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .border(1.dp, if (selected) BrandPrimary else BrandTextSecondary, CircleShape)
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(BrandPrimary)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTextStyle(14.sp))
    }
}