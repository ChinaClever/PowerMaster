<template>
  <el-card>
    <div class="dragContainer" @click.right="handleRightClick">
      <el-table class="dragTable" :data="tableData" border style="width: 100%;" :row-style="{background: 'revert'}" >
        <el-table-column fixed type="index" width="80" align="center" :resizable="false" />
        <template v-for="(formItem, index) in formParam" :key="index">
          <el-table-column :label="formItem" width="100" align="center" :resizable="false">
            <template #default="scope">
              <draggable
                :id="`${scope.$index}-${index}`"
                class="dragTd"
                :list="scope.row[formItem]"
                :itemKey="item => item.id"
                tag="div"
                :group="scope.row[formItem].length > 0 ? groupMachineFill : groupMachineBlank"
                animation="100"
                @start="onStart"
                @end.prevent="onEnd"
              >
                <template #item="{ element }">
                  <div class="box" v-if="element && element.name"> {{ element.name }} </div>
                </template>
              </draggable>
            </template>
          </el-table-column>
        </template>
      </el-table>
      <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
        <div class="menu_item" v-if="showMenuAdd" @click="addMachine">新增</div>
        <div class="menu_item" v-else @click="deleteMachine">删除</div>
      </div>
    </div>
  </el-card>
  <layoutForm ref="machineForm" @success="handleChange" />
</template>

<script lang="ts" setup>
import draggable from "vuedraggable";
import layoutForm from './layoutForm.vue'

const machineForm = ref()
const groupMachineFill = {
  name: 'MachineFill',
  pull: true, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: false // 拖入
}
const groupMachineBlank = {
  name: 'MachineBlank',
  pull: false, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: true, // 拖入
}

const operateMenu = ref({
  left: '0px',
  top: '0px',
  show: false,
  lndexX: 0,
  lndexY: 0,
})


// 右击弹出菜单
const handleRightClick = (e) => {
  e.preventDefault()
  const container = e.currentTarget;
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  const currentId = e.target.id ? e.target.id : e.target.parentNode.id
  console.log('handleRightClick', e.target, currentId, offsetX, offsetY)
  const lndexX = currentId.split('-')[1]
  const lndexY = currentId.split('-')[0]
  if (!currentId) return
  operateMenu.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    lndexX,
    lndexY,
  }
}
// 判断是否展示添加菜单项
const showMenuAdd = computed(() => {
  const lndexX = operateMenu.value.lndexX
  const lndexY = operateMenu.value.lndexY
  return !(tableData[lndexY][formParam[lndexX]].length > 0)
})
// 拖拽开始的事件
const onStart = () => {
  console.log('onStssasrt')
}
// 拖拽结束的事件
const onEnd = () => {
  console.log('onsEnd',showMenuAdd.value)
}

const tableData = reactive([
  {
    A: [{ name: 'a1', id: 1}],
    B: [{ name: 'b1', id: 2}],
    C: [],
    D: [{ name: 'd1', id: 3}],
    E: [],
    F: [{ name: 'f1', id: 4}],
    G: [{ name: 'g1', id: 5}],
    H: [{ name: 'h1', id: 6}],
    I: [],
    J: [{ name: 'j1', id: 8}],
    K: [],
    L: [],
    M: [{ name: 'm1', id: 9}],
    N: [{ name: 'n1', id: 10}],
    O: [{ name: 'o1', id: 20}],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [{ name: 'a2', id: 11}],
    B: [{ name: 'b2', id: 22}],
    C: [],
    D: [{ name: 'd2', id: 23}],
    E: [],
    F: [{ name: 'f2', id: 24}],
    G: [{ name: 'g2', id: 25}],
    H: [{ name: 'h2', id: 26}],
    I: [],
    J: [{ name: 'j2', id: 27}],
    K: [],
    L: [],
    M: [{ name: 'm2', id: 28}],
    N: [{ name: 'n2', id: 29}],
    O: [{ name: 'o2', id: 30}],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [{ name: 'a3', id: 31}],
    B: [{ name: 'b3', id: 32}],
    C: [],
    D: [{ name: 'd3', id: 33}],
    E: [],
    F: [{ name: 'f3', id: 34}],
    G: [{ name: 'g3', id: 35}],
    H: [{ name: 'h3', id: 36}],
    I: [],
    J: [{ name: 'j3', id: 37}],
    K: [],
    L: [],
    M: [],
    N: [{ name: 'n3', id: 39}],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [{ name: 'd4', id: 41}],
    E: [{ name: 'e4', id: 42}],
    F: [{ name: 'f4', id: 43}],
    G: [{ name: 'g4', id: 44}],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [{ name: 'a5', id: 51}],
    B: [{ name: 'b5', id: 52}],
    C: [{ name: 'c5', id: 53}],
    D: [{ name: 'd5', id: 54}],
    E: [{ name: 'e5', id: 55}],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [{ name: 'f6', id: 56}],
    G: [{ name: 'g6', id: 57}],
    H: [{ name: 'h6', id: 59}],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [],
    E: [],
    F: [],
    G: [],
    H: [],
    I: [],
    J: [],
    K: [],
    L: [],
    M: [],
    N: [],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
])

const formParam = Object.keys(tableData[0])

onMounted(() => {
  document.addEventListener('mousedown',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousedown',() => {});
})
</script>

<style lang="scss" scoped>
.dragContainer {
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
  .dragTd {
    width: 100%;
    height: 100%;
  }
  .box {
    height: 40px;
    background-color: rgb(12, 255, 44);
  }
}

:deep(.dragTable .hover-row .el-table__cell td) {
  background-color:unset!important;
}
:deep(.dragTable .el-table__cell) {
  padding: 0;
}
:deep(.dragTable .el-table__cell .cell) {
  height: 40px;
  line-height: 40px;
  padding: 0;
}
:deep(.dragTable .el-table__header .el-table__cell) {
  background-color: #ddd;
  box-shadow: 0 1px 0px #ddd;
}
:deep(.dragTable .el-table__body .el-table__row .el-table__cell:nth-of-type(1)) {
  background-color: #ddd;
  box-shadow: 0 1px 0px #ddd;
}
</style>
