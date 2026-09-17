package com.example.mall_android

import android.util.Log
import com.example.mall_android.model.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.File
import java.io.IOException
import java.net.URLEncoder

class ApiClient(val authStore: AuthStore) {
    companion object {
        const val BASE_URL = "http://43.153.148.187:3000/api"
        private val JSON = "application/json; charset=utf-8".toMediaType()
        val gson = Gson()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val req = chain.request()
            Log.d("MallApi", ">> ${req.method} ${req.url}")
            val resp = chain.proceed(req)
            Log.d("MallApi", "<< ${resp.code} ${resp.message}")
            resp
        }
        .build()

    private fun request(path: String, method: String = "GET", body: String? = null, type: Class<*>): Result<Any?> {
        val url = "$BASE_URL$path"
        val requestBuilder = Request.Builder().url(url)
            .method(method, body?.toRequestBody(JSON) ?: null)
        authStore.token?.let { requestBuilder.header("Authorization", "Bearer $it") }
        requestBuilder.header("Accept", "application/json")

        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string() ?: return Result.failure(Exception("Empty response"))
                val json = JsonParser.parseString(responseBody).asJsonObject
                if (json.get("success")?.asBoolean != true) {
                    return Result.failure(Exception(json.get("error")?.asString ?: "HTTP ${response.code}"))
                }
                val dataJson = json.get("data")
                if (dataJson == null || dataJson.isJsonNull) {
                    return Result.success(null)
                }
                Result.success(gson.fromJson(dataJson, type))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun <T> listRequest(path: String, type: Class<T>): Result<List<T>> {
        val url = "$BASE_URL$path"
        val requestBuilder = Request.Builder().url(url).get()
        authStore.token?.let { requestBuilder.header("Authorization", "Bearer $it") }

        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val json = JsonParser.parseString(body).asJsonObject
                if (json.get("success")?.asBoolean != true) {
                    return Result.failure(Exception(json.get("error")?.asString ?: "Error"))
                }
                val dataJson = json.get("data")
                if (dataJson == null || dataJson.isJsonNull) {
                    return Result.success(emptyList())
                }
                val listType = TypeToken.getParameterized(java.util.ArrayList::class.java, type).type
                val list = gson.fromJson<List<T>>(dataJson, listType) ?: emptyList()
                Result.success(list)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // ========== Banners ==========
    fun getBanners(): Result<List<Banner>> = listRequest("/banners", Banner::class.java)

    // ========== Categories ==========
    fun getCategories(): Result<List<Category>> = listRequest("/categories", Category::class.java)

    // ========== Products ==========
    fun getProducts(): Result<List<Product>> = listRequest("/products", Product::class.java)
    fun getProduct(id: String): Result<Product> = request("/products/$id", type = Product::class.java)
        .let { r -> r.map { it as? Product ?: Product() } }
    fun getProductsByCategory(categoryId: String): Result<List<Product>> = listRequest("/products/category/$categoryId", Product::class.java)
    fun getPopular(): Result<List<Product>> = listRequest("/products/popular", Product::class.java)
    fun searchProducts(query: String): Result<List<Product>> = listRequest("/products/search?q=${URLEncoder.encode(query, "UTF-8")}", Product::class.java)

    fun getGroupedProducts(): Result<Map<String, List<Product>>> {
        val url = "$BASE_URL/products/grouped"
        val requestBuilder = Request.Builder().url(url).get()
        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val json = JsonParser.parseString(body).asJsonObject
                if (json.get("success")?.asBoolean != true) {
                    return Result.failure(Exception(json.get("error")?.asString ?: "Error"))
                }
                val dataJson = json.get("data")
                if (dataJson == null || dataJson.isJsonNull) {
                    return Result.success(emptyMap())
                }
                val mapType = TypeToken.getParameterized(LinkedHashMap::class.java, String::class.java,
                    TypeToken.getParameterized(java.util.ArrayList::class.java, Product::class.java).type).type
                val map = gson.fromJson<Map<String, List<Product>>>(dataJson, mapType) ?: emptyMap()
                Result.success(map)
            }
        } catch (e: Exception) { Result.failure(e) }
    }

    // ========== Auth ==========
    fun login(username: String, password: String): Result<AuthResponse> {
        return request("/auth/login", "POST", gson.toJson(mapOf("username" to username, "password" to password)), AuthResponse::class.java)
            .let { r -> r.map { it as? AuthResponse ?: AuthResponse("", User()) } }
    }

