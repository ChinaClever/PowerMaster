<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="navList" navTitle="PDU配电">
    <template #NavInfo>
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/PDU.jpg" /></div>

        </div> -->
        <!-- <div class="line"></div> -->
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>正常
            </div>
            <div class="value"><span class="number">{{ statusNumber.normal }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>离线
            </div>
            <div class="value"><span class="number">{{ statusNumber.offline }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>预警
            </div>
            <div class="value"><span class="number">{{ statusNumber.warn }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>告警
            </div>
            <div class="value"><span class="number">{{ statusNumber.alarm }}</span>个</div>
          </div>
        </div>
        <div class="line"></div>

      </div>
    </template>
    <template #ActionBar>
      <!-- 搜索工作栏 -->
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
        v-show="switchValue !== 2"                          
      >
        <el-form-item>
          <template v-for="(status, index) in statusList" :key="index">
            <button :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
          </template>
        </el-form-item>
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
        </el-form-item>
     
      <el-form-item :style="{ marginLeft: '10px'}">
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
        <div style="float:right">
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;showPagination = 0;" :type="switchValue === 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 1;showPagination = 0;" :type="switchValue === 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 2;showPagination = 1;" :type="switchValue ===2 ? 'primary' : ''" v-show="switchValue ===1"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>
        </div>
      </el-form>
      <el-form
        class="-mb-15px"
        :model="queryDeletedPageParams"
        ref="queryFormRef2"
        :inline="true"
        label-width="68px"    
        v-show="switchValue == 2"                 
      >
      <el-form-item>
        <el-form-item label="网络地址" prop="devKey" >
          <el-autocomplete
            v-model="queryDeletedPageParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"                       
          />
        </el-form-item>        
      
      <el-form-item :style="{ marginLeft: '20px'}">
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
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
        <div style="float:right">
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;showPagination = 0;" :type="switchValue === 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 1;showPagination = 0;" :type="switchValue === 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 2;showPagination = 1;" :type="switchValue ===2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>
        </div>
      </el-form>      
    </template>
    <template #Content>
     <div>
      <el-table  v-show="switchValue == 1" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true" @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="运行状态" align="center" prop="status" >
          <template #default="scope">
            <el-tag  v-if="scope.row.status == 0 && scope.row.apparentPow == 0">空载</el-tag>
            <el-tag  v-if="scope.row.status == 0 && scope.row.apparentPow != 0">正常</el-tag>
            <el-tag type="warning" v-if="scope.row.status == 1">预警</el-tag>
            <el-popover
                placement="top-start"
                title="告警内容"
                :width="500"
                trigger="hover"
                :content="scope.row.pduAlarm"
                v-if="scope.row.status == 2"
              >
                <template #reference>
                  <el-tag type="danger">告警</el-tag>
                </template>
              </el-popover>
            <el-tag type="info" v-if="scope.row.status == 4">故障</el-tag>
            <el-tag type="info" v-if="scope.row.status == 5">离线</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总视在功率(kVA)" align="center" prop="apparentPow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if=" scope.row.apparentPow != null" >
              {{ scope.row.apparentPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="总有功功率(kW)" align="center" prop="pow" width="130px">
          <template #default="scope" >
            <el-text line-clamp="2" v-if=" scope.row.pow != null" >
              {{ scope.row.pow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="功率因素" align="center" prop="pf" width="180px" />
        <!-- 数据库查询 -->
        <el-table-column label="总电能(kWh)" align="center" prop="ele" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if=" scope.row.ele != null" >
              {{ scope.row.ele }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" /> 
        <el-table-column label="操作" align="center">
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
     </div> 
      <!-- 查看已删除PDU设备 -->
      <el-table  v-show="switchValue == 2" v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="运行状态" align="center" prop="status" >
          <template #default="scope">
            <el-tag type="info" v-if="scope.row.deleted">已删除</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" /> 
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.devKey)"
              v-if="scope.row.status == 5"
            >
              恢复设备
            </el-button>
          </template>
        </el-table-column>
      </el-table>
        <Pagination
        v-if="showPagination == 1"
        :total="deletedTotal"
        :page-size-arr="pageSizeArr"
        v-model:page="queryDeletedPageParams.pageNo"
        v-model:limit="queryDeletedPageParams.pageSize"
        @pagination="getDeletedList"
        />               
      <!-- 阵列模式分页 --> 
      <div class="arrayContainer" v-show="!switchValue && list.length > 0"> 
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="icon">
              <div v-if="item.pf != null">
                {{item.pf}}<br/>
                <span class="text-pf">PF</span>
              </div>                    
            </div>
            <div class="info">
              
              <div v-if=" item.pow != null ">有功功率：{{item.pow}}kW</div>    
              <div v-if="item.apparentPow != null">视在功率：{{item.apparentPow}}kVA</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status">
            <el-tag  v-if="item.status == 0 && item.apparentPow == 0">空载</el-tag>
            <el-tag  v-if="item.status == 0 && item.apparentPow != 0">正常</el-tag>
            <el-tag type="warning" v-if="item.status == 1">预警</el-tag>

            <el-popover
              placement="top-start"
              title="告警内容"
              :width="1000"
              trigger="hover"
              :content="item.pduAlarm"
              v-if="item.status == 2"
            >
              <template #reference>
                <el-tag type="danger">告警</el-tag>
              </template>
            </el-popover>

            <el-tag type="info" v-if="item.status == 4">故障</el-tag>
            <el-tag type="info" v-if="item.status == 5">离线</el-tag>
          </div>
          <button v-if="item.status != null && item.status != 5" class="detail" @click="toPDUDisplayScreen(item)">详情</button>
        </div>      
      </div>
        <Pagination
        v-if="showPagination == 0"
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
        />      
      <template v-if="list.length == 0 && switchValue ==0 && showPagination == 0">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </template>

  </CommonMenu>
  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
// import PDUDeviceForm from './PDUDeviceForm.vue'
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import { get } from 'http'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()
const navList = ref([]) as any // 左侧导航栏树结构列表
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const showPagination = ref(0)
const statusNumber = reactive({
  normal : 0,
  warn : 0,
  alarm : 0,
  offline : 0
})

const devKeyList = ref([])
const loadAll = async () => {
  var data = await PDUDeviceApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  console.log(objectArray)
  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const ip = ref("ip");

const statusList = reactive([
  {
    name: '正常',
    selected: true,
    value: 0,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '预警',
    selected: true,
    value: 1,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn'
  },
  {
    name: '告警',
    selected: true,
    value: 2,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
  {
    name: '故障',
    selected: true,
    value: 4,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
  {
    name: '离线',
    selected: true,
    value: 5,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
])

const handleClick = (row) => {
  if(row.type != null  && row.type == 3){
    queryParams.devKey = row.devKey
    queryDeletedPageParams.devKey = row.devKey
    handleQuery();
  }
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.cabinetIds = null;
    queryDeletedPageParams.cabinetIds = null;
    getDeletedList();
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.cabinetIds = [-1]
    queryDeletedPageParams.cabinetIds = [-1]
  }else{
    queryParams.cabinetIds = ids
    queryDeletedPageParams.cabinetIds = ids
  }

  getList();
  getDeletedList();
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
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null
  }
]) as any// 列表的数据
const allList = ref([
  { 
    id:null,
    status:null,
    apparentPow:null,
    pow:null,
    ele:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null
  }
]) as any//总列表的数据
const deletedList = ref([
  { 
    id:null,
    status:null,
    devKey:null,
    location:null,
    dataUpdateTime : "",
  }
]) as any//已经删除的PDU设备
const total = ref(0) // 列表的总页数
const deletedTotal = ref(0) // 已删除PDU设备列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
}) as any
const queryParamsAll = reactive({
  pageNo: 1,
  pageSize: -1,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
}) as any
const queryDeletedPageParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
}) as any
const queryFormRef = ref() // 搜索的表单
const queryFormRef2 = ref()
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    // var normal = 0;
    // var offline = 0;
    // var alarm = 0;
    // var warn = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.dataUpdateTime == null && obj?.pow == null){
        obj.status = 5;
        //offline++;
        return;
      }
      const splitArray = obj.dataUpdateTime.split(' ');
      obj.dataUpdateTime = splitArray[1];
      
      obj.apparentPow = obj.apparentPow.toFixed(3);
      obj.pow = obj.pow.toFixed(3);
      obj.ele = obj.ele.toFixed(1);
      obj.pf = obj.pf.toFixed(2);
      
      // if(obj.status == 0){
      //   normal++;
      // } else if (obj.status == 1){
      //   warn++;
      // } else if (obj.status == 2){
      //   alarm++;
      // } 
    });
    // const allData = await PDUDeviceApi.getPDUDevicePage(queryParamsAll);
    // allList.value = allData.list
    // allList.value.forEach((objAll) => {
    //   if(objAll?.dataUpdateTime == null && objAll?.pow == null){
    //     objAll.status = 5;
    //     offline++;
    //     return;
    //   }  
    //   if(objAll?.status == 0){
    //     normal++;
    //   } else if (objAll?.status == 1){
    //     warn++;
    //   } else if (objAll?.status == 2){
    //     alarm++;
    //   }          
    // });    
    //设置左边数量
    // statusNumber.normal = normal;
    // statusNumber.offline = offline;
    // statusNumber.alarm = alarm;
    // statusNumber.warn = warn;
    total.value = data.total
  } finally {
    loading.value = false
  }
}



