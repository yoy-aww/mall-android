package com.example.mall_android.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mall_android.model.Product
import com.example.mall_android.ui.theme.BrandBackground
import com.example.mall_android.ui.theme.BrandBorder
import com.example.mall_android.ui.theme.BrandOriginalPrice
import com.example.mall_android.ui.theme.BrandPrice
import com.example.mall_android.ui.theme.BrandSurface
import com.example.mall_android.ui.theme.BrandTag
import com.example.mall_android.ui.theme.BrandTagBg
import com.example.mall_android.ui.theme.BrandText
import com.example.mall_android.ui.theme.BrandTextSecondary
import com.example.mall_android.ui.theme.StatusCancelled
import com.example.mall_android.ui.theme.StatusCompleted
import com.example.mall_android.ui.theme.StatusDelivered
import com.example.mall_android.ui.theme.StatusPaid
import com.example.mall_android.ui.theme.StatusPending
import com.example.mall_android.ui.theme.StatusRefund
import com.example.mall_android.ui.theme.StatusShipped
import androidx.compose.ui.text.TextStyle

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
                model = product.image,
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
                    style = MaterialTextStyle(13.sp, FontWeight.Medium, BrandText)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "¥${String.format("%.2f", product.discountedPrice ?: product.originalPrice)}",
                        style = MaterialTextStyle(18.sp, FontWeight.Bold, BrandPrice)
                    )
                    if (product.discountedPrice != null && product.discountedPrice != product.originalPrice) {
                        Text(
                            "¥${String.format("%.2f", product.originalPrice)}",
                            style = MaterialTextStyle(12.sp, FontWeight.Normal, BrandOriginalPrice).copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
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
                                    style = MaterialTextStyle(10.sp, FontWeight.Normal, BrandTag),
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
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
        Text(
            "¥",
            style = MaterialTextStyle((size - 4).sp, FontWeight.Medium, BrandPrice)
        )
        Text(
            String.format("%.2f", price),
            style = MaterialTextStyle(size.sp, FontWeight.Bold, BrandPrice)
        )
        if (originalPrice != null && originalPrice > price) {
            Text(
                "¥${String.format("%.2f", originalPrice)}",
                style = MaterialTextStyle((size - 4).sp, FontWeight.Normal, BrandOriginalPrice).copy(
                    textDecoration = TextDecoration.LineThrough
                ),
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
        Text(
            label,
            style = MaterialTextStyle(11.sp, FontWeight.Medium, color),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
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
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("😢", style = MaterialTextStyle(48.sp, FontWeight.Normal, BrandText))
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            style = MaterialTextStyle(14.sp, FontWeight.Normal, BrandTextSecondary)
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) { Text("重试") }
        }
    }
}

@Composable
fun EmptyView(message: String = "暂无数据") {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("🛒", style = MaterialTextStyle(48.sp, FontWeight.Normal, BrandText))
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            textAlign = TextAlign.Center,
            style = MaterialTextStyle(14.sp, FontWeight.Normal, BrandTextSecondary)
        )
    }
}

private fun MaterialTextStyle(
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified
): TextStyle {
    val base = TextStyle(
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color
    )
    return base
}
