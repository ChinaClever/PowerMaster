<template>
  <CommonMenu
    @check="handleCheck"
    @node-click="handleClick"
    :showSearch="true"
    :dataList="navList"
    navTitle="均衡配电"
  >
    <template #NavInfo>
      <div>
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/PDU.jpg" /></div>
        </div> -->
        <!-- <div class="line"></div> -->
        <div class="status">
          <div class="box">
            <div class="top"> <div class="tag"></div>{{ statusList[0].name }} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.lessFifteen }}</span
              >个</div
            >
          </div>
          <div class="box">
            <div class="top"> <div class="tag empty"></div>{{switchValue==99?'小电压':'小电流'}} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.smallCurrent }}</span
              >个</div
            >
          </div>
          <div class="box">
            <div class="top"> <div class="tag warn"></div>{{ statusList[1].name }} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.greaterFifteen }}</span
              >个</div
            >
          </div>
          <div class="box">
            <div class="top"> <div class="tag error"></div>{{ statusList[2].name }} </div>
            <div class="value"
              ><span class="number">{{ statusNumber.greaterThirty }}</span
              >个</div
            >
          </div>
        </div>
      </div>
    </template>
    <template #ActionBar>
      <div style="float: left;">
      <el-form
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
      
        <el-form-item >
          <button :class="{ 'btnallSelected': butColor === 0 , 'btnallNotSelected': butColor === 1 }" type = "button" @click="toggleAllStatus1">
            全部 
          </button>
          <template v-for="(status, index) in statusList" :key="index">
            <button v-if="butColor === 0" :class="[status.activeClass]" @click.prevent="handleSelectStatus1(status.value)" style="width:70px;">{{status.name}}</button>
            <button v-else-if="butColor === 1" :class="[onclickColor === status.value ? status.activeClass:status.cssClass]" @click.prevent="handleSelectStatus1(status.value)" style="width:70px;">{{status.name}}</button>
          </template>
          <el-button
          type="primary"
          plain
          @click="openForm('create')"
          style="float: left;" 
        >
          <Icon icon="ep:plus" class="mr-5px" /> 平衡度范围颜色
        </el-button>
        <el-form-item>
          <el-form-item label="网络地址" prop="devKey">
            <el-autocomplete
              v-model="queryParams.devKey"
              :fetch-suggestions="querySearch"
              clearable
              class="!w-150px"
              placeholder="请输入网络地址"
              @select="handleQuery"
            />
            <el-form-item :style="{ marginLeft: '10px' }">
              <el-button @click="handleQuery"
                ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button
              >
              <el-button @click="resetQuery"
                ><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button
              >
        </el-form-item>
        
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
        </el-form-item>
     
      </el-form>
    </div>
      <div style=" float: right;">
          <el-button
            @click="pageSizeArr = [24, 36, 48, 96];
            queryParams.pageSize = 24;
            queryParams.curbance=1;
            getList();
            getNavAList();
            switchValue = 2
            "
            :type="switchValue == 2 ? 'primary' : ''"
            ><Icon icon="ep:grid" style="margin-right: 4px" />电流阵列</el-button
          >
          <el-button
            @click="
              statusList.forEach((item) => (item.selected = true));
              pageSizeArr = [24, 36, 48, 96];
              queryParams.pageSize = 24;
              queryParams.curbance=0;
              getList();
              getNavAList();
              switchValue = 99;
            "
            :type="switchValue == 99 ? 'primary' : ''"
            ><Icon icon="ep:grid" style="margin-right: 4px" />电压阵列</el-button
          >
          <el-button
            @click="
              pageSizeArr = [15, 25, 30, 50, 100];
              queryParams.pageSize = 15;
              queryParams.curbance=1;
              getList();
              getNavAList();
              switchValue = 3;
            "
            :type="switchValue == 3 ? 'primary' : ''"
            ><Icon icon="ep:expand" style="margin-right: 4px" />表格模式</el-button
          >
        </div>
    </template>
    <template #Content>
     <div v-if="switchValue && list.length > 0" class="table-height">
      <el-table
        v-show="switchValue == 3"
        :data="list"
        stripe
      >
        <el-table-column label="编号" align="center" prop="tableId" width="80px" >
          <template #default="{ $index }">
            {{ $index + 1 + (queryParams.pageNo - 1) * queryParams.pageSize }}
          </template>  
        </el-table-column>
        <!-- 数据库查询 -->
        <el-table-column label="所在位置" align="center" prop="location" />
        <el-table-column
          label="网络地址"
          align="center"
          prop="devKey"
          :class-name="ip"
          width="125px"
        />
        <el-table-column label="运行状态" align="center" prop="color" width="120px">
          <template #default="scope">
             <el-tag type="info" v-if="scope.row.color == 0 && scope.row.acur !=null && scope.row.bcur ==null && scope.row.ccur ==null">单相设备</el-tag>
            <el-tag type="info" v-if="scope.row.color == 1">小电流不平衡</el-tag>
            <el-tag type="success" v-if="scope.row.color == 2">大电流不平衡</el-tag>
            <el-tag type="warning" v-if="scope.row.color == 3">大电流不平衡</el-tag>
            <el-tag type="danger" v-if="scope.row.color == 4">大电流不平衡</el-tag>
            <el-tag type="info" v-if="scope.row.color == null && scope.row.status == 5">离线</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="电流(A)" align="center">
          <el-table-column label="A相" align="center" prop="acur" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.acur != null">
                {{ scope.row.acur.toFixed(2) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相" align="center" prop="bcur" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.bcur != null">
                {{ scope.row.bcur.toFixed(2) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相" align="center" prop="ccur" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.ccur != null">
                {{ scope.row.ccur.toFixed(2) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="不平衡度(%)" align="center" prop="curUnbalance" width="110px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.curUnbalance != null">
                {{ scope.row.curUnbalance }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>
        <el-table-column label="电压(V)" align="center">
          <el-table-column label="A相" align="center" prop="avol" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.avol">
                {{ scope.row.avol.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="B相" align="center" prop="bvol" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.bvol">
                {{ scope.row.bvol.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="C相" align="center" prop="cvol" width="95px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.cvol">
                {{ scope.row.cvol.toFixed(1) }}
              </el-text>
            </template>
          </el-table-column>
          <el-table-column label="不平衡度(%)" align="center" prop="volUnbalance" width="110px">
            <template #default="scope">
              <el-text line-clamp="2" v-if="scope.row.volUnbalance != null">
                {{ scope.row.volUnbalance }}
              </el-text>
            </template>
          </el-table-column>
        </el-table-column>

        <!-- 数据库查询 -->
        <el-table-column label="操作" align="center" width="95px">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="location=scope.row.location;timeType=0;toPDUDisplayScreen(scope.row)"
              v-if="scope.row.status != null && scope.row.status != 5 && scope.row.bvol != null && scope.row.cvol != null"
              style="background-color:#409EFF;color:#fff;border:none;width:60px;height:30px;"
            >
              详情
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDelete(scope.row.devKey)"
              v-if="scope.row.status == 5"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="switchValue == 2 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null && item.location != '未绑定' ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info">
              <template v-if="item.bcur == null || item.ccur == null">
                <div v-if="item.acur != null">相电流：{{ item.acur.toFixed(2) }}A</div>
              </template>
              <template v-else>
                <div v-if="item.acur != null">A相电流：{{ item.acur.toFixed(2) }}A</div>
              </template>
              <div v-if="item.bcur != null">B相电流：{{ item.bcur.toFixed(2) }}A</div>
              <div v-if="item.ccur != null">C相电流：{{ item.ccur.toFixed(2) }}A</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div class="icon" style="margin-left: 50px">
              <div v-if="item.curUnbalance != null && item.bcur != null && item.ccur != null">
                <span style="font-size: 20px">{{ item.curUnbalance }}%</span><br />不平衡度
              </div>
            </div>
            
            
           
          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" v-if="item.color != 0">
            <el-tag type="info" v-if="item.color == 1">小电流不平衡</el-tag>
            <el-tag type="success" v-if="item.color == 2">大电流不平衡</el-tag>
            <el-tag type="warning" v-if="item.color == 3">大电流不平衡</el-tag>
            <el-tag type="danger" v-if="item.color == 4">大电流不平衡</el-tag>
          </div>
          <div class="status" v-if="item.color == 0">
            <el-tag type="info">单相设备</el-tag>
          </div>
          <div class="status" v-if="item.status == 5 && item.color == null">
            <el-tag type="info">离线</el-tag>
          </div>
          <button
            v-if="item.status != null && item.color != 5 && item.bcur != null && item.ccur != null"
            class="detail"
            @click="location=item.location;timeType=0;showDialogVol(item)"
            >详情</button
          >
        </div>
      </div>

      

      <div v-if="switchValue == 99 && list.length > 0" class="arrayContainer">
        <div class="arrayItem" v-for="item in list" :key="item.devKey">
          <div class="devKey">{{ item.location != null && item.location != '未绑定' ? item.location : item.devKey }}</div>
          <div class="content">
            <div class="info">
              <template v-if="item.bvol == null || item.cvol == null">
                <div v-if="item.avol != null">相电压：{{ item.avol.toFixed(1) }}V</div>
              </template>
              <template v-else>
                <div v-if="item.avol != null">A相电压：{{ item.avol.toFixed(1) }}V</div>
              </template>
              <div v-if="item.bvol != null">B相电压：{{ item.bvol.toFixed(1) }}V</div>
              <div v-if="item.cvol != null">C相电压：{{ item.cvol.toFixed(1) }}V</div>
              <!-- <div >网络地址：{{ item.devKey }}</div> -->
              <!-- <div>AB路占比：{{item.fzb}}</div> -->
            </div>
            <div class="icon">
              <div v-if="item.volUnbalance != null && item.bvol != null && item.cvol != null">
                <span style="font-size: 20px">{{ item.volUnbalance }}%</span><br />不平衡度 
              </div>
            </div>
            

          </div>
          <!-- <div class="room">{{item.jf}}-{{item.mc}}</div> -->
          <div class="status" v-if="item.volColor != 0">
            <el-tag type="info" v-if="item.volColor == 1">小电压不平衡</el-tag>
            <el-tag type="success" v-if="item.volColor == 2">大电压不平衡</el-tag>
            <el-tag type="warning" v-if="item.volColor == 3">大电压不平衡</el-tag>
            <el-tag type="danger" v-if="item.volColor == 4">大电压不平衡</el-tag>
          </div>
          <div class="status" v-if="item.volColor == 0">
            <el-tag type="info">单相设备</el-tag>
          </div>
          <div class="status" v-if="item.status == 5 && item.color == null">
            <el-tag type="info">离线</el-tag>
          </div>
          <button
            v-if="item.status != null && item.status != 5 && item.bvol != null && item.cvol != null"
            class="detail"
            @click="location=item.location;timeType=0;showDialogVol(item)"
            >详情</button
          >
        </div>
      </div>

      <el-dialog v-model="dialogVisibleVol" @close="handleClose" :show-close=false style="background-color: #F1F1F1;">
        <template #header>
          <el-button @click="lineidBeforeChartUnmountOne()" style="float:right;margin-left: 10px;" >关闭</el-button>
    <span style="font-size: 20px; font-weight: bold;margin-top: -10px;">均衡配电详情</span>
    <span style="margin-left: 15px;margin-top: -3px;">所在位置：{{ location?location:'未绑定' }}</span>
    <span style="margin-left: 15px;margin-top: -3px;">网络地址：{{ vollocation }}</span>
    <span style="float: right;margin-top: 3px;">时间：{{ createTimes }} - {{ endTimes }}</span>
    <span style="float:right"><el-button @click="timeType=0;showTrend()" :type="timeType==0?'primary':''">近一小时</el-button><el-button @click="timeType=1;showTrend()" :type="timeType==1?'primary':''">近一天</el-button><el-button @click="timeType=2;showTrend()" :type="timeType==2?'primary':''">近三天</el-button><el-button @click="timeType=3;showTrend()" :type="timeType==3?'primary':''">近一个月</el-button></span>
    <!-- <span style="padding-left: 530px; margin-left: 10px;">更新时间: {{ dataUpdateTime }} </span> -->
        </template>
         <!-- 自定义的主要内容 -->
        <div class="custom-content" style="margin-top:-30px">
          <div class="custom-content-container">
          <el-card class="cardChilc" shadow="hover">
            <curUnblance :max="balanceObj.imbalanceValueA||0" :customColor="colorList[balanceObj.colorIndex].color" :name="colorList[balanceObj.colorIndex].name" />
            <!-- <div class="box" :style="{ borderColor: colorList[balanfceObj.colorIndex].color }">
              <div class="value">{{ balanceObj.imbalanceValueA }}%</div>
              <div
                class="day"
                :style="{ backgroundColor: colorList[balanceObj.colorIndex].color }"
                >{{ colorList[balanceObj.colorIndex].name }}</div
              >
              <div class="status1">
                <template v-for="item in statusList" :key="item.value">
                  <div class="box1" :style="{backgroundColor: item.color}"></div>{{ item.name }}
                </template>
              </div>
              <el-tooltip
                class="box-item"
                effect="dark"
                content="三相电流不平衡： 不平衡度%=（MAX相电流-三相平均电流）/三相平均电流×100%"
                placement="right"
              >
                <div @click.prevent="" class="question">?</div>
              </el-tooltip>
            </div> -->
          </el-card>
          <el-card class="cardChilc" style="margin: 0 10px" shadow="hover" >
            <div class="IechartBar" style="position: relative;">
                <div style="display: inline-block;
                width: 70%;
                height: 100%;">
                  <Echart :options="ABarOption" :height="300" />
                </div>
                <div style="display: inline-block;
                    position: absolute;
                    width: 150px;
                    height: 100px;
                    top: 30%;">
                  <div>
                    <span class="bullet" style="background-color:#E5B849;"></span><span style="width:50px;font-size:14px;">Ia：</span><span style="font-size:16px;">{{cur_valueACopy[0]}}A</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="background-color:#C8603A;"></span><span style="width:50px;font-size:14px;">Ib：</span><span style="font-size:16px;">{{cur_valueACopy[1]}}A</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="background-color:#AD3762;"></span><span style="width:50px;font-size:14px;">Ic：</span><span style="font-size:16px;">{{cur_valueACopy[2]}}A</span>
                  </div>
                </div>
                <!--<Echart :options="ABarOption" :height="300" />-->
              </div>
          </el-card>
          <el-card class="cardChilc" shadow="hover">
            <div style="display: flex;align-items: center;justify-content: space-between">
              <div style="font-size: 18px;font-weight: bold;margin-left: 5px;">电流趋势</div>
              <div>
                <el-select v-model="typeRadioCur" placeholder="请选择" style="width: 100px" v-show="timeType!=0">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
              </div>
            </div>
            <div class="IechartBar">
              <Echart :options="ALineOption" :height="250" style="margin-top:10px" />
            </div>
          </el-card>
        </div>
        <div class="custom-content-container">
          <el-card  class="cardChilc" shadow="hover">
            <volUnblance :max="balanceObj.imbalanceValueB||0" :customColor="colorList[4].color"  :name="colorList[4].name" />
            <!-- <div class="box" :style="{borderColor: colorList[balanceObj.colorIndex].color}">
              <div class="value">{{balanceObj.imbalanceValueB}}%</div>
              <div class="day" :style="{backgroundColor: colorList[0].color}">电压不平衡</div>
              <el-tooltip
                class="box-item"
                effect="dark"
                content="三相电压不平衡度=( 最大电压−最小电压)/平均电压×100%"
                placement="right"
              >
                <div @click.prevent="" class="question">?</div>
              </el-tooltip>
            </div> -->
          </el-card>
          <el-card class="cardChilc" style="margin: 0 10px" shadow="hover">
            <div class="IechartBar" style="position: relative;">
                <div style="display: inline-block;
                width: 70%;
                height: 100%;">
                  <Echart :options="BBarOption" :height="300"/>
                </div>
                <div style="display: inline-block;
                    position: absolute;
                    width: 150px;
                    height: 100px;
                    top: 30%;">
                  <div>
                    <span class="bullet" style="background-color: #075F71;"></span><span style="width:50px;font-size:14px;">Ua：</span><span style="font-size:16px;">{{vol_valueACopy[0]}}V</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="background-color:#119CB5;"></span><span style="width:50px;font-size:14px;">Ub：</span><span style="font-size:16px;">{{vol_valueACopy[1]}}V</span>
                  </div>
                  <div style="margin-top:10px;">
                    <span class="bullet" style="background-color:#45C0C9;"></span><span style="width:50px;font-size:14px;">Uc：</span><span style="font-size:16px;">{{vol_valueACopy[2]}}V</span>
                  </div>
                </div>
              </div>
          </el-card>
          <el-card class="cardChilc" shadow="hover">
            <div style="display: flex;align-items: center;justify-content: space-between">
              <div style="font-size: 18px;font-weight: bold;margin-left: 5px;">电压趋势</div>
              <div>
                <el-select v-model="typeRadioVol" placeholder="请选择" style="width: 100px" v-show="timeType!=0">
                  <el-option label="平均" value="平均" />
                  <el-option label="最大" value="最大" />
                  <el-option label="最小" value="最小" />
                </el-select>
              </div>
            </div>
            <div class="IechartBar" >
              <Echart :options="BLineOption" :height="250" style="margin-top:10px"/>
            </div>
          </el-card>
        </div>
      </div>
      </el-dialog>
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
  <CurbalanceColorForm ref="curBalanceColorForm" @success="getList" />
</template>

<script setup lang="ts">
// import { dateFormatter } from '@/utils/formatTime'
import download from '@/utils/download'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import CurbalanceColorForm from './component/CurbalanceColorForm.vue'
import { ElTree } from 'element-plus'
import { CabinetApi } from '@/api/cabinet/info'
import { CurbalanceColorApi } from '@/api/pdu/curbalancecolor'
import { EChartsOption } from 'echarts'
import Bar from './component/Bar.vue'
import Vol from './component/Vol.vue'
import curUnblance from './component/curUnblance.vue'
import volUnblance from './component/volUnblance.vue'
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;
const detailPDUID=ref();
const openDetailFlag=ref("0")
const timeType=ref(0)
/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })

const dialogVisibleCur = ref(false) //全屏弹窗的显示隐藏
const dialogVisibleVol = ref(false) //全屏弹窗的显示隐藏
const { push } = useRouter()
const navList = ref([]) as any // 左侧导航栏树结构列表
const devKeyList = ref([])
const curBalanceColorForm = ref()
const flashListTimer = ref()
const firstTimerCreate = ref(true)
const typeRadioCur = ref("最大")
const typeRadioVol = ref("最大")
const pduBalanceTrend = ref([])
const balanceTrendTime = ref([])
const clickPduId = ref('')
const createTimes = ref('')
const endTimes = ref('')
const pageSizeArr = ref([24, 36, 48, 96])
const switchValue = ref(2)
const statusNumber = reactive({
  lessFifteen: 0,
  greaterFifteen: 0,
  greaterThirty: 0,
  smallCurrent: 0
})
const BarFlag = ref(false);

const statusList = reactive([
  {
    selected: true,
    value: 2,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    selected: true,
    value: 3,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color:'#ffc402'
  },
  {
    selected: true,
    value: 4,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color:'#fa3333'
  },
  {
    name: '小电流',
    selected: true,
    value: 1,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline',
    color:'#aaa'
  }
])

const curlocation = ref()
const vollocation = ref()
const location = ref()
const colorList = [
  {
    name: '小电流不平衡',
    color: '#aaa'
  },
  {
    name: '大电流不平衡',
    color: '#3bbb00'
  },
  {
    name: '大电流不平衡',
    color: '#ffc402'
  },
  {
    name: '大电流不平衡',
    color: '#fa3333'
  },
  {
    name: '电压不平衡',
    color: '#0B758A'
  }
]

const colorVolList = [{
  name: '小电压不平衡',
  color: '#aaa',  //灰色
},{
  name: '大电压不平衡',
  color: '#3bbb00', //绿色
},{
  name: '大电压不平衡',
  color: '#ffc402', //黄色
},{
  name: '大电压不平衡',
  color: '#fa3333', //红色
}]

const balanceObj = reactive({
  pow_apparent_percent: 0,
  pow_active_percent: 0,
  cur_valueA: [],
  vol_value: [],
  imbalanceValueA: 0,
  imbalanceValueB: 0,
  colorIndex: 0
})

const ABarOption = ref<EChartsOption>({})
const BBarOption = ref<EChartsOption>({})

const ALineOption = ref<EChartsOption>({
  
  tooltip: {
    trigger: 'axis'
  },
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '8%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} A'
    }
  },
  xAxis: {},
  series: []
})

const BLineOption = ref<EChartsOption>({
  
  tooltip: {
    trigger: 'axis'
  },
  legend: { orient: 'horizontal', right: '25'},
  dataZoom:[{type: "inside"}],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    top: '8%',
    containLabel: true
  },
  yAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value} V'
    }
  },
  xAxis:{},
  series: []
})
// 格式化耗电量列数据，保留1位小数
function formatEQ(value: number, decimalPlaces: number | undefined){
  if (!isNaN(value)) {
    return Number(value).toFixed(decimalPlaces);
  } else {
      return null; // 或者其他默认值
  }
}

const cur_valueACopy = ref([]);
const vol_valueACopy = ref([]);

const getBalanceDetail = async (item) => {
  const res = await PDUDeviceApi.balanceDetail({ devKey: item.devKey })
  const defaultCurrentValue = [0.00, 0.00, 0.00];
  const defaultVoltageValue = [0.0, 0.0, 0.0];
 
  let cur_valueA = res.cur_value ? res.cur_value : defaultCurrentValue;
  let vol_value = res.vol_value ? res.vol_value : defaultVoltageValue;

  cur_valueACopy.value = cur_valueA.map(number => number.toFixed(2));
  vol_valueACopy.value = vol_value.map(number => number.toFixed(1));
 
  // 设置电流饼形图数据
  ABarOption.value = {
    title: {
      text: '相电流',
      left: 'left'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b} : {c}'
    },
    series: [
      {
        type: 'pie',
        radius: ['30%', '80%'],
        center: ['50%', '50%'],
        roseType: 'radius',
        itemStyle: {
          borderRadius: 5
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => `${params.value}A`,
          fontSize: 14,
          fontWeight: 'bold'
        },
        data: [
          { value: cur_valueA[0]?.toFixed(2), name: 'A相电流', itemStyle: { color: '#E5B849' } },
          { value: cur_valueA[1]?.toFixed(2), name: 'B相电流', itemStyle: { color: '#C8603A' } },
          { value: cur_valueA[2]?.toFixed(2), name: 'C相电流', itemStyle: { color: '#AD3762' } },
        ]
      }
    ]
  };
 
  // 设置电压饼形图数据
  BBarOption.value = {
    title: {
      text: '相电压',
      left: 'left'
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '80%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: 'inside',
          formatter: (params) => `${params.value}V`,
          fontSize: 14,
          fontWeight: 'bold'
        },
        data: [
          { value: vol_value[0]?.toFixed(1), name: 'A相电压', itemStyle: { color: '#075F71' } },
          { value: vol_value[1]?.toFixed(1), name: 'B相电压', itemStyle: { color: '#119CB5' } },
          { value: vol_value[2]?.toFixed(1), name: 'C相电压', itemStyle: { color: '#45C0C9' } },
        ]
      }
    ]
  };

  balanceObj.imbalanceValueA = res.curUnbalance
  balanceObj.imbalanceValueB = res.volUnbalance
  balanceObj.colorIndex = (res.color || 1) - 1
}
const butColor = ref(0);

