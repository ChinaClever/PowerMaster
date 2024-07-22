<template>
  <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="母线始端箱电力分析" :showCheckbox="false">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
        <div class="carousel-container">
          <!-- <el-carousel :interval="2500" motion-blur height="150px" arrow="never" trigger="click">
            <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
              <img width="auto" height="auto" :src="item.imgUrl" alt="" class="carousel-image" />
            </el-carousel-item>
          </el-carousel> -->
        </div> 
        <div class="nav_header">
          <span v-if="nowAddress">{{nowAddress}}</span>
          <span v-if="nowLocation">( {{nowLocation}} ) </span>
          <br/>
          <template v-if="queryParams.granularity == 'realtime' && queryParams.type == 'total'">
            <span>{{queryParams.timeRange[0]}}</span>
            <span>至</span>
            <span>{{queryParams.timeRange[1]}}</span>
          </template>
          <br/>
        </div>
        <div class="nav_content" v-if="queryParams.granularity == 'realtime' && queryParams.type == 'total'">
        <el-descriptions title="" direction="vertical" :column="1" border >
          <el-descriptions-item label="有功功率最大值 | 发生时间">
            <span>{{ formatNumber(maxActivePowDataTemp, 3) }} kWh</span> <br/>
            <span v-if="maxActivePowDataTimeTemp">{{ maxActivePowDataTimeTemp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="有功功率最小值 | 发生时间">
            <span>{{ formatNumber(minActivePowDataTemp, 3) }} kWh</span><br/>
            <span v-if="minActivePowDataTimeTemp">{{ minActivePowDataTimeTemp }}</span>
          </el-descriptions-item>
        </el-descriptions>
        </div>
      </div>
    </template>
    <template #ActionBar>
      <el-tabs v-model="activeName">
        <el-tab-pane label="原始数据" name="realtimeTabPane"/>
        <el-tab-pane label="小时极值数据" name="hourExtremumTabPane"/>
        <el-tab-pane label="天极值数据" name="dayExtremumTabPane"/>
      </el-tabs>
      <!-- 搜索工作栏 -->
      <el-form
        class="-mb-5px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="auto"
      >
      <el-form-item label="参数类型" prop="type">
        <el-cascader
          v-model="defaultSelected"
          collapse-tags
          :options="typeSelection"
          collapse-tags-tooltip
          :show-all-levels="true"
          @change="typeCascaderChange"
          class="!w-110px"
        />
      </el-form-item>
      <el-form-item label="时间段" prop="timeRange" >
        <el-date-picker
          value-format="YYYY-MM-DD HH:mm:ss"
          v-model="queryParams.timeRange"
          type="datetimerange"
          :shortcuts="activeName === 'realtimeTabPane' ? shortcuts : (activeName === 'hourExtremumTabPane' ? shortcuts1 : (activeName === 'dayExtremumTabPane' ? shortcuts2 : []))"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :disabled-date="disabledDate"

          class="!w-350px"
        />
                    <!-- @change="handleDayPick" -->
      </el-form-item>

        <el-form-item >
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>

      </el-form>
    </template>
    <template #Content>
      <div v-loading="loading">
        <el-tabs v-model="activeName1">
          <el-tab-pane label="图表" name="myChart">
            <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 65vh;"></div>
          </el-tab-pane>
          <el-tab-pane label="数据" name="myData">
            <div style="height: 67vh;">
            <el-table  
              border
              :data="tableData"
              style="height: 67vh; width: 99.97%;--el-table-border-color: none;border-right: 1px #143275 solid;border-left: 1px #143275 solid;border-bottom: 1px #143275 solid;"
              :highlight-current-row="false"
              :header-cell-style="{ backgroundColor: '#143275', color: '#ffffff', fontSize: '18px', textAlign: 'center', borderLeft: '0.5px #ffffff solid', borderBottom: '1px #ffffff solid' }"
              :cell-style="{ color: '#000000', fontSize: '16px', textAlign: 'center', borderBottom: '0.5px #143275 solid', borderLeft: '0.5px #143275 solid' }"
              :row-style="{ color: '#fff', fontSize: '14px', textAlign: 'center', }"
              empty-text="暂无数据" max-height="818">
              <el-table-column prop="create_time" label="记录时间" />
              <!-- 动态生成表头 -->
              <template v-for="item in headerData" :key="item.name">
                <el-table-column v-if="item.name === '最大有功功率'" label="有功功率最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="activePowMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小有功功率'" label="有功功率最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="activePowMinTimeData" label="发生时间"/>
                </el-table-column>


                <el-table-column v-else-if="item.name === '最大无功功率'" label="无功功率最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="reactivePowMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小无功功率'" label="无功功率最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="reactivePowMinTimeData" label="发生时间"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大剩余电流'" label="剩余电流最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="curResidualMaxTime" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小剩余电流'" label="剩余电流最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="curResidualMinTime" label="发生时间"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大零线电流'" label="零线电流最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="curZeroMaxTime" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最小零线电流'" label="零线电流最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="curZeroMinTime" label="发生时间"/>
                </el-table-column>


                <el-table-column v-else-if="item.name === '最大视在功率'" label="视在功率最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="apparentPowMaxTimeData" label="发生时间"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小视在功率'" label="视在功率最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="apparentPowMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最大电压'" label="电压最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="volMaxTimeData" label="发生时间"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小电压'" label="电压最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="volMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最大线电压'" label="线电压最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="volLineMaxTimeData" label="发生时间"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小线电压'" label="线电压最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="volLineMinTimeData" label="发生时间"/>
                </el-table-column>

                <el-table-column v-else-if="item.name === '最大电流'" label="电流最大值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="curMaxTimeData" label="发生时间"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小电流'" label="电流最小值">
                  <el-table-column :prop="item.name" label="数值"/>  
                  <el-table-column prop="curMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else :prop="item.name" :label="item.name"/>   
              </template>
            </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <!-- <el-empty v-show="!isHaveData" description="暂无数据" /> -->
      </div>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { HistoryDataApi } from '@/api/bus/historydata'
import { formatDate} from '@/utils/formatTime'
import { IndexApi } from '@/api/bus/busindex'
import { ElMessage } from 'element-plus'

defineOptions({ name: 'PDUHistoryLine' })

const activeName = ref('realtimeTabPane') // tab默认显示
const activeName1 = ref('myChart') // tab默认显示
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('')// 导航栏的位置信息
const nowLocation = ref('')// 导航栏的位置信息
const nowAddressTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const nowLocationTemp = ref('')// 暂时存储点击导航栏的位置信息 确认有数据再显示
const instance = getCurrentInstance();
const tableData = ref<Array<{ }>>([]); // 列表数据
const headerData = ref<any[]>([]);
const needFlush = ref(0) // 是否需要刷新图表
const loading = ref(false) // 加载中
const queryParams = reactive({
  busId: undefined as number | undefined,
  lineId: undefined,
  type: 'total',
  granularity: 'realtime',
  // 进入页面原始数据默认显示最近一小时
  timeRange: defaultHourTimeRange(1),
  devkey: undefined as string | undefined,
})

// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 1)
      return [start, end]
    },
  },
  {
    text: '最近十二小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 12)
      return [start, end]
    },
  },
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近两天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 48)
      return [start, end]
    },
  },
]
const shortcuts1 = [
  {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24)
      return [start, end]
    },
  },
  {
    text: '最近三天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 72)
      return [start, end]
    },
  },
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 168)
      return [start, end]
    },
  },
]
const shortcuts2 = [
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30)
      return [start, end]
    },
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30*3)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 24*30*6)
      return [start, end]
    },
  },
]

