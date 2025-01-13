<template>
  <!-- <div id="main"></div> -->
  <div class="energy" style="background-color: #E7E7E7;">
  <div class="header_app">
    <div class="header_app_text">所在位置：{{ location }}&nbsp;&nbsp;&nbsp; (名称：{{busName}})
    </div>
    <div class="header_app_text_other1">
          <el-col :span="10"> 
            <el-form
              class="-mb-15px"
              :model="queryParamsSearch"
              ref="queryFormRef"
              :inline="true"
              label-width="120px"
            >
              <el-form-item label="网络地址" prop="devKey" >
              <el-autocomplete
                v-model="queryParamsSearch.devKey"
                :fetch-suggestions="querySearch"
                placeholder="请输入网络地址"  
                clearable
                class="!w-160px"
                @select="handleQuery" 
              />
              </el-form-item>
            </el-form>
          </el-col>      
    </div>
    <div class="header_app_text_other">
      <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
    </div>
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
          <CardTitle title="日用电有功功率曲线" />
        </template>
        <Echart :options="echartsOptionPowTrend" :height="260" />
      </el-card>
      <el-card class="card-dayTop" shadow="never">
        <template #header>
          <CardTitle title="有功功率峰值" />
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
import { BusEnergyApi } from '@/api/bus/busenergy'
import 'echarts/lib/component/dataZoom';
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail';

const location = ref(history?.state?.location )
const busName = ref(history?.state?.busName )
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
  busId: history?.state?.id || 1,
  cabinetroomId: history?.state?.roomId || 1,
  devKey : history?.state?.devKey as string | undefined,
})
const queryParamsSearch = reactive({
  id: history?.state?.busId as number | undefined,
  devKey : history?.state?.devKey as string | undefined,
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

const devKeyList = ref([])
const loadAll = async () => {
  //debugger
  var data = await BusPowerLoadDetailApi.getBusdevKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  console.log(objectArray)
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const getBusIdAndLocation =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getBusIdAndLocation(queryParams);
    if (data != null){
      queryParams.busId = data.busId
      location.value = data.location
      busName.value = data.busName
    }else{
      location.value = null
      busName.value = null
    }
 } finally {
 }
}

watch(() => queryParams.cabinetroomId, (val) => {
  machineList.value = handleNavList(val)
  if (machineList.value.length == 0) {
    queryParams.busId = null
    return
  }
  const defaultValue = machineList.value[0] as any
  queryParams.busId = defaultValue.id
})

watch(() => queryParams.busId,(val) => {
  console.log('wwwwwwwwwww', val)
  getActivePowTrend()
  getMachineEleChain()
  getMachineEleTrend('DAY')
})

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  if (res.length > 0) {
    roomList.value = res
    machineList.value = handleNavList(queryParams.cabinetroomId)
    console.log('接口获取机房导航列表', res)
  }
}
const handleNavList = (cabinetroomId) => {
  const data = roomList.value as any
  const roomIndex = data.findIndex(item => item.id == cabinetroomId)
  let list = [] as any
  if (data[roomIndex].children.length > 0 && data[roomIndex].children[0].type == 2) {
    data[roomIndex].children.forEach(child => {
      if (child.children.length > 0)  {
        child.children.forEach(grandChild => {
          list.push(grandChild)
        })
      }
    })
  } else if(data[roomIndex].children.length > 0 && data[roomIndex].children[0].type == 3) {
    list = data[roomIndex].children.map(item => item)
  }
  return list
}

// 获取机柜有功功率趋势
const getActivePowTrend = async() => {
  const res = await BusEnergyApi.getActivePowTrend({id:queryParams.busId})//id
  Object.assign(ActivePowTrend, res)
  console.log('获取机柜有功功率趋势------', res.yesterdayList?.map(item => item?.dateTime?.split(' ')[1]))
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
        data: res.yesterdayList?.length > 0 
          ? res.yesterdayList?.map(item => item?.dateTime?.split(' ')[1].split(':').slice(0, 2).join(':')) 
          : res.todayList?.map(item => item?.dateTime?.split(' ')[1].split(':').slice(0, 2).join(':'))
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
              name: '昨日有功功率平均值线',
            },
          ]
        },
        emphasis: {
          focus: 'series'
        },
        data: res.yesterdayList?.map(item => item?.activePow)
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
          data: [{ type: 'average', name: '当日有功功率平均值线' }]
        },
        data: res.todayList?.map(item => item?.activePow)
      }
    ]
  }
  console.log('获取机柜有功功率趋势', res)
}
// 获取机柜用能环比
const getMachineEleChain = async() => {
  const res = await BusEnergyApi.getEleChain({id:queryParams.busId})//id
  Object.assign(EleChain, res)
  console.log('获取机柜用能环比', EleChain)
}
// 获取机柜能耗趋势
const getMachineEleTrend = async(type) => {
  try {
    EleTrendLoading.value = true
    const res = await BusEnergyApi.getEleTrend({ id: queryParams.busId, type })//id type
    echarsOptionEleTrend.value ={
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
    console.log('获取机柜能耗趋势', res)
  } finally {
    EleTrendLoading.value = false
    radioBtn.value = type
  }
  
}

/** 搜索按钮操作 */
const handleQuery = async () => {
  queryParams.devKey = queryParamsSearch.devKey;
  await getBusIdAndLocation();

}

onMounted(async () => {
  devKeyList.value = await loadAll();
})

onBeforeMount(() => {
  //getNavList()
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
    margin-top:10px;
    .card-hb {
      width: 40%;
      margin-left: 10px;
      margin-right: 5px;
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
      margin-right: 10px;
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
.header_app{
  background-color: white;
  display: flex;
  height: 50px;
  padding-left: 10px;
  box-shadow: 20px;
}
.header_app_text{                     
  background-color: white;
  width: 100%;
  align-content: center;
  color:#606266;
}                                                       
.header_app_text_other{
  align-content: center;
  background-color: white;
  margin-right: 5px;
}
.header_app_text_other1{
  align-content: center;
  background-color: white;

}
</style>
