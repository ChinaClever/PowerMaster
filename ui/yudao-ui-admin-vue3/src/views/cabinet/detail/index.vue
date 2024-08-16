<template>
  <div class="descriptionContainer">
    <div class="all-left">
      <el-card class="card" shadow="hover">
        <div class="progressBox">
          <div class="title">总负载率</div>
          <el-progress type="dashboard" :percentage="detailInfo.load_factor ? Number(detailInfo.load_factor).toFixed(2) : 0"  />
          <div class="power">电力容量</div>
          <div>20KW</div>
          <div class="local">
            <el-tag size="large" type="primary">{{detailInfo.room_name}}-{{detailInfo.cabinet_name}}</el-tag>
          </div>
        </div>
      </el-card>
    </div>
    <!-- <div > -->
      <el-card class="all-right" shadow="hover">
        <div class="ABTotalData">
          <el-table border v-if="ABTotalData.length > 0" :data="ABTotalData" max-height="160">
            <el-table-column prop="name" label="" min-width="100" align="center" />
            <el-table-column prop="pow_apparent" label="视在功率(kVA)" min-width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.pow_apparent ? Number(scope.row.pow_apparent).toFixed(3) : '0.000'}}</div>
              </template>
            </el-table-column>
            <el-table-column prop="pow_active" label="有功功率(kW)" min-width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.pow_active ? Number(scope.row.pow_active).toFixed(3) : '0.000'}}</div>
              </template>
            </el-table-column>
            <el-table-column prop="pow_reactive" label="无功功率(kVar)" min-width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.pow_reactive ? Number(scope.row.pow_reactive).toFixed(3) : '0.000'}}</div>
              </template>
            </el-table-column>
            <el-table-column prop="power_factor" label="功率因素" min-width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.power_factor ? Number(scope.row.power_factor).toFixed(2) : '0.00'}}</div>
              </template>
            </el-table-column>
            <el-table-column prop="zb" label="负载率(%)" min-width="100" align="center">
              <template #default="scope">
                <div v-if="scope.row.zb == '-'">-</div>
                <div v-else>{{scope.row.zb ? Number(scope.row.zb).toFixed(2) : '0.00'}}</div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    <!-- </div> -->
  </div>
  <div class="descriptionContainer">
    <div class="all-left" v-if="tableDataA.length > 0 || tableDataB.length > 0" >
      <el-card class="card" shadow="hover">
        <div class="tableContainer">
          <el-table v-if="tableDataA.length > 0" :data="tableDataA" max-height="160">
            <el-table-column label="A路" width="100" align="center" >
              <template #default="scope">
                <div>L{{scope.$index + 1}}</div>
              </template>
            </el-table-column>
            <el-table-column prop="V" label="电压" width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.V.toFixed(1)}}V</div>
              </template>
            </el-table-column>
            <el-table-column prop="A" label="电流" width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.A.toFixed(2)}}A</div>
              </template>
            </el-table-column>
          </el-table>
          <el-table v-if="tableDataB.length > 0" :data="tableDataB" max-height="160">
            <el-table-column label="B路" width="100" align="center" >
              <template #default="scope">
                <div>L{{scope.$index + 1}}</div>
              </template>
            </el-table-column>
            <el-table-column prop="V" label="电压" width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.V.toFixed(1)}}V</div>
              </template>
            </el-table-column>
            <el-table-column prop="A" label="电流" width="100" align="center">
              <template #default="scope">
                <div>{{scope.row.A.toFixed(2)}}A</div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
    </div>
    <el-card class="all-right" shadow="hover">
      <Echart :options="echartsOption" :height="450" />
      <div class="btns">
        <el-button class="btn" size="small" :plain="!(radioBtn == 'HOUR')" type="primary" @click="getPowTrend('HOUR')">最近1小时</el-button>
        <el-button class="btn" size="small" :plain="!(radioBtn == 'DAY')" type="primary" @click="getPowTrend('DAY')">最近24小时</el-button>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import TopologyShow from "./component/TopologyShow.vue"
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import { CabinetApi } from '@/api/cabinet/detail'
import { EChartsOption } from 'echarts'

