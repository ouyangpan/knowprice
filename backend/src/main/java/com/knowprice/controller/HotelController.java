package com.knowprice.controller;

import com.knowprice.entity.Hotel;
import com.knowprice.entity.RoomType;
import com.knowprice.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 酒店管理控制器
 * 
 * <p>提供酒店和房型信息的CRUD操作RESTful API接口。
 * 支持管理自有酒店和竞品酒店信息，为价格采集和对比分析提供基础数据。</p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>酒店信息的增删改查</li>
 *   <li>房型信息的增删改查</li>
 *   <li>获取自有酒店列表（用于价格对比基准）</li>
 *   <li>批量创建酒店及房型</li>
 * </ul>
 * 
 * <p>API基础路径: /hotels</p>
 * 
 * <p>酒店类型说明：</p>
 * <ul>
 *   <li>自有酒店（isOwn=true）：用户自己的酒店，作为价格对比的基准</li>
 *   <li>竞品酒店（isOwn=false）：竞争对手酒店，用于市场价格监测</li>
 * </ul>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/hotels")
@CrossOrigin(origins = "*")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    /**
     * 获取所有酒店列表
     * 
     * <p>返回系统中所有酒店信息，包括自有酒店和竞品酒店。</p>
     * 
     * @return 包含所有酒店列表的响应对象
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHotels() {
        List<Hotel> hotels = hotelService.getAllHotels();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", hotels);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取单个酒店详情
     * 
     * @param id 酒店ID
     * @return 包含酒店详情的响应对象，如果不存在则返回404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getHotelById(@PathVariable Long id) {
        Optional<Hotel> hotel = hotelService.getHotelById(id);
        Map<String, Object> response = new HashMap<>();
        if (hotel.isPresent()) {
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", hotel.get());
        } else {
            response.put("code", 404);
            response.put("message", "酒店不存在");
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取自有酒店列表
     * 
     * <p>返回所有标记为"自有"的酒店，这些酒店作为价格对比的基准。
     * 系统会重点监控自有酒店与竞品酒店的价格差异。</p>
     * 
     * @return 包含自有酒店列表的响应对象
     */
    @GetMapping("/own")
    public ResponseEntity<Map<String, Object>> getOwnHotels() {
        List<Hotel> hotels = hotelService.getOwnHotels();
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", hotels);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建新酒店
     * 
     * <p>添加新的酒店信息到系统。新建酒店时需指定是否为自有酒店。</p>
     * 
     * @param hotel 酒店信息（JSON格式）
     * @return 包含新创建酒店信息的响应对象
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createHotel(@RequestBody Hotel hotel) {
        Hotel savedHotel = hotelService.saveHotel(hotel);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "创建成功");
        response.put("data", savedHotel);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新酒店信息
     * 
     * @param id 要更新的酒店ID
     * @param hotelDetails 更新后的酒店信息
     * @return 包含更新后酒店信息的响应对象
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateHotel(@PathVariable Long id, @RequestBody Hotel hotelDetails) {
        try {
            Hotel updatedHotel = hotelService.updateHotel(id, hotelDetails);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", updatedHotel);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 删除酒店
     * 
     * <p>删除指定酒店及其关联的房型信息。注意：删除酒店会级联删除相关价格记录。</p>
     * 
     * @param id 要删除的酒店ID
     * @return 操作结果响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteHotel(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            hotelService.deleteHotel(id);
            response.put("code", 200);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 获取酒店的所有房型
     * 
     * <p>返回指定酒店的所有房型配置，包括房型名称、基础价格、设施等信息。</p>
     * 
     * @param hotelId 酒店ID
     * @return 包含房型列表的响应对象
     */
    @GetMapping("/{hotelId}/room-types")
    public ResponseEntity<Map<String, Object>> getRoomTypesByHotelId(@PathVariable Long hotelId) {
        List<RoomType> roomTypes = hotelService.getRoomTypesByHotelId(hotelId);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "获取成功");
        response.put("data", roomTypes);
        return ResponseEntity.ok(response);
    }

    /**
     * 创建房型
     * 
     * <p>为酒店添加新的房型配置。房型信息用于价格采集时的精确匹配。</p>
     * 
     * @param roomType 房型信息（需包含关联的酒店ID）
     * @return 包含新创建房型信息的响应对象
     */
    @PostMapping("/room-types")
    public ResponseEntity<Map<String, Object>> createRoomType(@RequestBody RoomType roomType) {
        RoomType savedRoomType = hotelService.saveRoomType(roomType);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "创建成功");
        response.put("data", savedRoomType);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新房型信息
     * 
     * @param id 要更新的房型ID
     * @param roomTypeDetails 更新后的房型信息
     * @return 包含更新后房型信息的响应对象
     */
    @PutMapping("/room-types/{id}")
    public ResponseEntity<Map<String, Object>> updateRoomType(@PathVariable Long id, @RequestBody RoomType roomTypeDetails) {
        try {
            RoomType updatedRoomType = hotelService.updateRoomType(id, roomTypeDetails);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "更新成功");
            response.put("data", updatedRoomType);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 删除房型
     * 
     * @param id 要删除的房型ID
     * @return 操作结果响应
     */
    @DeleteMapping("/room-types/{id}")
    public ResponseEntity<Map<String, Object>> deleteRoomType(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            hotelService.deleteRoomType(id);
            response.put("code", 200);
            response.put("message", "删除成功");
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "删除失败: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 批量创建酒店及房型
     * 
     * <p>一次性创建酒店及其所有房型，适用于新增酒店时同时配置多个房型的场景。</p>
     * 
     * <p>请求体格式：</p>
     * <pre>
     * {
     *   "hotel": {酒店对象},
     *   "roomTypes": [房型对象数组]
     * }
     * </pre>
     * 
     * @param request 包含酒店和房型信息的请求体
     * @return 包含新创建酒店信息的响应对象
     */
    @PostMapping("/with-room-types")
    public ResponseEntity<Map<String, Object>> createHotelWithRoomTypes(@RequestBody Map<String, Object> request) {
        try {
            Hotel hotel = (Hotel) request.get("hotel");
            List<RoomType> roomTypes = (List<RoomType>) request.get("roomTypes");
            
            Hotel savedHotel = hotelService.createHotelWithRoomTypes(hotel, roomTypes);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "创建成功");
            response.put("data", savedHotel);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
}
