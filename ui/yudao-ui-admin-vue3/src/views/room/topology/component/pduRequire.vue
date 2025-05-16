<template>
  <el-dialog
    v-model="dialogVisible"
    width="80%"
    align-center
    :show-close="false"
  >     
    <!-- 自定义的头部内容（可选） -->
    <template #header>
      <el-button @click="lineidBeforeChartUnmount()" style="float:right" >关闭</el-button>
      <span style="float: right; margin:7px 10px;">{{ queryParams1.startTime }} - {{ queryParams1.endTime }}</span>
      <div>
        <div><span style="font-weight: 700; font-size: 20px;">需量电流详情</span> 所在位置：{{location?location:'未绑定'}} 网络地址：{{onlyDevKey.split('-').length > 0 ? onlyDevKey.split('-')[0] : onlyDevKey}}</div> 
      </div>
      
    </template>

    <!-- 自定义的主要内容 -->
    
      <!-- <pow /> -->
      
      <div ref="lineidChartContainer" id="lineidChartContainer"  style="width: 90vw;height: 60vh;margin-left: -100px;"></div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { PDUDeviceApi } from '@/api/pdu/pdudevice';
import * as echarts from 'echarts';

const instance = getCurrentInstance();
let lineidChart = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainer = ref<HTMLElement | null>(null);
const dialogVisible = ref(false) //全屏弹窗的显示隐藏
const onlyDevKey = ref('')
const location = ref('')

