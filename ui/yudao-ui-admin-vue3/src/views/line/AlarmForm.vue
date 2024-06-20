<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-table :data="tableData" style="width: 100%">
      <el-table-column prop="id" label="编号" width="120" />
      <el-table-column prop="mail" label="邮箱" width="120" />
      <el-table-column prop="isEnable" label="是否可用" width="120" />
      <el-table-column prop="mailDesc" label="描述" width="600" />
      <el-table-column prop="createTime" label="创建时间" width="120" />
      <el-table-column prop="updateTime" label="更新时间" width="120" />
      <el-table-column label="操作" width="120">
        <template #default>
          <el-button link type="primary" size="small" @click="handleEdit">编辑</el-button>
          <el-button link type="primary" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import { AlarmApi } from '@/api/system/notify/alarm'
import { FormRules } from 'element-plus'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const tableData = ref<any>([])

/** 打开弹窗 */
const open = async (type: string) => {
  dialogVisible.value = true
  dialogTitle.value = type == 'phone' ? '手机配置': '邮箱配置'
  formType.value = type
  try {
    formLoading.value = true
    let res = null
    if (type == 'mail') {
      res = await AlarmApi.getMailList({})
    } else {
      res = await AlarmApi.getPhoneList({})
    }
    tableData.value.push({mail: '...'})
    console.log('res', res)
  } finally {
    formLoading.value = false
  }
  
}

defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const handleEdit = () => {
  console.log('handleEdit')
}

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  // 提交请求
  console.log('提交请求')
  formLoading.value = true
  try {
    // const data = formData.value as unknown as UserApi.UserVO
    // if (formType.value === 'create') {
    //   await UserApi.createUser(data)
    //   message.success(t('common.createSuccess'))
    // } else {
    //   await UserApi.updateUser(data)
    //   message.success(t('common.updateSuccess'))
    // }
    dialogVisible.value = false
    console.log('发送操作成功的事件')
    // 发送操作成功的事件
    emit('success')
  } catch (error) {
    console.log('error', error)
  } finally {
    formLoading.value = false
  }
}
</script>