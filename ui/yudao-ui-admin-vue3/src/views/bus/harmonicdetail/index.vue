<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="谐波监测详情">
    <template #NavInfo>
      <div >
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/Bus.png" /></div>
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
        <div class="line"></div>

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
        
        <el-form-item>  
          <span>位置：</span>
          <el-tag size="large">{{ roomName?location:'未绑定' }}</el-tag>
          <span> 名称：</span>
          <el-tag size="large">{{ busName }}</el-tag>
          <span>网络地址：</span>
          <el-tag size="large" style="margin-right:8vw">{{ devKey }}</el-tag>
          <el-select
            v-model="queryParams.harmonicType"
            placeholder="请选择"
            style="width: 240px"
          >
            <el-option v-for="(item,index) in harmonicLabel" :key="index" :label="item.label" :value="item.value" />
          </el-select>

          
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
      <div >
        <div v-if="realTimeVis">
          <div>{{ harmonicLabel[queryParams.harmonicType].label +'32次谐波含量谐波柱状图' }}</div>
          <HarmonicRealTime  width="70vw" height="58vh" :list="harmonicRealTime"/>
        </div>
        <br/>
        <div v-if="lineVis">
          <div style="display: flex;align-items: center;justify-content: space-between;width: 80%">
            <div>{{ harmonicLabel[queryParams.harmonicType].label +'各次谐波含量谐波趋势图' }}</div>
            <div>
              <el-select
                v-model="queryParams.harmonicArr"
                multiple
                placeholder="Select"
                collapse-tags
                collapse-tags-tooltip
                style="width: 240px"
              >
              <el-option label='全选' value='全选' @click='selectAll' />
              <el-option
                v-for="item in harmonicMultiple"
                :key="item.value"
                :label="item.label"
                :value="item.value"
                @click="getLabel(item.value)"
              />
              </el-select>

              <el-button 
                @click="subtractOneDay();handleDayPick()" 
              >
                &lt;
              </el-button>
              <el-date-picker
                v-model="queryParams.oldTime"
                value-format="YYYY-MM-DD HH:mm:ss"
                type="date"
                :disabled-date="disabledDate"
                @change="handleDayPick"
                class="!w-160px"
              />
              <el-button 
                @click="addtractOneDay();handleDayPick()" 
              >
                &gt;
              </el-button>
            </div>
          </div>
          <HarmonicLine  width="70vw" height="58vh" :list="harmonicLine"/>
        </div>
      </div>
    </template>
  </CommonMenu>


</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/bus/busindex'
import { ElTree } from 'element-plus'
import HarmonicRealTime from './component/HarmonicRealTime.vue'
import HarmonicLine from './component/HarmonicLine.vue'
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;


