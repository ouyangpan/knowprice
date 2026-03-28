package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报告实体类
 * 
 * <p>表示每日价格分析报告，包含价格汇总、趋势分析、竞品对比和智能建议等内容。
 * 每天最多生成一份报告。</p>
 * 
 * <p>数据库表：reports</p>
 * 
 * <p>报告内容结构（content字段JSON格式）：</p>
 * <ul>
 *   <li>reportDate - 报告日期</li>
 *   <li>generatedAt - 生成时间</li>
 *   <li>priceSummary - 价格汇总统计（按平台统计平均价、最高价、最低价）</li>
 *   <li>priceTrend - 近7天价格趋势数据</li>
 *   <li>competitorComparison - 竞品价格对比分析</li>
 *   <li>ruleSuggestions - 基于规则的定价建议</li>
 * </ul>
 * 
 * <p>生成时机：</p>
 * <ul>
 *   <li>定时任务：每日凌晨3点自动生成</li>
 *   <li>手动触发：通过API接口手动生成</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see ReportService
 */
@Data
@Entity
@Table(name = "reports")
public class Report {
    
    /**
     * 报告唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 报告日期
     * 
     * <p>报告对应的日期，每天最多一份报告。
     * 用于按日期查询报告。</p>
     */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;
    
    /**
     * 报告内容
     * 
     * <p>报告的详细内容，以JSON格式存储。
     * 包含价格汇总、趋势分析、竞品对比等结构化数据。</p>
     * 
     * <p>JSON结构示例：</p>
     * <pre>
     * {
     *   "reportDate": "2024-01-15",
     *   "generatedAt": "2024-01-15T03:00:00",
     *   "priceSummary": [...],
     *   "priceTrend": [...],
     *   "competitorComparison": [...],
     *   "ruleSuggestions": "..."
     * }
     * </pre>
     */
    @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    /**
     * LLM智能建议
     * 
     * <p>由大语言模型生成的智能定价建议文本。
     * 如果LLM功能未启用，此字段为null。</p>
     * 
     * <p>建议内容包括：</p>
     * <ul>
     *   <li>市场行情分析</li>
     *   <li>定价策略建议</li>
     *   <li>竞争态势分析</li>
     *   <li>收益优化建议</li>
     * </ul>
     */
    @Column(name = "llm_suggestions", columnDefinition = "TEXT")
    private String llmSuggestions;
    
    /**
     * 创建时间
     * 
     * <p>报告生成的时间戳。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
