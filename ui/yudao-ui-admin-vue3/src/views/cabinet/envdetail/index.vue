<template>
  <CommonMenu :showCheckbox="false"  @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="机柜环境详情">
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
        </div> -->
        <!-- <div class="line"></div>
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

        <el-form-item label="机柜Id" prop="ipAddr" >
          <el-input
            v-model="queryParams.id"
            placeholder="请输入IP"
            clearable
            class="!w-140px"
          />
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
    </template>
    <template #Content>
      <el-row v-show="visControll.visAllReport" :gutter="24" >
        <el-col :span="9">
          <el-card >
            <template #header>
              <div class="card-header">
                <el-row :gutter="24">
                  <el-col :span="12">
                    <div style="float:left">
                      <h2>前门</h2>
                    </div>
                  </el-col>
                  <el-col :span="12">
                    <div style="float:right">
                      <h2>后门</h2>
                    </div>
                  </el-col>
                </el-row>
              </div>
            </template>
            <el-row justify="center">
              <h1>上层</h1>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <div style="float:left">
                  {{  cabinetEnvData?.iceTopTem != null ? "温度:" + cabinetEnvData?.iceTopTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.iceTopHum != null ? "湿度:" + cabinetEnvData?.iceTopHum + "%" : ''}}
                </div>
              </el-col>
              <el-col :span="12">
                <div style="float:right">
                  {{  cabinetEnvData?.hotTopTem != null ? "温度:" + cabinetEnvData?.hotTopTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.hotTopHum != null ? "湿度:" + cabinetEnvData?.hotTopHum + "%"  : ''}}
                </div>
              </el-col>
            </el-row>
            <el-row justify="center">
              <h1>中层</h1>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <div style="float:left">
                  {{  cabinetEnvData?.iceMidTem != null ? "温度:" + cabinetEnvData?.iceMidTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.iceMidHum != null ? "湿度:" + cabinetEnvData?.iceMidHum + "%" : ''}}
                </div>
              </el-col>
              <el-col :span="12">
                <div style="float:right">
                  {{  cabinetEnvData?.hotMidTem != null ? "温度:" + cabinetEnvData?.hotMidTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.hotMidHum != null ? "湿度:" + cabinetEnvData?.hotMidHum + "%" : ''}}
                </div>
              </el-col>
            </el-row>
            <el-row justify="center">
              <h1 >下层</h1>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <div style="float:left">
                  {{  cabinetEnvData?.iceBomTem != null ? "温度:" + cabinetEnvData?.iceBomTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.iceBomHum != null ? "湿度:" + cabinetEnvData?.iceBomHum + "%" : ''}}
                </div>
              </el-col>
              <el-col :span="12">
                <div style="float:right">
                  {{  cabinetEnvData?.hotBomTem != null ? "温度:" + cabinetEnvData?.hotBomTem + "°C" : ''}}
                  <br/>
                  {{  cabinetEnvData?.hotBomHum != null ? "湿度:" + cabinetEnvData?.hotBomHum + "%" : ''}}
                </div>
              </el-col>
            </el-row>
          </el-card>
          <el-card >

            <el-row :gutter="24">
              <el-col :span="13">
                <div style="float:left">
                  门禁状态：
                </div>
                
              </el-col>
              <el-col :span="11">
                <div >
                  门禁状态：未检测出状态
                </div>
                
              </el-col>
            </el-row>
          </el-card>
          <el-card >

            <el-row :gutter="24">
              <el-col :span="13">
                <div style="float:left">
                  水浸状态：
                </div>
                
              </el-col>
              <el-col :span="11">
                <div >
                  水浸状态：
                </div>
                
              </el-col>
            </el-row>
          </el-card>
          <el-card >

            <el-row :gutter="24">
              <el-col :span="13">
                <div style="float:left">
                  烟雾状态：
                </div>
                
              </el-col>
              <el-col :span="11">
                <div >
                  烟雾状态：
                </div>
                
              </el-col>
            </el-row>
          </el-card>
        </el-col>
        <el-col :span="15">

          <el-row>
            <el-card>
              <el-row>
                <el-col >
                  <h1 style="width: 100%">前门</h1>
                </el-col>
                <el-col>                    
                  <el-form>
                    <el-form-item >
                      <el-button @click="iceTemAndHumSwitchValue = 1;" :type="iceTemAndHumSwitchValue == 1 ? 'primary' : ''">温度</el-button>
                      <el-button @click="iceTemAndHumSwitchValue = 2;" :type="iceTemAndHumSwitchValue == 2 ? 'primary' : ''">湿度</el-button>                      
                    </el-form-item>
                    <el-form-item style="margin-bottom: 10px;">
                      <el-button @click="queryParams.iceTimeType = 1;queryParams.timeArr = null;queryParams.id ? handleIceQuery() : ''" :type="queryParams.iceTimeType == 1 ? 'primary' : ''">过去24小时</el-button>
                      <el-button @click="queryParams.iceTimeType = 2;" :type="queryParams.iceTimeType == 2 ? 'primary' : ''">自定义</el-button>                      
                      <el-date-picker
                        v-if="queryParams.iceTimeType == 2"
                        v-model="queryParams.timeArr"
                        value-format="YYYY-MM-DD HH:mm:ss"
                        type="daterange"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        :disabled-date="disabledDate"
                        @change="handleIceDayPick"
                        class="!w-200px"
                      />
                    </el-form-item>
                  </el-form>                                                  
                </el-col> 
              </el-row>
              <div >
                <EnvTemLine v-show="iceTemAndHumSwitchValue == 1"  width="43vw" height="38vh" :list="iceTemList"  />
                <EnvHumLine v-show="iceTemAndHumSwitchValue == 2" width="43vw" height="38vh" :list="iceHumList"  />  
              </div>
            </el-card>
          </el-row>
          <el-row>
            <el-card>
              <el-row>
                <el-col >
                  <h1 style="width: 100%">后门</h1>
                </el-col>
                <el-col >
                  <el-form>          
                    <el-form-item>
                      <el-button @click="hotTemAndHumSwitchValue = 1;" :type="hotTemAndHumSwitchValue == 1 ? 'primary' : ''">温度</el-button>
                      <el-button @click="hotTemAndHumSwitchValue = 2;" :type="hotTemAndHumSwitchValue == 2 ? 'primary' : ''">湿度</el-button>                      
                    </el-form-item>
                    <div style="float:right;margin-top: 0;">
                      <el-form-item>
                        <el-button @click="queryParams.hotTimeType = 1;queryParams.timeArr = null;queryParams.id ? handleHotQuery() : ''" :type="queryParams.hotTimeType == 1 ? 'primary' : ''">过去24小时</el-button>
                        <el-button @click="queryParams.hotTimeType = 2;" :type="queryParams.hotTimeType == 2 ? 'primary' : ''">自定义</el-button>                      
                        <el-date-picker
                          v-if="queryParams.hotTimeType == 2"
                          v-model="queryParams.timeArr"
                          value-format="YYYY-MM-DD HH:mm:ss"
                          type="daterange"
                          start-placeholder="开始日期"
                          end-placeholder="结束日期"
                          :disabled-date="disabledDate"
                          @change="handleHotDayPick"
                          class="!w-200px"
                        />
                      </el-form-item>
                    </div>
                  </el-form>                        
                </el-col> 
              </el-row>
              <div >
                <EnvTemLine v-show="hotTemAndHumSwitchValue == 1" width="43vw" height="38vh" :list="hotTemList"  />
                <EnvHumLine v-show="hotTemAndHumSwitchValue == 2" width="43vw" height="38vh" :list="hotHumList"  />
              </div>                
            </el-card>
          </el-row>
        </el-col>
      </el-row>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/cabinet/index'