const radioBtn = ref('')
const detailInfo = reactive({})
const ABTotalData = ref({})
const tableDataA = ref({})
const tableDataB = ref({})
// const APDUData = ref<any>([])
// const APDUTriphase = ref([])
// const APDUTriphaseMax = ref(0)
// const APDUParam = ref<string[]>([])
// const CPDUData = ref([])
// const CPDUParam = ref<string[]>([])
// const SPDUData = ref([])
// const SPDUParam = ref<string[]>([])
// const {push} = useRouter() // 路由跳转
// const alarmClass = {
//   0: 'normal',
//   1: 'error',
//   2: 'warn',
//   4: 'warn',
//   8: 'error',
// }
const cabinetId = history?.state?.id || 1

const echartsOption = ref<EChartsOption>({})

// const triphaseAOption = reactive<EChartsOption>({}) as EChartsOption

// const triphaseBOption = reactive<EChartsOption>({}) as EChartsOption

// const regulateData = (data) => {
//   const lineKeys = Object.keys(data)
//   const result = [] as any
//   if (lineKeys.length > 0) {
//     for (let i = 0; i < data[lineKeys[0]].length; i++) {
//       const obj = {}
//       lineKeys.forEach(key => {
//         obj[key] = data[key][i]
//       })
//       result.push(obj)
//     }
//   }
//   return {
//     data: result,
//     keys: lineKeys
//   }
// }

const getMachineDetail = async() => {
  const res = await CabinetApi.getDetail({id:cabinetId})
  console.log('---------------', res)
  const tableData = [] as any
  if (res.cabinet_power && res.cabinet_power.path_a) {
    res.cabinet_power.path_a.name = 'A路'
    res.cabinet_power.path_a.zb = Number((res.cabinet_power.path_a.pow_apparent/res.cabinet_power.total_data.pow_apparent).toFixed(2)) * 100
    tableDataA.value = res.cabinet_power.path_a.cur_value.map((item,index) => {
      return {
        A: item,
        V: res.cabinet_power.path_a.vol_value[index]
      }
    })
    tableData.push(res.cabinet_power.path_a)
  }
  if (res.cabinet_power && res.cabinet_power.path_b) {
    res.cabinet_power.path_b.name = 'B路'
    res.cabinet_power.path_b.zb = Number((res.cabinet_power.path_b.pow_apparent/res.cabinet_power.total_data.pow_apparent).toFixed(2)) * 100
    tableData.push(res.cabinet_power.path_b)
    tableDataB.value = res.cabinet_power.path_b.cur_value.map((item,index) => {
      return {
        A: item,
        V: res.cabinet_power.path_b.vol_value[index]
      }
    })
  }
  if (res.cabinet_power && res.cabinet_power.total_data) {
    res.cabinet_power.total_data.name = '统计'
    res.cabinet_power.total_data.zb = '-'
    tableData.push(res.cabinet_power.total_data)
  }
  ABTotalData.value = tableData
  Object.assign(detailInfo, res)
}

