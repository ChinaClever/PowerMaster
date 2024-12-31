<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="插接箱负荷">
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
        </div> -->
        <!-- <div class="line"></div> -->
        <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;30%
            </div>
            <div class="value"><span class="number">{{statusNumber.lessThirty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag blue"></div>30%-60%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>60%-90%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterSixty}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;90%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterNinety}}</span>个</div>
          </div>
        </div>
        <div class="line"></div>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        v-show="switchValue !== 4"
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"                          
      >
        <el-form-item v-if="switchValue == 2 || switchValue == 3">
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
            <button v-else-if="butColor === 1" style="height:25px;" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus(status.value)">{{status.name}}</button>
          </template>
        </el-form-item>
        <!-- <el-button
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 平衡度范围颜色
        </el-button> -->
        <el-form-item label="网络地址" prop="devKey" >
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-150px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 10px;margin-right: 5px;">
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 2;showPagination = 0" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>                      
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;showPagination = 0" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 4;showPagination = 1" :type="switchValue === 4 ? 'primary' : ''" v-show="switchValue == 3"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>    
        </div>
      </el-form>
      <el-form
        v-show="switchValue == 4"
        class="-mb-15px"
        :model="queryDeletedPageParams"
        ref="queryFormRef2"
        :inline="true"
        label-width="68px"                          
      >
        <el-form-item label="网络地址" prop="devKey" >
          <el-autocomplete
            v-model="queryDeletedPageParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        <el-form-item style="margin-left: 10px;">
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
          <el-button @click="pageSizeArr=[24,36,48,96];queryParams.pageSize = 24;getList();switchValue = 2;showPagination = 0" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>                      
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;showPagination = 0" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryDeletedPageParams.pageSize = 15;getDeletedList();switchValue = 4;showPagination = 1" :type="switchValue === 4 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />已删除</el-button>    
        </div>
      </el-form>      
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3" v-loading="loading" style="height:710px;overflow-y:auto;" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toDetail" :border="true">
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" width="300px"/>
        <el-table-column label="设备地址" align="center" prop="devKey" :class-name="ip"/>
        <el-table-column label="设备名称" align="center" prop="boxName" />
        <el-table-column label="运行状态" align="center" prop="color" >
          <template #default="scope" >
            <!--<el-tag type="info"  v-if="scope.row.status == 5">离线</el-tag>-->
            <el-tag type="info"  v-if="scope.row.color == 0&&scope.row.status != 5">{{statusList[4].name}}</el-tag>
            <el-tag type="success" v-else-if="scope.row.color == 1&&scope.row.status != 5">{{(statusList[0].name)}}</el-tag>
            <el-tag type="primary" v-else-if="scope.row.color == 2&&scope.row.status != 5">{{(statusList[1].name)}}</el-tag>
            <el-tag type="warning" v-else-if="scope.row.color == 3&&scope.row.status != 5">{{(statusList[2].name)}}</el-tag>
            <el-tag type="danger" v-else-if="scope.row.color == 4&&scope.row.status != 5">{{(statusList[3].name)}}</el-tag>
            <!--<el-tag type="danger" v-if="scope.row.color != 0 && scope.row.color != 4 && scope.row.color != 3 && scope.row.color != 2 && scope.row.color != 1 && scope.row.status != 5">异常</el-tag>-->
          </template>
        </el-table-column>
        <el-table-column label="A相负载率(%)" align="center" prop="aloadRate" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" :style="{ color: getColor(scope.row.aloadRate) }" v-if="scope.row.aloadRate != null">
              {{ scope.row.aloadRate }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="B相负载率(%)" align="center" prop="bloadRate" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" :style="{ color: getColor(scope.row.bloadRate) }" v-if="scope.row.bloadRate != null">
              {{ scope.row.bloadRate }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column label="C相负载率(%)" align="center" prop="cloadRate" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" :style="{ color: getColor(scope.row.cloadRate) }" v-if="scope.row.cloadRate != null">
              {{ scope.row.cloadRate }}
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="toDetail(scope.row)"
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
    <!-- 查询已删除-->
      <el-table v-show="switchValue == 4" v-loading="loading" style="height:710px;overflow-y:auto;" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border=true>
        <el-table-column label="编号" align="center" prop="tableId" width="80px"/>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="设备名称" align="center" prop="boxName" />
        <el-table-column label="运行状态" align="center" prop="status" >
          <template #default="scope">
            <el-tag type="info" v-if="scope.row.status">已删除</el-tag>
          </template>
        </el-table-column>        
        <el-table-column label="设备地址" align="center" prop="devKey" :class-name="ip"/>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.boxId)"
            >
              恢复设备
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-show="showPagination == 1"
        :total="deletedTotal"
        :page-size-arr="pageSizeArr"
        v-model:page="queryDeletedPageParams.pageNo"
        v-model:limit="queryDeletedPageParams.pageSize"
        @pagination="getDeletedList"
      />        
      <div v-show="switchValue == 2  && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null ? item.location : item.devKey }}&nbsp;{{item.boxName}}</div>
          <div class="content">
            
            <div class="info" style="padding: 0 18px;margin-right:30px">                  
              <div  v-if="item.aloadRate != null && item.status != 0" ><el-text :style="{ color: getColor(item.aloadRate) }">A相：{{Math.round(item.aloadRate)}}%</el-text></div>
              <div  v-if="item.bloadRate != null && item.status != 0" ><el-text :style="{ color: getColor(item.bloadRate) }">B相：{{Math.round(item.bloadRate)}}%</el-text></div>
              <div  v-if="item.cloadRate != null && item.status != 0" ><el-text :style="{ color: getColor(item.cloadRate) }">C相：{{Math.round(item.cloadRate)}}%</el-text></div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div style="padding: 0 4px" v-show="item.status != 0"><Bar :width="100" :height="100" :max="{L1:item.aloadRate,L2:item.bloadRate,L3:item.cloadRate}" /></div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" >
            <el-tag type="info"  v-if="item.color == 0 && item.status != 0">{{(statusList[4].name)}}</el-tag>
            <!--<el-tag type="info"  v-if="item.status == 0">离线</el-tag>-->
            <el-tag type="success" v-else-if="item.color == 1&& item.status != 0">{{(statusList[0].name).slice(3, 10)}}</el-tag>
            <el-tag type="primary" v-else-if="item.color == 2&& item.status != 0">{{(statusList[1].name).slice(3, 10)}}</el-tag>
            <el-tag type="warning" v-else-if="item.color == 3&& item.status != 0">{{(statusList[2].name).slice(3, 10)}}</el-tag>
            <el-tag type="danger" v-else-if="item.color == 4&& item.status != 0">{{(statusList[3].name).slice(3, 10)}}</el-tag>
            <!--<el-tag type="danger" v-if="item.color != 0 && item.color != 4 && item.color != 3 && item.color != 2 && item.color != 1 && item.status != 0">异常</el-tag>-->
          </div>
          <button class="detail" @click="toDetail(item)" v-if="item.status != null && item.status != 0 && item.color != 0" >详情</button>
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
      <template v-if="list.length == 0 && switchValue == 2 && showPagination == 0">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
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
import Bar from './Bar.vue'


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })
const { push } = useRouter()
const showPagination = ref(0)
const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48,96])
const switchValue = ref(2)

