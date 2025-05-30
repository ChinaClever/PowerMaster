<template>
  <el-dialog v-model="dialogVisibleVol" @close="handleClose" :show-close="false" style="background-color: #F1F1F1;" width="80%" align-center>
    <template #header>
      <el-button @click="lineidBeforeChartUnmountOne()" style="float:right;margin-left: 10px;" >关闭</el-button>
      <span style="font-size: 20px; font-weight: bold;margin-top: -10px;">均衡配电详情</span>
      <span style="margin-left: 15px;margin-top: -3px;">所在位置：{{ location?location:'未绑定' }}</span>
      <span style="margin-left: 15px;margin-top: -3px;">网络地址：{{ vollocation }}</span>
      <span style="float: right;margin-top: 3px;">时间：{{ createTimes }} - {{ endTimes }}</span>
      <!-- <span style="padding-left: 530px; margin-left: 10px;">更新时间: {{ dataUpdateTime }} </span> -->
    </template>
      <!-- 自定义的主要内容 -->
    <div class="custom-content" style="margin-top:-30px">
      <div class="custom-content-container">
      <el-card class="cardChilc" shadow="hover">
        <curUnblance :max="balanceObj.imbalanceValueA" :customColor="colorList[balanceObj.colorIndex].color" :name="colorList[balanceObj.colorIndex].name" />
        <!-- <div class="box" :style="{ borderColor: colorList[balanceObj.colorIndex].color }">
          <div class="value">{{ balanceObj.imbalanceValueA }}%</div>
          <div
            class="day"
            :style="{ backgroundColor: colorList[balanceObj.colorIndex].color }"
            >{{ colorList[balanceObj.colorIndex].name }}</div
          >
          <div class="status1">
            <template v-for="item in statusList" :key="item.value">
              <div class="box1" :style="{backgroundColor: item.color}"></div>{{ item.name }}
            </template>
          </div>
          <el-tooltip
            class="box-item"
            effect="dark"
            content="三相电流不平衡： 不平衡度%=（MAX相电流-三相平均电流）/三相平均电流×100%"
            placement="right"
          >
            <div @click.prevent="" class="question">?</div>
          </el-tooltip>
        </div> -->
      </el-card>
      <el-card class="cardChilc" style="margin: 0 10px" shadow="hover" >
        <div class="IechartBar" style="position: relative;">
            <div style="display: inline-block;
            width: 70%;
            height: 100%;">
              <Echart :options="ABarOption" :height="300" />
            </div>
            <div style="display: inline-block;
                position: absolute;
                width: 150px;
                height: 100px;
                top: 30%;">
              <div>
                <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ia：</span><span style="font-size:16px;">{{cur_valueACopy[0]}}A</span>
              </div>
              <div v-if="cur_valueACopy?.length == 3" style="margin-top:10px;">
                <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ib：</span><span style="font-size:16px;">{{cur_valueACopy[1]}}A</span>
              </div>
              <div v-if="cur_valueACopy?.length == 3" style="margin-top:10px;">
                <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Ic：</span><span style="font-size:16px;">{{cur_valueACopy[2]}}A</span>
              </div>
            </div>
            <!--<Echart :options="ABarOption" :height="300" />-->
          </div>
      </el-card>
      <el-card class="cardChilc" shadow="hover">
        <div style="display: flex;align-items: center;justify-content: space-between">
          <div style="font-size: 18px;font-weight: bold;margin-left: 5px;">电流趋势</div>
          <div>
            <el-select v-model="typeRadioCur" placeholder="请选择" style="width: 100px">
              <el-option label="实时" value="实时" />
              <el-option label="平均" value="平均" />
              <el-option label="最大" value="最大" />
              <el-option label="最小" value="最小" />
            </el-select>
          </div>
        </div>
        <div class="IechartBar">
          <Echart :options="ALineOption" :height="250" style="margin-top:10px" />
        </div>
      </el-card>
    </div>
    <div class="custom-content-container">
      <el-card  class="cardChilc" shadow="hover">
        <volUnblance :max="balanceObj.imbalanceValueB" :customColor="colorList[4].color"  :name="colorList[4].name" />
        <!-- <div class="box" :style="{borderColor: colorList[balanceObj.colorIndex].color}">
          <div class="value">{{balanceObj.imbalanceValueB}}%</div>
          <div class="day" :style="{backgroundColor: colorList[0].color}">电压不平衡</div>
          <el-tooltip
            class="box-item"
            effect="dark"
            content="三相电压不平衡度=( 最大电压−最小电压)/平均电压×100%"
            placement="right"
          >
            <div @click.prevent="" class="question">?</div>
          </el-tooltip>
        </div> -->
      </el-card>
      <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
        <div class="IechartBar" style="position: relative;">
            <div style="display: inline-block;
            width: 70%;
            height: 100%;">
              <Echart :options="BBarOption" :height="300"/>
            </div>
            <div style="display: inline-block;
                position: absolute;
                width: 150px;
                height: 100px;
                top: 30%;">
              <div>
                <span class="bullet" style="color:#E5B849;">•</span><span style="width:50px;font-size:14px;">Ua：</span><span style="font-size:16px;">{{vol_valueACopy[0]}}V</span>
              </div>
              <div v-if="vol_valueACopy?.length == 3" style="margin-top:10px;">
                <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ub：</span><span style="font-size:16px;">{{vol_valueACopy[1]}}V</span>
              </div>
              <div v-if="vol_valueACopy?.length == 3" style="margin-top:10px;">
                <span class="bullet" style="color:#AD3762;">•</span><span style="width:50px;font-size:14px;">Uc：</span><span style="font-size:16px;">{{vol_valueACopy[2]}}V</span>
              </div>
            </div>
          </div>
      </el-card>
      <el-card class="cardChilc" shadow="hover">
        <div style="display: flex;align-items: center;justify-content: space-between">
          <div style="font-size: 18px;font-weight: bold;margin-left: 5px;">电压趋势</div>
          <div>
            <el-select v-model="typeRadioVol" placeholder="请选择" style="width: 100px">
              <el-option label="实时" value="实时" />
              <el-option label="平均" value="平均" />
              <el-option label="最大" value="最大" />
              <el-option label="最小" value="最小" />
            </el-select>
          </div>
        </div>
        <div class="IechartBar" >
          <Echart :options="BLineOption" :height="250" style="margin-top:10px"/>
        </div>
      </el-card>
    </div>
  </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import { EChartsOption } from 'echarts'
