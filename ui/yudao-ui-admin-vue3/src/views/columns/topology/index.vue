<template>
  <!-- <div style="height:100%;min-height: calc(100vh - 120px);display: flex; flex-direction: column;"> -->
    <ContentWrap>
    <div class="btn-main">
      <el-form
        class="-mb-15px topForm"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="" prop="jf" >
          机房：<el-select :size="isFromHome ? 'small' : ''" v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-100px">
            <el-option v-for="item in roomList" :key="item.roomId" :label="item.roomName" :value="item.roomId" />
          </el-select>
        </el-form-item >
        <span class="line"></span>
        <el-form-item label="" prop="jg">
          柜列：<el-select :size="isFromHome ? 'small' : ''" v-model="queryParams.cabinetColumnId" placeholder="请选择" class="!w-100px">
            <el-option v-for="item in machineList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-tooltip v-if="item.value == 4 || item.value == 7" placement="bottom">
            <template #content>
              <div class="tip">
                <div class="color1"></div>
                <div>无功功率</div>
              </div>
              <div class="tip">
                <div class="color2"></div>
                <div>有功功率</div>
              </div>
            </template>
            <el-button @click="switchBtn(item.value)" type="primary" :size="isFromHome ? 'small' : ''" :plain="chosenBtn != item.value">{{item.name}}</el-button>
          </el-tooltip>
          <el-button v-else @click="switchBtn(item.value)" :size="isFromHome ? 'small' : ''" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div v-if="!isFromHome">
        <el-button v-if="!editEnable" @click="editEnable = true" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
        <el-button v-if="editEnable" @click="handleCancel" type="primary" plain>取消</el-button>
        <el-button v-if="editEnable" @click="handleConfig" type="primary" plain>配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" type="primary" plain>保存</el-button>
      </div>
      <slot name="btn"></slot>
    </div>
  </ContentWrap>
  <ContentWrap style="height: calc(100vh - 250px);">
    <div ref="topologyContainer" class="topologyContainer" :style="`position: relative;z-index: 1;transform: scale(${scaleValue}, ${scaleValue});height: ${isFromHome ? (ContainerHeight * scaleValue + 'px') : 'auto'}`">
      <div class="Container" :style="{alignItems: machineColInfo.pduBar && machineColInfo.barA ? 'unset' : 'center'}">
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction ? 0 : 1}">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" :btns="btns" />
            <!-- <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" /> -->
          </div>
          <div class="startBus" v-if="!machineColInfo.barB.direction">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barB" :btns="btns" />
          </div>
        </div>
        <div class="main">
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList1">
              <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0" class="plugin-box">
                  <PluginBox :chosenBtn="chosenBtn" :pluginData="bus" :btns="btns" />
                  <div class="pointContainer">
                    <div v-for="pointIndex in 3" :key="pointIndex" class="point" :id="`plugin-${bus.boxIndex}_A-${pointIndex}`"></div>
                  </div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div v-if="chosenBtn == 8" class="Tbox">
                    <div class="T">
                      <div>T(L1)</div>
                      <div>30°C</div>
                    </div>
                    <div class="T">
                      <div>T(L2)</div>
                      <div>30°C</div>
                    </div>
                    <div class="T">
                      <div>T(L3)</div>
                      <div>30°C</div>
                    </div>
                    <div class="T">
                      <div>T(N)</div>
                      <div>30°C</div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
          <div v-if="machineColInfo.pduBar && machineColInfo.barB" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList2">
              <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0" class="plugin-box">
                  <PluginBox :chosenBtn="chosenBtn" :pluginData="bus" :btns="btns" />
                  <div class="pointContainer">
                    <div v-for="pointIndex in 3" :key="pointIndex" class="point" :id="`plugin-${bus.boxIndex}_B-${pointIndex}`"></div>
                  </div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div v-if="chosenBtn == 8" class="Tbox">
                    <div class="T">
                      <div>T(L1)</div>
                      <div>30°C</div>
                    </div>
                    <div class="T">
                      <div>T(L2)</div>
                      <div>30°C</div>
                    </div>
                    <div class="T">
                      <div>T(L3)</div>
                      <div>30°C</div>
                    </div>
                    <div class="T">
                      <div>T(N)</div>
                      <div>30°C</div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
          <div class="cabinetContainer" @click.right="handleRightClick">
            <div class="cabinetList" v-if="cabinetList && cabinetList.length">
              <template v-for="(cabinet,index) in cabinetList" :key="index">
                <div class="cabinetBox">
                  <div class="point">
                    <div v-if="cabinet.cabinetName" :id="'cab-A-' + index" class="leftPoint"></div>
                    <div v-if="cabinet.cabinetName" :id="'cab-B-' + index" class="rightPoint"></div>
                  </div>
                  <div class="cabinet">
                    <template v-if="cabinet.cabinetName">
                      <div class="inner_fill" :id="'cabinet-' + index" :style="{backgroundColor: cabinet.id ? 'rgba(180, 180, 180, 0.2)' : 'rgba(230, 240, 234)'}"></div>
                      <template v-if="cabinet.id">
                        <div v-if="chosenBtn == 0" class="fill_box">
                          <Echart :options="cabinet.echartsOptionLoad" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 1" class="fill_box">
                          <Echart :options="cabinet.echartsOptionA" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 2" class="fill_box">
                          <Echart :options="cabinet.echartsOptionV" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 3" class="fill_box">
                          <Echart :options="cabinet.echartsOptionFactor" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 4" class="fill_box">
                          <Echart :options="cabinet.echartsOptionApparent" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 7" class="fill_box">
                          <Echart :options="cabinet.echartsOptionBalance" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 8" class="fill_box">
                          <Echart :options="cabinet.echartsOptionTemp" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 9" class="fill_box">
                          <Echart :options="cabinet.echartsOptionCapacity" height="100%" />
                        </div>
                        <div v-if="chosenBtn == 10" class="fill_box">
                          <Echart :options="cabinet.echartsOptionEq" height="100%" />
                        </div>
                      </template>
                    </template>
                    <div v-else class="inner_empty" :id="'cabinet-' + index"></div>
                  </div>
                  <div class="status">{{cabinet.cabinetName || ''}}</div>
                </div>
              </template>
              <div class="operateBox">
                <div v-show="editEnable" class="operateIcon" @click.prevent="addMachine">+</div>
                <div v-show="editEnable" class="operateIcon" @click.prevent="deleteMachine">-</div>
              </div>
            </div>
            <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
              <div class="menu_item" v-if="operateMenu.add" @click="handleOperate('add')">新增</div>
              <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('watch')">查看</div>
              <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('edit')">编辑</div>
              <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('delete')">删除</div>
            </div>
          </div>
        </div>
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction}">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" :btns="btns" />
          </div>
          <div class="startBus" v-if="machineColInfo.barB.direction">
            <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barB" :btns="btns" />
          </div>
        </div>
      </div>
      <div v-if="isFromHome || !editEnable" class="mask" @click.right.prevent="console.log('---')"></div>
    </div>
  </ContentWrap>
  <!-- </div> -->
  <!-- 添加或修改用户对话框 -->
  <PluginForm ref="columnForm" @success="handleFormPlugin" />
  <CabForm ref="cabinetForm" @success="handleFormCabinet" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance, JsPlumbDefaults } from '@jsplumb/browser-ui'
