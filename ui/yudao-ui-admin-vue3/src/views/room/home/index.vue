<template>
   <div class="homeContainer">
    <div class="left">
      <ContentWrap>
        <div class="progress">
          <el-form
            class="-mb-15px"
            ref="queryFormRef"
            :inline="true"
            label-width="68px"
          >
            <el-form-item label="" prop="jf" >
              机房：{{roomDownVal?.roomName}}
            </el-form-item >
          </el-form>
        </div>
      </ContentWrap>
      <ContentWrap>
        <div style="font-weight: bold">
          功率
        </div>
        <div class="progress">
          <!--<el-progress type="dashboard" :percentage="roomDownVal.powApparent && roomDownVal.powApparent.toFixed()">-->
          <el-progress type="dashboard" :percentage="roomDownVal.powActive/roomDownVal.powApparent*100" width="200">
              <span class="percentage-value">{{ roomDownVal.powApparent ? roomDownVal.powApparent.toFixed(0) : 0 }}</span>
              <span class="percentage-label">总视在功率</span>
              <span class="percentage-unit">kVA</span>
          </el-progress>
          <div class="powActiveTotal-text">
           <span>{{ roomDownVal.powActive ? roomDownVal.powActive.toFixed(0) : 0 }}kW</span>
          </div>
        </div>
      </ContentWrap>
      <!--<el-card shadow="never">
        <template #header>
          <div>空间管理</div>
        </template>
        <div>未用空间：{{spaceInfo.freeSpace}}u</div>
        <div>已用空间：{{spaceInfo.usedSpace}}u</div>
        <div>总空间：{{spaceInfo.totalSpace}}u</div>
        <div>机柜数：{{spaceInfo.cabNum}}</div>
      </el-card>-->
      <el-card shadow="never" style="margin-bottom: 15px;font-size: 12px;">
        <div style="height:22.3vh;display: flex;flex-direction: column;justify-content: space-between">
          <div style="font-weight: bold;font-size: 16px">
            用能
          </div>
          <!--<template #header>
            <div>当前用能</div>
          </template>
          <div>今日：{{EqInfo.todayEq && EqInfo.todayEq.toFixed(2)}}kW·h</div>
          <div>本周：{{EqInfo.thisWeekEq && EqInfo.thisWeekEq.toFixed(2)}}kW·h</div>
          <div>本月：{{EqInfo.thisMonthEq  && EqInfo.thisMonthEq.toFixed(2)}}kW·h</div>-->
          <div style="display: flex; align-items: center; margin-bottom:2vh; margin-top:1vh;">
            <span>今日用能：</span>
            <span 
              style="display: inline-block; position: relative; width: 5vw;"
            >
              <el-progress :stroke-width="26" :format="format"  :percentage="EqInfo.todayEq/EqInfo.yesterdayEq*100" style="width: 9vw;" />
              <div style="position: absolute; bottom: 10%; left: 77%; transform: translateX(-50%); color: #000; border-radius: 3px; white-space: nowrap;">
                {{EqInfo.todayEq ? EqInfo.todayEq.toFixed(1) : 0}}kWh
              </div>
            </span>
            <span style="margin-left:3vh;">{{EqInfo.yesterdayEq ? EqInfo.yesterdayEq.toFixed(1) : 0}}kWh</span>
          </div>
          <div style="display: flex; align-items: center; margin-bottom:2vh; margin-top:1vh;">
            <span>本周用能：</span>
            <span 
              style="display: inline-block; position: relative; width: 5vw;"
            >
              <el-progress :stroke-width="26" :format="format"  :percentage="EqInfo.thisWeekEq/EqInfo.lastWeekEq*100" style="width: 9vw;" />
              <div style="position: absolute; bottom: 10%; left: 77%; transform: translateX(-50%); color: #000; border-radius: 3px; white-space: nowrap;">
                {{EqInfo.thisWeekEq ? EqInfo.thisWeekEq.toFixed(1) : 0}}kWh
              </div>
            </span>
            <span style="margin-left:3vh;">{{EqInfo.lastWeekEq ? EqInfo.lastWeekEq.toFixed(1) : 0}}kWh</span>
          </div>
          <div style="display: flex; align-items: center; margin-bottom:2vh; margin-top:1vh;">
            <span>本月用能：</span>
            <span 
              style="display: inline-block; position: relative; width: 5vw;"
            >
              <el-progress  :stroke-width="26" :format="format" :percentage="EqInfo.thisMonthEq/EqInfo.lastMonthEq*100" style="width: 9vw;" />
              <div style="position: absolute; bottom: 10%; left: 77%; transform: translateX(-50%); color: #000; border-radius: 3px; white-space: nowrap;">
                {{EqInfo.thisMonthEq ? EqInfo.thisMonthEq.toFixed(1) : 0}}kWh
              </div>
            </span>
            <span style="margin-left:3vh;">{{EqInfo.lastMonthEq ? EqInfo.lastMonthEq.toFixed(1) : 0}}kWh</span>
          </div>
        </div>
      </el-card>
      <el-card shadow="never" style="">
        <template #header>
          <div style="font-weight: bold">环境</div>
          <el-link @click="updateChart(); toggleTable = !toggleTable" type="primary" style="margin-left:12vw;">{{toggleTable?'湿度':'温度'}}</el-link>
        </template>
        <div ref="lineidChartContainer" id="lineidChartContainer" style="width:14vw;height:32.8vh;"></div>
        <!--<div>当前平均温度：{{envInfo.temAvg}}°C</div>
        <div>当前最高温度：{{envInfo.temMax}}°C</div>
        <div>当前最低温度：{{envInfo.temMin}}°C</div>
        <div>最近更新时间：{{envInfo.updateTime}}</div>-->
      </el-card>
    </div>
    <div class="center" id="center">
      <CabTopology :containerInfo="containerInfo" :isFromHome="true" @back-data="handleBackData" @getroomid="handleGetRoomId" />
      <ContentWrap class="CabEchart">
        <div style="background-color: #ffffff;border-radius: 5px;">
          <el-row>
            <el-col :span="16">
              <el-radio-group v-model="typeRadio">
                <el-radio-button label="功率曲线" value="功率曲线" @click="switchChartContainer =0"/>
                <el-radio-button label="有功电能" value="有功电能" :disabled="isPowActiveDisabled" @click="clickPower()"/>
                <el-radio-button label="负载率" value="负载率" @click="switchChartContainer =2"/>
                <el-radio-button label="PUE" value="PUE" @click="switchChartContainer =2"/>
              </el-radio-group>
            </el-col>
            <el-col :span="8">
              <el-radio-group v-model="timeRadio">
                <el-radio-button label="近一小时" value="近一小时" :disabled="isHourDisabled" />
                <el-radio-button label="近一天" value="近一天" :disabled="isDayAndMonthDisabled" />
                <el-radio-button label="近三天" value="近三天" :disabled="isDayAndMonthDisabled" />
                <el-radio-button label="近一月" value="近一月" :disabled="isDayAndMonthDisabled"/>
              </el-radio-group>
            </el-col>
          </el-row>
          <br/>
          <div ref="chartContainer4" id="chartContainer4" style="width: 55vw;height: 340px;" v-show="switchChartContainer == 2"></div>
          <div ref="chartContainer2" id="chartContainer2" style="width: 55vw;height: 340px;" v-show="switchChartContainer == 0"></div>
          <div ref="chartContainer3" id="chartContainer3" style="width: 55vw;height: 340px;" v-show="switchChartContainer == 1"></div>
        </div>
      </ContentWrap>
    </div>
    <div class="right">
      <el-card shadow="never" v-if="roomDownVal.displayFlag">
        <template #header>
          <div style="text-align: center">{{roomDownVal.displayType ? 'PUE' : '负载率'}}</div>
        </template>
        <div style="text-align: center">{{roomDownVal.displayType ? (roomDownVal.roomPue ? roomDownVal.roomPue.toFixed(2) : '0') : (roomDownVal.roomLoadFactor ? roomDownVal.roomLoadFactor.toFixed(0)+'%' : '0%')}}</div>
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
      <el-card shadow="never">
        <template #header>
          <div class="h-3 flex justify-between">
            <span> 设备统计</span>
          </div>
        </template>
        <el-table :data="tableData" style="" border class="text-12px">
          <el-table-column prop="name" label="" width="58" height="10vh" />
          <el-table-column prop="pdu_num" label="PDU" width="70" height="10vh" />
          <el-table-column prop="box_num" label="插接箱" width="70" height="10vh" />
          <el-table-column prop="bus_num" label="始端箱" width="70" height="10vh" />
        </el-table>
      </el-card>
      <el-card shadow="never" class="mb-8px">
        <template #header>
          <div class="h-3 flex justify-between">
            <span>告警统计</span>
          </div>
        </template>
        <el-table :data="tableData" border class="text-12px" height="57vh">
          <el-table-column prop="error" label="告警内容"  />
          <el-table-column prop="box" label="告警设备" />
          <el-table-column prop="time" label="告警时间" />
        </el-table>
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
import { EChartsOption } from 'echarts'
import { formatDate} from '@/utils/formatTime';

