<template>
  <div ref="frameContainer" class="frameContainer" style="position: relative;">
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <!-- <ContentWrap style="height: calc(100% - 15px)"> -->
          <div class="portLeft">
            <div class="port" v-for="port in portLeft" :key="port.id" :id="'portLeft' + port.id">{{port.name}}</div>
          </div>
        <!-- </ContentWrap> -->
      </el-col>
      <el-col :span="16" :xs="24">
        <draggable
          class="frameList"
          :list="frameList"
          :itemKey="item => item.id"
          ghost-class="ghost"
          chosen-class="chosenClass"
          animation="100"
          @start="onStart"
          @end.prevent="onEnd"
        >
          <template #item="{ element }">
            <div class="frame" :id="'frame' + element.id">
              {{ element.name }}
            </div>
          </template>
        </draggable>
          <!-- <div class="frame" v-for="frame in frameList" :key="frame.id" :id="'frame' + frame.id">{{frame.name}}</div> -->
      </el-col>
      <el-col :span="4" :xs="24">
        <!-- <ContentWrap style="height: calc(100% - 15px)"> -->
          <div class="portRight">
            <div class="port" v-for="port in portRight" :key="port.id" :id="'portRight' + port.id">{{port.name}}</div>
          </div>
        <!-- </ContentWrap> -->
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'
import { ref, reactive, onMounted  } from 'vue'
import { string } from 'vue-types';
import draggable from "vuedraggable";

const frameContainer = ref()
let instance: BrowserJsPlumbInstance | null = null

//拖拽开始的事件
const onStart = (e) => {
  console.log("开始拖拽")
}

//拖拽结束的事件
const onEnd = (e) => {
  if (e.oldIndex == e.newIndex) return false
  console.log('拖拽结束的事件frameList', e, frameList, instance?.connections)
  // const oldFrameId = frameList[e.oldIndex].id
  // const newFrameId = frameList[e.newIndex].id
  const instances = instance?.connections
  
  interface Type {
    sourceId: string;
    targetId: string;
  }
  let config: Type[] = []
  instances?.forEach(item => {
    config.push({
      sourceId: item.sourceId,
      targetId: item.targetId,
    })
  })
  instance?.deleteEveryConnection()
  nextTick(() => {
    config.forEach((item, index) => {
      instance?.connect({
        source: document.getElementById(item.sourceId) as Element,
        target: document.getElementById(item.targetId) as Element,
        anchor: ['Right', 'Left'],
      })
    })
  })
}

const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: frameContainer.value
  })
  // 公共参数配置
  const addEndpointConfig = {
    source: true,
    target: true,
    endpoint: "Rectangle",
  }
  const paintStyleConfig = {
    strokeWidth: 2, // 设置连接线的宽度为 2 像素
    stroke: frameList.length > 5 ? 'rgba(0, 0, 0, 0)' : '#000', // 设置连接线的颜色为黑色
    outlineWidth: 13,
    outlineStroke: 'rgba(0, 0, 0, 0)'
  }
  const hoverPaintStyleConfig = {
    ...paintStyleConfig,
    stroke: '#000',
  }
  // 禁止端口之间和机柜之间互相连接
  instance.bind('beforeDrop', connection => connection.sourceId.slice(0, 5) != connection.targetId.slice(0, 5))
  // 机柜创建连接点
  frameList.forEach(frame => {
    const frameElement = document.getElementById(`frame${frame.id}`) as Element
    instance?.addEndpoint(frameElement, {
      ...addEndpointConfig,
      anchor: 'Left',
    })
    instance?.addEndpoint(frameElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
  })
  // 左侧端口创建连接点并初始连接
  portLeft.forEach(port => {
    const portElement = document.getElementById(`portLeft${port.id}`) as Element
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Right',
    })
    const targetElement = document.getElementById(`frame${port.targetId}`) as Element
    const connect = instance?.connect({
      source: portElement,
      target: targetElement,
      endpoint: "Rectangle",
      anchors: ['Right', 'Left'],
      paintStyle: paintStyleConfig
    })
    if (connect && frameList.length > 5) {
      connect.hoverPaintStyle = hoverPaintStyleConfig
    }
  })
  // 右侧端口创建连接点并初始连接
  portRight.forEach(port => {
    const portElement = document.getElementById(`portRight${port.id}`) as Element
    instance?.addEndpoint(portElement, {
      ...addEndpointConfig,
      anchor: 'Left',
    })
    const targetElement = document.getElementById(`frame${port.targetId}`) as Element
    const connect = instance?.connect({
      source: targetElement,
      target: portElement,
      endpoint: "Rectangle",
      anchors: ['Right', 'Left'],
      paintStyle: paintStyleConfig
    })
    if (connect && frameList.length > 5) {
      connect.hoverPaintStyle = hoverPaintStyleConfig
    }
  })
}

onMounted(() => {
  initConnect()
})

const portLeft = reactive([
  {
    id: 101,
    name: '端口1',
    targetId: 201,
  },
  {
    id: 102,
    name: '端口2',
    targetId: 202,
  },
  {
    id: 103,
    name: '端口3',
    targetId: 203,
  },
  {
    id: 104,
    name: '端口4',
    targetId: 204,
  },
])
const portRight = reactive([
  {
    id: 301,
    name: '端口1',
    targetId: 201,
  },
  {
    id: 302,
    name: '端口2',
    targetId: 202,
  },
  {
    id: 303,
    name: '端口3',
    targetId: 203,
  },
  {
    id: 304,
    name: '端口4',
    targetId: 204,
  },
])
let frameList = reactive([
  {
    id: 201,
    name: '服务器1'
  },
  {
    id: 202,
    name: '服务器2'
  },
  {
    id: 203,
    name: '服务器3'
  },
  {
    id: 204,
    name: '服务器4'
  },
])
</script>

<style lang="scss" scoped>
.portRight,
.portLeft {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 30px;
  .port {
    width: 150px;
    height: 80px;
    border: 1px solid #000;
    line-height: 80px;
    text-align: center;
    margin-bottom: 30px;
  }
}
.frameList {
  height: calc(100% - 15px);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20px;
  box-sizing: border-box;
  .frame {
    box-sizing: border-box;
    padding: 20px;
    width: 100%;
    background-color: #fff;
    border: 1px solid #000;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
.ghost {
  border: solid 1px rgb(19, 41, 239);
}
.chosenClass {
  background-color: #f1f1f1;
}
</style>