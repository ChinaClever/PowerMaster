<template>
  <div class="descriptionContainer">
    <div class="all-left">
      <el-card class="card" shadow="never">
        <template #header>
          <CardTitle title="详细信息" />
        </template>
        <el-descriptions title="总参数" class="ABRord" direction="horizontal" :column="2" >
          <el-descriptions-item label="总负载率：">35%</el-descriptions-item>
          <el-descriptions-item label="视在功率：">190.000kVA</el-descriptions-item>
          <el-descriptions-item label="有功功率：">192.100kW</el-descriptions-item>
          <el-descriptions-item label="无功功率：">8.996kVar</el-descriptions-item>
          <el-descriptions-item label="功率因素：">无</el-descriptions-item>
          <el-descriptions-item label="电力容量：">52kVA</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-card class="card" shadow="never">
        <template #header>
          <CardTitle title="A路" />
        </template>
        <el-descriptions class="ABRord" direction="vertical" :column="2" >
          <el-descriptions-item label="视在功率">200.000kVA</el-descriptions-item>
          <el-descriptions-item label="有功功率">201.000kW</el-descriptions-item>
          <el-descriptions-item label="无功功率">10.112kVar</el-descriptions-item>
          <el-descriptions-item label="A路占比">45%</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-card class="card" shadow="never">
        <template #header>
          <CardTitle title="B路" />
        </template>
        <el-descriptions class="ABRord" direction="vertical" :column="2" >
          <el-descriptions-item label="视在功率">200.000kVA</el-descriptions-item>
          <el-descriptions-item label="有功功率">201.000kW</el-descriptions-item>
          <el-descriptions-item label="无功功率">10.112kVar</el-descriptions-item>
          <el-descriptions-item label="B路占比：">55%</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    <div class="all-right">
      <el-card class="card" shadow="never">
        <!-- <template #header>
          <CardTitle title="拓扑展示" />
        </template> -->
        <el-tabs v-model="activeName" class="tabs-container">
          <el-tab-pane label="A路" name="first">
            <div class="title">
              <span>所在位置：机房-机柜-A </span>
              <span>网络位置：192.168.1.93-0</span>
            </div>
            <el-button class="btn-jump" @click="toPDU" plain>PDU</el-button>
            <el-table v-if="APDUData.length > 0" :data="APDUData" max-height="160">
              <el-table-column label="相" width="100" align="center" >
                <template #default="scope">
                  <div>L{{scope.$index + 1}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('vol_value')" prop="vol_value" label="电压" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.vol_value.toFixed(1)}}V</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('cur_value')" prop="cur_value" label="电流" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.cur_value.toFixed(2)}}A</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('pow_value')" prop="pow_value" label="有功功率" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pow_value.toFixed(3)}}kW</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('pf')" prop="pf" label="功率因素" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pf.toFixed(2)}}</div>
                </template>
              </el-table-column>
            </el-table>
            <el-table v-if="CPDUData.length > 0" :data="CPDUData" max-height="160">
              <el-table-column label="回路" width="100" align="center" >
                <template #default="scope">
                  <div>C{{scope.$index + 1}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('vol_value')" prop="vol_value" label="电压" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.vol_value.toFixed(1)}}V</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('cur_value')" prop="cur_value" label="电流" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.cur_value.toFixed(2)}}A</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('pow_value')" prop="pow_value" label="有功功率" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pow_value.toFixed(3)}}kW</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('pf')" prop="pf" label="功率因素" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pf.toFixed(2)}}</div>
                </template>
              </el-table-column>
            </el-table>
            <el-table v-if="SPDUData.length > 0" :data="SPDUData" max-height="160" style="width: 500px">
              <el-table-column label="输出位" width="100" align="center" >
                <template #default="scope">
                  <div>{{scope.$index + 1}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('relay_state')" prop="relay_state" label="开关" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.relay_state == 1 ? '开' : '关'}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('cur_value')" prop="cur_value" label="电流" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.cur_value.toFixed(2)}}A</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('pow_value')" prop="pow_value" label="有功功率" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pow_value.toFixed(3)}}kW</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('pf')" prop="pf" label="功率因素" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pf.toFixed(2)}}</div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="B路" name="second">
            <div class="title">
              <span>所在位置：机房-机柜-B</span>
              <span>网络位置：192.168.1.165-0</span>
            </div>
            <el-table v-if="APDUData.length > 0" :data="APDUData" max-height="160">
              <el-table-column label="相" width="100" align="center" >
                <template #default="scope">
                  <div>L{{scope.$index + 1}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('vol_value')" prop="vol_value" label="电压" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.vol_value.toFixed(1)}}V</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('cur_value')" prop="cur_value" label="电流" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.cur_value.toFixed(2)}}A</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('pow_value')" prop="pow_value" label="有功功率" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pow_value.toFixed(3)}}kW</div>
                </template>
              </el-table-column>
              <el-table-column v-if="APDUParam.includes('pf')" prop="pf" label="功率因素" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pf.toFixed(2)}}</div>
                </template>
              </el-table-column>
            </el-table>
            <el-table v-if="CPDUData.length > 0" :data="CPDUData" max-height="160">
              <el-table-column label="回路" width="100" align="center" >
                <template #default="scope">
                  <div>C{{scope.$index + 1}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('vol_value')" prop="vol_value" label="电压" width="100" align="center">
                <template #default="scope">
                  <div :class="scope.row.vol_alarm_status == undefined ? '' : alarmClass[scope.row.vol_alarm_status]">{{scope.row.vol_value.toFixed(1)}}V</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('cur_value')" prop="cur_value" label="电流" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.cur_value.toFixed(2)}}A</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('pow_value')" prop="pow_value" label="有功功率" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pow_value.toFixed(3)}}kW</div>
                </template>
              </el-table-column>
              <el-table-column v-if="CPDUParam.includes('pf')" prop="pf" label="功率因素" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pf.toFixed(2)}}</div>
                </template>
              </el-table-column>
            </el-table>
            <el-table v-if="SPDUData.length > 0" :data="SPDUData" max-height="160" style="width: 500px">
              <el-table-column label="输出位" width="100" align="center" >
                <template #default="scope">
                  <div>{{scope.$index + 1}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('relay_state')" prop="relay_state" label="开关" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.relay_state == 1 ? '开' : '关'}}</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('cur_value')" prop="cur_value" label="电流" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.cur_value.toFixed(2)}}A</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('pow_value')" prop="pow_value" label="有功功率" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pow_value.toFixed(3)}}kW</div>
                </template>
              </el-table-column>
              <el-table-column v-if="SPDUParam.includes('pf')" prop="pf" label="功率因素" width="100" align="center">
                <template #default="scope">
                  <div>{{scope.row.pf.toFixed(2)}}</div>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="U位" name="third" />
          <el-tab-pane label="负载" name="fouth">
            <Echart :height="500" :options="loadOption" />
          </el-tab-pane>
          <TopologyShow v-if="activeName == 'third'" />
        </el-tabs>
      </el-card>
    </div>
  </div>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="实时曲线" />
    </template>
    <Echart :height="500" :options="echartsOption" />
    <!-- <div id="cabinetDetail" ></div> -->
  </el-card>
  <el-card class="card" shadow="never">
    <template #header>
      <CardTitle title="智能分析" />
    </template>
    <div class="powerContainer">
      <div class="power">
        <div class="label">有功功率：</div>
        <div class="progressContainer">
          <div class="progress">
            <div class="left" :style="`flex: ${33}`">{{33}}%</div>
            <div class="line"></div>
            <div class="right" :style="`flex: ${100 - 33}`">{{100 - 33}}%</div>
          </div>
        </div>
      </div>
      <div class="power">
        <div class="label">视在功率：</div>
        <div class="progressContainer">
          <div class="progress">
            <div class="left" :style="`flex: ${58}`">{{58}}%</div>
            <div class="line"></div>
            <div class="right" :style="`flex: ${100 - 58}`">{{100 - 58}}%</div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="false" class="triphase">
      <div class="round1 commonRound"></div>
      <div class="round2 commonRound"></div>
      <div class="round3 commonRound"></div>
      <div class="round4 commonRound"></div>
      <div class="round5 commonRound"></div>
      <div class="line1 commonLine">
        <div class="lineSon"></div>
      </div>
      <div class="line2 commonLine">
        <div class="lineSon"></div>
      </div>
      <div class="line3 commonLine">
        <div class="lineSon"></div>
      </div>
    </div>
    <div class="triphaseContainer">
      <div><Echart :height="500" :options="triphaseAOption" /></div>
      <div><Echart :height="500" :options="triphaseBOption" /></div>
    </div>
    
  </el-card>
