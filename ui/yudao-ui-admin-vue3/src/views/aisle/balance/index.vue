<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="柜列平衡" >
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
        <!-- <div class="overview">
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            <div class="info">
              <div>总电能</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dh.jpg" />
            <div class="info">
              <div>今日用电</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            <div class="info">
              <div>今日用电</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
        </div> -->
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
          <el-form-item label="柜列名称" prop="username">
            <el-input
              v-model="queryParams.name"
              placeholder="请输入柜列名称"
              clearable
              class="!w-160px"
              height="35"
            />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" @click="getTableData(true)"><Icon icon="ep:search" />搜索</el-button>
            <el-button @click="resetQuery" style="width:70px;" ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="handleSwitchModal(0)" :type="switchValue==0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />AB路占比</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue==1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-show="switchValue == 0 && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <!-- 电流 -->
            <div class="progressContainer">
              <div class="progress" v-if="item.rateA">
                <div class="left" :style="`flex: ${item.rateA==0&&item.rateB==0?50:(item.rateA>60?60:(item.rateA<40?40:item.rateA))}`">{{item.rateA}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${item.rateA==0&&item.rateB==0?50:(item.rateB>60?60:(item.rateB<40?40:item.rateB))}`">{{ item.rateB}}%</div>
                <div class="tip">
                  <span>
                    <div>{{item.powApparentA ? item.powApparentA: '0.000'}}kVA</div>
                    <div>A路视在功率</div>
                  </span>
                  <span>
                    <div>{{item.powApparentB ? item.powApparentA: '0.000'}}kVA</div>
                    <div>B路视在功率</div>
                  </span>
                </div>
              </div>
              <div style="margin-right:10px;margin-left: 25px;margin-top: 30px; text-align: center;">
                {{item.powApparentTotal ? item.powApparentTotal : '0.000'}}kVA
                <div>总视在功率</div>
              </div>
            </div>
            <div class="room">{{item.location}}</div>
            <button class="detail" @click.prevent="toDetail(item)">详情</button>
          </div>
        </div>
        <el-table v-show="switchValue == 1" style="width: 100%;" :data="tableData" >
          <el-table-column type="index" width="60" label="序号" align="center" />
          <el-table-column label="名称" min-width="90" align="center" prop="location" />
          <el-table-column label="总共" align="center">
            <el-table-column label="视在功率(kVA)" min-width="90" align="center" prop="powApparentTotal" />
            <el-table-column label="有功功率(kW)" min-width="90" align="center" prop="powActiveTotal" />
            <el-table-column label="无功功率(kVar)" min-width="90" align="center" prop="powReactiveTotal" />
          </el-table-column>
          <el-table-column label="A路" align="center">
            <el-table-column label="视在功率(kVA)" min-width="90" align="center" prop="powApparentA" />
            <el-table-column label="有功功率(kW)" min-width="90" align="center" prop="powActiveA" />
            <el-table-column label="无功功率(kVar)" min-width="90" align="center" prop="powReactiveA" />
          </el-table-column>
          <el-table-column label="B路"  align="center">
            <el-table-column label="视在功率(kVA)" min-width="90" align="center" prop="powApparentB" />
            <el-table-column label="有功功率(kW)" min-width="90" align="center" prop="powActiveB" />
            <el-table-column label="无功功率(kVar)" min-width="90" align="center" prop="powReactiveB" />
          </el-table-column>
        </el-table>
        <el-dialog v-loading="detailLoading" v-model="showDetailDialog" @close="handleClose" >
          <!-- 自定义的头部内容（可选） -->
          <template #header>
            <CardTitle title="AB占比情况" />
            <div class="powerContainer" v-if="balanceObj.pow_active_percent > 0">
              <div class="power">
                <div class="label">有功功率：</div>
                <div class="progressContainer">
                  <div class="progress">
                    <div class="left" :style="`flex: ${balanceObj.pow_active_percent}`">{{balanceObj.pow_active_percent.toFixed(0)}}%</div>
                    <div class="line"></div>
                    <div class="right" :style="`flex: ${100 - balanceObj.pow_active_percent}`">{{100 - balanceObj.pow_active_percent.toFixed(0)}}%</div>
                  </div>
                </div>
              </div>
              <div class="power">
                <div class="label">视在功率：</div>
                <div class="progressContainer">
                  <div class="progress">
                    <div class="left" :style="`flex: ${balanceObj.pow_apparent_percent}`">{{balanceObj.pow_apparent_percent.toFixed(0)}}%</div>
                    <div class="line"></div>
                    <div class="right" :style="`flex: ${100 - balanceObj.pow_apparent_percent}`">{{100 - balanceObj.pow_apparent_percent.toFixed(0)}}%</div>
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
                <curUnblance :max="balanceObj.imbalanceValueA" />
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
        <Pagination
          :total="queryParams.pageTotal"
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
import { IndexApi } from '@/api/aisle/aisleindex'
const curUnblance = defineAsyncComponent(() => import('./component/curUnblance.vue'))
const volUnblance = defineAsyncComponent(() => import('./component/volUnblance.vue'))
import { tourEmits } from 'element-plus';
import { table } from 'console';
import { set } from 'nprogress';

const { push } = useRouter() // 路由跳转
const router = useRouter() // 路由跳转
const tableLoading = ref(false) // 
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([]) as any
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const showDetailDialog = ref(false)
const apow = ref(null)
const bpow = ref(null)
const detailLoading = ref(false)
const ABarOption = ref<EChartsOption>({});
const BBarOption = ref<EChartsOption>({});

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
const queryParams = reactive({
  name: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
}) as any
const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  cur_valueB: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0,
})
// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getAisleMenu()
  navList.value = res
}

const resetQuery = () => {
 queryParams.name = undefined;
  getTableData(true)
}
// 接口获取机柜列表
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await IndexApi.getAisleBalancePage(queryParams)

    if (res.list) {

      tableData.value = res.list
      tableData.value.forEach(obj => {
        obj.rateA = obj?.rateA?.toFixed(0);
        obj.rateB = obj?.rateB?.toFixed(0);
        obj.powApparentTotal = obj?.powApparentTotal?.toFixed(3);
        obj.powActiveTotal = obj?.powActiveTotal?.toFixed(3);
        obj.powReactiveTotal = obj?.powReactiveTotal?.toFixed(3);
        obj.powApparentA = obj?.powApparentA?.toFixed(3);
        obj.powActiveA = obj?.powActiveA?.toFixed(3);
        obj.powReactiveA = obj?.powReactiveA?.toFixed(3);
        obj.powApparentB = obj?.powApparentB?.toFixed(3);
        obj.powActiveB = obj?.powActiveB?.toFixed(3);
        obj.powReactiveB = obj?.powReactiveB?.toFixed(3);
      });
      

      queryParams.pageTotal = res.total
    }
  } finally {
    tableLoading.value = false
  }
}

// 详情跳转
const toDetail = async (item) => {
  detailLoading.value = true
  showDetailDialog.value = true;
  apow.value=balanceObj.pow_apparent_percent=item.powApparentA/item.powApparentTotal*100;
  bpow.value=1-apow.value;
  balanceObj.pow_active_percent=item.powActiveA/item.powActiveTotal*100;
  let response=await IndexApi.getBalanceDetail(item.id)
  console.log(response)
  const cur_valueA=response.curLista
  if(cur_valueA!=null){
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
            { value: cur_valueA[0]?.toFixed(2), name: 'A相电流', itemStyle: { color: '#075F71' } },
            { value: cur_valueA[1]?.toFixed(2), name: 'B相电流', itemStyle: { color: '#119CB5' } },
            { value: cur_valueA[2]?.toFixed(2), name: 'C相电流', itemStyle: { color: '#45C0C9' } },
          ]
        }
      ]
    }
  }
  const cur_valueB = response.curListb
  if(cur_valueB!=null){
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
            { value: cur_valueB[0]?.toFixed(2), name: 'A相电流', itemStyle: { color: '#075F71' } },
            { value: cur_valueB[1]?.toFixed(2), name: 'B相电流', itemStyle: { color: '#119CB5' } },
            { value: cur_valueB[2]?.toFixed(2), name: 'C相电流', itemStyle: { color: '#45C0C9' } },
          ]
        }
      ]
    }
  }
  balanceObj.imbalanceValueA=response.curUnbalancea;
  balanceObj.imbalanceValueB=response.curUnbalanceb;
  detailLoading.value = false;
  // console.log('详情跳转', id, router, router.getRoutes())
  // push({path: '/cabinet/cab/balanceDetail', state: { id }})
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value == 0) { // 阵列
    queryParams.pageSize = 24
  } else {
    queryParams.pageSize = 10
  }
  getTableData(true)
}

// 处理左侧树导航选择事件
const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.aisleIds = null;
    getTableData(true)
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 2) {
      ids.push(item.id)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.aisleIds = [-1]
  }else{
    queryParams.aisleIds = ids
  }

  getTableData(true)
}

// 处理对话框关闭
const handleClose = () => {
  showDetailDialog.value = false;
  balanceObj.pow_apparent_percent= 0;
  balanceObj.pow_active_percent= 0;
  balanceObj.cur_valueA= [];
  balanceObj.cur_valueB= [];
  balanceObj.imbalanceValueA= 0;
  balanceObj.imbalanceValueB= 0;
  balanceObj.colorIndex= 0;
}

//打开对话框
// const showDialog=(id)=>{
  // alert("hello");
  // showDetailDialog.value = true;
  // apow.value = item.apow;
  // bpow.value = item.bpow;
// }
onBeforeMount( () => {
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
      margin-top: 30px;
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
        margin-top: 30px;
        
        .tip {
          width: 180px;
          position: absolute;
          top: -22px;
          left: 0;
          display: flex;
          justify-content: space-between;
          color: #000;
          font-size: 12px;
        }
        .line {
          position: relative;
          bottom: -10px;
          width: 3px;
          height: 25px;
          background-color: #000;
        }
        .left {
          position: relative;
          bottom: -10px;
          text-align: center;
          box-sizing: border-box;
          background-color: #3b8bf5;
          // border-right: 1px solid #000;
        }
        .right {
          position: relative;
          bottom: -10px;
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
      top: 8px;
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


.cardChilc {
  flex: 1;
  height: 100%;
}

.IechartBar {
  width: 100%;
  height: 100%;
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
:deep(.el-dialog) {
  width: 80%;
  margin-top: 50px;
  background-color: #f1f1f1;
}
</style>