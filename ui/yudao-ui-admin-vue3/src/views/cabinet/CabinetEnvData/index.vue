<template>
  <CommonMenu :dataList="navList" @check="handleCheck" navTitle="机柜环境数据" placeholder="机柜名">
    <template #NavInfo>
      <br/>
        <div class="nav_data">
          <div class="descriptions-container" style="font-size: 14px;">
            <div class="description-item">
                <span class="label">最近一分钟 :</span>
                <span class="value">{{ lastMinTotalData }}条</span>
            </div>
            <div class="description-item">
                <span class="label">最近一小时 :</span>
                <span class="value">{{ lastHourTotalData }}条</span>
            </div>
            <div class="description-item">
                <span class="label">最近一天 :</span>
                <span class="value">{{ lastDayTotalData }}条</span>
            </div>
            <div><span>全部传感器新增环境记录</span>
              <div class="line" style="margin-top: 10px;"></div>
            </div>
          </div>
        </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="auto"
      >
        <!-- <el-form-item label="传感器ID" prop="detect">
          <el-select
            v-model="detect"
            class="!w-130px"
            @change="handleQuery"
            >
            <el-option
              v-for="item in sensorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item> -->

        <el-form-item label="颗粒度" prop="type">
          <el-select
            v-model="queryParams.granularity"
            placeholder="请选择分钟/小时/天"
            class="!w-100px">
            <el-option label="分钟" value="realtime" />
            <el-option label="小时" value="hour" />
            <el-option label="天" value="day" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间段" prop="timeRange">
          <el-date-picker
          format="YYYY-MM-DD HH:mm:ss"
          v-model="selectTimeRange"
          type="datetimerange"
          :shortcuts="shortcuts"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          :disabled-date="disabledDate" 
          class="!w-335px"
        />
        </el-form-item>

        <!-- <div style="float:right; padding-right:78px"> -->
        <el-form-item >
          <el-button @click="handleQuery" style="background-color: #00778c;color:#ffffff;font-size: 13px;"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item> 

        <el-form-item label="筛选列">
          <el-cascader
            v-model="defaultOptionsCol"
            :options="optionsCol"
            :props="props"
            collapse-tags
            collapse-tags-tooltip
            :show-all-levels="false"
            @change="cascaderChange"
            class="!w-200px"
          />
        </el-form-item>

          <div style="float:right;">
          
        <el-button type="success" plain @click="handleExport" :loading="exportLoading" style="background-color: #00778c;color:#ffffff;font-size: 13px;top:2px;position: absolute;right:6px">
            <Icon icon="ep:download" class="mr-5px" /> 导出
          </el-button>
        </div>
        <!-- </div> -->
      </el-form>
    </template>
    <template #Content>
      <el-table v-loading="loading" :data="list" :border="true"  :show-overflow-tooltip="true" >
          <!-- 添加行号列 -->
        <el-table-column label="序号" align="center" width="100px" fixed="left">
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>
        </el-table-column>
        <el-table-column label="位置" align="center" min-width="200px" prop="address" fixed="left"/>
        <el-table-column label="时间" align="center" width="200px" prop="createTime"/>

        <template v-if="queryParams.granularity=='realtime'">
          <template v-for="item in tableHeaderArray" :key="item">
            <template v-for="position in item[1]" :key="position">
              <el-table-column :label="`${item[0]=='front'?'前门':'后门'}${position=='1'?'上':position=='2'?'中':'下'}`" align="center" width="200px">
                <el-table-column label="温度(℃)" align="center" width="200px" :prop="`front[${Number(position)-1}].temValue`" v-if="item[0]=='front'"/>
                <el-table-column label="温度(℃)" align="center" width="200px" :prop="`black[${Number(position)-1}].temValue`"  v-else/>"
                <el-table-column label="湿度(%RH)" align="center" width="200px" :prop="`front[${Number(position)-1}].humValue`" v-if="item[0]=='front'"/>
                <el-table-column label="湿度(%RH)" align="center" width="200px" :prop="`black[${Number(position)-1}].humValue`"  v-else/>
              </el-table-column>
            </template>
          </template>
        </template>

        <template v-if="queryParams.granularity!='realtime'">
          <template v-for="item in tableHeaderArray" :key="item">
            <template v-for="position in item[1]" :key="position">
              <el-table-column :label="`${item[0]=='front'?'前门':'后门'}${position[0]=='1'?'上':position[0]=='2'?'中':'下'}`" align="center" width="200px">
                <template v-for="choose in position[1]" :key="choose">
                  <template  v-if="choose=='avg'"> 
                    <el-table-column label="平均温度(℃)" align="center" width="200px" :prop="`front[${Number(position[0])-1}].tem_avg_value`" v-if="item[0]=='front'"/>
                    <el-table-column label="平均温度(℃)" align="center" width="200px" :prop="`black[${Number(position[0])-1}].tem_avg_value`" v-if="item[0]=='black'"/>

                    <el-table-column label="平均湿度(%RH)" align="center" width="200px" :prop="`front[${Number(position[0])-1}].hum_avg_value`" v-if="item[0]=='front'"/>
                    <el-table-column label="平均湿度(%RH)" align="center" width="200px" :prop="`black[${Number(position[0])-1}].hum_avg_value`" v-if="item[0]=='black'"/>
                  </template>

                  <template v-if="choose=='max'">
                    <template v-if="item[0]=='front'">
                      <el-table-column label="最大温度(℃)" align="center" width="200px" :prop="`front[${Number(position[0])-1}].tem_max_value`"/>
                      <el-table-column label="发生时间" align="center" width="200px" :prop="`front[${Number(position[0])-1}].max_time`" />
                    </template>

                    <template v-if="item[0]=='black'">
                      <el-table-column label="最大温度(℃)" align="center" width="200px" :prop="`black[${Number(position[0])-1}].tem_max_value`"/>
                      <el-table-column label="最大值发生时间" align="center" width="200px" :prop="`black[${Number(position[0])-1}].max_time`" />
                    </template>
                    
                    <template v-if="item[0]=='front'">
                      <el-table-column label="最大湿度(%RH)" align="center" width="200px" :prop="`front[${Number(position[0])-1}].hum_max_value`"/>
                      <el-table-column label="发生时间" align="center" width="200px" :prop="`front[${Number(position[0])-1}].max_time`"/>
                    </template> 

                    <template v-if="item[0]=='black'">
                      <el-table-column label="最大湿度(%RH)" align="center" width="200px" :prop="`black[${Number(position[0])-1}].hum_max_value`"/>
                      <el-table-column label="最大值发生时间" align="center" width="200px" :prop="`black[${Number(position[0])-1}].max_time`"/>
                    </template>
                  </template>
                  
                  <template v-if="choose=='min'">
                    <template v-if="item[0]=='front'">
                      <el-table-column label="最小温度(℃)" align="center" width="200px" :prop="`front[${Number(position[0])-1}].tem_min_value`"/>
                      <el-table-column label="发生时间" align="center" width="200px" :prop="`front[${Number(position[0])-1}].min_time`"/>
                    </template>

                    <template v-if="item[0]=='black'">
                      <el-table-column label="最小温度(℃)" align="center" width="200px" :prop="`black[${Number(position[0])-1}].tem_min_value`"/>
                      <el-table-column label="发生时间" align="center" width="200px" :prop="`black[${Number(position[0])-1}].min_time`"/>
                    </template>

                    <template v-if="item[0]=='front'">
                      <el-table-column label="最小湿度(%RH)" align="center" width="200px" :prop="`front[${Number(position[0])-1}].hum_min_value`"/>
                      <el-table-column label="发生时间" align="center" width="200px" :prop="`front[${Number(position[0])-1}].min_time`"/>
                    </template>

                    <template v-if="item[0]=='black'">
                      <el-table-column label="最小湿度(%RH)" align="center" width="200px" :prop="`black[${Number(position[0])-1}].hum_min_value`"/>
                      <el-table-column label="发生时间" align="center" width="200px" :prop="`black[${Number(position[0])-1}].min_time`"/>
                    </template>
                  </template>
                </template>
              </el-table-column>
            </template>
          </template>
        </template>

        <!-- <el-table-column label="前门" align="center" v-if="queryParams.granularity!='realtime'&&nowArray.find((item)=>Array.isArray(item)&&item[0]=='front')!=null">

          <template v-for="numIndex in [1,2,3,4]" :key="numIndex">
            <template v-if="nowArray.find((item)=>Array.isArray(item)&&item[0]=='front'&&item[1]==numIndex)!=null">
              <el-table-column label="传感器ID" align="center" width="200px">{{ numIndex }}</el-table-column>
              <el-table-column label="平均温度(℃)" align="center" width="200px" :prop="`front[${numIndex-1}].tem_avg_value`" v-if="nowArray.find((item)=>Array.isArray(item)&&item[0]=='front'&&item[1]==numIndex&&item[2]=='')!=null"/>
              <el-table-column label="平均湿度(%RH)" align="center" width="200px" :prop="`front[${numIndex-1}].hum_avg_value`"/>
              <el-table-column label="最大温度(℃)" align="center" width="200px" :prop="`front[${numIndex-1}].tem_max_value`"/>
              <el-table-column label="最大湿度(%RH)" align="center" width="200px" :prop="`front[${numIndex-1}].hum_max_value`"/>
              <el-table-column label="最大值发生时间" align="center" width="200px" :prop="`front[${numIndex-1}].max_time`"/>
              <el-table-column label="最小温度(℃)" align="center" width="200px" :prop="`front[${numIndex-1}].tem_min_value`"/>
              <el-table-column label="最小湿度(%RH)" align="center" width="200px" :prop="`front[${numIndex-1}].hum_min_value`"/>
              <el-table-column label="最小值发生时间" align="center" width="200px" :prop="`front[${numIndex-1}].min_time`"/>
            </template>
          </template>
        </el-table-column>
        <el-table-column label="后门" align="center" v-if="queryParams.granularity!='realtime'&&nowArray.find((item)=>Array.isArray(item)&&item[0]=='black')!=null">
          <template v-for="numIndex in [1,2,3,4]" :key="numIndex">
            <template v-if="nowArray.find((item)=>Array.isArray(item)&&item[0]=='black'&&item[1]==numIndex)!=null">
              <el-table-column label="传感器ID" align="center" width="200px">{{ numIndex }}</el-table-column>
              <el-table-column label="平均温度(℃)" align="center" width="200px" :prop="`front[${numIndex-1}].tem_avg_value`"/>
              <el-table-column label="平均湿度(%RH)" align="center" width="200px" :prop="`front[${numIndex-1}].hum_avg_value`"/>
              <el-table-column label="最大温度(℃)" align="center" width="200px" :prop="`front[${numIndex-1}].tem_max_value`"/>
              <el-table-column label="最大湿度(%RH)" align="center" width="200px" :prop="`front[${numIndex-1}].hum_max_value`"/>
              <el-table-column label="最大值发生时间" align="center" width="200px" :prop="`front[${numIndex-1}].max_time`"/>
              <el-table-column label="最小温度(℃)" align="center" width="200px" :prop="`front[${numIndex-1}].tem_min_value`"/>
              <el-table-column label="最小湿度(%RH)" align="center" width="200px" :prop="`front[${numIndex-1}].hum_min_value`"/>
              <el-table-column label="最小值发生时间" align="center" width="200px" :prop="`front[${numIndex-1}].min_time`"/>
            </template>
          </template>
        </el-table-column> -->
        <el-table-column label="详情" align="center" fixed="right" width="100px">
          <template #default="{ row }"> 
            <el-button type="primary" @click="toDetails(row.id,  row.address)" style="background-color: #00778c;color:#ffffff;font-size: 13px;">详情</el-button>
          </template>
        </el-table-column>

        <!-- 遍历其他列 -->
        <!-- <template v-for="column in tableColumns">
          <el-table-column :key="column.prop" 
                           :label="column.label" 
                           :align="column.align" 
                           :prop="column.prop" 
                           :formatter="column.formatter" 
                           :min-width="column.width" 
                           v-if="column.istrue&&column.slot!= 'actions'" 
                           >
            <template #default="{ row }">
              <div v-if="column.slot === 'actions'">
                <el-button type="primary" @click="toDetails(row.pdu_id, row.location, row.address.address, row.address.channel, row.address.position, row.sensor_id)">详情</el-button>
              </div>
              <div v-else-if="column.slot === 'detect'">
                {{ getCombinedString(row.address?.channel, row.address?.position) }}
              </div>
            </template>
          </el-table-column>

          <el-table-column :key="column.prop" 
                           :label="column.label" 
                           :align="column.align" 
                           :prop="column.prop" 
                           :formatter="column.formatter" 
                           :width="column.width" 
                           v-if="column.istrue&&column.slot == 'actions'" 
                           fixed="right"
                           >
            <template #default="{ row }">
              <div v-if="column.slot === 'actions'">
                <el-button type="primary" @click="toDetails(row.pdu_id, row.location, row.address.address, row.address.channel, row.address.position, row.sensor_id)">详情</el-button>
              </div>
              <div v-else-if="column.slot === 'detect'">
                {{ getCombinedString(row.address?.channel, row.address?.position) }}
              </div>
            </template>
          </el-table-column>
        </template> -->
        <!-- 超过一万条数据提示信息 -->
        <template v-if="shouldShowDataExceedMessage" #append>
          <tr>
            <td colspan="列数" style="text-align: center; padding: 12px 0;">
              <span style="margin:0 12px; color: red;">数据量过大，请筛选后查看更多数据。</span>
            </td>
          </tr>
        </template>
      </el-table>
      <!-- 分页 -->
      <Pagination
        :total="total"
        :page-size-arr="pageSizeArr"
        layout = "sizes, prev, pager, next, jumper"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
      <div class="realTotal" v-if="list?.length != 0">共 {{ realTotel }} 条</div>
    </template>
  </CommonMenu>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import { EnvDataApi } from '@/api/pdu/envData'
