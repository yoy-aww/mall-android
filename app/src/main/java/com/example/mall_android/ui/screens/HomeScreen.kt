package com.example.mall_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.MaterialTextStyle
import com.example.mall_android.ui.theme.*

@Composable
fun HomeScreen(
    apiClient: ApiClient,
    cartManager: CartManager,
    navController: NavController,
    cartItemCount: Int
) {
    var banners by remember { mutableStateOf<List<Banner>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var popular by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        withContext(Dispatchers.IO) {
            val bannerResult = apiClient.getBanners()
            bannerResult.onSuccess { banners = it }
                .onFailure { android.util.Log.e("HomeScreen", "Banners failed: ${it.message}") }
            val catResult = apiClient.getCategories()
            catResult.onSuccess { categories = it }
                .onFailure { android.util.Log.e("HomeScreen", "Categories failed: ${it.message}") }
            val popResult = apiClient.getPopular()
            popResult.onSuccess { popular = it }
                .onFailure { android.util.Log.e("HomeScreen", "Popular failed: ${it.message}") }
        }
        loading = false
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🍵", style = MaterialTextStyle(24.sp))
                Spacer(Modifier.height(12.dp))
                Text("加载中…", style = MaterialTextStyle(14.sp, color = BrandTextSecondary))
            }
        }
        return
    }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BrandBackground)
        ) {
            // ===== TopBar (same as Web App.tsx) =====
            item {
                HomeTopBar(
                    onSearchClick = { navController.navigate("search") },
                    onCartClick = { navController.navigate("cart") },
                    cartItemCount = cartItemCount
                )
            }

            // ===== Nav (hardcoded tabs, same as Web App.tsx) =====
            item {
                NavTabBar(
                    onHome = { navController.navigate("home") { popUpTo(0) { inclusive = true } } },
                    onProducts = { navController.navigate("category/products") },
                    onWelfare = { navController.navigate("category/welfare") },
                    onHerbs = { navController.navigate("category/herbs") },
                    onHealth = { navController.navigate("category/health") },
                    onActivity = { navController.navigate("category/activity") },
                    onProfile = { navController.navigate("profile") }
                )
            }

            // ===== Banner Carousel (only on homepage, same as Web HeroBanner) =====
            if (banners.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    HorizontalPager(
                        state = rememberPagerState { banners.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) { page ->
                        val banner = banners[page]
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (banner.link.isNotEmpty()) {
                                        if (banner.link.contains("product/product?id=")) {
                                            val id = banner.link.substringAfter("product/product?id=")
                                            navController.navigate("product/$id")
                                        } else if (banner.link.contains("type=")) {
                                            val type = banner.link.substringAfter("type=")
                                            navController.navigate("category/$type")
                                        }
                                    }
                                }
                        ) {
                            AsyncImage(
                                model = banner.image,
                                contentDescription = banner.title,
                                modifier = Modifier.fillMaxSize().background(BrandBorder),
                                contentScale = ContentScale.Crop
                            )
                            // Gradient overlay (left dark → right transparent, matches Web 90deg)
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.6f),
                                            Color.Black.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                            )
                            // Banner title/subtitle (left-aligned, matches Web hero-content)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(20.dp)
                                    .fillMaxWidth(0.65f)
                            ) {
                                Text(
                                    banner.title,
                                    style = MaterialTextStyle(22.sp, FontWeight.Bold, Color.White),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (banner.subtitle.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        banner.subtitle,
                                        style = MaterialTextStyle(13.sp, color = Color.White.copy(alpha = 0.85f)),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            // Page indicator (bottom center, matches Web hero-dots)
                            Row(
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                banners.forEachIndexed { i, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (i == page) 8.dp else 6.dp)
                                            .clip(RoundedCornerShape(if (i == page) 4.dp else 3.dp))
                                            .background(
                                                if (i == page) BrandAccent else Color.White.copy(alpha = 0.4f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ===== Category Navigation (same as Web Home.tsx cat-grid) =====
            if (categories.isNotEmpty()) {
                item {
                    SectionHeader("品类导航", "按品类选好物")
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        categories.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { cat ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = BrandSurface,
                                        tonalElevation = 1.dp,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { navController.navigate("category/${cat.id}") }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                cat.name,
                                                style = MaterialTextStyle(13.sp, FontWeight.Medium, BrandPrimaryDark),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (cat.productCount > 0) {
                                                Spacer(Modifier.height(2.dp))
                                                Text(
                                                    "${cat.productCount}件",
                                                    style = MaterialTextStyle(11.sp, color = BrandTextSecondary)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ===== Popular Products (same as Web Home.tsx prod-grid) =====
            if (popular.isNotEmpty()) {
                item {
                    SectionHeader("热销好物", "精选道地本草")
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        popular.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { product ->
                                    HomeProductCard(product) {
                                        navController.navigate("product/${product.id}")
                                    }
                                }
                            }
                        }
                    }
                    // "查看全部商品 →" (same as Web see-more)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("category/products") }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "查看全部商品 →",
                            style = MaterialTextStyle(14.sp, FontWeight.Medium, BrandPrimary)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

// ===== Section Header (matches Web section-head with ::before yellow bar) =====
@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Yellow accent bar (matches Web ::before pseudo-element)
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(BrandAccent)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                title,
                style = MaterialTextStyle(18.sp, FontWeight.Bold, BrandPrimaryDark)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                style = MaterialTextStyle(12.sp, color = BrandTextSecondary)
            )
        }
    }
}

// ===== TopBar (matches Web App.tsx topbar) =====
@Composable
private fun HomeTopBar(
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo mark
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandAccent),
            contentAlignment = Alignment.Center
        ) {
            Text("枸", style = MaterialTextStyle(16.sp, FontWeight.Bold, Color.White))
        }
        Spacer(Modifier.width(10.dp))
        // Search box
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Search,
                    null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "搜索好草本",
                    style = MaterialTextStyle(14.sp, color = Color.Gray),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        // Cart icon with badge
        Box(
            modifier = Modifier.clickable(onClick = onCartClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            if (cartItemCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 2.dp)
                        .clip(CircleShape)
                        .background(BrandAccent)
                        .size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$cartItemCount", style = MaterialTextStyle(9.sp, FontWeight.Bold, Color.White))
                }
            }
        }
    }
}

// ===== Nav (hardcoded tabs, same as Web App.tsx nav) =====
@Composable
private fun NavTabBar(
    onHome: () -> Unit,
    onProducts: () -> Unit,
    onWelfare: () -> Unit,
    onHerbs: () -> Unit,
    onHealth: () -> Unit,
    onActivity: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandPrimaryDark)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        NavTab("首页", onHome)
        NavTab("全部商品", onProducts)
        NavTab("惠民专区", onWelfare)
        NavTab("中药材", onHerbs)
        NavTab("保健品", onHealth)
        NavTab("活动专区", onActivity)
        NavTab("个人中心", onProfile)
    }
}

