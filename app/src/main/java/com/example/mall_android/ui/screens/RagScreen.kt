package com.example.mall_android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Store
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mall_android.ApiClient
import com.example.mall_android.AuthStore
import com.example.mall_android.ui.components.MaterialTextStyle
import com.example.mall_android.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<ApiClient.RagSource> = emptyList(),
    val productIds: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@Composable
fun RagScreen(
    authStore: AuthStore,
    apiClient: ApiClient,
    navController: NavController,
    onClose: () -> Unit
) {
    var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BrandBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI 助手",
                    style = MaterialTextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandText
                    )
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "关闭",
                        tint = BrandTextSecondary
                    )
                }
            }

            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Welcome message
                item {
                    BotMessageBubble(
                        text = "你好！我是商城AI助手，有什么可以帮你的吗？",
                        navController = navController
                    )
                }

                // User and AI messages
                items(messages) { message ->
                    if (message.isUser) {
                        UserMessageBubble(text = message.text)
                    } else {
                        BotMessageBubble(
                            text = message.text,
                            sources = message.sources,
                            productIds = message.productIds,
                            isLoading = message.isLoading,
                            navController = navController
                        )
                    }
                }
            }

            // Input area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = {
                        Text(
                            text = "输入你的问题...",
                            style = MaterialTextStyle(
                                fontSize = 14.sp,
                                color = BrandTextSecondary
                            )
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedTextColor = BrandText,
                        unfocusedTextColor = BrandText
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val question = inputText.trim()
                        if (question.isEmpty()) return@Button

                        scope.launch {
                            // Clear input
                            inputText = ""

                            // Add user message
                            messages = messages + ChatMessage(
                                text = question,
                                isUser = true
                            )

                            // Add loading placeholder
                            messages = messages + ChatMessage(
                                text = "",
                                isUser = false,
                                isLoading = true
                            )

                            // Call API
                            val result = withContext(Dispatchers.IO) {
                                apiClient.ragAsk(question)
                            }

                            // Remove loading message and add result
                            messages = messages.dropLast(1)

                            result.onSuccess { ragAnswer ->
                                messages = messages + ChatMessage(
                                    text = ragAnswer.answer,
                                    isUser = false,
                                    sources = ragAnswer.sources,
                                    productIds = ragAnswer.productIds
                                )
                            }.onFailure { e ->
                                messages = messages + ChatMessage(
                                    text = "抱歉，出错了：${e.message ?: "未知错误"}",
                                    isUser = false
                                )
                            }
                        }
                    },
                    enabled = inputText.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimary,
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "发送"
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMessageBubble(
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            modifier = Modifier
                .background(BrandPrimary, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTextStyle(
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun BotMessageBubble(
    text: String,
    sources: List<ApiClient.RagSource> = emptyList(),
    productIds: List<String> = emptyList(),
    isLoading: Boolean = false,
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = BrandSurface,
                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                )
                .border(1.dp, BrandBorder, RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            if (isLoading) {
                // Loading indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = BrandPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "思考中...",
                        style = MaterialTextStyle(
                            fontSize = 13.sp,
                            color = BrandTextSecondary
                        )
                    )
                }
            } else {
                // Answer text
                Text(
                    text = text,
                    style = MaterialTextStyle(
                        fontSize = 14.sp,
                        color = BrandText
                    )
                )

                // Sources
                if (sources.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "参考来源",
                        style = MaterialTextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandTextSecondary
                        )
                    )
                    sources.forEachIndexed { index, source ->
                        Spacer(Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "来源 ${index + 1}: ${source.doc}",
                                style = MaterialTextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandPrimary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "相关度: ${(source.score * 100).toInt()}%",
                                style = MaterialTextStyle(
                                    fontSize = 11.sp,
                                    color = BrandTextSecondary
                                )
                            )
                            if (source.text.isNotBlank()) {
                                Text(
                                    text = source.text,
                                    style = MaterialTextStyle(
                                        fontSize = 11.sp,
                                        color = BrandTextSecondary
                                    ),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Product links
                if (productIds.isNotEmpty() && navController != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "推荐商品",
                        style = MaterialTextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BrandTextSecondary
                        )
                    )
                    productIds.forEach { productId ->
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            onClick = {
                                navController.navigate("product/$productId")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = BrandBackground,
                            border = BorderStroke(1.dp, BrandBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Store,
                                    contentDescription = null,
                                    tint = BrandAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "查看商品 ${productId}",
                                    style = MaterialTextStyle(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BrandPrimary
                                    )
                                )
                                Spacer(Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = BrandTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
