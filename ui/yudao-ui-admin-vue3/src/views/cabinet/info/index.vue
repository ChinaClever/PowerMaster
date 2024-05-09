<template>
  <div class="master">
    <!-- 左大侧 -->
    <div class="master-left">
      <ContentWrap style="height: calc(100% - 15px)">
        <div v-if="!isCloseNav" class="nav-left">
          <!-- 左侧标题栏 -->
          <div class="navBar">微模块机房</div>
          <!-- 信息展示模式 -->
          <div v-if="switchNav">
            <div class="header">
              <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
              <div class="name">微模块机房</div>
              <div>机房202</div>
            </div>
            <div class="line"></div>
            <div class="status">
              <div class="box">
                <div class="top">
                  <div class="tag"></div>正常
                </div>
                <div class="value"><span class="number">24</span>个</div>
              </div>
              <div class="box">
                <div class="top">
                  <div class="tag empty"></div>空载
                </div>
                <div class="value"><span class="number">1</span>个</div>
              </div>
              <div class="box">
                <div class="top">
                  <div class="tag warn"></div>预警
                </div>
                <div class="value"><span class="number">1</span>个</div>
              </div>
              <div class="box">
                <div class="top">
                  <div class="tag error"></div>故障
                </div>
                <div class="value"><span class="number">0</span>个</div>
              </div>
            </div>
            <div class="line"></div>
            <div class="overview">
              <div class="count">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="info">
                  <div>总电能</div>
                  <div class="value">295.87 kW·h</div>
                </div>
              </div>
              <div class="count">
                <img class="count_img" alt="" src="@/assets/imgs/dh.jpg" />
                <div class="info">
                  <div>今日用电</div>
                  <div class="value">295.87 kW·h</div>
                </div>
              </div>
              <div class="count">
                <img class="count_img" alt="" src="@/assets/imgs/dn.jpg" />
                <div class="info">
                  <div>今日用电</div>
                  <div class="value">295.87 kW·h</div>
                </div>
              </div>
            </div>
          </div>
          <!-- 筛选模式 -->
          <div v-else style="margin-top: 10px">
            <NavTree ref="navTree" @node-click="handleClick" @check="handleCheck" :showSearch="true" :dataList="navList" />
          </div>
        </div>
        <div v-if="!isCloseNav" class="openNavtree" @click.prevent="handleSwitchNav">
          <Icon icon="ep:switch" />切换
        </div>
        <div v-if="!isCloseNav" class="reduce" @click.prevent="isCloseNav = true"><Icon icon="ep:arrow-left" />收起</div>
        <div v-if="isCloseNav" class="expand" @click.prevent="isCloseNav = false"><Icon icon="ep:arrow-right" /><span>展</span><span>开</span></div>
      </ContentWrap>
    </div>
    <!-- 右大侧 -->
    <div class="master-right">
      <ContentWrap>
        <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="68px"
        >
          <el-form-item>
            <template v-for="(status, index) in statusList" :key="index">
              <button type="button" :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index, $event)">{{status.name}}</button>
            </template>
          </el-form-item>
          <div>
            <el-form-item v-show="switchValue"  label="公司名称" prop="company">
              <el-input
                v-model="queryParams.company"
                placeholder="请输入公司名称"
                clearable
                class="!w-160px"
                height="35"
              />
            </el-form-item >
            <el-form-item v-show="switchValue" label="展示列" prop="showCol">
              <el-cascader
                ref="colNode"
                class="!w-230px"
                v-model="defaultOptionsCol"
                @change="cascaderChange"
                :options="optionsCol"
                :props="props"
                :clearable="false"
                collapse-tags />
            </el-form-item>
            <el-form-item>
              <el-button style="margin-left: 12px" v-show="switchValue" @click="getTableData(true)"><Icon icon="ep:search" />搜索</el-button>
              <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button>
            </el-form-item>
          </div>
          <el-form-item style="margin-left: auto">
            <el-button @click="handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
            <el-button @click="handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>
      <ContentWrap v-show="switchValue" style="min-height: 680px">
        <el-table style="width: 100%;" v-loading="loading" :data="listPage" @cell-dblclick="handleDbclick">
          <el-table-column label="位置" min-width="110" align="center">
            <template #default="scope">
              <div>{{scope.row.roomName}}-{{scope.row.cabinetName}}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="110" align="center">
            <template #default="scope">
              <div :style="{color: statusList[scope.row.status].color}">{{statusList[scope.row.status] && statusList[scope.row.status].name}}</div>
            </template>
          </el-table-column>
          <!-- <el-table-column v-if="queryParams.showCol.includes(12)" label="名称" min-width="110" align="center" prop="cabinetName" /> -->
          <el-table-column v-if="queryParams.showCol.includes(1)" label="总视在功率(kVA)" min-width="140" align="center" prop="apparentTotal" />
          <el-table-column v-if="queryParams.showCol.includes(2)" label="总有功功率(kW)" min-width="130" align="center" prop="activeTotal" />
          <el-table-column v-if="queryParams.showCol.includes(3)" label="总电能(kWh)" min-width="110" align="center" prop="eleTotal" />
          <el-table-column v-if="queryParams.showCol.includes(5)" label="A视在功率(kVA)" min-width="140" align="center" prop="apparentA" />
          <el-table-column v-if="queryParams.showCol.includes(6)" label="A有功功率(kW)" min-width="130" align="center" prop="activeA" />
          <el-table-column v-if="queryParams.showCol.includes(7)" label="A电能(kWh)" min-width="110" align="center" prop="eleA" />
          <el-table-column v-if="queryParams.showCol.includes(9)" label="B视在功率(kVA)" min-width="140" align="center" prop="apparentB" />
          <el-table-column v-if="queryParams.showCol.includes(10)" label="B有功功率(kW)" min-width="130" align="center" prop="activeB" />
          <el-table-column v-if="queryParams.showCol.includes(11)" label="B电能(kWh)" min-width="110" align="center" prop="eleB" />
          <el-table-column v-if="queryParams.showCol.includes(13)" label="负载比(%)" min-width="110" align="center" prop="loadFactor" />
          <el-table-column v-if="queryParams.showCol.includes(12)" label="所属公司" min-width="110" align="center" prop="company" />
          <el-table-column v-if="queryParams.showCol.includes(14)" label="A,B占比" align="center" prop="abzb">
            <template #default="scope">
              <div v-if="scope.row.abzb == '-'">-</div>
              <div v-else class="progressContainer">
                <div class="progress">
                  <div class="left" :style="`flex: ${scope.row.abzb}`">A:{{scope.row.abzb}}</div>
                  <div class="right" :style="`flex: ${100 - scope.row.abzb}`">B:{{100 - scope.row.abzb}}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" align="center" width="180px">
            <template #default="scope">
              <el-button
                type="primary"
                @click="openForm('edit', scope.row.cabinet_key)">编辑
              </el-button>
              <el-button
                type="danger"
                @click="handleDelete(scope.row.cabinet_key)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          :total="queryParams.pageTotal"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData(false)"
        />
      </ContentWrap>
      <ContentWrap v-show="!switchValue">
        <div class="arrayContainer">
          <div class="arrayItem" v-for="item in listPage" :key="item.id" @dblclick="handleArrayDbclick(item)">
            <div class="content">
              <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div>
              <div class="info">
                <div>视在功率：{{item.apparentTotal}}KVA</div>
                <div>有功功率：{{item.activeTotal}}KW</div>
                <div>负载率：{{item.loadFactor}}</div>
                <!-- <div>电能：50kWh</div> -->
              </div>
            </div>
            <div class="room">{{item.roomName}}-{{item.cabinetName}}</div>
            <div v-if="item.status == 0" class="status-empty">空载</div>
            <div v-if="item.status == 1" class="status-normal">正常</div>
            <div v-if="item.status == 2" class="status-warn">预警</div>
            <div v-if="item.status == 3" class="status-error">故障</div>
            <div v-if="item.status == 4" class="status-unbound">未绑定</div>
            <div v-if="item.status == 6" class="status-offline">离线</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
        </div>
        <Pagination
          :total="queryParams.pageTotal"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          @pagination="getTableData"
        />
      </ContentWrap>
    </div>
  </div>

  <!-- 添加或修改用户对话框 -->
  <MachineForm ref="machineForm" @success="saveMachine" />
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { object } from 'vue-types';
import MachineForm from './component/MachineForm.vue'
import { CabinetApi } from '@/api/cabinet/info'
import { fa } from 'node_modules/element-plus/es/locale';
// import MyButton from '@/components/MyButton/MyButton.vue';

