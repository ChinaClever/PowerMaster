<template>
  <ContentWrap>
    <el-tabs v-model="activeName" class="demo-tabs" @tab-click="handleClick">
      <el-tab-pane label="数据采集" name="second">
        <DcConfigForm ref="dcFormRef" @success="getDCList" />
      </el-tab-pane>
      <el-tab-pane label="计算服务" name="fourth">
        <StatisConfigForm ref="staticFormRef" @success="getStaticList" />
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>

</template>
<script lang="ts" setup>
import { ref } from 'vue'
import type { TabsPaneContext } from 'element-plus'
import { DcConfigApi, DcConfigVO } from '@/api/bus/busdcconfig'
import DcConfigForm from './DcConfigForm.vue'
import { StatisConfigApi, StatisConfigVO } from '@/api/bus/busstatisconfig'
import StatisConfigForm from './StatisConfigForm.vue'

/** 消息队列系统配置 列表 */
defineOptions({ name: 'MqConfig' })

const dcList = ref<DcConfigVO[]>([]) as any // 列表的数据
const dcListTotal = ref(0) // 列表的总页数
const dcQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  alarmPush: undefined,
})
const dcQueryFormRef = ref() // 搜索的表单

const staticList = ref<StatisConfigVO[]>([]) as any// 列表的数据
const staticTotal = ref(0) // 列表的总页数
const staticQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  billMode: undefined,
  dayCron: undefined,
  hourCron: undefined,
  eqDayCron: undefined,
  eqWeekCron: undefined,
  eqMonthCron: undefined,
})

const getDCList = async () => {
  try {
    const data = await DcConfigApi.getDcConfigPage(dcQueryParams)
    dcList.value = data.list
    dcListTotal.value = data.total
  } finally {
  }
}

const getStaticList = async () => {
  try {
    const data = await StatisConfigApi.getStatisConfigPage(staticQueryParams)
    staticList.value = data.list
    staticTotal.value = data.total
  }finally {
  }
}

const dcFormRef = ref()
const openDCForm = (type: string, id?: number) => {
  dcFormRef.value.open(type, id)
}


const staticFormRef = ref()
const openStaticForm = (type: string, id?: number) => {
  staticFormRef.value.open(type, id)
}



/** 初始化 **/
onMounted(async () => {
  await getDCList();
  await getStaticList();
  if(dcList.value && dcList.value.length > 0 && dcList.value[0].id){
    openDCForm('update', dcList.value[0].id);
  }else{
    openDCForm('create');
  }
})

const activeName = ref('second')

const handleClick = (tab: TabsPaneContext, event: Event) => {
  if(tab.props.label === "数据采集"){
    if(dcList.value && dcList.value.length > 0 && dcList.value[0].id){
      openDCForm('update', dcList.value[0].id);
    }else{
      openDCForm('create');
    }
    
  }
  if(tab.props.label === "计算服务"){
    if(staticList.value && staticList.value?.length > 0 && staticList.value[0].id){
      openStaticForm('update', staticList.value[0].id);
    } else {
      openStaticForm('create');
    }
  }

}
</script>

<style>
.demo-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-size: 32px;
  font-weight: 600;
}
</style>