const getDeletedList = async () => {
  try {
    const data = await PDUDeviceApi.getDeletedPDUDevice(queryDeletedPageParams);
    deletedList.value = data.list
    var tableIndex = 0;
    deletedList.value.forEach((obj) => {
      obj.tableId = (queryDeletedPageParams.pageNo - 1) * queryDeletedPageParams.pageSize + ++tableIndex;
      const splitArray = obj.dataUpdateTime.split(' ');
      obj.dataUpdateTime = splitArray[1];
    });  
    deletedTotal.value = data.total
  } catch (error) {
    
  }
}

const getListNoLoading = async () => {
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    // var normal = 0;
    // var offline = 0;
    // var alarm = 0;
    // var warn = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.dataUpdateTime == null && obj?.pow == null){
        obj.status = 5;
        //offline++;
        return;
      }
      const splitArray = obj?.dataUpdateTime?.split(' ');
      obj.dataUpdateTime = splitArray[1];
      
      obj.apparentPow = obj?.apparentPow?.toFixed(3);
      obj.pow = obj?.pow?.toFixed(3);
      obj.ele = obj?.ele?.toFixed(1);
      obj.pf = obj?.pf?.toFixed(2);

      // if(obj?.status == 0){
      //   normal++;
      // } else if (obj?.status == 1){
      //   warn++;
      // } else if (obj?.status == 2){
      //   alarm++;
      // }
    });
    // const allData = await PDUDeviceApi.getPDUDevicePage(queryParamsAll);
    // allList.value = allData.list
    // allList.value.forEach((objAll) => {
    //   if(objAll?.dataUpdateTime == null && objAll?.pow == null){
    //     objAll.status = 5;
    //     offline++;
    //     return;
    //   }  
    //   if(objAll?.status == 0){
    //     normal++;
    //   } else if (objAll?.status == 1){
    //     warn++;
    //   } else if (objAll?.status == 2){
    //     alarm++;
    //   }          
    // });
    // //设置左边数量
    // statusNumber.normal = normal;
    // statusNumber.offline = offline;
    // statusNumber.alarm = alarm;
    // statusNumber.warn = warn;

    total.value = data.total
  } catch (error) {
    
  }
}