const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗
const machineForm = ref() // 机柜表单组件
const navTree = ref() // 导航树组件
const colNode = ref() // 展示列组件
const loading = ref(false)
const isCloseNav = ref(false) // 左侧导航是否收起
const switchNav = ref(false) //false: 导航树 true：微模块展示
const switchValue = ref(0) // 0:阵列 1：表格
const listPage = ref<any>([]) // 表格数据
const navList = ref([]) // 左侧导航列表数据
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组
const defaultOptionsCol = reactive([1, 2, 3, 12, 13, 14])
const optionsCol = reactive([{
  value: 0,
  label: '总',
  children: [{
    value: 1,
    label: '总AB视在功率'
  }, {
    value: 2,
    label: '总AB有功功率'
  }, {
    value: 3,
    label: '总AB电能'
  }],
},{
  value: 4,
  label: 'A组',
  children: [{
    value: 5,
    label: 'A视在功率'
  }, {
    value: 6,
    label: 'A有功功率'
  }, {
    value: 7,
    label: 'A电能'
  }],
},{
  value: 8,
  label: 'B组',
  children: [{
    value: 9,
    label: 'B视在功率'
  }, {
    value: 10,
    label: 'B有功功率'
  }, {
    value: 11,
    label: 'B电能'
  }],
},{
  value: 12,
  label: '所属公司'
},{
  value: 13,
  label: '负载比'
},{
  value: 14,
  label: 'AB占比'
}
])
const queryParams = reactive({
  company: undefined,
  showCol: [1, 2, 3, 12, 13, 14] as number[],
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})
const statusList = reactive([
  {
    name: '空载',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
  {
    name: '正常',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '预警',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#ffc402'
  },
  {
    name: '故障',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  },
  {
    name: '未绑定',
    selected: true,
    value: 4,
    cssClass: 'btn_unbound',
    activeClass: 'btn_unbound unbound',
    color: '#05ebfc'
  },
  {
    name: '离线',
    selected: true,
    value: 5,
    cssClass: 'btn_offline',
    activeClass: 'btn_offline offline',
    color: '#7700ff'
  },
])
const props = { multiple: true }

// 接口获取机柜列表
const getTableData = async(reset = false) => {
  loading.value = true
  if (reset) queryParams.pageNo = 1
  const status =  statusList.filter(item => item.selected)
  try {
    const res = await CabinetApi.getCabinetInfo({
      pageNo: queryParams.pageNo,
      pageSize: queryParams.pageSize,
      cabinetIds: cabinetIds.value,
      // roomId: null,
      runStatus: status.map(item => item.value),
      pduBox: 0,
      company: queryParams.company
    })
    console.log('res', res)
    if (res.list) {
      const list = res.list.map(item => {
        const tableItem = {
          company: item.company,
          cabinet_key: item.cabinet_key,
          cabinetName: item.cabinet_name,
          roomName: item.room_name,
          status: item.status,
          apparentTotal: item.cabinet_power.total_data.pow_apparent.toFixed(3),
          apparentA: item.cabinet_power.path_a ? item.cabinet_power.path_a.pow_apparent.toFixed(3) : '-',
          apparentB: item.cabinet_power.path_b ? item.cabinet_power.path_b.pow_apparent.toFixed(3) : '-',
          activeTotal: item.cabinet_power.total_data.pow_active.toFixed(3),
          activeA: item.cabinet_power.path_a ? item.cabinet_power.path_a.pow_active.toFixed(3) : '-',
          activeB: item.cabinet_power.path_b ? item.cabinet_power.path_b.pow_active.toFixed(3) : '-',
          eleTotal: item.cabinet_power.total_data.ele_active.toFixed(1),
          eleA: item.cabinet_power.path_a ? item.cabinet_power.path_a.ele_active.toFixed(1) : '-',
          eleB: item.cabinet_power.path_b ? item.cabinet_power.path_b.ele_active.toFixed(1) : '-',
          loadFactor: Math.ceil(item.load_factor),
          abzb: '-' as number | string
        }
        if (item.cabinet_power.path_a && item.cabinet_power.path_b) {
          if (item.cabinet_power.path_a.pow_apparent == 0) tableItem.abzb = 0
          else tableItem.abzb = Math.ceil(Number((item.cabinet_power.path_a.pow_apparent / item.cabinet_power.total_data.pow_apparent).toFixed(0)))
        }
        return tableItem
      })
      listPage.value = list
      queryParams.pageTotal = res.total
      console.log('listPage', listPage.value)
    }
  } finally {
    loading.value = false
  }
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  console.log('接口获取机房导航列表', res)
  const ids = [] as number[]
  navList.value = res
  if (res && res.length > 0) {
    const room = res[0]
    room.children.forEach(child => {
      if (child.type == 3) {
        ids.push(child.id)
      }
      if(child.children.length > 0) {
        child.children.forEach(son => {
          ids.push(son.id)
        })
      }
    })
    navTree.value.initCheck([room.unique])
  }
  cabinetIds.value = ids
  getTableData(false)
}

// 保存机柜修改/删除
const saveMachine = async() => {
  getNavList()
}

// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  if (value == 0) { // 阵列
    queryParams.pageSize = 24
  } else {
    queryParams.pageSize = 10
  }
  getTableData(true)
}

