<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="80%">
    <el-table :data="tableData" style="width: 100%">
      <el-table-column prop="id" label="编号" width="135" align="center" />
      <el-table-column v-if="formType == 'mail'" prop="mail" label="邮箱" min-width="120" align="center">
        <template #default="scope">
          <el-input :disabled="scope.row.id != editId"  v-model="scope.row.mail" />
        </template>
      </el-table-column>
      <el-table-column v-if="formType == 'phone'" prop="phone" label="手机号" min-width="120" align="center">
        <template #default="scope">
          <el-input :disabled="scope.row.id != editId"  v-model="scope.row.phone" />
        </template>
      </el-table-column>
      <el-table-column prop="isEnable" label="是否可用" min-width="100" align="center" >
        <template #default="scope">
          <el-switch :disabled="scope.row.id != editId" v-model="scope.row.isEnable" :active-value="1" :inactive-value="0" />
        </template>
      </el-table-column>
      <el-table-column v-if="formType == 'mail'" prop="mailDesc" label="描述" min-width="120" align="center">
        <template #default="scope">
          <el-input :disabled="scope.row.id != editId"  v-model="scope.row.mailDesc" />
        </template>
      </el-table-column>
      <el-table-column v-if="formType == 'phone'" prop="smsDesc" label="描述" min-width="120" align="center">
        <template #default="scope">
          <el-input :disabled="scope.row.id != editId"  v-model="scope.row.smsDesc" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="100" align="center" />
      <el-table-column prop="updateTime" label="更新时间" min-width="100" align="center" />
      <el-table-column label="操作" min-width="100" align="center">
        <template #default="scope">
          <el-button link v-if="scope.row.id != editId" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button link v-if="scope.row.id != editId" type="primary" @click="handleDelete(scope.$index)">删除</el-button>
          <el-button link v-if="scope.row.id == editId" type="primary" @click="handleAdd(scope.row)">{{editId == '' ? '添加' : '确认'}}</el-button>
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

const addVlue = ref('')
const editId = ref('')
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：mail - 邮箱；phone - 短信
const tableData = ref<any>([])
const baseParam = {
  id: '',
  isEnable: 0,
  updateTime: '',
  createTime: '',
}

/** 打开弹窗 */
const open = async (type: string) => {
  editId.value = ''
  tableData.value = []
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
    tableData.value = res
    if (type == 'mail') {
      tableData.value.push({
        ...baseParam,
        mail: '',
        mailDesc: ''
      })
    } else {
      tableData.value.push({
        ...baseParam,
        phone: '',
        smsDesc: ''
      })
    }
  } finally {
    formLoading.value = false
  }
  
}

defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const handleEdit = (row) => {
  editId.value = row.id
}

const handleAdd = (row) => {
  console.log('handleAdd', row)
  if((formType.value == 'mail' && !row.mail) || (formType.value == 'phone' && !row.phone)) {
    message.error(`请填写${formType.value == 'mail' ? '邮箱' : '手机号'}`)
    return
  }
  editId.value = ''
  if (row.id == '') {
    if (formType.value == 'mail') {
      tableData.value.push({
        ...baseParam,
        mail: ''
      })
    } else {
      tableData.value.push({
        ...baseParam,
        phone: ''
      })
    }
    row.id = new Date().getTime() + '(临时编号)'
  }
}

const handleDelete = (index) => {
  console.log('handleDelete', index)
  tableData.value.splice(index, 1)
}

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调

const submitForm = async () => {
  // 校验表单
  // 提交请求
  console.log('提交请求', tableData.value)
  formLoading.value = true
  const popData = tableData.value.pop()
  try {
    let res = null
    console.log('tableData.value', tableData.value)
    if(formType.value == 'mail') {
      res = await AlarmApi.saveMailConfig(tableData.value.map(item =>{
        const result = {
          mail: item.mail,
          isEnable: item.isEnable,
          mailDesc: item.mailDesc,
        } as any
        if (String(item.id).indexOf('临时') < 0) result.id = item.id
        return result
      }))
    } else {
      res = await AlarmApi.savePhoneConfig(tableData.value.map(item =>{
        const result = {
          phone: item.phone,
          isEnable: item.isEnable,
          smsDesc: item.smsDesc,
        } as any
        if (String(item.id).indexOf('临时') < 0) result.id = item.id
        return result
      }))
    }
    dialogVisible.value = false
    console.log('发送操作成功的事件', res)
    message.success('配置成功')
    // 发送操作成功的事件
    emit('success')
  } catch (error) {
    console.log('error', error)
    tableData.value.push(popData)
  } finally {
    formLoading.value = false
  }
}
</script>