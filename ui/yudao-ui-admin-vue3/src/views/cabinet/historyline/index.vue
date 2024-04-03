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
         <el-form-item label="IP地址" prop="ipAddr">
           <el-input
             v-model="queryParams.ipAddr"
             placeholder="请输入IP地址"
             clearable
             @keyup.enter="handleQuery"
             class="!w-160px"
           />
           <el-form-item label="总/a路/b路" prop="type">
           <el-select
             v-model="queryParams.type"
             placeholder="请选择总/a路/b路"
             class="!w-120px"
           >
             <el-option label="总" value="total" />
             <el-option label="a路" value="a" />
             <el-option label="b路" value="b" />
           </el-select>
         </el-form-item>
         </el-form-item>
         <el-form-item label="颗粒度" prop="type">
           <el-select
             v-model="queryParams.granularity"
             placeholder="请选择分钟/小时/天"
             class="!w-120px"
           >
             <el-option label="分钟" value="realtime" />
             <el-option label="小时" value="hour" />
             <el-option label="天" value="day" />
           </el-select>
         </el-form-item>
         <el-form-item label="时间段" prop="createTime">
           <el-date-picker
             v-model="queryParams.createTime"
             value-format="YYYY-MM-DD HH:mm:ss"
             type="daterange"
             start-placeholder="开始日期"
             end-placeholder="结束日期"
             :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
             class="!w-210px"
           />
         </el-form-item>
         <div style="display: flex; justify-content: flex-end;">
         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
           <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
           <el-button type="primary" plain><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
         </el-form-item>
        </div>
       </el-form>


     </ContentWrap>
     <!-- 列表 -->
     <ContentWrap style="overflow: visible;">
      <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
    </ContentWrap>
   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { ElTree } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
/** pdu历史曲线 */
defineOptions({ name: 'HistoryLine' })

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined,
  createTime: undefined,
})

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
const loading = ref(true) // 列表的加载中

// 处理折线图数据
const createTimeData = ref<string[]>([]);
const totalActivePowData = ref<number[]>([]);
const aActivePowData = ref<number[]>([]);
const bActivePowData = ref<number[]>([]);
const totalApparentPowData = ref<number[]>([]);
const aApparentPowData = ref<number[]>([]);
const bApparentPowData = ref<number[]>([]);

const totalActivePowAvgValueData = ref<number[]>([]);
const totalActivePowMaxValueData = ref<number[]>([]);
const totalActivePowMinValueData = ref<number[]>([]);
const aActivePowAvgValueData = ref<number[]>([]);
const aActivePowMaxValueData = ref<number[]>([]);
const aActivePowMinValueData = ref<number[]>([]);
const bActivePowAvgValueData = ref<number[]>([]);
const bActivePowMaxValueData = ref<number[]>([]);
const bActivePowMinValueData = ref<number[]>([]);

const totalApparentPowAvgValueData = ref<number[]>([]);
const totalApparentPowMaxValueData = ref<number[]>([]);
const totalApparentPowMinValueData = ref<number[]>([]);
const aApparentPowAvgValueData = ref<number[]>([]);
const aApparentPowMaxValueData = ref<number[]>([]);
const aApparentPowMinValueData = ref<number[]>([]);
const bApparentPowAvgValueData = ref<number[]>([]);
const bApparentPowMaxValueData = ref<number[]>([]);
const bApparentPowMinValueData = ref<number[]>([]);