// 参数类型选项
const defaultSelected = ref(['total'])
const typeSelection = ref([
   { value: "total", label: '总'},
    { value: "line", label: '相', children:[
        { value: '1', label: 'L1' },
        { value: '2', label: 'L2' },
        { value: '3', label: 'L3' },
      ] 
    },
]) as any;

// 参数类型改变触发
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  switch(selected[0]){
    case 'line':
      queryParams.lineId = selected[1];
      break;
    case 'total':
      queryParams.lineId = undefined;
      break;
  }
  // 自动搜索
  handleQuery()
}

// 处理折线图数据
const createTimeData = ref<string[]>([]);
const powActiveData = ref<number[]>([]);
const powApparentData = ref<number[]>([]);
const powReactiveData = ref<number[]>([]);
const powerFactorData = ref<number[]>([]);
const curResidualData = ref<number[]>([]);
const curZeroData = ref<number[]>([]);
const curUnbalanceData = ref<number[]>([]);
const volUnbalanceData = ref<number[]>([]);
const loadRateData = ref<number[]>([]);
const volLineData = ref<number[]>([]);

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

const volLineAvgValueData = ref<number[]>([]);
const volLineMaxValueData = ref<number[]>([]);
const volLineMaxTimeData = ref<string[]>([]);
const volLineMinValueData = ref<number[]>([]);
const volLineMinTimeData = ref<string[]>([]);

const powActiveAvgValueData = ref<number[]>([]);
const powActiveMaxValueData = ref<number[]>([]);
const powActiveMaxTimeData = ref<string[]>([]);
const powActiveMinValueData = ref<number[]>([]);
const powActiveMinTimeData = ref<string[]>([]);

const powReactiveAvgValueData = ref<number[]>([]);
const powReactiveMaxValueData = ref<number[]>([]);
const powReactiveMaxTimeData = ref<string[]>([]);
const powReactiveMinValueData = ref<number[]>([]);
const powReactiveMinTimeData = ref<string[]>([]);

const powApparentAvgValueData = ref<number[]>([]);
const powApparentMaxValueData = ref<number[]>([]);
const powApparentMaxTimeData = ref<string[]>([]);
const powApparentMinValueData = ref<number[]>([]);
const powApparentMinTimeData = ref<string[]>([]);

const curResidualAvgValueData = ref<number[]>([]);
const curResidualMaxValueData = ref<number[]>([]);
const curResidualMaxTimeData = ref<string[]>([]);
const curResidualMinValueData = ref<number[]>([]);
const curResidualMinTimeData = ref<string[]>([]);

const curZeroAvgValueData = ref<number[]>([]);
const curZeroMaxValueData = ref<number[]>([]);
const curZeroMaxTimeData = ref<string[]>([]);
const curZeroMinValueData = ref<number[]>([]);
const curZeroMinTimeData = ref<string[]>([]);