const getPowTrend = async(type) => {
  if (type == radioBtn.value) return
  radioBtn.value = type
  const res = await CabinetApi.getPowTrend({id:cabinetId, type})
  echartsOption.value = {
    title: {
      text: '机柜列实时功率走势'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      top: '2',
      left: '180',
      selectedMode: 'single'
    },
    grid: {
      left: '8%',
      right: '8%',
      bottom: '35',
      top: '55'
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: res.map(item => item.dateTime)
    },
    yAxis: {
      type: 'value',
      // axisLabel: {
      //   formatter: '{value} '
      // }
    },
    series: [
      {
        name: '有功功率',
        type: 'line',
        symbol: 'none',
        data: res.map(item => +item.activePow.toFixed(3)),
        markPoint: {
          data: [
            { type: 'max', name: 'Max' },
            { type: 'min', name: 'Min' }
          ]
        },
      },
      {
        name: '视在功率',
        type: 'line',
        symbol: 'none',
        data: res.map(item => +item.apparentPow.toFixed(3)),
        markPoint: {
          // data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
          data: [
            { type: 'max', name: 'Max' },
            { type: 'min', name: 'Min' }
          ]
        },
      },
      {
        name: '无功功率',
        type: 'line',
        symbol: 'none',
        data: res.map(item => +item.reactivePow.toFixed(3)),
        markPoint: {
          // data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
          data: [
            { type: 'max', name: 'Max' },
            { type: 'min', name: 'Min' }
          ]
        },
      },
      {
        name: '功率因素',
        type: 'line',
        symbol: 'none',
        data: res.map(item => +item.powerFactor),
        markPoint: {
          // data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
          data: [
            { type: 'max', name: 'Max' },
            { type: 'min', name: 'Min' }
          ]
        },
      },
      {
        name: '负载率',
        type: 'line',
        symbol: 'none',
        data: res.map(item => +item.loadRate),
        markPoint: {
          // data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
          data: [
            { type: 'max', name: 'Max' },
            { type: 'min', name: 'Min' }
          ]
        },
      },
    ]
  }
  console.log('getPowTrend res', res)
}

const getData =  async() => {
  const res = await PDUDeviceApi.PDUDisplay({
    devKey: '192.168.1.93-0'
  })
  console.log('res', res)
  // if (res && res.pdu_data) {
  //   APDUData.value = regulateData(res.pdu_data.line_item_list || []).data
  //   APDUParam.value = Object.keys(res.pdu_data.line_item_list || [])
  //   APDUTriphase.value = APDUData.value.map(item => {
  //     if (item.cur_alarm_max > APDUTriphaseMax.value) APDUTriphaseMax.value = item.cur_alarm_max
  //     return item.cur_value
  //   })
  //   Object.assign(triphaseAOption, {
  //     title: [
  //       {
  //         text: 'A路电流',
  //         left: 'center',
  //         top: 'bottom'
  //       }
  //     ],
  //     polar: {
  //       radius: [10, '80%']
  //     },
  //     radiusAxis: {
  //       max: APDUTriphaseMax.value,
  //       // axisLine: false
  //     },
  //     angleAxis: {
  //       type: 'category',
  //       data: ['a','a', 'a','b', 'b', 'b','c', 'c', 'c'],
  //       startAngle: 110,
  //       axisLabel: false,
  //       axisTick: false
  //     },
  //     tooltip: {},
  //     series: {
  //       type: 'bar',
  //       data: ['32.25', , , '20.20', , ,'29.90'],
  //       coordinateSystem: 'polar',
  //       label: {
  //         show: true,
  //         position: 'middle',
  //         formatter: '{c}A'
  //       }
  //     },
  //     animation: false
  //   })
  //   Object.assign(triphaseBOption, {
  //     title: [
  //       {
  //         text: 'B路电流',
  //         left: 'center',
  //         top: 'bottom'
  //       }
  //     ],
  //     polar: {
  //       radius: [10, '80%']
  //     },
  //     radiusAxis: {
  //       max: APDUTriphaseMax.value,
  //       // axisLine: false
  //     },
  //     angleAxis: {
  //       type: 'category',
  //       data: ['a','a', 'a','b', 'b', 'b','c', 'c', 'c'],
  //       startAngle: 110,
  //       axisLabel: false,
  //       axisTick: false
  //     },
  //     tooltip: {},
  //     series: {
  //       type: 'bar',
  //       data: ['32.00', , , '22.00', , ,'28.00'],
  //       coordinateSystem: 'polar',
  //       label: {
  //         show: true,
  //         position: 'middle',
  //         formatter: '{c}A'
  //       }
  //     },
  //     animation: false
  //   })
  //   CPDUData.value = regulateData(res.pdu_data.loop_item_list || []).data
  //   CPDUParam.value = Object.keys(res.pdu_data.loop_item_list || [])
  //   SPDUData.value = regulateData(res.pdu_data.output_item_list || []).data
  //   SPDUParam.value = Object.keys(res.pdu_data.output_item_list || [])
  // }
  // console.log('APDUParam', APDUData.value, APDUTriphase.value)
}

