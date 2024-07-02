<template>
  <el-card style="margin-top: -5px;margin-bottom: 10px;">
    <div class="toolbar">
      <el-tag class="tag" size="large">机房1</el-tag>
      <div>
        <el-button v-if="!editEnable" @click="editEnable = true" type="primary">编辑</el-button>
        <el-button v-if="editEnable" @click="handleEdit" plain type="primary">取消</el-button>
        <el-button v-if="editEnable" @click="openSetting" plain type="primary"><Icon :size="16" icon="ep:setting" style="margin-right: 5px" />配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" plain type="primary">保存</el-button>
      </div>
    </div>
  </el-card>
  <el-card>
    <div class="dragContainer" v-loading="loading" @click.right="handleRightClick" style="height: calc(100vh - 220px)">
      <!-- <div class="mask" v-if="!editEnable" @click.prevent=""></div> -->
      <el-table class="dragTable" v-if="tableData.length > 0"  :data="tableData" border style="width: 100%;height: 100%" :row-style="{background: 'revert'}" :span-method="arraySpanMethod" >
        <el-table-column fixed type="index" width="60" align="center" :resizable="false" />
        <template v-for="(formItem, index) in formParam" :key="index">
          <el-table-column :label="formItem" min-width="60" align="center" :resizable="false">
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
                  <div class="normalDrag"  v-if="element && element.type == 2">{{element.name}}</div>
                  <div v-else-if="element.type == 1" :class="element.direction == '1' ? 'dragChild' : 'dragChildCol'">
                    <template v-if="element.cabinetList.length > 0">
                      <div :class="item.cabinetName ? 'dragSon fill' : 'dragSon'" v-for="(item, i) in element.cabinetList" :key="i">{{item.cabinetName}}</div>
                    </template>
                    <template v-else>
                      <div class="dragSon" v-for="item in element.amount" :key="item">{{item}}</div>
                    </template>
                  </div>
                </template>
              </draggable>
            </template>
          </el-table-column>
        </template>
      </el-table>
      <el-empty v-if="loading == false && tableData.length == 0" style="height: 100%" description="机房暂未配置，请先编辑配置" />
      <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
        <div class="menu_item" v-if="showMenuAdd" @click="addMachine">新增</div>
        <div class="menu_item" v-if="!showMenuAdd" @click="editMachine">编辑</div>
        <div class="menu_item" v-if="!showMenuAdd" @click="toDetail">查看</div>
        <div class="menu_item" v-if="!showMenuAdd" @click="deleteMachine">删除</div>
      </div>
    </div>
  </el-card>
  <layoutForm ref="machineForm" @success="handleChange" />
  <Dialog v-model="dialogVisible" title="设置房间的行列数" width="30%">
    <el-form>
      <el-form-item label="行数" label-width="80">
        <el-input-number v-model="rowColInfo.row" :min="1" :max="100" controls-position="right" placeholder="请输入" />
      </el-form-item>
      <el-form-item label="列数" label-width="80">
        <el-input-number v-model="rowColInfo.col" :min="1" :max="70" controls-position="right" placeholder="请输入" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { MachineRoomApi } from '@/api/cabinet/room'

const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗

