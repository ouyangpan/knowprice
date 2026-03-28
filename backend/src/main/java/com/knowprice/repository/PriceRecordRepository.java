package com.knowprice.repository;

import com.knowprice.entity.PriceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 价格记录数据访问接口
 * 
 * <p>提供价格记录实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了价格记录相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>按酒店、平台、日期等条件查询价格记录</li>
 *   <li>按日期范围查询价格记录</li>
 *   <li>组合条件查询</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see PriceRecord
 * @see JpaRepository
 */
public interface PriceRecordRepository extends JpaRepository<PriceRecord, Long> {
    
    /**
     * 按酒店ID查询价格记录
     * 
     * @param hotelId 酒店ID
     * @return 该酒店的价格记录列表
     */
    List<PriceRecord> findByHotel_Id(Long hotelId);
    
    /**
     * 按平台查询价格记录
     * 
     * @param platform 平台名称（ctrip/ly/meituan）
     * @return 该平台的价格记录列表
     */
    List<PriceRecord> findByPlatform(String platform);
    
    /**
     * 按日期查询价格记录
     * 
     * @param recordDate 采集日期
     * @return 该日期的价格记录列表
     */
    List<PriceRecord> findByRecordDate(LocalDate recordDate);
    
    /**
     * 按酒店和日期查询价格记录
     * 
     * @param hotelId 酒店ID
     * @param recordDate 采集日期
     * @return 符合条件的价格记录列表
     */
    List<PriceRecord> findByHotel_IdAndRecordDate(Long hotelId, LocalDate recordDate);
    
    /**
     * 按酒店和平台查询价格记录
     * 
     * @param hotelId 酒店ID
     * @param platform 平台名称
     * @return 符合条件的价格记录列表
     */
    List<PriceRecord> findByHotel_IdAndPlatform(Long hotelId, String platform);
    
    /**
     * 按酒店和日期范围查询价格记录
     * 
     * <p>使用JPQL查询，获取指定酒店在日期范围内的所有价格记录，
     * 用于价格趋势分析。</p>
     * 
     * @param hotelId 酒店ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 符合条件的价格记录列表
     */
    @Query("SELECT pr FROM PriceRecord pr WHERE pr.hotel.id = :hotelId AND pr.recordDate BETWEEN :startDate AND :endDate")
    List<PriceRecord> findByHotelIdAndRecordDateBetween(@Param("hotelId") Long hotelId, 
                                                     @Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 按平台和日期查询价格记录
     * 
     * <p>使用JPQL查询，获取指定平台在指定日期的价格记录。</p>
     * 
     * @param platform 平台名称
     * @param recordDate 采集日期
     * @return 符合条件的价格记录列表
     */
    @Query("SELECT pr FROM PriceRecord pr WHERE pr.platform = :platform AND pr.recordDate = :recordDate")
    List<PriceRecord> findByPlatformAndRecordDate(@Param("platform") String platform, 
                                                @Param("recordDate") LocalDate recordDate);
}
