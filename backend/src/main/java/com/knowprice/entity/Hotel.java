package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 酒店实体类
 * 
 * <p>表示系统中的酒店信息，包括自有酒店和竞品酒店。
 * 是价格采集和对比分析的核心数据实体。</p>
 * 
 * <p>酒店类型：</p>
 * <ul>
 *   <li>自有酒店（isOwn=true）：用户经营的酒店，作为价格对比基准</li>
 *   <li>竞品酒店（isOwn=false）：竞争对手酒店，用于市场价格监测</li>
 * </ul>
 * 
 * <p>数据库表：hotels</p>
 * 
 * <p>关联关系：</p>
 * <ul>
 *   <li>一对多关联RoomType：一个酒店可有多个房型</li>
 *   <li>一对多关联PriceRecord：一个酒店可有多条价格记录</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see RoomType
 * @see PriceRecord
 */
@Data
@Entity
@Table(name = "hotels")
public class Hotel {
    
    /**
     * 酒店唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 酒店名称
     * 
     * <p>酒店的完整名称，用于显示和搜索。
     * 建议使用OTA平台上的标准名称以便于价格匹配。</p>
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    /**
     * 酒店地址
     * 
     * <p>酒店的详细地址，包括省市区街道信息。</p>
     */
    @Column(name = "address", nullable = false, length = 500)
    private String address;
    
    /**
     * 所属区域
     * 
     * <p>酒店所在的城市或区域名称，用于按区域筛选和采集。
     * 例如：杭州、上海、北京等。</p>
     */
    @Column(name = "region", nullable = false, length = 100)
    private String region;
    
    /**
     * 酒店星级
     * 
     * <p>酒店的星级评定，取值范围1-5。
     * 用于同级别酒店的价格对比分析。</p>
     */
    @Column(name = "star_rating", nullable = false)
    private Integer starRating;
    
    /**
     * 酒店品牌
     * 
     * <p>酒店所属品牌名称，如：万豪、希尔顿、如家等。
     * 可为空，表示独立酒店或无品牌。</p>
     */
    @Column(name = "brand", length = 100)
    private String brand;
    
    /**
     * 是否为自有酒店
     * 
     * <p>标识该酒店是否为用户自己经营的酒店。
     * 自有酒店作为价格对比的基准，系统会重点分析自有酒店与竞品的价格差异。</p>
     * 
     * <p>取值：</p>
     * <ul>
     *   <li>true - 自有酒店</li>
     *   <li>false - 竞品酒店（默认）</li>
     * </ul>
     */
    @Column(name = "is_own", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isOwn = false;
    
    /**
     * 创建时间
     * 
     * <p>记录创建的时间戳，创建后不可更新。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    /**
     * 更新时间
     * 
     * <p>记录最后更新的时间戳，每次更新时自动刷新。</p>
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    /**
     * 更新前回调
     * 
     * <p>JPA生命周期回调，在实体更新前自动更新updatedAt字段。</p>
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
