<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="navList" navTitle="需量监测">
    <template #NavInfo>
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/PDU.jpg" /></div>
        </div> -->
        <!-- <div class="line"></div>       -->
        <!-- <div class="status">
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
        </div> -->
      <!-- <div class="nav_data">

      </div>  -->
      <div class="nav_data">
      <br/> 
    <div class="descriptions-container" style="font-size: 14px;">

    <div v-show="item.location !== null" v-for="item in maxCurAll" :key="item.devKey" style="  margin-top: 15px;margin-left: 10px;">
      <div>{{ item.location}}</div>
    </div>      
    <div v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span>网络地址 :</span>
      <span>{{ item.devKey}}</span>
    </div>    
    <div  v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span >发生时间 :</span>
      <span class="value">{{ item.l1MaxCurTime }}</span>
    </div>
    <div v-for="item in maxCurAll" :key="item.devKey" class="description-item">
      <span>最大电流 :</span>
      <span>{{ item.l1MaxCur}}A</span>
    </div>    

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
        <el-form-item label="时间段" prop="createTime" label-width="60px">
          <el-button 
            @click="queryParams.timeType = 0;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;handleQuery();showSearchBtn = false" 
            :type="queryParams.timeType == 0 ? 'primary' : ''"
          >
            最近24小时
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;handleMonthPick();showSearchBtn = false;" 
            :type="queryParams.timeType == 1 ? 'primary' : ''"
          >
            月份
          </el-button>
          <el-button 
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;showSearchBtn = true;" 
            :type="queryParams.timeType == 2 ? 'primary' : ''"
          >
            自定义
          </el-button>                            
        </el-form-item>
        <el-form-item >
          <el-date-picker
            v-if="queryParams.timeType == 1"
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="month"
            :disabled-date="disabledDate"
            @change="handleMonthPick"
            class="!w-160px"
          />
          <el-date-picker
            v-if="queryParams.timeType == 2"
            v-model="queryParams.timeArr"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            :shortcuts="shortcuts"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-190px"
          />
        <el-form-item v-show="showSearchBtn" style="margin-left: 10px">
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

        <div style="float:right ">
          <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 2px" />电流</el-button>
          <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 2px" />功率</el-button>          
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 1;" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 2;" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
    <!-- 三相数据显示 -->
      <el-table v-show="switchValue == 2 && valueMode == 0 && MaxLineId > 1" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true"  @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" width="180px" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="125px"/>
        <el-table-column label="L1最大电流(kA)" align="center" prop="l1MaxCur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxCurTime"/>
        <el-table-column label="L2最大电流(A)" align="center" prop="l2MaxCur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-show="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l2MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxCurTime"  />
        <el-table-column label="L3最大电流(A)" align="center" prop="l3MaxCur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-show="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l3MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l3MaxCurTime" />

        <el-table-column label="操作" align="center" width="130px">
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
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>  
    <!-- 单相数据显示 -->
      <el-table v-show="switchValue == 2 && valueMode == 0 && !(MaxLineId > 1)" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true" @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" />
        <el-table-column label="最大电流(kA)" align="center" prop="l1MaxCur"  >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxCur }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxCurTime"/>

        <el-table-column label="操作" align="center" width="130px">
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
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table> 
    <!-- 三相有数据显示 -->      
      <el-table v-show="switchValue == 2 && valueMode == 1 && MaxLineId > 1" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true" @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <el-table-column label="所在位置" align="center" prop="location" width="180px" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="125px"/>
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
            <el-text line-clamp="2" v-show="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l2MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxPowTime" />
        <el-table-column label="L3最大功率(kW)" align="center" prop="l3MaxPow" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-show="scope.row.l2MaxCur !== null && scope.row.l2MaxCur !== undefined ">
              {{ scope.row.l3MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l3MaxPowTime" />
        <el-table-column label="操作" align="center" width="130px">
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
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    <!-- 单相数据显示 -->      
      <el-table v-show="switchValue == 2 && valueMode == 1 && !(MaxLineId > 1)" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true" @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <el-table-column label="所在位置" align="center" prop="location"  />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip" width="125px"/>
        <el-table-column label="最大功率(kW)" align="center" prop="l1MaxPow"  >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxPow }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxPowTime" />
        <el-table-column label="操作" align="center" width="130px">
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
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>     
      <div  v-show="switchValue == 1 && list.length > 0  && valueMode == 1" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content" v-show="item.l3MaxPow !== undefined && item.l3MaxPow !== null">
            <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div>
            <div class="info">
              <div >L1最大功率：{{item.l1MaxPow}}kW</div>
              <div >L2最大功率：{{item.l2MaxPow}}kW</div>
              <div >L3最大功率：{{ item.l3MaxPow }}kW</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>
          <div class="content" v-show="item.l3MaxPow == undefined || item.l3MaxPow == null">
            <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxPow,L2:item.l2MaxPow,L3:item.l3MaxPow}" /></div>
            <div class="info">
              <div >最大功率：{{item.l1MaxPow}}kW</div>
              <div >发生时间：{{item.l1MaxPowTime}}</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>          
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->              
          <button class="detail" @click="toPDUDisplayScreen(item)" v-if="item.status != null && item.status != 5">详情</button>
        </div>
      </div>

      <div  v-show="switchValue == 1 && list.length > 0 && valueMode == 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content" v-show="item.l3MaxCur !== undefined && item.l3MaxCur !== null">
            <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>
            <div class="info">
              
              <div >L1最大电流：{{item.l1MaxCur}}A</div>
              <div >L2最大电流：{{item.l2MaxCur}}A</div>
              <div >L3最大电流：{{ item.l3MaxCur }}A</div>
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>
          <div class="content" v-show="item.l3MaxCur == undefined || item.l3MaxCur == null">
            <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>
            <div class="info">
              
              <div >最大电流：{{item.l1MaxCur}}A</div>
              <div >发生时间：{{item.l1MaxCurTime}}</div>

              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
          </div>          
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->                
          <button class="detail" @click="toPDUDisplayScreen(item)" v-if="item.status != null && item.status != 5">详情</button>
        </div>
      </div>

      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

      <template v-if="list.length == 0 && switchValue != 2">
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
import Pie from './component/Pie.vue'
// import PDUDeviceForm from './PDUDeviceForm.vue'
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })
const navList = ref([]) as any // 左侧导航栏树结构列表
const { push } = useRouter()
const MaxLineId = ref(0);
const valueMode = ref(0);
const now = ref()
const showSearchBtn = ref(false)
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(1)
// const statusNumber = reactive({
//   normal : 0,
//   warn : 0,
//   alarm : 0,
//   offline : 0
// })
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

