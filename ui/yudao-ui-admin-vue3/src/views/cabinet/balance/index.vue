<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜平衡" >
    <template #NavInfo>
      <div class="navInfo">
        <div class="line"></div>
        <div class="header">
          <div class="header_img">
            <div class="progress">
              <div class="left" :style="{flex: 1}">50%</div>
              <div class="progress-line"></div>
              <div class="right" :style="{flex: 1}">50%</div>
            </div>
          </div>
        </div>
        <div class="line"></div>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <div>
          <el-form-item label="公司名称" prop="username">
            <el-input
              v-model="queryParams.company"
              placeholder="请输入公司名称"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" @click="getTableData(true)"><Icon icon="ep:search" />搜索</el-button>
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;handleSwitchModal(0)" :type="switchValue==0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />AB路占比</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(1)" :type="switchValue==1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="switchValue == 0 && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <!-- 电流 -->
            <div class="progressContainer">
              <div style="margin-right:15px;margin-left:-15px;">
                <div>总视在功率：{{item.powApparentTotal || '0.00'}}kVA</div>
                <div>A路视在功率：{{item.powApparentA || '0.00'}}kVA</div>
                <div>B路视在功率：{{item.powApparentB || '0.00'}}kVA</div>
              </div>
              <div class="progress">
                <div class="left" :style="`flex: ${item.apow || '0.00'}`">{{item.apow || '0.00'}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${item.bpow || '0.00'}`">{{item.bpow || '0.00'}}%</div>
                <div class="tip">
                  <span>A路</span>
                  <span>B路</span>
                </div>
              </div>
              <!-- <div class="progress" v-else>
                <div class="left" :style="`flex: ${item.apow || '0.00'}`">{{item.apow || '0.00'}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex:  ${item.bpow || '0.00'}`">{{item.bpow || '0.00'}}%</div>
                <div class="tip">
                  <span>A路</span>
                  <span>B路</span>
                </div>
              </div> -->
            </div>
            <!-- 功率 -->
            <!-- <div class="progressContainer">
              <div class="text">功率占比：</div>
              <div class="progress" v-if="item.abglzb">
                <div class="left" :style="`flex: ${item.abglzb}`">{{item.abglzb}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - item.abglzb}`">{{100 - item.abglzb}}%</div>
              </div>
              <div class="progress" v-else>
                <div class="left" :style="`flex: 50`">null</div>
                <div class="line"></div>
                <div class="right" :style="`flex: 50`">null</div>
              </div>
              <div class="tip">
                <span>A路</span>
                B路
              </div>
            </div> -->
            <!-- 电流 -->
            <!-- <div class="content" v-if="switchValue == 1">
              <div class="road">A路</div>
              <div class="valueList">
                <div>Ia：{{item.Ia0 || '0.00'}}A</div>
                <div>Ia：{{item.Ia1 || '0.00'}}A</div>
                <div>Ia：{{item.Ia2 || '0.00'}}A</div>
              </div>
              <div class="road">B路</div>
              <div class="valueList">
                <div>Ia：{{item.Ib0 || '0.00'}}A</div>
                <div>Ia：{{item.Ib1 || '0.00'}}A</div>
                <div>Ia：{{item.Ib2 || '0.00'}}A</div>
              </div>
            </div> -->
            <!-- 功率 -->
            <!-- <div class="content" v-if="switchValue == 1">
              <div class="road">A路</div>
              <div class="valueList">
                <div>Pa：{{item.Pa0 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pa1 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pa2 || '0.00'}}Kw</div>
              </div>
              <div class="road">B路</div>
              <div class="valueList">
                <div>Pa：{{item.Pb0 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pb1 || '0.00'}}Kw</div>
                <div>Pa：{{item.Pb2 || '0.00'}}Kw</div>
              </div>
            </div> -->
            <div class="room">{{item.roomName}}-{{item.cabinetName}}</div>
            <button v-if="!item.pduBox && (item.apow != null || item.bpow != null)" class="detail" @click.prevent="showDialog(item)" >详情</button>
          </div>
        </div>

        <el-dialog v-model="dialogVisibleCur" @close="handleClose">
          <!-- 自定义的头部内容（可选） -->
          <template #header>
            <CardTitle title="AB占比情况" />
            <div class="powerContainer" v-if="balanceObj.pow_active_percent > 0">
              <div class="power">
                <div class="label">有功功率：</div>
                <div class="progressContainer">
                  <div class="progress">
                    <div class="left" :style="`flex: ${balanceObj.pow_active_percent}`">{{balanceObj.pow_active_percent.toFixed(2)}}%</div>
                    <div class="line"></div>
                    <div class="right" :style="`flex: ${100 - balanceObj.pow_active_percent}`">{{100 - balanceObj.pow_active_percent.toFixed(2)}}%</div>
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
          </template>
          <!-- 自定义的主要内容 -->
          <div class="custom-content">
            <CardTitle title="A路电流不平衡" />
            <div class="custom-content-container" v-if="apow !== null">
              <el-card class="cardChilc" shadow="hover">
                <curUnblance :max="balanceObj.imbalanceValueA"/>
              </el-card>
              <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
                <div class="IechartBar">
                  <Echart :options="ABarOption" :height="300" />
                </div>
              </el-card>
              <el-card  class="cardChilc" shadow="hover">
                <div class="IechartBar">
                  <Echart :options="ALineOption" :height="300"/>
                </div>
              </el-card>
            </div>

            <CardTitle title="B路电流不平衡" />
            <div class="custom-content-container" v-if="bpow !== null">
              <el-card class="cardChilc" shadow="hover">
                <volUnblance :max="balanceObj.imbalanceValueB"/>
              </el-card>
              <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
                <div class="IechartBar">
                  <Echart :options="BBarOption" :height="300"/>
                </div>
              </el-card>
              <el-card  class="cardChilc" shadow="hover">
                <div class="IechartBar">
                  <Echart :options="BLineOption" :height="300"/>
                </div>
              </el-card>
            </div>
          </div>
        </el-dialog>

        <el-table v-if="switchValue == 1" style="width: 100%;" :data="tableData" >
          <el-table-column type="index" width="60" label="序号" align="center" />
          <el-table-column label="所在位置" min-width="90" align="center" prop="roomName" />
          <el-table-column label="总视在功率(kVA)" min-width="90" align="center" prop="powApparentTotal" />
          <el-table-column label="A路视在功率(kVA)" min-width="90" align="center" prop="powApparentA" />
          <el-table-column label="B路视在功率(kVA)" min-width="90" align="center" prop="powApparentB" />
          <el-table-column label="A路占比" min-width="90" align="center" prop="aPow" />
          <el-table-column label="B路占比" min-width="90" align="center" prop="bPow" />
          <el-table-column  label="操作" width="100px" align="center">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="showDialog(scope.row)"
                style="background-color:#409EFF;color:#fff;border:none;width:65px;height:30px;"
              >
              设备详情
              </el-button>
            </template>
          </el-table-column>
        
          <!-- <el-table-column label="操作" width="100px">
          <template #default="scope">
            <el-button size="small" link @click="openListenerFieldForm(scope.row, scope.$index)">详情</el-button>
          </template>
        </el-table-column> -->
          <!-- <el-table-column label="A路" align="center">
            <el-table-column label="I1(A)" min-width="90" align="center" prop="acurValueOne" />
            <el-table-column label="I2(A)" min-width="90" align="center" prop="acurValueTwe" />
            <el-table-column label="I3(A)" min-width="90" align="center" prop="acurValueThree" />
            <el-table-column label="P1(kW)" min-width="90" align="center" prop="bpowValueOne" />
            <el-table-column label="P2(kW)" min-width="90" align="center" prop="bpowValueTwe" />
            <el-table-column label="P3(kW)" min-width="90" align="center" prop="bpowValueThree" />
          </el-table-column>
          <el-table-column label="B路"  align="center">
            <el-table-column label="I1(A)" min-width="90" align="center" prop="bcurValueOne" />
            <el-table-column label="I2(A)" min-width="90" align="center" prop="bcurValueTwe" />
            <el-table-column label="I3(A)" min-width="90" align="center" prop="bcurValueThree" />
            <el-table-column label="P1(kW)" min-width="90" align="center" prop="bpowValueOne" />
            <el-table-column label="P2(kW)" min-width="90" align="center" prop="bpowValueTwe" />
            <el-table-column label="P3(kW)" min-width="90" align="center" prop="bpowValueThree" />
          </el-table-column> -->
        </el-table>
        <Pagination
          :total="queryParams.pageTotal"
          :page-size-arr="pageSizeArr"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData(false)"
        />
        <template v-if="tableData.length == 0 && switchValue == 0">
          <el-empty description="暂无数据" :image-size="300" />
        </template>
      </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts';
import { CabinetApi } from '@/api/cabinet/info';
import { PDUDeviceApi } from '@/api/pdu/pdudevice';
import curUnblance from './component/curUnblance.vue';
import volUnblance from './component/volUnblance.vue';

const cabinetId = ref();

const { push } = useRouter(); // 路由跳转
const router = useRouter(); // 路由跳转
const tableLoading = ref(false); // 
const isFirst = ref(true); // 是否第一次调用getTableData函数
const navList = ref([]); // 左侧导航栏树结构列表
const tableData = ref([]);
const switchValue = ref(0); // 表格(1) 矩阵(0)切换
const cabinetIds = ref<number[]>([]); // 左侧导航菜单所选id数组
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})

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
  color: '#fa3333', //红色
}]

const pageSizeArr = ref([24,36,48,96]);
const ABarOption = ref<EChartsOption>({});
const BBarOption = ref<EChartsOption>({});

const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  cur_valueB: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0,
})

const getBalanceDetail = async(item) => {
  const res = await CabinetApi.getDetail({id:item});
  if (res.cabinet_power.path_a && res.cabinet_power.path_b) {
    if (res.cabinet_power.path_a.pow_apparent == 0) balanceObj.pow_apparent_percent = 0
    else balanceObj.pow_apparent_percent = (res.cabinet_power.path_a.pow_apparent / res.cabinet_power.total_data.pow_apparent as any).toFixed(2) * 100
    if (res.cabinet_power.path_a.pow_active == 0) balanceObj.pow_active_percent = 0
    else balanceObj.pow_active_percent = (res.cabinet_power.path_a.pow_active / res.cabinet_power.total_data.pow_active as any).toFixed(2) * 100
  }
  if (res.cabinet_power.path_a) {
    const cur_valueA = res.cabinet_power.path_a.cur_value.map(item => item.toFixed(2));
    ABarOption.value = {
      title: {
        text: 'A路电流饼形图',
        left: 'left'
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
  if (res.cabinet_power.path_b) {
    const cur_valueB = res.cabinet_power.path_b.cur_value.map(item => item.toFixed(2));
    BBarOption.value = {
      title: {
        text: 'B路电流饼形图',
        left: 'left'
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
            { value: cur_valueB[0], name: 'A相电流', itemStyle: { color: '#075F71' } },
            { value: cur_valueB[1], name: 'B相电流', itemStyle: { color: '#119CB5' } },
            { value: cur_valueB[2], name: 'C相电流', itemStyle: { color: '#45C0C9' } },
          ]
        }
      ]
    }
  }

  balanceObj.imbalanceValueA = res.curUnbalance;
  balanceObj.imbalanceValueB = res.volUnbalance;
  balanceObj.colorIndex = res.color - 1;
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 接口获取机柜列表
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await CabinetApi.getCabinetBalance({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      // roomId: null,
      runStatus: [],
      pduBox: 0,
      company: queryParams.company
    })
    if (res.list) {
      // const list = res.list.map(item => {
      //   const tableItem = {} as any
      //   tableItem.local = item.room_name + '-' + item.cabinet_name
      //   tableItem.id = item.cabinet_key.split('-')[1]
      //   const PathA = item.cabinet_power.path_a
      //   const PathB = item.cabinet_power.path_b
      //   if (PathA && PathA.cur_value.length > 0) { // A路电流
      //   console.log('PathA.cur_value', PathA.cur_value)
      //     PathA.cur_value.forEach((item, index) => {
      //       tableItem['Ia'+index] = item.toFixed(2)
      //     })
      //   }
      //   if (PathA && PathA.vol_value && PathA.vol_value.length > 0) { // A路电压
      //     PathA.vol_value.forEach((item, index) => {
      //       tableItem['Ua'+index] = item.toFixed(2)
      //     })
      //   }
      //   if (PathA && PathA.pow_value && PathA.pow_value.length > 0) { // A路功率
      //     PathA.pow_value.forEach((item, index) => {
      //       tableItem['Pa'+index] = item.toFixed(2)
      //     })
      //   }
      //   if (PathB && PathB.cur_value && PathB.cur_value.length > 0) { // B路电流
      //     PathB.cur_value.forEach((item, index) => {
      //       tableItem['Ib'+index] = item.toFixed(2)
      //     })
      //   }
      //   if (PathB && PathB.vol_value && PathB.vol_value.length > 0) { // B路电压
      //     PathB.vol_value.forEach((item, index) => {
      //       tableItem['Ub'+index] = item.toFixed(2)
      //     })
      //   }
      //   if (PathB && PathB.pow_value && PathB.pow_value.length > 0) { // B路功率
      //     PathB.pow_value.forEach((item, index) => {
      //       tableItem['Pb'+index] = item.toFixed(2)
      //     })
      //   }
      //   if (item.cabinet_power.path_a && item.cabinet_power.path_b) {
      //     if (item.cabinet_power.path_a.pow_apparent == 0) tableItem.abdlzb = 0
      //     else tableItem.abdlzb = Math.floor((item.cabinet_power.path_a.pow_apparent / item.cabinet_power.total_data.pow_apparent as any).toFixed(2) * 100)
      //     if (item.cabinet_power.path_a.pow_active == 0) tableItem.abglzb = 0
      //     else tableItem.abglzb = Math.floor((item.cabinet_power.path_a.pow_active / item.cabinet_power.total_data.pow_active as any).toFixed(2) * 100)
      //   }
      //   return tableItem
      // })
      tableData.value = res.list
      queryParams.pageTotal = res.total
    }
  } finally {
    tableLoading.value = false
  }
}

// 获取平衡度
const getBalanceDegree = async () => {
  const res = await PDUDeviceApi.getPDUDevicePage({
    pageNo: 1,
    pageSize: 24,
    cabinetIds : [cabinetId.value],
  })
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
  const res = await CabinetApi.getBalanceTrend({
    id: cabinetId.value
  })
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
          data: res.map(item => item.curA[0].curValue.toFixed(2)),
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
          data: res.map(item => item.curA[0].curValue.toFixed(2)),
        },
        {
          name: 'A2',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[1].curValue.toFixed(2)),
        },
        {
          name: 'A3',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curA[2].curValue.toFixed(2)),
        },
      ]
    }
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
          data: res.map(item => item.curB[0].curValue.toFixed(2)),
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
          data: res.map(item => item.curB[0].curValue.toFixed(2)),
        },
        {
          name: 'B2',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[1].curValue.toFixed(2)),
        },
        {
          name: 'B3',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.curB[2].curValue.toFixed(2)),
        },
      ]
    }
  }
}

const ALineOption = ref<EChartsOption>({
  title: {
    text: 'A路电流趋势',
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
    text: 'B路电流趋势',
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

// 详情跳转
const toDetail = (id) => {

  push({path: '/cabinet/cab/balanceDetail', state: { id }})
}

const colorFlag = ref(0);
const dialogVisibleCur = ref(false);
const curdevkey = ref();
const busName = ref();
const curlocation = ref();
const apow = ref(null);
const bpow = ref(null);

const showDialog = async (item) => {
  cabinetId.value = item.id || 1;
  colorFlag.value = item.color;
  dialogVisibleCur.value = true;
  curdevkey.value = item.devKey;
  busName.value = item.busName;
  curlocation.value = item.location;
  apow.value = item.apow;
  bpow.value = item.bpow;
  await getBalanceDetail(item.id);
  await getBalanceDegree();
  await getBalanceTrend();
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value == 0) { // 阵列
    queryParams.pageSize = 24
  } else {
    queryParams.pageSize = 15
  }
  getTableData(true)
}

// 处理左侧树导航选择事件
const handleCheck = (row) => {
  isFirst.value = false
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids
  getTableData(true)
}

onBeforeMount(() => {
  getNavList()
  getTableData()
})
</script>

<style lang="scss" scoped>
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
.navInfo {
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
      .progress {
        width: 100px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .progress-line {
          width: 3px;
          height: 25px;
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
.matrixContainer {
  height: calc(100vh - 320px);
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .item {
    width: 25%;
    min-width: 290px;
    height: 120px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    position: relative;
    .progressContainer {
      position: relative;
      height: 50px;
      display: flex;
      justify-content: center;
      align-items: center;
      margin-top: 40px;
      .progress {
        width: 180px;
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #eee;
        box-sizing: border-box;
        position: relative;
        display: flex;
        justify-content: center;
        .tip {
          width: 180px;
          position: absolute;
          top: -12px;
          left: 0;
          display: flex;
          justify-content: space-between;
          color: #000;
          font-size: 12px;
        }
        .line {
          width: 3px;
          height: 25px;
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
    .content {
      font-size: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      & > .valueList:last-of-type {
        margin-right: 0;
      }
      .road {
        margin-right: 10px;
      }
      .valueList {
        margin-right: 40px;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 13px;
    }
    .detail {
      width: 35px;
      height: 20px;
      cursor: pointer;
      font-size: 12px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 10px;
      bottom: 8px;
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

:deep(.el-dialog) {
  width: 80%;
  margin-top: 50px;
  background-color: #f1f1f1;
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
}

.cardChilc {
  flex: 1;
  height: 100%;
}

.IechartBar {
  width: 100%;
  height: 100%;
}
</style>