import { CabinetApi } from '@/api/cabinet/info'
import { formatDate, endOfDay, convertDate, addTime } from '@/utils/formatTime'
import { HistoryDataApi } from '@/api/cabinet/historydata'
import { ElMessage } from 'element-plus'
import PDUImage from '@/assets/imgs/PDU.jpg';
import { now } from 'lodash-es'
import { build } from 'vite'
const { push } = useRouter()
/** pdu历史数据 列表 */
defineOptions({ name: 'PDUEnvHistoryData' })

const navList = ref([]) as any // 左侧导航栏树结构列表
const lastMinTotalData = ref(0)
const lastHourTotalData = ref(0)
const lastDayTotalData = ref(0)
const message = useMessage() // 消息弹窗
const loading = ref(true) // 列表的加载中
const list = ref<Array<{ }>>([]); // 列表数据
const total = ref(0) // 数据总条数 超过10000条为10000
const realTotel = ref(0) // 数据的真实总条数
const detect = ref('all') // 监测点的值 默认全部
const selectTimeRange = ref()

const queryParams = reactive({
  pageNo: 1,
  pageSize: 15,
  granularity: 'realtime',
  channel: undefined as number | undefined,
  position: undefined as number | undefined,
  timeRange: undefined,
  cabinetIds:[]
})
const pageSizeArr = ref([15,30,50,100])
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中
const carouselItems = ref([
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
      { imgUrl: PDUImage},
    ]);//侧边栏轮播图图片路径
