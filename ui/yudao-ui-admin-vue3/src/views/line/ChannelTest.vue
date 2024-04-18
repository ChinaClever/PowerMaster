<template>
  <div class="main">
    <ContentWrap>
      <el-button type="primary">添加</el-button>
      <el-button type="primary">配置</el-button>
    </ContentWrap>
    <ContentWrap style="height: 700px;display: flex;align-items: center;padding-left: 300px;">
      <div class="container">
        <template v-for="(machine, index) in machineList" :key="index">
          <draggable
            class="machineBox"
            :list="machine"
            :itemKey="item => item.id"
            :group="machine.length > 0 ? groupMachineFill : groupMachineNull"
            filter=".forbid"
            tag="div"
            animation="100"
            @start="onStart"
            @end.prevent="onEnd"
            >
              <template #item="{ element }">
                <div class="machine forbid" v-if="element.name">
                  <span>{{element.name}}</span>
                    <Icon class="machineDelete" @click="machineDelete(element.id)" icon="ep:close" />
                </div>
                <div class="empty" v-else></div>
              </template>
          </draggable>
        </template>
        <div class="operateBox">
          <div class="operateIcon" @click="addMachine">+</div>
          <div class="operateIcon" @click="deleteMachine">-</div>
        </div>
      </div>
    </ContentWrap>
    <div class="deviceList">
      <div class="header">
        设备列表
      </div>
      <div class="deviceContainer">
        <template v-for="device in deviceList" :key="device.id">
          <draggable
            :list="device"
            :itemKey="item => item.id"
            :group="groupDevice"
            tag="div"
            animation="100"
            @start="onStart"
            @end.prevent="onEnd"
          >
            <template #item="{ element }">
              <div class="device"> {{ element.name }} </div>
            </template>
          </draggable>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import draggable from "vuedraggable"
import { Delete } from '@element-plus/icons-vue'
const machineList = reactive<any>([
  [],[],[],[],[],[],[],[],[],
])
const deviceList = reactive([
  [{
    id: '151',
    name: '配电-配电柜1'
  }],
  [{
    id: '1521',
    name: '机柜-IT机柜101'
  }],
  [{
    id: '1531',
    name: '网络柜'
  }],
])

const groupDevice = {
  name: 'groupCommon',
  pull: 'clone',
  put: false
}

const groupMachineNull = {
  name: 'groupCommon',
  pull: false,
  put: true
}

const groupMachineFill = {
  name: 'groupCommon',
  pull: false,
  put: false
}

const addMachine = () => {
  console.log('Math.random()*100',  Math.floor(Math.random() * 100))
  machineList.push([])
}

const deleteMachine = () => {
  console.log('deleteMachine')
  machineList.pop()
}

const machineDelete = (id) => {
  console.log('machineDelete', id)
  const index = machineList.findIndex(item => item.length > 0 && item[0].id == id)
  machineList[index] = []
}

const onStart = () => {
  console.log('onStart')
}

const onEnd = () => {
  console.log('onEnd', machineList)
}
</script>

<style scoped lang="scss">
.main {
  position: relative;
  .deviceList {
    position: absolute;
    top: 100px;
    left: 20px;
    .header{
      width: 200px;
      height: 25px;
      background-color: #0004ff;
      color: #fff;
      font-size: 13px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
}
.container {
  width: fit-content;
  height: 200px;
  display: flex;
  align-items: center;
  border: 4px solid #000;
  position: relative;
  .machineBox {
    width: 40px;
    height: 100px;
    position: relative;
    background-color: #ccc;
    border-left: 1px solid #555;
    .machine {
      height: 100%;
      background-color: #7bff00;
    }
    .empty {
      height: 100%;
    }
    .machineDelete {
      display: none;
      position: absolute;
      right: 2px;
      top: 2px;
      color: red;
    }
    .forbid:hover {
      .machineDelete {
        display: block;
      }
    }
  }
  .machineBox:first-of-type {
    border: none
  }
  .operateBox {
    width: 20px;
    height: 100px;
    position: absolute;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    right: -24px;
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
.deviceContainer {
  .device {
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #ddd;
    border-bottom: 1px solid #aaa;
  }
}
</style>