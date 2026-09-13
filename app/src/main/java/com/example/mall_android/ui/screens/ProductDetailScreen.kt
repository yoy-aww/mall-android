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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun ProductDetailScreen(productId: String, apiClient: ApiClient, cartManager: CartManager, navController: NavController, cartItemCount: Int) {
    var product by remember { mutableStateOf<Product?>(null) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var stats by remember { mutableStateOf<ReviewStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    val clipboard = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        loading = true; error = ""
        val productResult = apiClient.getProduct(productId)
        productResult.onSuccess { product = it }
        apiClient.getReviews(productId).onSuccess { reviews = it }
        apiClient.getReviewStats(productId).onSuccess { stats = it }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                title = { Text("商品详情") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        }
    ) { padding ->
        if (loading) { LoadingView(); return@Scaffold }
        val p = product ?: run { ErrorView("加载失败"); return@Scaffold }

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Image
            item {
                AsyncImage(
                    model = p.image,
                    contentDescription = p.name,
                    modifier = Modifier.fillMaxWidth().height(280.dp).background(BrandBorder),
                    contentScale = ContentScale.Crop
                )
            }

            // Price + Name
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PriceText(p.discountedPrice ?: p.originalPrice, if (p.discountedPrice != null) p.originalPrice else null, 24.0)
                        Spacer(Modifier.height(8.dp))
                        Text(p.name, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandText), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        if (p.tags.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Row {
                                p.tags.forEach { tag ->
                                    Surface(color = BrandTagBg, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(end = 6.dp)) {
                                        Text(tag, style = TextStyle(fontSize = 11.sp, color = BrandTag), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Text("库存：${p.stock}件", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("数量", style = TextStyle(fontSize = 14.sp, color = BrandText))
                            Spacer(Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(32.dp)) { Text("-", fontSize = 16.sp) }
                                Text(quantity.toString(), modifier = Modifier.width(40.dp), textAlign = TextAlign.Center, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                                OutlinedButton(onClick = { if (quantity < p.stock) quantity++ }, modifier = Modifier.size(32.dp)) { Text("+", fontSize = 16.sp) }
                            }
                        }
                    }
                }
            }

            // Description
            if (p.description.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("商品描述", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandText))
                            Spacer(Modifier.height(8.dp))
                            Text(p.description, style = TextStyle(fontSize = 14.sp, color = BrandText))
                        }
                    }
                }
            }

            // Reviews
            if (reviews.isNotEmpty() || stats != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), colors = CardDefaults.cardColors(containerColor = BrandSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("用户评价", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandText))
                                Spacer(Modifier.weight(1f))
                                if (stats != null) {
                                    Text("共${stats!!.total}条评价", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                                }
                            }
                            if (stats != null) {
                                Spacer(Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(String.format("%.1f", stats!!.avg), style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = BrandPrice))
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Row {
                                            repeat(5) { i ->
                                                Icon(
                                                    Icons.Default.Star,
                                                    null,
                                                    tint = if (i < stats!!.avg.toInt()) BrandPrice else BrandOriginalPrice,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Text("${stats!!.total}条评价", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                                    }
                                }
                            }
                            if (reviews.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Divider(color = BrandBorder)
                                reviews.take(10).forEach { review ->
                                    Spacer(Modifier.height(12.dp))
                                    Row {
                                        Icon(Icons.Default.Star, null, tint = BrandPrice, modifier = Modifier.size(16.dp))
                                        Text(" ${review.rating}分", style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary))
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(review.content, style = TextStyle(fontSize = 14.sp, color = BrandText))
                                    Spacer(Modifier.height(4.dp))
                                    Text(review.createdAt.take(10), style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                                    if (review.reply != null && review.reply!!.isNotEmpty()) {
                                        Spacer(Modifier.height(4.dp))
                                        Surface(color = BrandBackground, shape = RoundedCornerShape(8.dp)) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("商家回复", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandSecondary))
                                                Text(review.reply!!, style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
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

        // Bottom action bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = { cartManager.addToCart(p, quantity) }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("加入购物车")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                cartManager.clearCart()
                cartManager.addToCart(p, quantity)
                navController.navigate("cart")
            }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("立即购买")
            }
        }
    }
}
