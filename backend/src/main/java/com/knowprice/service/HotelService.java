package com.knowprice.service;

import com.knowprice.entity.Hotel;
import com.knowprice.entity.RoomType;
import com.knowprice.repository.HotelRepository;
import com.knowprice.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 酒店管理服务
 * 
 * <p>提供酒店和房型信息的业务逻辑处理，是Controller层与Repository层之间的桥梁。
 * 负责酒店数据的增删改查操作，以及房型管理的相关业务。</p>
 * 
 * <p>核心职责：</p>
 * <ul>
 *   <li>酒店信息的CRUD操作</li>
 *   <li>房型信息的CRUD操作</li>
 *   <li>按条件查询酒店（区域、星级、是否自有）</li>
 *   <li>批量创建酒店及房型</li>
 * </ul>
 * 
 * <p>事务管理：</p>
 * <p>所有写操作（增、删、改）都使用@Transactional注解确保数据一致性。</p>
 * 
 * @author KnowPrice Team
 * @version 1.0
 * @since 2024-01-01
 * @see HotelRepository
 * @see RoomTypeRepository
 */
@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    /**
     * 获取所有酒店列表
     * 
     * <p>从数据库查询所有酒店记录，包括自有酒店和竞品酒店。</p>
     * 
     * @return 酒店列表，如果没有数据则返回空列表
     */
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    /**
     * 根据ID获取酒店
     * 
     * <p>查询指定ID的酒店信息，返回Optional以处理空值情况。</p>
     * 
     * @param id 酒店ID
     * @return Optional包装的酒店对象，如果不存在则为空
     */
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    /**
     * 获取自有酒店列表
     * 
     * <p>查询所有标记为"自有"的酒店。自有酒店是用户经营的酒店，
     * 作为价格对比的基准，系统会重点分析自有酒店与竞品的价格差异。</p>
     * 
     * @return 自有酒店列表
     */
    public List<Hotel> getOwnHotels() {
        return hotelRepository.findByIsOwnTrue();
    }

    /**
     * 按区域查询酒店
     * 
     * <p>查询指定区域/城市的所有酒店，用于区域性的价格分析。</p>
     * 
     * @param region 区域名称（如：杭州、上海）
     * @return 该区域的酒店列表
     */
    public List<Hotel> getHotelsByRegion(String region) {
        return hotelRepository.findByRegion(region);
    }

    /**
     * 按星级查询酒店
     * 
     * <p>查询指定星级的所有酒店，用于同级别酒店的价格对比分析。</p>
     * 
     * @param starRating 星级（1-5）
     * @return 该星级的酒店列表
     */
    public List<Hotel> getHotelsByStarRating(Integer starRating) {
        return hotelRepository.findByStarRating(starRating);
    }

    /**
     * 保存酒店信息
     * 
     * <p>新增或更新酒店信息。如果酒店对象包含ID则更新，否则新增。</p>
     * 
     * @param hotel 酒店信息对象
     * @return 保存后的酒店对象（包含生成的ID）
     */
    @Transactional
    public Hotel saveHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    /**
     * 删除酒店
     * 
     * <p>根据ID删除酒店记录。注意：由于数据库外键约束，
     * 删除酒店可能会级联删除相关的房型和价格记录。</p>
     * 
     * @param id 要删除的酒店ID
     */
    @Transactional
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }

    /**
     * 获取酒店的所有房型
     * 
     * <p>查询指定酒店的所有房型配置，包括房型名称、基础价格、设施等信息。</p>
     * 
     * @param hotelId 酒店ID
     * @return 该酒店的房型列表
     */
    public List<RoomType> getRoomTypesByHotelId(Long hotelId) {
        return roomTypeRepository.findByHotel_Id(hotelId);
    }

    /**
     * 保存房型信息
     * 
     * <p>新增或更新房型信息。房型需要关联到具体的酒店。</p>
     * 
     * @param roomType 房型信息对象
     * @return 保存后的房型对象
     */
    @Transactional
    public RoomType saveRoomType(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    /**
     * 删除房型
     * 
     * @param id 要删除的房型ID
     */
    @Transactional
    public void deleteRoomType(Long id) {
        roomTypeRepository.deleteById(id);
    }

    /**
     * 批量创建酒店及房型
     * 
     * <p>在一个事务中创建酒店及其所有房型，确保数据一致性。
     * 适用于新增酒店时同时配置多个房型的场景。</p>
     * 
     * <p>处理流程：</p>
     * <ol>
     *   <li>保存酒店信息，获取生成的酒店ID</li>
     *   <li>遍历房型列表，设置酒店关联</li>
     *   <li>逐个保存房型信息</li>
     * </ol>
     * 
     * @param hotel 酒店信息
     * @param roomTypes 房型列表
     * @return 保存后的酒店对象
     */
    @Transactional
    public Hotel createHotelWithRoomTypes(Hotel hotel, List<RoomType> roomTypes) {
        Hotel savedHotel = hotelRepository.save(hotel);
        for (RoomType roomType : roomTypes) {
            roomType.setHotel(savedHotel);
            roomTypeRepository.save(roomType);
        }
        return savedHotel;
    }

    /**
     * 更新酒店信息
     * 
     * <p>更新指定ID酒店的基本信息，包括名称、地址、区域、星级、品牌、是否自有等属性。</p>
     * 
     * @param id 要更新的酒店ID
     * @param hotelDetails 更新后的酒店信息
     * @return 更新后的酒店对象
     * @throws RuntimeException 如果酒店不存在
     */
    @Transactional
    public Hotel updateHotel(Long id, Hotel hotelDetails) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("酒店不存在"));
        
        hotel.setName(hotelDetails.getName());
        hotel.setAddress(hotelDetails.getAddress());
        hotel.setRegion(hotelDetails.getRegion());
        hotel.setStarRating(hotelDetails.getStarRating());
        hotel.setBrand(hotelDetails.getBrand());
        hotel.setIsOwn(hotelDetails.getIsOwn());
        
        return hotelRepository.save(hotel);
    }

    /**
     * 更新房型信息
     * 
     * <p>更新指定ID房型的基本信息，包括房型名称、基础价格、设施配置等。</p>
     * 
     * @param id 要更新的房型ID
     * @param roomTypeDetails 更新后的房型信息
     * @return 更新后的房型对象
     * @throws RuntimeException 如果房型不存在
     */
    @Transactional
    public RoomType updateRoomType(Long id, RoomType roomTypeDetails) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("房型不存在"));
        
        roomType.setName(roomTypeDetails.getName());
        roomType.setBasePrice(roomTypeDetails.getBasePrice());
        roomType.setAmenities(roomTypeDetails.getAmenities());
        
        return roomTypeRepository.save(roomType);
    }
}
