package com.example.mall_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mall_android.ui.screens.AddressesScreen
import com.example.mall_android.ui.screens.AddressEditScreen
import com.example.mall_android.ui.screens.AfterSalesScreen
import com.example.mall_android.ui.screens.AuthScreen
import com.example.mall_android.ui.screens.CartScreen
import com.example.mall_android.ui.screens.CategoryScreen
import com.example.mall_android.ui.screens.CheckoutScreen
import com.example.mall_android.ui.screens.HomeScreen
import com.example.mall_android.ui.screens.NotificationsScreen
import com.example.mall_android.ui.screens.OrderDetailScreen
import com.example.mall_android.ui.screens.OrdersScreen
import com.example.mall_android.ui.screens.PaymentScreen
import com.example.mall_android.ui.screens.ProductDetailScreen
import com.example.mall_android.ui.screens.ProfileEditScreen
import com.example.mall_android.ui.screens.ProfileScreen
import com.example.mall_android.ui.screens.ProductListScreen
import com.example.mall_android.ui.screens.SearchScreen
import com.example.mall_android.ui.theme.BrandPrimary
import com.example.mall_android.ui.theme.MallTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authStore = AuthStore(this)
        val apiClient = ApiClient(authStore)
        val cartManager = CartManager.getInstance()

        setContent {
            MallTheme {
                MallApp(apiClient = apiClient, authStore = authStore, cartManager = cartManager)
            }
        }
    }
}

@Composable
fun MallApp(apiClient: ApiClient, authStore: AuthStore, cartManager: CartManager) {
    val navController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }
    var cartItemCount by remember { mutableIntStateOf(cartManager.totalItems) }

    Scaffold(
        bottomBar = {
            NavigationBar(tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("home") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Filled.Home, null) },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("products") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Filled.ShoppingBag, null) },
                    label = { Text("商品") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("cart") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    },
                    icon = {
                        Box {
                            Icon(Icons.Filled.ShoppingCart, null)
                            if (cartItemCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 4.dp)
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(BrandPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("$cartItemCount", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    },
                    label = { Text("购物车") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        navController.navigate("profile") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Filled.Person, null) },
                    label = { Text("我的") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                HomeScreen(apiClient, cartManager, navController, cartItemCount)
            }
            composable("search") {
                SearchScreen(apiClient, navController, cartManager, cartItemCount)
            }
            composable("products") {
                ProductListScreen(apiClient, cartManager, navController, cartItemCount)
            }
            composable("products/{cat}") { backStack ->
                val cat = backStack.arguments?.getString("cat") ?: ""
                ProductListScreen(apiClient, cartManager, navController, cartItemCount, initialCat = cat)
            }
            composable("cart") {
                CartScreen(apiClient, cartManager, navController, cartItemCount)
            }
            composable("profile") {
                ProfileScreen(authStore, apiClient, navController)
            }
            composable("product/{id}") { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                ProductDetailScreen(id, apiClient, cartManager, navController, cartItemCount)
            }
            composable("category/{id}") { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                CategoryScreen(id, apiClient, cartManager, navController, cartItemCount)
            }
            composable("auth") {
                AuthScreen(apiClient, authStore, navController)
            }
            composable("checkout/{addressId}") { backStack ->
                val addressId = backStack.arguments?.getString("addressId") ?: ""
                CheckoutScreen(addressId, apiClient, cartManager, authStore, navController)
            }
            composable("payment/{orderId}") { backStack ->
                val id = backStack.arguments?.getString("orderId") ?: ""
                PaymentScreen(id, apiClient, authStore, navController)
            }
            composable("orders") {
                OrdersScreen(apiClient, authStore, navController)
            }
            composable("order/{id}") { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                OrderDetailScreen(id, apiClient, authStore, navController)
            }
            composable("addresses") {
                AddressesScreen(apiClient, navController)
            }
            composable("address-edit") {
                AddressEditScreen("", apiClient, navController)
            }
            composable("address-edit/{id}") { backStack ->
                val id = backStack.arguments?.getString("id") ?: ""
                AddressEditScreen(id, apiClient, navController)
            }
            composable("aftersales") {
                AfterSalesScreen(apiClient, authStore, navController)
            }
            composable("notifications") {
                NotificationsScreen(apiClient, authStore, navController)
            }
            composable("profile-edit") {
                ProfileEditScreen(authStore, apiClient, navController)
            }
        }
    }
}