// 时间段快捷选项
const shortcuts = [
    {
    text: '最近一小时',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setHours(start.getHours() - 1)
      return [start, end]
    },
  },
    {
    text: '最近一天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 1)
      return [start, end]
    },
  },
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

// 传感器选项
const sensorOptions = ref([
  { value: 'all', label: '全部'},
  { value: "1", label: '传感器1'},
  { value: "2", label: '传感器2'},
  { value: "3", label: '传感器3'},
  { value: "4", label: '传感器4'},
])

//筛选选项
const props = { multiple: true}
const defaultOptionsCol = ref([["front","2"],['black','2']]);
const optionsCol = ref([
  { value: "front", label: '前门',children: [
    {value:"1",label:"上" },
    {value:"2",label:"中"},
    {value:"3",label:"下"},
  ]},
  { value: "black", label: '后门',children: [
    {value:"1",label:"上" },
    {value:"2",label:"中"},
    {value:"3",label:"下"},
  ]}
])
// const originalArray = ref(["tem", "hum", "location", "sensor_id"])
// const nowArray = ref([["front","2"],['black','2']]);
const tableHeaderArray=ref([["front",['2']],["black",["2"]]])

// 处理筛选列变化
const cascaderChange = (selectedCol) => {
  let tem=buildChoose(selectedCol)
  console.log(tem,"==========================tem");
  sortArray(tem,1)
  tableHeaderArray.value=tem;
  console.log(tableHeaderArray.value,"==========================tableHeaderArray");
}

