<template>
  <el-card shadow="never" v-loading="loading">
    <el-skeleton :loading="trendLoading" animated>
      <Echart :height="500" :options="lineChartOptions" />
    </el-skeleton>
  </el-card>
</template>
<script lang="ts" setup>
import { HistoryLineApi, HistoryLineVO } from '@/api/pdu/historyline'
import { EChartsOption } from 'echarts'
import { CardTitle } from '@/components/Card'

/** 会员终端卡片 */
defineOptions({ name: 'MemberTerminalCard' })

const loading = ref(true) // 加载中

const lineChartOptions = reactive<EChartsOption>({
  dataset: {
    dimensions: ['date', 'turnoverPrice', 'orderPayPrice', 'rechargePrice', 'expensePrice'],
    source: []
  },
  grid: {
    left: 20,
    right: 20,
    bottom: 20,
    top: 80,
    containLabel: true
  },
  legend: {
    top: 50
  },
  series: [
    { name: '营业额', type: 'line', smooth: true },
    { name: '商品支付金额', type: 'line', smooth: true },
    { name: '充值金额', type: 'line', smooth: true },
    { name: '支出金额', type: 'line', smooth: true }
  ],
  toolbox: {
    feature: {
      // 数据区域缩放
      dataZoom: {
        yAxisIndex: false // Y轴不缩放
      },
      brush: {
        type: ['lineX', 'clear'] // 区域缩放按钮、还原按钮
      },
      saveAsImage: { show: true, name: '交易状况' } // 保存为图片
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    },
    padding: [5, 10]
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    axisTick: {
      show: false
    }
  },
  yAxis: {
    axisTick: {
      show: false
    }
  }
}) as EChartsOption

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  id: undefined,
  pduId: undefined,
  type: undefined,
  topic: undefined,
  indexes: undefined,
  value: undefined,
  createTime: [],
})

/** 按照终端，查询会员统计列表 */
const getLineData = async () => {
  loading.value = true
  const list = await HistoryLineApi.getHistoryDataPage(queryParams)
  lineChartOptions.dataset['source'] = list
  loading.value = false
}

/** 初始化 **/
onMounted(() => {
  getLineData()
})
</script>
