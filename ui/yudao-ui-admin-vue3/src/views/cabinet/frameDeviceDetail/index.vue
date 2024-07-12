<template>
  <div class="descriptionContainer">
    <div class="all-left">
      <el-card class="card" shadow="hover">
        <div class="progressBox">
          <div class="title">总负载率</div>
          <el-progress type="dashboard" :percentage="detailInfo.total_zb ? Number(detailInfo.total_zb).toFixed(2)*100 : 0"  />
          <div class="power">电力容量</div>
          <div>{{detailInfo.total_ele_active ? detailInfo.total_ele_active.toFixed() : 0}}kW</div>
          <div class="local">
            <el-tag size="large" type="success">{{detailInfo.room_name}}-{{detailInfo.rack_name}}</el-tag>
          </div>
        </div>
      </el-card>
    </div>
    <el-card class="all-right" shadow="hover">
      <div class="btns">
        <el-button class="btn" size="small" :loading="EleTrendLoading" :plain="!(radioBtnNH == 'DAY')" type="primary" @click="getMachineEleTrend('DAY')">当日</el-button>
        <el-button class="btn" size="small" :loading="EleTrendLoading" :plain="!(radioBtnNH == 'WEEK')" type="primary" @click="getMachineEleTrend('WEEK')">当周</el-button>
        <el-button class="btn" size="small" :loading="EleTrendLoading" :plain="!(radioBtnNH == 'MONTH')" type="primary" @click="getMachineEleTrend('MONTH')">当月</el-button>
      </div>
      <Echart :options="echarsOptionEleTrend" :height="250"/>
    </el-card>
  </div>
  <div class="descriptionContainer">
    <div class="all-left">
      <el-card class="card" shadow="hover">
        <div class="ABTotalData">
          <el-table border v-if="ABTotalData.length > 0" :data="ABTotalData" max-height="400">
            <el-table-column prop="name" label="" min-width="100" align="center" />
            <el-table-column prop="pathA" label="A路" min-width="100" align="center" />
            <el-table-column prop="pathB" label="B路" min-width="100" align="center" />
            <el-table-column prop="total" label="统计" min-width="100" align="center" />
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
import { CabinetApi } from '@/api/cabinet/info'
import { EChartsOption } from 'echarts'
import 'echarts/lib/component/dataZoom';

const radioBtn = ref('')
const radioBtnNH = ref('DAY')
const detailInfo = reactive({})
const ABTotalData = ref({})
const cabinetId = history?.state?.id || 1
const EleTrendLoading = ref(false)
const echarsOptionEleTrend = ref<EChartsOption>({})
const echartsOption = ref<EChartsOption>({})
const EleTrendOption = {
  DAY: ['当日', '昨日'], 
  WEEK: ['当周', '上周'],
  MONTH: ['当月', '上月']
}