import { CabinetApi } from '@/api/cabinet/info'
import { ElTree } from 'element-plus'
import EnvTemLine from './component/EnvTemLine.vue'
import EnvHumLine from './component/EnvHumLine.vue'

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const iceTemList = ref([]) as any;
const iceHumList = ref([]) as any;
const hotTemList = ref([]) as any;
const hotHumList = ref([]) as any;
const switchValue = ref(1);
const iceTemAndHumSwitchValue = ref(1)
const hotTemAndHumSwitchValue = ref(1)
const visControll = reactive({
  visAllReport : true,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  temVis : false,
  ApowVis :false,
  BpowVis : false,

})

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

const handleIceDayPick = () => {
  if(queryParams?.oldTime && queryParams.iceTimeType == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }

 if (queryParams.timeArr && queryParams.iceTimeType == 2) {

    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    if(queryParams.id != null){

      handleIceQuery();
    }
  }
}

const handleHotDayPick = () => {
  if(queryParams?.oldTime && queryParams.hotTimeType == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }

 if (queryParams.timeArr && queryParams.hotTimeType == 2) {

    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    if(queryParams.id != null){
      handleHotQuery();
    }
  }
}

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey : undefined,
  id: history?.state?.id || 1,
  type: 'total',
  eqGranularity:"day",
  powGranularity : "hour",
  temGranularity : "hour",
  outputNumber : 10,
  ipAddr: undefined,
  createTime: undefined,
  timeArr:[],
  oldTime : null,
  newTime : null,
  cascadeAddr : 0,
  iceTimeType : 1,
  hotTimeType : 1
}) as any

