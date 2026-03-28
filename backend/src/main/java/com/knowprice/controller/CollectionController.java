package com.knowprice.controller;

import com.knowprice.entity.CollectionConfig;
import com.knowprice.entity.PriceRecord;
import com.knowprice.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 价格采集控制器
 * 
 * <p>提供酒店价格采集的RESTful API接口，支持从多个OTA平台（携程、同程、美团）
 * 采集酒店价格数据，并管理采集配置。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>触发全平台价格采集</li>
 *   <li>触发指定平台价格采集</li>
 *   <li>查询价格记录（支持按日期、平台、酒店筛选）</li>
 *   <li>管理采集配置（区域、星级、价格范围等）</li>
 * </ul>
 * 
 * <p>支持的平台：</p>
 * <ul>
 *   <li>ctrip - 携程旅行网</li>
 *   <li>ly - 同程旅行</li>
 *   <li>meituan - 美团酒店</li>
 * </ul>
 * 
 * <p>API基础路径: /collection</p>
 * 
 * <p>采集原理：</p>
 * <p>使用Playwright无头浏览器模拟用户访问OTA平台，
 * 解析页面数据获取酒店价格信息，支持动态渲染的页面内容。</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/collection")
@CrossOrigin(origins = "*")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    /**
     * 触发全平台价格采集
     * 
     * <p>同时触发携程、同程、美团三个平台的价格采集任务。
     * 采集结果会保存到数据库中，用于后续的价格分析和报告生成。</p>
     * 
     * <p>采集流程：</p>
     * <ol>
     *   <li>读取采集配置（区域、星级等筛选条件）</li>
     *   <li>依次调用各平台采集器</li>
     *   <li>解析并保存价格数据</li>
     *   <li>返回采集统计结果</li>
     * </ol>
     * 
     * @return 包含采集结果的响应对象（成功数量、失败数量等统计信息）
     */
    @PostMapping("/trigger")
    public ResponseEntity<Map<String, Object>> triggerCollection() {
        Map<String, Object> result = collectionService.collectPrices();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", result.get("message"));
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    /**
     * 触发指定平台价格采集
     * 
     * <p>只采集指定OTA平台的酒店价格数据，适用于需要单独更新某个平台数据的场景。</p>
     * 
     * @param platform 平台名称（ctrip/ly/meituan）
     * @return 包含采集结果的响应对象
     * 
     * <p>响应示例：</p>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "采集完成",
     *   "data": {
     *     "success": true,
     *     "platform": "ctrip",
     *     "collectedCount": 50
     *   }
     * }
     * </pre>
     */
    @PostMapping("/trigger/{platform}")
    public ResponseEntity<Map<String, Object>> triggerCollectionForPlatform(@PathVariable String platform) {
        Map<String, Object> result = collectionService.collectPricesForPlatform(platform);
        Map<String, Object> response = new HashMap<>();
        if ((Boolean) result.get("success")) {
            response.put("code", 200);
            response.put("message", result.get("message"));
        } else {
            response.put("code", 500);
            response.put("message", result.get("message"));
        }
        response.put("data", result);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取价格记录
     * 
     * <p>查询已采集的价格记录，支持多种筛选条件。如果不传任何参数，默认返回今日的价格记录。</p>
     * 
     * <p>筛选优先级：日期 > 平台 > 酒店ID</p>
     * 
     * @param date 按日期筛选（可选），格式：yyyy-MM-dd
     * @param platform 按平台筛选（可选），值：ctrip/ly/meituan
     * @param hotelId 按酒店ID筛选（可选）
     * @return 包含价格记录列表的响应对象
     * 
     * <p>价格记录包含：</p>
     * <ul>
     *   <li>酒店信息</li>
     *   <li>房型信息</li>
     *   <li>平台名称</li>
     *   <li>价格金额</li>
     *   <li>是否有房</li>
     *   <li>采集时间</li>
     * </ul>
     */
    @GetMapping("/records")
    public ResponseEntity<Map<String, Object>> getPriceRecords(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Long hotelId) {
        
        List<PriceRecord> records;
        
        if (date != null) {
            records = collectionService.getPriceRecordsByDate(date);
        } else if (platform != null) {
            records = collectionService.getPriceRecordsByPlatform(platform);
        } else if (hotelId != null) {
            records = collectionService.getPriceRecordsByHotel(hotelId);
        } else {
            records = collectionService.getPriceRecordsByDate(LocalDate.now());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", records);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前采集配置
     * 
     * <p>返回当前激活的采集配置，包括筛选条件和定时设置。</p>
     * 
     * @return 包含采集配置的响应对象
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        CollectionConfig config = collectionService.getActiveConfig();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", config);
        return ResponseEntity.ok(response);
    }

    /**
     * 保存采集配置
     * 
     * <p>保存或更新采集配置，配置会影响价格采集的筛选范围和定时任务。</p>
     * 
     * <p>配置项说明：</p>
     * <ul>
     *   <li>region - 目标采集区域/城市</li>
     *   <li>starRatings - 星级筛选列表（如：[3,4,5]）</li>
     *   <li>brands - 品牌筛选列表</li>
     *   <li>priceRange - 价格区间范围</li>
     *   <li>scheduledTime - 定时采集时间</li>
     *   <li>active - 是否启用</li>
     * </ul>
     * 
     * @param config 采集配置对象
     * @return 包含保存后配置的响应对象
     */
    @PostMapping("/config")
    public ResponseEntity<Map<String, Object>> saveConfig(@RequestBody CollectionConfig config) {
        CollectionConfig savedConfig = collectionService.saveConfig(config);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "保存成功");
        response.put("data", savedConfig);
        return ResponseEntity.ok(response);
    }
}
