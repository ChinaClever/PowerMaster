<template>
   <div class="homeContainer">
    <div class="left">
      <el-card style="height: 30vh">
        <template #header>
          <div class="flex justify-between" style="align-item: center">
            <div style="font-weight: bold;">负载率</div>
            <div style="font-size: 14px">
              机房：{{roomDownVal?.roomName}}
            </div>
          </div>
        </template>
        <div class="progress">
          <!--<el-progress type="dashboard" :percentage="roomDownVal.powApparent && roomDownVal.powApparent.toFixed()">-->
          <!-- <el-progress type="dashboard" :percentage="roomDownVal.powActive/roomDownVal.powApparent*100" width="200">
              <span class="percentage-value">{{ roomDownVal.powApparent ? roomDownVal.powApparent.toFixed(0) : 0 }}</span>
              <span class="percentage-label">总视在功率</span>
              <span class="percentage-unit">kVA</span>
          </el-progress> -->
          <el-progress type="dashboard" :percentage="roomDownVal.roomLoadFactor ? Math.min(roomDownVal.roomLoadFactor,100).toFixed(0) : 0" width="200" stroke-width="12" :color="roomDownVal.roomLoadFactor>90 ? `rgba(173, 55, 98, ${roomDownVal.roomLoadFactor/100})` : (roomDownVal.roomLoadFactor>=60 ? `rgba(200, 96, 58, ${(roomDownVal.roomLoadFactor+10)/100})` : `rgba(229, 184, 73, ${(roomDownVal.roomLoadFactor+40)/100})`)">
            <span class="percentage-value">{{roomDownVal.roomLoadFactor ? roomDownVal.roomLoadFactor.toFixed(0) : 0}}</span><br/>
            <span class="percentage-label">负载率</span>
            <span class="percentage-unit">%</span>
          </el-progress>
        </div>
      </el-card>
      <!--<el-card shadow="never">
        <template #header>
          <div>空间管理</div>
        </template>
        <div>未用空间：{{spaceInfo.freeSpace}}u</div>
        <div>已用空间：{{spaceInfo.usedSpace}}u</div>
        <div>总空间：{{spaceInfo.totalSpace}}u</div>
        <div>机柜数：{{spaceInfo.cabNum}}</div>
      </el-card>-->
      <el-card shadow="never" style="height:24vh;margin-bottom: 15px;">
        <template #header>
          <div class="flex justify-between" style="align-item: center">
            <div style="font-weight: bold;">用能</div>
            <div style="font-size: 14px">
              单位：kWh
            </div>
          </div>
        </template>
        <div style="height:14vh;display: flex;flex-direction: column;justify-content: space-between">
          <!--<template #header>
            <div>当前用能</div>
          </template>
          <div>今日：{{EqInfo.todayEq && EqInfo.todayEq.toFixed(2)}}kW·h</div>
          <div>本周：{{EqInfo.thisWeekEq && EqInfo.thisWeekEq.toFixed(2)}}kW·h</div>
          <div>本月：{{EqInfo.thisMonthEq  && EqInfo.thisMonthEq.toFixed(2)}}kW·h</div>-->
          <div class="flex items-center" style="height: 40%;">
            <div style="height: 100%;margin-right: 0.5vw">
              <img alt="" src="@/assets/imgs/dn.jpg" style="height: 100%" />
            </div>
            <div style="display: flex;flex-direction: column;flex: 1">
              <div style="font-weight: bold;background: linear-gradient(to right, rgba(30, 144, 255, 0.3), rgba(30, 144, 255, 0));padding-left: 0.2vw">
                今日能耗
              </div>
              <div style="display: flex; align-items: center;justify-content: space-between;font-size: 14px">
                <div>今日：</div>
                <div style="font-weight: bold">{{EqInfo.todayEq ? EqInfo.todayEq.toFixed(1) : 0}}</div>
              </div>
              <div style="display: flex; align-items: center;">
                <div style="width: 30%"></div>
                <el-progress :percentage="EqInfo.todayEq/EqInfo.yesterdayEq*100" :show-text="false" style="width: 70%;" />
              </div>
              <div style="display: flex; align-items: center;justify-content: space-between;font-size: 14px">
                <div>昨日：</div>
                <div style="font-weight: bold">{{EqInfo.yesterdayEq ? EqInfo.yesterdayEq.toFixed(1) : 0}}</div>
              </div>
            </div>
          </div>
          
          <div class="flex items-center" style="height: 40%;">
            <div style="height: 100%;margin-right: 0.5vw">
              <img alt="" src="@/assets/imgs/dn.jpg" style="height: 100%" />
            </div>
            <div style="display: flex;flex-direction: column;flex: 1">
              <div style="font-weight: bold;background: linear-gradient(to right, rgba(30, 144, 255, 0.3), rgba(30, 144, 255, 0));padding-left: 0.2vw">
                本月能耗
              </div>
              <div style="display: flex; align-items: center;justify-content: space-between;font-size: 14px">
                <div>本月：</div>
                <div style="font-weight: bold">{{EqInfo.thisMonthEq ? EqInfo.thisMonthEq.toFixed(1) : 0}}</div>
              </div>
              <div style="display: flex; align-items: center;">
                <div style="width: 30%"></div>
                <el-progress :percentage="EqInfo.thisMonthEq/EqInfo.lastMonthEq*100" :show-text="false" style="width: 70%;" />
              </div>
              <div style="display: flex; align-items: center;justify-content: space-between;font-size: 14px">
                <div>上月：</div>
                <div style="font-weight: bold">{{EqInfo.lastMonthEq ? EqInfo.lastMonthEq.toFixed(1) : 0}}</div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
      <el-card shadow="never" style="height: 39vh">
        <template #header>
          <div class="flex justify-between">
            <span style="font-weight: bold">柜列功率</span>
          </div>
        </template>
        <el-skeleton :loading="loading" animated :rows="7" style="height: 30vh">
          <div style="height: 30vh">
            <Echart v-if="roomDownVal.aisleList.length>= 6" :height="300" :options="powChartOptions" />
            <Echart v-else :height="300" :options="powChartOptionsBar" />
          </div>
        </el-skeleton>
      </el-card>
      
    </div>
    <div class="center" id="center">
      <CabTopology :containerInfo="containerInfo" :isFromHome="true" @back-data="handleBackData" @getroomid="handleGetRoomId" />
      <el-card shadow="never" style="height: 31vh">
        <div style="background-color: #ffffff;border-radius: 5px;">
          <el-row>
            <el-col :span="12">
              <el-radio-group v-model="typeRadio">
                <el-radio-button label="功率曲线" value="功率曲线" @click="switchChartContainer =0"/>
                <el-radio-button label="有效电能" value="有效电能" @click="clickPower()"/>
                <el-radio-button label="负载率" value="负载率" @click="switchChartContainer =2"/>
                <!-- <el-radio-button label="PUE" value="PUE" @click="switchChartContainer =2"/> -->
              </el-radio-group>
            </el-col>
            <el-col :span="12">
              <div style="display: flex;align-items: center;justify-content: space-around">
                <el-select v-model="typeRadioShow" placeholder="请选择" :style="{width: '100px',opacity: timeRadio != '近一小时' && typeRadio != '有效电能' ? '1' : '0'}">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
                <el-radio-group v-model="timeRadio">
                  <el-radio-button label="近一小时" value="近一小时" :disabled="isHourDisabled" />
                  <el-radio-button label="近一天" value="近一天" />
                  <el-radio-button label="近三天" value="近三天" />
                  <el-radio-button label="近一月" value="近一月" />
                </el-radio-group>
              </div>
            </el-col>
          </el-row>
          <br/>
          <div ref="chartContainer4" id="chartContainer4" style="width: 55vw;height: 220px;" v-show="switchChartContainer == 2"></div>
          <div ref="chartContainer2" id="chartContainer2" style="width: 55vw;height: 220px;" v-show="switchChartContainer == 0"></div>
          <div ref="chartContainer3" id="chartContainer3" style="width: 55vw;height: 220px;" v-show="switchChartContainer == 1"></div>
        </div>
      </el-card>
    </div>
    <div class="right">
      <el-card shadow="never" style="height: 30vh">
        <template #header>
          <div style="font-weight: bold">机房功率</div>
        </template>
        <el-skeleton :loading="loading" animated :rows="5" style="height: 21vh">
          <div class="flex justify-around" style="height: 21vh;flex-direction: column">
            <div class="flex justify-around">
              <div class="flex items-center" style="width: 50%">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="flex" style="flex-direction: column">
                  <div>总有功功率</div>
                  <div><span style="font-weight: bold">{{roomDownVal.powActive ? roomDownVal.powActive.toFixed(3) : '0.000'}}</span>kW</div>
                </div>
              </div>
              <div class="flex items-center" style="width: 50%">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="flex" style="flex-direction: column">
                  <div>总无功功率</div>
                  <div><span style="font-weight: bold">{{roomDownVal.powReactive ? roomDownVal.powReactive.toFixed(3) : '0.000'}}</span>kVar</div>
                </div>
              </div>
            </div>
            <div class="flex justify-around">
              <div class="flex items-center" style="width: 50%">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="flex" style="flex-direction: column">
                  <div>总视在功率</div>
                  <div><span style="font-weight: bold">{{roomDownVal.powApparent ? roomDownVal.powApparent.toFixed(3) : '0.000'}}</span>kVA</div>
                </div>
              </div>
              <div class="flex items-center" style="width: 50%">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="flex" style="flex-direction: column">
                  <div>总电力容量</div>
                  <div><span style="font-weight: bold">{{roomDownVal.powerCapacity ? roomDownVal.powerCapacity : '-----------'}}</span></div>
                </div>
              </div>
            </div>
            <!-- <div class="flex justify-around">
              <Echart :height="130" :width="130" :options="powRoomChartOptions" />
            </div> -->
          </div>
        </el-skeleton>
      </el-card>
      <el-card shadow="never" style="margin-bottom: 15px;height:24vh;">
        <template #header>
          <div style="font-weight: bold;font-size: 16px;">设备</div>
        </template>
        <div style="height:14vh;display: flex;flex-direction: column;justify-content: space-around">
          <div style="display: flex; align-items: center;">
            <div style="width: 20%">机柜:</div>
            <div style="font-weight: bold;width: 80%">{{deviceInfo.cabUse}}/{{deviceInfo.cabNum}}</div>
          </div>
          <div v-if="deviceInfo.pduNum != 0" style="display: flex; align-items: center;">
            <div style="width: 20%">PDU:</div>
            <div style="font-weight: bold;width: 80%">{{deviceInfo.pduOnLine}}/{{deviceInfo.pduNum}}</div>
          </div>
          <div v-else style="display: flex; align-items: center;">
            <div style="width: 20%">母线:</div>
            <div style="font-weight: bold;width: 80%">{{deviceInfo.busOnLine}}/{{deviceInfo.busNum}}</div>
          </div>
        </div>
      </el-card>
      <!--<el-card shadow="never">
        <template #header>
          <div>用能</div>
        </template>
        <div>昨日用能：{{EqInfo.yesterdayEq ? EqInfo.yesterdayEq.toFixed(2) : '0.00'}}kW·h</div>
        <div>上周用能：{{EqInfo.lastWeekEq ? EqInfo.lastWeekEq.toFixed(2) : '0.00'}}kW·h</div>
        <div>上月用能：{{EqInfo.lastMonthEq ? EqInfo.lastMonthEq.toFixed(2) : '0.00'}}kW·h</div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="roomDownVal.powActive && roomDownVal.powActive.toFixed()">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}kW</span>
              <span class="percentage-label">总有功功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>-->
       <el-card shadow="never" class="mb-8px" v-show="toggleAlarm===false" style="height: 39vh">
        <template #header>
          <div class="flex justify-between" style="align-items: center">
            <span style="font-weight: bold">环境/告警</span>
            <div>
              <el-link @click.prevent="toggleTable = !toggleTable;updateChart();" type="primary">{{!toggleTable?'温度':'湿度'}}</el-link>
              <el-link @click="toggleAlarm = !toggleAlarm" type="primary" style="margin-left: 5px">切换</el-link>
            </div>
          </div>
        </template>
        <div ref="lineidChartContainer" id="lineidChartContainer" style="width:14vw;height:30vh;"></div>
      </el-card>
      <el-card shadow="never" class="mb-8px" v-show="toggleAlarm===true" style="height: 39vh">
        <template #header>
          <div class="flex justify-between" style="align-items: center">
            <span style="font-weight: bold">环境/告警</span>
            <el-link @click="toggleAlarm = !toggleAlarm;" type="primary">切换</el-link>
          </div>
        </template>
        <el-skeleton :loading="loading" animated>
          <div ref="scrollableContainerOne" class="scrollable-container-one" @scroll="handleScroll">
            <el-table :data="alarmData" style="width: 100%" border class="text-12px" tooltip-formatter="tableRowFormatter">
              <el-table-column prop="alarmTypeDesc" label="告警类型">
                <template #default="{ row }">
                  <el-tooltip 
                    :content="`${row.alarmDesc}`" 
                    placement="top"
                  >
                    <span class="hover-text">{{ row.alarmTypeDesc }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column prop="alarmPosition" label="所在区域" />
            </el-table>
          </div>
        </el-skeleton>
      </el-card>
      
      <!--<el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>设备统计</span>
          </div>
        </template>
        <el-table :data="tableData" style="width: 15vw;height:25vh" border class="text-12px">
          <el-table-column prop="name" label=""  />
          <el-table-column prop="all" label="总数"  />
          <el-table-column prop="on" label="在线" />
          <el-table-column prop="off" label="离线" />
        </el-table>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div>环境数据</div>
        </template>
        <div>当前平均湿度：{{envInfo.humAvg}}%</div>
        <div>当前最高湿度：{{envInfo.humMax}}%</div>
        <div>当前最低湿度：{{envInfo.humMin}}%</div>
        <div>最近更新时间：{{envInfo.updateTime}}</div>
      </el-card>-->
    </div>
  </div>
</template>

<script lang="ts" setup>
import CabTopology from "../topology/index.vue"
import { MachineRoomApi } from '@/api/cabinet/room'
import { AlarmApi } from '@/api/system/notify/alarm'
import { EChartsOption } from 'echarts'
import { formatDate} from '@/utils/formatTime';

import * as echarts from 'echarts'
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;

const format = (percentage) => ( ``)  //用来自定义进度条的内容

const scrollableContainerOne = ref(null); // 挂载到设备模块

let scrollIntervalOne; // 设备模块的定时器
let scrollTimeout; // 用于检测滚动是否停止的延迟定时器
let isScrollingManually = false; // 标记是否正在手动滚动

const echartOptionsPower = ref<EChartsOption>({}) //用来存储功率曲线图表的配置选项
const environmentOptions = ref<EChartsOption>({}) //用来存储环境图表的配置选项
const roomId = ref<number>(0)
const radioBtn = ref('pow')
const containerInfo = reactive({
  width: 1000,
  roomId: query.roomId
}) // 机房拓扑容器信息
const deviceInfo = reactive({}) // 设备信息
const EqInfo = reactive({}) // 用能信息
const roomDownVal = reactive({}) // 机房信息
const envInfo = reactive({}) // 空间信息
const echartInfo = reactive<any>({}) //配置图表的数据系列
const toggleTable = ref(true)
const toggleAlarm = ref(false)
const loading = ref(true)
const alarmData = ref([])
const tableData = ref([
  {
    name: '总数',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'Num',
    error:'温度超阀值上限',
    box:'温湿度01',
    time:'2022-11-07 12:15:46'
  },{
    name: '在线',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'OnLine',
    error:'制冷压力超阀值上限',
    box:'精密空调',
    time:'2022-11-07 12:15:46'
  },{
    name: '告警',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'Inform',
    error:'检测到有水',
    box:'水浸',
    time:'2022-11-07 12:15:46'
  },{
    name: '离线',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'OffLine',
    error:'检测到有水',
    box:'水浸',
    time:'2022-11-07 12:15:46'
  }
])

console.log('111',tableData)

const switchChartContainer = ref(0);
const instance = getCurrentInstance();
const typeRadio = ref('功率曲线');
const timeRadio = ref('近一小时');
const isHourDisabled = ref(false);
const isDayAndMonthDisabled = ref(false);
const isPowActiveDisabled = ref(true);
const isLoadRateDisabled = ref(false);
const typeRadioShow = ref("最大")

const lineChartQueryParams = reactive({
  roomId: query.roomId as number | undefined,
  granularity: 'realtime',
  flag: 1
})

const createTimeData = ref<string[]>([]);
const allLineData = ref<string[]>([]);
const eqCreateTimeData = ref<string[]>([]);
const allEqData = ref<string[]>([]);
const lpCreateTimeData = ref<string[]>([]);
const allLoadPueData = ref<string[]>([]);
const eqValue = ref<number[]>([]);

const L1Data = ref<number[]>([]);
const L2Data = ref<number[]>([]);
const L3Data = ref<number[]>([]);

const dataTime = ref({
  L1DataTime: [],
  L2DataTime: [],
  L3DataTime: []
})

const numChartOptions = ref({
  title: { text: ''},
  tooltip: { trigger: 'item',
    formatter: '{b} : {c}',
    confine: true},
  grid: {
    bottom: 50,
    left: 30
  },
  legend: {
    data: ['PDU', '始端箱','插接箱'] // 图例项
  },
  xAxis: {
    type: 'category',nameLocation: 'end',
    data:['总数','在线','离线','告警']
  },
  yAxis: {
    type: 'value',
    boundaryGap: false
  },
  series: [
    {
      name: 'PDU',
      type: 'bar',
      data: [0,0,0,0],
    },
    {
      name: '始端箱',
      type: 'bar',
      data: [0,0,0,0],
    },
    {
      name: '插接箱',
      type: 'bar',
      data: [0,0,0,0],
    }
  ]
})

const powRoomChartOptions = ref({
  tooltip: {
    trigger: 'item',
    formatter: function (param) {
      let result = ''
      result += param.name + ':' + param.value;
      if (param.name === '总视在功率') {
        result += 'kVA';
      } else if(param.name === '总有功功率') {
        result += 'kW'
      } else if(param.name === '总无功功率') {
        result += 'kVar'
      }
      
      return result.trimEnd(); // 去除末尾多余的换行符
    },
    confine: true
  },
  series: [
    {
      type: 'pie',
      label: {
        show: false
      },
      data: [
        { value: 100, name: '总有功功率', itemStyle: { color: '#E5B849' } },
        { value: 100, name: '总无功功率', itemStyle: { color: '#C8603A' } },
        { value: 50, name: '总视在功率', itemStyle: { color: '#AD3762' } },
      ]
    }
  ]
});

const powChartOptionsBar = ref({
  title: { text: ''},
  tooltip: { trigger: 'item',
    formatter: '{b} : {c}',
    confine: true},
  grid: {
    left: '15%'
  },
  legend: {
    data: ['视在功率', '有功功率','无功功率'] // 图例项
  },
  xAxis: {
    type: 'category',
    data:[],
    axisLabel: {
      interval: 0, // 显示所有标签（0 表示不跳过任何标签）
    }
  },
  yAxis: {
    type: 'value',
    boundaryGap: false
  },
  series: [
    {
      name: '视在功率',
      type: 'bar',
      data: [],
    },
    {
      name: '有功功率',
      type: 'bar',
      data: [],
    },
    {
      name: '无功功率',
      type: 'bar',
      data: [],
    }
  ]
})

const powChartOptions = ref({
  // tooltip: {
  //   trigger: 'item',
  //   axisPointer: {
  //     type: 'shadow'
  //   },
  //   confine: true,
  //   formatter: function (params) {
  //     console.log('params', params)
  //     let result = '';
  //       // item 是每一个系列的数据
  //       const seriesName = params.name; // 系列名称
  //       const value = params.value[params.dataIndex]; // 数据值
  //       const marker = params.marker; // 标志图形
  //       result += `${marker}${seriesName}: ${value}kW·h<br/>`;
  //     return result;
  //   }
  // },
  legend: {
    data: ['视在功率', '有功功率', '无功功率'],
  },
  radar: {
    // shape: 'circle',
    indicator: [
      '柜列1'
    ]
  },
  series: [
    {
      name: '功率',
      type: 'radar',
      data: [
        {
          value: [0],
          name: '视在功率'
        },
        {
          value: [0],
          name: '有功功率'
        },
        {
          value: [0],
          name: '无功功率'
        }]
    }
  ]
});

const chartContainer2 = ref<HTMLElement | null>(null);
const chartContainer3 = ref<HTMLElement | null>(null);
const chartContainer4 = ref<HTMLElement | null>(null);
let myChart2 = null as echarts.ECharts | null; 
let myChart3 = null as echarts.ECharts | null; 
let myChart4 = null as echarts.ECharts | null; 


let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainer = ref<HTMLElement | null>(null);

const clickPower = async () => {
  switchChartContainer.value =1
  if(timeRadio.value == "近一小时") {
    timeRadio.value = '近一天'
    lineChartQueryParams.granularity = 'hour'
    isHourDisabled.value = true
  }

  const data3 = await MachineRoomApi.getLineChartData({...lineChartQueryParams,flag: 0});

  try {
    if (data3 != null){
      // 查到数据
      allEqData.value = data3
      console.log('allEqData',allEqData.value)
      if (timeRadio.value == '近一天' || '近三天'){
        eqCreateTimeData.value = data3.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm'));
      } else{
        eqCreateTimeData.value = data3.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }
      //L1Data.value = [];
      //L2Data.value = [];
      //L3Data.value = [];
      await initData();
      isHaveData.value = true
      initChart3()
    }else{
    }
    } finally {
  }
}

const initChart2 = () => {
    console.log("bbbbbbbbbbbbbbbb")
  if (chartContainer2.value && instance) {
    if (!myChart2) {
      myChart2 = echarts.init(chartContainer2.value);
    }
    myChart2.setOption(
      {
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          var result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            if(dataTime.value.L1DataTime.length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
              result += '发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
            }
            result += '&nbsp&nbsp' + params[i].value
            //判断是否给鼠标悬停上显示符号
            if(typeRadio.value === '电流') {
              result += ' A';
            }else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
              result += ' V';
            }else if (typeRadio.value === '有功功率') {
              result += ' kW';
            }else if (typeRadio.value === '无功功率') {
              result += ' kVar';
            }else if(typeRadio.value === '视在功率') {
              result += ' kVA'; 
            }else if(typeRadio.value === '功率曲线') {
              if(params[i].componentIndex == 0) {
                result += ' kVA'; 
              }else if(params[i].componentIndex == 1) {
                result += ' kW';
              }else if(params[i].componentIndex == 2) {
                result += ' kVar';
              }
            }else if (typeRadio.value === '负载率') {
              result += '%';
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { orient: 'horizontal', right: '25'},
        dataZoom:[{type: "inside"}],
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { 
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              // 根据 typeRadio 的值添加单位
              if (typeRadio.value === '电流') {
                return value + ' A';
              } else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
                return value + ' V';
              } else if (typeRadio.value === '有功功率') {
                return value + ' kW';
              } else if (typeRadio.value === '无功功率') {
                return value + ' kVar';
              } else if (typeRadio.value === '视在功率') {
                return value + ' kVA'; 
              } else if (typeRadio.value === '负载率') {
                return value + '%';
              } else if (typeRadio.value === '有效电能') {
                return value + ' kWh';
              } else {
                return value; // 如果没有匹配，返回原值
              }
            }
          }
        },
        grid: {
          left: '6%',   // 设置左侧边距
          right: '6%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: typeRadio.value === '功率曲线' ? [
          {name: '总视在功率', type: 'line', symbol: 'none', data: L1Data.value},
          {name: '总有功功率', type: 'line', symbol: 'none', data: L2Data.value},
          {name: '总无功功率', type: 'line', symbol: 'none', data: L3Data.value},
        ] : [
          {name: '统计', type: 'line', symbol: 'none', data: L1Data.value},
          {name: 'A路', type: 'line', symbol: 'none', data: L2Data.value},
          {name: 'B路', type: 'line', symbol: 'none', data: L3Data.value},
        ],
      }
    );
    instance.appContext.config.globalProperties.myChart2 = myChart2;
    console.log("aaaaaaaaaaaaaaaaaaaaaaaaa")
  }
}

