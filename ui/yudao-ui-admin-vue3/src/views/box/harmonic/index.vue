<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱谐波">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div> -->
        <div class="status" style="margin-top:20px">
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
              <div class="tag error"></div>告警
            </div>
            <div class="value"><span class="number">{{ statusNumber.alarm }}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <!--<div class="tag error"></div>-->总共
            </div>
            <div class="value"><span class="number">{{ statusNumber.total }}</span>个</div>
          </div>
        </div>
        <div class="line"></div>
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>{{ statusList[0].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>         
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>{{ statusList[1].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>{{ statusList[2].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterTwenty}}</span>个</div>
          </div>
        </div> -->
        <!-- <div class="line"></div> -->
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
      <el-form-item v-if="switchValue == 0 ">
        <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
          全部
        </button>
        <template v-for="(status, index) in statusList" :key="index">
          <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
        </template>
        <!-- <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-if="switchValue == 0 "
          style="height:35px;"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 谐波颜色范围
        </el-button> -->
        </el-form-item>

        <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 10px">
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流谐波</el-button>          
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />谐波含量</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <div v-if="switchValue !== 0 && list.length > 0">
        <el-table v-if="switchValue == 3" style="height:720px;margin-top:-10px;" v-loading="loading" :data="list"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location">
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.location != scope.row.devKey ? scope.row.location : '未绑定' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="设备名称" align="center" prop="boxName" />
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column v-if="valueMode == 0" label="Ia" align="center" prop="acurThd" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.acurThd != null">
              {{ scope.row.acurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ib" align="center" prop="bcurThd" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bcurThd != null">
              {{ scope.row.bcurThd }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="Ic" align="center" prop="ccurThd" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ccurThd != null">
              {{ scope.row.ccurThd }}
            </el-text>
          </template>
        </el-table-column>

        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="showDialog(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 5"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.boxId)"
              v-if="scope.row.status == 5"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <div v-else-if="switchValue == 0 && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.devKey !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey +'-' +item.boxName }}</div>
          <div class="content">
            <div class="info" >                  
              <div v-if="item.acurThd != null">Ia:{{item.acurThd}}</div>
              <div v-if="item.bcurThd != null">Ib:{{item.bcurThd}}</div>
              <div v-if="item.ccurThd != null">Ic:{{item.ccurThd}}</div>
            </div>
            <div class="icon">
              <div v-if="item.acurThd != null">
                <br/>
                电流谐波
              </div> 
            </div>   
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <!-- <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.atemStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.atemStatus != 0 || item.btemStatus != 0  || item.ctemStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.status == 0 " >{{statusList[0].name}}</el-tag>
            <el-tag v-else-if="item.status === 1" type="success" >{{statusList[1].name}}</el-tag>
            <el-tag v-else-if="item.status === 2" type="danger" >{{statusList[2].name}}</el-tag>
          </div>          
          <button class="detail" @click="showDialog(item)" v-if="item.status != null && item.status != 0" >详情</button>
        </div>
        </template>
      </div>

      <el-dialog v-model="dialogVisible" @close="handleClose">
        <!-- 自定义的头部内容（可选） -->
        <template #header>
          <el-form
        class="-mb-15px"
        :model="queryParamsCopy"
        ref="queryFormRef"
        :inline="true"
        label-width="120px"
      >      
        <!-- <el-form-item label="网络地址" prop="devKey">
          <el-input
            v-model="queryParams.devKey"
            placeholder="请输入网络地址"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item> -->
        <el-form-item>       
          <!--<el-tag size="large">所在位置：{{ location }}&nbsp;&nbsp;&nbsp; (名称：{{boxName}})</el-tag>-->
          <span>机房：{{ roomName }}&nbsp;&nbsp;</span>
          <span>母线：{{ busName }}&nbsp;&nbsp;</span>
          <span>插接箱：{{boxName}}&nbsp;&nbsp;</span>
          <span>网络地址：{{ devkey }}</span>
          <!--<el-select
            v-model="queryParamsCopy.harmonicType"
            placeholder="请选择"
            style="width: 240px"
          >
            <el-option label="A相电流谐波" :value = 3 />
            <el-option label="B相电流谐波" :value = 4 />
            <el-option label="C相电流谐波" :value = 5 />
          </el-select>-->
          
        </el-form-item>

        <el-form-item>
          <el-button 
            @click="subtractOneDay();handleDayPick()" 
          >
            &lt;
          </el-button>
          <el-date-picker
            v-model="queryParamsCopy.oldTime"
            :clearable = "false"
            :editable = "false"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="date"
            :disabled-date="disabledDate"
            @change="handleDayPick()"
            class="!w-160px"
          />
          <el-button 
            :disabled="clickAdd"
            @click="addtractOneDay();handleDayPick()" 
          >
            &gt;
          </el-button>
          <el-button @click="switchChartOrTable = 0" :type="switchChartOrTable === 0 ? 'primary' : ''">图表</el-button>
          <el-button @click="switchChartOrTable = 1" :type="switchChartOrTable === 1 ? 'primary' : ''">数据</el-button>
          <el-button @click="handleQueryCopy"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button type="success" plain @click="handleExportXLS" :loading="exportLoading">
            <i class="el-icon-download"></i> 导出
          </el-button>
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
        </template>
        <!-- 自定义的主要内容 -->
        <div class="custom-content">
          <div class="custom-content-container">
            <HarmonicLine v-if="switchChartOrTable == 0" width="75vw" height="58vh" :list="abcLineData"/>
            <div v-else-if="switchChartOrTable == 1" style="width: 100%;height:70vh;overflow-y:auto;">
              <el-table :data="tableData" :stripe="true" :show-overflow-tooltip="true" style="height:70vh;">
                <el-table-column label="时间" align="center" prop="time" />
                <el-table-column label="A相电流谐波" align="center" prop="lineOne" />

                <el-table-column label="B相电流谐波" align="center" prop="linetwe" />

                <el-table-column label="C相电流谐波" align="center" prop="linethree" />

              </el-table>
            </div>
          </div>
        </div>
      </el-dialog>

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
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <HarmonicColorForm ref="harmonicColorForm" @success="getList" />
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download';
import { IndexApi } from '@/api/bus/boxindex';
import { ElTree } from 'element-plus';
import HarmonicColorForm from './HarmonicColorForm.vue';
import { BoxHarmonicColorApi } from '@/api/bus/boxharmoniccolor';
import HarmonicRealTime from './component/HarmonicRealTime.vue';
import HarmonicLine from './component/HarmonicLine.vue';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const location = ref() as any;
const devkey = ref() as any;
const busName = ref() as any;
const boxName = ref() as any;
const roomName = ref() as any;
const butColor = ref(0);
const onclickColor = ref(-1);
const dialogVisible = ref(false);

const clickAdd = ref(true)

const harmonicColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const switchChartOrTable = ref(0);
const valueMode = ref(0)

const allData = ref();

const statusNumber = reactive({
  normal : 0,
  alarm : 0,
  offline : 0,
  total : 0
});

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

const queryParamsAll = reactive({
  pageNo: 1,
  pageSize: -1,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
})as any;

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
    pf:null,
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
  }
]) as any;// 列表的数据

const statusList = reactive([
  {
    name: '离线',
    selected: true,
    value: 0,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline'
  },
  {
    name: '正常',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '告警',
    selected: true,
    value: 2,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
])

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

const handleClick = async (row) => {
  if(row.type != null  && row.type == 7){
    queryParams.devKey = row.unique
    const data = await IndexApi.getBoxIdByDevKey({devKey : queryParams.devKey});
    queryParams.boxId = data;
    haveSearch.value = false;
    handleQueryCopy();
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
    pf:null,
    apf : null,
    bpf : null,
    cpf : null,
    temUnbalance : null,
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
  cabinetIds : [],
})as any

const queryParamsCopy = reactive({
  pageNo: 1,
  pageSize: 10,
  harmonicType : 3,
  harmonicArr:[1],
  devKey : undefined,
  boxId: undefined,
  outputNumber : 10,
  createTime: undefined,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0)),
  newTime : undefined,
}) as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBoxHarmonicPage(queryParams)
    list.value = data.list

    //获取颜色范围
    //var range = await BoxHarmonicColorApi.getBoxHarmonicColor();
    //if(range != null){
    //  statusList[0].name = '<' + range.rangeOne + '%';
    //  statusList[1].name = range.rangeTwo + '%-' +  range.rangeThree + "%";
    //  statusList[2].name = '>' + range.rangeFour + '%';
    //}

    var tableIndex = 0;


    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acurThd == null){
        return;
      } 
      obj.acurThd = obj.acurThd?.toFixed(2);
      obj.bcurThd = obj.bcurThd?.toFixed(2);
      obj.ccurThd = obj.ccurThd?.toFixed(2);

    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getBoxHarmonicPage(queryParams)
    list.value = data.list

    //获取颜色范围
    //var range = await BoxHarmonicColorApi.getBoxHarmonicColor();
    //if(range != null){
    //  statusList[0].name = '<' + range.rangeOne + '%';
    //  statusList[1].name = range.rangeTwo + '%-' +  range.rangeThree + "%";
    //  statusList[2].name = '>' + range.rangeFour + '%';
    //}

    var tableIndex = 0;


    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acurThd == null){
        return;
      } 
      obj.acurThd = obj.acurThd?.toFixed(2);
      obj.bcurThd = obj.bcurThd?.toFixed(2);
      obj.ccurThd = obj.ccurThd?.toFixed(2);

    });

    total.value = data.total
  } finally {
  }
}

