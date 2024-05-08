<template>
  <div ref="frameContainer" class="frameContainer">
    <!-- <div>
      <div>主要电流：192.168.1.188</div>
      <el-table :data="mainPDUData">
        <el-table-column prop="name" label="" width="70" align="center" />
        <el-table-column prop="dy" label="电压" width="70" align="center" />
        <el-table-column prop="dl" label="电流" width="70" align="center" />
      </el-table>
    </div> -->
      <div>
        <div class="portTitle">左侧端口</div>
        <div class="portLeft">
          <div class="port" v-for="port in portLeft" :key="port.id" :id="'portLeft' + port.id" @dblclick="showPortInfo(port)">{{port.name}}</div>
        </div>
      </div>
      <div class="frameBox">
        <div class="frameTitle">机柜</div>
        <div class="frameList">
          <div class="frame fill" v-for="frame in frameList" :key="frame.id" :id="'frame' + frame.id" @dblclick="showFrameInfo(frame)">{{frame.name}}</div>
        </div>
      </div>
      <div>
        <div class="portTitle">右侧端口</div>
        <div class="portRight">
          <div class="port" v-for="port in portRight" :key="port.id" :id="'portRight' + port.id" @dblclick="showPortInfo(port)">{{port.name}}</div>
        </div>
      </div>
    <!-- <div>
      <div>备用电流：192.165.1.158</div>
        <el-table :data="mainPDUData">
          <el-table-column prop="name" label="" width="70" align="center" />
          <el-table-column prop="dy" label="电压" width="70" align="center" />
          <el-table-column prop="dl" label="电流" width="70" align="center" />
        </el-table>
    </div> -->
  </div>
  <!-- <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form 
      ref="machineForm"
      v-loading="formLoading"
      :model="formData"
      label-width="200px"
      center
    >
      <el-form-item v-if="formData.frameStatus" label="机架状态：">
        <el-input :value="formData.frameStatus" disabled />
      </el-form-item>
      <el-form-item v-if="formData.pduStatus" label="PDU输入状态：">
        <el-input :value="formData.pduStatus" disabled />
      </el-form-item>
      <el-form-item label="机架与PDU连接状态：">
        <el-input :value="formData.connectStatus" disabled />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
    </template>
  </Dialog> -->
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, ready } from '@jsplumb/browser-ui'
import { ref, reactive, onMounted  } from 'vue'

const frameContainer = ref()
const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref({})

const showPortInfo = (data) => {
  dialogTitle.value = '端口信息'
  formData.value = data
  dialogVisible.value = true
}

const showFrameInfo = (data) => {
  dialogTitle.value = '机柜信息'
  formData.value = data
  dialogVisible.value = true
}

onMounted(() => {
  console.log('onMounted')
  // ready(() => {
    console.log('ready')
    const instance = newInstance({
      container: frameContainer.value
    })
    console.log('instance', instance)
    frameList.forEach(frame => {
      const frameElement = document.getElementById(`frame${frame.id}`) as Element
      instance.addEndpoint(frameElement, {
        target: true,
        endpoint: "Blank"
      })
      instance.addEndpoint(frameElement, {
        source: true,
        endpoint: "Blank"
      })
    })
    portLeft.forEach(port => {
      const portElement = document.getElementById(`portLeft${port.id}`) as Element
      instance.addEndpoint(portElement, {
        source: true,
        endpoint: "Blank"
      })
      const targetElement = document.getElementById(`frame${port.targetId}`) as Element
      const connect = instance.connect({
        source: portElement,
        target: targetElement,
        endpoint: "Blank",
        anchors: ['Right', 'Left'],
        // connector: {
        //   type: 'Bezier',
        //   options:{
        //     curviness: 50
        //   }
        // },
        paintStyle: {
          strokeWidth: 2, // 设置连接线的宽度为 2 像素
          stroke: frameList.length > 10 ? 'rgba(0, 0, 0, 0)' : '#111', // 设置连接线的颜色为黑色
          outlineWidth: 13,
          outlineStroke: 'rgba(0, 0, 0, 0)'
        },
      })
      if (frameList.length > 10) {
        connect.hoverPaintStyle = {
          strokeWidth: 2,
          stroke: '#111',
          outlineWidth: 13,
          outlineStroke: 'rgba(0, 0, 0, 0)'
        }
      }
      // console.log('connect', connect)
    })
    portRight.forEach(port => {
      const portElement = document.getElementById(`portRight${port.id}`) as Element
      instance.addEndpoint(portElement, {
        target: true,
        endpoint: "Blank"
      })
      const targetElement = document.getElementById(`frame${port.targetId}`) as Element
      console.log('portElement', portElement)
      const connect = instance.connect({
        source: targetElement,
        target: portElement,
        endpoint: "Blank",
        anchors: ['Right', 'Left'],
        // connector: {
        //   type: 'Bezier',
        //   options:{
        //     curviness: 50
        //   }
        // },
        paintStyle: {
          strokeWidth: 2, // 设置连接线的宽度为 2 像素
          stroke: frameList.length > 10 ? 'rgba(0, 0, 0, 0)' : '#111', // 设置连接线的颜色为黑色
          outlineWidth: 13,
          outlineStroke: 'rgba(0, 0, 0, 0)'
        },
      })
      if (frameList.length > 10) {
        connect.hoverPaintStyle = {
          strokeWidth: 2,
          stroke: '#111',
          outlineWidth: 13,
          outlineStroke: 'rgba(0, 0, 0, 0)'
        }
      }
      console.log('connect', connect)
    })
  // })
})

