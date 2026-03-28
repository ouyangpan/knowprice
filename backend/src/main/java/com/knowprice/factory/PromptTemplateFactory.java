package com.knowprice.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提示词模板工厂
 * 
 * <p>负责加载和管理LLM提示词模板的工厂类。从classpath下的资源文件中加载
 * 预定义的提示词模板，支持变量占位符替换功能。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>启动时自动加载提示词模板文件</li>
 *   <li>提供系统提示词和用户提示词的获取方法</li>
 *   <li>支持变量占位符填充</li>
 *   <li>提供默认提示词作为后备</li>
 * </ul>
 * 
 * <p>模板文件位置：</p>
 * <ul>
 *   <li>系统提示词：prompts/hotel_system_prompt.md</li>
 *   <li>价格分析：prompts/analysis/price_analysis_prompt.md</li>
 *   <li>高价推荐：prompts/recommendation/high_price_recommendation.md</li>
 *   <li>低价推荐：prompts/recommendation/low_price_recommendation.md</li>
 *   <li>正常价格推荐：prompts/recommendation/normal_price_recommendation.md</li>
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
 * @see PromptType
 */
@Component
public class PromptTemplateFactory {

    private static final Logger logger = LoggerFactory.getLogger(PromptTemplateFactory.class);
    
    /**
     * 系统提示词映射表
     * 
     * <p>存储按类型分类的系统提示词，用于定义AI角色和行为规范。</p>
     */
    private final Map<PromptType, String> systemPrompts = new HashMap<>();
    
    /**
     * 用户提示词映射表
     * 
     * <p>存储按类型分类的用户提示词模板，包含具体分析请求。</p>
     */
    private final Map<PromptType, String> userPrompts = new HashMap<>();
    
    /**
     * 初始化方法
     * 
     * <p>在Bean创建后自动执行，加载所有提示词模板文件。</p>
     */
    @PostConstruct
    public void init() {
        loadPrompts();
    }
    
    /**
     * 加载提示词模板文件
     * 
     * <p>从classpath资源目录加载预定义的提示词模板文件，
     * 并按类型存储到内存映射表中。</p>
     */
    private void loadPrompts() {
        try {
            systemPrompts.put(PromptType.HOTEL_SYSTEM, loadFile("prompts/hotel_system_prompt.md"));
            userPrompts.put(PromptType.PRICE_ANALYSIS, loadFile("prompts/analysis/price_analysis_prompt.md"));
            userPrompts.put(PromptType.HIGH_PRICE_RECOMMENDATION, loadFile("prompts/recommendation/high_price_recommendation.md"));
            userPrompts.put(PromptType.LOW_PRICE_RECOMMENDATION, loadFile("prompts/recommendation/low_price_recommendation.md"));
            userPrompts.put(PromptType.NORMAL_PRICE_RECOMMENDATION, loadFile("prompts/recommendation/normal_price_recommendation.md"));
            
            logger.info("提示词模板加载完成，共加载 {} 个系统提示词，{} 个用户提示词", 
                systemPrompts.size(), userPrompts.size());
        } catch (Exception e) {
            logger.error("加载提示词模板失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 从classpath加载文件内容
     * 
     * @param path 文件路径（相对于classpath根目录）
     * @return 文件内容字符串
     * @throws Exception 文件读取失败时抛出异常
     */
    private String loadFile(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    /**
     * 获取系统提示词
     * 
     * <p>根据类型获取对应的系统提示词。如果未找到，返回默认系统提示词。</p>
     * 
     * @param type 提示词类型
     * @return 系统提示词内容
     */
    public String getSystemPrompt(PromptType type) {
        String prompt = systemPrompts.get(type);
        if (prompt == null) {
            logger.warn("未找到系统提示词: {}", type);
            return getDefaultSystemPrompt();
        }
        return prompt;
    }
    
    /**
     * 获取用户提示词
     * 
     * <p>根据类型获取对应的用户提示词模板。如果未找到，返回默认用户提示词。</p>
     * 
     * @param type 提示词类型
     * @return 用户提示词模板内容
     */
    public String getUserPrompt(PromptType type) {
        String prompt = userPrompts.get(type);
        if (prompt == null) {
            logger.warn("未找到用户提示词: {}", type);
            return getDefaultUserPrompt();
        }
        return prompt;
    }
    
    /**
     * 获取用户提示词并填充变量
     * 
     * <p>根据类型获取用户提示词模板，并将变量占位符替换为实际值。</p>
     * 
     * <p>示例：</p>
     * <pre>
     * Map&lt;String, String&gt; variables = new HashMap&lt;&gt;();
     * variables.put("hotel_name", "杭州西湖大酒店");
     * variables.put("star_rating", "五星级");
     * String prompt = factory.getUserPrompt(PromptType.PRICE_ANALYSIS, variables);
     * </pre>
     * 
     * @param type 提示词类型
     * @param variables 变量名到变量值的映射
     * @return 填充变量后的用户提示词
     */
    public String getUserPrompt(PromptType type, Map<String, String> variables) {
        String template = getUserPrompt(type);
        return fillVariables(template, variables);
    }
    
    /**
     * 填充变量占位符
     * 
     * <p>将模板中的{变量名}占位符替换为实际值。</p>
     * 
     * @param template 模板字符串
     * @param variables 变量映射表
     * @return 填充后的字符串
     */
    private String fillVariables(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    
    /**
     * 获取默认系统提示词
     * 
     * <p>当加载失败或未找到对应类型时使用的后备提示词。</p>
     * 
     * @return 默认系统提示词
     */
    private String getDefaultSystemPrompt() {
        return "你是一位专业的酒店定价策略顾问，拥有丰富的酒店收益管理经验。";
    }
    
    /**
     * 获取默认用户提示词
     * 
     * <p>当加载失败或未找到对应类型时使用的后备提示词。</p>
     * 
     * @return 默认用户提示词
     */
    private String getDefaultUserPrompt() {
        return "请基于以下数据为我的酒店提供定价建议：";
    }
    
    /**
     * 提示词类型枚举
     * 
     * <p>定义系统中使用的提示词类型，每种类型对应不同的分析场景。</p>
     */
    public enum PromptType {
        /**
         * 酒店系统提示词
         * 
         * <p>定义AI作为酒店定价顾问的角色和行为规范。</p>
         */
        HOTEL_SYSTEM("酒店系统提示词"),
        
        /**
         * 价格分析提示词
         * 
         * <p>用于分析酒店价格数据，生成综合性的定价建议。</p>
         */
        PRICE_ANALYSIS("价格分析提示词"),
        
        /**
         * 高价推荐提示词
         * 
         * <p>当自有酒店价格高于市场平均价时使用，
         * 生成降价或保持高价策略的建议。</p>
         */
        HIGH_PRICE_RECOMMENDATION("高价推荐提示词"),
        
        /**
         * 低价推荐提示词
         * 
         * <p>当自有酒店价格低于市场平均价时使用，
         * 生成提价或保持低价策略的建议。</p>
         */
        LOW_PRICE_RECOMMENDATION("低价推荐提示词"),
        
        /**
         * 正常价格推荐提示词
         * 
         * <p>当自有酒店价格处于合理区间时使用，
         * 生成保持当前策略的建议。</p>
         */
        NORMAL_PRICE_RECOMMENDATION("正常价格推荐提示词");
        
        /**
         * 类型描述
         */
        private final String description;
        
        PromptType(String description) {
            this.description = description;
        }
        
        /**
         * 获取类型描述
         * 
         * @return 类型描述文本
         */
        public String getDescription() {
            return description;
        }
    }
}