const initChart3 = () => {
  if (chartContainer3.value && instance) {
    if (!myChart3) {
      myChart3 = echarts.init(chartContainer3.value);
    }
    myChart3.setOption(
      {
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          var result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
            //判断是否给鼠标悬停上显示符号
            if(typeRadio.value === '电流') {
              result += ' A';
            }else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
              result += ' V';
            }else if (typeRadio.value === '有功功率') {
              result += ' kW';
            }else if (typeRadio.value === '无功功率') {
              result += ' kVar';
            }else if(typeRadio.value === '视在功率') {
              result += ' kVA'; 
            }else if (typeRadio.value === '负载率') {
              result += '%';
            }else if (typeRadio.value === '有效电能') {
              result += ' kWh';
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { orient: 'horizontal', right: '25'},
        dataZoom:[{type: "inside"}],
        xAxis: {type: 'category', boundaryGap: false, data:eqCreateTimeData.value},
        yAxis: { 
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              // 根据 typeRadio 的值添加单位
              if (typeRadio.value === '电流') {
                return value + ' A';
              } else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
                return value + ' V';
              } else if (typeRadio.value === '有功功率') {
                return value + ' kW';
              } else if (typeRadio.value === '无功功率') {
                return value + ' kVar';
              } else if (typeRadio.value === '视在功率') {
                return value + ' kVA'; 
              } else if (typeRadio.value === '负载率') {
                return value + '%';
              } else if (typeRadio.value === '有效电能') {
                return value + ' kWh';
              } else {
                return value; // 如果没有匹配，返回原值
              }
            }
          }
        },
        grid: {
          left: '10%',   // 设置左侧边距
          right: '8%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: typeRadio.value === '功率曲线' ? [
          {name: '电量', type: 'line', symbol: 'none', data: eqValue.value},
        ] : (typeRadio.value === 'PUE' ? [
          {name: 'PUE', type: 'line', symbol: 'none', data: eqValue.value},
        ] : [
          {name: '负载率', type: 'line', symbol: 'none', data: eqValue.value},
        ]),
      }
    );

    instance.appContext.config.globalProperties.myChart3 = myChart3;
  }
}

