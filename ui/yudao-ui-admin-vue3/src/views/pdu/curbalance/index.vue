<template>
  <CommonMenu
    @check="handleCheck"
    @node-click="handleClick"
    :showSearch="true"
    :dataList="navList"
    navTitle="均衡配电"
  >
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/PDU.jpg" /></div>
        </div> -->
        <!-- <div class="line"></div> -->
        <div class="status">
          <div class="box">
            <div class="top"> <div class="tag"></div>{{ statusList[0].name }} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.lessFifteen }}</span
              >个</div
            >
          </div>
          <div class="box">
            <div class="top"> <div class="tag empty"></div>小电流 </div>
            <div class="value"
              ><span class="number">{{ statusNumber.smallCurrent }}</span
              >个</div
            >
          </div>
          <div class="box">
            <div class="top"> <div class="tag warn"></div>{{ statusList[1].name }} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.greaterFifteen }}</span
              >个</div
            >
          </div>
          <div class="box">
            <div class="top"> <div class="tag error"></div>{{ statusList[2].name }} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.greaterThirty }}</span
              >个</div
            >
          </div>
        </div>
        <div class="line"></div>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item v-if="switchValue == 2 || switchValue == 3">
          <template v-for="(status, index) in statusList" :key="index">
            <button
              :class="status.selected ? status.activeClass : status.cssClass"
              @click.prevent="handleSelectStatus(index)"
              >{{ status.name }}</button
            >
          </template>
        </el-form-item>
        <el-button
          v-show="switchValue == 2 || switchValue == 3"
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 平衡度范围颜色
        </el-button>
        <el-form-item>
          <el-form-item label="网络地址" prop="devKey">
            <el-autocomplete
              v-model="queryParams.devKey"
              :fetch-suggestions="querySearch"
              clearable
              class="!w-200px"
              placeholder="请输入网络地址"
              @select="handleQuery"
            />
            <el-form-item :style="{ marginLeft: '10px' }">
              <el-button @click="handleQuery"
                ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button
              >
              <el-button @click="resetQuery"
                ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button
              >
              <el-button
                type="primary"
                plain
                @click="openForm('create')"
                v-hasPermi="['pdu:PDU-device:create']"
              >
                <Icon icon="ep:plus" class="mr-5px" /> 新增
              </el-button>
              <el-button
                type="success"
                plain
                @click="handleExport"
                :loading="exportLoading"
                v-hasPermi="['pdu:PDU-device:export']"
              >
                <Icon icon="ep:download" class="mr-5px" /> 导出
              </el-button>
            </el-form-item>
          </el-form-item>
        </el-form-item>
        <div style="float: right">
          <el-button
            @click="pageSizeArr = [24, 36, 48, 96];queryParams.pageSize = 24;getList();switchValue = 2
            "
            :type="switchValue == 2 ? 'primary' : ''"
            ><Icon icon="ep:grid" style="margin-right: 4px" />电流阵列</el-button
          >
          <el-button
            @click="
              statusList.forEach((item) => (item.selected = true));
              pageSizeArr = [24, 36, 48, 96];
              queryParams.pageSize = 24;
              getList();
              switchValue = 0;
            "
            :type="switchValue == 0 ? 'primary' : ''"
            ><Icon icon="ep:grid" style="margin-right: 4px" />电压阵列</el-button
          >
          <el-button
            @click="
              pageSizeArr = [15, 25, 30, 50, 100];
              queryParams.pageSize = 15;
              getList();
              switchValue = 3;
            "
            :type="switchValue == 3 ? 'primary' : ''"
            ><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button
          >
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table
        v-show="switchValue == 3"
        v-loading="loading"
        :data="list"
        :stripe="true"
        :show-overflow-tooltip="true"
        @cell-dblclick="toPDUDisplayScreen"
      >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column
          label="网络地址"
          align="center"
          prop="devKey"
          :class-name="ip"
          width="125px"
        />
        <el-table-column label="运行状态" align="center" prop="color" width="120px">
          <template #default="scope">
            <el-tag type="info" v-if="scope.row.color == 1">小电流不平衡</el-tag>
            <el-tag type="success" v-if="scope.row.color == 2">大电流不平衡</el-tag>
            <el-tag type="warning" v-if="scope.row.color == 3">大电流不平衡</el-tag>
            <el-tag type="danger" v-if="scope.row.color == 4">大电流不平衡</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="电流(A)" align="center">
          <el-table-column label="A相" align="center" prop="acur" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.acur != null">
                {{ scope.row.acur }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相" align="center" prop="bcur" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.bcur != null">
                {{ scope.row.bcur }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相" align="center" prop="ccur" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.ccur != null">
                {{ scope.row.ccur }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="不平衡度(%)" align="center" prop="curUnbalance" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.curUnbalance != null">
                {{ scope.row.curUnbalance }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="电压(V)" align="center">
          <el-table-column label="A相" align="center" prop="avol" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.avol">
                {{ scope.row.avol }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相" align="center" prop="bvol" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.bvol">
                {{ scope.row.bvol }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相" align="center" prop="cvol" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.cvol">
                {{ scope.row.cvol }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="不平衡度(%)" align="center" prop="volUnbalance" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.volUnbalance != null">
                {{ scope.row.volUnbalance }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="95px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toPDUDisplayScreen(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 5"
            >
              设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.devKey)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-show="switchValue == 2 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info">
              <div v-if="item.acur != null">A相电流：{{ item.acur }}A</div>
              <div v-if="item.bcur != null">B相电流：{{ item.bcur }}A</div>
              <div v-if="item.ccur != null">C相电流：{{ item.ccur }}A</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div class="icon">
              <div v-if="item.curUnbalance != null">
                <span style="font-size: 20px">{{ item.curUnbalance }}%</span><br />不平衡度
              </div>
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" v-if="item.color != 0">
            <el-tag type="info" v-if="item.color == 1">小电流不平衡</el-tag>
            <el-tag type="success" v-if="item.color == 2">大电流不平衡</el-tag>
            <el-tag type="warning" v-if="item.color == 3">大电流不平衡</el-tag>
            <el-tag type="danger" v-if="item.color == 4">大电流不平衡</el-tag>
          </div>
          <div class="status" v-if="item.ccur == null && item.status != 5">
            <el-tag type="info">单相设备</el-tag>
          </div>
          <div class="status" v-if="item.status == 5">
            <el-tag type="info">离线</el-tag>
          </div>
          <button
            v-if="item.status != null && item.status != 5"
            class="detail"
            @click="showDialogCur(item)"
            >详情</button
          >
        </div>
      </div>

      <el-dialog v-model="dialogVisibleCur" @close="handleClose">
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <CardTitle title="电流不平衡" />
          <span style="margin-left: 1vw"
            ><el-tag size="large">{{ curlocation }}</el-tag></span
          >
        </template>
        <!-- 自定义的主要内容 -->
        <div class="custom-content">
          <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
            <div class="IechartBar">
              <Echart :options="ABarOption" :height="300" />
            </div>
          </el-card>
          <el-card class="cardChilc" shadow="hover">
            <div class="IechartBar">
              <Echart :options="ALineOption" :height="300" />
            </div>
          </el-card>
          <el-card class="cardChilc" shadow="hover">
            <div class="box" :style="{ borderColor: colorList[balanceObj.colorIndex].color }">
              <div class="value">{{ balanceObj.imbalanceValueA }}%</div>
              <div
                class="day"
                :style="{ backgroundColor: colorList[balanceObj.colorIndex].color }"
                >{{ colorList[balanceObj.colorIndex].name }}</div
              >
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
      </el-dialog>

      <div v-show="switchValue == 0 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info">
              <div v-if="item.avol != null">A相电压：{{ item.avol }}V</div>
              <div v-if="item.bvol != null">B相电压：{{ item.bvol }}V</div>
              <div v-if="item.cvol != null">C相电压：{{ item.cvol }}V</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div class="icon">
              <div v-if="item.volUnbalance != null">
                <span style="font-size: 20px">{{ item.volUnbalance }}%</span><br />不平衡度
              </div>
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status">
            <el-tag type="info">电压不平衡</el-tag>
          </div>
          <button
            v-if="item.status != null && item.status != 5"
            class="detail"
            @click="showDialogVol(item)"
            >详情</button
          >
        </div>
      </div>

      <el-dialog v-model="dialogVisibleVol" @close="handleClose">
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <CardTitle title="电压不平衡" />
          <span style="margin-left: 1vw"
            ><el-tag size="large">{{ vollocation }}</el-tag></span
          >
        </template>
        <!-- 自定义的主要内容 -->
        <div class="custom-content">
          <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
            <div class="IechartBar">
              <Echart :options="BBarOption" :height="300"/>
            </div>
          </el-card>
          <el-card class="cardChilc" shadow="hover">
            <div class="IechartBar">
              <Echart :options="BLineOption" :height="300"/>
            </div>
          </el-card>
          <el-card  class="cardChilc" shadow="hover">
            <div class="box" :style="{borderColor: colorList[balanceObj.colorIndex].color}">
              <div class="value">{{balanceObj.imbalanceValueB}}%</div>
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
      </el-dialog>

      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && switchValue != 3">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" />
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'
import { EChartsOption } from 'echarts'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const dialogVisibleCur = ref(false) //全屏弹窗的显示隐藏
const dialogVisibleVol = ref(false) //全屏弹窗的显示隐藏
const { push } = useRouter()
const navList = ref([]) as any // 左侧导航栏树结构列表
const devKeyList = ref([])
const curBalanceColorForm = ref()
const flashListTimer = ref()
const firstTimerCreate = ref(true)
const pageSizeArr = ref([24, 36, 48, 96])
const switchValue = ref(2)
const statusNumber = reactive({
  lessFifteen: 0,
  greaterFifteen: 0,
  greaterThirty: 0,
  smallCurrent: 0
})

const statusList = reactive([
  {
    name: '<15%',
    selected: true,
    value: 2,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '15%-30%',
    selected: true,
    value: 3,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn'
  },
  {
    name: '>30%',
    selected: true,
    value: 4,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
  {
    name: '小电流',
    selected: true,
    value: 1,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  }
])

const curlocation = ref()
const vollocation = ref()

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
  xAxis: {},
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

const getBalanceDetail = async (item) => {
  const res = await PDUDeviceApi.balanceDetail({ devKey: item.devKey })
  console.log('res', res)
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
        }
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
          data: ['A', 'B', 'C'],
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
          data: cur_valueA
        }
      ]
    }
  }
  if (res.vol_value) {
    const vol_value = res.vol_value
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
  console.log('balanceObj',balanceObj)
}

// 获取pdu电流趋势
const getBalanceTrend = async (item) => {
  const res = await PDUDeviceApi.balanceTrend({
    pduId: item.id
  })
  if (res.length > 0) {
    const timeList = res.map((item) => item.dateTime)
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
          data: res.map((item) => item.cur[0].curValue)
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
          data: res.map((item) => item.cur[0].curValue)
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => item.cur[1].curValue)
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => item.cur[2].curValue)
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
  
  console.log('ALineOption', ALineOption)
  console.log('item', item)
}

const showDialogCur = (item) => {
  dialogVisibleCur .value = true
  curlocation.value = item.devKey
  getBalanceDetail(item)
  getBalanceTrend(item)
}

const showDialogVol = (item) => {
  dialogVisibleVol.value = true
  vollocation.value = item.devKey
  getBalanceDetail(item)
  getBalanceTrend(item)
}

const loadAll = async () => {
  var data = await PDUDeviceApi.devKeyList()
  var objectArray = data.map((str) => {
    return { value: str }
  })
  console.log(objectArray)
  return objectArray
}

const querySearch = (queryString: string, cb: any) => {
  console.log(devKeyList.value)
  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
  }
}

const handleClick = (row) => {
  console.log('click', row)
}

const handleCheck = async (row) => {
  if (row.length == 0) {
    queryParams.cabinetIds = null
    getList()
    return
  }
  const ids = [] as any
  var haveCabinet = false
  row.forEach((item) => {
    if (item.type == 3) {
      ids.push(item.id)
      haveCabinet = true
    }
  })
  if (!haveCabinet) {
    queryParams.cabinetIds = [-1]
  } else {
    queryParams.cabinetIds = ids
  }

  getList()
}

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中
const list = ref([
  {
    id: null,
    status: null,
    apparentPow: null,
    pow: null,
    ele: null,
    devKey: null,
    location: null,
    dataUpdateTime: '',
    pduAlarm: '',
    pf: null,
    acur: null,
    bcur: null,
    ccur: null,
    curUnbalance: null
  }
]) as any // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData: undefined,
  status: [],
  cabinetIds: []
}) as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    var range = await CurbalanceColorApi.getCurbalanceColor()
    if (range != null) {
      statusList[0].name = '<' + range.rangeOne + '%'
      statusList[1].name = range.rangeTwo + '%-' + range.rangeThree + '%'
      statusList[2].name = '>' + range.rangeFour + '%'
    }

    var tableIndex = 0
    var lessFifteen = 0
    var greaterFifteen = 0
    var greaterThirty = 0
    var smallCurrent = 0
    data.list.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex
      if (obj?.dataUpdateTime == null && obj?.pow == null) {
        return
      }
      const splitArray = obj.dataUpdateTime.split(' ')
      obj.dataUpdateTime = splitArray[1]

      obj.apparentPow = obj.apparentPow.toFixed(3)
      obj.pow = obj.pow.toFixed(3)
      obj.ele = obj.ele.toFixed(1)
      obj.pf = obj.pf.toFixed(2)
      obj.acur = obj.acur?.toFixed(2)
      obj.bcur = obj.bcur?.toFixed(2)
      obj.ccur = obj.ccur?.toFixed(2)
      obj.curUnbalance = obj.curUnbalance?.toFixed(0)
      obj.avol = obj.avol?.toFixed(1)
      obj.bvol = obj.bvol?.toFixed(1)
      obj.cvol = obj.cvol?.toFixed(1)
      obj.volUnbalance = obj.volUnbalance?.toFixed(0)
      if (obj.color == 1) {
        smallCurrent++
      } else if (obj.color == 2) {
        lessFifteen++
      } else if (obj.color == 3) {
        greaterFifteen++
      } else if (obj.color == 4) {
        greaterThirty++
      }
    })
    statusNumber.smallCurrent = smallCurrent
    statusNumber.lessFifteen = lessFifteen
    statusNumber.greaterFifteen = greaterFifteen
    statusNumber.greaterThirty = greaterThirty
    total.value = data.total
    list.value = data.list
    console.log('111获取数据111', list)
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    var range = await CurbalanceColorApi.getCurbalanceColor()
    if (range != null) {
      statusList[0].name = '<' + range.rangeOne + '%'
      statusList[1].name = range.rangeTwo + '%-' + range.rangeThree + '%'
      statusList[2].name = '>' + range.rangeFour + '%'
    }
    var tableIndex = 0
    var lessFifteen = 0
    var greaterFifteen = 0
    var greaterThirty = 0
    var smallCurrent = 0
    data.list.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex
      if (obj?.dataUpdateTime == null && obj?.pow == null) {
        return
      }
      const splitArray = obj.dataUpdateTime.split(' ')
      obj.dataUpdateTime = splitArray[1]
      obj.apparentPow = obj.apparentPow.toFixed(3)
      obj.pow = obj.pow.toFixed(3)
      obj.ele = obj.ele.toFixed(1)
      obj.pf = obj.pf.toFixed(2)
      obj.acur = obj.acur?.toFixed(2)
      obj.bcur = obj.bcur?.toFixed(2)
      obj.ccur = obj.ccur?.toFixed(2)
      obj.curUnbalance = obj.curUnbalance?.toFixed(0)
      obj.avol = obj.avol?.toFixed(1)
      obj.bvol = obj.bvol?.toFixed(1)
      obj.cvol = obj.cvol?.toFixed(1)
      obj.volUnbalance = obj.volUnbalance?.toFixed(0)
      if (obj.color == 1) {
        smallCurrent++
      } else if (obj.color == 2) {
        lessFifteen++
      } else if (obj.color == 3) {
        greaterFifteen++
      } else if (obj.color == 4) {
        greaterThirty++
      }
    })

    statusNumber.smallCurrent = smallCurrent
    statusNumber.lessFifteen = lessFifteen
    statusNumber.greaterFifteen = greaterFifteen
    statusNumber.greaterThirty = greaterThirty
    list.value = data.list
    total.value = data.total
  } catch (error) {}
}

