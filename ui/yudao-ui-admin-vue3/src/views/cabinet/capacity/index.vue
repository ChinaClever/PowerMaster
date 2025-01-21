<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜容量">
    <template #NavInfo>
      <div class="navInfo">
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>100%
            </div>
            <div class="value"><span class="number">{{oneHundred}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag normal"></div>&gt;50%
            </div>
            <div class="value"><span class="number">{{ ninetyNine }}</span>个</div>
          </div>
          <div class="box">
            <div class="top" style="margin-left:-10px;">
              <div class="tag warn"></div>30%~50%
            </div>
            <div class="value"><span class="number">{{ fifty }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&lt;30%
            </div>
            <div class="value"><span class="number">{{ thirty }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div></div>全部
            </div>
            <div class="value"><span class="number">{{ total }}</span>个</div>
          </div>
        </div>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        :inline="true"
        label-width="68px"
      >
        <el-form-item style="margin-left: 5px;">
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
        </el-form-item>
        <div>
          <el-form-item label="公司名称" prop="company">
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-loading="tableLoading">
        <div v-if="switchValue == 0 && tableData.length > 0" class="matrixContainer">
          <div class="item" v-for="item in tableData" :key="item.id">
            <div class="content">
              <!-- <div>未用空间：{{item.freeSpace}}U</div> -->
              <div class="info">
                <div>已用空间：{{item.usedSpace}}U</div>
                <div>空间容量：{{item.cabinetHeight}}U</div>
                <div>设备数量：{{item.rackNum}}</div>
              </div>
              <div class="shapeContainer">
                <div class="shape">
                  <div style="position:relative;" :style="{flex: item.usedSpace}">
                    <div style="position:absolute;top:50px;left:5px;">剩余量</div>
                  </div>
                  <div class="fill" :style="{flex: 100-item.usedSpaceRate}">{{100-item.usedSpaceRate}}%</div>
                </div>
                <div class="bottom"></div>
              </div>
            </div>
            <div class="room">{{item.roomName}}-{{item.name}}</div>
            <div style="position: absolute;width:70px;height:30px;top:5px;right:5px;">剩余量100%</div>
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
          <el-table-column label="设备数量" min-width="110" align="center" prop="rackNum" />
          <el-table-column label="未用空间(U)" min-width="110" align="center" prop="freeSpace" />
          <el-table-column label="已用空间(U)" min-width="110" align="center" prop="usedSpace" />
          <el-table-column label="操作" align="center">
            <template #default="scope">
              <el-button
                link
                type="primary"
                @click="toDetail(scope.row.roomId, scope.row.id)"
                style="background-color:#409EFF;color:#fff;border:none;width:65px;height:30px;"
              >
              设备详情
              </el-button>
            </template>
          </el-table-column>
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
import { CabinetApi } from '@/api/cabinet/info';
import { CapacityApi } from '@/api/cabinet/capacity';

const {push} = useRouter();
const switchValue = ref(0);
const tableLoading = ref(false);
const tableData = ref([]);
const navList = ref([]); // 左侧导航栏树结构列表
const isFirst = ref(true); // 是否第一次调用getTableData函数
const cabinetIds = ref<number[]>([]); // 左侧导航菜单所选id数组
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
  startNum: 0,
  endNum: 100,
});
const pageSizeArr = ref([24,36,48,96]);
const butColor = ref(0);
const onclickColor = ref(-1);
const statusList = reactive([
  {
    name: '剩余量100%',
    selected: true,
    value: 0,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00',
    startNum: 100,
    endNum: 100,
  },
  {
    name: '剩余量>50%',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#ffc402',
    startNum: 50,
    endNum: 99.99,
  },
  {
    name: '30%<剩余量<50%',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#fa3333',
    startNum: 30,
    endNum: 49.99,
  },
  {
    name: '剩余量<30%',
    selected: true,
    value: 4,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    startNum: 0,
    endNum: 29.99,
  }
])
const thirty = ref(0);
const fifty = ref(0);
const ninetyNine = ref(0);
const oneHundred = ref(0);
const total = ref(0);

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

const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.startNum = statusList[index].startNum;
  queryParams.endNum = statusList[index].endNum;
  getTableData();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.startNum = 0;
  queryParams.endNum = 100;
  getTableData();
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

//获取机柜容量列表统计
const getCapacitystatistics = async () => {
  const res = await CapacityApi.getCapacity();
  thirty.value = res.thirty;
  fifty.value = res.fifty;
  ninetyNine.value  =res.ninetyNine;
  oneHundred.value = res.oneHundred;
  total.value  =res.total;
  console.log('data1111111111111',res);
}

// 跳转详情
const toDetail = (id,roomId) => {
  console.log('跳转详情', id)
  push({path: '/cabinet/cab/screen', state: { id , roomId}})
}

onBeforeMount(async () => {
  await getNavList();
  await getTableData();
  await getCapacitystatistics();
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
      padding-left: 0px;
      display: flex;
      align-items: center;
      .shapeContainer {
        padding-left: 50px;
        display: flex;
        flex-direction: column;
        align-items: center;
        .shape {
          display: flex;
          flex-direction: column;
          width: 45px;
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
        margin-left: 10px;;
      }
    }
    .room {
      position: absolute;
      top: 5px;
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
      bottom: 4px;
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

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #409EFF;
  color: white;
  border: none;
  border-radius: 5px;
}

.btnallNotSelected{
  margin-right: 10px;
  width: 58px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  color: #000000;
  border: 1px solid #409EFF;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
  }
}

.btn_fault,
.btn_offline,
.btn_normal,
.btn_warn,
.btn_error{
  width: 125px;
  height: 32px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
  }
}
.btn_offline {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.btn_fault{
  border: 1px solid orange;
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

.fault {
  background-color: orange;
  color: #fff;
  &:hover {
    color: #fff;
  }
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

  .status {
    display: flex;
    flex-wrap: wrap;
    margin-top:15px;
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
        .normal {
          background-color: #3B8BF5;
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
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
</style>