const initChart4 = () => {
  // console.log(L4Data.value,L5Data.value,L6Data.value)
  if (chartContainer4.value && instance) {
    if (!myChart4) {
      myChart4 = echarts.init(chartContainer4.value);
    }
    myChart4.setOption(
      {
        title: { text: ''},
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          var result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            if(dataTime.value.L1DataTime.length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
              result += '发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
            }
            result += '&nbsp&nbsp' + params[i].value
            //判断是否给鼠标悬停上显示符号
            if(typeRadio.value === '电流') {
              result += ' A';
            }else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
              result += ' V';
            }else if (typeRadio.value === '有功功率') {
              result += ' kW';
            }else if (typeRadio.value === '无功功率') {
              result += ' kVar';
            }else if(typeRadio.value === '视在功率') {
              result += ' kVA'; 
            }else if (typeRadio.value === '负载率') {
              result += '%';
            }
            result += '<br>';
          }
          return result;
        }},
        legend: { orient: 'horizontal', right: '25'},
        dataZoom:[{type: "inside"}],
        xAxis: {type: 'category', boundaryGap: false, data:lpCreateTimeData.value},
        yAxis: { 
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              // 根据 typeRadio 的值添加单位
              if (typeRadio.value === '电流') {
                return value + ' A';
              } else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
                return value + ' V';
              } else if (typeRadio.value === '有功功率') {
                return value + ' kW';
              } else if (typeRadio.value === '无功功率') {
                return value + ' kVar';
              } else if (typeRadio.value === '视在功率') {
                return value + ' kVA'; 
              } else if (typeRadio.value === '负载率') {
                return value + '%';
              } else if (typeRadio.value === '有效电能') {
                return value + ' kWh';
              } else {
                return value; // 如果没有匹配，返回原值
              }
            }
          }
        },
        grid: {
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          {name: typeRadio.value, type: 'line', symbol: 'none', data: L1Data.value},
        ],
      }
    );
    instance.appContext.config.globalProperties.myChart4 = myChart4;

  }
}

