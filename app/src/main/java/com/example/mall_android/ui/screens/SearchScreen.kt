package com.example.mall_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.Product
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController

@Composable
fun SearchScreen(apiClient: ApiClient, navController: NavController, cartManager: CartManager, cartItemCount: Int) {
    var query by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        apiClient.getProducts().onSuccess { products = it }
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
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            if (it.length > 1) {
                                LaunchedEffect(it) {
                                    loading = true
                                    hasSearched = true
                                    apiClient.searchProducts(it).onSuccess { results -> searchResults = results }
                                    loading = false
                                }
                            } else {
                                hasSearched = false
                                searchResults = emptyList()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索商品...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        }
    ) { padding ->
        if (loading) {
            LoadingView()
        } else if (hasSearched && searchResults.isEmpty()) {
            EmptyView("没有找到相关商品")
        } else if (hasSearched) {
            ProductGrid(searchResults) { product ->
                navController.navigate("product/${product.id}")
            }
        } else if (products.isNotEmpty()) {
            Text("全部商品", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
            ProductGrid(products) { product ->
                navController.navigate("product/${product.id}")
            }
        } else {
            EmptyView()
        }
    }
}
