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
          <el-button @click="handleSwitchModal(0)" :type="switchValue==0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />AB路占比</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue==1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="switchValue == 0 && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.key">
            <!-- 电流 -->
            <div class="progressContainer">
              <div style="margin-right:10px;margin-left: 5px;margin-top: 30px;">
                <div>总视在功率：{{item.powApparentTotal ? item.powApparentTotal : '0.000'}}kVA</div>
                <div>A路视在功率：{{item.powApparentA ? item.powApparentA: '0.000'}}kVA</div>
                <div>B路视在功率：{{item.powApparentB ? item.powApparentA: '0.000'}}kVA</div>
              </div>
              <div class="progress" v-if="item.rateA">
                <div class="left" :style="`flex: ${item.rateA}`">{{item.rateA}}%</div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${item.rateB}`">{{ item.rateB}}%</div>
                <div class="tip">
                  <span>A路</span>
                  <span>B路</span>
                </div>
              </div>
            </div>
            <div class="room">{{item.location}}</div>
            <button class="detail" @click.prevent="toDetail(item.id)">详情</button>
          </div>
        </div>
        <el-table v-if="switchValue == 1" style="width: 100%;" :data="tableData" >
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

const { push } = useRouter() // 路由跳转
const router = useRouter() // 路由跳转
const tableLoading = ref(false) // 
const navList = ref([]) // 左侧导航栏树结构列表
const tableData = ref([]) as any
const switchValue = ref(0) // 表格(1) 矩阵(0)切换
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
}) as any

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await IndexApi.getAisleMenu()
  navList.value = res
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
const toDetail = (id) => {
  console.log('详情跳转', id, router, router.getRoutes())
  push({path: '/cabinet/cab/balanceDetail', state: { id }})
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
      top: 8px;
    }
  }
}
</style>