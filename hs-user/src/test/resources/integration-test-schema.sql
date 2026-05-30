CREATE TABLE IF NOT EXISTS service_categories (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200),
    icon_url VARCHAR(500),
    sort_order INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_skus (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id INTEGER,
    name VARCHAR(200),
    description VARCHAR(500),
    cover_image VARCHAR(500),
    base_price DECIMAL(10,2),
    duration_minutes INTEGER,
    unit VARCHAR(50),
    sales_count INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pricing_rules (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    rule_type VARCHAR(50),
    rule_name VARCHAR(200),
    rule_config VARCHAR(1000),
    priority INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS worker_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0,
    frozen_balance DECIMAL(10,2) DEFAULT 0,
    total_earned DECIMAL(10,2) DEFAULT 0,
    version INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS worker_earnings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    worker_id BIGINT,
    order_id BIGINT,
    order_price DECIMAL(10,2),
    worker_ratio DECIMAL(4,2),
    worker_amount DECIMAL(10,2),
    platform_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 订单模块 ==========

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32),
    user_id BIGINT,
    worker_id BIGINT,
    sku_id BIGINT,
    category_id INTEGER,
    address_id BIGINT,
    appointment_time TIMESTAMP,
    status INTEGER DEFAULT 0,
    base_price DECIMAL(10,2),
    distance_fee DECIMAL(10,2),
    time_surcharge DECIMAL(10,2),
    coupon_discount DECIMAL(10,2),
    final_price DECIMAL(10,2),
    urgent_fee DECIMAL(10,2),
    pay_status INTEGER,
    pay_method INTEGER,
    pay_time TIMESTAMP,
    is_vip TINYINT DEFAULT 0,
    is_urgent INTEGER DEFAULT 0,
    user_remark VARCHAR(500),
    confirm_time TIMESTAMP,
    auto_confirm TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    from_status INTEGER,
    to_status INTEGER,
    operator_type INTEGER,
    operator_id BIGINT,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_address_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    full_address VARCHAR(500),
    lng DECIMAL(10,6),
    lat DECIMAL(10,6)
);

CREATE TABLE IF NOT EXISTS payment_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no VARCHAR(64),
    order_id BIGINT,
    user_id BIGINT,
    amount DECIMAL(10,2),
    method INTEGER,
    status INTEGER,
    third_party_no VARCHAR(100),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 用户模块 ==========

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20),
    password_hash VARCHAR(255),
    nickname VARCHAR(100),
    avatar_url VARCHAR(500),
    gender TINYINT DEFAULT 0,
    wx_openid VARCHAR(100),
    wx_unionid VARCHAR(100),
    status INTEGER DEFAULT 1,
    role VARCHAR(50),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    province VARCHAR(50),
    city VARCHAR(50),
    district VARCHAR(50),
    detail VARCHAR(200),
    lng DECIMAL(10,6),
    lat DECIMAL(10,6),
    is_default TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 服务者模块 ==========

CREATE TABLE IF NOT EXISTS workers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone VARCHAR(20),
    password_hash VARCHAR(255),
    name VARCHAR(100),
    id_card VARCHAR(20),
    avatar_url VARCHAR(500),
    gender TINYINT DEFAULT 0,
    category_id INTEGER,
    status INTEGER DEFAULT 0,
    verify_status INTEGER DEFAULT 0,
    avg_rating DECIMAL(3,2) DEFAULT 5.00,
    total_orders INTEGER DEFAULT 0,
    accept_rate DECIMAL(5,2) DEFAULT 1.00,
    today_orders INTEGER DEFAULT 0,
    online_status INTEGER DEFAULT 0,
    last_lng DECIMAL(10,6),
    last_lat DECIMAL(10,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 营销模块 ==========

CREATE TABLE IF NOT EXISTS coupon_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coupon_name VARCHAR(200),
    coupon_type INTEGER,
    threshold_amount DECIMAL(10,2),
    discount_amount DECIMAL(10,2),
    discount_rate DECIMAL(5,2),
    total_quantity INTEGER DEFAULT 0,
    valid_days INTEGER,
    category_id INTEGER,
    received_count INTEGER DEFAULT 0,
    type INTEGER DEFAULT 0,
    status INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    template_id BIGINT,
    status INTEGER DEFAULT 0,
    user_order_id BIGINT,
    expire_time TIMESTAMP,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS seckill_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    activity_name VARCHAR(200),
    template_id BIGINT,
    category_id INTEGER,
    total_stock INTEGER,
    limit_per_user INTEGER DEFAULT 1,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    preheat_time TIMESTAMP,
    status INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 通知模块 ==========

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receiver_type INTEGER,
    receiver_id BIGINT,
    title VARCHAR(200),
    content VARCHAR(1000),
    notification_type INTEGER,
    related_order_id BIGINT,
    is_read INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== 提现记录 ==========

CREATE TABLE IF NOT EXISTS worker_withdraws (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    withdraw_no VARCHAR(64),
    worker_id BIGINT,
    amount DECIMAL(10,2),
    bank_name VARCHAR(100),
    bank_card_no VARCHAR(50),
    status INTEGER DEFAULT 0,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
