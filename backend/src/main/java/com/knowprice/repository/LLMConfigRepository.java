package com.knowprice.repository;

import com.knowprice.entity.LLMConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * LLM配置数据访问接口
 * 
 * <p>提供LLM配置实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了LLM配置相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>获取系统中唯一的LLM配置</li>
 * </ul>
 * 
 * <p>说明：</p>
 * <p>系统只维护一份LLM配置，通过findFirstByOrderByIdAsc方法获取。</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see LLMConfig
 * @see JpaRepository
 */
public interface LLMConfigRepository extends JpaRepository<LLMConfig, Long> {
    
    /**
     * 获取第一个LLM配置
     * 
     * <p>按ID升序获取第一个LLM配置记录。系统只维护一份LLM配置，
     * 此方法用于获取当前生效的配置。</p>
     * 
     * @return LLM配置对象，如果不存在则返回null
     */
    LLMConfig findFirstByOrderByIdAsc();
}
