<template>
  <!-- <div id="main"></div> -->
  <div class="energy">
    <div class="top">
      <ContentWrap>
        <el-form
          class="-mb-15px topForm"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="" prop="jf">
            <el-select v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-200px">
              <el-option v-for="item in roomList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item >
          <span class="line"></span>
          <el-form-item label="" prop="jg">
            <el-select v-model="queryParams.cabinetId" placeholder="请选择" class="!w-200px">
              <el-option v-for="item in machineList" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </ContentWrap>
    </div>
    <div class="content">
      <el-card class="card-hb" shadow="never">
        <template #header>
          <CardTitle title="环比" />
        </template>
        <div class="hb-content">
          <div class="box">
            <div class="value">{{EleChain.todayEq && EleChain.todayEq.toFixed(1)}}</div>
            <div class="introduce">当日电量(kW·h)</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.yesterdayEq && EleChain.yesterdayEq.toFixed(1)}}</div>
            <div class="introduce">昨日电量(kW·h)</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.dayRate}}</div>
            <div class="introduce">日环比</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.thisWeekEq && EleChain.thisWeekEq.toFixed(1)}}</div>
            <div class="introduce">当周电量(kW·h)</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.lastWeekEq && EleChain.lastWeekEq.toFixed(1)}}</div>
            <div class="introduce">上周电量(kW·h)</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.weekRate}}</div>
            <div class="introduce">周环比</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.thisMonthEq && EleChain.thisMonthEq.toFixed(1)}}</div>
            <div class="introduce">当月电量(kW·h)</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.lastMonthEq && EleChain.lastMonthEq.toFixed(1)}}</div>
            <div class="introduce">上月电量(kW·h)</div>
          </div>
          <div class="box">
            <div class="value">{{EleChain.monthRate}}</div>
            <div class="introduce">月环比</div>
          </div>
        </div>
      </el-card>
      <el-card class="card-nh" shadow="never">
        <template #header>
          <div class="chart-nh">
            <CardTitle title="能耗趋势" />
            <div class="btns">
              <el-button class="btn" size="small" :loading="EleTrendLoading" :plain="!(radioBtn == 'DAY')" type="primary" @click="getMachineEleTrend('DAY')">当日</el-button>
              <el-button class="btn" size="small" :loading="EleTrendLoading" :plain="!(radioBtn == 'WEEK')" type="primary" @click="getMachineEleTrend('WEEK')">当周</el-button>
              <el-button class="btn" size="small" :loading="EleTrendLoading" :plain="!(radioBtn == 'MONTH')" type="primary" @click="getMachineEleTrend('MONTH')">当月</el-button>
            </div>
          </div>
        </template>
        <!-- <div style="height: 260px"> -->
          <Echart :options="echarsOptionEleTrend" :height="255"/>
        <!-- </div> -->
      </el-card>
    </div>
    <div class="bottom">
      <el-card class="card-day" shadow="never">
        <template #header>
          <CardTitle title="日用电总有功功率曲线" />
        </template>
        <Echart :options="echartsOptionPowTrend" :height="260" />
      </el-card>
      <el-card class="card-dayTop" shadow="never">
        <template #header>
          <CardTitle title="总有功功率峰值" />
        </template>
        <div class="box" style="margin-bottom: 10px;" @click="test">
          <div class="value">{{ActivePowTrend.todayMax && ActivePowTrend.todayMax.toFixed(3)}}</div>
          <div class="time">{{ActivePowTrend.todayMaxTime}}</div>
          <div class="day">当日(kW)</div>
        </div>
        <div class="box">
          <div class="value">{{ActivePowTrend.yesterdayMax && ActivePowTrend.yesterdayMax.toFixed(3)}}</div>
          <div class="time">{{ActivePowTrend.yesterdayMaxTime}}</div>
          <div class="day">昨日(kW)</div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
// import * as echarts from 'echarts';
import { EChartsOption } from 'echarts'
import { CabinetApi } from '@/api/cabinet/info'
import { CabinetEnergyApi } from '@/api/cabinet/energy'
import 'echarts/lib/component/dataZoom';
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;

const roomList = ref([]) // 左侧导航栏树结构列表
const machineList = ref([]) // 左侧导航栏树结构列表
const radioBtn = ref('DAY')
const EleTrendOption = {
  DAY: ['当日', '昨日'], 
  WEEK: ['当周', '上周'],
  MONTH: ['当月', '上月']
}
const EleTrendLoading = ref(false)
const queryParams = reactive({
  cabinetName: query.cabinetName || ' ',
  cabinetId: Number(query.id) || 1,
  roomName: query.roomName || ' ',
  cabinetroomId: Number(query.roomId) || 1
})
const EleChain = reactive({
  todayEq: '',
  yesterdayEq: '',
  dayRate: '',
  thisWeekEq: '',
  lastWeekEq: '',
  weekRate: '',
  thisMonthEq: '',
  lastMonthEq: '',
  monthRate: '',
})
const ActivePowTrend = reactive({})

watch(() => queryParams.cabinetroomId, (val) => {
  machineList.value = handleNavList(val)
  if (machineList.value.length == 0) {
    queryParams.cabinetId = null
    return
  }
  const defaultValue = machineList.value[0] as any
  queryParams.cabinetId = defaultValue.id
})

