package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CartScreen(apiClient: ApiClient, cartManager: CartManager, navController: NavController, cartItemCount: Int) {
    val items = cartManager.items
    var showCheckoutBar by remember { mutableStateOf(items.isNotEmpty()) }

    LaunchedEffect(items) {
        showCheckoutBar = items.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("购物车 (${cartManager.totalItems})", fontWeight = FontWeight.Bold) },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = {
                            cartManager.clearCart()
                        }) {
                            Text("清空", color = BrandPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        },
        bottomBar = {
            if (showCheckoutBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandSurface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("合计：", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                            Text("¥", style = TextStyle(fontSize = 14.sp, color = BrandPrice))
                            Text(String.format("%.2f", items.sumOf { it.price * it.quantity }), style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                        }
                        Text("共${cartManager.totalItems}件商品", style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                    }
                    Button(onClick = { navController.navigate("checkout/") }, modifier = Modifier.height(44.dp)) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("去结算")
                    }
                }
            }
        }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyView("购物车是空的")
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.height(90.dp)) {
                            AsyncImage(
                                model = item.productImage,
                                contentDescription = item.productName,
                                modifier = Modifier.size(90.dp).background(BrandBorder),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f).padding(8.dp), verticalArrangement = Arrangement.Center) {
                                Text(item.productName, maxLines = 2, overflow = TextOverflow.Ellipsis, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandText))
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("¥", style = TextStyle(fontSize = 12.sp, color = BrandPrice))
                                    Text(String.format("%.2f", item.price), style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Delete, null, tint = BrandTextSecondary, modifier = Modifier.size(14.dp))
                                    TextButton(onClick = { cartManager.removeFromCart(item.productId) }) { Text("删除", fontSize = 11.sp, color = BrandTextSecondary) }
                                    Spacer(Modifier.weight(1f))
                                    OutlinedButton(onClick = { if (item.quantity > 1) cartManager.updateQuantity(item.productId, item.quantity - 1) }, modifier = Modifier.size(26.dp)) { Text("-", fontSize = 13.sp) }
                                    Text(item.quantity.toString(), modifier = Modifier.width(30.dp), textAlign = TextAlign.Center, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                                    OutlinedButton(onClick = { cartManager.updateQuantity(item.productId, item.quantity + 1) }, modifier = Modifier.size(26.dp)) { Text("+", fontSize = 13.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
