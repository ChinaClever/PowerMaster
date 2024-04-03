<template>
  <ContentWrap style="height: calc(100% - 15px)">
    <div v-if="showSearch" class="head-container">
      <el-input v-model="deptName" class="mb-20px" clearable placeholder="请输入部门名称">
        <template #prefix>
          <Icon icon="ep:search" />
        </template>
      </el-input>
    </div>
    <div class="head-container">
      <el-tree
        ref="treeRef"
        :data="dataList"
        :props="defaultProps"
        :accordion="isAccordion"
        :filter-node-method="filterNode"
        :show-checkbox="showCheckbox"
        @node-click="handleNodeClick"
        @check="handleCheckedNodes" />
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus'
import * as DeptApi from '@/api/system/dept'
import { defineProps } from 'vue'

const defaultProps = {
  children: 'children',
  label: 'label'
}
const deptName = ref('')
const emits = defineEmits(['node-click', 'check'])
const props = defineProps({
  dataList: Array,
  showSearch: Boolean,
  isAccordion: {
    type: Boolean,
    default: false
  },
  showCheckbox: {
    type: Boolean,
    default: true
  },
})
const treeRef = ref<InstanceType<typeof ElTree>>()

/** 基于名字过滤 */
const filterNode = (value:string, data) => {
  if (!value) return true
  return data.label.includes(value)
}

/** 处理部门被点击 */
const handleNodeClick = async (row: { [key: string]: any }) => {
  console.log('row', row)
  emits('node-click', row)
}

/** 处理多选被点击 */
const handleCheckedNodes = async (checkedNodes, checkedKeys, halfCheckedNodes, halfCheckedKeys) => {
  emits('check', checkedNodes)
  console.log('处理多选被点击', checkedNodes, checkedKeys, halfCheckedNodes, halfCheckedKeys)
}

/** 监听deptName */
watch(deptName, (val) => {
  treeRef.value!.filter(val)
})

</script>

<style scoped>
/* 自定义按钮组件的样式 */
</style>