const butColor = ref(0);
const onclickColor = ref(-1);

const statusNumber = reactive({
  lessThirty : 0,
  greaterThirty : 0,
  greaterSixty : 0,
  greaterNinety : 0
})

const statusList = reactive([
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
  {
    name: '空载',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
])

const devKeyList = ref([]);
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

const handleClick = (row) => {
  console.log("click",row);
}

const handleCheck = async (row) => {
  if(row.length == 0){
    queryParams.boxDevKeyList = null;
    queryDeletedPageParams.boxDevKeyList = null;
    getList();
    getDeletedList();
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
    queryDeletedPageParams.boxDevKeyList = [-1]
  }else{
    queryParams.boxDevKeyList = ids
    queryDeletedPageParams.boxDevKeyList = ids
  }

  getList();
  getDeletedList();
}


const serverRoomArr =  ref([]);

const filterText = ref('');
const treeRef = ref<InstanceType<typeof ElTree>>();


watch(filterText, (val) => {
  treeRef.value!.filter(val);
})


const message = useMessage(); // 消息弹窗
const { t } = useI18n(); // 国际化

const loading = ref(false); // 列表的加载中
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
    aloadRate : null,
    bloadRate : null,
    cloadRate : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const deletedList = ref([
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
    aloadRate : null,
    bloadRate : null,
    cloadRate : null,
    curUnbalance : null,
  }
]) as any// 列表的数据
const total = ref(0); // 列表的总页数
const deletedTotal = ref(0); // 已删除列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devAddr: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:[],
  cabinetIds : [],
  isDeleted: 0,
})as any
const queryDeletedPageParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devAddr: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData:undefined,
  status:undefined,
  cabinetIds : [],
  isDeleted: 1,
})as any
const queryFormRef = ref(); // 搜索的表单
const queryFormRef2 = ref();
const exportLoading = ref(false); // 导出的加载中

