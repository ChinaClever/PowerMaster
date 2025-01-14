<template>
  <CommonMenu @check="handleCheck" :showSearch="true" :dataList="navList" navTitle="机柜负载">
    <template #NavInfo>
      <div class="navInfo">
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">微模块机房</div>
          <div>机房202</div>
        </div> -->
        <div class="line"></div>
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>未开通
            </div>
            <div class="value"><span class="number">{{Loadstatus[0]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;30%
            </div>
            <div class="value"><span class="number">{{Loadstatus[1]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag normal"></div>30%~60%
            </div>
            <div class="value"><span class="number">{{Loadstatus[2]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>60%~90%
            </div>
            <div class="value"><span class="number">{{Loadstatus[3]}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;90%
            </div>
            <div class="value"><span class="number">{{Loadstatus[4]}}</span>个</div>
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
        <el-form-item>
          <!--<template v-for="(status, index) in statusList" :key="index">
            <button :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
          </template>-->
          <el-button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">全部</el-button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
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
            <el-button style="margin-left: 12px" @click="getTableData(true)"><Icon icon="ep:search" />搜索</el-button>
            <!-- <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button> -->
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;handleSwitchModal(0)" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />视在功率</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(1)" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />有功功率</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(2)" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 2" style="width: 100%;" v-loading="loading" :data="listPage" class="loadContainer">
        <el-table-column label="位置" min-width="110" align="center" prop="roomName,cabinetName" >
          <template #default="scope">
            {{ scope.row.roomName }}-{{ scope.row.cabinetName }}
          </template>
        </el-table-column>
        <el-table-column label="负载率" min-width="80" align="center" prop="loadFactor" :formatter="formatApparentPower" />
        <el-table-column label="电力容量(kVA)" min-width="100" align="center" prop="powerCapacity" :formatter="formatApparentPower" />
        <el-table-column label="总视在功率(kVA)" min-width="110" align="center" prop="apparentTotal" :formatter="formatApparentPower" />
        <el-table-column label="A路视在功率(kVA)" min-width="120" align="center" prop="powApparentb" :formatter="formatApparentPower" />
        <el-table-column label="B路视在功率(kVA)" min-width="120" align="center" prop="powApparenta" :formatter="formatApparentPower" />
        <el-table-column label="总有功功率(kW)" min-width="110" align="center" prop="activeTotal" :formatter="formatApparentPower" />
        <el-table-column label="A路有功功率(kW)" min-width="120" align="center" prop="powActivea" :formatter="formatApparentPower" />
        <el-table-column label="B路有功功率(kW)" min-width="120" align="center" prop="powActiveb" :formatter="formatApparentPower" />
        <el-table-column label="更新时间" min-width="110" align="center" prop="dataUpdateTime" />
        <el-table-column label="设备" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toMachineDetail(scope.row)"
              style="background-color:#409EFF;color:#fff;border:none;width:65px;height:30px;"
            >
            设备详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-show="(switchValue == 0 || switchValue == 1) && listPage.length > 0" v-loading="loading" class="loadContainer">
        <div class="loadItem" v-for="load in listPage" :key="load.key">
          <div class="content">
            <div class="info" v-if="switchValue == 0">
              <div>总视在功率：{{formatNumber(load.apparentTotal,2)}}KVA</div>
              <div>A路视在功率：{{formatNumber(load.powApparenta,2)}}KVA</div>
              <div>B路视在功率：{{formatNumber(load.powApparentb,2)}}KVA</div>
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            </div>
            <div class="info" v-else>
              <div>总有功功率：{{formatNumber(load.activeTotal,2)}}kW</div>
              <div>A路有功功率：{{formatNumber(load.powActivea,2)}}kW</div>
              <div>B路有功功率：{{formatNumber(load.powActiveb,2)}}kW</div>
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            </div>
            <div class="waterPoloBox">
              <LiquidBall  :precent="formatLoadFactor(load.loadFactor)"/>
            </div>
            <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
          </div>
          <div class="room">{{load.roomName+'-'+load.cabinetName}}</div>
          <div class="status-container">
            <el-tag class="status" type="info" v-if="load.loadStatus == 0">{{statusList[0].name}}</el-tag>
            <el-tag class="status" type="success" v-else-if="load.loadStatus == 1">{{statusList[1].name.slice(3,10)}}</el-tag>
            <el-tag class="status" type="primary" v-else-if="load.loadStatus == 2">{{statusList[2].name.slice(7,11)}}</el-tag>
            <el-tag class="status" type="warning" v-else-if="load.loadStatus == 3">{{statusList[3].name.slice(7,11)}}</el-tag>
            <el-tag class="status" type="danger"  v-else-if="load.loadStatus == 4">{{statusList[4].name.slice(3,10)}}</el-tag>
            <el-tag class="status" type="info"  v-else-if="load.loadStatus == 5">离线</el-tag>
          </div>
          <div class="detail-container">
            <button class="detail" v-if="load.loadStatus !== 5 && load.loadStatus !== null" @click.prevent="toMachineDetail(load)">详情</button>
          </div>
        </div>
      </div>
      <Pagination
        :total="queryParams.pageTotal"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :page-size-arr="pageSizeArr"
        @pagination="getTableData(false)"
      />
      <template v-if="listPage.length == 0 && switchValue != 2 && !loading">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts';
import 'echarts-liquidfill';
import { CabinetApi } from '@/api/cabinet/info';
import LiquidBall from './compoent/LiquidBall.vue';

const { push } = useRouter();
const loading = ref(false);
const isFirst = ref(true); // 是否第一次调用getTableData函数
const navList = ref([]);
const Loadstatus = ref([0,0,0,0,0]);
const switchValue = ref(0);
const cabinetIds = ref<number[]>([]); // 左侧导航菜单所选id数组
const listPage = ref<any>([]); // 表格数据
const pageSizeArr = ref([24,36,48,96]);
const queryParams = reactive({
  company: undefined,
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
  loadStatus: []
});

const butColor = ref(0);
const onclickColor = ref(-1);

function formatApparentPower(row, column, cellValue ) {
  // console.log('测试',row+'-'+column+'-'+cellValue+'-'+num)
  // 假设保留两位小数
  return parseFloat(cellValue).toFixed(2);
}

const echartsOption = reactive({
  series: [
    {
      type: 'liquidFill',
      data: [0.38], // 设置水球图的填充比例
      label: {
        fontSize: 12, // 设置字体大小
        fontWeight: 'bold' // 设置字体粗细
      },
      radius: '100%',
      amplitude: 2, // 调整波浪的振幅
      outline: {
        show: false // 不显示外圈轮廓线
      },

      color: ['#3b8bf5'],
      backgroundStyle: {
        color: '#fff'
      }
    }
  ]
})

const statusList = reactive([
  {
    name: '未开通',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
  {
    name: '负载量<30%',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '30%≤负载量<60%',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#ffc402'
  },
  {
    name: '60%≤负载量<90%',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  },
  {
    name: '负载量>90%',
    selected: true,
    value: 4,
    cssClass: 'btn_unbound',
    activeClass: 'btn_unbound unbound',
    color: '#05ebfc'
  },
])


// 接口获取机柜列表
//const getTableData = async(reset = false) => {
//  loading.value = true
//  if (reset) queryParams.pageNo = 1
//  //const status =  statusList.filter(item => item.selected)
//  try {
//    const res = await CabinetApi.getIndexLoadPage({
//      pageNo: queryParams.pageNo,
//      pageSize: queryParams.pageSize,
//      cabinetIds: isFirst.value ? null : cabinetIds.value,
//      // roomId: null,
//      loadStatus: queryParams.loadStatus,
//      pduBox: 0,
//      company: queryParams.company
//    })
//    const res = await CabinetApi.getIndexLoadPage(queryParams)
//    console.log('res', res);
//    listPage.value = res.list;
//    queryParams.pageTotal = res.total;
//    console.log('listPage', listPage.value)
//  } finally {
//    loading.value = false
//  }
//}
const getTableData = async(reset = false) => {
  loading.value = true
  if (reset) queryParams.pageNo = 1
 
  try {
    const res = await CabinetApi.getIndexLoadPage(queryParams)
    if (!res) {
      console.error('API response is null or undefined');
      // 可以选择设置一个空数组和总页数为0，或者显示错误消息
      listPage.value = [];
      queryParams.pageTotal = 0;
    } else {
      console.log('res', res);
      listPage.value = res.list || []; // 如果res.list是undefined，使用空数组
      queryParams.pageTotal = res.total || 0; // 如果res.total是undefined，使用0
      console.log('listPage', listPage.value)
    }
  } catch (error) {
    console.error('Error fetching table data:', error);
    // 处理错误，如显示错误消息
    listPage.value = [];
    queryParams.pageTotal = 0;
  } finally {
    loading.value = false
  }
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.loadStatus = [];
  getTableData();
}

const formatNumber = (value, precision) => {
  if (typeof value === 'number' && !isNaN(value)) {
    return value.toFixed(precision);
  }
  return 0;
};

const formatLoadFactor = (value) => {
  if (typeof value === 'number' && !isNaN(value)) {
    return Math.ceil(Number(value.toFixed(2)));
  }
  return 0;
};
// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  console.log('接口获取机房导航列表', res)
  navList.value = res
}

// 接口获取负载状态
const getLoadStatusList = async() => {
  const res = await CabinetApi.getLoadStatus({})
  console.log('接口获取机房导航列表', res)
  Loadstatus.value = res
}

const handleClick = (row) => {
  console.log('Button clicked!', row);
}

const handleCheck = (row) => {
  console.log('handleCheck!', row);
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

// 处理状态选择事件
const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.loadStatus = [index];
  console.log('处理状态选择事件', index)
  //statusList[index].selected = !statusList[index].selected
  getTableData()
}
// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value != 2) { // 阵列
    queryParams.pageSize = 24
  } else {
    queryParams.pageSize = 10
  }
  getTableData(true)
}

const toMachineDetail = (row) => {
  console.log("row",row)
  //const devKey = '192.168.1.200-3';
  //const busId = 2;
  //const location = '192.168.1.200-3';
  //const busName = 'iBusbar-3';
  //push({ path:'/cabinet/cab/cabinetPowerLoadDetail', state: { devKey, busId ,location,busName }})
  const devKey = row.cabinet_key;
  const busId = row.roomId;
  const busName = row.cabinet_name;
  push({ path:'/cabinet/cab/cabinetPowerLoadDetail', state: { devKey, busId ,busName }})
}

onBeforeMount(() => {
  getNavList()
  getTableData()
  getLoadStatusList()
})
</script>

<style lang="scss" scoped>

.loadContainer {
  height: 600px;
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .loadItem {
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
      padding-left: 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      line-height: 1.6;
      margin-top: 5px;
      .waterPoloBox {
        flex: 1;
        margin-left: 15px;
        display: flex;
        justify-content: center;
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
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
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

.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  // width: 55px;
  // height: 32px;
  padding: 3px 8px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  &:hover {
    color: #7bc25a;
  }
}

.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
}

.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.btn_empty {
  border: 1px solid #aaa;
  background-color: #fff;
}

.empty {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.btn_warn {
  border: 1px solid #3b8bf5;
  background-color: #fff;
}

.warn {
  background-color: #3b8bf5;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.btn_error {
  border: 1px solid #ffc402;
  background-color: #fff;
}

.error {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.btn_unbound {
  border: 1px solid #fa3333;
  background-color: #fff;
}

.unbound {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.loadItem {
  position: relative;
  padding-right: 100px;
}

.status-container {
  position: absolute;
  right: 5px;
  top: 5px;
  text-align: center;
}

.status-container .status {
  display: block;
  padding-top: 5px;
  width: 50px;
  height: 25px;
  margin-bottom: 8px;
}

.detail-container{
  position: absolute;
  right: 5px;
  top: 80px;
}

.detail {
  position: relative;
}

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 25px;
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
  height: 25px;
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
</style>