const isHaveData = ref(true)
// 获取折线图数据
const getLineChartData =async () => {
  if(!lineChartQueryParams.roomId) {
    return
  }
 try {

    allLineData.value = []
    allLoadPueData.value = []
    allEqData.value = []


    const data = await MachineRoomApi.getLineChartData(lineChartQueryParams);
    console.log('获取折线图数据',data)
    console.log('lineChartQueryParams',lineChartQueryParams)

    if (data != null){
      // 查到数据
      allLoadPueData.value = data
      console.log('allLoadPueData',allLoadPueData.value)
      if (timeRadio.value == '近一小时'){
        lpCreateTimeData.value = data.map((item) => formatDate(item.create_time,'YYYY-MM-DD HH:mm'));
      }else if (timeRadio.value == '近一天' || '近三天'){
        lpCreateTimeData.value = data.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm'));
      } else{
        lpCreateTimeData.value = data.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }
      //L1Data.value = [];
      //L2Data.value = [];
      //L3Data.value = [];
      await initData();
      isHaveData.value = true
      initChart4()
    }else{
    }

    const data2 = await MachineRoomApi.getLineChartData({...lineChartQueryParams,flag: 2});

    if (data2 != null){
      // 查到数据
      allLineData.value = data2
      console.log('allLineData',allLineData.value)
      if (timeRadio.value == '近一小时'){
        createTimeData.value = data2.map((item) => formatDate(item.create_time,'YYYY-MM-DD HH:mm'));
      }else if (timeRadio.value == '近一天' || '近三天'){
        createTimeData.value = data2.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm'));
      } else{
        createTimeData.value = data2.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }
      //L1Data.value = [];
      //L2Data.value = [];
      //L3Data.value = [];
      await initData();
      isHaveData.value = true
      initChart2()
    }else{
    }

 } finally {
 }
}

