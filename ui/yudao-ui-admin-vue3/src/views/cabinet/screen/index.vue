<template>
  <ContentWrap>
    <div class="title" style="display:flex; justify-content: flex-start; margin-bottom:20px; margin-top:-10px;">
        <span>{{cabinetInfo.roomName}}-{{cabinetInfo.cabinetName}}</span>
        <span style="margin-left:10px;">空间总容量：{{cabinetInfo.cabinetHeight}}</span>
        <span style="margin-left:10px;">已用空间：{{cabinetInfo.usedSpace}}U</span>
        <span style="margin-left:10px;">未用空间：{{cabinetInfo.freeSpace}}U</span>
        <span style="margin-left:10px;">设备总数：{{cabinetInfo.rackNum}}</span>
        <el-button @click.prevent="toDetail(cabinetId,roomId)" type="primary" style="margin-left: auto;">编辑</el-button>
        <el-button @click="switchValue = 0;" :type="switchValue === 0 ? 'primary' : ''"><Icon icon="ep:grid" style="margin-right: 8px" />阵列模式</el-button>
        <el-button @click="switchValue = 1;" :type="switchValue === 1 ? 'primary' : ''"><Icon icon="ep:expand" style="margin-right: 8px" />表格模式</el-button>
    </div>
    <div v-if="switchValue === 0">
      <div class="screenContiner">
      <!--<div class="deviceList">
        <div v-for="item in deviceRight" :key="item.id" class="device">
          <div class="name">设备名称： {{item.rackName}}</div>
          <div class="info">
            <div>型号：{{item.rackType}}</div>
            <div>占用：{{item.uHeight}}</div>
          </div>
        </div>
      </div>
      <div class="machine">
        <div class="mainBorder">
          <div class="main">
            <template v-for="(item, index) in frameList" :key="index">
              <div v-if="item.uHeight > 0" class="Uitem active" :style="`min-height: ${height}`">{{item.rackName}}</div>
              <div v-else class="Uitem"></div>
            </template>
          </div>
        </div>
        <div class="base"></div>
      </div>-->
      <div class="deviceList">
        <div v-for="item in deviceLeft" :key="item.id" class="device">
          <div class="name"><span>设备名称： {{item.rackName}}</span><span style="margin-left:100px;"></span></div>
          <div class="info">
            <div><span>型号：{{item.rackType}}</span><span style="margin-left:100px;">状态：</span><span  v-if="item.runStatus==0" style="color:grey"> 关机</span><span v-else style="color:green"> 开机</span> </div>
            <!--<div>占用：{{item.uHeight}}</div>-->
            <div>功率：{{item.powActive}}KW</div>
            <div><span>电流：{{item.curValue}}A</span><span style="margin-left:100px;">高度：{{item.uHeight}}</span></div>
          </div>

        </div>
      </div>
      <div style="width:20%;height:100%;">
        <div class="machine">
        <div class="mainBorder">
          <div class="main">
            <template v-for="(item, index) in frameList" :key="index">
              <div v-if="item.uHeight > 0" class="Uitem active" :style="`min-height: ${height}`">{{item.rackName}}</div>
              <div v-else class="Uitem"></div>
            </template>
          </div>
        </div>
        <div class="base"></div>
        <div class="local">{{cabinetInfo.roomName}}-{{cabinetInfo.cabinetName}}</div>
        <div class="infomation">
          <div class="infoItem">
            <span class="num">{{cabinetInfo.cabinetHeight}}</span>
            <span>空间总容量</span>
          </div>
          <div class="line"></div>
          <div class="infoItem">
            <span class="num">{{cabinetInfo.usedSpace}}U</span>
            <span>已用空间</span>
          </div>
          <div class="line"></div>
          <div class="infoItem">
            <span class="num">{{cabinetInfo.freeSpace}}U</span>
            <span>未用空间</span>
          </div>
          <div class="line"></div>
          <div class="infoItem">
            <span class="num">{{cabinetInfo.rackNum}}</span>
            <span>设备总数</span>
          </div>
        </div>
      </div>
      </div>
      </div>
      <!--<div class="local">{{cabinetInfo.roomName}}-{{cabinetInfo.cabinetName}}</div>
      <div class="infomation">
        <div class="infoItem">
          <span class="num">{{cabinetInfo.cabinetHeight}}</span>
          <span>空间总容量</span>
        </div>
        <div class="line"></div>
        <div class="infoItem">
          <span class="num">{{cabinetInfo.usedSpace}}U</span>
          <span>已用空间</span>
        </div>
        <div class="line"></div>
        <div class="infoItem">
          <span class="num">{{cabinetInfo.freeSpace}}U</span>
          <span>未用空间</span>
        </div>
        <div class="line"></div>
        <div class="infoItem">
          <span class="num">{{cabinetInfo.rackNum}}</span>
          <span>设备总数</span>
        </div>
      </div>-->
    </div>
    <div v-else-if="switchValue === 1">
      <el-table :data="list" :stripe="true" :show-overflow-tooltip="true" :border="true" @cell-dblclick="toPDUDisplayScreen" >
        <el-table-column label="名称" align="center" prop="tableId"/>
        <el-table-column label="高度" align="center" prop="location" />
        <el-table-column label="型号" align="center" prop="tableId"/>
        <el-table-column label="总功率" align="center" prop="location" />
        <el-table-column label="A路功率" align="center" prop="tableId"/>
        <el-table-column label="B路功率" align="center" prop="location" />
        <el-table-column label="A路电流" align="center" prop="tableId"/>
        <el-table-column label="B路电流" align="center" prop="location" />
      </el-table>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { CabinetApi } from '@/api/cabinet/info';