// 侧边栏显示需要
const maxActivePowDataTemp = ref(0);// 最大有功功率 
const maxActivePowDataTimeTemp = ref();// 最大有功功率的发生时间 
const minActivePowDataTemp = ref(0);// 最小有功功率 
const minActivePowDataTimeTemp = ref();// 最小有功功率的发生时间 

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => {
  loading.value = true;
  try {
    const data = await HistoryDataApi.getBusHistoryDataDetails(queryParams);
    if (data != null && data.total != 0){
      isHaveData.value = true
      powActiveData.value = data.list.map((item) => formatNumber(item.pow_active, 3));
      powReactiveData.value = data.list.map((item) => formatNumber(item.pow_reactive, 3));
      powApparentData.value = data.list.map((item) => formatNumber(item.pow_apparent, 3));
      powerFactorData.value = data.list.map((item) => formatNumber(item.power_factor, 2));
      curResidualData.value = data.list.map((item) => formatNumber(item.cur_residual, 2));
      curZeroData.value = data.list.map((item) => formatNumber(item.cur_zero, 2));
      curUnbalanceData.value = data.list.map((item) => formatNumber(item.cur_unbalance, 2));
      volUnbalanceData.value = data.list.map((item) => formatNumber(item.vol_unbalance, 1));
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      }
      volLineData.value = data.list.map((item) => formatNumber(item.vol_line, 1));
      loadRateData.value = data.list.map((item) => formatNumber(item.load_rate, 2));

      curAvgValueData.value = data.list.map((item) => formatNumber(item.cur_avg_value, 2));
      curMaxValueData.value = data.list.map((item) => formatNumber(item.cur_max_value, 2));
      curMaxTimeData.value = data.list.map((item) => formatDate(item.cur_max_time));
      curMinValueData.value = data.list.map((item) => formatNumber(item.cur_min_value, 2));
      curMinTimeData.value = data.list.map((item) => formatDate(item.cur_min_time));

      curResidualAvgValueData.value = data.list.map((item) => formatNumber(item.cur_residual_avg_value, 2));
      curResidualMaxValueData.value = data.list.map((item) => formatNumber(item.cur_residual_max_value, 2));
      curResidualMaxTimeData.value = data.list.map((item) => formatDate(item.cur_residual_max_time));
      curResidualMinValueData.value = data.list.map((item) => formatNumber(item.cur_residual_min_value, 2));
      curResidualMinTimeData.value = data.list.map((item) => formatDate(item.cur_residual_min_time));

      curZeroAvgValueData.value = data.list.map((item) => formatNumber(item.cur_zero_avg_value, 2));
      curZeroMaxValueData.value = data.list.map((item) => formatNumber(item.cur_zero_max_value, 2));
      curZeroMaxTimeData.value = data.list.map((item) => formatDate(item.cur_zero_max_time));
      curZeroMinValueData.value = data.list.map((item) => formatNumber(item.cur_zero_min_value, 2));
      curZeroMinTimeData.value = data.list.map((item) => formatDate(item.cur_zero_min_time));

      volAvgValueData.value = data.list.map((item) => formatNumber(item.vol_avg_value, 1));
      volMaxValueData.value = data.list.map((item) => formatNumber(item.vol_max_value, 1));
      volMaxTimeData.value = data.list.map((item) => formatDate(item.vol_max_time));
      volMinValueData.value = data.list.map((item) => formatNumber(item.vol_min_value, 1));
      volMinTimeData.value = data.list.map((item) => formatDate(item.vol_min_time));

      volLineAvgValueData.value = data.list.map((item) => formatNumber(item.vol_avg_value, 1));
      volLineMaxValueData.value = data.list.map((item) => formatNumber(item.vol_max_value, 1));
      volLineMaxTimeData.value = data.list.map((item) => formatDate(item.vol_max_time));
      volLineMinValueData.value = data.list.map((item) => formatNumber(item.vol_min_value, 1));
      volLineMinTimeData.value = data.list.map((item) => formatDate(item.vol_min_time));

      powActiveAvgValueData.value = data.list.map((item) => formatNumber(item.pow_active_avg_value, 3));
      powActiveMaxValueData.value = data.list.map((item) => formatNumber(item.pow_active_max_value, 3));
      powActiveMaxTimeData.value = data.list.map((item) => formatDate(item.pow_active_max_time));
      powActiveMinValueData.value = data.list.map((item) => formatNumber(item.pow_active_min_value, 3));
      powActiveMinTimeData.value = data.list.map((item) => formatDate(item.pow_active_min_time));

      powReactiveAvgValueData.value = data.list.map((item) => formatNumber(item.pow_active_avg_value, 3));
      powReactiveMaxValueData.value = data.list.map((item) => formatNumber(item.pow_active_max_value, 3));
      powReactiveMaxTimeData.value = data.list.map((item) => formatDate(item.pow_active_max_time));
      powReactiveMinValueData.value = data.list.map((item) => formatNumber(item.pow_active_min_value, 3));
      powReactiveMinTimeData.value = data.list.map((item) => formatDate(item.pow_active_min_time));

      powApparentAvgValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_avg_value, 3));
      powApparentMaxValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_max_value, 3));
      powApparentMaxTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_max_time));
      powApparentMinValueData.value = data.list.map((item) => formatNumber(item.pow_apparent_min_value, 3));
      powApparentMinTimeData.value = data.list.map((item) => formatDate(item.pow_apparent_min_time));

      maxActivePowDataTemp.value = Math.max(...powActiveData.value);
      minActivePowDataTemp.value = Math.min(...powActiveData.value);
      powActiveData.value.forEach(function(num, index) {
        if (num == maxActivePowDataTemp.value){
          maxActivePowDataTimeTemp.value = createTimeData.value[index]
        }
        if (num == minActivePowDataTemp.value){
          minActivePowDataTimeTemp.value = createTimeData.value[index]
        }
      });

      // 图表显示的位置变化
      nowAddress.value = nowAddressTemp.value
      nowLocation.value = nowLocationTemp.value

    }else{
      isHaveData.value = false;
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
  } finally {
    loading.value = false;
  }
}

