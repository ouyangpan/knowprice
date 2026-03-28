-- 创建数据库
CREATE DATABASE IF NOT EXISTS hotel_price_monitor CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE hotel_price_monitor;

-- 酒店信息表
CREATE TABLE IF NOT EXISTS hotels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    region VARCHAR(100) NOT NULL,
    star_rating INT NOT NULL,
    brand VARCHAR(100),
    is_own BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 房型表
CREATE TABLE IF NOT EXISTS room_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    amenities TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
);

-- 价格记录表
CREATE TABLE IF NOT EXISTS price_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    availability BOOLEAN DEFAULT TRUE,
    record_date DATE NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE
);

-- 采集配置表
CREATE TABLE IF NOT EXISTS collection_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    region VARCHAR(100) NOT NULL,
    star_ratings JSON,
    brands JSON,
    price_range VARCHAR(50),
    schedule_time TIME DEFAULT '02:00:00',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 报告表
CREATE TABLE IF NOT EXISTS reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    report_date DATE NOT NULL,
    content LONGTEXT NOT NULL,
    llm_suggestions TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 大模型配置表
CREATE TABLE IF NOT EXISTS llm_configs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    enabled BOOLEAN DEFAULT FALSE,
    api_endpoint VARCHAR(500),
    api_key VARCHAR(500),
    model_name VARCHAR(100) DEFAULT 'minimax2.5',
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INT DEFAULT 2000,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Prompt模板表
CREATE TABLE IF NOT EXISTS prompt_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    system_prompt TEXT NOT NULL,
    user_prompt_template TEXT NOT NULL,
    version VARCHAR(20) DEFAULT '1.0',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认数据
INSERT INTO llm_configs (enabled, api_endpoint, model_name, temperature, max_tokens) 
VALUES (false, 'https://api.minimax.chat/v1/text/chatcompletions', 'minimax2.5', 0.7, 2000);

INSERT INTO prompt_templates (name, system_prompt, user_prompt_template, version, is_active) 
VALUES (
    '酒店定价专家',
    '你是一位专业的酒店定价策略顾问，拥有丰富的酒店收益管理经验。你的任务是基于提供的市场数据，为酒店经营者提供科学、合理的定价建议。\n\n你的建议应该：\n1. 基于数据分析，有理有据\n2. 考虑市场竞争态势\n3. 兼顾收益最大化和市场竞争力\n4. 给出具体的调价幅度和时机建议\n5. 预测可能的入住率变化',
    '请基于以下数据为我的酒店提供定价建议：\n\n【我的酒店信息】\n- 酒店名称：{hotel_name}\n- 星级：{star_rating}\n- 当前房型价格：\n  {room_prices}\n\n【竞品价格数据】\n{competitor_data}\n\n【历史价格趋势】（过去{history_days}天）\n{price_trend}\n\n【市场情况】\n- 今日是：{current_date}\n- 临近节假日：{upcoming_holidays}\n- 市场需求趋势：{demand_trend}\n\n请给出：\n1. 当前定价策略分析\n2. 具体调价建议（包含调价幅度和时机）\n3. 预期效果分析\n4. 风险提示',
    '1.0',
    true
);

-- 创建索引
CREATE INDEX idx_hotels_region ON hotels(region);
CREATE INDEX idx_hotels_star_rating ON hotels(star_rating);
CREATE INDEX idx_price_records_hotel_id ON price_records(hotel_id);
CREATE INDEX idx_price_records_record_date ON price_records(record_date);
CREATE INDEX idx_price_records_platform ON price_records(platform);
CREATE INDEX idx_reports_report_date ON reports(report_date);
