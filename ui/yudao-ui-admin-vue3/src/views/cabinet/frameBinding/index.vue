<template>
  <ContentWrap>
    <div class="cab-info">
      <el-tag class="tag" size="large">{{cabinetInfo.roomName}}-{{cabinetInfo.cabinetName}}</el-tag>
    </div>
    <div class="cab-btns">
      <el-button v-if="!editEnable" @click="editEnable = true" type="primary">编辑</el-button>
      <el-button v-else @click="editEnable = false" plain type="primary">取消</el-button>
      <el-button v-if="editEnable" @click="handleSubmit" plain type="primary">保存</el-button>
    </div>
    <div style="display: flex; justify-content: center;">
      <div ref="frameContainer" class="frameContainer" style="position: relative;z-index: 1;"  @click.right="handleRightClick">
        <div v-if="!editEnable" @click.prevent="" class="mask"></div>
        <div class="portLeft">
          <div class="port" v-for="port in portLeft" :key="port" :id="'portLeft' + port.id"> 
            <div class="info">{{port.id}}：{{port.value}}A</div>
            <img class="plug" alt="" src="@/assets/svgs/plug-n.svg" />
          </div>
        </div>
        <div class="frameList">
          <div class="bottomFrame">
            <div v-for="(frame,index) in frameListInit.length" :key="index" class="frame">{{frameListInit.length - index}}</div>
          </div>
          <div class="topFrame">
            <template v-for="(frame,index) in frameList" :key="index">
              <draggable
                :style="`height: ${frame[0] ? (frame[0].uHeight)*26 : 26}px`"
                class="frame"
                :id="'frame-' + index"
                :list="frame"
                :itemKey="item => item.id"
                :group="frame[0] ? groupFrameFill : groupFrameBlank"
                tag="div"
                animation="100"
                @start="onStart"
                @end.prevent="onEnd"
              >
                <template #item="{ element }">
                  <div v-if="element" class="active" :id="'frame-' + index + '-child'" >
                    {{ element.rackName }}
                  </div>
                </template>
              </draggable>
            </template>
          </div>
        </div>
        <div class="portRight">
          <div class="port" v-for="port in portRight" :key="port" :id="'portRight' + port.id"> 
            <img class="plug" alt="" src="@/assets/svgs/plug-n.svg" />
            <div class="info">{{port.id}}：{{port.value}}A</div>
          </div>
          <!-- <div class="port" v-for="port in portRight" :key="port" :id="'portRight' + port">插座位{{port}}</div> -->
        </div>
        <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
          <div class="menu_item" v-if="operateMenu.add" @click="openBindingFrom('add')">新增</div>
          <div class="menu_item" v-if="!operateMenu.add" @click="openBindingFrom('edit')">编辑</div>
          <div class="menu_item" v-if="!operateMenu.add" @click="handleDeleteFlame">删除</div>
        </div>
      </div>
    </div>
  </ContentWrap>
  <BindingFrom  ref="binding" @success="handleFormSuccess" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'
import draggable from "vuedraggable";
import { CabinetApi } from '@/api/cabinet/info'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import  BindingFrom  from './component/bindingFrom.vue'
import { ElMessageBox } from 'element-plus'


const message = useMessage() // 消息弹窗
const editEnable = ref(false) // 能否编辑
const binding = ref() // 绑定编辑弹窗组件
const frameContainer = ref() // 机架容量
const portLeft = ref<any[]>([]) // 左侧端口
const portRight = ref<any[]>([]) // 右侧端口
const frameList = ref([]) // 实际设备列表
const frameListInit = ref<any>([]) // 初始空的设备列表
const cabinetInfo = ref<any>({}) // 设备信息
const cabinetId = history?.state?.cabinetId || 1
const roomId = history?.state?.roomId || 1
console.log('测试', cabinetId+'-'+roomId)
const groupFrameFill = { // 有填入的群组
  name: 'FrameFill',
  pull: true, //拖出
  put: true // 拖入
}
const groupFrameBlank = { // 空的群组
  name: 'FrameBlank',
  pull: false, //拖出
  put: true, // 拖入
}
const operateMenu = ref({  // 操作菜单
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
  Uindex: 0,
})
const maxHiddenNumber = 100
let instance: BrowserJsPlumbInstance | null = null