const getListAll = async () => {
  try {
    var normal = 0;
    var offline = 0;
    var alarm = 0;
    var warn = 0;
    const allData = await PDUDeviceApi.getPDUDevicePage(queryParamsAll);
    allList.value = allData.list
    allList.value.forEach((objAll) => {
      if(objAll?.dataUpdateTime == null && objAll?.pow == null){
        objAll.status = 5;
        offline++;
        return;
      }  
      if(objAll?.status == 0){
        normal++;
      } else if (objAll?.status == 1){
        warn++;
      } else if (objAll?.status == 2){
        alarm++;
      }          
    });
    //设置左边数量
    statusNumber.normal = normal;
    statusNumber.offline = offline;
    statusNumber.alarm = alarm;
    statusNumber.warn = warn;
  } catch (error) {
    
  }
}

// 接口获取导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomList({})
  let arr = [] as any
  for (let i=0; i<res.length;i++){
  var temp = await CabinetApi.getRoomPDUList({id : res[i].id})
  arr = arr.concat(temp);
  }
  navList.value = arr
}

import { useRouter } from 'vue-router';

const router = useRouter();

const toPDUDisplayScreen = (row: { devKey: string; location: string; id: number }) => {
  const { devKey, location, id } = row;
  router.push({
    path: '/pdu/pdudisplayscreen',
    query: { devKey, id: id.toString(), location }
  });
};

// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

const handleSelectStatus = (index) => {
  statusList[index].selected = !statusList[index].selected
  const status =  statusList.filter(item => item.selected)
  const statusArr = status.map(item => item.value)
  queryParams.status = statusArr;
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  queryDeletedPageParams.pageNo = 1
  getList()
  getDeletedList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  queryFormRef2.value.resetFields()
  statusList.forEach((item) => item.selected = true)
  queryParams.status = [];
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
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
    await getList()
  } catch {}
}

/** 恢复按钮操作 */
const handleRestore = async (devKey: string) => {
  try {
     // 恢复的二次确认
    await message.restoreConfirm()
    // 发起恢复请求
    await PDUDeviceApi.restorePDUDevice({
      devKey: devKey
    })
    message.success(t('common.restoreSuccess'))
    // 刷新列表
    await getDeletedList();
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
  devKeyList.value = await loadAll();
  getList()
  getNavList();
  getListAll();
  flashListTimer.value = setInterval((getListNoLoading), 5000);
  flashListTimer.value = setInterval((getListAll), 5000);
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
    firstTimerCreate.value = false;
  }
})

onActivated(() => {
  getList();
  getNavList();
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
    flashListTimer.value = setInterval((getListAll), 5000);
  }
})
</script>

<style scoped lang="scss">
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
      background-color:  #f56c6c;
    }
  }
}

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
      height: 100%;
      .icon {
        font-size: 20px;
        width: 60px;
        height: 30px;
        margin: 0 25px 39px;
        text-align: center;
          .text-pf{
          font-size: 16px;
        }
      }
      .info{
        font-size: 16px;
        margin-bottom: 20px;
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
::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;

}
</style>
