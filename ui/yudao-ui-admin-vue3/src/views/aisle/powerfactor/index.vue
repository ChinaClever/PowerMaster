<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="功率因素">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/aisle.png" /></div>
        </div> -->
        <!-- <div class="line"> -->
          <Echart :height="150" :width="200" :options="echartsOption" />
          <div style="position: relative; text-align:center">最大功率因素：{{max!=null?max.toFixed(2):0.00}}</div>
          <div style="position: relative; text-align:center">最小功率因素：{{min!=null?min.toFixed(2):0.00}}</div>
          <!-- <div>用能最大柜列</div>
          <div>昨日：{{ yesterdayMaxEq==null?"无数据":yesterdayMaxEq }}</div>
          <div>上周：{{ lastWeekMaxEq==null?"无数据":lastWeekMaxEq }}</div>
          <div>上月：{{ lastMonthMaxEq==null?"无数据":lastMonthMaxEq }}</div> -->
        <!-- </div> -->
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>{{ statusList[0].name }}
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>小电流
            </div>
            <div class="value"><span class="number">{{statusNumber.smallCurrent}}</span>个</div>
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
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
        </div> -->
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
        <el-form-item >
          <div class="statusColor" v-show="switchValue == 0 ">
            <div style="background-color: rgb(255, 110, 118)">功率因数&lt;0.25</div> 
            <div style="background-color: rgb(253, 221, 96)">0.25&#8804;功率因数&lt;0.50</div>
            <div style="background-color: rgb(88, 217, 249)">0.50&#8804;功率因数&lt;0.75</div> 
            <div style="background-color: rgb(124, 255, 178)">0.75&#8804;功率因数</div>
          </div>
        </el-form-item>
        <!-- <el-form-item style="position: relative;">
          <el-checkbox-group  v-model="queryParams.status">
            <el-checkbox :label="5" :value="5">在线</el-checkbox>
          </el-checkbox-group>
        </el-form-item> -->
        <!-- <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"/>
        </el-form-item> -->
        <el-form-item label="柜列名" prop="name" :style="{position:'relative',left:nameLeft+'px'}">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入柜列名"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item>
        <el-form-item :style="{position: 'relative', left: buttonLeft+'px'}">
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
        <div style="float:right">
          <el-button @click="if(switchValue==3){pageSizeArr=[24,36,48];queryParams.pageSize=24;queryParams.pageNo=1;switchValue = 0;getList();nameLeft=-65;buttonLeft=-110}" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="if(switchValue==0){pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize=15;queryParams.pageNo=1;switchValue = 3;getList();nameLeft=-255;buttonLeft=-485}" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table style="height: calc(100vh - 215px);" v-show="switchValue == 3 " v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openPFDetail" :header-cell-style="headerCellStyle" :border="true">
        <el-table-column width="100" label="序号" align="center">
            <template #default="scope">
              {{ (queryParams.pageNo - 1) * queryParams.pageSize + scope.$index + 1 }}
            </template>  
          </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="总功率因素" align="center" prop="pfTotal" width="160px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.apfA != null">
              {{ scope.row.apfA }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="A路功率因素" align="center" prop="pfA" width="160px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bpfA != null">
              {{ scope.row.bpfA }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B路功率因素" align="center" prop="pfB" width="160px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cpfA != null">
              {{ scope.row.cpfA }}
            </el-text>
          </template>
        </el-table-column>
        
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="150">
          <template #default="scope">
            <el-button
              type="primary"
              @click="openPFDetail(scope.row)"
              v-if=" scope.row.pfTotal != null"
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
      <div v-show="switchValue == 0  && valueMode == 0 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="icon" v-if=" item.pfTotal != null ">
              <div>{{item.pfTotal}}</div>
              总功率因素           
            </div>
            <!-- <div class="info" >                  
              <div  v-if="item.pfA != null">A路:{{item.pfA}}</div>
              <div  v-if="item.pfB != null">B路:{{item.pfB}}</div>
            </div>           -->
            <div class="zu" v-if=" item.pfTotal != null">
              <Bar :width="70" :height="100" :max="{L1:item.pfA,L2:item.pfB}" />
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status">
            <el-tag v-if="item.pfTotal != null" >功率因素</el-tag>
            <el-tag v-else-if="item.pfTotal == null && item.status != 5" type="info">无数据</el-tag>
            <el-tag v-else type="info">离线</el-tag>
          </div>
          <button class="detail" @click="openPFDetail(item)" v-if="item.pfTotal != null" >详情</button>
        </div>
      </div>

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

      <template v-if="list.length == 0 && switchValue != 3">
        <el-empty description="暂无数据" :image-size="300" />
      </template>

      <el-dialog v-model="detailVis" title="功率因素详情"  width="70vw" height="58vh" >
        <el-row style="position: absolute;top: 20px; left: 20%; vertical-align: middle;">
          <span style="vertical-align:middle; position: relative;top: 5px; right: 120px;">机房：{{ location.split("-")[0] }}<span  v-for="n in Array(10)" :key="n">&nbsp;</span>柜列：{{location.split("-")[1]}}</span>
          <span style="position: relative;left: 220px;">
            <div style="vertical-align:middle;display: inline-block;">
              日期:
              <el-date-picker
                :clearable="false"
                v-model="detailQueryParams.oldTime"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="date"
                :disabled-date="disabledDate"
                @change="handleDayPick"
                class="!w-160px"
              />
            </div>
            <el-button 
              @click="subtractOneDay();handleDayPick()" 
              :type=" 'primary'"
            >
              &lt;前一日
            </el-button>
            <el-button 
              @click="addtractOneDay();handleDayPick()" 
              :type=" 'primary'"
            >
              &gt;后一日
            </el-button>
            <el-button 
              @click="switchChartOrTable = 0" 
              :type="switchChartOrTable == 0 ?  'primary' : ``"
            >
              图表
            </el-button>
            <el-button 
              @click="switchChartOrTable = 1" 
              :type=" switchChartOrTable == 1 ?  'primary' : ``"
            >
              数据
            </el-button>
            <el-button 
            @click="exportExcel()"
            v-show="switchChartOrTable == 1">
              导出
            </el-button>
          </span>
        </el-row>
        <br/>
        <PFDetail v-show="switchChartOrTable == 0"  width="68vw" height="58vh"  :list="pfESList"   />
        <el-table v-show="switchChartOrTable == 1" :data="pfTableList" :stripe="true" :show-overflow-tooltip="true" >
          <el-table-column label="时间" align="center" prop="time" />
          <el-table-column label="总功率因素" align="center" prop="factorTotalAvgValue" />
          <el-table-column label="A路功率因素" align="center" prop="factorAAvgValue" />
          <el-table-column label="B路功率因素" align="center" prop="factorBAvgValue" />
        </el-table>
      </el-dialog>
    </template>
  </CommonMenu>


  <!-- 表单弹窗：添加/修改 -->
  <!-- <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" /> -->
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { IndexApi } from '@/api/aisle/aisleindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'
import PFDetail from './component/PFDetail.vue'
// import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'
import Bar from "./Bar.vue"

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

// const { push } = useRouter()
const location = ref() as any;
const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])
const switchValue = ref(0)
const valueMode = ref(0)
const switchChartOrTable = ref(0)
const detailVis = ref(false);
const nameLeft=ref(-65)
const buttonLeft=ref(-110)
const min=ref()
const max=ref()
const pfESList = ref({}) as any
const pfTableList = ref([]) as any
const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
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

