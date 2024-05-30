<template>
  <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="PDU电力分析" :showCheckbox="false">
    <template #NavInfo>
      <div class="nav_header">
        <div class="nav_header_img"><img alt="" src="@/assets/imgs/PDU.jpg" /></div>
        <br/>
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
      <div class="nav_data" v-if="queryParams.granularity == 'realtime' && queryParams.type == 'total'">
        <el-statistic title="有功功率最大值" :value="formatNumber(maxActivePowDataTemp, 3)">
          <template #suffix>kW</template>
        </el-statistic>
        <el-statistic v-if="formatNumber(maxActivePowDataTemp, 3) != 0.0" title="发生于" :value="maxActivePowDataTimeTemp"/>
        <el-statistic v-if="formatNumber(maxActivePowDataTemp, 3) == 0.0" title="发生于" :value="Object('-')"/>
          <br/>
        <el-statistic title="有功功率最小值" :value="formatNumber(minActivePowDataTemp, 3)">
          <template #suffix>kW</template>
        </el-statistic>
        <el-statistic v-if="formatNumber(minActivePowDataTemp, 3) != 0.0" title="发生于" :value="minActivePowDataTimeTemp"/>
        <el-statistic v-if="formatNumber(minActivePowDataTemp, 3) == 0.0" title="发生于" :value="Object('-')"/>
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
      <!-- <el-form-item label="IP地址" prop="ipAddr">
        <el-input
          v-model="queryParams.ipAddr"
          placeholder="请输入IP地址"
          clearable
          @keyup.enter="handleQuery"
          class="!w-130px"
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
      </el-form-item> -->
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
          <el-button type="primary" plain><Icon icon="ep:download" class="mr-5px" /> 导出</el-button>
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
import { HistoryDataApi } from '@/api/pdu/historydata'
import { formatDate, convertDate, betweenDay } from '@/utils/formatTime'
import { get } from 'http';
import { CabinetApi } from '@/api/cabinet/info'
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
const cascadeAddr = ref(0) // 数字类型的级联地址
const needFlush = ref(0) // 是否需要刷新图表
const loading = ref(false) // 加载中
const queryParams = reactive({
  pduId: undefined as number | undefined,
  lineId: undefined,
  loopId: undefined,
  outletId: undefined,
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined as string | undefined,
  cascadeAddr: '0' as string | undefined,
  // 进入页面原始数据默认显示最近一小时
  timeRange: defaultHourTimeRange(1)
})

// 监控 cascadeAddr 如果变为 null 将其设置为 0
watch(() => queryParams.cascadeAddr, (newValue) => {
  if (newValue == null ) {
    queryParams.cascadeAddr = '0';
  }
});

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
const typeSelection = ref([]) as any;

// 参数类型改变触发
const typeChangeFlushFlag = ref(['total']) as any//为了触发监听
const typeCascaderChange = (selected) => {
  queryParams.type = selected[0];
  typeChangeFlushFlag.value = [selected[0],selected[1]]
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
    const data = await HistoryDataApi.getHistoryDataDetails(queryParams);
    if (data != null && data.total != 0){
      isHaveData.value = true
      volData.value = data.list.map((item) => formatNumber(item.vol_value, 1));
      curData.value = data.list.map((item) => formatNumber(item.cur_value, 2));
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      }
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

      maxActivePowDataTemp.value = Math.max(...activePowData.value);
      minActivePowDataTemp.value = Math.min(...activePowData.value);
      activePowData.value.forEach(function(num, index) {
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
          legend: { data: ['总有功功率', '总视在功率'] },
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {  restore:{}, saveAsImage: {}}},
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
    rowData['activePowMaxTimeData'] = activePowMaxTimeData.value[i];
    rowData['activePowMinTimeData'] = activePowMinTimeData.value[i];
    rowData['apparentPowMaxTimeData'] = apparentPowMaxTimeData.value[i];
    rowData['apparentPowMinTimeData'] = apparentPowMinTimeData.value[i];
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
    queryParams.timeRange = defaultHourTimeRange(24)
  }else{
    queryParams.granularity = 'day'
    queryParams.timeRange = defaultHourTimeRange(24*30)
  }
  needFlush.value ++;
});

