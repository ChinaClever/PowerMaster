<template>
  <el-row :gutter="18" >
    <el-col>
      <el-card>
        <el-row :gutter="18" >
          <el-col :span="2">
            <el-text line-clamp="2">
              <el-text class="mx-1" size="large">所在位置：{{ location }}</el-text>
            </el-text>
          </el-col>
          <el-col :span="10">
            <el-text line-clamp="2">
              <el-text class="mx-1" size="large">网络地址：{{ queryParams.devKey }}</el-text>
            </el-text>
          </el-col>
          <el-col :span="10">
            <el-form
              class="-mb-15px"
              :model="queryParams"
              ref="queryFormRef"
              :inline="true"
              label-width="120px"
            >
              <el-form-item label="IP地址" prop="ipAddr" >
              <el-input
                v-model="queryParams.ipAddr"
                placeholder="请输入IP地址"
                clearable
                class="!w-140px"
              />
              </el-form-item>

              <el-form-item label="级联地址" prop="cascadeAddr" label-width="70px">
                <el-input-number
                  v-model="queryParams.cascadeAddr"
                  :min="0"
                  controls-position="right"
                  :value-on-clear="0"
                    class="!w-100px"
                />
              </el-form-item>
              <el-form-item>
                <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
              </el-form-item>
            </el-form>
          </el-col>
          <el-col :span="2">
            <el-button type="primary" @click="openNewPage(queryParams.devKey)" >进入管理界面</el-button>
          </el-col>
        </el-row>
      </el-card>
    </el-col>
  </el-row>
  <el-row :gutter="24" >
    <el-col :span="6" class="card-box">
      <el-card>
        <template #header>
          <el-row class="text-container"> 
            <el-col :span="12">
              <el-text line-clamp="2">
                总数据
              </el-text>
            </el-col>   
          </el-row>
        </template>
        <el-row justify="center">
          <div ref="totalChartContainer" id="totalChartContainer" style="width: 400px; height: 200px;"></div>
        </el-row>
        <el-row class="text-container"> 
          <el-col :span="8">
            <el-text line-clamp="2" >
              电能消耗:<br />
              {{ totalData.ele }} kWh
            </el-text>
          </el-col>
          <el-col :span="8">
            <el-text line-clamp="2">
              频率:<br />
              {{ totalData.frequency }} Hz
            </el-text>
          </el-col>
          <el-col :span="8">
            <el-text line-clamp="2">
              PF:<br />
              {{ totalData.pf }}
            </el-text>
          </el-col>
        </el-row>
      </el-card>       
    </el-col>
    <el-col :span="6" class="card-box">
      <el-card>
        <template #header>
            <div>
              <span>A相</span>
            </div>
          </template>
          <el-row justify="center">
            <div ref="AChartContainer" id="AChartContainer" style="width: 400px; height: 200px;"></div>
          </el-row>
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: A.volColor }">
                U1:<br />
                {{ A.vol_value }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: A.powColor }">
                P1:<br />
                {{ A.pow_value }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" >
                PF1:<br />
                {{ A.pf }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" v-if="controlVis.haveB">
      <el-card>
        <template #header>
            <div>
              <span>B相</span>
            </div>
          </template>                                                                           
          <el-row justify="center">
            <div ref="BChartContainer" id="BChartContainer" style="width: 400px; height: 200px;"></div>
          </el-row>
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2"  :style="{ backgroundColor: B.volColor }">
                U2:<br />
                {{ B.vol_value }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: B.powColor }">
                P2:<br />
                {{ B.pow_value }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF2:<br />
                {{ B.pf }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
    <el-col :span="6" class="card-box" v-if="controlVis.haveC">          
      <el-card>
        <template #header>
            <div>
              <span>C相</span>
            </div>
          </template>
          <el-row justify="center">
            <div ref="CChartContainer" id="CChartContainer" style="width: 400px; height: 200px;"></div>
          </el-row>
          <el-row class="text-container">
            <el-col :span="8">
              <el-text line-clamp="2"  :style="{ backgroundColor: C.volColor }">
                U3:<br />
                {{ C.vol_value }} V
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2" :style="{ backgroundColor: C.powColor }">
                P3:<br />
                {{ C.pow_value }} kW
              </el-text>
            </el-col>
            <el-col :span="8">
              <el-text line-clamp="2">
                PF3:<br />
                {{ C.pf }}
              </el-text>
            </el-col>
          </el-row>
      </el-card>
    </el-col>
  </el-row>
  <el-collapse v-model="activeNames" >
    <el-card>
      <el-row>
        <el-col >
          <span style="width: 100%">趋势图</span>
        </el-col>
        <el-col >
          <div style="float:right;margin-top: 0;">
            <el-form-item  prop="type">
              <el-button @click="queryParams.powGranularity = `oneHour`;switchValue = 0;" :type="!switchValue ? 'primary' : ''">最近一小时</el-button>
              <el-button @click="queryParams.powGranularity = `twentyfourHour`;switchValue = 1;" :type="switchValue ? 'primary' : ''">过去24小时</el-button>
            </el-form-item>
          </div>
        </el-col> 
      </el-row>
      <div style="display: flex; justify-content: center; align-items: center;">
        <div ref="chartContainer" id="chartContainer" style="width: 70vw; height: 58vh;"></div>
      </div>
      
    </el-card>
    <el-collapse-item title="回路" name="1" v-if="controlVis.haveCircle">
      <ContentWrap>
        <el-table  :data="circleList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="回路" align="center" prop="circuit" />
          <el-table-column label="断路器状态" align="center" prop="breaker" v-if="controlVis.circleTableCol.breaker"> 
            <template #default="scope" >
              <el-tag type="" v-if="scope.row.breaker == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.breaker == 0">关闭</el-tag>
            </template>
          </el-table-column>                        
          <el-table-column label="当前电流" align="center" prop="cur_value" v-if="controlVis.circleTableCol.cur_value" >
            <template #default="scope" >
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.curColor }">
                {{ scope.row.cur_value }}A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="当前电压" align="center" prop="vol_value" v-if="controlVis.circleTableCol.vol_value" >
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.volColor }">
                {{ scope.row.vol_value }}V
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="有功功率" align="center" prop="pow_value" v-if="controlVis.circleTableCol.pow_value" >
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powColor }">
                {{ scope.row.pow_value }}kW
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="电能消耗" align="center" prop="ele" v-if="controlVis.circleTableCol.ele">
            <template #default="scope">
             {{ scope.row.ele }}kWh
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="输出位" name="3" v-if="controlVis.haveOutPut">
      <ContentWrap>
        <el-table  :data="output" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="序号" align="center" prop="no" />
          <el-table-column label="名称" align="center" prop="name" />
          <el-table-column label="开关状态" align="center" prop="relay_state" v-if="controlVis.outPutTableCol.relay_state">
            <template #default="scope">
              <el-tag type="" v-if="scope.row.relay_state == 1">开启</el-tag>
              <el-tag type="danger" v-if="scope.row.relay_state == 0" >关闭</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="输出电流(A)" align="center" prop="cur_value"  v-if="controlVis.outPutTableCol.cur_value">
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.curColor }">
                {{ scope.row.cur_value }}A
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="有功功率(kW)" align="center" prop="pow_value"  v-if="controlVis.outPutTableCol.pow_value">
            <template #default="scope">
              <el-text line-clamp="2"  :style="{ backgroundColor: scope.row.powColor }">
                {{ scope.row.pow_value }}kW
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="功率因数" align="center" prop="pf"  v-if="controlVis.outPutTableCol.pf"/>
          <el-table-column label="电能消耗(kWh)" align="center" prop="ele"  v-if="controlVis.outPutTableCol.ele">
            <template #default="scope">
             {{ scope.row.ele }}kWh
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
    <el-collapse-item title="传感器" name="2" v-if="controlVis.haveSensor">
      <ContentWrap>
        <el-table  :data="sensorList" :stripe="true" :show-overflow-tooltip="true">
          <el-table-column label="传感器名称" align="center" prop="temName" />
          <el-table-column label="传感器状态" align="center" prop="tem_alarm_status" >
            <template #default="scope" >
              <el-tag type="" v-if="scope.row.tem_alarm_status == 0">正常</el-tag>
              <el-tag type="danger" v-if="scope.row.tem_alarm_status == 1">最小值告警</el-tag>
              <el-tag type="warning" v-if="scope.row.tem_alarm_status == 2">下限预警</el-tag>
              <el-tag type="warning" v-if="scope.row.tem_alarm_status == 4">上限预警</el-tag>
              <el-tag type="danger" v-if="scope.row.tem_alarm_status == 8">最大值告警</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="传感器名称" align="center" prop="humName" />
          <el-table-column label="传感器状态" align="center" prop="hum_alarm_status" >
            <template #default="scope" >
              <el-tag type="" v-if="scope.row.hum_alarm_status == 0">正常</el-tag>
              <el-tag type="danger" v-if="scope.row.hum_alarm_status == 1">最小值告警</el-tag>
              <el-tag type="warning" v-if="scope.row.hum_alarm_status == 2">下限预警</el-tag>
              <el-tag type="warning" v-if="scope.row.hum_alarm_status == 4">上限预警</el-tag>
              <el-tag type="danger" v-if="scope.row.hum_alarm_status == 8">最大值告警</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </ContentWrap>
    </el-collapse-item>
  </el-collapse>

