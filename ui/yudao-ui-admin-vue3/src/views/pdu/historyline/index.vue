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
       <el-tabs v-model="activeName">
        <el-tab-pane label="原始数据" name="realtimeTabPane"/>
        <el-tab-pane label="极值数据" name="extremumTabPane"/>
      </el-tabs>
       <!-- 搜索工作栏 -->
       <el-form
         class="-mb-15px"
         :model="queryParams"
         ref="queryFormRef"
         :inline="true"
         label-width="70px"
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
        <el-form-item label="级联地址" prop="cascadeAddr">
          <el-input-number
            v-model="cascadeAddr"
            :min="0"
            controls-position="right"
            :value-on-clear="0"
            class="!w-100px"
          />
        </el-form-item>
        <el-form-item label="参数类型" prop="type">
          <el-cascader
            v-model="defaultSelected"
            collapse-tags
            :options="typeSelection"
            collapse-tags-tooltip
            :show-all-levels="true"
            @change="typeCascaderChange"
            class="!w-120px"
          />
        </el-form-item>
        <el-form-item label="时间段" prop="timeRange" >
          <el-date-picker
            value-format="YYYY-MM-DD HH:mm:ss"
            v-model="queryParams.timeRange"
            type="datetimerange"
            :shortcuts="shortcuts"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :disabled-date="disabledDate"
            @change="handleDayPick"
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
      <div style="padding-top: 40px">
        <el-tabs v-model="activeName1">
          <el-tab-pane label="图表" name="myChart"/>
          <el-tab-pane label="数据" name="myData"/>
        </el-tabs>
        <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 65vh;"></div>
      </div>

    </ContentWrap>
   </el-col>
  </el-row>

</template>

<script setup lang="ts">
import { ElTree, ElMessage } from 'element-plus'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { HistoryDataApi } from '@/api/pdu/historydata'
import { formatDate } from '@/utils/formatTime'
import { get } from 'http';
import { string } from 'vue-types';
/** pdu历史曲线 */
defineOptions({ name: 'HistoryLine' })
 // tab默认显示
const activeName = ref('realtimeTabPane')
const activeName1 = ref('mychart')
const instance = getCurrentInstance();
const cascadeAddr = ref(0) // 数字类型的级联地址

const queryParams = reactive({
  pduId: 32, // 测试默认查pduid10的详情，后面要改回undefined
  lineId: undefined,
  loopId: undefined,
  outletId: undefined,
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined,
  cascadeAddr: '0',
  // 进入页面原始数据默认显示最近一小时
  timeRange: defaultHourTimeRange(1)
})

//折叠功能
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

const loading = ref(true) // 加载中

// 时间段快捷选项
const shortcuts = [
  {
    text: '最近1小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 1)
      return [start, end]
    },
  },
  {
    text: '最近12小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 12)
      return [start, end]
    },
  },
  {
    text: '最近24小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近48小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 48)
      return [start, end]
    },
  },
]

// 参数类型选项
const defaultSelected = ref(['total'])
const typeSelection = ref([
  {
    value: "total",
    label: '总'
  },
  {
    value: "line",
    label: '相',
    children: [
      { value: "1", label: 'L1' },
      { value: "2", label: 'L2' },
      { value: "3", label: 'L3' },
    ],
  },
  {
    value: "loop",
    label: '回路',
    children: [
      { value: "1", label: 'C1' },
      { value: "2", label: 'C2' },
      { value: "3", label: 'C3' },
      { value: "4", label: 'C4' },
      { value: "5", label: 'C5' },
      { value: "6", label: 'C6' },
    ],
  },
  {
    value: "outlet",
    label: '输出位',
    children: (() => {
      const outlets: { value: string; label: string; }[] = [];
      for (let i = 1; i <= 48; i++) {
        outlets.push({ value: `${i}`, label: `${i}` });
      }
      return outlets;
    })(),
  },
]) 

