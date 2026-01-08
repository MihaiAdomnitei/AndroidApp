const express = require('express');
const { v4: uuidv4 } = require('uuid');

const router = express.Router();

// In-memory product storage
const products = new Map();

// Add some sample products
const sampleProducts = [
  { _id: uuidv4(), title: 'iPhone 15', price: 999, date: '2024-01-15', sold: false },
  { _id: uuidv4(), title: 'MacBook Pro', price: 2499, date: '2024-02-20', sold: true },
  { _id: uuidv4(), title: 'AirPods Pro', price: 249, date: '2024-03-10', sold: false },
];

sampleProducts.forEach(p => products.set(p._id, p));

// Get all products
router.get('/', (req, res) => {
  console.log('GET /api/product');
  const allProducts = Array.from(products.values());
  res.json(allProducts);
});

// Get single product
router.get('/:id', (req, res) => {
  console.log(`GET /api/product/${req.params.id}`);
  const product = products.get(req.params.id);
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  res.json(product);
});

// Create product
router.post('/', (req, res) => {
  console.log('POST /api/product', req.body);
  const { title, price, date, sold } = req.body;

  const product = {
    _id: uuidv4(),
    title: title || '',
    price: price || 0,
    date: date || new Date().toISOString().split('T')[0],
    sold: sold || false
  };

  products.set(product._id, product);

  // Broadcast to WebSocket clients
  const broadcast = req.app.get('broadcast');
  if (broadcast) {
    broadcast({ type: 'created', payload: product });
  }

  console.log('Product created:', product);
  res.status(201).json(product);
});

// Update product
router.put('/:id', (req, res) => {
  console.log(`PUT /api/product/${req.params.id}`, req.body);
  const { id } = req.params;

  if (!products.has(id)) {
    return res.status(404).json({ message: 'Product not found' });
  }

  const { title, price, date, sold } = req.body;
  const product = {
    _id: id,
    title: title || '',
    price: price || 0,
    date: date || '',
    sold: sold || false
  };

  products.set(id, product);

  // Broadcast to WebSocket clients
  const broadcast = req.app.get('broadcast');
  if (broadcast) {
    broadcast({ type: 'updated', payload: product });
  }

  console.log('Product updated:', product);
  res.json(product);
});

// Delete product
router.delete('/:id', (req, res) => {
  console.log(`DELETE /api/product/${req.params.id}`);
  const { id } = req.params;

  const product = products.get(id);
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }

  products.delete(id);

  // Broadcast to WebSocket clients
  const broadcast = req.app.get('broadcast');
  if (broadcast) {
    broadcast({ type: 'deleted', payload: product });
  }

  console.log('Product deleted:', id);
  res.status(204).send();
});

module.exports = router;

