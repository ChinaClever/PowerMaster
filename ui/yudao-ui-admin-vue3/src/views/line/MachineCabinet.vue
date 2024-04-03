<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <NavTree @node-click="handleClick" @check="handleCheck" :showSearch="true" :dataList="data" />
      </el-col>
      <el-col :span="20" :xs="24">
        <ContentWrap>
          <el-form
          class="-mb-15px"
          :model="queryParams"
          ref="queryFormRef"
          :inline="true"
          label-width="68px"
        >
          <el-form-item label="公司名称" prop="username">
            <el-input
              v-model="queryParams.username"
              placeholder="请输入公司名称"
              clearable
              class="!w-240px"
            />
          </el-form-item>
          <el-form-item label="展示列" prop="showCol">
            <el-cascader
              ref="colNode"
              class="!w-240px"
              v-model="defaultOptionsCol"
              @change="cascaderChange"
              :options="optionsCol"
              :props="props"
              collapse-tags />
          </el-form-item>
          <el-form-item>
            <el-button @click="handleQuery"><Icon icon="ep:search" />搜索</el-button>
            <el-button @click="openForm('add')" type="primary" plain><Icon icon="ep:plus" />添加</el-button>
          </el-form-item>
        </el-form>
        </ContentWrap>
        <ContentWrap>
          <el-table style="width: 100%" v-loading="loading" :data="list">
            <el-table-column v-if="queryParams.showCol.includes(1)" label="总AB视在功率" min-width="120" align="center" prop="abszgl" />
            <el-table-column v-if="queryParams.showCol.includes(2)" label="总AB有功功率" min-width="120" align="center" prop="abyggl" />
            <el-table-column v-if="queryParams.showCol.includes(3)" label="总AB电能" min-width="110" align="center" prop="abdn" />
            <el-table-column v-if="queryParams.showCol.includes(5)" label="A视在功率" min-width="110" align="center" prop="aszgl" />
            <el-table-column v-if="queryParams.showCol.includes(6)" label="A有功功率" min-width="110" align="center" prop="ayggl" />
            <el-table-column v-if="queryParams.showCol.includes(7)" label="A电能" min-width="110" align="center" prop="adn" />
            <el-table-column v-if="queryParams.showCol.includes(9)" label="B视在功率" min-width="110" align="center" prop="bszgl" />
            <el-table-column v-if="queryParams.showCol.includes(10)" label="B有功功率" min-width="110" align="center" prop="byggl" />
            <el-table-column v-if="queryParams.showCol.includes(11)" label="B电能" min-width="110" align="center" prop="bdn" />
            <el-table-column v-if="queryParams.showCol.includes(12)" label="名称" min-width="110" align="center" prop="mc" />
            <el-table-column v-if="queryParams.showCol.includes(13)" label="所属公司" min-width="110" align="center" prop="gs" />
            <el-table-column v-if="queryParams.showCol.includes(14)" label="所属机房" min-width="110" align="center" prop="jf" />
            <el-table-column v-if="queryParams.showCol.includes(15)" label="负载比" min-width="110" align="center" prop="fzb" />
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
                  @click="openForm('edit', scope.row.id)">编辑</el-button>
                <el-button
                  type="danger"
                  @click="handleDelete(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <Pagination
            :total="list.length"
            v-model:page="queryParams.pageNo"
            v-model:limit="queryParams.pageSize"
          />
        </ContentWrap>
      </el-col>
    </el-row>
  </div>

  <!-- 添加或修改用户对话框 -->
  <MachineForm ref="machineForm" @success="getList" />
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import MachineForm from './MachineForm.vue'
// import MyButton from '@/components/MyButton/MyButton.vue';

const message = useMessage() // 消息弹窗
const machineForm = ref()
const colNode = ref()
const testData = ref(null)
const loading = ref(false)
const queryParams = reactive({
  username: undefined,
  showCol: [1, 2, 3, 5, 6, 7, 9, 10, 11, 16] as number[],
  pageNo: 1,
  pageSize: 10,
})
const props = { multiple: true }
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
const defaultOptionsCol = reactive([[0,1], [0,2], [0,3], [4,5], [4,6], [4,7], [8,9], [8,10], [8,11], [16]])
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
    fzb: '50%',
    abzb: 30,
  },
  {
    id: 62,
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

</style>