const loading = ref(false)
const movingInfo = ref<any>({})
const rowColInfo = reactive({
  row: 14, // 行
  col: 18, // 列
})
const tableData = ref<any>([])
// ([
//   {
//     A: [{ name: 'a1', id: 1, status: 1, type: '2'}],
//     B: [{ name: 'b1', id: 2, status: 1, type: '2'}],
//     C: [],
//     D: [{ name: 'd1', id: 3, status: 1, type: '2'}],
//     E: [],
//     F: [{ name: 'f1', id: 4, status: 1, type: '2'}],
//     G: [{ name: 'g1', id: 5, status: 1, type: '2'}],
//     H: [{ name: 'h1', id: 6, status: 1, type: '2'}],
//     I: [],
//     J: [{ name: 'j1', id: 8, status: 1, type: '2'}],
//     K: [],
//     L: [],
//     M: [{ name: 'm1', id: 9, status: 1, type: '2'}],
//     N: [{ name: 'n1', id: 10, status: 1, type: '2'}],
//     O: [{ name: 'o1', id: 20, status: 1, type: '2'}],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [{ name: 'a2', id: 11, status: 1, type: '2'}],
//     B: [{ name: 'b2', id: 22, status: 1, type: '2'}],
//     C: [],
//     D: [{ name: 'd2', id: 23, status: 1, type: '2'}],
//     E: [],
//     F: [{ name: 'f2', id: 24, status: 1, type: '2'}],
//     G: [{ name: 'g2', id: 25, status: 1, type: '2'}],
//     H: [{ name: 'h2', id: 26, status: 1, type: '2'}],
//     I: [],
//     J: [{ name: 'j2', id: 27, status: 1, type: '2'}],
//     K: [],
//     L: [],
//     M: [{ name: 'm2', id: 28, status: 1, type: '2'}],
//     N: [{ name: 'n2', id: 29, status: 1, type: '2'}],
//     O: [{ name: 'o2', id: 30, status: 1, type: '2'}],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [{ name: 'a3', id: 31, status: 1, type: '2'}],
//     B: [{ name: 'b3', id: 32, status: 1, type: '2'}],
//     C: [],
//     D: [{ name: 'd3', id: 33, status: 1, type: '2'}],
//     E: [],
//     F: [{ name: 'f3', id: 34, status: 1, type: '2'}],
//     G: [{ name: 'g3', id: 35, status: 1, type: '2'}],
//     H: [{ name: 'h3', id: 36, status: 1, type: '2'}],
//     I: [],
//     J: [{ name: 'j3', id: 37, status: 1, type: '2'}],
//     K: [],
//     L: [],
//     M: [],
//     N: [{ name: 'n3', id: 39, status: 1, type: '2'}],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [{ name: 'd4', id: 41, status: 1, type: '2'}],
//     E: [{ name: 'e4', id: 42, status: 1, type: '2'}],
//     F: [{ name: 'f4', id: 43, status: 1, type: '2'}],
//     G: [{ name: 'g4', id: 44, status: 1, type: '2'}],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [{ name: 'a5', id: 51, status: 1, type: '2'}],
//     B: [{ name: 'b5', id: 52, status: 1, type: '2'}],
//     C: [{ name: 'c5', id: 53, status: 1, type: '2'}],
//     D: [{ name: 'd5', id: 54, status: 1, type: '2'}],
//     E: [{ name: 'e5', id: 55, status: 1, type: '2'}],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [{ name: 'f6', id: 56, status: 1, type: '2'}],
//     G: [{ name: 'g6', id: 57, status: 1, type: '2'}],
//     H: [{ name: 'h6', id: 59, status: 1, type: '2'}],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
//   {
//     A: [],
//     B: [],
//     C: [],
//     D: [],
//     E: [],
//     F: [],
//     G: [],
//     H: [],
//     I: [],
//     J: [],
//     K: [],
//     L: [],
//     M: [],
//     N: [],
//     O: [],
//     P: [],
//     Q: [],
//     R: [],
//   },
// ])

const dialogVisible = ref(false)
const editEnable = ref(false)
const tableHeight = ref(0)
const machineForm = ref()
const groupMachineFill = {
  name: 'MachineFill',
  pull: () => {
    return editEnable.value
  }, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: false // 拖入
}
const groupMachineBlank = {
  name: 'MachineBlank',
  pull: false, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: (event) => {
    console.log('event', event.el.id)
    const moveBox = movingInfo.value
    if (editEnable.value && moveBox.type == 1 && moveBox.amount > 1) {
      if (moveBox.direction == 1) {
        const X = +event.el.id.split('-')[1]
        if (X + moveBox.amount > rowColInfo.col) return false
      } else {
        const Y = +event.el.id.split('-')[0]
        if (Y + moveBox.amount > rowColInfo.row) return false
      }
    }
    return editEnable.value
  }, // 拖入
}

const operateMenu = ref({
  left: '0px',
  top: '0px',
  show: false,
  lndexX: 0,
  lndexY: 0,
  maxlndexX: 0,
  maxlndexY: 0,
})