/** 查询列表 */
const getList = () => {
loading.value = true
 try {
    // 生成假数据
    const fakeData = [
      {
        id: 1,
        location: "机房1-机柜1",
        cabinetId: 123,
        totalApparentPow: 2300,
        aApparentPow: 2300,
        bApparentPow: 2300,
        totalActivePow: 2000,
        aActivePow: 2100,
        bActivePow: 2100,
        createTime: "2024-03-27 18:00:00",

        totalApparentPowAvgValue: 2300,
        totalApparentPowMaxValue: 2600,
        totalApparentPowMaxTime: "2024-03-27 14:00:00",
        totalApparentPowMinValue: 2200,
        totalApparentPowMinTime: "2024-03-27 14:00:00",

        aApparentPowAvgValue: 1000,
        aApparentPowMaxValue: 1200,
        aApparentPowMaxTime: "2024-03-27 15:00:00",
        aApparentPowMinValue: 900,
        aApparentPowMinTime: "2024-03-27 03:00:00",
      
        bApparentPowAvgValue: 1100,
        bApparentPowMaxValue: 1300,
        bApparentPowMaxTime: "2024-03-27 15:00:00",
        bApparentPowMinValue: 1000,
        bApparentPowMinTime: "2024-03-27 03:00:00",

        totalActivePowAvgValue: 2200,
        totalActivePowMaxValue: 2350,
        totalActivePowMaxTime: "2024-03-27 14:00:00",
        totalActivePowMinValue: 2100,
        totalActivePowMinTime: "2024-03-27 14:00:00",

        aActivePowAvgValue: 1100,
        aActivePowMaxValue: 1200,
        aActivePowMaxTime: "2024-03-27 15:00:00",
        aActivePowMinValue: 1000,
        aActivePowMinTime: "2024-03-27 03:00:00",
      
        bActivePowAvgValue: 1100,
        bActivePowMaxValue: 1200,
        bActivePowMaxTime: "2024-03-27 15:00:00",
        bActivePowMinValue: 1130,
        bActivePowMinTime: "2024-03-27 03:00:00",
      },
      {
        id: 2,
        location: "机房1-机柜1",
        cabinetId: 123,
        totalApparentPow: 2400,
        aApparentPow: 2300,
        bApparentPow: 2300,
        totalActivePow: 2150,
        aActivePow: 2100,
        bActivePow: 2100,
        createTime: "2024-03-27 18:00:00",

        totalApparentPowAvgValue: 2300,
        totalApparentPowMaxValue: 2300,
        totalApparentPowMaxTime: "2024-03-27 14:00:00",
        totalApparentPowMinValue: 2200,
        totalApparentPowMinTime: "2024-03-27 14:00:00",

        aApparentPowAvgValue: 2200,
        aApparentPowMaxValue: 2400,
        aApparentPowMaxTime: "2024-03-27 15:00:00",
        aApparentPowMinValue: 2100,
        aApparentPowMinTime: "2024-03-27 03:00:00",
      
        bApparentPowAvgValue: 2200,
        bApparentPowMaxValue: 2400,
        bApparentPowMaxTime: "2024-03-27 15:00:00",
        bApparentPowMinValue: 2100,
        bApparentPowMinTime: "2024-03-27 03:00:00",

        totalActivePowAvgValue: 2300,
        totalActivePowMaxValue: 2300,
        totalActivePowMaxTime: "2024-03-27 14:00:00",
        totalActivePowMinValue: 2200,
        totalActivePowMinTime: "2024-03-27 14:00:00",

        aActivePowAvgValue: 2200,
        aActivePowMaxValue: 2400,
        aActivePowMaxTime: "2024-03-27 15:00:00",
        aActivePowMinValue: 2100,
        aActivePowMinTime: "2024-03-27 03:00:00",
      
        bActivePowAvgValue: 2200,
        bActivePowMaxValue: 2400,
        bActivePowMaxTime: "2024-03-27 15:00:00",
        bActivePowMinValue: 2100,
        bActivePowMinTime: "2024-03-27 03:00:00",
      },
      {
        id: 3,
        location: "机房1-机柜1",
        cabinetId: 123,
        totalApparentPow: 2300,
        aApparentPow: 2300,
        bApparentPow: 2300,
        totalActivePow: 2000,
        aActivePow: 2100,
        bActivePow: 2100,
        createTime: "2024-03-27 18:00:00",

        totalApparentPowAvgValue: 2300,
        totalApparentPowMaxValue: 2600,
        totalApparentPowMaxTime: "2024-03-27 14:00:00",
        totalApparentPowMinValue: 2200,
        totalApparentPowMinTime: "2024-03-27 14:00:00",

        aApparentPowAvgValue: 1000,
        aApparentPowMaxValue: 1200,
        aApparentPowMaxTime: "2024-03-27 15:00:00",
        aApparentPowMinValue: 900,
        aApparentPowMinTime: "2024-03-27 03:00:00",
      
        bApparentPowAvgValue: 1100,
        bApparentPowMaxValue: 1300,
        bApparentPowMaxTime: "2024-03-27 15:00:00",
        bApparentPowMinValue: 1000,
        bApparentPowMinTime: "2024-03-27 03:00:00",

        totalActivePowAvgValue: 2200,
        totalActivePowMaxValue: 2350,
        totalActivePowMaxTime: "2024-03-27 14:00:00",
        totalActivePowMinValue: 2100,
        totalActivePowMinTime: "2024-03-27 14:00:00",

        aActivePowAvgValue: 1100,
        aActivePowMaxValue: 1200,
        aActivePowMaxTime: "2024-03-27 15:00:00",
        aActivePowMinValue: 1000,
        aActivePowMinTime: "2024-03-27 03:00:00",
      
        bActivePowAvgValue: 1100,
        bActivePowMaxValue: 1200,
        bActivePowMaxTime: "2024-03-27 15:00:00",
        bActivePowMinValue: 1130,
        bActivePowMinTime: "2024-03-27 03:00:00",
      },
      {
        id: 4,
        location: "机房1-机柜1",
        cabinetId: 123,
        totalApparentPow: 2600,
        aApparentPow: 2300,
        bApparentPow: 2300,
        totalActivePow: 2500,
        aActivePow: 2100,
        bActivePow: 2100,
        createTime: "2024-03-27 18:00:00",

        totalApparentPowAvgValue: 2300,
        totalApparentPowMaxValue: 2300,
        totalApparentPowMaxTime: "2024-03-27 14:00:00",
        totalApparentPowMinValue: 2200,
        totalApparentPowMinTime: "2024-03-27 14:00:00",

        aApparentPowAvgValue: 2200,
        aApparentPowMaxValue: 2400,
        aApparentPowMaxTime: "2024-03-27 15:00:00",
        aApparentPowMinValue: 2100,
        aApparentPowMinTime: "2024-03-27 03:00:00",
      
        bApparentPowAvgValue: 2200,
        bApparentPowMaxValue: 2400,
        bApparentPowMaxTime: "2024-03-27 15:00:00",
        bApparentPowMinValue: 2100,
        bApparentPowMinTime: "2024-03-27 03:00:00",

        totalActivePowAvgValue: 2300,
        totalActivePowMaxValue: 2300,
        totalActivePowMaxTime: "2024-03-27 14:00:00",
        totalActivePowMinValue: 2200,
        totalActivePowMinTime: "2024-03-27 14:00:00",

        aActivePowAvgValue: 2200,
        aActivePowMaxValue: 2400,
        aActivePowMaxTime: "2024-03-27 15:00:00",
        aActivePowMinValue: 2100,
        aActivePowMinTime: "2024-03-27 03:00:00",
      
        bActivePowAvgValue: 2200,
        bActivePowMaxValue: 2400,
        bActivePowMaxTime: "2024-03-27 15:00:00",
        bActivePowMinValue: 2100,
        bActivePowMinTime: "2024-03-27 03:00:00",
      },
      {
        id: 5,
        location: "机房1-机柜1",
        cabinetId: 123,
        totalApparentPow: 2300,
        aApparentPow: 2300,
        bApparentPow: 2300,
        totalActivePow: 2000,
        aActivePow: 2100,
        bActivePow: 2100,
        createTime: "2024-03-27 18:00:00",

        totalApparentPowAvgValue: 2300,
        totalApparentPowMaxValue: 2600,
        totalApparentPowMaxTime: "2024-03-27 14:00:00",
        totalApparentPowMinValue: 2200,
        totalApparentPowMinTime: "2024-03-27 14:00:00",

        aApparentPowAvgValue: 1000,
        aApparentPowMaxValue: 1200,
        aApparentPowMaxTime: "2024-03-27 15:00:00",
        aApparentPowMinValue: 900,
        aApparentPowMinTime: "2024-03-27 03:00:00",
      
        bApparentPowAvgValue: 1100,
        bApparentPowMaxValue: 1300,
        bApparentPowMaxTime: "2024-03-27 15:00:00",
        bApparentPowMinValue: 1000,
        bApparentPowMinTime: "2024-03-27 03:00:00",

        totalActivePowAvgValue: 2200,
        totalActivePowMaxValue: 2350,
        totalActivePowMaxTime: "2024-03-27 14:00:00",
        totalActivePowMinValue: 2100,
        totalActivePowMinTime: "2024-03-27 14:00:00",

        aActivePowAvgValue: 1100,
        aActivePowMaxValue: 1200,
        aActivePowMaxTime: "2024-03-27 15:00:00",
        aActivePowMinValue: 1000,
        aActivePowMinTime: "2024-03-27 03:00:00",
      
        bActivePowAvgValue: 1100,
        bActivePowMaxValue: 1200,
        bActivePowMaxTime: "2024-03-27 15:00:00",
        bActivePowMinValue: 1130,
        bActivePowMinTime: "2024-03-27 03:00:00",
      },
      ];
      totalActivePowData.value = fakeData.map((item) => item.totalActivePow);
      aActivePowData.value = fakeData.map((item) => item.aActivePow);
      bActivePowData.value = fakeData.map((item) => item.bActivePow);
      totalApparentPowData.value = fakeData.map((item) => item.totalApparentPow); 
      aApparentPowData.value = fakeData.map((item) => item.aApparentPow);
      bApparentPowData.value = fakeData.map((item) => item.bApparentPow);
      createTimeData.value = fakeData.map((item) => item.createTime);

      totalActivePowAvgValueData.value = fakeData.map((item) => item.totalActivePowAvgValue);
      totalActivePowMaxValueData.value = fakeData.map((item) => item.totalActivePowMaxValue);
      totalActivePowMinValueData.value = fakeData.map((item) => item.totalActivePowMinValue);
      aActivePowAvgValueData.value = fakeData.map((item) => item.aActivePowAvgValue);
      aActivePowMaxValueData.value = fakeData.map((item) => item.aActivePowMaxValue);
      aActivePowMinValueData.value = fakeData.map((item) => item.aActivePowMinValue);
      bActivePowAvgValueData.value = fakeData.map((item) => item.bActivePowAvgValue);
      bActivePowMaxValueData.value = fakeData.map((item) => item.bActivePowMaxValue);
      bActivePowMinValueData.value = fakeData.map((item) => item.bActivePowMinValue);

      totalApparentPowAvgValueData.value = fakeData.map((item) => item.totalApparentPowAvgValue);
      totalApparentPowMaxValueData.value = fakeData.map((item) => item.totalApparentPowMaxValue);
      totalApparentPowMinValueData.value = fakeData.map((item) => item.totalApparentPowMinValue);
      aApparentPowAvgValueData.value = fakeData.map((item) => item.aApparentPowAvgValue);
      aApparentPowMaxValueData.value = fakeData.map((item) => item.aApparentPowMaxValue);
      aApparentPowMinValueData.value = fakeData.map((item) => item.aApparentPowMinValue);
      bApparentPowAvgValueData.value = fakeData.map((item) => item.bApparentPowAvgValue);
      bApparentPowMaxValueData.value = fakeData.map((item) => item.bApparentPowMaxValue);
      bApparentPowMinValueData.value = fakeData.map((item) => item.bApparentPowMinValue);

 } finally {
   loading.value = false
 }
}

