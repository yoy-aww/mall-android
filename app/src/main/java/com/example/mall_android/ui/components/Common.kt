package com.example.mall_android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mall_android.ui.theme.*
import com.example.mall_android.model.Product

@Composable
fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(ioObject = product.image).crossfade(true).build(),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(BrandBorder),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandText),
                    modifier = Modifier.height(40.dp)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "¥${String.format("%.2f", product.discountedPrice ?: product.originalPrice)}",
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandPrice)
                    )
                    if (product.discountedPrice != null && product.discountedPrice != product.originalPrice) {
                        Text(
                            "¥${String.format("%.2f", product.originalPrice)}",
                            style = TextStyle(fontSize = 12.sp, color = BrandOriginalPrice, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                            modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                        )
                    }
                }
                if (product.tags.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Row {
                        product.tags.take(2).forEach { tag ->
                            Surface(
                                color = BrandTagBg,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    tag,
                                    style = TextStyle(fontSize = 10.sp, color = BrandTag),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGrid(products: List<Product>, onProductClick: (Product) -> Unit) {
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(products) { product ->
            ProductCard(product) { onProductClick(product) }
        }
    }
}

@Composable
fun PriceText(price: Double, originalPrice: Double? = null, size: Float = 16f) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("¥", style = TextStyle(fontSize = size - 4, color = BrandPrice, fontWeight = FontWeight.Medium))
        Text(String.format("%.2f", price), style = TextStyle(fontSize = size, fontWeight = FontWeight.Bold, color = BrandPrice))
        if (originalPrice != null && originalPrice > price) {
            Text(
                "¥${String.format("%.2f", originalPrice)}",
                style = TextStyle(fontSize = size - 4, color = BrandOriginalPrice, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                modifier = Modifier.padding(start = 4.dp, bottom = 1.dp)
            )
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "pending" -> StatusPending
        "paid" -> StatusPaid
        "shipped" -> StatusShipped
        "delivered" -> StatusDelivered
        "completed" -> StatusCompleted
        "cancelled" -> StatusCancelled
        "refund" -> StatusRefund
        else -> Color.Gray
    }
    val label = when (status) {
        "pending" -> "待付款"
        "paid" -> "已付款"
        "shipped" -> "已发货"
        "delivered" -> "待收货"
        "completed" -> "已完成"
        "cancelled" -> "已取消"
        "refund" -> "退款中"
        else -> status
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
        Text(label, style = TextStyle(fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String, onRetry: (() -> Unit)? = null) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Text("😢", style = TextStyle(fontSize = 48.sp))
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, style = TextStyle(color = BrandTextSecondary, fontSize = 14.sp))
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("重试") }
        }
    }
}

@Composable
fun EmptyView(message: String = "暂无数据") {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(48.dp))
        Text("🛒", style = TextStyle(fontSize = 48.sp))
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, style = TextStyle(color = BrandTextSecondary, fontSize = 14.sp))
    }
}

@Composable
fun PriceText(price: Double, originalPrice: Double? = null, size: Float = 16f) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("¥", style = TextStyle(fontSize = size - 4, color = BrandPrice, fontWeight = FontWeight.Medium))
        Text(String.format("%.2f", price), style = TextStyle(fontSize = size, fontWeight = FontWeight.Bold, color = BrandPrice))
        if (originalPrice != null && originalPrice > price) {
            Text(
                "¥${String.format("%.2f", originalPrice)}",
                style = TextStyle(fontSize = size - 4, color = BrandOriginalPrice, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                modifier = Modifier.padding(start = 4.dp, bottom = 1.dp)
            )
        }
    }
}
