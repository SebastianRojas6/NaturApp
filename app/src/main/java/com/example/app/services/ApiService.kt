package com.naturapp.services

import com.naturapp.models.Order
import com.naturapp.models.Product
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ApiService(private val storageService: StorageService) {

    companion object {
        private const val BASE_URL = "http://192.168.0.19:9090/api"
    }

    private suspend fun request(
        endpoint: String,
        method: String = "GET",
        body: JSONObject? = null
    ): JSONObject {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val url = URL("$BASE_URL$endpoint")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = method
            conn.setRequestProperty("Content-Type", "application/json")

            storageService.getToken()?.let { token ->
                conn.setRequestProperty("Authorization", "Bearer $token")
            }

            if (body != null) {
                conn.doOutput = true
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
            }

            val responseCode = conn.responseCode
            if (responseCode !in 200..299) {
                throw Exception("HTTP $responseCode: ${conn.responseMessage}")
            }

            val responseText = conn.inputStream.bufferedReader().use { it.readText() }

            if (responseText.trimStart().startsWith("[")) {
                JSONObject().put("data", JSONArray(responseText))
            } else {
                JSONObject(responseText)
            }
        }
    }


    suspend fun getProducts(category: String? = null): List<Product> {
        val query = if (category != null && category != "todos") "?category=$category" else ""
        val response = request("/products$query")
        val array = response.optJSONArray("data") ?: JSONArray()
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            Product.fromMap(jsonToMap(obj))
        }
    }

    suspend fun getProductById(id: String): Product {
        val response = request("/products/$id")
        return Product.fromMap(jsonToMap(response))
    }

    suspend fun searchProducts(query: String): List<Product> {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val response = request("/products/search?q=$encoded")
        val array = response.optJSONArray("data") ?: JSONArray()
        return (0 until array.length()).map { i ->
            Product.fromMap(jsonToMap(array.getJSONObject(i)))
        }
    }

    suspend fun getCategories(): List<String> {
        val response = request("/categories")
        val array = response.optJSONArray("data") ?: JSONArray()
        return (0 until array.length()).map { i -> array.getString(i) }
    }


    suspend fun createOrder(orderData: JSONObject): Order {
        val response = request("/orders", method = "POST", body = orderData)
        return Order.fromMap(jsonToMap(response))
    }

    suspend fun getOrders(): List<Order> {
        val response = request("/orders")
        val array = response.optJSONArray("data") ?: JSONArray()
        return (0 until array.length()).map { i ->
            Order.fromMap(jsonToMap(array.getJSONObject(i)))
        }
    }

    suspend fun getOrderById(id: String): Order {
        val response = request("/orders/$id")
        return Order.fromMap(jsonToMap(response))
    }


    suspend fun login(email: String, password: String): JSONObject {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val data = request("/auth/login", method = "POST", body = body)
        val token = data.optString("token")
        if (token.isNotEmpty()) {
            storageService.saveToken(token)
            data.optJSONObject("user")?.let { user ->
                storageService.saveUserProfile(
                    user.optString("name"),
                    user.optString("email")
                )
            }
        }
        return data
    }


    private fun jsonToMap(obj: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        obj.keys().forEach { key -> map[key] = obj.get(key) }
        return map
    }
}
