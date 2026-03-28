package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 采集配置实体类
 * 
 * <p>表示价格采集任务的配置参数，包括采集区域、星级筛选、品牌筛选、
 * 价格范围和定时设置等。</p>
 * 
 * <p>数据库表：collection_configs</p>
 * 
 * <p>配置项说明：</p>
 * <ul>
 *   <li>region - 目标采集区域/城市</li>
 *   <li>starRatings - 星级筛选列表（JSON数组）</li>
 *   <li>brands - 品牌筛选列表（JSON数组）</li>
 *   <li>priceRange - 价格区间范围</li>
 *   <li>scheduleTime - 定时采集时间</li>
 *   <li>isActive - 是否启用该配置</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>价格采集时读取配置决定采集范围</li>
 *   <li>定时任务根据配置执行自动采集</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see CollectionService
 */
@Data
@Entity
@Table(name = "collection_configs")
public class CollectionConfig {
    
    /**
     * 配置唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 采集区域
     * 
     * <p>目标采集的城市或区域名称。
     * 采集器会根据此参数筛选对应区域的酒店。</p>
     * 
     * <p>示例：杭州、上海、北京等</p>
     */
    @Column(name = "region", nullable = false, length = 100)
    private String region;
    
    /**
     * 星级筛选列表
     * 
     * <p>需要采集的酒店星级列表，以JSON数组格式存储。
     * 为空表示不限制星级。</p>
     * 
     * <p>示例：[3, 4, 5] 表示只采集3-5星级酒店</p>
     */
    @Column(name = "star_ratings", columnDefinition = "JSON")
    private String starRatings;
    
    /**
     * 品牌筛选列表
     * 
     * <p>需要采集的酒店品牌列表，以JSON数组格式存储。
     * 为空表示不限制品牌。</p>
     * 
     * <p>示例：["万豪", "希尔顿", "如家"]</p>
     */
    @Column(name = "brands", columnDefinition = "JSON")
    private String brands;
    
    /**
     * 价格区间范围
     * 
     * <p>采集的价格区间筛选条件。
     * 用于限定采集的价格范围。</p>
     * 
     * <p>示例：100-500、500-1000等</p>
     */
    @Column(name = "price_range", length = 50)
    private String priceRange;
    
    /**
     * 定时采集时间
     * 
     * <p>每日自动执行价格采集的时间。
     * 默认为凌晨2点。</p>
     */
    @Column(name = "schedule_time")
    private LocalTime scheduleTime = LocalTime.of(2, 0, 0);
    
    /**
     * 是否激活
     * 
     * <p>标识该配置是否为当前激活的配置。
     * 系统同一时间只会使用一个激活的配置。</p>
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
     * <p>配置创建的时间戳。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
