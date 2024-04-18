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
         </el-form-item>
         <el-form-item label="参数类型" prop="type">
           <el-select
             v-model="queryParams.type"
             class="!w-120px"
           >
             <el-option label="总" value="total" />
             <el-option label="相" value="line" />
             <el-option label="回路" value="loop" />
             <el-option label="输出位" value="outlet" />
           </el-select>
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
          <el-form-item label="时间段" prop="timeRange">
            <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="queryParams.timeRange"
            type="datetimerange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
          />
          </el-form-item>

        <div style="float:right">
         <el-form-item >
           <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
           <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
           <el-button type="primary" plain><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
         </el-form-item>
         </div>
       </el-form>
     </ContentWrap>
     <ContentWrap style="overflow: visible;">
      <el-tabs v-model="activeName">
        <el-tab-pane label="日原始数据" name="first">
          <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
        </el-tab-pane>
        <el-tab-pane label="逐日极值数据" name="second">Config</el-tab-pane>
      </el-tabs>

    </ContentWrap>
   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { ElTree } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { formatDate } from '@/utils/formatTime'
/** pdu历史曲线 */
defineOptions({ name: 'HistoryLine' })
const activeName = ref('first') // tabs显示第一个
const queryParams = reactive({
  id: 12, // 测试默认查pduid10的详情，后面要改回undefined
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined,
  timeRange: undefined,
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
// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近半年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
]
// 处理折线图数据
const volData = ref<number[]>([]);
const curData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);
const activePowData = ref<number[]>([]);
const apparentPowData = ref<number[]>([]);

const curAvgValueData = ref<number[]>([]);
const curMaxValueData = ref<number[]>([]);
const curMaxTimeData = ref<string[]>([]);
const curMinValueData = ref<number[]>([]);
const curMinTimeData = ref<string[]>([]);

const volAvgValueData = ref<number[]>([]);
const volMaxValueData = ref<number[]>([]);
const volMaxTimeData = ref<string[]>([]);
const volMinValueData = ref<number[]>([]);
const volMinTimeData = ref<string[]>([]);

const activePowAvgValueData = ref<number[]>([]);
const activePowMaxValueData = ref<number[]>([]);
const activePowMaxTimeData = ref<string[]>([]);
const activePowMinValueData = ref<number[]>([]);
const activePowMinTimeData = ref<string[]>([]);

