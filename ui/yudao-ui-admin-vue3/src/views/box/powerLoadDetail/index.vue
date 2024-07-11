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
      <span>| 电力负荷<el-tag size="large">  {{ location }}</el-tag></span>
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
        <div ref="chartContainer" id="chartContainer" style="width: 75vw; height: 20vh;margin-top: 20px; "></div>
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
          <div ref="chartContainer1" id="chartContainer1" style="width: 420px; height: 350px; margin-top: -80px;"></div>
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
      <el-radio-button label="有效电能" value="有效电能" :disabled="isPowActiveDisabled"/>
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
import { ElMessage } from 'element-plus';
import { formatDate} from '@/utils/formatTime'
const input = ref('')
// const value1 = ref('')
const location = ref(history?.state?.location)
const instance = getCurrentInstance();
const typeRadio = ref('电流')
const timeRadio = ref('近一小时')
const isHourDisabled = ref(false)
const isPowActiveDisabled = ref(true)
 let intervalId: number | null = null; // 定时器
const queryParams = reactive({
  id: history?.state?.boxId as number | undefined,
  devKey : history?.state?.devKey as string | undefined,
})
const lineChartQueryParams = reactive({
  id: history?.state?.boxId as number | undefined,
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
            data: ['正常运行', '经济运行', '高损耗运行'],
            icon: 'rect',
            selected: {
              正常运行: true,
              经济运行: true,
              高损耗运行: true
            },
        },

        tooltip: {
          show: false  // 是否显示 tooltip
        },
        xAxis: {
            min: 0,
            max: 100,
            type: 'value',
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
                    color: 'blue'
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
              show: false  // 设置为 false 隐藏该系列数据
          },
          {
              name: '经济运行',
              type: 'line',
              data: [],
              show: false  // 设置为 false 隐藏该系列数据
          },
          {
              name: '高损耗运行',
              type: 'line',
              data: [],
              show: false  // 设置为 false 隐藏该系列数据
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
            }
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
const L1Data = ref<number[]>([]);
const L2Data = ref<number[]>([]);
const L3Data = ref<number[]>([]);

const chartContainer2 = ref<HTMLElement | null>(null);
let myChart2 = null as echarts.ECharts | null; 
const initChart2 = () => {
  if (chartContainer2.value && instance) {
    myChart2 = echarts.init(chartContainer2.value);
    myChart2.setOption(
      {
        title: { text: ''},
        tooltip: { trigger: 'axis'},
        legend: { orient: 'horizontal', right: '25'},
        dataZoom:[{type: "inside"}],
        xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
        yAxis: { type: 'value'},
        series: [
          {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value},
          {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
          {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
        ],
      }
    );
    instance.appContext.config.globalProperties.myChart2 = myChart2;

  }
}

const getDetailData =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getBoxDetailData(queryParams);
    if (data != null){
      runLoad.value = formatNumber(data.runLoad, 2);
      ratedCapacity.value = formatNumber(data.ratedCapacity, 1);
      reserveMargin.value = formatNumber(data.reserveMargin, 2);
      powActive.value = formatNumber(data.powActive, 2);
      powReactive.value = formatNumber(data.powReactive, 2);
      peakDemand.value = formatNumber(data.peakDemand, 1);
      powActivepPercentage.value = peakDemand.value == 0 ? 0 :  ((powActive.value / peakDemand.value) * 100).toFixed(2);
      powReactivepPercentage.value = peakDemand.value == 0 ? 0 : ((powReactive.value / peakDemand.value) * 100 ).toFixed(2)
      loadPercentage.value = ratedCapacity.value == 0 ? 0 :  ((runLoad.value / ratedCapacity.value) * 100).toFixed(2);
      if (loadPercentage.value < 40){
        xAxisLabel.value = '正常运行'
      }else if (loadPercentage.value < 80){
        xAxisLabel.value = '经济运行'
      }else if (loadPercentage.value < 100){
        xAxisLabel.value = '正常运行'
      }else{
        xAxisLabel.value = '高损耗运行'
      }
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
  await initData();
  if ( value == '有效电能'){
     // 选有效电能不能选近一小时
     isHourDisabled.value = true
  }else{
    isHourDisabled.value = false
  }
  // 更新数据后重新渲染图表
  myChart2?.setOption({
      series: [
        {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value},
        {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
        {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
      ],
    });
});

// 监听切换时间颗粒度
watch( ()=>timeRadio.value, async(value)=>{
  if ( value == '近一小时'){
    // 选近一小时不能选有效电能
    isPowActiveDisabled.value = true
    lineChartQueryParams.granularity = 'realtime'
  }else if (value == '近一天'){
    isPowActiveDisabled.value = false
    lineChartQueryParams.granularity = 'hour'
  }else{
    isPowActiveDisabled.value = false
    lineChartQueryParams.granularity = 'day'
  }
  await getLineChartData();
  // 更新数据后重新渲染图表
  if (isHaveData.value == true){
    console.log(L1Data.value)
    myChart2?.setOption({
    title: { text: ''},
    tooltip: { trigger: 'axis'},
    legend: { orient: 'horizontal', right: '25'},
    dataZoom:[{type: "inside"}],
    xAxis: {type: 'category', boundaryGap: false, data:createTimeData.value},
    yAxis: { type: 'value'},
    series: [
      {name: 'L1', type: 'line', symbol: 'none', data: L1Data.value},
      {name: 'L2', type: 'line', symbol: 'none', data: L2Data.value},
      {name: 'L3', type: 'line', symbol: 'none', data: L3Data.value},
    ],
  }, true);
  }
 
});

const isHaveData = ref(true)
// 获取折线图数据
const getLineChartData =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getBoxLineChartData(lineChartQueryParams);
    if (data != null){
      // 查到数据
      allLineData.value = data
      if (timeRadio.value == '近一小时'){
        createTimeData.value = data.L1.map((item) => formatDate(item.create_time));
      }else{
        createTimeData.value = data.L1.map((item) => formatDate(item.create_time, 'YYYY-MM-DD'));
      }
      await initData();
      isHaveData.value = true
    }else{
      // 没查到数据
      isHaveData.value = false
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      }); 
      myChart2?.setOption({
        title: {
          text: '暂无数据',
          x: 'center',
          y: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'normal',
          }
        }
      }, true);
    }
 } finally {
 }
}