//拖拽开始的事件
const onStart = (e) => {
  console.log("开始拖拽")
}

//拖拽结束的事件
const onEnd = (e) => {
  console.log('拖拽结束的事件', e, e.from.id, e.to.id)
  // 如果没有进行置换的拖拽直接返回
  if (e.from == e.to) return false
  // 计算移动的距离，并修改位置值
  const fromIndex = +e.from.id.split('-')[1]
  const toIndex = +e.to.id.split('-')[1]
  const count =  fromIndex - toIndex
  const targetFrame = frameList.value[toIndex][0] as any
  targetFrame.uAddress = targetFrame.uAddress + count
  // 删除所有连接
  instance?.deleteEveryConnection()
  toCreatConnect()
}

const toCreatConnect = () => {
  console.log('测试11',frameList.value);
  // 公共参数配置
  const addEndpointConfig = {
    source: true,
    target: true,
    endpoint: { type: "Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
  }
  const paintStyleConfig = {
    strokeWidth: 2, // 设置连接线的宽度为 2 像素
    stroke: frameList.value.length > maxHiddenNumber ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
    dashstyle: '5 5',
    outlineWidth: 13,
    outlineStroke: 'rgba(0, 0, 0, 0)'
  }
  // const hoverPaintStyleConfig = {
  //   ...paintStyleConfig,
  //   stroke: '#000',
  // }
  // 左侧端口创建连接点
  portLeft.value.forEach(port => {
    const portElement = document.getElementById(`portLeft${port.id}`) as Element
    // 添加瞄点
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
  })
  // 右侧端口创建连接点
  portRight.value.forEach(port => {
    const portElement = document.getElementById(`portRight${port.id}`) as Element
    // 添加瞄点
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Left',
    })
  })
  // 机柜部分
  frameList.value.forEach((fm, index) => {
    if (fm[0]) {
      const frame = fm[0] as any
      const frameElement = document.getElementById(`frame-${index}-child`) as HTMLElement
      console.log('frameElement', frameElement, frame)
      const connectInfo = [] as any
      // 创建左侧瞄点
      instance?.addEndpoint(frameElement, {
        ...addEndpointConfig,
        anchor: 'Left', // [0, 0.7, 0, 0]
      })
      // 创建右侧瞄点
      instance?.addEndpoint(frameElement, {
        ...addEndpointConfig,
        anchor: 'Right', // [1.04, 0.7, 0, 0]
      })
      // 创建左侧瞄点 并连接
      if (frame.outletIdA && frame.outletIdA.length > 0) {
        frame.outletIdA.forEach(port => {
          if (frame.uHeight == 5) {console.log('port', port)}
          const targetElement = document.getElementById(`portLeft${port}`) as Element
          // 进行连线
          let connect = instance?.connect({
            source: frameElement,
            target: targetElement,
            endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
            anchors: ['Left', 'Right'],
            paintStyle: paintStyleConfig
          })
          connectInfo.push({
            connect,
            element: targetElement,
            frameElement,
          })
        })
      }
      // 创建右侧瞄点 并连接
      if (frame.outletIdB && frame.outletIdB.length > 0) {
        frame.outletIdB.forEach(port => {
          const targetElement = document.getElementById(`portRight${port}`) as Element
          // 进行连线
          let connect = instance?.connect({
            source: frameElement,
            target: targetElement,
            endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
            anchors: ['Right', 'Left'],
            paintStyle: paintStyleConfig
          })
          connectInfo.push({
            connect,
            element: targetElement
          })
        })
      }
      frameElement.onclick = null
      // 监听鼠标移入服务器事件
      frameElement.onclick = (e) => {
        connectInfo.forEach((item, index) => {
          instance?.deleteConnection(item.connect)
          connectInfo[index].connect = instance?.connect({
            source: frameElement,
            target: item.element,
            endpoint: { type:"Dot", options:{ radius:2, fillStyle: 'rgba(0, 0, 0, 0)'}},
            anchors: item.element.id.includes('Left') ? ['Left', 'Right'] : ['Right', 'Left'],
            paintStyle: paintStyleConfig
          })
        })
      }
      // 监听鼠标移出服务器事件
      // frameElement.addEventListener('mouseout', (e) => {
      //   connectInfo.forEach(item => {
      //     instance?.deleteConnection(item.connect)
      //   })
      // })
    }
  })
}

