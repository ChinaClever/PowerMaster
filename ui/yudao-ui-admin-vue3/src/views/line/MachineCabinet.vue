<template>
  <div class="master">
    <!-- 左大侧 -->
    <div class="master-left" :style="`flex: ${leftFlex}`">
      <ContentWrap style="height: calc(100% - 15px)">
        <div v-if="!switchValue && !isCloseNav" class="nav-left">
          <div class="navBar">微模块机房</div>
          <div class="header">
            <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
            <div class="name">微模块机房</div>
            <div>机房202</div>
            <div class="reduce" @click.prevent="closeNav"><Icon icon="ep:arrow-left" />收起</div>
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
        <NavTree v-if="!switchValue && !isCloseNav" @node-click="handleClick" @check="handleCheck" :showSearch="true" :dataList="data" />
        <div v-if="isCloseNav" class="expand" @click.prevent="expandNav"><Icon icon="ep:arrow-right" /><span>展</span><span>开</span></div>
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
          <el-form-item v-show="switchValue"  label="公司名称" prop="username">
            <el-input
              v-model="queryParams.username"
              placeholder="请输入公司名称"
              clearable
              class="!w-160px"
            />
          </el-form-item >
          <el-form-item v-show="switchValue" label="展示列" prop="showCol">
            <el-cascader
              ref="colNode"
              class="!w-220px"
              v-model="defaultOptionsCol"
              @change="cascaderChange"
              :options="optionsCol"
              :props="props"
              collapse-tags />
          </el-form-item>
          <el-form-item style="float:right;">
            <el-button v-show="switchValue" @click="handleQuery"><Icon icon="ep:search" />搜索</el-button>
            <el-button v-show="switchValue" @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button>
            <el-button @click="switchValue = 0" :type="!switchValue ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px;" />阵列模式</el-button>
            <el-button @click="switchValue = 1" :type="switchValue ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px;" />表格模式</el-button>
          </el-form-item>
        </el-form>
      </ContentWrap>
      <ContentWrap v-show="switchValue">
        <el-table style="width: 100%;min-height: 600px" v-loading="loading" :data="listPage">
          <el-table-column v-if="queryParams.showCol.includes(14)" label="位置" min-width="110" align="center" prop="jf">
            <template #default="scope">
              <div>{{scope.row.jf}}-{{scope.row.jg}}</div>
            </template>
          </el-table-column>
          <el-table-column label="状态" min-width="110" align="center">
            <template #default="scope">
              <div>{{statusList[scope.row.status] && statusList[scope.row.status].name}}</div>
            </template>
          </el-table-column>
          <el-table-column v-if="queryParams.showCol.includes(12)" label="名称" min-width="110" align="center" prop="mc" />
          <el-table-column v-if="queryParams.showCol.includes(1)" label="总AB视在功率" min-width="120" align="center" prop="abszgl" />
          <el-table-column v-if="queryParams.showCol.includes(2)" label="总AB有功功率" min-width="120" align="center" prop="abyggl" />
          <el-table-column v-if="queryParams.showCol.includes(3)" label="总AB电能" min-width="110" align="center" prop="abdn" />
          <el-table-column v-if="queryParams.showCol.includes(5)" label="A视在功率" min-width="110" align="center" prop="aszgl" />
          <el-table-column v-if="queryParams.showCol.includes(6)" label="A有功功率" min-width="110" align="center" prop="ayggl" />
          <el-table-column v-if="queryParams.showCol.includes(7)" label="A电能" min-width="110" align="center" prop="adn" />
          <el-table-column v-if="queryParams.showCol.includes(9)" label="B视在功率" min-width="110" align="center" prop="bszgl" />
          <el-table-column v-if="queryParams.showCol.includes(10)" label="B有功功率" min-width="110" align="center" prop="byggl" />
          <el-table-column v-if="queryParams.showCol.includes(11)" label="B电能" min-width="110" align="center" prop="bdn" />
          <el-table-column v-if="queryParams.showCol.includes(15)" label="负载比" min-width="110" align="center" prop="fzb" />
          <el-table-column v-if="queryParams.showCol.includes(13)" label="所属公司" min-width="110" align="center" prop="gs" />
          <el-table-column v-if="queryParams.showCol.includes(16)" label="A,B占比" align="center" prop="abzb">
            <template #default="scope">
              <div class="progressContainer">
                <div class="progress">
                  <div class="left" :style="`flex: ${scope.row.abzb}`">{{scope.row.abzb}}</div>
                  <div class="right" :style="`flex: ${100 - scope.row.abzb}`">{{100 - scope.row.abzb}}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" align="center" width="180px">
            <template #default="scope">
              <el-button
                type="primary"
                @click="openForm('edit', scope.row.id)">编辑
              </el-button>
              <el-button
                type="danger"
                @click="handleDelete(scope.row.id)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <Pagination
          :total="list.length"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
        />
      </ContentWrap>
      <ContentWrap v-show="!switchValue">
        <div class="arrayContainer">
          <div class="arrayItem" v-for="item in listPage" :key="item.id">
            <div class="content">
              <div><img class="icon" alt="" src="@/assets/imgs/jigui.jpg" /></div>
              <div class="info">
                <div>总视在功率：{{item.abszgl}}</div>
                <div>总有功功率：{{item.abyggl}}</div>
                <div>AB路占比：{{item.fzb}}</div>
              </div>
            </div>
            <div class="room">{{item.jf}}-{{item.mc}}</div>
            <div v-if="item.status == 0" class="status-empty">空载</div>
            <div v-if="item.status == 1" class="status-normal">正常</div>
            <div v-if="item.status == 2" class="status-warn">预警</div>
            <div v-if="item.status == 3" class="status-error">故障</div>
            <button class="detail" @click.prevent="toMachineDetail">详情</button>
          </div>
        </div>
        <Pagination
          :total="list.length"
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
        />
      </ContentWrap>
    </div>
  </div>

  <!-- 添加或修改用户对话框 -->
  <MachineForm ref="machineForm" @success="getList" />
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { object } from 'vue-types';
import MachineForm from './MachineForm.vue'
// import MyButton from '@/components/MyButton/MyButton.vue';