const onclickColor = ref(-1);
const toggleAllStatus1 = () => {
  butColor.value = 0;
  onclickColor.value = -1;
  queryParams.color = [0,1,2,3,4];
  handleQuery();
}
const handleSelectStatus1 = (index) => {
  butColor.value = 1;
  onclickColor.value = index;
  queryParams.color = [index];
  handleQuery();
}

// 获取pdu电流趋势
const getBalanceTrend = async (item) => {
  const res = await PDUDeviceApi.balanceTrend({
    pduId: item.id,
    timeType: timeType.value
  })

  pduBalanceTrend.value = res
  clickPduId.value = item.id
  
  createTimes.value = res[0].dateTime;
  const lastIndex = res.length - 1;
  endTimes.value = res[lastIndex].dateTime;
  if (res.length > 0) {
    const timeList = res.map((item) => item.dateTime)
    balanceTrendTime.value = timeList
    ALineOption.value.grid = {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    }
    BLineOption.value.grid = {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '8%',
      containLabel: true
    }
    if (res[0].cur && res[0].cur.length == 1) {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }}
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curValue,2))
        }
      ]
    } else if (res[0].cur && res[0].cur.length == 3) {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curValue,2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[1].curValue,2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[2].curValue,2))
        }
      ]
    }if (res[0].vol && res[0].vol.length == 1) {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[0].volValue,1)),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3) {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += 'V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[0].volValue,1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[1].volValue,1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[2].volValue,1)),
        },
      ]
    }
  }
}