function sortArray(arr,now) {
  if(now==1){
    arr.sort((a, b) => a[0]!=b[0]?(a[0]=='front'?-1:1):1);
    arr.forEach(item=>{
      sortArray(item[1],2);
    })
  }else if(now==2){
    arr.sort((a, b)=>a[0]-b[0])
    if(queryParams.granularity!='realtime'){
      arr.forEach(item=>{
        sortArray(item[1],3);
      })
    }
  }else if(now==3){
    arr.sort((a,b)=>{
      if(a=='avg'){return -1;}
      if(a=='max'&&b=='avg'){return -1;}
      return 1;
    })
  }
}
function buildChoose(arr){
  if(arr==null||arr.length==0)return [];
  if(arr[0].length==1) {
    let ans=[];
    for(let i=0;i<arr.length;i++){
      ans.push(arr[i][0]);
    }
    return ans;
  }
  let ans=[];
  for(let i=0;i<arr.length;i++){
    let tem=ans.find(item=>item[0]==arr[i][0]);
    if(tem!=null){
      tem[1].push(arr[i].slice(1))
    }else{
      ans.push([arr[i][0],[arr[i].slice(1)]])
    }
  }
  for(let i=0;i<ans.length;i++){
    ans[i][1]=buildChoose(ans[i][1]);
  }
  return ans;
}
// 最后一页显示数据量过大的提示
const shouldShowDataExceedMessage = computed(() => {
  const lastPageNo = Math.ceil(total.value / queryParams.pageSize);
  return queryParams.pageNo === lastPageNo && total.value >= 10000;
});