const getListAll = async () => {
  try {
    const allData = await IndexApi.getBoxIndexStatistics();
    statusNumber.normal = allData.normal;
    statusNumber.offline = allData.offline;
    statusNumber.alarm = allData.alarm;
    statusNumber.total = allData.total;
      } finally {

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

const haveSearch = ref(false);
const harmonicRealTime = ref();
const realTimeVis = ref(false);
const seriesAndTimeArr = ref() as any;
const lineVis = ref(false);
const harmonicLine = ref([]) as any;

const tableData = ref([])

const disabledDate = (date) => {
  // 获取今天的日期
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);

  // 如果date在今天之后，则禁用
  if(switchValue.value == 0){
    return date > today;
  }else {
    return date >= today;
  }
  
}

const abcLineData = ref({});

const getDetail = async () => {

  //if(!haveSearch.value){
  //  const data = await IndexApi.getHarmonicRedis(queryParamsCopy);
  //  harmonicRealTime.value = data;
  //  if(harmonicRealTime.value.times != null){
  //    realTimeVis.value = true;
  //  }else{
  //    realTimeVis.value = false;
  //  }
  //  haveSearch.value = true;
  //}

  const lineData = await IndexApi.getHarmonicLine(queryParamsCopy);
  abcLineData.value = lineData;

  tableData.value = abcLineData.value.lineOne.map((item, index) => ({
      time: abcLineData.value.time[index],
      lineOne: item,
      linetwe: abcLineData.value.linetwe[index],
      linethree: abcLineData.value.linethree[index]
    }));

  //seriesAndTimeArr.value = lineData;
  //if(seriesAndTimeArr.value.time != null && seriesAndTimeArr.value.time?.length > 0){
  //  const filteredSeries = seriesAndTimeArr.value.series.filter((item,index) => queryParamsCopy.harmonicArr.includes(index));
  //  harmonicLine.value.series = filteredSeries;
  //  harmonicLine.value.time = seriesAndTimeArr.value.time;
  //  lineVis.value = true;
  //} else {
  //  lineVis.value = false;
  //}
}

const handleQueryCopy = async () => {
  if(queryParamsCopy.oldTime){

    await getDetail();
  }
}

const handleDayPick = async () => {

  if(queryParamsCopy?.oldTime ){
    var date = new Date(queryParamsCopy.oldTime + 'Z') // 添加 "Z" 表示 UTC 时间

    var today = new Date(); // 今天的日期
    today.setHours(0, 0, 0, 0); // 去掉时间部分，只比较日期


    if (date.getFullYear() == today.getFullYear() && date.getMonth() == today.getMonth() && date.getDate() == today.getDate()) {
      clickAdd.value = true
    } else {
      clickAdd.value = false
    }

    await getDetail();
  } 
}


const subtractOneDay = () => {
  var date = new Date(queryParamsCopy.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间
  date.setDate(date.getDate() - 1); // 减去一天
  clickAdd.value = false

  queryParamsCopy.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParamsCopy.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 加去一天

  var today = new Date(); // 今天的日期
  today.setHours(0, 0, 0, 0); // 去掉时间部分，只比较日期

  if (date.getFullYear() == today.getFullYear() && date.getMonth() == today.getMonth() && date.getDate() == today.getDate()) {
    clickAdd.value = true
  }

  queryParamsCopy.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

watch(() => [queryParamsCopy.harmonicArr], () => {
  if(seriesAndTimeArr.value.series != null && seriesAndTimeArr.value.series?.length > 0){
    const filteredSeries = seriesAndTimeArr.value.series.filter((item,index) => queryParamsCopy.harmonicArr.includes(index));
    harmonicLine.value.series = filteredSeries;
    harmonicLine.value.time = seriesAndTimeArr.value.time;
  }
});

watch(() => [queryParamsCopy.harmonicType], () => {
  haveSearch.value = false;
  handleQueryCopy();
});

const showDialog = async (item) => {
  queryParamsCopy.devKey = item.devKey;
  queryParamsCopy.boxId = item.boxId;
  location.value = item.location ? item.location : '未绑定';
  roomName.value = item.roomName  ? item.roomName : '未绑定';
  devkey.value = item.devKey;
  busName.value = item.busName;
  boxName.value = item.boxName;
  queryParamsCopy.oldTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0))
  clickAdd.value = true
  dialogVisible.value = true;
  await handleQueryCopy();
}

const toDetail = (row) =>{
  const devKey = row.devKey;
  const boxId = row.boxId
  const location = row.location ? row.location : devKey;
  const name = row.boxName
  push({path: '/bus/boxmonitor/boxharmonicdetail', state: { devKey, boxId ,location ,name}})
}

const handleSelectStatus = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.status = [index];
  handleQuery();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.status = [0,1,2];
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  //statusList.forEach((item) => item.selected = true)
  butColor.value = 0;
  queryParams.color = [];
  onclickColor.value = -1;
  handleQuery()
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  harmonicColorForm.value.open(type)
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

const handleExportXLS = async ()=>{
  try {
    // 导出的二次确认
    await message.exportConfirm();
    // 发起导出
    queryParams.pageNo = 1;
    exportLoading.value = true;
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await IndexApi.getBoxHarmonicDetailExcel(queryParamsCopy, axiosConfig);
    console.log("data",data);
    await download.excel(data, '谐波详细.xlsx');
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error);
  } finally {
    exportLoading.value = false;
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await IndexApi.exportIndex(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  getList();
  getNavList();
  getListAll();
  flashListTimer.value = setInterval((getListNoLoading), 5000);
})

onBeforeUnmount(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value);
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value);
    flashListTimer.value = null;
    firstTimerCreate.value = false;
  }
})

onActivated(() => {
  getList();
  getNavList();
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
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
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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

@media screen and (min-width:2048px){
  .arrayContainer {
    width:100%;
    height: 78vh;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
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
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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
  .arrayContainer {
    width:100%;
    height: 600px;
    overflow: hidden;
    overflow-y: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    margin-top: -10px;
  .arrayItem {
    width: 33%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        width: 74px;
        height: 60px;
        margin: 0 28px;
        text-align: center;
        font-size: large;
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

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 35px;
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
  height: 35px;
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

::v-deep .el-table .el-table__header th{
  background-color: #f5f7fa;
  color: #909399;
  height: 50px;
}

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-60px;
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 70px;
}
</style>
