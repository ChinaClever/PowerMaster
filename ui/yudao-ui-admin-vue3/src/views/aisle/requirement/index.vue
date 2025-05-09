<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="需量监测">
    <template #NavInfo>
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/aisle.png" /></div>
        </div>
        <div class="line"></div> -->
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
        <!-- <div class="line"></div> -->
         <div style="margin-left: 25px;margin-top: 30px;font-size: medium;">
          <span>最大视在功率</span>
          <br/>
          <span>A路：{{ maxA }}</span>
          <br/>
          <span>B路：{{ maxB }}</span>
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

        <el-form-item label="时间段" prop="createTime" label-width="100px" style="vertical-align: center; position: relative; left: -30px;">
          <el-button id="latest24h"
            @click="queryParams.timeType = 0;queryParams.oldTime = dayjs(new Date()).subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss');queryParams.newTime = dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss');queryParams.timeArr = null;handleQuery()" 
            :type="queryParams.timeType == 0 ? 'primary' : ''"
          >
            最近24小时
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;handleMonthPick();handleQuery()" 
            :type="queryParams.timeType == 1 ? 'primary' : ''"
          >
            月份
          </el-button>
          <el-button 
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;" 
            :type="queryParams.timeType == 2 ? 'primary' : ''"
          >
            自定义
          </el-button>                            
        </el-form-item>
        <el-form-item label="柜列名称" prop="name" label-width="100px" style="position: relative; left: -220px;">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入柜列名称"
            clearable
            style="width: 200px"
            @keydown.enter.prevent="handleQuery"
          />
        </el-form-item>
        <el-form-item style="position: absolute; left: 720px;">
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        </el-form-item>
        <el-form-item style="position: absolute; left: 725px; vertical-align: center;">
          <el-date-picker
            v-if="queryParams.timeType == 1"
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="month"
            :disabled-date="disabledDate"
            @change="handleMonthPick();handleQuery();"
            class="!w-160px"
          />
          <el-date-picker
            v-if="queryParams.timeType == 2"
            v-model="queryParams.timeArr"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            @change="handleDayPick();handleQuery();"
            class="!w-200px"
          />
        </el-form-item>
        <!-- <el-form-item style="position: absolute; left: 550px;" vertical-align="center">
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
        </el-form-item> -->
        <el-radio-group v-show="switchValue == 1" v-model="apparentOrActive" size="default" style="position: absolute; right:250px; vertical-align: center;">
          <el-radio-button label="视在功率" value="视在功率" />
          <el-radio-button label="有功功率" value="有功功率" />
        </el-radio-group>
        <div style="float:right;vertical-align: center;">
          <!-- <el-button @click="visMode = 0;" :type="visMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />电流</el-button> -->
          <!-- <el-button @click="visMode = 1;" :type="visMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />功率</el-button> -->
          <el-button @click="if(switchValue!=0){pageSizeArr=[24,36,48];queryParams.pageSize = 24;switchValue = 0;queryParams.pageNo=1;handleQuery();}" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />阵列模式</el-button>
          <el-button @click="if(switchValue!=1){pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;switchValue = 1;queryParams.pageNo=1;handleQuery();apparentOrActive='视在功率';}" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <!-- <el-table v-show="switchValue == 1 && visMode == 0" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openDetail" >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" />

        <el-table-column label="所在位置" align="center" prop="location" width="180px" />
        <el-table-column label="L1最大电流" align="center" prop="l1MaxCur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l1MaxCur }}kA
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l1MaxCurTime" />
        <el-table-column label="L2最大电流" align="center" prop="l2MaxCur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l2MaxCur }}A
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" align="center" prop="l2MaxCurTime" />
        <el-table-column label="L3最大电流" align="center" prop="l3MaxCur" width="100px" >
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.l3MaxCur }}A
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
      </el-table> -->

      <el-table style="height: calc(100vh - 215px);" v-show="switchValue == 1 && visMode == 1 &&apparentOrActive=='视在功率'" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openDetail" :header-cell-style="headerCellStyle" :border="true">
        <el-table-column width="65" label="序号" align="center">
            <template #default="scope">
              {{ (queryParams.pageNo - 1) * queryParams.pageSize + scope.$index + 1 }}
            </template>  
          </el-table-column>
        <el-table-column label="所在位置" align="center" prop="location"  width="250" />
        <el-table-column label="总共最大功率(KVA)" align="center" prop="maxApparentTotal"/>
        <el-table-column label="发生时间" align="center" prop="maxApparentTotalTime"  width="170"/>
        <el-table-column label="A路最大功率(KVA)" align="center" prop="maxApparentA" />
        <el-table-column label="发生时间" align="center" prop="maxApparentATime" width="170" />
        <el-table-column label="B路最大功率(KVA)" align="center" prop="maxApparentB"/>
        <el-table-column label="发生时间" align="center" prop="maxApparentBTime" width="170"/>
        <el-table-column label="操作" align="center" width="100px">
          <template #default="scope">
            <el-button
              type="primary"
              @click="queryParams.lineType = 1;openDetail(scope.row)"

            >
              详情
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
      <el-table style="height: calc(100vh - 215px);" v-show="switchValue == 1 && visMode == 1 &&apparentOrActive=='有功功率'" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openDetail" :header-cell-style="headerCellStyle" :border="true">
        <el-table-column width="65" label="序号" align="center">
            <template #default="scope">
              {{ (queryParams.pageNo - 1) * queryParams.pageSize + scope.$index + 1 }}
            </template>  
          </el-table-column>
        <el-table-column label="所在位置" align="center" prop="location"  width="250" />
        <el-table-column label="总共最大功率(KW)" align="center" prop="maxPowTotal"/>
        <el-table-column label="发生时间" align="center" prop="maxPowTotalTime"  width="170"/>
        <el-table-column label="A路最大功率(KW)" align="center" prop="maxPowA" />
        <el-table-column label="发生时间" align="center" prop="maxPowATime" width="170" />
        <el-table-column label="B路最大功率(KW)" align="center" prop="maxPowB"/>
        <el-table-column label="发生时间" align="center" prop="maxPowBTime" width="170"/>
        <el-table-column label="操作" align="center" width="100px">
          <template #default="scope">
            <el-button
              type="primary"
              @click="queryParams.lineType = 1;openDetail(scope.row)"

            >
              详情
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
      <div style="height: calc(100vh - 215px);" v-show="switchValue == 0 && visMode == 1 && list.length > 0" class="arrayContainer" v-loading="loading">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <!-- <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.maxPowTotal,L2:item.maxPowA,L3:item.maxPowB}" /></div> -->
            <!-- <div class="info">
              <div >总共：{{item.maxPowTotal}}kW</div>
              <div >A路 ：{{item.maxPowA}}kW</div>
              <div >B路 ：{{item.maxPowB }}kW</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            <!-- </div> -->
            <div class="lien"><span class="left"></span><span class="middle">视在功率</span> <span class="right">有功功率</span></div><br/>
            <div class="lien"><span class="left">A路</span><span class="middle">{{ item.maxApparentA  }}kVA</span> <span class="right">{{ item.maxPowA  }}kW</span></div><br/>
            <div class="lien"><span class="left">B路</span><span class="middle">{{ item.maxApparentB }}kVA</span> <span class="right">{{ item.maxPowB }}kW</span></div>
          </div>
          <!-- </div> -->
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->   
          <div class="status"  >
            <el-tag>需量功率</el-tag>
          </div>           
          <button class="detail" @click="queryParams.lineType = 1;openDetail(item)" >详情</button>
        </div>
      </div>

      <!-- <div  v-show="switchValue == 0 && visMode == 0 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div style="padding: 0 28px"><Pie :width="50" :height="50" :max="{L1:item.l1MaxCur,L2:item.l2MaxCur,L3:item.l3MaxCur}" /></div>
            <div class="info">            
              <div >A相：{{item.l1MaxCur}}A</div>
              <div >B相：{{item.l2MaxCur}}A</div>
              <div >C相：{{ item.l3MaxCur }}A</div>
            </div>
          </div> 
          <div class="status"  >
            <el-tag>需量电流</el-tag>
          </div>                
          <button class="detail" @click="queryParams.lineType = 0;openDetail(item)" >详情</button>
        </div>
      </div> -->

      <!-- <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      /> -->
      <Pagination
          :total="total"
          :page-size="queryParams.pageSize"
          :page-sizes="pageSizeArr"
          :current-page="queryParams.pageNo"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />

      <template v-if="list.length == 0 && switchValue != 2">
        <el-empty description="暂无数据" :image-size="300" />
      </template>

      <el-dialog v-model="detailVis" :title="queryParams.lineType == 0 ? `电流详情`: `功率详情`"  width="70vw" height="58vh" >
        <div class="tagInDialog">
          <span style="position: relative;left: -270px;">机房：{{ location.split("-")[0] }}<span v-for="i in Array(5)" :key="i">&nbsp;</span>柜列：{{ location.split("-")[1] }} <span v-for="i in Array(5)" :key="i">&nbsp;</span> 结果所在时间段: {{ startTime }}&nbsp;&nbsp;到&nbsp;&nbsp;{{ endTime }}</span> 
          <span style="position: relative;right: -220px">
            <!-- <el-button
              @click="clickChart"
              :type="isChart? 'primary' : ''"
            >
              图表
            </el-button>
            <el-button
              @click="clickData"
              :type="!isChart ? 'primary' : ''"
            >
              数据
            </el-button> -->
            <el-button type="success" plain @click="handleExport" :loading="exportLoading">
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </span>
        </div>
        <div v-show="isChart">
          <RequirementLine width="68vw" height="58vh" :list="requirementLine"  />
        </div>
        <!-- <el-table v-show="!isChart"  :data="tableData" stripe style="width: 100%">
          <el-table-column label="时间" align="center" prop="time" />
        </el-table> -->
      </el-dialog>
    </template>
  </CommonMenu>
  
  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/aisle/aisleindex'
