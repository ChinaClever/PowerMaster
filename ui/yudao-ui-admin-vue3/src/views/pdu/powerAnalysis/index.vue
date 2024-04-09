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

         <el-form-item label="总/输出位" prop="createTime">
          <el-cascader
            v-model="defaultSelected"
            collapse-tags
            :options="selection"
            collapse-tags-tooltip
            :show-all-levels="false"
            @change="cascaderChange"
            class="!w-120px"
          />
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

         <el-form-item label="颗粒度" prop="type">
            <el-select
              v-model="queryParams.granularity"
              placeholder="请选择天/周/月"
              class="!w-120px" >
              <el-option label="天" value="day" />
              <el-option label="周" value="week" />
              <el-option label="月" value="month" />
            </el-select>
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
           <el-table-column :key="column.prop" :label="column.label" :align="column.align" :prop="column.prop" :formatter="column.formatter" :width="column.width" v-if="column.istrue" />
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
     <ContentWrap style="overflow: visible;">
      <div ref="rankChartContainer" id="rankChartContainer" style="width: 70vw; height: 58vh"></div>
    </ContentWrap>

   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { ElTree } from 'element-plus'
import * as echarts from 'echarts';

defineOptions({ name: 'PowerAnalysis' })

const serverRoomArr =  [
 {
   value: '1',
   label: '机房1',
   children: [
     {
       value: '1-1',
       label: '柜列1',
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

const message = useMessage() 
// 列表
const loading = ref(true)
const list = ref<Array<{ 
   id: number; 
   location: string; 
   cabinetId: number; 
   startEle: number;
   startTime: string;
   endEle: number;
   endTime: string;
   eq: number;
   bill: number;
   createTime:string
}>>([]); 
const total = ref(0)
const queryParams = reactive({
 pageNo: 1,
 pageSize: 10,
 type: 'total',
 granularity: 'day',
 searchTime: undefined,
})
const queryFormRef = ref()
const exportLoading = ref(false)

// 总/输出位筛选
const defaultSelected = ref(['total'])
const selection = ref([
  {
    value: "total",
    label: '总'
  },
  {
    value: "outlet",
    label: '输出位',
    children: [
      { value: "输出位1", label: '输出位1' },
      { value: "输出位2", label: '输出位2' },
      { value: "输出位3", label: '输出位3' },
      { value: "输出位4", label: '输出位4' },
      { value: "输出位5", label: '输出位5' },
      { value: "输出位6", label: '输出位6' },
      { value: "输出位7", label: '输出位7' },
      { value: "输出位8", label: '输出位8' },
      { value: "输出位9", label: '输出位9' },
      { value: "输出位10", label: '输出位10' },
    ],
  },

])

function getRandomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}
const cascaderChange = (select) => {
  if (select[0] === 'outlet'){
    list.value = [];
    for (let i = 1; i <= 10; i++) {
      const startEle = getRandomInt(100, 500);
      const endEle = getRandomInt(500, 1000); 
      const eq = endEle - startEle; 
      const location = `机房${Math.ceil(i / 5)}-机柜${i}-PDU${i}-${select[1]}`; 
      const bill = getRandomInt(100, 300); 
      list.value.push({
          id: i,
          cabinetId: 101,
          location: location,
          startEle: startEle,
          startTime: "2024-04-08 08:00:00",
          endEle: endEle,
          endTime: "2024-04-08 09:00:00",
          eq: eq,
          bill: bill,
          createTime: "2024-04-08 09:00:00",
      });
    }
    total.value = 10
  }else{
    getList();
  }

}

// 柱状图
const rankChartContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null;
const eqData = ref<number[]>([]);
const initChart = () => {
  const instance = getCurrentInstance();
  if (rankChartContainer.value && instance) {
    rankChart = echarts.init(rankChartContainer.value);
    rankChart.setOption({
      title: { text: '各PDU耗电量'},
      tooltip: { trigger: 'axis'},
      legend: { data: []},
      toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{}, saveAsImage:{}}},
      xAxis: {type: 'category', data: ['PDU1', 'PDU2', 'PDU3', 'PDU4', 'PDU5']},
      yAxis: { type: 'value', name: "kWh"},
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' }, barWidth: 50},
      ],
    });
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }

};

window.addEventListener('resize', function() {
  rankChart?.resize(); 
});

