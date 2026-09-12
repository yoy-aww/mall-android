package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController

@Composable
fun AddressesScreen(apiClient: ApiClient, navController: NavController) {
    var addresses by remember { mutableStateOf<List<Address>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        apiClient.getAddresses().onSuccess { addresses = it }
        loading = false
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("地址管理", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (loading) { LoadingView() }
            else if (addresses.isEmpty()) {
                EmptyView("暂无地址")
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(addresses) { addr ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))
                                .clickable { navController.navigate("address-edit/${addr.id}") },
                            colors = CardDefaults.cardColors(containerColor = BrandSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(addr.receiverName, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                                    Spacer(Modifier.width(8.dp))
                                    Text(addr.receiverPhone, style = TextStyle(fontSize = 14.sp))
                                    if (addr.isDefault == 1) {
                                        Surface(color = BrandSuccess, shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(start = 8.dp)) {
                                            Text("默认", style = TextStyle(fontSize = 10.sp, color = Color.White), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Default.Edit, null, tint = BrandTextSecondary, modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("${addr.province}${addr.city}${addr.address}", style = TextStyle(fontSize = 13.sp, color = BrandTextSecondary))
                                if (addr.label.isNotEmpty()) {
                                    Text(addr.label, style = TextStyle(fontSize = 12.sp, color = BrandTextSecondary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                                }
                            }
                        }
                    }
                }
            }
            Button(onClick = { navController.navigate("address-edit/") }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("添加新地址", fontSize = 16.sp) }
        }
    }
}
