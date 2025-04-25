<template>
  <CommonMenu @check="handleCheck" :showSearch="true" :dataList="navList" navTitle="机柜负荷">
    <template #NavInfo>
      <div class="navInfo">
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">微模块机房</div>
          <div>机房202</div>
        </div> -->
        <div class="status">
          <div class="box">
            <div class="value"><span class="number">{{statusNumber.LoadRateZero}}</span>个</div>
            <div class="top">
              <div class="tag empty"></div>空载
            </div>
            
          </div>
          <div class="box">
            <div class="value"><span class="number">{{statusNumber.lessThirty}}</span>个</div>
            <div class="top">
              <div class="tag"></div>&lt;30%
            </div>
            
          </div>
          <div class="box">
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
            <div class="top">
              <div class="tag normal"></div>30%~60%
            </div>
            
          </div>
          <div class="box">
            <div class="value"><span class="number">{{statusNumber.greaterSixty}}</span>个</div>
            <div class="top" style="margin-left:-10px;">
              <div class="tag warn"></div>60%~90%
            </div>
           
          </div>
          <div class="box">
            <div class="value"><span class="number">{{statusNumber.greaterNinety}}</span>个</div>
            <div class="top">
              <div class="tag error"></div>&gt;90%
            </div>
            
          </div>
          <div class="box">
            <div class="top">
              <div></div>全部
            </div>
            <div class="value"><span class="number">{{statusNumber.total}}</span>个</div>
          </div>
        </div>
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
            <el-button style="width:60px;" @click="getTableData(true)"><Icon icon="ep:search" />搜索</el-button>
            <el-button @click="resetData()" style="width:50px;margin-left:-1px;">重置</el-button>
            <!-- <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button> -->
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;handleSwitchModal(0)" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />图表模式</el-button>
          <!-- <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(1)" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />有功功率</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(3)" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />无功功率</el-button> -->
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(2)" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 2" style="width: 100%;" :data="listPage" class="loadContainer">
        <el-table-column label="位置" min-width="110" align="center" prop="roomName,cabinetName" >
          <template #default="scope">
            {{ scope.row.roomName }}-{{ scope.row.cabinetName }}
          </template>
        </el-table-column>
        <el-table-column label="负载率" min-width="80" align="center" prop="loadFactor" :formatter="formatApparentPower" />
        <el-table-column label="电力容量(kVA)" min-width="100" align="center" prop="powerCapacity" :formatter="formatnumOne" />
        <el-table-column label="总视在功率(kVA)" min-width="110" align="center" prop="apparentTotal" :formatter="formatnumThree" />
        <el-table-column label="A路视在功率(kVA)" min-width="120" align="center" prop="powApparentb" :formatter="formatnumThree" />
        <el-table-column label="B路视在功率(kVA)" min-width="120" align="center" prop="powApparenta" :formatter="formatnumThree" />
        <el-table-column label="总有功功率(kW)" min-width="110" align="center" prop="activeTotal" :formatter="formatnumThree" />
        <el-table-column label="A路有功功率(kW)" min-width="120" align="center" prop="powActivea" :formatter="formatnumThree" />
        <el-table-column label="B路有功功率(kW)" min-width="120" align="center" prop="powActiveb" :formatter="formatnumThree" />
        <el-table-column label="A路无功功率(kVar)" min-width="120" align="center" prop="powReactivea" :formatter="formatnumThree" />
        <el-table-column label="B路无功功率(kVar)" min-width="120" align="center" prop="powReactiveb" :formatter="formatnumThree" />
        <el-table-column label="更新时间" min-width="110" align="center" prop="dataUpdateTime" />
        <el-table-column label="操作" min-width="90" align="center">
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
      

      <!-- <div v-show="(switchValue == 0 || switchValue == 1 || switchValue == 3) && listPage.length > 0"  class="loadContainer">
        <div class="loadItem" v-for="load in listPage" :key="load.key">
          <div class="content">
            <div class="info" v-if="switchValue == 0">
              <div>总视在功率：{{formatNumber(load.apparentTotal,3) || '0.000'}}KVA</div>
              <div>A路视在功率：{{formatNumber(load.powApparenta,3) || '0.000'}}KVA</div>
              <div>B路视在功率：{{formatNumber(load.powApparentb,3) || '0.000'}}KVA</div> -->
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            <!-- </div>
            <div class="info" v-else-if="switchValue == 1">
              <div>总有功功率：{{formatNumber(load.activeTotal,3)|| '0.000'}}kW</div>
              <div>A路有功功率：{{formatNumber(load.powActivea,3)|| '0.000'}}kW</div>
              <div>B路有功功率：{{formatNumber(load.powActiveb,3)|| '0.000'}}kW</div> -->
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            <!-- </div>
            <div class="info" v-else-if="switchValue == 3">
              <div>总无功功率：{{formatNumber(load.powReactiveTotal,3)|| '0.000'}}kVar</div>
              <div>A路无功功率：{{formatNumber(load.powReactivea,3)|| '0.000'}}kVar</div>
              <div>B路无功功率：{{formatNumber(load.powReactiveb,3)|| '0.000'}}kVar</div> -->
              <!-- <div>电力容量：{{load.pow_capacity}}</div> -->
            <!-- </div>
            <div class="waterPoloBox">
              <LiquidBall  :precent="load.loadFactor || 0"/>
            </div> -->
            <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
          <!-- </div>
          <div class="room">{{load.roomName+'-'+load.cabinetName}}</div> -->
          <!-- <div class="status-container">
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
      </div> -->

      <div v-show="(switchValue == 0 || switchValue == 1 || switchValue == 3) && listPage.length > 0" class="arrayContainer">

          <div class="arrayItem" v-for="load in listPage" :key="load.key">
          <div class="devKey">{{ load.location != null ? load.location : load.devKey }}</div>
          <div v-if="load.apparentTotal != null && load.apparentTotal != 0" class="content">
            <div class="info" >
              <div v-if="load.apparentTotal != null">
                <el-text>
                  总视在功率：{{formatNumber(load.apparentTotal,3) || '0.000'}}kVA
                </el-text>
              </div>
              <div v-if="load.activeTotal != null">
                <el-text>
                  总有功功率：{{formatNumber(load.activeTotal,3)|| '0.000'}}kW
                </el-text>
              </div>
              <div v-if="load.powReactiveTotal != null">
                <el-text>
                  总无功功率：{{formatNumber(load.powReactiveTotal,3)|| '0.000'}}kVar
                </el-text>
              </div>
             
            </div>
            <!-- <div v-if="load.powReactiveTotal != null" style="display: flex;flex-direction: column;">
              <div style="text-align: center;font-size: 24px;">{{load.powReactiveTotal ? load.powReactiveTotal : 0}}</div> 
              <div style="text-align: center;font-size: 10px;">总功率因数</div>
            </div> -->
            <div class="waterPoloBox">
              <LiquidBall  :precent="load.loadFactor || 0"/>
            </div>
          </div>
         
         
           <div style="display:flex;height:10vh;justify-content: center;align-item:center;margin-bottom: -3vh;margin-top: 1vh">
            <!-- <Environment v-if="valueMode == 0 && item.curAList" class="chart" width="100%" height="100%" :load-factor="{first: item.curAList?.[2] ? item.curAList[2].toFixed(0) : 0,second: item.curAList?.[1] ? item.curAList[1].toFixed(0) : 0,third: item.curAList?.[0] ? item.curAList[0].toFixed(0) : 0,label: ['Ic','Ib','Ia'],unit: ['A','A','A'],color: ['#AD3762','#C8603A','#E5B849']}" style="margin-right:-15px;"/>  -->
            <!-- <Environment v-else-if="valueMode == 1 && item.volAList" class="chart" width="100%" height="100%" :load-factor="{first: item.volAList?.[2] ? item.volAList[2].toFixed(0) : 0,second: item.volAList?.[1] ? item.volAList[1].toFixed(0) : 0,third: item.volAList?.[0] ? item.volAList[0].toFixed(0) : 0,label: ['Uc','Ub','Ua'],unit: ['V','V','V'],color: ['#45C0C9','#119CB5','#075F71']}" style="margin-right:-15px;"/>  -->
            <Environment  class="chart" width="100%" height="100%" :load-factor="{first: load.powReactivea ? Number(load.powReactivea).toFixed(0) : 0,second: load.powActivea ? Number(load.powActivea).toFixed(0) : 0,third: load.powApparenta ? Number(load.powApparenta).toFixed(0) : 0,label: ['Q','P','S'],unit: ['kVar', 'kW', 'kVA'],color: ['#800080','#91cc75','#5470c6']}" style="margin-right:-15px;"/>
            <EnvironmentCopy  class="chart" width="100%" height="100%" :load-factor="{first: load.powReactiveb ? Number(load.powReactiveb).toFixed(0) : 0,second: load.powActiveb ? Number(load.powActiveb).toFixed(0) : 0,third: load.powApparentb ? Number(load.powApparentb).toFixed(0) : 0,label: ['Q','P','S'],unit: ['kVar', 'kW', 'kVA'],color: ['#800080','#91cc75','#5470c6']}"/>

            </div>
          <div style="display:flex;justify-content: space-around;padding: 5px 0;">
            <div>A路</div>
            <div>B路</div>
          </div>

            

          <div  style="position: absolute;bottom: 0;right: 0">
            <button class="detail" v-if="load.loadStatus !== 5 && load.loadStatus !== null" @click.prevent="toMachineDetail(load)">详情</button>
          </div>
          <!-- <div style="display: inline-block;
            width: 50%;
            height: 50%;
            margin-right:-15px;"
          >
           
            <EnvironmentCopy  class="chart" width="100%" height="100%" :load-factor="item"/>
          </div>
          <div  style="display: inline-block;
                         width: 50%;
            height: 50%;
            margin-right:-15px;">

            <EnvironmentCopy  class="chart" width="100%" height="100%" :load-factor="item"/>
          </div> -->

    
          <div class="status-container">
            <el-tag class="status" type="info" v-if="load.loadStatus == 0">{{statusList[0].name}}</el-tag>
            <el-tag class="status" type="success" v-else-if="load.loadStatus == 1">{{statusList[1].name.slice(3,10)}}</el-tag>
            <el-tag class="status" type="primary" v-else-if="load.loadStatus == 2">{{statusList[2].name.slice(7,11)}}</el-tag>
            <el-tag class="status" type="warning" v-else-if="load.loadStatus == 3">{{statusList[3].name.slice(7,11)}}</el-tag>
            <el-tag class="status" type="danger"  v-else-if="load.loadStatus == 4">{{statusList[4].name.slice(3,10)}}</el-tag>
            <el-tag class="status" type="info"  v-else-if="load.loadStatus == 5">离线</el-tag>
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
import Environment from '@/views/aisle/power/component/Environment.vue'
import EnvironmentCopy from '@/views/aisle/power/component/EnvironmentCopy.vue'
import { ElTree } from 'element-plus'

