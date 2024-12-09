<template>
  <div class="container">
    <div v-if="showSearch" class="head-container">
      <el-input v-model="deptName" class="mb-20px" clearable :placeholder="placeholder">
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
        :lazy="lazy"
        :load="load"
        node-key="unique"
        :default-checked-keys="checkKeys"
        @node-click="handleNodeClick"
        @check="handleCheckedNodes" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus'
import * as DeptApi from '@/api/system/dept'
import { defineProps } from 'vue'
import type Node from 'element-plus/es/components/tree/src/model/node'
import { string } from 'vue-types'

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
  lazy: {
    type: Boolean,
    default: false
  },
  load: {
    type: Function,
    required: false
  },
  placeholder: {
    type: String,
    default: "请输入",
    required: false
  }
})

const defaultProps = {
  children: 'children',
  label: 'name'
}

const checkKeys = ref<number[]>([])
const deptName = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

/** 复选初始化 */
const initCheck = (keys) => {
  treeRef.value!.setCheckedKeys(keys)
  console.log('initCheck', props.dataList)
}

/** 基于名字过滤 */
const filterNode = (value:string, data) => {
//   if (!value) return true
// console.log('测试1', data)
//   return data.name.includes(value) || data.unique.includes(value);
if (!value) return true;

  // 确保 data 是一个对象
  if (typeof data !== 'object' || data === null) {
    console.error('Invalid data object');
    return false;
  }
    console.log('测试1', data);

  // 确保 name 和 unique 属性存在且为字符串
  const name = typeof data.name === 'string' ? data.name : '';
  const unique = typeof data.unique === 'string' ? data.unique : '';

  return name.includes(value) || unique.includes(value);
}

/** 处理部门被点击 */
const handleNodeClick = async (row: { [key: string]: any }) => {
  console.log('处理部门被点击row', row)
  emits('node-click', row)
}

/** 处理多选被点击 */
const handleCheckedNodes = async (checkedNodes, checkedKeys) => {
  emits('check', treeRef.value?.getCheckedNodes())
  console.log('处理多选被点击', treeRef.value?.getCheckedKeys(true), treeRef.value?.getCheckedNodes())
}

/** 监听deptName */
watch(deptName, (val) => {
  treeRef.value!.filter(val)
})

defineExpose({ initCheck })

</script>

<style scoped>
/* 自定义按钮组件的样式 */
.container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}
</style>