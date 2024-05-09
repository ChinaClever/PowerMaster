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
              <button :class="status.selected ? status.activeClass : status.cssClass" @click.prevent="handleSelectStatus(index)">{{status.name}}</button>
            </template>
          </el-form-item>
          <div>
            <el-form-item v-show="switchValue"  label="公司名称" prop="username">
              <el-input
                v-model="queryParams.username"
                placeholder="请输入公司名称"
                clearable
                class="!w-160px"
                height="35"
              />
            </el-form-item>
            <el-form-item>
              <el-button style="margin-left: 12px" v-show="switchValue" ><Icon icon="ep:search" />搜索</el-button>
              <!-- <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button> -->
            </el-form-item>
          </div>
          <el-form-item style="margin-left: auto">
            <el-button @click="handleSwitchModal(0)" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
            <el-button @click="handleSwitchModal(1)" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>
      <ContentWrap v-show="switchValue" style="min-height: 680px">
        <!-- <el-table style="width: 100%;" v-loading="loading" :data="listPage" @cell-dblclick="handleDbclick">
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
          <el-table-column v-if="queryParams.showCol.includes(12)" label="名称" min-width="110" align="center" prop="cabinetName" />
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
        /> -->
        <el-table style="width: 100%;" v-loading="loading" :data="listPage" >
          <el-table-column label="位置" min-width="110" align="center" prop="local" />
          <el-table-column label="负载率" min-width="110" align="center" prop="loadPrecent" />
          <el-table-column label="总视在功率" min-width="110" align="center" prop="zszgl" />
          <el-table-column label="A路视在功率" min-width="110" align="center" prop="aszgl" />
          <el-table-column label="B路视在功率" min-width="110" align="center" prop="bszgl" />
          <el-table-column label="电力容量" min-width="110" align="center" prop="dlrl" />
          <el-table-column label="更新时间" min-width="110" align="center" prop="gxsj" />
        </el-table>
      </ContentWrap>
      <ContentWrap v-show="!switchValue" style="min-height: 680px">
        <div class="loadContainer">
          <div class="loadItem">
            <div class="content">
              <div class="info">
                <div>总视在功率：0.153KVA</div>
                <div>A路视在功率：0.063KVA</div>
                <div>B路视在功率：0.112KVA</div>
                <div>电力容量：50p</div>
              </div>
              <div class="waterPoloBox">
                <LiquidBall />
              </div>
              <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
            </div>
            <div class="room">机房1-机柜1</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
          <div class="loadItem">
            <div class="content">
              <div class="info">
                <div>总视在功率：0.153KVA</div>
                <div>A路视在功率：0.063KVA</div>
                <div>B路视在功率：0.112KVA</div>
                <div>电力容量：50p</div>
              </div>
              <div class="waterPoloBox">
                <Echart :height="60" :width="60" :options="echartsOption" />
              </div>
              <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
            </div>
            <div class="room">机房1-机柜1</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
          <div class="loadItem">
            <div class="content">
              <div class="info">
                <div>总视在功率：0.153KVA</div>
                <div>A路视在功率：0.063KVA</div>
                <div>B路视在功率：0.112KVA</div>
                <div>电力容量：50p</div>
              </div>
              <div class="waterPoloBox">
                <Echart :height="60" :width="60" :options="echartsOption" />
              </div>
              <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
            </div>
            <div class="room">机房1-机柜1</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
          <div class="loadItem">
            <div class="content">
              <div class="info">
                <div>总视在功率：0.153KVA</div>
                <div>A路视在功率：0.063KVA</div>
                <div>B路视在功率：0.112KVA</div>
                <div>电力容量：50p</div>
              </div>
              <div class="waterPoloBox">
                <Echart :height="60" :width="60" :options="echartsOption" />
              </div>
              <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
            </div>
            <div class="room">机房1-机柜1</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
          <div class="loadItem">
            <div class="content">
              <div class="info">
                <div>总视在功率：0.153KVA</div>
                <div>A路视在功率：0.063KVA</div>
                <div>B路视在功率：0.112KVA</div>
                <div>电力容量：50p</div>
              </div>
              <div class="waterPoloBox">
                <Echart :height="60" :width="60" :options="echartsOption" />
              </div>
              <!-- <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div> -->
            </div>
            <div class="room">机房1-机柜1</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
        </div>
        <!-- <div class="arrayContainer">
          <div class="arrayItem" v-for="item in listPage" :key="item.id" @dblclick="handleArrayDbclick(item)">
            <div class="content">
              <div><img class="icon" alt="" src="@/assets/imgs/jg.jpg" /></div>
              <div class="info">
                <div>视在功率：{{item.apparentTotal}}KVA</div>
                <div>有功功率：{{item.activeTotal}}KW</div>
                <div>负载率：{{item.loadFactor}}</div>
                <div>电能：50kWh</div>
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
        /> -->
      </ContentWrap>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'
import 'echarts-liquidfill'
import { CabinetApi } from '@/api/cabinet/info'
import LiquidBall from './compoent/LiquidBall.vue'

const loading = ref(false)

