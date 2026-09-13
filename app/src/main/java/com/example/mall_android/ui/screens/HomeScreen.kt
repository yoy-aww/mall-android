package com.example.mall_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
            apiClient.getBanners().onSuccess { banners = it }
                .onFailure { android.util.Log.e("Home", "banners: ${it.message}") }
            apiClient.getCategories().onSuccess { categories = it }
                .onFailure { android.util.Log.e("Home", "categories: ${it.message}") }
            apiClient.getPopular().onSuccess { popular = it }
                .onFailure { android.util.Log.e("Home", "popular: ${it.message}") }
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
            // ===== TopBar =====
            item {
                AppTopBar(
                    onSearchClick = { navController.navigate("search") },
                    onCartClick = { navController.navigate("cart") },
                    cartItemCount = cartItemCount
                )
            }

            // ===== Nav (matches Web App.tsx nav → /products?cat=xxx) =====
            item {
                AppNavBar(
                    onHome = { navController.navigate("home") { popUpTo(0) { inclusive = true } } },
                    onProducts = { navController.navigate("products") { popUpTo(0) { inclusive = true } } },
                    onWelfare = { navController.navigate("products/welfare") },
                    onTea = { navController.navigate("products/tea") },
                    onHerbs = { navController.navigate("products/herbs") },
                    onHealth = { navController.navigate("products/health") },
                    onActivity = { navController.navigate("products/activity") },
                    onSupplements = { navController.navigate("products/supplements") },
                    onProfile = { navController.navigate("profile") }
                )
            }

            // ===== Banner Carousel =====
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
                                    navigateFromBanner(banner.link, navController)
                                }
                        ) {
                            AsyncImage(
                                model = banner.image,
                                contentDescription = banner.title,
                                modifier = Modifier.fillMaxSize().background(BrandBorder),
                                contentScale = ContentScale.Crop
                            )
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

            // ===== Category Navigation (matches Web Home.tsx cat-grid → /products?cat=xxx) =====
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
                                            .clickable { navController.navigate("products/${cat.id}") }
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

            // ===== Popular Products (matches Web Home.tsx prod-grid) =====
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
                                    ProductCard(product, modifier = Modifier.weight(1f)) {
                                        navController.navigate("product/${product.id}")
                                    }
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("products/") }
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

// ===== Banner link navigation (matches Web App.tsx convertMiniPath logic) =====
// /pages/category/category?type=xxx → products/xxx (product list with filter)
// /pages/product/product?id=xxx → product/xxx (product detail)
// empty link → no action
private fun navigateFromBanner(link: String, navController: NavController) {
    if (link.isEmpty()) return
    when {
        link.contains("product/product?id=") -> {
            val id = link.substringAfter("product/product?id=")
            navController.navigate("product/$id")
        }
        link.contains("type=") -> {
            val type = link.substringAfter("type=")
            navController.navigate("products/$type")
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