/** 打开弹窗 */
const open = async (data) => {
  const res = await PDUDeviceApi.getLocation({devKey: data.devKey});
    onlyDevKey.value = data.devKey
    location.value = res
  showDialog(data.pduId,"hour",0,1)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const showDialog = (id, type,flagValue,l1MaxCur) => {
  lineidChart?.dispose()
  getLineid(id, type,flagValue)
  if(l1MaxCur!= null && l1MaxCur != undefined && l1MaxCur != 0){
  dialogVisible.value = true
}else {
  ElMessage({
    message: '该设备没有数据',
    type: 'warning',
  });
}
  flagValue.value = 0
}

const getFullTimeByDate = (date) => {
  var year = date.getFullYear();//年
  var month = date.getMonth();//月
  var day = date.getDate();//日
  var hours = date.getHours();//时
  var min = date.getMinutes();//分
  var second = date.getSeconds();//秒
  return year + "-" +
      ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
      (day > 9 ? day : ("0" + day)) + " " +
      (hours > 9 ? hours : ("0" + hours)) + ":" +
      (min > 9 ? min : ("0" + min)) + ":" +
      (second > 9 ? second : ("0" + second));
}

const queryParams1 = reactive({
  id : undefined,
  type : undefined,
  startTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  endTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
})

const now1 = new Date();
let startTime = new Date(now1.getTime() - 24 * 60 * 60 * 1000);
let endTime = new Date();

const createTimes = ref('')
const endTimes = ref('')

const flagValue = ref(0)

//获取电流信息
const getLineid = async (id, type,flagValue) => {
  queryParams1.id = id;
  queryParams1.type = type;
  queryParams1.startTime = getFullTimeByDate(startTime);
  queryParams1.endTime = getFullTimeByDate(endTime);
  const result = await PDUDeviceApi.getMaxLineHisdata(queryParams1)

  const lChartData = ref({
    cur_max_value : [] as number[], //电流
    pow_active_max_value : [] as number[], //功率
  });

  const llChartData = ref({
    cur_max_value : [] as number[],
    pow_active_max_value : [] as number[],
  });

  const lllChartData = ref({
    cur_max_value : [] as number[],
    pow_active_max_value : [] as number[],
  });

  const lineidDateTimes = ref([] as string[])

  createTimes.value = result.dateTimes[0]
  endTimes.value = result.dateTimes[result.dateTimes.length-1]

  if(type === 'hour'){
    lineidDateTimes.value = result.dateTimes.map(dateTimeStr => {
      // 找到最后一个冒号的位置
      let lastIndexOfColon = dateTimeStr.lastIndexOf(':');
      // 截取到最后一个冒号之前的部分
      return dateTimeStr.substring(0, lastIndexOfColon);
    })
  }else if(type === 'day'){
    lineidDateTimes.value = result.dateTimes.map(dateTimeStr => {
      return dateTimeStr.substring(0, 10);
    })
  }

  //result.dateTimes

  const lData = result.l
  const llData = result.ll
  const lllData = result.lll

  // 更新图表数据
  updateChartData(lChartData, lData);
  updateChartData(llChartData, llData);
  updateChartData(lllChartData, lllData);
 
  // 假设 updateChart 函数返回正确的 ECharts 配置对象
  const option = updateChart(lChartData, llChartData, lllChartData, lineidDateTimes);
 
  // 获取正确的图表容器
  const chartContainer = flagValue === 0 ? document.getElementById('lineidChartContainer') : document.getElementById('lineidChartContainerOne');
  if (!chartContainer) {
    console.error('Chart container not found');
    return;
  }
 
  // 检查是否已经存在一个图表实例
  if (!lineidChart || lineidChart.getDom() !== chartContainer) {
    // 如果不存在或者容器已更改，则创建新的图表实例
    if (lineidChart) {
      // 如果存在旧实例，则销毁它
      lineidChart.dispose();
    }
    lineidChart = echarts.init(chartContainer);
  }
  

  // 设置图表选项
  lineidChart.setOption(option, true);
}

const updateChartData = (chartData, dataArray) => {
  chartData.value.cur_max_value = dataArray.map(item => item.cur_max_value);
  chartData.value.pow_active_max_value = dataArray.map(item => item.pow_active_max_value);
};

const updateChart = (lChartData, llChartData, lllChartData, lineidDateTimes) => {
  interface DataItem {
    Year: any;
    Country: any;
    Income: string; // 现在我们指定Income为字符串类型
  }

  const formatToTwoDecimals = (num) => {
    if (typeof num === 'number') {
      return num.toFixed(2);
    } else if (typeof num === 'string') {
      // 如果已经是字符串，则尝试转换为数字再格式化
      const parsedNum = parseFloat(num);
      return isNaN(parsedNum) ? "0.00" : parsedNum.toFixed(2);
    } else {
      return "0.00"; // 或者根据你的需求选择一个默认值
    }
  };

  const newData: DataItem[] = [];
  for (let i = 0; i < lineidDateTimes.value.length; i++) {
    newData.push({ Year: lineidDateTimes.value[i], Country: 'L1-电流', Income: formatToTwoDecimals(lChartData.value.cur_max_value[i]) });
    newData.push({ Year: lineidDateTimes.value[i], Country: 'L2-电流', Income: formatToTwoDecimals(llChartData.value.cur_max_value[i]) });
    newData.push({ Year: lineidDateTimes.value[i], Country: 'L3-电流', Income: formatToTwoDecimals(lllChartData.value.cur_max_value[i]) });
  }

  const newData1: DataItem[] = [];
  for (let i = 0; i < lineidDateTimes.value.length; i++) {
    newData1.push({ Year: lineidDateTimes.value[i], Country: 'L1功率', Income: formatToTwoDecimals(lChartData.value.pow_active_max_value[i]) });
    newData1.push({ Year: lineidDateTimes.value[i], Country: 'L2功率', Income: formatToTwoDecimals(llChartData.value.pow_active_max_value[i]) });
    newData1.push({ Year: lineidDateTimes.value[i], Country: 'L3功率', Income: formatToTwoDecimals(lllChartData.value.pow_active_max_value[i]) });
  }

  if(flagValue.value == 0){
    return {
      dataset: [
    {
      id: 'dataset_raw',
      source: newData
    },
    {
      id: 'dataset_l',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L1-电流' }
            ]
        }
      }
    },
    {
      id: 'dataset_ll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L2-电流' }
            ]
        }
      }
    },
    {
      id: 'dataset_lll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L3-电流' }
            ]
        }
      }
    }
  ],
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
        let result = params[0].value.Year + '<br>';
        params.forEach((param) => {
            let unit = param.seriesName.includes('功率')? 'kW' : 'A';
            result += `${param.seriesName}: ${param.value.Income} ${unit}<br>`;
        });
        return result;
    }
},
  xAxis: {
    type: 'category',
    axisTick: {
      alignWithLabel: true, // 让刻度线与标签对齐
    },
  },
  yAxis: {
    
  },
  legend: {
    data: ['L1-电流', 'L2-电流', 'L3-电流']
  },
  series: [
    {
      type: 'line',
      datasetId: 'dataset_l',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L1-电流'
    },
    {
      type: 'line',
      datasetId: 'dataset_ll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L2-电流'
    },
    {
      type: 'line',
      datasetId: 'dataset_lll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L3-电流'
    }
  ]
    }
  }else if(flagValue.value == 1){
    return {
      dataset: [
    {
      id: 'dataset_raw',
      source: newData1
    },
    {
      id: 'dataset_l',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L1功率' }
            ]
        }
      }
    },
    {
      id: 'dataset_ll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L2功率' }
            ]
        }
      }
    },
    {
      id: 'dataset_lll',
      fromDatasetId: 'dataset_raw',
      transform: {
        type: 'filter',
        config: {
          and: [
              { dimension: 'Country', '=': 'L3功率' }
            ]
        }
      }
    }
  ],
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
        let result = '';
        params.forEach((param) => {
            let unit = param.seriesName.includes('功率')? 'kW' : 'A';
            result += `${param.seriesName}: ${param.value.Income} ${unit}<br>`;
        });
        return result;
    }
},
  xAxis: {
    type: 'category',
    nameLocation: 'middle'
  },
  yAxis: {
    
  },
  legend: {
    data: ['L1功率', 'L2功率', 'L3功率']
  },
  series: [
    {
      type: 'line',
      datasetId: 'dataset_l',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L1功率'
    },
    {
      type: 'line',
      datasetId: 'dataset_ll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L2功率'
    },
    {
      type: 'line',
      datasetId: 'dataset_lll',
      showSymbol: false,
      encode: {
        x: 'Year',
        y: 'Income',
        itemName: 'Year',
        tooltip: ['Income']
      },
      name: 'L3功率'
    }
  ]
    }
  }
}

const lineidBeforeChartUnmount = () => {
  lineidChart?.dispose() // 销毁图表实例
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
</style>