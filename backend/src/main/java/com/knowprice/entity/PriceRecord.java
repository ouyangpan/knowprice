package com.knowprice.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 价格记录实体类
 * 
 * <p>表示从OTA平台采集的酒店价格数据记录。
 * 每条记录包含酒店、房型、平台、价格、可用性等信息。</p>
 * 
 * <p>数据库表：price_records</p>
 * 
 * <p>关联关系：</p>
 * <ul>
 *   <li>多对一关联Hotel：每条价格记录对应一个酒店</li>
 *   <li>多对一关联RoomType：每条价格记录对应一个房型</li>
 * </ul>
 * 
 * <p>数据来源：</p>
 * <p>由各平台采集器（Collector）从OTA平台抓取并保存。</p>
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
 * @see Hotel
 * @see RoomType
 */
@Data
@Entity
@Table(name = "price_records")
public class PriceRecord {
    
    /**
     * 价格记录唯一标识ID
     * 
     * <p>自增主键，由数据库自动生成。</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 关联酒店
     * 
     * <p>该价格记录对应的酒店，使用懒加载方式关联。</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    /**
     * 关联房型
     * 
     * <p>该价格记录对应的房型，使用懒加载方式关联。</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;
    
    /**
     * 采集平台
     * 
     * <p>价格数据的来源平台标识。</p>
     * 
     * <p>可选值：</p>
     * <ul>
     *   <li>ctrip - 携程旅行网</li>
     *   <li>ly - 同程旅行</li>
     *   <li>meituan - 美团酒店</li>
     * </ul>
     */
    @Column(name = "platform", nullable = false, length = 50)
    private String platform;
    
    /**
     * 价格金额
     * 
     * <p>从OTA平台采集到的酒店房间价格，单位：人民币（元）。</p>
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private Double price;
    
    /**
     * 是否有房
     * 
     * <p>表示该房型在采集时是否有可预订的房间。</p>
     * 
     * <p>取值：</p>
     * <ul>
     *   <li>true - 有房可订（默认）</li>
     *   <li>false - 满房/不可订</li>
     * </ul>
     */
    @Column(name = "availability", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean availability = true;
    
    /**
     * 采集日期
     * 
     * <p>价格数据的采集日期，用于按日期查询和趋势分析。
     * 注意：这是价格对应的入住日期，而非采集时间。</p>
     */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;
    
    /**
     * 创建时间
     * 
     * <p>记录入库的时间戳，即实际采集时间。</p>
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