watch(() => queryParams.cabinetId,(val) => {
  if (val !=null) {
  getActivePowTrend()
  getMachineEleChain()
  getMachineEleTrend('DAY')
  }
})

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  if (res.length > 0) {
    roomList.value = res
    machineList.value = handleNavList(queryParams.cabinetroomId)
  }
}
const handleNavList = (cabinetroomId) => {
  const data = roomList.value as any
  const roomIndex = data.findIndex(item => item.id == cabinetroomId)
  let list = [] as any
  if (data[roomIndex].children.length > 0) {
    data[roomIndex].children.forEach(child => {
      if (child.type == 2)  {
        if(child.children.length > 0){
        child.children.forEach(grandChild => {
          list.push(grandChild)
        })
        }
      } else if(child.type == 3) {
        list.push(child)
      }
    })
  }
  return list
}

// 获取机柜有功功率趋势
const getActivePowTrend = async() => {
  const res = await CabinetEnergyApi.getActivePowTrend({id:queryParams.cabinetId})
  Object.assign(ActivePowTrend, res)
  echartsOptionPowTrend.value = {
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        animation: false,
        label: {
          backgroundColor: '#505765'
        }
      },
      formatter(params) {
        let result = ''
        params.forEach(item => {
          if(item.seriesName == '昨日'&&item.value!=null&&item.value!=''){
            result+=`昨日最大值：${item.value}KW 发生时间：${res.yesterdayList[item.dataIndex]?.maxActivePowTime||''}<br/>`
          }
          if(item.seriesName == '当日'&&item.value!=null&&item.value!=''){
            result+=`当日最大值：${item.value||'-'}KW 发生时间：${res.todayList[item.dataIndex]?.maxActivePowTime||''}<br/>`
          }
        })
        console.log("params=========",params);
        return result ;
      }
    },
    legend: {
      data: ['昨日', '当日'],
      right: '4%'
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
        boundaryGap: false,
        axisLine: { onZero: false },
        data: res.yesterdayList?.length > 0 ? res.yesterdayList?.map(item => item?.dateTime?.split(' ')[1]) : res.todayList?.map(item => item?.dateTime?.split(' ')[1]) 
      }
    ],
    yAxis: [
      {
        name: 'kW',
        type: 'value'
      },
    ],
    series: [
      {
        name: '昨日',
        type: 'line',
        // smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2
        },
        markLine: {
          data: [
            {
              type: 'average',
              name: 'Avg1',
            },
          ]
        },
        emphasis: {
          focus: 'series'
        },
        data: res.yesterdayList.map(item => item.maxActivePow)
      },
      {
        name: '当日',
        type: 'line',
        // smooth: true,
        symbol: 'none',
        lineStyle: {
          width: 2
        },
        emphasis: {
          focus: 'series'
        },
        markLine: {
          data: [{ type: 'average', name: 'Avg2' }]
        },
        data: res.todayList.map(item => item.maxActivePow)
      }
    ]
  }
}
// 获取机柜用能环比
const getMachineEleChain = async() => {
  const res = await CabinetEnergyApi.getEleChain({id:queryParams.cabinetId})
  Object.assign(EleChain, res)
}
// 获取机柜能耗趋势
const getMachineEleTrend = async(type) => {
  try {
    EleTrendLoading.value = true
    const res = await CabinetEnergyApi.getEleTrend({ id: queryParams.cabinetId, type })
    echarsOptionEleTrend.value ={
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: function(params) {
          return `${params[0].seriesName}：${params[0].value}kW·h` + '<br>' + `${params[1].seriesName}：${params[1].value}kW·h`; // 使用 <b> 标签使数值加粗显示
        }
      },
      legend: {
        data: EleTrendOption[type],
        right: '4%'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
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
  } finally {
    EleTrendLoading.value = false
    radioBtn.value = type
  }
}

onBeforeMount(() => {
  getNavList()
  getActivePowTrend()
  getMachineEleChain()
  getMachineEleTrend('DAY')
})

const echarsOptionEleTrend = ref<EChartsOption>({})

const echartsOptionPowTrend = ref<EChartsOption>({})
</script>

<style scoped lang="scss">
:deep(.topForm .el-form-item) {
  margin-right: 0px
}
:deep(.el-card) {
  border: none;
}
.topForm .line {
  display: inline-block;
  width: 8px;
  border-bottom: 1px solid #000;
  margin: 0px 8px 13px 8px;
}
.energy {
  .content {
    width: 100%;
    display: flex;
    .card-hb {
      width: 40%;
      margin-right: 10px;
      .hb-content {
        display: flex;
        flex-wrap: wrap;
        .box {
          // height: 75px;
          width: 33%;
          box-sizing: border-box;
          border: 4px solid #fff;
          display: flex;
          flex-direction: column;
          align-items: center;
          background-color: #f6f6f6;
          .value {
            font-size: 24px;
            padding: 8px 0;
            color: #00289e;
          }
          .introduce {
            color: rgb(0, 0, 0, 0.5);
            font-size: 14px;
            padding-bottom: 10px;
          }
        }
      }
    }
    .card-nh {
      width: 60%;
      .chart-nh {
        position: relative;
        .btns {
          position: absolute;
          top: 0;
          right: 50px;
          width: 150px;
          display: flex;
          // justify-content: space-between;
          .btn {
            flex-shrink: 0;
            width: 55px;
            height: 31px;
            margin-left: 10px;
          }
        }
      }
    }
  }
  .bottom {
    display: flex;
    margin-top: 10px;
    .card-day {
      flex: 1;
    }
    .card-dayTop {
      width: 350px;
      .box {
        display: flex;
        flex-direction: column;
        align-items: center;
        border: 1px solid #00289e;
        .value {
          font-size: 30px;
          padding: 12px 0;
        }
        .time {
          font-size: 14px;
          padding-bottom: 8px;
        }
        .day {
          width: 100%;
          font-size: 16px;
          text-align: center;
          color: #fff;
          background-color: #00289e;
          padding: 6px 0;
        }
      }
    }
  }
}

</style>