//初始化折线图
const chartContainer = ref<HTMLElement | null>(null);
let realtimeChart = null as echarts.ECharts | null; 
const initChart = () => {
  if ( isHaveData.value == true ){
    if (chartContainer.value && instance) {
      realtimeChart = echarts.init(chartContainer.value);
      if (realtimeChart) {
        realtimeChart.setOption({
          title: { text: ''},
          tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
          legend: { data: ['总有功功率', '总视在功率', '总无功功率', '功率因素', '剩余电流', '零线电流', '电压三相不平衡', '电流三相不平衡'],
                selected: {  "总有功功率": true, "总视在功率": true, "总无功功率": false, "功率因素": false, 
                              "剩余电流": false, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": false }
              },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {  restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '总有功功率', type: 'line', symbol: 'none', data: powActiveData.value},
            {name: '总视在功率', type: 'line', symbol: 'none', data: powApparentData.value},
            {name: '总无功功率', type: 'line', symbol: 'none', data: powReactiveData.value},
            {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
            {name: '剩余电流', type: 'line', symbol: 'none', data: curResidualData.value},
            {name: '零线电流', type: 'line', symbol: 'none', data: curZeroData.value},
            {name: '电流三相不平衡', type: 'line', symbol: 'none', data: curUnbalanceData.value},
            {name: '电压三相不平衡', type: 'line', symbol: 'none', data: volUnbalanceData.value},
        ],
          dataZoom:[{type: "inside"}],
        });
      }
           // 图例切换监听
            totalRealtimeLegendListener(realtimeChart);
      // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
      instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
    }
  }
  // 每次切换图就要动态生成数据表头
  headerData.value = realtimeChart?.getOption().series as any[];
  updateTableData();
};

// 表格映射图数据
const updateTableData = () => {
  const data: any[] = [];
  const length = headerData.value[0]?.data?.length || 0;
  for (let i = 0; i < length; i++) {
    const rowData: { [key: string]: any } = {};
    rowData['create_time'] = createTimeData.value[i];
    rowData['curMaxTimeData'] = curMaxTimeData.value[i];
    rowData['curMinTimeData'] = curMinTimeData.value[i];
    rowData['volMaxTimeData'] = volMaxTimeData.value[i];
    rowData['volMinTimeData'] = volMinTimeData.value[i];
    rowData['volLineMaxTimeData'] = volLineMaxTimeData.value[i];
    rowData['volLineMinTimeData'] = volLineMinTimeData.value[i];

    rowData['activePowMaxTimeData'] = powActiveMaxTimeData.value[i];
    rowData['activePowMinTimeData'] = powActiveMinTimeData.value[i];
    rowData['reactivePowMaxTimeData'] = powReactiveMaxTimeData.value[i];
    rowData['reactivePowMinTimeData'] = powReactiveMinTimeData.value[i];
    rowData['apparentPowMaxTimeData'] = powApparentMaxTimeData.value[i];
    rowData['apparentPowMinTimeData'] = powApparentMinTimeData.value[i];

    rowData['curResidualMaxTime'] = curResidualMaxTimeData.value[i];
    rowData['curResidualMinTime'] = curResidualMinTimeData.value[i];
    rowData['curZeroMaxTime'] = curZeroMaxTimeData.value[i];
    rowData['curZeroMinTime'] = curZeroMinTimeData.value[i];
    for (const item of headerData.value) {
      rowData[item.name] = item.data[i];
    }
    data.push(rowData);
  }
  tableData.value = data;
};

// 在组件销毁时手动销毁图表
const beforeUnmount = () => {
  realtimeChart?.dispose();
};
// 折线图随着窗口大小改变
window.addEventListener('resize', function() {
  realtimeChart?.resize(); 
});

// 监听切换原始数据、极值数据tab
watch( ()=>activeName.value, async(newActiveName)=>{
  if ( newActiveName == 'realtimeTabPane'){
    queryParams.granularity = 'realtime'
    queryParams.timeRange = defaultHourTimeRange(1)
  }else if (newActiveName == 'hourExtremumTabPane'){
    queryParams.granularity = 'hour'
    queryParams.timeRange = defaultHourTimeRange(40)
  }else{
    queryParams.granularity = 'day'
    queryParams.timeRange = defaultHourTimeRange(24*30)
  }
  needFlush.value ++;
});

// 监听类型颗粒度
watch(() => [activeName.value, queryParams.type, needFlush.value], async (newValues) => {
    const [newActiveName, newType] = newValues;
          console.log(newType)
    // 处理参数变化
    if (newType == 'total'){
      if ( newActiveName == 'realtimeTabPane'){
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
           realtimeChart.setOption({
              title: { text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['总有功功率', '总视在功率', '总无功功率', '功率因素', '剩余电流', '零线电流', '电压三相不平衡', '电流三相不平衡'],
                    selected: {  "总有功功率": true, "总视在功率": true, "总无功功率": false, "功率因素": false, 
                                  "剩余电流": false, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": false }
                  },
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '总有功功率', type: 'line', symbol: 'none', data: powActiveData.value},
                {name: '总视在功率', type: 'line', symbol: 'none', data: powApparentData.value},
                {name: '总无功功率', type: 'line', symbol: 'none', data: powReactiveData.value},
                {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
                {name: '剩余电流', type: 'line', symbol: 'none', data: curResidualData.value},
                {name: '零线电流', type: 'line', symbol: 'none', data: curZeroData.value},
                {name: '电流三相不平衡', type: 'line', symbol: 'none', data: curUnbalanceData.value},
                {name: '电压三相不平衡', type: 'line', symbol: 'none', data: volUnbalanceData.value},
            ],
              dataZoom:[{type: "inside"}],
            });
          }
            // 图例切换监听
            totalRealtimeLegendListener(realtimeChart);
        }
         // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }else{
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption({     
              title: {text: ''},
              tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
              legend: { data: ['平均有功功率', '最大有功功率', '最小有功功率','平均无功功率', '最大无功功率', '最小无功功率','平均视在功率', '最大视在功率', '最小视在功率', 
                                '平均剩余电流', '最大剩余电流', '最小剩余电流', '平均零线电流', '最大零线电流', '最小零线电流'],
                        selected: { 平均有功功率: true, 最大有功功率: true, 最小有功功率: true, 平均无功功率: false, 最大无功功率: false, 最小无功功率: false, 
                        平均视在功率: false, 最大视在功率: false, 最小视在功率: false, 平均剩余电流: false, 最大剩余电流: false, 最小剩余电流: false,平均零线电流: false, 最大零线电流: false, 最小零线电流: false,}
              },
              grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: [
                {type: 'category', boundaryGap: false, data: createTimeData.value}
              ],
              yAxis: { type: 'value'},
              series: [
                { name: '平均有功功率', type: 'line', symbol: 'none', data: powActiveAvgValueData.value, },
                { name: '最大有功功率', type: 'line', symbol: 'none', data: powActiveMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小有功功率', type: 'line', symbol: 'none', data: powActiveMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均无功功率', type: 'line', symbol: 'none', data: powReactiveAvgValueData.value, },
                { name: '最大无功功率', type: 'line', symbol: 'none', data: powReactiveMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小无功功率', type: 'line', symbol: 'none', data: powReactiveMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均视在功率', type: 'line', symbol: 'none', data: powApparentAvgValueData.value, },
                { name: '最大视在功率', type: 'line', symbol: 'none', data: powApparentMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小视在功率', type: 'line', symbol: 'none', data: powApparentMinValueData.value, lineStyle: {type: 'dashed'}},

                { name: '平均剩余电流', type: 'line', symbol: 'none', data: curResidualAvgValueData.value, },
                { name: '最大剩余电流', type: 'line', symbol: 'none', data: curResidualMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小剩余电流', type: 'line', symbol: 'none', data: curResidualMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均零线电流', type: 'line', symbol: 'none', data: curZeroAvgValueData.value, },
                { name: '最大零线电流', type: 'line', symbol: 'none', data: curZeroMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小零线电流', type: 'line', symbol: 'none', data: curZeroMinValueData.value, lineStyle: {type: 'dashed'}},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
            // 图例切换监听
            totalHourAndDayLegendListener(realtimeChart);
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }
    }
    
    if (newType == 'line'){
      if ( newActiveName == 'realtimeTabPane'){
          await getList();
          // 销毁原有的图表实例
          beforeUnmount()
          if ( isHaveData.value == true ){
            // 创建新的图表实例
            realtimeChart = echarts.init(document.getElementById('chartContainer'));
            // 设置新的配置对象
            if (realtimeChart) {
              realtimeChart.setOption({
                // 这里设置 Echarts 的配置项和数据
                title: { text: ''},
                tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
                legend: { data:  ['有功功率', '视在功率', '无功功率', '功率因素', '电压', '电流', '负载率', '线电压'],
                          selected: {  "有功功率": true, "视在功率": false, "无功功率": false, "功率因素": false
                            , "电压": false, "电流": false, "负载率": false, "线电压": false
                           }},
                grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
                toolbox: {feature: {  restore:{}, saveAsImage: {}}},
                xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
                yAxis: { type: 'value'},
                series: [
                  {name: '有功功率', type: 'line', symbol: 'none', data: powActiveData.value},
                  {name: '无功功率', type: 'line', symbol: 'none', data: powReactiveData.value},
                  {name: '功率因素', type: 'line', symbol: 'none', data: powerFactorData.value},
                  {name: '视在功率', type: 'line', symbol: 'none', data: powApparentData.value},
                  {name: '电压', type: 'line', symbol: 'none', data: powActiveData.value},
                  {name: '电流', type: 'line', symbol: 'none', data: powActiveData.value},
                  {name: '负载率', type: 'line', symbol: 'none', data: loadRateData.value},
                  {name: '线电压', type: 'line', symbol: 'none', data: volLineData.value},
                ],
                dataZoom:[{type: "inside"}],
              });
            }
            // 图例切换监听
            lineRealtimeLegendListener(realtimeChart);
          }
          // 每次切换图就要动态生成数据表头
          headerData.value = realtimeChart?.getOption().series as any[];
          updateTableData();
      }else{
        await getList();
        // 销毁原有的图表实例
        beforeUnmount()
        if ( isHaveData.value == true ){
          // 创建新的图表实例
          realtimeChart = echarts.init(document.getElementById('chartContainer'));
          // 设置新的配置对象
          if (realtimeChart) {
            realtimeChart.setOption( {
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: [ '平均有功功率', '最大有功功率', '最小有功功率','平均无功功率', '最大无功功率', '最小无功功率','平均视在功率', '最大视在功率', '最小视在功率',
                               '平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压','平均线电压', '最大线电压', '最小线电压'],
                      selected: {  平均有功功率: true, 最大有功功率: true, 最小有功功率: true, 平均无功功率: false, 最大无功功率: false, 最小无功功率: false,
                                  平均视在功率: false, 最大视在功率: false, 最小视在功率: false, 平均电流: false, 最大电流: false, 最小电流: false, 
                                  平均电压: false, 最大电压: false, 最小电压: false, 平均线电压: false, 最大线电压: false, 最小线电压: false }
                    },
            grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value},
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均电流', type: 'line', symbol: 'none', data: curAvgValueData.value, },
              { name: '最大电流', type: 'line', symbol: 'none', data: curMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电流', type: 'line', symbol: 'none', data: curMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均电压', type: 'line', symbol: 'none', data: volAvgValueData.value, },
              { name: '最大电压', type: 'line', symbol: 'none', data: volMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小电压', type: 'line', symbol: 'none', data: volMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均线电压', type: 'line', symbol: 'none', data: volLineAvgValueData.value, },
              { name: '最大线电压', type: 'line', symbol: 'none', data: volLineMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小线电压', type: 'line', symbol: 'none', data: volLineMinValueData.value, lineStyle: {type: 'dashed'}},

              { name: '平均有功功率', type: 'line', symbol: 'none', data: powActiveAvgValueData.value, },
              { name: '最大有功功率', type: 'line', symbol: 'none', data: powActiveMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小有功功率', type: 'line', symbol: 'none', data: powActiveMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均无功功率', type: 'line', symbol: 'none', data: powReactiveAvgValueData.value, },
              { name: '最大无功功率', type: 'line', symbol: 'none', data: powReactiveMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小无功功率', type: 'line', symbol: 'none', data: powReactiveMinValueData.value, lineStyle: {type: 'dashed'}},

              { name: '平均视在功率', type: 'line', symbol: 'none', data: powApparentAvgValueData.value, },
              { name: '最大视在功率', type: 'line', symbol: 'none', data: powApparentMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小视在功率', type: 'line', symbol: 'none', data: powApparentMinValueData.value, lineStyle: {type: 'dashed'}},
            ],
            dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          lineHourAndDayLegendListener(realtimeChart);
      }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();      
      }
    }

});

// 总数据实时图例切换函数
function totalRealtimeLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '总有功功率':
     if (params.selected[legendName]){
          optionsToUpdate = {  "总有功功率": true , "功率因素": false, 
                            "剩余电流": false, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": false };
        }
        break;

      case '总视在功率':
      if (params.selected[legendName]){
          optionsToUpdate = { "总视在功率": true, "功率因素": false, 
                            "剩余电流": false, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": false };
      }
        break;

      case '总无功功率':
      if (params.selected[legendName]){
          optionsToUpdate = { "总无功功率": true, "功率因素": false, "剩余电流": false, "零线电流": false,
                             "电压三相不平衡": false, "电流三相不平衡": false };
      }
        break;

      case '功率因素':
      if (params.selected[legendName]){
        optionsToUpdate = {  "总有功功率": false, "总视在功率": false, "总无功功率": false, "功率因素": true, 
                            "剩余电流": false, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": false };
      }
        break;

      case '剩余电流':
      if (params.selected[legendName]){
        optionsToUpdate =  {  "总有功功率": false, "总视在功率": false, "总无功功率": false, "功率因素": false, 
                            "剩余电流": true, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": false };
      }
        break;

      case '零线电流':
      if (params.selected[legendName]){
        optionsToUpdate = {  "总有功功率": false, "总视在功率": false, "总无功功率": false, "功率因素": false, 
                            "剩余电流": false, "零线电流": true, "电压三相不平衡": false, "电流三相不平衡": false };
      }
        break;

      case '电压三相不平衡':
      if (params.selected[legendName]){
        optionsToUpdate ={  "总有功功率": false, "总视在功率": false, "总无功功率": false, "功率因素": false, 
                            "剩余电流": false, "零线电流": false, "电压三相不平衡": true, "电流三相不平衡": false };
      }
        break;

      case '电流三相不平衡':
      if (params.selected[legendName]){
        optionsToUpdate = {  "总有功功率": false, "总视在功率": false, "总无功功率": false, "功率因素": false, 
                            "剩余电流": false, "零线电流": false, "电压三相不平衡": false, "电流三相不平衡": true };
      }
        break;
        
      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['总有功功率', '总视在功率', '总无功功率', '功率因素', '剩余电流', '零线电流', '电压三相不平衡', '电流三相不平衡'],
        selected: optionsToUpdate
      },
    });
  });
}

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; 
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '总有功功率':
      case '总无功功率':
      case '有功功率':
      case '无功功率':
      case '平均有功功率':
      case '平均无功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +powActiveMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +powActiveMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      
      case '最大无功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +powReactiveMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小无功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +powReactiveMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '总视在功率':
      case '视在功率':
      case '平均视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  发生时间: ' +powApparentMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  发生时间: ' +powApparentMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '电流':
      case '电流三相不平衡': 
      case '平均电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '剩余电流':
      case '平均剩余电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大剩余电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curResidualMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小剩余电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curResidualMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '零线电流':
      case '平均零线电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大零线电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curZeroMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小零线电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curZeroMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '电压':
      case '线电压':
      case '电压三相不平衡': 
      case '平均电压':
      case '平均线电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  发生时间: ' +volMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  发生时间: ' +volMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '最大线电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  发生时间: ' +volLineMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小线电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  发生时间: ' +volLineMinTimeData.value[item.dataIndex] + '<br/>';
        break;

      case '功率因素':
      case '负载率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + '   发生时间: ' +powerFactorData.value[item.dataIndex] + '<br/>';
        break;
    }
    
  });
  return tooltipContent;
}

// 总数据小时、天 图例切换函数
function totalHourAndDayLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '平均视在功率':
      case '最大视在功率':
      case '最小视在功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均视在功率": true, "最大视在功率": true, "最小视在功率": true, "平均剩余电流": false, "最大剩余电流": false, "最小剩余电流": false, 
        "平均零线电流": false, "最大零线电流": false, "最小零线电流": false };
      }
        break;

      case '平均有功功率':
      case '最大有功功率':
      case '最小有功功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均有功功率": true, "最大有功功率": true, "最小有功功率": true, "平均剩余电流": false, "最大剩余电流": false, "最小剩余电流": false, 
        "平均零线电流": false, "最大零线电流": false, "最小零线电流": false };
      }
        break;

      case '平均无功功率':
      case '最大无功功率':
      case '最小无功功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均无功功率": true, "最大无功功率": true, "最小无功功率": true, "平均剩余电流": false, "最大剩余电流": false, "最小剩余电流": false, 
        "平均零线电流": false, "最大零线电流": false, "最小零线电流": false };
      }
        break;

      case '平均剩余电流':
      case '最大剩余电流':
      case '最小剩余电流':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均剩余电流": true, "最大剩余电流": true, "最小剩余电流": true, "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                          "平均有功功率": false, "最大有功功率": false, "最小有功功率": false,"平均无功功率": false, "最大无功功率": false, "最小无功功率": false};
      }
        break;

      case '平均零线电流':
      case '最大零线电流':
      case '最小零线电流':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均零线电流": true, "最大零线电流": true, "最小零线电流": true, "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                          "平均有功功率": false, "最大有功功率": false, "最小有功功率": false,"平均无功功率": false, "最大无功功率": false, "最小无功功率": false};
      }
        break;


      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data:  ['平均有功功率', '最大有功功率', '最小有功功率','平均无功功率', '最大无功功率', '最小无功功率','平均视在功率', '最大视在功率', '最小视在功率', 
                '平均剩余电流', '最大剩余电流', '最小剩余电流', '平均零线电流', '最大零线电流', '最小零线电流'],
        selected: optionsToUpdate
      },
    });
  });
}

