<template>
 <el-card>
  <div class="machineContainer">
    <div class="machineTable">
      <div class="tHead">
        <div class="th"></div>
        <div class="th" v-for="(head, index) in heads" :key="index">{{head}}</div>
      </div>
      <div class="tBody" >
        <div class="order">
          <div class="to" v-for="(o, index) in order" :key="index">{{o}}</div>
        </div>
        <div class="colContainer" @click.right="handleClick">
          <div class="col" v-for="(col, index) in heads" :key="index">
            <template v-for="(td, i) in tablleData[col]" :key="i">
              <draggable
                :id="`${index}-${i}`"
                class="dragTd"
                :list="td"
                :itemKey="item => item.id"
                tag="div"
                :group="td.length > 0 ? groupMachineFill : groupMachineBlank"
                animation="100"
                @start="onStart"
                @end.prevent="onEnd"
              >
                <template #item="{ element }">
                  <div class="content" v-if="element && element.name"> {{ element.name }} </div>
                </template>
              </draggable>
            </template>
          </div>
          <div class="menu" v-if="menu.show" :style="{left: `${menu.left}`, top: `${menu.top}`}">
            <div class="menu_item" @click="addMachine">新增</div>
            <div class="menu_item" @click="deleteMachine">删除</div>
          </div>
        </div>
      </div>
    </div>
  </div>
 </el-card>
</template>

<script lang="ts" setup>
import draggable from "vuedraggable"

const heads = reactive(['A', 'B', 'C', 'D'])
const order = reactive([1, 2, 3, 4])
const groupMachineFill = {
  name: 'MachineFill',
  pull: true, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: false
}
const groupMachineBlank = {
  name: 'MachineBlank',
  pull: false, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: true, 
  // put: (e) => { //拖入
  //   console.log('拖入', e.el.children.length,)
  //   return e.el.children.length < 1
  // }
}
const menu = ref({
  left: '0px',
  top: '0px',
  show: false,
  location: []
})
const tablleData = reactive({
  A: [[],[{
    id: 1,
    name: 'A2'
  }],[{
    id: 2,
    name: 'A3'
  }],[{
    id: 3,
    name: 'A4'
  }],],
  B: [[{
    id: 0,
    name: 'B1'
  }],[{
    id: 1,
    name: 'B2'
  }],[{
    id: 2,
    name: 'B3'
  }],[]],
  C: [[{
    id: 1,
    name: 'C1' 
  }],[{
    id: 2,
    name: 'C2'
  }],[],[{
    id: 3,
    name: 'C3'
  }],],
  D: [[],[],[{
    id: 2,
    name: 'D3'
  }],[{
    id: 3,
    name: 'D4'
  }],]
})
const E = reactive([])
const A = reactive([[],[{
  id: 1,
  name: 'A2'
}],[{
  id: 2,
  name: 'A3'
}],[{
  id: 3,
  name: 'A4'
}],])
const B = reactive([[{
  id: 0,
  name: 'B1'
}],[{
  id: 1,
  name: 'B2'
}],[{
  id: 2,
  name: 'B3'
}],[]])
const C = reactive([[{
  id: 1,
  name: 'C1' 
}],[{
  id: 2,
  name: 'C2'
}],[],[{
  id: 3,
  name: 'C3'
}],])
const D = reactive([[],[],[{
  id: 2,
  name: 'D3'
}],[{
  id: 3,
  name: 'D4'
}],])
// 增加机柜
const addMachine = () => {
  const index = heads[menu.value.location[0]]
  tablleData[index][menu.value.location[1]].push({ id: '888', name: 'test'})
  menu.value.show = false
}
// 删除机柜
const deleteMachine = () => {
  console.log('删除机柜')
  const index = heads[menu.value.location[0]]
  tablleData[index][menu.value.location[1]].pop()
  menu.value.show = false
}
// 处理右击事件
const handleClick = (e) => {
  e.preventDefault()
  const currentId = e.target.id ? e.target.id : e.target.parentNode.id
  menu.value = {
    left: e.layerX + 'px',
    top: e.layerY + 'px',
    show: true,
    location: currentId.split('-')
  }
}

//拖拽开始的事件
const onStart = (e) => {
  console.log("开始拖拽")
}

//拖拽结束的事件
const onEnd = (e) => {
  console.log("结束拖拽", C, e)
}

onMounted(() => {
  document.addEventListener('mousedown',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && menu.value.show && element.className != 'menu_item') {
      menu.value.show = false
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousedown',() => {});
})
</script>

<style lang="scss" scoped>
.machineContainer {
  background-color: #fff;
  // width: 100%;
  // box-sizing: border-box;
  .machineTable {
    display: inline-flex;
    flex-direction: column;
    .tHead {
      display: flex;
      border-right: 1px solid #eee;
      border-bottom: 1px solid #eee;
      box-shadow: 0 2px 10px #eee;
      margin-bottom: 1px;
      .th {
        width: 80px;
        height: 35px;
        background-color: #f9fafa;
        border-left: 1px solid #eee;
        border-top: 1px solid #eee;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
    .tBody {
      display: flex;
      .colContainer {
        display: flex;
        position: relative;
        .dragTd {
          width: 80px;
          height: 35px;
          background-color: #fff;
          border-right: 1px solid #b9eef8;
          border-bottom: 1px solid #b9eef8;
          .content {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #bfa;
          }
        }
        .menu {
          box-sizing: border-box;
          position: absolute;
          padding: 10px 0;
          background-color: #fff;
          box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
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
      
      .order {
        border-left: 1px solid #eee;
        box-shadow: 0 0 10px #eee;
        .to {
          width: 80px;
          height: 35px;
          background-color: #f9fafa;
          // border-radius: 2px;
          border-right: 1px solid #efefef;
          border-bottom: 1px solid #efefef;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
    }
  }
}
// .ghost {
//   border: solid 1px rgb(19, 41, 239);
// }
// .chosenClass {
//   background-color: #f1f1f1;
// }
</style>