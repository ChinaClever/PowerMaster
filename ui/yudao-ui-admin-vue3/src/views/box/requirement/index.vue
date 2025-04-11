<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="需量监测">
    <template #NavInfo>
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div> -->
        <div style="font-size:14px; margin-top:45px; margin-left:20px">
          <div ><span>{{ titleName }}</span>
          </div>
          <div>
            <span>位置：</span>
            <span>{{ statusNumber.location }}</span>
          </div>
          <div >
            <span>名称：</span>
            <span>{{ statusNumber.busName  }}</span>
          </div>
          <div >
            <span>相位：</span>
            <span>{{ statusNumber.lineName }}</span>
          </div>
          <div >
            <span>时间：</span>
            <span>{{ statusNumber.create_time }}</span>
          </div>
          <div >
            <span>{{ flagName }}：</span>
            <span>{{ statusNumber.cur_max_value }}{{ flagName == '功率' ? ' kW' : ' A' }}</span>
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
        label-width="64px"                          
      >
        <el-form-item label="时间段" prop="createTime" label-width="50px">
          <el-button 
            @click="queryParams.timeType = 0;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;handleQuery();showSearchBtn = false" 
            :type="queryParams.timeType == 0 ? 'primary' : ''"
          >
            最近24小时
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;handleMonthPick();showSearchBtn = false" 
            :type="queryParams.timeType == 1 ? 'primary' : ''"
          >
            月份
          </el-button>
          <el-button 
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;showSearchBtn = true" 
            :type="queryParams.timeType == 2 ? 'primary' : ''"
          >
            自定义
          </el-button>     
          <el-date-picker  
            style="padding-left: 10px;"
            v-if="queryParams.timeType == 1"
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="month"
            :disabled-date="disabledDate"
            @change="handleMonthPick"
            class="!w-160px"
          />
          <el-date-picker  
            style="padding-left: 10px;"
            v-if="queryParams.timeType == 2"
            v-model="queryParams.timeArr"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="datetimerange"
            :shortcuts="shortcuts"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-200px"
          />                       
        </el-form-item>

        <el-form-item>
          <span>网络地址：</span>
          <el-form-item prop="devKey">
            <el-autocomplete
              v-model="queryParams.devKey"
              :fetch-suggestions="querySearch"
              clearable
              class="!w-200px"
              placeholder="请输入网络地址"
              @select="handleQuery"
            />
            <el-form-item style="margin-left: 10px">
              <el-button @click="handleQuery"
                ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button
              >
              <el-button @click="resetQuery"
                ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button
              >
            </el-form-item>
          </el-form-item>
        </el-form-item>
        <div style="float:right">
          <el-button @click="visModeShow(0)" :type="visMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />电流</el-button>
          <el-button @click="visModeShow(1)" :type="visMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />功率</el-button>
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 1;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <div v-if="switchValue !== 2  && list.length > 0" class="table-height">
        <el-table v-show="switchValue == 1 && visMode == 0 && list.length > 0" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" width="180px">
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.location != scope.row.devKey ? scope.row.location : '未绑定' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="180px" />
        <el-table-column label="L1最大电流(A)" align="center" prop="l1MaxCur" width="120px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxCurTime" />
        <el-table-column label="L2最大电流(A)" align="center" prop="l2MaxCur" width="120px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l2MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxCurTime" />
        <el-table-column label="L3最大电流(A)" align="center" prop="l3MaxCur" width="120px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l3MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l3MaxCurTime" />

        <el-table-column label="操作" align="center" width="135px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="queryParams.lineType = 0;openDetail(scope.row)"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.boxId)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-table v-show="switchValue == 1 && visMode == 1 && list.length > 0" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <el-table-column label="所在位置" align="center" prop="location" width="180px">
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.location != scope.row.devKey ? scope.row.location : '未绑定' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="180px" />
        <el-table-column label="L1最大功率(kW)" align="center" prop="l1MaxPow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxPowTime" />
        <el-table-column label="L2最大功率(kW)" align="center" prop="l2MaxPow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l2MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxPowTime" />
        <el-table-column label="L3最大功率(kW)" align="center" prop="l3MaxPow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l3MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l3MaxPowTime" />
        <el-table-column label="操作" align="center" width="135px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="queryParams.lineType = 1;openDetail(scope.row)"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"

            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.boxId)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div  v-show="switchValue == 0 && visMode == 1 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info" style="margin-left:10px;font-size: 15px;">
              <div >A相：{{item.l1MaxPow}}kW</div>
              <div >B相：{{item.l2MaxPow}}kW</div>
              <div >C相：{{ item.l3MaxPow }}kW</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div style="padding: 0 28px"><Pie :width="80" :height="80" :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->   
          <div class="status" style="margin-right:-20px;">
            <el-tag>需量功率</el-tag>
          </div>           
          <button class="detail" @click="queryParams.lineType = 1;openDetail(item)" >详情</button>
        </div>
      </div>

      <div  v-show="switchValue == 0 && visMode == 0 && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
        <div v-if="item.devKey !== null" class="arrayItem">
        <!-- <div class="arrayItem" v-for="item in list" :key="item.devKey"> -->
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info" style="margin-left:10px;font-size: 15px;">
              <div >A相：{{item.l1MaxCur}}A</div>
              <div >B相：{{item.l2MaxCur}}A</div>
              <div >C相：{{ item.l3MaxCur }}A</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div style="padding: 0 28px"><Pie :width="80" :height="80" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->    
          <div class="status" style="margin-right:-20px;">
            <el-tag>需量电流</el-tag>
          </div>                
          <button class="detail" @click="queryParams.lineType = 0;openDetail(item)" >详情</button>
        </div>
        </template>
      </div>

      </div>
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

      <template v-if="list.length == 0 && switchValue !== null">
        <el-empty description="暂无数据" :image-size="595" />
      </template>

      <el-dialog v-model="detailVis" width="70vw" height="58vh" >
        <div style="display: flex; align-items: center; justify-content: space-between; width: 100%;margin-top:-50px;">
          <div>
            <span style="font-weight:bold;font-size:20px;margin-right:10px;">{{queryParams.lineType == 0 ? `电流详情`: `功率详情`}}</span>
            <span style="margin-right:10px;">结果所在位置：{{ location }}</span>
            <span>时间段: {{ queryParams.oldTime }}&nbsp;&nbsp;到&nbsp;&nbsp;{{ queryParams.newTime }}</span>
          </div>
          <div style="display: flex; gap: 10px;margin-right:30px;"> <!-- 子div用于包含按钮，并设置按钮之间的间距 -->
            <el-button
              @click="switchChartOrTable = 0"
              :type="switchChartOrTable == 0 ? 'primary' : ''"
            >
              图表
            </el-button>
            <el-button
              @click="switchChartOrTable = 1"
              :type="switchChartOrTable == 1 ? 'primary' : ''"
            >
              数据
            </el-button>
            <el-button type="success" plain @click="handleExport" :loading="exportLoading">
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </div>
        </div>
        <div style="margin-top:3vh;">
          <RequirementLine v-if="switchChartOrTable == 0" width="68vw" height="58vh" :list="requirementLine"  />
          <el-table style="height:550px;ovrflow:hidden;overflow-y:auto;" v-if="switchChartOrTable == 1" :data="pfTableList" :stripe="true" :show-overflow-tooltip="true" >
            <el-table-column label="设备识别码" align="center" prop="devKey" />
            <el-table-column label="相" align="center" prop="line" />
            <el-table-column label="最大电流时间" align="center" prop="cur_max_time" />
            <el-table-column label="最大电流" align="center" prop="cur_max_value" />
            <el-table-column label="最大有功功率时间" align="center" prop="pow_active_max_time" />
            <el-table-column label="最大有功功率" align="center" prop="pow_active_max_value" />
          </el-table>
        </div>

      </el-dialog>
    </template>
  </CommonMenu>
  
  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/bus/boxindex'
