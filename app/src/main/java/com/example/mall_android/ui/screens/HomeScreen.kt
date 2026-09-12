package com.example.mall_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
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

@Composable
fun HomeScreen(apiClient: ApiClient, cartManager: CartManager, navController: NavController, cartItemCount: Int) {
    var banners by remember { mutableStateOf<List<Banner>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var popular by remember { mutableStateOf<List<Product>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true; error = ""
        val bannerResult = apiClient.getBanners()
        val catResult = apiClient.getCategories()
        val popResult = apiClient.getPopular()
        bannerResult.onSuccess { banners = it }
        catResult.onSuccess { categories = it }
        popResult.onSuccess { popular = it }
        loading = false
    }

    if (loading) { LoadingView(); return }
    if (error.isNotEmpty()) { ErrorView(error); return }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("商城", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandPrimary)
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Banner Carousel
            if (banners.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.pager.HorizontalPager(
                        state = androidx.compose.foundation.pager.rememberPagerState { banners.size },
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                    ) { page ->
                        val banner = banners[page]
                        Box(
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                .clickable { if (banner.link.isNotEmpty()) navController.navigate("product/${banner.link}") },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(ioObject = banner.image).crossfade(true).build(),
                                contentDescription = banner.title,
                                modifier = Modifier.fillMaxSize().background(BrandBorder),
                                contentScale = ContentScale.Crop
                            )
                            Box(modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f)))
                            ))
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                                Text(banner.title, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White))
                                if (banner.subtitle.isNotEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(banner.subtitle, style = TextStyle(fontSize = 13.sp, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Categories
            if (categories.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("分类", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandText), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
                item {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            Column(
                                modifier = Modifier
                                    .width(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { navController.navigate("category/${cat.id}") }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(ioObject = cat.icon).crossfade(true).build(),
                                    contentDescription = cat.name,
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(22.dp)).background(BrandSurface),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(cat.name, style = TextStyle(fontSize = 11.sp, color = BrandText), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Popular Products
            if (popular.isNotEmpty()) {
                item {
                    Text("热销推荐", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandText), modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                }
                items(popular) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12dp))
                            .clickable { navController.navigate("product/${product.id}") },
                        colors = CardDefaults.cardColors(containerColor = BrandSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.height(100.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(ioObject = product.image).crossfade(true).build(),
                                contentDescription = product.name,
                                modifier = Modifier.size(100.dp).background(BrandBorder),
                                contentScale = ContentScale.Crop
                            )
                            Column(modifier = Modifier.weight(1f).padding(10.dp), verticalArrangement = Arrangement.Center) {
                                Text(product.name, maxLines = 2, overflow = TextOverflow.Ellipsis, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = BrandText))
                                Spacer(Modifier.height(4dp))
                                PriceText(product.discountedPrice ?: product.originalPrice, product.discountedPrice ?: null, 16f)
                                Spacer(Modifier.height(4.dp))
                                Text("已售 ${product.stock} 件", style = TextStyle(fontSize = 11.sp, color = BrandTextSecondary))
                            }
                        }
                    }
                }
            }
        }
    }
}
