package com.knowprice.service;

import com.knowprice.collector.PriceCollector;
import com.knowprice.entity.CollectionConfig;
import com.knowprice.entity.Hotel;
import com.knowprice.entity.PriceRecord;
import com.knowprice.repository.CollectionConfigRepository;
import com.knowprice.repository.HotelRepository;
import com.knowprice.repository.PriceRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 价格采集服务
 * 
 * <p>负责从多个OTA平台（携程、同程、美团）采集酒店价格数据的核心业务服务。
 * 通过调用各平台的采集器（Collector）实现数据抓取，并将结果持久化到数据库。</p>
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li>协调多平台价格采集任务的执行</li>
 *   <li>管理采集配置（区域、星级、定时等）</li>
 *   <li>保存采集结果到数据库</li>
 *   <li>提供价格记录查询服务</li>
 * </ul>
 * 
 * <p>采集流程：</p>
 * <ol>
 *   <li>读取激活的采集配置</li>
 *   <li>根据配置筛选目标酒店</li>
 *   <li>调用对应平台的采集器执行采集</li>
 *   <li>解析采集结果并保存到数据库</li>
 *   <li>返回采集统计信息</li>
 * </ol>
 * 
 * <p>支持的平台：</p>
 * <ul>
 *   <li>ctrip - 携程旅行网</li>
 *   <li>ly - 同程旅行</li>
 *   <li>meituan - 美团酒店</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see PriceCollector
 * @see CollectionConfig
 */
@Service
public class CollectionService {

    private static final Logger logger = LoggerFactory.getLogger(CollectionService.class);

    @Autowired
    private CollectionConfigRepository collectionConfigRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private PriceRecordRepository priceRecordRepository;

    @Autowired
    private List<PriceCollector> priceCollectors;

    /**
     * 执行全平台价格采集
     * 
     * <p>同时触发携程、同程、美团三个平台的价格采集任务。
     * 采集结果会自动保存到数据库中，用于后续的价格分析和报告生成。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>读取激活的采集配置</li>
     *   <li>依次调用各平台采集器</li>
     *   <li>为每条记录设置采集日期并保存</li>
     *   <li>记录成功/失败的平台列表</li>
     * </ol>
     * 
     * @return 采集结果Map，包含：
     *         <ul>
     *           <li>success - 是否全部成功</li>
     *           <li>message - 结果消息</li>
     *           <li>successPlatforms - 成功的平台列表</li>
     *           <li>failedPlatforms - 失败的平台列表</li>
     *           <li>timestamp - 采集时间戳</li>
     *         </ul>
     */
    @Transactional
    public Map<String, Object> collectPrices() {
        logger.info("开始执行价格采集任务");
        
        List<CollectionConfig> configs = collectionConfigRepository.findByIsActiveTrue();
        if (configs.isEmpty()) {
            logger.warn("没有找到激活的采集配置");
            return Map.of(
                "success", false,
                "message", "没有找到激活的采集配置"
            );
        }

        CollectionConfig config = configs.get(0);
        List<String> platforms = List.of("ctrip", "ly", "meituan");
        
        List<String> successPlatforms = new ArrayList<>();
        List<String> failedPlatforms = new ArrayList<>();
        
        for (String platform : platforms) {
            try {
                logger.info("开始采集 {} 平台数据", platform);
                PriceCollector collector = getCollector(platform);
                
                if (collector != null) {
                    List<PriceRecord> records = collector.collect(config.getRegion(), config.getStarRatings());
                    
                    for (PriceRecord record : records) {
                        record.setRecordDate(LocalDate.now());
                        priceRecordRepository.save(record);
                    }
                    
                    successPlatforms.add(platform);
                    logger.info("{} 平台采集成功，共 {} 条记录", platform, records.size());
                } else {
                    failedPlatforms.add(platform);
                    logger.warn("未找到 {} 平台的采集器", platform);
                }
            } catch (Exception e) {
                failedPlatforms.add(platform);
                logger.error("{} 平台采集失败: {}", platform, e.getMessage());
            }
        }

        logger.info("价格采集任务完成，成功: {}，失败: {}", successPlatforms, failedPlatforms);
        
        return Map.of(
            "success", true,
            "message", "采集完成",
            "successPlatforms", successPlatforms,
            "failedPlatforms", failedPlatforms,
            "timestamp", LocalDateTime.now().toString()
        );
    }

