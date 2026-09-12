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
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController

@Composable
fun CheckoutScreen(addressId: String, apiClient: ApiClient, cartManager: CartManager, authStore: AuthStore, navController: NavController) {
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var selectedAddressId by remember { mutableStateOf(addressId) }
    var shippingMethod by remember { mutableStateOf("standard") }
    var remark by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<OrderPreview?>(null) }

    val cartItems = cartManager.items

    LaunchedEffect(Unit) {
        apiClient.getAddresses().onSuccess { addresses = it }
    }

    LaunchedEffect(cartItems, shippingMethod) {
        if (cartItems.isNotEmpty()) {
            apiClient.previewOrder(cartItems, shippingMethod).onSuccess { preview = it }
        }
    }

    val selectedAddress = addresses.find { it.id == selectedAddressId }

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
            EmptyView("购物车为空")
            Button(onClick = { navController.navigateUp() }, modifier = Modifier.align(Alignment.Center).padding(16.dp)) { Text("返回首页") }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Address selection
                item {
                    Text("收货地址", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(12.dp))
                    if (addresses.isEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                            Column(modifier = Modifier.padding(16dp)) {
                                Text("暂无收货地址", style = TextStyle(color = BrandTextSecondary))
                                TextButton(onClick = { navController.navigate("addresses") }) { Text("添加地址", color = BrandPrimary) }
                            }
                        }
                    } else {
                        addresses.forEach { addr ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedAddressId = addr.id },
                                colors = CardDefaults.cardColors(containerColor = if (addr.id == selectedAddressId) BrandDiscountBg else BrandSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (addr.id == selectedAddressId) 4.dp else 1.dp)
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = addr.id == selectedAddressId, onClick = { selectedAddressId = addr.id })
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("${addr.receiverName} ${addr.receiverPhone}", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                                            if (addr.isDefault == 1) {
                                                Surface(color = BrandSuccess, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(start = 6.dp)) {
                                                    Text("默认", style = TextStyle(fontSize = 10.sp, color = Color.White), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                        }
                                        Text("${addr.province}${addr.city}${addr.address}", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                                    }
                                }
                            }
                        }
                        TextButton(onClick = { navController.navigate("addresses") }) {
                            Text("+ 添加新地址", color = BrandPrimary, fontSize = 13.sp)
                        }
                    }
                }

                // Products
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("商品清单", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                            cartItems.forEach { item ->
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

                // Shipping method
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("配送方式", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(8.dp))
                            Row {
                                FilterChip(selected = shippingMethod == "standard", onClick = { shippingMethod = "standard" }, label = { Text("普通快递 ¥8") }, modifier = Modifier.weight(1f))
                                Spacer(Modifier.width(8.dp))
                                FilterChip(selected = shippingMethod == "sfx", onClick = { shippingMethod = "sfx" }, label = { Text("顺丰 ¥15") }, modifier = Modifier.weight(1f))
                            }
                            if (preview != null) {
                                Spacer(Modifier.height(8.dp))
                                Text("满¥199免运费（顺丰不免）", style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                            }
                        }
                    }
                }

                // Remark
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("备注（可选）", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(value = remark, onValueChange = { remark = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3)
                        }
                    }
                }

                // Price summary
                if (preview != null) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row {
                                    Text("商品小计", style = TextStyle(fontSize = 14.sp), modifier = Modifier.weight(1f))
                                    Text("¥${String.format("%.2f", preview!!.subtotal)}", style = TextStyle(fontSize = 14.sp))
                                }
                                Spacer(Modifier.height(6.dp))
                                Row {
                                    Text("运费", style = TextStyle(fontSize = 14.sp), modifier = Modifier.weight(1f))
                                    Text(if (preview!!.free) "免运费" else "¥${String.format("%.2f", preview!!.shippingFee)}", style = TextStyle(fontSize = 14.sp, color = if (preview!!.free) BrandSuccess else BrandText))
                                }
                                Spacer(Modifier.height(8.dp))
                                Divider()
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text("应付总额", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                    Text("¥", style = TextStyle(fontSize = 14.sp, color = BrandPrice))
                                    Text(String.format("%.2f", preview!!.total), style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            loading = true
                            val addr = selectedAddress ?: return@Button
                            apiClient.createOrder(
                                items = cartItems,
                                shippingAddress = "${addr.province}${addr.city}${addr.address}",
                                receiverName = addr.receiverName,
                                receiverPhone = addr.receiverPhone,
                                remark = remark.ifEmpty { null },
                                shippingMethod = shippingMethod
                            ).onSuccess { order ->
                                cartManager.clearCart()
                                navController.navigate("payment/${order.id}")
                            }.onFailure { loading = false }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !loading && selectedAddress != null
                    ) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        else Text("提交订单", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
