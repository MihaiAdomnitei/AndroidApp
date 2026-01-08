const express = require('express');
const cors = require('cors');
const http = require('http');
const WebSocket = require('ws');
const authRouter = require('./routes/auth');
const productRouter = require('./routes/product');
const { verifyToken } = require('./middleware/auth');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

// Middleware
app.use(cors());
app.use(express.json());

// Routes
app.use('/api/auth', authRouter);
app.use('/api/product', verifyToken, productRouter);

// WebSocket connections
const clients = new Map();

wss.on('connection', (ws, req) => {
  console.log('New WebSocket connection');

  ws.on('message', (message) => {
    try {
      const data = JSON.parse(message);
      if (data.type === 'authorization') {
        clients.set(ws, data.token);
        console.log('Client authorized');
      }
    } catch (e) {
      console.error('WebSocket message error:', e);
    }
  });

  ws.on('close', () => {
    clients.delete(ws);
    console.log('Client disconnected');
  });
});

// Broadcast to all connected clients
const broadcast = (event) => {
  const message = JSON.stringify(event);
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
};

// Export broadcast function for use in routes
app.set('broadcast', broadcast);

const PORT = process.env.PORT || 3000;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on http://0.0.0.0:${PORT}`);
  console.log(`WebSocket running on ws://0.0.0.0:${PORT}`);
});

