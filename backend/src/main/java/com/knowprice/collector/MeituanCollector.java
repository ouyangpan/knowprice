package com.knowprice.collector;

import com.knowprice.entity.Hotel;
import com.knowprice.entity.PriceRecord;
import com.knowprice.entity.RoomType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 美团酒店价格采集器
 * 
 * <p>实现从美团酒店（https://hotel.meituan.com）采集酒店价格数据的功能。
 * 使用Playwright无头浏览器模拟用户访问，解析页面DOM获取数据。</p>
 * 
 * <p>采集流程：</p>
 * <ol>
 *   <li>根据城市名称构建搜索URL</li>
 *   <li>访问酒店搜索列表页</li>
 *   <li>解析酒店列表获取基本信息</li>
 *   <li>进入酒店详情页获取房型价格</li>
 *   <li>封装为PriceRecord对象返回</li>
 * </ol>
 * 
 * <p>支持的城市：</p>
 * <ul>
 *   <li>杭州、上海、北京、广州、深圳</li>
 *   <li>成都、重庆、西安、南京、苏州</li>
 * </ul>
 * 
 * <p>配置项：</p>
 * <ul>
 *   <li>playwright.headless - 是否使用无头模式（默认true）</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see PriceCollector
 * @see CollectionService
 */
@Component
public class MeituanCollector implements PriceCollector {

    private static final Logger logger = LoggerFactory.getLogger(MeituanCollector.class);
    
    /**
     * 平台标识符
     */
    private static final String PLATFORM_NAME = "meituan";
    
    /**
     * 美团酒店首页URL
     */
    private static final String BASE_URL = "https://hotel.meituan.com";

    /**
     * 是否使用无头模式运行浏览器
     */
    @Value("${playwright.headless:true}")
    private boolean headless;

    /**
     * 获取平台名称
     * 
     * @return "meituan"
     */
    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    /**
     * 执行美团平台价格采集
     * 
     * <p>使用Playwright Chromium浏览器访问美团酒店页面，
     * 解析页面数据获取酒店和房型价格信息。</p>
     * 
     * @param region 目标采集城市
     * @param starRatings 星级筛选条件
     * @return 采集到的价格记录列表
     * @throws Exception 采集失败时抛出异常
     */
    @Override
    public List<PriceRecord> collect(String region, String starRatings) throws Exception {
        logger.info("开始采集美团平台数据，地区: {}, 星级: {}", region, starRatings);
        
        List<PriceRecord> records = new ArrayList<>();
        
        try {
            com.microsoft.playwright.Playwright playwright = com.microsoft.playwright.Playwright.create();
            
            com.microsoft.playwright.options.BrowserType.LaunchOptions launchOptions = 
                new com.microsoft.playwright.options.BrowserType.LaunchOptions()
                    .setHeadless(headless);
            
            try (com.microsoft.playwright.Browser browser = playwright.chromium().launch(launchOptions);
                 com.microsoft.playwright.BrowserContext context = browser.newContext()) {
                
                PageHelper pageHelper = new PageHelper(context.newPage());
                
                String searchUrl = buildSearchUrl(region, starRatings);
                logger.info("访问美团搜索页面: {}", searchUrl);
                
                pageHelper.navigate(searchUrl);
                pageHelper.waitForLoadState("networkidle");
                
                List<HotelData> hotelList = pageHelper.extractHotelList();
                
                for (HotelData hotelData : hotelList) {
                    Hotel hotel = createOrGetHotel(hotelData);
                    
                    List<RoomTypeData> roomList = pageHelper.extractRoomList(hotelData.getHotelUrl());
                    
                    for (RoomTypeData roomData : roomList) {
                        PriceRecord record = new PriceRecord();
                        record.setHotel(hotel);
                        
                        RoomType roomType = new RoomType();
                        roomType.setHotel(hotel);
                        roomType.setName(roomData.getName());
                        roomType.setBasePrice(roomData.getPrice());
                        record.setRoomType(roomType);
                        
                        record.setPlatform(PLATFORM_NAME);
                        record.setPrice(roomData.getPrice());
                        record.setAvailability(roomData.isAvailable());
                        
                        records.add(record);
                    }
                }
                
                logger.info("美团平台数据采集完成，共 {} 条记录", records.size());
            }
            
            playwright.close();
            
        } catch (Exception e) {
            logger.error("美团平台数据采集失败: {}", e.getMessage(), e);
            throw e;
        }
        
        return records;
    }

    /**
     * 构建美团酒店搜索URL
     * 
     * @param region 城市名称
     * @param starRatings 星级参数
     * @return 完整的搜索URL
     */
    private String buildSearchUrl(String region, String starRatings) {
        String cityCode = getCityCode(region);
        StringBuilder url = new StringBuilder(BASE_URL);
        url.append("/").append(cityCode).append("/");
        
        if (starRatings != null && !starRatings.isEmpty()) {
            url.append("?level=").append(starRatings);
        }
        
        return url.toString();
    }

