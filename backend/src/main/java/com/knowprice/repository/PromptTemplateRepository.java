package com.knowprice.repository;

import com.knowprice.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 提示词模板数据访问接口
 * 
 * <p>提供提示词模板实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了提示词模板相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>查询激活的提示词模板</li>
 *   <li>按名称查询模板</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see PromptTemplate
 * @see JpaRepository
 */
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    
    /**
     * 查询所有激活的提示词模板
     * 
     * <p>返回系统中所有标记为激活状态的提示词模板。</p>
     * 
     * @return 激活的提示词模板列表
     */
    List<PromptTemplate> findByIsActiveTrue();
    
    /**
     * 按名称查询提示词模板
     * 
     * @param name 模板名称
     * @return 匹配的提示词模板，如果不存在则返回null
     */
    PromptTemplate findByName(String name);
}