    /**
     * 执行指定平台价格采集
     * 
     * <p>只采集指定OTA平台的酒店价格数据，适用于需要单独更新某个平台数据的场景。
     * 比如某个平台数据异常需要重新采集时使用。</p>
     * 
     * @param platform 平台名称（ctrip/ly/meituan）
     * @return 采集结果Map，包含：
     *         <ul>
     *           <li>success - 是否成功</li>
     *           <li>message - 结果消息</li>
     *           <li>count - 采集记录数</li>
     *           <li>platform - 平台名称</li>
     *           <li>timestamp - 采集时间戳</li>
     *         </ul>
     */
    @Transactional
    public Map<String, Object> collectPricesForPlatform(String platform) {
        logger.info("开始采集 {} 平台数据", platform);
        
        try {
            List<CollectionConfig> configs = collectionConfigRepository.findByIsActiveTrue();
            if (configs.isEmpty()) {
                return Map.of(
                    "success", false,
                    "message", "没有找到激活的采集配置"
                );
            }

            CollectionConfig config = configs.get(0);
            PriceCollector collector = getCollector(platform);
            
            if (collector != null) {
                List<PriceRecord> records = collector.collect(config.getRegion(), config.getStarRatings());
                
                for (PriceRecord record : records) {
                    record.setRecordDate(LocalDate.now());
                    priceRecordRepository.save(record);
                }
                
                logger.info("{} 平台采集成功，共 {} 条记录", platform, records.size());
                
                return Map.of(
                    "success", true,
                    "message", "采集成功",
                    "count", records.size(),
                    "platform", platform,
                    "timestamp", LocalDateTime.now().toString()
                );
            } else {
                return Map.of(
                    "success", false,
                    "message", "未找到该平台的采集器"
                );
            }
        } catch (Exception e) {
            logger.error("{} 平台采集失败: {}", platform, e.getMessage());
            return Map.of(
                "success", false,
                "message", "采集失败: " + e.getMessage()
            );
        }
    }

    /**
     * 获取指定平台的采集器
     * 
     * <p>从注入的采集器列表中查找匹配指定平台的采集器实例。
     * Spring会自动注入所有实现了PriceCollector接口的Bean。</p>
     * 
     * @param platform 平台名称
     * @return 匹配的采集器实例，未找到则返回null
     */
    private PriceCollector getCollector(String platform) {
        for (PriceCollector collector : priceCollectors) {
            if (collector.getPlatformName().equalsIgnoreCase(platform)) {
                return collector;
            }
        }
        return null;
    }

    /**
     * 按日期查询价格记录
     * 
     * <p>获取指定日期采集的所有价格记录，用于查看某日的价格数据。</p>
     * 
     * @param date 查询日期
     * @return 该日期的价格记录列表
     */
    public List<PriceRecord> getPriceRecordsByDate(LocalDate date) {
        return priceRecordRepository.findByRecordDate(date);
    }

    /**
     * 按平台查询价格记录
     * 
     * <p>获取指定平台的所有价格记录，用于分析某个平台的价格数据。</p>
     * 
     * @param platform 平台名称（ctrip/ly/meituan）
     * @return 该平台的价格记录列表
     */
    public List<PriceRecord> getPriceRecordsByPlatform(String platform) {
        return priceRecordRepository.findByPlatform(platform);
    }

    /**
     * 按酒店查询价格记录
     * 
     * <p>获取指定酒店的所有价格记录，用于查看某个酒店在各平台的价格历史。</p>
     * 
     * @param hotelId 酒店ID
     * @return 该酒店的价格记录列表
     */
    public List<PriceRecord> getPriceRecordsByHotel(Long hotelId) {
        return priceRecordRepository.findByHotel_Id(hotelId);
    }

    /**
     * 获取当前激活的采集配置
     * 
     * <p>返回系统中当前激活的采集配置。如果存在多个激活配置，只返回第一个。</p>
     * 
     * @return 激活的采集配置，如果没有则返回null
     */
    public CollectionConfig getActiveConfig() {
        List<CollectionConfig> configs = collectionConfigRepository.findByIsActiveTrue();
        return configs.isEmpty() ? null : configs.get(0);
    }

    /**
     * 保存采集配置
     * 
     * <p>新增或更新采集配置。配置会影响价格采集的筛选范围和定时任务。</p>
     * 
     * @param config 采集配置对象
     * @return 保存后的配置对象
     */
    @Transactional
    public CollectionConfig saveConfig(CollectionConfig config) {
        return collectionConfigRepository.save(config);
    }
}
