<template>
  <div class="hotel-setting-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>酒店基本信息</span>
        </div>
      </template>
      <el-form :model="hotelForm" label-width="120px">
        <el-form-item label="酒店名称">
          <el-input v-model="hotelForm.name" placeholder="请输入酒店名称" />
        </el-form-item>
        <el-form-item label="酒店地址">
          <el-input v-model="hotelForm.address" placeholder="请输入酒店地址" />
        </el-form-item>
        <el-form-item label="所属地区">
          <el-input v-model="hotelForm.region" placeholder="请输入所属地区" />
        </el-form-item>
        <el-form-item label="酒店星级">
          <el-select v-model="hotelForm.starRating" placeholder="请选择星级">
            <el-option label="二星级" :value="2" />
            <el-option label="三星级" :value="3" />
            <el-option label="四星级" :value="4" />
            <el-option label="五星级" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="酒店品牌">
          <el-input v-model="hotelForm.brand" placeholder="请输入酒店品牌" />
        </el-form-item>
        <el-form-item label="是否自有酒店">
          <el-switch v-model="hotelForm.isOwn" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveHotelInfo" :loading="saving">保存酒店信息</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mt-20">
      <template #header>
        <div class="card-header">
          <span>房型管理</span>
          <el-button type="primary" size="small" @click="addRoomType">
            <el-icon><Plus /></el-icon>
            添加房型
          </el-button>
        </div>
      </template>
      <el-table :data="roomTypes" style="width: 100%" v-loading="loadingRoomTypes">
        <el-table-column prop="name" label="房型名称" width="180" />
        <el-table-column prop="basePrice" label="基准价格" width="120">
          <template #default="scope">
            {{ scope.row.basePrice }} 元
          </template>
        </el-table-column>
        <el-table-column prop="amenities" label="设施服务" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editRoomType(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deleteRoomType(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 房型编辑对话框 -->
    <el-dialog
      v-model="roomTypeDialogVisible"
      :title="isEditRoomType ? '编辑房型' : '添加房型'"
      width="500px"
    >
      <el-form :model="roomTypeForm" label-width="100px">
        <el-form-item label="房型名称">
          <el-input v-model="roomTypeForm.name" placeholder="请输入房型名称" />
        </el-form-item>
        <el-form-item label="基准价格">
          <el-input-number v-model="roomTypeForm.basePrice" :min="0" :step="10" placeholder="请输入基准价格" />
        </el-form-item>
        <el-form-item label="设施服务">
          <el-input v-model="roomTypeForm.amenities" type="textarea" placeholder="请输入设施服务，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="roomTypeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRoomType" :loading="savingRoomType">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../utils/api'

const saving = ref(false)
const loadingRoomTypes = ref(false)
const savingRoomType = ref(false)
const currentHotelId = ref(null)

const hotelForm = reactive({
  name: '',
  address: '',
  region: '',
  starRating: '',
  brand: '',
  isOwn: true
})

const roomTypes = ref([])

const roomTypeDialogVisible = ref(false)
const isEditRoomType = ref(false)
const roomTypeForm = reactive({
  id: '',
  hotelId: '',
  name: '',
  basePrice: 0,
  amenities: ''
})

const loadHotels = async () => {
  try {
    const response = await api.getOwnHotels()
    if (response.code === 200 && response.data && response.data.length > 0) {
      const hotel = response.data[0]
      currentHotelId.value = hotel.id
      hotelForm.name = hotel.name || ''
      hotelForm.address = hotel.address || ''
      hotelForm.region = hotel.region || ''
      hotelForm.starRating = hotel.starRating || ''
      hotelForm.brand = hotel.brand || ''
      hotelForm.isOwn = hotel.isOwn !== false
      
      loadRoomTypes(hotel.id)
    }
  } catch (error) {
    console.error('加载酒店信息失败:', error)
  }
}

const loadRoomTypes = async (hotelId) => {
  if (!hotelId) return
  loadingRoomTypes.value = true
  try {
    const response = await api.getRoomTypes(hotelId)
    if (response.code === 200) {
      roomTypes.value = response.data || []
    }
  } catch (error) {
    console.error('加载房型信息失败:', error)
  } finally {
    loadingRoomTypes.value = false
  }
}

const saveHotelInfo = async () => {
  if (!hotelForm.name) {
    ElMessage.warning('请输入酒店名称')
    return
  }
  saving.value = true
  try {
    const hotelData = {
      name: hotelForm.name,
      address: hotelForm.address,
      region: hotelForm.region,
      starRating: hotelForm.starRating,
      brand: hotelForm.brand,
      isOwn: hotelForm.isOwn
    }
    
    let response
    if (currentHotelId.value) {
      response = await api.updateHotel(currentHotelId.value, hotelData)
    } else {
      response = await api.createHotel(hotelData)
    }
    
    if (response.code === 200) {
      ElMessage.success(response.message || '保存成功')
      if (!currentHotelId.value && response.data) {
        currentHotelId.value = response.data.id
      }
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存酒店信息失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const addRoomType = () => {
  if (!currentHotelId.value) {
    ElMessage.warning('请先保存酒店信息')
    return
  }
  isEditRoomType.value = false
  roomTypeForm.id = ''
  roomTypeForm.hotelId = currentHotelId.value
  roomTypeForm.name = ''
  roomTypeForm.basePrice = 0
  roomTypeForm.amenities = ''
  roomTypeDialogVisible.value = true
}

const editRoomType = (roomType) => {
  isEditRoomType.value = true
  roomTypeForm.id = roomType.id
  roomTypeForm.hotelId = roomType.hotelId || currentHotelId.value
  roomTypeForm.name = roomType.name
  roomTypeForm.basePrice = roomType.basePrice
  roomTypeForm.amenities = roomType.amenities
  roomTypeDialogVisible.value = true
}

const saveRoomType = async () => {
  if (!roomTypeForm.name) {
    ElMessage.warning('请输入房型名称')
    return
  }
  savingRoomType.value = true
  try {
    const roomTypeData = {
      hotel: { id: currentHotelId.value },
      name: roomTypeForm.name,
      basePrice: roomTypeForm.basePrice,
      amenities: roomTypeForm.amenities
    }
    
    let response
    if (isEditRoomType.value) {
      response = await api.updateRoomType(roomTypeForm.id, roomTypeData)
    } else {
      response = await api.createRoomType(roomTypeData)
    }
    
    if (response.code === 200) {
      ElMessage.success(response.message || '保存成功')
      roomTypeDialogVisible.value = false
      loadRoomTypes(currentHotelId.value)
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存房型失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    savingRoomType.value = false
  }
}

const deleteRoomType = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个房型吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.deleteRoomType(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadRoomTypes(currentHotelId.value)
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除房型失败:', error)
      ElMessage.error('删除失败，请重试')
    }
  }
}

onMounted(() => {
  loadHotels()
})
</script>

<style scoped>
.hotel-setting-container {
  padding: 20px 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mt-20 {
  margin-top: 20px;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>