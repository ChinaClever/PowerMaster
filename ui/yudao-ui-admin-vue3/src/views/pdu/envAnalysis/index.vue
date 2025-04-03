<template>
  <CommonMenu1 :dataList="navList" @node-click="handleClick" navTitle="环境数据分析" :showCheckbox="false" placeholder="如:192.168.1.96-0">
    <template #NavInfo>
      <br/>    <br/> 
      <div class="nav_data">
       
    <div class="nav_header" style="font-size: 14px;">
          <span v-if="nowAddress">{{nowAddress}}</span>
          <span v-if="nowLocation">( {{nowLocation}} ) </span>
          <br/>
      </div>
    <div class="descriptions-container" v-if="maxTemDataTimeTemp" style="font-size: 14px;">
                <!-- 处理原始数据和小时极值数据的菜单栏 -->
      <div v-if="queryParams.granularity != 'day'&& queryParams.timeRange != null" class="description-item" > 
            <span class="label">开始时间 :</span>
            <span class="value">{{   formatTime(queryParams.timeRange[0])  }}</span>
          </div>
          
          <div  v-if="queryParams.granularity != 'day'  && queryParams.timeRange != null" class="description-item">
            <span class="label">结束时间 :</span>
            <span class="value">{{ formatTime(queryParams.timeRange[1])}}</span>
          </div>


          <div  class="description-item" v-if="queryParams.granularity != 'day'" >
            <span class="label">最高温度 :</span>
            <span >{{ formatNumber(maxTemDataTemp, 1)}} ℃</span>
          </div>
          <div v-if="maxTemDataTimeTemp &&queryParams.granularity != 'day'" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ formatTime(maxTemDataTimeTemp) }}</span>
          </div>

          <div class="description-item" v-if="queryParams.granularity != 'day'">
              <span class="label">最低温度 :</span>
              <span >{{ formatNumber(minTemDataTemp, 1)}}℃ </span>
            </div>
          <div v-if="minTemDataTimeTemp &&queryParams.granularity != 'day'" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ formatTime(minTemDataTimeTemp) }}</span>
          </div>

          <!-- 处理天极值数据的菜单栏 -->
          <div v-if="queryParams.granularity == 'day'&& queryParams.timeRange != null" class="description-item" > 
            <span class="label">开始时间 :</span>
            <span class="value">{{   formatTime(queryParams.timeRange[0])  }}</span>
          </div>
          
          <div  v-if="queryParams.granularity == 'day'  && queryParams.timeRange != null" class="description-item">
            <span class="label">结束时间 :</span>
            <span class="value">{{ formatTime(queryParams.timeRange[1])}}</span>
          </div>
          <div  class="description-item" v-if="queryParams.granularity == 'day'" >
            <span class="label">最高温度 :</span>
            <span >{{ maxTemDataTemp}} ℃</span>
          </div>
          <div v-if="maxTemDataTimeTemp &&queryParams.granularity == 'day'" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ formatTime(maxTemDataTimeTemp) }}</span>
          </div>

          <div class="description-item" v-if="queryParams.granularity == 'day'">
              <span class="label">最低温度 :</span>
              <span >{{minTemDataTemp}}℃ </span>
            </div>
          <div v-if="minTemDataTimeTemp &&queryParams.granularity == 'day'" class="description-item">
            <span class="label">发生时间 :</span>
            <span class="value">{{ formatTime(minTemDataTimeTemp) }}</span>
          </div>
          
          <div class="line"></div>
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
      <el-form-item label="传感器" prop="detect">
        <el-select
          v-model="detect"
          class="!w-130px"
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
          
        </el-form-item>

        <el-button type="success" plain @click="handleExport1" :loading="exportLoading" style="float: right;">
             <Icon icon="ep:download" class="mr-5px" /> 导出
           </el-button>
      </el-form>
    </template>
    
    <template #Content>
      <div v-loading="loading">
        <el-tabs v-model="activeName1" v-if="loading2">
          <el-tab-pane label="图表" name="myChart">
            <div ref="chartContainer" id="chartContainer" class="adaptiveStyle"></div>
          </el-tab-pane>
          
          <el-tab-pane label="数据" name="myData">
            <div style="height: 67vh;">
              <el-table  
              :stripe="true" 
                :border="true"
                :data="tableData"
                style="height: 100%; width: 99.97%;"
                :header-cell-style="{ backgroundColor: '#F5F7FA', color: '#909399', textAlign: 'center', borderLeft: '1px #EDEEF2 solid', fontSize: '14px',borderBottom: '1px #EDEEF2 solid',fontWeight: 'bold' }"
                :cell-style="{ color: '#606266', fontSize: '14px', textAlign: 'center', borderBottom: '0.25px #F5F7FA solid', borderLeft: '0.25px #F5F7FA solid' }"
                :row-style="{ fontSize: '14px', textAlign: 'center', }"
                empty-text="暂无数据" max-height="818"
                >
              <!-- 添加行号列 -->
                <el-table-column label="序号" align="center" width="100px">
                  <template #default="{ $index }">
                    {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
                  </template>
                </el-table-column>
                <el-table-column  prop="create_time" label="记录时间" />
                <!-- 动态生成表头 -->
                <template v-for="item in headerData" :key="item.name">
                  <el-table-column v-if="item.name === '最高温度'" label="温度最高值">
                    <el-table-column :prop="item.name" label="温度最高值℃"/>   
                    <el-table-column prop="temMaxTimeData" label="发生时间"/>
                  </el-table-column>

                  <el-table-column v-else-if="item.name === '最低温度'" label="温度最低值">
                    <el-table-column :prop="item.name" label="温度最低值℃"/>   
                    <el-table-column prop="temMinTimeData" label="发生时间"/>
                  </el-table-column>
                  <el-table-column v-else-if="item.name === '最大湿度'" label="湿度最大值">
                    <el-table-column :prop="item.name" label="湿度最大值(%RH)"/>   
                    <el-table-column prop="humMaxTimeData" label="发生时间"/>
                  </el-table-column>
                   <el-table-column v-else-if="item.name === '最小湿度'" label="湿度最小值">
                    <el-table-column :prop="item.name" label="湿度最小值(%RH)"/>   
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
  </CommonMenu1>
</template>
<script setup lang="ts">
import { CabinetApi } from '@/api/cabinet/info'
import * as echarts from 'echarts';
import { onMounted } from 'vue'
import { EnvDataApi } from '@/api/pdu/envData'
import { formatDate } from '@/utils/formatTime'
import PDUImage from '@/assets/imgs/PDU.jpg'
import dayjs from 'dayjs'
import download from '@/utils/download'
import { HistoryDataApi } from '@/api/pdu/historydata'
import  CommonMenu1 from './CommonMenu1.vue'


/** pdu曲线 */
defineOptions({ name: 'PDUEnvLine' })
import { ElMessage } from 'element-plus'
const activeName = ref('realtimeTabPane') // tab默认显示
const activeName1 = ref('myChart') // tab默认显示
const navList = ref([]) as any // 左侧导航栏树结构列表
const nowAddress = ref('') as any// 导航栏的位置信息
const nowLocation = ref('')// 导航栏的位置信息
const instance = getCurrentInstance();
const tableData = ref<Array<{ }>>([]); // 折线图表格数据
const headerData = ref<any[]>([]);
const needFlush = ref(0) // 是否需要刷新图表
const loading = ref(false) //  列表的加载中
const detect = ref('') as any// 监测点的值 默认全部
const message = useMessage() // 消息弹窗
const exportLoading = ref(false)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  pduId: undefined as number | undefined,
  sensorId: undefined as number | undefined,
  channel: undefined as number | undefined,
  position: undefined as number | undefined,
  nowAddress: undefined as string | undefined,
  pduKey: undefined as string | undefined,
  granularity: 'realtime',
  // ipAddr: undefined as string | undefined,
  // cascadeAddr: '0',
  cabinetId: undefined as number | undefined,
  // 进入页面原始数据默认显示最近一小时
  timeRange: defaultHourTimeRange(1)
})
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
// 传感器选项
const sensorOptions = ref([
  { value: "1", label: '传感器1'},
  { value: "2", label: '传感器2'},
  { value: "3", label: '传感器3'},
  { value: "4", label: '传感器4'},
  
]) 

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

