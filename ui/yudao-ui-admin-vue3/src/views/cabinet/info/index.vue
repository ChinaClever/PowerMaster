<template>
  <CommonMenu @check="handleCheck" :showSearch="true" :dataList="navList" navTitle="机柜配电">
    <template #NavInfo>
      <div class="navInfo">
        <div class="line"></div>
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>未绑定
            </div>
            <div class="value"><span class="number">{{Unbound}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag"></div>正常
            </div>
            <div class="value"><span class="number">{{Normal}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>预警
            </div>
            <div class="value"><span class="number">{{Warning}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>告警
            </div>
            <div class="value"><span class="number">{{Alarm}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>离线
            </div>
            <div class="value"><span class="number">{{Offline}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div></div>全部
            </div>
            <div class="value"><span class="number">{{totalAll}}</span>个</div>
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
        style="height:54px;"
      >
        <el-form-item style="margin-left: 5px">
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部 
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
          <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button>
        </el-form-item>
        <div style="margin-left:10px;">
          <el-form-item v-show="switchValue"  label="公司名称" prop="company">
            <el-input
              v-model="queryParams.company"
              placeholder="请输入公司名称"
              clearable
              class="!w-130px"
              height="35"
            />
          </el-form-item >
          <el-form-item v-show="switchValue" label="展示列" prop="showCol">
            <el-cascader
              ref="colNode"
              class="!w-230px"
              v-model="defaultOptionsCol"
              @change="cascaderChange"
              :options="optionsCol"
              :props="props"
              :clearable="false"
              collapse-tags />
          </el-form-item>
          <el-form-item>
            <el-button style="margin-left: 12px" v-show="switchValue" @click="getTableData()"><Icon icon="ep:search" />搜索</el-button>
            <!--<el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button>-->
          </el-form-item>
        </div>
        <el-form-item style="margin-left: auto">
          <!-- <el-button @click="handleSwitchModal(0);showPagination = 0;" :type="switchValue == 0? 'primary' : ''" style="width: 100px;"><Icon icon="ep:grid" style="margin-right:3px;"/>阵列模式</el-button>
          <el-button @click="handleSwitchModal(1);showPagination = 0;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:expand"  />表格模式</el-button>
          <el-button @click="handleSwitchLogicRemoveModal(2,true);showPagination = 1;" :type="switchValue == 2 ? 'primary' : ''"  v-show="switchValue" ><Icon icon="ep:expand"  />已删除</el-button> -->

          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;handleSwitchModal(0);switchValue = 0;showPagination = 0;" :type="switchValue === 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchModal(1);switchValue = 1;showPagination = 0;" :type="switchValue === 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;handleSwitchLogicRemoveModal(2,true);switchValue = 2;showPagination = 1;" :type="switchValue ===2 ? 'primary' : ''" v-show="switchValue ===1"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>
          <!--  <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 2;showPagination = 1;" :type="switchValue ===2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button> 
  --> 
         
        </el-form-item>
      </el-form>
    </template>
    <template #Content>
      <div v-if="switchValue && listPage.length > 0" class="table-height">
        <el-table v-show="switchValue == 1" :data="listPage" @cell-dblclick="handleDbclick">
        <el-table-column label="位置" min-width="110" align="center">
          <template #default="scope">
            <div>{{scope.row.roomName}}-{{scope.row.cabinetName}}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="110" align="center">
          <template #default="scope">
            <div :style="{color: statusList[scope.row.status].color}">{{statusList[scope.row.status] && statusList[scope.row.status].name}}</div>
          </template>
        </el-table-column>
        <!-- <el-table-column v-if="queryParams.showCol.includes(12)" label="名称" min-width="110" align="center" prop="cabinetName" /> -->
        <el-table-column v-if="queryParams.showCol.includes(1)" label="总视在功率(kVA)" min-width="140" align="center" prop="apparentTotal" />
        <el-table-column v-if="queryParams.showCol.includes(2)" label="总有功功率(kW)" min-width="130" align="center" prop="activeTotal" />
        <el-table-column v-if="queryParams.showCol.includes(3)" label="总电能(kWh)" min-width="110" align="center" prop="eleTotal" />
        <el-table-column v-if="queryParams.showCol.includes(5)" label="A视在功率(kVA)" min-width="140" align="center" prop="apparentA" />
        <el-table-column v-if="queryParams.showCol.includes(6)" label="A有功功率(kW)" min-width="130" align="center" prop="activeA" />
        <el-table-column v-if="queryParams.showCol.includes(17)" label="A无功功率(kVar)" min-width="130" align="center" prop="reactiveA"/>
        <el-table-column v-if="queryParams.showCol.includes(7)" label="A电能(kWh)" min-width="110" align="center" prop="eleA" />
        <el-table-column v-if="queryParams.showCol.includes(9)" label="B视在功率(kVA)" min-width="140" align="center" prop="apparentB" />
        <el-table-column v-if="queryParams.showCol.includes(10)" label="B有功功率(kW)" min-width="130" align="center" prop="activeB" />
        <el-table-column v-if="queryParams.showCol.includes(18)" label="B无功功率(kVar)" min-width="130" align="center" prop="reactiveB"/>
        <el-table-column v-if="queryParams.showCol.includes(11)" label="B电能(kWh)" min-width="110" align="center" prop="eleB" />
        <el-table-column v-if="queryParams.showCol.includes(12)" label="总无功功率(kVar)" min-width="120" align="center" prop="powerReactiveTotal"/>
        <el-table-column v-if="queryParams.showCol.includes(13)" label="功率因素" align="center" prop="powerFactorTotal" />
        <el-table-column v-if="queryParams.showCol.includes(15)" label="负载比(%)" min-width="110" align="center" prop="loadFactor" />
        <el-table-column v-if="queryParams.showCol.includes(14)" label="所属公司" min-width="110" align="center" prop="company" />
        <el-table-column v-if="queryParams.showCol.includes(16)" label="A,B占比" align="center" prop="abzb">
          <template #default="scope">
            <div v-if="scope.row.abzb == '-'">-</div>
            <div v-else class="progressContainer">
              <div class="progress">
                <div class="left" :style="`flex: ${scope.row.abzb}`">A:{{scope.row.abzb}}</div>
                <div class="right" :style="`flex: ${100 - scope.row.abzb}`">B:{{100 - scope.row.abzb}}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" align="center" width="180px">
          <template #default="scope">
            <el-button 
              type="primary"
              @click="openForm('edit', scope.row.cabinet_key)">编辑
            </el-button>
              <el-button v-if ="scope.row.cabinetBoxes"
                type="danger"  style="margin-left: 2px"
                @click="handleDelete(scope.row.id,2)">删除母线
              </el-button>
              <el-button v-else-if ="scope.row.cabinetPdus"
                type="danger" style="margin-left: 2px"
                @click="handleDelete(scope.row.id,1)">删除pdu
              </el-button> 
              <el-button v-else-if ="scope.row.rackIndices"
                type="danger" style="margin-left: 2px"
                @click="handleDelete(scope.row.id,3)">删除机架
              </el-button>
              <el-button v-else
                type="danger" style="margin-left: 2px"
                @click="handleDelete(scope.row.id,4)">删除机柜
              </el-button>
          </template>
        </el-table-column>
      </el-table>


   <el-table v-if="switchValue == 2" v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border="true">
         <el-table-column label="位置" min-width="110" align="center">
            <template #default="scope">
               <div>{{scope.row.roomname}} - {{scope.row.name}}</div>
            </template>
        </el-table-column>
        <el-table-column label="状态" min-width="110" align="center">
            <template #default="scope">
                <div :style="{color: statusList[scope.row.runStatus].color}">{{statusList[scope.row.runStatus] && statusList[scope.row.runStatus].name}}</div>
            </template>
        </el-table-column>
        
       <el-table-column label="删除日期" min-width="110" align="center">
            <template #default="scope">
               {{ (new Date(scope.row.updateTime)).toISOString().slice(0, 10) }}
            </template>
        </el-table-column>
        
       <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.id)"
            >
              恢复设备
            </el-button>
          </template>
        </el-table-column>
     </el-table>
      </div>
      <Pagination
        v-if="showPagination == 1"
        :total="queryParams.pageTotal"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="handleSwitchLogicRemoveModal(2,false)"
      />
      <div v-if="!switchValue && listPage.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in listPage" :key="item.id" @dblclick="handleArrayDbclick(item.cabinet_key)">
          <div class="content">
            <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
            <div v-if="item.status !== 5" class="info" style="margin-left:10px;">
              <div>视在功率：{{item.apparentTotal}}KVA</div>
              <div>有功功率：{{item.activeTotal}}KW</div>
              <div>无功功率：{{item.powerReactiveTotal}}KVAR</div>
              <!-- 负载率： -->
            </div>
            <div style="padding: 0 28px"><LiquidBall :width="50" :height="50" :precent="item.powerFactorTotal*100 || ''" :status="item.status" /></div>
          </div>
          <div class="room">{{item.roomName}}-{{item.cabinetName}}</div>
          <div v-if="item.status == 0" class="status-empty">未绑定</div>
          <!--<div v-if="item.status == 0" class="status-unbound">正常</div>-->
          <div v-else-if="item.status == 1" class="status-normal">正常</div>
          <div v-else-if="item.status == 2" class="status-warn">预警</div>
          <div v-else-if="item.status == 3" class="status-error">告警</div>
          <!--<div v-else-if="item.status == 4" class="status-error">故障</div>-->
          <div v-else-if="item.status == 4" class="status-offline">离线</div>
          <button v-if="item.status !== 4" class="detail" @click.prevent="toMachineDetail(item)">详情</button>
        </div>
      </div>
      <Pagination
        v-if="showPagination == 0"
        :total="queryParams.pageTotal"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getTableData()"
      />
      <template v-if="listPage.length == 0 && !switchValue">
        <el-empty description="暂无数据" :image-size="595" />
      </template>
      <!--<div v-if="listPage.length == 0 && !switchValue" style="display:flex;">-->
        
      <!-- </div> -->
    </template>
  </CommonMenu>
  <!-- 添加或修改用户对话框 -->
  <MachineForm ref="machineForm" @success="saveMachine" :roomList="navList" />
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import MachineForm from './component/MachineForm.vue';
import LiquidBall from './component/LiquidBall.vue';
import { CabinetApi } from '@/api/cabinet/info';
import { Console } from 'console';
// import MyButton from '@/components/MyButton/MyButton.vue';

const { push } = useRouter(); // 路由跳转
const message = useMessage(); // 消息弹窗
const machineForm = ref(); // 机柜表单组件
const colNode = ref(); // 展示列组件
const loading = ref(false);
const butColor = ref(0);
const onclickColor = ref(-1);
// const isCloseNav = ref(false) // 左侧导航是否收起
const isFirst = ref(true); // 是否第一次调用getTableData函数
const switchValue = ref(0); // 0:阵列 1：表格
const showPagination = ref(0);
const listPage = ref<any>([]); // 表格数据
const deletedList = ref<any>([]); //已删除的
const navList = ref([]); // 左侧导航列表数据
const cabinetIds = ref<number[]>([]); // 左侧导航菜单所选id数组
const defaultOptionsCol = reactive([1, 2, 12, 13, 15, 16]);
const pageSizeArr = ref([24,36,48,96]);
const total = ref(0) // 列表的总页数
const deletedTotal = ref(0) // 已删除PDU设备列表的总页数
// 运行状态 0：未绑定 1：正常 2：预警 3：告警 4：离线
const Unbound = ref();
const Normal = ref();
const Warning = ref();
const Alarm = ref();
const Offline = ref();
const totalAll = ref();

const flashListTimer = ref();
const flashListTimerCopy = ref();

const optionsCol = reactive([{
  value: 0,
  label: '总',
  children: [{
    value: 1,
    label: '总AB视在功率'
  }, {
    value: 2,
    label: '总AB有功功率'
  },
  {
    value: 12,
    label: '总AB无功功率'
  },
  {
    value: 3,
    label: '总AB有功电能'
  },
  {
    value: 13,
    label: '总功率因素'
  }],
},{
  value: 4,
  label: 'A路',
  children: [{
    value: 5,
    label: 'A视在功率'
  }, {
    value: 6,
    label: 'A有功功率'
  }, {
    value: 17,
    label: 'A无功功率'
  }, {
    value: 7,
    label: 'A有功电能'
  }],
},{
  value: 8,
  label: 'B路',
  children: [{
    value: 9,
    label: 'B视在功率'
  }, {
    value: 10,
    label: 'B有功功率'
  },{
    value: 18,
    label: 'B无功功率'
  }, {
    value: 11,
    label: 'B有功电能'
  }],
},
//{
//  value: 12,
//  label: '无功功率'
//}
//,
//{
//  value: 13,
//  label: '功率因素'
//}
{
  value: 14,
  label: '所属公司'
},{
  value: 15,
  label: '负载比'
},{
  value: 16,
  label: 'AB占比'
}
])
const queryParams = reactive({
  company: undefined,
  showCol: [1, 2, 12, 13, 15, 16] as number[],
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
  runStatus:[]
}) as any

const statusList = reactive([
  {
    name: '未绑定',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
  {
    name: '正常',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '预警',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#ffc402'
  },
  {
    name: '告警',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  },
  {
    name: '离线',
    selected: true,
    value: 4,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline',
    color: '#7700ff'
  },
])
const props = { multiple: true }

// 接口获取机柜列表
const getTableData = async() => {
  let ids;
  if(cabinetIds.value.length == 0){
    ids = null;
  } else{
    ids =cabinetIds.value
  }
  loading.value = true
  //if (reset) queryParams.pageNo = 1
  try {
    const res = await CabinetApi.getCabinetInfo({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: isFirst.value ? null : ids,
      // roomId: null,
      runStatus: queryParams.runStatus,
      company: queryParams.company
    })

    if (res.list) {
      const list = res.list.map(item => {
        const tableItem = {
          company: item.company,
          id: item.id,
          cabinet_key: item.cabinet_key,
          cabinetName: item.cabinet_name,
          roomName: item.room_name,
          status: item.status,
          cabinetBoxes: item.cabinetBoxes,
          cabinetPdus: item.cabinetPdus,
          rackIndices: item.rackIndices,
          //apparentTotal: formatNumber(item.cabinet_power.total_data.pow_apparent, 3),
          //apparentA: formatNumber(item.cabinet_power.path_a?.pow_apparent, 3),
          //apparentB: formatNumber(item.cabinet_power.path_b?.pow_apparent, 3),
          //activeTotal: formatNumber(item.cabinet_power.total_data.pow_active, 3),
          //activeA: formatNumber(item.cabinet_power.path_a?.pow_active, 3),
          //activeB: formatNumber(item.cabinet_power.path_b?.pow_active, 3),
          //eleTotal: formatNumber(item.cabinet_power.total_data.ele_active, 1),
          //eleA: formatNumber(item.cabinet_power.path_a?.ele_active, 1),
          //eleB: formatNumber(item.cabinet_power.path_b?.ele_active, 1),
          apparentTotal: item.cabinet_power.total_data.pow_apparent.toFixed(3),
          apparentA: item.cabinet_power.path_a?.pow_apparent.toFixed(3),
          apparentB: item.cabinet_power.path_b?.pow_apparent.toFixed(3),
          activeTotal: item.cabinet_power.total_data.pow_active.toFixed(3),
          activeA: item.cabinet_power.path_a?.pow_active.toFixed(3),
          activeB: item.cabinet_power.path_b?.pow_active.toFixed(3),
          reactiveA: item.cabinet_power.path_a?.pow_reactive.toFixed(3),
          reactiveB: item.cabinet_power.path_b?.pow_reactive.toFixed(3),
          eleTotal: item.cabinet_power.total_data.ele_active.toFixed(1),
          eleA: item.cabinet_power.path_a?.ele_active.toFixed(1),
          eleB: item.cabinet_power.path_b?.ele_active.toFixed(1),
          powerFactorTotal: item.cabinet_power.total_data.power_factor,
          powerReactiveTotal: item.cabinet_power.total_data.pow_reactive.toFixed(3),
          loadFactor: formatLoadFactor(item.load_factor),
          abzb: '-' as number | string
        }
        if (item.cabinet_power.path_a && item.cabinet_power.path_b) {
          if (item.cabinet_power.path_a.pow_apparent == 0) tableItem.abzb = 0
          else tableItem.abzb = Math.ceil(Number((item.cabinet_power.path_a.pow_apparent / item.cabinet_power.total_data.pow_apparent).toFixed(0)))
        }
        return tableItem
      })
      console.log("res.list",res.list)
      listPage.value = list;
      queryParams.pageTotal = res.total;

    }
     const resStatus =await CabinetApi.getCabinetInfoStatus();
    Unbound.value = resStatus.unbound;
    Normal.value = resStatus.normal;
    Warning.value = resStatus.warning;
    Alarm.value = resStatus.alarm;
    Offline.value = resStatus.offline;
    totalAll.value = resStatus.total;
  } finally {
    loading.value = false
  }
}


const formatLoadFactor = (value) => {
  if (typeof value === 'number' && !isNaN(value)) {
    return Math.ceil(Number(value.toFixed(2)));
  }

  return 0;
};

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({});
  navList.value = res;
}


// 保存机柜修改/删除
const saveMachine = async() => {
  getTableData();
  getNavList();
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  showPagination.value = 0;
  if (switchValue.value == value) return
  switchValue.value = value;
  if (value == 0) { // 阵列
    queryParams.pageSize = 24;
  } else {
    queryParams.pageSize = 24;
  }
  getTableData();
}

//已删除
const handleSwitchLogicRemoveModal = async (value, reset = false) =>{
    showPagination.value = 1;
    switchValue.value = value;
    if (reset) queryParams.pageNo = 1
    const res = await CabinetApi.getDeletedCabinetPage({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
    })
    deletedList.value = res.list;
    queryParams.pageTotal = res.total
    deletedTotal.value = res.total;
}

//恢复设备
const handleRestore = async(id) =>{
    const res = await CabinetApi.getrestorerCabinet({
      id: id,
    });
    if(res != "1"){
       message.success('设备恢复失败!')
    }
    // 刷新列表
    await getNavList()
    handleSwitchLogicRemoveModal(2,true);
    message.success('设备恢复成功!');
}


//处理表格双击事件
const handleDbclick = (e) => {
  const id = e.cabinet_key.split('-')[1]
  const roomId = e.cabinet_key.split('-')[0];
  const type = 'hour';
  const location = e.roomName;
  const cabinetName = e.cabinetName;
  push({path: '/cabinet/cab/detail', state: {location , cabinetName ,id ,roomId , type}})
  //push('/cabinet/cab/detail')
}

// 处理阵列双击事件
const handleArrayDbclick = (key) => {
  openForm('edit', key)
}

// 处理状态选择事件
const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.runStatus = [index];
  getTableData();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.runStatus = [];
  getTableData();
}

// 跳转详情页
const toMachineDetail = (key) => {
  const id = key.cabinet_key.split('-')[1]
  const roomId = key.cabinet_key.split('-')[0];
  const type = 'hour';
  const location = key.roomName;
  const cabinetName = key.cabinetName;
  if(key.status == 0){
     message.error('未绑定设备无法查看详情!')
     return;
  }
  
  push({path: '/cabinet/cab/detail', state: {location , cabinetName ,id ,roomId , type}})
}

const handleCheck = (row) => {
  isFirst.value = false
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids
  getTableData()
}

// 打开 编辑/添加 表单弹窗
const openForm = async(type: string, key?: string) => {
  if (type == 'add') {
    machineForm.value.open(type)
  } else if (type == 'edit' && key) {
    const id = key.split('-')[1]
    try {
      loading.value = true
      const res = await CabinetApi.getCabinetInfoItem({id})
      machineForm.value.open(type, res)
    } finally {
      loading.value =false
    }
  }
}

// 处理删除事件
const handleDelete = async (id: number, index: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await CabinetApi.deleteCabinetInfo({
      id: id,
      type: index
    })
    message.success('删除成功')
    // 刷新列表
    await getNavList()
    getTableData()
  } catch (error) {
  }
}

// 展示列选择处理事件
const cascaderChange = (_row) => {
  const checkedNodes = colNode.value.getCheckedNodes(true)
  queryParams.showCol = checkedNodes.map(item => item.value)
}

onBeforeMount(() => {
  getNavList();
  getTableData();
  flashListTimer.value = setInterval((getTableData), 5000);
  // flashListTimerCopy.value = setInterval((getNavList), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
  if(flashListTimerCopy.value){
    clearInterval(flashListTimerCopy.value)
    flashListTimerCopy.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
  if(flashListTimerCopy.value){
    clearInterval(flashListTimerCopy.value)
    flashListTimerCopy.value = null;
  }
})
</script>

<style scoped lang="scss">
.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  width: 55px;
  height: 32px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  margin-right: 5px;
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
  border: 1px solid #ffc402;
  background-color: #fff;
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
}
.error {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_unbound {
  border: 1px solid #05ebfc;
  background-color: #fff;
}
.unbound {
  background-color: #05ebfc;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_offline {
  border: 1px solid #7700ff;
  background-color: #fff;
}
.offline {
  background-color: #7700ff;
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
    .box {
      height: 50px;
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
    margin-top: 30px;
    margin-bottom: 10px;
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
      // border-right: 1px solid #000;
    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
  }
}

@media screen and (min-width:2048px){
  .table-height{
    height: 76vh;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    height: 78vh;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
    .arrayItem {
      min-width: 290px;
      width: 20%;
      height: 120px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 36px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        line-height: 1.7;
        .icon {
          width: 30px;
          height: 30px;
          margin: 0 28px;
        }
      }
      .room {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .status-empty {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ccc;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-normal {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #3bbb00;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-warn {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffc402;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-error {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fa3333;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-unbound {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #05ebfc;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-offline {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #7700ff;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .detail {
        width: 40px;
        height: 25px;
        cursor: pointer;
        padding: 0;
        border: 1px solid #ccc;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        position: absolute;
        right: 8px;
        bottom: 8px;
      }
    }
  }
}

@media screen and (max-width:2048px) and (min-width:1600px){
  .table-height{
    height: 710px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
    .arrayItem {
      min-width: 290px;
      width: 25%;
      height: 120px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 36px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        line-height: 1.7;
        .icon {
          width: 30px;
          height: 30px;
          margin: 0 28px;
        }
      }
      .room {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .status-empty {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ccc;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-normal {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #3bbb00;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-warn {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffc402;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-error {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fa3333;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-unbound {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #05ebfc;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-offline {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #7700ff;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .detail {
        width: 40px;
        height: 25px;
        cursor: pointer;
        padding: 0;
        border: 1px solid #ccc;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        position: absolute;
        right: 8px;
        bottom: 8px;
      }
    }
  }
}

@media screen and (max-width:1600px){
  .table-height{
    height: 600px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    height: 600px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
    .arrayItem {
      min-width: 290px;
      width: 33.3%;
      height: 120px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #eef4fc;
      border: 5px solid #fff;
      padding-top: 36px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        line-height: 1.7;
        .icon {
          width: 30px;
          height: 30px;
          margin: 0 28px;
        }
      }
      .room {
        position: absolute;
        left: 8px;
        top: 8px;
      }
      .status-empty {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ccc;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-normal {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #3bbb00;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-warn {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #ffc402;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-error {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fa3333;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-unbound {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #05ebfc;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .status-offline {
        width: 40px;
        height: 20px;
        font-size: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #7700ff;
        color: #fff;
        position: absolute;
        right: 8px;
        top: 8px;
      }
      .detail {
        width: 40px;
        height: 25px;
        cursor: pointer;
        padding: 0;
        border: 1px solid #ccc;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #fff;
        position: absolute;
        right: 8px;
        bottom: 8px;
      }
    }
  }
}
.btnallSelected {
  margin-right: 5px;
  width: 55px;
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
  margin-right: 5px;
  width: 55px;
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
:deep(.el-card){
  --el-card-padding:5px;
}
</style>