</template>

<script setup lang="ts">

// import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import * as echarts from 'echarts';
import router from '@/router';
// import { object } from 'vue-types';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const instance = getCurrentInstance();
const switchValue = ref(0);

//折叠列表显示的项
const activeNames = ref(["1","2","3","4","5"])

//计时器
const flashListTimer = ref({
  tableDataTimer : null as any,
  chartTimer : null as any,
});
const firstTimerCreate = ref(true);

const controlVis = ref({
  haveCircle : false,
  haveOutPut : false,
  haveSensor : false,
  haveC : false,
  haveB : false,
  circleTableCol : {
    breaker : false,
    cur_value : false,
    vol_value : false,
    pow_value : false,
    ele : false
  },
  outPutTableCol : {
    relay_state : false,
    cur_value : false, 
    pow_value : false,
    pf : false,
    ele : false
  },
})

const location = ref("");
// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const testData = ref({
})as any

// const loading = ref(true) // 列表的加载中

const circleList = ref([]) as any // 列表的数据

const output = ref([]) as any// 列表的数据

const sensorList = ref([
]) as any// 列表的数据

const chartData = ref({
  apparentList : [] as number[],
  activeList : [] as number[],
  dateTimes : [] as string[]
})

// const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey: "",
  ipAddr: null,
  cascadeAddr:0,
  createTime: [],
  cascadeNum: undefined,
  id : '',
  powGranularity: "oneHour",
})

