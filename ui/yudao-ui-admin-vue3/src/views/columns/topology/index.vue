<template>
  <ContentWrap>
    <div class="btn-main">
      <div class="btns">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" type="primary" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <el-button @click="handleAdd" type="primary" ><Icon icon="ep:add" class="mr-5px" />添加</el-button>
    </div>
  </ContentWrap>
  <ContentWrap>
    <div ref="topologyContainer" class="topologyContainer" style="position: relative;z-index: 1">
      <div class="LeftBus">
        <div class="startBus">
          始端箱
        </div>
        <div class="busListContainer">
          <div class="bridge"></div>
          <div class="busList">
            <div class="plugin-box">
              <div class="box">
                <div>Ia</div>
                <div>30</div>
              </div>
              <div class="box">
                <div>Ib</div>
                <div>8.1</div>
              </div>
              <div class="box">
                <div>Ic</div>
                <div>50</div>
              </div>
              <div class="name">电流</div>
              <div id="plugin-A-1" class="pointA"></div>
              <div id="plugin-B-1" class="pointB"></div>
              <div id="plugin-C-1" class="pointC"></div>
            </div>
            <div class="plugin-box">
              <div class="box">
                <div>Ia</div>
                <div>30</div>
              </div>
              <div class="box">
                <div>Ib</div>
                <div>40</div>
              </div>
              <div class="box">
                <div>Ic</div>
                <div>50</div>
              </div>
              <div class="name">电流</div>
            </div>
            <div class="connector">
              <span class="text">连接器</span>
            </div>
            <div class="template-box">
              <div class="connector">
                <span class="text">连接器</span>
              </div>
              <div class="T">
                <div>T(L1)</div>
                <div>30°C</div>
              </div>
              <div class="T">
                <div>T(L2)</div>
                <div>30°C</div>
              </div>
              <div class="T">
                <div>T(L3)</div>
                <div>30°C</div>
              </div>
              <div class="T">
                <div>T(N)</div>
                <div>30°C</div>
              </div>
            </div>
            <div class="plugin-box">
              <div class="box">
                <div>Ia</div>
                <div>30</div>
              </div>
              <div class="box">
                <div>Ib</div>
                <div>40</div>
              </div>
              <div class="box">
                <div>Ic</div>
                <div>50</div>
              </div>
              <div class="name">电流</div>
            </div>
          </div>
        </div>
      </div>
      <div class="RightBus">
        <div class="busListContainer">
          <div class="bridge"></div>
          <div class="busList">
            <div class="plugin-box">
              <div class="box">
                <div>Ia</div>
                <div>30</div>
              </div>
              <div class="box">
                <div>Ib</div>
                <div>40</div>
              </div>
              <div class="box">
                <div>Ic</div>
                <div>50</div>
              </div><div class="name">电流</div>
              <div id="plugin-A-2" class="pointA"></div>
              <div id="plugin-B-2" class="pointB"></div>
              <div id="plugin-C-2" class="pointC"></div>
            </div>
            <div class="plugin-box">
              <div class="box">
                <div>Ia</div>
                <div>30</div>
              </div>
              <div class="box">
                <div>Ib</div>
                <div>40</div>
              </div>
              <div class="box">
                <div>Ic</div>
                <div>50</div>
              </div>
              <div class="name">电流</div>
            </div>
            <div class="connector">
              <span class="text">连接器</span>
            </div>
            <div class="template-box">
              <div class="connector">
                <span class="text">连接器</span>
              </div>
              <div class="T">
                <div>T(L1)</div>
                <div>30°C</div>
              </div>
              <div class="T">
                <div>T(L2)</div>
                <div>30°C</div>
              </div>
              <div class="T">
                <div>T(L3)</div>
                <div>30°C</div>
              </div>
              <div class="T">
                <div>T(N)</div>
                <div>30°C</div>
              </div>
            </div>
            <div class="plugin-box">
              <div class="box">
                <div>Ia</div>
                <div>30</div>
              </div>
              <div class="box">
                <div>Ib</div>
                <div>40</div>
              </div>
              <div class="box">
                <div>Ic</div>
                <div>50</div>
              </div>
              <div class="name">电流</div>
            </div>
          </div>
        </div>
        <div class="startBus">
          始端箱
        </div>
      </div>
      <div class="cabinetContainer">
        <div class="cabinetList">
          <div class="cabinet">
            <div class="inner"></div>
            <div id="cab-A-1" class="leftPoint"></div>
            <div id="cab-B-1" class="rightPoint"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div id="cab-A-2" class="leftPoint"></div>
            <div id="cab-B-2" class="rightPoint"></div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div id="cab-A-3" class="leftPoint"></div>
            <div id="cab-B-3" class="rightPoint"></div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
          <div class="cabinet">
            <div class="inner"></div>
            <div class="status">机柜1</div>
          </div>
        </div>
      </div>
    </div>
    
  </ContentWrap>
  <!-- 添加或修改用户对话框 -->
  <ColumForm ref="ColumForm" @success="saveMachine" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance } from '@jsplumb/browser-ui'

let instance: BrowserJsPlumbInstance | null = null

const btns = [
  {
    value: 0,
    name: '负荷'
  },
  {
    value: 1,
    name: '电流'
  },
  {
    value: 2,
    name: '电压'
  },
  {
    value: 3,
    name: '功率因素'
  },
  {
    value: 4,
    name: '有功功率'
  },
  {
    value: 5,
    name: '无功功率'
  },
  {
    value: 6,
    name: '视在功率'
  },
  {
    value: 7,
    name: '供电平衡'
  },
  {
    value: 8,
    name: '温度'
  },
  {
    value: 9,
    name: '容量'
  },
  {
    value: 10,
    name: '用能'
  },
]
const topologyContainer = ref()
const chosenBtn = ref(0)