import Pie from './component/Pie.vue'
import RequirementLine from './component/RequirementLine.vue'
// import PDUDeviceForm from './PDUDeviceForm.vue'
import { dayjs, ElTree } from 'element-plus'
import { time } from 'console'
import { is } from '@/utils/is'
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

// const { push } = useRouter()
const apparentOrActive=ref();
const startTime = ref() as any;
const endTime = ref() as any;
const location = ref();
const visMode = ref(1);
const requirementLine = ref([]) as any;
const detailVis = ref(false);
const now = ref()
const pageSizeArr = ref([24,36,48])
const switchValue = ref(0)
const maxA=ref("")
const maxB=ref("")
const openDetailFlag=ref("0")
// const statusNumber = reactive({
//   normal : 0,
//   warn : 0,
//   alarm : 0,
//   offline : 0
// })
const isChart=ref(true)

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
  if(row.type != null  && row.type == 3){
    queryParams.devKey = row.devKey
    handleQuery();
  }
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.aisleIds = null;
    getList();
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

  getList();
}

const serverRoomArr =  ref([])

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

watch(openDetailFlag,(val) => {
  if(val == "1") {
    openDetail({id: query.id,location: query.location})
  }
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
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  name:null,
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
  oldTime : dayjs(new Date()).subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
  newTime : dayjs(new Date()).format('YYYY-MM-DD HH:mm:ss'),
}) as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中



