<template>
  <CommonMenu :showCheckbox="true" @check="handleCheck"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="机柜环境">
    <template #NavInfo>
      <div >
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/temhum.png" /></div>
          <div class="name">温湿度</div>
        </div>
        <div class="line"></div>
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;15%
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
              <div class="tag warn"></div>15%-30%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;30
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
        </div>
        <div class="line"></div>
        <div class="overview">
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            <div class="info">
              <div>总电能</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dh.jpg" />
            <div class="info">
              <div>今日用电</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
          <div class="count">
            <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
            <div class="info">
              <div>今日用电</div>
              <div class="value">295.87 kW·h</div>
            </div>
          </div>
        </div> -->
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
        <el-form-item>
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus">
            全部
          </button>
           
          <template v-for="(status, index) in statusList" :key="index">
              <button
                class="btn_normal normal"
                @click.prevent="handleSelectStatus(index)"
                :style="{
                  backgroundColor: isSelected(index) ? 'white' : status.color,
                  borderColor: status.color,
                  borderWidth: '1px',
                  borderStyle: 'solid',
                  color:isSelected(index) ? '#000' : 'white'
                }"
                style="width: 100px;"
              >
              {{ status.startNum}}°C ~ {{ status.endNum}}°C
              </button>
              <!-- <button v-else
                   class="btn_normal normal"
                @click.prevent="handleSelectStatus(index)"
                :style="{
                  backgroundColor: isSelected(index) ? 'white' : status.color,
                  borderColor: status.color,
                  borderWidth: '1px',
                  borderStyle: 'solid',
                  color:isSelected(index) ? '#000' : 'white'
                }"
                style="width: 90px;"
              >
              {{ status.name }}
              </button> -->

              <!--       name: `${item.min}°C ~ ${item.max}°C`,
      startNum: item.min,
      endNum: item.max, -->
          </template>

          <el-button
            type="primary"
            plain
            @click="openForm('create')"
          >
            <Icon icon="ep:plus" class="mr-5px" /> 温度范围颜色
          </el-button>
        </el-form-item>
        <el-form-item prop="company">
          <el-input
            v-model="queryParams.company"
            placeholder="请输入公司名称"
            clearable
            class="!w-160px"
            height="35"
          />
          <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
          <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>

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
          <!-- <el-button @click="switchTemAndHum = 0;" :type="switchTemAndHum == 0 ? 'primary' : ''"><Icon icon="mdi:temperature-celsius" style="margin-right: 4px" />温度</el-button>
          <el-button @click="switchTemAndHum = 1;" :type="switchTemAndHum == 1 ? 'primary' : ''"><Icon icon="carbon:humidity" style="margin-right: 4px" />温度</el-button> -->
          <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 24;switchValue = 0;handleQuery();" :type="switchValue == 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />冷通道</el-button>
          <el-button @click="pageSizeArr=[24,36,48];queryParams.pageSize = 24;switchValue = 1;handleQuery();" :type="switchValue == 1 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 4px" />热通道</el-button>
          <el-button @click="pageSizeArr=[15, 25,30, 50, 100];queryParams.pageSize = 15;switchValue = 2;handleQuery();" :type="switchValue == 2 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 4px" />环境表格</el-button>
        </div>
      </el-form>
    </template>
    <template #Content>
    <div v-loading="tableLoading">
      <el-table v-show="switchValue == 2" v-loading="loading" :data="tableData" :stripe="true" :show-overflow-tooltip="true"  @cell-dblclick="toCabinetEnvDetail" >
        <!-- <el-table-column label="编号" align="center" prop="tableId" /> -->
        <el-table-column label="序号" align="center" width="80px">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column label="前门" align="center" >
          <el-table-column label="上" align="center" >
            <template #default="scope" >
              <el-text v-if="switchTemAndHum == 0" :style="{ color : scope.row.iceTopTemColor }">
                 {{ scope.row.iceTopTem === null ? '--' : scope.row.iceTopTem }}°C  
              </el-text>
              <el-text v-if="switchTemAndHum == 1">
                 {{ scope.row.iceTopHum === null ? '--' : scope.row.iceTopHum }}%  
              </el-text>
            </template>
          </el-table-column>        
          <el-table-column label="中" align="center" >
            <template #default="scope" >
              <el-text v-if="switchTemAndHum == 0" :style="{ color : scope.row.iceMidTemColor }">
                {{ scope.row.iceMidTem === null ? '--' : scope.row.iceMidTem }}°C  
              </el-text>
              <el-text v-if="switchTemAndHum == 1">
                {{ scope.row.iceMidHum === null ? '--' : scope.row.iceMidHum }}%  
              </el-text>                
            </template>
          </el-table-column>     
          <el-table-column label="下" align="center" >
            <template #default="scope" >
              <el-text v-if="switchTemAndHum == 0" :style="{ color : scope.row.iceBomTemColor }">
                {{ scope.row.iceBomTem === null ? '--' : scope.row.iceBomTem }}°C  
              </el-text>          
              <el-text v-if="switchTemAndHum == 1">
                {{ scope.row.iceBomHum === null ? '--' : scope.row.iceBomHum }}%  
              </el-text>   
            </template>
          </el-table-column>     
        </el-table-column>
        <el-table-column label="后门" align="center" >
          <el-table-column label="上" align="center" >
            <template #default="scope" >
              <el-text v-if="switchTemAndHum == 0" :style="{ color : scope.row.hotTopTemColor }">
                 {{ scope.row.hotTopTem === null ? '--' : scope.row.hotTopTem }}°C  
              </el-text>       
              <el-text v-if="switchTemAndHum == 1">
                 {{ scope.row.hotTopHum === null ? '--' : scope.row.hotTopHum }}%  
              </el-text>            
            </template>
          </el-table-column>        
          <el-table-column label="中" align="center" >
            <template #default="scope" >
              <el-text v-if="switchTemAndHum == 0" :style="{ color : scope.row.hotMidTemColor }">
                  {{ scope.row.hotMidTem === null ? '--' : scope.row.hotMidTem }}°C  
              </el-text>               
              <el-text v-if="switchTemAndHum == 1">
                  {{ scope.row.hotMidHum === null ? '--' : scope.row.hotMidHum }}%  
              </el-text>    
            </template>
          </el-table-column>     
          <el-table-column label="下" align="center" >
            <template #default="scope" >
              <el-text v-if="switchTemAndHum == 0" :style="{ color : scope.row.hotBomTemColor }">
                {{ scope.row.hotBomTem === null ? '--' : scope.row.hotBomTem }}°C  
              </el-text>             
              <el-text v-if="switchTemAndHum == 1">
                 {{ scope.row.hotBomHum === null ? '--' : scope.row.hotBomHum }}%  
              </el-text>   
            </template>
          </el-table-column> 
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link  
              type="primary"
              @click="toCabinetEnvDetail(scope.row)"
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

      <div v-show="switchValue == 1 && tableData != null" class="arrayContainer">
        <div class="arrayItem" v-for="item in tableData" :key="item.id">
          <div class="devKey">{{ item.location }}</div>
          <div class="content">
            <div  class="icon">
                <div><span>类型</span></div>
                <div><span> T </span></div>
                <div><span> H </span></div>
            </div>
            <div  class="tem">
                <div><span>冷</span></div>
                <div><span :style="{ color : item.iceAverageTemColor }">
                     {{ item.iceAverageTem === null ? '--' : item.iceAverageTem }}℃  
                </span></div>
                <div><span :style="{ color : item.iceAverageTemColor }">
                     {{ item.iceAverageHum === null ? '--' : item.iceAverageHum }}% 
                </span></div>
            </div>
            <div  class="tem">
                <div><span>热</span></div>
                <div><span :style="{ color : item.hotAverageTemColor }">
                    {{ item.hotAverageTem === null ? '--' : item.hotAverageTem }}℃  
                </span></div>
                <div><span :style="{ color : item.hotAverageTemColor }">
                    {{ item.hotAverageHum === null ? '--' : item.hotAverageHum }}% 
                </span></div>
            </div>
          </div>
          <button class="detail" @click="toCabinetEnvDetail(item)">详情</button>
        </div>
      </div>

      <div v-show="switchValue == 0 && tableData !=null" class="arrayContainer">
        <div class="arrayItem" v-for="item in tableData" :key="item.id">
          <div class="devKey">{{ item.location }}</div>
          <div class="content">
            <div  class="icon">
                <div><span>类型</span></div>
                <div><span> T </span></div>
                <div><span> H </span></div>
            </div>
            <div  class="tem">
                <div><span>冷</span></div>
                <div><span :style="{ color : item.iceAverageTemColor }">
                   {{ item.iceAverageTem === null ? '--' : item.iceAverageTem }}℃  
                 </span></div>
                <div><span :style="{ color : item.iceAverageTemColor }">
                   {{ item.iceAverageHum === null ? '--' : item.iceAverageHum }}% 
                </span></div>
            </div>
            <div  class="tem">
                <div><span>热</span></div>
                <div><span :style="{ color : item.hotAverageTemColor }">
                   {{ item.hotAverageTem === null ? '--' : item.hotAverageTem }}℃  
                </span></div>
                <div><span :style="{ color : item.hotAverageTemColor }"> 
                   {{ item.hotAverageHum === null ? '--' : item.hotAverageHum }}% 
                </span></div>
            </div>
          </div>
          <button class="detail" @click="toCabinetEnvDetail(item)">详情</button>
        </div>
      </div>

      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />

      <template v-if="tableData == null">
        <el-empty description="暂无数据" :image-size="300" />
      </template>
    </div>
    </template>
  </CommonMenu>
  
  <!-- 表单弹窗：添加/修改 -->
  <TemColorForm ref="temColorForm" @success="getList" />
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import TemColorForm from './TemColorForm.vue'
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/cabinet/index'

