<template>
  <el-dialog v-model="dialogVisibleCur" @close="handleClose" width="80%" align-center>
    <!-- 自定义的头部内容（可选） -->
    <template #header>
      <div>
        <span style="font-weight:bold;font-size:20px;margin-right:10px">电流不平衡</span>
        <span style="margin-right:10px">所在位置：{{ curlocation? curlocation:'未绑定'}}</span>
        <span>网络地址：{{ curdevkey }}</span>
      </div>
    </template>
    <!-- 自定义的主要内容 -->
    <div class="custom-content" style="margin-top: -30px;">
      <div class="custom-content-container">
        <el-card class="cardChilc" shadow="hover">
          <curUnblance :max="balanceObj.imbalanceValueA" :customColor="colorList[balanceObj.colorIndex].color" :name="colorList[balanceObj.colorIndex].name" />
        </el-card>
        <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
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
              <div style="margin-top:10px;">
                <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ib：</span><span style="font-size:16px;">{{cur_valueACopy[1]}}A</span>
              </div>
              <div style="margin-top:10px;">
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
            <Echart :options="ALineOption" :height="300" />
          </div>
        </el-card>
      </div>
      <div class="custom-content-container">
        <el-card  class="cardChilc" shadow="hover">
          <volUnblance :max="balanceObj.imbalanceValueB.toFixed(2)" :customColor=" colorVolList[balanceObj.colorIndex].color" :name="colorVolList[balanceObj.colorIndex].name"/>
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
              <div style="margin-top:10px;">
                <span class="bullet" style="color:#C8603A;">•</span><span style="width:50px;font-size:14px;">Ub：</span><span style="font-size:16px;">{{vol_valueACopy[1]}}V</span>
              </div>
              <div style="margin-top:10px;">
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
          <div class="IechartBar">
            <Echart :options="BLineOption" :height="300"/>
          </div>
        </el-card>
      </div>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts';
import { IndexApi } from '@/api/bus/busindex';
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail';
import curUnblance from '@/views/bus/curbalance/component/curUnblance.vue';
import volUnblance from '@/views/bus/curbalance/component/volUnblance.vue';

const curdevkey = ref();
const voldevkey = ref();
const dialogVisibleCur = ref(false);
const dialogVisibleVol = ref(false);
const typeRadioCur = ref("最大")
const typeRadioVol = ref("最大")
const busBalanceTrend = ref([])
const balanceTrendTime = ref([])
const clickBusId = ref('')
const curlocation = ref();

const cur_valueACopy = ref([]);
const vol_valueACopy = ref([]);

const busName = ref();

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
  color: '#075F71',
}]

const ALineOption = ref<EChartsOption>({
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let tooltipContent = `记录时间: ${params[0].name}<br/>`;
      // 遍历params数组，构建电压信息
      const phases = ['A相电流', 'B相电流', 'C相电流'];
      params.forEach((item, index) => {
        if (index < phases.length && item.seriesName) {
          tooltipContent += `${phases[index]}: ${item.value} A<br/>`;
        }
      });
      
      return tooltipContent;
    }
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
  xAxis:{},
  series: [{data:[],type:"line",symbol:"none"}]
})

