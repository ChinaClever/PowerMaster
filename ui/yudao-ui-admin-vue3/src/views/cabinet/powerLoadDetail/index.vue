<template>
<div style="background-color: #E7E7E7;">
  <div class="header_app">
    <div class="header_app_text">所在位置：{{ roomName }}-{{cabinetName}}
    </div>
    <div class="header_app_text_other1">
          <el-col :span="10">
            <el-form
              class="-mb-15px"
              :model="queryParamsSearch"
              ref="queryFormRef"
              :inline="true"
              label-width="120px"
            >
              <el-form-item label="网络地址" prop="devKey" >
              <el-autocomplete
                v-model="queryParamsSearch.devKey"
                :fetch-suggestions="querySearch"
                placeholder="请输入网络地址"  
                clearable
                class="!w-160px"
                @select="handleQuery" 
              />
              </el-form-item>
            </el-form>
          </el-col>      
    </div>
    <div class="header_app_text_other">
      <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
    </div>
  </div>
  <div style="margin:10px;">
  <el-row v-show="hasData">
    <el-col :span="12" style="background-color: #E7E7E7;padding: 0px;padding-right: 10px;">
      <div  style="background-color: #FFFFFF;width: 100%;height: 100%;border-radius: 5px">
      <div style="color:#606266;padding-left:10px;padding-top:10px">| 电力负荷</div>
      <br/>    <br/>  
      <el-row :gutter="25">
        <el-col :span="7" :offset="1">
          <el-card style="background-color: dodgerblue;">
            <!-- <img style="float: left;" width="50px" height="30px" src="@/assets/imgs/PDU.jpg" alt=""/> -->
             <span style="padding-left: 2vw; color: white; font-size: 18px;">{{ratedCapacity}} </span><span style="font-size: 10px;color: white;">kVA</span><br/>
             <span style="padding-left: 3vw;color: white; font-size: 14px;" >额定容量</span>
          </el-card>
        </el-col> 
        <el-col :span="7">
          <el-card style="background-color:crimson;">
            <!-- <img style="float: left;" width="50px" height="30px" src="@/assets/imgs/PDU.jpg" alt=""/> -->
             <span style="padding-left: 2vw; color: white; font-size: 18px;">{{runLoad}} </span><span style="font-size: 10px;color: white;">kVA</span><br/>
             <span style="padding-left: 3vw;color: white; font-size: 14px;" >运行负荷</span>
          </el-card>
        </el-col>
        <el-col :span="7">
          <el-card style="background-color:darkorchid;">
            <!-- <img style="float: left;" width="50px" height="30px" src="@/assets/imgs/PDU.jpg" alt=""/> -->
             <span style="padding-left: 2vw; color: white; font-size: 18px;">{{reserveMargin}} </span><span style="font-size: 10px;color: white;">kVA</span><br/>
             <span style="padding-left: 3vw;color: white; font-size: 14px;" >负荷余量</span>
          </el-card>
        </el-col>
        <div ref="chartContainer" id="chartContainer" style="width: 75vw; height: 20vh;margin-top: 20px; "></div>
      </el-row>
      </div>
    </el-col>
    <el-col :span="12"  style="background-color: #E7E7E7;padding: 0px;">
      <div  style="background-color: #FFFFFF;width: 100%;height: 100%;border-radius: 5px">
      <div style="color:#606266;padding-left:10px;padding-top:10px">| 功率</div>
      <br/>      <br/>
      <el-row>
        <el-col :span="10">
          <el-row style="padding-left: 10px;">
            <el-card style="width: 300px; height: 80px;">
              <span style="font-size: 18px;">{{powActive}}</span>              
              <span style="font-size: 14px; float: right; padding-top: 30px;">有功功率</span>
              <p style="font-size: 10px; padding-left: 10px;">kW</p>
            </el-card>
          </el-row>
          <el-row style="padding-left: 10px;margin-top: 10px;">
            <el-card style="width: 300px; height: 80px;">
              <span style="font-size: 18px;">{{powReactive}}</span>              
              <span style="font-size: 14px; float: right; padding-top: 30px;">无功功率</span>
              <p style="font-size: 10px; padding-left: 10px;">kVar</p>
            </el-card>
          </el-row>
          <el-row style="padding-left: 10px;margin-top: 10px;">
            <el-card style="width: 300px; height: 80px;">
              <span style="font-size: 18px;">{{peakDemand}}</span>              
              <span style="font-size: 14px; float: right; padding-top: 30px;">最大需量</span>
              <p style="font-size: 10px; padding-left: 10px;">kVA</p>
            </el-card>
          </el-row>
        </el-col>
        <el-col :span="14">
          <div ref="chartContainer1" id="chartContainer1" style="width: 460px; height: 350px; margin-top: -80px;"></div>
        </el-col>
      </el-row>
      </div>
    </el-col>
  </el-row>
  <el-row v-show="!hasData" >
    <el-col>设备离线或者输入的地址不正确</el-col>
  </el-row>
  </div>
  <div style="margin:10px;background-color: #ffffff;padding: 10px;border-radius: 5px" v-show="hasData">
    <el-row  v-show="hasData">
      <el-col :span="19">
      <el-radio-group v-model="typeRadio">
        <el-radio-button v-if="pduBox == false" label="电流" value="电流" @click="switchChartContainer = 0;"/>
        <el-radio-button v-if="pduBox == false" label="电压" value="电压" @click="switchChartContainer = 2;"/>
        <!--<el-radio-button label="有效电能" value="有效电能" :disabled="isPowActiveDisabled" @click="switchChartContainer = 1;"/>-->
        <el-radio-button label="有功功率" value="有功功率" @click="switchChartContainer = 3;"/>
        <el-radio-button label="无功功率" value="无功功率" @click="switchChartContainer = 4;"/>
        <el-radio-button label="视在功率" value="视在功率" @click="switchChartContainer = 5;"/>
        <el-radio-button label="功率因素" value="功率因素" @click="switchChartContainer = 6;"/>
        <el-radio-button label="负载率" value="负载率" @click="switchChartContainer = 7;"/>
      </el-radio-group> 
    </el-col>
    <el-col :span="5">
      <el-radio-group v-model="timeRadio">
        <el-radio-button label="近一小时" value="近一小时" :disabled="isHourDisabled" />
        <el-radio-button label="近一天" value="近一天" :disabled="isDayAndMonthDisabled" />
        <el-radio-button label="近三天" value="近三天" :disabled="isDayAndMonthDisabled" />
        <el-radio-button label="近一月" value="近一月" :disabled="isDayAndMonthDisabled"/>
      </el-radio-group>
    </el-col>
    </el-row>
  <br/>
  <div v-if="switchChartContainer === 0 && pduBox == false">
    <CurChart v-if="visContro.curVis" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  <div v-else-if="switchChartContainer === 2 && pduBox == false">
    <VolChart v-if="visContro.volVis" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  <div v-else-if="switchChartContainer === 3">
    <ActivePower v-if="visContro.activeVis" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  <div v-else-if="switchChartContainer === 4">
    <ReactivePower v-if="visContro.reactiveVis" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  <div v-else-if="switchChartContainer === 5">
    <CurrentPower v-if="visContro.currentVis" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  <div v-else-if="switchChartContainer === 6">
    <PowerFactor v-if="visContro.factorVis" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  <div v-else-if="switchChartContainer === 7">
    <loadRateTu v-if="visContro.loadRate" style="width: 85vw; height: 340px;" :curChartData="curChartData" :timeRadio="timeRadio"/>
  </div>
  
  </div>