function initData (){
  if(timeRadio.value == '近一小时'){
    switch (typeRadio.value){
      case '电流':
        L1Data.value = allLineData.value.L1.map((item) => formatNumber(item.cur_value, 2));
        L2Data.value = allLineData.value.L2.map((item) => formatNumber(item.cur_value, 2));
        L3Data.value = allLineData.value.L3.map((item) => formatNumber(item.cur_value, 2));
        break;
      case '电压':
        L1Data.value = allLineData.value.L1.map((item) => formatNumber(item.vol_value, 1));
        L2Data.value = allLineData.value.L2.map((item) => formatNumber(item.vol_value, 1));
        L3Data.value = allLineData.value.L3.map((item) => formatNumber(item.vol_value, 1));
        break;
      case '有效电能':
        L1Data.value = allLineData.value.L1.map((item) => formatNumber(item.pow_active, 3));
        L2Data.value = allLineData.value.L2.map((item) => formatNumber(item.pow_active, 3));
        L3Data.value = allLineData.value.L3.map((item) => formatNumber(item.pow_active, 3));
        break;
    }
  }else{
    switch (typeRadio.value){
      case '电流':
        L1Data.value = allLineData.value.L1.map((item) => formatNumber(item.cur_avg_value, 2));
        L2Data.value = allLineData.value.L2.map((item) => formatNumber(item.cur_avg_value, 2));
        L3Data.value = allLineData.value.L3.map((item) => formatNumber(item.cur_avg_value, 2));
        break;
      case '电压':
        L1Data.value = allLineData.value.L1.map((item) => formatNumber(item.vol_avg_value, 1));
        L2Data.value = allLineData.value.L2.map((item) => formatNumber(item.vol_avg_value, 1));
        L3Data.value = allLineData.value.L3.map((item) => formatNumber(item.vol_avg_value, 1));
        break;
      case '有效电能':
        L1Data.value = allLineData.value.L1.map((item) => formatNumber(item.pow_active_avg_value, 3));
        L2Data.value = allLineData.value.L2.map((item) => formatNumber(item.pow_active_avg_value, 3));
        L3Data.value = allLineData.value.L3.map((item) => formatNumber(item.pow_active_avg_value, 3));
        break;
      }
  }
  
}

// 折线图随着窗口大小改变
window.addEventListener('resize', function() {
  myChart?.resize(); 
  myChart1?.resize(); 
  myChart2?.resize(); 
});

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