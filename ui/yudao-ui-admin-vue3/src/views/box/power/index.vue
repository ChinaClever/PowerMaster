<template>
  <CommonMenu @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="均衡配电">
    <template #NavInfo>
      <div>
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Box.png" /></div>
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
        <el-form-item>
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
          <el-button @click="valueMode = 0;" :type="valueMode == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电流</el-button>            
          <el-button @click="valueMode = 1;" :type="valueMode == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />电压</el-button>            
          <el-button @click="valueMode = 2;" :type="valueMode == 2 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />有功功率</el-button>            
          <el-button @click="valueMode = 3;" :type="valueMode == 3 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />无功功率</el-button>                     
          <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 24;getList();switchValue = 0;" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />阵列模式</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;getList();switchValue = 3;" :type="switchValue == 3 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
      <el-table v-show="switchValue == 3" v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="编号" align="center" prop="tableId" />
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column v-if="valueMode == 0" label="A相电流" align="center" prop="acur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.acur != null">
              {{ scope.row.acur }}A
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="A相电流状态" align="center" prop="acurStatus" width="130px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.acurStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="B相电流" align="center" prop="bcur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bcur != null">
              {{ scope.row.bcur }}A
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="B相电流状态" align="center" prop="bcurStatus" width="130px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.bcurStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="C相电流" align="center" prop="ccur" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.ccur != null">
              {{ scope.row.ccur }}A
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 0" label="C相电流状态" align="center" prop="ccurStatus" width="130px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.ccurStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column  v-if="valueMode == 1"  label="A相电压" align="center" prop="avol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.avol">
              {{ scope.row.avol }}V
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="A相电压状态" align="center" prop="avolStatus" width="130px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.avolStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
            <el-text line-clamp="2" v-if="scope.row.avolStatus != null">
              {{ scope.row.avolStatus }}
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="B相电压" align="center" prop="bvol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bvol">
              {{ scope.row.bvol }}V
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="B相电压状态" align="center" prop="bvolStatus" width="130px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.bvolStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="C相电压" align="center" prop="cvol" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cvol">
              {{ scope.row.cvol }}V
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 1" label="C相电压状态" align="center" prop="cvolStatus" width="130px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.cvolStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="A相有功功率" align="center" prop="aactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aactivePow">
              {{ scope.row.aactivePow }}kW
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="A相有功功率状态" align="center" prop="aactivePowStatus" width="150px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.aactivePowStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="B相有功功率" align="center" prop="bactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.bactivePow">
              {{ scope.row.bactivePow }}kW
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="B相有功功率状态" align="center" prop="bactivePowStatus" width="150px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.bactivePowStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="C相有功功率" align="center" prop="cactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.cactivePow">
              {{ scope.row.cactivePow }}kW
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 2" label="C相有功功率状态" align="center" prop="cactivePowStatus" width="150px" >
          <template #default="scope" >
            <el-tag type="danger" v-if="scope.row.cactivePowStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="A相无功功率" align="center" prop="areactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.aactivePow">
              {{ scope.row.aactivePow }}kVar
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="B相无功功率" align="center" prop="breactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.breactivePow">
              {{ scope.row.breactivePow }}kVar
            </el-text>
          </template>
        </el-table-column>
        <el-table-column v-if="valueMode == 3" label="C相无功功率" align="center" prop="creactivePow" width="130px" >
          <template #default="scope" >
            <el-text line-clamp="2" v-if="scope.row.creactivePow">
              {{ scope.row.creactivePow }}kVar
            </el-text>
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
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
              @click="handleDelete(scope.row.id)"
              v-if="scope.row.status == 5"
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
            <div class="icon" >
              <div v-if="false" >
                不平衡度<br/>{{ item.volUnbalance }}%
              </div>              
            </div>
            <div class="info" v-if="valueMode == 0" >                  
              <div :style="{backgroundColor : item.acurColor}" v-if="item.acur != null">A相电流：{{item.acur}}A</div>
              <div :style="{backgroundColor : item.bcurColor}" v-if="item.bcur != null">B相电流：{{item.bcur}}A</div>
              <div :style="{backgroundColor : item.ccurColor}" v-if="item.ccur != null">C相电流：{{item.ccur}}A</div>
            </div>
            <div class="info" v-if="valueMode == 1">                  
              <div :style="{backgroundColor : item.avolColor}"  v-if="item.avol != null">A相电压：{{item.avol}}V</div>
              <div :style="{backgroundColor : item.bvolColor}" v-if="item.bvol != null">B相电压：{{item.bvol}}V</div>
              <div :style="{backgroundColor : item.cvolColor}" v-if="item.cvol != null">C相电压：{{item.cvol}}V</div>
            </div>
            <div class="info" v-if="valueMode == 2">                  
              <div :style="{backgroundColor : item.aactivePowColor}" v-if="item.aactivePow != null">A相有功功率：{{item.aactivePow}}kW</div>
              <div :style="{backgroundColor : item.bactivePowColor}" v-if="item.bactivePow != null">B相有功功率：{{item.bactivePow}}kW</div>
              <div :style="{backgroundColor : item.cactivePowColor}" v-if="item.cactivePow != null">C相有功功率：{{item.cactivePow}}kW</div>
            </div>
            <div class="info" v-if="valueMode == 3">                  
              <div v-if="item.areactivePow != null">A相无功功率：{{item.areactivePow}}kVar</div>
              <div v-if="item.breactivePow != null">B相无功功率：{{item.breactivePow}}kVar</div>
              <div v-if="item.creactivePow != null">C相无功功率：{{item.creactivePow}}kVar</div>
            </div>
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" v-if="valueMode == 0">
            <el-tag type="info" v-if="item.acurStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.acurStatus != 0 || item.bcurStatus != 0  || item.ccurStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 1">
            <el-tag type="info" v-if="item.avolStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.avolStatus != 0 || item.bvolStatus != 0 || item.cvolStatus != 0 " >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div>
          <div class="status" v-if="valueMode == 2">
            <el-tag type="info" v-if="item.aactivePowStatus == null " >离线</el-tag>
            <el-tag type="danger" v-else-if="item.aactivePowStatus != 0 || item.bactivePowStatus != 0 || item.cactivePowStatus != 0" >告警</el-tag>
            <el-tag v-else >正常</el-tag>
          </div>
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
      <template v-if="list.length == 0 && switchValue != 3">
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

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const curBalanceColorForm = ref()
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])
const switchValue = ref(0)
const valueMode = ref(0)

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