const chartContainer = ref<HTMLElement | null>(null);
let myChart = null as echarts.ECharts | null; // 显式声明 myChart 的类型
const initChart = () => {
  const instance = getCurrentInstance();
  if (chartContainer.value && instance) {
    myChart = echarts.init(chartContainer.value);
    myChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['总有功功率', '总视在功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {} }},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      yAxis: { type: 'value'},
      series: [
        {name: '总有功功率', type: 'line', data: totalActivePowData.value},
        {name: '总视在功率', type: 'line', data: totalApparentPowData.value},
        
      ],

    });
    // 将 myChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.myChart = myChart;
  }
};
// 在组件销毁时手动销毁图表
const beforeUnmount = () => {
    myChart?.dispose(); // 销毁图表实例
};
window.addEventListener('resize', function() {
    myChart?.resize(); 
});


// 监听类型颗粒度
watch([() => queryParams.type, () => queryParams.granularity], (newValues) => {
  const [newType, newGranularity] = newValues;
  if (newType == 'total'){
     if ( newGranularity == 'realtime'){
    // 销毁原有的图表实例
    beforeUnmount()
    // 创建新的图表实例
    myChart = echarts.init(document.getElementById('chartContainer'));
    // 设置新的配置对象
    if (myChart) {
    myChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['总有功功率', '总视在功率'] },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {} }},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      yAxis: { type: 'value'},
      series: [
        {name: '总有功功率', type: 'line', data: totalActivePowData.value},
        {name: '总视在功率', type: 'line', data: totalApparentPowData.value },
      ]
    });
  }
    }else{
      // 销毁原有的图表实例
      beforeUnmount()
      // 创建新的图表实例
      myChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (myChart) {
        myChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis'},
          legend: { data: ['总平均有功功率', '总最大有功功率', '总最小有功功率','总平均视在功率', '总最大视在功率', '总最小视在功率']},
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: {saveAsImage: {}}},
          xAxis: [
            {type: 'category', boundaryGap: false, data: createTimeData.value}
          ],
          yAxis: { type: 'value'},
          series: [
            { name: '总平均有功功率', type: 'line',data: totalActivePowAvgValueData.value, lineStyle: {type: 'dashed'}},
            { name: '总最大有功功率', type: 'line',data: totalActivePowMaxValueData.value, },
            { name: '总最小有功功率',type: 'line',data: totalActivePowMinValueData.value, },
            { name: '总平均视在功率',type: 'line',data:  totalApparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
            { name: '总最大视在功率', type: 'line', data: totalApparentPowMaxValueData.value, },
            { name: '总最小视在功率',type: 'line',data:  totalApparentPowMinValueData.value, },
          ]
        });
      }
    }
  }

  if (newType == 'a'){
     if ( newGranularity == 'realtime'){
      // 销毁原有的图表实例
      beforeUnmount()
      // 创建新的图表实例
      myChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (myChart) {
        myChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          title: { text: ''},
          tooltip: { trigger: 'axis' },
          legend: { data: ['a路有功功率', 'a路总视在功率'] },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {saveAsImage: {} }},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: 'a路有功功率', type: 'line', data: aActivePowData.value},
            {name: 'a路总视在功率', type: 'line', data: aApparentPowData.value },
          ]
        });
      }
      }else{
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        myChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (myChart) {
          myChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis'},
            legend: { data: ['a路平均有功功率', 'a路最大有功功率', 'a路最小有功功率','a路平均视在功率', 'a路最大视在功率', 'a路最小视在功率']},
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: {saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value}
            ],
            yAxis: { type: 'value'},
            series: [
              { name: 'a路平均有功功率', type: 'line',data: aActivePowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'a路最大有功功率', type: 'line',data: aActivePowMaxValueData.value, },
              { name: 'a路最小有功功率',type: 'line',data: aActivePowMinValueData.value, },
              { name: 'a路平均视在功率',type: 'line',data:  aApparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'a路最大视在功率', type: 'line', data: aApparentPowMaxValueData.value, },
              { name: 'a路最小视在功率',type: 'line',data:  aApparentPowMinValueData.value, },
            ]
          });
        }
      }
    }

    if (newType == 'b'){
     if ( newGranularity == 'realtime'){
      // 销毁原有的图表实例
      beforeUnmount()
      // 创建新的图表实例
      myChart = echarts.init(document.getElementById('chartContainer'));
      // 设置新的配置对象
      if (myChart) {
        myChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          title: { text: ''},
          tooltip: { trigger: 'axis' },
          legend: { data: ['b路有功功率', 'b路总视在功率'] },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {saveAsImage: {} }},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: 'b路有功功率', type: 'line', data: bActivePowData.value},
            {name: 'b路总视在功率', type: 'line', data: bApparentPowData.value },
          ]
        });
      }
      }else{
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        myChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (myChart) {
          myChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis'},
            legend: { data: ['b路平均有功功率', 'b路最大有功功率', 'b路最小有功功率','b路平均视在功率', 'b路最大视在功率', 'b路最小视在功率']},
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: {saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value}
            ],
            yAxis: { type: 'value'},
            series: [
              { name: 'b路平均有功功率', type: 'line',data: bActivePowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'b路最大有功功率', type: 'line',data: bActivePowMaxValueData.value, },
              { name: 'b路最小有功功率',type: 'line',data: bActivePowMinValueData.value, },
              { name: 'b路平均视在功率',type: 'line',data:  bApparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'b路最大视在功率', type: 'line', data: bApparentPowMaxValueData.value, },
              { name: 'b路最小视在功率',type: 'line',data:  bApparentPowMinValueData.value, },
            ]
          });
        }
      }
    }
 
    
});


/** 搜索按钮操作 */
const handleQuery = () => {
 getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
 handleQuery()
}


/** 初始化 **/
onMounted(() => {
 getList();
  initChart();

})

</script>