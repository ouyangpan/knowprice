package com.knowprice.controller;

import com.knowprice.entity.LLMConfig;
import com.knowprice.entity.PromptTemplate;
import com.knowprice.service.LLMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 大语言模型(LLM)配置控制器
 * 
 * <p>提供LLM配置和提示词模板管理的RESTful API接口。
 * 支持配置大模型API参数、测试连接、管理提示词模板等功能，
 * 为智能定价建议提供AI能力支持。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>LLM配置的获取与保存（API地址、密钥、模型参数等）</li>
 *   <li>测试LLM连接是否正常</li>
 *   <li>提示词模板的增删改查</li>
 *   <li>检查LLM功能是否启用</li>
 * </ul>
 * 
 * <p>支持的LLM平台：</p>
 * <ul>
 *   <li>MiniMax（默认）</li>
 *   <li>OpenAI兼容接口</li>
 *   <li>其他支持Chat Completion API的平台</li>
 * </ul>
 * 
 * <p>API基础路径: /llm</p>
 * 
 * <p>使用场景：</p>
 * <p>系统在生成每日报告时，会调用配置的LLM生成智能定价建议，
 * 帮助用户做出更合理的定价决策。</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/llm")
@CrossOrigin(origins = "*")
public class LLMController {

    @Autowired
    private LLMService llmService;

    /**
     * 获取LLM配置
     * 
     * <p>返回当前系统的LLM配置信息，包括API端点、模型名称、参数设置等。
     * 敏感信息（如API密钥）会进行脱敏处理。</p>
     * 
     * @return 包含LLM配置的响应对象
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        LLMConfig config = llmService.getLLMConfig();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", config);
        return ResponseEntity.ok(response);
    }

    /**
     * 保存LLM配置
     * 
     * <p>保存或更新LLM配置信息。配置保存后会立即生效，用于后续的AI定价建议生成。</p>
     * 
     * <p>配置项说明：</p>
     * <ul>
     *   <li>enabled - 是否启用LLM功能</li>
     *   <li>apiEndpoint - LLM API地址</li>
     *   <li>apiKey - API密钥</li>
     *   <li>modelName - 使用的模型名称</li>
     *   <li>temperature - 生成温度（0-1，越高越随机）</li>
     *   <li>maxTokens - 最大生成Token数</li>
     * </ul>
     * 
     * @param config LLM配置对象
     * @return 包含保存后配置的响应对象
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody LLMConfig config) {
        LLMConfig savedConfig = llmService.saveLLMConfig(config);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "保存成功");
        response.put("data", savedConfig);
        return ResponseEntity.ok(response);
    }

    /**
     * 测试LLM连接
     * 
     * <p>使用当前配置测试LLM API连接是否正常。
     * 会发送一个简单的测试请求验证API可用性。</p>
     * 
     * <p>测试内容：发送"你好"消息，检查是否能收到正常响应。</p>
     * 
     * @return 包含测试结果的响应对象
     * 
     * <p>成功响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "连接测试成功"
     * }
     * </pre>
     * 
     * <p>失败响应示例：</p>
     * <pre>
     * {
     *   "code": 500,
     *   "message": "连接失败: API密钥无效"
     * }
     * </pre>
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> result = llmService.testConnection();
        Map<String, Object> response = new HashMap<>();
        
        if ((Boolean) result.get("success")) {
            response.put("code", 200);
        } else {
            response.put("code", 500);
        }
        
        response.put("message", result.get("message"));
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有提示词模板
     * 
     * <p>返回系统中所有的提示词模板，用于自定义LLM的输入提示。
     * 不同的模板适用于不同的分析场景。</p>
     * 
     * <p>模板类型：</p>
     * <ul>
     *   <li>系统提示词 - 定义AI角色和行为规范</li>
     *   <li>价格分析提示词 - 用于价格数据分析</li>
     *   <li>定价建议提示词 - 用于生成定价建议</li>
     * </ul>
     * 
     * @return 包含提示词模板列表的响应对象
     */
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getAllTemplates() {
        List<PromptTemplate> templates = llmService.getAllPromptTemplates();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", templates);
        return ResponseEntity.ok(response);
    }

    /**
     * 保存提示词模板
     * 
     * <p>创建或更新提示词模板。模板支持变量占位符，
     * 在生成建议时会被实际数据替换。</p>
     * 
     * <p>常用变量：</p>
     * <ul>
     *   <li>{hotelName} - 酒店名称</li>
     *   <li>{priceData} - 价格数据</li>
     *   <li>{date} - 日期</li>
     *   <li>{competitorPrices} - 竞品价格</li>
     * </ul>
     * 
     * @param template 提示词模板对象
     * @return 包含保存后模板的响应对象
     */
    @PostMapping("/templates")
    public ResponseEntity<Map<String, Object>> saveTemplate(@RequestBody PromptTemplate template) {
        PromptTemplate savedTemplate = llmService.savePromptTemplate(template);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "保存成功");
        response.put("data", savedTemplate);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除提示词模板
     * 
     * @param id 要删除的模板ID
     * @return 操作结果响应
     */
    @DeleteMapping("/templates/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        llmService.deletePromptTemplate(id);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "删除成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 检查LLM是否启用
     * 
     * <p>快速检查系统是否启用了LLM功能。前端可根据此状态
     * 决定是否显示AI相关功能入口。</p>
     * 
     * @return 包含启用状态的响应对象
     * 
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "获取成功",
     *   "data": { "enabled": true }
     * }
     * </pre>
     */
    @GetMapping("/enabled")
    public ResponseEntity<Map<String, Object>> isEnabled() {
        boolean enabled = llmService.isLLMEnabled();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", Map.of("enabled", enabled));
        return ResponseEntity.ok(response);
    }
}