// 参数类型改变触发
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  switch(selected[0]){
    case 'line':
      queryParams.lineId = selected[1];
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'loop':
      queryParams.loopId = selected[1];
      queryParams.lineId = undefined;
      queryParams.outletId = undefined;
      break;
    case 'outlet':
      queryParams.outletId = selected[1];
      queryParams.loopId = undefined;
      queryParams.lineId = undefined;
      break;
    case 'total':
      queryParams.lineId = undefined;
      queryParams.loopId = undefined;
      queryParams.outletId = undefined;
      break;
  }
}

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
        volData.value = data.list.map((item) => formatNumber(item.vol_value, 1));
        curData.value = data.list.map((item) => formatNumber(item.cur_value, 2));
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
        activePowData.value = data.list.map((item) => formatNumber(item.pow_active, 3));
        apparentPowData.value = data.list.map((item) => formatNumber(item.pow_apparent, 3));

        curAvgValueData.value = data.list.map((item) => formatNumber(item.cur_avg_value, 2));
        curMaxValueData.value = data.list.map((item) => formatNumber(item.cur_max_value, 2));
        curMaxTimeData.value = data.list.map((item) => formatDate(item.cur_max_time));
        curMinValueData.value = data.list.map((item) => formatNumber(item.cur_min_value, 2));
        curMinTimeData.value = data.list.map((item) => formatDate(item.cur_min_time));
        
        volAvgValueData.value = data.list.map((item) => formatNumber(item.vol_avg_value, 1));
        volMaxValueData.value = data.list.map((item) => formatNumber(item.vol_max_value, 1));
        volMaxTimeData.value = data.list.map((item) => formatDate(item.vol_max_time));
        volMinValueData.value = data.list.map((item) => formatNumber(item.vol_min_value, 1));
        volMinTimeData.value = data.list.map((item) => formatDate(item.vol_min_time));

        activePowAvgValueData.value = data.list.map((item) => formatNumber(item.pow_active_avg_value, 3));
        activePowMaxValueData.value = data.list.map((item) => formatNumber(item.pow_active_max_value, 3));
        activePowMaxTimeData.value = data.list.map((item) => formatDate(item.pow_active_max_time));
        activePowMinValueData.value = data.list.map((item) => formatNumber(item.pow_active_min_value, 3));
        activePowMinTimeData.value = data.list.map((item) => formatDate(item.pow_active_min_time));

        apparentPowAvgValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_avg_value, 3));
        apparentPowMaxValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_max_value, 3));
        apparentPowMaxTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_max_time));
        apparentPowMinValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_min_value, 3));
        apparentPowMinTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_min_time));
  } finally {
    loading.value = false
  }
}
//初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let realtimeChart = null as echarts.ECharts | null; 
const initChart = () => {
  if (chartContainer.value && instance) {
    realtimeChart = echarts.init(chartContainer.value);
      if (realtimeChart) {
        realtimeChart.setOption({
          title: { text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总有功功率', '总视在功率'] },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '总有功功率', type: 'line', symbol: 'none', data: activePowData.value},
            {name: '总视在功率', type: 'line', symbol: 'none', data: apparentPowData.value},
          ],
          dataZoom:[{type: "inside"}],
        });
      }
    // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
  }
};
// 在组件销毁时手动销毁图表
const beforeUnmount = () => {
    realtimeChart?.dispose();
};
window.addEventListener('resize', function() {
    realtimeChart?.resize(); 
});

watch( ()=>activeName.value, async(newActiveName)=>{
  if ( newActiveName == 'realtimeTabPane' ){
    queryParams.timeRange = defaultHourTimeRange(1)
  }else{
    queryParams.timeRange = defaultHourTimeRange(24)
  }
});

