<template>
  <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="PDU环境分析" :showCheckbox="false">
    <template #NavInfo>
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
        label-width="70px"
      >
      <el-form-item label="IP地址" prop="ipAddr">
        <el-input
          v-model="queryParams.ipAddr"
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
      </el-form-item>
      <el-form-item label="传感器" prop="sensorId">
        <el-select
          v-model="queryParams.sensorId"
          class="!w-100px"
          @change="handleQuery"
          >
          <el-option
            v-for="item in sensorOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
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
                <el-table-column v-if="item.name === '最高温度'" label="温度最高值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="temMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最低温度'" label="温度最低值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="temMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '最大湿度'" label="湿度最大值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="humMaxTimeData" label="发生时间"/>
                </el-table-column>
                 <el-table-column v-else-if="item.name === '最小湿度'" label="湿度最小值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="humMinTimeData" label="发生时间"/>
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
import { CabinetApi } from '@/api/cabinet/info'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { EnvDataApi } from '@/api/pdu/envData'
import { formatDate } from '@/utils/formatTime'


/** pdu曲线 */
defineOptions({ name: 'PDUEnvLine' })
const activeName = ref('realtimeTabPane') // tab默认显示
const activeName1 = ref('myChart') // tab默认显示
const navList = ref([]) as any // 左侧导航栏树结构列表
const instance = getCurrentInstance();
const tableData = ref<Array<{ }>>([]); // 折线图表格数据
const headerData = ref<any[]>([]);
const cascadeAddr = ref(0) // 数字类型的级联地址
const needFlush = ref(0) // 是否需要刷新图表
const sensorOptions = ref([]) as any;// 传感器选项
const loading = ref(true) //  列表的加载中
const queryParams = reactive({
  pduId: undefined as number | undefined,
  sensorId: 1,
  type: 'total',
  granularity: 'realtime',
  ipAddr: undefined as string | undefined,
  cascadeAddr: '0',
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
    text: '最近七天',
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


// 处理折线图数据
const humValueData = ref<number[]>([]);
const temValueData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);

const humAvgValueData = ref<number[]>([]);
const humMaxValueData = ref<number[]>([]);
const humMaxTimeData = ref<string[]>([]);
const HumMinValueData = ref<number[]>([]);
const humMinTimeData = ref<string[]>([]);

const temAvgValueData = ref<number[]>([]);
const temMaxValueData = ref<number[]>([]);
const temMaxTimeData = ref<string[]>([]);
const temMinValueData = ref<number[]>([]);
const temMinTimeData = ref<string[]>([]);

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => {
  loading.value = true;
  try {
    const data = await EnvDataApi.getEnvDataDetails(queryParams);
    if (data != null && data.total != 0){
      isHaveData.value = true
      humValueData.value = data.list.map((item) => formatNumber(item.hum_value, 1));
      temValueData.value = data.list.map((item) => formatNumber(item.tem_value, 1));
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      }
      humAvgValueData.value = data.list.map((item) => formatNumber(item.hum_avg_value, 1));
      humMaxValueData.value = data.list.map((item) => formatNumber(item.hum_max_value, 1));
      humMaxTimeData.value = data.list.map((item) => formatDate(item.hum_max_time));
      HumMinValueData.value = data.list.map((item) => formatNumber(item.hum_min_value, 1));
      humMinTimeData.value = data.list.map((item) => formatDate(item.hum_min_time));

      temAvgValueData.value = data.list.map((item) => formatNumber(item.tem_avg_value, 1));
      temMaxValueData.value = data.list.map((item) => formatNumber(item.tem_max_value, 1));
      temMaxTimeData.value = data.list.map((item) => formatDate(item.tem_max_time));
      temMinValueData.value = data.list.map((item) => formatNumber(item.tem_min_value, 1));
      temMinTimeData.value = data.list.map((item) => formatDate(item.tem_min_time));
      
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
          legend: { data: ['温度','湿度'], selected: { 温度: true, 湿度: false}},
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {  restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: '温度', type: 'line', symbol: 'none', data: temValueData.value},
            {name: '湿度', type: 'line', symbol: 'none', data: humValueData.value},
          ],
          dataZoom:[{type: "inside"}],
        });
      }
      // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
      instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
    }
    // 图例切换监听
    setupLegendListener(realtimeChart);
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
    rowData['humMaxTimeData'] = humMaxTimeData.value[i];
    rowData['humMinTimeData'] = humMinTimeData.value[i];
    rowData['temMaxTimeData'] = temMaxTimeData.value[i];
    rowData['temMinTimeData'] = temMinTimeData.value[i];
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
watch(() => [activeName.value, needFlush.value], async (newValues) => {
    const [newActiveName] = newValues;
    // 处理参数变化
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
              legend: { data: ['温度','湿度'], selected: { 温度: true, 湿度: false}},
              grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
              toolbox: {feature: {  restore:{}, saveAsImage: {}}},
              xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
              yAxis: { type: 'value'},
              series: [
                {name: '温度', type: 'line', symbol: 'none', data: temValueData.value},
                {name: '湿度', type: 'line', symbol: 'none', data: humValueData.value},
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
          realtimeChart.setOption({     
            title: {text: ''},
            tooltip: { trigger: 'axis', formatter: customTooltipFormatter},
            legend: { data: ['平均温度', '最高温度', '最低温度','平均湿度', '最大湿度', '最小湿度'],
                      selected: { 平均温度: true, 最高温度: true, 最低温度: true, 
                      平均湿度: false, 最大湿度: false, 最小湿度: false, }
            },
            grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value}
            ],
            yAxis: { type: 'value'},
            series: [
              { name: '平均温度', type: 'line', symbol: 'none', data: temAvgValueData.value, },
              { name: '最高温度', type: 'line', symbol: 'none', data: temMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最低温度', type: 'line', symbol: 'none', data: temMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '平均湿度', type: 'line', symbol: 'none', data: humAvgValueData.value, },
              { name: '最大湿度', type: 'line', symbol: 'none', data: humMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '最小湿度', type: 'line', symbol: 'none', data: HumMinValueData.value, lineStyle: {type: 'dashed'}},
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
});

// 实时图例切换函数
function setupLegendListener(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '温度':
        if (params.selected[legendName]){
          optionsToUpdate = { "温度": true, "湿度": false};
        }
        break;

      case '湿度':
        if (params.selected[legendName]){
          optionsToUpdate = { "温度": false, "湿度": true};
        }
        break;

      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['温度', '湿度'],
        selected: optionsToUpdate
      },
    });
  });
}

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; // X 轴数值
  params.forEach(function(item) {
    switch( item.seriesName ){
      case '温度':
      case '平均温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最高温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +temMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最低温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +temMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '湿度':
      case '平均湿度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' %RH  记录时间: ' +params[0].name + '<br/>';
        break;
      case '最大湿度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' %RH  发生时间: ' +humMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '最小湿度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' %RH  发生时间: ' +humMinTimeData.value[item.dataIndex] + '<br/>';
        break;
    }
    
  });
  return tooltipContent;
}

