package com.example.mall_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.icons.Icons as M3Icons
import com.example.mall_android.ui.theme.MallTheme
import com.example.mall_android.ui.screens.*

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
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("首页") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("search") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    },
                    icon = { Icon(Icons.Default.Search, null) },
                    label = { Text("搜索") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        navController.navigate("cart") { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    },
                    icon = {
                        Badge(count = cartItemCount, containerColor = M3Icons.Default.Red400) {
                            Icon(Icons.Default.ShoppingCart, null)
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
                    icon = { Icon(Icons.Default.Person, null) },
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
