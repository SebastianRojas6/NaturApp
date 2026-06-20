package com.naturapp.services

import android.content.Context
import androidx.room.*
import com.naturapp.models.CartItem
import com.naturapp.models.Product
import kotlinx.coroutines.flow.Flow
@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "product_id") val productId: String,
    val name: String,
    val price: Double,
    val image: String,
    val quantity: Int = 1
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "product_id") val productId: String,
    val name: String,
    val price: Double,
    val image: String,
    @ColumnInfo(name = "added_date") val addedDate: Long = System.currentTimeMillis()
)

@Dao
interface CartDao {
    @Query("SELECT * FROM cart ORDER BY id DESC")
    fun getAll(): Flow<List<CartEntity>>

    @Query("SELECT COALESCE(SUM(price * quantity), 0) FROM cart")
    suspend fun getTotal(): Double

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart")
    suspend fun getCount(): Int

    @Query("SELECT * FROM cart WHERE product_id = :productId LIMIT 1")
    suspend fun findByProductId(productId: String): CartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartEntity)

    @Query("UPDATE cart SET quantity = :quantity WHERE product_id = :productId")
    suspend fun updateQuantity(productId: String, quantity: Int)

    @Query("DELETE FROM cart WHERE product_id = :productId")
    suspend fun removeByProductId(productId: String)

    @Query("DELETE FROM cart")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY added_date DESC")
    fun getAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT COUNT(*) FROM favorites WHERE product_id = :productId")
    suspend fun isFavorite(productId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE product_id = :productId")
    suspend fun remove(productId: String)
}

@Database(
    entities = [CartEntity::class, FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NaturAppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile private var INSTANCE: NaturAppDatabase? = null

        fun getInstance(context: Context): NaturAppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NaturAppDatabase::class.java,
                    "naturapp.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}

class DatabaseService(context: Context) {

    private val db       = NaturAppDatabase.getInstance(context)
    private val cartDao  = db.cartDao()
    private val favDao   = db.favoriteDao()


    fun getCartItemsFlow(): Flow<List<CartEntity>> = cartDao.getAll()

    suspend fun addToCart(product: Product) {
        val existing = cartDao.findByProductId(product.id)
        if (existing != null) {
            cartDao.updateQuantity(product.id, existing.quantity + 1)
        } else {
            cartDao.insert(
                CartEntity(
                    productId = product.id,
                    name      = product.name,
                    price     = product.price,
                    image     = product.image
                )
            )
        }
    }

    suspend fun updateCartQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            cartDao.removeByProductId(productId)
        } else {
            cartDao.updateQuantity(productId, quantity)
        }
    }

    suspend fun removeFromCart(productId: String) = cartDao.removeByProductId(productId)

    suspend fun getCartTotal(): Double = cartDao.getTotal()

    suspend fun getCartCount(): Int = cartDao.getCount()

    suspend fun clearCart() = cartDao.clearAll()

    fun getFavoritesFlow(): Flow<List<FavoriteEntity>> = favDao.getAll()

    suspend fun addFavorite(product: Product) {
        favDao.insert(
            FavoriteEntity(
                productId = product.id,
                name      = product.name,
                price     = product.price,
                image     = product.image
            )
        )
    }

    suspend fun removeFavorite(productId: String) = favDao.remove(productId)

    suspend fun isFavorite(productId: String): Boolean = favDao.isFavorite(productId) > 0

    fun CartEntity.toCartItem() = CartItem(
        id        = this.id,
        productId = this.productId,
        name      = this.name,
        price     = this.price,
        image     = this.image,
        quantity  = this.quantity
    )
}
