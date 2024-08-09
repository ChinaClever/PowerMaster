<template>
  <el-table :data="tableData" style="width: 100%">
    <el-table-column prop="ip" label="ip地址" min-width="120" align="center">
      <template #default="scope">
        <el-input :disabled="scope.row.id != editId"  v-model="scope.row.ip" />
      </template>
    </el-table-column>
    <el-table-column label="操作" width="200" align="center">
      <template #default="scope">
        <el-button link v-if="scope.row.id != editId" type="primary" @click="handleEdit(scope.row)">编辑</el-button>
        <el-button link v-if="scope.row.id != editId" type="primary" @click="handleDelete(scope.$index)">删除</el-button>
        <el-button link v-if="scope.row.id == editId" type="primary" @click="handleAdd(scope.row)">{{editId == '' ? '添加' : '确认'}}</el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
<script lang="ts" setup>
import { AlarmApi } from '@/api/system/notify/alarm'
import { FormRules } from 'element-plus'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const editId = ref('')
const formType = ref('') // 表单的类型：mail - 邮箱；phone - 短信
const tableData = ref<any>([{
  ip: '',
  id: ''
}])
const emit = defineEmits(['update'])

const handleEdit = (row) => {
  editId.value = row.id
}

const handleAdd = (row) => {
  console.log('handleAdd', row)
  if(!row.ip) {
    message.error('请填写邮箱')
    return
  }
  editId.value = ''
  if (row.id == '') {
    tableData.value.push({
      id: '',
      ip: ''
    })
    row.id = new Date().getTime() + '(临时编号)'
  }
}

const handleDelete = (index) => {
  console.log('handleDelete', index)
  tableData.value.splice(index, 1)
}
watch(tableData,(val)=>{
  console.log('tableDatawatch', val)
  emit('update', val)
},{deep: true})
</script>
