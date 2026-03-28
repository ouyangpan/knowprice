package com.knowprice.repository;

import com.knowprice.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 报告数据访问接口
 * 
 * <p>提供报告实体的数据库访问操作，继承自Spring Data JPA的JpaRepository。
 * 定义了报告相关的自定义查询方法。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>按日期查询报告</li>
 *   <li>按日期范围查询报告</li>
 *   <li>获取所有报告（按日期倒序）</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see Report
 * @see JpaRepository
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    /**
     * 按日期查询报告
     * 
     * <p>每个日期最多只有一份报告。</p>
     * 
     * @param reportDate 报告日期
     * @return 该日期的报告，如果不存在则返回null
     */
    Report findByReportDate(LocalDate reportDate);
    
    /**
     * 按日期范围查询报告
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日期范围内的报告列表
     */
    List<Report> findByReportDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取所有报告（按日期倒序）
     * 
     * <p>使用JPQL查询，返回所有报告并按报告日期降序排列，
     * 最新的报告排在最前面。</p>
     * 
     * @return 所有报告列表（按日期降序）
     */
    @Query("SELECT r FROM Report r ORDER BY r.reportDate DESC")
    List<Report> findAllOrderByReportDateDesc();
}