const butColor = ref(0);
const onclickColor = ref(-1);

// 存储当前选中的按钮索引
const selectedIndex = ref<number | null>(null);
const allSelected = ref<boolean>(false);

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const { push } = useRouter()

const switchTemAndHum = ref(0);
const temColorForm = ref();
const flashListTimer = ref();
const firstTimerCreate = ref(true);
const pageSizeArr = ref([24,36,48])
const switchValue = ref(0)
const tableLoading = ref(false);
// const statusNumber = reactive({
//   lessFifteen : 0,
//   greaterFifteen : 0,
//   greaterThirty : 0,
//   smallCurrent : 0
// })

const toCabinetEnvDetail = (row) =>{
  const id = row.id
  push({path: '/cabinet/cab/cabinetenvdetail', state: {  id }})
  // push('/cabinet/cab/cabinetenvdetail?id=' + row.id);
}



const statusList:any = ref([]);

const getCabinetColorAll = async () => {
  const res = await IndexApi.getCabinetColorAll()
  console.log('res', res)
  if (res != null) {
  statusList.value = res.map(item =>{
    if(switchValue.value == 0){
      return {
        name: `${item.min}°C ~ ${item.max}°C`,
        startNum: item.min,
        endNum: item.max,
        selected: true, // 根据实际情况设置默认选中状态
        cssClass: 'btn_normal',// 根据实际情况设置样式类
        activeClass: 'btn_normal normal',// 根据实际情况设置样式类
        value: 0,
        color: item.color
      }; 
    }else{
      return {
        name: `${item.hotMin}°C ~ ${item.hotMax}°C`,
        startNum: item.hotMin,
        endNum: item.hotMax,
        selected: true, // 根据实际情况设置默认选中状态
        cssClass: 'btn_normal',// 根据实际情况设置样式类
        activeClass: 'btn_normal normal',// 根据实际情况设置样式类
        value: 0,
        color: item.hotColor
      }; 
    }
  });
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

const serverRoomArr =  ref([])

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()


watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const tableData = ref([]);
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
  switchValue : undefined,
})as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  getCabinetColorAll();
  queryParams.switchValue = switchValue.value
  loading.value = true
  try {
    const data = await IndexApi.getCabinetEnvPage(queryParams);
      tableData.value =  data.list
      total.value = data.total
    // list.value.forEach((obj) => {
    //   obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
    //   obj.iceTopTem = obj.iceTopTem?.toFixed(1);
    //   obj.iceTopHum = obj.iceTopHum?.toFixed(1);
    //   obj.iceMidTem = obj.iceMidTem?.toFixed(1);
    //   obj.iceMidHum = obj.iceMidHum?.toFixed(1);
    //   obj.iceBomTem = obj.iceBomTem?.toFixed(1);
    //   obj.iceBomHum = obj.iceBomHum?.toFixed(1);
    //   obj.hotTopTem = obj.hotTopTem?.toFixed(1);
    //   obj.hotTopHum = obj.hotTopHum?.toFixed(1);
    //   obj.hotMidTem = obj.hotMidTem?.toFixed(1);
    //   obj.hotMidHum = obj.hotMidHum?.toFixed(1);
    //   obj.hotBomTem = obj.hotBomTem?.toFixed(1);
    //   obj.hotBomHum = obj.hotBomHum?.toFixed(1);
    // });
  } finally {
    loading.value = false
  }
}