import curUnblance from '@/views/pdu/curbalance/component/curUnblance.vue';
import volUnblance from '@/views/pdu/curbalance/component/volUnblance.vue';

const dialogVisibleVol = ref(false) //全屏弹窗的显示隐藏
const BarFlag = ref(false);
const curUnblance1 = ref()

/** 打开弹窗 */
const open = async (data) => {
  const res = await PDUDeviceApi.getLocation({devKey: data.devKey});
  location.value = res
  showDialogVol({id: data.pduId,devKey: data.devKey})
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const showDialogVol = (item) => {

  dialogVisibleVol.value = true
  vollocation.value = item.devKey
  typeRadioCur.value = "最大"
  typeRadioVol.value = "最大"

  getBalanceDetail(item)
  getBalanceTrend(item)
  curUnblance1.value = balanceObj.imbalanceValueA
// 将 item 的属性赋值给 barMaxValues

BarFlag.value = true;
}

const curlocation = ref()
const vollocation = ref()
const location = ref()
const colorList = [
  {
    name: '小电流不平衡',
    color: '#aaa'
  },
  {
    name: '大电流不平衡',
    color: '#3bbb00'
  },
  {
    name: '大电流不平衡',
    color: '#ffc402'
  },
  {
    name: '大电流不平衡',
    color: '#fa3333'
  },
  {
    name: '电压不平衡',
    color: '#0B758A'
  }
]

const colorVolList = [{
  name: '小电压不平衡',
  color: '#aaa',  //灰色
},{
  name: '大电压不平衡',
  color: '#3bbb00', //绿色
},{
  name: '大电压不平衡',
  color: '#ffc402', //黄色
},{
  name: '大电压不平衡',
  color: '#fa3333', //红色
}]

const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  vol_value: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0
})