const initConnect = () => {
  // 创建实例
  instance = newInstance({
    container: topologyContainer.value
  })
}

const toCreatConnect = () => {
  const sourceEle = document.getElementById('plugin-A-1') as Element
  const targetEle = document.getElementById('cab-A-1') as Element
  const sourceEle1 = document.getElementById('plugin-B-1') as Element
  const targetEle1 = document.getElementById('cab-A-2') as Element
  const sourceEle2 = document.getElementById('plugin-C-1') as Element
  const targetEle2 = document.getElementById('cab-A-3') as Element
  const sourceEle21 = document.getElementById('plugin-A-2') as Element
  const targetEle22 = document.getElementById('cab-B-1') as Element
  const sourceEle23 = document.getElementById('plugin-B-2') as Element
  const targetEle24 = document.getElementById('cab-B-2') as Element
  const sourceEle25 = document.getElementById('plugin-C-2') as Element
  const targetEle26 = document.getElementById('cab-B-3') as Element
  // 进行连线
  instance?.connect({
    source: sourceEle,
    target: targetEle,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#ccc',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle1,
    target: targetEle1,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#ccc',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle2,
    target: targetEle2,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#ccc',
      dashstyle: '5 5'
    }
  })
  // 进行连线
  instance?.connect({
    source: sourceEle21,
    target: targetEle22,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#bb0000',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle23,
    target: targetEle24,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#bb0000',
      dashstyle: '5 5'
    }
  })
  instance?.connect({
    source: sourceEle25,
    target: targetEle26,
    paintStyle: {
      strokeWidth: 1,
      stroke: '#bb0000',
      dashstyle: '5 5'
    }
  })
}

const switchBtn = (value) => {
  chosenBtn.value = value
  console.log('switchBtn', )
}

onMounted(() => {
  initConnect()
  toCreatConnect()
})
</script>

<style lang="scss" scoped>
.btn-main {
  display: flex;
  justify-content: space-between;
}
// .btns {
//   display: flex;
//   justify-content: flex-end;
// }
.LeftBus,
.RightBus {
  display: flex;
  .startBus {
    width: 66px;
    height: 66px;
    border: 1px solid #999;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: silver;
    box-shadow: 0 0 10px silver;
  }
  .busListContainer {
    flex: 1;
    .bridge {
      height: 12px;
      background-color: rgb(155, 166, 190);
      margin-top: 27px;
    }
    .busList {
      display: flex;
      justify-content: space-around;
      font-size: 13px;
      .plugin-box {
        position: relative;
        height: fit-content;
        font-size: 12px;
        display: flex;
        border: 1px solid;
        border-top: none;
        padding-top: 20px;
        background-color: #fff;
        .name {
          position: absolute;
          top: 2px;
          left: 18px;
        }
        .box {
          display: flex;
          flex-direction: column;
          align-items: center;
          margin: 0 3px;
        }
        .line {
          width: 5px;
          height: 100px;
          background-color: #000;
        }
        .pointA {
          position: absolute;
          bottom: -8px;
          left: 5px;
          border: 1px solid;
          width: 5px;
          height: 5px;
          border-radius: 50%;
        }
        .pointB {
          position: absolute;
          bottom: -8px;
          left: 28px;
          border: 1px solid;
          width: 5px;
          height: 5px;
          border-radius: 50%;
        }
        .pointC {
          position: absolute;
          bottom: -8px;
          right: 5px;
          border: 1px solid;
          width: 5px;
          height: 5px;
          border-radius: 50%;
        }
      }
      .connector {
        position: relative;
        height: 20px;
        width: 50px;
        border-radius: 2px;
        background-color: #E6E6E6;
        box-shadow: 0 0 10px #E6E6E6;
        margin-top: -16px;
        .text {
          position: absolute;
          bottom: 24px;
          left: 5px;
        }
      }
      .template-box {
        position: relative;
        width: 75px;
        font-size: 12px;
        border: 1px solid #999;
        border-top: none;
        background-color: rgb(252, 252, 252);
        display: flex;
        flex-wrap: wrap;
        .connector {
          margin: -16px auto 0;
        }
        .T {
          width: 50%;
          box-sizing: border-box;
          display: flex;
          flex-direction: column;
          align-items: center;
        }
      }
    }
  }
}
.RightBus {
  // margin-top: -100px;
  .busListContainer {
    .busList {
      .plugin-box {
        .line {
          height: 25px
        }
      }
    }
  }
}
.cabinetContainer {
  margin-top: 50px;
  padding: 20px 0;
  .cabinetList {
    width: 100%;
    display: flex;
    justify-content: space-between;
    .cabinet {
      position: relative;
      // min-width: 110px;
      flex: 1;
      height: 350px;
      box-sizing: border-box;
      border: 2px solid;
      .inner {
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #888;
        background-color: #E6E6E6;
      }
      .status {
        font-size: 12px;
        line-height: 1;
        color: #000;
        text-align: center;
        margin-top: 10px;
      }
    }
    .leftPoint {
      position: absolute;
      left: 20px;
      top: -8px;
      width: 5px;
      height: 5px;
      border: 1px solid;
      border-radius: 50%;
    }
    .rightPoint {
      position: absolute;
      right: 20px;
      top: -8px;
      width: 5px;
      height: 5px;
      border: 1px solid;
      border-radius: 50%;
    }
  }
}
</style>