const apparentPowAvgValueData = ref<number[]>([]);
const apparentPowMaxValueData = ref<number[]>([]);
const apparentPowMaxTimeData = ref<string[]>([]);
const apparentPowMinValueData = ref<number[]>([]);
const apparentPowMinTimeData = ref<string[]>([]);
/** 查询列表 */
const getList = async () => {
loading.value = true
 try {
      const data = await HistoryDataApi.getHistoryDataDetails(queryParams)
      volData.value = data.list.map((item) => item.vol_value);
      curData.value = data.list.map((item) => item.cur_value);
      createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      activePowData.value = data.list.map((item) => item.pow_active);
      apparentPowData.value = data.list.map((item) => item.pow_apparent);

      curAvgValueData.value = data.list.map((item) => item.cur_avg_value);
      curMaxValueData.value = data.list.map((item) => item.cur_max_value);
      curMaxTimeData.value = data.list.map((item) => formatDate(item.cur_max_time));
      curMinValueData.value = data.list.map((item) => item.cur_min_value);
      curMinTimeData.value = data.list.map((item) => formatDate(item.cur_min_time));
      
      volAvgValueData.value = data.list.map((item) => item.vol_avg_value);
      volMaxValueData.value = data.list.map((item) => item.vol_max_value);
      volMaxTimeData.value = data.list.map((item) => formatDate(item.vol_max_time));
      volMinValueData.value = data.list.map((item) => item.vol_min_value);
      volMinTimeData.value = data.list.map((item) => formatDate(item.vol_min_time));

      activePowAvgValueData.value = data.list.map((item) => item.pow_active_avg_value);
      activePowMaxValueData.value = data.list.map((item) => item.pow_active_max_value);
      activePowMaxTimeData.value = data.list.map((item) => formatDate(item.pow_active_max_time));
      activePowMinValueData.value = data.list.map((item) => item.pow_active_min_value);
      activePowMinTimeData.value = data.list.map((item) => formatDate(item.pow_active_min_time));

      apparentPowAvgValueData.value = data.list.map((item) => item.pow_apparent_avg_value);
      apparentPowMaxValueData.value = data.list.map((item) => item.pow_apparent_max_value);
      apparentPowMaxTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_max_time));
      apparentPowMinValueData.value = data.list.map((item) => item.pow_apparent_min_value);
      apparentPowMinTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_min_time));
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
      toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
      xAxis: {type: 'category', boundaryGap: false, data: createTimeData.value},
      yAxis: { type: 'value'},
      series: [
        {name: '总有功功率', type: 'line', data: activePowData.value},
        {name: '总视在功率', type: 'line', data: apparentPowData.value},
    
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
    // 处理参数变化
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
          toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '总有功功率', type: 'line', data: activePowData.value},
            {name: '总视在功率', type: 'line', data: apparentPowData.value},
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
            legend: { data: ['平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率']},
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value}
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均有功功率', type: 'line',data: activePowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大有功功率', type: 'line',data: activePowMaxValueData.value, },
              { name: '最小有功功率',type: 'line',data: activePowMinValueData.value, },
              { name: '平均视在功率',type: 'line',data:  apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大视在功率', type: 'line', data: apparentPowMaxValueData.value, },
              { name: '最小视在功率',type: 'line',data:  apparentPowMinValueData.value, },
            ]
          });
        }
      }
    }
    
    if (newType == 'line'){
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
              legend: { data: ['电压', '电流', '有功功率', '视在功率'],
                        selected: {  "电压": true, "电流": false, "有功功率": false, "视在功率": false }},
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '电压', type: 'line', data: volData.value, lineStyle: {type: 'dashed'} },
                {name: '电流', type: 'line', data: curData.value, lineStyle: {type: 'dashed'} },
                {name: '有功功率', type: 'line', data: activePowData.value, lineStyle: {type: 'dashed'} },
                {name: '视在功率', type: 'line', data: apparentPowData.value, lineStyle: {type: 'dashed'} },
              ]
            });
          }
          // 图例切换监听
          setupLegendListener(myChart);
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
              legend: { data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
                                '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                        selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均电压: false, 最大电压: false, 最小电压: false, 
                                    平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                      },
              grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
              toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
              xAxis: [
                {type: 'category', boundaryGap: false, data: createTimeData.value},
              ],
              yAxis: { type: 'value'},
              series: [
                { name: '平均电流', type:'line', data: curAvgValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最大电流', type:'line', data: curMaxValueData.value,},
                { name: '最小电流', type:'line', data: curMinValueData.value, },
                { name: '平均电压', type:'line', data: volAvgValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最大电压', type:'line', data: volMaxValueData.value, },
                { name: '最小电压', type:'line', data: volMinValueData.value,},

                { name: '平均有功功率', type: 'line',data: activePowAvgValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最大有功功率', type: 'line',data: activePowMaxValueData.value, },
                { name: '最小有功功率',type: 'line',data: activePowMinValueData.value, },
                { name: '平均视在功率',type: 'line',data:  apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最大视在功率', type: 'line', data: apparentPowMaxValueData.value,},
                { name: '最小视在功率',type: 'line',data:  apparentPowMinValueData.value,},
              ]
          });
        }
        // 图例切换监听
        setupLegendListener1(myChart);

      }
    }

    if (newType == 'loop'){
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
              legend: { data: ['电压', '电流', '有功功率', '视在功率'],
                        selected: {  "电压": true, "电流": false, "有功功率": false, "视在功率": false }},
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '电压', type: 'line', data: volData.value},
                {name: '电流', type: 'line', data: curData.value},
                {name: '有功功率', type: 'line', data: activePowData.value},
                {name: '视在功率', type: 'line', data: apparentPowData.value},
              ]
          });
        }
        // 图例切换监听
        setupLegendListener(myChart);
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
            legend: { data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
                              '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                      selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均电压: false, 最大电压: false, 最小电压: false, 
                                  平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value},
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流', type:'line', data: curAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大电流', type:'line', data: curMaxValueData.value},
              { name: '最小电流', type:'line', data: curMinValueData.value},
              { name: '平均电压', type:'line', data: volAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大电压', type:'line', data: volMaxValueData.value},
              { name: '最小电压', type:'line', data: volMinValueData.value},

              { name: '平均有功功率', type: 'line',data: activePowAvgValueData.value, lineStyle: {type: 'dashed'} },
              { name: '最大有功功率', type: 'line',data: activePowMaxValueData.value},
              { name: '最小有功功率',type: 'line',data: activePowMinValueData.value},
              { name: '平均视在功率',type: 'line',data:  apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大视在功率', type: 'line', data: apparentPowMaxValueData.value},
              { name: '最小视在功率',type: 'line',data:  apparentPowMinValueData.value},
            ]
          });
        }
        // 图例切换监听
        setupLegendListener1(myChart);
      }
    }

    if (newType == 'outlet'){
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
          legend: { data: [ '电流', '有功功率', '视在功率'],
                selected: { "电流": true, "有功功率": false, "视在功率": false }},
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '电流', type: 'line', data: curData.value },
            {name: '有功功率', type: 'line', data: activePowData.value },
            {name: '视在功率', type: 'line', data: apparentPowData.value },
          ]
        
        });
        }
        // 图例切换监听
        setupLegendListener(myChart);
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
            legend: { data: ['平均电流', '最大电流', '最小电流', '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                      selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { dataView:{}, dataZoom:{}, restore:{},saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value},
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流', type:'line', data: curAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大电流', type:'line', data: curMaxValueData.value},
              { name: '最小电流', type:'line', data: curMinValueData.value},

              { name: '平均有功功率', type: 'line',data: activePowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大有功功率', type: 'line',data: activePowMaxValueData.value},
              { name: '最小有功功率',type: 'line',data: activePowMinValueData.value},
              { name: '平均视在功率',type: 'line',data:  apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大视在功率', type: 'line', data: apparentPowMaxValueData.value},
              { name: '最小视在功率',type: 'line',data:  apparentPowMinValueData.value},
            ]
          });
        }
        // 图例切换监听
        setupLegendListener1(myChart);
      }
    }
});