const getRoomInfo = async() => {
  loading.value = true
  try {
    const res = await MachineRoomApi.getRoomDetail({id: 4})
    console.log('res', res)
    const data = [] as any
    const Obj = {}
    rowColInfo.col = res.xLength
    rowColInfo.row = res.yLength
    for(let i=0; i < res.xLength; i++) {
      Obj[getTableColCharCode(i)] = []
    }
    for(let i=0; i < res.yLength; i++) {
      data.push(JSON.parse(JSON.stringify(Obj)))
    }
    res.aisleList.forEach(item => {
      console.log('------', item)
      for(let i=0; i < item.length; i++) {
        const dataItem =  {
          id: item.id,
          name: item.aisleName,
          direction: item.direction == 'x' ? 1 : 2,
          type: 1,
          amount: item.cabinetList.length,
          cabinetList: item.cabinetList,
          first: false,
          originAmount: item.cabinetList.length,
          originDirection: item.direction == 'x' ? 1 : 2,
        }
        if (i == 0) dataItem.first = true
        // {type: '1', name: '啊大苏打', direction: '1', amount: 1, status: 1,first: true, id: '', originAmount: 1, originDirection: '1'}
        if (dataItem.direction == 1) {
          data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, dataItem)
        } else {
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, dataItem)
        }
      }
    })
    res.cabinetList.forEach(item => {
      data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, {...item, name: item.cabinetName, type: 2})
    })
    console.log('data', data)
    tableData.value = data
  } finally {
    loading.value = false
  }
}

const openSetting = () => {
  dialogVisible.value = true
}

const arraySpanMethod = ({
  row,
  columnIndex,
}) => {
  if (columnIndex > 0) {
    const td = row[getTableColCharCode(columnIndex - 1)]
    const tdData = td[0]
    if (tdData && tdData.type && tdData.type == 1) { // 如果是柜列
      console.log('===========', td)
      if (tdData.first) { // 如果是柜列中开头第一个  合并单元格
        if (tdData.direction == 1) { // 横向
          return [1, tdData.amount]
        } else { // 纵向
          return [tdData.amount, 1]
        }
      } else { // 如果不是柜列中开头第一个 该单元格不显示
        return [0, 0]
      }
    } 
  }
  return [1, 1]
}

