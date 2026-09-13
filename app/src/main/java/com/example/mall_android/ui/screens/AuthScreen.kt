package com.example.mall_android.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.*
import com.example.mall_android.model.*
import com.example.mall_android.ui.components.*
import com.example.mall_android.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.Color

@Composable
fun AuthScreen(apiClient: ApiClient, authStore: AuthStore, navController: NavController) {
    var isLogin by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                title = { Text(if (isLogin) "登录" else "注册", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize(), verticalArrangement = Arrangement.Center) {
            // Brand
            Text("商城", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = BrandPrimary), modifier = Modifier.align(Alignment.CenterHorizontally))
            Text("登录或注册以继续", style = TextStyle(fontSize = 14.sp, color = BrandTextSecondary), modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("用户名") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("密码") }, leadingIcon = { Icon(Icons.Default.Lock, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            if (!isLogin) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = nickname, onValueChange = { nickname = it }, label = { Text("昵称（可选）") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("手机号（可选）") }, modifier = Modifier.fillMaxWidth())
            }

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(error, style = TextStyle(color = Color.Red, fontSize = 13.sp))
            }

            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                loading = true; error = ""
                if (isLogin) {
                    apiClient.login(username, password).onSuccess {
                        authStore.token = it.token
                        authStore.user = it.user
                        navController.navigateUp()
                    }.onFailure { loading = false; error = it.message ?: "登录失败" }
                } else {
                    apiClient.register(username, password, nickname.ifEmpty { null }, phone.ifEmpty { null }).onSuccess {
                        navController.navigateUp()
                    }.onFailure { loading = false; error = it.message ?: "注册失败" }
                }
            }, modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !loading) {
                if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text(if (isLogin) "登录" else "注册", fontSize = 16.sp)
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if (isLogin) "没有账号？去注册" else "已有账号？去登录", color = BrandPrimary, fontSize = 14.sp)
            }
        }
    }
}
