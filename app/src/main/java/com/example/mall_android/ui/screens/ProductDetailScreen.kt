package com.example.mall_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProductDetailScreen(productId: String, apiClient: ApiClient, cartManager: CartManager, navController: NavController, cartItemCount: Int) {
    var product by remember { mutableStateOf<Product?>(null) }
    var categoryName by remember { mutableStateOf("") }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var stats by remember { mutableStateOf<ReviewStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        loading = true; error = ""
        withContext(Dispatchers.IO) {
            val productResult = apiClient.getProduct(productId)
            productResult.onSuccess { p ->
                product = p
                if (p.categoryId.isNotEmpty()) {
                    apiClient.getCategories().onSuccess { cats ->
                        val c = cats.find { it.id == p.categoryId }
                        if (c != null) categoryName = c.name
                    }
                }
            }.onFailure { error = "商品不存在或加载失败" }
            apiClient.getReviews(productId).onSuccess { reviews = it }
                .onFailure { reviews = emptyList() }
            apiClient.getReviewStats(productId).onSuccess { stats = it }
                .onFailure { stats = null }
        }
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
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("加载中…", style = MaterialTextStyle(14.sp, color = BrandTextSecondary))
                }
            }
            return@Scaffold
        }
        val p = product ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😵", style = MaterialTextStyle(48.sp, color = BrandText))
                    Spacer(Modifier.height(12.dp))
                    Text(error.ifEmpty { "加载失败" }, style = MaterialTextStyle(14.sp, color = BrandTextSecondary))
                }
            }
            return@Scaffold
        }

        val price = p.discountedPrice ?: p.originalPrice
        val save = if (p.discountedPrice != null) (p.originalPrice - price) else 0.0

        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            item {
                AsyncImage(
                    model = p.image,
                    contentDescription = p.name,
                    modifier = Modifier.fillMaxWidth().height(260.dp).background(BrandBorder),
                    contentScale = ContentScale.Crop
                )
            }

            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                    if (p.tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            p.tags.forEach { tag ->
                                Surface(color = BrandTagBg, shape = RoundedCornerShape(999.dp)) {
                                    Text(tag, style = MaterialTextStyle(12.sp, color = BrandTag), modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp))
                                }
                            }
                        }
                    }

                    Text(p.name, style = MaterialTextStyle(22.sp, FontWeight.Bold, BrandPrimaryDark), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    if (categoryName.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text("分类：$categoryName", style = MaterialTextStyle(13.sp, color = BrandTextSecondary))
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("¥", style = MaterialTextStyle(14.sp, FontWeight.Medium, BrandPrice))
                        Text(
                            String.format("%.2f", price),
                            style = MaterialTextStyle(30.sp, FontWeight.Bold, BrandPrice)
                        )
                        if (p.discountedPrice != null) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "¥${String.format("%.2f", p.originalPrice)}",
                                style = MaterialTextStyle(14.sp, color = BrandTextSecondary).copy(textDecoration = TextDecoration.LineThrough)
                            )
                        }
                        if (save > 0) {
                            Spacer(Modifier.width(8.dp))
                            Surface(color = BrandDiscountBg, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "省 ¥${String.format("%.2f", save)}",
                                    style = MaterialTextStyle(12.sp, color = BrandAccent),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("库存：${p.stock} 件", style = MaterialTextStyle(13.sp, color = BrandTextSecondary))

                    if (p.description.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Surface(color = BrandTagBg, shape = RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("商品简介", style = MaterialTextStyle(13.sp, FontWeight.Bold, BrandPrimary))
                                Spacer(Modifier.height(6.dp))
                                Text(p.description, style = MaterialTextStyle(14.sp, color = BrandText))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("数量", style = MaterialTextStyle(14.sp, color = BrandText))
                        Spacer(Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier.size(32.dp)
                            ) { Text("-", style = MaterialTextStyle(16.sp)) }
                            Text(
                                quantity.toString(),
                                modifier = Modifier.width(40.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTextStyle(16.sp, FontWeight.Bold)
                            )
                            OutlinedButton(
                                onClick = { if (quantity < p.stock) quantity++ },
                                modifier = Modifier.size(32.dp)
                            ) { Text("+", style = MaterialTextStyle(16.sp)) }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row {
                        OutlinedButton(
                            onClick = { cartManager.addToCart(p, quantity) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier  .size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("加入购物车")
                        }
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = {
                                cartManager.clearCart()
                                cartManager.addToCart(p, quantity)
                                navController.navigate("cart")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("立即购买")
                        }
                    }
                }
            }

            if (stats != null || reviews.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    Surface(color = BrandSurface, shape = RoundedCornerShape(0.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("用户评价", style = MaterialTextStyle(18.sp, FontWeight.Bold, BrandPrimaryDark))
                                Spacer(Modifier.width(8.dp))
                                Text("(${stats?.total ?: 0})", style = MaterialTextStyle(13.sp, color = BrandTextSecondary))
                                Spacer(Modifier.weight(1f))
                            }

                            if (stats != null && stats!!.total > 0) {
                                Spacer(Modifier.height(14.dp))
                                Surface(
                                    color = BrandBackground,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    String.format("%.1f", stats!!.avg),
                                                    style = MaterialTextStyle(36.sp, FontWeight.Bold, BrandPrimaryDark)
                                                )
                                                Text("${stats!!.total} 条评价", style = MaterialTextStyle(12.sp, color = BrandTextSecondary))
                                            }
                                            Spacer(Modifier.width(20.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                repeat(5) { i ->
                                                    Text(
                                                        if (i < stats!!.avg.toInt()) "★" else "☆",
                                                        style = MaterialTextStyle(16.sp, color = if (i < stats!!.avg.toInt()) Color(0xFFFADB14) else Color.Gray),
                                                        textAlign = TextAlign.Start
                                                    )
                                                }
                                                Spacer(Modifier.height(6.dp))
                                                val total = stats!!.total.coerceAtLeast(1)
                                                val dist = stats!!.dist
                                                repeat(5) { i ->
                                                    val count = dist.getOrNull(i) ?: 0
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("${i + 1}星", style = MaterialTextStyle(12.sp, color = BrandTextSecondary), modifier = Modifier.width(28.dp))
                                                        LinearProgressIndicator(
                                                            progress = { if (total > 0) count.toFloat() / total else 0f },
                                                            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                            color = BrandPrimary,
                                                            trackColor = BrandTagBg
                                                        )
                                                        Text(count.toString(), style = MaterialTextStyle(12.sp, color = BrandTextSecondary), modifier = Modifier.width(24.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (reviews.isEmpty()) {
                                Spacer(Modifier.height(20.dp))
                                Text("暂无评价，快来抢沙发吧", style = MaterialTextStyle(14.sp, color = BrandTextSecondary), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                            }

                            if (reviews.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Divider(color = BrandBorder)
                                reviews.take(10).forEach { review ->
                                    Spacer(Modifier.height(14.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Brush.linearGradient(listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB)))),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                (review.nickname.ifEmpty { review.username }).take(1),
                                                style = MaterialTextStyle(14.sp, FontWeight.Bold, Color.White)
                                            )
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(review.nickname.ifEmpty { review.username }, style = MaterialTextStyle(14.sp, FontWeight.Bold, BrandText))
                                            Row {
                                                repeat(5) { i ->
                                                    Icon(
                                                        Icons.Default.Star,
                                                        null,
                                                        tint = if (i < review.rating) Color(0xFFFADB14) else Color.Gray,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(Modifier.weight(1f))
                                        Text(review.createdAt.take(10), style = MaterialTextStyle(12.sp, color = BrandTextSecondary))
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(review.content, style = MaterialTextStyle(14.sp, color = BrandText), maxLines = 5, overflow = TextOverflow.Ellipsis)
                                    if (!review.images.isNullOrEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            review.images.take(6).forEach { img ->
                                                AsyncImage(
                                                    model = img,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                    if (!review.reply.isNullOrEmpty()) {
                                        Spacer(Modifier.height(8.dp))
                                        Surface(color = BrandTagBg, shape = RoundedCornerShape(8.dp)) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("商家回复", style = MaterialTextStyle(12.sp, FontWeight.Bold, BrandPrimary))
                                                Text(review.reply!!, style = MaterialTextStyle(13.sp, color = BrandText))
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
    }
}