watch(() => queryParams.granularity, (newValues) => {
    const  newGranularity = newValues;
    if ( newGranularity == 'realtime'){
      // 配置筛选列
      defaultOptionsCol.value = [["front","2"],['black','2']];
      tableHeaderArray.value=[["front",['2']],["black",["2"]]];
      optionsCol.value =[
        { value: "front", label: '前门',children: [
          {value:"1",label:"上" },
          {value:"2",label:"中"},
          {value:"3",label:"下"},
        ]},
        { value: "black", label: '后门',children: [
          {value:"1",label:"上"},
          {value:"2",label:"中"},
          {value:"3",label:"下"},
        ]},
      ]
      // originalArray.value =["tem", "hum", "location", "sensor_id"];
      // 配置表格列
      // tableColumns.value =([
      //   { label: '位置', align: 'center', prop: 'address.address' , istrue:true, width: '180px'},
      //   { label: '网络地址', align: 'center', prop: 'location' , istrue:false, width: '180px'},
      //   { label: '时间', align: 'center', prop: 'create_time', width: '200px', formatter: formatTime, istrue:true},
      //   { label: '监测点', align: 'center', slot: 'detect' , istrue: true},
      //   { label: '传感器ID', align: 'center', prop: 'sensor_id' , istrue:false, width: '120px'},
      //   { label: '温度(℃)', align: 'center', prop: 'tem_value', istrue:true, formatter: formatData},
      //   { label: '湿度(%RH)', align: 'center', prop: 'hum_value' , istrue:true, formatter: formatData},
      //   { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
      // ]);
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }else{
      // nowArray.value = [["front",[]"2"],["black","2","max"]];
      // 配置筛选列
      // defaultOptionsCol.value = [["tem"], ["hum"]];
      defaultOptionsCol.value = [["front","2","max"],["black","2","max"]];
      tableHeaderArray.value=[["front",[["2",["max"]]]],["black",[["2",["max"]]]]]
      optionsCol.value =[
        { value: "front", label: '前门',children: [
          {value:"1",label:"上",children:[
            {value:'max',label:'最大值'},
            {value:'min',label:'最小值'},
            {value:'avg',label:'平均值'}
          ]},
          {value:"2",label:"中",children:[
            {value:'max',label:'最大值'},
            {value:'min',label:'最小值'},
            {value:'avg',label:'平均值'}
          ]},
          {value:"3",label:"下",children:[
            {value:'max',label:'最大值'},
            {value:'min',label:'最小值'},
            {value:'avg',label:'平均值'}
          ]},
        ]},
        { value: "black", label: '后门',children: [
          {value:"1",label:"上",children:[
            {value:'max',label:'最大值'},
            {value:'min',label:'最小值'},
            {value:'avg',label:'平均值'}
          ]},
          {value:"2",label:"中",children:[
            {value:'max',label:'最大值'},
            {value:'min',label:'最小值'},
            {value:'avg',label:'平均值'}
          ]},
          {value:"3",label:"下",children:[
            {value:'max',label:'最大值'},
            {value:'min',label:'最小值'},
            {value:'avg',label:'平均值'}
          ]},
        ]}
      ]

      // 配置筛选列
      // defaultOptionsCol.value = [
      //   ["tem_value", "tem_avg_value"],["tem_value", "tem_max"], ["tem_value", "tem_min"],
      //   ["hum_value", "hum_avg_value"],["hum_value", "hum_max"], ["hum_value", "hum_min"],
      // ];
      // optionsCol.value = [
      //   { value: "tem_value", label: '温度', children: [
      //       { value: "tem_avg_value", label: '平均温度'},
      //       { value: "tem_max", label: '最高温度' },
      //       { value: "tem_min", label: '最低温度' },
      //     ]
      //   },
      //   { value: "hum_value", label: '湿度', children: [
      //       { value: "hum_avg_value", label: '平均湿度'},
      //       { value: "hum_max", label: '最大湿度' },
      //       { value: "hum_min", label: '最小湿度' },
      //     ]
      //   },
      //   {
      //     value: "sensor_id", label: '传感器ID'
      //   },
      //   {
      //     value: "location", label: '网络地址'
      //   }
      // ] as any;
      // originalArray.value =["tem_avg_value", "tem_max", "tem_min", 
      //                     "hum_avg_value", "hum_max", "hum_min", "location", "sensor_id"],
      // // 配置表格列
      // tableColumns.value = [
      //   { label: '位置', align: 'center', prop: 'address.address', istrue:true, width: '180px'}, 
      //   { label: '网络地址', align: 'center', prop: 'location' , istrue:false, width: '160px'},
      //   { label: '监测点', align: 'center', slot: 'detect' , istrue: true},
      //   { label: '传感器ID', align: 'center', prop: 'sensor_id' , istrue:false, width: '160px'},
      //   { label: '记录时间', align: 'center', prop: 'create_time' , width: '230px', istrue:true,formatter: formatTime},
      //   { label: '平均湿度(%RH)', align: 'center', prop: 'hum_avg_value', istrue:false, width: '180px', formatter: formatData},
      //   { label: '最大湿度(%RH)', align: 'center', prop: 'hum_max_value', istrue:false, width: '180px', formatter: formatData },
      //   { label: '最大湿度时间', align: 'center', prop: 'hum_max_time' , width: '230px', istrue:false,formatter: formatTime},
      //   { label: '最小湿度(%RH)', align: 'center', prop: 'hum_min_value', istrue:false, width: '180px', formatter: formatData },
      //   { label: '最小湿度时间', align: 'center', prop: 'hum_min_time' , width: '230px', istrue:false,formatter: formatTime},
      //   { label: '平均温度(℃)', align: 'center', prop: 'tem_avg_value', istrue:true, width: '180px', formatter: formatData },
      //   { label: '最高温度(℃)', align: 'center', prop: 'tem_max_value', istrue:true, width: '180px', formatter: formatData },
      //   { label: '最高温度时间', align: 'center', prop: 'tem_max_time' , width: '230px', istrue:true,formatter: formatTime},
      //   { label: '最低温度(℃)', align: 'center', prop: 'tem_min_value', istrue:true, width: '180px', formatter: formatData },
      //   { label: '最低温度时间', align: 'center', prop: 'tem_min_time' , width: '230px', istrue:true,formatter: formatTime},
      //   { label: '操作', align: 'center', slot: 'actions', istrue:true, width: '160px'},
      // ] as any;
      queryParams.pageNo = 1;
      queryParams.pageSize = 15;
      getList();
    }
  });