</template>

<script lang="ts" setup>
import TopologyShow from "./component/TopologyShow.vue"
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import { EChartsOption } from 'echarts'


const APDUData = ref<any>([])
const APDUTriphase = ref([])
const APDUTriphaseMax = ref(0)
const APDUParam = ref<string[]>([])
const CPDUData = ref([])
const CPDUParam = ref<string[]>([])
const SPDUData = ref([])
const SPDUParam = ref<string[]>([])
const { push } = useRouter() // 路由跳转
const activeName = ref('first')
const alarmClass = {
  0: 'normal',
  1: 'error',
  2: 'warn',
  4: 'warn',
  8: 'error',
}
const toPDU = () => {
  push('/pdu/pdudisplayscreen')
}

const echartsOption = reactive<EChartsOption>({
  title: {
    text: '机柜列实时功率走势'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {},
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00']
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} W'
    }
  },
  series: [
    {
      name: 'sourceA',
      type: 'line',
      data: [200, 110, 330, 410, 420, 320, 390],
      markPoint: {
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name: 'Min' }
        ]
      },
    },
    {
      name: 'sourceB',
      type: 'line',
      data: [300, 360, 230, 300, 320, 280, 290],
      markPoint: {
        // data: [{ name: '周最低', value: -2, xAxis: 1, yAxis: -1.5 }]
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name: 'Min' }
        ]
      },
    }
  ]
}) as EChartsOption

