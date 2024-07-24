<template>
  <CommonMenu :dataList="navList" @node-click="handleClick" navTitle="始端箱环境数据分析" :showCheckbox="false" placeholder="如:192.168.1.96-0">
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
        <span v-if="nowIpAddr">( {{nowIpAddr}} ) </span>
        <br/>
        <template v-if="queryParams.granularity == 'realtime'">
          <span>{{queryParams.timeRange[0]}}</span>
          <span>至</span>
          <span>{{queryParams.timeRange[1]}}</span>
        </template>
        <br/>
      </div>
      <div class="nav_content" v-if="queryParams.granularity == 'realtime'">
        <el-descriptions title="" direction="vertical" :column="1" border >
          <el-descriptions-item label="A路最高温度 | 发生时间">
            <span>{{ formatNumber(maxTemDataTemp, 1) }} kWh</span><br/>
            <span v-if="maxTemDataTimeTemp">{{ maxTemDataTimeTemp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="A路最低温度 | 发生时间">
            <span>{{ formatNumber(minTemDataTemp, 1) }} kWh</span><br/>
            <span v-if="minTemDataTimeTemp">{{ minTemDataTimeTemp }}</span>
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
        label-width="70px"
      >

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

        <el-form-item>
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
                <el-table-column v-if="item.name === 'A路最高温度'" label="A路温度最高值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="aTemMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === 'A路最低温度'" label="A路温度最低值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="aTemMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-if="item.name === 'B路最高温度'" label="B路温度最高值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="bTemMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === 'B路最低温度'" label="B路温度最低值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="bTemMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-if="item.name === 'C路最高温度'" label="C路温度最高值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="cTemMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === 'C路最低温度'" label="C路温度最低值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="cTemMinTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-if="item.name === '中线最高温度'" label="中线温度最高值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="nTemMaxTimeData" label="发生时间"/>
                </el-table-column>
                <el-table-column v-else-if="item.name === '中线最低温度'" label="中线温度最低值">
                  <el-table-column :prop="item.name" label="数值"/>   
                  <el-table-column prop="nTemMinTimeData" label="发生时间"/>
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
import { IndexApi } from '@/api/bus/busindex'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { EnvDataApi } from '@/api/bus/envData'
import { formatDate } from '@/utils/formatTime'
import PDUImage from '@/assets/imgs/PDU.jpg'
import { ElMessage } from 'element-plus'
defineOptions({ name: 'BusEnvLine' })

const activeName = ref('realtimeTabPane') // tab默认显示
const activeName1 = ref('myChart') // tab默认显示
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('') as any// 导航栏的位置信息
const nowIpAddr = ref('')// 导航栏的位置信息
const instance = getCurrentInstance();
const tableData = ref<Array<{ }>>([]); // 折线图表格数据
const headerData = ref<any[]>([]);
const needFlush = ref(0) // 是否需要刷新图表
const loading = ref(false) //  列表的加载中
const queryParams = reactive({
  busId: undefined as number | undefined,
  granularity: 'realtime',
  devkey: undefined as string | undefined,
  // 进入页面原始数据默认显示最近一小时
  timeRange: defaultHourTimeRange(1)
})
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径

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
const aTemValueData = ref<number[]>([]);
const bTemValueData = ref<number[]>([]);
const cTemValueData = ref<number[]>([]);
const nTemValueData = ref<number[]>([]);
const createTimeData = ref<string[]>([]);

const aTemAvgValueData = ref<number[]>([]);
const aTemMaxValueData = ref<number[]>([]);
const aTemMaxTimeData = ref<string[]>([]);
const aTemMinValueData = ref<number[]>([]);
const aTemMinTimeData = ref<string[]>([]);

const bTemAvgValueData = ref<number[]>([]);
const bTemMaxValueData = ref<number[]>([]);
const bTemMaxTimeData = ref<string[]>([]);
const bTemMinValueData = ref<number[]>([]);
const bTemMinTimeData = ref<string[]>([]);

const cTemAvgValueData = ref<number[]>([]);
const cTemMaxValueData = ref<number[]>([]);
const cTemMaxTimeData = ref<string[]>([]);
const cTemMinValueData = ref<number[]>([]);
const cTemMinTimeData = ref<string[]>([]);

const nTemAvgValueData = ref<number[]>([]);
const nTemMaxValueData = ref<number[]>([]);
const nTemMaxTimeData = ref<string[]>([]);
const nTemMinValueData = ref<number[]>([]);
const nTemMinTimeData = ref<string[]>([]);

const maxTemDataTemp = ref(0);// 最高温度
const maxTemDataTimeTemp = ref();// 最高温度的发生时间 
const minTemDataTemp = ref(0);// 最低温度 
const minTemDataTimeTemp = ref();// 最低温度的发生时间 

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => {
  loading.value = true;
  try {
    const data = await EnvDataApi.getBusEnvDataDetails(queryParams);
    if (data != null && data.total != 0){
      isHaveData.value = true
      aTemValueData.value = data.list.map((item) => formatNumber(item.tem_a, 1));
      bTemValueData.value = data.list.map((item) => formatNumber(item.tem_b, 1));
      cTemValueData.value = data.list.map((item) => formatNumber(item.tem_c, 1));
      nTemValueData.value = data.list.map((item) => formatNumber(item.tem_n, 1));
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time));
      }

      aTemAvgValueData.value = data.list.map((item) => formatNumber(item.tem_a_avg_value, 1));
      aTemMaxValueData.value = data.list.map((item) => formatNumber(item.tem_a_max_value, 1));
      aTemMaxTimeData.value = data.list.map((item) => formatDate(item.tem_a_max_time));
      aTemMinValueData.value = data.list.map((item) => formatNumber(item.tem_a_min_value, 1));
      aTemMinTimeData.value = data.list.map((item) => formatDate(item.tem_a_min_time));

      bTemAvgValueData.value = data.list.map((item) => formatNumber(item.tem_b_avg_value, 1));
      bTemMaxValueData.value = data.list.map((item) => formatNumber(item.tem_b_max_value, 1));
      bTemMaxTimeData.value = data.list.map((item) => formatDate(item.tem_b_max_time));
      bTemMinValueData.value = data.list.map((item) => formatNumber(item.tem_b_min_value, 1));
      bTemMinTimeData.value = data.list.map((item) => formatDate(item.tem_b_min_time));

      cTemAvgValueData.value = data.list.map((item) => formatNumber(item.tem_c_avg_value, 1));
      cTemMaxValueData.value = data.list.map((item) => formatNumber(item.tem_c_max_value, 1));
      cTemMaxTimeData.value = data.list.map((item) => formatDate(item.tem_c_max_time));
      cTemMinValueData.value = data.list.map((item) => formatNumber(item.tem_c_min_value, 1));
      cTemMinTimeData.value = data.list.map((item) => formatDate(item.tem_c_min_time));

      nTemAvgValueData.value = data.list.map((item) => formatNumber(item.tem_n_avg_value, 1));
      nTemMaxValueData.value = data.list.map((item) => formatNumber(item.tem_n_max_value, 1));
      nTemMaxTimeData.value = data.list.map((item) => formatDate(item.tem_n_max_time));
      nTemMinValueData.value = data.list.map((item) => formatNumber(item.tem_n_min_value, 1));
      nTemMinTimeData.value = data.list.map((item) => formatDate(item.tem_n_min_time));

      maxTemDataTemp.value = Math.max(...aTemValueData.value);
      minTemDataTemp.value = Math.min(...aTemValueData.value);
      aTemValueData.value.forEach(function(num, index) {
        if (num == maxTemDataTemp.value){
          maxTemDataTimeTemp.value = createTimeData.value[index]
        }
        if (num == minTemDataTemp.value){
          minTemDataTimeTemp.value = createTimeData.value[index]
        }
      });

      
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
          legend: { data: ['A路温度','B路温度','C路温度','中线温度'], selected: { A路温度: true, B路温度: true, C路温度: true, 中线温度: true}},
          grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
          toolbox: {feature: {  restore:{}, saveAsImage: {}}},
          xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
          yAxis: { type: 'value'},
          series: [
            {name: 'A路温度', type: 'line', symbol: 'none', data: aTemValueData.value},
            {name: 'B路温度', type: 'line', symbol: 'none', data: bTemValueData.value},
            {name: 'C路温度', type: 'line', symbol: 'none', data: cTemValueData.value},
            {name: '中线温度', type: 'line', symbol: 'none', data: nTemValueData.value},
          ],
          dataZoom:[{type: "inside"}],
        });
      }
      // 将 realtimeChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
      instance.appContext.config.globalProperties.realtimeChart = realtimeChart;
    }
    // 图例切换监听
    // setupLegendListener(realtimeChart);
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
    rowData['aTemMaxTimeData'] = aTemMaxTimeData.value[i];
    rowData['aTemMinTimeData'] = aTemMinTimeData.value[i];
    rowData['bTemMaxTimeData'] = bTemMaxTimeData.value[i];
    rowData['bTemMinTimeData'] = bTemMinTimeData.value[i];
    rowData['cTemMaxTimeData'] = cTemMaxTimeData.value[i];
    rowData['cTemMinTimeData'] = cTemMinTimeData.value[i];
    rowData['nTemMaxTimeData'] = nTemMaxTimeData.value[i];
    rowData['nTemMinTimeData'] = nTemMinTimeData.value[i];
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
            legend: { data: ['A路温度','B路温度','C路温度','中线温度'], selected: { A路温度: true, B路温度: true, C路温度: true, 中线温度: true}},
            grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
            xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
            yAxis: { type: 'value'},
            series: [
              {name: 'A路温度', type: 'line', symbol: 'none', data: aTemValueData.value},
              {name: 'B路温度', type: 'line', symbol: 'none', data: bTemValueData.value},
              {name: 'C路温度', type: 'line', symbol: 'none', data: cTemValueData.value},
              {name: '中线温度', type: 'line', symbol: 'none', data: nTemValueData.value},
            ],
            dataZoom:[{type: "inside"}],
          });
          }
          // 图例切换监听
          // setupLegendListener(realtimeChart);
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
            legend: { 
                      data: ['A路平均温度', 'A路最高温度', 'A路最低温度', 'B路平均温度', 'B路最高温度', 'B路最低温度',
                              'C路平均温度', 'C路最高温度', 'C路最低温度', '中线平均温度', '中线最高温度', '中线最低温度'],
                      selected: { A路平均温度: true, A路最高温度: true, A路最低温度: true, 
                                  B路平均温度: false, B路最高温度: false, B路最低温度: false,
                                  C路平均温度: false, C路最高温度: false, C路最低温度: false, 
                                  中线平均温度: false, 中线最高温度: false, 中线最低温度: false,  }
            },
            grid: {left: '3%', right: '4%', bottom: '3%', containLabel: true },
            toolbox: {feature: {  restore:{}, saveAsImage: {}}},
            xAxis: [
              {type: 'category', boundaryGap: false, data: createTimeData.value}
            ],
            yAxis: { type: 'value'},
            series: [
              { name: 'A路平均温度', type: 'line', symbol: 'none', data: aTemAvgValueData.value, },
              { name: 'A路最高温度', type: 'line', symbol: 'none', data: aTemMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'A路最低温度', type: 'line', symbol: 'none', data: aTemMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'B路平均温度', type: 'line', symbol: 'none', data: bTemAvgValueData.value, },
              { name: 'B路最高温度', type: 'line', symbol: 'none', data: bTemMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'B路最低温度', type: 'line', symbol: 'none', data: bTemMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'C路平均温度', type: 'line', symbol: 'none', data: cTemAvgValueData.value, },
              { name: 'C路最高温度', type: 'line', symbol: 'none', data: cTemMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: 'C路最低温度', type: 'line', symbol: 'none', data: cTemMinValueData.value, lineStyle: {type: 'dashed'}},
              { name: '中线平均温度', type: 'line', symbol: 'none', data: nTemAvgValueData.value, },
              { name: '中线最高温度', type: 'line', symbol: 'none', data: nTemMaxValueData.value, lineStyle: {type: 'dashed'}},
              { name: '中线最低温度', type: 'line', symbol: 'none', data: nTemMinValueData.value, lineStyle: {type: 'dashed'}},
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
// function setupLegendListener(realtimeChart) {
//   realtimeChart?.on('legendselectchanged', function (params) {
//     var legendName = params.name;
//     var optionsToUpdate = {};
//     switch (legendName) {
//       case '温度':
//         if (params.selected[legendName]){
//           optionsToUpdate = { "温度": true, "湿度": false};
//         }
//         break;

//       case '湿度':
//         if (params.selected[legendName]){
//           optionsToUpdate = { "温度": false, "湿度": true};
//         }
//         break;

//       default:
//         break;
//     }

//     realtimeChart?.setOption({
//       legend: {
//         data: ['温度', '湿度'],
//         selected: optionsToUpdate
//       },
//     });
//   });
// }

// 给折线图提示框的数据加单位
function customTooltipFormatter(params: any[]) {
  var tooltipContent = ''; // X 轴数值
  params.forEach(function(item) {
    switch( item.seriesName ){
      case 'A路温度':
      case 'B路温度':
      case 'C路温度':
      case '中线温度':
      case 'A路平均温度':
      case 'B路平均温度':
      case 'C路平均温度':
      case '中线平均温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  记录时间: ' +params[0].name + '<br/>';
        break;
      case 'A路最高温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +aTemMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case 'B路最高温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +bTemMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case 'C路最高温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +cTemMaxTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '中线最高温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +nTemMaxTimeData.value[item.dataIndex] + '<br/>';
        break;

      case 'A路最低温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +aTemMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case 'B路最低温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +bTemMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case 'C路最低温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +cTemMinTimeData.value[item.dataIndex] + '<br/>';
        break;
      case '中线最低温度':
        tooltipContent += item.marker + ' ' + item.seriesName + ': ' + item.value + ' ℃  发生时间: ' +nTemMinTimeData.value[item.dataIndex] + '<br/>';
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
      case 'A路平均温度':
      case 'A路最高温度':
      case 'A路最低温度':
        if (params.selected[legendName]){
          optionsToUpdate = { "A路平均温度": true, "A路最高温度": true, "A路最低温度": true, "B路平均温度": false, "B路最高温度": false, "B路最低温度": false,
                              "C路平均温度": false, "C路最高温度": false, "C路最低温度": false, "中线平均温度": false, "中线最高温度": false, "中线最低温度": false,};
        }
        break;
      case 'B路平均温度':
      case 'B路最高温度':
      case 'B路最低温度':
        if (params.selected[legendName]){
          optionsToUpdate = { "B路平均温度": true, "B路最高温度": true, "B路最低温度": true, "A路平均温度": false, "A路最高温度": false, "A路最低温度": false,
                              "C路平均温度": false, "C路最高温度": false, "C路最低温度": false, "中线平均温度": false, "中线最高温度": false, "中线最低温度": false,};
        }
        break;
      case 'C路平均温度':
      case 'C路最高温度':
      case 'C路最低温度':
        if (params.selected[legendName]){
          optionsToUpdate = { "C路平均温度": true, "C路最高温度": true, "C路最低温度": true, "A路平均温度": false, "A路最高温度": false, "A路最低温度": false,
                              "B路平均温度": false, "B路最高温度": false, "B路最低温度": false, "中线平均温度": false, "中线最高温度": false, "中线最低温度": false,};
        }
        break;
      case '中线平均温度':
      case '中线最高温度':
      case '中线最低温度':
        if (params.selected[legendName]){
          optionsToUpdate = { "中线平均温度": true, "中线最高温度": true, "中线最低温度": true, "A路平均温度": false, "A路最高温度": false, "A路最低温度": false,
                              "B路平均温度": false, "B路最高温度": false, "B路最低温度": false, "C路平均温度": false, "C路最高温度": false, "C路最低温度": false,};
        }
        break;

      default:
        break;
    }

    realtimeChart?.setOption({
      legend: {
        data: ['A路平均温度', 'A路最高温度', 'A路最低温度','B路平均温度', 'B路最高温度', 'B路最低温度','C路平均温度', 'C路最高温度', 'C路最低温度','中线平均温度', '中线最高温度', '中线最低温度'],
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


// 导航栏选择后触发
const handleClick = async (row) => {
   if(row.type != null  && row.type == 6){
    nowIpAddr.value = ''
    maxTemDataTemp.value = 0
    minTemDataTemp.value = 0

    queryParams.devkey = row.unique
    findFullName(navList.value, row.unique, fullName => {
      nowAddress.value = fullName
    });
    let data: any[] = [];
    tableData.value = data;
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
    // queryParams.busId = undefined;
    needFlush.value++;
}

/** 初始化 **/
onMounted( async () => {
  getNavList()
  // 获取路由参数中的 pdu_id
  let queryBusId = useRoute().query.busId as string | undefined;
  let queryDevKey = useRoute().query.devKey as string;
  let queryLocation = useRoute().query.location as string;
  queryParams.busId = queryBusId ? parseInt(queryBusId, 10) : undefined;
  if (queryParams.busId != undefined){
    await getList();
    if (queryLocation == null) {
      nowAddress.value = '';
    } else {
      nowAddress.value = queryLocation;
    }
    nowIpAddr.value = queryDevKey
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
    font-size: 15px;
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