const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: frameContainer.value
  })
  // 监听连接
  instance.bind('beforeDrop', connection => {
    const result = judgeFrame(connection.sourceId, connection.targetId)
    const frame = frameList.value[result.frameNum][0] as any
    frame[result.outletId].push(+result.portNum)
    console.log('禁止端口之间和机柜之间互相连接', connection, frameList.value)
    // 禁止端口之间和机柜之间互相连接
    return connection.sourceId.slice(0, 5) != connection.targetId.slice(0, 5)
  })
  // 监听连接断开
  instance.bind('beforeDetach', function(connection) {
    if (connection.suspendedElement) { // 用户手动断开连接
      let sourceId = connection.sourceId
      if (connection.suspendedElement.id.includes('frame')) {
        sourceId = connection.sourceId.includes('port') ? connection.sourceId : connection.targetId
      } else if (connection.suspendedElement.id.includes('port')) {
        sourceId = connection.sourceId.includes('frame') ? connection.sourceId : connection.targetId
      }
      const result = judgeFrame(sourceId, connection.suspendedElement.id)
      console.log('connection.sourceId, connection.suspendedElement.id', connection,connection.source, connection.target, connection.sourceId, connection.targetId, connection.suspendedElement.id)
      const frame = frameList.value[result.frameNum][0] as any
      console.log('frame', frame, result)
      frame[result.outletId] = frame[result.outletId].filter(item => item != result.portNum)
      console.log('用户手动断开连接', result, frame)
    }
    // 如果返回 false，则连接断开操作会被取消
    return true
  })
}

// 判断连线的源和目标的下标 以及连接的是左侧还是右侧
const judgeFrame = (sourceId, targetId) => {
  let portNum = 0
  let frameNum = 0
  let outletId = 'A'
  if (targetId.includes('Left')) {
    portNum = targetId.split('portLeft')[1]
    frameNum = sourceId.split('-')[1]
  } else if(sourceId.includes('Left')) {
    portNum = sourceId.split('portLeft')[1]
    frameNum = targetId.split('-')[1]
  } else if (targetId.includes('Right')) {
    portNum = targetId.split('portRight')[1]
    frameNum = sourceId.split('-')[1]
    outletId = 'B'
  } else if (sourceId.includes('Right')) {
    portNum = sourceId.split('portRight')[1]
    frameNum = targetId.split('-')[1]
    outletId = 'B'
  }
  return {portNum, frameNum, outletId: 'outletId' + outletId}
}

// 处理右击事件
const handleRightClick = (e) => {
  e.preventDefault()
  const container = e.currentTarget
  const currentId = e.target.id.split('-')[1]
  const className = e.target.className
  if (className.includes('active') || className == 'frame') {
    const rect = container.getBoundingClientRect()
    const offsetX = e.clientX - Math.ceil(rect.left) + 1
    const offsetY = e.clientY - Math.ceil(rect.top) + 1
    console.log('handleRightClick',e.target, className, currentId, offsetX, offsetY, Math.ceil(rect.left), Math.ceil(rect.top), e.clientX, e.clientY)
    const Uindex = toCountU(currentId)
    operateMenu.value = {
      left: offsetX + 'px',
      top: offsetY + 'px',
      show: true,
      add: className == 'frame',
      curIndex: currentId,
      Uindex
    }
  }
}