// const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

//数据
const totalData = ref({
  ele : 0,
  frequency : 0,
  pow : 0,
  powPercentage : 0,
  pf : 0,
  powApparent : 0
})
const A = ref({
  vol_value : null,
  volColor : null,
  pow_value : null,
  powColor : null,
  cur_value : null,
  curColor : null,
  curPercemtage: null,
  pf : null
}) as any
const B = ref({
  vol_value : null,
  volColor : null,
  pow_value : null,
  powColor : null,
  cur_value : null,
  curColor : null,
  curPercemtage: null,
  pf : null
}) as any
const C = ref({
  vol_value : null,
  volColor : null,
  pow_value : null,
  powColor : null,
  cur_value : null,
  curColor : null,
  curPercemtage: null,
  pf : null
}) as any

// const redColor = ref("red")


/** 查询列表 */
// const getList = async () => {
//   loading.value = true
//   try {
//     const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
//     list.value = data.list
//     total.value = data.total
//   } finally {
//     loading.value = false
//   }
// }

/** 搜索按钮操作 */
// const handleQuery = () => {
//   queryParams.pageNo = 1
//   getList()
// }

/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
// const formRef = ref()
// const openForm = (type: string, id?: number) => {
//   formRef.value.open(type, id)
// }

/** 删除按钮操作 */
// const handleDelete = async (id: number) => {
//   try {
//     // 删除的二次确认
//     await message.delConfirm()
//     // 发起删除
//     await PDUDeviceApi.deletePDUDevice(id)
//     message.success(t('common.delSuccess'))
//     // 刷新列表
//     await getList()
//   } catch {}
// }

const openNewPage = (devKey) => {
  const url = 'http://' + devKey.split('-')[0] + '/index.html';
  window.open(url, '_blank');
}

let chart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const chartContainer = ref<HTMLElement | null>(null);
let totalChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const totalChartContainer = ref<HTMLElement | null>(null);
let AChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const AChartContainer = ref<HTMLElement | null>(null);
let BChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const BChartContainer = ref<HTMLElement | null>(null);
let CChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const CChartContainer = ref<HTMLElement | null>(null);

