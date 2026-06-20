package com.naturapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.naturapp.models.Product
import com.naturapp.services.ApiService
import com.naturapp.services.DatabaseService
import com.naturapp.services.StorageService
import com.naturapp.ui.components.NaturError
import com.naturapp.ui.components.NaturGreen
import com.naturapp.ui.components.NaturNavy
import com.naturapp.ui.screens.CartScreen
import com.naturapp.ui.screens.HomeScreen
import com.naturapp.ui.screens.OrdersScreen
import com.naturapp.ui.screens.ProfileScreen
import com.naturapp.viewmodels.CartViewModel
import com.naturapp.viewmodels.OrdersViewModel
import com.naturapp.viewmodels.ProductsViewModel
import com.naturapp.viewmodels.ProfileViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home    : Screen("home",    "NaturApp", Icons.Default.Eco)
    object Cart    : Screen("cart",    "Carrito",  Icons.Default.ShoppingCart)
    object Orders  : Screen("orders",  "Pedidos",  Icons.Default.Receipt)
    object Profile : Screen("profile", "Perfil",   Icons.Default.Person)
}

val BOTTOM_TABS = listOf(Screen.Home, Screen.Cart, Screen.Orders, Screen.Profile)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storageService  = StorageService(applicationContext)
        val databaseService = DatabaseService(applicationContext)
        val apiService      = ApiService(storageService)

        val productsViewModel = ProductsViewModel(apiService, storageService)
        val cartViewModel     = CartViewModel(databaseService, apiService)
        val ordersViewModel   = OrdersViewModel(apiService)
        val profileViewModel  = ProfileViewModel(storageService)

        setContent {
            NaturAppTheme {
                NaturAppNavHost(
                    productsViewModel = productsViewModel,
                    cartViewModel     = cartViewModel,
                    ordersViewModel   = ordersViewModel,
                    profileViewModel  = profileViewModel
                )
            }
        }
    }
}

@Composable
fun NaturAppNavHost(
    productsViewModel: ProductsViewModel,
    cartViewModel: CartViewModel,
    ordersViewModel: OrdersViewModel,
    profileViewModel: ProfileViewModel
) {
    val navController = rememberNavController()
    val cartState     by cartViewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor   = NaturNavy
            ) {
                val currentDestination by navController.currentBackStackEntryAsState()
                val currentRoute       = currentDestination?.destination?.route

                BOTTOM_TABS.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick  = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            if (screen == Screen.Cart && cartState.count > 0) {
                                BadgedBox(badge = {
                                    Badge { Text("${cartState.count}") }
                                }) {
                                    Icon(screen.icon, contentDescription = screen.label)
                                }
                            } else {
                                Icon(screen.icon, contentDescription = screen.label)
                            }
                        },
                        label  = { Text(screen.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = NaturGreen,
                            selectedTextColor   = NaturGreen,
                            indicatorColor      = NaturGreen.copy(alpha = 0.12f),
                            unselectedIconColor = Color(0xFF888888),
                            unselectedTextColor = Color(0xFF888888)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Home.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    productsViewModel   = productsViewModel,
                    cartViewModel       = cartViewModel,
                    onNavigateToProduct = { id -> navController.navigate("product/$id") }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(cartViewModel = cartViewModel)
            }
            composable(Screen.Orders.route) {
                OrdersScreen(ordersViewModel = ordersViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(profileViewModel = profileViewModel)
            }
            composable("product/{id}") { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("id") ?: return@composable
                val context   = LocalContext.current
                val storage   = remember { StorageService(context) }
                val api       = remember { ApiService(storage) }
                ProductDetailScreen(
                    productId     = productId,
                    apiService    = api,
                    cartViewModel = cartViewModel,
                    onBack        = { navController.popBackStack() }
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TEMA
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun NaturAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary      = NaturGreen,
            onPrimary    = Color.White,
            secondary    = NaturNavy,
            onSecondary  = Color.White,
            background   = Color(0xFFF5F5F5),
            surface      = Color.White,
            onBackground = Color(0xFF1A1A1A),
            onSurface    = Color(0xFF1A1A1A)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    apiService: ApiService,
    cartViewModel: CartViewModel,
    onBack: () -> Unit
) {
    var product by remember { mutableStateOf<Product?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error   by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productId) {
        try {
            product = apiService.getProductById(productId)
        } catch (e: Exception) {
            error = "No se pudo cargar el producto"
        } finally {
            loading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint               = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = NaturNavy,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        when {
            loading -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NaturGreen)
                }
            }
            error != null -> {
                Box(
                    modifier         = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error!!, color = NaturError)
                }
            }
            product != null -> {
                val p = product!!
                LazyColumn(
                    modifier            = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding      = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        AsyncImage(
                            model              = p.image,
                            contentDescription = p.name,
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    item {
                        Text(
                            text       = p.name,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color      = NaturNavy
                        )
                        Text(
                            text       = p.getFormattedPrice(),
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = NaturGreen
                        )
                    }
                    item {
                        Text(
                            text       = p.description,
                            fontSize   = 14.sp,
                            color      = Color(0xFF555555),
                            lineHeight = 22.sp
                        )
                    }
                    if (p.benefits.isNotEmpty()) {
                        item {
                            Text(
                                text       = "Beneficios",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = NaturNavy
                            )
                            p.benefits.forEach { b ->
                                Text("• $b", fontSize = 14.sp, color = Color(0xFF444444))
                            }
                        }
                    }
                    item {
                        Button(
                            onClick  = { cartViewModel.addItem(p, onError = {}) },
                            enabled  = p.isAvailable(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = NaturGreen)
                        ) {
                            Text(
                                text       = if (p.isAvailable()) "Agregar al Carrito" else "Sin Stock",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}