// const tableColumns = ref([
//   { label: '位置', align: 'center', prop: 'address.address' , istrue:true, width: '230px'},
//   { label: '网络地址', align: 'center', prop: 'location' , istrue:false, width: '180px'},
//   { label: '时间', align: 'center', prop: 'create_time', width: '200px', formatter: formatTime, istrue:true},
//   { label: '监测点', align: 'center', slot: 'detect' , istrue: true},
//   { label: '传感器ID', align: 'center', prop: 'sensor_id' , istrue:true, width: '120px'},
//   { label: '温度(℃)', align: 'center', prop: 'tem_value', istrue:true, formatter: formatData},
//   { label: '湿度(%RH)', align: 'center', prop: 'hum_value' , istrue:true, formatter: formatData1},
//   { label: '操作', align: 'center', slot: 'actions' , istrue:true, width: '120px'},
// ]) as any;

/** 查询列表 */
const getList = async (needLoading=true) => {
  loading.value = needLoading;
  try {
    if ( selectTimeRange.value != undefined){
      // 格式化日期范围 加上23:59:59的时分秒 
      const selectedStartTime = formatDate(selectTimeRange.value[0])
     
      const selectedEndTime = formatDate(selectTimeRange.value[1])
     // selectTimeRange.value = [selectedStartTime, selectedEndTime];
      queryParams.timeRange = [selectedStartTime, selectedEndTime];
    }else{
      queryParams.timeRange = [];
    }
    const data = await HistoryDataApi.getHistoryEnvData(queryParams)
    for(let i=0;i<data.list.length;i++){
      data.list[i].front?.forEach((item:any) => {
        item.temValue = item.temValue?.toFixed(1)
        item.humValue = item.humValue?.toFixed(0)
        item.hum_avg_value=item.hum_avg_value?.toFixed(0)
        item.hum_max_value=item.hum_max_value?.toFixed(0)
        item.hum_min_value=item.hum_min_value?.toFixed(0)
        item.tem_avg_value=item.tem_avg_value?.toFixed(1)
        item.tem_max_value=item.tem_max_value?.toFixed(1)
        item.tem_min_value=item.tem_min_value?.toFixed(1)
      })
      data.list[i].black?.forEach((item:any) => {
        item.temValue = item.temValue?.toFixed(1)
        item.humValue = item.humValue?.toFixed(0)
        item.hum_avg_value=item.hum_avg_value?.toFixed(0)
        item.hum_max_value=item.hum_max_value?.toFixed(0)
        item.hum_min_value=item.hum_min_value?.toFixed(0)
        item.tem_avg_value=item.tem_avg_value?.toFixed(1)
        item.tem_max_value=item.tem_max_value?.toFixed(1)
        item.tem_min_value=item.tem_min_value?.toFixed(1)
      })
    }
    for(let i = 0;i<data.list.length;i++){
      let frontHave=[];
      data.list[i].front=data.list[i].front==null?[]:data.list[i].front;
      data.list[i]?.front?.sort((a:any, b:any) => a.sensorId - b.sensorId);
      for(let j=0;j<data.list[i].front.length;j++){
        frontHave.push(data.list[i].front[j].sensorId)
      }
      for(let j=1;j<=3;j++){
        if(!frontHave.includes(j)){
          data.list[i].front.splice(j-1,0,null)
        }
      }

      let blackHave=[];
      data.list[i].black=data.list[i].black==null?[]:data.list[i].black;
      data.list[i].black?.sort((a:any, b:any) => a.sensorId - b.sensorId);
      for(let j=0;j<data.list[i].black?.length||0;j++){
        blackHave.push(data.list[i].black[j].sensorId)
      }
      for(let j=1;j<=3;j++){
        if(!blackHave.includes(j)){
          data.list[i].black?.splice(j-1,0,null)
        }
      }
    }
    console.log("data.list===================",data.list);
    list.value = data.list
    realTotel.value = data.total
    if (data.total > 10000){
      total.value = 10000
    }else{
      total.value = data.total
    }
    
  } finally {
    loading.value = false
  }
}

