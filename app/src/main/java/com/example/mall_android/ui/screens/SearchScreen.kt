package com.example.mall_android.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mall_android.ApiClient
import com.example.mall_android.CartManager
import com.example.mall_android.model.Product
import com.example.mall_android.ui.components.EmptyView
import com.example.mall_android.ui.components.LoadingView
import com.example.mall_android.ui.components.ProductGrid
import com.example.mall_android.ui.theme.BrandSurface
import com.example.mall_android.ui.theme.BrandText
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun SearchScreen(apiClient: ApiClient, navController: NavController, cartManager: CartManager, cartItemCount: Int) {
    var query by remember { mutableStateOf("") }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        apiClient.getProducts().onSuccess { products = it }
        loading = false
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            hasSearched = true
            apiClient.searchProducts(searchQuery).onSuccess { results -> searchResults = results }
        } else {
            hasSearched = false
            searchResults = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, "返回")
                    }
                },
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { newValue ->
                            query = newValue
                            if (newValue.length > 1) {
                                searchQuery = newValue
                            } else {
                                searchQuery = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("搜索商品...") },
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
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