const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗
const machineForm = ref()
const colNode = ref()
const testData = ref(null)
const loading = ref(false)
const isCloseNav = ref(false)
const switchValue = ref(0)
const leftFlex = ref(8)
const queryParams = reactive({
  username: undefined,
  showCol: [1, 2, 3, 14] as number[],
  pageNo: 1,
  pageSize: 10,
})
const statusList = reactive([
  {
    name: '空载',
    selected: true,
    value: 0,
    cssClass: 'btn_normal',
    activeClass: 'btn_normal normal'
  },
  {
    name: '正常',
    selected: true,
    value: 1,
    cssClass: 'btn_empty',
    activeClass: 'btn_empty empty'
  },
  {
    name: '预警',
    selected: true,
    value: 2,
    cssClass: 'btn_warn',
    activeClass: 'btn_warn warn'
  },
  {
    name: '故障',
    selected: true,
    value: 3,
    cssClass: 'btn_error',
    activeClass: 'btn_error error'
  },
])
const props = { multiple: true }
const closeNav = () => {
  console.log('closeNav')
  leftFlex.value = 1
  isCloseNav.value = true
}
const expandNav = () => {
  console.log('expandNav')
  leftFlex.value = 8
  isCloseNav.value = false
}
const handleSelectStatus = (index) => {
  statusList[index].selected = !statusList[index].selected
  const status =  statusList.filter(item => item.selected)
  const statusArr = status.map(item => item.value)
  listPage.value = list.filter(item => statusArr.includes(item.status))
  console.log('list', list, statusArr)
}
const toMachineDetail = () => {
  console.log('toMachineDetail!')
  push('/line/Managment')
}
const handleClick = (row) => {
  console.log('Button clicked!', row);
}
const handleCheck = (row) => {
  console.log('handleCheck!', row);
}
const handleQuery = () => {
  const arr = JSON.parse(JSON.stringify(list))
  console.log('handleQuery clicked!', arr);
}
const openForm = (type: string, id?: number) => {
  const findData = list.find(item => item.id == id)
  console.log('ormRef.value', machineForm.value)
  machineForm.value.open(type, findData)
}
const handleDelete = async (id: number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 发起删除
    // await UserApi.deleteUser(id)
    const index = list.findIndex(item => item.id == id)
    list.splice(index, 1)
    message.success('common.delSuccess')
    // 刷新列表
    // await getList()
  } catch (error) {
    
  }
}
const cascaderChange = (row) => {
  const checkedNodes = colNode.value.getCheckedNodes(true)
  queryParams.showCol = checkedNodes.map(item => item.value)
  console.log('cascaderChange', defaultOptionsCol, row)
}
const getList = () => {
  console.log('getList')
}
onBeforeMount(() => {
  listPage.value = list
})
const listPage = ref<any>([])
const defaultOptionsCol = reactive([[0,1], [0,2], [0,3], [14]])
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
  label: '名称'
},{
  value: 13,
  label: '所属公司'
}, {
  value: 14,
  label: '所属机房'
}, {
  value: 15,
  label: '负载率'
}, {
  value: 16,
  label: 'AB占比'
}
])
let list = reactive([
  {
    id: 1,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜1',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 2,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜2',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 3,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜3',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 4,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜4',
    status: 0,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 5,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜1',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 6,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜6',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 7,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜7',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 8,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜8',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 9,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜9',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 10,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜10',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 11,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜11',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 12,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜12',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 13,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜13',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 14,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜14',
    status: 2,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 15,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜15',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 16,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜16',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 17,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜17',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 18,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜18',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 19,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜19',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 20,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜20',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 21,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜21',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 22,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜22',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 23,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜23',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 24,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜24',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 25,
    abszgl: '25',
    abyggl: '52',
    abdn: '45',
    aszgl: '22',
    ayggl: '5',
    adn: '52',
    bszgl: '25',
    byggl: '52',
    bdn: '40',
    mc: '服务器1',
    gs: '亚马逊',
    jf: '机房202',
    jg: '机柜25',
    status: 1,
    fzb: '50%',
    abzb: 30,
  },
])
const data = reactive([{
  label: '机房 1',
  children: [{
    label: '柜列1',
    children: [{
      label: '机柜1',
    }]
  }],
}, {
  label: '机房 2',
  children: [{
    label: '柜列1',
    children: [{
      label: '机柜1',
    }]
  }],
}, {
  label: '机房 3',
  children: [{
    label: '柜列1',
    children: [{
      label: '机柜1',
    }]
  }],
}],)
</script>