const initChart = async () => {
  
  if (chartContainer.value && instance) {
    chart = echarts.init(chartContainer.value);
    chart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'axis' },
      legend: { data: ['视在功率','有功功率']},
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
      xAxis: {type: 'category', axisLabel: { formatter: 
            function (value) {
              if(queryParams.powGranularity == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(queryParams.powGranularity == "twentyfourHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          },boundaryGap: false, data:chartData.value.dateTimes},
      yAxis: { type: 'value'},
      series: [
        {name: '视在功率', type: 'line', data: chartData.value.apparentList , symbol: 'circle', symbolSize: 4},
        {name: '有功功率', type: 'line', data: chartData.value.activeList , symbol: 'circle', symbolSize: 4},
      ],

    });
    // 将 chart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.chart = chart;
  }
  if (totalChartContainer.value && instance) {
    totalChart = echarts.init(totalChartContainer.value);
    totalChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      tooltip: { trigger: 'item', formatter: function(params) {
                                      if (params.name === '视在功率') {
                                          return params.name + ': ' + params.value + 'kVA';
                                      } else if (params.name === '有功功率') {
                                          return params.name + ': ' + params.value + 'kW';
                                      }
                                  } },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },
          data: [{value : totalData.value.pow, name: '有功功率', label: { show: true, position: 'outside', formatter: '{c}kW',fontSize: 13 },itemStyle: { color: '#0A69EE' }  },
                 {value : totalData.value.powApparent , name : '视在功率' , label: { show: true, position: 'outside', formatter: '{c}kVA',fontSize: 13  }, itemStyle: { color: '#0AD0EE' } }],
        },
      ],
    });
    // 将 totalChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.totalChart = totalChart;
  }
  if (AChartContainer.value && instance) {
    AChart = echarts.init(AChartContainer.value);
    AChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },
          data: [{value : A.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13 ,backgroundColor : A.value.curColor },itemStyle: { color: '#0AD0EE' }  },],
        },
      ],
    });
    // 将 AChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.AChart = AChart;
  }
  if (BChartContainer.value && instance) {
    BChart = echarts.init(BChartContainer.value);
    BChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },
          data: [{value : B.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13, backgroundColor : B.value.curColor },itemStyle: { color: '#0AD0EE' }  },],
        },
      ],
    });
    // 将 BChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.BChart = BChart;
  }
  if (CChartContainer.value && instance) {
    CChart = echarts.init(CChartContainer.value);
    CChart.setOption({
      // 这里设置 Echarts 的配置项和数据
      title: { text: ''},
      // tooltip: { trigger: 'item', formatter: '{b}: {c}A' },
      grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
      series: [
        { type: 'pie', radius: ['50%', '65%'], avoidLabelOverlap: false,  labelLine: { show: false },
          data: [{value : A.value.cur_value, name: '电流', label: { show: true, position: 'center', formatter: '{c}A',fontSize: 13, backgroundColor : C.value.curColor },itemStyle: { color: '#0AD0EE' }  },],
        },
      ],
    });
    // 将 CChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    instance.appContext.config.globalProperties.CChart = CChart;
  }
};

// 在组件销毁时手动销毁图表
const beforeChartUnmount = () => {
  chart?.dispose(); // 销毁图表实例
};

// window.addEventListener('resize', function() {
//   chart?.resize(); 
// });

const setNewChartData = async () => {
  var params = { devKey : queryParams.devKey , oldTime : chartData.value.dateTimes[chartData.value.dateTimes.length - 1], type : queryParams.powGranularity}
  var temp =  {dateTime : '' , apparent : 0, active: 0 };
  temp =  await PDUDeviceApi.ChartNewData(params);

  chartData.value.apparentList.shift()
  chartData.value.activeList.shift()
  chartData.value.dateTimes.shift()

  chartData.value.dateTimes.push(temp.dateTime);
  chartData.value.apparentList.push(temp.apparent.toFixed(3));
  chartData.value.activeList.push(temp.active.toFixed(3));


  chart?.setOption({
    xAxis: { data: chartData.value.dateTimes },
    series: [
      { data: chartData.value.apparentList },
      { data: chartData.value.activeList},
    ],
  });
}

const flashChartData = async () =>{
  var tempParams = { id : queryParams.id, type : queryParams.powGranularity}
  chartData.value = await PDUDeviceApi.PDUHis(tempParams); 

  chart?.setOption({
    xAxis: { data: chartData.value.dateTimes },
    series: [
      { data: chartData.value.apparentList },
      { data: chartData.value.activeList},
    ],
  });
}