const initData = () => {
  dataTime.value = {
    L1DataTime: [],
    L2DataTime: [],
    L3DataTime: []
  }
  if(timeRadio.value == '近一天' || timeRadio.value == '近三天' || timeRadio.value == '近一月'){
    switch (typeRadio.value){
      case '有效电能':
        if(allEqData.value != null && timeRadio.value == '近一月'){
        eqValue.value = allEqData.value.map((item) => item.eq_value.toFixed(3));
        } else if (allEqData.value != null && (timeRadio.value == '近三天' || timeRadio.value == '近一天')){
        eqValue.value = allEqData.value.map((item) => item.ele_total.toFixed(3));
        }
        break;
      case '负载率':
        let itemLoadLineType = typeRadioShow.value == "最小" ? 'load_rate_min_value' : (typeRadioShow.value == "最大" ? 'load_rate_max_value' : 'load_rate_avg_value')
        let itemLoadLineTimeType = typeRadioShow.value == "最小" ? 'load_rate_min_time' : (typeRadioShow.value == "最大" ? 'load_rate_max_time' : '')
        if(allLoadPueData.value != null){
          L1Data.value = allLoadPueData.value.map((item) => item[itemLoadLineType].toFixed(0));
          if(itemLoadLineTimeType != '') {
            dataTime.value.L1DataTime = allLoadPueData.value.map((item) => item[itemLoadLineTimeType] ? item[itemLoadLineTimeType].slice(0, -3) : '');
          }
        }
        break;
      case 'PUE':
        if(allLoadPueData.value != null){
        L1Data.value = allLoadPueData.value.map((item) => item.room_pue_avg_value.toFixed(2));
        }
        break;
      case '功率曲线':
        let itemPowType = typeRadioShow.value == "最小" ? '_min_' : (typeRadioShow.value == "最大" ? '_max_' : '_avg_')
        if(allLineData.value != null){
          L1Data.value = allLineData.value.map((item) => item[`apparent_total${itemPowType}value`].toFixed(3));
          L2Data.value = allLineData.value.map((item) => item[`active_total${itemPowType}value`].toFixed(3));
          L3Data.value = allLineData.value.map((item) => item[`reactive_total${itemPowType}value`].toFixed(3));

          if(itemPowType != '_avg_') {
            dataTime.value.L1DataTime = allLineData.value.map((item) => item[`apparent_total${itemPowType}time`] ? item[`apparent_total${itemPowType}time`].slice(0, -3) : '');
            dataTime.value.L2DataTime = allLineData.value.map((item) => item[`active_total${itemPowType}time`] ? item[`active_total${itemPowType}time`].slice(0, -3) : '');
            dataTime.value.L3DataTime = allLineData.value.map((item) => item[`reactive_total${itemPowType}time`] ? item[`reactive_total${itemPowType}time`].slice(0, -3) : '');
          }
        }
       break;
      }
  } else if(timeRadio.value == '近一小时'){
    switch (typeRadio.value){
      case '有效电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.map((item) => item.ele_total.toFixed(3));
        }
        break;
      case '负载率':
        if(allLoadPueData.value != null){
        L1Data.value = allLoadPueData.value.map((item) => item.load_rate.toFixed(0));
        }
        break;
      case 'PUE':
        if(allLoadPueData.value != null){
        L1Data.value = allLoadPueData.value.map((item) => item.room_pue.toFixed(2));
        }
        break;
      case '功率曲线':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.map((item) => item.apparent_total.toFixed(3));
        L2Data.value = allLineData.value.map((item) => item.active_total.toFixed(3));
        L3Data.value = allLineData.value.map((item) => item.reactive_total.toFixed(3));
        }
       break;
      }
  }
}