// 监听类型颗粒度
watch(() => [activeName.value, typeChangeFlushFlag.value, needFlush.value], async (newValues) => {
    const [newActiveName, newType] = newValues;
    // 处理参数变化
    if (newType[0] == 'total'){
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
              legend: { data: ['总有功功率', '总视在功率'] },
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: {  restore:{},saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '总有功功率', type: 'line', symbol: 'none', data: activePowData.value},
                {name: '总视在功率', type: 'line', symbol: 'none', data: apparentPowData.value},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
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
              legend: { data: ['平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                        selected: { 平均有功功率: true, 最大有功功率: true, 最小有功功率: true, 
                        平均视在功率: false, 最大视在功率: false, 最小视在功率: false, }
              },
              grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: [
                {type: 'category', boundaryGap: false, data: createTimeData.value}
              ],
              yAxis: { type: 'value'},
              series: [
                { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value, },
                { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value, },
                { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
      }
    }
    
    if (newType[0] == 'line'){
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
                legend: { data: ['电压', '电流', '有功功率', '视在功率'],
                          selected: {  "电压": true, "电流": false, "有功功率": false, "视在功率": false }},
                grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
                toolbox: {feature: {  restore:{}, saveAsImage: {}}},
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
            legend: { data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
                              '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                      selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均电压: false, 最大电压: false, 最小电压: false, 
                                  平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
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

              { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value, },
              { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value,},
              { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
            ],
            dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          setupLegendListener1(realtimeChart);  
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();      
      }
    }

    if (newType[0] == 'loop'){
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
              legend: { data: ['电压', '电流', '有功功率', '视在功率'],
                        selected: {  "电压": true, "电流": false, "有功功率": false, "视在功率": false }},
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
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
              legend: { data: ['平均电流', '最大电流', '最小电流','平均电压', '最大电压', '最小电压',
                                '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                        selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均电压: false, 最大电压: false, 最小电压: false, 
                                    平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
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

                { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value},
                { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value, lineStyle: {type: 'dashed'} },
                { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value, lineStyle: {type: 'dashed'} },
                { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value},
                { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          setupLegendListener1(realtimeChart);
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();     
      }
    }

    if (newType[0] == 'outlet'){
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
            legend: { data: [ '电流', '有功功率', '视在功率'],
                  selected: { "电流": true, "有功功率": false, "视在功率": false }},
            grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
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
              legend: { data: ['平均电流', '最大电流', '最小电流', '平均有功功率', '最大有功功率', '最小有功功率','平均视在功率', '最大视在功率', '最小视在功率'],
                        selected: { 平均电流: false, 最大电流: false, 最小电流: false, 平均视在功率: false, 最大视在功率: false, 最小视在功率: false}
                      },
              grid: {left: '3%', right: '4%',bottom: '3%', containLabel: true },
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: [{type: 'category', boundaryGap: false, data: createTimeData.value},],
              yAxis: { type: 'value'},
              series: [
                { name: '平均电流', type: 'line', symbol: 'none', data: curAvgValueData.value,},
                { name: '最大电流', type: 'line', symbol: 'none', data: curMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小电流', type: 'line', symbol: 'none', data: curMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均有功功率', type: 'line', symbol: 'none', data: activePowAvgValueData.value,},
                { name: '最大有功功率', type: 'line', symbol: 'none', data: activePowMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小有功功率', type: 'line', symbol: 'none', data: activePowMinValueData.value, lineStyle: {type: 'dashed'}},
                { name: '平均视在功率', type: 'line', symbol: 'none', data: apparentPowAvgValueData.value,},
                { name: '最大视在功率', type: 'line', symbol: 'none', data: apparentPowMaxValueData.value, lineStyle: {type: 'dashed'}},
                { name: '最小视在功率', type: 'line', symbol: 'none', data: apparentPowMinValueData.value, lineStyle: {type: 'dashed'}},
              ],
              dataZoom:[{type: "inside"}],
            });
          }
          // 图例切换监听
          setupLegendListener1(realtimeChart);
        }
        // 每次切换图就要动态生成数据表头
        headerData.value = realtimeChart?.getOption().series as any[];
        updateTableData();
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

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; 
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '总有功功率':
      case '有功功率':
      case '平均有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +activePowMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小有功功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kW  发生时间: ' +activePowMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '总视在功率':
      case '视在功率':
      case '平均视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  发生时间: ' +apparentPowMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小视在功率':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' kVA  发生时间: ' +apparentPowMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '电流':
      case '平均电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小电流':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' A  发生时间: ' +curMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '电压':
      case '平均电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大电压':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  发生时间: ' +volMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小电压':
      tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' V  发生时间: ' +volMinTimeData.value[item.dataIndex] + '<br/>';
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

// 获取参数类型最大值 例如lineId=6 表示下拉框为L1~L6
const getTypeMaxValue = async () => {
    const data = await HistoryDataApi.getTypeMaxValue()
    const lineIdMaxValue = data.line_id_max_value;
    const loopIdMaxValue = data.loop_id_max_value;
    const outletIdMaxValue = data.outlet_id_max_value;
    const typeSelectionValue  = [
    {
      value: "total",
      label: '总'
    },
    {
      value: "line",
      label: '相',
      children: (() => {
        const lines: { value: any; label: string; }[] = [];
        for (let i = 1; i <= lineIdMaxValue; i++) {
          lines.push({ value: `${i}`, label: `L${i}` });
        }
        return lines;
      })(),
    },
    {
      value: "loop",
      label: '回路',
      children: (() => {
        const loops: { value: any; label: string; }[] = [];
        for (let i = 1; i <= loopIdMaxValue; i++) {
          loops.push({ value: `${i}`, label: `C${i}` });
        }
        return loops;
      })(),
    },
    {
      value: "outlet",
      label: '输出位',
      children: (() => {
        const outlets: { value: any; label: string; }[] = [];
        for (let i = 1; i <= outletIdMaxValue; i++) {
          outlets.push({ value: `${i}`, label: `${i}` });
        }
        return outlets;
      })(),
    },
  ]
  typeSelection.value = typeSelectionValue;
}

// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 4){
    queryParams.pduId = undefined
    queryParams.ipAddr = row.ip
    queryParams.cascadeAddr = row?.unique?.split("-")[1];
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
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
  arr = arr.concat(temp);
  }
  navList.value = arr
}

/** 搜索按钮操作 */
const handleQuery = () => {
  // IP地址的正则表达式
  const ipRegex = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
  if (queryParams.ipAddr == null || queryParams.ipAddr == '' || ipRegex.test(queryParams.ipAddr)){
    queryParams.cascadeAddr = cascadeAddr.value.toString();
    if (queryParams.ipAddr != undefined && ipRegex.test(queryParams.ipAddr)){
      queryParams.pduId = undefined;
    }
    needFlush.value++;
  }else{
    ElMessage.error('IP地址格式有误,请重新输入！')
  }
}

/** 初始化 **/
onMounted( async () => { 
  getTypeMaxValue()
  getNavList()
  // 获取路由参数中的 pdu_id
  const queryPduId = useRoute().query.pduId as string  | undefined;
  const queryLocation = useRoute().query.location as string  | undefined;
  const queryAddress = useRoute().query.address as string;
  const queryIpAddr = queryLocation?.split("-")[0];
  const queryCascadeAddr = queryLocation?.split("-")[1];
  queryParams.pduId = queryPduId ? parseInt(queryPduId, 10) : undefined;
  queryParams.ipAddr = queryIpAddr ? queryIpAddr : undefined;
  queryParams.cascadeAddr = queryCascadeAddr ? queryCascadeAddr : undefined;
  cascadeAddr.value = queryCascadeAddr ? parseInt(queryCascadeAddr, 10) : 0;
  // nowAddress.value = queryAddress ? queryAddress : '';
  if (queryParams.pduId != undefined){
    await getList(); 
    nowAddress.value = queryAddress
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
    font-size: 13px;
    padding-top: 28px;
  }
  .nav_header_img {
    width: 110px;
    height: 110px;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    border: 1px solid #555;
  }

  img {
      width: 75px;
      height: 75px;
  }

.nav_data{
  padding-left: 50px;
}

  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }

</style>