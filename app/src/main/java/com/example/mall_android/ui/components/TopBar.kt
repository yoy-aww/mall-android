package com.example.mall_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mall_android.ui.theme.*

@Composable
fun AppTopBar(
    onSearchClick: () -> Unit,
    onCartClick: () -> Unit,
    cartItemCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandAccent),
            contentAlignment = Alignment.Center
        ) {
            Text("枸", style = MaterialTextStyle(16.sp, FontWeight.Bold, Color.White))
        }
        Spacer(Modifier.width(10.dp))
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Search,
                    null,
                    tint = BrandPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "搜索好草本",
                    style = MaterialTextStyle(14.sp, color = Color.Gray),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier.clickable(onClick = onCartClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            if (cartItemCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 2.dp)
                        .clip(CircleShape)
                        .background(BrandAccent)
                        .size(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$cartItemCount", style = MaterialTextStyle(9.sp, FontWeight.Bold, Color.White))
                }
            }
        }
    }
}

@Composable
fun AppNavBar(
    onHome: () -> Unit,
    onProducts: () -> Unit,
    onWelfare: () -> Unit,
    onHerbs: () -> Unit,
    onHealth: () -> Unit,
    onActivity: () -> Unit,
    onProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandPrimaryDark)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        NavTabItem("首页", onHome)
        NavTabItem("全部商品", onProducts)
        NavTabItem("惠民专区", onWelfare)
        NavTabItem("中药材", onHerbs)
        NavTabItem("保健品", onHealth)
        NavTabItem("活动专区", onActivity)
        NavTabItem("个人中心", onProfile)
    }
}

@Composable
private fun NavTabItem(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = MaterialTextStyle(13.sp, color = Color.White.copy(alpha = 0.8f)))
    }
}