// 接口获取导航列表
const getNavList = async () => {
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i = 0; i < res.length; i++) {
    var temp = await CabinetApi.getRoomPDUList({ id: res[i].id })
    arr = arr.concat(temp)
  }
  navList.value = arr
}

import { useRouter } from 'vue-router'

const router = useRouter()

// const toPDUDisplayScreen = (row: { devKey: string; location: string; id: number }) => {
//   const { devKey, location, id } = row;
//   router.push({
//     path: '/pdu/pdudevicebalance',
//     query: { devKey, id: id.toString(), location }
//   });
// };

const toPDUDisplayScreen = (row) => {
  const devKey = row.devKey
  const pduId = row.id
  const location = row.location ? row.location : devKey
  push({ path: '/pdu/pdudevicebalance', state: { devKey, pduId, location } })
}
// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

const handleSelectStatus = (index) => {
  statusList[index].selected = !statusList[index].selected
  const status = statusList.filter((item) => item.selected)
  const statusArr = status.map((item) => item.value)
  if (statusArr.length != statusList.length) {
    queryParams.color = statusArr
    queryParams.status = [0, 1, 2, 3, 4]
  } else {
    queryParams.color = []
    queryParams.status = []
  }
  handleQuery()
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  statusList.forEach((item) => (item.selected = true))
  handleQuery()
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type)
}