const ABarOption = ref<EChartsOption>({})
const BBarOption = ref<EChartsOption>({})

const ALineOption = ref<EChartsOption>({
  
  tooltip: {
    trigger: 'axis'
  },
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '8%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis: {},
  series: []
})

const BLineOption = ref<EChartsOption>({
  
  tooltip: {
    trigger: 'axis'
  },
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '8%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} V'
    }
  },
  xAxis:{},
  series: []
})
// 格式化耗电量列数据，保留1位小数
function formatEQ(value: number, decimalPlaces: number | undefined){
  if (!isNaN(value)) {
    return Number(value).toFixed(decimalPlaces);
  } else {
      return null; // 或者其他默认值
  }
}
const lineidBeforeChartUnmountOne = () => {
  dialogVisibleVol.value = false
  console.log(111)
}

const cur_valueACopy = ref([]);
const vol_valueACopy = ref([]);
const typeRadioCur = ref("最大")
const typeRadioVol = ref("最大")
const pduBalanceTrend = ref([])
const balanceTrendTime = ref([])
const clickPduId = ref('')
const createTimes = ref('')
const endTimes = ref('')

const getBalanceDetail = async (item) => {
  const res = await PDUDeviceApi.balanceDetail({ devKey: item.devKey })
  const defaultCurrentValue = [0.00, 0.00, 0.00];
  const defaultVoltageValue = [0.0, 0.0, 0.0];
 
  let cur_valueA = res.cur_value ? res.cur_value : defaultCurrentValue;
  let vol_value = res.vol_value ? res.vol_value : defaultVoltageValue;

  cur_valueACopy.value = cur_valueA.map(number => number.toFixed(2));
  vol_valueACopy.value = vol_value.map(number => number.toFixed(1));
 
  // 设置电流饼形图数据
  ABarOption.value = {
    title: {
      text: '相电流',
      left: 'left'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b} : {c}'
    },
    series: [
      {
        type: 'pie',
        radius: ['30%', '80%'],
        center: ['50%', '50%'],
        roseType: 'radius',
        itemStyle: {
          borderRadius: 5
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => `${params.value}A`,
          fontSize: 14,
          fontWeight: 'bold'
        },
        data: cur_valueA?.length == 3 ? [
          { value: cur_valueA[0].toFixed(2), name: 'A相电流', itemStyle: { color: '#E5B849' } },
          { value: cur_valueA[1].toFixed(2), name: 'B相电流', itemStyle: { color: '#C8603A' } },
          { value: cur_valueA[2].toFixed(2), name: 'C相电流', itemStyle: { color: '#AD3762' } },
        ] : [
          { value: cur_valueA[0].toFixed(2), name: 'A相电流', itemStyle: { color: '#E5B849' } },
        ]
      }
    ]
  };
 
  // 设置电压饼形图数据
  BBarOption.value = {
    title: {
      text: '相电压',
      left: 'left'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '80%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => `${params.value}V`,
          fontSize: 14,
          fontWeight: 'bold'
        },
        data: vol_value?.length == 3 ? [
          { value: vol_value[0].toFixed(1), name: 'A相电压', itemStyle: { color: '#075F71' } },
          { value: vol_value[1].toFixed(1), name: 'B相电压', itemStyle: { color: '#119CB5' } },
          { value: vol_value[2].toFixed(1), name: 'C相电压', itemStyle: { color: '#45C0C9' } },
        ] : [
          { value: vol_value[0].toFixed(1), name: 'A相电压', itemStyle: { color: '#075F71' } },
        ]
      }
    ]
  };

  balanceObj.imbalanceValueA = res.curUnbalance
  balanceObj.imbalanceValueB = res.volUnbalance
  balanceObj.colorIndex = (res.color || 1) - 1
}