getData()
getMachineDetail()
getPowTrend('DAY')
onMounted(()=> {
})
</script>

<style lang="scss" scoped>

.descriptionContainer {
  // width: 100%;
  // box-sizing: border-box;
  display: flex;
  justify-content: space-between;
  flex-shrink: 0;
  margin-bottom: 16px;
  .all-left {
    // flex: 3;
    width: 30%;
    box-sizing: border-box;
    flex-shrink: 0;
    // overflow: hidden;
    margin-right: 15px;
    .card {
      // height: 100%;
    }
    .tableContainer {
      height: 100%;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: space-around;
    }
    .progressBox {
      width: 100%;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      font-size: 18px;
      position: relative;
      .local {
        position: absolute;
        top: -10px;
        left: -10px;
        // width: auto;
        // padding: 10px 15px;
        // background-color: #fff;
      }
      .title {
        // font-size: 16px;
        margin-bottom: 10px;
      }
      .power {
        margin-top: 15px;
      }
    }
  }
  .all-right {
    // margin-left: 16px;
    height: auto !important;
    // width: calc(70% - 15px);
    flex: 1;
    overflow: hidden;
    // flex: 1 !important;
    // flex-shrink: 0 !important;
    color: #606060;
    position: relative;
    box-sizing: border-box;
      .btns {
        position: absolute;
        z-index: 9;
        right: 30px;
        top: 20px;
      }
    
    // .card {
      .ABTotalData {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100%;
      }
      height: 100%;
      box-sizing: border-box;
      .tabs-container {
        font-size: 16px;
        color: #606060;
        line-height: 30px;
        position: relative;
        // font-weight: bold;
        .title {
          width: 500px;
          display: flex;
          justify-content: space-between;
          margin-bottom: 10px;
        }
        .btn-jump {
          position: absolute;
          top: 0px;
          right: 10px;
        }
        .normal {
          color: #0fe24e;
        }
        .warn {
          color: #dadada;
        }
        .error {
          color: #fd0808;
        }
      }
    // }
  }
}
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
.triphaseContainer {
  margin-top: 20px;
  display: flex;
  div {
    width: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
.triphase {
  width: 350px;
  height: 350px;
  position: relative;
  .commonRound {
    border-radius: 50%;
    box-sizing: border-box;
    border: 1px solid #c6caf7;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
  .round1 {
    width: 70px;
    height: 70px;
  }
  .round2 {
    width: 140px;
    height: 140px;
  }
  .round3 {
    width: 210px;
    height: 210px;
  }
  .round4 {
    width: 280px;
    height: 280px;
  }
  .round5 {
    width: 350px;
    height: 350px;
  }
  .commonLine {
    width: 175px;
    height: 6px;
    // border-bottom: 4px solid #c6caf7;
    background-color: #c6caf7;
    position: absolute;
    left: 50%;
    top: calc(50% - 3px);
    .icon-right {
      position: absolute;
      top: -6px;
      right: -8px;
    }
  }
  .line1 {
    transform-origin: 0 2px;
    transform: rotate(30deg);
    .lineSon {
      width: 150px;
      height: 6px;
      background-color: #fd0808;
    }
  }
  .line2 {
    transform-origin: 0 2px;
    transform: rotate(150deg);
    .lineSon {
      width: 120px;
      height: 6px;
      background-color: #fd0808;
    }
  }
  .line3 {
    transform-origin: 0 2px;
    transform: rotate(-90deg);
    .lineSon {
      width: 174px;
      height: 6px;
      background-color: #fd0808;
    }
  }
}
:deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
}
:deep(.el-card) {
  height: 100%;
  box-sizing: border-box;
}
// :deep(.el-table__row .el-table__cell) {
//   border: none;
// }
// :deep(.el-table__header-wrapper .el-table__header .el-table__cell) {
//   border: none;
// }
:deep(.el-table) :before {
  height: 0;
}
:deep(.tableContainer .el-table){
  width: auto;
}
</style>