<template>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="AB占比情况" />
    </template>
    <div class="powerContainer" v-if="balanceObj.pow_active_percent > 0">
      <div class="power">
        <div class="label">有功功率：</div>
        <div class="progressContainer">
          <div class="progress">
            <div class="left" :style="`flex: ${balanceObj.pow_active_percent}`">{{balanceObj.pow_active_percent}}%</div>
            <div class="line"></div>
            <div class="right" :style="`flex: ${100 - balanceObj.pow_active_percent}`">{{100 - balanceObj.pow_active_percent}}%</div>
          </div>
        </div>
      </div>
      <div class="power">
        <div class="label">视在功率：</div>
        <div class="progressContainer">
          <div class="progress">
            <div class="left" :style="`flex: ${balanceObj.pow_apparent_percent}`">{{balanceObj.pow_apparent_percent}}%</div>
            <div class="line"></div>
            <div class="right" :style="`flex: ${100 - balanceObj.pow_apparent_percent}`">{{100 - balanceObj.pow_apparent_percent}}%</div>
          </div>
        </div>
      </div>
    </div>
  </el-card>
  <el-card class="card" shadow="never"> 
    <template #header>
      <CardTitle title="A路电流不平衡" />
    </template>
    <div class="ImbalanceA">
      <el-card  class="cardChilc" style="margin: 0 10px" shadow="hover">
        <div class="IechartBar">
          <Echart :options="ABarOption" :height="300"/>
        </div>
      </el-card>
      <el-card  class="cardChilc" shadow="hover">
        <div class="IechartBar">
          <Echart :options="ALineOption" :height="300"/>
        </div>
      </el-card>
      <el-card  class="cardChilc" shadow="hover">
        <div class="box" :style="{borderColor: colorList[balanceObj.colorIndex].color}">
          <div class="value">{{balanceObj.imbalanceValueA}}%</div>
          <div class="day" :style="{backgroundColor: colorList[balanceObj.colorIndex].color}">{{colorList[balanceObj.colorIndex].name}}</div>
          <el-tooltip
            class="box-item"
            effect="dark"
            content="小电流不平衡是指"
            placement="right"
          >
            <div @click.prevent="" class="question">?</div>
          </el-tooltip>
        </div>
      </el-card>
    </div>
  </el-card>
  <el-card class="card" shadow="never"> 
    <template #header>
      <CardTitle title="B路电流不平衡" />
    </template>
    <div class="ImbalanceA">
      <el-card  class="cardChilc" style="margin: 0 10px" shadow="hover">
        <div class="IechartBar">
          <Echart :options="BBarOption" :height="300"/>
        </div>
      </el-card>
      <el-card  class="cardChilc" shadow="hover">
        <div class="IechartBar">
          <Echart :options="BLineOption" :height="300"/>
        </div>
      </el-card>
      <el-card  class="cardChilc" shadow="hover">
        <div class="box" :style="{borderColor: colorList[balanceObj.colorIndex].color}">
          <div class="value">{{balanceObj.imbalanceValueB}}%</div>
          <div class="day" :style="{backgroundColor: colorList[balanceObj.colorIndex].color}">{{colorList[balanceObj.colorIndex].name}}</div>
          <el-tooltip
            class="box-item"
            effect="dark"
            content="小电流不平衡是指"
            placement="right"
          >
            <div @click.prevent="" class="question">?</div>
          </el-tooltip>
        </div>
      </el-card>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts';
import { CabinetApi } from '@/api/cabinet/detail';
import { PDUDeviceApi } from '@/api/pdu/pdudevice';

const cabinetId = history?.state?.id || 1

const colorList = [{
  name: '小电流不平衡',
  color: '#aaa',
},{
  name: '大电流不平衡',
  color: '#3bbb00',
},{
  name: '大电流不平衡',
  color: '#ffc402',
},{
  name: '大电流不平衡',
  color: '#fa3333',
}]

const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  cur_valueB: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0,
})