// 监听类型颗粒度
watch([() => activeName.value, () => queryParams.type], async (newValues) => {
    const [newActiveName, newType] = newValues;
    console.log(newType)
    // 处理参数变化
    if (newType == 'total'){
      if ( newActiveName == 'realtimeTabPane'){
        // 颗粒度变回实时 查询数据
        queryParams.granularity = 'realtime'
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        realtimeChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (realtimeChart) {
        realtimeChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          title: { text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总有功功率', '总视在功率'] },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: { dataView:{}, restore:{},saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '总有功功率', type: 'line', symbol: 'none', data: activePowData.value},
            {name: '总视在功率', type: 'line', symbol: 'none', data: apparentPowData.value},
          ],
          dataZoom:[{type: "inside"}],
        });
      }
      }else{
        // 默认查询一天的极值数据    
        queryParams.granularity = 'hour'
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        realtimeChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (realtimeChart) {
          realtimeChart.setOption({     
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                      selected: { 平均有功功率: true, 最大有功功率: true, 最小有功功率: true, 
                      平均视在功率: false, 最大视在功率: false, 最小视在功率: false, }
            },
            grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
            toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value}
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value, },
              { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value, },
              { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value, },
              { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value, },
            ],
            dataZoom:[{type: "inside"}],
          });
        }
      }
    }
    
    if (newType == 'line'){
      if ( newActiveName == 'realtimeTabPane'){
          // 颗粒度变回实时 查询数据
          queryParams.granularity = 'realtime'
          await getList();
          // 销毁原有的图表实例
          beforeUnmount()
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption({
              // 这里设置 Echarts 的配置项和数据
              title: { text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['电压', '电流', '有功功率', '视在功率'],
                        selected: {  "电压": true, "电流": false, "有功功率": false, "视在功率": false }},
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '电压', type: 'line', symbol: 'none', data: volData.value},
                {name: '电流', type: 'line', symbol: 'none', data: curData.value},
                {name: '有功功率', type: 'line', symbol: 'none', data: activePowData.value},
                {name: '视在功率', type: 'line', symbol: 'none', data: apparentPowData.value},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          setupLegendListener(realtimeChart);
      }else{
        // 默认查询一天的极值数据    
        queryParams.granularity = 'hour'
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        realtimeChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (realtimeChart) {
          realtimeChart.setOption( {
          title: {text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
                            '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                    selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均电压: false, 最大电压: false, 最小电压: false, 
                                平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                  },
          grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
          toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
          xAxis: [
            {type: 'category', boundaryGap: false, data: createTimeData.value},
          ],
          yAxis: { type: 'value'},
          series: [
            { name: '平均电流', type: 'line', symbol: 'none', data: curAvgValueData.value, lineStyle: {type: 'dashed'}},
            { name: '最大电流', type: 'line', symbol: 'none', data: curMaxValueData.value, },
            { name: '最小电流', type: 'line', symbol: 'none', data: curMinValueData.value, },
            { name: '平均电压', type: 'line', symbol: 'none', data: volAvgValueData.value, lineStyle: {type: 'dashed'}},
            { name: '最大电压', type: 'line', symbol: 'none', data: volMaxValueData.value, },
            { name: '最小电压', type: 'line', symbol: 'none', data: volMinValueData.value, },

            { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value, lineStyle: {type: 'dashed'}},
            { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value, },
            { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value, },
            { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
            { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value,},
            { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value,},
          ],
          dataZoom:[{type: "inside"}],
          });
        }
        // 图例切换监听
        setupLegendListener1(realtimeChart);

      }
    }

    if (newType == 'loop'){
      if ( newActiveName == 'realtimeTabPane'){
          // 颗粒度变回实时 查询数据
          queryParams.granularity = 'realtime'
          await getList();
          // 销毁原有的图表实例
          beforeUnmount()
            // 创建新的图表实例
            realtimeChart = echarts.init(document.getElementById('chartContainer'));
            // 设置新的配置对象
            if (realtimeChart) {
            realtimeChart.setOption({
              // 这里设置 Echarts 的配置项和数据
              title: { text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['电压', '电流', '有功功率', '视在功率'],
                        selected: {  "电压": true, "电流": false, "有功功率": false, "视在功率": false }},
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '电压', type: 'line', symbol: 'none', data: volData.value},
                {name: '电流', type: 'line', symbol: 'none', data: curData.value},
                {name: '有功功率', type: 'line', symbol: 'none', data: activePowData.value},
                {name: '视在功率', type: 'line', symbol: 'none', data: apparentPowData.value},
              ],
              dataZoom:[{type: "inside"}],
          });
        }
        // 图例切换监听
        setupLegendListener(realtimeChart);
      }else{
        // 默认查询一天的极值数据    
        queryParams.granularity = 'hour'
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        realtimeChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (realtimeChart) {
          realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
                              '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                      selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均电压: false, 最大电压: false, 最小电压: false, 
                                  平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value},
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流', type: 'line', symbol: 'none', data: curAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大电流', type: 'line', symbol: 'none', data: curMaxValueData.value},
              { name: '最小电流', type: 'line', symbol: 'none', data: curMinValueData.value},
              { name: '平均电压', type: 'line', symbol: 'none', data: volAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大电压', type: 'line', symbol: 'none', data: volMaxValueData.value},
              { name: '最小电压', type: 'line', symbol: 'none', data: volMinValueData.value},

              { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value, lineStyle: {type: 'dashed'} },
              { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value},
              { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value},
              { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value},
              { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value},
            ],
            dataZoom:[{type: "inside"}],
          });
        }
        // 图例切换监听
        setupLegendListener1(realtimeChart);
      }
    }

    if (newType == 'outlet'){
      if ( newActiveName == 'realtimeTabPane'){
        // 颗粒度变回实时 查询数据
        queryParams.granularity = 'realtime'
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        realtimeChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (realtimeChart) {
        realtimeChart.setOption({
          // 这里设置 Echarts 的配置项和数据
          title: { text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: [ '电流', '有功功率', '视在功率'],
                selected: { "电流": true, "有功功率": false, "视在功率": false }},
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '电流', type: 'line', symbol: 'none', data: curData.value },
            {name: '有功功率', type: 'line', symbol: 'none', data: activePowData.value },
            {name: '视在功率', type: 'line', symbol: 'none', data: apparentPowData.value },
          ],
          dataZoom:[{type: "inside"}],
        });
        }
        // 图例切换监听
        setupLegendListener(realtimeChart);
      }else{
        // 默认查询一天的极值数据    
        queryParams.granularity = 'hour'
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        // 创建新的图表实例
        realtimeChart = echarts.init(document.getElementById('chartContainer'));
        // 设置新的配置对象
        if (realtimeChart) {
          realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['平均电流', '最大电流', '最小电流', '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                      selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: { dataView:{}, restore:{}, saveAsImage: {}}},
            xAxis: [{type: 'category', boundaryGap: false, data: createTimeData.value},],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流', type: 'line', symbol: 'none', data: curAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大电流', type: 'line', symbol: 'none', data: curMaxValueData.value},
              { name: '最小电流', type: 'line', symbol: 'none', data: curMinValueData.value},
              { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value},
              { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value},
              { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value},
              { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value},
            ],
            dataZoom:[{type: "inside"}],
          });
        }
        // 图例切换监听
        setupLegendListener1(realtimeChart);
      }
    }
});

