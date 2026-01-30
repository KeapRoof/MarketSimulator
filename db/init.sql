-- Table des utilisateurs
CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    balance DECIMAL(15, 2) DEFAULT 0.00
);

-- Table des actifs
CREATE TABLE assets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    ticker VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    volatility_rate DECIMAL(5, 2) DEFAULT 0.00,

    -- Colonnes spécifiques aux Stocks (NULL pour Crypto)
    dividend_yield FLOAT,
    market_open_time TIME,
    market_close_time TIME,

    -- Colonnes spécifiques aux Cryptos (NULL pour Stock)
    blockchain VARCHAR(100),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Table des wallets (1 wallet par user par type d'asset)
CREATE TABLE wallets (
     id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
     user_id BIGINT UNSIGNED NOT NULL,
     asset_type VARCHAR(50) NOT NULL,
     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
     UNIQUE KEY unique_user_asset_type (user_id, asset_type)
);

-- Table des positions dans le wallet (actifs détenus)
CREATE TABLE wallet_positions (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT UNSIGNED NOT NULL,
    asset_id BIGINT UNSIGNED NOT NULL,
    quantity DECIMAL(15, 4) DEFAULT 0.0000,
    average_purchase_price DECIMAL(15, 2) DEFAULT 0.00,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE,
    UNIQUE KEY unique_wallet_asset (wallet_id, asset_id)
);

-- Données de test
INSERT INTO users (username, password, balance) VALUES
    ('alice',  'password123', 10000.00),
    ('bob', 'securepass', 15000.00),
    ('charlie', 'mypassword', 20000.00);

INSERT INTO assets (ticker, name, asset_type, price, volatility_rate) VALUES
    ('AAPL', 'Apple Inc.', 'Stock', 150.00, 0.01),
    ('MSFT', 'Microsoft Corporation', 'Stock', 300.00, 0.02),
    ('GOOGL', 'Alphabet Inc.', 'Stock', 2800.00, 0.03),
    ('BTC', 'Bitcoin', 'Crypto', 45000.00, 0.07),
    ('ETH', 'Ethereum', 'Crypto', 3000.00, 0.05);

-- Création automatique des wallets pour les utilisateurs
INSERT INTO wallets (user_id, asset_type) VALUES
    (1, 'Stock'),
    (1, 'Crypto'),
    (2, 'Stock'),
    (2, 'Crypto'),
    (3, 'Stock'),
    (3, 'Crypto');

INSERT INTO wallet_positions (wallet_id, asset_id, quantity, average_purchase_price) VALUES
    (1, 1, 10.0000, 150.00),
    (2, 4, 0.5000, 45000.00);