const portLeft = reactive([
  {
    id: 101,
    name: '端口1',
    targetId: 202,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
  {
    id: 102,
    name: '端口2',
    targetId: 201,
    pduStatus: '220v 3A',
    connectStatus: '警告'
  },
  {
    id: 103,
    name: '端口3',
    targetId: 203,
    pduStatus: '220v 3A',
    connectStatus: '异常'
  },
  {
    id: 104,
    name: '端口4',
    targetId: 204,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
  {
    id: 105,
    name: '端口5',
    targetId: 205,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
  {
    id: 106,
    name: '端口6',
    targetId: 208,
    pduStatus: '220v 3A',
    connectStatus: '警告'
  },
  {
    id: 107,
    name: '端口7',
    targetId: 207,
    pduStatus: '220v 3A',
    connectStatus: '异常'
  },
  {
    id: 108,
    name: '端口8',
    targetId: 206,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
])
const portRight = reactive([
  {
    id: 301,
    name: '端口1',
    targetId: 202,
    pduStatus: '220v 3A',
    connectStatus: '异常'
  },
  {
    id: 302,
    name: '端口2',
    targetId: 203,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
  {
    id: 303,
    name: '端口3',
    targetId: 204,
    pduStatus: '220v 3A',
    connectStatus: '警告'
  },
  {
    id: 304,
    name: '端口4',
    targetId: 201,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
  {
    id: 305,
    name: '端口5',
    targetId: 206,
    pduStatus: '220v 3A',
    connectStatus: '异常'
  },
  {
    id: 306,
    name: '端口6',
    targetId: 207,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
  {
    id: 307,
    name: '端口7',
    targetId: 205,
    pduStatus: '220v 3A',
    connectStatus: '警告'
  },
  {
    id: 308,
    name: '端口8',
    targetId: 208,
    pduStatus: '220v 3A',
    connectStatus: '正常'
  },
])
const frameList = reactive([
  {
    id: 201,
    name: '服务器1',
    frameStatus: '未占用',
    connectStatus: '正常'
  },
  {
    id: 202,
    name: '服务器2',
    frameStatus: '未占用',
    connectStatus: '正常'
  },
  {
    id: 203,
    name: '服务器3',
    frameStatus: '上电',
    connectStatus: '正常'
  },
  {
    id: 204,
    name: '服务器4',
    frameStatus: '下电',
    connectStatus: '异常'
  },
  {
    id: 205,
    name: '服务器5',
    frameStatus: '未占用',
    connectStatus: '正常'
  },
  {
    id: 206,
    name: '服务器6',
    frameStatus: '未占用',
    connectStatus: '正常'
  },
  {
    id: 207,
    name: '服务器7',
    frameStatus: '上电',
    connectStatus: '正常'
  },
  {
    id: 208,
    name: '服务器8',
    frameStatus: '下电',
    connectStatus: '异常'
  },
])
const mainPDUData = reactive([
  {
    name: 'L1',
    dy: '225.9V',
    dl: '0.0A'
  },
  {
    name: 'L2',
    dy: '220.1V',
    dl: '2.0A'
  },
  {
    name: 'L3',
    dy: '221.5',
    dl: '2.5A'
  },
])
</script>

<style lang="scss" scoped>
.frameContainer {
  height: 400px;
  position: relative;
  width: 100%;
  box-sizing: border-box;
  display: flex;
  justify-content: space-around;
}
.frameBox {
  height: 100%;
  margin: 0 80px;
  .frameTitle {
    // height: 120px;
    display: flex;
    align-items: center;
    justify-content: center;
    // text-align: center;
    font-size: 20px;
    font-weight: bold;
    margin-bottom: 20px;
  }
}
.portTitle {
  height: 50px;
  font-size: 20px;
  font-weight: bold;
}
.portRight,
.portLeft {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  // border: 1px solid #000;
  border-bottom: none;
  .port {
    position: relative;
    width: 60px;
    height: 30px;
    // border-bottom: 1px solid #000;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    box-sizing: border-box;
    background-color: antiquewhite;
    margin-bottom: 10px;
  }
  .port::before {
    position: absolute;
    top: -50px;
    left: -40px;
    content: '';
    width: 60px;
    height: 60px;
    border-radius: 60px;
    // border: 1px solid #000;
    background-color: #fff;
  }
  .port::after {
    position: absolute;
    top: -50px;
    right: -40px;
    content: '';
    width: 60px;
    height: 60px;
    border-radius: 60px;
    // border: 1px solid #000;
    background-color: #fff;
  }
}
.frameList {
  width: 200px;
  display: flex;
  flex-direction: column;
  font-size: 14px;
  // box-sizing: border-box;
  border: 3px solid #0080ff;
  border-bottom: 12px solid #0080ff;
  .frame {
    box-sizing: border-box;
    width: 100%;
    height: 40px;
    background-color: #fff;
    border: 1px solid #0080ff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .fill {
    background-color: #b4ee97;
  }
}
:deep(.el-table__row .el-table__cell) {
  border: none;
}
:deep(.el-table__header-wrapper .el-table__header .el-table__cell) {
  border: none;
}
:deep(.el-table) :before {
  height: 0;
}
// :deep(.el-table .el-table__body .el-table__row) {
//   max-height: 20px;
// }
</style>