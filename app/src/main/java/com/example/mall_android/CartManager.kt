package com.example.mall_android

import com.example.mall_android.model.CartItem

class CartManager {
    companion object {
        private lateinit var instance: CartManager
        fun getInstance(): CartManager {
            if (::instance.isInitialized) return instance
            instance = CartManager()
            return instance
        }
    }

    private val _items = mutableListOf<CartItem>()
    val items: List<CartItem> get() = _items.toList()
    val totalItems: Int get() = _items.sumOf { it.quantity }

    fun addToCart(product: com.example.mall_android.model.Product, quantity: Int = 1) {
        val price = product.discountedPrice ?: product.originalPrice
        val existing = _items.find { it.productId == product.id }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            _items.add(CartItem(
                productId = product.id,
                productName = product.name,
                productImage = product.image,
                price = price,
                quantity = quantity
            ))
        }
    }

    fun removeFromCart(productId: String) {
        _items.removeAll { it.productId == productId }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val item = _items.find { it.productId == productId } ?: return
        if (quantity <= 0) removeFromCart(productId)
        else item.quantity = quantity
    }

    fun clearCart() = _items.clear()

    fun getProductIds(): Set<String> = _items.map { it.productId }.toSet()

    fun getQuantity(productId: String): Int = _items.find { it.productId == productId }?.quantity ?: 0

    fun clear() = clearCart()
}