/** 导出按钮操作 */
// const handleExport = async () => {
//   try {
//     // 导出的二次确认
//     await message.exportConfirm()
//     // 发起导出
//     exportLoading.value = true
//     const data = await PDUDeviceApi.exportPDUDevice(queryParams)
//     download.excel(data, 'PDU设备.xls')
//   } catch {
//   } finally {
//     exportLoading.value = false
//   }
// }

const getTestData = async()=>{

  testData.value = await PDUDeviceApi.PDUDisplay(queryParams);
  circleList.value = [];
  output.value = [];

  if(testData.value.pdu_data?.loop_item_list?.ele){
    var temp = [] as any;
    for (let i = 0; i < testData.value.pdu_data?.loop_item_list["pow_apparent"].length; i++) {
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.loop_item_list) {
        loopItem[key] = testData.value.pdu_data.loop_item_list[key][i];
        loopItem["circuit"] = "C" + (i + 1); 
        controlVis.value.circleTableCol[key] = true;
        if (key.includes("alarm_status")) {
          var alarmStatus = testData.value.pdu_data.loop_item_list[key][i];
          if(alarmStatus == 1 ||alarmStatus == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          } else {
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "";
          }
        }
      }
      temp.push(loopItem);
    }
    circleList.value = temp;
    controlVis.value.haveCircle = true;
  }else{
    controlVis.value.haveCircle = false;
  }

  circleList.value.forEach(element => {
    element.cur_value = element.cur_value?.toFixed(2);
    element.vol_value = element.vol_value?.toFixed(1);
    element.pow_value = element.pow_value?.toFixed(3);
    element.ele = element.ele?.toFixed(1);
  });

  if(testData.value.pdu_data?.output_item_list?.name){
    var temp = [] as any;
    for (let i = 0; i < testData.value.pdu_data.output_item_list["name"].length; i++) {
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.output_item_list) {
        loopItem[key] = testData.value.pdu_data.output_item_list[key][i];
        loopItem["no"] = i + 1;
        controlVis.value.outPutTableCol[key] = true;
        if (key.includes("alarm_status")) {
          var alarmStatus = testData.value.pdu_data.output_item_list[key][i];
          if(alarmStatus == 1 ||alarmStatus == 8){
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "red";
          } else {
            var alarmColor = key.split("_")[0] + "Color";
            loopItem[alarmColor] = "";
          }
        }
      }
      temp.push(loopItem);
    }
    output.value = temp;
    controlVis.value.haveOutPut = true;
  } else {
    controlVis.value.haveOutPut = false;
  }


  output.value.forEach(element => {
    element.cur_value = element.cur_value?.toFixed(2);
    element.pow_value = element.pow_value?.toFixed(3);
    element.pf = element.pf?.toFixed(2);
    element.ele = element.ele?.toFixed(1);
  });

  if(testData.value.pdu_data?.env_item_list?.tem_value){
    var temp = [] as any;
    for(let i = 0; i < testData.value.pdu_data.env_item_list["tem_value"].length; i++){
      let loopItem = {} as any;
      for (let key in testData.value.pdu_data.env_item_list) {
        loopItem[key] = testData.value.pdu_data.env_item_list[key][i];
      }
      loopItem["temName"] = "温度" + (i + 1);
      loopItem["humName"] = "湿度" + (i + 1);
      temp.push(loopItem);
    }
    sensorList.value = temp;
    controlVis.value.haveSensor = true;
  } else {
    controlVis.value.haveSensor = false;
  }

  totalData.value.pow =  testData.value.pdu_data.pdu_tg_data.pow?.toFixed(3);
  if(testData.value.pdu_data.pdu_tg_data.pow_apparent != 0){
    totalData.value.powPercentage = (testData.value.pdu_data.pdu_tg_data.pow / testData.value.pdu_data.pdu_tg_data.pow_apparent) * 100;
  } else {
    totalData.value.powPercentage = 0;
  }
  
  totalData.value.ele = testData.value.pdu_data.pdu_tg_data.ele?.toFixed(1);

  totalData.value.pf = testData.value.pdu_data.pdu_tg_data.pf?.toFixed(2);
  totalData.value.frequency = testData.value.dev_hz;
  totalData.value.powApparent = testData.value.pdu_data.pdu_tg_data.pow_apparent?.toFixed(3);
  
  A.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[0]?.toFixed(2);
  A.value.curPercemtage = (testData.value.pdu_data.line_item_list.cur_value[0] / testData.value.pdu_data.line_item_list.cur_alarm_max[0]) * 100;
  let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[0];
  if(curalarm == 1 || curalarm == 8 ){
    A.value.curColor = "exception";
  } else if(curalarm == 2 || curalarm == 4 ){
    A.value.curColor = "warning";
  } else{
    A.value.curColor = "";
  }

  A.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[0]?.toFixed(1);
  let u1alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[0];
  if(u1alarm == 1 || u1alarm == 8 ){
    A.value.volColor = "red";
  } else if(u1alarm == 2 || u1alarm == 4 ){
    A.value.volColor = "yellow";
  } else{
    A.value.volColor = "";
  }
  
  A.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[0]?.toFixed(3);
  let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[0];
  if(powalarm == 1 || powalarm == 8 ){
    A.value.powColor = "red";
  } else if(powalarm == 2 || powalarm == 4 ){
    A.value.powColor = "yellow";
  } else {
    A.value.powColor = "";
  }

  A.value.pf = testData.value.pdu_data.line_item_list.pf[0]?.toFixed(2);

  if(testData.value.pdu_data.line_item_list.ele.length > 1){
    B.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[1]?.toFixed(2);
    B.value.curPercemtage = (testData.value.pdu_data.line_item_list.cur_value[1] / testData.value.pdu_data.line_item_list.cur_alarm_max[1]) * 100;
    let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[1];
    if(curalarm == 1 || curalarm == 8 ){
      B.value.curColor = "exception";
    } else if(curalarm == 2 || curalarm == 4 ){
      B.value.curColor = "warning";
    } else{
      B.value.curColor = "";
    }

    B.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[1]?.toFixed(1);
    let u2alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[1];
    if(u2alarm == 1 || u2alarm == 8 ){
      B.value.volColor = "red";
    } else if(u2alarm == 2 || u2alarm == 4 ){
      B.value.volColor = "yellow";
    } else {
      B.value.volColor = "";
    }
    
    B.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[1]?.toFixed(3);
    let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[1];
    if(powalarm == 1 || powalarm == 8 ){
      B.value.powColor = "red";
    } else if(powalarm == 2 || powalarm == 4 ){
      B.value.powColor = "yellow";
    } else {
      B.value.powColor = "";
    }
    
    B.value.pf = testData.value.pdu_data.line_item_list.pf[1]?.toFixed(2);
    controlVis.value.haveB = true;
  }
  if(testData.value.pdu_data.line_item_list.ele.length > 2){
    C.value.cur_value = testData.value.pdu_data.line_item_list.cur_value[2]?.toFixed(2);
    C.value.curPercemtage = (testData.value.pdu_data.line_item_list.cur_value[2] / testData.value.pdu_data.line_item_list.cur_alarm_max[2]) * 100;
    let curalarm = testData.value.pdu_data.line_item_list.cur_alarm_status[2];
    if(curalarm == 1 || curalarm == 8 ){
      C.value.curColor = "exception";
    } else if(curalarm == 2 || curalarm == 4 ){
      C.value.curColor = "warning";
    } else{
      C.value.curColor = "";
    }

    C.value.vol_value = testData.value.pdu_data.line_item_list.vol_value[2]?.toFixed(1);
    let u2alarm = testData.value.pdu_data.line_item_list.vol_alarm_status[2];
    if(u2alarm == 1 || u2alarm == 8 ){
      C.value.volColor = "red";
    } else if(u2alarm == 2 || u2alarm == 4 ){
      C.value.volColor = "yellow";
    } else{
      C.value.volColor = "";
    }
    
    C.value.pow_value =testData.value.pdu_data.line_item_list.pow_value[2]?.toFixed(3);
    let powalarm = testData.value.pdu_data.line_item_list.pow_alarm_status[2];
    if(powalarm == 1 || powalarm == 8 ){
      C.value.powColor = "red";
    } else if(powalarm == 2 || powalarm == 4 ){
      C.value.powColor = "yellow";
    } else {
      C.value.powColor = "";
    }

    C.value.pf = testData.value.pdu_data.line_item_list.pf[2]?.toFixed(2);
    controlVis.value.haveC = true;
  }
  
  var tempParams = { id : queryParams.id, type : queryParams.powGranularity}
  chartData.value = await PDUDeviceApi.PDUHis(tempParams); 
  chartData.value.apparentList.forEach((obj,index) => {
    chartData.value.apparentList[index] = obj?.toFixed(3);
  });
  chartData.value.activeList.forEach((obj,index) => {
    chartData.value.activeList[index] = obj?.toFixed(3);
  });
}

