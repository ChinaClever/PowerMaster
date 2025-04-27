<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱功数">
    <template #NavInfo>
      <div>
        <div class="header">
          <!-- <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div> -->
        </div>
        <!-- <div class="line"></div> -->
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
        <!--<el-form-item label="参数类型" prop="type">
        <el-cascader
          v-model="defaultSelected"
          collapse-tags
          :options="typeSelection"
          collapse-tags-tooltip
          :show-all-levels="true"
          @change="typeCascaderChange"
          class="!w-130px"
        />
        </el-form-item>-->
        <el-form-item prop="devKey">
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
          <span>网络地址</span>
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
      <div v-if="switchValue !== 0  && list.length > 0">
        <el-table v-if="switchValue == 3" v-loading="loading" style="height:720px;margin-top:-10px;overflow-y:auto;" :data="list" :show-overflow-tooltip="true"  @cell-dblclick="openPFDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" width="300px">
          <template #default="scope" >
            <el-text line-clamp="2" >
              {{ scope.row.location != scope.row.devKey ? scope.row.location : '未绑定' }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="网络地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column v-if="typeText == 'line'" label="总功率因数" align="center" prop="totalPf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.totalPowFactor != null">
              {{ scope.row.totalPowFactor }}
            </el-text>
          </template>
        </el-table-column>
        <!--<el-table-column v-if="typeText == 'line'" label="A相功率因数" align="center" prop="apf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.apf != null">
              {{ scope.row.apf }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'line'" label="B相功率因数" align="center" prop="bpf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bpf != null">
              {{ scope.row.bpf }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'line'" label="C相功率因数" align="center" prop="cpf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cpf != null">
              {{ scope.row.cpf }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路1功率因数" align="center" prop="loop1pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路2功率因数" align="center" prop="loop2pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路3功率因数" align="center" prop="loop3pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[2] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路4功率因数" align="center" prop="loop4pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[3] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路5功率因数" align="center" prop="loop5pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[4] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路6功率因数" align="center" prop="loop6pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[5] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路7功率因数" align="center" prop="loop7pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[6] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路8功率因数" align="center" prop="loop8pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[7] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="typeText == 'loop'" label="回路9功率因数" align="center" prop="loop9pf" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.loopPowFactor != null">
              {{ scope.row.loopPowFactor[8] }}
            </el-text>
          </template>
        </el-table-column>-->
        
        
        <el-table-column label="输出位1功率因数" align="center" prop="outlet1pf" width="150px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletPowFactor != null">
              {{ scope.row.outletPowFactor[0] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="输出位2功率因数" align="center" prop="outlet2pf" width="150px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletPowFactor != null">
              {{ scope.row.outletPowFactor[1] }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="输出位3功率因数" align="center" prop="outlet3pf" width="150px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.outletPowFactor != null">
              {{ scope.row.outletPowFactor[2] }}
            </el-text>
          </template>
        </el-table-column>
        
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="150px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="openPFDetail(scope.row)"
              v-if=" scope.row.status != null && scope.row.status != 5"
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

      <div v-else-if="switchValue == 0  && list.length > 0" class="arrayContainer">
        <template v-for="item in list" :key="item.devKey">
          <div v-if="item.devKey !== null" class="arrayItem">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info" style="padding-left: 20px;">

              <!--div v-if="item.phasePowFactor!= null && typeText == 'line'">
                <div v-for="(phasePF,index) in item.phasePowFactor" :key="index">
                  <div >{{ phaseLineText[index] }}{{phasePF}}</div>
                </div>
              </div>
              <div v-else-if="item.loopPowFactor != null && typeText == 'loop'" style="height:100px;width:130px;overflow:hidden;overflow-y:auto;">
                <div v-for="(loopPF,index) in item.loopPowFactor" :key="index">
                  <div>{{ loopLineText[index] }}{{loopPF}}</div>
                </div>
              </div>-->
              <div>
                <div v-for="(outletPF,index) in item.outletPowFactor" :key="index">
                  <div>{{ outletLineText[index] }}{{outletPF}}</div>
                </div>
              </div>
            </div>  
            
            <div class="icon" >
              <div v-if=" item.totalPowFactor != null  && typeText == 'line' && item.outletPowFactor.length >1" style="font-size: 18px;">
                <span style="font-size: 20px; ">{{ item.totalPowFactor }}</span><br/>总功率因数
              </div>
              <div v-if=" item.totalPowFactor != null  && typeText == 'line' && item.outletPowFactor.length <=1" style="font-size: 18px; padding-top: 10px;">
                <span style="font-size: 20px;"></span>功率因数
              </div>

              <div v-else-if=" item.totalPowFactor != null  && typeText == 'loop'">
                <span style="font-size: 20px;">{{ item.totalPowFactor }}</span><br/>总功率因数
              </div>                
            </div>

          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
            <div class="status">
              <el-tag v-if="item.status === 1" type="success">正常</el-tag>
              <el-tag v-else-if="item.status === 0" type="info">离线</el-tag>
              <el-tag v-else-if="item.status === 2" type="danger">告警</el-tag>
            </div>
          <button class="detail" @click="openPFDetail(item)"  v-if="item.status != null && item.status != 0">详情</button>
        </div>
        </template>
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

      <el-dialog v-model="detailVis">
        <div class="custom-row" style="display: flex; align-items: center;">
          <!-- 位置标签 -->
          <div class="location-tag el-col">
            <span style="margin-right:10px;font-size:18px;font-weight:bold;">功率因数详情</span>
            <span>所在位置：{{ location? location:'未绑定'}}</span>
            <span> 网络地址：{{ devkey }}</span>
          </div>

          <!-- 日期选择器 -->
          <div class="date-picker-col el-col">
            <el-date-picker
              v-model="queryParams.oldTime"
              :clearable = "false"
              :editable = "false"
              value-format="YYYY-MM-DD HH:mm:ss"
              type="datetime"
              :disabled-date="disabledDate"
              @change="handleDayPick()"
              :picker-options="pickerOptions"
              placeholder="选择日期时间"
            />
            <el-button @click="subtractOneDay(); handleDayPick()" type="primary" style="margin-left:10px;">&lt; 前一日</el-button>
            <el-button :disabled="clickAdd" @click="addtractOneDay(); handleDayPick()" type="primary">&gt; 后一日</el-button>
          </div>

          <!-- 图表/数据切换按钮组 -->
          <div class="chart-data-buttons el-col" style="margin-right: 50px;">
            <div class="button-group">
              <el-select v-model="typeRadioShow" placeholder="请选择" style="width: 100px">
                <el-option label="平均" value="平均" />
                <el-option label="最大" value="最大" />
                <el-option label="最小" value="最小" />
              </el-select>
              <el-button @click="switchChartOrTable = 0" :type="switchChartOrTable === 0 ? 'primary' : ''">图表</el-button>
              <el-button @click="switchChartOrTable = 1" :type="switchChartOrTable === 1 ? 'primary' : ''">数据</el-button>
              <el-button type="success" plain @click="handleExportXLS" :loading="exportLoading">
                <i class="el-icon-download"></i> 导出
              </el-button>
            </div>
          </div>
        </div>
        <br/>
        <PFDetail v-if="switchChartOrTable == 0"  width="75vw" height="70vh"  :list="pfESList" />
        <div v-else-if="switchChartOrTable == 1" style="width: 100%;height:70vh;overflow-y:auto;">
          <el-table :data="pfTableList" :stripe="true" :show-overflow-tooltip="true" style="height:70vh;" >
            <!-- <el-table-column label="输出位3功率因数" align="center" prop="powerFactorAvgValueC"/> -->
            <el-table-column label="序号" align="center" prop="index" width="80px" />
            <el-table-column label="时间" align="center" prop="createTime" />
            <el-table-column label="输出位1功率因数" align="center" :prop="`powerFactor${typeRadioShow == '最大' ? 'Max' : (typeRadioShow == '最小' ? 'Min' : 'Avg')}Value`" />
            <el-table-column v-if="typeRadioShow != '平均'" label="发生时间" align="center" :prop="`powerFactor${typeRadioShow == '最大' ? 'Max' : 'Min'}Time`" />
            <el-table-column v-if="outletNum >= 2" label="输出位2功率因数" align="center" :prop="`powerFactor${typeRadioShow == '最大' ? 'Max' : (typeRadioShow == '最小' ? 'Min' : 'Avg')}Valueb`" />
            <el-table-column v-if="outletNum >= 2 && typeRadioShow != '平均'" label="发生时间" align="center" :prop="`powerFactor${typeRadioShow == '最大' ? 'Max' : 'Min'}Timeb`" />
            <el-table-column v-if="outletNum == 3" label="输出位3功率因数" align="center" :prop="`powerFactor${typeRadioShow == '最大' ? 'Max' : (typeRadioShow == '最小' ? 'Min' : 'Avg')}Valuec`" />
            <el-table-column v-if="outletNum == 3 && typeRadioShow != '平均'" label="发生时间" align="center" :prop="`powerFactor${typeRadioShow == '最大' ? 'Max' : 'Min'}Timec`" />
          </el-table>
        </div>
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
import { BusPowerLoadDetailApi } from '@/api/bus/buspowerloaddetail'
import PFDetail from './component/PFDetail.vue'
// import CurbalanceColorForm from './CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const boxName = ref() as any;
const location = ref() as any;
const devkey = ref() as any;
const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(0)
const switchChartOrTable = ref(0)
const detailVis = ref(false);

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

const butColor = ref(0);
const onclickColor = ref(-1);

const typeRadioShow = ref("最大")

const clickAdd = ref(true)

const statusNumber = reactive({
  normal : 0,
  alarm : 0,
  offline : 0,
  total : 0
});
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

const getBoxIdAndLocation =async () => {
 try {
    const data = await BusPowerLoadDetailApi.getBoxIdAndLocation(queryParams);
    if (data != null){
      openPFDetail(data)
    }else{
      location.value = null
      boxName.value = null
    }
 } finally {
 }
}

const openPFDetail = async (row) =>{
  queryParams.boxId = row.boxId;
  queryParams.oldTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  location.value = row.location;
  devkey.value = row.devKey;
  await getDetail();
  clickAdd.value = true
  detailVis.value = true;
  typeRadioShow.value = '最大'
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
    var date = new Date(queryParams.oldTime + 'Z') // 添加 "Z" 表示 UTC 时间

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

const phaseLineText = ref(['A相：','B相：','C相：']);
const loopLineText = ref(['回路1：','回路2：','回路3：','回路4：','回路5：','回路6：','回路7：','回路8：','回路9：']);
const outletLineText = ref(['输出位1：','输出位2：','输出位3：']);

//const defaultSelected = ref(['line'])
const typeSelection = ref([]) as any;
var typeText = ref('line');
// 参数类型改变触发
//const typeCascaderChange = (selected) => {
//  queryParams.type = selected[0];
//  typeText = selected[0];
//  // 自动搜索
//  handleQuery()
//}

// 参数类型选择框初始化，相固定3相
const getTypeMaxValue = async () => {
    const typeSelectionValue  = [
    {
      value: "line",
      label: '相'
    },
    {
    value: "loop",
    label: '回路'
    },
    {
    value: "outlet",
    label: '输出位'
    },
  ]
  typeSelection.value = typeSelectionValue;
}


const subtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  clickAdd.value = false

  queryParams.oldTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParams.oldTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  var today = new Date(); // 今天的日期
  today.setHours(0, 0, 0, 0); // 去掉时间部分，只比较日期


  if (date.getFullYear() == today.getFullYear() && date.getMonth() == today.getMonth() && date.getDate() == today.getDate()) {
    clickAdd.value = true
  }

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

const serverRoomArr =  ref([])

const outletNum = ref(0)

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
  devKey : history?.state?.devKey as string | undefined,
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
  const data = await IndexApi.getBoxPFDetail(queryParams);
  
  outletNum.value = 0

  pfTableList.value = data?.table;
  pfTableList.value?.forEach((obj,index) => {
    obj.index = index+1
    obj.powerFactorAvgValue = obj?.powerFactorAvgValue?.toFixed(2);
    obj.powerFactorAvgValueb = obj?.powerFactorAvgValueb?.toFixed(2);
    obj.powerFactorAvgValuec = obj?.powerFactorAvgValuec?.toFixed(2);

    obj.powerFactorMaxValue = obj?.powerFactorMaxValue?.toFixed(2);
    obj.powerFactorMaxValueb = obj?.powerFactorMaxValueb?.toFixed(2);
    obj.powerFactorMaxValuec = obj?.powerFactorMaxValuec?.toFixed(2);

    obj.powerFactorMaxTime = obj.powerFactorMaxTime ? obj.powerFactorMaxTime.slice(0,-3) : '';
    obj.powerFactorMaxTimeb = obj.powerFactorMaxTimeb ? obj.powerFactorMaxTimeb.slice(0,-3) : '';
    obj.powerFactorMaxTimec = obj.powerFactorMaxTimec ? obj.powerFactorMaxTimec.slice(0,-3) : '';

    obj.powerFactorMinValue = obj?.powerFactorMinValue?.toFixed(2);
    obj.powerFactorMinValueb = obj?.powerFactorMinValueb?.toFixed(2);
    obj.powerFactorMinValuec = obj?.powerFactorMinValuec?.toFixed(2);
    
    obj.powerFactorMinTime = obj.powerFactorMinTime ? obj.powerFactorMinTime.slice(0,-3) : '';
    obj.powerFactorMinTimeb = obj.powerFactorMinTimeb ? obj.powerFactorMinTimeb.slice(0,-3) : '';
    obj.powerFactorMinTimec = obj.powerFactorMinTimec ? obj.powerFactorMinTimec.slice(0,-3) : '';

    if(outletNum.value < 3 && (obj.powerFactorAvgValuec || obj.powerFactorMaxValuec || obj.powerFactorMinValuec)) {
      outletNum.value = 3
    } else if(outletNum.value < 2 && (obj.powerFactorAvgValueb || obj.powerFactorMaxValueb || obj.powerFactorMinValueb)) {
      outletNum.value = 2
    } else if(outletNum.value < 1 && (obj.powerFactorAvgValue || obj.powerFactorMaxValue || obj.powerFactorMinValue)) {
      outletNum.value = 1
    } 
    console.log(outletNum.value)
  });


  getPfESList()
}

watch( ()=>typeRadioShow.value, ()=>{
  getPfESList()
})

const getPfESList = () => {
  let itemFactorType = typeRadioShow.value == "最小" ? 'Min' : (typeRadioShow.value == "最大" ? 'Max' : '')

  pfESList.value = []
  if(itemFactorType != '' && pfTableList.value) {
    pfESList.value = pfTableList.value.map(obj => ({
      pow_factor_value1: obj[`powerFactor${itemFactorType}Value`],
      powerFactorTime: [
        obj[`powerFactor${itemFactorType}Time`],
        obj[`powerFactor${itemFactorType}Timeb`],
        obj[`powerFactor${itemFactorType}Timec`]
      ],
      pow_factor_value2: obj[`powerFactor${itemFactorType}Valueb`],
      pow_factor_value3: obj[`powerFactor${itemFactorType}Valuec`],
      time: obj.createTime,
      outletNum: outletNum.value
    }));
  } else {
    pfESList.value = pfTableList.value.map(obj => ({
      pow_factor_value1: obj.powerFactorAvgValue,
      pow_factor_value2: obj.powerFactorAvgValueb,
      pow_factor_value3: obj.powerFactorAvgValuec,
      time: obj.createTime,
      outletNum: outletNum.value
    }));
  }
  console.log(pfESList.value)
}

const getList = async () => {
  loading.value = true;
  try {
    const data = await IndexApi.getBoxPFPage(queryParams);
    // console.log('data',data);

    list.value = data.list;

    // console.log('list.value',list.value);
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.phasePowFactor == null){
        return;
      } 
      for(var i= 0;i < obj.phasePowFactor.length; i++)
      {
        obj.phasePowFactor[i] = obj.phasePowFactor[i]?.toFixed(2);
      }
      for(var i= 0;i < obj.loopPowFactor.length; i++)
      {
        obj.loopPowFactor[i] = obj.loopPowFactor[i]?.toFixed(2);
      }
      for(var i= 0;i < obj.outletPowFactor.length; i++)
      {
        obj.outletPowFactor[i] = obj.outletPowFactor[i]?.toFixed(2);
      }
      obj.totalPowFactor = obj.totalPowFactor?.toFixed(2);
    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getBoxPFPage(queryParams);
    // console.log('data',data);

    list.value = data.list;

    // console.log('list.value',list.value);
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.phasePowFactor == null){
        return;
      } 
      for(var i= 0;i < obj.phasePowFactor.length; i++)
      {
        obj.phasePowFactor[i] = obj.phasePowFactor[i]?.toFixed(2);
      }
      for(var i= 0;i < obj.loopPowFactor.length; i++)
      {
        obj.loopPowFactor[i] = obj.loopPowFactor[i]?.toFixed(2);
      }
      for(var i= 0;i < obj.outletPowFactor.length; i++)
      {
        obj.outletPowFactor[i] = obj.outletPowFactor[i]?.toFixed(2);
      }
      obj.totalPowFactor = obj.totalPowFactor?.toFixed(2);
    });

    total.value = data.total
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


// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
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


/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields();
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
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await IndexApi.getBoxPFDetailExcel(queryParams, axiosConfig)
    console.log("data",data)
    await download.excel(data, '功率因数详细.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
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
  getBoxIdAndLocation();
  getList();
  getListAll();
  getNavList();
  getTypeMaxValue();
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
      width: 20%;
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
          font-size: 18px;
          width: 100px;
          height: 50px;
          margin-left:30px;
          margin-bottom: 20px;
          margin-right:20px;
          text-align: center;
          .text-pf{
            font-size: 16px;
          }
        }
        .info{
          font-size: 16px;
          margin-bottom: 20px;
          margin-left: 10px;
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

@media screen and (max-width:2048px) and (min-width:1600px) {
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
      border-radius: 7px;
      .content {
        display: flex;
        align-items: center;
        height: 100%;
        .icon {
          font-size: 20px;
          width: 100px;
          height: 50px;
          margin-left:20px;
          margin-bottom: 20px;
          margin-right:20px;
          text-align: center;
          .text-pf{
            font-size: 16px;
          }
        }
        .info{
          font-size: 16px;
          margin-bottom: 20px;
          margin-left: 10px;
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

@media screen and (max-width:1600px) {
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
        display: flex;
        align-items: center;
        height: 100%;
        .icon {
          font-size: 20px;
          width: 100px;
          height: 50px;
          margin-left:20px;
          margin-bottom: 20px;
          margin-right:20px;
          text-align: center;
          .text-pf{
            font-size: 16px;
          }
        }
        .info{
          font-size: 16px;
          margin-bottom: 20px;
          margin-left: 10px;
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
  height: 80px;

}

.custom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin-top:-50px;
}
 
.button-group {
  display: flex;
  gap: 10px;
}

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-dialog){
  width: 80%;
  height: 80%;
  margin-top:100px;
}

:deep(.el-tag){
 margin-right:-60px;
}
</style>
