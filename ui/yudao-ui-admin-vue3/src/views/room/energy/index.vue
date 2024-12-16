<template>
  <el-card shadow="never">
    <div class="dragContainer" v-loading="loading">
      <el-table
        ref="dragTable"
        class="dragTable"
        v-if="tableData.length > 0"
        :data="tableData"
        border
        :row-style="{ background: 'revert' }"
        row-class-name="dragRow"
      >
        <el-table-column fixed type="index" width="60" align="center" :resizable="false" />
        <template v-for="(label) in formParam" :key="label">
          <el-table-column :label="label" min-width="60" align="center" :resizable="false" />
        </template>
      </el-table>
    </div>
  </el-card>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue';

const tableData = ref<TableRow[]>([]);
const loading = ref(true);
const numAColumns = 30; // 动态列数（这里为了示例设为固定值）
const numBRows = 30; // 动态行数（这里为了示例设为固定值）
// 表格列的数据类型
interface TableRow {
  [key: string]: string;
}
// 初始化表格数据
const initializeTableData = () => {
  const data: TableRow[] = [];
  for (let i = 0; i < numBRows; i++) {
    const row = Array(numAColumns).fill('').map((_, colIndex) => ({
      [getTableColCharCode(colIndex)]: '' // 直接创建对象属性
    })).reduce((acc, obj) => ({ ...acc, ...obj }), {}); // 合并对象属性到同一行
    data.push(row);
  }
  tableData.value = data;
  loading.value = false;
};

// 获取表格列label字符
const getTableColCharCode = (num: number): string => {
  const charCodeA = 65;
  if (num < 26) {
    return String.fromCharCode(charCodeA + num);
  } else if (num < 52) {
    return `A ${String.fromCharCode(charCodeA + num - 26)}`;
  } else {
    return `B ${String.fromCharCode(charCodeA + num - 52)}`;
  }
};

const formParam = computed(() => {
  return Object.keys(tableData.value[0] || {});
});

onMounted(() => {
  initializeTableData();
});
</script>

<style scoped>
.dragTable {
  /* 使用 CSS 硬件加速 */
  transform: translateZ(0);
  /* 根据需要添加其他样式 */
}
</style>