<style scoped lang="scss">
.master {
  display: flex;
  .master-left {
    overflow: hidden;
    box-sizing: border-box;
    padding-right: 20px;
    transition: all 0.3s linear;
    .expand {
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
    flex: 40;
  }
}
.btn_normal,
.btn_empty,
.btn_warn,
.btn_error {
  width: 58px;
  height: 35px;
  cursor: pointer;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    color: #7bc25a;
  }
}
.btn_normal {
  border: 1px solid #aaa;
  background-color: #fff;
  margin-right: 8px;
}
.normal {
  background-color: #aaa;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_empty {
  border: 1px solid #3bbb00;
  background-color: #fff;
  margin-right: 8px;
}
.empty {
  background-color: #3bbb00;
  color: #fff;
  &:hover {
    color: #fff;
  }
}
.btn_warn {
  border: 1px solid #ffc402;
  background-color: #fff;
  margin-right: 8px;
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
:deep(.master-left .el-card__body) {
  padding: 0;
}
.nav-left {
  width: 100%;
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
  .navBar {
    width: 100%;
    height: 46px;
    line-height: 46px;
    padding-left: 10px;
    background-color: #d5ffc1;
    font-size: 14px;
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    position: relative;
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 5px;
      top: 5px;
      color: #777;
      cursor: pointer;
    }
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
      box-sizing: border-box;
      background-color: var(--el-color-primary);
      // border-right: 1px solid #000;
    }
    .right {
      background-color:  #f56c6c;
    }
  }
}
.arrayContainer {
  height: 600px;
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
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
</style>