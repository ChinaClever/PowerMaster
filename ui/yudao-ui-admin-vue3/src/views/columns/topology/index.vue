<template>
  <!-- <div style="height:100%;min-height: calc(100vh - 120px);display: flex; flex-direction: column;"> -->
    <ContentWrap>
    <div class="btn-main" style="justify-content:space-between">
      <div style="display: flex;flex-direction: column; width: 500px;">
        <div class="btns" v-if="cabinetList && cabinetList.length">
          <span v-show="chosenBtn == 8">前门：</span>
          <template v-for="(status, index) in statusList[chosenBtn]" :key="index">
            <el-button :size="isFromHome ? 'small' : 'default'" :style="`background-color: ${status.color};color: white;`">{{status.name}}</el-button>
          </template> 
        </div>
        <div v-show="chosenBtn == 8" class="btns" v-if="cabinetList && cabinetList.length" style="margin-top: 10px;">
          <span v-show="chosenBtn == 8">后门：</span>
          <template v-for="(status, index) in statusList[chosenBtn]" :key="index">
            <el-button :size="isFromHome ? 'small' : 'default'" :style="`background-color: ${status.hotColor};color: white;`">{{status.hotName}}</el-button>
          </template> 
        </div>
      </div>
      <div>
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div style="display:flex;">
        <div v-if="!isFromHome" style="margin-left: 10px">
          <el-button v-if="!editEnable" @click="handleEditClick" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
          <el-button v-if="editEnable" @click="handleCancel" type="primary" plain>取消</el-button>
          <el-button v-if="editEnable" @click="handleConfig" type="primary" plain>配置</el-button>
          <el-button v-if="editEnable" @click="handleSubmit" type="primary" plain>保存</el-button>
        </div>
        <slot name="btn"></slot>
      </div>
    </div>
  </ContentWrap>
  <ContentWrap>
    <div ref="topologyContainer" class="topologyContainer" :style="`position: relative;z-index: 1;`" @click="showAllConnect">
      <div style="margin-top:-25px"></div>
      <div style="height:20px;"></div>
      <div class="Container" :style="{alignItems: machineColInfo.pduBar && machineColInfo.barA ? 'unset' : 'center', minHeight: isFromHome ? 'unset' : '600px'}">
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus"  @click.right="handleBarRightClick($event)">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction ? 0 : 1}" @dblclick="handleInitialDblick($event, 'A')">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" :btns="btns" />
            <!-- <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" /> -->
          </div>
          <div class="startBus" v-if="!machineColInfo.barB.direction" @dblclick="handleInitialDblick($event, 'B')">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barB" :btns="btns" />
          </div>
          <div class="maskPoint1"></div>
          <div class="maskPoint2"></div>
          <div class="menu" v-if="operateMenuBox.show && editEnable && operateMenuBox.type == 'bar'" :style="{left: `${operateMenuBox.left}`, top: `${operateMenuBox.top}`}">
            <div class="menu_item" @click="handleBarOperate('edit')">编辑</div>
            <div class="menu_item" @click="handleBarOperate('delete')">删除</div>
          </div>
        </div>
        <div class="main">
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer" @click.right="handlePluginRightClick($event, 'A')">
            <div class="bridge"></div>
            <div class="busList1">
              <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0 || bus.outletNum > 0" class="plugin-box" :id="`box-${index}`" @dblclick="handlePluginDblick($event, 'A')" @click.stop="showPluginConnect('A',bus.boxIndex)">
                  <PluginBox :chosenBtn="chosenBtn" :pluginData="bus" :btns="btns" />
                  <div class="pointContainer">
                    <div v-for="pointIndex in bus.outletNum" :key="pointIndex" class="point" :id="`plugin-${bus.boxIndex}_A-${pointIndex}`"></div>
                  </div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box" :id="`box-${index}`">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div v-if="chosenBtn == 8 && bus.temData" class="Tbox">
                    <div v-for="(tmp, count) in bus.temData" :key="count" class="T">
                      <div>T(L{{count}})</div>
                      <div>{{tmp}}°C</div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <div class="menu" v-if="operateMenuBox.show && editEnable && operateMenuBox.type == 'A'" :style="{left: `${operateMenuBox.left}`, top: `${operateMenuBox.top}`}">
              <div class="menu_item" @click="handleBoxOperate('edit', 'A')">编辑</div>
              <div class="menu_item" @click="handleBoxOperate('delete', 'A')">删除</div>
            </div>
          </div>
          <div v-if="machineColInfo.pduBar && machineColInfo.barB" class="busListContainer" @click.right="handlePluginRightClick($event, 'B')" style="margin-bottom: 80px">
            <div class="bridge"></div>
            <div class="busList2">
              <template v-for="(bus, index) in machineColInfo.barB.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0 || bus.outletNum > 0" class="plugin-box" :id="`box-${index}`" @dblclick="handlePluginDblick($event, 'B')" @click.stop="showPluginConnect('B',bus.boxIndex)">
                  <PluginBox :chosenBtn="chosenBtn" :pluginData="bus" :btns="btns" />
                  <div class="pointContainer">
                    <div v-for="pointIndex in bus.outletNum" :key="pointIndex" class="point" :id="`plugin-${bus.boxIndex}_B-${pointIndex}`"></div>
                  </div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box" :id="`box-${index}`">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div v-if="chosenBtn == 8 && bus.temData" class="Tbox">
                    <div v-for="(tmp, count) in bus.temData" :key="count" class="T">
                      <div>T(L{{count}})</div>
                      <div>{{tmp}}°C</div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <div class="menu" v-if="operateMenuBox.show && editEnable && operateMenuBox.type == 'B'" :style="{left: `${operateMenuBox.left}`, top: `${operateMenuBox.top}`}">
              <div class="menu_item" @click="handleBoxOperate('edit', 'B')">编辑</div>
              <div class="menu_item" @click="handleBoxOperate('delete', 'B')">删除</div>
            </div>
          </div>
          <div class="cabinetContainer" @click.right="handleCabRightClick">
            <div class="cabinetList" v-if="cabinetList && cabinetList.length">
              <template v-for="(cabinet,index) in cabinetList" :key="index">
                <div class="cabinetBox">
                  <div class="point">
                    <div v-if="cabinet.cabinetName" :id="'cab-A-' + index" class="leftPoint"></div>
                    <div v-if="cabinet.cabinetName" :id="'cab-B-' + index" class="rightPoint"></div>
                  </div>
                  <div class="cabinet">
                    <template v-if="cabinet.cabinetName">
                      <el-tooltip effect="light">
                        <template #content>
                          名称：{{cabinet.cabinetName}} <br/>
                          负载率：{{cabinet.loadRate ? cabinet.loadRate.toFixed(1) : '0.0'}}%<br/>
                          昨日用能：{{cabinet.yesterdayEq || 0}}kW·h<br/>
                          总有功功率：{{cabinet.powActive ? cabinet.powActive.toFixed(3) : '0.000'}}kW<br/>
                          总视在功率：{{cabinet.powApparent ? cabinet.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                          总无功功率：{{cabinet.powReactive ? cabinet.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                          总功率因素：{{cabinet.powerFactor ? cabinet.powerFactor.toFixed(2) : '0.00'}}<br/>
                          A路供电占比：{{cabinet.outletA}} <br/>
                          B路供电占比：{{cabinet.outletB}} <br/>
                          <!-- 最高温度：{{cabinet.tem}}°C<br/> -->
                          已用空间：{{cabinet.usedSpace}}U<br/>
                          未用空间：{{cabinet.freeSpace}}U<br/>
                        </template>
                        <div class="inner_fill" @click.stop="showCabinetConnect(index)" @dblclick="handleJump(cabinet)" :id="'cabinet-' + index" :style="{backgroundColor: cabinet.id ? 'rgba(180, 180, 180, 0.2)' : 'rgba(230, 240, 234)'}"></div>
                      </el-tooltip>
                      <template v-if="cabinet.id">
                        <div v-if="chosenBtn == 0" class="fill_box">
                          <Echart :options="cabinet.echartsOptionLoad" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:center;font-size:12px;">
                            <div>负载率</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 1" class="fill_box">
                          <Echart v-if="cabinet.lineCurA || cabinet.lineCurB" :options="cabinet.echartsOptionA1" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:space-around;font-size:12px;">
                            <div>Ia</div>
                            <div v-if="(cabinet.lineCurA && cabinet.lineCurA.length == 3) || (cabinet.lineCurB && cabinet.lineCurB.length == 3)">Ib</div>
                            <div v-if="(cabinet.lineCurA && cabinet.lineCurA.length == 3) || (cabinet.lineCurB && cabinet.lineCurB.length == 3)">Ic</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 2" class="fill_box">
                          <Echart v-if="cabinet.lineVolA || cabinet.lineVolB" :options="cabinet.echartsOptionV1" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:space-around;font-size:12px;">
                            <div>Ua</div>
                            <div v-if="(cabinet.lineVolA && cabinet.lineVolA.length == 3) || (cabinet.lineVolB && cabinet.lineVolB.length == 3)">Ub</div>
                            <div v-if="(cabinet.lineVolA && cabinet.lineVolA.length == 3) || (cabinet.lineVolB && cabinet.lineVolB.length == 3)">Uc</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 3" class="fill_box">
                          <Echart :options="cabinet.echartsOptionFactor" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:center;font-size:12px;">
                            <div>功率因数</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 4" class="fill_box">
                          <Echart :options="cabinet.echartsOptionApparent" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:center;font-size:12px;">
                            <div>功率</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 7" class="fill_box">
                          <Echart :options="cabinet.echartsOptionBalance" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:space-around;font-size:12px;">
                            <div>A路</div>
                            <div>B路</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 8" class="fill_box">
                          <Echart :options="cabinet.echartsOptionTemp" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:space-around;font-size:12px;">
                            <div>前门</div>
                            <div>后门</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 9" class="fill_box">
                          <Echart :options="cabinet.echartsOptionCapacity" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:center;font-size:12px;">
                            <div>容量</div>
                          </div>
                        </div>
                        <div v-if="chosenBtn == 10" class="fill_box">
                          <Echart :options="cabinet.echartsOptionEq" height="100%" />
                          <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:center;font-size:12px;">
                            <div>昨日用能</div>
                          </div>
                        </div>
                      </template>
                    </template>
                    <div v-else class="inner_empty" :id="'cabinet-' + index"></div>
                  </div>
                  <div style="width: 100%;display: flex;justify-content: center;">
                    <div class="ti_xing">
                    </div>
                  </div>
                  <div class="status">{{cabinet.cabinetName || ''}}</div>
                </div>
              </template>
              <div class="operateBox">
                <div v-show="editEnable" class="operateIcon" @click.prevent="addMachine">+</div>
                <div v-show="editEnable" class="operateIcon" @click.prevent="deleteMachine">-</div>
              </div>
            </div>
            <div class="menu" v-if="operateMenu.show && editEnable" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
              <div class="menu_item" v-if="operateMenu.add" @click="handleOperate('add')">新增</div>
              <div class="menu_item" v-if="!operateMenu.add" @click="handleJump(false)">查看</div>
              <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('edit')">编辑</div>
              <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('delete')">删除</div>
            </div>
          </div>
        </div>
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction}" @dblclick="handleInitialDblick($event, 'A')">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" :btns="btns" />
          </div>
          <div class="startBus" v-if="machineColInfo.barB.direction" @dblclick="handleInitialDblick($event, 'B')">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barB" :btns="btns" />
          </div>
        </div>
      </div>
      <el-form
        v-if="!isFromHome"
        class="mt-15px topForm"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="" prop="jf" >
          机房：<el-select :size="isFromHome ? 'small' : ''" v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-200px">
            <el-option v-for="item in roomList" :key="item.roomId" :label="item.roomName" :value="item.roomId" />
          </el-select>
        </el-form-item >
        <span class="line"></span>
        <el-form-item label="" prop="jg">
          柜列：<el-select :size="isFromHome ? 'small' : ''" v-model="queryParams.cabinetColumnId" placeholder="请选择" class="!w-200px">
            <el-option v-for="item in machineList" :key="item.id" :label="item.aisleName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div v-if="!editEnable && machineColInfo.barA" class="mask" @click.right.prevent="console.log('---')"></div>
    </div>
  </ContentWrap>
  <!-- </div> -->
  <!-- 添加或修改用户对话框 -->
  <PluginForm ref="columnForm" @success="handleFormPlugin" />
  <CabForm ref="cabinetForm" @success="handleFormCabinet" />
  <BoxForm ref="columnBoxForm" @success="handleFormBox" />

  <el-dialog v-model="detailVis" style="width: 80%;height: 80%;margin-top: 100px;">
    <div class="custom-row" style="display: flex; align-items: center;">
      <!-- 位置标签 -->
      <div class="location-tag el-col">
        <span style="margin-right:10px;font-size:18px;font-weight:bold;">功率因素详情</span>
        <span>所在位置：{{ machineColInfo.roomName?machineColInfo.roomName:'未绑定' }}</span>
        <span> 机柜名称：{{ queryParamsPF.cabinetName?queryParamsPF.cabinetName:'未绑定' }}</span>
      </div>

      <!-- 日期选择器 -->
      <div class="date-picker-col el-col">
        <el-date-picker
          v-model="queryParamsPF.startTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetime"
          :picker-options="pickerOptions"
          placeholder="选择日期时间"
        />
        <el-button @click="subtractOneDay(); handleDayPick()" type="primary" style="margin-left:10px;">&lt; 前一日</el-button>
        <el-button @click="addtractOneDay(); handleDayPick()" type="primary">&gt; 后一日</el-button>
      </div>

      <!-- 图表/数据切换按钮组 -->
      <div class="chart-data-buttons el-col" style="margin-right: 50px;">
        <div class="button-group">
          <el-button @click="switchChartOrTable = 0" :type="switchChartOrTable === 0 ? 'primary' : ''">图表</el-button>
          <el-button @click="switchChartOrTable = 1" :type="switchChartOrTable === 1 ? 'primary' : ''">数据</el-button>
          <el-button type="success" plain @click="handleExportXLS" :loading="exportLoading">
            <i class="el-icon-download"></i> 导出
          </el-button>
        </div>
      </div>
    </div>
    <br/>
    <PFDetail v-if="switchChartOrTable == 0"  width="75vw" height="70vh"  :list="pfESList"   />
    <div v-else-if="switchChartOrTable == 1" style="width: 100%;height:70vh;overflow-y:auto;">
      <el-table style="height:70vh;" :data="pfTableList" :show-overflow-tooltip="true" >
      <el-table-column label="时间" align="center" prop="create_time" />
      <el-table-column label="总功率因素" align="center" prop="factor_total_avg_value" />
      <el-table-column label="A路功率因素" align="center" prop="factor_a_avg_value" />
      <el-table-column label="B路功率因素" align="center" prop="factor_b_avg_value" />
    </el-table>
    </div>
  </el-dialog>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance, JsPlumbDefaults } from '@jsplumb/browser-ui'
import { MachineColumnApi } from '@/api/cabinet/column'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/cabinet/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import download from '@/utils/download'
import PluginForm from './component/PluginForm.vue'
import PFDetail from './component/PFDetail.vue'
import CabForm from './component/CabForm.vue'
import InitialBox from './component/InitialBox.vue'
import PluginBox from './component/PluginBox.vue'
import BoxForm from './component/BoxForm.vue'
import { EChartsOption } from 'echarts'
import { useRoute } from 'vue-router'

const route = useRoute();
const query = route.query;

const message = useMessage()
const { push } = useRouter()
let instance: BrowserJsPlumbInstance | null = null
const roomList = ref<any>([]) // 机房列表
const machineList = ref<any>([]) // 机柜列列表
const tempList:any = ref([])
const detailVis = ref(false)
const switchChartOrTable = ref(0)
const queryParams = reactive({
  cabinetColumnId: Number(query.id),
  cabinetroomId: Number(query.roomId),
  roomDownValId:Number(query.roomValId)
})
const statusList = reactive([
  [{
    name: '负载率<30%',
    selected: true,
    value: 1,
    color: '#3bbb00'
  },
  {
    name: '30%≤负载率<60%',
    selected: true,
    value: 2,
    color: '#05ebfc'
  },
  {
    name: '60%≤负载率<90%',
    selected: true,
    value: 3,
    color: '#ffc402'
  },
  {
    name: '负载率>90%',
    selected: true,
    value: 4,
    color: '#fa3333'
  }],
  [{
    name: 'Ia',
    selected: true,
    value: 1,
    color: '#E5B849'
  },
  {
    name: 'Ib',
    selected: true,
    value: 2,
    color: '#C8603A'
  },
  {
    name: 'Ic',
    selected: true,
    value: 3,
    color: '#AD3762'
  }],
  [{
    name: 'Ua',
    selected: true,
    value: 1,
    color: '#075F71'
  },
  {
    name: 'Ub',
    selected: true,
    value: 2,
    color: '#119CB5'
  },
  {
    name: 'Uc',
    selected: true,
    value: 3,
    color: '#45C0C9'
  }],
  [{
    name: '功率因素<25%',
    selected: true,
    value: 1,
    color: '#FF6E76'
  },
  {
    name: '25%≤功率因素<50%',
    selected: true,
    value: 2,
    color: '#FDDD60'
  },
  {
    name: '50%≤功率因素<75%',
    selected: true,
    value: 3,
    color: '#58D9F9'
  },
  {
    name: '功率因素>75%',
    selected: true,
    value: 4,
    color: '#7CFFB2'
  }],
  [{
    name: '有功功率',
    selected: true,
    value: 1,
    color: '#91cc75'
  },
  {
    name: '无功功率',
    selected: true,
    value: 2,
    color: '#800080'
  }],[],[],
  [{
    name: '视在功率',
    selected: true,
    value: 1,
    color: '#5470c6'
  }],
  [],
  [{
    name: '剩余量>50%',
    selected: true,
    value: 1,
    color: '#3bbb00'
  },
  {
    name: '30%≤剩余量<50%',
    selected: true,
    value: 2,
    color: '#ffc402'
  },
  {
    name: '剩余量<30%',
    selected: true,
    value: 3,
    color: '#fa3333'
  }]
])
const btns = [
  {
    value: 0,
    name: '负载率',
    unitName: '负载率',
  },
  {
    value: 1,
    name: '电流',
    unitName: '电流',
  },
  {
    value: 2,
    name: '电压',
    unitName: '电压',
  },
  {
    value: 3,
    name: '功率因素',
    unitName: '功率因素',
  },
  {
    value: 4,
    name: '功率',
    unitName: '功率(kW)',
  },
  // {
  //   value: 5,
  //   name: '无功功率'
  // },
  // {
  //   value: 6,
  //   name: '视在功率'
  // },
  {
    value: 7,
    name: '供电平衡',
    unitName: '视在功率(KVA)',
  },
  {
    value: 8,
    name: '温度',
    unitName: '温度(°C)',
  },
  {
    value: 9,
    name: '容量',
    unitName: '插接箱',
  },
  {
    value: 10,
    name: '用能',
    unitName: '昨日用能(kW·h)',
  },
]

const flashListTimer = ref();
const flashListTimerCopy = ref();

const echartsOptionCab = ref<EChartsOption>({})
let intervalTimer = null as any
const topologyContainer = ref()
const chosenBtn = ref(0)
const scaleValue = ref(1)
const ContainerHeight = ref(100)
const editEnable = ref(false)
const barChangeType = ref()
const columnForm = ref()
const columnBoxForm = ref()
const cabinetForm = ref()
const machineColInfo = reactive<any>({})
const cabinetList = ref<any>([])
const busListA = ref<any>([])
const busListB = ref<any>([])
const roomDownVal =ref();
const operateMenu = ref({  // 操作菜单
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
})
const operateMenuBox = ref({  // 操作插接箱/连接器菜单
  left: '0px',
  top: '0px',
  show: false,
  curIndex: 0,
  type: '',
})
const {containerInfo, isFromHome} = defineProps({
  containerInfo: {
    type: Object,
  },
  isFromHome: {
    type: Boolean,
    default: false,
  },
})
const emit = defineEmits(['backData', 'idChange', 'getpdubar','sendList']) // 定义 success 事件，用于操作成功后的回调

const saveInit = async (cabIndex) => {
  const res = await CabinetApi.saveCabinetInfo({
    ...cabinetList.value[cabIndex],
  })
  console.log("res",res,{
    ...cabinetList.value[cabIndex],
  })
}
// 连接初始化准备
const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: topologyContainer.value
  })
  // 监听连接
  instance.bind('beforeDrop', ({connection}) => {
    console.log('connection', connection)
    const sourceId = connection.source.id
    const targetId = connection.target.id
    const cabId = sourceId.includes('cab') ? sourceId : targetId // 机柜id
    const cabIndex = cabId.split('-')[2]  // 机柜下标
    const cabRoad = cabId.split('-')[1] // 机柜AB路
    const pluginId = sourceId.includes('plugin') ? sourceId : targetId // 插接箱id
    const pluginName = pluginId.split('_')[0] // 插接箱名字
    const pluginOutLet = pluginId.split('-')[2] // 插接箱输出位
    // 机柜与机柜 插接箱与插接箱之间不能相互连接   机柜A路只能连母线A路 机柜B路只能连母线B路
    if ((cabId.split('-')[0] ==  pluginId.split('-')[0]) ||  (cabRoad == 'A' && pluginId.includes('B')) || (cabRoad == 'B' && pluginId.includes('A'))) {
      return false
    }
    cabinetList.value[cabIndex][`boxOutletId${cabRoad}`] = +pluginOutLet
    cabinetList.value[cabIndex][`boxIndex${cabRoad}`] = Number(pluginName.split('-')[1])
    let boxList_Index = machineColInfo[`bar${cabRoad}`].boxList.findIndex(box => box.boxIndex == Number(pluginName.split('-')[1]))

    cabinetList.value[cabIndex][`casId${cabRoad}`] = machineColInfo[`bar${cabRoad}`].boxList[boxList_Index].casAddr
    cabinetList.value[cabIndex][`barId${cabRoad}`] = machineColInfo[`bar${cabRoad}`].barId
    cabinetList.value[cabIndex][`busIp${cabRoad}`] = machineColInfo[`bar${cabRoad}`].devIp
    console.log('监听连接', connection, cabId, pluginId, cabinetList.value[cabIndex],)
    // 处理手动连接的样式
    if (cabRoad == 'A') {
      connection.paintStyle = {
        strokeWidth: 1,
        stroke: "#ccc",
        dashstyle: "5 5"
      }
    } else if (cabRoad == 'B') {
      connection.paintStyle = {
        strokeWidth: 1,
        stroke: "#bb0000",
        dashstyle: "5 5"
      }
    }
    return true
  })
  // 监听连接断开
  instance.bind('beforeDetach', function(connection) {
    console.log('监听连接断开', connection, connection.sourceId, connection.source,connection.target)
    if (connection.suspendedElement) { // 用户手动断开连接
      console.log("c1111111111111")
      const targetId = connection.target.id
      const sourceId = connection.source.id
      const cabRoad = connection.source.id.includes('cab') ? sourceId.split('-')[1] : targetId.split(/[-_]/)[2]
      const index = connection.source.id.includes('cab') ? sourceId.split('-')[2] : targetId.split(/[-_]/)[1]
      cabinetList.value[index][`boxOutletId${cabRoad}`] = null
      cabinetList.value[index][`boxIndex${cabRoad}`] = null
      cabinetList.value[index][`casId${cabRoad}`] = null
      cabinetList.value[index][`barId${cabRoad}`] = null
      cabinetList.value[index][`busIp${cabRoad}`] = null
      console.log(cabinetList.value[index])
      const cabElement = document.getElementById('cab-' + cabRoad + '-' + index) as Element
      instance?.addEndpoint(cabElement, {
        source: true,
        target: true,
        endpoint: 'Dot'
      })
    }
    // 如果返回 false，则连接断开操作会被取消
    return true
  })
}
// 创建瞄点并连接
const toCreatConnect = (onlyDelete = false) => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    console.log('toCreatConnect', cabinetList.value, machineColInfo)
    nextTick(() => {
      machineColInfo.barA.boxList.forEach(item => {
        if (item.type) return
        for(let i=1; i <= item.outletNum; i++) {
          const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_A-' + i) as Element
          // console.log('boxElementA', boxElementA, item.boxIndex)
          // 删除瞄点
          instance?.removeAllEndpoints(boxElementA)
          // 添加瞄点
          instance?.addEndpoint(boxElementA, {
            source: true,
            target: true,
            endpoint: 'Dot'
          })
          // 更新瞄点
          instance?.revalidate(boxElementA)
        }
      })
      machineColInfo.barB.boxList.forEach(item => {
        if (item.type) return
        for(let i=1; i <= item.outletNum; i++) {
          const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_B-' + i) as Element
          console.log('boxElementA', boxElementA, item.boxIndex)
          // 删除瞄点
          instance?.removeAllEndpoints(boxElementA)
          // 添加瞄点
          instance?.addEndpoint(boxElementA, {
            source: true,
            target: true,
            endpoint: 'Dot'
          })
          // 更新瞄点
          instance?.revalidate(boxElementA)
        }
      })
      cabinetList.value.forEach((item, index) => {
        if (!item.cabinetName && machineColInfo.pduBar) return
        addCabinetAnchor(index, item, onlyDelete)
      })
      return Promise.resolve()
    })
  }
}
const showAllConnect = () => {
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName && machineColInfo.pduBar) return
    addCabinetAnchor(index, item)
  })
}
const showCabinetConnect = (i) => {
  instance?.deleteEveryConnection()
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName && machineColInfo.pduBar) return
    if(index == i) {
      addCabinetAnchor(index, item)
    } else {
      const cabElementA = document.getElementById('cab-A-' + index) as Element
      const cabElementB = document.getElementById('cab-B-' + index) as Element
      console.log('cabElementB', cabElementB, cabElementA, item)
      instance?.removeAllEndpoints(cabElementA)
      instance?.removeAllEndpoints(cabElementB)
      // 添加瞄点
      instance?.addEndpoint(cabElementA, {
        source: true,
        target: true,
        endpoint: 'Dot'
      })
      instance?.addEndpoint(cabElementB, {
        source: true,
        target: true,
        endpoint: 'Dot'
      })
      instance?.revalidate(cabElementA)
      instance?.revalidate(cabElementB)
    }
  })
}
const showPluginConnect = (road,boxIndex) => {
  instance?.deleteEveryConnection()
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName && machineColInfo.pduBar) return
    const cabElementA = document.getElementById('cab-A-' + index) as Element
    const cabElementB = document.getElementById('cab-B-' + index) as Element
    console.log('cabElementB', cabElementB, cabElementA, item)
    instance?.removeAllEndpoints(cabElementA)
    instance?.removeAllEndpoints(cabElementB)
    // 添加瞄点
    instance?.addEndpoint(cabElementA, {
      source: true,
      target: true,
      endpoint: 'Dot'
    })
    instance?.addEndpoint(cabElementB, {
      source: true,
      target: true,
      endpoint: 'Dot'
    })
    if (item[`boxIndex${road}`] !== '' && item[`boxIndex${road}`] == boxIndex) { // 有连接
      const source = document.getElementById('cab-' + road + '-' + index) as Element
      const target = document.getElementById(`plugin-${item[`boxIndex${road}`]}_${road}-${item[`boxOutletId${road}`]}`)  as Element
      console.log('target', source, target, item.boxIndexA, item.boxOutletIdA, machineColInfo)
      instance?.connect({
        source,
        target,
        paintStyle: {
          strokeWidth: 1,
          stroke: road == 'A' ? '#ccc' : '#bb0000',
          dashstyle: '5 5'
        }
      })
    }
    instance?.revalidate(cabElementA)
    instance?.revalidate(cabElementB)
  })
}
// 创建瞄点并连接
const controlEndpointShow = (show) => {
  setTimeout((() => {
    const points = document.querySelectorAll('.jtk-endpoint')
    console.log('points', points)
    points.forEach(element  => {
      // console.log('element', element)
      const a = element as any
      a.style.opacity = show ? 1 : 0.1
    })
  }), 100)
}
// 更新插接箱瞄点
const updatePluginAnchor = () => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    machineColInfo.barA.boxList.forEach(item => {
      if (item.type) return
      for(let i=1; i <= item.outletNum; i++) {
        const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_A-' + i) as Element
        // 更新瞄点
        instance?.revalidate(boxElementA)
      }
    })
    machineColInfo.barB.boxList.forEach(item => {
      if (item.type) return
      for(let i=1; i <= item.outletNum; i++) {
        const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_B-' + i) as Element
        // 更新瞄点
        instance?.revalidate(boxElementA)
      }
    })
  }
}
// 给某个机柜加瞄点 并进行连接
const addCabinetAnchor = (index, data = {} as any, onlyDelete = false) => {
  const cabElementA = document.getElementById('cab-A-' + index) as Element
  const cabElementB = document.getElementById('cab-B-' + index) as Element
  console.log('cabElementB', cabElementB, cabElementA, data)
  instance?.removeAllEndpoints(cabElementA)
  instance?.removeAllEndpoints(cabElementB)
  if (onlyDelete) return
  // 添加瞄点
  if (data.boxIndexA === '' || !data.boxOutletIdA) instance?.addEndpoint(cabElementA, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (data.boxIndexA === '' || !data.boxOutletIdB) instance?.addEndpoint(cabElementB, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if ((data.boxIndexA !== '' && data.boxIndexA > -1) && data.boxOutletIdA) { // A路有连接
    const source = document.getElementById('cab-A-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxIndexA}_A-${data.boxOutletIdA}`)  as Element
    console.log('target', source, target, data.boxIndexA, data.boxOutletIdA, machineColInfo)
    instance?.connect({
      source,
      target,
      paintStyle: {
        strokeWidth: 1,
        stroke: '#ccc',
        dashstyle: '5 5'
      }
    })
  }
  if ((data.boxIndexA !== '' && data.boxIndexA > -1) && data.boxOutletIdB) { // B路有连接
    const source = document.getElementById('cab-B-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxIndexB}_B-${data.boxOutletIdB}`)  as Element
    console.log('target---', source, target)
    instance?.connect({
      source,
      target,
      paintStyle: {
        strokeWidth: 1,
        stroke: '#bb0000',
        dashstyle: '5 5'
      }
    })
  }
  instance?.revalidate(cabElementA)
  instance?.revalidate(cabElementB)
}
// 更新机柜和插接箱的连接 
const updateCabinetConnect = () => {
  nextTick(() => {
    cabinetList.value.forEach((item, index) => {
      if (!item.cabinetName || !machineColInfo.barA) return
      console.log('更新机柜的连接')
      const cabElementA = document.getElementById('cab-A-' + index) as Element
      const cabElementB = document.getElementById('cab-B-' + index) as Element
      instance?.revalidate(cabElementA)
      instance?.revalidate(cabElementB)
    })
    updatePluginAnchor()
  })
}

const queryParamsPF = reactive({
  pageNo: 1,
  pageSize: 24,
  cabinetIds : [],
})as any
const pfESList = ref({}) as any
const pfTableList = ref([]) as any
const exportLoading = ref(false) // 导出的加载中

const openPFDetail = async (row) =>{
  queryParamsPF.cabinetName = row.cabinetName
  queryParamsPF.cabinetIds = [row.id];
  queryParamsPF.startTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  console.log('row',row);
  await getDetail();
  detailVis.value = true;
}

/** 查询列表 */
const getDetail = async () => {
  const data = await IndexApi.getCabinetPFDetail(queryParamsPF);
  console.log('数据',data);
  pfESList.value = data?.chart;
  pfESList.value?.powerFactorAvgValueTotal?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.powerFactorAvgValueA?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.powerFactorAvgValueB?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });

  pfTableList.value = data?.table;
  pfTableList.value?.forEach((obj) => {
    obj.factor_total_avg_value = obj?.factor_total_avg_value?.toFixed(2);
    obj.factor_a_avg_value = obj?.factor_a_avg_value?.toFixed(2);
    obj.factor_b_avg_value = obj?.factor_b_avg_value?.toFixed(2);
  });
}

const subtractOneDay = () => {
  var date = new Date(queryParamsPF.startTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  queryParamsPF.startTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParamsPF.startTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  queryParamsPF.startTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const handleDayPick = async () => {

  if(queryParamsPF?.startTime ){
    await getDetail();
    
  } 
}

const handleExportXLS = async ()=>{
  try {
    // 导出的二次确认
    await message.exportConfirm();
    // 发起导出
    queryParamsPF.pageNo = 1;
    exportLoading.value = true;
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await IndexApi.getCabinetPFDetailExcel(queryParamsPF, axiosConfig);
    console.log("data",data);
    await download.excel(data, '功率因数详细.xlsx');
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error);
  } finally {
    exportLoading.value = false;
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

// 处理机柜右击事件
const handleCabRightClick = (e) => {
  console.log('处理右击事件', e.target.className)
  e.preventDefault()
  if (!editEnable.value || (!e.target.className.includes('inner_empty') && !e.target.className.includes('inner_fill'))) return
  const container = e.currentTarget
  const currentIndex = e.target.id.split('-')[1]
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  const isAdd = !cabinetList.value[currentIndex].cabinetName
  operateMenu.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    add: isAdd,
    curIndex: currentIndex
  }
  console.log('operateMenu',e.target.className, operateMenu.value, cabinetList.value[currentIndex])
}
// 处理始端箱右击事件
const handleBarRightClick = (e) => {
  e.preventDefault()
  if (!editEnable.value) return
  const targetId = e.target.id || e.target.parentNode.id
  console.log('处理始端箱右击事件', e.target.className, e.target.parentNode, targetId, e.currentTarget)
  const container = e.currentTarget
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  operateMenuBox.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    curIndex: 0,
    type: 'bar'
  }
}
// 处理插接箱右击事件
const handlePluginRightClick = (e, type) => {
  e.preventDefault()
  if (!editEnable.value) return
  const targetId = e.target.id || e.target.parentNode.id
  console.log('处理插接箱右击事件',type, e.target.className, e.target.parentNode, targetId, e.currentTarget)
  if (!targetId || !targetId.includes('box')) return
  const container = e.currentTarget
  const currentIndex = targetId.split('-')[1]
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  operateMenuBox.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    curIndex: currentIndex,
    type
  }
}
const handleInitialDblick = (e, road) => {
  console.log('machineColInfo', machineColInfo)
  if(chosenBtn.value == 0) {
    push({path: '/bus/busmonitor/powerLoadDetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 1) {
    push({path: '/bus/busmonitor/buspowerdetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 2) {
    push({path: '/bus/busmonitor/buspowerdetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 3) {
    push({path: '/bus/busmonitor/buspowerfactor', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 4) {
    push({path: '/bus/busmonitor/buspowerdetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 7) {
    push({path: '/bus/busmonitor/busbalancedetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 8) {
    push({path: '/bus/busmonitor/bustem', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 10) {
    push({path: '/bus/busmonitor/busenergydetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  }
}
const handlePluginDblick = (e, road) => {
  const targetId = e.target.id || e.target.parentNode.id
  const index = targetId.split('-')[1]
  console.log('targetId', targetId, machineColInfo, index)
  if(chosenBtn.value == 0) {
    push({path: '/bus/boxmonitor/boxpowerLoadDetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 1) {
    push({path: '/bus/boxmonitor/boxpowerDetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 2) {
    push({path: '/bus/boxmonitor/boxpowerDetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 3) {
    push({path: '/bus/boxmonitor/boxpowerfactor', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 4) {
    push({path: '/bus/boxmonitor/boxpowerDetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 7) {
    push({path: '/bus/boxmonitor/boxbalancedetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 8) {
    push({path: '/bus/boxmonitor/boxtem', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 10) {
    push({path: '/bus/boxmonitor/boxenergydetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  }
}
// 处理始端箱菜单点击事件
const handleBarOperate = async(type) => {
  const index = operateMenuBox.value.curIndex
  console.log('handleBarOperate', type, index, machineColInfo)
  if (type == 'edit') {
    handleConfig()
  }else{
    barChangeType.value = 'delete'
    machineColInfo.barA = null
    machineColInfo.barB = null
    cabinetList.value.forEach(item => {
      item.busIpA = null
      item.busIpB = null
      item.barIdA = null
      item.barIdB = null
      item.boxIndexA = null
      item.boxIndexB = null
      item.boxOutletIdA = null
      item.boxOutletIdB = null
      item.cabinetBoxes = null
    })
    console.log("machineColInfo.cabinetList",machineColInfo.cabinetList)
    instance?.deleteEveryConnection()
    // if(res != "1"){
    //    message.error('柜列母线删除失败!')
    // }
    operateMenuBox.value.show = false;
    // message.success('柜列母线删除成功!');
    // getMachineColInfo();
  }
}
// 处理插接箱/连接器菜单点击事件
const handleBoxOperate = async(type, road) => {
  const index = operateMenuBox.value.curIndex
  console.log('handleBoxOperate', type, index, machineColInfo)
  if (type == 'edit') {
    const data = machineColInfo[`bar${road}`].boxList[index]
    columnBoxForm.value.open(data)
  }else{
    const data = machineColInfo[`bar${road}`].boxList[index]
    const res = await MachineColumnApi.getDeleteAisleSingleBox({id: data.id})
    if(res != "1"){
       message.error('柜列插接箱/连接器单个删除失败!')
    }
    operateMenuBox.value.show = false;
    cabinetList.value.forEach(async item => {
      if(item[`boxIndex${road}`] == data.boxIndex) {
        item[`busIp${road}`] = null
        item[`barId${road}`] = null
        item[`boxIndex${road}`] = null
        item[`casId${road}`] = null
        item[`boxOutletId${road}`] = null
        item.cabinetBoxes[`boxIndex${road}`] = null
        item.cabinetBoxes[`boxKey${road}`] = null
        item.cabinetBoxes[`outletId${road}`] = null
        const resItem = await CabinetApi.saveCabinetInfo({
          ...item,
          pduBox: true
        })
        console.log("resCab",{...item},resItem)
      }
    })
    message.success('柜列插接箱/连接器单个删除成功!');
    setTimeout(() => { getMachineColInfo() } ,1000)
  }
}
// 跳转机柜
const handleJump = (data) => {
 let target = {} as any
  if (data) {
    target = data
  } else {
    target = cabinetList.value[operateMenu.value.curIndex]
  }
  console.log('target', target)
  if (!target.id) {
    message.error(`该机柜暂未保存绑定，无法跳转`)
    return
  }
  if(chosenBtn.value == 0) {
    push({path: '/cabinet/cab/cabinetPowerLoadDetail', query: {cabinet: target.id,roomId: target.roomId,roomName: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 1) {
    push({path: '/cabinet/cab/detail', state: {id: target.id,roomId: target.roomId,type: 'hour',location: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 2) {
    push({path: '/cabinet/cab/detail', state: {id: target.id,roomId: target.roomId,type: 'hour',location: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 3) {
    //弹窗
    openPFDetail(target)
  } else if(chosenBtn.value == 4) {
    push({path: '/cabinet/cab/detail', state: {id: target.id,roomId: target.roomId,type: 'hour',location: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 7) {
    push({path: '/cabinet/cab/balance', query: {openDetailFlag: 1,id: target.id}})
  } else if(chosenBtn.value == 8) {
    push({path: '/cabinet/cab/cabinetenvdetail', query: { id: target.id }})
  } else if(chosenBtn.value == 9) {
    push({path: '/cabinet/cab/screen', state: { id: target.id,roomId: target.roomId }})
  } else if(chosenBtn.value == 10) {
    push({path: '/cabinet/cab/energyDetail', query: { cabinetId: target.id,cabinetroomId: target.roomId,roomName: machineColInfo.roomName,cabinetName: target.cabinetName }})
  }
}
// 处理菜单点击事件
const handleOperate = (type) => {
  operateMenu.value.show = false
  const index = operateMenu.value.curIndex
  if (type == 'add' || type == 'edit') {
    if(barChangeType.value == 'edit') {
      ElMessage({
        message: '请先保存母线',
        type: 'warning',
      })
      return
    }
    let info = {
      roomId: machineColInfo.roomId,
      roomName: machineColInfo.roomName,
      aisleId: machineColInfo.id,
      barA: false,
      index: Number(index)+1
    } as any
    if (machineColInfo.barA) {
      info = {
        roomId: machineColInfo.roomId,
        roomName: machineColInfo.roomName,
        aisleId: machineColInfo.id,
        index: Number(index)+1,
        barA: machineColInfo.barA.boxList,
        barB: machineColInfo.barB.boxList,
        barIdA: machineColInfo.barA.barId,
        busIpA: machineColInfo.barA.devIp,
        barIdB: machineColInfo.barB.barId,
        busIpB: machineColInfo.barB.devIp,
        boxAmount: machineColInfo.barA.boxList.length
      }
    }
    const data = type == 'add' ? null : cabinetList.value[index]
    console.log("data",data)
    cabinetForm.value.open(type, data, info)
  } else if (type == 'delete') {
    ElMessageBox.confirm('确认删除吗？', '提示', {
      confirmButtonText: '确 认',
      cancelButtonText: '取 消',
      type: 'warning'
    }).then(async () => {
      const cabItem = cabinetList.value[index]
      if(cabItem.cabinetBoxes) {
        console.log("aaaaaa---cabItem",cabItem)
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 2
        })
      } else if(cabItem.cabinetPdus) {
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 1
        })
      } else if(cabItem.rackIndices) {
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 3
        })
      } else {
        cabinetList.value.splice(index, 1, {})
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 4
        })
      }
      getMachineColInfo()
    })
  }
  console.log('handleOperate', machineColInfo)
}
// 处理编辑事件
const handleEditClick = () => {
  editEnable.value = true
  controlEndpointShow(true)
}
// 处理编辑取消事件
const handleCancel = () => {
  ElMessageBox.confirm('取消会使已修改的操作丢失，确认取消？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    instance?.deleteEveryConnection()
    editEnable.value = false
    barChangeType.value = ''
    getMachineColInfo()
  })
}
// 处理配置点击事件  打开配置弹窗
const handleConfig = () => {
  console.log('handleConfig', machineColInfo)
  let data = null as any | null
  if(machineColInfo.barA) {
    console.log('machineColInfo.barA', machineColInfo.barA)
    const boxList = machineColInfo.barA.boxList
    data = {
      barIdA: machineColInfo.barA.barId,
      ipA: machineColInfo.barA.devIp,
      barIdB: machineColInfo.barB.barId,
      directionA: machineColInfo.barA.direction,
      directionB: machineColInfo.barB.direction,
      casAddrA: machineColInfo.barA.casAddr,
      casAddrB: machineColInfo.barB.casAddr,
      ipB: machineColInfo.barB.devIp,
      cjxAmount: boxList.length,
    }
    console.log(data)
  }
  console.log('???',data)
  columnForm.value.open(data)
}
// 处理保存事件
const handleSubmit = () => {
  console.log('handleSubmit')
  saveMachineBus()
}
// 插接箱弹窗确认后的处理
const handleFormPlugin = (data) => {
  console.log('handleFormSave', data)
  barChangeType.value = 'edit'
  let arrA = [] as any
  let arrB = [] as any
  const machineColInfoLength = (machineColInfo.barA && machineColInfo.barA.boxList) ? machineColInfo.barA.boxList.length : 0
  for(let i=0; i < data.cjxAmount; i++) {
    if(i < machineColInfoLength) {
      arrA.push(machineColInfo.barA.boxList[i])
      arrB.push(machineColInfo.barB.boxList[i])
    } else {
      arrA.push({
        casAddr: i+2,
        outletNum: 3,
        type: 0,
        boxName: 'box-' + i,
        boxIndex: i,
      })
      arrB.push({
        casAddr: i+2,
        outletNum: 3,
        type: 0,
        boxName: 'box-' + i,
        boxIndex: i,
      })
    }
  }
  const boxA = {
    barId: data.barIdA,
    casAddr: data.casAddrA,
    devIp: data.ipA,
    path: 'A',
    direction: data.directionA,
    boxList: arrA
  }
  const boxB = {
    barId: data.barIdB,
    casAddr: data.casAddrB,
    devIp: data.ipB,
    path: 'B',
    direction: data.directionB,
    boxList: arrB
  }
  machineColInfo.barA = boxA
  machineColInfo.barB = boxB
  machineColInfo.pduBar = 1
  console.log('machineColInfo', machineColInfo)
  toCreatConnect() // 因为添加插接箱需要添加瞄点 所以要创建
}
// 机柜弹窗确认后的处理
const handleFormCabinet = (data) => {
  console.log('handleFormCabinet', data, operateMenu.value)
  data.index = +operateMenu.value.curIndex + 1
  cabinetList.value.splice(operateMenu.value.curIndex, 1, data)
  if (machineColInfo.barA && machineColInfo.pduBar) nextTick(() => {
    addCabinetAnchor(operateMenu.value.curIndex, data)
  })
  getMachineColInfo()
}
// 插接箱弹窗确认后处理
const handleFormBox = (data) => {
  const arr = [] as any
  const bar = `bar${operateMenuBox.value.type}`
  const index = operateMenuBox.value.curIndex
  const length = data.outletNum
  console.log(data)
  machineColInfo[bar].boxList.splice(index, 1, data)
  console.log(machineColInfo[bar].boxList)
  console.log(index)
  cabinetList.value.forEach(item => {
      console.log(item[`boxIndex${operateMenuBox.value.type}`],index)
    if(item[`boxIndex${operateMenuBox.value.type}`] == data.boxIndex && item[`boxOutletId${operateMenuBox.value.type}`] > data.outletNum) {
      item[`busIp${operateMenuBox.value.type}`] = null
      item[`barId${operateMenuBox.value.type}`] = null
      item[`boxIndex${operateMenuBox.value.type}`] = null
      item[`casId${operateMenuBox.value.type}`] = null
      item[`boxOutletId${operateMenuBox.value.type}`] = null
      item.cabinetBoxes[`boxIndex${operateMenuBox.value.type}`] = null
      item.cabinetBoxes[`boxKey${operateMenuBox.value.type}`] = null
      item.cabinetBoxes[`outletId${operateMenuBox.value.type}`] = null
      console.log(item)
    } else if(item[`boxIndex${operateMenuBox.value.type}`] == data.boxIndex && data.casAddr != '' && item[`casId${operateMenuBox.value.type}`] != Number(data.casAddr) ) {
      console.log(data)
      item[`casId${operateMenuBox.value.type}`] = Number(data.casAddr)
      console.log(item)
    }
  })
  instance?.deleteEveryConnection()
  showAllConnect()
  for (let i=1; i <= length; i++) {
    const boxElement = document.getElementById('plugin-' + index + '_' + operateMenuBox.value.type + '-' + i) as Element
    if (!boxElement) {
      arr.push(i)
    }
  }
  nextTick(() => {
    for (let i=1; i <= length; i++) {
      const boxElement = document.getElementById('plugin-' + index + '_' + operateMenuBox.value.type + '-' + i) as Element
      if (arr.includes(i)) {
        console.log('------i', i, boxElement)
        instance?.addEndpoint(boxElement, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
      }
      // 更新瞄点
      instance?.revalidate(boxElement)
    }
  })
}
// 接口获取柜列状态数据详情
const getDataDetail = async() => {
  const res = await MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})
  console.log('接口获取柜列状态数据详情', res)
  handleDataDetail(res)
}
// 处理柜列状态数据详情
const handleDataDetail = (res) => {
  console.log('machineColInfo', machineColInfo)
  let show = cabinetList.value
  console.log(show)
  res.cabinetList && res.cabinetList.forEach(cab => {
    cabinetList.value.forEach((item, index) => {
      if (item.id == cab.id) {
        const common = {
          type: 'bar',
          emphasis: {
            focus: 'series'
          },
          label: {
            show: true,
            formatter: '{c}', // 显示数据值
          },
        }
        let seriesA = [
          {
            name: 'L1',
            data: [(cab.lineCurA ? cab.lineCurA[0] : 0), (cab.lineCurB ? cab.lineCurB[0] : 0)],
            ...common
          }
        ] as any
        if (cab.lineCurA && cab.lineCurB && cab.lineCurA.length == 3 && cab.lineCurB.length == 3) {
          seriesA = [...seriesA, {
            name: 'L2',
            data: [(cab.lineCurA ? cab.lineCurA[1] : 0), (cab.lineCurB ? cab.lineCurB[1] : 0)],
            ...common
          }, {
            name: 'L3',
            data: [(cab.lineCurA ? cab.lineCurA[2] : 0), (cab.lineCurB ? cab.lineCurB[2] : 0)],
            ...common
          }]
        }
        cabinetList.value[index] = {
          ...cab,
          ...item,
          echartsOptionLoad: { // 负载
            xAxis: {
              type: 'category',
              data: ['负载率'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-30',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true,
            },
            yAxis: {
              type: 'value',
              max: 100,
              show: false,
            },
            series: [
              {
                name: 'load',
                data: [cab.loadRate ? cab.loadRate.toFixed(0) : 0],
                type: 'bar',
                barWidth: '100%',
                showBackground: true,
                itemStyle: {
                  color: function (params) {
                      const value = params.value;
                      if (value < 30) {
                          return '#3bbb00'; 
                      } else if (value < 60) {
                          return '#05ebfc';
                      } else if (value < 90) {
                          return '#ffc402';
                      } else {
                        return '#fa3333'
                      }
                  }
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}', // 显示数据值
                },
              }
            ]
          },
          echartsOptionA1: { // 电流
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ((cab.lineCurA && cab.lineCurA.length == 3) || (cab.lineCurB && cab.lineCurB.length == 3)) ? ['Ia', 'Ib','Ic'] : ['Ia'],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              },
              axisLine: {
                show: false
              }
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [{
              name: 'A',
              itemStyle: {
                color: function (params) {
                    const index = params.dataIndex;
                    if (index == 0) {
                        return '#E5B849';
                    } else if (index == 1) {
                        return '#C8603A';
                    } else if (index == 2) {
                        return '#AD3762';
                    } else {
                      return '#fa3333'
                    }
                }
              },
              data: cab.lineCurA && cab.lineCurA.length == 3 ? [(cab.lineCurA ? cab.lineCurA[0] : 0),(cab.lineCurA ? cab.lineCurA[1] : 0),(cab.lineCurA ? cab.lineCurA[2] : 0)] : [(cab.lineCurA ? cab.lineCurA[0] : 0)],
              ...common
            },{
              name: 'B',
              itemStyle: {
                color: function (params) {
                    const index = params.dataIndex;
                    if (index == 0) {
                        return '#E5B849';
                    } else if (index == 1) {
                        return '#C8603A';
                    } else if (index == 2) {
                        return '#AD3762';
                    } else {
                      return '#fa3333'
                    }
                }
              },
              data: cab.lineCurB && cab.lineCurB.length == 3 ? [(cab.lineCurB ? cab.lineCurB[0] : 0),(cab.lineCurB ? cab.lineCurB[1] : 0),(cab.lineCurB ? cab.lineCurB[2] : 0)] : [(cab.lineCurB ? cab.lineCurB[0] : 0)],
              ...common
            }]
          },
          echartsOptionV1: { // 电压
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ((cab.lineVolA && cab.lineVolA.length == 3) || (cab.lineVolB && cab.lineVolB.length == 3)) ? ['Ua', 'Ub','Uc'] : ['Ua'],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              },
              axisLine: {
                show: false
              }
            },
            grid: {
              left: '-28',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [{
              name: 'A',
              itemStyle: {
                color: function (params) {
                    const index = params.dataIndex;
                    if (index == 0) {
                        return '#075F71';
                    } else if (index == 1) {
                        return '#119CB5';
                    } else if (index == 2) {
                        return '#45C0C9';
                    } else {
                      return '#fa3333'
                    }
                }
              },
              data: cab.lineVolA && cab.lineVolA.length == 3 ? [(cab.lineVolA ? cab.lineVolA[0] : 0),(cab.lineVolA ? cab.lineVolA[1] : 0),(cab.lineVolA ? cab.lineVolA[2] : 0)] : [(cab.lineVolA ? cab.lineVolA[0] : 0)],
              ...common
            },{
              name: 'B',
              itemStyle: {
                color: function (params) {
                    const index = params.dataIndex;
                    if (index == 0) {
                        return '#075F71';
                    } else if (index == 1) {
                        return '#119CB5';
                    } else if (index == 2) {
                        return '#45C0C9';
                    } else {
                      return '#fa3333'
                    }
                }
              },
              data: cab.lineVolB && cab.lineVolB.length == 3 ? [(cab.lineVolB ? cab.lineVolB[0] : 0),(cab.lineVolB ? cab.lineVolB[1] : 0),(cab.lineVolB ? cab.lineVolB[2] : 0)] : [(cab.lineVolB ? cab.lineVolB[0] : 0)],
              ...common
            }]
          },
          echartsOptionFactor: { // 功率因素
            xAxis: {
              type: 'category',
              data: ['功率因素'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-26',
              right: '0',
              bottom: '4.6%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: 1, 
              show: false,
            },
            series: [
              {
                name: 'load',
                data: [cab.powerFactor],
                type: 'bar',
                barWidth: '100%',
                showBackground: true,
                itemStyle: {
                  color: function (params) {
                      const value = params.value;
                      if (value < 0.25) {
                          return '#FF6E76';
                      } else if (value < 0.5) {
                          return '#FDDD60';
                      } else if (value < 0.75) {
                          return '#58D9F9';
                      } else {
                        return '#7CFFB2'
                      }
                  }
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}', // 显示数据值
                },
              }
            ]
          },
          echartsOptionApparent: { //功率
            xAxis: {
                type: 'category',
                data: ['功率'],
                axisTick: {
                  show: false
                },
                axisLine: {
                  show: false
                },
                axisLabel: {
                    show: false // 隐藏 ECharts 自带的标签
                }
            },
            yAxis: {
                type: 'value',
                show: false
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            series: [
                {
                    name: 'Small Red Bar',
                    type: 'bar',
                    data: [cab.powActive.toFixed(3)], // 左侧小柱形图的数据
                    itemStyle: {
                        color: '#91cc75'
                    },
                    stack: 'Ad',
                    barWidth: '100%',
                    label: {
                        show: true, // 显示数值
                        position: 'inside', // 数值显示在柱形图内部
                        color: 'white', // 数值颜色
                        fontSize: 12 // 数值字体大小
                    }
                },
                {
                    name: 'Small Blue Bar',
                    type: 'bar',
                    data: [cab.powReactive.toFixed(3)], // 右侧小柱形图的数据
                    itemStyle: {
                        color: '#800080'
                    },
                    stack: 'Ad',
                    barWidth: '100%',
                    label: {
                        show: true, // 显示数值
                        position: 'inside', // 数值显示在柱形图内部
                        color: 'white', // 数值颜色
                        fontSize: 12 // 数值字体大小
                    }
                }
            ]
          },
          echartsOptionBalance: {
            xAxis: {
                type: 'category',
                data: ['A路', 'B路'],
                axisTick: {
                  show: false
                },
                axisLine: {
                  show: false
                },
                axisLabel: {
                    show: false // 隐藏 ECharts 自带的标签
                }
            },
            yAxis: {
                type: 'value',
                show: false
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            series: [
                {
                    name: '视在功率',
                    type: 'bar',
                    data: [cab.powApparentA, cab.powApparentB], // 大柱形图的数据
                    itemStyle: {
                        color: '#5470c6'
                    },
                    z: 1, // 确保大柱形图在底层
                    label: {
                        show: true, // 显示数值
                        position: 'top', // 数值显示在柱形图顶部
                        fontSize: 12 // 数值字体大小
                    }
                },
            ]
          },
          echartsOptionTemp: { // 温度
            xAxis: {
              type: 'category',
              data: ['冷通道温度','热通道温度'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [
              {
                data: [cab.temData ? cab.temData : 0,cab.temDataHot ? cab.temDataHot : 0],
                type: 'bar',
                itemStyle: {
                  color: function (params) {
                      const index = params.dataIndex;
                      if (index == 0) {
                        for (const item of tempList.value) {
                            if (params.value < item.max) {
                                return item.color;
                            }
                        }
                        return 'red';
                      } else if (index == 1) {
                        for (const item of tempList.value) {
                            if (params.value < item.hotMax) {
                                return item.hotColor;
                            }
                        }
                        return 'red';
                      } else {
                        return '#fa3333'
                      }
                  }
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}°C', // 显示数据值
                },
              }
            ]
          },
          echartsOptionCapacity: { // 容量
            xAxis: {
              type: 'category',
              data: [`总容量:${item.cabinetHeight}`],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: item.cabinetHeight, 
              show: false,
            },
            series: [
              {
                data: [item.usedSpace],
                type: 'bar',
                barWidth: '100%',
                showBackground: true,
                itemStyle: {
                  color: function (params) {
                      const value = params.value;
                      if (value/item.cabinetHeight < 0.5) {
                          return '#3bbb00';
                      } else if (value/item.cabinetHeight < 0.7) {
                          return '#ffc402';
                      } else {
                        return '#fa3333'
                      }
                  }
                },
                backgroundStyle: {
                  color: 'rgba(180, 180, 180, 0.2)'
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '已用容量:\n{c}', // 显示数据值
                },
              }
            ]
          },
          echartsOptionEq: { // 用能
            xAxis: {
              type: 'category',
              data: ['昨日用能'],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-40',
              right: '0',
              bottom: '4.9%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: item.eleLimitDay, 
              show: false,
            },
            series: [
              {
                name: 'load',
                data: [item.yesterdayEq ? item.yesterdayEq.toFixed(2) : 0],
                type: 'bar',
                barWidth: '100%',
                showBackground: true,
                backgroundStyle: {
                  color: 'rgba(180, 180, 180, 0.2)'
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}', // 显示数据值
                },
              }
            ]
          }
        }
        console.log('ssssssss----------', cab, cab.id)
        return
      }
    })
  })
  emit('backData', [...cabinetList.value], scaleValue.value)
  echartsOptionCab.value = {
    title: {
      text: '机柜列实时统计'
    },
    grid: {
      left: '45',
      right: '80',
      bottom: '0',
      top: '50',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: cabinetList.value.map(item => item.cabinetName || '')
    },
    legend:{
      top: '0',
      right: '80',
      selectedMode: 'single'
    },
    yAxis: {
      type: 'value',
      // show: false
    },
    series: [
      {
        name:'有功功率',
        data: cabinetList.value.map(item => item.powActive == undefined ? null : item.powActive),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
      {
        name:'昨日用能',
        data: cabinetList.value.map(item => item.yesterdayEq == undefined ? null : item.yesterdayEq),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  }
  console.log('接口获取柜列状态数据详情end', cabinetList.value, machineColInfo)
}
const getCabinetColorAll = async () => {
  const res = await IndexApi.getCabinetColorAll()
  if (res != null) {
    tempList.value = res
    tempList.value.forEach((item,index) => {
      statusList[8].push({
        name: item.min + '°C ~ ' + item.max + '°C',
        selected: true,
        value: index+1,
        color: item.color,
        hotName: item.hotMin + '°C ~ ' + item.hotMax + '°C',
        hotColor: item.hotColor
      })
    })
  }
}
// 处理母线插接箱的初始化处理
// const handleBusInit = (data) => {
//   if(data.pduBar == 1 && data.barA && data.barA.boxList) {
//     data.barA.boxList.forEach((item,index) => {
//       machineColInfo.barA.boxList[index].boxName = 'plugin-' + index
//     })
//     data.barB.boxList.forEach((item,index) => {
//       machineColInfo.barB.boxList[index].boxName = 'plugin-' + index
//     })
//   }
//   console.log('machineColInfo', machineColInfo, data)
// }
// 接口获取柜列信息
const getMachineColInfo = async() => {
  const result = await MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})

  emit('sendList', result);
    //push({path: '/aisle/index', state: { roomDownVal: result.roomId}});
    Object.assign(machineColInfo, result);
    handleCabinetList(result); 
    // handleBusInit(result);
    console.log('getMachineColInfo', result);
}

// 接口获取柜列信息
const getMachineColInfoReal = async() => {
  const result = await MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})

  emit('sendList', result);
    //push({path: '/aisle/index', state: { roomDownVal: result.roomId}});
    // Object.assign(machineColInfo, result);
    handleCabinetListReal(result); 
    // handleBusInit(result);
    console.log('getMachineColInfo', result);
}


const saveMachineBus = async() => {
  const filterCabinet = cabinetList.value.filter(item => item.cabinetName)
  console.log('filterCabinet', filterCabinet)
  console.log('machineColInfo', machineColInfo)
  // filterCabinet.forEach((cab, index) => {
  //   if (Number.isInteger(cab.boxIndexA)) {
  //     const target = machineColInfo.barA.boxList.find(box => box.boxIndex == cab.boxIndexA)
  //     if (target) cab.boxNameA = target.boxName
  //   }
  //   if (Number.isInteger(cab.boxIndexB)) {
  //     const target = machineColInfo.barB.boxList.find(box => box.boxIndex == cab.boxIndexB)
  //     if (target) cab.boxNameB = target.boxName
  //   }
  // })
  console.log('cabinetList', filterCabinet, {
    ...machineColInfo,
    length: cabinetList.value.length,
    cabinetList: filterCabinet,
  })
  const res = await MachineColumnApi.saveAisleDetail({
    ...machineColInfo,
    length: cabinetList.value.length,
    cabinetList: filterCabinet
  })
  if(filterCabinet.length && barChangeType.value != 'edit' && barChangeType.value != 'delete') {
    filterCabinet.forEach(async (cab, index) => {
      const resCab = await CabinetApi.saveCabinetInfo({
        ...cab,
        pduBox: true
      })
      console.log("resCab",{...cab},resCab)
    })
  }
  message.success('保存成功！')
  editEnable.value = false
  barChangeType.value = ''
  setTimeout(() => { getMachineColInfo() } ,1000)
  console.log('saveMachineBus', res)
}
// 处理机柜列表
const handleCabinetList = async(data) => {
  console.log('处理机柜列表', data)
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({})
  }
  // 给机柜要连接的插接箱 找到对应的下标
  data.cabinetList && data.cabinetList.forEach(item => {
    if(item.index > 0) {
      arr.splice(item.index - 1, 1, item)
    }
  })
  console.log('arr', arr)
  cabinetList.value = arr
  handleDataDetail(data)
  handleCssScale()
  await toCreatConnect()
  controlEndpointShow(true)
}
// 实时处理机柜列表
const handleCabinetListReal = async(data) => {
  let show = cabinetList.value
  console.log(show)
  handleDataDetail(data)
  updateCabinetConnect()
}
// 增加空机柜
const addMachine = () => {
  console.log('addMachine')
  cabinetList.value.push({})
  updateCabinetConnect()
}
// 删除空机柜
const deleteMachine = () => {
  console.log('deleteMachine')
  cabinetList.value.pop()
  updateCabinetConnect()
}
//
const switchBtn = (value) => {
  chosenBtn.value = value
  nextTick(()=>{updateCabinetConnect()})
  console.log('switchBtn', value, cabinetList.value,)
}

window.addEventListener('resize', function() {
  console.log('resize----')
  updateCabinetConnect()
})

// 计算出要缩放的比例
const handleCssScale = () => {
  scaleValue.value = 1
  // if (scaleValue.value != 1) scaleValue.value = 1/scaleValue.value
  isFromHome && nextTick(()=>{
    const targetMain = document.querySelector('.topologyContainer > .Container > .main') as Element
    const startBusWidth = machineColInfo.pduBar ? 132 : 0 // 两边始端箱宽度总和 
    const childWidth = targetMain.getBoundingClientRect().width + startBusWidth // Container元素的宽
    const childHeight = targetMain.getBoundingClientRect().height + 30 // Container元素的高 30是padding的长
    ContainerHeight.value = childHeight
    scaleValue.value = +(((containerInfo?.width - 30)/childWidth).toFixed(2))
    if (childHeight > 400) scaleValue.value = +(400/childHeight).toFixed(2)
    if (scaleValue.value > 1) scaleValue.value = 1
    console.log('containerInfo', childWidth, containerInfo?.width, scaleValue.value, childHeight, childHeight*scaleValue.value,machineColInfo)
  })
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await MachineColumnApi.getAisleList({})
  console.log('接口获取机房导航列表*****', res)
  if (res && res.length) {
    roomList.value = res
    handleNavList(queryParams.cabinetroomId)
  }
}

const handleNavList = (cabinetroomId) => {
  let targetRoom = null as any
  if (!queryParams.cabinetroomId) {
    if(queryParams.roomDownValId == null || queryParams.roomDownValId  == ""){
        queryParams.cabinetroomId = roomList.value[0].roomId
    }else{
        queryParams.cabinetroomId = queryParams.roomDownValId 
    }
    targetRoom = roomList.value[0]
  } else {
    targetRoom = roomList.value.find(item => item.roomId == queryParams.cabinetroomId)
  }
  machineList.value = targetRoom.aisleList || []
  if (!queryParams.cabinetColumnId && machineList.value) {
    queryParams.cabinetColumnId = machineList.value[0].id
  }
  emit('idChange', queryParams.cabinetroomId)
  getMachineColInfo()
}

watch(() => queryParams.cabinetroomId, (val) => {
  if (val) {
    const targetRoom = roomList.value.find(item => item.roomId == val)
    console.log(targetRoom)
    machineList.value = targetRoom?.aisleList || []
    if (!machineList.value.find(item => item.id == queryParams.cabinetColumnId)) {
      queryParams.cabinetColumnId = machineList.value[0].id
    }
  }
})

watch(() => queryParams.cabinetColumnId,(val) => {
  console.log('wwwwwwwwwww', val, machineList.value)
  emit('idChange', val)
  editEnable.value = false
  barChangeType.value = ''
  toCreatConnect(true)
  instance?.deleteEveryConnection()
  getMachineColInfo()
})

watch(() => containerInfo, (val) => {
  if (val) {
    queryParams.cabinetColumnId = containerInfo?.cabinetColumnId
    queryParams.cabinetroomId = containerInfo?.cabinetroomId
  }
},{immediate: true})

onMounted(() => {
  getNavList()
  initConnect()
  getCabinetColorAll()
  flashListTimer.value = setInterval((getMachineColInfoReal), 5000);
  // intervalTimer = setInterval(() => {
  //   getDataDetail()
  // }, 10000)
  document.addEventListener('mouseup',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
    if (event.button == 0 && operateMenuBox.value.show && element.className != 'menu_item') {
      operateMenuBox.value.show = false
    }
  })
})

onBeforeUnmount(() => {
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})
</script>

<style lang="scss" scoped>
.topologyContainer {
  transform-origin: center top;
  transform: scale(1, 1);
}
.CabEchart {
  width: 100%;
  height: 500px;
  margin-top: 50px;
}
.mask {
  width: 100%;
  height: 290px;
  position: absolute;
  top: 0;
  z-index: 99;
}
.btn-main {
  display: flex;
  align-items: center;
}
.tip {
  display: flex;
  align-items: center;
  .color1 {
    width: 13px;
    height: 10px;
    border-radius: 3px;
    background-color: #95d475;
    margin-right: 5px;
  }
  .color2 {
    width: 13px;
    height: 10px;
    border-radius: 3px;
    background-color: #eebe77;
    margin-right: 5px;
  }
}
.btns {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.topForm .line {
  display: inline-block;
  width: 8px;
  border-bottom: 1px solid #000;
  margin: 0px 8px 13px 8px;
}
:deep(.topForm .el-form-item) {
  margin-right: 0px
}
:deep(.el-card__body) {
  overflow-x: auto;
  overflow-y: hidden;
}
.topologyMain :deep(.el-card__body) {
  & > div {
    display: flex;
    justify-content: center;
  }
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
.Container {
  display: flex;
  // overflow: hidden;
  // align-items: center;
  // padding-bottom: 20px;
  // min-height: calc(100vh - 270px);
  .Bus {
    position: relative;
    .startBus {
      position: relative;
      z-index: 100;
      font-size: 12px;
      color: #fcfcfce1;
      height: 76px;
      box-sizing: border-box;
      border: 1px solid #999;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: silver;
      // box-shadow: 0 0 10px silver;
      margin-bottom: 50px;
    }
    .maskPoint1 {
      width: 10px;
      height: 6px;
      position: absolute;
      left: -5px;
      top: -6px;
      background-color: #fff;
    }
    .maskPoint2{
      width: 6px;
      height: 6px;
      position: absolute;
      left: -6px;
      top: 0;
      background-color: #fff;
    }
  }
  .main {
    // height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    .busListContainer {
      height: 80px;
      margin-bottom: 38px;
      position: relative;
      .bridge {
        height: 12px;
        background-color: rgb(155, 166, 190);
        margin-top: 27px;
      }
      .busList2 {
        box-sizing: border-box;
        position: absolute;
        width: 100%;
        height: 100%;
        left: -30px;
      }
      .busList1,.busList2 {
        padding: 0 19px;
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        .plugin-box {
          position: relative;
          height: fit-content;
          font-size: 12px;
          display: flex;
          flex-direction: column;
          border: 1px solid;
          border-top: none;
          background-color: #fff;
          margin: 0 39px;
          z-index: 999;
          .line {
            width: 5px;
            height: 100px;
            background-color: #000;
          }
          .pointContainer {
            width: 100%;
            height: 5px;
            display: flex;
            justify-content: space-around;
            position: absolute;
            bottom: 0px;
            .point {
              border: 1px solid;
              width: 5px;
              height: 5px;
              border-radius: 50%;
              opacity: 0;
            }
          }
        }
        .connector {
          position: relative;
          height: 20px;
          width: 50px;
          border-radius: 2px;
          background-color: #E6E6E6;
          margin-top: -16px;
          .text {
            position: absolute;
            bottom: 24px;
            left: 5px;
          }
        }
        .template-box {
          position: relative;
          width: 75px;
          font-size: 12px;
          margin: 0 39px;
          .connector {
            margin: -16px auto 0;
          }
          .Tbox {
            width: 75px;
            position: absolute;
            top: 0;
            border: 1px solid #999;
            border-top: none;
            background-color: rgb(252, 252, 252);
            display: flex;
            flex-wrap: wrap;
          }
          .T {
            width: 50%;
            box-sizing: border-box;
            display: flex;
            flex-direction: column;
            align-items: center;
          }
        }
      }
    }
  }
}
.cabinetContainer {
  box-sizing: border-box;
  position: relative;
  // margin-top: 80px;
  .cabinetList {
    width: 100%;
    display: flex;
    justify-content: center;
    .cabinetBox {
      .point {
        height: 5px;
        display: flex;
        justify-content: space-around;
        opacity: 0;
        .leftPoint {
          width: 5px;
          height: 5px;
          border: 1px solid;
          border-radius: 50%;
        }
        .rightPoint {
          width: 5px;
          height: 5px;
          border: 1px solid;
          border-radius: 50%;
        }
      }
      .cabinet {
        position: relative;
        width: 100px;
        height: 339px;
        box-sizing: border-box;
        border: 2px solid;
      }
      .inner_empty {
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        background-color: #f9f9f9;
      }
      .inner_fill {
        position: absolute;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        z-index: 10;
      }
      .fill_box {
        position: absolute;
        z-index: 1;
        width: 100%;
        height: 100%;
        padding: 5px;
        box-sizing: border-box;
        background-color: #f2fff8;
      }
      .status {
        font-size: 12px;
        line-height: 1;
        color: #000;
        text-align: center;
        margin-top: 10px;
      }
      .ti_xing {
        width:100%;
        border-bottom: 3px solid black;
        margin: 0 7px;
        box-sizing: border-box;
        height: 0;
      }
    }
  }
  .operateBox {
    width: 20px;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    cursor: pointer;
    .operateIcon {
      width: 25px;
      height: 25px;
      display: flex;
      box-sizing: border-box;
      justify-content: center;
      align-items: center;
      font-size: 25px;
      border-radius: 50%;
      background-color: greenyellow;
      line-height: 1;
      padding-bottom: 3px;
    }
  }
}
.menu {
  box-sizing: border-box;
  position: absolute;
  padding: 10px 0;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  z-index: 999;
  .menu_item {
    width: 70px;
    height: 30px;
    padding: 0 10px;
    box-sizing: border-box;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .menu_item:hover {
    background-color: rgb(231, 245, 255);
    color: rgb(82, 177, 255);
  }
}
</style>