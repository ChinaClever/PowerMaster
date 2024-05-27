<template>
  <ContentWrap>
    <div class="btns">
      <template v-for="item in btns" :key="item.value">
        <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
      </template>
    </div>
  </ContentWrap>
  <ContentWrap>
  <div class="topologyContainer">
      <div class="loadA">
        <div class="box">
          <div class="title">A路始端箱</div>
          <div class="info">
            <div>Ua</div>
            <div>0.1V</div>
          </div>
          <div class="info">
            <div>Ua</div>
            <div>0.1V</div>
          </div>
          <div class="info">
            <div>Ua</div>
            <div>0.1V</div>
          </div>
        </div>
        <div class="line"></div>
        <div class="boxList">
          <div class="boxItem" v-for="(item, index) in boxs" :key="index">
            <div class="layer"></div>
            <div class="stage"></div>
            <div class="box">
              <div class="title">{{item.t1+(index + 1)}}#插接箱</div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="loadB">
        <div class="box boxB">
          <div class="title">A路始端箱</div>
          <div class="info">
            <div>Ua</div>
            <div>0.1V</div>
          </div>
          <div class="info">
            <div>Ua</div>
            <div>0.1V</div>
          </div>
          <div class="info">
            <div>Ua</div>
            <div>0.1V</div>
          </div>
        </div>
        <div class="line"></div>
        <div class="boxList">
          <div class="boxItem" v-for="(item, index) in boxs" :key="index">
            <div class="box">
              <div class="title">{{item.t2+(index + 1)}}#插接箱</div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
              <div class="info">
                <div>Ua</div>
                <div>0.1V</div>
              </div>
            </div>
            <div class="stage"></div>
            <div class="layer"></div>
          </div>
        </div>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>
const boxs = [
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  // {
  //   t1: 'A',
  //   t2: 'B',
  // },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
  {
    t1: 'A',
    t2: 'B',
  },
]

const btns = [
  {
    name: '电流',
    value: 0
  },
  {
    name: '电压',
    value: 1
  },
  {
    name: '有功功率',
    value: 2
  },
  {
    name: '无功功率',
    value: 3
  },
  {
    name: '环境因素',
    value: 4
  },
  {
    name: '温度',
    value: 5
  },
]

const chosenBtn = ref(0)

const switchBtn = (value) => {
  chosenBtn.value = value
  console.log('switchBtn', )
}
</script>

<style scoped lang="scss">
.btns {
  display: flex;
  justify-content: flex-end;
}

.topologyContainer {
  box-sizing: border-box;
  height: calc(100vh - 250px);
  // display: flex;
  // flex-direction: column;
  // justify-content: space-around;
  // height: calc(100% - var(--app-content-padding) - var(--app-content-padding) - var(--app-footer-height)) !important;
  overflow: auto;
  padding: 20px 0;
  .loadA,
  .loadB {
    display: flex;
    .box {
      width: 100px;
      height: fit-content;
      border: 1px solid #ddd;
      background-color: #fff;
      flex-shrink: 0;
      .title {
        padding: 15px 13px;
        font-size: 13px;
        font-weight: bold;
        color: #0051ff;
      }
      .info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
        padding: 0 10px;
        font-size: 12px;
      }
    }
    .line {
      flex-shrink: 0;
      width: 12px;
      height: 30px;
      background-color: red;
      margin-top: 48px;
    }
    .boxList {
      flex: 1;
      box-sizing: border-box;
      display: flex;
      justify-content: space-between;
      padding: 0 10px 20px 10px;
      border-top: 4px solid red;
      margin-top: 60px;
      .boxItem {
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 15px;
        .layer {
          width: 30px;
          height: 8px;
          background-color: red;
        }
        .stage {
          width: 4px;
          height: 10px;
          background-color: red;
        }
      }
    }
  }
  .loadB {
    .boxB {
      margin-top: 100px;
    }
    .line {
      margin-top: 150px;
      background-color: green;
    }
    .boxList {
      margin-top: 0px;
      margin-bottom: 60px;
      border-top: none;
      border-bottom: 4px solid green;
      padding: 0 10px;
      .boxItem {
        justify-content: flex-end;
        .layer {
          background-color: green;
        }
        .stage {
          background-color: green;
        }
      }
    }
  }
}
</style>