// 实时图例切换函数
function setupLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var legendData = realtimeChart?.getOption().legend[0]?.data;

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

      realtimeChart?.setOption({
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

    realtimeChart?.setOption({
      legend: {
        data: ['电压', '电流', '有功功率', '视在功率'],
        selected: optionsToUpdate
      },
    });
  });
}

function customTooltipFormatter(params: any[]) {
  var tooltipContent = params[0].name + '<br/>'; // X 轴数值
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '总有功功率':
      case '有功功率':
      case '平均有功功率':
      case '最大有功功率':
      case '最小有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW <br/>';
        break;
      case '总视在功率':
      case '视在功率':
      case '平均视在功率':
      case '最大视在功率':
      case '最小视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA <br/>';
        break;
      case '电流':
      case '平均电流':
      case '最大电流':
      case '最小电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A <br/>'; 
        break;
      case '电压':
      case '平均电压':
      case '最大电压':
      case '最小电压':
      tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V <br/>'; 
      break;
    }
    
  });
  return tooltipContent;
}

// 小时、天 图例切换函数
function setupLegendListener1(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var legendData = realtimeChart?.getOption().legend[0]?.data;
    // var legendSelected = realtimeChart?.getOption().legend[0]?.selected;
    // 检查图例是否有电压，没有就是输出位
    if (!legendData.includes('平均电压')) {
      var optionsToUpdate = {};
      switch (legendName) {
      case '平均电流':
      case '最大电流':
      case '最小电流':

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

    realtimeChart?.setOption({
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

    realtimeChart?.setOption({
      legend: {
        data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
               '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
        selected: optionsToUpdate
      },
    });
  });
}

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return value.toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

// 原始数据默认查询的时间范围
function defaultHourTimeRange(hour: number){
  // 先获取本地时区偏移量（以分钟为单位，需要转换为毫秒）
  var timezoneOffset = new Date().getTimezoneOffset() * 60 * 1000
  // 计算当前时间和1小时前的时间，并考虑时区偏移量
  var end = new Date(new Date().getTime() - timezoneOffset);
  var start = new Date(end.getTime() - 60 * 60 * 1000 * hour);
  // 格式化时间并返回
  return [start.toISOString().slice(0, 19).replace('T', ' '), end.toISOString().slice(0, 19).replace('T', ' ')]
}

const disabledDate = (date) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

const handleDayPick = () => {
  // 获取选择的开始日期和结束日期
  const startDate = new Date(queryParams.timeRange[0]);
  const endDate = new Date(queryParams.timeRange[1]);
  // 计算两个日期之间的天数差
  const diffTime = Math.abs(endDate.getTime() - startDate.getTime()); 
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  // 如果天数差超过48小时，则重置选择的日期
  if (diffDays > 2) {
    queryParams.timeRange = [];
    ElMessage({
      message: '时间选择不超过48小时',
      type: 'warning',
    })
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  console.log(queryParams.timeRange)
  // IP地址的正则表达式
  const ipRegex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  if (queryParams.ipAddr == null || queryParams.ipAddr == '' || ipRegex.test(queryParams.ipAddr)){
    queryParams.cascadeAddr = cascadeAddr.value.toString();
    getList()
  }else{
    ElMessage.error('IP地址格式有误,请重新输入！')
  }
}

/** 重置按钮操作 */
const resetQuery = () => {
 handleQuery()
}

/** 初始化 **/
onMounted( async () => {
  await getList();
  initChart();
})

</script>