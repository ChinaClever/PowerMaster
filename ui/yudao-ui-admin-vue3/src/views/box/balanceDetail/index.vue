<template>
  <el-card class="card" shadow="never"> 
    <template #header>
      <CardTitle title="电流不平衡" /><el-tag size="large">{{ roomName }}</el-tag>
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
            content="三相电流不平衡： 不平衡度%=（MAX相电流-三相平均电流）/三相平均电流×100%"
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
        <div class="box" :style="{borderColor: colorVolList[balanceObj.colorIndex].color}">
          <div class="value">{{balanceObj.imbalanceValueB.toFixed(2)}}%</div>
          <div class="day" :style="{backgroundColor: colorVolList[balanceObj.colorIndex].color}">电压不平衡</div>
          <el-tooltip
            class="box-item"
            effect="dark"
            content="三相电压不平衡度=( 最大电压−最小电压)/平均电压×100%"
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
const roomName =  history?.state?.roomName 
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
  colorIndex: 0,
})

const getBalanceDetail = async() => {
  const res = await IndexApi.getBoxBalanceDetail({devKey:devKey})
  console.log('11111111',res)

  if (res.cur_value) {
    const cur_valueA = res.cur_value
    console.log('cur_valueA',cur_valueA)
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
        text: '电流饼形图',
        left: 'center'
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b} : {c}'
      },
      series: [
        {
          type: 'pie',
          radius: [20, 120],
          center: ['50%', '50%'],
          roseType: 'radius',
          itemStyle: {
            borderRadius: 5
          },
          label: {
            show: true,
            position: 'inside', // 将标签显示在饼图内部
            formatter: (params) => {
              return `${params.value}A`;
            },
            fontSize: 14,
            fontWeight: 'bold'
          },
          data: [
            { value: cur_valueA[0], name: 'A相电流', itemStyle: { color: '#075F71' } },
            { value: cur_valueA[1], name: 'B相电流', itemStyle: { color: '#119CB5' } },
            { value: cur_valueA[2], name: 'C相电流', itemStyle: { color: '#45C0C9' } },
          ]
        }
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
        text: '电压饼形图',
        left: 'center'
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
            { value: vol_value[0], name: 'A相电压', itemStyle: { color: '#E5B849' } },
            { value: vol_value[1], name: 'B相电压', itemStyle: { color: '#C8603A' } },
            { value: vol_value[2], name: 'C相电压', itemStyle: { color: '#AD3762' } },
          ]
        }
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
  console.log('22222222',res)
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
          data: res.map(item => item.cur[0].curValue.toFixed(2)),
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
          data: res.map(item => item.vol[0].volValue.toFixed(1)),
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
}

const ABarOption = ref<EChartsOption>({})
const BBarOption = ref<EChartsOption>({})

const ALineOption = ref<EChartsOption>({
  title: {
    text: '电流趋势',
    left: 'center'
  },
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