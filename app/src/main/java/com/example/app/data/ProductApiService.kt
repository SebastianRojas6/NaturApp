package com.example.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class OpenFoodResponse(val products: List<OpenFoodProduct>)
data class OpenFoodProduct(
    val code: String?,
    val product_name: String?,
    val ingredients_text: String?,
    val image_url: String?
)

interface ProductApiService {
    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("search_simple") simple: Int = 1,
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 30
    ): OpenFoodResponse

    companion object {
        fun create(): ProductApiService {
            return Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ProductApiService::class.java)
        }
    }
}