    fun register(username: String, password: String, nickname: String? = null, phone: String? = null): Result<Map<String, Any>> {
        val body = mutableMapOf<String, String?>(
            "username" to username,
            "password" to password
        )
        nickname?.let { body["nickname"] = it }
        phone?.let { body["phone"] = it }
        return request("/auth/register", "POST", gson.toJson(body), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    }

    fun getMe(): Result<User> = request("/auth/me", type = User::class.java)
        .let { r -> r.map { it as? User ?: User() } }

    fun updateMe(nickname: String? = null, phone: String? = null): Result<Map<String, Any>> {
        val uid = authStore.user?.id ?: return Result.failure(Exception("Not logged in"))
        val body = mutableMapOf<String, String?>()
        nickname?.let { body["nickname"] = it }
        phone?.let { body["phone"] = it }
        return request("/auth/users/$uid", "PUT", gson.toJson(body), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    }

    fun changePassword(oldPassword: String, newPassword: String): Result<Map<String, Any>> {
        return request("/auth/change-password", "POST", gson.toJson(mapOf("oldPassword" to oldPassword, "newPassword" to newPassword)), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    }

    data class AuthResponse(val token: String, val user: User)

    // ========== Reviews ==========
    fun getReviews(productId: String): Result<List<Review>> = listRequest("/reviews?productId=$productId", Review::class.java)
    fun getReviewStats(productId: String): Result<ReviewStats> = request("/reviews/product/$productId/stats", type = ReviewStats::class.java)
        .let { r -> r.map { it as? ReviewStats ?: ReviewStats() } }
    fun createReview(productId: String, rating: Int, content: String, images: List<String>? = null): Result<Map<String, Any>> {
        val body = mutableMapOf<String, Any>(
            "productId" to productId,
            "userId" to (authStore.user?.id ?: ""),
            "rating" to rating,
            "content" to content
        )
        images?.let { body["images"] = it }
        return request("/reviews", "POST", gson.toJson(body), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    }

    // ========== Orders ==========
    fun getMyOrders(status: String? = null): Result<PaginatedList> {
        val uid = authStore.user?.id ?: ""
        val query = if (status.isNullOrEmpty()) "/orders?userId=$uid" else "/orders?userId=$uid&status=$status"
        return request(query, type = PaginatedList::class.java)
            .let { r -> r.map { it as? PaginatedList ?: PaginatedList() } }
    }

    fun getOrder(id: String): Result<Order> = request("/orders/$id", type = Order::class.java)
        .let { r -> r.map { it as? Order ?: Order() } }

    fun previewOrder(items: List<CartItem>, shippingMethod: String = "standard"): Result<OrderPreview> {
        return request("/orders/preview", "POST", gson.toJson(mapOf(
            "items" to items.map { mapOf("productId" to it.productId, "quantity" to it.quantity) },
            "shippingMethod" to shippingMethod
        )), OrderPreview::class.java)
            .let { r -> r.map { it as? OrderPreview ?: OrderPreview() } }
    }

    fun createOrder(items: List<CartItem>, shippingAddress: String, receiverName: String, receiverPhone: String, remark: String? = null, shippingMethod: String = "standard"): Result<OrderCreateResult> {
        val body = mutableMapOf<String, Any>(
            "items" to items.map { mapOf("productId" to it.productId, "quantity" to it.quantity) },
            "shippingAddress" to shippingAddress,
            "receiverName" to receiverName,
            "receiverPhone" to receiverPhone,
            "shippingMethod" to shippingMethod
        )
        remark?.let { body["remark"] = it }
        return request("/orders", "POST", gson.toJson(body), OrderCreateResult::class.java)
            .let { r -> r.map { it as? OrderCreateResult ?: OrderCreateResult() } }
    }

    fun payOrder(id: String): Result<Order> = request("/orders/$id/payment", "POST", "{}", type = Order::class.java)
        .let { r -> r.map { it as? Order ?: Order() } }
    fun cancelOrder(id: String, reason: String? = null): Result<Order> =
        request("/orders/$id/cancel", "POST", gson.toJson(mapOf("reason" to (reason ?: ""))), type = Order::class.java)
            .let { r -> r.map { it as? Order ?: Order() } }
    fun confirmDelivery(id: String): Result<Order> = request("/orders/$id/deliver", "POST", "{}", type = Order::class.java)
        .let { r -> r.map { it as? Order ?: Order() } }
    fun confirmOrder(id: String): Result<Order> = request("/orders/$id/confirm", "POST", "{}", type = Order::class.java)
        .let { r -> r.map { it as? Order ?: Order() } }

    data class PaginatedList(val list: List<Order> = emptyList(), val pagination: Map<String, Any>? = null)
    data class OrderCreateResult(val id: String = "", val subtotal: Double = 0.0, val shippingFee: Double = 0.0, val total: Double = 0.0, val free: Boolean = false, val shippingMethod: String = "")

    // ========== Addresses ==========
    fun getAddresses(): Result<List<Address>> = listRequest("/addresses", Address::class.java)
    fun createAddress(address: Address): Result<Map<String, Any>> =
        request("/addresses", "POST", gson.toJson(address), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    fun updateAddress(id: String, address: Address): Result<Map<String, Any>> =
        request("/addresses/$id", "PUT", gson.toJson(address), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    fun deleteAddress(id: String): Result<Map<String, Any>> =
        request("/addresses/$id", "DELETE", null, Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }

    // ========== After-sales ==========
    fun getAfterSales(): Result<List<AfterSale>> = listRequest("/aftersales", AfterSale::class.java)
    fun createAfterSale(orderId: String, items: List<AfterSaleItem>, reason: String, description: String): Result<Map<String, Any>> =
        request("/aftersales", "POST", gson.toJson(mapOf(
            "orderId" to orderId, "items" to items, "reason" to reason, "description" to description
        )), Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }

    // ========== Notifications ==========
    fun getNotifications(): Result<NotificationResponse> {
        return request("/notifications", type = NotificationResponse::class.java)
            .let { r -> r.map { it as? NotificationResponse ?: NotificationResponse() } }
    }

    fun markNotificationRead(id: String): Result<Map<String, Any>> {
        return request("/notifications/$id/read", "PUT", "{}", Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    }

    fun markAllNotificationsRead(): Result<Map<String, Any>> {
        return request("/notifications/all/read", "PUT", "{}", Map::class.java)
            .let { r -> r.map { it as? Map<String, Any> ?: emptyMap() } }
    }

    data class NotificationResponse(val list: List<Notification> = emptyList(), val unread: Int = 0)

    // ========== RAG AI 问答 ==========
    data class RagAnswer(
        val answer: String = "",
        val sources: List<RagSource> = emptyList(),
        val productIds: List<String> = emptyList()
    )

    data class RagSource(
        val doc: String = "",
        val score: Double = 0.0,
        val text: String = ""
    )

    fun ragAsk(question: String): Result<RagAnswer> {
        return request("/rag/ask", "POST",
            gson.toJson(mapOf("question" to question)),
            RagAnswer::class.java
        ).let { r -> r.map { it as? RagAnswer ?: RagAnswer() } }
    }

    // ========== SSE 通知流 ==========
    fun sseTicket(): Result<String> {
        val url = "$BASE_URL/notifications/ticket"
        val requestBuilder = Request.Builder().url(url)
            .post("{}".toRequestBody(JSON))
            .header("Accept", "application/json")
        authStore.token?.let { requestBuilder.header("Authorization", "Bearer $it") }

        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val json = JsonParser.parseString(body).asJsonObject
                if (json.get("success")?.asBoolean != true) {
                    return Result.failure(Exception(json.get("error")?.asString ?: "Error"))
                }
                val dataJson = json.get("data")
                val ticket = dataJson?.asJsonObject?.get("ticket")?.asString ?: ""
                Result.success(ticket)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sseStreamUrl(ticket: String): String {
        return "$BASE_URL/notifications/stream?ticket=$ticket"
    }

    // ========== Upload ==========
    fun uploadImage(file: File): Result<Map<String, String>> {
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
            .build()
        val requestBuilder = Request.Builder()
            .url("$BASE_URL/upload")
            .post(requestBody)
        authStore.token?.let { requestBuilder.header("Authorization", "Bearer $it") }
        return try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val json = JsonParser.parseString(body).asJsonObject
                if (json.get("success")?.asBoolean != true) {
                    return Result.failure(Exception(json.get("error")?.asString ?: "Error"))
                }
                val dataJson = json.get("data")
                if (dataJson == null || dataJson.isJsonNull) {
                    return Result.success(emptyMap())
                }
                val map = gson.fromJson<Map<String, String>>(dataJson, Map::class.java) ?: emptyMap()
                Result.success(map)
            }
        } catch (e: Exception) { Result.failure(e) }
    }
}