import Pie from './component/Pie.vue'
// import PDUDeviceForm from './PDUDeviceForm.vue'
import RequirementLine from './component/RequirementLine.vue'
import { ElTree } from 'element-plus'

/** PDU设备 列表 */
// defineOptions({ name: 'PDUDevice' })

const startTime = ref() as any;
const endTime = ref() as any;
const location = ref();
const roomName = ref();
const visMode = ref(0);
const requirementLine = ref([]) as any;
const detailVis = ref(false);
const now = ref()
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const showSearchBtn = ref(false)
const switchChartOrTable = ref(0)
const pfTableList = ref([]) as any
const titleName = ref();
const flagName = ref();
const statusNumber = reactive({
    location : null,
    devKey : null,
    busName : null,
    lineName : null,
    bus_id : null,
    line_id : null,
    create_time : null,
    cur_max_value : null
})
// 时间段快捷选项
const shortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    },
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    },
  },
  {
    text: '最近六个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 6)
      return [start, end]
    },
  },
]
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
    name: '离线',
    selected: true,
    value: 5,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'

  },
])


const visModeShow = (flag) => {
  if(flag == 0){
    visMode.value =0;
    getListAll(flag);
  }else{
    visMode.value =1;
    getListAll(flag);
  }
}

const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}
const querySearch = async (queryString: string, cb: any) => {
  if(queryString.length>7){
    var results = await IndexApi.boxFindKeys({key:queryString});
    let arr: any[] = [];
    results.map(item => {
      arr.push({value:item})
    });
    cb(arr)
  }else{
      const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  cb(results)
  }
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

const handleClick = (row) => {
  if(row.type != null  && row.type == 3){
    queryParams.devKey = row.devKey
    handleQuery();
  }
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.boxDevKeyList = null;
    getList();
    return;
  }
  const ids = [] as any
  var haveCabinet = false;
  row.forEach(item => {
    if (item.type == 7) {
      ids.push(item.unique)
      haveCabinet = true;
    }
  })
  if(!haveCabinet ){
    queryParams.boxDevKeyList = [-1]
  }else{
    queryParams.boxDevKeyList = ids
  }

  getList();
}