// 格式化监测位置
function getCombinedString(channel: number, position: number) {
  let channelText = '';
  let positionText = '';

   if (channel === 1) {
    channelText = '前';
  } else if (channel === 2) {
    channelText = '后';
  }
  if (position === 1) {
    positionText = '上';
  } else if (position === 2) {
    positionText = '中';
  } else if (position === 3) {
    positionText = '下';
  }
  return `${channelText}${positionText}`;
}

// 格式化日期
function formatTime(_row: any, _column: any, cellValue: number): string {
  if (!cellValue) {
    return ''
  }

  return dayjs(cellValue).format('YYYY-MM-DD HH:mm')
}

// 格式化温湿度列数据，保留一位小数
function formatData(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(1);
}
function formatData1(_row: any, _column: any, cellValue: number): string {
  return Number(cellValue).toFixed(0);
}

// 导航栏选择后触发
const handleCheck = async (node) => {
   let arr = [] as any
  node.forEach(item => { 
    if(item.type == 3){
      arr.push(item.id);
    }
  });
   //没筛选到 不显示任何数据 参数传0 后端返回空
   if(arr.length == 0 && node.length != 0){
      arr.push(0)
      ElMessage({
        message: '暂无数据',
        type: 'warning',
      });
    }
    queryParams.cabinetIds = arr
    console.log('123123123',queryParams.cabinetIds)
    handleQuery()

}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 禁选未来的日期
const disabledDate = (date) => {
  const today = new Date();
  return date > today;
}

