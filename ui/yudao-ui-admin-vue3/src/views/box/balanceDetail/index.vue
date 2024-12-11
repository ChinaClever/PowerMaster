<template>
  <el-card class="card" shadow="never"> 
    <template #header>
      <CardTitle title="电流不平衡" /><el-tag size="large">{{ location }}</el-tag>
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
          <div class="value">{{balanceObj.imbalanceValueA.toFixed(2)}}%</div>
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
      <CardTitle title="电压不平衡" />
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
          <div class="value">{{balanceObj.imbalanceValueB.toFixed(2)}}%</div>
          <div class="day" :style="{backgroundColor: colorList[0].color}">电压不平衡</div>
          <el-tooltip
            class="box-item"
            effect="dark"
            content="电压不平衡是指"
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
import { EChartsOption } from 'echarts'
import { IndexApi } from '@/api/bus/boxindex'


const boxId = history?.state?.boxId || -1
const devKey = history?.state?.devKey || "0"
const location =  history?.state?.location 

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
  vol_value: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0,
})

const getBalanceDetail = async() => {
  const res = await IndexApi.getBoxBalanceDetail({devKey:devKey})

  if (res.cur_value) {
    const cur_valueA = res.cur_value
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
          data: ["A","B","C"],
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

          type: 'bar',
          barWidth: '20%',
          data: cur_valueA,
        },
      ]
    }
  }
  if (res.vol_value) {
    const vol_value = res.vol_value
    // const max = Math.max(...vol_value) // 最大值
    // // 计算平均值
    // let sum = 0
    // vol_value.forEach(item => {
    //   sum = sum + item
    // })
    // const average = sum / vol_value.length
    // // 平衡度
    // balanceObj.imbalanceValueB =  +(((max - average) * 100 / average).toFixed(0))
    BBarOption.value = {
      title: {
        text: '电压柱形图',
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
          data: ["A","B","C"],
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: '电压',
          axisLabel: {
            formatter: '{value} V'
          }
        }
      ],
      series: [
        {
          type: 'bar',
          barWidth: '20%',
          data: vol_value,
        },
      ]
    }
  }

  balanceObj.imbalanceValueA = res.curUnbalance
  balanceObj.imbalanceValueB = res.volUnbalance
  balanceObj.colorIndex = res.color - 1
}

// 获取平衡度
// const getBalanceDegree = async () => {
//   const res = await IndexApi.getPDUDevicePage({
//     pageNo: 1,
//     pageSize: 24,
//     boxIds : [boxId],
//   })
//   console.log('res', res)
//   if (res.list.length > 0) {
//     const itemA = res.list.find(item => item.location.includes('A路'))
//     const itemB = res.list.find(item => item.location.includes('B路'))
//     if (itemA) {
//       balanceObj.imbalanceValueA = itemA.curUnbalance
//       balanceObj.colorIndex = itemA.color - 1
//     }
//     if (itemB) {
//       balanceObj.imbalanceValueB = itemB.curUnbalance
//       balanceObj.colorIndex = itemB.color - 1
//     }
//   }
// }

// 获取pdu电流趋势
const getBalanceTrend = async () => {
  const res = await IndexApi.getBoxBalanceTrend({
    boxId: boxId
  })
  if (res.length > 0) {
    const timeList = res.map(item => item.dateTime)
    if(res[0].cur && res[0].cur.length == 1) {
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
          data: res.map(item => item.cur[0].curValue),
        },
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
          data: res.map(item => item.cur[0].curValue),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[1].curValue),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.cur[2].curValue),
        },
      ]
    }
    console.log('ALineOption', ALineOption)
    if (res[0].vol && res[0].vol.length == 1) {
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
          data: res.map(item => item.vol[0].volValue),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[1].volValue),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[2].volValue),
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
    text: '电压趋势',
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
    name: '电压',
    axisLabel: {
      formatter: '{value} V'
    }
  },
  xAxis:{},
  series: []
})

onBeforeMount(()=> {
  getBalanceDetail()
  // getBalanceDegree()
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
</style>