watch( ()=>typeRadioShow.value, async()=>{
  await initData()
  await initChart2()
  await initChart4()
})
// 监听切换类型
watch( ()=>typeRadio.value, async(value)=>{
  //L1Data.value = [];
  //L2Data.value = [];
  //L3Data.value = [];
  await initData();
  if ( value == '有效电能'){
     // 选有效电能不能选近一小时
     isHourDisabled.value = true
  }else{
    isHourDisabled.value = false
  }
  // 更新数据后重新渲染图表
  myChart2?.setOption({
        tooltip: { trigger: 'axis' ,formatter: function(params) {
          var result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            if(dataTime.value.L1DataTime.length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
              result += '发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
            }
            result += '&nbsp&nbsp' + params[i].value
            //判断是否给鼠标悬停上显示符号
            if(typeRadio.value === '电流') {
              result += ' A';
            }else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
              result += ' V';
            }else if (typeRadio.value === '有功功率') {
              result += ' kW';
            }else if (typeRadio.value === '无功功率') {
              result += ' kVar';
            }else if(typeRadio.value === '视在功率') {
              result += ' kVA'; 
            }else if(typeRadio.value === '功率曲线') {
              if(params[i].componentIndex == 0) {
                result += ' kVA'; 
              }else if(params[i].componentIndex == 1) {
                result += ' kW';
              }else if(params[i].componentIndex == 2) {
                result += ' kVar';
              }
            }else if (typeRadio.value === '负载率') {
              result += '%';
            }else if (typeRadio.value === '有效电能') {
              result += ' kWh';
            }
            result += '<br>';
          }
          return result;
        }},
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { 
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              // 根据 typeRadio 的值添加单位
              if (typeRadio.value === '电流') {
                return value + ' A';
              } else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
                return value + ' V';
              } else if (typeRadio.value === '有功功率') {
                return value + ' kW';
              } else if (typeRadio.value === '无功功率') {
                return value + ' kVar';
              } else if (typeRadio.value === '视在功率') {
                return value + ' kVA'; 
              } else if (typeRadio.value === '负载率') {
                return value + '%';
              } else if (typeRadio.value === '有效电能') {
                return value + ' kWh';
              } else {
                return value; // 如果没有匹配，返回原值
              }
            }
          }
        },
                grid: {
          left: '6%',   // 设置左侧边距
          right: '6%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
      series: typeRadio.value === '功率曲线' ? [
        {name: '总视在功率', type: 'line', symbol: 'none', data: L1Data.value},
        {name: '总有功功率', type: 'line', symbol: 'none', data: L2Data.value},
        {name: '总无功功率', type: 'line', symbol: 'none', data: L3Data.value},
      ] : [
        {name: '统计', type: 'line', symbol: 'none', data: L1Data.value},
        {name: 'A路', type: 'line', symbol: 'none', data: L2Data.value},
        {name: 'B路', type: 'line', symbol: 'none', data: L3Data.value},
      ],
    });
  myChart3?.setOption({
      tooltip: { trigger: 'axis' ,formatter: function(params) {
        var result = params[0].name + '<br>';
        for (var i = 0; i < params.length; i++) {
          result +=  params[i].marker + params[i].seriesName + ': &nbsp&nbsp&nbsp&nbsp' + params[i].value;
          //判断是否给鼠标悬停上显示符号
          if(typeRadio.value === '电流') {
            result += ' A';
          }else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
            result += ' V';
          }else if (typeRadio.value === '有功功率') {
            result += ' kW';
          }else if (typeRadio.value === '无功功率') {
            result += ' kVar';
          }else if(typeRadio.value === '视在功率') {
            result += ' kVA'; 
          }else if (typeRadio.value === '负载率') {
            result += '%';
          }else if (typeRadio.value === '有效电能') {
            result += ' kWh';
          }
          result += '<br>';
        }
        return result;
      }},
      yAxis: { 
        type: 'value',
        axisLabel: {
          formatter: function(value) {
            // 根据 typeRadio 的值添加单位
            if (typeRadio.value === '电流') {
              return value + ' A';
            } else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
              return value + ' V';
            } else if (typeRadio.value === '有功功率') {
              return value + ' kW';
            } else if (typeRadio.value === '无功功率') {
              return value + ' kVar';
            } else if (typeRadio.value === '视在功率') {
              return value + ' kVA'; 
            } else if (typeRadio.value === '负载率') {
              return value + '%';
            } else if (typeRadio.value === '有效电能') {
              return value + ' kWh';
            } else {
              return value; // 如果没有匹配，返回原值
            }
          }
        }
      },            
              grid: {
          left: '8%',   // 设置左侧边距
          right: '8%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
      series: [
        {name: '电量', type: 'line', symbol: 'none', data: eqValue.value},
      ],
    });
    // 更新数据后重新渲染图表
    myChart4?.setOption({
      tooltip: { trigger: 'axis' ,formatter: function(params) {
          var result = params[0].name + '<br>';
          for (var i = 0; i < params.length; i++) {
            result +=  params[i].marker + params[i].seriesName;
            if(dataTime.value.L1DataTime.length && dataTime.value[`L${i+1}DataTime`][params[i].dataIndex] != '') {
              result += '发生时间:' + dataTime.value[`L${i+1}DataTime`][params[i].dataIndex]
            }
            result += '&nbsp&nbsp' + params[i].value
            //判断是否给鼠标悬停上显示符号
            if(typeRadio.value === '电流') {
              result += ' A';
            }else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
              result += ' V';
            }else if (typeRadio.value === '有功功率') {
              result += ' kW';
            }else if (typeRadio.value === '无功功率') {
              result += ' kVar';
            }else if(typeRadio.value === '视在功率') {
              result += ' kVA'; 
            }else if (typeRadio.value === '负载率') {
              result += '%';
            }else if (typeRadio.value === '有效电能') {
              result += ' kWh';
            }
            result += '<br>';
          }
          return result;
        }},
        xAxis: {type: 'category', boundaryGap: false, data:lpCreateTimeData.value},
        yAxis: { 
          type: 'value',
          axisLabel: {
            formatter: function(value) {
              // 根据 typeRadio 的值添加单位
              if (typeRadio.value === '电流') {
                return value + ' A';
              } else if (typeRadio.value === '电压' || typeRadio.value === '线电压') {
                return value + ' V';
              } else if (typeRadio.value === '有功功率') {
                return value + ' kW';
              } else if (typeRadio.value === '无功功率') {
                return value + ' kVar';
              } else if (typeRadio.value === '视在功率') {
                return value + ' kVA'; 
              } else if (typeRadio.value === '负载率') {
                return value + '%';
              } else if (typeRadio.value === '有效电能') {
                return value + ' kWh';
              } else {
                return value; // 如果没有匹配，返回原值
              }
            }
          }
        },
                grid: {
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
      series: [
        {name: typeRadio.value, type: 'line', symbol: 'none', data: L1Data.value},
      ],
    });
});

// 监听切换时间颗粒度
watch( ()=>timeRadio.value, async(value)=>{
  if ( value == '近一小时'){
    // 选近一小时不能选有效电能
    isPowActiveDisabled.value = true
    isLoadRateDisabled.value = false
    lineChartQueryParams.granularity = 'realtime'
  }else if (value == '近一天'){
    isPowActiveDisabled.value = false
    isLoadRateDisabled.value = true//没选中近一个小时不能选负载率
    lineChartQueryParams.granularity = 'hour'
  }else if (value == '近三天'){
    isPowActiveDisabled.value = false
    isLoadRateDisabled.value = true//没选中近一个小时不能选负载率
    lineChartQueryParams.granularity = 'SeventyHours'
  }else{
    isPowActiveDisabled.value = false
        isLoadRateDisabled.value = true//没选中近一个小时不能选负载率
    lineChartQueryParams.granularity = 'day'
  }
  if(typeRadio.value == "有效电能") {
    await clickPower()
  }
  else {
    await getLineChartData()
  }

});

//获取数据
const handleGetRoomId = (data) => {
  roomId.value = data
  lineChartQueryParams.roomId = data
  // getRoomDataDetail()
  getRoomDevData()
  // getRoomPowData()
  // getRoomEchartData()
  // getRoomEnvData()
  // getRoomEqData()
  getLineChartData()
  console.log('handleGetRoomId', data)
}
// 获取机房数据详情
const getRoomDataDetail = async() => {
  const res = await MachineRoomApi.getRoomDataDetail({id: roomId.value})
  console.log('***getRoomDataDetail', res)
}
// 获取机房主页面设备数据
const getRoomDevData = async() => {
  const res = await MachineRoomApi.getMachineNum({roomId: roomId.value})

  console.log('***获取机房主页面设备数据', res)

  numChartOptions.value.series = [
    {
      name: 'PDU',
      type: 'bar',
      data: [res.pduNum,res.pduOnLine,res.pduOffLine,res.pduInform],
    },
    {
      name: '始端箱',
      type: 'bar',
      data: [res.busNum,res.busOnLine,res.busOffLine,res.busInform],
    },
    {
      name: '插接箱',
      type: 'bar',
      data: [res.boxNum,res.boxOnLine,res.boxOffLine,res.boxInform],
    }
  ]
  
  Object.assign(deviceInfo, res)
}
// 获取机房主页面功率数据
const getRoomPowData = async() => {
  const res = await MachineRoomApi.getRoomPowData({id: roomId.value})
  Object.assign(roomDownVal, res)
  console.log('***获取机房主页面功率数据', res)
}
// 获取机房主页面曲线数据
const getRoomEchartData = async() => {
  const res = await MachineRoomApi.getRoomEchartData({id: roomId.value})
  console.log('***获取机房主页面曲线数据', res)
  Object.assign(echartInfo, {
    powList: res.powList || [],
    eqList: res.eqList || []
  })
  switchTrend(radioBtn.value, true)
  console.log('....', echartInfo)
}
// 获取机房主页面环境数据
const getRoomEnvData = async() => {
  const res = await MachineRoomApi.getRoomEnvData({id: roomId.value})
  Object.assign(envInfo, res)
  console.log('***获取机房主页面环境数据', envInfo)
}
// 获取机房主页面用能
const getRoomEqData = async() => {
  const res = await MachineRoomApi.getRoomEqData({id: roomId.value})
  Object.assign(EqInfo, res)
  console.log('***获取机房主页面用能', res)
}
// 获取空间信息 ?需不需要刷新
const handleBackData = (data) => {
  console.log('***',data)
  Object.assign(roomDownVal, data)

  
  updateChart()
  getRoomEqData()
  getHomeAlarmData()
  updatePowChartOptions()
  loading.value = false
}