const openPFDetail = async (row) =>{
  detailQueryParams.id = row.id;
  detailQueryParams.oldTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  location.value = row.location ? row.location  : row.devKey ;
  await getDetail();
  detailVis.value = true;
}


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

const handleDayPick = async () => {

  if(detailQueryParams?.oldTime ){
    await getDetail();
    
  } 
}

const subtractOneDay = () => {
  var date = new Date(detailQueryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  detailQueryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(detailQueryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  detailQueryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};


const handleClick = (row) => {
  console.log("click",row)
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
    pf : null,
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
  timeType : 0,
  name: undefined
})as any

const detailQueryParams = reactive({
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
  timeType : 0,
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getDetail = async () => {
  const data = await IndexApi.getAislePFDetail(detailQueryParams);
  pfESList.value = data?.chart;
  pfESList.value?.factorTotalAvgValue?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.factorAAvgValue?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.factorBAvgValue?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });

  pfTableList.value = data?.table;
  pfTableList.value?.forEach((obj) => {
    obj.factorTotalAvgValue = obj?.factorTotalAvgValue?.toFixed(2);
    obj.factorAAvgValue = obj?.factorAAvgValue?.toFixed(2);
    obj.factorBAvgValue = obj?.factorBAvgValue?.toFixed(2);
  });
}

const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getAislePFPage(queryParams)

    list.value = data.list
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.pfTotal == null ){
        return;
      } 

      obj.pfTotal = obj.pfTotal?.toFixed(2);
      obj.pfA = obj.pfA?.toFixed(2);
      obj.pfB = obj.pfB?.toFixed(2);

    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getAislePFPage(queryParams)
    list.value = data.list
    var tableIndex = 0;    

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.pfTotal == null ){
        return;
      } 

      obj.pfTotal = obj.pfTotal?.toFixed(2);
      obj.pfA = obj.pfA?.toFixed(2);
      obj.pfB = obj.pfB?.toFixed(2);
      
    });

    total.value = data.total
  } catch (error) {
    
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

// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type)
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
    const data = await IndexApi.exportIndex(queryParams)
    download.excel(data, 'PDU设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

//导出excel
async function exportExcel(){
  try{
    await message.exportConfirm()
    exportLoading.value = true
    const data=await IndexApi.getAislePFDetailExcel({
      ...detailQueryParams
    })
    download.excel(data, '功率因素详情.xls');
  }
  catch(error){
    console.error('导出失败：', error);
  }
  finally{
    exportLoading.value = false
  } 
}

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

function headerCellStyle() {
    return {
      backgroundColor: '#eee', // 表头背景颜色
    };
  }

const echartsOption = computed(() => ({
  series: [
    {
      type: 'gauge',
      startAngle: 180,
      endAngle: 0,
      center: ['50%', '75%'],
      radius: '90%',
      min: 0,
      max: 1,
      splitNumber: 8,
      axisLine: {
        lineStyle: {
          width: 6,
          color: [
            [0.25, '#FF6E76'],
            [0.5, '#FDDD60'],
            [0.75, '#58D9F9'],
            [1, '#7CFFB2']
          ]
        }
      },
      pointer: {
        icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
        length: '12%',
        width: 20,
        offsetCenter: [0, '-60%'],
        itemStyle: {
          color: 'auto'
        }
      },
      axisTick: {
        length: 12,
        lineStyle: {
          color: 'auto',
          width: 2
        }
      },
      splitLine: {
        length: 20,
        lineStyle: {
          color: 'auto',
          width: 5
        }
      },
      axisLabel: {
        color: '#464646',
        fontSize: 20,
        distance: -60,
        rotate: 'tangential',
        formatter: function (value) {
          if (value === 0.875) {
          } else if (value === 0.625) {
          } else if (value === 0.375) {
          } else if (value === 0.125) {
          }
          return '';
        }
      },
      title: {
        offsetCenter: [0, '-10%'],
        fontSize: 20
      },
      detail: {
        fontSize: 50,
        offsetCenter: [0, '-10%'],
        valueAnimation: true,
        formatter: function (value) {
          return Math.round(value * 100) + '';
        },
        color: 'inherit'
      },
      data: [
        {
          value: max.value
        }
      ]
    }
  ]
}));
/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  const result=await IndexApi.getMaxAndMinPowFac();
  console.log(result.min);
  min.value=result.min.factor_total;
  max.value=result.max.factor_total;
  getList()
  getNavList();
  flashListTimer.value = setInterval((getListNoLoading), 5000);
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
  getList()
  getNavList();
  if(!firstTimerCreate.value){
    flashListTimer.value = setInterval((getListNoLoading), 5000);
  }
})

function handleSizeChange(val) {
  queryParams.pageSize = val
  queryParams.pageNo = 1
  getList()
}
function handleCurrentChange(val) {
  queryParams.pageNo = val
  getList()
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
  display: inline-block;
  flex-wrap: wrap;
  height: calc(100vh - 215px);
  width: 100%;
  .arrayItem {
    display: inline-block;
    vertical-align: top;
    width: 25%;
    height: 140px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 40px;
    position: relative;
    .content {
      height : 60%;
      padding-left: 20px;
      display: flex;
      align-items: center;
      .count_img {
        margin: 0 35px 0 13px;
      }
      .icon {
        div{
          font-size: large;
        }
        width: 74px;
        height: 30px;
        margin: 0 28px;
        font-size: small;
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
.zu{
  position: absolute;
  right: 30%;
  bottom: -5px;
}
.statusColor{
  div{
    display: inline-block;
    font-size: small;
    margin-right: 10px;
    width: 130px;
    text-align: center;
    border-radius: 7px;
  }
}
</style>