</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import * as echarts from 'echarts';
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail';
import { ElMessage } from 'element-plus';
import { formatDate} from '@/utils/formatTime';
import { has } from 'lodash-es';
import CurChart from './component/CurChart.vue';
import VolChart from './component/VolChart.vue';
import ActivePower from './component/ActivePower.vue';
import ReactivePower from './component/ReactivePower.vue';
import CurrentPower from './component/CurrentPower.vue';
import PowerFactor from './component/PowerFactor.vue';
import loadRateTu from './component/loadRateTu.vue';
import { CabinetApi } from '@/api/cabinet/detail';
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;

const visContro = ref({
  curVis:false,
  volVis:false,
  loadRate:true,
  activeVis:false,
  reactiveVis:false,
  currentVis:false,
  factorVis:false,
})

const queryFormRef = ref(); // 搜索的表单
const input = ref('');
// const value1 = ref('')
const hasData = ref(true);
const roomName = ref(query.roomName);
const cabinetName = ref(query.cabinetName);
const pduBox = ref(query.pduBox);

const instance = getCurrentInstance();
const typeRadio = ref('负载率');
const timeRadio = ref('近一小时');
const isHourDisabled = ref(false);
const isDayAndMonthDisabled = ref(false);
const isPowActiveDisabled = ref(true);
const isLoadRateDisabled = ref(false);
const switchChartContainer = ref(7);
let intervalId: number | null = null; // 定时器
const queryParams = reactive({
  id: Number(query.cabinet) as number | undefined,
  roomId: Number(query.roomId) as number | undefined,
  cabinet_key : query.cabinet_key as string | undefined,
  cabinet_name: query.cabinet_name as string | undefined,
  type: 1,
  granularity: "SeventyHours"
})
const queryParamsSearch = reactive({
  id: Number(query.busId) as number | undefined,
  devKey : query.devKey as string | undefined,
})
const lineChartQueryParams = reactive({
  id: Number(query.busId) as number | undefined,
  granularity: 'realtime',
})

