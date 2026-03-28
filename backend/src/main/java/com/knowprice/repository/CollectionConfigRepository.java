package com.knowprice.repository;

import com.knowprice.entity.CollectionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 采集配置数据访问接口
 * 
 * <p>提供采集配置实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了采集配置相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>查询激活的采集配置</li>
 *   <li>按区域查询配置</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see CollectionConfig
 * @see JpaRepository
 */
public interface CollectionConfigRepository extends JpaRepository<CollectionConfig, Long> {
    
    /**
     * 查询所有激活的采集配置
     * 
     * <p>返回系统中所有标记为激活状态的采集配置。
     * 系统同一时间通常只使用一个激活配置。</p>
     * 
     * @return 激活的采集配置列表
     */
    List<CollectionConfig> findByIsActiveTrue();
    
    /**
     * 按区域查询采集配置
     * 
     * @param region 区域名称
     * @return 该区域的采集配置列表
     */
    List<CollectionConfig> findByRegion(String region);
}