// 获取pdu电流趋势
const getBalanceTrend = async (item) => {
  const res = await PDUDeviceApi.balanceTrend({
    pduId: item.id,
    timeType: 1
  })

  pduBalanceTrend.value = res
  clickPduId.value = item.id
  
  createTimes.value = res[0].dateTime;
  const lastIndex = res.length - 1;
  endTimes.value = res[lastIndex].dateTime;
  if (res.length > 0) {
    const timeList = res.map((item) => item.dateTime)
    balanceTrendTime.value = timeList
    ALineOption.value.grid = {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    }
    BLineOption.value.grid = {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    }
    if (res[0].cur && res[0].cur.length == 1) {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curMaxValue,2))
        }
      ]
    } else if (res[0].cur && res[0].cur.length == 3) {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curMaxValue,2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[1].curMaxValue,2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[2].curMaxValue,2))
        }
      ]
    }if (res[0].vol && res[0].vol.length == 1) {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[0].volMaxValue,1)),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3) {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += 'V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[0].volMaxValue,1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[1].volMaxValue,1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[2].volMaxValue,1)),
        },
      ]
    }
  }
}

// 获取pdu电流趋势
const getBalanceTrendReal = async () => {
  const res = await PDUDeviceApi.balanceTrend({
    pduId: clickPduId.value,
    timeType: 0
  })
  
  if (res.length > 0) {
    const timeList = res.map((item) => item.dateTime)
    if(typeRadioCur.value == "实时") {
      ALineOption.value.grid = {
        left: '6%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
    }
    if(typeRadioVol.value == "实时") {
      BLineOption.value.grid = {
        left: '6%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
    }
    if (res[0].cur && res[0].cur.length == 1 && typeRadioCur.value == "实时") {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curValue,2))
        }
      ]
    } else if (res[0].cur && res[0].cur.length == 3 && typeRadioCur.value == "实时") {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curValue,2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[1].curValue,2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[2].curValue,2))
        }
      ]
    }if (res[0].vol && res[0].vol.length == 1 && typeRadioVol.value == "实时") {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[0].volValue),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3 && typeRadioVol.value == "实时") {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += 'V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[0].volValue,1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[1].volValue,1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[2].volValue,1)),
        },
      ]
    }
  }
}

const changeType = (flag) => {
  if (pduBalanceTrend.value.length > 0) {
    let itemCurType = typeRadioCur.value == "最小" ? 'curMinValue' : (typeRadioCur.value == "最大" ? 'curMaxValue' : 'curValue')
    let itemVolType = typeRadioVol.value == "最小" ? 'volMinValue' : (typeRadioVol.value == "最大" ? 'volMaxValue' : 'volValue')
    console.log(itemCurType,itemVolType,flag)
    if(typeRadioCur.value != "实时") {
      ALineOption.value.grid = {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: balanceTrendTime.value
      }
    }
    if(typeRadioVol.value != "实时") {
      BLineOption.value.grid = {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: balanceTrendTime.value
      }
    }
   
    if (pduBalanceTrend.value[0].cur && pduBalanceTrend.value[0].cur.length == 1 && flag) {
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[0][`${itemCurType}`],2))
        }
      ]
    } else if (pduBalanceTrend.value[0].cur && pduBalanceTrend.value[0].cur.length == 3 && flag) {
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[0][`${itemCurType}`],2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[1][`${itemCurType}`],2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[2][`${itemCurType}`],2))
        }
      ]
    }if (pduBalanceTrend.value[0].vol && pduBalanceTrend.value[0].vol.length == 1 && !flag) {
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[0][`${itemVolType}`],1)),
        },
      ]
    } else if(pduBalanceTrend.value[0].vol && pduBalanceTrend.value[0].vol.length == 3 && !flag) {
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[0][`${itemVolType}`],1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[1][`${itemVolType}`],1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[2][`${itemVolType}`],1)),
        },
      ]
    }
  }
}

// 监听切换类型
watch(() => typeRadioCur.value ,(value)=>{
  if(value == "实时") {
    getBalanceTrendReal()
  } else {
   changeType(true)
  }
})