/** 搜索按钮操作 */
const handleQuery = () => {
    queryParams.pageNo = 1
    // getNavNewData();
    getList();
}

/** 详情操作*/
const toDetails = (cabinetId: number|null, address: string|null) => {
  // let detectValue = channel?.toString()+position?.toString()
  console.log("selectTimeRange",selectTimeRange);
  if(selectTimeRange.value!=null&&selectTimeRange.value.length==2){
    push({path:"/cabinet/record/CabinetEnvAnalysis",state:{cabinetId,address,start:dayjs(selectTimeRange.value[0]).format("YYYY-MM-DD HH:mm:ss"),end:dayjs(selectTimeRange.value[1]).format("YYYY-MM-DD HH:mm:ss")}})
  }else{
    push({path:"/cabinet/record/CabinetEnvAnalysis",state:{cabinetId,address}})
  }
}

/** 导出按钮操作 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    queryParams.pageNo = 1
    exportLoading.value = true
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await HistoryDataApi.exportHistoryEnvData(queryParams, axiosConfig)
    await download.excel(data, '机柜环境记录.xlsx')
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error)
  } finally {
    exportLoading.value = false
  }
}

// 获取导航的数据显示
const getNavNewData = async() => {
  const res = await HistoryDataApi.getHistoryEnvDataCount()
  lastMinTotalData.value = res.min
  lastHourTotalData.value = res.hour
  lastDayTotalData.value = res.day
}
const format = (date) => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

let intervalId=null as any;
/** 初始化 **/
onMounted( () => {
  const now = new Date()
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
   // 使用上述自定义的 format 函数将日期对象转换为指定格式的字符串
selectTimeRange.value = [
  dayjs(startOfMonth).format('YYYY-MM-DD HH:mm:ss'),
  dayjs(now).format('YYYY-MM-DD HH:mm:ss')
];
intervalId=setInterval(() => {
  getList(false);
}, 60000);
  getNavList()
  getNavNewData()
  getList()
});
onBeforeUnmount(() => {
  if(intervalId!=null){
    clearInterval(intervalId);
  }
})
</script>

<style scoped>
.realTotal{
  float: right;
  padding-top: 20px;
  padding-right: 20px;
  font-size: 14px;
  font-weight: 400; 
  color: #606266
}

.nav_data{
  padding-left: 12px;
  width: 190px;
}
.nav_content span{
  font-size: 18px;
}
.carousel-container {
  width: 100%;
  max-width: 100%;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover; 
}

.description-item {
  display: flex;
  align-items: center;
}

.label {
  text-align: right; /* 文本右对齐 */
  margin-right: 10px; /* 控制冒号后的间距 */
  text-align: left;
}

.value {
  flex: 1; /* 自动扩展以对齐数据 */
  text-align: left;

}
  .line {
    height: 1px;
    margin-top: 28px;

    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
  ::v-deep .el-table .el-table__header th {
    background-color: #f7f7f7;
    color: #909399;
}
/deep/ .el-pagination.is-background .el-pager li.is-active {
  background-color: #00778c;
}
</style>