// 获取pdu电流趋势
const getBalanceTrendReal = async () => {
  const res = await PDUDeviceApi.balanceTrend({
    pduId: clickPduId.value,
    timeType: 0
  })
  
  if (res.length > 0) {
    const timeList = res.map((item) => item.dateTime)
    if(typeRadioCur.value == "实时") {
      ALineOption.value.grid = {
        left: '6%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
    }
    if(typeRadioVol.value == "实时") {
      BLineOption.value.grid = {
        left: '6%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
    }
    if (res[0].cur && res[0].cur.length == 1 && typeRadioCur.value == "实时") {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curValue,2))
        }
      ]
    } else if (res[0].cur && res[0].cur.length == 3 && typeRadioCur.value == "实时") {
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[0].curValue,2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[1].curValue,2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map((item) => formatEQ(item.cur[2].curValue,2))
        }
      ]
    }if (res[0].vol && res[0].vol.length == 1 && typeRadioVol.value == "实时") {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => item.vol[0].volValue),
        },
      ]
    } else if(res[0].vol && res[0].vol.length == 3 && typeRadioVol.value == "实时") {
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;&nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += 'V';
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }},
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: timeList
      }
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[0].volValue,1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[1].volValue,1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: res.map(item => formatEQ(item.vol[2].volValue,1)),
        },
      ]
    }
  }
}