const valueMode = ref(2)

// 数据转化
const getSafeNumber = (value) => {
  const num = Number(value);
  return isNaN(num) ? 0 : num;
};



const { push } = useRouter();
const loading = ref(false);
const isFirst = ref(true); // 是否第一次调用getTableData函数
const navList = ref([]);
const statusNumber = reactive({
  LoadRateZero:0,
  lessThirty : 0,
  greaterThirty : 0,
  greaterSixty : 0,
  greaterNinety : 0,
  total: 0
})
const switchValue = ref(0);
const cabinetIds = ref<number[]>([]); // 左侧导航菜单所选id数组
const listPage = ref<any>([]); // 表格数据
const pageSizeArr = ref([24,36,48,96]);
const queryParams = reactive({
  company: undefined,
  cabinetIds:[] as number[],
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
  loadStatus: []
});

const butColor = ref(0);
const onclickColor = ref(-1);
const flashListTimer = ref();

function formatApparentPower(row, column, cellValue ) {
  // 假设保留两位小数
  if (cellValue === null || cellValue === undefined) {
    return 0;
  }
  return parseFloat(cellValue).toFixed(0);
}
function formatnumOne(row, column, cellValue ) {
    if (cellValue === null || cellValue === undefined) {
    return 0;
  }
  // 假设保留两位小数
  return parseFloat(cellValue).toFixed(1);
}
function formatnumThree(row, column, cellValue ) {
    if (cellValue === null || cellValue === undefined) {
    return 0;
  }
  // 假设保留两位小数
  return parseFloat(cellValue).toFixed(3);
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
    name: '空载',
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

const getTableData = async(reset = false) => {
  loading.value = true
  if (reset) queryParams.pageNo = 1
  try {
    const res = await CabinetApi.getIndexLoadPage(queryParams)
    getLoadStatusList();
    if (!res) {
      // 可以选择设置一个空数组和总页数为0，或者显示错误消息
      listPage.value = [];
      queryParams.pageTotal = 0;
    } else {

      listPage.value = res.list || []; // 如果res.list是undefined，使用空数组
      queryParams.pageTotal = res.total || 0; // 如果res.total是undefined，使用0
    }
  } catch (error) {
    // 处理错误，如显示错误消息
    listPage.value = [];
    queryParams.pageTotal = 0;
  } finally {
    loading.value = false;
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
  navList.value = res
}

// 接口获取负载状态
const getLoadStatusList = async() => {
  const res = await CabinetApi.getLoadStatus({})
    statusNumber.LoadRateZero = res.LoadRateZero;
    statusNumber.greaterNinety = res.greaterNinety;
    statusNumber.lessThirty = res.lessThirty;
    statusNumber.greaterThirty = res.greaterThirty;
    statusNumber.greaterSixty = res.greaterSixty;
    statusNumber.total = res.total;
  // Loadstatus.value = res
}

const handleClick = (row) => {
  console.log('Button clicked!', row);
}

const resetData = () => {
  queryParams.company = undefined;
    butColor.value = 0;
  onclickColor.value = -1;
  queryParams.loadStatus = [];
  getTableData();
}

const handleCheck = (row) => {

  isFirst.value = false
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids;
  queryParams.cabinetIds =ids;
  getTableData(true)
}

// 处理状态选择事件
const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.loadStatus = [index];

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
  //const devKey = '192.168.1.200-3';
  //const busId = 2;
  //const location = '192.168.1.200-3';
  //const busName = 'iBusbar-3';
  //push({ path:'/cabinet/cab/cabinetPowerLoadDetail', state: { devKey, busId ,location,busName }})
  const roomId = row.roomId;
  const cabinet = row.id;
  const cabinetName = row.cabinetName;
  const roomName = row.roomName;
  const pduBox = row.pduBox;
  push({ path:'/cabinet/cab/cabinetPowerLoadDetail', query: {roomName, roomId ,cabinetName, cabinet, pduBox}})
}

onBeforeMount(() => {
  getNavList();
  getTableData();
  flashListTimer.value = setInterval((getTableData), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})
</script>

<style lang="scss" scoped>

.loadContainer {
  height: 720px;
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
      height: 30px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      margin-top: 30px;
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
  padding: 6px 8px;
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


.arrayContainer {
  width:100%;
  height: 720px;
  overflow: hidden;
  overflow-y: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  margin-top: -10px;
  .arrayItem {
    width: 25%;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      display: flex;
      justify-content: space-around;
      align-items: center;
      .icon {
        width: 74px;
        height: 30px;
        font-size: large;
        text-align: center;
      }
    }
    .devKey{
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    // .status {
    //   width: 40px;
    //   height: 20px;
    //   font-size: 12px;
    //   display: flex;
    //   align-items: center;
    //   justify-content: center;

    //   // color: #fff;
    //   color:#000;
    //   position: absolute;
    //   right: 38px;
    //   top: 8px;
    // }
    .detail {
      width: 40px;
      height: 25px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      margin-right: 8px;
      margin-bottom: 8px;
      cursor: pointer;
      z-index: 10
    }
  }
}


.el-form {
  display: flex;
  flex-wrap: nowrap;
}
</style>