const serverRoomArr =  ref([])

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const getFullTimeByDate = (date) => {
  var year = date.getFullYear();//年
  var month = date.getMonth();//月
  var day = date.getDate();//日
  var hours = date.getHours();//时
  var min = date.getMinutes();//分
  var second = date.getSeconds();//秒
  return year + "-" +
      ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
      (day > 9 ? day : ("0" + day)) + " " +
      (hours > 9 ? hours : ("0" + hours)) + ":" +
      (min > 9 ? min : ("0" + min)) + ":" +
      (second > 9 ? second : ("0" + second));
}

const disabledDate = (date) => {
  // 获取今天的日期
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);

  // 如果date在今天之后，则禁用
  if(queryParams.timeType == 0){
    return date > today;
  }else {
    return date >= today;
  }
  
}

const handleDayPick = () => {
  if(queryParams?.oldTime && queryParams.timeType == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }

 if (queryParams.timeArr && queryParams.timeType == 2) {

    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
  }

  handleQuery();
}

const handleMonthPick = () => {

  if(queryParams.oldTime){
    var newTime = new Date(queryParams.oldTime);
    newTime.setMonth(newTime.getMonth() + 1);
    newTime.setDate(newTime.getDate() - 1);
    newTime.setHours(23,59,59)
    queryParams.newTime = getFullTimeByDate(newTime);

  }else {
    queryParams.newTime = null;
  }
  handleQuery()
} 

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
    roomName:null,
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null
  }
]) as any// 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds:[],
  timeType : 0,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
}) as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中