/** 查询列表 */
const getList = async () => {
  loading.value = true;
  try {
    console.log('queryParams',queryParams);
    const data = await IndexApi.getIndexPage(queryParams);
    console.log('data',data);
    list.value = data.list;
    var tableIndex = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
    });

    total.value = data.total
  } finally {
    loading.value = false;
  }
}

const getLoadRateStatus = async () => {
    const data = await IndexApi.getLoadRateStatus()
    console.log('statusNumber',data);
    statusNumber.greaterNinety = data.greaterNinety;
    statusNumber.lessThirty = data.lessThirty;
    statusNumber.greaterThirty = data.greaterThirty;
    statusNumber.greaterSixty = data.greaterSixty;
}

const getDeletedList = async () => {
  try {
    const data = await IndexApi.getDeletedIndexPage(queryDeletedPageParams);
    deletedList.value = data.list;
    var tableIndex = 0;    

    deletedList.value.forEach((obj) => {
      obj.tableId = (queryDeletedPageParams.pageNo - 1) * queryDeletedPageParams.pageSize + ++tableIndex;
    });

    deletedTotal.value = data.total;
  } catch (error) {
    
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getIndexPage(queryParams);
    list.value = data.list;
    var tableIndex = 0;    
    var lessThirty = 0;
    var greaterThirty = 0;
    var greaterSixty = 0;
    var greaterNinety = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;


      if(obj.color == 4){
        greaterNinety++;
      } else if (obj.color == 1) {
        lessThirty++;
      } else if (obj.color == 2) {
        greaterThirty++;
      } else if (obj.color == 3) {
        greaterSixty++;
      }
    });
    statusNumber.greaterNinety = greaterNinety;
    statusNumber.lessThirty = lessThirty;
    statusNumber.greaterThirty = greaterThirty;
    statusNumber.greaterSixty = greaterSixty;
    total.value = data.total
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

const toDetail = (row) =>{
  const devKey = row.devKey;
  const boxId = row.boxId;
  const location = row.location != null ? row.location : devKey;
  push({path: '/bus/boxmonitor/boxpowerLoadDetail', state: { devKey, boxId ,location}});
}


// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

//const handleSelectStatus = (index) => {
//  statusList[index].selected = !statusList[index].selected
//  const status =  statusList.filter(item => item.selected)
//  const statusArr = status.map(item => item.value)
//  if(statusArr.length != statusList.length){
//    queryParams.color = statusArr;
//    //queryParams.status = [5];
//  }else{
//    queryParams.color = null;
//    //queryParams.status = [];
//  }
//  handleQuery();
//}

const handleSelectStatus = (index) => {
  console.log('index',index);
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.color = [index];
  handleQuery();
}

const toggleAllStatus = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.color = [0,1,2,3,4];
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1;
  queryDeletedPageParams.pageNo = 1;
  getList();
  getDeletedList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields();
  queryFormRef2.value.resetFields();
  butColor.value = 0;
  //statusList.forEach((item) => item.selected = true)
  queryParams.color = [];
  onclickColor.value = -1;
  handleQuery();
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type);
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
     await getList();
  } catch {}
}

/** 恢复按钮操作 */
const handleRestore = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm();
    // 发起删除
    await IndexApi.restoreIndex(id);
    message.success(t('common.restoreSuccess'));
    // 刷新列表
     await getDeletedList();
  } catch {}
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

const getColor = (loadRate: number) => {
  if (loadRate < 60) {
    return 'null' // 低于60时不显示颜色
  } else if (loadRate < 90) {
    return '#ffc402' // 60到90之间为黄色
  } else {
    return '#fa3333' // 高于90时为红色
  }
}



/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
  getList();
  getNavList();
  getLoadRateStatus();
  flashListTimer.value = setInterval((getList), 5000);
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
    flashListTimer.value = setInterval((getList), 5000);
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

.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  // width: 55px;
  // height: 32px;
  padding: 3px 8px;
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
          font-size: large;
          font-weight: bold;
          margin-right: auto;
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
        .blue {
          background-color: #3b8bf5;
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
    margin-top: 18px;
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
}

.btnallSelected {
  margin-right: 10px;
  width: 58px;
  height: 25px;
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
  height: 25px;
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

:deep(.el-card){
  --el-card-padding:5px;
}

:deep(.el-tag){
  margin-right:-60px;
}
</style>