watch(() => queryParams.granularity, (newValues) => {
   const newGranularity = newValues;
   if ( newGranularity == 'day'){
    getList();
    rankChart?.setOption({
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' }, barWidth: 50},
      ],
    })
   }
   if ( newGranularity == 'week'){
    const fakeData = [
      {
        id: 1,
        location: "机房1-机柜1-PDU1",
        cabinetId: 101,
        startEle: 150,
        startTime: "2024-03-01 00:00:00",
        endEle: 500,
        endTime: "2024-03-07 00:00:00",
        eq: 350,
        bill: 100.2,
        createTime: "2024-03-07 00:00:00",
      },
      {
        id: 2,
        location: "机房1-机柜2-PDU1",
        cabinetId: 102,
        startEle: 500,
        startTime: "2024-03-07 00:00:00",
        endEle: 850,
        endTime: "2024-03-14 00:00:00",
        eq: 700,
        bill: 210,
        createTime: "2024-03-14 00:00:00"
      },
      {
        id: 3,
        location: "机房1-机柜2-PDU1",
        cabinetId: 103,
        startEle: 1000,
        startTime: "2024-03-14 00:00:00",
        endEle: 1400,
        endTime: "2024-03-21 00:00:00",
        eq: 400,
        bill: 280,
        createTime: "2024-03-21 00:00:00",
      },
      {
        id: 4,
        location: "机房1-机柜2-PDU1",
        cabinetId: 103,
        startEle: 1000,
        startTime: "2024-03-14 00:00:00",
        endEle: 1450,
        endTime: "2024-03-21 00:00:00",
        eq: 450,
        bill: 300,
        createTime: "2024-03-21 00:00:00",
      },
      {
        id: 5,
        location: "机房1-机柜2-PDU1",
        cabinetId: 102,
        startEle: 500,
        startTime: "2024-03-14 00:00:00",
        endEle: 850,
        endTime: "2024-03-21 00:00:00",
        eq: 350,
        bill: 210,
        createTime: "2024-03-21 00:00:00"
      },
    ];
    eqData.value = fakeData.map((item) => item.eq);
    list.value = fakeData
    total.value = 5
    rankChart?.setOption({
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' }, barWidth: 50},
      ],
    })
   }
   if ( newGranularity == 'month'){
    const fakeData = [
      {
        id: 1,
        location: "机房1-机柜1-PDU1",
        cabinetId: 101,
        startEle: 140,
        startTime: "2024-03-01 00:00:00",
        endEle: 500,
        endTime: "2024-03- 00:00:00",
        eq: 350,
        bill: 1000,
        createTime: "2024-03-07 00:00:00",
      },
      {
        id: 2,
        location: "机房1-机柜2-PDU1",
        cabinetId: 102,
        startEle: 500,
        startTime: "2024-03-07 00:00:00",
        endEle: 850,
        endTime: "2024-03-14 00:00:00",
        eq: 700,
        bill: 1500,
        createTime: "2024-03-14 00:00:00"
      },
      {
        id: 3,
        location: "机房1-机柜2-PDU1",
        cabinetId: 103,
        startEle: 1000,
        startTime: "2024-03-14 00:00:00",
        endEle: 1400,
        endTime: "2024-03-21 00:00:00",
        eq: 250,
        bill: 1600,
        createTime: "2024-03-21 00:00:00",
      },
      {
        id: 4,
        location: "机房1-机柜5-PDU1",
        cabinetId: 103,
        startEle: 1000,
        startTime: "2024-03-14 00:00:00",
        endEle: 1450,
        endTime: "2024-03-21 00:00:00",
        eq: 450,
        bill: 2000,
        createTime: "2024-03-21 00:00:00",
      },
      {
        id: 5,
        location: "机房1-机柜6-PDU1",
        cabinetId: 102,
        startEle: 500,
        startTime: "2024-03-14 00:00:00",
        endEle: 850,
        endTime: "2024-03-21 00:00:00",
        eq: 400,
        bill: 2310,
        createTime: "2024-03-21 00:00:00"
      },
    ];
    eqData.value = fakeData.map((item) => item.eq);
    list.value = fakeData
    total.value = 5
    rankChart?.setOption({
      series: [
        {name:"耗电量",  type: 'bar', data: eqData.value, label: { show: true, position: 'top' }, barWidth: 50},
      ],
    })
   }
 });

const tableColumns = ref([
   { label: '编号', align: 'center', prop: 'id' , istrue:true},
   { label: '位置', align: 'center', prop: 'location' , istrue:true, width: '230px'},
   { label: '开始电能(kWh)', align: 'center', prop: 'startEle' , istrue:true, width: '140px'},
   { label: '开始时间', align: 'center', prop: 'startTime' , formatter: dateFormatter, width: '200px' , istrue:true},
   { label: '结束电能(kWh)', align: 'center', prop: 'endEle' , istrue:true, width: '140px'},
   { label: '结束时间', align: 'center', prop: 'endTime' , formatter: dateFormatter, width: '200px' , istrue:true},
   { label: '电量', align: 'center', prop: 'eq' , istrue:true, width: '140px'},
   { label: '电费(元)', align: 'center', prop: 'bill' , istrue:true},
   { label: '创建时间', align: 'center', prop: 'createTime', formatter: dateFormatter, width: '200px' , istrue:true},
]);

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    // 生成假数据
    const fakeData = [
      {
        id: 1,
        location: "机房1-机柜1-PDU1",
        cabinetId: 101,
        startEle: 150,
        startTime: "2024-04-08 08:00:00",
        endEle: 200,
        endTime: "2024-04-08 09:00:00",
        eq: 50,
        bill: 20.5,
        createTime: "2024-04-08 09:15:00"
      },
      {
        id: 2,
        location: "机房1-机柜2-PDU1",
        cabinetId: 102,
        startEle: 120,
        startTime: "2024-04-08 10:30:00",
        endEle: 180,
        endTime: "2024-04-08 12:00:00",
        eq: 60,
        bill: 15.75,
        createTime: "2024-04-08 12:15:00"
      },
      {
        id: 3,
        location: "机房1-机柜2-PDU2",
        cabinetId: 103,
        startEle: 300,
        startTime: "2024-04-08 14:00:00",
        endEle: 350,
        endTime: "2024-04-08 16:00:00",
        eq: 50,
        bill: 32.0,
        createTime: "2024-04-08 16:30:00"
      },
      {
        id: 4,
        location: "机房1-机柜3-PDU3",
        cabinetId: 104,
        startEle: 80,
        startTime: "2024-04-08 09:30:00",
        endEle: 120,
        endTime: "2024-04-08 10:45:00",
        eq: 40,
        bill: 10.25,
        createTime: "2024-04-08 11:00:00"
      },
      {
        id: 5,
        location: "机房1-机柜5-PDU1",
        cabinetId: 105,
        startEle: 200,
        startTime: "2024-04-08 13:00:00",
        endEle: 250,
        endTime: "2024-04-08 15:00:00",
        eq: 50,
        bill: 21.0,
        createTime: "2024-04-08 15:30:00"
      }
    ];
    eqData.value = fakeData.map((item) => item.eq);
    list.value = fakeData
    total.value = 5
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
 initChart();
})

</script>