// 获取机架详情
const getRackDetail = async() => {
  const res = await CabinetApi.getRackDetail({id:cabinetId})
  console.log('---------------', res)
  const tableData = [] as any
  const pow_apparentA = (res.rack_power.path_a.pow_apparent.reduce((a, b) => a + b)).toFixed(3)
  const pow_apparentB = res.rack_power.path_b.pow_apparent.reduce((a, b) => a + b).toFixed(3)
  const pow_activeA = res.rack_power.path_a.pow_active.reduce((a, b) => a + b).toFixed(3)
  const pow_activeB = res.rack_power.path_b.pow_active.reduce((a, b) => a + b).toFixed(3)
  tableData.push({
    name: '视在功率/kVA',
    pathA: pow_apparentA,
    pathB: pow_apparentB,
    total: res.rack_power.total_data.pow_apparent.toFixed(3),
  })
  tableData.push({
    name: '有功功率/kW',
    pathA: pow_activeA,
    pathB: pow_activeB,
    total: res.rack_power.total_data.pow_active.toFixed(3),
  })
  tableData.push({
    name: '无功功率/kVar',
    pathA: (res.rack_power.path_a.pow_reactive.reduce((a, b) => a + b)).toFixed(3),
    pathB: (res.rack_power.path_b.pow_reactive.reduce((a, b) => a + b)).toFixed(3),
    total: res.rack_power.total_data.pow_reactive.toFixed(3),
  })
  tableData.push({
    name: '功率因素',
    pathA: pow_apparentA > 0 ? (pow_activeA/pow_apparentA).toFixed(2) : 0,
    pathB: pow_apparentB > 0 ? (pow_activeB/pow_apparentB).toFixed(2) : 0,
    total: res.rack_power.total_data.pow_active > 0 ? (res.rack_power.total_data.pow_active/res.rack_power.total_data.pow_apparent).toFixed(2) : 0,
  })
  tableData.push({
    name: '负载率/%',
    pathA: pow_apparentA > 0 ? Number((pow_apparentA/res.rack_power.total_data.pow_apparent).toFixed(2)) * 100 : 0,
    pathB: pow_apparentB > 0 ? Number((pow_apparentB/res.rack_power.total_data.pow_apparent).toFixed(2)) * 100 : 0,
    total: '-',
  })
  // if (res.rack_power && res.rack_power.path_a) {
  //   const szgl = res.rack_power.path_a.pow_apparent.reduce((a, b) => a + b)
  //   const yggl = res.rack_power.path_a.pow_active.reduce((a, b) => a + b)
  //   tableData.push({
  //     name: 'A路',
  //     wggl: res.rack_power.path_a.pow_reactive.reduce((a, b) => a + b),
  //     glys: szgl > 0 ? (yggl/szgl).toFixed(2) : 0,
  //     zb: szgl > 0 ? Number((szgl/res.rack_power.total_data.pow_apparent).toFixed(2)) * 100 : 0,
  //     yggl,
  //     szgl
  //   })
  // }
  // if (res.rack_power && res.rack_power.path_b) {
  //   const szgl = res.rack_power.path_b.pow_apparent.reduce((a, b) => a + b)
  //   const yggl = res.rack_power.path_b.pow_active.reduce((a, b) => a + b)
  //   console.log('szgl', szgl,yggl, szgl > 0 ? 1 : 2)
  //   tableData.push({
  //     name: 'B路',
  //     wggl: res.rack_power.path_b.pow_reactive.reduce((a, b) => a + b),
  //     glys: szgl > 0 ? (yggl/szgl).toFixed(2) : 0,
  //     zb: szgl > 0 ? Number((szgl/res.rack_power.total_data.pow_apparent).toFixed(2)) * 100 : 0,
  //     yggl,
  //     szgl
  //   })
  // }
  // if (res.rack_power && res.rack_power.total_data) {
  //   res.rack_power.total_data.name = '统计'
  //   res.rack_power.total_data.zb = '-'
  //   tableData.push({
  //     name: '统计',
  //     wggl: res.rack_power.total_data.pow_reactive,
  //     yggl: res.rack_power.total_data.pow_active,
  //     szgl: res.rack_power.total_data.pow_apparent,
  //     glys: (res.rack_power.total_data.pow_active/res.rack_power.total_data.pow_apparent).toFixed(2),
  //     zb: '-',
  //   })
  // }
  res.total_zb = res.rack_power.total_data.power_factor
  res.total_ele_active = res.rack_power.total_data.ele_active
  console.log('tableData', tableData)
  ABTotalData.value = tableData
  Object.assign(detailInfo, res)
}
// 获取功率趋势
const getPowTrend = async(type) => {
  if (type == radioBtn.value) return
  radioBtn.value = type
  const res = await CabinetApi.getPowTrend({id:cabinetId, type})
  console.log('res', res)
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
    ]
  }
  console.log('getPowTrend res', res)
}
// 获取机柜能耗趋势
const getMachineEleTrend = async(type) => {
  try {
    EleTrendLoading.value = true
    const res = await CabinetApi.getEleTrend({ id: cabinetId, type })
    echarsOptionEleTrend.value ={
      title: {
        text: '实时能耗走势'
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function(params) {
          console.log('params', params)
          return `${params[0].seriesName}：${params[0].value}kW·h` + '<br>' + `${params[1].seriesName}：${params[1].value}kW·h`; // 使用 <b> 标签使数值加粗显示
        }
      },
      legend: {
        data: EleTrendOption[type],
        right: '45%'
      },
      grid: {
        left: '3%',
        right: '3%',
        bottom: '5',
        top: '65',
        containLabel: true
      },
      dataZoom: [
        {
          type: 'inside',
          realtime: true,
          start: 0,
          end: 100
        }
      ],
      xAxis: [
        {
          type: 'category',
          data: res.map(item => item.dateTime),
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          name: 'kW·h'
        }
      ],
      series: [
        {
          name: EleTrendOption[type][0],
          type: 'bar',
          barWidth: '20%',
          data: res.map(item => item.eq.toFixed(1)),
          // markPoint: {
          //   data: [
          //     { type: 'max', name: 'Max' },
          //     { type: 'min', name: 'Min' }
          //   ]
          // }
        },
        {
          name: EleTrendOption[type][1],
          type: 'bar',
          barWidth: '20%',
          data: res.map(item => item.lastEq.toFixed(1)),
          itemStyle: {
            color: '#bd0000' // 设置柱形图的颜色
          }
        }
      ]
    }
    console.log('获取机柜能耗趋势', res)
  } finally {
    EleTrendLoading.value = false
    radioBtn.value = type
  }
}

getRackDetail()
getPowTrend('DAY')
getMachineEleTrend('DAY')
onMounted(()=> {
})
</script>

<style lang="scss" scoped>

.descriptionContainer {
  display: flex;
  justify-content: space-between;
  flex-shrink: 0;
  margin-bottom: 16px;
  .all-left {
    width: 35%;
    box-sizing: border-box;
    flex-shrink: 0;
    margin-right: 15px;
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
    height: auto !important;
    flex: 1;
    overflow: hidden;
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
:deep(.el-card__body) {
  height: 100%;
  box-sizing: border-box;
}
:deep(.el-card) {
  height: 100%;
  box-sizing: border-box;
}
:deep(.el-table) :before {
  height: 0;
}
</style>