const changeType = (flag) => {
  if (pduBalanceTrend.value.length > 0) {
    let itemCurType = typeRadioCur.value == "最小" ? 'curMinValue' : (typeRadioCur.value == "最大" ? 'curMaxValue' : 'curValue')
    let itemVolType = typeRadioVol.value == "最小" ? 'volMinValue' : (typeRadioVol.value == "最大" ? 'volMaxValue' : 'volValue')
    console.log(itemCurType,itemVolType,flag)
    if(typeRadioCur.value != "实时") {
      ALineOption.value.grid = {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: balanceTrendTime.value
      }
    }
    if(typeRadioVol.value != "实时") {
      BLineOption.value.grid = {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: balanceTrendTime.value
      }
    }
   
    if (pduBalanceTrend.value[0].cur && pduBalanceTrend.value[0].cur.length == 1 && flag) {
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[0][`${itemCurType}`],2))
        }
      ]
    } else if (pduBalanceTrend.value[0].cur && pduBalanceTrend.value[0].cur.length == 3 && flag) {
      ALineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[0][`${itemCurType}`],2))
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[1][`${itemCurType}`],2))
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map((item) => formatEQ(item.cur[2][`${itemCurType}`],2))
        }
      ]
    }if (pduBalanceTrend.value[0].vol && pduBalanceTrend.value[0].vol.length == 1 && !flag) {
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[0][`${itemVolType}`],1)),
        },
      ]
    } else if(pduBalanceTrend.value[0].vol && pduBalanceTrend.value[0].vol.length == 3 && !flag) {
      BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[0][`${itemVolType}`],1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[1][`${itemVolType}`],1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: pduBalanceTrend.value.map(item => formatEQ(item.vol[2][`${itemVolType}`],1)),
        },
      ]
    }
  }
}
 
