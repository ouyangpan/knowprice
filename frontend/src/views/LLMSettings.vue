<template>
  <div class="llm-settings-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>大模型设置</span>
        </div>
      </template>
      <el-form :model="llmForm" label-width="120px" v-loading="loading">
        <el-form-item label="启用大模型建议">
          <el-switch v-model="llmForm.enabled" />
        </el-form-item>
        <el-form-item label="API服务地址">
          <el-input v-model="llmForm.apiEndpoint" placeholder="请输入API服务地址" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="llmForm.apiKey" type="password" placeholder="请输入API密钥" show-password />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-select v-model="llmForm.modelName" placeholder="请选择模型">
            <el-option label="minimax2.5" value="minimax2.5" />
            <el-option label="gpt-3.5-turbo" value="gpt-3.5-turbo" />
            <el-option label="gpt-4" value="gpt-4" />
          </el-select>
        </el-form-item>
        <el-form-item label="温度参数">
          <el-input-number v-model="llmForm.temperature" :min="0" :max="1" :step="0.1" :precision="1" />
        </el-form-item>
        <el-form-item label="最大Token数">
          <el-input-number v-model="llmForm.maxTokens" :min="500" :max="4000" :step="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveLLMConfig" :loading="saving">保存配置</el-button>
          <el-button @click="testLLMConnection" :loading="testing">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="mt-20">
      <template #header>
        <div class="card-header">
          <span>Prompt模板管理</span>
          <el-button type="primary" size="small" @click="addPromptTemplate">
            <el-icon><Plus /></el-icon>
            添加模板
          </el-button>
        </div>
      </template>
      <el-table :data="promptTemplates" style="width: 100%" v-loading="loadingTemplates">
        <el-table-column prop="name" label="模板名称" width="180" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="isActive" label="是否启用" width="100">
          <template #default="scope">
            <el-switch v-model="scope.row.isActive" @change="updatePromptTemplate(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editPromptTemplate(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="deletePromptTemplate(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Prompt模板编辑对话框 -->
    <el-dialog
      v-model="promptTemplateDialogVisible"
      :title="isEditPromptTemplate ? '编辑Prompt模板' : '添加Prompt模板'"
      width="600px"
    >
      <el-form :model="promptTemplateForm" label-width="100px">
        <el-form-item label="模板名称">
          <el-input v-model="promptTemplateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="promptTemplateForm.version" placeholder="请输入版本号" />
        </el-form-item>
        <el-form-item label="系统提示词">
          <el-input v-model="promptTemplateForm.systemPrompt" type="textarea" :rows="4" placeholder="请输入系统提示词" />
        </el-form-item>
        <el-form-item label="用户提示词模板">
          <el-input v-model="promptTemplateForm.userPromptTemplate" type="textarea" :rows="6" placeholder="请输入用户提示词模板" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="promptTemplateForm.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="promptTemplateDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="savePromptTemplate" :loading="savingTemplate">保存</el-button>
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

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const loadingTemplates = ref(false)
const savingTemplate = ref(false)

const llmForm = reactive({
  enabled: false,
  apiEndpoint: '',
  apiKey: '',
  modelName: 'minimax2.5',
  temperature: 0.7,
  maxTokens: 2000
})

const promptTemplates = ref([])
const promptTemplateDialogVisible = ref(false)
const isEditPromptTemplate = ref(false)
const promptTemplateForm = reactive({
  id: '',
  name: '',
  version: '',
  systemPrompt: '',
  userPromptTemplate: '',
  isActive: true
})

const loadLLMConfig = async () => {
  loading.value = true
  try {
    const response = await api.getLLMConfig()
    if (response.code === 200 && response.data) {
      const config = response.data
      llmForm.enabled = config.enabled || false
      llmForm.apiEndpoint = config.apiEndpoint || 'https://api.minimax.chat/v1/text/chatcompletions'
      llmForm.apiKey = config.apiKey || ''
      llmForm.modelName = config.modelName || 'minimax2.5'
      llmForm.temperature = config.temperature || 0.7
      llmForm.maxTokens = config.maxTokens || 2000
    }
  } catch (error) {
    console.error('加载大模型配置失败:', error)
  } finally {
    loading.value = false
  }
}

const loadPromptTemplates = async () => {
  loadingTemplates.value = true
  try {
    const response = await api.getPromptTemplates()
    if (response.code === 200) {
      promptTemplates.value = response.data || []
    }
  } catch (error) {
    console.error('加载Prompt模板失败:', error)
  } finally {
    loadingTemplates.value = false
  }
}

const saveLLMConfig = async () => {
  saving.value = true
  try {
    const configData = {
      enabled: llmForm.enabled,
      apiEndpoint: llmForm.apiEndpoint,
      apiKey: llmForm.apiKey,
      modelName: llmForm.modelName,
      temperature: llmForm.temperature,
      maxTokens: llmForm.maxTokens
    }
    
    const response = await api.saveLLMConfig(configData)
    if (response.code === 200) {
      ElMessage.success('大模型配置保存成功')
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存大模型配置失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const testLLMConnection = async () => {
  testing.value = true
  try {
    const response = await api.testLLMConnection()
    if (response.code === 200) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(response.message || '连接测试失败')
    }
  } catch (error) {
    console.error('测试连接失败:', error)
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

const addPromptTemplate = () => {
  isEditPromptTemplate.value = false
  promptTemplateForm.id = ''
  promptTemplateForm.name = ''
  promptTemplateForm.version = ''
  promptTemplateForm.systemPrompt = ''
  promptTemplateForm.userPromptTemplate = ''
  promptTemplateForm.isActive = true
  promptTemplateDialogVisible.value = true
}

const editPromptTemplate = (template) => {
  isEditPromptTemplate.value = true
  promptTemplateForm.id = template.id
  promptTemplateForm.name = template.name
  promptTemplateForm.version = template.version
  promptTemplateForm.systemPrompt = template.systemPrompt
  promptTemplateForm.userPromptTemplate = template.userPromptTemplate
  promptTemplateForm.isActive = template.isActive
  promptTemplateDialogVisible.value = true
}

const savePromptTemplate = async () => {
  if (!promptTemplateForm.name || !promptTemplateForm.systemPrompt) {
    ElMessage.warning('请填写完整的模板信息')
    return
  }
  savingTemplate.value = true
  try {
    const templateData = {
      name: promptTemplateForm.name,
      version: promptTemplateForm.version || '1.0',
      systemPrompt: promptTemplateForm.systemPrompt,
      userPromptTemplate: promptTemplateForm.userPromptTemplate,
      isActive: promptTemplateForm.isActive
    }
    
    let response
    if (isEditPromptTemplate.value) {
      response = await api.savePromptTemplate({ ...templateData, id: promptTemplateForm.id })
    } else {
      response = await api.savePromptTemplate(templateData)
    }
    
    if (response.code === 200) {
      ElMessage.success('Prompt模板保存成功')
      promptTemplateDialogVisible.value = false
      loadPromptTemplates()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存Prompt模板失败:', error)
    ElMessage.error('保存失败，请重试')
  } finally {
    savingTemplate.value = false
  }
}

const updatePromptTemplate = async (template) => {
  try {
    await api.savePromptTemplate(template)
    ElMessage.success('模板状态更新成功')
  } catch (error) {
    console.error('更新模板失败:', error)
    ElMessage.error('更新失败')
  }
}

const deletePromptTemplate = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个Prompt模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await api.deletePromptTemplate(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadPromptTemplates()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadLLMConfig()
  loadPromptTemplates()
})
</script>

<style scoped>
.llm-settings-container {
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