const handleClick = (row) => {
  console.log("click",row)
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
    acur : null,
    bcur : null,
    ccur : null,
    curUnbalance : null,
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
const getList = async () => {
  loading.value = true
  try {
    const data = await IndexApi.getBoxRedisPage(queryParams)

    list.value = data.list
    var tableIndex = 0;

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acur == null){
        return;
      } 
      obj.acur = obj.acur?.toFixed(2);
      obj.bcur = obj.bcur?.toFixed(2);
      obj.ccur = obj.ccur?.toFixed(2);
      obj.avol = obj.avol?.toFixed(1);
      obj.bvol = obj.bvol?.toFixed(1);
      obj.cvol = obj.cvol?.toFixed(1);
      obj.aactivePow = obj.aactivePow?.toFixed(3);
      obj.bactivePow = obj.bactivePow?.toFixed(3);
      obj.cactivePow = obj.cactivePow?.toFixed(3);
      obj.areactivePow = obj.areactivePow?.toFixed(3);
      obj.breactivePow = obj.breactivePow?.toFixed(3);
      obj.creactivePow = obj.creactivePow?.toFixed(3);
    });

    total.value = data.total
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getBoxRedisPage(queryParams)
    list.value = data.list
    var tableIndex = 0;    

    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      if(obj?.acur == null){
        return;
      } 
      obj.acur = obj.acur?.toFixed(2);
      obj.bcur = obj.bcur?.toFixed(2);
      obj.ccur = obj.ccur?.toFixed(2);
      obj.avol = obj.avol?.toFixed(1);
      obj.bvol = obj.bvol?.toFixed(1);
      obj.cvol = obj.cvol?.toFixed(1);
      obj.aactivePow = obj.aactivePow?.toFixed(3);
      obj.bactivePow = obj.bactivePow?.toFixed(3);
      obj.cactivePow = obj.cactivePow?.toFixed(3);
      obj.areactivePow = obj.areactivePow?.toFixed(3);
      obj.breactivePow = obj.breactivePow?.toFixed(3);
      obj.creactivePow = obj.creactivePow?.toFixed(3);
    });

    total.value = data.total
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

const toPDUDisplayScreen = (row) =>{
  push('/pdu/pdudisplayscreen?devKey=' + row.devKey + '&location=' + row.location + '&id=' + row.id);
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

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll();
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
</style>
