<template>
  <el-input v-model="input" style="width: 240px" placeholder="Please input" />&nbsp;&nbsp;
  <!-- <el-button><Icon icon="ep:arrow-left" class="mr-5px" /> 上一日</el-button>
  <el-date-picker
    v-model="value1"
    type="date"
    placeholder="Pick a day"
    size="default"
  />
  <el-button>下一日&nbsp;<Icon icon="ep:arrow-right" class="mr-5px" /></el-button> -->

  <el-button  type="primary"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
  <hr/> <br/>

  <el-row>
    <el-col :span="13">
      <span>| 电力负荷</span>
      <br/>    <br/>  
      <el-row :gutter="25">
        <el-col :span="7" :offset="1">
          <el-card style="background-color: dodgerblue;">
            <img style="float: left;" width="50px" height="30px" src="@/assets/imgs/PDU.jpg" alt=""/>
             <span style="padding-left: 2vw; color: white; font-size: 18px;">{{ratedCapacity}} </span><span style="font-size: 10px;color: white;">kVA</span><br/>
             <span style="padding-left: 3vw;color: white; font-size: 14px;" >额定容量</span>
          </el-card>
        </el-col> 
        <el-col :span="7">
          <el-card style="background-color:crimson;">
            <img style="float: left;" width="50px" height="30px" src="@/assets/imgs/PDU.jpg" alt=""/>
             <span style="padding-left: 2vw; color: white; font-size: 18px;">{{runLoad}} </span><span style="font-size: 10px;color: white;">kVA</span><br/>
             <span style="padding-left: 3vw;color: white; font-size: 14px;" >运行负荷</span>
          </el-card>
        </el-col>
        <el-col :span="7">
          <el-card style="background-color:darkorchid;">
            <img style="float: left;" width="50px" height="30px" src="@/assets/imgs/PDU.jpg" alt=""/>
             <span style="padding-left: 2vw; color: white; font-size: 18px;">{{reserveMargin}} </span><span style="font-size: 10px;color: white;">kVA</span><br/>
             <span style="padding-left: 3vw;color: white; font-size: 14px;" >负荷余量</span>
          </el-card>
        </el-col>
        <div ref="chartContainer" id="chartContainer" style="width: 75vw; height: 20vh;"></div>
      </el-row>

    </el-col>
    <el-col :span="11">
      <span>| 功率</span>
      <br/>      <br/>
      <el-row>
        <el-col :span="11">
          <el-row>
            <el-card style="width: 300px; height: 80px;">
              <span style="font-size: 18px;">{{powActive}}</span>              
              <span style="font-size: 14px; float: right; padding-top: 30px;">有功功率</span>
              <p style="font-size: 10px; padding-left: 10px;">kW</p>
            </el-card>
          </el-row>
          <el-row>
            <el-card style="width: 300px; height: 80px;">
              <span style="font-size: 18px;">{{powReactive}}</span>              
              <span style="font-size: 14px; float: right; padding-top: 30px;">无功功率</span>
              <p style="font-size: 10px; padding-left: 10px;">kVar</p>
            </el-card>
          </el-row>
          <el-row>
            <el-card style="width: 300px; height: 80px;">
              <span style="font-size: 18px;">{{peakDemand}}</span>              
              <span style="font-size: 14px; float: right; padding-top: 30px;">最大需量</span>
              <p style="font-size: 10px; padding-left: 10px;">kW</p>
            </el-card>
          </el-row>
        </el-col>
        <el-col :span="13">
          <div ref="chartContainer1" id="chartContainer1" style="width: 350px; height: 350px; margin-top: -80px;"></div>
        </el-col>
      </el-row>
    </el-col>
  </el-row>
  <hr/>
  <el-row>
    <el-col :span="20">
    <el-radio-group v-model="typeRadio">
      <el-radio-button label="电流" value="电流" />
      <el-radio-button label="电压" value="电压" />
      <el-radio-button label="有效电能" value="有效电能" />
    </el-radio-group>
  </el-col>
  <el-col :span="4">
    <el-radio-group v-model="timeRadio">
      <el-radio-button label="近一小时" value="近一小时" :disabled="isHourDisabled" />
      <el-radio-button label="近一天" value="近一天" />
      <el-radio-button label="近一月" value="近一月" />
    </el-radio-group>
  </el-col>
  </el-row>
  <br/>
  <div ref="chartContainer2" id="chartContainer2" style="width: 85vw; height: 340px;"></div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import * as echarts from 'echarts';
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail'
const input = ref('')
// const value1 = ref('')
const instance = getCurrentInstance();
const typeRadio = ref('电流')
const timeRadio = ref('近一小时')
const isHourDisabled = ref(false)
 let intervalId: number | null = null; // 定时器