/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBoxLinePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      obj.l1MaxCur = obj.l1MaxCur?.toFixed(2);
      obj.l1MaxVol = obj.l1MaxVol?.toFixed(2);
      obj.l1MaxPow = obj.l1MaxPow?.toFixed(3);
      obj.l2MaxCur = obj.l2MaxCur?.toFixed(2);
      obj.l2MaxVol = obj.l2MaxVol?.toFixed(2);
      obj.l2MaxPow = obj.l2MaxPow?.toFixed(3);
      obj.l3MaxCur = obj.l3MaxCur?.toFixed(2);
      obj.l3MaxVol = obj.l3MaxVol?.toFixed(2);
      obj.l3MaxPow = obj.l3MaxPow?.toFixed(3);
      obj.l1MaxCurTime = obj.l1MaxCurTime?.slice(0,16);
      obj.l2MaxCurTime = obj.l2MaxCurTime?.slice(0,16);
      obj.l3MaxCurTime = obj.l3MaxCurTime?.slice(0,16);
    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListAll = async (flagVlaue) => {
  try {
    queryParams.flagVlaue = flagVlaue
    const allData = await IndexApi.getBoxLineMax(queryParams)
    //设置左边数量
    statusNumber.location = allData.location?allData.location:allData.devKey;
    statusNumber.devKey = allData.devKey;
    statusNumber.busName = allData.busName;
    statusNumber.lineName = allData.lineName;
    statusNumber.bus_id = allData.bus_id;
    statusNumber.line_id = allData.line_id;
    statusNumber.create_time = allData.create_time;
    if(flagVlaue == 0){
      titleName.value ='最大电流需量';
      flagName.value ='电流';
      statusNumber.cur_max_value = parseFloat(allData.cur_max_value).toFixed(2);
    }else{
      titleName.value ='最大功率需量';
      flagName.value ='功率';
      statusNumber.cur_max_value = parseFloat(allData.pow_active_max_value).toFixed(2);
    }
  } catch (error) {
  }
}

const getNavList = async() => {
  const res = await IndexApi.getBoxMenu()
  serverRoomArr.value = res
  if (res && res.length > 0) {
    const room = res[0]
    const keys = [] as string[]
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type)
        })
      }
    })
  }
}

const openDetail = async (row) =>{
  queryParams.boxId = row.boxId;
  const tableData = await IndexApi.getBoxLineCurLinePage(queryParams)
  pfTableList.value = tableData?.list;
  pfTableList.value.forEach(item => item.cur_max_value = item.cur_max_value.toFixed(2)+'A')
  pfTableList.value.forEach(item => item.pow_active_max_value = item.pow_active_max_value+'KW')

  const lineData = await IndexApi.getBoxLineCurLine(queryParams);
  requirementLine.value = lineData;
  requirementLine.value.formatter = queryParams.lineType == 0 ? '{value} A' : '{value} kW';
  location.value = row.location != null ? row.location : row.devKey
  roomName.value = row.roomName;
  startTime.value = lineData.time[0];
  endTime.value = lineData.time[lineData.time.length - 1];
  detailVis.value = true;
}


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  if(queryParams.timeType != 0 && queryParams.oldTime == null ){
    return;
  }
  getList()
    getListAll(0);
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.devKey = undefined;
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await IndexApi.deleteIndex(id)
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
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await IndexApi.getBoxLineCurLineExcel(queryParams, axiosConfig)
    await download.excel(data, '电流详细.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async() => {
    devKeyList.value = await loadAll();
  getList()
  getNavList();
  getListAll(0);
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

@media screen and (min-width:2048px){
  .table-height{
    height: 76vh;
    overflow: hidden;
    overflow-y: auto;
    margin-top: -10px;
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
      .icon {
        width: 60px;
        height: 30px;
        margin: 0 28px;
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

@media screen and (max-width:2048px) and (min-width:1600px){
  .table-height{
    height: 720px;
    overflow: hidden;
    overflow-y: auto;
    margin-top: -10px;
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
      .icon {
        width: 60px;
        height: 30px;
        margin: 0 28px;
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

@media screen and (max-width:1600px){
  .table-height{
    height: 600px;
    overflow: hidden;
    overflow-y: auto;
    margin-top: -10px;
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
      .icon {
        width: 60px;
        height: 30px;
        margin: 0 28px;
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

::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;
}

:deep(.el-card){
  --el-card-padding:5px;
}
</style>
