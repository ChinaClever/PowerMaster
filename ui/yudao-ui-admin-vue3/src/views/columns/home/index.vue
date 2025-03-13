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
            <span class="line"></span>
            <el-form-item label="" prop="jg">
              柜列：{{roomDownVal?.aisleName}}
            </el-form-item>
          </el-form>
        </div>
      </ContentWrap>
      <ContentWrap>
        <div class="progress">
          <!--<el-progress type="dashboard" :percentage="powerInfo.powApparent && powerInfo.powApparent.toFixed()">-->
          <el-progress type="dashboard" :percentage="mainInfo.powActiveTotal/mainInfo.powApparentTotal*100">
              <span class="percentage-value">{{ mainInfo.powApparentTotal}}</span>
              <span class="percentage-label">总视在功率</span>
              <span class="percentage-unit">kVA</span>
          </el-progress>
          <div class="powActiveTotal-text">
           <span>{{ mainInfo.powActiveTotal}}kW</span>
          </div>
        </div>
        <div style="display:flex;justify-content: space-around;padding: 5px 0;">
          <div>A路</div>
          <div>B路</div>
        </div>
        <div style="display:flex;height:10vh;justify-content: center;align-item:center;margin-bottom: -40px">
          <Environment class="chart" width="100%" height="100%" :load-factor="{first: mainInfo.powReactiveA,second: mainInfo.powActiveA,third: mainInfo.powApparentA,label: ['Q','P','S'],unit: ['KVAR', 'KW', 'KVA']}" style="margin-right:-15px;"/> <!-- TODO 换成A路电流 -->
          <EnvironmentCopy class="chart" width="100%" height="100%" :load-factor="{first: mainInfo.powReactiveB,second: mainInfo.powActiveB,third: mainInfo.powApparentB,label: ['Q','P','S'],unit: ['KVAR', 'KW', 'KVA']}"/>
        </div>
        <!-- <div style="display:flex;justify-content: space-around">
          <div>A路</div>
          <div>B路</div>
        </div>
        
        <div style="margin-bottom: -40px">
          <div style="display: inline-block;
            width: 50%;
            margin-right:-10px;"
          >
            <Environment class="chart" width="100%" height="100%" :load-factor="ceshi"/>
          </div>
          <div style="display: inline-block;
              width: 50%;
              height: 20%;">
            <EnvironmentCopy class="chart" width="100%" height="100%" :load-factor="ceshi"/>
          </div>
        </div> -->
      </ContentWrap>
      <el-card shadow="never" style="margin-bottom: 15px;font-size: 12px">
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
      </el-card>
      <!--<el-card v-if="pduBar" shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>A路</div>
        </template>
        <div>电流：
          <template v-if="mainInfo.barLineCurA">
            <span v-for="(Eq, i) in mainInfo.barLineCurA" :key="i">I{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
        <div>电压：
          <template v-if="mainInfo.barLineVolA">
            <span v-for="(Eq, i) in mainInfo.barLineVolA" :key="i">U{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
      </el-card>-->
      <ContentWrap v-if="mainInfo">
        <el-link @click="toggleTable = !toggleTable" type="primary" style="margin-left:12vw;">切换</el-link>
        <Echart :options="toggleTable?echartsOptionC:echartsOptionV" height="28vh"/>
      </ContentWrap>
    </div>
    <div class="center" id="center">
      <Topology :containerInfo="containerInfo" :isFromHome="true" @back-data="handleCabEchart" @id-change="handleIdChange" @getpdubar="handlePduBar" @send-list="sendRoomIdValList">
        <template #btn>
          <el-button class="ml-12px" @click="handleJump" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
        </template>
      </Topology>
      <ContentWrap class="CabEchart">
        <div style="background-color: #ffffff;border-radius: 5px;">
          <el-row>
            <el-col :span="18">
              <el-radio-group v-model="typeRadio">
                <el-radio-button label="电流" value="电流" @click="switchChartContainer =0"/>
                <el-radio-button label="电压" value="电压" @click="switchChartContainer =0"/>
                <el-radio-button label="有功电能" value="有功电能" :disabled="isPowActiveDisabled" @click="switchChartContainer =1"/>
                <el-radio-button label="有功功率" value="有功功率" @click="switchChartContainer =0"/>
                <el-radio-button label="无功功率" value="无功功率" @click="switchChartContainer =0"/>
                <el-radio-button label="视在功率" value="视在功率" @click="switchChartContainer =0"/>
                <el-radio-button label="功率因素" value="功率因素" @click="switchChartContainer =0"/>
              </el-radio-group>
            </el-col>
            <el-col :span="6">
              <el-radio-group v-model="timeRadio">
                <el-radio-button label="近一小时" value="近一小时" :disabled="isHourDisabled" />
                <el-radio-button label="近一天" value="近一天" :disabled="isDayAndMonthDisabled" />
                <el-radio-button label="近三天" value="近三天" :disabled="isDayAndMonthDisabled" />
                <el-radio-button label="近一月" value="近一月" :disabled="isDayAndMonthDisabled"/>
              </el-radio-group>
            </el-col>
          </el-row>
          <br/>
          <div ref="chartContainer2" id="chartContainer2" style="width: 70vw;height: 340px;" v-show="switchChartContainer == 0"></div>
          <div ref="chartContainer3" id="chartContainer3" style="width: 70vw;height: 340px;" v-show="switchChartContainer == 1"></div>
        </div>
      </ContentWrap>
    </div>
    <!--<div class="right">
      <el-card shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>历史用能</div>
        </template>
        <div>昨日：{{EqInfo.yesterdayEq && EqInfo.yesterdayEq.toFixed(2)}}kW·h</div>
        <div>上周：{{EqInfo.lastWeekEq && EqInfo.lastWeekEq.toFixed(2)}}kW·h</div>
        <div>上月：{{EqInfo.lastMonthEq && EqInfo.lastMonthEq.toFixed(2)}}kW·h</div>
      </el-card>
      <el-card v-if="pduBar" shadow="never" style="margin-bottom: 15px">
        <template #header>
          <div>B路</div>
        </template>
        <div>电流：
          <template v-if="mainInfo.barLineCurB">
            <span v-for="(Eq, i) in mainInfo.barLineCurB" :key="i">I{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
        <div>电压：
          <template v-if="mainInfo.barLineVolB">
            <span v-for="(Eq, i) in mainInfo.barLineVolB" :key="i">U{{String.fromCharCode(97 + i)}}: {{Eq}}、</span>
          </template>
          <span v-else>无</span>
        </div>
      </el-card>
      <ContentWrap>
        <div class="progress">
          <el-progress type="dashboard" :percentage="mainInfo.powActive && mainInfo.powActive.toFixed()">
            <template #default="{ percentage }">
              <span class="percentage-value">{{ percentage }}</span>
              <span class="percentage-label">总有功功率</span>
            </template>
          </el-progress>
        </div>
      </ContentWrap>
      <ContentWrap v-if="mainInfo.cabinetList && mainInfo.cabinetList.length">
        <Echart :options="echartsOptionB" :height="300" />
      </ContentWrap>
    </div>-->
  </div>
</template>

<script lang="ts" setup>
import Topology from '../topology/index.vue'
import * as echarts from 'echarts';
import { EChartsOption } from 'echarts'
import { MachineColumnApi } from '@/api/cabinet/column'
import { IndexApi } from '@/api/aisle/aisleindex'
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail';
import Environment from './component/Environment.vue'
import EnvironmentCopy from './component/EnvironmentCopy.vue'
import { formatDate} from '@/utils/formatTime';


const format = (percentage) => ( ``)  //用来自定义进度条的内容
const toggleTable = ref(true)

const {push} = useRouter()
const containerInfo = reactive({
  width: 0,
  cabinetColumnId: history?.state?.id,
  cabinetroomId: history?.state?.roomId,
})
console.log('containerInfo', containerInfo)
const scaleVal = ref(1)
const echartsOptionCab = ref<EChartsOption>({})
const pduBar = ref(1) // 0:pdu 1:母线

const instance = getCurrentInstance();
const typeRadio = ref('电流');
const timeRadio = ref('近一小时');
const isHourDisabled = ref(false);
const isDayAndMonthDisabled = ref(false);
const isPowActiveDisabled = ref(true);
const isLoadRateDisabled = ref(false);
const switchChartContainer = ref(0);

const lineChartQueryParams = reactive({
  id: 6331,
  granularity: 'realtime',
})

const echartsOptionA = reactive<EChartsOption>({})
const echartsOptionB = reactive<EChartsOption>({})
const echartsOptionC = reactive<EChartsOption>({})
const echartsOptionV = reactive<EChartsOption>({})

const mainInfo = reactive({})
const EqInfo = reactive({})

const roomDownVal = ref()

const ceshi = ref({
    "id": 1,
    "roomId": 1,
    "roomName": "王雁杰机房",
    "name": "12",
    "pduBar": true,
    "isDelete": null,
    "createTime": null,
    "length": null,
    "devKeyA": "172.16.101.2-1",
    "devKeyB": "172.16.101.2-2",
    "type": null,
    "location": "王雁杰机房-12",
    "status": 0,
    "eleActiveTotal": 57236.734375,
    "powApparentTotal": 426.88,
    "powActiveTotal": 377,
    "powReactiveTotal": 146.742,
    "eleActiveA": 28624.53741610683,
    "powApparentA": 236,
    "powActiveA": 195,
    "powReactiveA": 98,
    "eleActiveB": 28612.196358193876,
    "powApparentB": 190,
    "powActiveB": 182,
    "powReactiveB": 48
})

const createTimeData = ref<string[]>([]);
const allLineData = ref<string[]>([]);
const allEqData = ref<string[]>([]);
const eqValue = ref<number[]>([]);
const eqCreateTimeData = ref<string[]>([]);
const L1Data = ref<number[]>([]);
const L2Data = ref<number[]>([]);
const L3Data = ref<number[]>([]);

const chartContainer2 = ref<HTMLElement | null>(null);
const chartContainer3 = ref<HTMLElement | null>(null);
let myChart2 = null as echarts.ECharts | null; 
let myChart3 = null as echarts.ECharts | null; 
const initChart2 = () => {
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
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value },
          {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
          {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
        ],
      }
    );
    instance.appContext.config.globalProperties.myChart2 = myChart2;

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
          left: '6%',   // 设置左侧边距
          right: '6%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
        series: [
          {name: '电量', type: 'line', symbol: 'none', data: eqValue.value},
        ],
      }
    );

    instance.appContext.config.globalProperties.myChart3 = myChart3;
  }
}