const BLineOption = ref<EChartsOption>({
  tooltip: {
    trigger: 'axis',
    formatter: function (params) {
      let tooltipContent = `记录时间: ${params[0].name}<br/>`; // 显示记录时间
      // 遍历params数组，构建电压信息
      const phases = ['A相电压', 'B相电压', 'C相电压'];
      params.forEach((item, index) => {
        if (index < phases.length && item.seriesName) {
          tooltipContent += `${phases[index]}: ${item.value} V<br/>`;
        }
      });
      return tooltipContent;
    }
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
  series: [{data:[],type:"line",symbol:"none"}]
})

const getBalanceDetail = async(item) => {
  const res = await IndexApi.getBusBalanceDetail({devKey: item.devKey});
  console.log('11111111', res);
 
  // 定义默认值
  const defaultCurrentValue = [0.00, 0.00, 0.00]; // 假设三相电流默认值为0
  const defaultVoltageValue = [0.0, 0.0, 0.0]; // 假设三相电压默认值为0
 
  let cur_valueA = res.cur_value ? res.cur_value : defaultCurrentValue;
  let vol_value = res.vol_value ? res.vol_value : defaultVoltageValue;

  cur_valueACopy.value = cur_valueA.map(number => number.toFixed(2));
  vol_valueACopy.value = vol_value.map(number => number.toFixed(1));
 
  // 设置电流饼形图数据
  ABarOption.value = {
    title: {
      text: '电流饼形图',
      left: 'left'
    },
    tooltip: {
      trigger: 'item',
      // formatter: '{b} : {c}'
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
        data: [
          { value: cur_valueA[0].toFixed(2), name: 'A相电流', itemStyle: { color: '#E5B849' } },
          { value: cur_valueA[1].toFixed(2), name: 'B相电流', itemStyle: { color: '#C8603A' } },
          { value: cur_valueA[2].toFixed(2), name: 'C相电流', itemStyle: { color: '#AD3762' } },
        ]
      }
    ]
  };
 
  // 设置电压饼形图数据
  BBarOption.value = {
    title: {
      text: '电压饼形图',
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
        data: [
          { value: vol_value[0].toFixed(1), name: 'A相电压', itemStyle: { color: '#075F71' } },
          { value: vol_value[1].toFixed(1), name: 'B相电压', itemStyle: { color: '#119CB5' } },
          { value: vol_value[2].toFixed(1), name: 'C相电压', itemStyle: { color: '#45C0C9' } },
        ]
      }
    ]
  };
 
  balanceObj.imbalanceValueA = res.curUnbalance || 0;
  balanceObj.imbalanceValueB = res.volUnbalance || 0;
  balanceObj.colorIndex = (res.color || 1) - 1;
  busName.value = res.busName || '未知';
}


const getBalanceTrend = async (item) => {
  const res = await IndexApi.getBusBalanceTrend({
    busId: item.busId,
    timeType: 1
  })

  busBalanceTrend.value = res
  clickBusId.value = item.busId

  if (res.length > 0) {
    const timeList = res.map((item) => item.dateTime)
    balanceTrendTime.value = timeList
    if (res[0].cur && res[0].cur.length == 1) {
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
          data: res.map((item) => item.cur[0].curMaxValue.toFixed(2))
        }
      ]
    } else if (res[0].cur && res[0].cur.length == 3) {
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
          data: res.map((item) => item.cur[0].curMaxValue.toFixed(2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => item.cur[1].curMaxValue.toFixed(2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => item.cur[2].curMaxValue.toFixed(2))
        }
      ]
    }if (res[0].vol && res[0].vol.length == 1) {
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
          data: res.map(item => item.vol[0].volMaxValue.toFixed(1)),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3) {
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
          data: res.map(item => item.vol[0].volMaxValue.toFixed(1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[1].volMaxValue.toFixed(1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[2].volMaxValue.toFixed(1)),
        },
      ]
    }
  }
  
  console.log('ALineOption', ALineOption)
  console.log('item', item)
}

const getBalanceTrendReal = async () => {
  const res = await IndexApi.getBusBalanceTrend({
    busId: clickBusId.value,
    timeType: 0
  })
  
  if (res.length > 0) {
    const timeList = res.map(item => item.dateTime);
    console.log(res)
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
    if(res[0].cur && res[0].cur.length == 1 && typeRadioCur.value == "实时") {
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
          data: res.map(item => item.cur[0].curValue.toFixed(2)),
        },
      ]
    } else if (res[0].cur && res[0].cur.length == 3 && typeRadioCur.value == "实时") {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      console.log(ALineOption.value.xAxis)
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[0].curValue.toFixed(2)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[1].curValue.toFixed(2)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[2].curValue.toFixed(2)),
        },
      ]
    }
    if (res[0].vol && res[0].vol.length == 1 && typeRadioVol.value == "实时") {
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
          data: res.map(item => item.vol[0].volValue.toFixed(1)),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3 && typeRadioVol.value == "实时") {
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
          data: res.map(item => item.vol[0].volValue.toFixed(1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[1].volValue.toFixed(1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[2].volValue.toFixed(1)),
        },
      ]
    }
  }

  console.log(ALineOption.value.xAxis)
}