const handleClick = (row) => {
  console.log('Button clicked!', row);
  if(row.type != null  && row.type == 3){
    queryParams.devKey = row.devKey
    handleQuery();
  }
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.cabinetIds = null;
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
  }else{
    queryParams.cabinetIds = ids
  }

  getList();
}


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
    dataUpdateTime : "",
    pduAlarm:"",
    pf:null
  }
]) as any// 列表的数据
const maxCurAll = ref([
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
    const data = await PDUDeviceApi.getPDULinePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    list.value.forEach((obj) => {

      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      obj.l1MaxCur = obj.l1MaxCur?.toFixed(1);
      obj.l1MaxVol = obj.l1MaxVol?.toFixed(1);
      obj.l1MaxPow = obj.l1MaxPow?.toFixed(3);
      obj.l2MaxCur = obj.l2MaxCur?.toFixed(1);
      obj.l2MaxVol = obj.l2MaxVol?.toFixed(1);
      obj.l2MaxPow = obj.l2MaxPow?.toFixed(3);
      obj.l3MaxCur = obj.l3MaxCur?.toFixed(1);
      obj.l3MaxVol = obj.l3MaxVol?.toFixed(1);
      obj.l3MaxPow = obj.l3MaxPow?.toFixed(3);
    });
    const allData = await PDUDeviceApi.getPDUDeviceMaxCur(queryParams)
    maxCurAll.value = allData.list
    maxCurAll.value.forEach((obj) => {
      obj.l1MaxCur = obj.l1MaxCur?.toFixed(1);
    })
    total.value = data.total
  } finally {
    loading.value = false
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

// const toPDUDisplayScreen = (row) =>{
//   push('/pdu/pdudisplayscreen?devKey=' + row.devKey + '&location=' + row.location + '&id=' + row.id);
// }
import { useRouter } from 'vue-router';

const router = useRouter();
const toPDUDisplayScreen = (row: { devKey: string; location: string; id: number }) => {
  const { devKey, location } = row;
  router.push({
    path: '/pdu/pdudisplayscreen',
    query: { devKey,  location }
  });
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  if(queryParams.timeType != 0 && queryParams.oldTime == null ){
    return;
  }
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
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
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await PDUDeviceApi.deletePDUDevice(id)
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
/** 获得es历史数据PDU相id的最大值 */
const giveValue = async () => {
  MaxLineId.value = await PDUDeviceApi.getPDUMaxLineId(queryParams)
  // console.log(MaxLineId.value)
}
/** 初始化 **/
onMounted(() => {
  giveValue()
  getList()
  getNavList();

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
.description-item {
  display: flex;
  align-items: left;
  margin-top: 15px;
  margin-left: 10px;
  
}

.label {
  width:100px; /* 控制冒号前的宽度 */
  //text-align: right; /* 文本右对齐 */
  //margin-right: 10px; /* 控制冒号后的间距 */
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
  
}
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 80px;

}  
</style>