import * as echarts from 'echarts'

const format = (percentage) => ( ``)  //用来自定义进度条的内容

const echartOptionsPower = ref<EChartsOption>({}) //用来存储功率曲线图表的配置选项
const environmentOptions = ref<EChartsOption>({}) //用来存储环境图表的配置选项
const roomId = ref<number>(0)
const radioBtn = ref('pow')
const containerInfo = reactive({
  width: 1000,
  roomId: history?.state?.roomId
}) // 机房拓扑容器信息
const deviceInfo = reactive({}) // 设备信息
const EqInfo = reactive({}) // 用能信息
const roomDownVal = reactive({}) // 机房信息
const envInfo = reactive({}) // 空间信息
const echartInfo = reactive<any>({}) //配置图表的数据系列
const toggleTable = ref(false)
const tableData = ref([
  {
    name: '总数',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'total',
    error:'温度超阀值上限',
    box:'温湿度01',
    time:'2022-11-07 12:15:46'
  },{
    name: '在线',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'online',
    error:'制冷压力超阀值上限',
    box:'精密空调',
    time:'2022-11-07 12:15:46'
  },{
    name: '告警',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'alarm',
    error:'检测到有水',
    box:'水浸',
    time:'2022-11-07 12:15:46'
  },{
    name: '离线',
    pdu_num: 0,
    box_num: 0,
    bus_num: 0,
    flag: 'offline',
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

const lineChartQueryParams = reactive({
  roomId: history?.state?.roomId as number | undefined,
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

const chartContainer2 = ref<HTMLElement | null>(null);
const chartContainer3 = ref<HTMLElement | null>(null);
const chartContainer4 = ref<HTMLElement | null>(null);
let myChart2 = null as echarts.ECharts | null; 
let myChart3 = null as echarts.ECharts | null; 
let myChart4 = null as echarts.ECharts | null; 


let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainer = ref<HTMLElement | null>(null);

const clickPower = () => {
  if(!isPowActiveDisabled) {
    switchChartContainer.value =1
  }
}

const initChart2 = () => {
    console.log("bbbbbbbbbbbbbbbb")
  if (chartContainer2.value && instance) {
    myChart2 = echarts.init(chartContainer2.value);
    myChart2.setOption(
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
              } else if (typeRadio.value === '有功电能') {
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
    myChart3 = echarts.init(chartContainer3.value);
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
                                      }else if (typeRadio.value === '有功电能') {
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
              } else if (typeRadio.value === '有功电能') {
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
    myChart4 = echarts.init(chartContainer4.value);
    myChart4.setOption(
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
              } else if (typeRadio.value === '有功电能') {
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

    const data3 = await MachineRoomApi.getLineChartData({...lineChartQueryParams,flag: 0});

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

const initData = () => {
  if(timeRadio.value == '近一天' || timeRadio.value == '近三天' || timeRadio.value == '近一月'){
    switch (typeRadio.value){
      case '有功电能':
        if(allEqData.value != null && timeRadio.value == '近一月'){
        eqValue.value = allEqData.value.map((item) => item.eq_value.toFixed(3));
        } else if (allEqData.value != null && (timeRadio.value == '近三天' || timeRadio.value == '近一天')){
        eqValue.value = allEqData.value.map((item) => item.ele_total.toFixed(3));
        }
        break;
      case '负载率':
        if(allEqData.value != null){
        L1Data.value = allLoadPueData.value.map((item) => item.load_rate_avg_value.toFixed(0));
        }
        break;
      case 'PUE':
        if(allEqData.value != null){
        L1Data.value = allLoadPueData.value.map((item) => item.room_pue_avg_value.toFixed(2));
        }
        break;
      case '功率曲线':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.map((item) => item.apparent_total_avg_value.toFixed(3));
        L2Data.value = allLineData.value.map((item) => item.active_total_avg_value.toFixed(3));
        L3Data.value = allLineData.value.map((item) => item.reactive_total_avg_value.toFixed(3));
        }
       break;
      }
  } else if(timeRadio.value == '近一小时'){
    switch (typeRadio.value){
      case '有功电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.map((item) => item.ele_total.toFixed(3));
        }
        break;
      case '负载率':
        if(allEqData.value != null){
        L1Data.value = allLoadPueData.value.map((item) => item.load_rate.toFixed(0));
        }
        break;
      case 'PUE':
        if(allEqData.value != null){
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

// 监听切换类型
watch( ()=>typeRadio.value, async(value)=>{
  //L1Data.value = [];
  //L2Data.value = [];
  //L3Data.value = [];
  await initData();
  if ( value == '有功电能'){
     // 选有功电能不能选近一小时
     isHourDisabled.value = true
  }else{
    isHourDisabled.value = false
  }
  // 更新数据后重新渲染图表
  myChart2?.setOption({
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
            }else if (typeRadio.value === '有功电能') {
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
              } else if (typeRadio.value === '有功电能') {
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
                                    }else if (typeRadio.value === '有功电能') {
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
            } else if (typeRadio.value === '有功电能') {
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
                                    }else if (typeRadio.value === '有功电能') {
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
              } else if (typeRadio.value === '有功电能') {
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
    // 选近一小时不能选有功电能
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
  await getLineChartData();

});

//获取数据
const handleGetRoomId = (data) => {
  roomId.value = data
  lineChartQueryParams.roomId = data
  // getRoomDataDetail()
  // getRoomDevData()
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
  const res = await MachineRoomApi.getRoomDevData({id: roomId.value})
  Object.assign(deviceInfo, res)
  console.log('***获取机房主页面设备数据', res)
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

  tableData.value.forEach((item,index) => {
    tableData.value[index].pdu_num = data.map?.pdu ? data.map.pdu[tableData.value[index].flag] : 0
    tableData.value[index].box_num = data.map?.box ? data.map.box[tableData.value[index].flag] : 0
    tableData.value[index].bus_num = data.map?.bus ? data.map.bus[tableData.value[index].flag] : 0
  })
  initChart()
  getRoomEqData()
}

const initChart = () => {
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
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
          data: [roomDownVal.temAvgFront ? roomDownVal.temAvgFront : 0,roomDownVal.temAvgBlack ? roomDownVal.temAvgBlack : 0],
        },
        {
          name: '最高温度',
          type: 'bar',
          data: [roomDownVal.temMaxFront ? roomDownVal.temMaxFront : 0,roomDownVal.temMaxBlack ? roomDownVal.temMaxBlack : 0],
        }
      ]
  })
}

const updateChart = () => {
  lineidChart = echarts.init(document.getElementById('lineidChartContainer'));
  if(toggleTable.value === true){
    lineidChart.setOption({
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
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
          data: [roomDownVal.temAvgFront ? roomDownVal.temAvgFront : 0,roomDownVal.temAvgBlack ? roomDownVal.temAvgBlack : 0],
        },
        {
          name: '最高温度',
          type: 'bar',
          data: [roomDownVal.temMaxFront ? roomDownVal.temMaxFront : 0,roomDownVal.temMaxBlack ? roomDownVal.temMaxBlack : 0],
        }
      ]
  })
  }else if(toggleTable.value === false){
    lineidChart.setOption( {
      title: { text: ''},
      tooltip: { trigger: 'axis',      formatter: function (params) {
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
          data: [roomDownVal.humAvgFront ? roomDownVal.humAvgFront : 0,roomDownVal.humAvgBlack ? roomDownVal.humAvgBlack : 0],
        },
        {
          name: '最高湿度',
          type: 'bar',
          data: [roomDownVal.humMaxFront ? roomDownVal.humMaxFront : 0,roomDownVal.humMaxBlack ? roomDownVal.humMaxBlack : 0],
        }
      ]
  })
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

onMounted(() => {
  const centerEle = document.getElementById('center')
  containerInfo.width = centerEle?.offsetWidth as number
  console.log('centerEle', containerInfo.width, centerEle?.offsetWidth, centerEle?.offsetHeight)
})
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
    padding-bottom: 15px;
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
  .percentage-value {
    display: block;
    margin-top: 10px;
    font-size: 50px;
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

.responsive-table {
  table-layout: fixed;  /* 确保列宽按照设置的宽度显示 */
  word-wrap: break-word; /* 文本过长时换行 */
}
</style>