watch([() => queryParams.powGranularity], async ([newPowGranularity]) => {
    // 销毁原有的图表实例
    beforeChartUnmount();
    //获取数据
    var tempParams = { id : queryParams.id, type : newPowGranularity}
    chartData.value = await PDUDeviceApi.PDUHis(tempParams); 
    chartData.value.apparentList.forEach((obj,index) => {
      chartData.value.apparentList[index] = obj?.toFixed(3);
    });
    chartData.value.activeList.forEach((obj,index) => {
      chartData.value.activeList[index] = obj?.toFixed(3);
    });
    // 创建新的图表实例
    chart = echarts.init(document.getElementById('chartContainer'));
    // 设置新的配置对象
    if (chart) {
      chart.setOption({
        // 这里设置 Echarts 的配置项和数据
        title: { text: ''},
        tooltip: { trigger: 'axis' },
        legend: { data: ['视在功率','有功功率']},
        grid: {left: '3%', right: '4%', bottom: '3%',containLabel: true},
        toolbox: {feature: {saveAsImage: {},dataView:{},dataZoom :{},restore :{}, }},
        xAxis: {type: 'category', axisLabel: { formatter: 
            function (value) {
              if(queryParams.powGranularity == "oneHour"){
                // 截取字符串的前n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(11, 19);
              } else if(queryParams.powGranularity == "twentyfourHour"){
                // 截取字符串的n位，即yyyy-MM-dd HH:mm:ss
                return value.substring(5, 19);
              }
            }
          }, boundaryGap: false, data:chartData.value.dateTimes},
        yAxis: { type: 'value'},
        series: [
          {name: '视在功率', type: 'line', data: chartData.value.apparentList , symbol: 'circle', symbolSize: 4},
          {name: '有功功率', type: 'line', data: chartData.value.activeList , symbol: 'circle', symbolSize: 4},
        ],

      });
    }
    if(flashListTimer.value.chartTimer){
      var time = 0;
      if(queryParams.powGranularity == "oneHour"){
        time = 60000;
        // time = 3000;
      } else if(queryParams.powGranularity == "twentyfourHour"){
        time = 3600000;
        // time = 3000;
      }
      clearInterval(flashListTimer.value.chartTimer)
      flashListTimer.value.chartTimer = null;
      flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
    }
})