const queryParams = reactive({
  id: undefined as number | undefined,
  devKey : undefined as string | undefined,
})
const lineChartQueryParams = reactive({
  id: 4 as number | undefined,
  granularity: 'day',
})
const runLoad = ref();
const ratedCapacity = ref();
const reserveMargin = ref();
const powActive = ref();
const powReactive = ref();
const peakDemand = ref();

const powActivepPercentage = ref();
const powReactivepPercentage = ref();
// interface AxisData {
//   value: string;
//   symbol: 'normal' | 'economy' | 'high-loss';
//   color: string;
// }
// const xAxisData: AxisData[] = [
//   { value: '0%', symbol: 'normal', color: 'yellow' },
//   { value: '10%', symbol: 'normal', color: 'yellow' },
//   { value: '20%', symbol: 'normal', color: 'yellow' },
//   { value: '30%', symbol: 'normal', color: 'yellow' },
//   { value: '40%', symbol: 'normal', color: 'yellow' },
//   { value: '50%', symbol: 'economy', color: 'green' },
//   { value: '60%', symbol: 'economy', color: 'green' },
//   { value: '70%', symbol: 'economy', color: 'green' },
//   { value: '80%', symbol: 'economy', color: 'green' },
//   { value: '90%', symbol: 'economy', color: 'green' },
//   { value: '100%', symbol: 'high-loss', color: 'yellow' },
//   { value: '', symbol: 'high-loss', color: 'red' }
// ];

const chartContainer = ref<HTMLElement | null>(null);
let myChart = null as echarts.ECharts | null; 
const initChart = () => {
  if (chartContainer.value && instance) {
    myChart = echarts.init(chartContainer.value);
    myChart.setOption(
      {
        legend: {
        orient: 'horizontal',
        left: 'center'
    },
    tooltip: {
      show: false  // 是否显示 tooltip
    },
    xAxis: {
        type: 'category',
        data: ['10%', '20%', '30%', '40%', '50%', '60%', '70%', '80%', '90%', '100%'],
        axisTick: { alignWithLabel: true },
        axisLabel: {
            interval: 0,
            rotate: 0,
            formatter: function(value: string, _index: number) {
                return value;
            }
        },
        axisLine: {
            lineStyle: {
                color: 'blue' // 设置x轴线颜色为蓝色
            }
        }
        },
    yAxis: {
        show: false // 不显示y轴
    },
    series: [
    {
        name: '',
        type: 'line',
        data: [[ '20%' , 50]],
        markLine: {
          // symbol: ['none', 'arrow'], 
          // data: [{
          //     lineStyle: {
          //     type: 'solid',  // 线的类型，可以是 'solid', 'dashed', 'dotted'
          //     color: 'blue',  // 线的颜色
          //     width: 2  // 线的宽度
          //   },
          //   tooltip: {
          //       show: false  // 是否显示 tooltip
          //   },
          //   label: {
          //       show: true,
          //       position: 'end',  // 标签的位置在终点
          //       formatter: '箭头'  // 标签内容，这里可以根据需要自定义
          //   },
          //   data: [ 
          //     ['20%' , 0]
          //         ]  // 终点在 x 轴上，x 坐标保持和数据点一致，y 坐标为 0
          // }]
        }
      },
    ],
  }
);
  instance.appContext.config.globalProperties.myChart = myChart;

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
          trigger: 'item'
        },
        legend: {
          orient: 'horizontal',
          bottom: '25'
        },
        series: [
          {
            name: 'Access From',
            type: 'pie',
            radius: '50%',
            data: [
              { value: powActivepPercentage, name: '有功功率' },
              { value: powReactivepPercentage, name: '无功功率' },
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
    );
    instance.appContext.config.globalProperties.myChart1 = myChart1;

  }
}

const chartContainer2 = ref<HTMLElement | null>(null);
let myChart2 = null as echarts.ECharts | null; 
const initChart2 = () => {
  if (chartContainer2.value && instance) {
    myChart2 = echarts.init(chartContainer2.value);
    myChart2.setOption(
      {
        title: {
          text: ''
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          orient: 'horizontal',
          right: '25'
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value} °C'
          }
        },
        series: [
          {
            name: 'Highest',
            type: 'line',
            data: [10, 11, 13, 11, 12, 12, 9],
            markPoint: {
              data: [
                { type: 'max', name: 'Max' },
                { type: 'min', name: 'Min' }
              ]
            },
            markLine: {
              data: [{ type: 'average', name: 'Avg' }]
            }
          },
          {
            name: 'Lowest',
            type: 'line',
            data: [1, -2, 2, 5, 3, 2, 0],
            markPoint: {
              data: [
                { type: 'max', name: 'Max' },
                { type: 'min', name: 'Min' }
              ]
            },
            markLine: {
              data: [{ type: 'average', name: 'Avg' }]
            }
          }
        ]
      }
    );
    instance.appContext.config.globalProperties.myChart2 = myChart2;

  }
}