const runLoad = ref();
const ratedCapacity = ref();
const reserveMargin = ref();
const powActive = ref();
const powReactive = ref();
const peakDemand = ref();
const powActivepPercentage = ref();
const powReactivepPercentage = ref();
const loadPercentage = ref();
const xAxisLabel = ref('');

const devKeyList = ref([]);
const switchType = ref(0);

const loadAll = async () => {
  //debugger
  var data = await BusPowerLoadDetailApi.getBusdevKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}
function fromZeroTo100(precision){
  let ans=[]
  let now=0;
  for(let i=0;i<=100/precision;i++){
    now+=i/precision;
    ans.push(now);
  }
  return ans;
}
const chartContainer = ref<HTMLElement | null>(null);
let myChart = null as echarts.ECharts | null; 
const initChart = () => {
  if (chartContainer.value && instance) {
    myChart = echarts.init(chartContainer.value);
    myChart.setOption(
      {        
        legend: {
            orient: 'horizontal',
            left: 80,
            top: 5,
            data: ['安全', '正常', '预警', '危险'],
            icon: 'rect',
            selected: {
              安全: true,
              正常: true,
              预警: true,
              危险: true
            },
            selectedMode: false, // 禁用选中模式
        },

        tooltip: {
          show: false  // 是否显示 tooltip
        },
        xAxis: {
            min: 0,
            max: 100,
            type: 'value',
            interval: 5,
            // data:[0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100],
            // data: [0,5, 10,15, 20,25, 30,35, 40,45, 50,55, 60,65, 70,75, 80,85, 90,95, 100],
            // data: fromZeroTo100(0.01), 
            axisTick: { alignWithLabel: true, show: false },
            axisLabel: {
              interval: 0,
              rotate: 0,
              formatter: function(value, _index) {
                if (value == 0) {
                    return value;
                }
                return value + '%'; // 去掉前导零并在数值后加上百分号
              }
            },
            axisLine: {
                lineStyle: {
                      color: function(value) {
                          // 根据 value 返回不同的颜色
                          if (value <= 50) {
                              return '#16c60c'; 
                          } else if (value <= 75) {
                              return '#0078d7'; 
                          } else if(value<=90){
                              return '#fff100'; 
                          }else{
                            return '#e81224';
                          }
                      },
                      width: 1, // 设置粗细
                }
            }
        },
        yAxis: {
          show: false // 不显示y轴
        },
        series: [
          {
              name: '安全',
              type: 'line',
              data: [],
              itemStyle: {
                  color: '#16c60c', // 设置颜色
              },
              show: false,  // 设置为 false 隐藏该系列数据
              
          },
          {
              name: '正常',
              type: 'line',
              data: [],
              itemStyle: {
                  color: '#0078d7', // 设置颜色
              },
              show: false,  // 设置为 false 隐藏该系列数据
          },
          {
              name: '预警',
              type: 'line',
              data: [],
              itemStyle: {
                  color: '#fff100', // 设置颜色
              },
              show: false,  // 设置为 false 隐藏该系列数据
          },
          {
              name: '危险',
              type: 'line',
              data: [],
              itemStyle: {
                  color: '#e81224', // 设置颜色
              },
              show: false,  // 设置为 false 隐藏该系列数据
          },
          {
            name: '',
            type: 'line',
            data: [[ loadPercentage.value , 50]],
            markLine: {
              data: [
                [
                  { xAxis: loadPercentage.value, yAxis: 50, symbol: 'none',  label: {
                            show: true,
                            position: 'start',
                            formatter: loadPercentage.value + ' %'+ xAxisLabel.value,
                            textStyle: {
                              fontSize: 16,
                              fontWeight: 'bold',
                              color: getTextColor(loadPercentage.value),
                            }
                        }},
                  { xAxis: loadPercentage.value, yAxis: 0, symbol: 'arrow' },
                ]
              ],
              lineStyle: {
                normal: {
                    type: 'solid',
                    color: 'green',
                  },
              },
            }
          },
        ],
      }
    )
    instance.appContext.config.globalProperties.myChart = myChart;

  } 
        // 根据 loadPercentage.value 设置字体颜色的函数
        function getTextColor (value) {
            if (value <= 40) {
                return 'rgb(223, 196, 43)'; 
            } else if (value <= 75) {
                return 'rgb(56,201,73)'; 
            } else {
                return 'rgb(230,93,93)'; 
            }
        }
};

