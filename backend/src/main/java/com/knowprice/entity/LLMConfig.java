package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 大语言模型(LLM)配置实体类
 * 
 * <p>存储LLM API的配置信息，包括API端点、密钥、模型参数等。
 * 系统通过此配置调用LLM生成智能定价建议。</p>
 * 
 * <p>数据库表：llm_configs</p>
 * 
 * <p>配置项说明：</p>
 * <ul>
 *   <li>enabled - 是否启用LLM功能</li>
 *   <li>apiEndpoint - LLM API地址</li>
 *   <li>apiKey - API密钥</li>
 *   <li>modelName - 使用的模型名称</li>
 *   <li>temperature - 生成温度参数</li>
 *   <li>maxTokens - 最大生成Token数</li>
 * </ul>
 * 
 * <p>支持的LLM平台：</p>
 * <ul>
 *   <li>MiniMax（默认）</li>
 *   <li>OpenAI兼容接口</li>
 *   <li>其他支持Chat Completion API的平台</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see LLMService
 * @see PromptTemplate
 */
@Data
@Entity
@Table(name = "llm_configs")
public class LLMConfig {
    
    /**
     * 配置唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 是否启用LLM功能
     * 
     * <p>控制系统是否启用LLM智能建议功能。
     * 禁用后报告将不包含LLM建议。</p>
     * 
     * <p>取值：</p>
     * <ul>
     *   <li>true - 启用</li>
     *   <li>false - 禁用（默认）</li>
     * </ul>
     */
    @Column(name = "enabled", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean enabled = false;
    
    /**
     * API端点地址
     * 
     * <p>LLM服务的API地址，需要支持Chat Completion API格式。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>MiniMax: https://api.minimax.chat/v1/text/chatcompletions</li>
     *   <li>OpenAI: https://api.openai.com/v1/chat/completions</li>
     * </ul>
     */
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    /**
     * API密钥
     * 
     * <p>调用LLM API所需的认证密钥。
     * 不同平台的密钥格式可能不同。</p>
     */
    @Column(name = "api_key", length = 500)
    private String apiKey;
    
    /**
     * 模型名称
     * 
     * <p>要使用的LLM模型名称，根据API提供商支持的模型选择。</p>
     * 
     * <p>示例：</p>
     * <ul>
     *   <li>MiniMax: minimax2.5（默认）</li>
     *   <li>OpenAI: gpt-4, gpt-3.5-turbo</li>
     * </ul>
     */
    @Column(name = "model_name", length = 100, columnDefinition = "DEFAULT 'minimax2.5'")
    private String modelName = "minimax2.5";
    
    /**
     * 生成温度
     * 
     * <p>控制LLM输出的随机性，取值范围0-1。
     * 值越高输出越随机，值越低输出越确定。</p>
     * 
     * <p>建议值：</p>
     * <ul>
     *   <li>0.7 - 平衡创造性和一致性（默认）</li>
     *   <li>0.0-0.3 - 更确定的输出</li>
     *   <li>0.8-1.0 - 更有创造性</li>
     * </ul>
     */
    @Column(name = "temperature", precision = 3, scale = 2, columnDefinition = "DEFAULT 0.7")
    private Double temperature = 0.7;
    
    /**
     * 最大Token数
     * 
     * <p>LLM单次响应的最大Token数量。
     * 影响响应内容的最大长度。</p>
     * 
     * <p>默认值：2000</p>
     */
    @Column(name = "max_tokens", columnDefinition = "DEFAULT 2000")
    private Integer maxTokens = 2000;
    
    /**
     * 创建时间
     * 
     * <p>配置创建的时间戳。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * 更新时间
     * 
     * <p>配置最后更新的时间戳。</p>
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