const isHaveData = ref(true)
// 获取折线图数据
const getLineChartData =async () => {
 try {
    allLineData.value = null
    allEqData.value = null
    
    const data = await BusPowerLoadDetailApi.getLineChartData(lineChartQueryParams);
    console.log('获取折线图数据',data)
    console.log('lineChartQueryParams',lineChartQueryParams)
    if (data != null){
      // 查到数据
      allLineData.value = data
      console.log('allLineData',allLineData.value)
      if (timeRadio.value == '近一小时'){
        createTimeData.value = data.L1.map((item) => formatDate(item.create_time,'YYYY-MM-DD HH:mm'));
      }else if (timeRadio.value == '近一天' || '近三天'){
        createTimeData.value = data.L1.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm'));
      } else{
        createTimeData.value = data.L1.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }
      //L1Data.value = [];
      //L2Data.value = [];
      //L3Data.value = [];
      await initData();
      isHaveData.value = true
    }else{
    }

    const data2 = await BusPowerLoadDetailApi.getBusEqChartData(lineChartQueryParams);
    console.log("aaaaaaaaaasdad")
    if (data2 != null){
      // 查到数据
      allEqData.value = data2
      console.log('allEqData',allEqData.value)
       if (timeRadio.value == '近一天'|| '近三天'){
        eqCreateTimeData.value = data2.L1.map((item) => formatDate(item.create_time, 'YYYY-MM-DD HH:mm'));
      } else{
        eqCreateTimeData.value = data2.L1.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }
      //L1Data.value = [];
      //L2Data.value = [];
      //L3Data.value = [];
      await initData();
      isHaveData.value = true
    }else{
    }
 } finally {
 }
}