// 相数据实时图例切换函数
function lineRealtimeLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '有功功率':
     if (params.selected[legendName]){
          optionsToUpdate = { "有功功率": true,  "功率因素": false , "电压": false, 
                            "电流": false, "负载率": false, "线电压": false }
        }
        break;

      case '视在功率':
      if (params.selected[legendName]){
          optionsToUpdate = { "视在功率": true,  "功率因素": false , "电压": false, 
                            "电流": false, "负载率": false, "线电压": false }
      }
        break;

      case '无功功率':
      if (params.selected[legendName]){
          optionsToUpdate = { "无功功率": true,  "功率因素": false , "电压": false, 
                            "电流": false, "负载率": false, "线电压": false }
      }
        break;

      case '功率因素':
      if (params.selected[legendName]){
        optionsToUpdate = { "功率因素": true,  "有功功率": false,  "视在功率": false,  
                           "无功功率": false , "电压": false, "电流": false, "负载率": false, "线电压": false }
      }
        break;

      case '电流':
      if (params.selected[legendName]){
        optionsToUpdate = { "电流": true, "功率因素": false , "有功功率": false,  "视在功率": false,  
                             "无功功率": false , "电压": false,  "负载率": false, "线电压": false }
      }
        break;

      case '电压':
      if (params.selected[legendName]){
        optionsToUpdate = { "电压": true, "功率因素": false , "有功功率": false, "电流": false, 
                          "视在功率": false,  "无功功率": false, "负载率": false}
      }
        break;

      case '线电压':
      if (params.selected[legendName]){
        optionsToUpdate = { "线电压": true, "功率因素": false , "有功功率": false, "电流": false, 
                            "视在功率": false,  "无功功率": false, "负载率": false}
      }
        break;

      case '负载率':
      if (params.selected[legendName]){
        optionsToUpdate = { "负载率": true, "电压": false, "线电压": false, "功率因素": false ,"电流": false,  
                            "有功功率": false,  "视在功率": false,  "无功功率": false}
      }
        break;
        
      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['有功功率', '视在功率', '无功功率', '功率因素', '电压', '电流', '负载率', '线电压'],
        selected: optionsToUpdate
      },
    });
  });
}

