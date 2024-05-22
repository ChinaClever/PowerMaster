<template>
  <el-row :gutter="20">
   <el-col :span="treeWidth" :xs="24">
     <el-input
       v-model="filterText"
       style="width: 190px"
       placeholder=""
     />

     <el-tree
       ref="treeRef"
       style="max-width: 600px"
       class="filter-tree"
       :data="serverRoomArr"
       :props="defaultProps"
       default-expand-all
       show-checkbox
       :filter-node-method="filterNode"
     />
   </el-col>
   <el-col :span="24 - treeWidth" :xs="24">
     <ContentWrap>
       <!-- 搜索工作栏 -->
       <el-form
         class="-mb-15px"
         :model="queryParams"
         ref="queryFormRef"
         :inline="true"
         label-width="120px"
       >
         <el-form-item label="" prop="collaspe">
           <el-switch 
             v-model="isCollapsed"  
             active-color="#409EFF" 
             inactive-color="#909399"
             active-text="折叠"  
             active-value="100"
             inactive-value="0" 
             @change="toggleCollapse" />
         </el-form-item>

         <el-form-item label="时间段" prop="searchTime">
           <el-date-picker
             v-model="queryParams.searchTime"
             value-format="YYYY-MM-DD HH:mm:ss"
             type="daterange"
             start-placeholder="开始日期"
             end-placeholder="结束日期"
             :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
             class="!w-210px"
           />
         </el-form-item>

         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
           <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
           <el-button type="success" plain @click="handleExport" :loading="exportLoading">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
         </el-form-item>
       </el-form>
     </ContentWrap>
     <!-- 列表 -->
     <ContentWrap>
       <el-table v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
         <template v-for="column in tableColumns">
           <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" v-if="column.istrue" />
         </template>
       </el-table>
       <!-- 分页 -->
       <Pagination
         :total="total"
         v-model:page="queryParams.pageNo"
         v-model:limit="queryParams.pageSize"
         @pagination="getList"
       />
     </ContentWrap>

   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { ElTree } from 'element-plus'

defineOptions({ name: 'PowerAnalysis' })

const serverRoomArr =  [
 {
   value: '1',
   label: '机房1',
   children: [
     {
       value: '1-1',
       label: '柜列1',
       children: [
       {
         value: '1-1-1',
         label: '机柜1',
       },
       {
         value: '1-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
 {
   value: '2',
   label: '机房2',
   children: [
     {
       value: '2-1',
       label: '柜列1',
       children: [
       {
         value: '2-1-1',
         label: '机柜1',
       },
       {
         value: '2-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
 {
   value: '3',
   label: '机房3',
   children: [
     {
       value: '3-1',
       label: '柜列1',
       children: [
       {
         value: '3-1-1',
         label: '机柜1',
       },
       {
         value: '3-1-2',
         label: '机柜2',
       },]
     },
   ],
 },
]
//折叠功能
let treeWidth = ref(3)
let isCollapsed = ref(0);
const toggleCollapse = () => {
 treeWidth.value = isCollapsed.value == 0 ? 3 : 0;
};
//树型控件
interface Tree {
 [key: string]: any
}
const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()
const filterNode = (value: string, data: Tree) => {
 if (!value) return true
 return data.label.includes(value)
}
const defaultProps = {
 children: 'children',
 label: 'label',
}
watch(filterText, (val) => {
 treeRef.value!.filter(val)
})

const message = useMessage() // 消息弹窗

const loading = ref(true) // 列表的加载中
const list = ref<Array<{ 
   id: number; 
   location: string; 
   totalEle: number;
   aEle: number;
   bEle: number;
   createTime:string
}>>([]); // 列表数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
 pageNo: 1,
 pageSize: 10,
 searchTime: undefined,
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

const tableColumns = ref([
   { label: '编号', align: 'center', prop: 'id' , istrue:true},
   { label: '位置', align: 'center', prop: 'address' , istrue:true},
   { label: '总电能(kWh)', align: 'center', prop: 'totalEle' , istrue:true},
   { label: 'a路电能(kWh)', align: 'center', prop: 'aEle' , istrue:true},
   { label: 'b路电能(kWh)', align: 'center', prop: 'bEle' , istrue:true},
   { label: '网络地址', align: 'center', prop: 'location' , istrue:true},
   { label: '创建时间', align: 'center', prop: 'createTime', formatter: dateFormatter, istrue:true},
]);

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    // 生成假数据
    const fakeData = [
    {
      id: 1,
      location: "机房1-机柜1",
      totalEle: 500,
      aEle: 250,
      bEle: 250,
      createTime: "2024-04-08 09:00:00"
    },
    {
    id: 2,
    location: "机房1-机柜2",
    totalEle: 800,
    aEle: 400,
    bEle: 400,
    createTime: "2024-04-08 10:30:00"
    },
    {
    id: 3,
    location: "机房1-机柜3",
    totalEle: 1200,
    aEle: 600,
    bEle: 600,
    createTime: "2024-04-08 14:00:00"
    },
    {
    id: 4,
    location: "机房1-机柜4",
    totalEle: 300,
    aEle: 150,
    bEle: 150,
    createTime: "2024-04-08 09:30:00"
    },
    {
    id: 5,
    location: "机房2-机柜1",
    totalEle: 1000,
    aEle: 500,
    bEle: 500,
    createTime: "2024-04-08 13:00:00"
    },
    {
    id: 6,
    location: "机房2-机柜2",
    totalEle: 600,
    aEle: 300,
    bEle: 300,
    createTime: "2024-04-08 16:00:00"
    },
    {
    id: 7,
    location: "机房2-机柜3",
    totalEle: 400,
    aEle: 200,
    bEle: 200,
    createTime: "2024-04-08 11:30:00"
    },
    {
    id: 8,
    location: "机房2-机柜4",
    totalEle: 700,
    aEle: 350,
    bEle: 350,
    createTime: "2024-04-08 15:00:00"
    },
    {
    id: 9,
    location: "机房2-机柜5",
    totalEle: 900,
    aEle: 450,
    bEle: 450,
    createTime: "2024-04-08 12:45:00"
    },
    {
    id: 10,
    location: "机房2-机柜6",
    totalEle: 200,
    aEle: 100,
    bEle: 100,
    createTime: "2024-04-08 14:30:00"
    }
    ];
    list.value = fakeData
    total.value = 10
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
 queryParams.pageNo = 1
 getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
 queryFormRef.value.resetFields()
 handleQuery()
}


/** 导出按钮操作 */
const handleExport = async () => {
 try {
   // 导出的二次确认
   await message.exportConfirm()
   // 发起导出
   exportLoading.value = true
   const data = await HistoryDataApi.exportHistoryData(queryParams)
   download.excel(data, '电能分析.xls')
 } catch {
 } finally {
   exportLoading.value = false
 }
}

/** 初始化 **/
onMounted(() => {
 getList();
})
</script>