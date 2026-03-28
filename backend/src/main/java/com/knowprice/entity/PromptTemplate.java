package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提示词模板实体类
 * 
 * <p>存储LLM的提示词模板，包括系统提示词和用户提示词模板。
 * 支持变量占位符，在生成建议时会被实际数据替换。</p>
 * 
 * <p>数据库表：prompt_templates</p>
 * 
 * <p>模板类型：</p>
 * <ul>
 *   <li>系统提示词 - 定义AI角色和行为规范</li>
 *   <li>用户提示词 - 包含具体分析请求和数据</li>
 * </ul>
 * 
 * <p>支持的变量占位符：</p>
 * <ul>
 *   <li>{hotel_name} - 酒店名称</li>
 *   <li>{star_rating} - 星级</li>
 *   <li>{brand} - 品牌</li>
 *   <li>{region} - 区域</li>
 *   <li>{room_prices} - 房型价格列表</li>
 *   <li>{competitor_data} - 竞品数据</li>
 *   <li>{price_trend} - 价格趋势</li>
 *   <li>{current_date} - 当前日期</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see LLMService
 * @see LLMConfig
 */
@Data
@Entity
@Table(name = "prompt_templates")
public class PromptTemplate {
    
    /**
     * 模板唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板名称
     * 
     * <p>模板的显示名称，用于管理和选择模板。</p>
     * 
     * <p>示例：酒店定价专家、价格分析助手等</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 系统提示词
     * 
     * <p>定义AI的角色、行为规范和输出格式。
     * 在Chat Completion API中作为system角色的消息。</p>
     * 
     * <p>示例：</p>
     * <pre>
     * 你是一位专业的酒店定价顾问，拥有丰富的酒店行业经验。
     * 你的任务是根据提供的市场数据，为酒店提供专业的定价建议。
     * </pre>
     */
    @Column(name = "system_prompt", nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;
    
    /**
     * 用户提示词模板
     * 
     * <p>包含具体分析请求的模板，支持变量占位符。
     * 在Chat Completion API中作为user角色的消息。</p>
     * 
     * <p>示例：</p>
     * <pre>
     * 请分析以下酒店的价格数据：
     * 酒店名称：{hotel_name}
     * 星级：{star_rating}
     * 房型价格：
     * {room_prices}
     * 
     * 请给出定价建议。
     * </pre>
     */
    @Column(name = "user_prompt_template", nullable = false, columnDefinition = "TEXT")
    private String userPromptTemplate;
    
    /**
     * 模板版本
     * 
     * <p>模板的版本号，用于追踪模板变更。</p>
     */
    @Column(name = "version", length = 20, columnDefinition = "DEFAULT '1.0'")
    private String version = "1.0";
    
    /**
     * 是否激活
     * 
     * <p>标识该模板是否为当前激活使用的模板。
     * 系统默认使用激活的模板生成建议。</p>
     * 
     * <p>取值：</p>
     * <ul>
     *   <li>true - 激活（默认）</li>
     *   <li>false - 未激活</li>
     * </ul>
     */
    @Column(name = "is_active", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;
    
    /**
     * 创建时间
     * 
     * <p>模板创建的时间戳。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * 更新时间
     * 
     * <p>模板最后更新的时间戳。</p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * 更新前回调
     * 
     * <p>JPA生命周期回调，在实体更新前自动更新updatedAt字段。</p>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
