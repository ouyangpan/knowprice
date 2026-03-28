package com.knowprice.repository;

import com.knowprice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * 酒店数据访问接口
 * 
 * <p>提供酒店实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了酒店相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>按区域查询酒店</li>
 *   <li>按星级查询酒店</li>
 *   <li>查询自有酒店</li>
 *   <li>组合条件查询</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see Hotel
 * @see JpaRepository
 */
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    /**
     * 按区域查询酒店
     * 
     * @param region 区域名称
     * @return 该区域的酒店列表
     */
    List<Hotel> findByRegion(String region);
    
    /**
     * 按星级查询酒店
     * 
     * @param starRating 星级（1-5）
     * @return 该星级的酒店列表
     */
    List<Hotel> findByStarRating(Integer starRating);
    
    /**
     * 按区域和星级组合查询酒店
     * 
     * @param region 区域名称
     * @param starRating 星级
     * @return 符合条件的酒店列表
     */
    List<Hotel> findByRegionAndStarRating(String region, Integer starRating);
    
    /**
     * 查询所有自有酒店
     * 
     * <p>自有酒店是用户经营的酒店，作为价格对比的基准。</p>
     * 
     * @return 自有酒店列表
     */
    List<Hotel> findByIsOwnTrue();
    
    /**
     * 按区域模糊匹配和星级列表查询酒店
     * 
     * <p>使用JPQL查询，支持区域名称模糊匹配和多个星级筛选。</p>
     * 
     * @param region 区域名称（模糊匹配）
     * @param starRatings 星级列表
     * @return 符合条件的酒店列表
     */
    @Query("SELECT h FROM Hotel h WHERE h.region LIKE %:region% AND h.starRating IN :starRatings")
    List<Hotel> findByRegionContainingAndStarRatings(@Param("region") String region, @Param("starRatings") List<Integer> starRatings);
}
