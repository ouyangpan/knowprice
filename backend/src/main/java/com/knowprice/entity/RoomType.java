package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 房型实体类
 * 
 * <p>表示酒店的房型信息，关联到具体的酒店。
 * 用于价格采集时的房型匹配和基础价格参考。</p>
 * 
 * <p>数据库表：room_types</p>
 * 
 * <p>关联关系：</p>
 * <ul>
 *   <li>多对一关联Hotel：每个房型属于一个酒店</li>
 *   <li>一对多关联PriceRecord：一个房型可有多条价格记录</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>价格采集时按房型匹配OTA平台数据</li>
 *   <li>基础价格作为定价参考</li>
 *   <li>房型设施信息展示</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see Hotel
 * @see PriceRecord
 */
@Data
@Entity
@Table(name = "room_types")
public class RoomType {
    
    /**
     * 房型唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 所属酒店
     * 
     * <p>该房型所属的酒店，使用懒加载方式关联。
     * 每个房型必须关联到一个已存在的酒店。</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    /**
     * 房型名称
     * 
     * <p>房型的名称描述，如：大床房、双床房、豪华套房等。
     * 建议使用OTA平台上的标准名称以便于价格匹配。</p>
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    /**
     * 基础价格
     * 
     * <p>该房型的基准价格，作为定价参考。
     * 通常取酒店官方定价或历史平均价格。</p>
     */
    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private Double basePrice;
    
    /**
     * 房型设施
     * 
     * <p>房型包含的设施信息，以文本形式存储。
     * 例如：WiFi、早餐、空调、电视、独立卫浴等。</p>
     */
    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities;
    
    /**
     * 创建时间
     * 
     * <p>记录创建的时间戳，创建后不可更新。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