const serverRoomArr =  ref([]) as any

//折叠功能


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

const handleClick = (row) => {
  if(row.type != null  && row.type == 3){
    queryParams.id = row.id
    handleQuery();
  }
}

//柱状图宽度
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

const cabinetEnvData = ref({}) as any

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

const getList = async () => {
  loading.value = true

  await handleIceQuery();
  await handleHotQuery();

  const data = await IndexApi.getCabinetEnvPage({cabinetIds : [queryParams.id]});
  list.value = data.list

  list.value.forEach((obj) => {
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
  cabinetEnvData.value = list.value[0];


  loading.value = false

}


watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


// 下拉框选项数组
// const deviceStatus = ref([])

// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.id != null ){
    await getList();
    queryParams.devKey = null;
  }
}


const handleIceQuery = async () => {

  if(queryParams.id != null){
    
    const data = await IndexApi.getCabinetIceTemAndHumById({id : queryParams.id,timeType : queryParams.iceTimeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

    iceTemList.value = data.temResult;
    iceHumList.value = data.humResult;
  }
}

const handleHotQuery = async () => {

  if(queryParams.id != null){
    const data = await IndexApi.getCabinetHotTemAndHumById({id : queryParams.id,timeType : queryParams.hotTimeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});

    hotTemList.value = data.temResult;
    hotHumList.value = data.humResult;    
  }
}
/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
// const formRef = ref()
// const openForm = (type: string, id?: number) => {
//   formRef.value.open(type, id)
// }

// /** 删除按钮操作 */
// const handleDelete = async (id: number) => {
//   try {
//     // 删除的二次确认
//     await message.delConfirm()
//     // 发起删除
//     await PDUDeviceApi.deletePDUDevice(id)
//     message.success(t('common.delSuccess'))
//     // 刷新列表
//     await getList()
//   } catch {}
// }

// /** 导出按钮操作 */
// const handleExport = async () => {
//   try {
//     // 导出的二次确认
//     await message.exportConfirm()
//     // 发起导出
//     exportLoading.value = true
//     const data = await PDUDeviceApi.exportPDUDevice(queryParams)
//     download.excel(data, 'PDU设备.xls')
//   } catch {
//   } finally {
//     exportLoading.value = false
//   }
// }

/** 初始化 **/
onMounted( async () =>  {
  // getList();
  // initChart();
  getNavList();
})

onBeforeMount(async () =>{
  await getList();
})
</script>
<style scoped lang="scss">
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

.centered-div {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}
.right-div{
  display: flex;
  justify-content: right; /* 水平居右 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
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
  margin-left: 30px;
  .progress {
    width: 400px;
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #eee;
    box-sizing: border-box;
    position: relative;
    display: flex;
    justify-content: center;
    .line {
      width: 3px;
      height: 36px;
      background-color: #000;
    }
    .left {
      text-align: center;
      box-sizing: border-box;
      background-color: #3b8bf5;
    }
    .right {
      text-align: center;
      background-color:  #f86f13;
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
