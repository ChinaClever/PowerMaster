<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱温度">
    <template #NavInfo>
      <div>
         <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div>
        <div class="line"></div> -->
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
        <div class="status">
          <div class="box">
            <div class="top"> <div class="tag"></div>正常</div>
            <div class="value">
              <span class="number">{{leftDataList.normal}}</span>个</div>
          </div>
          <div class="box">
            <div class="top"> <div class="tag empty"></div>离线</div>
            <div class="value">
              <span class="number">{{leftDataList.offline}}</span>个</div>
          </div>
          <div class="box">
            <div class="top"> <div class="tag error"></div>告警</div>
            <div class="value">
              <span class="number">{{leftDataList.alarm}}</span>个</div>
          </div>
          <div class="box">
            <div class="top"> 总共</div>
            <div class="value">
              <span class="number">{{leftDataList.total}}</span>个</div>
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
      <el-form-item v-show="valueMode != 3 && valueMode != 4">
        <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus" style="height:34px;">
            全部
          </button>
          <template v-for="(data,index) in statusList" :key="index">
            <button
              :class="[onclickColor === data.value ? data.activeClass:data.cssClass]"
              @click.prevent="handleSelectStatus(data.value)"
              >{{ data.name }}</button
            >
          </template>
        </el-form-item>

        <!-- <el-form-item >
          <el-checkbox-group  v-model="queryParams.status"  @change="handleQuery">
            <el-checkbox :label="1" :value="1">在线</el-checkbox>
          </el-checkbox-group>
        </el-form-item> -->
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3" v-loading="loading" style="margin-top:-10px;height:720px;overflow-y:auto;" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="openTemDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="网络地址" align="center" prop="devKey" width="250px"/>
        <el-table-column v-if="valueMode == 0" label="A相温度(°C)" align="center" prop="atem" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.atem != null" :type=" scope.row.atemStatus != 0 ? 'danger' : '' ">
              {{ scope.row.atem }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="B相温度(°C)" align="center" prop="btem" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.btem != null" :type=" scope.row.btemStatus != 0 ? 'danger' : '' ">
              {{ scope.row.btem }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="C相温度(°C)" align="center" prop="ctem" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ctem != null" :type=" scope.row.ctemStatus != 0 ? 'danger' : '' ">
              {{ scope.row.ctem }}
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="130px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openTemDetail(scope.row)"
              v-if=" scope.row.status != null && scope.row.status != 1"
              style="background-color:#409EFF;color:#fff;border:none;width:100px;height:30px;"
            >
            设备详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.boxId)"
              v-if="scope.row.status == 1"
              style="background-color:#fa3333;color:#fff;border:none;width:60px;height:30px;"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>    

      <div v-show="switchValue == 0  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <img  class="icon"  src="@/assets/imgs/temicon.png" />    
            <div class="info" >                  
              <div :style="{backgroundColor : item.atemColor}" v-if="item.atem != null">A: {{item.atem}}°C</div>
              <div :style="{backgroundColor : item.btemColor}" v-if="item.btem != null">B: {{item.btem}}°C</div>
              <div :style="{backgroundColor : item.ctemColor}" v-if="item.ctem != null">C: {{item.ctem}}°C</div>
              <div :style="{backgroundColor : item.ntemColor}" v-if="item.ntem != null">N: {{item.ntem}}°C</div>
            </div>          
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.atemStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.atemStatus != 0 || item.btemStatus != 0  || item.ctemStatus != 0 " >告警</el-tag>
            <el-tag type="success" v-else >正常</el-tag>
          </div>
          <button class="detail" @click="openTemDetail(item)" v-if="item.status != null && item.status != 0"  >详情</button>
        </div>
      </div>
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <template v-if="list.length == 0 && switchValue != 3">
        <el-empty description="暂无数据" :image-size="300" />
      </template>

      <el-dialog v-model="detailVis" title="温度详情"  width="70vw" height="58vh">
        <el-row class="custom-row">
          <el-tag style="margin-left: 6vw; margin-top: -130px">{{ location }}</el-tag>
          <div style="margin-left: -14vw;">
            日期:
            <el-date-picker
              v-model="queryParams.oldTime"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="date"
              :disabled-date="disabledDate"
              @change="handleDayPick"
              class="!w-160px"
            />
          </div>
          
          
          <el-button
            style="margin-left: 1vw;"
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
          <div class="button-group" style="margin-left: auto">
            <el-button
              @click="switchChartOrTable = 0"
              :type="switchChartOrTable === 0 ? 'primary' : ''"
            >
              图表
            </el-button>
            <el-button
              @click="switchChartOrTable = 1"
              :type="switchChartOrTable === 1 ? 'primary' : ''"
            >
              数据
            </el-button>
            <el-button type="success" plain @click="handleExportXLS" :loading="exportLoading">
              <Icon icon="ep:download" class="mr-5px" /> 导出
            </el-button>
          </div>

        </el-row>
        <br/>
        <TemDetail v-show="switchChartOrTable == 0" width="68vw" height="58vh"  :list="temESList"  />
        <el-table v-show="switchChartOrTable == 1" :data="temTableList" :stripe="true" :show-overflow-tooltip="true" >
          <el-table-column label="时间" align="center" prop="temAvgTime" />
          <el-table-column label="A相温度" align="center" prop="temAvgValueA" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.temAvgValueA }}°C
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相温度" align="center" prop="temAvgValueB" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.temAvgValueB }}°C
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相温度" align="center" prop="temAvgValueC" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.temAvgValueC }}°C
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="N相温度" align="center" prop="temAvgValueN" >
            <template #default="scope" >
              <el-text line-clamp="2" >
                {{ scope.row.temAvgValueN }}°C
              </el-text>
            </template>
          </el-table-column>
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
import { IndexApi } from '@/api/bus/boxindex'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'