import { MachineColumnApi } from '@/api/cabinet/column'
import { CabinetApi } from '@/api/cabinet/info'
import { ElMessage, ElMessageBox } from 'element-plus'
import PluginForm from './component/PluginForm.vue'
import CabForm from './component/CabForm.vue'
import InitialBox from './component/InitialBox.vue'
import PluginBox from './component/PluginBox.vue'
import { EChartsOption } from 'echarts'

const message = useMessage()
let instance: BrowserJsPlumbInstance | null = null
const roomList = ref([]) // 机房列表
const machineList = ref<any>([]) // 机柜列表
const queryParams = reactive({
  cabinetColumnId: history?.state?.id || 1,
  cabinetroomId: history?.state?.roomId || 1
})
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
const echartsOptionCab = ref<EChartsOption>({})
let intervalTimer = null as any
const topologyContainer = ref()
const chosenBtn = ref(0)
const scaleValue = ref(1)
const ContainerHeight = ref(100)
const editEnable = ref(false)
const columnForm = ref()
const cabinetForm = ref()
const machineColInfo = reactive<any>({})
const cabinetList = ref<any>([])
const busListA = ref<any>([])
const busListB = ref<any>([])
const operateMenu = ref({  // 操作菜单
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
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
const emit = defineEmits(['backData', 'idChange', 'getpdubar']) // 定义 success 事件，用于操作成功后的回调

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
    console.log('监听连接', connection, cabId, pluginId, cabinetList.value,)
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
    console.log('监听连接断开', connection, connection.sourceId, connection.source)
    if (connection.suspendedElement) { // 用户手动断开连接
      const targetId = connection.target.id.includes('cab') ? connection.target.id : connection.source.id
      const cabRoad = targetId.split('-')[1]
      const index = targetId.split('-')[2]
      cabinetList.value[index][`boxOutletId${cabRoad}`] = ''
      cabinetList.value[index][`boxIndex${cabRoad}`] = ''
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
        const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_A-1') as Element
        const boxElementB = document.getElementById('plugin-' + item.boxIndex + '_A-2') as Element
        const boxElementC = document.getElementById('plugin-' + item.boxIndex + '_A-3') as Element
        console.log('boxElementA', boxElementA, item.boxIndex)
        // 删除瞄点
        instance?.removeAllEndpoints(boxElementA)
        instance?.removeAllEndpoints(boxElementB)
        instance?.removeAllEndpoints(boxElementC)
        if (onlyDelete) return
        // 添加瞄点
        instance?.addEndpoint(boxElementA, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementB, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementC, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        // 更新瞄点
        instance?.revalidate(boxElementA)
        instance?.revalidate(boxElementB)
        instance?.revalidate(boxElementC)
      })
      machineColInfo.barB.boxList.forEach(item => {
        if (item.type) return
        const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_B-1') as Element
        const boxElementB = document.getElementById('plugin-' + item.boxIndex + '_B-2') as Element
        const boxElementC = document.getElementById('plugin-' + item.boxIndex + '_B-3') as Element
        console.log('boxElementA', boxElementA, item.boxIndex)
        instance?.removeAllEndpoints(boxElementA)
        instance?.removeAllEndpoints(boxElementB)
        instance?.removeAllEndpoints(boxElementC)
        instance?.deleteEndpoint('plugin-' + item.boxIndex + '_B-1')
        instance?.deleteEndpoint('plugin-' + item.boxIndex + '_B-2')
        instance?.deleteEndpoint('plugin-' + item.boxIndex + '_B-3')
        if (onlyDelete) return
        // 添加瞄点
        instance?.addEndpoint(boxElementA, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementB, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.addEndpoint(boxElementC, {
          source: true,
          target: true,
          endpoint: 'Dot'
        })
        instance?.revalidate(boxElementA)
        instance?.revalidate(boxElementB)
        instance?.revalidate(boxElementC)
      })
      cabinetList.value.forEach((item, index) => {
        if (!item.cabinetName) return
        addCabinetAnchor(index, item, onlyDelete)
      })
    })
  }
}
// // 给某个插接箱加瞄点
// const addPluginAnchor = (boxName) => {
//   console.log('给某个插接箱加瞄点', boxName)
//   const boxElementA1 = document.getElementById(boxName + '_A-1') as Element
//   const boxElementA2 = document.getElementById(boxName + '_A-2') as Element
//   const boxElementA3 = document.getElementById(boxName + '_A-3') as Element
//   const boxElementB1 = document.getElementById(boxName + '_B-1') as Element
//   const boxElementB2 = document.getElementById(boxName + '_B-2') as Element
//   const boxElementB3 = document.getElementById(boxName + '_B-3') as Element
//   console.log('boxElementA', boxElementA1, boxElementB1, boxName)
//   // 删除瞄点
//   instance?.removeAllEndpoints(boxElementA1)
//   instance?.removeAllEndpoints(boxElementA2)
//   instance?.removeAllEndpoints(boxElementA3)
//   instance?.removeAllEndpoints(boxElementB1)
//   instance?.removeAllEndpoints(boxElementB2)
//   instance?.removeAllEndpoints(boxElementB3)
//   // 添加瞄点
//   instance?.addEndpoint(boxElementA1, {
//     source: true,
//     target: true,
//     endpoint: 'Dot'
//   })
//   instance?.addEndpoint(boxElementA2, {
//     source: true,
//     target: true,
//     endpoint: 'Dot'
//   })
//   instance?.addEndpoint(boxElementA3, {
//     source: true,
//     target: true,
//     endpoint: 'Dot'
//   })
//   instance?.addEndpoint(boxElementB1, {
//     source: true,
//     target: true,
//     endpoint: 'Dot'
//   })
//   instance?.addEndpoint(boxElementB2, {
//     source: true,
//     target: true,
//     endpoint: 'Dot'
//   })
//   instance?.addEndpoint(boxElementB3, {
//     source: true,
//     target: true,
//     endpoint: 'Dot'
//   })
// }
// 更新插接箱瞄点
const updatePluginAnchor = () => {
  console.log('updatePluginAnchorupdatePluginAnchor')
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    machineColInfo.barA.boxList.forEach(item => {
      if (item.type) return
      const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_A-1') as Element
      const boxElementB = document.getElementById('plugin-' + item.boxIndex + '_A-2') as Element
      const boxElementC = document.getElementById('plugin-' + item.boxIndex + '_A-3') as Element
      console.log('更新插接箱瞄点boxElementA', boxElementA, item, machineColInfo.barA)
      // 更新瞄点
      instance?.revalidate(boxElementA)
      instance?.revalidate(boxElementB)
      instance?.revalidate(boxElementC)
    })
    machineColInfo.barB.boxList.forEach(item => {
      if (item.type) return
      const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_B-1') as Element
      const boxElementB = document.getElementById('plugin-' + item.boxIndex + '_B-2') as Element
      const boxElementC = document.getElementById('plugin-' + item.boxIndex + '_B-3') as Element
      // 更新瞄点
      instance?.revalidate(boxElementA)
      instance?.revalidate(boxElementB)
      instance?.revalidate(boxElementC)
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
  if (!Number.isInteger(data.boxIndexA) || !data.boxOutletIdA) instance?.addEndpoint(cabElementA, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (!Number.isInteger(data.boxIndexB) || !data.boxOutletIdB) instance?.addEndpoint(cabElementB, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (Number.isInteger(data.boxIndexA) && data.boxOutletIdA) { // A路有连接
    const source = document.getElementById('cab-A-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxIndexA}_A-${data.boxOutletIdA}`)  as Element
    console.log('target', source, target)
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
  if (Number.isInteger(data.boxIndexB) && data.boxOutletIdB) { // B路有连接
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
// 处理右击事件
const handleRightClick = (e) => {
  console.log('处理右击事件', e.target.parentNode)
  e.preventDefault()
  if (e.target.className != 'inner_empty' && e.target.className != 'inner_fill') return
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
// 处理菜单点击事件
const handleOperate = (type) => {
  operateMenu.value.show = false
  const index = operateMenu.value.curIndex
  if (type == 'add' || type == 'edit') {
    let info = {
      roomId: machineColInfo.roomId,
      barA: false
    } as any
    if (machineColInfo.barA) {
      info = {
        roomId: machineColInfo.roomId,
        barA: true,
        busNameA: machineColInfo.barA.busName,
        busIpA: machineColInfo.barA.devIp,
        busNameB: machineColInfo.barB.busName,
        busIpB: machineColInfo.barB.devIp,
        boxAmount: (machineColInfo.barA.boxList.filter(item => !item.type)).length
      }
    }
    const data = type == 'add' ? null : cabinetList.value[index]
    cabinetForm.value.open(type, data, info)
  } else if (type == 'delete') {
    ElMessageBox.confirm('确认删除吗？', '提示', {
      confirmButtonText: '确 认',
      cancelButtonText: '取 消',
      type: 'warning'
    }).then(async () => {
      const cabItem = cabinetList.value[index]
      if (cabItem.boxIndexA && cabItem.boxOutletIdA) {
        const connections = instance?.getConnections() as any
        const targetConnect = connections?.find(item => item.source.id == ('cab-A-' + index))
        instance?.deleteConnection(targetConnect)
      }
      if (cabItem.boxIndexB && cabItem.boxOutletIdB) {
        const connections = instance?.getConnections() as any
        const targetConnect = connections?.find(item => item.source.id == ('cab-B-' + index))
        instance?.deleteConnection(targetConnect)
      }
      cabinetList.value.splice(index, 1, {})
    })
  }
  console.log('handleOperate', machineColInfo)
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
    getMachineColInfo()
  })
}
// 处理配置点击事件  打开配置弹窗
const handleConfig = () => {
  console.log('handleConfig', machineColInfo)
  let data = null as any | null
  if(machineColInfo.barA) {
    console.log('machineColInfo', machineColInfo.barA)
    const boxList = machineColInfo.barA.boxList
    const plugin = boxList.filter(item => !item.type)
    const connect = boxList.filter(item => item.type)
    data = {
      nameA: machineColInfo.barA.busName,
      ipA: machineColInfo.barA.devIp,
      nameB: machineColInfo.barB.busName,
      directionA: machineColInfo.barA.direction,
      directionB: machineColInfo.barB.direction,
      ipB: machineColInfo.barB.devIp,
      cjxAmount: plugin.length,
      ljqAmount: connect.length,
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
  let big = 0
  let small = 0
  let type = 0
  let count = 0
  let arr = [] as any
  if (data.cjxAmount == data.ljqAmount) {
    for(let i = 0; i < (data.cjxAmount + data.ljqAmount); i++) {
      const type = i % 2
      arr.push({
        boxIndex: count,
        type,
      })
      if (type) count++
    }
  } else {
    if (data.cjxAmount > data.ljqAmount) {
      big = data.cjxAmount
      small = data.ljqAmount
    } else {
      big = data.ljqAmount
      small = data.cjxAmount
      type = 1
    }
    const diff = Math.floor(big/(small + 1))
    let odd = big % (small + 1)
    for(let i = 0; i < big; i++) {
      arr.push({
        boxIndex: i,
        type,
      })
    }
    for(let i = 0; i < small; i++) {
      if (odd > 0) {
        count++
        odd--
      }
      arr.splice((+diff) * (i+1) + count, 0, {
        boxIndex: i,
        type: type ? 0 : 1,
      })
      count++
    }
  }
  const boxA = {
    busName: data.nameA,
    devIp: data.ipA,
    path: 'A',
    direction: data.directionA,
    boxList: arr
  }
  const boxB = {
    busName: data.nameB,
    devIp: data.ipB,
    path: 'B',
    direction: data.directionB,
    boxList: arr
  }
  // 处理插接箱删除的时候存在连接的情况，以及新增插接箱的瞄点创建
  // const beforeCjxAmount = machineColInfo.barA ? (machineColInfo.barA.boxList.filter(item => !item.type)).length : 0
  // console.log('beforeCjxAmount', beforeCjxAmount)
  // if (beforeCjxAmount > 0) {
  //   let count = beforeCjxAmount - data.cjxAmount
  //   if (count > 0) { // 删除插接箱 需要考虑删除的插接箱是否存在连接的状态
  //     // for(let i=)
  //   } else if (count < 0) { // 增加插接箱
  //     count = -count
  //     toCreatConnect()
  //     const boxFilter = arr.filter(item => !item.type)
  //     nextTick(() => {
  //       for(let i=0; i < count; i++) {
  //         console.log('boxFilter[beforeCjxAmount + i].boxName', boxFilter[beforeCjxAmount + i].boxName, boxFilter[beforeCjxAmount + i], beforeCjxAmount, i)
  //         addPluginAnchor(boxFilter[beforeCjxAmount + i].boxName)
  //       }
  //       updatePluginAnchor()
  //     })
  //   }
  // } else if (beforeCjxAmount == 0) { // 如果开始的插接箱为0
  //   const boxFilter = arr.filter(item => !item.type)
  //   nextTick(() => {
  //     for(let i=0; i < boxFilter.length; i++) {
  //       addPluginAnchor(boxFilter[i].boxName)
  //     }
  //     updatePluginAnchor()
  //     cabinetList.value.forEach((item, index) => {
  //       if (!item.cabinetName) return
  //       addCabinetAnchor(index)
  //     })
  //   })
  // }
  machineColInfo.barA = boxA
  machineColInfo.barB = boxB
  console.log('machineColInfo', machineColInfo)
  toCreatConnect() // 因为添加插接箱需要添加瞄点 所以要创建
}
// 机柜弹窗确认后的处理
const handleFormCabinet = (data) => {
  console.log('handleFormCabinet', data, operateMenu.value)
  data.index = +operateMenu.value.curIndex + 1
  cabinetList.value.splice(operateMenu.value.curIndex, 1, data)
  if (machineColInfo.barA) nextTick(() => {
    addCabinetAnchor(operateMenu.value.curIndex, data)
  })
}
// 接口获取柜列状态数据详情
const getDataDetail = async() => {
  const res = await MachineColumnApi.getDataDetail({id: queryParams.cabinetColumnId})
  console.log('接口获取柜列状态数据详情', res)
  handleDataDetail(res)
}
// 处理柜列状态数据详情
const handleDataDetail = (res) => {
  if (res.barA) {
    machineColInfo.barA = {
      ...res.barA,
      ...machineColInfo.barA,
    }
    // chineColInfo.barA.powerFactor ? Number(machineColInfo
    res.barA.boxList.forEach((item, index) => {
      machineColInfo.barA.boxList[index] = {
        ...machineColInfo.barA.boxList[index],
        ...item
      }
    })
  }
  if (res.barB) {
    machineColInfo.barB = {
      ...res.barB,
      ...machineColInfo.barB,
    }
    res.barB.boxList.forEach((item, index) => {
      machineColInfo.barB.boxList[index] = {
        ...machineColInfo.barB.boxList[index],
        ...item
      }
    })
  }
  res.cabinetList && res.cabinetList.forEach(cab => {
    cabinetList.value.forEach((item, index) => {
      if (item.id == cab.id) {
        cabinetList.value[index] = {
          ...item,
          ...cab,
          echartsOptionLoad: { // 负载
            xAxis: {
              type: 'category',
              data: ['负载率'],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '-10',
              right: '16',
              bottom: '3%',
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
                data: [cab.loadRate],
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
          },
          echartsOptionA: { // 电流
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ['A路', 'B路'],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '-20',
              right: '0',
              bottom: '3%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [
              {
                name: 'L1',
                data: [3, 5],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
              },
              {
                name: 'L2',
                data: [2, 6],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
              },
              {
                name: 'L3',
                data: [1, 3],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
              },
            ]
          },
          echartsOptionV: { // 电压
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ['A路', 'B路'],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '-20',
              right: '0',
              bottom: '3%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [
              {
                name: 'L1',
                data: [3, 5],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
              },
              {
                name: 'L2',
                data: [2, 6],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
              },
              {
                name: 'L3',
                data: [1, 3],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
              },
            ]
          },
          echartsOptionFactor: { // 功率因素
            xAxis: {
              type: 'category',
              data: ['功率因素'],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '-10',
              right: '16',
              bottom: '3%',
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
          },
          echartsOptionApparent: { // 功率
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ['功率'],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '-20',
              right: '0',
              bottom: '3%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [
              {
                name: '有功功率',
                data: [3],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
                itemStyle: {
                  color: '#95d475'
                }
              },
              {
                name: '无功功率',
                data: [2],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
                itemStyle: {
                  color: '#eebe77'
                }
              },
            ]
          },
          echartsOptionBalance: { // 供电平衡
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ['A路', 'B路'],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '-20',
              right: '0',
              bottom: '3%',
              top: '8%',
              containLabel: true
            },
            // legend: {},
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [
              {
                name: '有功功率',
                data: [3, 5],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
                itemStyle: {
                  color: '#95d475'
                }
              },
              {
                name: '无功功率',
                data: [2, 6],
                type: 'bar',
                stack: 'Ad',
                emphasis: {
                  focus: 'series'
                },
                label: {
                  show: true,
                  formatter: '{c}', // 显示数据值
                },
                itemStyle: {
                  color: '#eebe77'
                }
              },
            ]
            // xAxis: {
            //   type: 'category',
            //   data: ['A路', 'B路'],
            //   axisTick: {
            //     show: false
            //   }
            // },
            // grid: {
            //   left: '-15',
            //   right: '0',
            //   bottom: '3%',
            //   top: '8%',
            //   containLabel: true
            // },
            // yAxis: {
            //   type: 'value',
            //   show: false,
            // },
            // series: [
            //   {
            //     name: '视在功率',
            //     data: [cab.powApparentA, cab.powApparentB],
            //     type: 'bar',
            //     label: {
            //       show: true,
            //       formatter: '{c}', // 显示数据值
            //     },
            //   },
            // ]
          },
          echartsOptionTemp: { // 温度
            xAxis: {
              type: 'category',
              data: [`温度`],
              axisTick: {
                show: false
              }
            },
            grid: {
              left: '0',
              right: '16',
              bottom: '3%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: 45, 
              show: false,
            },
            series: [
              {
                data: [23],
                type: 'bar',
                barWidth: '100%',
                showBackground: true,
                backgroundStyle: {
                  color: 'rgba(180, 180, 180, 0.2)'
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
              }
            },
            grid: {
              left: '0',
              right: '16',
              bottom: '3%',
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
              }
            },
            grid: {
              left: '-10',
              right: '16',
              bottom: '3%',
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
                data: [item.yesterdayEq],
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
  const res1 = MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})
  const res2 = MachineColumnApi.getDataDetail({id: queryParams.cabinetColumnId})
  Promise.all([res1, res2]).then((resultList) => {
    Object.assign(machineColInfo, resultList[0])
    handleCabinetList(resultList[0], resultList[1])
    // handleBusInit(resultList[0])
    console.log('getMachineColInfo', resultList)
  })
}

const saveMachineBus = async() => {
  const filterCabinet = cabinetList.value.filter(item => item.cabinetName)
  console.log('machineColInfo', machineColInfo)
  filterCabinet.forEach((cab, index) => {
    if (Number.isInteger(cab.boxIndexA)) {
      const target = machineColInfo.barA.boxList.find(box => box.boxIndex == cab.boxIndexA)
      if (target) cab.boxNameA = target.boxName
    }
    if (Number.isInteger(cab.boxIndexB)) {
      const target = machineColInfo.barB.boxList.find(box => box.boxIndex == cab.boxIndexB)
      if (target) cab.boxNameB = target.boxName
    }
  })
  console.log('cabinetList', filterCabinet)
  const res = await MachineColumnApi.saveAisleDetail({
    ...machineColInfo,
    length: cabinetList.value.length,
    cabinetList: filterCabinet,
  })
  message.success('保存成功！')
  editEnable.value = false
  console.log('saveMachineBus', res)
}
// 处理机柜列表
const handleCabinetList = (data, status) => {
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({})
  }
  // 给机柜要连接的插接箱 找到对应的下标
  data.cabinetList && data.cabinetList.forEach(item => {
    if (item.boxNameA) {
      const target = data.barA.boxList.find(box => box.boxName == item.boxNameA)
      item.boxIndexA = target.boxIndex
    }
    if (item.boxNameB) {
      const target = data.barB.boxList.find(box => box.boxName == item.boxNameB)
      item.boxIndexB = target.boxIndex
    }
    arr.splice(item.index - 1, 1, item)
  })
  console.log('arr', arr)
  cabinetList.value = arr
  handleDataDetail(status)
  handleCssScale()
  toCreatConnect()
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
  nextTick(()=>{updatePluginAnchor()})
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
    debugger
    const targetMain = document.querySelector('.topologyContainer > .Container > .main') as Element
    const startBusWidth = machineColInfo.pduBar ? 132 : 0 // 两边始端箱宽度总和 
    const childWidth = targetMain.getBoundingClientRect().width + startBusWidth // Container元素的宽
    const childHeight = targetMain.getBoundingClientRect().height + 30 // Container元素的高 30是padding的长
    ContainerHeight.value = childHeight
    scaleValue.value = +(((containerInfo?.width - 30)/childWidth).toFixed(2))
    console.log('containerInfo', childWidth, containerInfo?.width, scaleValue.value, childHeight, childHeight*scaleValue.value)
  })
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await MachineColumnApi.getAisleList({})
  console.log('接口获取机房导航列表*****', res)
  if (res && res.length) {
    roomList.value = res
    machineList.value = handleNavList(queryParams.cabinetroomId)
  }
}

const handleNavList = (cabinetroomId) => {
  const data = roomList.value as any
  const findRoom = data.find(item => item.roomId == cabinetroomId)
  if (!findRoom.aisleList || findRoom.aisleList.length == 0) return []
  const findMachine = findRoom.aisleList.find(item => item.id == queryParams.cabinetColumnId)
  if (findMachine) emit('getpdubar', findMachine.pduBar)
  // pduBar.value = findMachine.pduBar
  console.log('roomIndex', findMachine, findRoom)
  return findRoom.aisleList
}

watch(() => queryParams.cabinetroomId, (val) => {
  machineList.value = handleNavList(val)
  if (machineList.value.length == 0) {
    queryParams.cabinetColumnId = null
    return
  }
  const defaultValue = machineList.value[0] as any
  queryParams.cabinetColumnId = defaultValue.id
})

watch(() => queryParams.cabinetColumnId,(val) => {
  console.log('wwwwwwwwwww', val, machineList.value)
  emit('idChange', val)
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
  getMachineColInfo()
  initConnect()
  // intervalTimer = setInterval(() => {
  //   getDataDetail()
  // }, 10000)
  document.addEventListener('mouseup',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
})

onBeforeUnmount(() => {
  intervalTimer = null
})
</script>

<style lang="scss" scoped>
.topologyContainer {
  transform-origin: left top;
  transform: scale(1, 1);
}
.CabEchart {
  width: 100%;
  height: 500px;
  margin-top: 50px;
}
.mask {
  width: 100%;
  height: calc(100% - 32px);
  position: absolute;
  top: 0;
  z-index: 99;
}
.btn-main {
  display: flex;
  justify-content: space-between;
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
// .btns {
//   display: flex;
//   justify-content: flex-end;
// }
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
.Container {
  display: flex;
  // align-items: center;
  // padding-bottom: 20px;
  // min-height: calc(100vh - 270px);
  .Bus {
    width: 66px;
    .startBus {
      font-size: 12px;
      color: #fcfcfce1;
      width: 66px;
      height: 66px;
      box-sizing: border-box;
      border: 1px solid #999;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: silver;
      box-shadow: 0 0 10px silver;
      margin-bottom: 50px;
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
        left: -60px;
      }
      .busList1,.busList2 {
        padding: 0 40px;
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
            bottom: -5px;
            .point {
              border: 1px solid;
              width: 5px;
              height: 5px;
              border-radius: 50%;
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
  margin-top: 80px;
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
  .cabinetList {
    width: 100%;
    display: flex;
    justify-content: center;
    .cabinetBox {
      .point {
        height: 7px;
        display: flex;
        justify-content: space-around;
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
</style>