/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const location = ref(query.location);
const busName = ref(query.busName);
const devKey = ref(query.devKey);
const roomName = ref(query.roomName);
const haveSearch = ref(false);
const switchValue = ref(1);
const harmonicRealTime = ref();
const harmonicLine = ref({ value: { series: [], time: [] } }) as any;
const realTimeVis = ref(false);
const lineVis = ref(false);
const seriesAndTimeArr = ref() as any;
const harmonicLabel = ref([
  {
  label: "A相电压谐波",
  value: 0
  },
  {
  label: "B相电压谐波",
  value: 1
  },
  {
  label: "C相电压谐波",
  value: 2
  },
  {
  label: "A相电流谐波",
  value: 3
  },
  {
  label: "B相电流谐波",
  value: 4
  },{
  label: "C相电流谐波",
  value: 5
  }
])
const harmonicMultiple = ref([
  {
  label: "A相电压1次谐波含量",
  value: 1
  },
  {
  label: "A相电压2次谐波含量",
  value: 2
  },
  {
  label: "A相电压3次谐波含量",
  value: 3
  },
  {
  label: "A相电压4次谐波含量",
  value: 4
  },
  {
  label: "A相电压5次谐波含量",
  value: 5
  },
  {
  label: "A相电压6次谐波含量",
  value: 6
  },
  {
  label: "A相电压7次谐波含量",
  value: 7
  },
  {
  label: "A相电压8次谐波含量",
  value: 8
  },
  {
  label: "A相电压9次谐波含量",
  value: 9
  },
  {
  label: "A相电压10次谐波含量",
  value: 10
  },
  {
  label: "A相电压11次谐波含量",
  value: 11
  },
  {
  label: "A相电压12次谐波含量",
  value: 12
  },
  {
  label: "A相电压13次谐波含量",
  value: 13
  },
  {
  label: "A相电压14次谐波含量",
  value: 14
  },
  {
  label: "A相电压15次谐波含量",
  value: 15
  },
  {
  label: "A相电压16次谐波含量",
  value: 16
  },
  {
  label: "A相电压17次谐波含量",
  value: 17
  },
  {
  label: "A相电压18次谐波含量",
  value: 18
  },
  {
  label: "A相电压19次谐波含量",
  value: 19
  },
  {
  label: "A相电压20次谐波含量",
  value: 20
  },
  {
  label: "A相电压21次谐波含量",
  value: 21
  },
  {
  label: "A相电压22次谐波含量",
  value: 22
  },
  {
  label: "A相电压23次谐波含量",
  value: 23
  },
  {
  label: "A相电压24次谐波含量",
  value: 24
  },
  {
  label: "A相电压25次谐波含量",
  value: 25
  },
  {
  label: "A相电压26次谐波含量",
  value: 26
  },
  {
  label: "A相电压27次谐波含量",
  value: 27
  },
  {
  label: "A相电压28次谐波含量",
  value: 28
  },
  {
  label: "A相电压29次谐波含量",
  value: 29
  },
  {
  label: "A相电压30次谐波含量",
  value: 30
  },
  {
  label: "A相电压31次谐波含量",
  value: 31
  },
  {
  label: "A相电压32次谐波含量",
  value: 32
  }
])

const serverRoomArr =  ref([])
const labeldata = ref('')
labeldata.value = harmonicMultiple.value[0].label

const getLabel = (num) => {
  labeldata.value = harmonicMultiple.value[num-1].label
}

const selectAll = () => {

  if(queryParams.harmonicArr != null && queryParams.harmonicArr?.length < 33 ){
    queryParams.harmonicArr = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32]
  }
  if(queryParams.harmonicArr != null && queryParams.harmonicArr?.length == 33){
    queryParams.harmonicArr = [1]
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


// const activeNames = ref(["1","2","3","4","5"])

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  harmonicType : 0,
  harmonicArr:[1],
  devKey : query.devKey,
  busId: query.busId,
  outputNumber : 10,
  createTime: undefined,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0)),
  newTime : undefined,
}) as any

const handleClick = async (row) => {
  if(row.type != null  && row.type == 6){
    queryParams.devKey = row.unique
    const data = await IndexApi.getBusIdByDevKey({devKey : queryParams.devKey});
    queryParams.busId = data;
    haveSearch.value = false;
    handleQuery();
  }
}

const getDetail = async () => {

  if(!haveSearch.value){
    const data = await IndexApi.getHarmonicRedis(queryParams);
    harmonicRealTime.value = data;
    if(harmonicRealTime.value.times != null){
      realTimeVis.value = true;
    }else{
      realTimeVis.value = false;
    }
    haveSearch.value = true;
  }

  const lineData = await IndexApi.getHarmonicLine(queryParams);
  seriesAndTimeArr.value = lineData;
  console.log('seriesAndTimeArr.value',seriesAndTimeArr.value)
  if(seriesAndTimeArr.value.time != null && seriesAndTimeArr.value.time?.length > 0){
    const filteredSeries = seriesAndTimeArr.value.series.filter((item,index) => queryParams.harmonicArr.includes(index));
    console.log('filteredSeries',filteredSeries)
    harmonicLine.value.series = filteredSeries;
    harmonicLine.value.time = seriesAndTimeArr.value.time;
    lineVis.value = true;
  } else {
    lineVis.value = false;
  }
}