/** 删除按钮操作 */
const handleDelete = async (devKey: string) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await PDUDeviceApi.deletePDUDevice({
      devKey: devKey
    })
    message.success(t('common.delSuccess'))
    // 刷新列表
    // await getList()
  } catch {}
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await PDUDeviceApi.exportPDUDevice(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll()
  getList()
  getNavList()
  flashListTimer.value = setInterval(getListNoLoading, 5000)
})

onBeforeUnmount(() => {
  if (flashListTimer.value) {
    clearInterval(flashListTimer.value)
    flashListTimer.value = null
  }
})

onBeforeRouteLeave(() => {
  if (flashListTimer.value) {
    clearInterval(flashListTimer.value)
    flashListTimer.value = null
    firstTimerCreate.value = false
  }
})

onActivated(() => {
  getList()
  getNavList()
  if (!firstTimerCreate.value) {
    flashListTimer.value = setInterval(getListNoLoading, 5000)
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
      box-shadow: 0 3px 4px 1px rgba(0, 0, 0, 0.12);
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
    margin-top: 20px;
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
    margin-top: 28px;
    margin-bottom: 20px;
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
      background-color: #f56c6c;
    }
  }
}

@media screen and (min-width: 2048px) {
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
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
          margin: 0 25px;
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
    display: flex;
    flex-wrap: wrap;
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
          margin: 0 25px;
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
    display: flex;
    flex-wrap: wrap;
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
          margin: 0 25px;
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

::v-deep .el-table .el-table__header th {
  background-color: #f5f7fa;
  color: #909399;
}

:deep(.el-dialog) {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}
.custom-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
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

</style>