const getListNoLoading = async () => {
  try {
    const data = await IndexApi.getCabinetEnvPage(queryParams);
    list.value = data.list
    var tableIndex = 0;
    list.value.forEach((obj) => {
      obj.tableId = (queryParams.pageNo - 1) * queryParams.pageSize + ++tableIndex;
      obj.iceTopTem = obj.iceTopTem?.toFixed(1);
      obj.iceTopHum = obj.iceTopHum?.toFixed(1);
      obj.iceMidTem = obj.iceMidTem?.toFixed(1);
      obj.iceMidHum = obj.iceMidHum?.toFixed(1);
      obj.iceBomTem = obj.iceBomTem?.toFixed(1);
      obj.iceBomHum = obj.iceBomHum?.toFixed(1);
      obj.hotTopTem = obj.hotTopTem?.toFixed(1);
      obj.hotTopHum = obj.hotTopHum?.toFixed(1);
      obj.hotMidTem = obj.hotMidTem?.toFixed(1);
      obj.hotMidHum = obj.hotMidHum?.toFixed(1);
      obj.hotBomTem = obj.hotBomTem?.toFixed(1);
      obj.hotBomHum = obj.hotBomHum?.toFixed(1);
    });
    total.value = data.total
  } catch (error) {
    
  }
}