const changeType = (flag) => {
  if (busBalanceTrend.value.length > 0) {
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
    
    if (busBalanceTrend.value[0].cur && busBalanceTrend.value[0].cur.length == 1 && flag) {
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map((item) => formatEQ(item.cur[0][`${itemCurType}`],2))
        }
      ]
    } else if (busBalanceTrend.value[0].cur && busBalanceTrend.value[0].cur.length == 3 && flag) {
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map((item) => formatEQ(item.cur[0][`${itemCurType}`],2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map((item) => formatEQ(item.cur[1][`${itemCurType}`],2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map((item) => formatEQ(item.cur[2][`${itemCurType}`],2))
        }
      ]
    }if (busBalanceTrend.value[0].vol && busBalanceTrend.value[0].vol.length == 1 && !flag) {
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map(item => formatEQ(item.vol[0][`${itemVolType}`],1)),
        },
      ]
    } else if(busBalanceTrend.value[0].vol && busBalanceTrend.value[0].vol.length == 3 && !flag) {
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map(item => formatEQ(item.vol[0][`${itemVolType}`],1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map(item => formatEQ(item.vol[1][`${itemVolType}`],1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: busBalanceTrend.value.map(item => formatEQ(item.vol[2][`${itemVolType}`],1)),
        },
      ]
    }
  }
}

// 格式化耗电量列数据，保留1位小数
function formatEQ(value: number, decimalPlaces: number | undefined){
  if (!isNaN(value)) {
    return Number(value).toFixed(decimalPlaces);
  } else {
      return null; // 或者其他默认值
  }
}

/** 打开弹窗 */
const open = async (data) => {
  const res = await BusPowerLoadDetailApi.getBusIdAndLocation({devKey: data.devKey});
  showDialogCur(res)
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

const showDialogCur = (item) => {
  //colorFlag.value = item.color;
  dialogVisibleCur.value = true;
  curdevkey.value = item.devKey;
  busName.value = item.busName;
  curlocation.value = item.location;
  typeRadioCur.value = "最大"
  typeRadioVol.value = "最大"
  getBalanceDetail(item);
  getBalanceTrend(item);
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

.btn_offline,
.btn_normal,
.btn_warn,
.btn_error {
  width: 58px;
  height: 35px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
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
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
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
    margin-top: 30px;
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
    margin-top: 18px;
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
      background-color:  #f56c6c;
    }
  }
}

@media screen and (min-width: 2048px) {
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
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
          margin-left:25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
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
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
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
          margin-left:25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
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
  .arrayContainer {
    width:100%;
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
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
          margin-left:25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
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

:deep(.el-table .el-table__header th) {
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;

}

.form-container {
  display: flex;
  align-items: center;
}
 
.inline-form-item,
.inline-button,
.inline-checkbox-group,
.inline-autocomplete,
.button-group {
  margin-right: 10px; /* Adjust spacing as needed */
}
 
.inline-checkbox-group {
  display: flex;
  align-items: center;
}
 
.inline-checkbox {
  margin-right: 5px; /* Adjust spacing between checkboxes if needed */
}
 
.inline-autocomplete {
  width: 200px; /* Adjust width as needed */
}

.custom-content{
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.custom-content-container{
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin: 10px;
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

:deep(.el-card){
  --el-card-padding:5px;
}

</style>