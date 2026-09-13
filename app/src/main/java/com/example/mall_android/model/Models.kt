package com.example.mall_android.model

data class Banner(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val image: String = "",
    val link: String = "",
    val sortOrder: Int = 0,
    val enabled: Int = 0,
    val audioUrl: String = ""
)

data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = "",
    val productCount: Int = 0,
    val sortOrder: Int = 0
)

data class Product(
    val id: String = "",
    val name: String = "",
    val image: String = "",
    val originalPrice: Double = 0.0,
    val discountedPrice: Double? = null,
    val categoryId: String = "",
    val description: String = "",
    val stock: Int = 0,
    val tags: List<String> = emptyList()
)

data class Review(
    val id: String = "",
    val productId: String = "",
    val userId: String = "",
    val username: String = "",
    val nickname: String = "",
    val rating: Int = 0,
    val content: String = "",
    val images: List<String> = emptyList(),
    val reply: String? = null,
    val replyAt: String? = null,
    val createdAt: String = ""
)

data class ReviewStats(
    val avg: Double = 0.0,
    val total: Int = 0,
    val dist: List<Int> = emptyList()
)

data class Address(
    val id: String = "",
    val userId: String = "",
    val label: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val province: String = "",
    val city: String = "",
    val address: String = "",
    val isDefault: Int = 0
)

data class AfterSale(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val orderStatus: String = "",
    val items: List<AfterSaleItem> = emptyList(),
    val reason: String = "",
    val description: String = "",
    val images: List<String> = emptyList(),
    val status: String = "",
    val handleReason: String? = null,
    val createdAt: String = "",
    val handledAt: String? = null
)

data class AfterSaleItem(
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0
)

data class Notification(
    val id: String = "",
    val userId: String = "",
    val type: String = "",
    val title: String = "",
    val content: String = "",
    val relatedId: String? = null,
    val read: Int = 0,
    val createdAt: String = ""
)

data class User(
    val id: String = "",
    val username: String = "",
    val nickname: String = "",
    val role: String = "",
    val phone: String = ""
)

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    var quantity: Int = 1
)

data class OrderPreview(
    val items: List<OrderPreviewItem> = emptyList(),
    val subtotal: Double = 0.0,
    val shippingFee: Double = 0.0,
    val total: Double = 0.0,
    val free: Boolean = false,
    val shippingMethod: String = "standard"
)

data class OrderPreviewItem(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)

data class Order(
    val id: String = "",
    val userId: String = "",
    val status: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val shippingFee: Double = 0.0,
    val subtotal: Double = 0.0,
    val shippingMethod: String = "",
    val shippingAddress: String = "",
    val receiverName: String = "",
    val receiverPhone: String = "",
    val remark: String = "",
    val tracking: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0
)
