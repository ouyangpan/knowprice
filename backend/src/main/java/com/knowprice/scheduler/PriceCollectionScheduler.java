package com.knowprice.scheduler;

import com.knowprice.service.CollectionService;
import com.knowprice.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 价格采集定时任务调度器
 * 
 * <p>负责定时执行价格采集和报告生成任务的调度器组件。
 * 使用Spring Scheduling实现定时任务调度。</p>
 * 
 * <p>定时任务：</p>
 * <ul>
 *   <li>每日凌晨2点：执行全平台价格采集</li>
 *   <li>每日凌晨3点：生成每日价格分析报告</li>
 * </ul>
 * 
 * <p>任务执行流程：</p>
 * <ol>
 *   <li>凌晨2点触发价格采集任务</li>
 *   <li>采集携程、同程、美团三个平台的酒店价格</li>
 *   <li>凌晨3点触发报告生成任务</li>
 *   <li>生成价格汇总、趋势分析、竞品对比和智能建议</li>
 * </ol>
 * 
 * <p>配置说明：</p>
 * <p>定时任务使用Cron表达式配置，格式：秒 分 时 日 月 周</p>
 * <ul>
 *   <li>"0 0 2 * * ?" - 每天凌晨2点执行</li>
 *   <li>"0 0 3 * * ?" - 每天凌晨3点执行</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see CollectionService
 * @see ReportService
 */
@Component
public class PriceCollectionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PriceCollectionScheduler.class);

    /**
     * 价格采集服务
     * 
     * <p>用于执行多平台价格采集任务。</p>
     */
    @Autowired
    private CollectionService collectionService;

    /**
     * 报告生成服务
     * 
     * <p>用于生成每日价格分析报告。</p>
     */
    @Autowired
    private ReportService reportService;

    /**
     * 每日定时采集任务
     * 
     * <p>每天凌晨2点自动执行价格采集任务。依次采集携程、同程、美团
     * 三个平台的酒店价格数据，然后生成每日报告。</p>
     * 
     * <p>执行内容：</p>
     * <ol>
     *   <li>调用CollectionService.collectPrices()执行全平台采集</li>
     *   <li>调用ReportService.generateDailyReport()生成报告</li>
     * </ol>
     * 
     * <p>Cron表达式：0 0 2 * * ?</p>
     * <ul>
     *   <li>秒：0</li>
     *   <li>分：0</li>
     *   <li>时：2（凌晨2点）</li>
     *   <li>日：*（每天）</li>
     *   <li>月：*（每月）</li>
     *   <li>周：?（不指定）</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyCollectionTask() {
        logger.info("========== 每日定时采集任务开始 ==========");
        
        try {
            logger.info("开始执行价格采集...");
            collectionService.collectPrices();
            logger.info("价格采集完成");
        } catch (Exception e) {
            logger.error("价格采集失败: {}", e.getMessage(), e);
        }
        
        try {
            logger.info("开始生成每日报告...");
            reportService.generateDailyReport();
            logger.info("每日报告生成完成");
        } catch (Exception e) {
            logger.error("报告生成失败: {}", e.getMessage(), e);
        }
        
        logger.info("========== 每日定时采集任务完成 ==========");
    }

    /**
     * 每日报告生成任务
     * 
     * <p>每天凌晨3点自动执行报告生成任务。作为采集任务的补充，
     * 确保即使采集任务中的报告生成失败，也能独立生成报告。</p>
     * 
     * <p>执行内容：</p>
     * <ul>
     *   <li>调用ReportService.generateDailyReport()生成报告</li>
     *   <li>如果当日报告已存在，则直接返回已有报告</li>
     * </ul>
     * 
     * <p>Cron表达式：0 0 3 * * ?</p>
     * <ul>
     *   <li>秒：0</li>
     *   <li>分：0</li>
     *   <li>时：3（凌晨3点）</li>
     *   <li>日：*（每天）</li>
     *   <li>月：*（每月）</li>
     *   <li>周：?（不指定）</li>
     * </ul>
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void dailyReportTask() {
        logger.info("========== 每日报告生成任务开始 ==========");
        
        try {
            reportService.generateDailyReport();
            logger.info("每日报告生成完成");
        } catch (Exception e) {
            logger.error("报告生成失败: {}", e.getMessage(), e);
        }
        
        logger.info("========== 每日报告生成任务完成 ==========");
    }
}