// 获取数据
const getData = async() => {  
  const res = await CabinetApi.getCabinetInfoItem({id: cabinetId})
  console.log('res', res)
  cabinetInfo.value = res
  const leftPort = [] as any
  const rightPort = [] as any
  const frames = [] as any
  for(let i = 1; i <= res.outletA; i++) {
    leftPort.push({
      id: i,
      value: null,
      status: null
    })
  }
  for(let i = 1; i <= res.outletB; i++) {
    rightPort.push({
      id: i,
      value: null,
      status: null
    })
  }
  for(let i = 1; i <= res.cabinetHeight; i++) {
    frames.push([])
  }
  frameListInit.value = [...frames]
  let count = 0
  if (res.rackIndexList && Array.isArray(res.rackIndexList) && res.rackIndexList.length > 0) {
    res.rackIndexList.sort((a,b) => a.uAddress - b.uAddress)
    res.rackIndexList.forEach(item => {
      frames.splice(item.uAddress-1-count, item.uHeight, [{
        id: item.id,
        rackName: item.rackName,
        rackType: item.rackType,
        company: item.company,
        uAddress: item.uAddress,
        uHeight: item.uHeight,
        outletIdA: item.outletIdA,
        outletIdB: item.outletIdB,
      }])
      count = (count + item.uHeight - 1)
    })
  }
  frameList.value = frames.reverse()
  portLeft.value = leftPort.reverse()
  portRight.value = rightPort.reverse()
  console.log('leftPort', frameList.value, frameListInit.value)
  getPortList()
  nextTick(toCreatConnect)
}

// 获取端口
const getPortList=  async() => {
  const cabinetInfoValue = cabinetInfo.value
  if (cabinetInfoValue.pduIpA) {
    const res = await PDUDeviceApi.PDUDisplay({devKey: cabinetInfoValue.pduIpA})
    if (res.pdu_data && res.pdu_data.output_item_list) {
      const current = res.pdu_data.output_item_list.cur_value.reverse()
      const state = res.pdu_data.output_item_list.relay_state.reverse()
      portLeft.value.forEach((item, index) => {
        portLeft.value[index].value = current[index]
        portLeft.value[index].status = state[index]
      })
    }
  }
  if (cabinetInfoValue.pduIpB) {
    const res = await PDUDeviceApi.PDUDisplay({devKey: cabinetInfoValue.pduIpB})
    if (res.pdu_data && res.pdu_data.output_item_list) {
      const current = res.pdu_data.output_item_list.cur_value.reverse()
      const state = res.pdu_data.output_item_list.relay_state.reverse()
      portRight.value.forEach((item, index) => {
        portRight.value[index].value = current[index]
        portRight.value[index].status = state[index]
      })
    }
  }
}

// 处理保存提交事件
const handleSubmit = async() => {
  console.log('handleSubmit')
  const racksFilter = frameList.value.filter(item => item[0])
  const racks = racksFilter.map(item => item[0])
  const res = await CabinetApi.saveRackBatch({
    cabinetId: cabinetInfo.value.id,
    roomId: cabinetInfo.value.roomId,
    racks
  })
  console.log('res', res)
  if (res) {
    message.success('保存成功')
  }
}

// 处理设备删除事件
const handleDeleteFlame = () => {
  ElMessageBox.confirm('确认删除吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消'
  })
    .then(() => {
      const index = operateMenu.value.curIndex
      let frame = frameList.value[index] as any
      frame.pop()
      console.log('handleDeleteFlame', frameList.value, frame)
      nextTick(toCreatConnect)
    })
    .catch(() => console.info('操作取消'))
}

// 打开编辑/新增弹窗
const openBindingFrom = (type) => {
  console.log('type', type)
  const index = operateMenu.value.curIndex
  const frameListCopy = frameList.value as any
  console.log('openBindingFrom', frameListCopy, index,frameListCopy.length)
  const data = frameListCopy[index].length > 0 ? frameListCopy[index][0] : {}
  binding.value.open(type, data, operateMenu.value.Uindex, cabinetInfo.value.outletA, cabinetInfo.value.outletB)
}