// 小时、天 图例切换函数
function setupLegendListener1(realtimeChart) {
  realtimeChart?.on('legendselectchanged', function (params) {
    var legendName = params.name;
    var optionsToUpdate = {};
    switch (legendName) {
      case '平均温度':
      case '最高温度':
      case '最低温度':
        if (params.selected[legendName]){
          optionsToUpdate = { "平均温度": true, "最高温度": true, "最低温度": true, "平均湿度": false, "最大湿度": false, "最小湿度": false};
        }
        break;

      case '平均湿度':
      case '最大湿度':
      case '最小湿度':
        if (params.selected[legendName]){
          optionsToUpdate = { "平均湿度": true, "最大湿度": true, "最小湿度": true, "平均温度": false, "最高温度": false, "最低温度": false};
        }
        break;

      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['平均温度', '最高温度', '最低温度','平均湿度', '最大湿度', '最小湿度'],
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
  today.setHours(0, 0, 0, 0);
  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);
  // 如果date在今天之后，则禁用
  return date > today;
}

// 处理实时数据的时间选择不超过xxx范围
// const handleDayPick = () => {
//   if (activeName.value=='realtimeTabPane'){
//     // 获取选择的开始日期和结束日期
//     const startDate = new Date(queryParams.timeRange[0]);
//     const endDate = new Date(queryParams.timeRange[1]);
//     // 计算两个日期之间的天数差
//     const diffTime = Math.abs(endDate.getTime() - startDate.getTime()); 
//     const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
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
//     // 获取选择的开始日期和结束日期
//     const startDate = new Date(queryParams.timeRange[0]);
//     const endDate = new Date(queryParams.timeRange[1]);
//     // 计算两个日期之间的天数差
//     const diffTime = Math.abs(endDate.getTime() - startDate.getTime()); 
//     const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
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
//     // 获取选择的开始日期和结束日期
//     const startDate = new Date(queryParams.timeRange[0]);
//     const endDate = new Date(queryParams.timeRange[1]);
//     // 计算两个日期之间的天数差
//     const diffTime = Math.abs(endDate.getTime() - startDate.getTime()); 
//     const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
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
const getSensorIdMaxValue = async () => {
  const data = await EnvDataApi.getSensorIdMaxValue()
  const sensorIdMaxValue = data.sensor_id_max_value;
  const sensorSelectionValue: { value: number; label: string; }[] = [];
  for (let i = 1; i <= sensorIdMaxValue; i++) {
    sensorSelectionValue.push({ value: i, label: `${i}`});
  }
  sensorOptions.value = sensorSelectionValue;
}

// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 4){
    queryParams.pduId = undefined
    queryParams.ipAddr = row.ip
    queryParams.cascadeAddr = row?.unique?.split("-")[1];
    handleQuery();
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
  getSensorIdMaxValue()
  getNavList()
  // 获取路由参数中的 pdu_id
  const queryPduId = useRoute().query.pduId as string | undefined;
  const querySensorId = useRoute().query.sensorId as string | undefined;
  const queryLocation = useRoute().query.location as string | undefined;
  const queryIpAddr = queryLocation?.split("-")[0];
  const queryCascadeAddr = queryLocation?.split("-")[1];
  queryParams.pduId = queryPduId ? parseInt(queryPduId, 10) : undefined;
  queryParams.sensorId = querySensorId ? parseInt(querySensorId, 10) : 1;
  queryParams.ipAddr = queryIpAddr ? queryIpAddr : undefined;
  queryParams.cascadeAddr = queryCascadeAddr ? queryCascadeAddr : '0';
  cascadeAddr.value = queryCascadeAddr ? parseInt(queryCascadeAddr, 10) : 0;
  if (queryParams.pduId != undefined){
    await getList();
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
</style>