const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
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

const handleSelectStatus = (index) => {
  // console.log('index',index);
  // console.log('statusList',statusList.value[index].startNum);
  butColor.value = 1;
  if(allSelected.value) {
     allSelected.value = false;
  }
  colorIndex.value = index;
  selectedIndex.value = index;
  if(switchValue.value ==1){
  queryParams.startNum = statusList.value[index].startNum+15;
  queryParams.endNum = statusList.value[index].endNum+15;
  }else{
  queryParams.startNum = statusList.value[index].startNum;
  queryParams.endNum = statusList.value[index].endNum;
  }

  handleQuery();
}

// 检查按钮是否被选中（即是否应该变为灰色）
const isSelected = (index: number): boolean => {
  return selectedIndex.value !== null && selectedIndex.value !== index;
};

const colorIndex = ref(0);
const toggleAllStatus = () => {
  butColor.value = 0;
  colorIndex.value = 0;
  allSelected.value = !allSelected.value;
  selectedIndex.value = null;
  onclickColor.value = -1;
  queryParams.startNum = null;
  queryParams.endNum = null;
  handleQuery();
}


/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  if(switchValue.value ==1){
  queryParams.startNum = statusList.value[colorIndex.value].startNum+15;
  queryParams.endNum = statusList.value[colorIndex.value].endNum+15;
  }else{
  queryParams.startNum = statusList.value[colorIndex.value].startNum;
  queryParams.endNum = statusList.value[colorIndex.value].endNum;
  }
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
    butColor.value = 0;
    colorIndex.value =0;
  allSelected.value = !allSelected.value;
  selectedIndex.value = null;
  onclickColor.value = -1;
  queryParams.startNum = null;
  queryParams.endNum = null;
  handleQuery()
}

/** 添加/修改操作 */
const openForm = async (type: string) => {
  temColorForm.value.open(type);
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

/** 初始化 **/
onMounted(() => {
  // getCabinetColorAll();
  getList();
  getNavList();
  // flashListTimer.value = setInterval((getListNoLoading), 60000);
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
  // if(!firstTimerCreate.value){
  //   flashListTimer.value = setInterval((getListNoLoading), 60000);
  // }
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

.btn_fault,
.btn_offline,
.btn_normal,
.btn_warn,
.btn_error{
  width: 125px;
  height: 32px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 5px;
  &:hover {
    color: #7bc25a;
  }
}
.btn_offline {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.btn_fault{
  border: 1px solid orange;
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
  border: none;
  background-color: #fff;
  margin-right: 8px;
}
.normal {
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
    height: 120px;
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
        text-align: center;
      }
      .tem {
        width: 100px;
        height: 30px;
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
