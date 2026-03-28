package com.knowprice.service;

import com.knowprice.entity.Hotel;
import com.knowprice.entity.PriceRecord;
import com.knowprice.entity.Report;
import com.knowprice.repository.HotelRepository;
import com.knowprice.repository.PriceRecordRepository;
import com.knowprice.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 报告生成服务
 * 
 * <p>负责生成酒店价格分析报告的核心业务服务。整合价格数据，进行多维度分析，
 * 并调用LLM服务生成智能定价建议。</p>
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li>生成每日价格分析报告</li>
 *   <li>价格汇总统计（按平台统计最高价、最低价、平均价）</li>
 *   <li>价格趋势分析（近7天价格走势）</li>
 *   <li>竞品价格对比分析</li>
 *   <li>基于规则的建议生成</li>
 *   <li>调用LLM生成智能定价建议</li>
 * </ul>
 * 
 * <p>报告内容结构：</p>
 * <ul>
 *   <li>reportDate - 报告日期</li>
 *   <li>generatedAt - 生成时间</li>
 *   <li>priceSummary - 价格汇总统计</li>
 *   <li>priceTrend - 价格趋势数据</li>
 *   <li>competitorComparison - 竞品对比分析</li>
 *   <li>ruleSuggestions - 规则建议</li>
 *   <li>llmSuggestions - LLM智能建议（如果启用）</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see LLMService
 * @see Report
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PriceRecordRepository priceRecordRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private LLMService llmService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 生成每日报告
     * 
     * <p>生成当日价格分析报告，包含价格汇总、趋势分析、竞品对比和智能建议。
     * 如果当日报告已存在，则直接返回已有报告。</p>
     * 
     * <p>生成流程：</p>
     * <ol>
     *   <li>检查今日报告是否已存在</li>
     *   <li>获取今日价格记录</li>
     *   <li>生成价格汇总统计</li>
     *   <li>生成近7天价格趋势</li>
     *   <li>生成竞品对比分析</li>
     *   <li>生成规则建议</li>
     *   <li>调用LLM生成智能建议（如果启用）</li>
     *   <li>保存报告到数据库</li>
     * </ol>
     * 
     * @return 生成的报告对象
     */
    @Transactional
    public Report generateDailyReport() {
        logger.info("开始生成每日报告");
        
        LocalDate today = LocalDate.now();
        
        Report existingReport = reportRepository.findByReportDate(today);
        if (existingReport != null) {
            logger.info("今日报告已存在，将更新报告");
            return existingReport;
        }

        List<PriceRecord> todayRecords = priceRecordRepository.findByRecordDate(today);
        
        Map<String, Object> reportContent = new HashMap<>();
        
        reportContent.put("reportDate", today.toString());
        reportContent.put("generatedAt", LocalDateTime.now().toString());
        
        reportContent.put("priceSummary", generatePriceSummary(todayRecords));
        
        reportContent.put("priceTrend", generatePriceTrend());
        
        reportContent.put("competitorComparison", generateCompetitorComparison(todayRecords));
        
        reportContent.put("ruleSuggestions", generateRuleBasedSuggestions(todayRecords));
        
        String llmSuggestions = null;
        try {
            if (llmService.isLLMEnabled()) {
                llmSuggestions = llmService.generatePricingSuggestions(todayRecords);
                logger.info("大模型建议生成成功");
            }
        } catch (Exception e) {
            logger.error("大模型建议生成失败: {}", e.getMessage());
        }

        Report report = new Report();
        report.setReportDate(today);
        try {
            report.setContent(objectMapper.writeValueAsString(reportContent));
        } catch (Exception e) {
            report.setContent("{}");
            logger.error("报告内容序列化失败", e);
        }
        report.setLlmSuggestions(llmSuggestions);
        
        Report savedReport = reportRepository.save(report);
        
        logger.info("每日报告生成完成，报告ID: {}", savedReport.getId());
        
        return savedReport;
    }

    /**
     * 生成价格汇总统计
     * 
     * <p>按平台统计价格数据，计算每个平台的平均价、最低价、最高价和记录数。</p>
     * 
     * @param records 价格记录列表
     * @return 价格汇总列表，每个元素包含平台名称和统计数据
     */
    private List<Map<String, Object>> generatePriceSummary(List<PriceRecord> records) {
        Map<String, List<Double>> platformPrices = new HashMap<>();
        
        for (PriceRecord record : records) {
            String platform = record.getPlatform();
            platformPrices.computeIfAbsent(platform, k -> new ArrayList<>()).add(record.getPrice());
        }
        
        List<Map<String, Object>> summary = new ArrayList<>();
        
        for (Map.Entry<String, List<Double>> entry : platformPrices.entrySet()) {
            Map<String, Object> platformSummary = new HashMap<>();
            platformSummary.put("platform", entry.getKey());
            
            List<Double> prices = entry.getValue();
            double avgPrice = prices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double minPrice = prices.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double maxPrice = prices.stream().mapToDouble(Double::doubleValue).max().orElse(0);
            
            platformSummary.put("avgPrice", Math.round(avgPrice * 100) / 100.0);
            platformSummary.put("minPrice", Math.round(minPrice * 100) / 100.0);
            platformSummary.put("maxPrice", Math.round(maxPrice * 100) / 100.0);
            platformSummary.put("count", prices.size());
            
            summary.add(platformSummary);
        }
        
        return summary;
    }

    /**
     * 生成价格趋势数据
     * 
     * <p>统计近7天各平台的平均价格走势，用于分析价格变化趋势。</p>
     * 
     * @return 趋势数据列表，每个元素包含日期和各平台平均价格
     */
    private List<Map<String, Object>> generatePriceTrend() {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<PriceRecord> records = priceRecordRepository.findByRecordDate(date);
            
            Map<String, Object> dayTrend = new HashMap<>();
            dayTrend.put("date", date.toString());
            
            Map<String, Double> platformAvgPrices = new HashMap<>();
            Map<String, List<Double>> platformPrices = new HashMap<>();
            
            for (PriceRecord record : records) {
                String platform = record.getPlatform();
                platformPrices.computeIfAbsent(platform, k -> new ArrayList<>()).add(record.getPrice());
            }
            
            for (Map.Entry<String, List<Double>> entry : platformPrices.entrySet()) {
                double avg = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0);
                platformAvgPrices.put(entry.getKey(), Math.round(avg * 100) / 100.0);
            }
            
            dayTrend.put("prices", platformAvgPrices);
            trend.add(dayTrend);
        }
        
        return trend;
    }

    /**
     * 生成竞品价格对比分析
     * 
     * <p>对比自有酒店与竞品酒店的价格差异，计算价格差异百分比。
     * 帮助了解自有酒店在市场中的价格定位。</p>
     * 
     * <p>计算公式：价格差异百分比 = (竞品价格 - 自有酒店最低价) / 自有酒店最低价 * 100</p>
     * 
     * @param records 价格记录列表
     * @return 竞品对比列表，每个元素包含竞品信息和价格差异
     */
    private List<Map<String, Object>> generateCompetitorComparison(List<PriceRecord> records) {
        List<Map<String, Object>> comparison = new ArrayList<>();
        
        Map<String, List<Double>> hotelPrices = new HashMap<>();
        
        for (PriceRecord record : records) {
            String hotelName = record.getHotel().getName();
            String key = hotelName + "_" + record.getPlatform();
            hotelPrices.computeIfAbsent(key, k -> new ArrayList<>()).add(record.getPrice());
        }
        
        List<Hotel> ownHotels = hotelRepository.findByIsOwnTrue();
        
        for (Hotel ownHotel : ownHotels) {
            double ownMinPrice = Double.MAX_VALUE;
            
            for (PriceRecord record : records) {
                if (record.getHotel().getId().equals(ownHotel.getId())) {
                    ownMinPrice = Math.min(ownMinPrice, record.getPrice());
                }
            }
            
            if (ownMinPrice == Double.MAX_VALUE) {
                continue;
            }
            
            for (PriceRecord record : records) {
                if (!record.getHotel().getId().equals(ownHotel.getId())) {
                    Map<String, Object> comp = new HashMap<>();
                    comp.put("hotelName", record.getHotel().getName());
                    comp.put("platform", record.getPlatform());
                    comp.put("roomType", record.getRoomType().getName());
                    comp.put("price", record.getPrice());
                    
                    double diff = ((record.getPrice() - ownMinPrice) / ownMinPrice) * 100;
                    comp.put("priceDiff", Math.round(diff * 100) / 100.0);
                    
                    comparison.add(comp);
                }
            }
        }
        
        return comparison;
    }

    /**
     * 生成基于规则的建议
     * 
     * <p>根据自有酒店价格与市场平均价格的对比，生成简单的定价建议。
     * 这是一个基于固定规则的快速建议，更详细的分析由LLM提供。</p>
     * 
     * <p>建议规则：</p>
     * <ul>
     *   <li>价格低于市场平均10%以上：建议适当提价</li>
     *   <li>价格高于市场平均10%以上：建议适当降价</li>
     *   <li>价格在合理区间：建议保持当前策略</li>
     * </ul>
     * 
     * @param records 价格记录列表
     * @return 规则建议文本
     */
    private String generateRuleBasedSuggestions(List<PriceRecord> records) {
        if (records.isEmpty()) {
            return "暂无足够数据进行分析建议";
        }
        
        Map<String, List<Double>> platformPrices = new HashMap<>();
        
        for (PriceRecord record : records) {
            String platform = record.getPlatform();
            platformPrices.computeIfAbsent(platform, k -> new ArrayList<>()).add(record.getPrice());
        }
        
        double overallAvg = records.stream().mapToDouble(PriceRecord::getPrice).average().orElse(0);
        
        List<Hotel> ownHotels = hotelRepository.findByIsOwnTrue();
        
        if (ownHotels.isEmpty()) {
            return "请先在系统中添加自有酒店信息";
        }
        
        StringBuilder suggestion = new StringBuilder();
        
        for (Hotel ownHotel : ownHotels) {
            List<Double> ownPrices = new ArrayList<>();
            for (PriceRecord record : records) {
                if (record.getHotel().getId().equals(ownHotel.getId())) {
                    ownPrices.add(record.getPrice());
                }
            }
            
            if (!ownPrices.isEmpty()) {
                double ownAvg = ownPrices.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                double diffPercent = ((overallAvg - ownAvg) / overallAvg) * 100;
                
                if (diffPercent > 10) {
                    suggestion.append(String.format("【%s】价格低于市场平均%.1f%%，建议适当提价以增加收益。", 
                        ownHotel.getName(), Math.abs(diffPercent)));
                } else if (diffPercent < -10) {
                    suggestion.append(String.format("【%s】价格高于市场平均%.1f%%，建议适当降价以提升竞争力。", 
                        ownHotel.getName(), Math.abs(diffPercent)));
                } else {
                    suggestion.append(String.format("【%s】价格处于市场合理区间，建议保持当前定价策略。", 
                        ownHotel.getName()));
                }
            }
        }
        
        return suggestion.length() > 0 ? suggestion.toString() : "数据不足，无法生成具体建议";
    }

    /**
     * 按日期获取报告
     * 
     * @param date 报告日期
     * @return 该日期的报告，如果不存在则返回null
     */
    public Report getReportByDate(LocalDate date) {
        return reportRepository.findByReportDate(date);
    }

    /**
     * 按日期范围获取报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的报告列表
     */
    public List<Report> getReportsByDateRange(LocalDate startDate, LocalDate endDate) {
        return reportRepository.findByReportDateBetween(startDate, endDate);
    }

    /**
     * 获取所有报告（按日期倒序）
     * 
     * @return 所有报告列表，按报告日期降序排列
     */
    public List<Report> getAllReports() {
        return reportRepository.findAllOrderByReportDateDesc();
    }

    /**
     * 按ID获取报告
     * 
     * @param id 报告ID
     * @return 报告对象，如果不存在则返回null
     */
    public Report getReportById(Long id) {
        return reportRepository.findById(id).orElse(null);
    }

    /**
     * 删除报告
     * 
     * @param id 要删除的报告ID
     */
    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }
}
