package com.example.mall_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProductListScreen(
    apiClient: ApiClient,
    cartManager: CartManager,
    navController: NavController,
    cartItemCount: Int
) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        error = ""
        withContext(Dispatchers.IO) {
            val pr = apiClient.getProducts()
            pr.onSuccess { products = it }
                .onFailure { error = "加载失败，请稍后重试" }
            val cr = apiClient.getCategories()
            cr.onSuccess { categories = it }
                .onFailure { }
        }
        loading = false
    }

    val filtered = remember(products, selectedCat) {
        if (selectedCat.isEmpty()) products else products.filter { it.categoryId == selectedCat }
    }

    val curCat = categories.find { it.id == selectedCat }
    val title = curCat?.name ?: "全部商品"

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BrandBackground)
        ) {
            // ===== TopBar (same as HomeScreen) =====
            item {
                AppTopBar(
                    onSearchClick = { navController.navigate("search") },
                    onCartClick = { navController.navigate("cart") },
                    cartItemCount = cartItemCount
                )
            }
            // ===== Nav (same as HomeScreen) =====
            item {
                AppNavBar(
                    onHome = { navController.navigate("home") { popUpTo(0) { inclusive = true } } },
                    onProducts = { navController.navigate("products") { popUpTo(0) { inclusive = true } } },
                    onWelfare = { navController.navigate("category/welfare") },
                    onHerbs = { navController.navigate("category/herbs") },
                    onHealth = { navController.navigate("category/health") },
                    onActivity = { navController.navigate("category/activity") },
                    onProfile = { navController.navigate("profile") }
                )
            }

            // ===== Page header (matches Web page-head) =====
            if (!loading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            title,
                            style = MaterialTextStyle(24.sp, FontWeight.Bold, BrandPrimaryDark)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "共 ${filtered.size} 件",
                            style = MaterialTextStyle(13.sp, color = BrandTextSecondary)
                        )
                    }
                }
            }

            // ===== Filter bar (matches Web filter-bar with chips) =====
            if (!loading && categories.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // "全部" chip
                        FilterChip(
                            label = "全部",
                            selected = selectedCat.isEmpty(),
                            onClick = { selectedCat = "" },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                label = cat.name,
                                selected = selectedCat == cat.id,
                                onClick = { selectedCat = cat.id },
                                modifier = Modifier  .padding(end = 8.dp)
                            )
                        }
                    }
                }
            }

            // ===== Loading skeleton =====
            if (loading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // ===== Error state =====
            if (!loading && error.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("😵", style = MaterialTextStyle(48.sp, color = BrandText))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            error,
                            style = MaterialTextStyle(15.sp, color = BrandTextSecondary)
                        )
                    }
                }
            }

            // ===== Empty state =====
            if (!loading && error.isEmpty() && filtered.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛒", style = MaterialTextStyle(48.sp, color = BrandText))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "暂无商品",
                            style = MaterialTextStyle(15.sp, color = BrandTextSecondary)
                        )
                    }
                }
            }

            // ===== Product grid (2 columns, matches Web prod-grid) =====
            if (!loading && filtered.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        filtered.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEach { product ->
                                    ProductCard(product) {
                                        navController.navigate("product/${product.id}")
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) BrandPrimary else BrandSurface
    val textColor = if (selected) Color.White else BrandTextSecondary
    val borderColor = if (selected) BrandPrimary else BrandBorder

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTextStyle(13.sp, color = textColor),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}