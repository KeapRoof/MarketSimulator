CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    balance DECIMAL(15, 2) DEFAULT 0.00
);

CREATE TABLE assets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    ticker VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    price DECIMAL(15, 2) NOT NULL
);

CREATE TABLE wallets (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE,
    user_id BIGINT UNSIGNED,
    asset_id BIGINT UNSIGNED,
    quantity DECIMAL(15, 4) DEFAULT 0.0000,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (asset_id) REFERENCES assets(id)
);

INSERT INTO users (username, password, balance) VALUES
('alice',  'password123', 10000.00),
('bob', 'securepass', 15000.00),
('charlie', 'mypassword', 20000.00);

INSERT INTO assets (ticker, name, asset_type, price) VALUES
('AAPL', 'Apple Inc.', 'Stock', 150.00),
('GOOGL', 'Alphabet Inc.', 'Stock', 2800.00),
('BTC', 'Bitcoin', 'Crypto', 45000.00),
('ETH', 'Ethereum', 'Crypto', 3000.00);

