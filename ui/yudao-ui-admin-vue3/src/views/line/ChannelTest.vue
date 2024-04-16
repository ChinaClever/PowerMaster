<template>
  <div class="main">
    <div class="container" @click="addMachine">
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
            <div class="machine forbid" v-if="element.name">{{element.name}}</div>
            <div class="machine" v-else></div>
          </template>
        </draggable>
      </template>
      <div class="machineBox" ></div>
    </div>
    <div class="deviceContainer">
      <draggable
        :list="deviceList[0]"
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
    </div>
  </div>
</template>

<script setup lang="ts">
import draggable from "vuedraggable"
const machineList = reactive([
  [],[],[],[],[],[],[],[],[],
])
const deviceList = reactive([
  [{
    id: '151',
    name: 'weq'
  }]
])

const groupDevice = {
  name: 'groupCommon',
  pull: 'clone',
  put: true
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

const onStart = () => {
  console.log('onStart')
}

const onEnd = () => {
  console.log('onEnd', machineList)
}
</script>

<style scoped lang="scss">
.container {
  width: fit-content;
  height: 200px;
  display: flex;
  align-items: center;
  border: 4px solid #000;
  .machineBox {
    width: 40px;
    height: 100px;
    background-color: #ccc;
    border-left: 1px solid #555;
  }
  .machineBox:first-of-type {
    border: none
  }
}
.deviceContainer {
  margin-top: 50px;
  .device {
    width: 60px;
    height: 60px;
    background-color: rgb(217, 255, 0);
  }
}
</style>