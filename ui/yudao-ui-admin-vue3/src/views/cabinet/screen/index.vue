<template>
  <ContentWrap>
    <div class="screenContiner">
      <div class="deviceList">
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
      </div>
      <div class="machine">
        <div class="mainBorder">
          <div class="main">
            <div class="Uitem"></div>
            <div class="Uitem"></div>
            <div class="Uitem"></div>
            <div class="Uitem"></div>
            <div class="Uitem"></div>
            <div class="Uitem active"></div>
            <div class="Uitem active"></div>
            <div class="Uitem"></div>
            <div class="Uitem active"></div>
            <div class="Uitem"></div>
            <div class="Uitem"></div>
            <div class="Uitem"></div>
            <div class="Uitem"></div>
          </div>
        </div>
        <div class="base"></div>
      </div>
      <div class="deviceList">
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
        <div class="device">
          <div class="name">设备名称： 127.0.0.1</div>
          <div class="info">
            <div>型号：S15SF2-49T5-QW</div>
            <div>占用：1</div>
          </div>
        </div>
      </div>
    </div>
    <div class="infomation">
      <div class="infoItem">
        <span class="num">42</span>
        <span>空间总容量</span>
      </div>
      <div class="line"></div>
      <div class="infoItem">
        <span class="num">30U</span>
        <span>已用空间</span>
      </div>
      <div class="line"></div>
      <div class="infoItem">
        <span class="num">12U</span>
        <span>未用空间</span>
      </div>
      <div class="line"></div>
      <div class="infoItem">
        <span class="num">8</span>
        <span>设备总数</span>
      </div>
    </div>
  </ContentWrap>
</template>

<script lang="ts" setup>

</script>

<style lang="scss" scoped>
.screenContiner {
  position: relative;
  display: flex;
  justify-content: space-evenly;
  .deviceList {
    height: 588px;
    overflow: auto;
    &::-webkit-scrollbar {
      width: 0;
      height: 0;
    }
    .device {
      width: 310px;
      height: 130px;
      margin-bottom: 15px;
      font-size: 14px;
      border: 1px solid #eee;
      .name {
        padding: 10px 8px;
        // background-color: #eee;
        background: linear-gradient(to bottom right, #000c4248, #0004ff);
        
      }
      .info {
        padding: 0 8px;
        div {
          margin-top: 15px;
        }
      }
    }
  }
  .machine {
    position: relative;
    width: 300px;
    height: fit-content;
    box-sizing: border-box;
    &::before {
      content: "";
      position: absolute;
      bottom: -20px; /* 控制梯形的上方边长 */
      left: 20px;
      width: 12px;
      height: 0;
      border-bottom: 20px solid #90b8df; /* 控制梯形的底边长度和颜色 */
      border-left: 10px solid transparent; /* 控制梯形的左侧斜边 */
      border-right: 10px solid transparent; /* 控制梯形的右侧斜边 */
      transform: rotateX(180deg)
    }
    &::after {
      content: "";
      position: absolute;
      bottom: -20px; /* 控制梯形的上方边长 */
      right: 20px;
      width: 12px;
      height: 0;
      border-bottom: 20px solid #90b8df; /* 控制梯形的底边长度和颜色 */
      border-left: 10px solid transparent; /* 控制梯形的左侧斜边 */
      border-right: 10px solid transparent; /* 控制梯形的右侧斜边 */
      transform: rotateX(180deg)
    }
    .mainBorder {
      // height: calc(100% - 20px);
      background-color: #90b8df;
      box-sizing: border-box;
      padding: 20px 20px 50px 20px;
      .main {
        width: 100%;
        // height: 100%;
        box-sizing: border-box;
        padding: 10px 5px 10px 5px;
        background-color: #000;
        .Uitem {
          width: 100%;
          height: 30px;
          background-color: #fff;
          border-radius: 5px;
          margin-bottom: 5px;
        }
        .active {
          background-color: #5298df;
        }
      }
    }
    .base {
      height: 20px;
      background-color: #51677c;
    }
  }
}
.infomation {
  display: flex;
  justify-content: center;
  align-items: center;
  .infoItem {
    padding: 30px;
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