package com.knowprice.collector;

import com.knowprice.entity.PriceRecord;
import java.util.List;

/**
 * 价格采集器接口
 * 
 * <p>定义了OTA平台价格采集器的标准接口。所有平台采集器（携程、同程、美团等）
 * 都需要实现此接口，以便于统一管理和调用。</p>
 * 
 * <p>实现类：</p>
 * <ul>
 *   <li>{@link CtripCollector} - 携程旅行网采集器</li>
 *   <li>{@link LyCollector} - 同程旅行采集器</li>
 *   <li>{@link MeituanCollector} - 美团酒店采集器</li>
 * </ul>
 * 
 * <p>使用方式：</p>
 * <p>Spring会自动注入所有实现了此接口的Bean，CollectionService通过
 * getPlatformName()方法匹配对应的采集器执行采集任务。</p>
 * 
 * <p>采集原理：</p>
 * <p>各实现类通常使用Playwright无头浏览器模拟用户访问OTA平台，
 * 解析页面DOM结构获取酒店价格信息。</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see CollectionService
 * @see PriceRecord
 */
public interface PriceCollector {
    
    /**
     * 获取采集器对应的平台名称
     * 
     * <p>返回平台的唯一标识符，用于在采集服务中匹配对应的采集器。</p>
     * 
     * <p>标准平台名称：</p>
     * <ul>
     *   <li>ctrip - 携程旅行网</li>
     *   <li>ly - 同程旅行</li>
     *   <li>meituan - 美团酒店</li>
     * </ul>
     * 
     * @return 平台名称标识符
     */
    String getPlatformName();
    
    /**
     * 执行价格采集
     * 
     * <p>根据指定的区域和星级条件，从OTA平台采集酒店价格数据。</p>
     * 
     * <p>采集流程：</p>
     * <ol>
     *   <li>构建搜索URL</li>
     *   <li>使用Playwright访问页面</li>
     *   <li>解析酒店列表</li>
     *   <li>进入酒店详情页获取房型价格</li>
     *   <li>封装为PriceRecord对象返回</li>
     * </ol>
     * 
     * @param region 目标采集区域/城市（如：杭州、上海）
     * @param starRatings 星级筛选条件（JSON格式，如：[3,4,5]）
     * @return 采集到的价格记录列表
     * @throws Exception 采集过程中发生的异常
     */
    List<PriceRecord> collect(String region, String starRatings) throws Exception;
}
