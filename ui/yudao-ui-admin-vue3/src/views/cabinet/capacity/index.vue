<template>
  <CommonMenu :dataList="navList" @check="handleCheck">
    <template #ActionBar>
      aaa
    </template>
    <template #Content>
      <div class="matrixContainer">
          <div class="item" v-for="item in 9" :key="item">
            <div class="content">
              <div>未用空间</div>
              <div class="info">
                <div>已用空间：</div>
                <div>空间容量：</div>
                <div>设备总数：</div>
              </div>
            </div>
            <div class="room">机房1-机柜1</div>
            <button class="detail" @click.prevent="toDetail(item.id)">详情</button>
          </div>
        </div>
    </template>
  </CommonMenu>
</template>

<script lang="ts" setup>
import { CabinetApi } from '@/api/cabinet/info'

const navList = ref([]) // 左侧导航栏树结构列表
const cabinetIds = ref<number[]>([]) // 左侧导航菜单所选id数组

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await CabinetApi.getRoomMenuAll({})
  navList.value = res
}

// 处理左侧树导航选择事件
const handleCheck = (row) => {
  const ids = [] as any
  row.forEach(item => {
    if (item.type == 3) {
      ids.push(item.id)
    }
  })
  cabinetIds.value = ids
  // getTableData(true)
}

onBeforeMount(() => {
  getNavList()
})
</script>

<style lang="scss" scoped>
.matrixContainer {
  height: calc(100vh - 320px);
  overflow: auto;
  display: flex;
  flex-wrap: wrap;
  align-content: flex-start;
  .item {
    width: 25%;
    min-width: 275px;
    height: 130px;
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
      .info {
        line-height: 1.7;
        font-size: 13px;
        margin-left: 30%;
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
      right: 5px;
      top: 4px;
    }
  }
}
</style>