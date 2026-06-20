const express = require("express");
const cors = require("cors");

const app = express();

app.use(cors());
app.use(express.json());

const products = [
  {
    id: "1",
    name: "Chía Orgánica",
    description: "Semillas de chía 100% naturales ricas en omega 3",
    price: 12.9,
    image: "https://picsum.photos/200",
    category: "superfoods",
    stock: 50,
    rating: 4.5,
    benefits: ["Omega 3", "Fibra", "Proteína"],
  },
  {
    id: "2",
    name: "Aceite de Coco",
    description: "Aceite de coco virgen extra prensado en frío",
    price: 28.5,
    image: "https://picsum.photos/201",
    category: "aceites",
    stock: 30,
    rating: 4.8,
    benefits: ["Antimicrobial", "Energía", "Piel sana"],
  },
  {
    id: "3",
    name: "Maca Negra",
    description: "Maca negra andina en polvo, energizante natural",
    price: 18.0,
    image: "https://picsum.photos/202",
    category: "superfoods",
    stock: 0,
    rating: 4.2,
    benefits: ["Energía", "Vitalidad", "Fertilidad"],
  },
  {
    id: "4",
    name: "Té de Manzanilla",
    description: "Infusión de manzanilla pura para relajación",
    price: 8.5,
    image: "https://picsum.photos/203",
    category: "infusiones",
    stock: 100,
    rating: 4.6,
    benefits: ["Relajante", "Digestivo", "Antiinflamatorio"],
  },
  {
    id: "5",
    name: "Miel de Abeja",
    description: "Miel pura de abeja de los valles andinos",
    price: 22.0,
    image: "https://picsum.photos/204",
    category: "miel",
    stock: 25,
    rating: 4.9,
    benefits: ["Antibacterial", "Energía", "Sistema inmune"],
  },
];

// Pedidos en memoria (se pierden al reiniciar el servidor)
const orders = [];

// ── PRODUCTOS ─────────────────────────────────────────────────────────────────

// GET /api/products
// GET /api/products?category=superfoods
// Retorna todos los productos, o filtrados por categoría si se pasa el query param
app.get("/api/products", (req, res) => {
  const { category } = req.query;
  if (category) {
    return res.json(products.filter((p) => p.category === category));
  }
  res.json(products);
});

// GET /api/products/search?q=chia
// Búsqueda por nombre (case-insensitive)
// OJO: debe ir ANTES de /api/products/:id para que "search" no se interprete como un id
app.get("/api/products/search", (req, res) => {
  const query = (req.query.q || "").toLowerCase();
  const results = products.filter(
    (p) =>
      p.name.toLowerCase().includes(query) ||
      p.description.toLowerCase().includes(query),
  );
  res.json(results);
});

// GET /api/products/:id
// Retorna un producto por su ID, o 404 si no existe
app.get("/api/products/:id", (req, res) => {
  const product = products.find((p) => p.id === req.params.id);
  if (!product) {
    return res.status(404).json({ error: "Producto no encontrado" });
  }
  res.json(product);
});

// ── CATEGORÍAS ────────────────────────────────────────────────────────────────

// GET /api/categories
// Lista fija de categorías disponibles en el catálogo
app.get("/api/categories", (req, res) => {
  res.json([
    "todos",
    "superfoods",
    "aceites",
    "capsulas",
    "infusiones",
    "miel",
  ]);
});

// ── PEDIDOS ───────────────────────────────────────────────────────────────────

// POST /api/orders
// Crea un nuevo pedido con los items del carrito
// Body esperado: { items: [...], total: 00.00, address: "..." }
app.post("/api/orders", (req, res) => {
  const newOrder = {
    id: Date.now().toString(), // ID único basado en timestamp
    ...req.body,
    status: "pendiente",
    date: new Date().toISOString(),
  };
  orders.push(newOrder);
  res.status(201).json(newOrder);
});

app.get("/api/orders", (req, res) => {
  res.json(orders);
});

app.get("/api/orders/:id", (req, res) => {
  const order = orders.find((o) => o.id === req.params.id);
  if (!order) {
    return res.status(404).json({ error: "Pedido no encontrado" });
  }
  res.json(order);
});

app.post("/api/auth/login", (req, res) => {
  const { email, password } = req.body;
  if (!email || !password) {
    return res.status(400).json({ error: "Email y contraseña requeridos" });
  }
  res.json({
    token: "token-fake-naturapp-123",
    user: {
      name: "Usuario NaturApp",
      email: email,
    },
  });
});

app.listen(9090, "0.0.0.0", () => {
  console.log("✅ Servidor NaturApp corriendo en http://0.0.0.0:9090");
  console.log("📱 Desde el celular usa: http://192.168.0.19:9090/api");
});
