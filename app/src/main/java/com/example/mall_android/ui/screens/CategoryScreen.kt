package com.example.mall_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController

@Composable
fun CategoryScreen(categoryId: String, apiClient: ApiClient, cartManager: CartManager, navController: NavController, cartItemCount: Int) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        if (categoryId == "products") {
            // "全部商品" — show all products
            apiClient.getProducts().onSuccess { products = it }
        } else {
            apiClient.getProductsByCategory(categoryId).onSuccess { products = it }
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
                title = { Text("商品列表", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        }
    ) { padding ->
        if (loading) LoadingView()
        else if (products.isEmpty()) EmptyView("该分类暂无商品")
        else ProductGrid(products) { product -> navController.navigate("product/${product.id}") }
    }
}
