package com.knowprice.service;

import com.knowprice.entity.LLMConfig;
import com.knowprice.entity.PromptTemplate;
import com.knowprice.entity.PriceRecord;
import com.knowprice.factory.PromptTemplateFactory;
import com.knowprice.factory.PromptTemplateFactory.PromptType;
import com.knowprice.repository.LLMConfigRepository;
import com.knowprice.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * 大语言模型(LLM)服务
 * 
 * <p>提供大语言模型集成功能的核心服务，负责调用LLM API生成智能定价建议。
 * 支持多种LLM平台，通过配置可灵活切换不同的模型提供商。</p>
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li>LLM配置管理（API端点、密钥、模型参数）</li>
 *   <li>连接测试与验证</li>
 *   <li>智能定价建议生成</li>
 *   <li>提示词模板管理</li>
 *   <li>LLM API调用与响应解析</li>
 * </ul>
 * 
 * <p>支持的LLM平台：</p>
 * <ul>
 *   <li>MiniMax（默认配置）</li>
 *   <li>OpenAI兼容接口</li>
 *   <li>其他支持Chat Completion API的平台</li>
 * </ul>
 * 
 * <p>API调用格式：</p>
 * <p>使用标准的Chat Completion API格式，支持system和user两种角色的消息。</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see PromptTemplateFactory
 * @see LLMConfig
 */