// 处理切换按钮点击事件
const handleSwitchNav = () => {
  switchNav.value = !switchNav.value
}

//处理表格双击事件
const handleDbclick = (e) => {
  console.log('处理表格双击事件', e, e.id)
  push('/cabinet/cab/detail')
}

// 处理阵列双击事件
const handleArrayDbclick = (data) => {
  console.log('处理阵列双击事件', data)
  openForm('add')
}

// 处理状态选择事件
const handleSelectStatus = (index, event) => {
  console.log('处理状态选择事件', index, event)
  statusList[index].selected = !statusList[index].selected
  getTableData()
}

// 跳转详情页
const toMachineDetail = () => {
  console.log('toMachineDetail!')
  push('/cabinet/cab/detail')
}

const handleClick = (row) => {
  console.log('Button clicked!', row);
}

const handleCheck = (row) => {
  console.log('handleCheck!', row);
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids
  getTableData(true)
}

// 打开 编辑/添加 表单弹窗
const openForm = async(type: string, key?: string) => {
  if (type == 'add') {
    machineForm.value.open(type)
  } else if (type == 'edit' && key) {
    const id = key.split('-')[1]
    try {
      loading.value = true
      const res = await CabinetApi.getCabinetInfoItem({id})
      console.log('res', res)
      machineForm.value.open(type, res)
    } finally {
      loading.value =false
    }
  }
}