const getBalanceDetail = async() => {
  const res = await CabinetApi.getDetail({id:cabinetId})
  console.log('res2222', res)
  if (res.cabinet_power.path_a && res.cabinet_power.path_b) {
    if (res.cabinet_power.path_a.pow_apparent == 0) balanceObj.pow_apparent_percent = 0
    else balanceObj.pow_apparent_percent = (res.cabinet_power.path_a.pow_apparent / res.cabinet_power.total_data.pow_apparent as any).toFixed(2) * 100
    if (res.cabinet_power.path_a.pow_active == 0) balanceObj.pow_active_percent = 0
    else balanceObj.pow_active_percent = (res.cabinet_power.path_a.pow_active / res.cabinet_power.total_data.pow_active as any).toFixed(2) * 100
  }
  if (res.cabinet_power.path_a) {
    const cur_valueA = res.cabinet_power.path_a.cur_value
    // const max = Math.max(...cur_valueA) // 最大值
    // // 计算平均值
    // let sum = 0
    // cur_valueA.forEach(item => {
    //   sum = sum + item
    // })
    // const average = sum/cur_valueA.length
    // // 平衡度
    // balanceObj.imbalanceValueA =  +(((max - average) * 100 / average).toFixed(0))
    ABarOption.value = {
      title: {
        text: '电流柱形图',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: Object.keys(cur_valueA),
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '电流',
          axisLabel: {
            formatter: '{value} A'
          }
        }
      ],
      series: [
        {
          name: 'A路',
          type: 'bar',
          barWidth: '20%',
          data: cur_valueA,
        },
      ]
    }
  }
  if (res.cabinet_power.path_b) {
    const cur_valueB = res.cabinet_power.path_b.cur_value
    // const max = Math.max(...cur_valueB) // 最大值
    // // 计算平均值
    // let sum = 0
    // cur_valueB.forEach(item => {
    //   sum = sum + item
    // })
    // const average = sum / cur_valueB.length
    // // 平衡度
    // balanceObj.imbalanceValueB =  +(((max - average) * 100 / average).toFixed(0))
    BBarOption.value = {
      title: {
        text: '电流柱形图',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: Object.keys(cur_valueB),
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '电流',
          axisLabel: {
            formatter: '{value} A'
          }
        }
      ],
      series: [
        {
          name: 'B路',
          type: 'bar',
          barWidth: '20%',
          data: cur_valueB,
        },
      ]
    }
  }
}

// 获取平衡度
const getBalanceDegree = async () => {
  const res = await PDUDeviceApi.getPDUDevicePage({
    pageNo: 1,
    pageSize: 24,
    cabinetIds : [cabinetId],
  })
  console.log('res', res)
  if (res.list.length > 0) {
    const itemA = res.list.find(item => item.location.includes('A路'))
    const itemB = res.list.find(item => item.location.includes('B路'))
    if (itemA) {
      balanceObj.imbalanceValueA = itemA.curUnbalance
      balanceObj.colorIndex = itemA.color - 1
    }
    if (itemB) {
      balanceObj.imbalanceValueB = itemB.curUnbalance
      balanceObj.colorIndex = itemB.color - 1
    }
  }
}

// 获取pdu电流趋势
const getBalanceTrend = async () => {
  console.log('cabinetId.value111',cabinetId);
  const res = await CabinetApi.getBalanceTrend({
    id: cabinetId
  })
  console.log('getBalanceTrendres222',res);
  if (res.length > 0) {
    const timeList = res.map(item => item.dateTime)
    if(res[0].curA && res[0].curA.length == 1) {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[0].curValue),
        },
      ]
    } else if (res[0].curA && res[0].curA.length == 3) {
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[0].curValue),
        },
        {
          name: 'A2',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[1].curValue),
        },
        {
          name: 'A3',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[2].curValue),
        },
      ]
    }
    console.log('ALineOption', ALineOption)
    if (res[0].curB && res[0].curB.length == 1) {
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'B1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[0].curValue),
        },
      ]
    } else if(res[0].curB && res[0].curB.length == 3) {
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'B1',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[0].curValue),
        },
        {
          name: 'B2',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[1].curValue),
        },
        {
          name: 'B3',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[2].curValue),
        },
      ]
    }
  }
}

const ABarOption = ref<EChartsOption>({})
const BBarOption = ref<EChartsOption>({})

const ALineOption = ref<EChartsOption>({
  title: {
    text: '电流趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    name: '电流',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis:{},
  series: []
})

const BLineOption = ref<EChartsOption>({
  title: {
    text: '电流趋势',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    name: '电流',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis:{},
  series: []
})

onBeforeMount(()=> {
  getBalanceDetail()
  getBalanceDegree()
  getBalanceTrend()
})
</script>

<style lang="scss" scoped>
.powerContainer {
  display: flex;
  .power {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    // padding-left: 50px;
    margin: 20px 0;
    .label {
      font-size: 16px;
      font-weight: bold;
    }
    .progressContainer {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 30px;
      .progress {
        width: 400px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .line {
          width: 3px;
          height: 36px;
          background-color: #000;
        }
        .left {
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          text-align: center;
          background-color:  #f86f13;
        }
      }
    }
  }
}
.ImbalanceA {
  height: 350px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 10px 0;
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
    height: 100%;
    // margin: 10px 5px;
  }
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
  .IechartBar {
    width: 100%;
    height: 100%;
  }
}

.card {
  margin-bottom: 10px;
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 70px;
  background-color: #f1f1f1;
}
</style>