const loadOption = reactive<EChartsOption>({
  legend: {
    data: ['视在功率', '有功功率']
  },
  radar: {
    // shape: 'circle',
    indicator: [
      { name: '服务器1', max: 6500 },
      { name: '服务器2', max: 16000 },
      { name: '服务器3', max: 30000 },
      { name: '服务器4', max: 38000 },
      { name: '服务器5', max: 52000 },
      { name: '服务器6', max: 25000 },
      { name: '服务器7', max: 6500 },
      { name: '服务器8', max: 16000 },
      { name: '服务器9', max: 30000 },
      { name: '服务器10', max: 38000 },
      { name: '服务器11', max: 52000 },
      { name: '服务器12', max: 25000 }
    ]
  },
  series: [
    {
      name: 'Budget vs spending',
      type: 'radar',
      data: [
        {
          value: [4200, 8000, 20000, 35000, 50000, 18000, 4200, 13000, 20000, 35000, 55000, 21000],
          name: '视在功率'
        },
        {
          value: [5000, 14000, 28000, 26000, 42000, 21000, 4000, 14500, 28000, 26500, 42000, 21000],
          name: '有功功率'
        }
      ]
    }
  ]
})

const triphaseAOption = reactive<EChartsOption>({}) as EChartsOption

const triphaseBOption = reactive<EChartsOption>({}) as EChartsOption

const regulateData = (data) => {
  const lineKeys = Object.keys(data)
  const result = [] as any
  if (lineKeys.length > 0) {
    for (let i = 0; i < data[lineKeys[0]].length; i++) {
      const obj = {}
      lineKeys.forEach(key => {
        obj[key] = data[key][i]
      })
      result.push(obj)
    }
  }
  return {
    data: result,
    keys: lineKeys
  }
}

const getData =  async() => {
  const res = await PDUDeviceApi.PDUDisplay({
    devKey: '192.168.1.93-0'
  })
  console.log('res', res)
  if (res && res.pdu_data) {
    APDUData.value = regulateData(res.pdu_data.line_item_list || []).data
    APDUParam.value = Object.keys(res.pdu_data.line_item_list || [])
    APDUTriphase.value = APDUData.value.map(item => {
      if (item.cur_alarm_max > APDUTriphaseMax.value) APDUTriphaseMax.value = item.cur_alarm_max
      return item.cur_value
    })
    Object.assign(triphaseAOption, {
      title: [
        {
          text: 'A路电流',
          left: 'center',
          top: 'bottom'
        }
      ],
      polar: {
        radius: [10, '80%']
      },
      radiusAxis: {
        max: APDUTriphaseMax.value,
        // axisLine: false
      },
      angleAxis: {
        type: 'category',
        data: ['a','a', 'a','b', 'b', 'b','c', 'c', 'c'],
        startAngle: 110,
        axisLabel: false,
        axisTick: false
      },
      tooltip: {},
      series: {
        type: 'bar',
        data: ['32.25', , , '20.20', , ,'29.90'],
        coordinateSystem: 'polar',
        label: {
          show: true,
          position: 'middle',
          formatter: '{c}A'
        }
      },
      animation: false
    })
    Object.assign(triphaseBOption, {
      title: [
        {
          text: 'B路电流',
          left: 'center',
          top: 'bottom'
        }
      ],
      polar: {
        radius: [10, '80%']
      },
      radiusAxis: {
        max: APDUTriphaseMax.value,
        // axisLine: false
      },
      angleAxis: {
        type: 'category',
        data: ['a','a', 'a','b', 'b', 'b','c', 'c', 'c'],
        startAngle: 110,
        axisLabel: false,
        axisTick: false
      },
      tooltip: {},
      series: {
        type: 'bar',
        data: ['32.00', , , '22.00', , ,'28.00'],
        coordinateSystem: 'polar',
        label: {
          show: true,
          position: 'middle',
          formatter: '{c}A'
        }
      },
      animation: false
    })
    CPDUData.value = regulateData(res.pdu_data.loop_item_list || []).data
    CPDUParam.value = Object.keys(res.pdu_data.loop_item_list || [])
    SPDUData.value = regulateData(res.pdu_data.output_item_list || []).data
    SPDUParam.value = Object.keys(res.pdu_data.output_item_list || [])
  }
  console.log('APDUParam', APDUData.value, APDUTriphase.value)
}

getData()

</script>

<style lang="scss" scoped>
.descriptionContainer {
  display: flex;
  justify-content: space-between;
  align-items: stretch;
  margin-bottom: 16px;
  .all-left {
    width: calc(60% - 16px);
    box-sizing: border-box;
    margin-right: 15xpx;
    .card {
      // height: 100%;
    }
  }
  .all-right {
    width: 40%;
    color: #606060;
    box-sizing: border-box;
    .card {
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
    }
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
:deep(.el-table__row .el-table__cell) {
  border: none;
}
:deep(.el-table__header-wrapper .el-table__header .el-table__cell) {
  border: none;
}
:deep(.el-table) :before {
  height: 0;
}
</style>