const express = require('express');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../middleware/auth');

const router = express.Router();

// In-memory user storage (for demo purposes)
const users = new Map();

// Create a default test user
const defaultPassword = bcrypt.hashSync('test', 10);
users.set('test', { username: 'test', password: defaultPassword });

// Register
router.post('/register', async (req, res) => {
  try {
    const { username, password } = req.body;

    if (!username || !password) {
      return res.status(400).json({ message: 'Username and password required' });
    }

    if (users.has(username)) {
      return res.status(400).json({ message: 'User already exists' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    users.set(username, { username, password: hashedPassword });

    console.log(`User registered: ${username}`);
    res.status(201).json({ message: 'User created successfully' });
  } catch (error) {
    console.error('Register error:', error);
    res.status(500).json({ message: 'Server error' });
  }
});

// Login
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;

    console.log(`Login attempt: ${username}`);

    if (!username || !password) {
      return res.status(400).json({ message: 'Username and password required' });
    }

    const user = users.get(username);
    if (!user) {
      return res.status(401).json({ message: 'Invalid credentials' });
    }

    const isValidPassword = await bcrypt.compare(password, user.password);
    if (!isValidPassword) {
      return res.status(401).json({ message: 'Invalid credentials' });
    }

    const token = jwt.sign({ username }, JWT_SECRET, { expiresIn: '24h' });

    console.log(`Login successful: ${username}`);
    res.json({ token });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ message: 'Server error' });
  }
});

module.exports = router;
