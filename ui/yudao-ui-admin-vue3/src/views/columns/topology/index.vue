<template>
  <ContentWrap>
    <div class="btn-main">
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div>
        <el-button v-if="!editEnable" @click="editEnable = true" type="primary" plain><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
        <el-button v-if="editEnable" @click="handleCancel" type="primary" plain>取消</el-button>
        <el-button v-if="editEnable" @click="handleConfig" type="primary" plain>配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" type="primary" plain>保存</el-button>
      </div>
    </div>
  </ContentWrap>
  <ContentWrap>
    <div ref="topologyContainer" class="topologyContainer" style="position: relative;z-index: 1">
      <div class="Container" :style="{alignItems: machineColInfo.pduBar && machineColInfo.barA ? 'unset' : 'center'}">
        <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus">
          <div class="startBus" :style="{opacity: machineColInfo.barA.direction ? 0 : 1}">
            始端箱
          </div>
          <div class="startBus" v-if="!machineColInfo.barB.direction">
            始端箱
          </div>
        </div>
        <div class="main">
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer">
            <div class="bridge"></div>
            <div class="busList1">
              <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                <!-- 插接箱 -->
                <div v-if="bus.type == 0" class="plugin-box">
                  <div class="box">
                    <div>Ia</div>
                    <div>30</div>
                  </div>
                  <div class="box">
                    <div>Ib</div>
                    <div>8.1</div>
                  </div>
                  <div class="box">
                    <div>Ic</div>
                    <div>50</div>
                  </div>
                  <div class="name">电流</div>
                  <div :id="bus.boxName + '_A-1'" class="pointA"></div>
                  <div :id="bus.boxName + '_A-2'" class="pointB"></div>
                  <div :id="bus.boxName + '_A-3'" class="pointC"></div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div class="Tbox">
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
                  <div class="box">
                    <div>Ia</div>
                    <div>30</div>
                  </div>
                  <div class="box">
                    <div>Ib</div>
                    <div>8.1</div>
                  </div>
                  <div class="box">
                    <div>Ic</div>
                    <div>50</div>
                  </div>
                  <div class="name">电流</div>
                  <div :id="bus.boxName + '_B-1'" class="pointA"></div>
                  <div :id="bus.boxName + '_B-2'" class="pointB"></div>
                  <div :id="bus.boxName + '_B-3'" class="pointC"></div>
                </div>
                <!-- 连接器 -->
                <div v-else class="template-box">
                  <div class="connector">
                    <span class="text">连接器</span>
                  </div>
                  <div class="Tbox">
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
              <!-- <div class="connector">
                <span class="text">连接器</span>
              </div>
              <div class="template-box">
                <div class="connector">
                  <span class="text">连接器</span>
                </div>
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
              <div class="plugin-box">
                <div class="box">
                  <div>Ia</div>
                  <div>30</div>
                </div>
                <div class="box">
                  <div>Ib</div>
                  <div>40</div>
                </div>
                <div class="box">
                  <div>Ic</div>
                  <div>50</div>
                </div>
                <div class="name">电流</div>
              </div> -->
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
                    <div :class="cabinet.cabinetName ? 'inner_fill' : 'inner_empty'" :id="'cabinet-' + index"></div>
                  </div>
                  <div class="status">{{cabinet.cabinetName || ''}}</div>
                </div>
              </template>
              <div class="operateBox">
                <div class="operateIcon" @click.prevent="addMachine">+</div>
                <div class="operateIcon" @click.prevent="deleteMachine">-</div>
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
            始端箱
          </div>
          <div class="startBus" v-if="machineColInfo.barB.direction">
            始端箱
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
  <!-- 添加或修改用户对话框 -->
  <PluginForm ref="columnForm" @success="handleFormPlugin" />
  <CabForm ref="cabinetForm" @success="handleFormCabinet" :roomList="roomList" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance, JsPlumbDefaults } from '@jsplumb/browser-ui'
import { MachineColumnApi } from '@/api/cabinet/column'
import { CabinetApi } from '@/api/cabinet/info'
import { ElMessage, ElMessageBox } from 'element-plus'
import PluginForm from './component/PluginForm.vue'
import CabForm from './component/CabForm.vue'

const message = useMessage()
let instance: BrowserJsPlumbInstance | null = null