const getDetailData =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getDetailData(queryParams);
    console.log(data)
    if (data != null){
      runLoad.value = formatNumber(data.data.runLoad, 2);
      ratedCapacity.value = formatNumber(data.data.ratedCapacity, 1);
      reserveMargin.value = formatNumber(data.data.reserveMargin, 2);
      powActive.value = formatNumber(data.data.powActive, 2);
      powReactive.value = formatNumber(data.data.powReactive, 2);
      peakDemand.value = formatNumber(data.data.peakDemand, 1);
      powActivepPercentage.value = ((powActive.value / peakDemand.value) * 100 ).toFixed(2)
      powReactivepPercentage.value = ((powReactive.value / peakDemand.value) * 100 ).toFixed(2)
    }else{
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });  
    }
 } finally {
 }
}

// 监听切换类型
watch( ()=>typeRadio.value, async(value)=>{
  if ( value == '电流'){
    isHourDisabled.value = false

  }else if (value == '电压'){
    isHourDisabled.value = false

  }else{
    // 有效电能的情况把时间颗粒度的近一小时变成禁选 如果已经选了近一小时 默认自动变成选近一天
    if (timeRadio.value == '近一小时'){
      timeRadio.value = '近一天'
    }
    isHourDisabled.value = true
  }
});

// 监听切换时间颗粒度
watch( ()=>timeRadio.value, async(value)=>{
  if ( value == '近一小时'){
    lineChartQueryParams.granularity = 'realtime'
  }else if (value == '近一天'){
    lineChartQueryParams.granularity = 'hour'
  }else{
    lineChartQueryParams.granularity = 'day'
  }
});

const getLineChartData =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getLineChartData(lineChartQueryParams);
    if (data != null){
      // runLoad.value = formatNumber(data.data.runLoad, 2);
      // ratedCapacity.value = formatNumber(data.data.ratedCapacity, 1);
      // reserveMargin.value = formatNumber(data.data.reserveMargin, 2);
      // powActive.value = formatNumber(data.data.powActive, 2);
      // powReactive.value = formatNumber(data.data.powReactive, 2);
      // peakDemand.value = formatNumber(data.data.peakDemand, 1);
      // powActivepPercentage.value = ((powActive.value / peakDemand.value) * 100 ).toFixed(2)
      // powReactivepPercentage.value = ((powReactive.value / peakDemand.value) * 100 ).toFixed(2)
    }else{
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });  
    }
 } finally {
 }
}

// 处理数据后有几位小数点
function formatNumber(value, decimalPlaces) {
    if (!isNaN(value)) {
        return value.toFixed(decimalPlaces);
    } else {
        return null; // 或者其他默认值
    }
}

   
/** 初始化 **/
onMounted(async () => {
  await getDetailData();
  await getLineChartData();
  initChart();
  initChart1();
  initChart2();

  // 设置每五秒执行一次 getDetailData 方法
  // intervalId = window.setInterval(() => {
  //   getDetailData();
  // }, 5000);
})

onUnmounted(() => {
  // 清除定时器
  if (intervalId !== null) {
    clearInterval(intervalId);
  }
});

</script>

<style scoped>

</style>