// 右击弹出菜单
const handleRightClick = (e) => {
  e.preventDefault()
  if (!editEnable.value) return
  const container = e.currentTarget;
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  const currentId = e.target.id ? e.target.id : (e.target.parentNode.id ? e.target.parentNode.id :  e.target.parentNode.parentNode.id)
  console.log('handleRightClick', e.target, currentId, offsetX, offsetY)
  const lndexX = currentId.split('-')[1]
  const lndexY = currentId.split('-')[0]
  if (!currentId) return
  operateMenu.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    lndexX, // 当前列
    lndexY, // 当前行
    maxlndexX: rowColInfo.col - lndexX,
    maxlndexY: rowColInfo.row - lndexY,
  }
  console.log('operateMenu', operateMenu.value)
}
// 判断是否展示添加菜单项
const showMenuAdd = computed(() => {
  const lndexX = operateMenu.value.lndexX
  const lndexY = operateMenu.value.lndexY
  console.log('tableData.value[lndexY][formParam.value[lndexX]]', tableData.value[lndexY][formParam.value[lndexX]], lndexX, lndexY)
  return !(tableData.value[lndexY][formParam.value[lndexX]].length > 0)
})
// 拖拽开始的事件
const onStart = ({from}) => {
  const X = from.id.split('-')[1]
  const Y = from.id.split('-')[0]
  const target = tableData.value[Y][formParam.value[X]][0]
  if (target.type == 1) movingInfo.value = target
  console.log('onStssasrt', target)
}
// 拖拽结束的事件
const onEnd = ({from, to}) => {
  console.log('onsEnd',tableData.value, from, to, from.id, to.id)
  if (from.id != to.id) { // 发生移动才处理
    const X = to.id.split('-')[1]
    const Y = to.id.split('-')[0]
    const targetTo = tableData.value[Y][formParam.value[X]][0]
    console.log('value*******', targetTo)
    if (targetTo.type == 1 && targetTo.amount > 1) {
      const X = +from.id.split('-')[1]
      const Y = +from.id.split('-')[0]
      for (let i=  1; i < targetTo.amount; i++) {
        if (targetTo.direction == 1) {
          tableData.value[Y][formParam.value[X+i]] = []
        } else {
          tableData.value[Y+i][formParam.value[X]] = []
        }
      }
    }
  }
}
// 增加机柜弹框
const addMachine = () => {
  machineForm.value.open('add', null, operateMenu.value)
  operateMenu.value.show = false
}
// 编辑机柜弹框
const editMachine = () => {
  const Y = operateMenu.value.lndexY
  const X = formParam.value[operateMenu.value.lndexX]
  machineForm.value.open('edit', {...tableData.value[Y][X][0]}, operateMenu.value)
  operateMenu.value.show = false
}
// 查看机柜/柜列
const toDetail = () => {
  const Y = operateMenu.value.lndexY
  const X = formParam.value[operateMenu.value.lndexX]
  const target = tableData.value[Y][X][0]
  if (target.type == 1) {
    push('/arrayCabinet/topology')
  } else {
    push('/cabinet/cab/detail')
  }
}
// 删除机柜
const deleteMachine = () => {
  const Y = operateMenu.value.lndexY
  const X = formParam.value[operateMenu.value.lndexX]
  const target = JSON.parse(JSON.stringify(tableData.value[Y][X][0])) // 要删除的目标
  console.log('删除机柜',tableData.value[Y][X], target)
  if (target.type && target.type == 1) {
    for (let i = 0; i < target.originAmount; i++) {
      if (target.direction == 1) {
        // const charCode = X.charCodeAt(0) + i
        console.log('String.fromCharCode(charCode)', operateMenu.value.lndexX, operateMenu.value.lndexX+i)
        tableData.value[Y][formParam.value[+operateMenu.value.lndexX + i]].splice(0, 1)
      } else {
        tableData.value[+Y + i][X].splice(0, 1)
      }
    }
  } else {
    tableData.value[Y][X].splice(0, 1)
  }
  operateMenu.value.show = false
}
// 处理增加/编辑机柜
const handleChange = (data) => {
  console.log('处理增加/编辑机柜+++++', data)
  const X = formParam.value[operateMenu.value.lndexX] // 当前机柜/机柜列所处列
  const Y = operateMenu.value.lndexY // 当前机柜/机柜列所处行
  console.log('当前机柜/机柜列所处列', X, Y)
  if (data.originDirection && data.originDirection != data.direction) deleteMachine() // 如果方向发生改变则先把原来的删除 再去新增
  tableData.value[Y][X].splice(0, 1, {...data, first: true, originAmount: data.amount, originDirection: data.direction})
  if (data.type == 1 && data.amount > 1) { // 类型是机柜列并且机柜数量大于1
    let length = 0
    if (data.originAmount && (data.originAmount - data.amount) > 0) { // data有origin说明是编辑  并且编辑后的机柜数少于原来的机柜数  则要处理的数量就是原来的机柜树
      length = data.originAmount
    } else {
      length = data.amount
    }
    for(let i = 1; i < length; i++ ) {
      if (data.direction == 1) { // 横向机柜列
        // const charCode = X.charCodeAt(0) + i
        if (i >= data.amount) {
          tableData.value[Y][formParam.value[+operateMenu.value.lndexX + i]] = []
        } else {
          tableData.value[Y][formParam.value[+operateMenu.value.lndexX + i]].splice(0, 1, {...data, first: false})
        }
      } else { // 纵向机柜列
        if (i >= data.amount) {
          tableData.value[+Y + i][X] = []
        } else {
          tableData.value[+Y + i][X].splice(0, 1, {...data, first: false})
        }
      }
    }
  }
}
// 处理设置提交
const submitSetting = () => {
  const rowNum = rowColInfo.row // 设置的行数
  const colNum = rowColInfo.col // 设置的列数
  const data = [] as any
  // 根据设置的宽高来修改数据
  const tableDataCopy= tableData.value
  if (tableDataCopy.length > 0) { // 如果是表格已经有数据了  根据设置的宽高修改数据
    const keyLength =  formParam.value.length // 原表格的列数
    if (colNum < keyLength || rowNum < tableDataCopy.length) { // 如果配置的行/列小于原先的行/列  判断要删除的数据中是否存在机柜/柜列  存在的话不允许删除并提示
      for(let i=0; i < tableDataCopy.length; i++) {
        for(let j=0; j < keyLength; j++) {
          if (i >= rowNum || j >= colNum) {
            if (tableDataCopy[i][getTableColCharCode(j)][0]) {
              ElMessageBox.alert('所配置的行列会删除原先的数据，请重新配置', '提示', {
                confirmButtonText: '确认',
              })
              rowColInfo.row = tableDataCopy.length
              rowColInfo.col = keyLength
              return
            }
          }
        }
      }
    }
    for(let i=0; i < rowNum; i++) {
      data.push({})
      for(let j=0; j < colNum; j++) {
        if (j >= keyLength || i >= tableDataCopy.length) { // 如果设置的行/列大于原先的  则赋空值
          data[i][getTableColCharCode(j)] = []
        } else { // 如果没有大于原先的  则赋原本表格相应的值
          data[i][getTableColCharCode(j)] = tableData.value[i][getTableColCharCode(j)]
        }
      }
    }
    // console.log('根据设置的宽高来修改数据', data)
  } else { // 根据设置的宽高来创建数据
    const Obj = {}
    for(let i=0; i < colNum; i++) {
      Obj[getTableColCharCode(i)] = []
    }
    for(let i=0; i < rowNum; i++) {
      data.push(JSON.parse(JSON.stringify(Obj)))
    }
  }
  tableData.value = data
  dialogVisible.value = false
}
// 获取表格列label字符
const getTableColCharCode = (num):string => {
  if (num < 26) {
    return String.fromCharCode(65 + num)
  } else if (num < 52) {
    return 'A ' + String.fromCharCode(65 -26 + num)
  } else {  // 列数最多70
    return 'B ' + String.fromCharCode(65 -52 + num)
  }
}
// 处理提交保存事件
const handleSubmit = () => {
  const aisleList = [] as any
  const cabinetList = [] as any
  for(let i = 0; i < rowColInfo.row; i++) {
    for(let j = 0; j < rowColInfo.col; j++) {
      const target = tableData.value[i][getTableColCharCode(j)][0]
      if (target && target.type == 1 && target.first) {
        aisleList.push({
          id: target.id,
          aisleName: target.name,
          xCoordinate: j + 1,
          yCoordinate: i + 1,
          direction: target.direction == 1 ? 'x' : 'y',
          length: target.amount
        })
      } else if (target && target.type == 2) {
        cabinetList.push({
          id: target.id,
          cabinetName: target.name,
          xCoordinate: j + 1,
          yCoordinate: i + 1,
        })
      }
    }
  }
  try {
    loading.value = true
    const res = MachineRoomApi.saveRoomDetail({
      id: 4,
      roomName: 'CES-JF',
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
      aisleList,
      cabinetList
    })
    console.log('aisleList...', res)
    editEnable.value = false
    message.success('保存成功！')
  } finally {
    loading.value = false
  }
}