const chartContainer1 = ref<HTMLElement | null>(null);
let myChart1 = null as echarts.ECharts | null; 
const initChart1 = () => {
  if (chartContainer1.value && instance) {
    myChart1 = echarts.init(chartContainer1.value);
    myChart1.setOption(
      {
        title: {
          text: '',
          subtext: '',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b} : {c}%'
        },
        legend: {
          orient: 'horizontal',
          bottom: '25',
          selectedMode: false
        },
        series: [
          {
            name: '',
            type: 'pie',
            radius: '50%',
            label: {
              formatter: '{b}: {d}%',
            },
            data: [
              { value: powReactivepPercentage.value, name: '无功功率', },
              { value: powActivepPercentage.value, name: '有功功率' },
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
          }          
        ]
      }
    );
    instance.appContext.config.globalProperties.myChart1 = myChart1;

  }
}

// 处理折线图数据
const createTimeData = ref<string[]>([]);
const allLineData = ref<string[]>([]);
const allEqData = ref<string[]>([]);
const eqValue = ref<number[]>([]);
const eqCreateTimeData = ref<string[]>([]);
const L1Data = ref<number[]>([]);
const L2Data = ref<number[]>([]);
const L3Data = ref<number[]>([]);

const curChartData = ref();

let myChart2 = null as echarts.ECharts | null; 
let myChart3 = null as echarts.ECharts | null; 