const btns = [
  {
    value: 0,
    name: '负荷'
  },
  {
    value: 1,
    name: '电流'
  },
  {
    value: 2,
    name: '电压'
  },
  {
    value: 3,
    name: '功率因素'
  },
  {
    value: 4,
    name: '有功功率'
  },
  {
    value: 5,
    name: '无功功率'
  },
  {
    value: 6,
    name: '视在功率'
  },
  {
    value: 7,
    name: '供电平衡'
  },
  {
    value: 8,
    name: '温度'
  },
  {
    value: 9,
    name: '容量'
  },
  {
    value: 10,
    name: '用能'
  },
]
const topologyContainer = ref()
const chosenBtn = ref(0)
const editEnable = ref(false)
const columnForm = ref()
const cabinetForm = ref()
const machineColInfo = reactive<any>({})
const cabinetList = ref<any>([])
const busListA = ref<any>([])
const busListB = ref<any>([])
const roomList = ref([]) // 机房列表数据
const operateMenu = ref({  // 操作菜单
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
})
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
    cabinetList.value[cabIndex][`boxName${cabRoad}`] = 1 + Number(pluginName.split('-')[1])
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
      cabinetList.value[index][`boxName${cabRoad}`] = ''
    }
    // 如果返回 false，则连接断开操作会被取消
    return true
  })
  
}

const toCreatConnect = () => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    console.log('toCreatConnect', cabinetList.value, machineColInfo)
    nextTick(() => {
      machineColInfo.barA.boxList.forEach(item => {
        if (item.type) return
        const boxElementA = document.getElementById(item.boxName + '_A-1') as Element
        const boxElementB = document.getElementById(item.boxName + '_A-2') as Element
        const boxElementC = document.getElementById(item.boxName + '_A-3') as Element
        console.log('boxElementA', boxElementA, item.boxName)
        // 删除瞄点
        instance?.removeAllEndpoints(boxElementA)
        instance?.removeAllEndpoints(boxElementB)
        instance?.removeAllEndpoints(boxElementC)
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
        const boxElementA = document.getElementById(item.boxName + '_B-1') as Element
        const boxElementB = document.getElementById(item.boxName + '_B-2') as Element
        const boxElementC = document.getElementById(item.boxName + '_B-3') as Element
        console.log('boxElementA', boxElementA, item.boxName)
        instance?.removeAllEndpoints(boxElementA)
        instance?.removeAllEndpoints(boxElementB)
        instance?.removeAllEndpoints(boxElementC)
        instance?.deleteEndpoint(item.boxName + '_B-1')
        instance?.deleteEndpoint(item.boxName + '_B-2')
        instance?.deleteEndpoint(item.boxName + '_B-3')
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
        addCabinetAnchor(index, item)
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
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    machineColInfo.barA.boxList.forEach(item => {
      if (item.type) return
      const boxElementA = document.getElementById(item.boxName + '_A-1') as Element
      const boxElementB = document.getElementById(item.boxName + '_A-2') as Element
      const boxElementC = document.getElementById(item.boxName + '_A-3') as Element
      console.log('更新插接箱瞄点boxElementA', boxElementA, item, machineColInfo.barA)
      // 更新瞄点
      instance?.revalidate(boxElementA)
      instance?.revalidate(boxElementB)
      instance?.revalidate(boxElementC)
    })
    machineColInfo.barB.boxList.forEach(item => {
      const boxElementA = document.getElementById(item.boxName + '_B-1') as Element
      const boxElementB = document.getElementById(item.boxName + '_B-2') as Element
      const boxElementC = document.getElementById(item.boxName + '_B-3') as Element
      // 更新瞄点
      instance?.revalidate(boxElementA)
      instance?.revalidate(boxElementB)
      instance?.revalidate(boxElementC)
    })
  }
}
// 给某个机柜加瞄点 并进行连接
const addCabinetAnchor = (index, data = {} as any) => {
  const cabElementA = document.getElementById('cab-A-' + index) as Element
  const cabElementB = document.getElementById('cab-B-' + index) as Element
  console.log('cabElementB', cabElementB, cabElementA, data)
  instance?.removeAllEndpoints(cabElementA)
  instance?.removeAllEndpoints(cabElementB)
  // 添加瞄点
  if (!data.boxNameA || !data.boxOutletIdA) instance?.addEndpoint(cabElementA, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (!data.boxNameB || !data.boxOutletIdB) instance?.addEndpoint(cabElementB, {
    source: true,
    target: true,
    endpoint: 'Dot'
  })
  if (data.boxNameA && data.boxOutletIdA) { // A路有连接
    const source = document.getElementById('cab-A-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxNameA-1}_A-${data.boxOutletIdA}`)  as Element
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
  if (data.boxNameB && data.boxOutletIdB) { // B路有连接
    const source = document.getElementById('cab-B-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxNameB-1}_B-${data.boxOutletIdB}`)  as Element
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
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName || !machineColInfo.barA) return
    nextTick(() => {
      console.log('更新机柜的连接')
      const cabElementA = document.getElementById('cab-A-' + index) as Element
      const cabElementB = document.getElementById('cab-B-' + index) as Element
      instance?.revalidate(cabElementA)
      instance?.revalidate(cabElementB)
      updatePluginAnchor()
    })
  })
  
}
// 处理右击事件
const handleRightClick = (e) => {
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
      if (cabItem.boxNameA && cabItem.boxOutletIdA) {
        const connections = instance?.getConnections() as any
        const targetConnect = connections?.find(item => item.source.id == ('cab-A-' + index))
        instance?.deleteConnection(targetConnect)
      }
      if (cabItem.boxNameB && cabItem.boxOutletIdB) {
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
      arr.push({
        boxName: '',
        type: i % 2,
      })
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
        boxName: 'plugin-' + i,
        type,
      })
    }
    for(let i = 0; i < small; i++) {
      if (odd > 0) {
        count++
        odd--
      }
      arr.splice((+diff) * (i+1) + count, 0, {
        boxName: 'connect' + i,
        type: type ? 0 : 1,
      })
      count++
    }
  }
  arr = arr.map((item, index) => {
    return {
      ...item,
      boxIndex: index
    }
  })
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
  toCreatConnect()
}
// 机柜弹窗确认后的处理
const handleFormCabinet = (data) => {
  console.log('handleFormCabinet', data)
  data.index = operateMenu.value.curIndex
  cabinetList.value.splice(operateMenu.value.curIndex, 1, data)
  if (machineColInfo.barA) nextTick(() => {
    addCabinetAnchor(operateMenu.value.curIndex, data)
  })
}
// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  console.log('接口获取机房导航列表', res)
  roomList.value = res
}
// 接口获取柜列信息
const getMachineColInfo = async() => {
  const res = await MachineColumnApi.getAisleDetail({id:6})
  Object.assign(machineColInfo, res)
  console.log('getMachineColInfo', res, res.length)
  handleCabinetList(res)
}

