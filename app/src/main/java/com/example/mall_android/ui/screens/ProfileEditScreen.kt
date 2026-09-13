package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle

@Composable
fun ProfileEditScreen(authStore: AuthStore, apiClient: ApiClient, navController: NavController) {
    var nickname by remember { mutableStateOf(authStore.user?.nickname ?: "") }
    var phone by remember { mutableStateOf(authStore.user?.phone ?: "") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf("") }
    var changePw by remember { mutableStateOf(false) }

    Scaffold(topBar = {
        TopAppBar(
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.Default.ArrowBack, "返回") } },
            title = { Text("编辑资料", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("昵称") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("手机号") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = changePw, onCheckedChange = { changePw = it })
                Text("修改密码", style = TextStyle(fontSize = 14.sp))
            }

            if (changePw) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = oldPassword, onValueChange = { oldPassword = it }, label = { Text("原密码") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("新密码") }, modifier = Modifier.fillMaxWidth())
            }

            if (msg.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(msg, style = TextStyle(color = Color(0xFF43A047), fontSize = 13.sp))
            }

            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                loading = true; msg = ""
                val result = apiClient.updateMe(nickname.ifEmpty { null }, phone.ifEmpty { null })
                if (changePw && oldPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    apiClient.changePassword(oldPassword, newPassword).onSuccess { msg = "保存成功" }
                        .onFailure { loading = false; msg = it.message ?: "保存失败" }
                } else {
                    result.onSuccess { authStore.user = com.example.mall_android.model.User(id = authStore.user?.id ?: "", username = authStore.user?.username ?: "", nickname = nickname, role = authStore.user?.role ?: "", phone = phone) }
                        .onFailure { loading = false; msg = "保存失败" }
                }
                if (!changePw || newPassword.isEmpty()) loading = false
            }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !loading) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("保存", fontSize = 16.sp)
            }
        }
    }
}