const updatePowChartOptions = () => {
  let powApparentData
  let powActiveData
  let powReactiveData
  if(roomDownVal.aisleList) {
    powApparentData = roomDownVal.aisleList.map(item => item.powApparentTotal ? item.powApparentTotal.toFixed(3) : '0.000')
    powActiveData = roomDownVal.aisleList.map(item => item.powActiveTotal ? item.powActiveTotal.toFixed(3) : '0.000')
    powReactiveData = roomDownVal.aisleList.map(item => item.powReactiveTotal ? item.powReactiveTotal.toFixed(3) : '0.000')
  } else {
    powApparentData = []
    powActiveData = []
    powReactiveData = []
  }
  if(roomDownVal.aisleList.length >= 6) {
    powChartOptions.value.radar.indicator = roomDownVal.aisleList.map(item => ({name: item.aisleName}))
  
    powChartOptions.value.series = [
      {
        name: '功率',
        type: 'radar',
        data: [
          {
            value: powApparentData,
            name: '视在功率'
          },
          {
            value: powActiveData,
            name: '有功功率'
          },
          {
            value: powReactiveData,
            name: '无功功率'
          }
        ]
      }
    ]

    powRoomChartOptions.value.series = [
      {
        type: 'pie',
        label: {
          show: false
        },
        data: [
          { value: roomDownVal.powActive ? roomDownVal.powActive.toFixed(3) : '0.000', name: '总有功功率', itemStyle: { color: '#E5B849' } },
          { value: roomDownVal.powReactive ? roomDownVal.powReactive.toFixed(3) : '0.000', name: '总无功功率', itemStyle: { color: '#C8603A' } },
          { value: roomDownVal.powApparent ? roomDownVal.powApparent.toFixed(3) : '0.000', name: '总视在功率', itemStyle: { color: '#AD3762' } },
        ]
      }
    ]
  } else {
    powChartOptionsBar.value.xAxis = {
      type: 'category',
      data: roomDownVal.aisleList.map(item => item.aisleName),
      axisLabel: {
        interval: 0, // 显示所有标签（0 表示不跳过任何标签）
      }
    }
    powChartOptionsBar.value.series = [
      {
        name: '视在功率',
        type: 'bar',
        data: powApparentData,
      },
      {
        name: '有功功率',
        type: 'bar',
        data: powActiveData,
      },
      {
        name: '无功功率',
        type: 'bar',
        data: powReactiveData,
      }
    ]
  }
  console.log(powChartOptionsBar.value)
}

const getHomeAlarmData = async() => {
  //获取告警轮播信息
  const res2 = await AlarmApi.getAlarmRecord({
    pageNo: 1,
    pageSize: 30,
    alarmStatus: [0],
    roomId: Number(roomId.value)
  })
  if (res2.list) {
    alarmData.value = res2.list
  }
}

const initChart = () => {
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',confine: true,      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === '平均温度' || param.seriesName === '最高温度') {
            result += '℃';
          }else{
            result += '%'
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['平均温度', '最高温度'], // 图例项
      },
      xAxis: {
        type: 'category',nameLocation: 'end',
        data:['前门','后门']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '平均温度',
          type: 'bar',
          data: [roomDownVal.temAvgFront ? roomDownVal.temAvgFront.toFixed(1) : 0,roomDownVal.temAvgBlack ? roomDownVal.temAvgBlack.toFixed(1) : 0],
        },
        {
          name: '最高温度',
          type: 'bar',
          data: [roomDownVal.temMaxFront ? roomDownVal.temMaxFront.toFixed(1) : 0,roomDownVal.temMaxBlack ? roomDownVal.temMaxBlack.toFixed(1) : 0],
        }
      ]
  })
}

const updateChart = () => {
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  if(toggleTable.value === true){
    lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',confine: true,      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === '平均温度' || param.seriesName === '最高温度') {
            result += '℃';
          }else{
            result += '%'
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['平均温度', '最高温度'], // 图例项
      },
      grid: {
        bottom: '10%'
      },
      xAxis: {
        type: 'category',nameLocation: 'end',
        data:['前门','后门']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '平均温度',
          type: 'bar',
          data: [roomDownVal.temAvgFront ? roomDownVal.temAvgFront.toFixed(1) : 0,roomDownVal.temAvgBlack ? roomDownVal.temAvgBlack.toFixed(1) : 0],
        },
        {
          name: '最高温度',
          type: 'bar',
          data: [roomDownVal.temMaxFront ? roomDownVal.temMaxFront.toFixed(1) : 0,roomDownVal.temMaxBlack ? roomDownVal.temMaxBlack.toFixed(1) : 0],
        }
      ]
    })
    if(!roomDownVal.temAvgFront && !roomDownVal.temAvgBlack && !roomDownVal.temMaxFront && !roomDownVal.temMaxBlack && loading.value) {
      toggleAlarm.value = true
    }
  }else if(toggleTable.value === false){
    lineidChart.setOption( {
      title: { text: ''},
      tooltip: { trigger: 'axis',confine: true,      formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === '平均温度' || param.seriesName === '最高温度') {
            result += '℃';
          }else{
            result += '%'
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      legend: {
        data: ['平均湿度','最高湿度'], // 图例项
      },
      grid: {
        bottom: '10%'
      },
      xAxis: {
        type: 'category',nameLocation: 'end',
        data:['前门','后门']
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '平均湿度',
          type: 'bar',
          data: [roomDownVal.humAvgFront ? roomDownVal.humAvgFront.toFixed(0) : 0,roomDownVal.humAvgBlack ? roomDownVal.humAvgBlack.toFixed(0) : 0],
        },
        {
          name: '最高湿度',
          type: 'bar',
          data: [roomDownVal.humMaxFront ? roomDownVal.humMaxFront.toFixed(0) : 0,roomDownVal.humMaxBlack ? roomDownVal.humMaxBlack.toFixed(0) : 0],
        }
      ]
    })
    if(!roomDownVal.humAvgFront && !roomDownVal.humAvgBlack && !roomDownVal.humMaxFront && !roomDownVal.humMaxBlack && loading.value) {
      toggleAlarm.value = true
    }
  }
}