// 监听切换类型
watch( ()=>typeRadioVol.value, (value)=>{
  if(value == "实时") {
    getBalanceTrendReal()
  } else {
   changeType(false)
  }
})

</script>

<style lang="scss" scoped>
:deep(.ip:hover) {
  color: blue !important;
  cursor: pointer;
}

.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      color: #777777;
      cursor: pointer;
      font-size: 13px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
  }
}


.bthnn {
  width: 58px;
  height: 35px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px
  
}

.btn_offline,
.btn_normal,
.btn_warn,
.btn_error {
  width: 58px;
  height: 32px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
  }
}
.btn_offline {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.offline {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
  margin-right: 8px;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #ffc402;
  background-color: #fff;
  margin-right: 8px;
}
.warn {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #fa3333;
  background-color: #fff;
  margin-right: 8px;
}
.error {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0, 0, 0, 0.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    margin-top: 20px;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);
    }
    .right {
      overflow: hidden;
      background-color: #f56c6c;
    }
  }
}

@media screen and (min-width: 2048px) {
  .table-height{
    height: 78vh;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 20%;
      height: 140px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
          margin-top: 10px;
        }
      }
      .devKey {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .room {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .status {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        color: #fff;
        position: absolute;
        right: 38px;
        top: 8px;
      }
      .detail {
        width: 40px;
        height: 25px;
        padding: 0;
        border: 1px solid #ccc;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        position: absolute;
        right: 8px;
        bottom: 8px;
        cursor: pointer;
      }
    }
  }
}

@media screen and (max-width: 2048px) and (min-width: 1600px) {
  .table-height{
    height: 720px;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 25%;
      height: 140px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
          margin-top: 10px;
        }
      }
      .devKey {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .room {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .status {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        color: #fff;
        position: absolute;
        right: 38px;
        top: 8px;
      }
      .detail {
        width: 40px;
        height: 25px;
        padding: 0;
        border: 1px solid #ccc;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        position: absolute;
        right: 8px;
        bottom: 8px;
        cursor: pointer;
      }
    }
  }
}

@media screen and (max-width: 1600px) {
  .table-height{
    height: 600px;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 33%;
      height: 140px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
          margin-top: 10px;
        }
      }
      .devKey {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .room {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .status {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;

        color: #fff;
        position: absolute;
        right: 38px;
        top: 8px;
      }
      .detail {
        width: 40px;
        height: 25px;
        padding: 0;
        border: 1px solid #ccc;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        position: absolute;
        right: 8px;
        bottom: 8px;
        cursor: pointer;
      }
    }
  }
}

:deep(.master-left .el-card__body) {
  padding: 0;
}

:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

:deep(.el-form .el-form-item) {
  margin-right: 0;
}


::v-deep .el-table .el-table__header th {
  background-color: #f7f7f7;
  color: #909399;
  height: 30px;
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 70px;
}

.custom-content{
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.question {
  width: 12px;
  height: 12px;
  position: absolute;
  top: 2px;
  right: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
}

.cardChilc {
  flex: 1;
  margin: 0 10px;
  box-sizing: border-box;
  .box {
    position: relative;
    height: 121px;
    width: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    border: 1px solid #00289e;
    margin: 72px auto;
    
    .value {
      font-size: 30px;
      padding: 20px 0;
    }
    .day {
      width: 100%;
      font-size: 16px;
      text-align: center;
      color: #fff;
      background-color: #00289e;
      padding: 10px 0;
    }
  }
}

::v-deep .el-table th,
::v-deep .el-table td{
 border-right: none;
}

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 5px;
}

.btnallNotSelected{
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #000000;
  border: 1px solid #409EFF;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
  }
}
.status1 {
  font-size: 14px;
  display: flex;
  align-items: center;
  .box1 {
    font-display: center;
    width: 10px;
    height: 10px;
    border-radius: 2px;
    margin-left: 10px;
    margin-right: 5px;
  }
}
.custom-content-container{
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin: 10px;
}

.label-container {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中 */
  color:#000;
}

:deep(.el-card){
  --el-card-padding:5px;
}
</style>