const maxTemDataTemp = ref(0);// 最高温度
const maxTemDataTimeTemp = ref();// 最高温度的发生时间 
const minTemDataTemp = ref(0);// 最低温度 
const minTemDataTimeTemp = ref();// 最低温度的发生时间 
const a=ref(0)
const b=ref(0)
const loading2=ref(false)

/** 查询列表 */
const isHaveData = ref(false);
const getList = async () => { 
  loading.value = true;
  try {
    // 
    const data = await EnvDataApi.getEnvDataDetails(queryParams);
    if (data != null && data.total != 0){
      loading2.value=true
      isHaveData.value = true
      temValueData.value = data.list.map((item) => formatNumber(item.tem_value, 1));
      humValueData.value = data.list.map((item) => formatNumber(item.hum_value, 0));
      if (activeName.value === 'dayExtremumTabPane'){
        createTimeData.value = data.list.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }else{
        createTimeData.value = data.list.map((item) => formatDate(item.create_time,"YYYY-MM-DD HH:mm"));
      }
      humAvgValueData.value = data.list.map((item) => formatNumber(item.hum_avg_value, 0));
      humMaxValueData.value = data.list.map((item) => formatNumber(item.hum_max_value, 0));
      humMaxTimeData.value = data.list.map((item) => formatDate(item.hum_max_time,"YYYY-MM-DD HH:mm"));
      HumMinValueData.value = data.list.map((item) => formatNumber(item.hum_min_value, 0));
      humMinTimeData.value = data.list.map((item) => formatDate(item.hum_min_time,"YYYY-MM-DD HH:mm"));

      temAvgValueData.value = data.list.map((item) => formatNumber(item.tem_avg_value, 1));
      temMaxValueData.value = data.list.map((item) => formatNumber(item.tem_max_value, 1));
      temMaxTimeData.value = data.list.map((item) => formatDate(item.tem_max_time,"YYYY-MM-DD HH:mm"));
      temMinValueData.value = data.list.map((item) => formatNumber(item.tem_min_value, 1));
      temMinTimeData.value = data.list.map((item) => formatDate(item.tem_min_time,"YYYY-MM-DD HH:mm"));
      
      if(activeName.value == "realtimeTabPane"){
      maxTemDataTemp.value = Math.max(...temValueData.value);
      minTemDataTemp.value = Math.min(...temValueData.value);
      temValueData.value.forEach(function(num, index) {
        if (num == maxTemDataTemp.value&&a.value==0){
          maxTemDataTimeTemp.value = createTimeData.value[index]
          a.value=1;
        }
          if (num == minTemDataTemp.value&&b.value==0){
          minTemDataTimeTemp.value = createTimeData.value[index]
          b.value=1;
        }});
      }
      else if(activeName.value != "realtimeTabPane"){
      maxTemDataTemp.value = Math.max(...temMaxValueData.value);
      minTemDataTemp.value = Math.min(...temMinValueData.value);
      temMaxValueData.value.forEach(function(num, index) {
        if (num == maxTemDataTemp.value&&a.value==0){
          
          maxTemDataTimeTemp.value = createTimeData.value[index]
          a.value=1;

        }});
      temMinValueData.value.forEach(function(num, index) {
          if (num == minTemDataTemp.value&&b.value==0){
          
          minTemDataTimeTemp.value = createTimeData.value[index]
          b.value=1;     
        }});
      }
      // 图表显示的ip变化
      nowLocation.value = data.ipAddr
    }else{
      isHaveData.value = false;
      loading2.value=false
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
  } catch (error) {
    ElMessage({
      message: '暂无数据',
      type: 'warning',
    });
} 
  finally {
    loading.value = false;
    a.value=0;
    b.value=0;
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
  //debugger
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
        //debugger
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
      //debugger
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
        tooltipContent += item.marker +' 记录时间: ' +params[0].name +  ' ' + item.seriesName + ': ' + item.value + '℃  <br/>';
        break;
      case '最高温度':
        tooltipContent += item.marker +' 发生时间: ' +temMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + '℃  <br/>';
        break;
      case '最低温度':
        tooltipContent += item.marker +' 发生时间: ' +temMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + '℃  <br/>';
        break;
      case '湿度':
      case '平均湿度':
        tooltipContent += item.marker +' 记录时间: ' +params[0].name +  ' ' + item.seriesName + ': ' + item.value + '%RH  <br/>';
        break;
      case '最大湿度':
        tooltipContent += item.marker +' 发生时间: ' +humMaxTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + '%RH  <br/>';
        break;
      case '最小湿度':
        tooltipContent += item.marker +' 发生时间: ' +humMinTimeData.value[item.dataIndex] +  ' ' + item.seriesName + ': ' + item.value + '%RH  <br/>';
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
        return Number(value).toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

// 格式化日期
function formatTime( cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD HH:mm')
}
function formatDayTime( cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD')
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

//导出Excel
const handleExport1 = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    queryParams.nowAddress =nowAddress.value
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await HistoryDataApi.exportEnvHistorydetailsPageData(queryParams, axiosConfig)
    await download.excel(data, 'PDU环境分析历史数据详情.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
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
   if(row.type != null  && row.type == 4){
    queryParams.pduKey = row.unique
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
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
}
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();
/** 搜索按钮操作 */
const handleQuery = () => {
  
    
   // const firstChar = detect.value[0];
    const secondChar = detect.value[0];
    if (secondChar != null){    
    // queryParams.channel = Number(firstChar);
    queryParams.sensorId = Number(secondChar);
    // 更新路由查询参数
    const querySensorId = String(Number(secondChar));
        router.push({
            query: {
                ...route.query, // 保留现有查询参数
                sensorId: querySensorId // 添加或更新 sensorId 参数
            }
        });
    }
    needFlush.value++;
}
/** 初始化 **/
onMounted( async () => {
  console.log('22231');
  getNavList()
  // 获取路由参数中的 pdu_id
  let queryPduId = useRoute().query.pduId as string | undefined;
  let querySensorId = useRoute().query.sensorId as string | undefined;
  detect.value = querySensorId;
        console.log(detect);
      console.log(detect.value); // 打印最新的值
  let queryLocation = useRoute().query.location as string;
  let queryAddress = useRoute().query.address as string;
  let queryDetectValue = useRoute().query.detectValue as string;
  // queryParams.pduId = queryPduId ? parseInt(queryPduId, 10) : undefined;
  queryParams.pduKey =queryLocation;
  queryParams.sensorId = querySensorId ? parseInt(querySensorId, 10) : undefined;
  if (queryParams.pduKey != undefined){
    await getList();
    if (queryAddress == null) {
      nowAddress.value = '该设备还未绑定机房';
    } else {
      nowAddress.value = queryAddress;
    }
    nowLocation.value = queryLocation
    // detect.value = queryDetectValue == null ? undefined : queryDetectValue
    initChart();
  }
})



</script>

<style scoped>
/*  
// 表格部分样式
// 最外层透明 */
/* :deep( .el-table),
:deep( .el-table__expanded-cell) {
  background-color: transparent;
  color: #93dcfe;
  font-size: 24px;
} */
 
/* 表格内背景颜色  */
/* :deep( .el-table th),
:deep( .el-table tr),
:deep( .el-table td) {
  background-color: transparent;
  border: 0px;
  color: #93dcfe;
  font-size: 24px;
  height: 5px;
  font-family: Source Han Sans CN Normal, Source Han Sans CN Normal-Normal;
  font-weight: Normal;
} */
 
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
  font-size: 14px;
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
.description-item {
  display: flex;
  align-items: center;
}

.label {
  text-align: right; /* 文本右对齐 */
  margin-right: 10px; /* 控制冒号后的间距 */
  text-align: left;
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
  text-align: left;

}
  .line {
    height: 1px;
    margin-top: 28px;

    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }

@media (min-width:2048px) {
  .adaptiveStyle {
    width: 76vw;
    height: 65vh;
  }
}

@media (max-width:2048px) and (min-width:1600px) {
  .adaptiveStyle {
    width: 70vw;
    height: 65vh;
  }
}

@media (max-width:1600px) {
  .adaptiveStyle {
    width: 85vw;
    height: 65vh;
  }
}
</style>