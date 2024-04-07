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

         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
           <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
           <el-button type="primary" plain><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
         </el-form-item>

       </el-form>


     </ContentWrap>
     <!-- 列表 -->
     <ContentWrap style="overflow: visible;">
      <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
    </ContentWrap>
    <ContentWrap style="overflow: visible;">
      <div ref="rankContainer" id="rankContainer" style="width: 70vw; height: 58vh;"></div>
    </ContentWrap>
   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { ElTree } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
defineOptions({ name: 'ECDistribution' })

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
const startEleData = ref<number[]>([]);
const startTimeData = ref<string[]>([]);
const endEleData = ref<number[]>([]);
const endTimeData = ref<string[]>([]);
const eqData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);


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
        startEle: 2000,
        startTime: "2024-03-21 00:00:00",
        endEle: 3000,
        endTime: "2024-03-22 00:00:00",
        eq: 1000,
        bill: 700,
        createTime: "2024-03-22 00:00:00",
      },
      {
        id: 2,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 3000,
        startTime: "2024-03-22 00:00:00",
        endEle: 4200,
        endTime: "2024-03-23 00:00:00",
        eq: 1200,
        bill: 700,
        createTime: "2024-03-23 00:00:00",
      },
      {
        id: 3,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 4200,
        startTime: "2024-03-23 00:00:00",
        endEle: 5100,
        endTime: "2024-03-24 00:00:00",
        eq: 900,
        bill: 700,
        createTime: "2024-03-24 00:00:00",
      },
      {
        id: 4,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 5100,
        startTime: "2024-03-24 00:00:00",
        endEle: 6500,
        endTime: "2024-03-25 00:00:00",
        eq: 1400,
        bill: 1200,
        createTime: "2024-03-25 00:00:00",
      },
      {
        id: 5,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 6500,
        startTime: "2024-03-25 00:00:00",
        endEle: 7100,
        endTime: "2024-03-26 00:00:00",
        eq: 600,
        bill: 500,
        createTime: "2024-03-26 00:00:00",
      },
    ];
  
    startEleData.value = fakeData.map((item) => item.startEle);
    startTimeData.value = fakeData.map((item) => item.startTime);
    endEleData.value = fakeData.map((item) => item.endEle);
    endTimeData.value = fakeData.map((item) => item.endTime);
    eqData.value = fakeData.map((item) => item.eq);
    createTimeData.value = fakeData.map((item) => item.createTime);

 } finally {
   loading.value = false
 }
}

const chartContainer = ref<HTMLElement | null>(null);
let myChart = null as echarts.ECharts | null; // 显式声明 myChart 的类型
const rankContainer = ref<HTMLElement | null>(null);
let myChart1 = null as echarts.ECharts | null; // 显式声明 myChart 的类型
const initChart = () => {
  const instance = getCurrentInstance();
  if (chartContainer.value && instance) {
    myChart = echarts.init(chartContainer.value);
    myChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: '总耗电量5100kWh', top: -4},
      tooltip: { trigger: 'axis' },
      legend: { data: []},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {} }},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      yAxis: { type: 'value', name: "kWh", nameTextStyle: {fontSize:"16px"},},
      series: [
        {name: '耗电量', type: 'line', data: eqData.value},
        
      ],

    });
    // 将 myChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.myChart = myChart;
  }

  if (rankContainer.value && instance) {
    myChart1 = echarts.init(rankContainer.value);
    myChart1.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: '耗电量排行榜', top: -4},
      tooltip: { trigger: 'axis',  axisPointer: { type: "shadow"} },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {} }},
      xAxis: {
          type: "value",
          axisLine: {
            show: false,
          },
          axisTick: {
            show: false,
          },
          //不显示X轴刻度线和数字
          splitLine: { show: false },
          axisLabel: { show: false },
        },
      yAxis: {
        type: "category",
        data: [ "0001",
        "0002",
        "0003",
        "0004",
        "0005",],
        //升序
        inverse: true,
        splitLine: { show: false },
        axisLine: {
          show: false,
        },
        axisTick: {
          show: false,
        },
        //key和图间距
        offset: 10,
        //动画部分
        animationDuration: 300,
        animationDurationUpdate: 300,
        //key文字大小
        nameTextStyle: {
          fontSize: 5,
        },
      },
      series: [
          {
            //柱状图自动排序，排序自动让Y轴名字跟着数据动
            realtimeSort: true,
            name: "耗电量",
            type: "bar",
            data: eqData.value,
            barWidth: 20,
            barGap: 5,
            smooth: true,
            valueAnimation: true,
            //Y轴数字显示部分
            label: {
              normal: {
                show: true,
                position: "right",
                valueAnimation: true,
                offset: [5, -2],
                textStyle: {
                  color: "#333",
                  fontSize: 16,
                },
              },
            },
            itemStyle: {
              emphasis: {
                barBorderRadius: 7,
              },
              //颜色样式部分
              normal: {
                barBorderRadius: 7,
                color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
                  { offset: 0, color: "#3977E6" },
                  { offset: 1, color: "#37BBF8" },
                ]),
              },
            },
          },
      ],
          //动画部分
        
         animationDuration: 0,
        animationDurationUpdate: 3000,
        animationEasing: "linear",
        animationEasingUpdate: "linear",
    });
    window.addEventListener('resize', function() {
        myChart1?.resize(); 
    });

    // 将 myChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.myChart1 = myChart1;
  }
};

window.addEventListener('resize', function() {
    myChart?.resize();
    myChart1?.resize();  
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