// 处理表单添加/编辑成功
const handleFormSuccess = (data) => {
  const curIndex = operateMenu.value.curIndex
  const frames = [...frameList.value] as any
  const value = frames[curIndex][0] as any
  if (value) { // 如果对应位置有值  只能替换对应的值
    const id = value.id || -1
    const count = data.uHeight - value.uHeight
    console.log('count', count)
    if (count == 0) {
      frames.splice(curIndex, 1, [{ id, ...data }])
    } else if (count > 0) {
      frames.splice(curIndex - count, 1 + count, [{ id, ...data }])
    } else if (count < 0) {
      const arr = [] as any
      for(let i=0; i< -count; i++) {
        arr.push([])
      }
      frames.splice(curIndex, 1, [{ id, ...data }], ...arr)
    }
  } else {
    frames.splice(curIndex - (data.uHeight - 1), data.uHeight, [{ undefined, ...data }])
  }
  frameList.value = frames
  instance?.deleteEveryConnection()
  nextTick(toCreatConnect)
}

// 计算所处的是哪个U位
const toCountU = (index) => {
  let count = 0
  frameList.value.find((item, i) => {
    const target = item[0] as any
    if (target && target.uHeight > 1) {
      count = count + target.uHeight
    } else {
      count++
    }
    if (i == index) return true
  })
  console.log('frameListInit.value.length - count', frameListInit.value.length - count + 1, count)
  const result = frameListInit.value.length - count + 1
  return result
}

watch(frameList, (val) => {
  if (frameList.value.length < 1) return
  console.log('val', val)
  const arr = val as any
  const Aselect = [] as any
  const Bselect = [] as any
  const ANotSelect = [] as any
  const BNotSelect = [] as any
  arr.forEach(child => {
    if (!child[0]) return
    console.log('child[0]', cabinetInfo.value)
    Aselect.push(...child[0].outletIdA)
    Bselect.push(...child[0].outletIdB)
  })
  console.log('BNotSelsssssssssssect', Aselect.includes(2), Bselect)
  // for(let i = 1; i <= cabinetInfo.value.outletA; i++) {
  //   console.log('Aselect.includes(i)', i, Aselect.includes(i))
  //   if (!Aselect.includes(i)) ANotSelect.push(i)
  //   if (!Bselect.includes(i)) BNotSelect.push(i)
  // }
  console.log('BNotSelect', ANotSelect, BNotSelect)
})

onMounted(() => {
  initConnect()
  getData()
  document.addEventListener('mouseup',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
    if (event.button == 0 && element.className && typeof element.className === 'string' && element.className.includes('active')) {
      console.log('element', element.className)
      instance?.deleteEveryConnection()
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mouseup', () => {})
})

</script>

<style lang="scss" scoped>
.cab-info {
  position: relative;
  .tag {
    position: absolute;
    top: 0;
    left: 0;
  }
}
.frameContainer {
  display: flex;
  align-items: center;
  position: relative;
  .mask {
    position: absolute;
    left: 0;
    right: 0;
    z-index: 999;
    height: 100%;
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
}
.portRight,
.portLeft {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-evenly;
  padding: 0 30px;
  .port {
    width: 108px;
    height: 32px;
    font-size: 12px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    .plug {
      width: 40px;
      height: 22px;
    }
  }
}
.portRight {
  .port {
    width: 128px;
    .info {
      width: 60px;
    }
  }
}
.frameList {
  position: relative;
  width: 250px;
  height: fit-content;
  border: 5px solid #90b8df;
  box-sizing: border-box;
  margin: 0 200px;
  font-size: 12px;
  .frame {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 26px;
    box-sizing: border-box;
    background-color: transparent;
    border-bottom: 1px solid #eaeaea;
    .active {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #5298df;
    }
  }
  .topFrame {
    width: 240px;
    box-sizing: border-box;
    position: absolute;
    top: 0;
  }
  .bottomFrame {
    .frame {
      width: 240px;
      background-color: transparent;
      justify-content: flex-start;
      border-bottom: none;
    }
    // height: 23px;
    // box-sizing: border-box;
    // background-color: #fff;
    // border-bottom: 1px solid #fafafa;
  }
}
.cab-btns {
  position: absolute;
  right: 38px;
}
.ghost {
  border: solid 1px rgb(19, 41, 239);
}
.chosenClass {
  background-color: #f1f1f1;
}
</style>