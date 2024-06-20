<template>
  <el-card style="margin-top: -5px;margin-bottom: 10px;">
    <div class="toolbar">
      <el-tag class="tag" size="large">机房1</el-tag>
      <div>
        <el-button v-if="!editEnable" @click="editEnable = true" type="primary">编辑</el-button>
        <el-button v-if="editEnable" @click="editEnable = false" plain type="primary">取消</el-button>
        <el-button v-if="editEnable" @click="openSetting" plain type="primary"><Icon :size="16" icon="ep:setting" style="margin-right: 5px" />配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" plain type="primary">保存</el-button>
      </div>
    </div>
  </el-card>
  <el-card>
    <div class="dragContainer" @click.right="handleRightClick">
      <div class="mask" v-if="!editEnable" @click.prevent=""></div>
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
                  <div :class="element.status == 1 ? 'normalDrag' : 'warnDrag'"  v-if="element && element.name"> {{ element.name }} </div>
                </template>
              </draggable>
            </template>
          </el-table-column>
        </template>
      </el-table>
      <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
        <div class="menu_item" v-if="showMenuAdd" @click="addMachine">新增</div>
        <div class="menu_item" v-if="!showMenuAdd" @click="editMachine">编辑</div>
        <div class="menu_item" v-if="!showMenuAdd" @click="deleteMachine">删除</div>
      </div>
    </div>
  </el-card>
  <layoutForm ref="machineForm" @success="handleChange" />
  <Dialog v-model="dialogVisible" title="设置房间的行列数" width="30%">
    <el-form>
      <el-form-item label="行数" label-width="80">
        <el-input-number v-model="roomInfo.row" :min="1" :max="50" @change="handleChange" controls-position="right" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="列数" label-width="80">
        <el-input-number v-model="roomInfo.col" :min="1" :max="50" @change="handleChange" controls-position="right" placeholder="请输入" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="submitSetting">确 定</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import draggable from "vuedraggable";
import layoutForm from './component/layoutForm.vue'

const roomInfo = reactive({
  row: 10,
  col: 10,
})
const dialogVisible = ref(false)
const editEnable = ref(false)
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


const openSetting = () => {
  console.log('openSetting')
  dialogVisible.value = true
}

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
// 增加机柜弹框
const addMachine = () => {
  machineForm.value.open('add')
  operateMenu.value.show = false
}
// 编辑机柜弹框
const editMachine = () => {
  const Y = operateMenu.value.lndexY
  const X = formParam[operateMenu.value.lndexX]
  machineForm.value.open('edit', {...tableData[Y][X][0]})
  operateMenu.value.show = false
}
// 删除机柜
const deleteMachine = () => {
  const Y = operateMenu.value.lndexY
  const X = formParam[operateMenu.value.lndexX]
  console.log('删除机柜',tableData[Y][X])
  tableData[Y][X].splice(0, 1)
  operateMenu.value.show = false
}
// 处理增加/编辑机柜
const handleChange = (data) => {
  console.log('data', data)
  const Y = operateMenu.value.lndexY
  const X = formParam[operateMenu.value.lndexX]
  tableData[Y][X].splice(0, 1, data)
}
// 处理设置提交
const submitSetting = () => {
  const rowNum = roomInfo.row
  const colNum = roomInfo.col
  const data = [] as any
  // 根据设置的宽高来修改数据
  if (tableData.length == 0) {
    console.log('根据设置的宽高来修改数据')
  } else { // 根据设置的宽高来创建数据
    const Obj = {}
    for(let i=0; i < rowNum; i++) {
      Obj[String.fromCharCode(65 + i)] = []
    }
    for(let i=0; i < colNum; i++) {
      data.push(Obj)
    }
    console.log('data', data)
  }
  dialogVisible.value = false
}
// 处理提交保存事件
const handleSubmit = () => {
  console.log('handleSubmit')
}

const tableData = reactive([
  {
    A: [{ name: 'a1', id: 1, status: 1}],
    B: [{ name: 'b1', id: 2, status: 1}],
    C: [],
    D: [{ name: 'd1', id: 3, status: 1}],
    E: [],
    F: [{ name: 'f1', id: 4, status: 1}],
    G: [{ name: 'g1', id: 5, status: 0}],
    H: [{ name: 'h1', id: 6, status: 0}],
    I: [],
    J: [{ name: 'j1', id: 8, status: 1}],
    K: [],
    L: [],
    M: [{ name: 'm1', id: 9, status: 1}],
    N: [{ name: 'n1', id: 10, status: 1}],
    O: [{ name: 'o1', id: 20, status: 1}],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [{ name: 'a2', id: 11, status: 1}],
    B: [{ name: 'b2', id: 22, status: 1}],
    C: [],
    D: [{ name: 'd2', id: 23, status: 1}],
    E: [],
    F: [{ name: 'f2', id: 24, status: 1}],
    G: [{ name: 'g2', id: 25, status: 1}],
    H: [{ name: 'h2', id: 26, status: 1}],
    I: [],
    J: [{ name: 'j2', id: 27, status: 0}],
    K: [],
    L: [],
    M: [{ name: 'm2', id: 28, status: 0}],
    N: [{ name: 'n2', id: 29, status: 1}],
    O: [{ name: 'o2', id: 30, status: 1}],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [{ name: 'a3', id: 31, status: 1}],
    B: [{ name: 'b3', id: 32, status: 1}],
    C: [],
    D: [{ name: 'd3', id: 33, status: 1}],
    E: [],
    F: [{ name: 'f3', id: 34, status: 1}],
    G: [{ name: 'g3', id: 35, status: 1}],
    H: [{ name: 'h3', id: 36, status: 1}],
    I: [],
    J: [{ name: 'j3', id: 37, status: 1}],
    K: [],
    L: [],
    M: [],
    N: [{ name: 'n3', id: 39, status: 1}],
    O: [],
    P: [],
    Q: [],
    R: [],
  },
  {
    A: [],
    B: [],
    C: [],
    D: [{ name: 'd4', id: 41, status: 1}],
    E: [{ name: 'e4', id: 42, status: 1}],
    F: [{ name: 'f4', id: 43, status: 1}],
    G: [{ name: 'g4', id: 44, status: 1}],
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
    A: [{ name: 'a5', id: 51, status: 0}],
    B: [{ name: 'b5', id: 52, status: 0}],
    C: [{ name: 'c5', id: 53, status: 1}],
    D: [{ name: 'd5', id: 54, status: 1}],
    E: [{ name: 'e5', id: 55, status: 1}],
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
    F: [{ name: 'f6', id: 56, status: 1}],
    G: [{ name: 'g6', id: 57, status: 1}],
    H: [{ name: 'h6', id: 59, status: 1}],
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
.toolbar {
  display: flex;
  justify-content: space-between;
}
.dragContainer {
  position: relative;
  .mask {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
    z-index: 999;
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
  .dragTd {
    width: 100%;
    height: 100%;
  }
  .warnDrag {
    // height: 40px;
    background-color: rgb(255, 219, 12);
  }
  .normalDrag {
    // height: 40px;
    background-color: rgb(12, 255, 44);
  }
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-card) {
  margin-top: -5px;
  margin-bottom: 10px;
}
:deep(.el-input-number) {
  width: 100%;
}
:deep(.dragTable .hover-row .el-table__cell td) {
  background-color:unset!important;
}
:deep(.dragTable .el-table__cell) {
  padding: 0;
}
:deep(.dragTable .el-table__cell .cell) {
  height: 50px;
  line-height: 50px;
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