    /**
     * 获取城市对应的美团城市编码
     * 
     * <p>美团使用城市拼音全拼作为城市编码。</p>
     * 
     * @param region 城市名称
     * @return 美团城市编码
     */
    private String getCityCode(String region) {
        if (region.contains("杭州") || region.contains("杭州市")) {
            return "hangzhou";
        } else if (region.contains("上海") || region.contains("上海市")) {
            return "shanghai";
        } else if (region.contains("北京") || region.contains("北京市")) {
            return "beijing";
        } else if (region.contains("广州") || region.contains("广州市")) {
            return "guangzhou";
        } else if (region.contains("深圳") || region.contains("深圳市")) {
            return "shenzhen";
        } else if (region.contains("成都") || region.contains("成都市")) {
            return "chengdu";
        } else if (region.contains("重庆") || region.contains("重庆市")) {
            return "chongqing";
        } else if (region.contains("西安") || region.contains("西安市")) {
            return "xian";
        } else if (region.contains("南京") || region.contains("南京市")) {
            return "nanjing";
        } else if (region.contains("苏州") || region.contains("苏州市")) {
            return "suzhou";
        }
        return "hangzhou";
    }

    /**
     * 创建酒店实体对象
     */
    private Hotel createOrGetHotel(HotelData hotelData) {
        Hotel hotel = new Hotel();
        hotel.setName(hotelData.getName());
        hotel.setAddress(hotelData.getAddress());
        hotel.setRegion(hotelData.getRegion());
        hotel.setStarRating(hotelData.getStarRating());
        hotel.setBrand(hotelData.getBrand());
        hotel.setIsOwn(false);
        return hotel;
    }

    /**
     * 酒店数据内部类
     */
    static class HotelData {
        private String name;
        private String address;
        private String region;
        private Integer starRating;
        private String brand;
        private String hotelUrl;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        public Integer getStarRating() { return starRating; }
        public void setStarRating(Integer starRating) { this.starRating = starRating; }
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
        public String getHotelUrl() { return hotelUrl; }
        public void setHotelUrl(String hotelUrl) { this.hotelUrl = hotelUrl; }
    }

    /**
     * 房型数据内部类
     */
    static class RoomTypeData {
        private String name;
        private Double price;
        private boolean available;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }

    /**
     * 页面操作辅助类
     */
    static class PageHelper {
        private final com.microsoft.playwright.Page page;

        public PageHelper(com.microsoft.playwright.Page page) {
            this.page = page;
        }

        public void navigate(String url) {
            page.navigate(url);
        }

        public void waitForLoadState(String state) {
            page.waitForLoadState(state);
        }

        /**
         * 从页面提取酒店列表
         */
        public List<HotelData> extractHotelList() {
            List<HotelData> hotelList = new ArrayList<>();
            
            try {
                page.waitForSelector(".hotel-item", new com.microsoft.playwright.options.PageWaitForSelectorOptions().setTimeout(10000));
                
                List<com.microsoft.playwright.Locator> items = page.locator(".hotel-item").all();
                
                for (com.microsoft.playwright.Locator item : items) {
                    try {
                        HotelData hotel = new HotelData();
                        
                        String name = item.locator(".hotel-name").textContent();
                        hotel.setName(name != null ? name.trim() : "");
                        
                        String address = item.locator(".hotel-address").textContent();
                        hotel.setAddress(address != null ? address.trim() : "");
                        
                        String brand = item.locator(".brand-logo").textContent();
                        hotel.setBrand(brand != null ? brand.trim() : "");
                        
                        String starRating = item.locator(".hotel-star").textContent();
                        if (starRating != null && starRating.contains("星")) {
                            hotel.setStarRating(Integer.parseInt(starRating.replaceAll("[^0-9]", "")));
                        }
                        
                        String detailUrl = item.locator("a").first().getAttribute("href");
                        hotel.setHotelUrl(detailUrl != null && !detailUrl.startsWith("http") ? BASE_URL + detailUrl : detailUrl);
                        
                        hotelList.add(hotel);
                    } catch (Exception e) {
                        logger.warn("解析美团酒店信息失败: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                logger.warn("未找到美团酒店列表或页面结构变化: {}", e.getMessage());
            }
            
            return hotelList;
        }

        /**
         * 从酒店详情页提取房型列表
         */
        public List<RoomTypeData> extractRoomList(String hotelUrl) {
            List<RoomTypeData> roomList = new ArrayList<>();
            
            try {
                if (hotelUrl != null && !hotelUrl.isEmpty()) {
                    page.navigate(hotelUrl);
                    page.waitForLoadState("networkidle");
                    
                    page.waitForSelector(".room-type-item", new com.microsoft.playwright.options.PageWaitForSelectorOptions().setTimeout(10000));
                    
                    List<com.microsoft.playwright.Locator> rooms = page.locator(".room-type-item").all();
                    
                    for (com.microsoft.playwright.Locator room : rooms) {
                        try {
                            RoomTypeData roomData = new RoomTypeData();
                            
                            String roomName = room.locator(".room-title").textContent();
                            roomData.setName(roomName != null ? roomName.trim() : "");
                            
                            String priceStr = room.locator(".price").textContent();
                            if (priceStr != null) {
                                String price = priceStr.replaceAll("[^0-9.]", "");
                                if (!price.isEmpty()) {
                                    roomData.setPrice(Double.parseDouble(price));
                                }
                            }
                            
                            String availability = room.locator(".status").textContent();
                            roomData.setAvailable(availability != null && !availability.contains("售罄") && !availability.contains("满"));
                            
                            roomList.add(roomData);
                        } catch (Exception e) {
                            logger.warn("解析美团房型信息失败: {}", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("获取美团房型列表失败: {}", e.getMessage());
            }
            
            return roomList;
        }
    }
}