// 处理删除事件
const handleDelete = async (key: string) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    await CabinetApi.deleteCabinetInfo({
      id: key.split('-')[1]
    })
    message.success('删除成功')
    // 刷新列表
    await getNavList()
  } catch (error) {
    console.log(error)
  }
}

// 展示列选择处理事件
const cascaderChange = (row) => {
  const checkedNodes = colNode.value.getCheckedNodes(true)
  queryParams.showCol = checkedNodes.map(item => item.value)
}

onBeforeMount(() => {
  getNavList()
})

</script>

<style scoped lang="scss">
.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      // width: 100%;
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      color: #777777;
      cursor: pointer;
      font-size: 13px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
  }
}
.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  width: 55px;
  height: 32px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  &:hover {
    color: #7bc25a;
  }
}
.btn_normal {
  border: 1px solid #3bbb00;
  background-color: #fff;
}
.normal {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_empty {
  border: 1px solid #aaa;
  background-color: #fff;
}
.empty {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #ffc402;
  background-color: #fff;
}
.warn {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #fa3333;
  background-color: #fff;
}
.error {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_unbound {
  border: 1px solid #05ebfc;
  background-color: #fff;
}
.unbound {
  background-color: #05ebfc;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_offline {
  border: 1px solid #7700ff;
  background-color: #fff;
}
.offline {
  background-color: #7700ff;
  color: #fff;
  &:hover {
    color: #fff;
  }
}

.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);
      // border-right: 1px solid #000;
    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
  }
}
.arrayContainer {
  height: 600px;
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .arrayItem {
    width: 25%;
    height: 120px;
    font-size: 13px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 36px;
    position: relative;
    .content {
      display: flex;
      align-items: center;
      line-height: 1.7;
      .icon {
        width: 30px;
        height: 30px;
        margin: 0 28px;
      }
    }
    .room {
      position: absolute;
      left: 8px;
      top: 8px;
    }
    .status-empty {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #ccc;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .status-normal {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #3bbb00;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .status-warn {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #ffc402;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .status-error {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fa3333;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .status-unbound {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #05ebfc;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .status-offline {
      width: 40px;
      height: 20px;
      font-size: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #7700ff;
      color: #fff;
      position: absolute;
      right: 8px;
      top: 8px;
    }
    .detail {
      width: 40px;
      height: 25px;
      cursor: pointer;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 8px;
      bottom: 8px;
    }
  }
}
:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
}
</style>