const showDialogCur = (item) => {
  dialogVisibleCur .value = true;
  curlocation.value = item.devKey;
  typeRadioCur.value = "最大"
  typeRadioVol.value = "最大"
  getBalanceDetail(item);
  getBalanceTrend(item);
}

const barMaxValues = ref({
  L1: 0,
  L2: 0,
  L3: 0
});

const curUnblance1 = ref()

const volMaxValues = ref({
  L1: 0,
  L2: 0,
  L3: 0
});
const itemValue = ref();

const showDialogVol = (item) => {

  dialogVisibleVol.value = true
  vollocation.value = item.devKey
  typeRadioCur.value = "最大"
  typeRadioVol.value = "最大"

  getBalanceDetail(item)
  getBalanceTrend(item)
  // changeChart('cur');
  // changeChart('vol');
  detailPDUID.value=item.id;
  console.log(detailPDUID.value,"detailPDUID");
  curUnblance1.value = balanceObj.imbalanceValueA
// 将 item 的属性赋值给 barMaxValues

BarFlag.value = true;
}

const loadAll = async () => {
  var data = await PDUDeviceApi.devKeyList()
  var objectArray = data.map((str) => {
    return { value: str }
  })
  return objectArray
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
    return devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
  }
}

const handleClick = (row) => {
  console.log('click', row)
}