// 实时图例切换函数
function setupLegendListener(myChart) {
  myChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var legendData = myChart?.getOption().legend[0]?.data;

    // 检查图例是否有电压，没有就是输出位
    if (!legendData.includes('电压')) {
      var optionsToUpdate = {};
      switch (legendName) {
        case '电流':
          optionsToUpdate = { "电流": true, "有功功率": false, "视在功率": false };
          break;
        case '有功功率':
          if (params.selected[legendName]){
            optionsToUpdate = {"电流": false, "有功功率": true};
          }
          break;
        case '视在功率':
        if (params.selected[legendName]){
          optionsToUpdate = {"电流": false, "视在功率": true };
        }
          break;
        default:
          break;
      }

      myChart?.setOption({
        legend: {
          data: ['电流', '有功功率', '视在功率'],
          selected: optionsToUpdate
        },
      });

      return; 
    }

    var optionsToUpdate = {};
    switch (legendName) {
      case '电压':
        optionsToUpdate = { "电压": true, "电流": false, "有功功率": false, "视在功率": false };
        break;

      case '电流':
        optionsToUpdate = { "电压": false, "电流": true, "有功功率": false, "视在功率": false };
        break;

      case '有功功率':
        if (params.selected[legendName]){
          optionsToUpdate = { "电压": false, "电流": false, "有功功率": true};
        }
        break;

      case '视在功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "电压": false, "电流": false, "视在功率": true };
      }
        break;

      case '平均电流':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电压": false, "最大电压": false, "最小电压": false, "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                            "平均有功功率": false, "最大有功功率": false, "最小有功功率": false, };
      }
        break;

        
      default:
        break;
    }

    myChart?.setOption({
      legend: {
        data: ['电压', '电流', '有功功率', '视在功率'],
        selected: optionsToUpdate
      },
    });
  });
}

// 小时、天图例切换函数
function setupLegendListener1(myChart) {
  myChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var legendData = myChart?.getOption().legend[0]?.data;
    var legendSelected = myChart?.getOption().legend[0]?.selected;
    // 检查图例是否有电压，没有就是输出位
    if (!legendData.includes('平均电压')) {
      var optionsToUpdate = {};
      switch (legendName) {
      case '平均电流':
      case '最大电流':
      case '最小电流':
        console.log(legendSelected[legendName])
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电流": true, "最大电流": true, "最小电流": true,  "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                             "平均有功功率": false, "最大有功功率": false, "最小有功功率": false, };
      }
        break;

      case '平均视在功率':
      case '最大视在功率':
      case '最小视在功率':
      if (params.selected[legendName]){
        optionsToUpdate = {  "平均视在功率": true, "最大视在功率": true, "最小视在功率": true, "平均电流": false, "最大电流": false, "最小电流": false };
      }
        break;

      case '平均有功功率':
      case '最大有功功率':
      case '最小有功功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均有功功率": true, "最大有功功率": true, "最小有功功率": true,  "平均电流": false, "最大电流": false, "最小电流": false,};
      }
        break;


      default:
        break;
    }

    myChart?.setOption({
      legend: {
        data: ['平均电流', '最大电流', '最小电流','平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
        selected: optionsToUpdate
      },
    });

      return; 
    }


    var optionsToUpdate = {};
    switch (legendName) {
      case '平均电流':
      case '最大电流':
      case '最小电流':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电流": true, "最大电流": true, "最小电流": true, "平均电压": false, "最大电压": false, "最小电压": false, 
            "平均视在功率": false, "最大视在功率": false, "最小视在功率": false, "平均有功功率": false, "最大有功功率": false, "最小有功功率": false, };
      }
        break;

      case '平均电压':
      case '最大电压':
      case '最小电压':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电压": true, "最大电压": true, "最小电压": true , "平均电流": false, "最大电流": false, "最小电流": false, 
        "平均视在功率": false, "最大视在功率": false, "最小视在功率": false, "平均有功功率": false, "最大有功功率": false, "最小有功功率": false, };
      }
        break;

      case '平均视在功率':
      case '最大视在功率':
      case '最小视在功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均视在功率": true, "最大视在功率": true, "最小视在功率": true, "平均电流": false, "最大电流": false, "最小电流": false, 
        "平均电压": false, "最大电压": false, "最小电压": false };
      }
        break;

      case '平均有功功率':
      case '最大有功功率':
      case '最小有功功率':
      if (params.selected[legendName]){
        optionsToUpdate = {"平均有功功率": true, "最大有功功率": true, "最小有功功率": true, "平均电流": false, "最大电流": false, "最小电流": false,
         "平均电压": false, "最大电压": false, "最小电压": false};
      }
        break;


      default:
        break;
    }

    myChart?.setOption({
      legend: {
        data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
               '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
        selected: optionsToUpdate
      },
    });
  });
}

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