const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()

// const beforeSerUnmount = () => {
//     serChart?.dispose(); // 销毁图表实例
// };

// window.addEventListener('resize', function() {
//     rankChart?.resize(); 
//     powChart?.resize(); 
//     temChart?.resize(); 
// });

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

watch(() => [queryParams.harmonicType], () => {
  if(queryParams.harmonicType == 0){
    harmonicMultiple.value = [
      {
      label: "A相电压1次谐波含量",
      value: 1
      },
      {
      label: "A相电压2次谐波含量",
      value: 2
      },
      {
      label: "A相电压3次谐波含量",
      value: 3
      },
      {
      label: "A相电压4次谐波含量",
      value: 4
      },
      {
      label: "A相电压5次谐波含量",
      value: 5
      },
      {
      label: "A相电压6次谐波含量",
      value: 6
      },
      {
      label: "A相电压7次谐波含量",
      value: 7
      },
      {
      label: "A相电压8次谐波含量",
      value: 8
      },
      {
      label: "A相电压9次谐波含量",
      value: 9
      },
      {
      label: "A相电压10次谐波含量",
      value: 10
      },
      {
      label: "A相电压11次谐波含量",
      value: 11
      },
      {
      label: "A相电压12次谐波含量",
      value: 12
      },
      {
      label: "A相电压13次谐波含量",
      value: 13
      },
      {
      label: "A相电压14次谐波含量",
      value: 14
      },
      {
      label: "A相电压15次谐波含量",
      value: 15
      },
      {
      label: "A相电压16次谐波含量",
      value: 16
      },
      {
      label: "A相电压17次谐波含量",
      value: 17
      },
      {
      label: "A相电压18次谐波含量",
      value: 18
      },
      {
      label: "A相电压19次谐波含量",
      value: 19
      },
      {
      label: "A相电压20次谐波含量",
      value: 20
      },
      {
      label: "A相电压21次谐波含量",
      value: 21
      },
      {
      label: "A相电压22次谐波含量",
      value: 22
      },
      {
      label: "A相电压23次谐波含量",
      value: 23
      },
      {
      label: "A相电压24次谐波含量",
      value: 24
      },
      {
      label: "A相电压25次谐波含量",
      value: 25
      },
      {
      label: "A相电压26次谐波含量",
      value: 26
      },
      {
      label: "A相电压27次谐波含量",
      value: 27
      },
      {
      label: "A相电压28次谐波含量",
      value: 28
      },
      {
      label: "A相电压29次谐波含量",
      value: 29
      },
      {
      label: "A相电压30次谐波含量",
      value: 30
      },
      {
      label: "A相电压31次谐波含量",
      value: 31
      },
      {
      label: "A相电压32次谐波含量",
      value: 32
      }
    ]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 1){
    harmonicMultiple.value = [{
      label: "B相电压1次谐波含量",
      value: 1
      },
      {
      label: "B相电压2次谐波含量",
      value: 2
      },
      {
      label: "B相电压3次谐波含量",
      value: 3
      },
      {
      label: "B相电压4次谐波含量",
      value: 4
      },
      {
      label: "B相电压5次谐波含量",
      value: 5
      },
      {
      label: "B相电压6次谐波含量",
      value: 6
      },
      {
      label: "B相电压7次谐波含量",
      value: 7
      },
      {
      label: "B相电压8次谐波含量",
      value: 8
      },
      {
      label: "B相电压9次谐波含量",
      value: 9
      },
      {
      label: "B相电压10次谐波含量",
      value: 10
      },
      {
      label: "B相电压11次谐波含量",
      value: 11
      },
      {
      label: "B相电压12次谐波含量",
      value: 12
      },
      {
      label: "B相电压13次谐波含量",
      value: 13
      },
      {
      label: "B相电压14次谐波含量",
      value: 14
      },
      {
      label: "B相电压15次谐波含量",
      value: 15
      },
      {
      label: "B相电压16次谐波含量",
      value: 16
      },
      {
      label: "B相电压17次谐波含量",
      value: 17
      },
      {
      label: "B相电压18次谐波含量",
      value: 18
      },
      {
      label: "B相电压19次谐波含量",
      value: 19
      },
      {
      label: "B相电压20次谐波含量",
      value: 20
      },
      {
      label: "B相电压21次谐波含量",
      value: 21
      },
      {
      label: "B相电压22次谐波含量",
      value: 22
      },
      {
      label: "B相电压23次谐波含量",
      value: 23
      },
      {
      label: "B相电压24次谐波含量",
      value: 24
      },
      {
      label: "B相电压25次谐波含量",
      value: 25
      },
      {
      label: "B相电压26次谐波含量",
      value: 26
      },
      {
      label: "B相电压27次谐波含量",
      value: 27
      },
      {
      label: "B相电压28次谐波含量",
      value: 28
      },
      {
      label: "B相电压29次谐波含量",
      value: 29
      },
      {
      label: "B相电压30次谐波含量",
      value: 30
      },
      {
      label: "B相电压31次谐波含量",
      value: 31
      },
      {
      label: "B相电压32次谐波含量",
      value: 32
    }]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 2){
    harmonicMultiple.value = [{
      label: "C相电压1次谐波含量",
      value: 1
      },
      {
      label: "C相电压2次谐波含量",
      value: 2
      },
      {
      label: "C相电压3次谐波含量",
      value: 3
      },
      {
      label: "C相电压4次谐波含量",
      value: 4
      },
      {
      label: "C相电压5次谐波含量",
      value: 5
      },
      {
      label: "C相电压6次谐波含量",
      value: 6
      },
      {
      label: "C相电压7次谐波含量",
      value: 7
      },
      {
      label: "C相电压8次谐波含量",
      value: 8
      },
      {
      label: "C相电压9次谐波含量",
      value: 9
      },
      {
      label: "C相电压10次谐波含量",
      value: 10
      },
      {
      label: "C相电压11次谐波含量",
      value: 11
      },
      {
      label: "C相电压12次谐波含量",
      value: 12
      },
      {
      label: "C相电压13次谐波含量",
      value: 13
      },
      {
      label: "C相电压14次谐波含量",
      value: 14
      },
      {
      label: "C相电压15次谐波含量",
      value: 15
      },
      {
      label: "C相电压16次谐波含量",
      value: 16
      },
      {
      label: "C相电压17次谐波含量",
      value: 17
      },
      {
      label: "C相电压18次谐波含量",
      value: 18
      },
      {
      label: "C相电压19次谐波含量",
      value: 19
      },
      {
      label: "C相电压20次谐波含量",
      value: 20
      },
      {
      label: "C相电压21次谐波含量",
      value: 21
      },
      {
      label: "C相电压22次谐波含量",
      value: 22
      },
      {
      label: "C相电压23次谐波含量",
      value: 23
      },
      {
      label: "C相电压24次谐波含量",
      value: 24
      },
      {
      label: "C相电压25次谐波含量",
      value: 25
      },
      {
      label: "C相电压26次谐波含量",
      value: 26
      },
      {
      label: "C相电压27次谐波含量",
      value: 27
      },
      {
      label: "C相电压28次谐波含量",
      value: 28
      },
      {
      label: "C相电压29次谐波含量",
      value: 29
      },
      {
      label: "C相电压30次谐波含量",
      value: 30
      },
      {
      label: "C相电压31次谐波含量",
      value: 31
      },
      {
      label: "C相电压32次谐波含量",
      value: 32
    }]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 3){
    harmonicMultiple.value = [{
      label: "A相电流1次谐波含量",
      value: 1
      },
      {
      label: "A相电流2次谐波含量",
      value: 2
      },
      {
      label: "A相电流3次谐波含量",
      value: 3
      },
      {
      label: "A相电流4次谐波含量",
      value: 4
      },
      {
      label: "A相电流5次谐波含量",
      value: 5
      },
      {
      label: "A相电流6次谐波含量",
      value: 6
      },
      {
      label: "A相电流7次谐波含量",
      value: 7
      },
      {
      label: "A相电流8次谐波含量",
      value: 8
      },
      {
      label: "A相电流9次谐波含量",
      value: 9
      },
      {
      label: "A相电流10次谐波含量",
      value: 10
      },
      {
      label: "A相电流11次谐波含量",
      value: 11
      },
      {
      label: "A相电流12次谐波含量",
      value: 12
      },
      {
      label: "A相电流13次谐波含量",
      value: 13
      },
      {
      label: "A相电流14次谐波含量",
      value: 14
      },
      {
      label: "A相电流15次谐波含量",
      value: 15
      },
      {
      label: "A相电流16次谐波含量",
      value: 16
      },
      {
      label: "A相电流17次谐波含量",
      value: 17
      },
      {
      label: "A相电流18次谐波含量",
      value: 18
      },
      {
      label: "A相电流19次谐波含量",
      value: 19
      },
      {
      label: "A相电流20次谐波含量",
      value: 20
      },
      {
      label: "A相电流21次谐波含量",
      value: 21
      },
      {
      label: "A相电流22次谐波含量",
      value: 22
      },
      {
      label: "A相电流23次谐波含量",
      value: 23
      },
      {
      label: "A相电流24次谐波含量",
      value: 24
      },
      {
      label: "A相电流25次谐波含量",
      value: 25
      },
      {
      label: "A相电流26次谐波含量",
      value: 26
      },
      {
      label: "A相电流27次谐波含量",
      value: 27
      },
      {
      label: "A相电流28次谐波含量",
      value: 28
      },
      {
      label: "A相电流29次谐波含量",
      value: 29
      },
      {
      label: "A相电流30次谐波含量",
      value: 30
      },
      {
      label: "A相电流31次谐波含量",
      value: 31
      },
      {
      label: "A相电流32次谐波含量",
      value: 32
    }]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 4){
    harmonicMultiple.value = [{
      label: "B相电流1次谐波含量",
      value: 1
      },
      {
      label: "B相电流2次谐波含量",
      value: 2
      },
      {
      label: "B相电流3次谐波含量",
      value: 3
      },
      {
      label: "B相电流4次谐波含量",
      value: 4
      },
      {
      label: "B相电流5次谐波含量",
      value: 5
      },
      {
      label: "B相电流6次谐波含量",
      value: 6
      },
      {
      label: "B相电流7次谐波含量",
      value: 7
      },
      {
      label: "B相电流8次谐波含量",
      value: 8
      },
      {
      label: "B相电流9次谐波含量",
      value: 9
      },
      {
      label: "B相电流10次谐波含量",
      value: 10
      },
      {
      label: "B相电流11次谐波含量",
      value: 11
      },
      {
      label: "B相电流12次谐波含量",
      value: 12
      },
      {
      label: "B相电流13次谐波含量",
      value: 13
      },
      {
      label: "B相电流14次谐波含量",
      value: 14
      },
      {
      label: "B相电流15次谐波含量",
      value: 15
      },
      {
      label: "B相电流16次谐波含量",
      value: 16
      },
      {
      label: "B相电流17次谐波含量",
      value: 17
      },
      {
      label: "B相电流18次谐波含量",
      value: 18
      },
      {
      label: "B相电流19次谐波含量",
      value: 19
      },
      {
      label: "B相电流20次谐波含量",
      value: 20
      },
      {
      label: "B相电流21次谐波含量",
      value: 21
      },
      {
      label: "B相电流22次谐波含量",
      value: 22
      },
      {
      label: "B相电流23次谐波含量",
      value: 23
      },
      {
      label: "B相电流24次谐波含量",
      value: 24
      },
      {
      label: "B相电流25次谐波含量",
      value: 25
      },
      {
      label: "B相电流26次谐波含量",
      value: 26
      },
      {
      label: "B相电流27次谐波含量",
      value: 27
      },
      {
      label: "B相电流28次谐波含量",
      value: 28
      },
      {
      label: "B相电流29次谐波含量",
      value: 29
      },
      {
      label: "B相电流30次谐波含量",
      value: 30
      },
      {
      label: "B相电流31次谐波含量",
      value: 31
      },
      {
      label: "B相电流32次谐波含量",
      value: 32
    }]
    queryParams.harmonicArr = [1]
  }
  if(queryParams.harmonicType == 5){
    harmonicMultiple.value = [{
      label: "C相电流1次谐波含量",
      value: 1
      },
      {
      label: "C相电流2次谐波含量",
      value: 2
      },
      {
      label: "C相电流3次谐波含量",
      value: 3
      },
      {
      label: "C相电流4次谐波含量",
      value: 4
      },
      {
      label: "C相电流5次谐波含量",
      value: 5
      },
      {
      label: "C相电流6次谐波含量",
      value: 6
      },
      {
      label: "C相电流7次谐波含量",
      value: 7
      },
      {
      label: "C相电流8次谐波含量",
      value: 8
      },
      {
      label: "C相电流9次谐波含量",
      value: 9
      },
      {
      label: "C相电流10次谐波含量",
      value: 10
      },
      {
      label: "C相电流11次谐波含量",
      value: 11
      },
      {
      label: "C相电流12次谐波含量",
      value: 12
      },
      {
      label: "C相电流13次谐波含量",
      value: 13
      },
      {
      label: "C相电流14次谐波含量",
      value: 14
      },
      {
      label: "C相电流15次谐波含量",
      value: 15
      },
      {
      label: "C相电流16次谐波含量",
      value: 16
      },
      {
      label: "C相电流17次谐波含量",
      value: 17
      },
      {
      label: "C相电流18次谐波含量",
      value: 18
      },
      {
      label: "C相电流19次谐波含量",
      value: 19
      },
      {
      label: "C相电流20次谐波含量",
      value: 20
      },
      {
      label: "C相电流21次谐波含量",
      value: 21
      },
      {
      label: "C相电流22次谐波含量",
      value: 22
      },
      {
      label: "C相电流23次谐波含量",
      value: 23
      },
      {
      label: "C相电流24次谐波含量",
      value: 24
      },
      {
      label: "C相电流25次谐波含量",
      value: 25
      },
      {
      label: "C相电流26次谐波含量",
      value: 26
      },
      {
      label: "C相电流27次谐波含量",
      value: 27
      },
      {
      label: "C相电流28次谐波含量",
      value: 28
      },
      {
      label: "C相电流29次谐波含量",
      value: 29
      },
      {
      label: "C相电流30次谐波含量",
      value: 30
      },
      {
      label: "C相电流31次谐波含量",
      value: 31
      },
      {
      label: "C相电流32次谐波含量",
      value: 32
    }]
  }
  haveSearch.value = false;
  labeldata.value = harmonicMultiple.value[0].label;
  handleQuery();
});

watch(() => [queryParams.harmonicArr], () => {
  if(seriesAndTimeArr.value.series != null && seriesAndTimeArr.value.series?.length > 0){
    const filteredSeries = seriesAndTimeArr.value.series.filter((item,index) => queryParams.harmonicArr.includes(index));
    harmonicLine.value.series = filteredSeries;
    harmonicLine.value.time = seriesAndTimeArr.value.time;
  }
});

// const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

const getNavList = async() => {
  const res = await IndexApi.getBusMenu()
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


/** 搜索按钮操作 */
const handleQuery = async () => {
  if(queryParams.oldTime){

    await getDetail();
  }
}

onBeforeMount(async () =>{
  await getNavList();
  await getDetail();
})

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
