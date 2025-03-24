<template>
  <div class="container">
    <div v-if="showSearch" class="head-container">
      <el-input v-model="deptName" class="mb-20px" clearable :placeholder="placeholder" >
        <template #prefix>
          <Icon icon="ep:search" />
        </template>
      </el-input>
    </div>
    <div class="tree-container" style="margin-left:-20px;height:500px;width:calc(100% + 40px); /* 增加宽度以适应滚动条 */ overflow-x: auto; white-space: nowrap;">
      <el-tree
        ref="treeRef"
        :data="showList"
        :props="defaultProps"
        :accordion="isAccordion"
        :filter-node-method="filterNode"
        :show-checkbox="showCheckbox"
        :lazy="lazy"
        :load="load"
        node-key="id"
        :default-checked-keys="checkKeys"
        :default-expanded-keys="expandKeys"
        @node-click="handleNodeClick"
        @check="handleCheckedNodes"
        style="min-width: 200px; /* 根据你的内容设置最小宽度 */ display: inline-block; white-space: nowrap;">
        <!-- 注意：这里的 style 属性是直接在 el-tree 上设置的，用于确保树组件本身不换行 -->
      </el-tree>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ElTree } from 'element-plus';
import * as DeptApi from '@/api/system/dept';
import { defineProps } from 'vue';
import type Node from 'element-plus/es/components/tree/src/model/node';
import { string } from 'vue-types';
import { ar, da } from 'element-plus/es/locale';
import { c } from 'vite/dist/node/types.d-aGj9QkWt';
import { add } from '@jsplumb/browser-ui';

const emits = defineEmits(['node-click', 'check'])
const props = defineProps({
  dataList: Array<any>,
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
const showList = ref<any[]>([])
const expandKeys=ref([])
/** 复选初始化 */
const initCheck = (keys) => {
  treeRef.value!.setCheckedKeys(keys)
  console.log('initCheck', props.dataList)
}

/** 基于名字过滤 */
const filterNode = (value:string, data) => {
  if (!value) return true

  return data.name.includes(value) || data.unique.includes(value);
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
watch(deptName, () => {
  search()
})
watch(()=>props.dataList, (val:any[]) => {
  showList.value = val
  console.log('showList.value', showList.value)
})
defineExpose({ initCheck })
// showList.value = props.dataList
function search(){
  if(deptName.value==null||deptName.value=="") {
    showList.value = props.dataList;
    expandKeys.value=[]
    return;
  }
  let ans=[]
  for(let i in props.dataList){
    // console.log("i",i)
    // console.log("props.dataList[i]",props.dataList[i])
    let deepAns=deepFind(props.dataList[i])
    if(deepAns!=null){
      ans.push(deepAns)
    }
  }
  console.log('ans',ans)
  showList.value=ans
  for(let i in ans){
    addExpandKeys(ans[i])
  }
  // treeRef.value.expandAll();
  // console.log("treeRef.value",treeRef.value)
  // const allNodes = treeRef.value.store._getAllNodes();
  // allNodes.forEach(node => {
  //   node.expanded = true;
  // });
}
function addExpandKeys(node){
  if(node==null||node.children==null||node.children.length==0)return;
  expandKeys.value.push(node.id)
  for(let i in node.children){
    addExpandKeys(node.children[i])
  }
}
function deepFind(node){
  // console.log('befingNode',node)
  if(node==null) return null
  if(node.children==null||node.children.length==0){
    // console.log('node',node)
    if(node.name.includes(deptName.value)) return {...node}
    else return null
  }else{
    let ans=[]
    for(let i in node.children){
      let deepAns=deepFind(node.children[i])
      if(deepAns!=null){
        ans.push(deepAns)
      }
    }
    if(ans.length>0){
      return {...node,children:ans}
    }else{
      return null
    }
  }
}
search()
</script>

<style scoped>
/* 自定义按钮组件的样式 */
.container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}
</style>