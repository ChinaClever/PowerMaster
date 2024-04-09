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
  type: 'total',
  granularity: 'day',
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
const loading = ref(true) 

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

function getRandomInt(): number {
    return Math.floor(Math.random() * (250 - 180 + 1)) + 180;
}
const cascaderChange = (select) => {
  if (select[0] === 'outlet'){
    let data : number[] = [];
    for (let i = 0; i < 10; i++) {
      data.push(getRandomInt());
    }
    console.log(data)
    myChart?.setOption({
      title: { text: select[1]+'总耗电量200kWh', top: -4},
      series: [{ data: data }]
    });
  }else{
    myChart?.setOption({
      title: { text: 'PDU1总耗电量2000kWh', top: -4},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      series: [{name: '耗电量', type: 'line', data: eqData.value}],
    });
  }

}

// 处理折线图数据
const startEleData = ref<number[]>([]);
const startTimeData = ref<string[]>([]);
const endEleData = ref<number[]>([]);
const endTimeData = ref<string[]>([]);
const eqData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);

// 生成假数据
const getList = () => {
loading.value = true
 try {
    const fakeData = [
      {
        id: 1,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 2000,
        startTime: "2024-03-21",
        endEle: 3000,
        endTime: "2024-03-22",
        eq: 1000,
        bill: 700,
        createTime: "2024-03-22",
      },
      {
        id: 2,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 3000,
        startTime: "2024-03-22",
        endEle: 4200,
        endTime: "2024-03-23",
        eq: 1200,
        bill: 700,
        createTime: "2024-03-23",
      },
      {
        id: 3,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 4200,
        startTime: "2024-03-23",
        endEle: 5100,
        endTime: "2024-03-24",
        eq: 900,
        bill: 700,
        createTime: "2024-03-24",
      },
      {
        id: 4,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 5100,
        startTime: "2024-03-24",
        endEle: 6500,
        endTime: "2024-03-25",
        eq: 1400,
        bill: 1200,
        createTime: "2024-03-25",
      },
      {
        id: 5,
        location: "机房1-机柜1",
        cabinetId: 123,
        startEle: 6500,
        startTime: "2024-03-25",
        endEle: 7100,
        endTime: "2024-03-26",
        eq: 600,
        bill: 500,
        createTime: "2024-03-26",
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
let myChart = null as echarts.ECharts | null; 
const rankContainer = ref<HTMLElement | null>(null);
let rankChart = null as echarts.ECharts | null; 
const initChart = () => {
  const instance = getCurrentInstance();
  if (chartContainer.value && instance) {
    myChart = echarts.init(chartContainer.value);
    myChart.setOption({
      title: { text: 'PDU1总耗电量2000kWh', top: -4},
      tooltip: { trigger: 'axis' },
      legend: { data: []},
      grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true},
      toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
      xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
      yAxis: { type: 'value', name: "kWh"},
      series: [{name: '耗电量', type: 'line', data: eqData.value}],

    });
    instance.appContext.config.globalProperties.myChart = myChart;
  }

  if (rankContainer.value && instance) {
    rankChart = echarts.init(rankContainer.value);
    rankChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: '输出位耗电量排行', top: -4},
      tooltip: { trigger: 'axis',  axisPointer: { type: "shadow"} },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
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
        data: [ "输出位1", "输出位2", "输出位3", "输出位4", "输出位5", "输出位6", "输出位7", "输出位8", "输出位9", "输出位10"],
        //升序
        inverse: true,
        splitLine: { show: false },
        axisLine: {
          show: false,
        },
        axisLabel: { fontSize: 16 },
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
          fontSize: 15,
        },
      },
      series: [
        {
          //柱状图自动排序，排序自动让Y轴名字跟着数据动
          realtimeSort: true,
          name: "耗电量",
          type: "bar",
          data:  [ "200", "100", "135", "154", "120", "245", "200", "212", "191", "200"],
          barWidth: 20,
          barGap: 5,
          smooth: true,
          valueAnimation: true,
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
              // formatter: '{value}kWh'
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
    instance.appContext.config.globalProperties.rankChart = rankChart;
  }
};

watch(() => queryParams.granularity, (newValues) => {
  const newGranularity = newValues;
  if ( newGranularity == 'day'){
    myChart?.setOption({
      xAxis: {data:createTimeData.value},
      series: [{data: eqData.value}],
    });
    rankChart?.setOption({
      series: [{data: [ "200", "100", "135", "154", "120", "245", "200", "212", "191", "200"]}],
    })

  }
  if ( newGranularity == 'week'){
    myChart?.setOption({
      xAxis: {data:['2024年3月第一周', '2024年3月第二周', '2024年3月第三周', '2024年3月第四周',]},
      series: [{data:['6000', '6500', '5500', '6210'] }],
    });
    rankChart?.setOption({
      series: [{data: [ "2000", "1000", "1305", "1540", "1200", "1245", "1200", "1212", "1191", "1200"]}],
    })
  }
  if ( newGranularity == 'month'){
    myChart?.setOption({
      xAxis: {type: 'category', boundaryGap: false, data:[
        '2024年1月',
        '2024年2月',
        '2024年3月',
        '2024年4月',
        '2024年5月',
        '2024年6月',]},
      series: [{data:['24000', '25000', '27000', '24500', '25000', '26000'] }],
    });
    rankChart?.setOption({
      series: [{data: [ "6000", "4500", "5305", "5540", "5200", "5245", "5200", "5212", "5191", "5200"]}],
    })

  }


});

window.addEventListener('resize', function() {
  myChart?.resize();
  rankChart?.resize();  
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