// 相数据小时、天 图例切换函数
function lineHourAndDayLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '平均视在功率':
      case '最大视在功率':
      case '最小视在功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均视在功率": true, "最大视在功率": true, "最小视在功率": true, "平均电流": false, "最大电流": false, "最小电流": false, 
                            "平均电压": false, "最大电压": false, "最小电压": false, "平均线电压": false, "最大线电压": false, "最小线电压": false,  };
      }
        break;

      case '平均有功功率':
      case '最大有功功率':
      case '最小有功功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均有功功率": true, "最大有功功率": true, "最小有功功率": true, "平均电流": false, "最大电流": false, "最小电流": false, 
                          "平均电压": false, "最大电压": false, "最小电压": false, "平均线电压": false, "最大线电压": false, "最小线电压": false, };
      }
        break;

      case '平均无功功率':
      case '最大无功功率':
      case '最小无功功率':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均无功功率": true, "最大无功功率": true, "最小无功功率": true, "平均电流": false, "最大电流": false, "最小电流": false, 
                         "平均电压": false, "最大电压": false, "最小电压": false, "平均线电压": false, "最大线电压": false, "最小线电压": false };
      }
        break;

      case '平均电流':
      case '最大电流':
      case '最小电流':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电流": true, "最大电流": true, "最小电流": true, "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                          "平均有功功率": false, "最大有功功率": false, "最小有功功率": false,"平均无功功率": false, "最大无功功率": false, "最小无功功率": false,
                           "平均电压": false, "最大电压": false, "最小电压": false, "平均线电压": false, "最大线电压": false, "最小线电压": false};
      }
        break;

      case '平均电压':
      case '最大电压':
      case '最小电压':
      if (params.selected[legendName]){
        optionsToUpdate = { "平均电压": true, "最大电压": true, "最小电压": true, "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                 "平均有功功率": false, "最大有功功率": false, "最小有功功率": false,"平均无功功率": false, "最大无功功率": false, "最小无功功率": false,
                 "平均电流": false, "最大电流": false, "最小电流": false }
          }
        break;

      case '平均线电压':
      case '最大线电压':
      case '最小线电压':
      if (params.selected[legendName]){
        optionsToUpdate = {"平均线电压": true, "最大线电压": true, "最小线电压": true, "平均视在功率": false, "最大视在功率": false, "最小视在功率": false,
                 "平均有功功率": false, "最大有功功率": false, "最小有功功率": false,"平均无功功率": false, "最大无功功率": false, "最小无功功率": false,
                 "平均电流": false, "最大电流": false, "最小电流": false  };
      }
        break;


      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data:  ['平均有功功率', '最大有功功率', '最小有功功率','平均无功功率', '最大无功功率', '最小无功功率','平均视在功率', '最大视在功率', '最小视在功率', 
                '平均电流', '最大电流', '最小电流', '平均电压', '最大电压', '最小电压', '平均线电压', '最大线电压', '最小线电压'],
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

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  // today.setHours(0, 0, 0, 0);
  // // 设置date的时间为0时0分0秒，以便与today进行比较
  // date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

