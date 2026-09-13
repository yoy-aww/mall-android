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
import androidx.compose.ui.text.TextStyle

@Composable
fun AddressEditScreen(addressId: String, apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var receiverName by remember { mutableStateOf("") }
    var receiverPhone by remember { mutableStateOf("") }
    var province by remember { mutableStateOf("北京市") }
    var city by remember { mutableStateOf("北京市") }
    var detailAddress by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val isNew = addressId.isEmpty()
    val isLoggedIn = authStore.isLoggedIn()

    LaunchedEffect(Unit) {
        if (!isNew) {
            apiClient.getAddresses().onSuccess { addresses ->
                addresses.find { it.id == addressId }?.let {
                    receiverName = it.receiverName
                    receiverPhone = it.receiverPhone
                    province = it.province
                    city = it.city
                    detailAddress = it.address
                    label = it.label
                    isDefault = it.isDefault == 1
                }
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text(if (isNew) "添加地址" else "编辑地址", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (!isLoggedIn) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("请先登录后再保存地址", style = TextStyle(fontSize = 13.sp, color = Color(0xFFE65100)))
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { navController.navigate("auth") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("去登录", fontSize = 14.sp)
                        }
                    }
                }
                return@Column
            }

            OutlinedTextField(value = receiverName, onValueChange = { receiverName = it }, label = { Text("收件人") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = receiverPhone, onValueChange = { receiverPhone = it }, label = { Text("手机号") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("省份") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("城市") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = detailAddress, onValueChange = { detailAddress = it }, label = { Text("详细地址") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("标签（如：家、公司）") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("设为默认地址", style = TextStyle(fontSize = 14.sp))
            }

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(error, style = TextStyle(color = Color.Red, fontSize = 13.sp))
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isNew) {
                    OutlinedButton(onClick = {
                        apiClient.deleteAddress(addressId).onSuccess { navController.navigateUp() }
                            .onFailure { error = it.message ?: "删除失败" }
                    }, modifier = Modifier.weight(1f)) { Text("删除") }
                }
                Button(onClick = {
                    loading = true; error = ""
                    if (receiverName.isBlank() || receiverPhone.isBlank() || detailAddress.isBlank()) {
                        loading = false; error = "请填写收件人、手机号和详细地址"
                        return@Button
                    }
                    val addr = Address(
                        id = if (isNew) "" else addressId,
                        userId = "",
                        label = label,
                        receiverName = receiverName,
                        receiverPhone = receiverPhone,
                        province = province,
                        city = city,
                        address = detailAddress,
                        isDefault = if (isDefault) 1 else 0
                    )
                    val result = if (isNew) apiClient.createAddress(addr) else apiClient.updateAddress(addressId, addr)
                    result.onSuccess { loading = false; navController.navigateUp() }
                        .onFailure { loading = false; error = it.message ?: "保存失败" }
                }, modifier = Modifier.weight(1f).height(48.dp), enabled = !loading) {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("保存", fontSize = 16.sp)
                }
            }
        }
    }
}