const getDetailData =async () => {
 try {
    const data = await CabinetApi.getCabinetDetailData({
      type: 0,
      id: Number(query.cabinet),
      roomId: Number(query.roomId),
    });
    if (data != null){
      hasData.value = true
      runLoad.value = formatNumber(data.runLoad, 2);
      ratedCapacity.value = formatNumber(data.ratedCapacity, 2);
      reserveMargin.value = formatNumber(data.reserveMargin, 2);
      powActive.value = formatNumber(data.powActive, 2);
      powReactive.value = formatNumber(data.powReactive, 2);
      peakDemand.value = formatNumber(data.peakDemand, 2);
      powActivepPercentage.value = runLoad.value == 0 ? 0 :  ((powActive.value / runLoad.value) * 100).toFixed(2);
      powReactivepPercentage.value = runLoad.value == 0 ? 0 : ((powReactive.value / runLoad.value) * 100 ).toFixed(2)
      loadPercentage.value = ratedCapacity.value == 0 ? 0 :  ((runLoad.value / ratedCapacity.value) * 100).toFixed(2);
      //loadPercentage.value = 76 测试数据
      if (loadPercentage.value <= 50){
        xAxisLabel.value = '安全'
      }else if (loadPercentage.value <= 75){
        xAxisLabel.value = '正常'
      }else if (loadPercentage.value <= 90){
        xAxisLabel.value = '预警'
      }else {
        xAxisLabel.value = '危险'
      }
    }else{
        hasData.value = false;
    }
 } finally {
 }
}

const getBusIdAndLocation =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getBusIdAndLocation(queryParams);
    if (data != null){
      queryParams.id = data.busId
      lineChartQueryParams.id = data.busId
      location.value = data.location
      busName.value = data.busName
    }else{
      location.value = null
      busName.value = null
    }
 } finally {
 }
}

// 监听切换类型
watch( ()=>typeRadio.value, async(value)=>{
  await initData();
  if ( value == '有效电能'){
     // 选有效电能不能选近一小时
     isHourDisabled.value = true
  }else{
    isHourDisabled.value = false
  }
});

//刷新图
const flashChartData = async () =>{
  await getDetailData();
  await getCVLineChartData();
  //await initData();
        function getTextColor (value) {
            if (value <= 40) {
                return 'rgb(223, 196, 43)'; 
            } else if (value <= 75) {
                return 'rgb(56,201,73)'; 
            } else {
                return 'rgb(230,93,93)'; 
            }
        }
  // 更新数据后重新渲染图表
  myChart?.setOption(
    {        
      legend: {
          orient: 'horizontal',
          left: 80,
          top: 5,
          data: ['正常运行', '经济运行', '高损耗运行'],
          icon: 'rect',
          selected: {
            正常运行: true,
            经济运行: true,
            高损耗运行: true
          },
          selectedMode: false, // 禁用选中模式
      },

      tooltip: {
        show: false  // 是否显示 tooltip
      },
      xAxis: {
          min: 0,
          max: 100,
          type: 'value',
          interval: 10,
          data: [0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100], 
          axisTick: { alignWithLabel: true, show: false },
          axisLabel: {
            interval: 0,
            rotate: 0,
            formatter: function(value, _index) {
              if (value == 0) {
                  return value;
              }
              return value + '%'; // 去掉前导零并在数值后加上百分号
            }
          },
          axisLine: {
              lineStyle: {
                    color: function(value) {
                        // 根据 value 返回不同的颜色
                        if (value <= 40) {
                            return 'rgb(223, 196, 43)'; 
                        } else if (value <= 75) {
                            return 'rgb(56,201,73)'; 
                        } else {
                            return 'rgb(230,93,93)'; 
                        }
                    },
                    width: 1, // 设置粗细
              }
          }
      },
      yAxis: {
        show: false // 不显示y轴
      },
      series: [
        {
            name: '正常运行',
            type: 'line',
            data: [],
            itemStyle: {
                color: 'rgb(223, 196, 43)', // 设置颜色
            },
            show: false,  // 设置为 false 隐藏该系列数据
            
        },
        {
            name: '经济运行',
            type: 'line',
            data: [],
            itemStyle: {
                color: 'rgb(56,201,73)', // 设置颜色
            },
            show: false,  // 设置为 false 隐藏该系列数据
        },
        {
            name: '高损耗运行',
            type: 'line',
            data: [],
            itemStyle: {
                color: 'rgb(230,93,93)', // 设置颜色
            },
            show: false,  // 设置为 false 隐藏该系列数据
        },
        {
          name: '',
          type: 'line',
          data: [[ loadPercentage.value , 50]],
          markLine: {
            data: [
              [
                { xAxis: loadPercentage.value, yAxis: 50, symbol: 'none',  label: {
                          show: true,
                          position: 'start',
                          formatter: loadPercentage.value + ' %'+ xAxisLabel.value,
                          textStyle: {
                            fontSize: 16,
                            fontWeight: 'bold',
                            color: getTextColor(loadPercentage.value),
                          }
                      }},
                { xAxis: loadPercentage.value, yAxis: 0, symbol: 'arrow' },
              ]
            ],
            lineStyle: {
              normal: {
                  type: 'solid',
                  color: 'green',
                },
            },
          }
        },
      ],
    }
  );
  myChart1?.setOption(
    {
      title: {
        text: '',
        subtext: '',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b} : {c}%'
      },
      legend: {
        orient: 'horizontal',
        bottom: '25',
        selectedMode: false
      },
      series: [
        {
          name: '',
          type: 'pie',
          radius: '50%',
          label: {
            formatter: '{b}: {d}%',
          },
          data: [
            { value: powReactivepPercentage.value, name: '无功功率', },
            { value: powActivepPercentage.value, name: '有功功率' },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          },
        }          
      ]
    }
  );
}
const isHaveData = ref(true)