const handleEdit = () => {
  editEnable.value = false
  getRoomInfo()
}

// const formParam = Object.keys(tableData[0])
const formParam = computed(() => {
  return Object.keys(tableData.value[0])
})

getRoomInfo()

onMounted(() => {
  document.addEventListener('mousedown', (event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item') {
      operateMenu.value.show = false
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousedown',() => {})
  window.onresize = null
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
    height: 50%;
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
    min-height: 40px;
    .dragChild {
      width: 100%;
      height: 100%;
      min-height: 40px;
      box-sizing: border-box;
      display: flex;
      border: 1px solid #000;
      // align-items: center;
      .dragSon {
        height: 100%;
        min-height: 40px;
        flex: 1;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #effaff;
        border-right: 1px solid #bed1ff;
      }
      .fill {
        background-color: #0cff2c;
      }
    }
    .dragChildCol {
      width: 100%;
      height: 100%;
      min-height: 40px;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      border: 1px solid #000;
      .dragSon {
        flex: 1;
        min-height: 40px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #effaff;
        border-bottom: 1px solid #bed1ff;
      }
      .fill {
        background-color: #0cff2c;
      }
    }
  }
  .warnDrag {
    min-height: 40px;
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    // height: 40px;
    background-color: rgb(255, 219, 12);
  }
  .normalDrag {
    min-height: 40px;
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    // height: 40px;
    background-color: rgb(12, 255, 44);
    box-sizing: border-box;
    border-right: 1px solid #ebeef5;
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
  width: 100%;
  height: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  & > div {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
:deep(.dragTable .el-table__header .el-table__cell) {
  background-color: #ddd;
  box-shadow: 0 1px 0px #ddd;
}
:deep(.dragTable .el-table__body) {
  height: 100%;
}
:deep(.dragTable .el-table__body .el-table__row .el-table__cell:nth-of-type(1)) {
  background-color: #ddd;
  box-shadow: 0 1px 0px #ddd;
}
</style>