const {push} = useRouter();
const cabinetInfo = ref({});
const deviceLeft = ref([]);
const deviceRight = ref([]);
const frameList = ref([]);
const height = ref('0px');
const roomId = history?.state?.roomId || 1;
const cabinetId = history?.state?.id || 1;
const switchValue = ref(0);
const editEnable = ref(false);


const getData = async() => {
  const res = await CabinetApi.getCabinetInfoItem({id: cabinetId});
  console.log('res', res);
  cabinetInfo.value = res;
  if (res.rackIndexList && res.rackIndexList.length > 0) {
    deviceLeft.value = res.rackIndexList;
    console.log('deviceLeft.value', deviceLeft.value);
    //deviceLeft.value = res.rackIndexList.filter((item,index) => index%2 == 0);
    //deviceRight.value = res.rackIndexList.filter((item,index) => index%2 == 1);
    const frames = [] as any;
    for(let i = 1; i <= res.cabinetHeight; i++) {
      frames.push({});
    }
    res.rackIndexList.forEach(item => {
      frames.splice(item.uAddress-1, item.uHeight, item);
    })
    frameList.value = frames.reverse();
// 根据货架索引列表的长度动态调整高度
    if (res.rackIndexList.length < 11) {
      height.value = '30px';
    } else if (res.rackIndexList.length.length < 16) {
      height.value = '25px';
    } else if (res.rackIndexList.length.length < 21) {
      height.value = '20px';
    } else if (res.rackIndexList.length.length < 26) {
      height.value = '15px';
    }
  }
}
const toDetail = (cabinetId,roomId) => {
  console.log('跳转详情cabinetId', cabinetId);
  console.log('跳转详情roomId', roomId);
  // alert('跳转详情')
  push({path: '/cabinet/cab/frameBinding', state: { cabinetId,roomId }});
}

getData();
</script>

<style lang="scss" scoped>
.screenContiner {
  position: relative;
  display: flex;
  justify-content: space-evenly;
  .deviceList {
    width:80%;
    height: 588px;
    overflow: auto;
    display: flex;
    flex-wrap: wrap;
    align-content: flex-start;
    &::-webkit-scrollbar {
      width: 0;
      height: 0;
    }
    .device {
      width: 310px;
      height: 130px;
      margin-bottom: 15px;
      margin: 10px;
      font-size: 14px;
      border: 1px solid #eee;
      box-sizing: border-box;
      .name {
        padding: 10px 8px;
        background-color: #eee;
        // background: linear-gradient(to bottom right, #000c4248, #0004ff);
        
      }
      .info {
        padding: 0 8px;
        div {
          margin-top: 10px;
        }
      }
    }
  }
  .machine {
    position: relative;
    width: 260px;
    height: fit-content;
    box-sizing: border-box;
    // &::before {
    //   content: "";
    //   position: absolute;
    //   bottom: -15px; /* 控制梯形的上方边长 */
    //   left: 20px;
    //   width: 12px;
    //   height: 0;
    //   border-bottom: 15px solid #90b8df; /* 控制梯形的底边长度和颜色 */
    //   border-left: 10px solid transparent; /* 控制梯形的左侧斜边 */
    //   border-right: 10px solid transparent; /* 控制梯形的右侧斜边 */
    //   transform: rotateX(180deg)
    // }
    // &::after {
    //   content: "";
    //   position: absolute;
    //   bottom: -15px; /* 控制梯形的上方边长 */
    //   right: 20px;
    //   width: 12px;
    //   height: 0;
    //   border-bottom: 15px solid #90b8df; /* 控制梯形的底边长度和颜色 */
    //   border-left: 10px solid transparent; /* 控制梯形的左侧斜边 */
    //   border-right: 10px solid transparent; /* 控制梯形的右侧斜边 */
    //   transform: rotateX(180deg)
    // }
    .mainBorder {
      // height: calc(100% - 20px);
      background-color: #90b8df;
      box-sizing: border-box;
      padding: 18px 18px 40px 18px;
      .main {
        height: 500px;
        display: flex;
        flex-direction: column;
        width: 100%;
        // height: 100%;
        box-sizing: border-box;
        border: 5px solid;
        background-color: #fff;
        .Uitem {
          flex: 1;
          width: 100%;
          background-color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #eee;
          font-size: 14px;
        }
        .active {
          min-height: 20px;
          height: 30px;
          background-color: #5298df;
          border-radius: 5px;
          margin-top: 1px;
          margin-bottom: 1px;
        }
      }
    }
    .base {
      height: 20px;
      background-color: #51677c;
    }
  }
}
.local {
  display: flex;
  justify-content: center;
  align-items: center;
  padding-top: 15px;
}
.infomation {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 500px;
  margin-left:-120px;
  .infoItem {
    padding: 20px 30px;
    display: flex;
    flex-direction: column;
    // justify-content: center;
    align-items: center;
    font-size: 13px;
    .num {
      font-size: 16px;
      margin-bottom: 5px;
    }
  }
  .line {
    width: 1px;
    height: 20px;
    background-color: #eee;
  }
}
</style>