@Service
public class LLMService {

    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);

    @Autowired
    private LLMConfigRepository llmConfigRepository;

    @Autowired
    private PromptTemplateRepository promptTemplateRepository;

    @Autowired
    private PromptTemplateFactory promptTemplateFactory;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 检查LLM功能是否启用
     * 
     * <p>快速检查系统中是否配置并启用了LLM功能。
     * 前端可根据此状态决定是否显示AI相关功能入口。</p>
     * 
     * @return true表示已启用，false表示未启用或未配置
     */
    public boolean isLLMEnabled() {
        LLMConfig config = llmConfigRepository.findFirstByOrderByIdAsc();
        return config != null && config.getEnabled();
    }

    /**
     * 获取LLM配置
     * 
     * <p>返回当前系统的LLM配置信息。如果不存在配置，则创建一个默认配置。
     * 默认配置使用MiniMax平台的API。</p>
     * 
     * <p>默认配置：</p>
     * <ul>
     *   <li>enabled: false（默认禁用）</li>
     *   <li>apiEndpoint: MiniMax API地址</li>
     *   <li>modelName: minimax2.5</li>
     *   <li>temperature: 0.7</li>
     *   <li>maxTokens: 2000</li>
     * </ul>
     * 
     * @return LLM配置对象
     */
    public LLMConfig getLLMConfig() {
        LLMConfig config = llmConfigRepository.findFirstByOrderByIdAsc();
        if (config == null) {
            config = new LLMConfig();
            config.setEnabled(false);
            config.setApiEndpoint("https://api.minimax.chat/v1/text/chatcompletions");
            config.setModelName("minimax2.5");
            config.setTemperature(0.7);
            config.setMaxTokens(2000);
            config = llmConfigRepository.save(config);
        }
        return config;
    }

    /**
     * 保存LLM配置
     * 
     * <p>保存或更新LLM配置信息。配置保存后立即生效。</p>
     * 
     * @param config LLM配置对象
     * @return 保存后的配置对象
     */
    @Transactional
    public LLMConfig saveLLMConfig(LLMConfig config) {
        return llmConfigRepository.save(config);
    }

    /**
     * 测试LLM连接
     * 
     * <p>使用当前配置测试LLM API连接是否正常。发送一个简单的测试请求，
     * 验证API端点、密钥等配置是否正确。</p>
     * 
     * <p>测试内容：发送"你好，请回复'连接测试成功'"消息，检查是否能收到正常响应。</p>
     * 
     * @return 测试结果Map，包含：
     *         <ul>
     *           <li>success - 是否成功</li>
     *           <li>message - 结果消息</li>
     *         </ul>
     */
    public Map<String, Object> testConnection() {
        LLMConfig config = getLLMConfig();
        
        if (!config.getEnabled()) {
            return Map.of(
                "success", false,
                "message", "大模型功能未启用"
            );
        }

        if (config.getApiEndpoint() == null || config.getApiKey() == null) {
            return Map.of(
                "success", false,
                "message", "API配置不完整"
            );
        }

        try {
            String testPrompt = "你好，请回复'连接测试成功'";
            String systemPrompt = promptTemplateFactory.getSystemPrompt(PromptType.HOTEL_SYSTEM);
            
            Map<String, Object> response = callLLM(testPrompt, systemPrompt);
            
            if (response.containsKey("error")) {
                return Map.of(
                    "success", false,
                    "message", "API调用失败: " + response.get("error")
                );
            }
            
            return Map.of(
                "success", true,
                "message", "连接测试成功"
            );
        } catch (Exception e) {
            logger.error("大模型连接测试失败: {}", e.getMessage());
            return Map.of(
                "success", false,
                "message", "连接测试失败: " + e.getMessage()
            );
        }
    }

    /**
     * 生成智能定价建议
     * 
     * <p>基于价格记录数据，调用LLM生成详细的定价建议。
     * 使用价格分析类型的提示词模板。</p>
     * 
     * @param records 价格记录列表
     * @return LLM生成的定价建议文本
     * @throws Exception 如果LLM功能未启用或API调用失败
     */
    public String generatePricingSuggestions(List<PriceRecord> records) throws Exception {
        LLMConfig config = getLLMConfig();
        
        if (!config.getEnabled()) {
            throw new RuntimeException("大模型功能未启用");
        }

        String systemPrompt = promptTemplateFactory.getSystemPrompt(PromptType.HOTEL_SYSTEM);
        
        Map<String, String> variables = buildPromptVariables(records);
        String userPrompt = promptTemplateFactory.getUserPrompt(PromptType.PRICE_ANALYSIS, variables);
        
        Map<String, Object> response = callLLM(userPrompt, systemPrompt);
        
        if (response.containsKey("error")) {
            throw new RuntimeException("API调用失败: " + response.get("error"));
        }
        
        return extractContent(response);
    }

    /**
     * 根据价格水平生成推荐建议
     * 
     * <p>根据自有酒店价格与市场的差异程度，选择不同类型的提示词模板
     * 生成针对性的定价建议。</p>
     * 
     * <p>建议类型选择：</p>
     * <ul>
     *   <li>价格差异 > 10%：高价推荐（建议降价）</li>
     *   <li>价格差异 < -10%：低价推荐（建议提价）</li>
     *   <li>其他：正常价格推荐（保持策略）</li>
     * </ul>
     * 
     * @param records 价格记录列表
     * @param priceDiffPercent 价格差异百分比
     * @return LLM生成的推荐建议文本
     * @throws Exception 如果LLM功能未启用或API调用失败
     */
    public String generateRecommendationByPriceLevel(List<PriceRecord> records, double priceDiffPercent) throws Exception {
        LLMConfig config = getLLMConfig();
        
        if (!config.getEnabled()) {
            throw new RuntimeException("大模型功能未启用");
        }

        String systemPrompt = promptTemplateFactory.getSystemPrompt(PromptType.HOTEL_SYSTEM);
        
        PromptType recommendationType;
        if (priceDiffPercent > 10) {
            recommendationType = PromptType.HIGH_PRICE_RECOMMENDATION;
        } else if (priceDiffPercent < -10) {
            recommendationType = PromptType.LOW_PRICE_RECOMMENDATION;
        } else {
            recommendationType = PromptType.NORMAL_PRICE_RECOMMENDATION;
        }
        
        Map<String, String> variables = buildPromptVariables(records);
        variables.put("context_data", "价格差异百分比: " + String.format("%.1f%%", priceDiffPercent));
        
        String userPrompt = promptTemplateFactory.getUserPrompt(recommendationType, variables);
        
        Map<String, Object> response = callLLM(userPrompt, systemPrompt);
        
        if (response.containsKey("error")) {
            throw new RuntimeException("API调用失败: " + response.get("error"));
        }
        
        return extractContent(response);
    }

    /**
     * 构建提示词变量
     * 
     * <p>从价格记录中提取关键信息，构建用于填充提示词模板的变量Map。</p>
     * 
     * <p>变量列表：</p>
     * <ul>
     *   <li>hotel_name - 酒店名称</li>
     *   <li>star_rating - 星级</li>
     *   <li>brand - 品牌</li>
     *   <li>region - 区域</li>
     *   <li>room_prices - 房型价格列表</li>
     *   <li>competitor_data - 竞品数据</li>
     *   <li>price_trend - 价格趋势</li>
     *   <li>current_date - 当前日期</li>
     * </ul>
     * 
     * @param records 价格记录列表
     * @return 变量名到变量值的映射
     */
    private Map<String, String> buildPromptVariables(List<PriceRecord> records) {
        Map<String, String> variables = new HashMap<>();
        
        variables.put("hotel_name", "我的酒店");
        variables.put("star_rating", "五星级");
        variables.put("brand", "自有品牌");
        variables.put("region", "杭州市西湖区");
        
        StringBuilder roomPricesBuilder = new StringBuilder();
        Map<String, List<Double>> hotelPrices = new HashMap<>();
        
        for (PriceRecord record : records) {
            String key = record.getRoomType().getName();
            hotelPrices.computeIfAbsent(key, k -> new ArrayList<>()).add(record.getPrice());
        }
        
        for (Map.Entry<String, List<Double>> entry : hotelPrices.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
            roomPricesBuilder.append(String.format("- %s: 平均%.2f元\n", entry.getKey(), avg));
        }
        variables.put("room_prices", roomPricesBuilder.toString());
        
        variables.put("competitor_data", "竞品酒店价格数据已收集");
        variables.put("price_trend", "近期价格趋势稳定");
        variables.put("current_date", java.time.LocalDate.now().toString());
        variables.put("upcoming_holidays", "无");
        variables.put("demand_trend", "需求稳定");
        variables.put("competition_level", "中等");
        variables.put("history_days", "7");
        
        return variables;
    }

    /**
     * 调用LLM API
     * 
     * <p>构建请求体并调用LLM的Chat Completion API。
     * 支持system和user两种角色的消息。</p>
     * 
     * <p>请求格式：</p>
     * <pre>
     * {
     *   "model": "模型名称",
     *   "temperature": 0.7,
     *   "max_tokens": 2000,
     *   "messages": [
     *     {"role": "system", "content": "系统提示词"},
     *     {"role": "user", "content": "用户消息"}
     *   ]
     * }
     * </pre>
     * 
     * @param userContent 用户消息内容
     * @param systemContent 系统提示词（可为null）
     * @return API响应Map，失败时包含"error"键
     */
    private Map<String, Object> callLLM(String userContent, String systemContent) {
        LLMConfig config = getLLMConfig();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", config.getModelName());
        requestBody.put("temperature", config.getTemperature());
        requestBody.put("max_tokens", config.getMaxTokens());
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        if (systemContent != null && !systemContent.isEmpty()) {
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemContent);
            messages.add(systemMessage);
        }
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                config.getApiEndpoint(),
                HttpMethod.POST,
                request,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                return Map.of("error", "API返回异常状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("LLM API调用失败: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * 从API响应中提取内容
     * 
     * <p>解析Chat Completion API的响应，提取生成的文本内容。</p>
     * 
     * <p>响应格式：</p>
     * <pre>
     * {
     *   "choices": [
     *     {
     *       "message": {
     *         "role": "assistant",
     *         "content": "生成的内容"
     *       }
     *     }
     *   ]
     * }
     * </pre>
     * 
     * @param response API响应Map
     * @return 生成的内容文本，解析失败返回null
     */
    private String extractContent(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            logger.error("解析响应内容失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取激活的提示词模板
     * 
     * <p>返回当前激活的提示词模板。如果不存在，则创建一个默认模板。</p>
     * 
     * @return 激活的提示词模板
     */
    public PromptTemplate getActivePromptTemplate() {
        List<PromptTemplate> templates = promptTemplateRepository.findByIsActiveTrue();
        
        if (templates.isEmpty()) {
            PromptTemplate defaultTemplate = new PromptTemplate();
            defaultTemplate.setName("酒店定价专家");
            defaultTemplate.setSystemPrompt(promptTemplateFactory.getSystemPrompt(PromptType.HOTEL_SYSTEM));
            defaultTemplate.setUserPromptTemplate(promptTemplateFactory.getUserPrompt(PromptType.PRICE_ANALYSIS));
            defaultTemplate.setVersion("1.0");
            defaultTemplate.setIsActive(true);
            return promptTemplateRepository.save(defaultTemplate);
        }
        
        return templates.get(0);
    }

    /**
     * 获取所有提示词模板
     * 
     * @return 所有提示词模板列表
     */
    public List<PromptTemplate> getAllPromptTemplates() {
        return promptTemplateRepository.findAll();
    }

    /**
     * 保存提示词模板
     * 
     * @param template 提示词模板对象
     * @return 保存后的模板对象
     */
    @Transactional
    public PromptTemplate savePromptTemplate(PromptTemplate template) {
        return promptTemplateRepository.save(template);
    }

    /**
     * 删除提示词模板
     * 
     * @param id 要删除的模板ID
     */
    @Transactional
    public void deletePromptTemplate(Long id) {
        promptTemplateRepository.deleteById(id);
    }
}