const updateChart = async (type, visKey) => {
  curChartData.value = {}
  switchType.value = type; // 设置 switchType
  await getCVLineChartData(); // 获取数据

  // 重置所有 visContro 状态
  Object.keys(visContro.value).forEach((key) => {
    visContro.value[key] = false;
  });

  // 设置当前 visContro 状态
  visContro.value[visKey] = true;
};

watch(
  () => switchChartContainer.value,
  async (newValue) => {
    const actions = {
      0: () => updateChart(1, 'curVis'),
      2: () => updateChart(1, 'volVis'),
      3: () => updateChart(0, 'activeVis'),
      4: () => updateChart(0, 'reactiveVis'),
      5: () => updateChart(0, 'currentVis'),
      6: () => updateChart(0, 'factorVis'),
      7: () => updateChart(0, 'loadRate'),
    };

    if (actions[newValue]) {
      await actions[newValue]();
    }
  }
);


// 监听切换时间颗粒度
watch( ()=>timeRadio.value, async(value)=>{
  if ( value == '近一小时'){
    // 选近一小时不能选有效电能
    isPowActiveDisabled.value = true
    isLoadRateDisabled.value = false
    lineChartQueryParams.granularity = 'realtime'
    await getCVLineChartData();
  }else if (value == '近一天'){
    isPowActiveDisabled.value = false
    isLoadRateDisabled.value = true//没选中近一个小时不能选负载率
    lineChartQueryParams.granularity = 'hour'
    await getCVLineChartData();
  }else if (value == '近三天'){
    isPowActiveDisabled.value = false
    isLoadRateDisabled.value = true//没选中近一个小时不能选负载率
    lineChartQueryParams.granularity = 'SeventyHours'
    await getCVLineChartData();
  }else{
    isPowActiveDisabled.value = false
    isLoadRateDisabled.value = true//没选中近一个小时不能选负载率
    lineChartQueryParams.granularity = 'day'
    await getCVLineChartData();
  }
});

// 获取折线图数据
const getCVLineChartData =async () => {
  const data = await CabinetApi.getCabinetLineChartDetailData({
    id: Number(query.cabinet),
    roomId: Number(query.roomId),
    type: switchType.value,
    granularity: lineChartQueryParams.granularity,
    //granularity: "SeventyHours"
  });

  curChartData.value = data;
}