const handleCheck = async (row) => {
  if (row.length == 0) {
    queryParams.pduKeyList = null
    getList()
    return
  }
  const pduKeys = [] as any
  var haveCabinet = false
  row.forEach((item) => {
    if (item.type == 4) {
      pduKeys.push(item.unique)
      haveCabinet = true
    }
  })
  if (!haveCabinet) {
    queryParams.pduKeyList = [-1]
  } else {
    queryParams.pduKeyList = pduKeys
  }

  getList()
}

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
    id: null,
    status: null,
    apparentPow: null,
    pow: null,
    ele: null,
    devKey: null,
    location: null,
    dataUpdateTime: '',
    pduAlarm: '',
    pf: null,
    acur: null,
    bcur: null,
    ccur: null,
    curUnbalance: null
  }
]) as any // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 24,
  devKey: undefined,
  createTime: [],
  cascadeNum: undefined,
  serverRoomData: undefined,
  status: [],
  cabinetIds: [],
  curbance: 1
}) as any
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出的加载中

/** 查询列表 */
const getList = async () => {
  getCurbalanceColor();
  loading.value = true
  try {
    const data = await PDUDeviceApi.getPDUDevicePage(queryParams)
    // var range = await CurbalanceColorApi.getCurbalanceColor()
    // if (range != null) {
    //   statusList[0].name = '<' + range.rangeOne + '%'
    //   statusList[1].name = range.rangeTwo + '%-' + range.rangeThree + '%'
    //   statusList[2].name = '>' + range.rangeFour + '%'
    // }
    total.value = data.total
    list.value = data.list
  } finally {
    loading.value = false
  }
}
const getCurbalanceColor = async () => {
  const res = await CurbalanceColorApi.getCurbalanceColor()
  if (res != null) {
    statusList[0].name = '<' + res.rangeOne + '%'
    statusList[1].name = res.rangeTwo + '%-' + res.rangeThree + '%'
    statusList[2].name = '>' + res.rangeFour + '%'
  }
}

provide('parentMethod', getCurbalanceColor);

const getNavAList = async() => {
    const resStatus =await PDUDeviceApi.getBalancedDistribution(queryParams);
    statusNumber.smallCurrent = resStatus.smallCurrent;
    statusNumber.lessFifteen = resStatus.lessFifteen;
    statusNumber.greaterFifteen = resStatus.greaterFifteen;
    statusNumber.greaterThirty = resStatus.greaterThirty;
}
// 接口获取导航列表
const getNavList = async () => {
  let arr = [] as any
  var temp = await CabinetApi.getRoomPDUList()
  arr = arr.concat(temp);
  navList.value = arr
}

import { useRouter } from 'vue-router'
import { ITEM_RENDER_EVT } from 'element-plus/es/components/virtual-list/src/defaults'
import { iteratee } from 'lodash-es'

const router = useRouter()

// const toPDUDisplayScreen = (row: { devKey: string; location: string; id: number }) => {
//   const { devKey, location, id } = row;
//   router.push({
//     path: '/pdu/pdudevicebalance',
//     query: { devKey, id: id.toString(), location }
//   });
// };

const toPDUDisplayScreen = (row) => {
  const foundItem = list.value.find(item => item.devKey === row.devKey);
  detailPDUID.value=row.id;
  showDialogVol(foundItem);
  console.log(detailPDUID.value,"detailPDUID.value");
}
// const openNewPage = (scope) => {
//   const url = 'http://' + scope.row.devKey.split('-')[0] + '/index.html';
//   window.open(url, '_blank');
// }

const handleSelectStatus = (index) => {

  statusList[index].selected = !statusList[index].selected
  const status = statusList.filter((item) => item.selected)
  const statusArr = status.map((item) => item.value)
  if (statusArr.length != statusList.length) {
    queryParams.color = statusArr
  } else {
    queryParams.color = []
  }
  handleQuery()
}

const toggleAllStatus = () => {
  const allSelected = statusList.every(item => item.selected);
  
  if (allSelected) {
    // 如果所有按钮都已选中，则全部取消选中
    statusList.forEach(item => item.selected = false);
  } else {
    // 如果至少有一个按钮未选中，则全部选中
    statusList.forEach(item => item.selected = true);
  }

  // 更新查询参数
  const status = statusList.filter(item => item.selected);
  const statusArr = status.map(item => item.value);
  if (statusArr.length != statusList.length) {
    queryParams.color = statusArr
  } else {
    queryParams.color = []
  }
  handleQuery();
}

/** 搜索按钮操作 */
const handleQuery = () => {

    if (queryParams.devKey !== ''  &&  queryParams.devKey !== undefined ){
        butColor.value = 0;
        onclickColor.value = -1;
        queryParams.color = [0,1,2,3,4];
    }
    if(switchValue.value==2||switchValue.value==3){
      queryParams.curbance=0;
    }else if(switchValue.value==99){
      queryParams.curbance=1;
    }
  queryParams.pageNo = 1
  getList();
  getNavAList();
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  statusList.forEach((item) => (item.selected = true))
  handleQuery()
}

/** 添加/修改操作 */

const openForm = (type: string) => {
  curBalanceColorForm.value.open(type)
}

/** 删除按钮操作 */
const handleDelete = async (devKey: string) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await PDUDeviceApi.deletePDUDevice({
      devKey: devKey
    })
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

watch(() => list.value ,async()=>{
  if(dialogVisibleCur.value) {
    getBalanceDetail({devKey: curlocation.value})
  }
  if(dialogVisibleVol.value) {
    getBalanceDetail({devKey: vollocation.value})
  }
})