/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getAisleLinePage(queryParams)
    list.value = data.list
    var tableIndex = 0;
    list.value.forEach((obj) => {

      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      obj.maxPowTotal = obj.maxPowTotal?.toFixed(3);
      obj.maxPowA = obj.maxPowA?.toFixed(3);
      obj.maxPowB = obj.maxPowB?.toFixed(3);
      
      obj.maxApparentTotal = obj.maxApparentTotal?.toFixed(3);
      obj.maxApparentA=obj.maxApparentA?.toFixed(3);
      obj.maxApparentB=obj.maxApparentB?.toFixed(3);

      obj.maxPowTotalTime=obj.maxPowTotalTime?.substring(0,16)
      obj.maxPowATime=obj.maxPowATime?.substring(0,16)
      obj.maxPowBTime=obj.maxPowBTime?.substring(0,16)

      obj.maxApparentTotalTime=obj.maxApparentTotalTime?.substring(0,16)
      obj.maxApparentATime=obj.maxApparentATime?.substring(0,16)
      obj.maxApparentBTime=obj.maxApparentBTime?.substring(0,16)
    });

    total.value = data.total
    showMax();
  } finally {
    loading.value = false
  }
}

const getNavList = async() => {
  const res = await IndexApi.getAisleMenu()
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
  queryParams.id = row.id;
  const lineData = await IndexApi.getAisleLineCurLine(queryParams);
  requirementLine.value = lineData;
  requirementLine.value.formatter = queryParams.lineType == 0 ? '{value} A' : '{value} kW';
  // requirementLine.value.series.splice(1,0,requirementLine.value.series.pop());
  console.log('requirementLine',requirementLine.value);
  location.value = row?.location 
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
  
}
/** 重置按钮操作 */
const resetQuery = () => {
  queryParams.name = undefined
  if(queryParams.timeType != 0 && queryParams.oldTime == null ){
    return;
  }
  getList()
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
    exportLoading.value = true
    const data = await IndexApi.getAislePFDetailExcel(queryParams)
    download.excel(data, '需量检测.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

function headerCellStyle() {
    return {
      backgroundColor: '#eee', // 表头背景颜色
    };
  }
/** 初始化 **/
onMounted(async () => {
  // const res=await IndexApi.getMaxApparentPower({});
  // console.log('maxApparentPower',res)
  getList()
  getNavList();
  showMax();
  openDetailFlag.value = query.openDetailFlag || "0"
})
async function showMax(){
  const res = await IndexApi.getMaxApparentPower(queryParams);
  console.log('maxApparentPower',res);
  maxA.value=res.A.roomName+"-"+res.A.aisleName;
  maxB.value=res.B.roomName+"-"+res.B.aisleName;
}

function handleSizeChange(val) {
  queryParams.pageSize = val
  queryParams.pageNo = 1
  getList()
}
function handleCurrentChange(val) {
  queryParams.pageNo = val
  getList()
}

function handleFreeDay(){
  let oldTime=new Date();
  oldTime.setHours(0);
  oldTime.setMinutes(0);
  oldTime.setSeconds(0);
  queryParams.oldTime = dayjs(oldTime).format("YYYY-MM-DD HH:mm:ss");
  queryParams.newTime=dayjs(new Date()).format("YYYY-MM-DD HH:mm:ss");
  queryParams.timeArr=[queryParams.oldTime,queryParams.newTime];
}

function clickChart(){
  if(isChart.value)return;
  isChart.value=true;
}
function clickData(){
  if(!isChart.value)return;
  isChart.value=false;
}
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
  // display: flex;
  flex-wrap: wrap;
  .arrayItem {
    display: inline-block;
    width: 25%;
    height: 160px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
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
.content{
  top:40px;
  display: inline-block;
  position: absolute;
  left: 5%;
  div{
    margin-top: 10px;
    display: inline-block;
    span{
      text-align: center;
    }
    .left{
      display: inline-block;
      width: 25px;
    }
    .middle{
      display: inline-block;
      width: 120px;
    }
    .right{
      display: inline-block;
      width: 120px;
    }
  }
}
.tagInDialog{
  position: absolute;
  left: 30%;
  top: 22px;
}
</style>