const initData = () => {
  if(timeRadio.value == '近一小时'){
    switch (typeRadio.value){
      case '电流':
        curChartData.value = allLineData.value;
        break;
      case '电压':
        curChartData.value = allLineData.value
        break;
      case '有效电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.L1.map((item) => item.eq_value.toFixed(3));
        }
        break;
      case '有功功率':
        curChartData.value = allLineData.value;
        break;              
      case '无功功率':
        curChartData.value = allLineData.value;
       break;
      case '视在功率':
        curChartData.value = allLineData.value;
       break;
      case '功率因素':
        curChartData.value = allLineData.value;
        break;    
      case '负载率':
        curChartData.value = allLineData.value;
        break;    
    }
  }else if(timeRadio.value == '近一天' || timeRadio.value == '近三天'){
    switch (typeRadio.value){
      case '电流':
        curChartData.value = allLineData.value;
        break;
      case '电压':
        curChartData.value = allLineData.value;
        break;
      case '有效电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.L1.map((item) => item.ele_active.toFixed(3));
        }
        break;
      case '有功功率':
        curChartData.value = allLineData.value;
        break;              
      case '无功功率':
        curChartData.value = allLineData.value;
       break;
      case '视在功率':
        curChartData.value = allLineData.value;
       break;
      case '功率因素':
        curChartData.value = allLineData.value;
        break; 
      case '负载率':
        curChartData.value = allLineData.value;
        break;  
      }
  }else{
    switch (typeRadio.value){
      case '电流':
        curChartData.value = allLineData.value;
        break;
      case '电压':
        curChartData.value = allLineData.value;
        break;
      case '有效电能':
        if(allEqData.value != null){
        eqValue.value = allEqData.value.L1.map((item) => item.eq_value.toFixed(3));
        }
        break;
      case '有功功率':
        curChartData.value = allLineData.value;
        break;              
      case '无功功率':
        curChartData.value = allLineData.value;
       break;
      case '视在功率':
        curChartData.value = allLineData.value;
       break;
      case '功率因素':
        curChartData.value = allLineData.value;
        break;
      case '负载率':
        curChartData.value = allLineData.value;
        break;  
      }
  }
  
}

// 折线图随着窗口大小改变
window.addEventListener('resize', function() {
  myChart?.resize(); 
  myChart1?.resize(); 
  myChart2?.resize(); 
  myChart3?.resize();
});

// 处理数据后有几位小数点
function formatNumber(value,index) {
  if (typeof value === 'number') {
    return value.toFixed(index); 
  } else {
    console.log('尝试对非数字值使用 toFixed 方法', value);
    return '0.000'; 
  }
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.devKey = queryParamsSearch.devKey;
  await getBusIdAndLocation();
  await flashChartData();
}

/** 初始化 **/
onMounted(async () => {
  try {
    //devKeyList.value = await loadAll();
    await getDetailData();
    await getCVLineChartData();
    visContro.value.curVis = true;
    initChart();
    initChart1();
    //initChart2()
    //initChart3()
    // 设置每五秒执行一次 getDetailData 方法
    intervalId = window.setInterval(() => {
     getDetailData();
    }, 5000);
  } catch (error) {
    console.error('onMounted 钩子中的异步操作失败:', error);
  }
})

onUnmounted(() => {
  // 清除定时器
  if (intervalId !== null) {
    clearInterval(intervalId);
  }
});

</script>

<style scoped lang="scss">
//   ::v-deep .el-card__body {
//     padding:12px;

// }
.header_app{
  background-color: white;
  display: flex;
  height: 50px;
  padding-left: 10px;
  box-shadow: 20px;
}
.header_app_text{                     
  background-color: white;
  width: 100%;
  align-content: center;
  color:#606266;
}                                                       
.header_app_text_other{
  align-content: center;
  background-color: white;
  margin-right: 5px;
}
.header_app_text_other1{
  align-content: center;
  background-color: white;

}
</style>