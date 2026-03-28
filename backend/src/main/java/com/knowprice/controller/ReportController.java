package com.knowprice.controller;

import com.knowprice.entity.Report;
import com.knowprice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报告管理控制器
 * 
 * <p>提供酒店价格分析报告的RESTful API接口，支持报告的查询、生成和删除操作。
 * 报告包含每日价格汇总、趋势分析、竞品对比以及LLM智能定价建议等内容。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>获取所有报告或按日期范围筛选报告</li>
 *   <li>根据ID或日期查询单个报告</li>
 *   <li>手动触发生成每日报告</li>
 *   <li>删除指定报告</li>
 * </ul>
 * 
 * <p>API基础路径: /reports</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取报告列表
     * 
     * <p>支持按日期范围筛选报告。如果不传日期参数，则返回所有报告。</p>
     * 
     * @param startDate 开始日期（可选），格式：yyyy-MM-dd
     * @param endDate 结束日期（可选），格式：yyyy-MM-dd
     * @return 包含报告列表的响应对象
     * 
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "获取成功",
     *   "data": [Report对象列表]
     * }
     * </pre>
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<Report> reports;
        
        if (startDate != null && endDate != null) {
            reports = reportService.getReportsByDateRange(startDate, endDate);
        } else {
            reports = reportService.getAllReports();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", reports);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取单个报告
     * 
     * @param id 报告ID
     * @return 包含报告详情的响应对象，如果不存在则返回404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getReportById(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        Map<String, Object> response = new HashMap<>();
        
        if (report != null) {
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", report);
        } else {
            response.put("code", 404);
            response.put("message", "报告不存在");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 根据日期获取报告
     * 
     * <p>获取指定日期的价格分析报告，每个日期最多只有一份报告。</p>
     * 
     * @param date 报告日期，格式：yyyy-MM-dd
     * @return 包含报告详情的响应对象，如果不存在则返回404
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getReportByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Report report = reportService.getReportByDate(date);
        Map<String, Object> response = new HashMap<>();
        
        if (report != null) {
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", report);
        } else {
            response.put("code", 404);
            response.put("message", "报告不存在");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 生成每日报告
     * 
     * <p>手动触发生成当日价格分析报告。报告内容包括：</p>
     * <ul>
     *   <li>价格汇总统计（最高价、最低价、平均价）</li>
     *   <li>价格趋势分析</li>
     *   <li>竞品价格对比</li>
     *   <li>LLM智能定价建议（如果启用）</li>
     * </ul>
     * 
     * @return 包含新生成报告的响应对象
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport() {
        try {
            Report report = reportService.generateDailyReport();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "报告生成成功");
            response.put("data", report);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "报告生成失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 删除报告
     * 
     * @param id 要删除的报告ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