// 监听切换类型
// watch(() => typeRadioCur.value ,(value)=>{
//   if(value == "实时") {
//     getBalanceTrendReal()
//   } else {
//    changeType(true)
//   }
// })

// 监听切换类型
// watch( ()=>typeRadioVol.value, (value)=>{
//   if(value == "实时") {
//     getBalanceTrendReal()
//   } else {
//    changeType(false)
//   }
// })

watch(openDetailFlag,async (val) => {
  if(val == "1") {
    const data = await PDUDeviceApi.getLocation({devKey: query.devKey});
    location.value = data
    showDialogVol({id: query.pduId,devKey: query.devKey})
  }
})

/** 初始化 **/
onMounted(async () => {
  devKeyList.value = await loadAll()
  getList()
  getCurbalanceColor()
  getNavList()
  getNavAList()
  // if (!firstTimerCreate.value) {
    // flashListTimer.value = setInterval(getList, 5000);
  // }

  flashListTimer.value = setInterval(() => {
        // 你的定时器逻辑
           getList();
             getNavAList();
      }, 5000);
  openDetailFlag.value = query.openDetailFlag || "0"
  // flashListTimer.value = setInterval((getList), 5000);
  // flashListTimer.value = setInterval((getNavAList), 5000);
})

onBeforeUnmount(() => {
  if (flashListTimer.value) {
    clearInterval(flashListTimer.value)
    flashListTimer.value = null
  }
})
const lineidBeforeChartUnmountOne = () => {
  dialogVisibleVol.value = false
  console.log(111)
}
onBeforeRouteLeave(() => {
  if (flashListTimer.value) {
    clearInterval(flashListTimer.value)
    flashListTimer.value = null
    firstTimerCreate.value = false
  }
})
let trend=null;
onActivated(() => {
  getList()
  getNavList()
  // if (!firstTimerCreate.value) {
    // flashListTimer.value = setInterval(getList, 5000);
  // }
  // setInterval(() => {
  //        setTimeout(() => {
  //         getList()
  //      }, 0);
  // }, 5000);

})
async function showTrend(){
  typeRadioCur.value = "最大";
  typeRadioVol.value = "最大";
  let res=await await PDUDeviceApi.balanceTrend({
    pduId: detailPDUID.value,
    timeType: timeType.value,
  });
  trend=res;
  console.log(trend);
  changeChart('cur');
  changeChart("vol");
}

watch([typeRadioCur],()=>changeChart("cur"))
watch([typeRadioVol],()=>changeChart("vol"))


function changeChart(curVol){
  if(trend!=null&&trend.length!=0){
    if(curVol=='cur'){
      ALineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: trend.map(item => item.dateTime)
      }
      ALineOption.value.grid = {
        left: '3%',
        right: '4%',
        bottom: '3%',
        top: '8%',
        containLabel: true
      }
      ALineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += ' A';
          }
          if(timeType.value!=0){
            if(typeRadioCur.value=='平均'){
              result+="&nbsp;&nbsp记录时间:"+trend?.[param.dataIndex]?.dateTime;
            }else if(typeRadioCur.value=='最大'){
              result+= "&nbsp;&nbsp发生时间:"+trend?.[param.dataIndex]?.cur[param.seriesIndex].curMaxTime;
            }else if(typeRadioCur.value=='最小'){
              result+= "&nbsp;&nbsp发生时间:"+trend?.[param.dataIndex]?.cur[param.seriesIndex].curMinTime;
            }
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }}
      if(trend[0].cur.length==1){
        ALineOption.value.series = [
          {
            name: 'A',
            type: 'line',
            symbol: 'none',
            data: trend.map((item) => timeType.value==0?formatEQ(item.cur[0].curValue,2):typeRadioCur.value=='平均'?formatEQ(item.cur[0].curValue,2):typeRadioCur.value=='最大'?formatEQ(item.cur[0].curMaxValue,2):formatEQ(item.cur[0].curMinValue,2))
          }
        ]
      }else if(trend[0].cur.length==3){
        ALineOption.value.series = [
          {
            name: 'A',
            type: 'line',
            symbol: 'none',
            data: trend.map((item) => timeType.value==0?formatEQ(item.cur[0].curValue,2):typeRadioCur.value=='平均'?formatEQ(item.cur[0].curValue,2):typeRadioCur.value=='最大'?formatEQ(item.cur[0].curMaxValue,2):formatEQ(item.cur[0].curMinValue,2))
          },
          {
            name: 'B',
            type: 'line',
            symbol: 'none',
            data: trend.map((item) => timeType.value==0?formatEQ(item.cur[1].curValue,2):typeRadioCur.value=='平均'?formatEQ(item.cur[1].curValue,2):typeRadioCur.value=='最大'?formatEQ(item.cur[1].curMaxValue,2):formatEQ(item.cur[1].curMinValue,2))
          },
          {
            name: 'C',
            type: 'line',
            symbol: 'none',
            data: trend.map((item) => timeType.value==0?formatEQ(item.cur[2].curValue,2):typeRadioCur.value=='平均'?formatEQ(item.cur[2].curValue,2):typeRadioCur.value=='最大'?formatEQ(item.cur[2].curMaxValue,2):formatEQ(item.cur[2].curMinValue,2))
          }
        ]
      }
    }else if(curVol=='vol'){
      BLineOption.value.xAxis = {
        type: 'category',
        boundaryGap: false,
        data: trend.map(item => item.dateTime)
      }
      BLineOption.value.tooltip= { trigger: 'axis', formatter: function (params) {
        let result = params[0].name + '<br>';
        params.forEach(param => {
          result += param.marker + param.seriesName + ': &nbsp;&nbsp;' + param.value;
          if (param.seriesName === 'A' || param.seriesName === 'B' || param.seriesName === 'C') {
            result += 'V';
          }
          if(timeType.value!=0){
            if(typeRadioVol.value=='平均'){
              result+="&nbsp;&nbsp记录时间:"+trend?.[param.dataIndex]?.dateTime;
            }else if(typeRadioVol.value=='最大'){
              result+= "&nbsp;&nbsp发生时间:"+trend?.[param.dataIndex]?.vol[param.seriesIndex].volMaxTime;
            }else if(typeRadioVol.value=='最小'){
              result+= "&nbsp;&nbsp发生时间:"+trend?.[param.dataIndex]?.vol[param.seriesIndex].volMinTime;
            }
          }
          result += '<br>';
        });
        return result.trimEnd(); // 去除末尾多余的换行符
      }}
      if(trend[0].vol.length==1){
        BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: trend.map(item => timeType.value==0?formatEQ(item.vol[0].volValue,1):typeRadioVol.value=='平均'?formatEQ(item.vol[0].volValue,1):typeRadioVol.value=='最大'?formatEQ(item.vol[0].volMaxValue,1):formatEQ(item.vol[0].volMinValue,1)),
        },
      ]
      }else if(trend[0].vol.length==3){ 
        BLineOption.value.series = [
        {
          name: 'A',
          type: 'line',
          symbol: 'none',
          data: trend.map(item => timeType.value==0?formatEQ(item.vol[0].volValue,1):typeRadioVol.value=='平均'?formatEQ(item.vol[0].volValue,1):typeRadioVol.value=='最大'?formatEQ(item.vol[0].volMaxValue,1):formatEQ(item.vol[0].volMinValue,1)),
        },
        {
          name: 'B',
          type: 'line',
          symbol: 'none',
          data: trend.map(item => timeType.value==0?formatEQ(item.vol[1].volValue,1):typeRadioVol.value=='平均'?formatEQ(item.vol[1].volValue,1):typeRadioVol.value=='最大'?formatEQ(item.vol[1].volMaxValue,1):formatEQ(item.vol[1].volMinValue,1)),
        },
        {
          name: 'C',
          type: 'line',
          symbol: 'none',
          data: trend.map(item => timeType.value==0?formatEQ(item.vol[2].volValue,1):typeRadioVol.value=='平均'?formatEQ(item.vol[2].volValue,1):typeRadioVol.value=='最大'?formatEQ(item.vol[2].volMaxValue,1):formatEQ(item.vol[2].volMinValue,1)),
        },
      ]
      }
    }
  }else{
    if(curVol=='cur'){
      ALineOption.value.series=[]
      ALineOption.value.xAxis={}
    }else if(curVol=='vol'){ 
      BLineOption.value.series=[]
      BLineOption.value.xAxis={}
    }
  }
}
</script>