/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.ipAddr){
    console.log(1)
    queryParams.devKey = queryParams.ipAddr +'-' +  queryParams.cascadeAddr;
    await getTestData();
    initChart();
  }
}



/** 初始化 **/
onMounted(() => {

  // getList()
  
  // addDataPoint();
  // initChart();
})

onBeforeMount(async () =>{
  location.value = router.currentRoute.value.query.location as string;
  if(location.value == null || location.value == "null"){
    location.value = "";
  }
  queryParams.devKey = router.currentRoute.value.query.devKey as string;
  queryParams.id = router.currentRoute.value.query.id as string;
  
  await getTestData();
  initChart();
  flashListTimer.value.tableDataTimer = setInterval((getTestData), 5000);
  flashListTimer.value.chartTimer = setInterval((setNewChartData), 60000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value.tableDataTimer && flashListTimer.value.chartTimer){
    clearInterval(flashListTimer.value.tableDataTimer)
    clearInterval(flashListTimer.value.chartTimer)
    flashListTimer.value.tableDataTimer = null;
    flashListTimer.value.chartTimer = null;
  }
})

onActivated( async () => {
  await getTestData();
  flashChartData();
  if(!firstTimerCreate.value){
    flashListTimer.value.tableDataTimer = setInterval((getTestData), 5000);
    var time = 0;
    if(queryParams.powGranularity == "oneHour"){
      time = 60000;
      // time = 3000;
    } else if(queryParams.powGranularity == "twentyfourHour"){
      time = 3600000;
      // time = 3000;
    }
    flashListTimer.value.chartTimer = setInterval((setNewChartData), time);
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value.tableDataTimer && flashListTimer.value.chartTimer){
    clearInterval(flashListTimer.value.tableDataTimer)
    clearInterval(flashListTimer.value.chartTimer)
    flashListTimer.value.tableDataTimer = null;
    flashListTimer.value.chartTimer = null;
    firstTimerCreate.value = false;
  }
  
})





</script>

