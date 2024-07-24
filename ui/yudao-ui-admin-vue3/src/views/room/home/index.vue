<template>
   <div class="homeContainer">
    <div class="left">
      <el-card shadow="never">
        <template #header>
          <div>用能</div>
        </template>
        <div>当日用能：</div>
        <div>本周用能：</div>
        <div>本月用能：</div>
      </el-card>
      <ContentWrap>
        <div>总视在功率</div>
      </ContentWrap>
      <el-card shadow="never">
        <template #header>
          <div>空间管理</div>
        </template>
        <div>未用空间：</div>
        <div>已用空间：</div>
        <div>总空间：</div>
        <div>机柜数：</div>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div>功率曲线</div>
        </template>
      </el-card>
    </div>
    <div class="center" id="center">
      <CabTopology :isFromHome="true" />
    </div>
    <div class="right">
      <el-card shadow="never">
        <template #header>
          <div>用能</div>
        </template>
        <div>当日用能：</div>
        <div>本周用能：</div>
        <div>本月用能：</div>
      </el-card>
      <ContentWrap>
        <div>总有功功率</div>
      </ContentWrap>
      <el-card shadow="never">
        <template #header>
          <div>设备数量</div>
        </template>
        <div>报警数量：</div>
        <div>正常数量：</div>
        <div>离线数量：</div>
        <div>设备总数：</div>
      </el-card>
      <el-card shadow="never">
        <template #header>
          <div>用能曲线</div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script lang="ts" setup>
import CabTopology from "../topology/index.vue"

const containerInfowidth = ref(0)

</script>

<style lang="scss" scoped>
.homeContainer {
  box-sizing: border-box;
  width: 100%;
  // height: calc(100vh - 120px);
  min-height: 550px;
  max-height: calc(100vh - 120px);
  box-sizing: border-box;
  // background-color: #999;
  display: flex;
  .center {
    flex: 1;
    box-sizing: border-box;
    overflow: hidden;
    margin: 0 15px;
    padding-bottom: 15px;
    position: relative;
  }
  .left {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }
  .right {
    width: 300px;
    box-sizing: border-box;
    overflow: hidden;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
  }
}
:deep(.el-card) {
  margin-bottom: 15px;
}
</style>