const initData = () => {
  if(timeRadio.value == '近一小时'){
    switch (typeRadio.value){
      case '电流':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.cur_value.toFixed(2));
        L2Data.value = allLineData.value.L2.map((item) => item.cur_value.toFixed(2));
        L3Data.value = allLineData.value.L3.map((item) => item.cur_value.toFixed(2));
        }
        break;
      case '电压':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.vol_value.toFixed(1));
        L2Data.value = allLineData.value.L2.map((item) => item.vol_value.toFixed(1));
        L3Data.value = allLineData.value.L3.map((item) => item.vol_value.toFixed(1));
        }
        break;
      case '有功电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.L1.map((item) => item.eq_value.toFixed(3));
        }
        break;
      case '有功功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_active.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_active.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_active.toFixed(3));
        }
        break;              
      case '无功功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_reactive.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_reactive.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_reactive.toFixed(3));
        }
       break;
      case '视在功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_apparent.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_apparent.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_apparent.toFixed(3));
        }
       break;
      case '功率因素':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.power_factor.toFixed(2));
        L2Data.value = allLineData.value.L2.map((item) => item.power_factor.toFixed(2));
        L3Data.value = allLineData.value.L3.map((item) => item.power_factor.toFixed(2));
        }
        break;
      case '线电压':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.vol_line.toFixed(1));
        L2Data.value = allLineData.value.L2.map((item) => item.vol_line.toFixed(1));
        L3Data.value = allLineData.value.L3.map((item) => item.vol_line.toFixed(1));
        }
        break;
      case '负载率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.load_rate);
        L2Data.value = allLineData.value.L2.map((item) => item.load_rate);
        L3Data.value = allLineData.value.L3.map((item) => item.load_rate);
        }
        break;             
    }
  }else if(timeRadio.value == '近一天' || timeRadio.value == '近三天'){
    switch (typeRadio.value){
      case '电流':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.cur_avg_value.toFixed(2));
        L2Data.value = allLineData.value.L2.map((item) => item.cur_avg_value.toFixed(2));
        L3Data.value = allLineData.value.L3.map((item) => item.cur_avg_value.toFixed(2));
        }
        break;
      case '电压':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.vol_avg_value.toFixed(1));
        L2Data.value = allLineData.value.L2.map((item) => item.vol_avg_value.toFixed(1));
        L3Data.value = allLineData.value.L3.map((item) => item.vol_avg_value.toFixed(1));
        }
        break;
      case '有功电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.L1.map((item) => item.ele_active.toFixed(3));
        }
        break;
      case '有功功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_active_avg_value.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_active_avg_value.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_active_avg_value.toFixed(3));
        }
        break;              
      case '无功功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_reactive_avg_value.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_reactive_avg_value.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_reactive_avg_value.toFixed(3));
        }
       break;
      case '视在功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_apparent_avg_value.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_apparent_avg_value.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_apparent_avg_value.toFixed(3));
        }
       break;
      case '功率因素':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.power_factor_avg_value.toFixed(2));
        L2Data.value = allLineData.value.L2.map((item) => item.power_factor_avg_value.toFixed(2));
        L3Data.value = allLineData.value.L3.map((item) => item.power_factor_avg_value.toFixed(2));
        }
        break;
      case '线电压':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.vol_line_avg_value.toFixed(1));
        L2Data.value = allLineData.value.L2.map((item) => item.vol_line_avg_value.toFixed(1));
        L3Data.value = allLineData.value.L3.map((item) => item.vol_line_avg_value.toFixed(1));
        }
        break;
      case '负载率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.load_rate);
        L2Data.value = allLineData.value.L2.map((item) => item.load_rate);
        L3Data.value = allLineData.value.L3.map((item) => item.load_rate);
        }
        break;    
      }
  }else{
    switch (typeRadio.value){
      case '电流':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.cur_avg_value.toFixed(2));
        L2Data.value = allLineData.value.L2.map((item) => item.cur_avg_value.toFixed(2));
        L3Data.value = allLineData.value.L3.map((item) => item.cur_avg_value.toFixed(2));
        }
        break;
      case '电压':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.vol_avg_value.toFixed(1));
        L2Data.value = allLineData.value.L2.map((item) => item.vol_avg_value.toFixed(1));
        L3Data.value = allLineData.value.L3.map((item) => item.vol_avg_value.toFixed(1));
        }
        break;
      case '有功电能':
        if(allEqData.value != null){
        console.log("aaaaaaa",allEqData.value)
        eqValue.value = allEqData.value.L1.map((item) => item.eq_value.toFixed(3));
        }
        break;
      case '有功功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_active_avg_value.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_active_avg_value.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_active_avg_value.toFixed(3));
        }
        break;              
      case '无功功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_reactive_avg_value.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_reactive_avg_value.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_reactive_avg_value.toFixed(3));
        }
       break;
      case '视在功率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.pow_apparent_avg_value.toFixed(3));
        L2Data.value = allLineData.value.L2.map((item) => item.pow_apparent_avg_value.toFixed(3));
        L3Data.value = allLineData.value.L3.map((item) => item.pow_apparent_avg_value.toFixed(3));
        }
       break;
      case '功率因素':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.power_factor_avg_value.toFixed(2));
        L2Data.value = allLineData.value.L2.map((item) => item.power_factor_avg_value.toFixed(2));
        L3Data.value = allLineData.value.L3.map((item) => item.power_factor_avg_value.toFixed(2));
        }
        break;
      case '线电压':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.vol_line_avg_value.toFixed(1));
        L2Data.value = allLineData.value.L2.map((item) => item.vol_line_avg_value.toFixed(1));
        L3Data.value = allLineData.value.L3.map((item) => item.vol_line_avg_value.toFixed(1));
        }
        break;
      case '负载率':
        if(allLineData.value != null){
        L1Data.value = allLineData.value.L1.map((item) => item.load_rate);
        L2Data.value = allLineData.value.L2.map((item) => item.load_rate);
        L3Data.value = allLineData.value.L3.map((item) => item.load_rate);
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
  if (value == '负载率' ){
    isDayAndMonthDisabled.value = true
  }else{
    isDayAndMonthDisabled.value = false
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
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
      series: [
        {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value},
        {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
        {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
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
          left: '6%',   // 设置左侧边距
          right: '6%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
      series: [
        {name: '电量', type: 'line', symbol: 'none', data: eqValue.value},
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
  // 更新数据后重新渲染图表
  if (isHaveData.value == true){
    myChart2?.setOption({
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
          left: '5%',   // 设置左侧边距
          right: '5%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
    series: [
      {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value},
      {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
      {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
    ],
  }, true);
  }
  // 更新数据后重新渲染图表
  if (isHaveData.value == true){
    console.log(eqValue.value)
    myChart3?.setOption({
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
          left: '6%',   // 设置左侧边距
          right: '6%',  // 设置右侧边距
          top: '10%',    // 设置上侧边距
          bottom: '10%', // 设置下侧边距
        },
    series: [
      {name: '电量', type: 'line', symbol: 'none', data: eqValue.value},
    ],
  }, true);
  }

});
const getMainData = async() => {
  Object.assign(mainInfo, roomDownVal.value)
  console.log(mainInfo)

  mainInfo.powActiveA = mainInfo.powActiveA.toFixed(0)
  mainInfo.powActiveB = mainInfo.powActiveB.toFixed(0)
  mainInfo.powReactiveA = mainInfo.powReactiveA.toFixed(0)
  mainInfo.powReactiveB = mainInfo.powReactiveB.toFixed(0)
  mainInfo.powApparentA = mainInfo.powApparentA.toFixed(0)
  mainInfo.powApparentB = mainInfo.powApparentB.toFixed(0)
  mainInfo.powApparentTotal = mainInfo.powApparentTotal.toFixed(0)
  mainInfo.powActiveTotal = mainInfo.powActiveTotal.toFixed(0)

  mainInfo.curAList.forEach((item,index) => {
    mainInfo.curAList[index] = mainInfo.curAList[index].toFixed(2)
    mainInfo.curBList[index] = mainInfo.curBList[index].toFixed(2)
    mainInfo.volAList[index] = mainInfo.volAList[index].toFixed(1)
    mainInfo.volBList[index] = mainInfo.volBList[index].toFixed(1)
  })

  console.log(mainInfo)


  Object.assign(echartsOptionC, {
    title: {
      text: '电流'
    },
    legend: {
      data: ['A', 'B'] // 换接口的值
    },
    grid: {left: '3%', right: '3%', bottom: '3%',containLabel: true},
    xAxis: {
      type: 'category',
      data: ['Ia', 'Ib', 'Ic'],
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'A',//legend里的data数据分别渲染上去
        type: 'bar',
        barGap: 0,
        label: { // 在柱状图上面显示
          show: true,
          position: 'top'
        },
        emphasis: { // 这个属性是强调，突出的
          focus: 'series'
        },
        data: mainInfo.curAList
      },
      {
        name: 'B',//legend里的data数据分别渲染上去
        type: 'bar',
        barGap: 0,
        label: { // 在柱状图上面显示
          show: true,
          position: 'top'
        },
        emphasis: { // 这个属性是强调，突出的
          focus: 'series'
        },
        data: mainInfo.curBList
      }
    ]
  })
  Object.assign(echartsOptionV, {
    title: {
      text: '电压'
    },
    legend: {
      data: ['A', 'B'] // 换接口的值
    },
    grid: {left: '3%', right: '3%', bottom: '3%',containLabel: true},
    xAxis: {
      type: 'category',
      data: ['Ua', 'Ub', 'Uc'],
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: 'A',//legend里的data数据分别渲染上去
        type: 'bar',
        barGap: 0,
        label: { // 在柱状图上面显示
          show: true,
          position: 'top'
        },
        emphasis: { // 这个属性是强调，突出的
          focus: 'series'
        },
        data: mainInfo.volAList
      },
      {
        name: 'B',//legend里的data数据分别渲染上去
        type: 'bar',
        barGap: 0,
        label: { // 在柱状图上面显示
          show: true,
          position: 'top'
        },
        emphasis: { // 这个属性是强调，突出的
          focus: 'series'
        },
        data: mainInfo.volBList
      }
    ]
  })
  
//Object.assign(echartsOptionA, {
//  title: {
//    text: '实时功率'
//  },
//  legend: {
//    data: ['视在功率', '有功功率'],
//    top: 0,
//    right: 0,
//  },
//  grid: {
//    bottom: 0,
//    top: 0,
//  },
//  radar: {
//    indicator: res.cabinetList.map((item, index) => {
//      return {
//        name: '机柜' + (index+1),
//        max: 10
//      }
//    })
//  },
//  series: [
//    {
//      name: 'power',
//      type: 'radar',
//      data: [
//        {
//          value: res.cabinetList.map(item => item.powApparentA),
//          name: '视在功率'
//        },
//        {
//          value: res.cabinetList.map(item => item.powActiveA),
//          name: '有功功率',
//          areaStyle: {
//            color: 'rgba(206, 255, 171, 0.6)'
//          }
//        }
//      ]
//    }
//  ]
//})
//
//Object.assign(echartsOptionB, {
//  title: {
//    text: 'B路功率'
//  },
//  legend: {
//    data: ['视在功率', '有功功率'],
//    top: 0,
//    right: 0,
//  },
//  grid: {
//    bottom: 0,
//    top: 0,
//  },
//  radar: {
//    indicator: res.cabinetList.map((item, index) => {
//      return {
//        name: '机柜' + (index+1),
//        max: 10
//      }
//    })
//  },
//  series: [
//    {
//      name: 'power',
//      type: 'radar',
//      data: [
//        {
//          value: res.cabinetList.map(item => item.powApparentB),
//          name: '视在功率'
//        },
//        {
//          value: res.cabinetList.map(item => item.powActiveB),
//          name: '有功功率',
//          areaStyle: {
//            color: 'rgba(206, 255, 171, 0.6)'
//          }
//        }
//      ]
//    }
//  ]
//})
}

const getMainEq = async() => {
  console.log(containerInfo)
  const res =  await IndexApi.getAisleEleChain({id: containerInfo.cabinetColumnId})
  const res2 =  await MachineColumnApi.getMainEq({id: containerInfo.cabinetColumnId})
  console.log('getMainEq res', res)
  Object.assign(EqInfo, res)
}

const sendRoomIdValList = async (result) =>{
   roomDownVal.value = result;
   containerInfo.cabinetColumnId = roomDownVal.value.id
   getMainData()
   getMainEq()
   await getLineChartData()
   initChart2()
   initChart3()
}


// 处理跳转
const handleJump = () => {
   push({path: '/aisle/topology', state: { id: containerInfo.cabinetColumnId, roomId: containerInfo.cabinetroomId,roomValId:roomDownVal.value.roomId }})
}
// 处理时pdu还是母线的事件
const handlePduBar = (type) => {
  pduBar.value = type
}
// 处理柜列id/机柜id切换事件
const handleIdChange = (id) => {
  containerInfo.cabinetColumnId = id
}
// 处理柜列实时统计图表
const handleCabEchart = (result, scale) => {
  console.log('handleCabEchart', result, typeof result)
  scaleVal.value = scale
  echartsOptionCab.value = {
    title: {
      text: '机柜列实时统计'
    },
    grid: {
      left: '0',
      right: '25',
      bottom: '5',
      top: '50',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: result.map(item => item.cabinetName || ''),
      axisLabel: {
        interval: 0 // 设置为 0 表示所有标签都显示
      }
    },
    legend:{
      top: '0',
      right: '50',
      selectedMode: 'single'
    },
    yAxis: {
      type: 'value',
      // show: false,
    },
    series: [
      {
        name:'有功功率',
        data: result.map(item => item.powActive == undefined ? null : item.powActive),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
      {
        name:'昨日用能',
        data: result.map(item => item.yesterdayEq == undefined ? null : item.yesterdayEq),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  }
}

// getMainData()
// getMainEq()

onMounted(() => {
  const centerEle = document.getElementById('center')
  containerInfo.width = centerEle?.offsetWidth as number
  // getMainData()
  // getMainEq()
  // await getLineChartData()
  // initChart2()
  // initChart3()
  console.log('centerEle', containerInfo.width, centerEle?.offsetWidth, centerEle?.offsetHeight)
})

</script>

<style lang="scss" scoped>
.homeContainer {
  box-sizing: border-box;
  width: 100%;
  // height: calc(100vh - 120px);
  min-height: 550px;
  box-sizing: border-box;
  // background-color: #999;
  display: flex;
  .center {
    flex: 1;
    box-sizing: border-box;
    overflow: hidden;
    margin: 0 15px;
    padding-bottom: 15px;
    position: relative;
    display: flex;
    flex-direction: column;
    .mask {
      width: 100%;
      height: calc(100% - 50px);
      position: absolute;
      bottom: 0;
      z-index: 99;
    }
    .CabEchart {
      flex: 1;
      margin-bottom: 0;
      z-index: 999;
    }
  }
  .left {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
  }
  .right {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
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
    font-size: 12px;
  }
  .percentage-unit {
    display: block;
    font-size: 10px;
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

// :deep(.progress .el-progress__text::after ){
//   content: '60kW'; /* 要显示的文本 */
//   position: absolute;
//   top: 0; /* 放置在原元素的 */
//   left: 100%;
//   color: #000;
//   padding: 5px;
//   border-radius: 3px;
//   white-space: nowrap;
//   transform: translateX(5px); /* 稍微向右边移动一点以避免与进度条重叠 */
//   opacity: 0; /* 默认隐藏 */
//   transition: opacity 0.3s; /* 添加过渡效果 */
//   pointer-events: none; /* 确保提示框不会干扰鼠标事件 */
// }
 
:deep(.progress:hover .powActiveTotal-text) {
  opacity: 1; /* 鼠标悬停时显示 */
}

:deep(.el-card__header) {
  padding: 15px;
}
:deep(.el-card__body) {
  padding: 15px;
}
:deep(.CabEchart .el-card__body) {
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  & > div {
    box-sizing: border-box;
    height: 100%;
    width: 100%;
  }
}

</style>