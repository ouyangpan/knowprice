package com.knowprice.repository;

import com.knowprice.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 房型数据访问接口
 * 
 * <p>提供房型实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了房型相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>按酒店ID查询房型列表</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see RoomType
 * @see JpaRepository
 */
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    
    /**
     * 按酒店ID查询房型列表
     * 
     * <p>查询指定酒店的所有房型配置。</p>
     * 
     * @param hotelId 酒店ID
     * @return 该酒店的房型列表
     */
    List<RoomType> findByHotel_Id(Long hotelId);
}