import TemDetail from './component/TemDetail.vue'
// import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const location = ref() as any;
const detailVis = ref(false);
const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const switchChartOrTable = ref(0);
const valueMode = ref(0)

const butColor = ref(0);
const onclickColor = ref(-1);

const leftDataList = ref([])
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
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '告警',
    selected: true,
    value: 2,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  }
])

const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
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
  queryParams.status = [];
  handleQuery();
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

const handleClick = (row) => {
  console.log("click",row)
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

const openTemDetail = async (row) =>{
  queryParams.boxId = row.boxId;
  queryParams.oldTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  location.value = row.location ? row.location : row.devKey;
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

  if(queryParams?.oldTime ){
    await getDetail();
  } 
}

const subtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  queryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  queryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

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

const serverRoomArr =  ref([])

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中
const temESList = ref([]) as any;
const temTableList = ref([]) as any;
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
    atem : null,
    btem : null,
    ctem : null,
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
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getDetail = async () => {
  const data = await IndexApi.getBoxTemDetail(queryParams);
  temESList.value = data?.chart;
  temTableList.value = data?.table;
  temTableList.value?.forEach((obj) => {
    obj.temAvgValueA = obj?.temAvgValueA?.toFixed(0);
    obj.temAvgValueB = obj?.temAvgValueB?.toFixed(0);
    obj.temAvgValueC = obj?.temAvgValueC?.toFixed(0);
    obj.temAvgValueN = obj?.temAvgValueN?.toFixed(0);
  });
}
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBoxTemPage(queryParams);
    console.log('queryParams',queryParams);
    const res = await IndexApi.getBoxIndexStatistics();
    console.log('data',data);
    console.log('res',res);
 
    // 初始情况下，使用 API 返回的数据
    let processedList = data.list.map((obj, index) => {
      const tableId = (queryParams.pageNo - 1) * queryParams.pageSize + index + 1;
      return {
        ...obj,
        tableId,
        atem: obj.atem?.toFixed(0),
        btem: obj.btem?.toFixed(0),
        ctem: obj.ctem?.toFixed(0),
      };
    });

    list.value = processedList;

    total.value = data.total;
    leftDataList.value = res;
  } finally {
    loading.value = false;
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getBoxTemPage(queryParams);
    list.value = data.list;
    var tableIndex = 0;    

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.atem == null){
        return;
      } 
      obj.atem = obj.atem?.toFixed(0);
      obj.btem = obj.btem?.toFixed(0);
      obj.ctem = obj.ctem?.toFixed(0);    
      obj.ntem = obj.ntem?.toFixed(0);
    });

    total.value = data.total;
  } catch (error) {
    
  }
}

const getNavList = async() => {
  const res = await IndexApi.getBoxMenu();
  serverRoomArr.value = res;
  if (res && res.length > 0) {
    const room = res[0];
    const keys = [] as string[];
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type);
        })
      }
    })
  }

}


// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

const toggleAllSelected = () => {
 // 提取前三个按钮的状态
  const [button0Selected, button1Selected, button2Selected] = statusList.slice(0, 3).map(item => item.selected);
 
  // 检查前三个按钮的状态是否都为false或者true
  if (button0Selected === button1Selected && button1Selected === button2Selected) {
    // 如果都为false或者true，则切换所有按钮的状态
    statusList.forEach(item => {
      item.selected = !item.selected;
    });
  } else {
    // 如果前三个按钮的状态不一致，则检查多数的状态
    const countSelected = statusList.filter(item => item.selected).length;
    const majorityState = countSelected > statusList.length / 2;
 
    // 将所有按钮的状态改为多数的状态
    statusList.forEach(item => {
      item.selected = majorityState;
    });
  }
  statusList.forEach((item, index) => {
    console.log(`Button ${index} selected state: ${item.selected}`);
  });
  handleQuery();
};

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1;
  getList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields();
  //statusList.forEach((item) => item.selected = true)
  butColor.value = 0;
  queryParams.status = [];
  onclickColor.value = -1;
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
    await message.delConfirm();
    // 发起删除
    await IndexApi.deleteIndex(id);
    message.success(t('common.delSuccess'));
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
    const data = await IndexApi.getBoxTemDetailExcel(queryParams, axiosConfig);
    console.log("data",data);
    await download.excel(data, '温度详细.xlsx');
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
    await message.exportConfirm();
    // 发起导出
    exportLoading.value = true;
    const data = await IndexApi.exportIndex(queryParams);
    download.excel(data, 'PDU设备.xls');
  } catch {
  } finally {
    exportLoading.value = false;
  }
}

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  getList();
  getNavList();
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
  getList()
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
    margin-top: 30px;
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
        width: 60px;
        height: 60px;
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
        width: 60px;
        height: 60px;
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
        width: 60px;
        height: 60px;
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

.custom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
}
 
.button-group {
  display: flex;
  gap: 10px;
}

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-60px;
}
</style>