//配置ECharts图表
const switchTrend = (type, first = false) => {
  if (type == radioBtn.value && !first) return
  radioBtn.value = type
  if (type == 'pow') {
    echartOptionsPower.value = {
      title: {
        text: '功率曲线'
      },
      grid: {
        left: '0',
        right: '20',
        bottom: '0',
        top: '40',
        containLabel: true
      },
      legend: {},
      xAxis: {
        type: 'category',
        data: echartInfo.powList.map(item => item.dateTime)
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          name: '有功功率',
          data: echartInfo.powList.map(item => item.activePow ? item.activePow.toFixed(3) : '0.000'),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
        {
          name: '无功功率',
          data: echartInfo.powList.map(item => item.reactivePow ? item.reactivePow.toFixed(3) : '0.000'),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
        {
          name: '视在功率',
          data: echartInfo.powList.map(item => item.apparentPow ? item.apparentPow.toFixed(3) : '0.000'),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
      ]
    }
    console.log('echartOptionsPower', echartOptionsPower.value)
  } else if (type == 'ele') {
    echartOptionsPower.value = {
      title: {
        text: '用能曲线'
      },
      grid: {
        left: '0',
        right: '20',
        bottom: '0',
        top: '40',
        containLabel: true
      },
      legend: {selectedMode: 'single'},
      xAxis: {
        type: 'category',
        data: echartInfo.eqList.map(item => item.dateTime)
      },
      yAxis: {
        type: 'value'
      },
      series: [//配置图表的数据系列
        {
          name: '近一个月用能',
          data: echartInfo.eqList.map(item => item.eq),
          type: 'line',
          smooth: true,
          symbol: 'none',
        },
      ]
    }
  }
}

const startScrolling = () => {
  // 检查是否已经有一个定时器在运行
  if (scrollIntervalOne || isScrollingManually) return;
 
  scrollIntervalOne = setInterval(() => {
    // 设备模块的滚动逻辑
    scrollContainer('scrollableContainerOne');
  }, 1000);
};
 
const scrollContainer = (containerName) => {
  let containerRef, interval;
  if (containerName === 'scrollableContainerOne') {
    containerRef = scrollableContainerOne;
    interval = scrollIntervalOne;
  }

  // 检查 containerRef.value 是否存在，用来解决控制台报错
  if (!containerRef?.value) {
    return; // 可以返回一个特定的值或对象来表示错误
  }
 
  const { scrollTop, clientHeight, scrollHeight } = containerRef.value;
  const scrollStep = 10; // 滚动步长
  const scrollTolerance = -10; // 停止前的容忍范围
 
  if (scrollTop + clientHeight >= scrollHeight) {
    // 滚动到顶部
    containerRef.value.scrollTop = 0;
  } else if (scrollTop + scrollStep + scrollTolerance >= scrollHeight - clientHeight) {
    // 接近底部时停止定时器
    clearInterval(interval);
    scrollIntervalOne = null;
  } else {
    // 继续滚动
    containerRef.value.scrollTop += scrollStep;
  }
};
 
const stopScrolling = () => {
  clearInterval(scrollIntervalOne);
  scrollIntervalOne = null;
};
 
const handleScroll = (event, containerName) => {
  // 停止自动滚动
  isScrollingManually = true;
  stopScrolling();
 
  // 设置延迟来判断滚动是否停止
  clearTimeout(scrollTimeout);
  scrollTimeout = setTimeout(() => {
    // 如果在延迟期间没有新的滚动事件，则恢复自动滚动
    isScrollingManually = false;
    startScrolling();
  }, 1000); // 延迟时间，单位毫秒，可以根据需要调整
};

onMounted(() => {
  const centerEle = document.getElementById('center')
  containerInfo.width = centerEle?.offsetWidth as number
  console.log('centerEle', containerInfo.width, centerEle?.offsetWidth, centerEle?.offsetHeight)
  if (scrollableContainerOne.value) {
    scrollableContainerOne.value.addEventListener('scroll', (event) => handleScroll(event, 'scrollableContainerOne'));
  }
 
  // 初始启动自动滚动
  startScrolling();
})
 
onUnmounted(() => {
  // 移除滚动事件监听器
  if (scrollableContainerOne.value) {
    scrollableContainerOne.value.removeEventListener('scroll', (event) => handleScroll(event, 'scrollableContainerOne'));
  }
 
  // 确保在组件卸载时清除定时器
  stopScrolling();
});
</script>

<style lang="scss" scoped>
.homeContainer {
  box-sizing: border-box;
  width: 100%;
  // height: calc(100vh - 120px);
  min-height: 550px;
  // max-height: calc(100vh - 120px);
  box-sizing: border-box;
  // background-color: #999;
  display: flex;
  .center {
    flex: 1;
    box-sizing: border-box;
    overflow: auto;
    margin: 0 15px;
    position: relative;
    display: flex;
    flex-direction: column;
    .CabEchart{
      flex: 1;
      min-height: 200px;
      position: relative;
      .btns {
        position: absolute;
        z-index: 9;
        right: 30px;
        top: 20px;
      }
    }
  }
  .left {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }
  .right {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }
}
.progress {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  height: 21vh;
  .percentage-value {
    display: block;
    margin-top: 50px;
    font-size: 40px;
  }
  .percentage-label {
    display: block;
    margin-top: 10px;
    font-size: 16px;
  }
  .percentage-unit {
    display: block;
    font-size: 14px;
  }
}

.bullet {
  display: inline-block;
  margin-right: 5px;
  border-radius: 50%;
  width: 20px;
  height: 20px;
}

.count_img {
  display: inline-block;
  width: 40px;
  height: 40px;
}
 
.powActiveTotal-text {
  position: absolute;
  top: 0; /* 放置在原元素的 */
  right: 0;
  color: #000;
  padding: 5px;
  border-radius: 3px;
  white-space: nowrap;
  opacity: 0;
  transition: opacity 0.3s; /* 添加过渡效果 */
  pointer-events: none; /* 确保提示框不会干扰鼠标事件 */
}
 
:deep(.progress:hover .powActiveTotal-text) {
  opacity: 1; /* 鼠标悬停时显示 */
}

:deep(.el-card) {
  margin-bottom: 15px;
}
:deep(.el-card__header) {
  padding: 15px;
}
:deep(.el-progress-circle) {
  width: 21vh;
  height: 21vh;
}
:deep(.el-card__body) {
  padding: 15px;
}
:deep(.CabEchart .el-card__body) {
  width: 100%;
  height: 100%;
  & > div {
    width: 100%;
    height: 100%;
    box-sizing: border-box;
    padding: 5 20px;
  }
}

@media screen and (min-width:2048px){
  .scrollable-container-one{
    height: 33vh;
    overflow-y: auto;
  }
}

@media screen and (max-width:2048px) and (min-width:1600px){
  .scrollable-container-one{
    height: 30vh;
    overflow-y:auto;
  }
}

@media screen and (max-width:1600px){
  .scrollable-container-one{
    height: 42vh;
    overflow-y: auto;
  }
}

.responsive-table {
  table-layout: fixed;  /* 确保列宽按照设置的宽度显示 */
  word-wrap: break-word; /* 文本过长时换行 */
}
</style>
