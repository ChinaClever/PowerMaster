<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜容量">
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
            <el-button style="margin-left: 12px" @click="getTableData(true)" ><Icon icon="ep:search" />搜索</el-button>
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
          <el-button @click="handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="switchValue == 0 && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.id">
            <div class="content">
              <!-- <div>未用空间：{{item.freeSpace}}U</div> -->
              <div class="shapeContainer">
                <div class="shape">
                  <div :style="{flex: item.usedSpace}"></div>
                  <div class="fill" :style="{flex: item.freeSpace}">{{item.freeSpace}}U</div>
                </div>
                <div class="bottom"></div>
              </div>
              <div class="info">
                <div>已用空间：{{item.usedSpace}}U</div>
                <div>空间容量：{{item.cabinetHeight}}U</div>
                <div>设备数量：{{item.rackNum}}U</div>
              </div>
            </div>
            <div class="room">{{item.roomName}}-{{item.name}}</div>
            <button class="detail" @click.prevent="toDetail(item.id,item.roomId)">详情</button>
          </div>
        </div>
        <el-table v-if="switchValue == 1" style="width: 100%;height: calc(100vh - 320px);" :data="tableData" >
          <el-table-column type="index" width="100" label="序号" align="center" />
          <el-table-column label="位置" min-width="110" align="center" prop="local" >
            <template #default="scope">
              {{scope.row.roomName}}-{{scope.row.name}}
            </template>
          </el-table-column>
          <el-table-column label="公司" min-width="110" align="center" prop="company" />
          <el-table-column label="设备数量(U)" min-width="110" align="center" prop="rackNum" />
          <el-table-column label="未用空间(U)" min-width="110" align="center" prop="freeSpace" />
          <el-table-column label="已用空间(U)" min-width="110" align="center" prop="usedSpace" />
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
import { CabinetApi } from '@/api/cabinet/info'
import { CapacityApi } from '@/api/cabinet/capacity'

const {push} = useRouter()
const switchValue = ref(0)
const tableLoading = ref(false)
const tableData = ref([])
const navList = ref([]) // 左侧导航栏树结构列表
const isFirst = ref(true) // 是否第一次调用getTableData函数
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})


// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 接口获取机柜容量页面数据
const getTableData = async(reset = false) => {
  tableLoading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await CapacityApi.getCapacityData({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      company: queryParams.company,
      cabinetIds: isFirst.value ? null : cabinetIds.value,
      runStatus: [0, 1, 2, 3, 4, 5],
      roomId: null
    })
    console.log('res', res)
    if (res.list) {
      tableData.value = res.list
      queryParams.pageTotal = res.total
    }
  } finally {
    tableLoading.value = false
  }
}

// 处理左侧树导航选择事件
const handleCheck = (row) => {
  console.log('handleCheck', row)
  isFirst.value = false
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  console.log('handleCheck-----', ids)
  cabinetIds.value = ids
  getTableData(true)
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

// 跳转详情
const toDetail = (id,roomId) => {
  console.log('跳转详情', id)
  push({path: '/cabinet/cab/screen', state: { id , roomId}})
}

onBeforeMount(() => {
  getNavList()
  getTableData()
})
</script>

<style lang="scss" scoped>
.matrixContainer {
  height: calc(100vh - 320px);
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .item {
    width: 25%;
    min-width: 275px;
    height: 130px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 36px;
    position: relative;
    .content {
      padding-left: 30px;
      display: flex;
      align-items: center;
      .shapeContainer {
        display: flex;
        flex-direction: column;
        align-items: center;
        .shape {
          display: flex;
          flex-direction: column;
          width: 40px;
          height: 75px;
          background-color: #fff;
          .fill {
            background-color: #42ee5f;
            display: flex;
            align-items: center;
            justify-content: center;
          }
        }
        .bottom {
          width: 50px;
          height: 2px;
          background-color: #000;
        }
      }
      .info {
        line-height: 1.7;
        font-size: 13px;
        margin-left: 20%;
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
      right: 5px;
      top: 4px;
    }
  }
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
</style>