@Composable
private fun NavTab(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTextStyle(13.sp, color = Color.White.copy(alpha = 0.8f)))
    }
}

// ===== Product Card (matches Web Home.tsx prod-card) =====
@Composable
private fun HomeProductCard(product: Product, onClick: () -> Unit) {
    val price = product.discountedPrice ?: product.originalPrice
    val discount = product.discountedPrice?.let {
        Math.round(price / product.originalPrice * 10)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = BrandSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image (160dp, matches Web prod-img height: 160px)
            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(BrandSecondary),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(12.dp, 17.dp, 17.dp, 17.dp)) {
                // Tags (up to 3, pill-shaped, matches Web prod-tags)
                if (product.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        product.tags.take(3).forEach { tag ->
                            Surface(
                                color = BrandTagBg,
                                shape = RoundedCornerShape(997.dp)
                            ) {
                                Text(
                                    tag,
                                    style = MaterialTextStyle(11.sp, color = BrandPrimary),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
                // Product name (2 lines max, matches Web prod-name)
                Text(
                    product.name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTextStyle(14.sp, FontWeight.Medium, BrandText)
                )
                Spacer(Modifier.height(7.dp))
                // Price row: now + old (strikethrough) + discount badge
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(bottom = 7.dp)
                ) {
                    // Current price
                    Text("¥", style = MaterialTextStyle(12.sp, FontWeight.Medium, BrandPrice))
                    Text(
                        String.format("%.2f", price),
                        style = MaterialTextStyle(18.sp, FontWeight.Bold, BrandPrice)
                    )
                    // Original price with strikethrough
                    if (product.discountedPrice != null && product.discountedPrice != product.originalPrice) {
                        Text(
                            "¥${String.format("%.2f", product.originalPrice)}",
                            style = MaterialTextStyle(12.sp, color = BrandTextSecondary).copy(
                                textDecoration = TextDecoration.LineThrough
                            ),
                            modifier = Modifier.padding(start = 7.dp, bottom = 7.dp)
                        )
                    }
                    // Discount badge (e.g. "8折")
                    if (discount != null) {
                        Surface(
                            color = BrandAccentLight,
                            shape = RoundedCornerShape(7.dp),
                            modifier = Modifier.padding(start = 7.dp)
                        ) {
                            Text(
                                "${discount}折",
                                style = MaterialTextStyle(11.sp, color = BrandAccent),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                // Stock (matches Web prod-stock)
                Text(
                    "库存 ${product.stock} 件",
                    style = MaterialTextStyle(12.sp, color = BrandTextSecondary)
                )
            }
        }
    }
}