const saveMachineBus = async() => {
  console.log('cabinetList', cabinetList.value)
  const res = await MachineColumnApi.saveAisleDetail({
    ...machineColInfo,
    length: cabinetList.value.length,
    cabinetList: cabinetList.value.filter(item => item.cabinetName),
  })
  message.success('保存成功！')
  editEnable.value = false
  console.log('saveMachineBus', res)
}
// 处理机柜列表
const handleCabinetList = (data) => {
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({})
  }
  data.cabinetList && data.cabinetList.forEach(item => {
    arr.splice(item.index - 1, 1, item)
  })
  console.log('arr', arr)
  cabinetList.value = arr
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
  console.log('switchBtn')
}

getMachineColInfo()

onMounted(() => {
  getNavList()
  initConnect()
  document.addEventListener('mouseup',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
})
</script>

<style lang="scss" scoped>
.btn-main {
  display: flex;
  justify-content: space-between;
}
// .btns {
//   display: flex;
//   justify-content: flex-end;
// }
:deep(.el-card__body) {
  overflow-x: auto;
  overflow-y: hidden;
}
.Container {
  display: flex;
  // align-items: center;
  padding-bottom: 20px;
  min-height: calc(100vh - 270px);
  .Bus {
    width: 66px;
    .startBus {
      width: 66px;
      height: 66px;
      box-sizing: border-box;
      border: 1px solid #999;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: silver;
      box-shadow: 0 0 10px silver;
      margin-bottom: 50px;
    }
  }
  .main {
    flex: 1;
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
          border: 1px solid;
          border-top: none;
          padding-top: 20px;
          background-color: #fff;
          margin: 0 39px;
          .name {
            position: absolute;
            top: 2px;
            left: 18px;
          }
          .box {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin: 0 3px;
          }
          .line {
            width: 5px;
            height: 100px;
            background-color: #000;
          }
          .pointA {
            position: absolute;
            bottom: -8px;
            left: 5px;
            border: 1px solid;
            width: 5px;
            height: 5px;
            border-radius: 50%;
          }
          .pointB {
            position: absolute;
            bottom: -8px;
            left: 28px;
            border: 1px solid;
            width: 5px;
            height: 5px;
            border-radius: 50%;
          }
          .pointC {
            position: absolute;
            bottom: -8px;
            right: 5px;
            border: 1px solid;
            width: 5px;
            height: 5px;
            border-radius: 50%;
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
  margin-top: 50px;
  position: relative;
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
        width: 105px;
        height: 350px;
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
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        background-color: #83f8b8;
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