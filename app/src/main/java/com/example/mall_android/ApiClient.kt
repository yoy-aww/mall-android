package com.example.mall_android

import com.example.mall_android.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

class ApiClient(val authStore: AuthStore) {
    companion object {
        const val BASE_URL = "http://10.0.2.2:3456/api"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        private val gson = Gson()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private inline fun <T> request(path: String, method: String = "GET", body: String? = null): Result<T> {
        val url = "$BASE_URL$path"
        val requestBuilder = Request.Builder().url(url)
            .method(method, body?.toRequestBody(JSON) ?: null)
        authStore.token?.let { requestBuilder.header("Authorization", "Bearer $it") }
        requestBuilder.header("Accept", "application/json")

        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))
                val wrapper = gson.fromJson(responseBody, ApiResponse::class.java)
                if (wrapper?.success == true) {
                    val data = wrapper.data
                    Result.success(data)
                } else {
                    Result.failure(Exception(wrapper?.error ?: "HTTP ${response.code}"))
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private class ApiResponse<T>(val success: Boolean, val data: T?, val error: String?)

    private inline fun <T> listRequest(path: String): Result<List<T>> {
        val url = "$BASE_URL$path"
        val requestBuilder = Request.Builder().url(url).get()
        authStore.token?.let { requestBuilder.header("Authorization", "Bearer $it") }

        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val wrapper = gson.fromJson(body, object : TypeToken<ApiResponse<List<T>>>() {}.type)
                if (wrapper?.success == true) Result.success(wrapper.data ?: emptyList())
                else Result.failure(Exception(wrapper?.error ?: "Error"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun decodeJsonList(json: String?, type: Class<*>): List<Any> {
        return if (json.isNullOrEmpty()) emptyList()
        else runCatching { gson.fromJson(json, type) }.getOrDefault(emptyList())
    }

    // ========== Banners ==========
    fun getBanners(): Result<List<Banner>> = listRequest<Banner>("/banners")

    // ========== Categories ==========
    fun getCategories(): Result<List<Category>> = listRequest<Category>("/categories")

    // ========== Products ==========
    fun getProducts(): Result<List<Product>> = listRequest<Product>("/products")
    fun getProduct(id: String): Result<Product> = request<Product>("/products/$id")
    fun getProductsByCategory(categoryId: String): Result<List<Product>> = listRequest<Product>("/products/category/$categoryId")
    fun getPopular(): Result<List<Product>> = listRequest<Product>("/products/popular")
    fun searchProducts(query: String): Result<List<Product>> = listRequest<Product>("/products/search?q=${URLEncoder.encode(query, "UTF-8")}")
    fun getGroupedProducts(): Result<Map<String, List<Product>>> {
        val url = "$BASE_URL/products/grouped"
        val requestBuilder = Request.Builder().url(url).get()
        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val wrapper = gson.fromJson(body, object : TypeToken<ApiResponse<Map<String, List<Product>>>>() {}.type)
                if (wrapper?.success == true) Result.success(wrapper.data ?: emptyMap())
                else Result.failure(Exception(wrapper?.error ?: "Error"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // ========== Auth ==========
    fun login(username: String, password: String): Result<AuthResponse> =
        request<AuthResponse>("/auth/login", "POST", gson.toJson(mapOf("username" to username, "password" to password)))
    fun register(username: String, password: String, nickname: String? = null, phone: String? = null): Result<Map<String, Any>> {
        val body = mutableMapOf<String, String?>(
            "username" to username,
            "password" to password
        )
        nickname?.let { body["nickname"] = it }
        phone?.let { body["phone"] = it }
        return request("/auth/register", "POST", gson.toJson(body))
    }
    fun getMe(): Result<User> = request<User>("/auth/me")
    fun updateMe(nickname: String? = null, phone: String? = null): Result<Map<String, Any>> {
        val uid = authStore.user?.id ?: return Result.failure(Exception("Not logged in"))
        val body = mutableMapOf<String, String?>()
        nickname?.let { body["nickname"] = it }
        phone?.let { body["phone"] = it }
        return request("/auth/users/$uid", "PUT", gson.toJson(body))
    }
    fun changePassword(oldPassword: String, newPassword: String): Result<Map<String, Any>> =
        request("/auth/change-password", "POST", gson.toJson(mapOf("oldPassword" to oldPassword, "newPassword" to newPassword)))

    data class AuthResponse(val token: String, val user: User)

    // ========== Reviews ==========
    fun getReviews(productId: String): Result<List<Review>> = listRequest<Review>("/reviews?productId=$productId")
    fun getReviewStats(productId: String): Result<ReviewStats> = request<ReviewStats>("/reviews/product/$productId/stats")
    fun createReview(productId: String, rating: Int, content: String, images: List<String>? = null): Result<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "productId" to productId,
            "userId" to (authStore.user?.id ?: ""),
            "rating" to rating,
            "content" to content
        )
        images?.let { body["images"] = it }
        return request("/reviews", "POST", gson.toJson(body))
    }

    // ========== Orders ==========
    fun getMyOrders(): Result<PaginatedList<Order>> {
        val uid = authStore.user?.id ?: ""
        return request<PaginatedList<Order>>("/orders?userId=$uid")
    }
    fun getOrder(id: String): Result<Order> = request<Order>("/orders/$id")
    fun previewOrder(items: List<CartItem>, shippingMethod: String = "standard"): Result<OrderPreview> =
        request("/orders/preview", "POST", gson.toJson(mapOf("items" to items.map { mapOf("productId" to it.productId, "quantity" to it.quantity) }, "shippingMethod" to shippingMethod)))
    fun createOrder(items: List<CartItem>, shippingAddress: String, receiverName: String, receiverPhone: String, remark: String? = null, shippingMethod: String = "standard"): Result<OrderCreateResult> {
        val body = mutableMapOf<String, Any>(
            "items" to items.map { mapOf("productId" to it.productId, "quantity" to it.quantity) },
            "shippingAddress" to shippingAddress,
            "receiverName" to receiverName,
            "receiverPhone" to receiverPhone,
            "shippingMethod" to shippingMethod
        )
        remark?.let { body["remark"] = it }
        return request("/orders", "POST", gson.toJson(body))
    }
    fun payOrder(id: String): Result<Order> = request("/orders/$id/payment", "POST", "{}")
    fun cancelOrder(id: String, reason: String? = null): Result<Order> =
        request("/orders/$id/cancel", "POST", gson.toJson(mapOf("reason" to (reason ?: ""))))
    fun confirmDelivery(id: String): Result<Order> = request("/orders/$id/deliver", "POST", "{}")
    fun confirmOrder(id: String): Result<Order> = request("/orders/$id/confirm", "POST", "{}")

    data class PaginatedList<T>(val list: List<T>, val pagination: Map<String, Any>?)
    data class OrderCreateResult(val id: String, val subtotal: Double, val shippingFee: Double, val total: Double, val free: Boolean, val shippingMethod: String)

    // ========== Addresses ==========
    fun getAddresses(): Result<List<Address>> = listRequest<Address>("/addresses")
    fun createAddress(address: Address): Result<Map<String, Any>> =
        request("/addresses", "POST", gson.toJson(address))
    fun updateAddress(id: String, address: Address): Result<Map<String, Any>> =
        request("/addresses/$id", "PUT", gson.toJson(address))
    fun deleteAddress(id: String): Result<Map<String, Any>> =
        request("/addresses/$id", "DELETE", null)

    // ========== After-sales ==========
    fun getAfterSales(): Result<List<AfterSale>> = listRequest<AfterSale>("/aftersales")
    fun createAfterSale(orderId: String, items: List<AfterSaleItem>, reason: String, description: String): Result<Map<String, Any>> =
        request("/aftersales", "POST", gson.toJson(mapOf(
            "orderId" to orderId, "items" to items, "reason" to reason, "description" to description
        )))

    // ========== Notifications ==========
    fun getNotifications(): Result<NotificationResponse> = request<NotificationResponse>("/notifications")
    fun markNotificationRead(id: String): Result<Map<String, Any>> = request("/notifications/$id/read", "PUT", "{}")
    fun markAllNotificationsRead(): Result<Map<String, Any>> = request("/notifications/all/read", "PUT", "{}")

    data class NotificationResponse(val list: List<Notification>, val unread: Int)

    // ========== Upload ==========
    fun uploadImage(file: File): Result<Map<String, String>> {
        val token = authStore.token
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
            .build()
        val requestBuilder = Request.Builder()
            .url("$BASE_URL/upload")
            .post(requestBody)
        token?.let { requestBuilder.header("Authorization", "Bearer $it") }
        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val wrapper = gson.fromJson(body, object : TypeToken<ApiResponse<Map<String, String>>>() {}.type)
                if (wrapper?.success == true) Result.success(wrapper.data ?: emptyMap())
                else Result.failure(Exception(wrapper?.error ?: "Error"))
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}