<style lang="scss" scoped>
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


.bthnn {
  width: 58px;
  height: 35px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px
  
}

.btn_offline,
.btn_normal,
.btn_warn,
.btn_error {
  width: 58px;
  height: 32px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
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
      box-shadow: 0 3px 4px 1px rgba(0, 0, 0, 0.12);
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
    margin-top: 20px;
    .box {
      height: 70px;
      width: 40%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: left;
      flex-direction: column;
      margin-left: auto;
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
      background-color: #f56c6c;
    }
  }
}

@media screen and (min-width: 2048px) {
  .table-height{
    height: 78vh;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 20%;
      height: 140px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #fff;
      border: 1px solid #e4e7ed;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
          margin-top: 10px;
        }
      }
      .devKey {
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

@media screen and (max-width: 2048px) and (min-width: 1600px) {
  .table-height{
    height: 720px;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 24%;
      height: 140px;
      font-size: 13px;
      margin: 4px;
      border-radius: 7px;
      box-sizing: border-box;
      background-color: #fff;
      border: 1px solid #e4e7ed;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
          margin-top: 10px;
        }
      }
      .devKey {
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

@media screen and (max-width: 1600px) {
  .table-height{
    height: 600px;
    margin-top:-10px;
    overflow: hidden;
    overflow-y: auto;
  }
  .arrayContainer {
    display: flex;
    flex-wrap: wrap;
    .arrayItem {
      width: 33%;
      height: 140px;
      font-size: 13px;
      box-sizing: border-box;
      background-color: #fff;
      border: 1px solid #e4e7ed;
      padding-top: 40px;
      position: relative;
      .content {
        display: flex;
        align-items: center;
        .icon {
          width: 60px;
          height: 30px;
          margin: 0 25px;
          text-align: center;
        }
        .info {
          margin-left: 15px;
          margin-top: 10px;
        }
      }
      .devKey {
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


::v-deep .el-table .el-table__header th {
  background-color: #f7f7f7;
  color: #909399;
  height: 30px;
}

:deep(.el-dialog) {
  width: 80%;
  margin-top: 70px;
}

.custom-content{
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.question {
  width: 12px;
  height: 12px;
  position: absolute;
  top: 2px;
  right: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid;
  border-radius: 50%;
  font-size: 12px;
  cursor: pointer;
}

.cardChilc {
  flex: 1;
  margin: 0 10px;
  box-sizing: border-box;
  .box {
    position: relative;
    height: 121px;
    width: 200px;
    display: flex;
    flex-direction: column;
    align-items: center;
    border: 1px solid #00289e;
    margin: 72px auto;
    
    .value {
      font-size: 30px;
      padding: 20px 0;
    }
    .day {
      width: 100%;
      font-size: 16px;
      text-align: center;
      color: #fff;
      background-color: #00289e;
      padding: 10px 0;
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
.status1 {
  font-size: 14px;
  display: flex;
  align-items: center;
  .box1 {
    font-display: center;
    width: 10px;
    height: 10px;
    border-radius: 2px;
    margin-left: 10px;
    margin-right: 5px;
  }
}
.custom-content-container{
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin: 10px;
}

.label-container {
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中 */
  color:#000;
}

:deep(.el-card){
  --el-card-padding:5px;
}
.bullet{
  display: inline-block;
  height: 10px;
  width: 10px;
  border-radius: 5px;
}
</style>