const listPage = reactive([
  {
    local: '机房1-机柜1',
    loadPrecent: '38%',
    zszgl: '0.153KVA',
    aszgl: '0.063KVA',
    bszgl: '0.112KVA',
    dlrl: '50p',
    gxsj: '2024-5-7 11:00'
  },
  {
    local: '机房1-机柜1',
    loadPrecent: '38%',
    zszgl: '0.153KVA',
    aszgl: '0.063KVA',
    bszgl: '0.112KVA',
    dlrl: '50p',
    gxsj: '2024-5-7 11:00'
  },
  {
    local: '机房1-机柜1',
    loadPrecent: '38%',
    zszgl: '0.153KVA',
    aszgl: '0.063KVA',
    bszgl: '0.112KVA',
    dlrl: '50p',
    gxsj: '2024-5-7 11:00'
  },
  {
    local: '机房1-机柜1',
    loadPrecent: '38%',
    zszgl: '0.153KVA',
    aszgl: '0.063KVA',
    bszgl: '0.112KVA',
    dlrl: '50p',
    gxsj: '2024-5-7 11:00'
  },
  {
    local: '机房1-机柜1',
    loadPrecent: '38%',
    zszgl: '0.153KVA',
    aszgl: '0.063KVA',
    bszgl: '0.112KVA',
    dlrl: '50p',
    gxsj: '2024-5-7 11:00'
  },
])

const echartsOption = reactive({
  series: [
    {
      type: 'liquidFill',
      data: [0.38], // 设置水球图的填充比例
      label: {
        fontSize: 12, // 设置字体大小
        fontWeight: 'bold' // 设置字体粗细
      },
      radius: '100%',
      amplitude: 2, // 调整波浪的振幅
      outline: {
        show: false // 不显示外圈轮廓线
      },

      color: ['#3b8bf5'],
      backgroundStyle: {
        color: '#fff'
      }
    }
  ]
})

const statusList = reactive([
  {
    name: '未开通',
    selected: true,
    value: 0,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty',
    color: '#aaa'
  },
  {
    name: '负载量<30%',
    selected: true,
    value: 1,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal',
    color: '#3bbb00'
  },
  {
    name: '30%≤负载量<60%',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn',
    color: '#ffc402'
  },
  {
    name: '60%≤负载量<90%',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error',
    color: '#fa3333'
  },
  {
    name: '负载量>90%',
    selected: true,
    value: 4,
    cssClass: 'btn_unbound',
    activeClass: 'btn_unbound unbound',
    color: '#05ebfc'
  },
])

























const navList = ref([])
const switchNav = ref(false) //false: 导航树 true：微模块展示
const isCloseNav = ref(false) // 左侧导航是否收起
const switchValue = ref(0)
const queryParams = reactive({
  company: undefined,
  showCol: [1, 2, 3, 12, 13, 14] as number[],
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  console.log('接口获取机房导航列表', res)
  navList.value = res
}

const handleClick = (row) => {
  console.log('Button clicked!', row);
}

const handleCheck = (row) => {
  console.log('handleCheck!', row);
  // const ids = [] as any
  // row.forEach(item => {
  //   if (item.type == 3) {
  //     ids.push(item.id)
  //   }
  // })
  // getTableData(true, ids)
}
// 处理切换按钮点击事件
const handleSwitchNav = () => {
  switchNav.value = !switchNav.value
}
// 处理状态选择事件
const handleSelectStatus = (index, event) => {
  console.log('处理状态选择事件', index, event)
  statusList[index].selected = !statusList[index].selected
}
// 处理切换 表格/阵列 模式
const handleSwitchModal = (value) => {
  if (switchValue.value == value) return
  switchValue.value = value
  // if (value == 0) { // 阵列
  //   queryParams.pageSize = 24
  // } else {
  //   queryParams.pageSize = 10
  // }
  // getTableData(true)
}

onBeforeMount(() => {
  getNavList()
})
</script>

<style lang="scss" scoped>

.loadContainer {
  height: 600px;
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .loadItem {
    width: 25%;
    min-width: 275px;
    height: 145px;
    font-size: 12px;
    box-sizing: border-box;
    background-color: #eef4fc;
    border: 5px solid #fff;
    padding-top: 36px;
    position: relative;
    .content {
      padding-left: 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      line-height: 1.6;
      .waterPoloBox {
        flex: 1;
        margin-left: 15px;
        display: flex;
        justify-content: center;
      }
    }
    .room {
      position: absolute;
      left: 10px;
      top: 8px;
      font-size: 13px;
    }
    .detail {
      width: 35px;
      height: 20px;
      cursor: pointer;
      font-size: 12px;
      padding: 0;
      border: 1px solid #ccc;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #fff;
      position: absolute;
      right: 10px;
      top: 8px;
    }
  }
}















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
.btn_normal,
.btn_empty,
.btn_warn,
.btn_error,
.btn_unbound,
.btn_offline {
  // width: 55px;
  // height: 32px;
  padding: 3px 8px;
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
  border: 1px solid #3b8bf5;
  background-color: #fff;
}
.warn {
  background-color: #3b8bf5;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_error {
  border: 1px solid #ffc402;
  background-color: #fff;
}
.error {
  background-color: #ffc402;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_unbound {
  border: 1px solid #fa3333;
  background-color: #fff;
}
.unbound {
  background-color: #fa3333;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
</style>