// 处理实时数据的时间选择不超过xxx范围
// const handleDayPick = () => {
//   if (activeName.value=='realtimeTabPane'){
//     // 计算两个日期之间的天数差
//    const diffDays = betweenDay(convertDate(queryParams.timeRange[0]), convertDate(queryParams.timeRange[1]))
//     // 如果天数差超过2天，则重置选择的日期
//     if (diffDays > 2) {
//       queryParams.timeRange = [];
//       ElMessage({
//         message: '时间选择不超过48小时',
//         type: 'warning',
//       })
//     }
//   }
//   if (activeName.value=='hourExtremumTabPane'){
//     // 计算两个日期之间的天数差
//    const diffDays = betweenDay(convertDate(queryParams.timeRange[0]), convertDate(queryParams.timeRange[1]))
//     // 如果天数差超过7天，则重置选择的日期
//     if (diffDays > 7) {
//       queryParams.timeRange = [];
//       ElMessage({
//         message: '时间选择不超过7天',
//         type: 'warning',
//       })
//     }
//   }
//     if (activeName.value=='dayExtremumTabPane'){
//     // 计算两个日期之间的天数差
//    const diffDays = betweenDay(convertDate(queryParams.timeRange[0]), convertDate(queryParams.timeRange[1]))
//     // 如果天数差超过7天，则重置选择的日期
//     if (diffDays > 180) {
//       queryParams.timeRange = [];
//       ElMessage({
//         message: '时间选择不超过6个月',
//         type: 'warning',
//       })
//     }
//   }
// }


// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 6){
    queryParams.busId = undefined
    queryParams.devkey = row.unique
    findFullName(navList.value, row.unique, fullName => {
      nowAddressTemp.value = fullName
      nowLocationTemp.value = row.unique
    });
    handleQuery();
  }
}

// 得到位置全名
function findFullName(data, targetUnique, callback, fullName = '') {
  for (let item of data) {
    const newFullName = fullName === '' ? item.name : fullName + '-' + item.name;
    if (item.unique === targetUnique) {
      callback(newFullName);
    }
    if (item.children && item.children.length > 0) {
      findFullName(item.children, targetUnique, callback, newFullName);
    }
  }
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
  navList.value = res
}

/** 搜索按钮操作 */
const handleQuery = () => {
  needFlush.value++;
}

/** 初始化 **/
onMounted( async () => { 
  getNavList()
  // 获取路由参数中的 pdu_id
  const queryBusId = useRoute().query.busId as string  | undefined;
  const queryLocation = useRoute().query.location as string;
  queryParams.busId = queryBusId ? parseInt(queryBusId, 10) : undefined;
  if (queryParams.busId != undefined){
    await getList(); 
    nowAddress.value = queryLocation
    initChart();
  }
})


</script>

<style scoped>
/*  
// 表格部分样式
// 最外层透明 */
:deep( .el-table),
:deep( .el-table__expanded-cell) {
  background-color: transparent;
  color: #93dcfe;
  font-size: 24px;
}
 
/* 表格内背景颜色  */
:deep( .el-table th),
:deep( .el-table tr),
:deep( .el-table td) {
  background-color: transparent;
  border: 0px;
  color: #93dcfe;
  font-size: 24px;
  height: 5px;
  font-family: Source Han Sans CN Normal, Source Han Sans CN Normal-Normal;
  font-weight: Normal;
}
 
/* // 去掉最下面的那一条线  */
.el-table::before {
  height: 0px;
}
 
/* // 设置表格行高度 */
:deep( .el-table__body tr)
:deep( .el-table__body td) {
  padding: 0;
  height: 54px;
}
 
/* // 修改高亮当前行颜色 */
:deep( .el-table tbody tr:hover>td ){
  background: #063570 !important;
}
 
/* // 取消当前行高亮 */
:deep( .el-table tbody tr) {
  pointer-events: none;
}
 
/* 修改表头样式-加边框 */
/* ::v-deep .el-table__header-wrapper {
  border: solid 1px #04c2ed;
} */
 
/* // 表格斑马自定义颜色 */
:deep(.el-table__row.warning-row)  {
  background: #01205A;
}
 
 
/* 去掉表格里的padding */
:deep(.el-table .cell),
:deep(.el-table th div) {
  padding-left: 0px;
  padding-right: 0px;
  padding-top: 0px;
  padding-bottom: 0px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav_header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 16px;
  }

.nav_data{
  padding-left: 5px;
  width: 195px;
}
.nav_content span{
  font-size: 18px;
}
.carousel-container {
  width: 100%;
  max-width: 100%;
}
.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover; 
}

</style>