<template>
  <!-- <div style="height:100%;min-height: calc(100vh - 120px);display: flex; flex-direction: column;"> -->
    <ContentWrap>
    <div class="btn-main" style="justify-content:space-between">
      <div style="display: flex;flex-direction: column; width: 600px;">
        <div v-if="chosenBtn != 8" class="btns">
          <template v-for="(status, index) in statusList[chosenBtn]" :key="index">
            <div class="statusRadius" :style="{backgroundColor: status.color}"></div>{{status.name}}
            <!-- <el-button :size="isFromHome ? 'small' : 'default'" :style="`background-color: ${status.color};color: white;`">{{status.name}}</el-button> -->
          </template> 
        </div>
        <div v-else class="btns">
          前门:
          <template v-for="(status, index) in frontStatusList" :key="index">
            <div class="statusRadius" :style="{backgroundColor: status.color}"></div>{{status.name}}
            <!-- <el-button :size="isFromHome ? 'small' : 'default'" :style="`background-color: ${status.color};color: white;`">{{status.name}}</el-button> -->
          </template> 
        </div>
        <div v-if="chosenBtn == 8" class="btns" style="margin-top: 10px;">
          后门:
          <template v-for="(status, index) in blackStatusList" :key="index">
            <div class="statusRadius" :style="{backgroundColor: status.color}"></div>{{status.name}}
            <!-- <el-button :size="isFromHome ? 'small' : 'default'" :style="`background-color: ${status.color};color: white;`">{{status.name}}</el-button> -->
          </template> 
        </div>
      </div>
      <div class="flex items-center">
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" round color="#00778c" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div style="display:flex;">
        <div v-if="!isFromHome" style="margin-left: 10px">
          <el-button v-if="!editEnable" @click="handleEditClick" type="primary" color="black"><Icon icon="ep:edit" class="mr-5px" />编辑</el-button>
          <el-button v-if="editEnable" @click="handleCancel" type="primary" plain color="black">取消</el-button>
          <el-button v-if="editEnable" @click="handleConfig" type="primary" plain color="black">母线配置</el-button>
          <!-- <el-button v-if="editEnable" @click="handleSubmit" type="primary" plain color="black">保存</el-button> -->
        </div>
        <slot name="btn"></slot>
      </div>
    </div>
  </ContentWrap>
  <ContentWrap>
    <div ref="topologyContainer" class="topologyContainer" :style="`position: relative;z-index: 1;`" @click="showAllConnect">
      <div style="margin-top:-25px"></div>
      <div style="height:20px;"></div>
      <div class="Container" :style="{minHeight: isFromHome ? 'unset' : '600px'}">
        <div class="busContainer" :style="{alignItems: machineColInfo.pduBar && machineColInfo.barA ? 'unset' : 'center'}">
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus" @click.right="handleBarRightClick($event)">
            <el-tooltip effect="light" :hide-after="0">
              <template #content>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 50%">
                    母线名称：{{machineColInfo.barA.busName ? machineColInfo.barA.busName : ''}} <br/>
                    网络地址：{{machineColInfo.barA.devIp ? machineColInfo.barA.devIp+'-'+ machineColInfo.barA.barId : ''}}<br/>
                    软件版本号：{{machineColInfo.barA.busVersion ? machineColInfo.barA.busVersion : ''}}<br/>
                    断路器状态：{{machineColInfo.barA.breakerStatus ? breakerStatusList[machineColInfo.barA.breakerStatus] : ''}}<br/>
                    剩余电流：{{machineColInfo.barA.curResidualValueTotal ? machineColInfo.barA.curResidualValueTotal.toFixed(2) : '0.00'}}A<br/>
                    防雷状态：{{machineColInfo.barA.lspStatus ? lspStatusList[machineColInfo.barA.lspStatus] : ''}}<br/>
                    突变频率：{{machineColInfo.barA.hzValue ? machineColInfo.barA.hzValue : ''}}
                  </div>
                  <div style="width: 50%">
                    始端箱状态：{{machineColInfo.barA.status ? barStatusList[machineColInfo.barA.status] : ''}} <br/>
                    总功率因数：{{machineColInfo.barA.powerFactorTotal ? machineColInfo.barA.powerFactorTotal.toFixed(2) : '0.00'}}<br/>
                    总有功功率：{{machineColInfo.barA.powValueTotal ? machineColInfo.barA.powValueTotal.toFixed(3) : '0.000'}}kW<br/>
                    总无功功率：{{machineColInfo.barA.powReactiveTotal ? machineColInfo.barA.powReactiveTotal.toFixed(3) : '0.000'}}kVar<br/>
                    总视在功率：{{machineColInfo.barA.powApparentTotal ? machineColInfo.barA.powApparentTotal.toFixed(3) : '0.000'}}kVA<br/>
                    电压不平衡：{{machineColInfo.barA.volUnbalance ? machineColInfo.barA.volUnbalance.toFixed(2) : '0.00'}}%<br/>
                    电流不平衡：{{machineColInfo.barA.curUnbalance ? machineColInfo.barA.curUnbalance.toFixed(2) : '0.00'}}%
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    <br/>
                    负载率：<br/>
                    相电流：<br/>
                    相电压：<br/>
                    线电压：<br/>
                    有功功率：<br/>
                    无功功率：<br/>
                    视在功率：<br/>
                    功率因数：<br/>
                    电压谐波：<br/>
                    电流谐波：
                  </div>
                  <div style="width: 25%">
                    A相<br/>
                    {{machineColInfo.barA.lineLoadRate?.[0] ? machineColInfo.barA.lineLoadRate[0].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barA.lineCur?.[0] ? machineColInfo.barA.lineCur[0].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barA.lineVol?.[0] ? machineColInfo.barA.lineVol[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.volLineValue?.[0] ? machineColInfo.barA.volLineValue[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.powActive?.[0] ? machineColInfo.barA.powActive[0].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barA.powReactive?.[0] ? machineColInfo.barA.powReactive[0].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barA.powApparent?.[0] ? machineColInfo.barA.powApparent[0].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barA.powerFactor?.[0] ? machineColInfo.barA.powerFactor[0].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.volThd?.[0] ? (machineColInfo.barA.volThd[0]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.curThd?.[0] ? (machineColInfo.barA.curThd[0]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    B相<br/>
                    {{machineColInfo.barA.lineLoadRate?.[1] ? machineColInfo.barA.lineLoadRate[1].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barA.lineCur?.[1] ? machineColInfo.barA.lineCur[1].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barA.lineVol?.[1] ? machineColInfo.barA.lineVol[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.volLineValue?.[1] ? machineColInfo.barA.volLineValue[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.powActive?.[1] ? machineColInfo.barA.powActive[1].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barA.powReactive?.[1] ? machineColInfo.barA.powReactive[1].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barA.powApparent?.[1] ? machineColInfo.barA.powApparent[1].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barA.powerFactor?.[1] ? machineColInfo.barA.powerFactor[1].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.volThd?.[1] ? (machineColInfo.barA.volThd[1]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.curThd?.[1] ? (machineColInfo.barA.curThd[1]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    C相<br/>
                    {{machineColInfo.barA.lineLoadRate?.[2] ? machineColInfo.barA.lineLoadRate[2].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barA.lineCur?.[2] ? machineColInfo.barA.lineCur[2].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barA.lineVol?.[2] ? machineColInfo.barA.lineVol[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.volLineValue?.[2] ? machineColInfo.barA.volLineValue[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.powActive?.[2] ? machineColInfo.barA.powActive[2].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barA.powReactive?.[2] ? machineColInfo.barA.powReactive[2].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barA.powApparent?.[2] ? machineColInfo.barA.powApparent[2].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barA.powerFactor?.[2] ? machineColInfo.barA.powerFactor[2].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.volThd?.[2] ? (machineColInfo.barA.volThd[2]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.curThd?.[2] ? (machineColInfo.barA.curThd[2]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    A相温度： {{machineColInfo.barA.temData?.[0] ? machineColInfo.barA.temData[0].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    B相温度：{{machineColInfo.barA.temData?.[1] ? machineColInfo.barA.temData[1].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    C相温度：{{machineColInfo.barA.temData?.[2] ? machineColInfo.barA.temData[2].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    N相温度：{{machineColInfo.barA.temData?.[3] ? machineColInfo.barA.temData[3].toFixed(0) : '0'}}°C<br/>
                  </div>
                </div>
              </template> 
              <div class="startBus" v-if="!machineColInfo.barA.direction" @dblclick="handleInitialDblick($event, 'A')" style="background-color: #43939c" @click.stop="showInitialConnect('A')">
                <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" :btns="btns" />
                <div style="background-color: white;color: black;position: absolute;bottom: -20px;left: 50%;transform: translateX(-50%);">A路母线</div>
              </div>
              <div v-else></div>
            </el-tooltip>
            <el-tooltip effect="light" :hide-after="0">
              <template #content>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 50%">
                    母线名称：{{machineColInfo.barB.busName ? machineColInfo.barB.busName : ''}} <br/>
                    网络地址：{{machineColInfo.barB.devIp ? machineColInfo.barB.devIp+'-'+ machineColInfo.barB.barId : ''}}<br/>
                    软件版本号：{{machineColInfo.barB.busVersion ? machineColInfo.barB.busVersion : ''}}<br/>
                    断路器状态：{{machineColInfo.barB.breakerStatus ? breakerStatusList[machineColInfo.barB.breakerStatus] : ''}}<br/>
                    剩余电流：{{machineColInfo.barB.curResidualValueTotal ? machineColInfo.barB.curResidualValueTotal.toFixed(2) : '0.00'}}A<br/>
                    防雷状态：{{machineColInfo.barB.lspStatus ? lspStatusList[machineColInfo.barB.lspStatus] : ''}}<br/>
                    突变频率：{{machineColInfo.barB.hzValue ? machineColInfo.barB.hzValue : ''}}
                  </div>
                  <div style="width: 50%">
                    始端箱状态：{{machineColInfo.barB.status ? barStatusList[machineColInfo.barB.status] : ''}} <br/>
                    总功率因数：{{machineColInfo.barB.powerFactorTotal ? machineColInfo.barB.powerFactorTotal.toFixed(2) : '0.00'}}<br/>
                    总有功功率：{{machineColInfo.barB.powValueTotal ? machineColInfo.barB.powValueTotal.toFixed(3) : '0.000'}}kW<br/>
                    总无功功率：{{machineColInfo.barB.powReactiveTotal ? machineColInfo.barB.powReactiveTotal.toFixed(3) : '0.000'}}kVar<br/>
                    总视在功率：{{machineColInfo.barB.powApparentTotal ? machineColInfo.barB.powApparentTotal.toFixed(3) : '0.000'}}kVA<br/>
                    电压不平衡：{{machineColInfo.barB.volUnbalance ? machineColInfo.barB.volUnbalance.toFixed(2) : '0.00'}}%<br/>
                    电流不平衡：{{machineColInfo.barB.curUnbalance ? machineColInfo.barB.curUnbalance.toFixed(2) : '0.00'}}%
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    <br/>
                    负载率：<br/>
                    相电流：<br/>
                    相电压：<br/>
                    线电压：<br/>
                    有功功率：<br/>
                    无功功率：<br/>
                    视在功率：<br/>
                    功率因数：<br/>
                    电压谐波：<br/>
                    电流谐波：
                  </div>
                  <div style="width: 25%">
                    A相<br/>
                    {{machineColInfo.barB.lineLoadRate?.[0] ? machineColInfo.barB.lineLoadRate[0].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barB.lineCur?.[0] ? machineColInfo.barB.lineCur[0].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barB.lineVol?.[0] ? machineColInfo.barB.lineVol[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.volLineValue?.[0] ? machineColInfo.barB.volLineValue[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.powActive?.[0] ? machineColInfo.barB.powActive[0].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barB.powReactive?.[0] ? machineColInfo.barB.powReactive[0].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barB.powApparent?.[0] ? machineColInfo.barB.powApparent[0].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barB.powerFactor?.[0] ? machineColInfo.barB.powerFactor[0].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.volThd?.[0] ? (machineColInfo.barB.volThd[0]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.curThd?.[0] ? (machineColInfo.barB.curThd[0]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    B相<br/>
                    {{machineColInfo.barB.lineLoadRate?.[1] ? machineColInfo.barB.lineLoadRate[1].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barB.lineCur?.[1] ? machineColInfo.barB.lineCur[1].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barB.lineVol?.[1] ? machineColInfo.barB.lineVol[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.volLineValue?.[1] ? machineColInfo.barB.volLineValue[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.powActive?.[1] ? machineColInfo.barB.powActive[1].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barB.powReactive?.[1] ? machineColInfo.barB.powReactive[1].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barB.powApparent?.[1] ? machineColInfo.barB.powApparent[1].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barB.powerFactor?.[1] ? machineColInfo.barB.powerFactor[1].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.volThd?.[1] ? (machineColInfo.barB.volThd[1]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.curThd?.[1] ? (machineColInfo.barB.curThd[1]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    C相<br/>
                    {{machineColInfo.barB.lineLoadRate?.[2] ? machineColInfo.barB.lineLoadRate[2].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barB.lineCur?.[2] ? machineColInfo.barB.lineCur[2].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barB.lineVol?.[2] ? machineColInfo.barB.lineVol[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.volLineValue?.[2] ? machineColInfo.barB.volLineValue[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.powActive?.[2] ? machineColInfo.barB.powActive[2].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barB.powReactive?.[2] ? machineColInfo.barB.powReactive[2].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barB.powApparent?.[2] ? machineColInfo.barB.powApparent[2].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barB.powerFactor?.[2] ? machineColInfo.barB.powerFactor[2].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.volThd?.[2] ? (machineColInfo.barB.volThd[2]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.curThd?.[2] ? (machineColInfo.barB.curThd[2]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    A相温度： {{machineColInfo.barB.temData?.[0] ? machineColInfo.barB.temData[0].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    B相温度：{{machineColInfo.barB.temData?.[1] ? machineColInfo.barB.temData[1].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    C相温度：{{machineColInfo.barB.temData?.[2] ? machineColInfo.barB.temData[2].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    N相温度：{{machineColInfo.barB.temData?.[3] ? machineColInfo.barB.temData[3].toFixed(0) : '0'}}°C<br/>
                  </div>
                </div>
              </template> 
              <div class="startBus" v-if="!machineColInfo.barB.direction" @dblclick="handleInitialDblick($event, 'B')" style="background-color: #acd997" @click.stop="showInitialConnect('B')">
                <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barB" :btns="btns" />
                <div style="background-color: white;color: black;position: absolute;bottom: -20px;left: 50%;transform: translateX(-50%);">B路母线</div>
              </div>
            </el-tooltip>
              <!-- <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" /> -->
            <div class="maskPoint1"></div>
            <div class="maskPoint2"></div>
            <div class="menu" v-if="operateMenuBox.show && editEnable && operateMenuBox.type == 'bar'" :style="{left: `${operateMenuBox.left}`, top: `${operateMenuBox.top}`}">
              <div class="menu_item" @click="handleBarOperate('edit')">编辑</div>
              <div class="menu_item" @click="handleBarOperate('delete')">删除</div>
            </div>
          </div>
          <div class="main">
            <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="busListContainer" @click.right="handlePluginRightClick($event, 'A')">
              <div class="bridge"></div>
              <div class="busList1">
                <template v-for="(bus, index) in machineColInfo.barA.boxList" :key="index">
                  <el-tooltip effect="light" :hide-after="0">
                    <template #content>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 33%">
                          基本信息：<br/>
                          网络地址：{{bus.boxKey}}<br/>
                          插接箱名称：{{bus.boxName}}<br/>
                          软件版本号：{{bus.boxVersion}}<br/>
                          断路器状态：{{bus.breakerStatus ? breakerStatusList[bus.breakerStatus] : ''}}
                        </div>
                        <div style="width: 33%">
                          <br/>
                          插接箱状态：{{bus.status ? barStatusList[bus.status] : ''}}<br/>
                          电压不平衡：{{bus.volUnbalance ? bus.volUnbalance.toFixed(2) : '0.00'}}%<br/>
                          电流不平衡：{{bus.curUnbalance ? bus.curUnbalance.toFixed(2) : '0.00'}}%<br/>
                          回路数量：{{bus.loopNum}}
                        </div>
                        <div style="width: 33%">
                          <br/>
                          总功率因数：{{bus.powerFactorTotal ? bus.powerFactorTotal.toFixed(2) : '0.00'}}<br/>
                          总有功功率：{{bus.powValueTotal ? bus.powValueTotal.toFixed(3) : '0.000'}}kW<br/>
                          总无功功率：{{bus.powReactiveTotal ? bus.powReactiveTotal.toFixed(3) : '0.000'}}kVar<br/>
                          总视在功率：{{bus.powApparentTotal ? bus.powApparentTotal.toFixed(3) : '0.000'}}kVA
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        插接位：
                      </div>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 13%">
                          <br/>
                          有功功率：<br/>
                          无功功率：<br/>
                          视在功率：<br/>
                          功率因数：
                        </div>
                        <div style="width: 29%">
                          输出位1<br/>
                          {{bus.powActive?.[0] ? bus.powActive[0].toFixed(3) : '0.000'}}kW<br/>
                          {{bus.powReactive?.[0] ? bus.powReactive[0].toFixed(3) : '0.000'}}kVar<br/>
                          {{bus.powApparent?.[0] ? bus.powApparent[0].toFixed(3) : '0.000'}}kVA<br/>
                          {{bus.powerFactor?.[0] ? bus.powerFactor[0].toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          输出位2<br/>
                          {{bus.powActive?.[1] ? bus.powActive[1].toFixed(3) : '0.000'}}kW<br/>
                          {{bus.powReactive?.[1] ? bus.powReactive[1].toFixed(3) : '0.000'}}kVar<br/>
                          {{bus.powApparent?.[1] ? bus.powApparent[1].toFixed(3) : '0.000'}}kVA<br/>
                          {{bus.powerFactor?.[1] ? bus.powerFactor[1].toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          输出位3<br/>
                          {{bus.powActive?.[2] ? bus.powActive[2].toFixed(3) : '0.000'}}kW<br/>
                          {{bus.powReactive?.[2] ? bus.powReactive[2].toFixed(3) : '0.000'}}kVar<br/>
                          {{bus.powApparent?.[2] ? bus.powApparent[2].toFixed(3) : '0.000'}}kVA<br/>
                          {{bus.powerFactor?.[2] ? bus.powerFactor[2].toFixed(2) : '0.00'}}
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        输入相：
                      </div>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 13%">
                          <br/>
                          负载率：<br/>
                          相电流：<br/>
                          相电压：<br/>
                          电流谐波：
                        </div>
                        <div style="width: 29%">
                          A相<br/>
                          {{bus.lineLoadRate?.[0] ? bus.lineLoadRate[0].toFixed(0) : '0'}}%<br/>
                          {{bus.lineCur?.[0] ? bus.lineCur[0].toFixed(2) : '0.00'}}A<br/>
                          {{bus.lineVol?.[0] ? bus.lineVol[0].toFixed(1) : '0.0'}}V<br/>
                          {{bus.curThd?.[0] ? (bus.curThd[0]/100).toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          B相<br/>
                          {{bus.lineLoadRate?.[1] ? bus.lineLoadRate[1].toFixed(0) : '0'}}%<br/>
                          {{bus.lineCur?.[1] ? bus.lineCur[1].toFixed(2) : '0.00'}}A<br/>
                          {{bus.lineVol?.[1] ? bus.lineVol[1].toFixed(1) : '0.0'}}V<br/>
                          {{bus.curThd?.[1] ? (bus.curThd[1]/100).toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          C相<br/>
                          {{bus.lineLoadRate?.[2] ? bus.lineLoadRate[2].toFixed(0) : '0'}}%<br/>
                          {{bus.lineCur?.[2] ? bus.lineCur[2].toFixed(2) : '0.00'}}A<br/>
                          {{bus.lineVol?.[2] ? bus.lineVol[2].toFixed(1) : '0.0'}}V<br/>
                          {{bus.curThd?.[2] ? (bus.curThd[2]/100).toFixed(2) : '0.00'}}
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        温度：
                      </div>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 25%">
                          A相温度：{{bus.temData?.[0] ? bus.temData[0].toFixed(0) : '0'}}°C
                        </div>
                        <div style="width: 25%">
                          B相温度：{{bus.temData?.[1] ? bus.temData[1].toFixed(0) : '0'}}°C
                        </div>
                        <div style="width: 25%">
                          C相温度：{{bus.temData?.[2] ? bus.temData[2].toFixed(0) : '0'}}°C
                        </div>
                        <div style="width: 25%">
                          N相温度：{{bus.temData?.[3] ? bus.temData[3].toFixed(0) : '0'}}°C
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 33%">
                          回路电流：<br/>
                          C1：{{bus.curValueLoop?.[0] ? bus.curValueLoop[0].toFixed(2) : '0.00'}}A<br/>
                          C4：{{bus.curValueLoop?.[3] ? bus.curValueLoop[3].toFixed(2) : '0.00'}}A<br/>
                          C7：{{bus.curValueLoop?.[6] ? bus.curValueLoop[6].toFixed(2) : '0.00'}}A
                        </div>
                        <div style="width: 33%">
                          <br/>
                          C2：{{bus.curValueLoop?.[1] ? bus.curValueLoop[1].toFixed(2) : '0.00'}}A<br/>
                          C5：{{bus.curValueLoop?.[4] ? bus.curValueLoop[4].toFixed(2) : '0.00'}}A<br/>
                          C8：{{bus.curValueLoop?.[7] ? bus.curValueLoop[7].toFixed(2) : '0.00'}}A
                        </div>
                        <div style="width: 33%">
                          <br/>
                          C3：{{bus.curValueLoop?.[2] ? bus.curValueLoop[2].toFixed(2) : '0.00'}}A<br/>
                          C6：{{bus.curValueLoop?.[5] ? bus.curValueLoop[5].toFixed(2) : '0.00'}}A<br/>
                          C9：{{bus.curValueLoop?.[8] ? bus.curValueLoop[8].toFixed(2) : '0.00'}}A
                        </div>
                      </div>
                    </template>
                    <!-- 插接箱 -->
                    <div v-if="bus.type == 0 || bus.outletNum > 0" class="plugin-box" :id="`box-${index}`" @dblclick="handlePluginDblick($event, 'A')" @click.stop="showPluginConnect('A',bus.boxIndex)">
                      <PluginBox :chosenBtn="chosenBtn" :pluginData="bus" :btns="btns" />
                      <div class="pointContainer">
                        <div v-for="pointIndex in bus.outletNum" :key="pointIndex" class="point" :id="`plugin-${bus.boxIndex}_A-${pointIndex}`"></div>
                      </div>
                    </div>
                    <!-- 连接器 -->
                    <div v-else class="template-box" :id="`box-${index}`">
                      <div class="connector">
                        <span class="text">连接器</span>
                      </div>
                      <div v-if="chosenBtn == 8 && bus.temData" class="Tbox">
                        <div v-for="(tmp, count) in bus.temData" :key="count" class="T">
                          <div>T(L{{count}})</div>
                          <div>{{tmp}}°C</div>
                        </div>
                      </div>
                    </div>
                  </el-tooltip>
                </template>
              </div>
              <div class="menu" v-if="operateMenuBox.show && editEnable && operateMenuBox.type == 'A'" :style="{left: `${operateMenuBox.left}`, top: `${operateMenuBox.top}`}">
                <div class="menu_item" @click="handleBoxOperate('edit', 'A')">编辑</div>
                <div class="menu_item" @click="handleBoxOperate('delete', 'A')">删除</div>
              </div>
            </div>
            <div v-if="machineColInfo.pduBar && machineColInfo.barB" class="busListContainer" @click.right="handlePluginRightClick($event, 'B')" style="margin-bottom: 80px">
              <div class="bridge"></div>
              <div class="busList2">
                <template v-for="(bus, index) in machineColInfo.barB.boxList" :key="index">
                  <el-tooltip effect="light" :hide-after="0">
                    <template #content>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 33%">
                          基本信息：<br/>
                          网络地址：{{bus.boxKey}}<br/>
                          插接箱名称：{{bus.boxName}}<br/>
                          软件版本号：{{bus.boxVersion}}<br/>
                          断路器状态：{{bus.breakerStatus ? breakerStatusList[bus.breakerStatus] : ''}}
                        </div>
                        <div style="width: 33%">
                          <br/>
                          插接箱状态：{{bus.status ? barStatusList[bus.status] : ''}}<br/>
                          电压不平衡：{{bus.volUnbalance ? bus.volUnbalance.toFixed(2) : '0.00'}}%<br/>
                          电流不平衡：{{bus.curUnbalance ? bus.curUnbalance.toFixed(2) : '0.00'}}%<br/>
                          回路数量：{{bus.loopNum}}
                        </div>
                        <div style="width: 33%">
                          <br/>
                          总功率因数：{{bus.powerFactorTotal ? bus.powerFactorTotal.toFixed(2) : '0.00'}}<br/>
                          总有功功率：{{bus.powValueTotal ? bus.powValueTotal.toFixed(3) : '0.000'}}kW<br/>
                          总无功功率：{{bus.powReactiveTotal ? bus.powReactiveTotal.toFixed(3) : '0.000'}}kVar<br/>
                          总视在功率：{{bus.powApparentTotal ? bus.powApparentTotal.toFixed(3) : '0.000'}}kVA
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        插接位：
                      </div>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 13%">
                          <br/>
                          有功功率：<br/>
                          无功功率：<br/>
                          视在功率：<br/>
                          功率因数：
                        </div>
                        <div style="width: 29%">
                          输出位1<br/>
                          {{bus.powActive?.[0] ? bus.powActive[0].toFixed(3) : '0.000'}}kW<br/>
                          {{bus.powReactive?.[0] ? bus.powReactive[0].toFixed(3) : '0.000'}}kVar<br/>
                          {{bus.powApparent?.[0] ? bus.powApparent[0].toFixed(3) : '0.000'}}kVA<br/>
                          {{bus.powerFactor?.[0] ? bus.powerFactor[0].toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          输出位2<br/>
                          {{bus.powActive?.[1] ? bus.powActive[1].toFixed(3) : '0.000'}}kW<br/>
                          {{bus.powReactive?.[1] ? bus.powReactive[1].toFixed(3) : '0.000'}}kVar<br/>
                          {{bus.powApparent?.[1] ? bus.powApparent[1].toFixed(3) : '0.000'}}kVA<br/>
                          {{bus.powerFactor?.[1] ? bus.powerFactor[1].toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          输出位3<br/>
                          {{bus.powActive?.[2] ? bus.powActive[2].toFixed(3) : '0.000'}}kW<br/>
                          {{bus.powReactive?.[2] ? bus.powReactive[2].toFixed(3) : '0.000'}}kVar<br/>
                          {{bus.powApparent?.[2] ? bus.powApparent[2].toFixed(3) : '0.000'}}kVA<br/>
                          {{bus.powerFactor?.[2] ? bus.powerFactor[2].toFixed(2) : '0.00'}}
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        输入相：
                      </div>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 13%">
                          <br/>
                          负载率：<br/>
                          相电流：<br/>
                          相电压：<br/>
                          电流谐波：
                        </div>
                        <div style="width: 29%">
                          A相<br/>
                          {{bus.lineLoadRate?.[0] ? bus.lineLoadRate[0].toFixed(0) : '0'}}%<br/>
                          {{bus.lineCur?.[0] ? bus.lineCur[0].toFixed(2) : '0.00'}}A<br/>
                          {{bus.lineVol?.[0] ? bus.lineVol[0].toFixed(1) : '0.0'}}V<br/>
                          {{bus.curThd?.[0] ? (bus.curThd[0]/100).toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          B相<br/>
                          {{bus.lineLoadRate?.[1] ? bus.lineLoadRate[1].toFixed(0) : '0'}}%<br/>
                          {{bus.lineCur?.[1] ? bus.lineCur[1].toFixed(2) : '0.00'}}A<br/>
                          {{bus.lineVol?.[1] ? bus.lineVol[1].toFixed(1) : '0.0'}}V<br/>
                          {{bus.curThd?.[1] ? (bus.curThd[1]/100).toFixed(2) : '0.00'}}
                        </div>
                        <div style="width: 29%">
                          C相<br/>
                          {{bus.lineLoadRate?.[2] ? bus.lineLoadRate[2].toFixed(0) : '0'}}%<br/>
                          {{bus.lineCur?.[2] ? bus.lineCur[2].toFixed(2) : '0.00'}}A<br/>
                          {{bus.lineVol?.[2] ? bus.lineVol[2].toFixed(1) : '0.0'}}V<br/>
                          {{bus.curThd?.[2] ? (bus.curThd[2]/100).toFixed(2) : '0.00'}}
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        温度：
                      </div>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 25%">
                          A相温度：{{bus.temData?.[0] ? bus.temData[0].toFixed(0) : '0'}}°C
                        </div>
                        <div style="width: 25%">
                          B相温度：{{bus.temData?.[1] ? bus.temData[1].toFixed(0) : '0'}}°C
                        </div>
                        <div style="width: 25%">
                          C相温度：{{bus.temData?.[2] ? bus.temData[2].toFixed(0) : '0'}}°C
                        </div>
                        <div style="width: 25%">
                          N相温度：{{bus.temData?.[3] ? bus.temData[3].toFixed(0) : '0'}}°C
                        </div>
                      </div>
                      <hr/>
                      <div class="flex justify-between" style="width: 25vw">
                        <div style="width: 33%">
                          回路电流：<br/>
                          C1：{{bus.curValueLoop?.[0] ? bus.curValueLoop[0].toFixed(2) : '0.00'}}A<br/>
                          C4：{{bus.curValueLoop?.[3] ? bus.curValueLoop[3].toFixed(2) : '0.00'}}A<br/>
                          C7：{{bus.curValueLoop?.[6] ? bus.curValueLoop[6].toFixed(2) : '0.00'}}A
                        </div>
                        <div style="width: 33%">
                          <br/>
                          C2：{{bus.curValueLoop?.[1] ? bus.curValueLoop[1].toFixed(2) : '0.00'}}A<br/>
                          C5：{{bus.curValueLoop?.[4] ? bus.curValueLoop[4].toFixed(2) : '0.00'}}A<br/>
                          C8：{{bus.curValueLoop?.[7] ? bus.curValueLoop[7].toFixed(2) : '0.00'}}A
                        </div>
                        <div style="width: 33%">
                          <br/>
                          C3：{{bus.curValueLoop?.[2] ? bus.curValueLoop[2].toFixed(2) : '0.00'}}A<br/>
                          C6：{{bus.curValueLoop?.[5] ? bus.curValueLoop[5].toFixed(2) : '0.00'}}A<br/>
                          C9：{{bus.curValueLoop?.[8] ? bus.curValueLoop[8].toFixed(2) : '0.00'}}A
                        </div>
                      </div>
                    </template>
                    <!-- 插接箱 -->
                    <div v-if="bus.type == 0 || bus.outletNum > 0" class="plugin-box" :id="`box-${index}`" @dblclick="handlePluginDblick($event, 'B')" @click.stop="showPluginConnect('B',bus.boxIndex)">
                      <PluginBox :chosenBtn="chosenBtn" :pluginData="bus" :btns="btns" />
                      <div class="pointContainer">
                        <div v-for="pointIndex in bus.outletNum" :key="pointIndex" class="point" :id="`plugin-${bus.boxIndex}_B-${pointIndex}`"></div>
                      </div>
                    </div>
                    <!-- 连接器 -->
                    <div v-else class="template-box" :id="`box-${index}`">
                      <div class="connector">
                        <span class="text">连接器</span>
                      </div>
                      <div v-if="chosenBtn == 8 && bus.temData" class="Tbox">
                        <div v-for="(tmp, count) in bus.temData" :key="count" class="T">
                          <div>T(L{{count}})</div>
                          <div>{{tmp}}°C</div>
                        </div>
                      </div>
                    </div>
                  </el-tooltip>
                </template>
              </div>
              <div class="menu" v-if="operateMenuBox.show && operateMenuBox.type == 'B'" :style="{left: `${operateMenuBox.left}`, top: `${operateMenuBox.top}`}">
                <!-- <div class="menu_item" @click="handleBoxOperate('edit', 'B')">编辑</div>
                <div class="menu_item" @click="handleBoxOperate('delete', 'B')">删除</div> -->
                <el-cascader-panel class="menu_item_panel" :options="menuOptionsBox" :props="{expandTrigger: 'hover'}" @change="handleMenuBox" @expand-change="expandChange" />
              </div>
            </div>
          </div>
          <div v-if="machineColInfo.pduBar && machineColInfo.barA" class="Bus" @click.right="handleBarRightClick($event)">
            <el-tooltip effect="light" :hide-after="0">
              <template #content>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 50%">
                    母线名称：{{machineColInfo.barA.busName ? machineColInfo.barA.busName : ''}} <br/>
                    网络地址：{{machineColInfo.barA.devIp ? machineColInfo.barA.devIp+'-'+ machineColInfo.barA.barId : ''}}<br/>
                    软件版本号：{{machineColInfo.barA.busVersion ? machineColInfo.barA.busVersion : ''}}<br/>
                    断路器状态：{{machineColInfo.barA.breakerStatus ? breakerStatusList[machineColInfo.barA.breakerStatus] : ''}}<br/>
                    剩余电流：{{machineColInfo.barA.curResidualValueTotal ? machineColInfo.barA.curResidualValueTotal.toFixed(2) : '0.00'}}A<br/>
                    防雷状态：{{machineColInfo.barA.lspStatus ? lspStatusList[machineColInfo.barA.lspStatus] : ''}}<br/>
                    突变频率：{{machineColInfo.barA.hzValue ? machineColInfo.barA.hzValue : ''}}
                  </div>
                  <div style="width: 50%">
                    始端箱状态：{{machineColInfo.barA.status ? barStatusList[machineColInfo.barA.status] : ''}} <br/>
                    总功率因数：{{machineColInfo.barA.powerFactorTotal ? machineColInfo.barA.powerFactorTotal.toFixed(2) : '0.00'}}<br/>
                    总有功功率：{{machineColInfo.barA.powValueTotal ? machineColInfo.barA.powValueTotal.toFixed(3) : '0.000'}}kW<br/>
                    总无功功率：{{machineColInfo.barA.powReactiveTotal ? machineColInfo.barA.powReactiveTotal.toFixed(3) : '0.000'}}kVar<br/>
                    总视在功率：{{machineColInfo.barA.powApparentTotal ? machineColInfo.barA.powApparentTotal.toFixed(3) : '0.000'}}kVA<br/>
                    电压不平衡：{{machineColInfo.barA.volUnbalance ? machineColInfo.barA.volUnbalance.toFixed(2) : '0.00'}}%<br/>
                    电流不平衡：{{machineColInfo.barA.curUnbalance ? machineColInfo.barA.curUnbalance.toFixed(2) : '0.00'}}%
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    <br/>
                    负载率：<br/>
                    相电流：<br/>
                    相电压：<br/>
                    线电压：<br/>
                    有功功率：<br/>
                    无功功率：<br/>
                    视在功率：<br/>
                    功率因数：<br/>
                    电压谐波：<br/>
                    电流谐波：
                  </div>
                  <div style="width: 25%">
                    A相<br/>
                    {{machineColInfo.barA.lineLoadRate?.[0] ? machineColInfo.barA.lineLoadRate[0].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barA.lineCur?.[0] ? machineColInfo.barA.lineCur[0].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barA.lineVol?.[0] ? machineColInfo.barA.lineVol[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.volLineValue?.[0] ? machineColInfo.barA.volLineValue[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.powActive?.[0] ? machineColInfo.barA.powActive[0].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barA.powReactive?.[0] ? machineColInfo.barA.powReactive[0].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barA.powApparent?.[0] ? machineColInfo.barA.powApparent[0].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barA.powerFactor?.[0] ? machineColInfo.barA.powerFactor[0].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.volThd?.[0] ? (machineColInfo.barA.volThd[0]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.curThd?.[0] ? (machineColInfo.barA.curThd[0]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    B相<br/>
                    {{machineColInfo.barA.lineLoadRate?.[1] ? machineColInfo.barA.lineLoadRate[1].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barA.lineCur?.[1] ? machineColInfo.barA.lineCur[1].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barA.lineVol?.[1] ? machineColInfo.barA.lineVol[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.volLineValue?.[1] ? machineColInfo.barA.volLineValue[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.powActive?.[1] ? machineColInfo.barA.powActive[1].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barA.powReactive?.[1] ? machineColInfo.barA.powReactive[1].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barA.powApparent?.[1] ? machineColInfo.barA.powApparent[1].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barA.powerFactor?.[1] ? machineColInfo.barA.powerFactor[1].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.volThd?.[1] ? (machineColInfo.barA.volThd[1]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.curThd?.[1] ? (machineColInfo.barA.curThd[1]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    C相<br/>
                    {{machineColInfo.barA.lineLoadRate?.[2] ? machineColInfo.barA.lineLoadRate[2].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barA.lineCur?.[2] ? machineColInfo.barA.lineCur[2].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barA.lineVol?.[2] ? machineColInfo.barA.lineVol[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.volLineValue?.[2] ? machineColInfo.barA.volLineValue[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barA.powActive?.[2] ? machineColInfo.barA.powActive[2].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barA.powReactive?.[2] ? machineColInfo.barA.powReactive[2].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barA.powApparent?.[2] ? machineColInfo.barA.powApparent[2].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barA.powerFactor?.[2] ? machineColInfo.barA.powerFactor[2].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.volThd?.[2] ? (machineColInfo.barA.volThd[2]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barA.curThd?.[2] ? (machineColInfo.barA.curThd[2]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    A相温度： {{machineColInfo.barA.temData?.[0] ? machineColInfo.barA.temData[0].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    B相温度：{{machineColInfo.barA.temData?.[1] ? machineColInfo.barA.temData[1].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    C相温度：{{machineColInfo.barA.temData?.[2] ? machineColInfo.barA.temData[2].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    N相温度：{{machineColInfo.barA.temData?.[3] ? machineColInfo.barA.temData[3].toFixed(0) : '0'}}°C<br/>
                  </div>
                </div>
              </template> 
              <div class="startBus" v-if="machineColInfo.barA.direction" @dblclick="handleInitialDblick($event, 'A')" style="background-color: #43939c" @click.stop="showInitialConnect('A')">
                <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barA" :btns="btns" />
                <div style="background-color: white;color: black;position: absolute;bottom: -20px;left: 50%;transform: translateX(-50%);">A路母线</div>
              </div>
              <div v-else></div>
            </el-tooltip>
            <el-tooltip effect="light" :hide-after="0">
              <template #content>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 50%">
                    母线名称：{{machineColInfo.barB.busName ? machineColInfo.barB.busName : ''}} <br/>
                    网络地址：{{machineColInfo.barB.devIp ? machineColInfo.barB.devIp+'-'+ machineColInfo.barB.barId : ''}}<br/>
                    软件版本号：{{machineColInfo.barB.busVersion ? machineColInfo.barB.busVersion : ''}}<br/>
                    断路器状态：{{machineColInfo.barB.breakerStatus ? breakerStatusList[machineColInfo.barB.breakerStatus] : ''}}<br/>
                    剩余电流：{{machineColInfo.barB.curResidualValueTotal ? machineColInfo.barB.curResidualValueTotal.toFixed(2) : '0.00'}}A<br/>
                    防雷状态：{{machineColInfo.barB.lspStatus ? lspStatusList[machineColInfo.barB.lspStatus] : ''}}<br/>
                    突变频率：{{machineColInfo.barB.hzValue ? machineColInfo.barB.hzValue : ''}}
                  </div>
                  <div style="width: 50%">
                    始端箱状态：{{machineColInfo.barB.status ? barStatusList[machineColInfo.barB.status] : ''}} <br/>
                    总功率因数：{{machineColInfo.barB.powerFactorTotal ? machineColInfo.barB.powerFactorTotal.toFixed(2) : '0.00'}}<br/>
                    总有功功率：{{machineColInfo.barB.powValueTotal ? machineColInfo.barB.powValueTotal.toFixed(3) : '0.000'}}kW<br/>
                    总无功功率：{{machineColInfo.barB.powReactiveTotal ? machineColInfo.barB.powReactiveTotal.toFixed(3) : '0.000'}}kVar<br/>
                    总视在功率：{{machineColInfo.barB.powApparentTotal ? machineColInfo.barB.powApparentTotal.toFixed(3) : '0.000'}}kVA<br/>
                    电压不平衡：{{machineColInfo.barB.volUnbalance ? machineColInfo.barB.volUnbalance.toFixed(2) : '0.00'}}%<br/>
                    电流不平衡：{{machineColInfo.barB.curUnbalance ? machineColInfo.barB.curUnbalance.toFixed(2) : '0.00'}}%
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    <br/>
                    负载率：<br/>
                    相电流：<br/>
                    相电压：<br/>
                    线电压：<br/>
                    有功功率：<br/>
                    无功功率：<br/>
                    视在功率：<br/>
                    功率因数：<br/>
                    电压谐波：<br/>
                    电流谐波：
                  </div>
                  <div style="width: 25%">
                    A相<br/>
                    {{machineColInfo.barB.lineLoadRate?.[0] ? machineColInfo.barB.lineLoadRate[0].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barB.lineCur?.[0] ? machineColInfo.barB.lineCur[0].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barB.lineVol?.[0] ? machineColInfo.barB.lineVol[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.volLineValue?.[0] ? machineColInfo.barB.volLineValue[0].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.powActive?.[0] ? machineColInfo.barB.powActive[0].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barB.powReactive?.[0] ? machineColInfo.barB.powReactive[0].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barB.powApparent?.[0] ? machineColInfo.barB.powApparent[0].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barB.powerFactor?.[0] ? machineColInfo.barB.powerFactor[0].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.volThd?.[0] ? (machineColInfo.barB.volThd[0]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.curThd?.[0] ? (machineColInfo.barB.curThd[0]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    B相<br/>
                    {{machineColInfo.barB.lineLoadRate?.[1] ? machineColInfo.barB.lineLoadRate[1].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barB.lineCur?.[1] ? machineColInfo.barB.lineCur[1].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barB.lineVol?.[1] ? machineColInfo.barB.lineVol[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.volLineValue?.[1] ? machineColInfo.barB.volLineValue[1].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.powActive?.[1] ? machineColInfo.barB.powActive[1].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barB.powReactive?.[1] ? machineColInfo.barB.powReactive[1].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barB.powApparent?.[1] ? machineColInfo.barB.powApparent[1].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barB.powerFactor?.[1] ? machineColInfo.barB.powerFactor[1].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.volThd?.[1] ? (machineColInfo.barB.volThd[1]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.curThd?.[1] ? (machineColInfo.barB.curThd[1]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                  <div style="width: 25%">
                    C相<br/>
                    {{machineColInfo.barB.lineLoadRate?.[2] ? machineColInfo.barB.lineLoadRate[2].toFixed(0) : '0'}}%<br/>
                    {{machineColInfo.barB.lineCur?.[2] ? machineColInfo.barB.lineCur[2].toFixed(2) : '0.00'}}A<br/>
                    {{machineColInfo.barB.lineVol?.[2] ? machineColInfo.barB.lineVol[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.volLineValue?.[2] ? machineColInfo.barB.volLineValue[2].toFixed(1) : '0.0'}}V<br/>
                    {{machineColInfo.barB.powActive?.[2] ? machineColInfo.barB.powActive[2].toFixed(3) : '0.000'}}kW<br/>
                    {{machineColInfo.barB.powReactive?.[2] ? machineColInfo.barB.powReactive[2].toFixed(3) : '0.000'}}kVar<br/>
                    {{machineColInfo.barB.powApparent?.[2] ? machineColInfo.barB.powApparent[2].toFixed(3) : '0.000'}}kVA<br/>
                    {{machineColInfo.barB.powerFactor?.[2] ? machineColInfo.barB.powerFactor[2].toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.volThd?.[2] ? (machineColInfo.barB.volThd[2]/100).toFixed(2) : '0.00'}}<br/>
                    {{machineColInfo.barB.curThd?.[2] ? (machineColInfo.barB.curThd[2]/100).toFixed(2) : '0.00'}}<br/>
                  </div>
                </div>
                <hr/>
                <div class="flex justify-between" style="width: 20vw">
                  <div style="width: 25%">
                    A相温度： {{machineColInfo.barB.temData?.[0] ? machineColInfo.barB.temData[0].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    B相温度：{{machineColInfo.barB.temData?.[1] ? machineColInfo.barB.temData[1].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    C相温度：{{machineColInfo.barB.temData?.[2] ? machineColInfo.barB.temData[2].toFixed(0) : '0'}}°C<br/>
                  </div>
                  <div style="width: 25%">
                    N相温度：{{machineColInfo.barB.temData?.[3] ? machineColInfo.barB.temData[3].toFixed(0) : '0'}}°C<br/>
                  </div>
                </div>
              </template> 
              <div class="startBus" v-if="machineColInfo.barB.direction" @dblclick="handleInitialDblick($event, 'B')" style="background-color: #acd997" @click.stop="showInitialConnect('B')">
                <InitialBox :chosenBtn="chosenBtn" :pluginData="machineColInfo.barB" :btns="btns" />
                <div style="background-color: white;color: black;position: absolute;bottom: -20px;left: 50%;transform: translateX(-50%);">B路母线</div>
              </div>
            </el-tooltip>
          </div>
        </div>
        <div class="cabinetContainer" @click.right="handleCabRightClick">
          <div class="cabinetList" v-if="cabinetList && cabinetList.length">
            <template v-for="(cabinet,index) in cabinetList" :key="index">
              <div class="cabinetBox">
                <div class="point">
                  <div v-if="cabinet.cabinetName" :id="'cab-A-' + index" class="leftPoint"></div>
                  <div v-if="cabinet.cabinetName" :id="'cab-B-' + index" class="rightPoint"></div>
                </div>
                <div class="cabinet">
                  <template v-if="cabinet.cabinetName">
                    <el-tooltip effect="light" :hide-after="0">
                      <template #content>
                        <div v-if="cabinet.cabinetBoxes || cabinet.cabinetPdus">
                          <div class="flex justify-between" style="width: 20vw">
                            <div style="width: 50%">
                              机柜状态：{{cabinet.runStatus ? statusColor[cabinet.runStatus].name : '正常'}} <br/>
                              机柜名称：{{cabinet.cabinetName}} <br/>
                              机柜负荷：{{cabinet.loadRate ? cabinet.loadRate.toFixed(1) : '0.0'}}%<br/>
                              昨日用能：{{cabinet.yesterdayEq ? cabinet.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                            </div>
                            <div style="width: 50%">
                              总功率因素：{{cabinet.powerFactor ? cabinet.powerFactor.toFixed(2) : '0.00'}}<br/>
                              总有功功率：{{cabinet.powActive ? cabinet.powActive.toFixed(3) : '0.000'}}kW<br/>
                              总视在功率：{{cabinet.powApparent ? cabinet.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                              总无功功率：{{cabinet.powReactive ? cabinet.powReactive.toFixed(3) : '0.000'}}kVar
                            </div>
                          </div>
                          <hr/>
                          <div class="flex justify-between" style="width: 20vw">
                            <div style="width: 50%">
                              A路占比：{{cabinet.outletA ? cabinet.outletA : '0'}}%<br/>
                              A路功率：{{cabinet.powActiveA ? cabinet.powActiveA.toFixed(3) : '0.000'}}kW<br/>
                              A路设备：{{cabinet.cabinetkeya}}
                            </div>
                            <div style="width: 50%">
                              B路占比：{{cabinet.outletB ? cabinet.outletB : '0'}}%<br/>
                              B路功率：{{cabinet.powActiveB ? cabinet.powActiveB.toFixed(3) : '0.000'}}kW<br/>
                              B路设备：{{cabinet.cabinetkeyb}}
                            </div>
                          </div>
                          <hr/>
                          <div class="flex justify-between" style="width: 20vw">
                            <div style="width: 50%">
                              前门温度：{{cabinet.temData ? cabinet.temData.toFixed(1) : ''}}°C<br/>
                              前门湿度：{{cabinet.temData ? cabinet.temData.toFixed(0) : ''}}%<br/>
                              前门露点温度: {{cabinet.dewPointa ? cabinet.dewPointa.toFixed(1) : ''}}°C
                            </div>
                            <div style="width: 50%">
                              后门温度：{{cabinet.temDataHot ? cabinet.temDataHot.toFixed(1) : ''}}°C<br/>
                              后门湿度：{{cabinet.temDataHot ? cabinet.temDataHot.toFixed(0) : ''}}%<br/>
                              后门露点温度: {{cabinet.dewPointb ? cabinet.dewPointb.toFixed(1) : ''}}°C
                            </div>
                          </div>
                          <div v-if="cabinet.alarmLogRecord && cabinet.runStatus != 1" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                            <hr/>
                            告警类型：{{alarmTypeDesc[cabinet.alarmLogRecord?.alarmType]}}<br/>
                            告警描述：{{cabinet.alarmLogRecord?.alarmDesc}}
                          </div>
                        </div>
                        <div v-else>
                          <div class="flex justify-between" style="width: 20vw">
                            <div style="width: 50%">
                              机柜状态：未绑定 <br/>
                              机柜名称：{{cabinet.cabinetName}} <br/>
                              机柜负荷：0.0%<br/>
                              昨日用能：0.0kW·h
                            </div>
                            <div style="width: 50%">
                              总功率因素：0.00<br/>
                              总有功功率：0.000kW<br/>
                              总视在功率：0.000kVA<br/>
                              总无功功率：0.000kVar
                            </div>
                          </div>
                          <hr/>
                          <div class="flex justify-between" style="width: 20vw">
                            <div style="width: 50%">
                              A路占比：0%<br/>
                              A路功率：0.000kW<br/>
                              A路设备：
                            </div>
                            <div style="width: 50%">
                              B路占比：0%<br/>
                              B路功率：0.000kW<br/>
                              B路设备：
                            </div>
                          </div>
                          <hr/>
                          <div class="flex justify-between" style="width: 20vw">
                            <div style="width: 50%">
                              前门温度：°C<br/>
                              前门湿度：%<br/>
                              前门露点温度: °C
                            </div>
                            <div style="width: 50%">
                              后门温度：°C<br/>
                              后门湿度：%<br/>
                              后门露点温度: °C
                            </div>
                          </div>
                        </div>
                      </template>
                      <div class="inner_fill" @click.stop="showCabinetConnect(index)" @dblclick="handleJump(cabinet)" :id="'cabinet-' + index" :style="{backgroundColor: cabinet.id ? '' : 'rgba(230, 240, 234)'}"></div>
                    </el-tooltip>
                    <template v-if="cabinet.id && (cabinet.cabinetBoxes || cabinet.cabinetPdus)">
                      <div v-if="chosenBtn == 0" class="fill_box">
                        <Echart :options="cabinet.echartsOptionLoad" height="100%" />
                        <div class="cabinetEchartsName">
                          <div>负载率</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 1" class="fill_box">
                        <Echart v-if="(cabinet.lineCurA && cabinet.lineCurA.length == 1) || (cabinet.lineCurB && cabinet.lineCurB.length == 1)" :options="cabinet.echartsOptionA1" height="100%" />
                        <div v-else style="height: 100%;display: flex;flex-direction: column;justify-content: space-around;align-items: center;font-size: 12px;font-weight: bold">
                          <div style="display: flex;flex-direction: column;width: 100%;justify-content: center;align-items: center;">
                            <div>L1(A)</div>
                            <div style="display: flex;justify-content: space-around;align-items: center;width: 100%;">
                              <div>{{cabinet.lineCurA ? cabinet.lineCurA[0].toFixed(2) : 0}}</div>
                              <div>{{cabinet.lineCurB ? cabinet.lineCurB[0].toFixed(2) : 0}}</div>
                            </div>
                          </div>
                          <div style="display: flex;flex-direction: column;width: 100%;justify-content: center;align-items: center;">
                            <div>L2(A)</div>
                            <div style="display: flex;justify-content: space-around;align-items: center;width: 100%;">
                              <div>{{cabinet.lineCurA ? cabinet.lineCurA[1].toFixed(2) : 0}}</div>
                              <div>{{cabinet.lineCurB ? cabinet.lineCurB[1].toFixed(2) : 0}}</div>
                            </div>
                          </div>
                          <div style="display: flex;flex-direction: column;width: 100%;justify-content: center;align-items: center;">
                            <div>L3(A)</div>
                            <div style="display: flex;justify-content: space-around;align-items: center;width: 100%;">
                              <div>{{cabinet.lineCurA ? cabinet.lineCurA[2].toFixed(2) : 0}}</div>
                              <div>{{cabinet.lineCurB ? cabinet.lineCurB[2].toFixed(2) : 0}}</div>
                            </div>
                          </div>
                        </div>
                        <div class="cabinetEchartsName" style="justify-content:space-around;">
                          <div>A路</div>
                          <div>B路</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 2" class="fill_box">
                        <Echart v-if="(cabinet.lineVolA && cabinet.lineVolA.length == 1) || (cabinet.lineVolB && cabinet.lineVolB.length == 1)" :options="cabinet.echartsOptionV1" height="100%" />
                        <div v-else style="height: 100%;display: flex;flex-direction: column;justify-content: space-around;align-items: center;font-size: 12px;font-weight: bold">
                          <div style="display: flex;flex-direction: column;width: 100%;justify-content: center;align-items: center;">
                            <div>L1(V)</div>
                            <div style="display: flex;justify-content: space-around;align-items: center;width: 100%;">
                              <div>{{cabinet.lineVolA ? cabinet.lineVolA[0].toFixed(1) : 0}}</div>
                              <div>{{cabinet.lineVolB ? cabinet.lineVolB[0].toFixed(1) : 0}}</div>
                            </div>
                          </div>
                          <div style="display: flex;flex-direction: column;width: 100%;justify-content: center;align-items: center;">
                            <div>L2(V)</div>
                            <div style="display: flex;justify-content: space-around;align-items: center;width: 100%;">
                              <div>{{cabinet.lineVolA ? cabinet.lineVolA[1].toFixed(1) : 0}}</div>
                              <div>{{cabinet.lineVolB ? cabinet.lineVolB[1].toFixed(1) : 0}}</div>
                            </div>
                          </div>
                          <div style="display: flex;flex-direction: column;width: 100%;justify-content: center;align-items: center;">
                            <div>L3(V)</div>
                            <div style="display: flex;justify-content: space-around;align-items: center;width: 100%;">
                              <div>{{cabinet.lineVolA ? cabinet.lineVolA[2].toFixed(1) : 0}}</div>
                              <div>{{cabinet.lineVolB ? cabinet.lineVolB[2].toFixed(1) : 0}}</div>
                            </div>
                          </div>
                        </div>
                        <div class="cabinetEchartsName" style="justify-content:space-around;">
                          <div>A路</div>
                          <div>B路</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 3" class="fill_box">
                        <Echart :options="cabinet.echartsOptionFactor" height="100%" />
                        <div class="cabinetEchartsName">
                          <div>功率因数</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 4" class="fill_box">
                        <div style="height: 12%;display: flex;flex-direction: column;justify-content: center;align-items: center;font-size: 12px;font-weight: bold">
                          <div>总视在功率</div>
                          <div>{{cabinet.powApparent ? cabinet.powApparent.toFixed(1) : 0}}kVA</div>
                        </div>
                        <Echart :options="cabinet.echartsOptionApparent" height="87%" />
                        <div class="cabinetEchartsName">
                          <div>功率</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 7" class="fill_box">
                        <div style="height: 12%;display: flex;flex-direction: column;justify-content: center;align-items: center;font-size: 12px;font-weight: bold" :style="{color: cabinet.deviation ? (cabinet.deviation>=19.5 ? `rgba(240, 58, 23, ${cabinet.deviation/100})` : (cabinet.deviation>=9.5 ? `rgba(255, 192, 0, ${(cabinet.deviation+80)/100})` : (cabinet.deviation>=4.5 ? `rgba(0, 120, 215, ${(cabinet.deviation+90)/100})` : `rgba(22, 198, 12, ${(cabinet.deviation+95)/100})`))) : '#eef4fc'}">
                          <div>偏差率</div>
                          <div>{{cabinet.deviation ? cabinet.deviation : 0}}%</div>
                        </div>
                        <Echart :options="cabinet.echartsOptionBalance" height="88%" />
                        <div class="cabinetEchartsName" style="justify-content:space-around;">
                          <div>A路</div>
                          <div>B路</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 8" class="fill_box">
                        <Echart :options="cabinet.echartsOptionTemp" height="100%" />
                        <div style="background-color:black;position: absolute;bottom:5px;color:white;display:flex;width:90%;justify-content:space-around;font-size:12px;">
                          <div>前门</div>
                          <div>后门</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 9" class="fill_box">
                        <Echart :options="cabinet.echartsOptionCapacity" height="100%" />
                        <div class="cabinetEchartsName">
                          <div>容量</div>
                        </div>
                      </div>
                      <div v-if="chosenBtn == 10" class="fill_box">
                        <Echart :options="cabinet.echartsOptionEq" height="100%" />
                        <div class="cabinetEchartsName">
                          <div>昨日用能</div>
                        </div>
                      </div>
                    </template>
                    <template v-else-if="cabinet.id">
                      <div class="fill_box">
                      </div>
                    </template>
                  </template>
                  <div v-else class="inner_empty" :id="'cabinet-' + index"></div>
                </div>
                <div style="width: 100%;display: flex;justify-content: center;">
                  <div class="ti_xing">
                  </div>
                </div>
                <div class="status">{{cabinet.cabinetName || ''}}</div>
              </div>
            </template>
            <div class="operateBox">
              <div v-show="editEnable" class="operateIcon" @click.prevent="addMachine">+</div>
              <div v-show="editEnable" class="operateIcon" @click.prevent="deleteMachine">-</div>
            </div>
          </div>
          <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
            <!-- <div class="menu_item" v-if="operateMenu.add" @click="handleOperate('add')">新增</div>
            <div class="menu_item" v-if="!operateMenu.add" @click="handleJump(false)">查看</div>
            <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('edit')">编辑</div>
            <div class="menu_item" v-if="!operateMenu.add" @click="handleOperate('delete')">删除</div> -->
            <el-cascader-panel ref="areaIdsCascader" class="menu_item_panel" :options="menuOptions" :props="{expandTrigger: 'hover'}" @change="handleMenu" @expand-change="expandChange" />
          </div>
        </div>
      </div>
      <el-form
        v-if="!isFromHome"
        class="mt-15px topForm"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="68px"
      >
        <el-form-item label="" prop="jf" >
          机房：<el-select :size="isFromHome ? 'small' : ''" v-model="queryParams.cabinetroomId" placeholder="请选择" class="!w-200px">
            <el-option v-for="item in roomList" :key="item.roomId" :label="item.roomName" :value="item.roomId" />
          </el-select>
        </el-form-item >
        <span class="line"></span>
        <el-form-item label="" prop="jg">
          柜列：<el-select :size="isFromHome ? 'small' : ''" v-model="queryParams.cabinetColumnId" placeholder="请选择" class="!w-200px">
            <el-option v-for="item in machineList" :key="item.id" :label="item.aisleName" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <div v-if="!editEnable && machineColInfo.barA" class="mask" @click.right.prevent="console.log('---')"></div>
    </div>
  </ContentWrap>
  <!-- </div> -->
  <!-- 添加或修改用户对话框 -->
  <PluginForm ref="columnForm" @success="handleFormPlugin" />
  <CabForm ref="cabinetForm" @success="handleFormCabinet" :roomList="roomList" :roomId="Number(queryParams.cabinetroomId)" />
  <BoxForm ref="columnBoxForm" @success="handleFormBox" />
  <layoutForm ref="machineForm" @success="handleChange" />

  <el-dialog v-model="detailVis" style="width: 80%;height: 80%;margin-top: 100px;">
    <div class="custom-row" style="display: flex; align-items: center;">
      <!-- 位置标签 -->
      <div class="location-tag el-col">
        <span style="margin-right:10px;font-size:18px;font-weight:bold;">功率因数详情</span>
        <span>所在位置：{{ machineColInfo.roomName?machineColInfo.roomName:'未绑定' }}</span>
        <span> 机柜名称：{{ queryParamsPF.cabinetName?queryParamsPF.cabinetName:'未绑定' }}</span>
      </div>

      <!-- 日期选择器 -->
      <div class="date-picker-col el-col">
        <el-date-picker
          v-model="queryParamsPF.startTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="datetime"
          :picker-options="pickerOptions"
          placeholder="选择日期时间"
        />
        <el-button @click="subtractOneDay(); handleDayPick()" type="primary" style="margin-left:10px;">&lt; 前一日</el-button>
        <el-button @click="addtractOneDay(); handleDayPick()" type="primary">&gt; 后一日</el-button>
      </div>

      <!-- 图表/数据切换按钮组 -->
      <div class="chart-data-buttons el-col" style="margin-right: 50px;">
        <div class="button-group">
          <el-button @click="switchChartOrTable = 0" :type="switchChartOrTable === 0 ? 'primary' : ''">图表</el-button>
          <el-button @click="switchChartOrTable = 1" :type="switchChartOrTable === 1 ? 'primary' : ''">数据</el-button>
          <el-button type="success" plain @click="handleExportXLS" :loading="exportLoading">
            <i class="el-icon-download"></i> 导出
          </el-button>
        </div>
      </div>
    </div>
    <br/>
    <PFDetail v-if="switchChartOrTable == 0"  width="75vw" height="70vh"  :list="pfESList"   />
    <div v-else-if="switchChartOrTable == 1" style="width: 100%;height:70vh;overflow-y:auto;">
      <el-table style="height:70vh;" :data="pfTableList" :show-overflow-tooltip="true" >
      <el-table-column label="时间" align="center" prop="create_time" />
      <el-table-column label="总功率因数" align="center" prop="factor_total_avg_value" />
      <el-table-column label="A路功率因数" align="center" prop="factor_a_avg_value" />
      <el-table-column label="B路功率因数" align="center" prop="factor_b_avg_value" />
    </el-table>
    </div>
  </el-dialog>
  <aisleRequire ref="aisleRequirement" />
  <aisleBalance ref="aisleBalanceDetail" />
  <aisleFactor ref="aisleFactorDetail" />
  <cabinetBalance ref="cabinetBalanceDetail" />
  <busRequire ref="busRequirement" />
  <busBalance ref="busBalanceDetail" />
  <busTem ref="busTemDetail" />
  <boxRequire ref="boxRequirement" />
  <boxBalance ref="boxBalanceDetail" />
  <boxTem ref="boxTemDetail" />
  <pduRequire ref="pduRequirement" />
  <pduBalance ref="pduBalanceDetail" />
</template>

<script lang="ts" setup>
import { newInstance, BezierConnector, BrowserJsPlumbInstance, JsPlumbDefaults } from '@jsplumb/browser-ui'
import { MachineColumnApi } from '@/api/cabinet/column'
import { MachineRoomApi } from '@/api/cabinet/room'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/cabinet/index'
import { ElMessage, ElMessageBox } from 'element-plus'
import download from '@/utils/download'
import PluginForm from './component/PluginForm.vue'
import PFDetail from './component/PFDetail.vue'
import CabForm from './component/CabForm.vue'
import InitialBox from './component/InitialBox.vue'
import PluginBox from './component/PluginBox.vue'
import BoxForm from './component/BoxForm.vue'
import { EChartsOption } from 'echarts'
import { useRoute } from 'vue-router'
import layoutForm from './component/layoutForm.vue'
import aisleRequire from '@/views/room/topology/component/aisleRequire.vue';
import aisleBalance from '@/views/room/topology/component/aisleBalance.vue';
import aisleFactor from '@/views/room/topology/component/aisleFactor.vue';
import cabinetBalance from '@/views/room/topology/component/cabinetBalance.vue';
import busRequire from '@/views/room/topology/component/busRequire.vue';
import busBalance from '@/views/room/topology/component/busBalance.vue';
import busTem from '@/views/room/topology/component/busTem.vue';
import boxRequire from '@/views/room/topology/component/boxRequire.vue';
import boxBalance from '@/views/room/topology/component/boxBalance.vue';
import boxTem from '@/views/room/topology/component/boxTem.vue';
import pduRequire from '@/views/room/topology/component/pduRequire.vue';
import pduBalance from '@/views/room/topology/component/pduBalance.vue';

const route = useRoute();
const query = route.query;

const message = useMessage()
const { push } = useRouter()
let instance: BrowserJsPlumbInstance | null = null
const roomList = ref<any>([]) // 机房列表
const machineList = ref<any>([]) // 机柜列列表
const tempList:any = ref([])
const detailVis = ref(false)
const switchChartOrTable = ref(0)
const queryParams = reactive({
  cabinetColumnId: Number(query.id),
  cabinetroomId: Number(query.roomId),
  roomDownValId:Number(query.roomValId)
})
const alarmTypeDesc = ref(['','PDU离线','PDU告警','PDU预警','母线告警','母线离线','机柜容量','机柜电力容量告警','机柜每日电量限额告警'])

const breakerStatusList = ['','合闸','分闸','跳闸']
const lspStatusList = ['','工作正常','损坏']
const barStatusList = ['离线','正常','告警']

const statusColor = ref([
  {
    name: '未绑定',
    color: '#3e62c7',
    value: 0,
  },
  {
    name: '正常',
    color: '#298447',
    value: 1,
  },
  {
    name: '预警',
    color: '#e56e19',
    value: 2,
  },
  {
    name: '告警',
    color: '#d42023',
    value: 3,
  },
  {
    name: '离线',
    color: '#757c8a',
    value: 4,
  },
  {
    name: '空机柜',
    color: '#f5f7fa',
    value: 5,
  },
])
const frontStatusList = [
  {
    name: '<=20°C',
    selected: true,
    value: 1,
    color: '#0078d7'
  },
  {
    name: '20°C~24°C',
    selected: true,
    value: 2,
    color: '#008000'
  },
  {
    name: '24°C~27°C',
    selected: true,
    value: 3,
    color: '#32cd32'
  },
  {
    name: '27°C~30°C',
    selected: true,
    value: 4,
    color: '#FFC000'
  },
  {
    name: '30°C~35°C',
    color: '#f7630c',
    value: 5,
  },
  {
    name: '>35°C',
    color: '#e81224',
    value: 6,
  }
]
const blackStatusList = [
  {
    name: '<=30°C',
    color: '#008000',
    value: 1,
  },
  {
    name: '30°C~35°C',
    color: '#32cd32',
    value: 2,
  },
  {
    name: '35°C~40°C',
    color: '#FFC000',
    value: 3,
  },
  {
    name: '40°C~45°C',
    color: '#f7630c',
    value: 4,
  },
  {
    name: '>45°C',
    color: '#e81224',
    value: 5,
  }
]
const statusList = reactive([
  [{
    name: '0%~50%',
    selected: true,
    value: 1,
    color: '#16c60c'
  },
  {
    name: '50%~75%',
    selected: true,
    value: 2,
    color: '#0078d7'
  },
  {
    name: '75%~90%',
    selected: true,
    value: 3,
    color: '#FFC000'
  },
  {
    name: '90%~100%+',
    selected: true,
    value: 4,
    color: '#f03a17'
  }],
  [{
    name: 'A路',
    selected: true,
    value: 1,
    color: '#E5B849'
  },
  {
    name: 'B路',
    selected: true,
    value: 2,
    color: '#C8603A'
  }],
  [{
    name: 'A路',
    selected: true,
    value: 1,
    color: '#075F71'
  },
  {
    name: 'B路',
    selected: true,
    value: 2,
    color: '#119CB5'
  }],
  [{
    name: '0~0.75',
    selected: true,
    value: 1,
    color: '#f03a17'
  },
  {
    name: '0.75~0.84',
    selected: true,
    value: 2,
    color: '#FFC000'
  },
  {
    name: '0.85~0.89',
    selected: true,
    value: 3,
    color: '#0078d7'
  },
  {
    name: '0.90~1',
    selected: true,
    value: 4,
    color: '#16c60c'
  }],
  [{
    name: '有功功率',
    selected: true,
    value: 1,
    color: '#5470c6'
  },
  {
    name: '无功功率',
    selected: true,
    value: 2,
    color: '#FFC000'
  }],[],[],
  [{
    name: '<=5%',
    selected: true,
    value: 1,
    color: '#16c60c'
  },{
    name: '5%~10%',
    selected: true,
    value: 2,
    color: '#0078d7'
  },{
    name: '10%~20%',
    selected: true,
    value: 3,
    color: '#FFC000'
  },{
    name: '>20%',
    selected: true,
    value: 4,
    color: '#f03a17'
  }],
  [],
  [{
    name: '剩余量>50%',
    selected: true,
    value: 1,
    color: '#3bbb00'
  },
  {
    name: '30%≤剩余量<50%',
    selected: true,
    value: 2,
    color: '#ffc402'
  },
  {
    name: '剩余量<30%',
    selected: true,
    value: 3,
    color: '#fa3333'
  }]
])
const btns = [
  {
    value: 1,
    name: '电流',
    unitName: '电流(A)',
  },
  {
    value: 2,
    name: '电压',
    unitName: '电压(V)',
  },
  {
    value: 4,
    name: '功率',
    unitName: '功率(kW)',
  },
  {
    value: 0,
    name: '负载率',
    unitName: '负载率',
  },
  {
    value: 7,
    name: '供电均衡',
    unitName: '视在功率(KVA)',
  },
  {
    value: 3,
    name: '功率因数',
    unitName: '功率因数',
  },
  {
    value: 10,
    name: '昨日用能',
    unitName: '昨日用能(kW·h)',
  },
  {
    value: 11,
    name: '断路器',
    unitName: '断路器',
  },
  {
    value: 8,
    name: '温度',
    unitName: '温度(°C)',
  },
  // {
  //   value: 9,
  //   name: '容量',
  //   unitName: '插接箱',
  // }
]
const btnsCabinet = [
  {
    value: 4,
    name: '功率',
    unitName: '功率(kW)',
  },
  {
    value: 7,
    name: '供电平衡',
    unitName: '视在功率(KVA)',
  },
  {
    value: 8,
    name: '温度',
    unitName: '温度(°C)',
  },
  {
    value: 9,
    name: '容量',
    unitName: '插接箱',
  },
  {
    value: 10,
    name: '用能',
    unitName: '昨日用能(kW·h)',
  }
]

const flashListTimer = ref();
const flashListTimerCopy = ref();

const echartsOptionCab = ref<EChartsOption>({})
let intervalTimer = null as any
const topologyContainer = ref()
const chosenBtn = ref(4)
const chosenBtnCabinet = ref(4)
const scaleValue = ref(1)
const ContainerHeight = ref(100)
const editEnable = ref(false)
const barChangeType = ref()
const columnForm = ref()
const columnBoxForm = ref()
const machineForm = ref();
const cabinetForm = ref()
const machineColInfo = reactive<any>({})
const cabinetList = ref<any>([])
const busListA = ref<any>([])
const busListB = ref<any>([])
const roomDownVal =ref();
const operateMenu = ref({  // 操作菜单
  left: '0px',
  top: '0px',
  show: false,
  add: false,
  curIndex: 0,
})
const operateMenuBox = ref({  // 操作插接箱/连接器菜单
  left: '0px',
  top: '0px',
  show: false,
  curIndex: 0,
  type: '',
})
const {containerInfo, isFromHome} = defineProps({
  containerInfo: {
    type: Object,
  },
  isFromHome: {
    type: Boolean,
    default: false,
  },
})

const aisleRequirement = ref()
const aisleBalanceDetail = ref()
const aisleFactorDetail = ref()
const cabinetBalanceDetail = ref()
const busRequirement = ref()
const busBalanceDetail = ref()
const busTemDetail = ref()
const boxRequirement = ref()
const boxBalanceDetail = ref()
const boxTemDetail = ref()
const pduRequirement = ref()
const pduBalanceDetail = ref()

const menuOptions = ref([
  {
    value: '小抓手',
    label: '小抓手',
    children: [{}]
  }
])

const menuOptionsCopy = ref([
  {
    value: '编辑',
    label: '编辑',
    children: [
      {
        value: '新增机柜',
        label: '新增机柜',
      },
      {
        value: '机柜编辑',
        label: '机柜编辑',
      },
      {
        value: '机柜解绑',
        label: '机柜解绑',
      },
      {
        value: '机柜删除',
        label: '机柜删除'
      }
    ]
  },
  {
    value: '柜列：',
    label: '柜列：',
    children: [
      {
        value: '柜列配电',
        label: '柜列配电',
      },
      {
        value: '柜列用能',
        label: '柜列用能',
      },
      {
        value: '柜列需量',
        label: '柜列需量',
      },
      {
        value: '柜列供电平衡',
        label: '柜列供电平衡',
      },
      {
        value: '柜列功率因数',
        label: '柜列功率因数',
      }
    ]
  },
  {
    value: '机柜：',
    label: '机柜：',
    children: [
      {
        value: '机柜负荷',
        label: '机柜负荷',
      },
      {
        value: '机柜配电',
        label: '机柜配电',
      },
      {
        value: '机柜温度',
        label: '机柜温度',
      },
      {
        value: '机柜用能',
        label: '机柜用能',
      },
      {
        value: '机柜需量',
        label: '机柜需量',
      },
      {
        value: '机柜供电平衡',
        label: '机柜供电平衡',
      },
      {
        value: '机柜功率因数',
        label: '机柜功率因数',
      }
    ]
  },
  {
    value: 'A路设备 ',
    label: 'A路设备 ',
    children: [
      {
        value: 'A路配电',
        label: 'A路配电',
      },
      {
        value: 'A路需量',
        label: 'A路需量',
      },
      {
        value: 'A路设备管理',
        label: 'A路设备管理',
      },
      {
        value: 'A路供电平衡',
        label: 'A路供电平衡',
      }
    ]
  },
  {
    value: 'B路设备 ',
    label: 'B路设备 ',
    children: [
      {
        value: 'B路配电',
        label: 'B路配电',
      },
      {
        value: 'B路需量',
        label: 'B路需量',
      },
      {
        value: 'B路设备管理',
        label: 'B路设备管理',
      },
      {
        value: 'B路供电平衡',
        label: 'B路供电平衡',
      }
    ]
  },
  {
    value: 'A路母线 ',
    label: 'A路母线 ',
    children: [
      {
        value: 'A路母线负荷',
        label: 'A路母线负荷',
      },
      {
        value: 'A路母线配电',
        label: 'A路母线配电',
      },
      {
        value: 'A路母线需量',
        label: 'A路母线需量',
      },
      {
        value: 'A路母线温度',
        label: 'A路母线温度',
      },
      {
        value: 'A路母线三相平衡',
        label: 'A路母线三相平衡',
      }
    ]
  },
  {
    value: 'B路母线 ',
    label: 'B路母线 ',
    children: [
      {
        value: 'B路母线负荷',
        label: 'B路母线负荷',
      },
      {
        value: 'B路母线配电',
        label: 'B路母线配电',
      },
      {
        value: 'B路母线需量',
        label: 'B路母线需量',
      },
      {
        value: 'B路母线温度',
        label: 'B路母线温度',
      },
      {
        value: 'B路母线三相平衡',
        label: 'B路母线三相平衡',
      }
    ]
  },
  {
    value: 'A路设备 ',
    label: 'A路设备 ',
    children: [
      {
        value: 'A路插接箱配电',
        label: 'A路插接箱配电',
      },
      {
        value: 'A路插接箱需量',
        label: 'A路插接箱需量',
      },
      {
        value: 'A路插接箱温度',
        label: 'A路插接箱温度',
      },
      {
        value: 'A路插接箱设备管理',
        label: 'A路插接箱设备管理',
      },
      {
        value: 'A路插接箱供电平衡',
        label: 'A路插接箱供电平衡',
      }
    ]
  },
  {
    value: 'B路设备 ',
    label: 'B路设备 ',
    children: [
      {
        value: 'B路插接箱配电',
        label: 'B路插接箱配电',
      },
      {
        value: 'B路插接箱需量',
        label: 'B路插接箱需量',
      },
      {
        value: 'B路插接箱温度',
        label: 'B路插接箱温度',
      },
      {
        value: 'B路插接箱设备管理',
        label: 'B路插接箱设备管理',
      },
      {
        value: 'B路插接箱供电平衡',
        label: 'B路插接箱供电平衡',
      }
    ]
  }
])

const menuOptionsBox = ref([
  {
    value: '编辑',
    label: '编辑 ',
    children: [
      {
        value: '插接箱编辑',
        label: '插接箱编辑',
      },
      {
        value: '插接箱删除',
        label: '插接箱删除',
      }
    ]
  },
  {
    value: '插接箱 ',
    label: '插接箱 ',
    children: [
      {
        value: '插接箱配电',
        label: '插接箱配电',
      },
      {
        value: '插接箱需量',
        label: '插接箱需量',
      },
      {
        value: '插接箱温度',
        label: '插接箱温度',
      },
      {
        value: '插接箱设备管理',
        label: '插接箱设备管理',
      },
      {
        value: '插接箱供电平衡',
        label: '插接箱供电平衡',
      }
    ]
  }
])

const emit = defineEmits(['backData', 'idChange', 'getpdubar','sendList']) // 定义 success 事件，用于操作成功后的回调

// 连接初始化准备
const initConnect = () => {
  let cabinetIndex
  // 创建实例
  instance = newInstance({
    container: topologyContainer.value
  })
  // 监听连接
  instance.bind('beforeDrop', async ({connection}) => {
    console.log('connection', connection)
    const sourceId = connection.source.id
    const targetId = connection.target.id
    const cabId = sourceId.includes('cab') ? sourceId : targetId // 机柜id
    const cabIndex = cabId.split('-')[2]  // 机柜下标
    cabinetIndex = cabIndex
    const cabRoad = cabId.split('-')[1] // 机柜AB路
    const pluginId = sourceId.includes('plugin') ? sourceId : targetId // 插接箱id
    const pluginName = pluginId.split('_')[0] // 插接箱名字
    const pluginOutLet = pluginId.split('-')[2] // 插接箱输出位
    // 机柜与机柜 插接箱与插接箱之间不能相互连接   机柜A路只能连母线A路 机柜B路只能连母线B路
    if ((cabId.split('-')[0] ==  pluginId.split('-')[0]) ||  (cabRoad == 'A' && pluginId.includes('B')) || (cabRoad == 'B' && pluginId.includes('A'))) {
      return false
    }
    cabinetList.value[cabIndex][`boxOutletId${cabRoad}`] = +pluginOutLet
    cabinetList.value[cabIndex][`boxIndex${cabRoad}`] = Number(pluginName.split('-')[1])
    let boxList_Index = machineColInfo[`bar${cabRoad}`].boxList.findIndex(box => box.boxIndex == Number(pluginName.split('-')[1]))

    cabinetList.value[cabIndex][`casId${cabRoad}`] = machineColInfo[`bar${cabRoad}`].boxList[boxList_Index].casAddr
    cabinetList.value[cabIndex][`barId${cabRoad}`] = machineColInfo[`bar${cabRoad}`].barId
    cabinetList.value[cabIndex][`busIp${cabRoad}`] = machineColInfo[`bar${cabRoad}`].devIp
    console.log('监听连接', connection, cabId, pluginId, cabinetList.value[cabIndex],)
    // 处理手动连接的样式
    if (cabRoad == 'A') {
      connection.paintStyle = {
        strokeWidth: 1,
        stroke: "#43939c",
        dashstyle: "5 5"
      }
    } else if (cabRoad == 'B') {
      connection.paintStyle = {
        strokeWidth: 1,
        stroke: "#acd997",
        dashstyle: "5 5"
      }
    }
    if(cabIndex >= 0) {
      const res = await CabinetApi.saveCabinetInfo({
        ...cabinetList.value[cabIndex]
      })
      if(res) {
        message.success('绑定成功')
      } else {
        message.error('绑定失败')
      }
    }
    getMachineColInfoReal()
    showCabinetConnect(cabIndex)
    return true
  })
  // 监听连接断开
  instance.bind('beforeDetach', async function(connection) {
    console.log('监听连接断开', connection, connection.sourceId, connection.source,connection.target)
    if (connection.suspendedElement) { // 用户手动断开连接
      console.log("c1111111111111")
      const targetId = connection.target.id
      const sourceId = connection.source.id
      const cabRoad = connection.source.id.includes('cab') ? sourceId.split('-')[1] : targetId.split(/[-_]/)[2]
      const index = connection.source.id.includes('cab') ? sourceId.split('-')[2] : targetId.split(/[-_]/)[1]
      cabinetIndex = index
      cabinetList.value[index][`boxOutletId${cabRoad}`] = null
      cabinetList.value[index][`boxIndex${cabRoad}`] = null
      cabinetList.value[index][`casId${cabRoad}`] = null
      cabinetList.value[index][`barId${cabRoad}`] = null
      cabinetList.value[index][`busIp${cabRoad}`] = null
      console.log(cabinetList.value[index])
      const cabElement = document.getElementById('cab-' + cabRoad + '-' + index) as Element
      instance?.addEndpoint(cabElement, {
        source: true,
        target: true,
        endpoint: 'Dot',
        paintStyle: {
          strokeWidth: 1,
          stroke: cabRoad == 'A' ? '#43939c' : '#acd997',
          dashstyle: '5 5'
        }
      })

      if(index >= 0) {
        const res = await CabinetApi.saveCabinetInfo({
          ...cabinetList.value[index]
        })
        if(res) {
          message.success('解绑成功')
        } else {
          message.error('解绑失败')
        }
      }
      getMachineColInfoReal()
      showCabinetConnect(index)
    }
    // 如果返回 false，则连接断开操作会被取消
    return true
  })
}
// 创建瞄点并连接
const toCreatConnect = (onlyDelete = false) => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    console.log('toCreatConnect', cabinetList.value, machineColInfo)
    nextTick(() => {
      machineColInfo.barA.boxList.forEach(item => {
        if (item.type) return
        for(let i=1; i <= item.outletNum; i++) {
          const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_A-' + i) as Element
          // console.log('boxElementA', boxElementA, item.boxIndex)
          // 删除瞄点
          instance?.removeAllEndpoints(boxElementA)
          // 添加瞄点
          instance?.addEndpoint(boxElementA, {
            source: true,
            target: true,
            endpoint: 'Dot',
            paintStyle: {
              fill: "#43939c"
            }
          })
          // 更新瞄点
          instance?.revalidate(boxElementA)
        }
      })
      machineColInfo.barB.boxList.forEach(item => {
        if (item.type) return
        for(let i=1; i <= item.outletNum; i++) {
          const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_B-' + i) as Element
          console.log('boxElementA', boxElementA, item.boxIndex)
          // 删除瞄点
          instance?.removeAllEndpoints(boxElementA)
          // 添加瞄点
          instance?.addEndpoint(boxElementA, {
            source: true,
            target: true,
            endpoint: 'Dot',
            paintStyle: {
              fill: "#acd997"
            }
          })
          // 更新瞄点
          instance?.revalidate(boxElementA)
        }
      })
      cabinetList.value.forEach((item, index) => {
        if (!item.cabinetName || item.cabinetPdus) return
        if(onlyDelete) {
          addCabinetAnchor(index, item, onlyDelete)
        } else {
          const cabElementA = document.getElementById('cab-A-' + index) as Element
          const cabElementB = document.getElementById('cab-B-' + index) as Element
          console.log('cabElementB', cabElementB, cabElementA, item)
          instance?.removeAllEndpoints(cabElementA)
          instance?.removeAllEndpoints(cabElementB)
          // 添加瞄点
          instance?.addEndpoint(cabElementA, {
            source: true,
            target: true,
            endpoint: 'Dot',
            paintStyle: {
              fill: "#43939c"
            }
          })
          instance?.addEndpoint(cabElementB, {
            source: true,
            target: true,
            endpoint: 'Dot',
            paintStyle: {
              fill: "#acd997"
            }
          })
          instance?.revalidate(cabElementA)
          instance?.revalidate(cabElementB)
        }
      })
      instance?.deleteEveryConnection()
      return Promise.resolve()
    })
  }
}

const showAllConnect = () => {
  instance?.deleteEveryConnection()
  // console.log(machineColInfo.pduBar)
  // cabinetList.value.forEach((item, index) => {
  //   if (!item.cabinetName || !machineColInfo.barA) return
  //   addCabinetAnchor(index, item)
  // })
}

const showCabinetConnect = (i) => {
  instance?.deleteEveryConnection()
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName || !machineColInfo.barA) return
    if(index == i) {
      addCabinetAnchor(index, item)
    } else {
      const cabElementA = document.getElementById('cab-A-' + index) as Element
      const cabElementB = document.getElementById('cab-B-' + index) as Element
      console.log('cabElementB', cabElementB, cabElementA, item)
      instance?.removeAllEndpoints(cabElementA)
      instance?.removeAllEndpoints(cabElementB)
      // 添加瞄点
      instance?.addEndpoint(cabElementA, {
        source: true,
        target: true,
        endpoint: 'Dot',
        paintStyle: {
          fill: "#43939c"
        }
      })
      instance?.addEndpoint(cabElementB, {
        source: true,
        target: true,
        endpoint: 'Dot',
        paintStyle: {
          fill: "#acd997"
        }
      })
      instance?.revalidate(cabElementA)
      instance?.revalidate(cabElementB)
    }
  })
}

const showPluginConnect = (road,boxIndex) => {
  instance?.deleteEveryConnection()
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName || !machineColInfo.pduBar) return
    const cabElementA = document.getElementById('cab-A-' + index) as Element
    const cabElementB = document.getElementById('cab-B-' + index) as Element
    console.log('cabElementB', cabElementB, cabElementA, item)
    instance?.removeAllEndpoints(cabElementA)
    instance?.removeAllEndpoints(cabElementB)
    // 添加瞄点
    instance?.addEndpoint(cabElementA, {
      source: true,
      target: true,
      endpoint: 'Dot',
      paintStyle: {
        fill: "#43939c"
      }
    })
    instance?.addEndpoint(cabElementB, {
      source: true,
      target: true,
      endpoint: 'Dot',
      paintStyle: {
        fill: "#acd997"
      }
    })
    if (item[`boxIndex${road}`] !== '' && item[`boxIndex${road}`] == boxIndex) { // 有连接
      const source = document.getElementById('cab-' + road + '-' + index) as Element
      const target = document.getElementById(`plugin-${item[`boxIndex${road}`]}_${road}-${item[`boxOutletId${road}`]}`)  as Element
      console.log('target', source, target, item.boxIndexA, item.boxOutletIdA, machineColInfo)
      instance?.connect({
        source,
        target,
        paintStyle: {
          strokeWidth: 1,
          stroke: road == 'A' ? '#43939c' : '#acd997',
          dashstyle: '5 5'
        },
        endpointStyle: {
          fill: road == 'A' ? '#43939c' : '#acd997'
        }
      })
    }
    instance?.revalidate(cabElementA)
    instance?.revalidate(cabElementB)
  })
}

const showInitialConnect = (road) => {
  instance?.deleteEveryConnection()
  cabinetList.value.forEach((item, index) => {
    if (!item.cabinetName || !machineColInfo.pduBar) return
    const cabElementA = document.getElementById('cab-A-' + index) as Element
    const cabElementB = document.getElementById('cab-B-' + index) as Element
    console.log('cabElementB', cabElementB, cabElementA, item)
    instance?.removeAllEndpoints(cabElementA)
    instance?.removeAllEndpoints(cabElementB)
    // 添加瞄点
    instance?.addEndpoint(cabElementA, {
      source: true,
      target: true,
      endpoint: 'Dot',
      paintStyle: {
        fill: "#43939c"
      }
    })
    instance?.addEndpoint(cabElementB, {
      source: true,
      target: true,
      endpoint: 'Dot',
      paintStyle: {
        fill: "#acd997"
      }
    })
    if (item[`boxIndex${road}`] !== '') { // 有连接
      const source = document.getElementById('cab-' + road + '-' + index) as Element
      const target = document.getElementById(`plugin-${item[`boxIndex${road}`]}_${road}-${item[`boxOutletId${road}`]}`)  as Element
      console.log('target', source, target, item.boxIndexA, item.boxOutletIdA, machineColInfo)
      instance?.connect({
        source,
        target,
        paintStyle: {
          strokeWidth: 1,
          stroke: road == 'A' ? '#43939c' : '#acd997',
          dashstyle: '5 5'
        },
        endpointStyle: {
          fill: road == 'A' ? '#43939c' : '#acd997'
        }
      })
    }
    instance?.revalidate(cabElementA)
    instance?.revalidate(cabElementB)
  })
}

// 创建瞄点并连接
const controlEndpointShow = (show) => {
  setTimeout((() => {
    const points = document.querySelectorAll('.jtk-endpoint')
    console.log('points', points)
    points.forEach(element  => {
      // console.log('element', element)
      const a = element as any
      a.style.opacity = show ? 1 : 0.1
    })
  }), 100)
}
// 更新插接箱瞄点
const updatePluginAnchor = () => {
  if (cabinetList.value && cabinetList.value.length && machineColInfo.barA) {
    machineColInfo.barA.boxList.forEach(item => {
      if (item.type) return
      for(let i=1; i <= item.outletNum; i++) {
        const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_A-' + i) as Element
        // 更新瞄点
        instance?.revalidate(boxElementA)
      }
    })
    machineColInfo.barB.boxList.forEach(item => {
      if (item.type) return
      for(let i=1; i <= item.outletNum; i++) {
        const boxElementA = document.getElementById('plugin-' + item.boxIndex + '_B-' + i) as Element
        // 更新瞄点
        instance?.revalidate(boxElementA)
      }
    })
  }
}
// 给某个机柜加瞄点 并进行连接
const addCabinetAnchor = (index, data = {} as any, onlyDelete = false) => {
  const cabElementA = document.getElementById('cab-A-' + index) as Element
  const cabElementB = document.getElementById('cab-B-' + index) as Element
  // console.log('cabElementB', cabElementB, cabElementA, data)
  instance?.removeAllEndpoints(cabElementA)
  instance?.removeAllEndpoints(cabElementB)
  if (onlyDelete) return
  // 添加瞄点
  // if (data.boxIndexA === '' || !data.boxOutletIdA) 
  instance?.addEndpoint(cabElementA, {
    source: true,
    target: true,
    endpoint: 'Dot',
    paintStyle: {
      fill: "#43939c"
    }
  })
  // if (data.boxIndexA === '' || !data.boxOutletIdB) 
  instance?.addEndpoint(cabElementB, {
    source: true,
    target: true,
    endpoint: 'Dot',
    paintStyle: {
      fill: "#acd997"
    }
  })
  if ((data.boxIndexA !== '' && data.boxIndexA > -1) && data.boxOutletIdA) { // A路有连接
    const source = document.getElementById('cab-A-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxIndexA}_A-${data.boxOutletIdA}`)  as Element
    // console.log('target', source, target, data.boxIndexA, data.boxOutletIdA, machineColInfo)
    instance?.connect({
      source,
      target,
      paintStyle: {
        strokeWidth: 1,
        stroke: '#43939c',
        dashstyle: '5 5'
      },
      endpointStyle: {
        fill: '#43939c'
      }
    })
  }
  if ((data.boxIndexA !== '' && data.boxIndexA > -1) && data.boxOutletIdB) { // B路有连接
    const source = document.getElementById('cab-B-' + index) as Element
    const target = document.getElementById(`plugin-${data.boxIndexB}_B-${data.boxOutletIdB}`)  as Element
    // console.log('target---', source, target)
    instance?.connect({
      source,
      target,
      paintStyle: {
        strokeWidth: 1,
        stroke: '#acd997',
        dashstyle: '5 5'
      },
      endpointStyle: {
        fill: '#acd997'
      }
    })
  }
  instance?.revalidate(cabElementA)
  instance?.revalidate(cabElementB)
}
// 更新机柜和插接箱的连接 
const updateCabinetConnect = () => {
  nextTick(() => {
    cabinetList.value.forEach((item, index) => {
      if (!item.cabinetName || !machineColInfo.barA) return
      console.log('更新机柜的连接')
      const cabElementA = document.getElementById('cab-A-' + index) as Element
      const cabElementB = document.getElementById('cab-B-' + index) as Element
      instance?.revalidate(cabElementA)
      instance?.revalidate(cabElementB)
    })
    updatePluginAnchor()
  })
}

const queryParamsPF = reactive({
  pageNo: 1,
  pageSize: 24,
  cabinetIds : [],
})as any
const pfESList = ref({}) as any
const pfTableList = ref([]) as any
const exportLoading = ref(false) // 导出的加载中

const openPFDetail = async (row) =>{
  queryParamsPF.cabinetName = row.cabinetName
  queryParamsPF.cabinetIds = [row.id];
  queryParamsPF.startTime = getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),new Date().getDate(),0,0,0));
  console.log('row',row);
  await getDetail();
  detailVis.value = true;
}

/** 查询列表 */
const getDetail = async () => {
  const data = await IndexApi.getCabinetPFDetail(queryParamsPF);
  console.log('数据',data);
  pfESList.value = data?.chart;
  pfESList.value?.powerFactorAvgValueTotal?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.powerFactorAvgValueA?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });
  pfESList.value?.powerFactorAvgValueB?.forEach((obj) => {
    obj = obj?.toFixed(2);
  });

  pfTableList.value = data?.table;
  pfTableList.value?.forEach((obj) => {
    obj.factor_total_avg_value = obj?.factor_total_avg_value?.toFixed(2);
    obj.factor_a_avg_value = obj?.factor_a_avg_value?.toFixed(2);
    obj.factor_b_avg_value = obj?.factor_b_avg_value?.toFixed(2);
  });
}

const subtractOneDay = () => {
  var date = new Date(queryParamsPF.startTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() - 1); // 减去一天

  queryParamsPF.startTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const addtractOneDay = () => {
  var date = new Date(queryParamsPF.startTime + "Z"); // 添加 "Z" 表示 UTC 时间

  date.setDate(date.getDate() + 1); // 减去一天

  queryParamsPF.startTime = date.toISOString().slice(0, 19).replace("T", " "); // 转换为新的日期字符串
};

const handleDayPick = async () => {

  if(queryParamsPF?.startTime ){
    await getDetail();
    
  } 
}

const handleExportXLS = async ()=>{
  try {
    // 导出的二次确认
    await message.exportConfirm();
    // 发起导出
    queryParamsPF.pageNo = 1;
    exportLoading.value = true;
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await IndexApi.getCabinetPFDetailExcel(queryParamsPF, axiosConfig);
    console.log("data",data);
    await download.excel(data, '功率因数详细.xlsx');
  } catch (error) {
    // 处理异常
    console.error('导出失败：', error);
  } finally {
    exportLoading.value = false;
  }
}

const getFullTimeByDate = (date) => {
  var year = date.getFullYear();//年
  var month = date.getMonth();//月
  var day = date.getDate();//日
  var hours = date.getHours();//时
  var min = date.getMinutes();//分
  var second = date.getSeconds();//秒
  return year + "-" +
      ((month + 1) > 9 ? (month + 1) : "0" + (month + 1)) + "-" +
      (day > 9 ? day : ("0" + day)) + " " +
      (hours > 9 ? hours : ("0" + hours)) + ":" +
      (min > 9 ? min : ("0" + min)) + ":" +
      (second > 9 ? second : ("0" + second));
}

// 处理机柜右击事件
const handleCabRightClick = (e) => {
  console.log('处理右击事件', e.target.className)
  e.preventDefault()
  if (!e.target.className.includes('inner_empty') && !e.target.className.includes('inner_fill')) return
  const container = e.currentTarget
  const currentIndex = e.target.id.split('-')[1]
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  const isAdd = !cabinetList.value[currentIndex].cabinetName

  menuOptions.value = []
  menuOptions.value[0] = JSON.parse(JSON.stringify(menuOptionsCopy.value[0]));

  if(currentIndex >= 0) {
    menuOptions.value[0].value = cabinetList.value[currentIndex]

    menuOptions.value[1] = menuOptionsCopy.value[1]
    menuOptions.value[1].value = cabinetList.value[currentIndex].aisleId
    menuOptions.value[1].label = "柜列：" + machineColInfo.aisleName

    menuOptions.value[2] = menuOptionsCopy.value[2]
    menuOptions.value[2].value = cabinetList.value[currentIndex]
    menuOptions.value[2].label = "机柜：" + cabinetList.value[currentIndex].cabinetName

    console.log(cabinetList.value[currentIndex])
    if(cabinetList.value[currentIndex].cabinetBoxes && cabinetList.value[currentIndex].cabinetkeya && cabinetList.value[currentIndex].cabinetkeyb) {
      menuOptions.value[3] = menuOptionsCopy.value[5]
      menuOptions.value[3].label = "A路母线：" + cabinetList.value[currentIndex].busIpA + '-' + cabinetList.value[currentIndex].barIdA
      menuOptions.value[4] = menuOptionsCopy.value[6]
      menuOptions.value[4].label = "B路母线：" + cabinetList.value[currentIndex].busIpB + '-' + cabinetList.value[currentIndex].barIdB

      menuOptions.value[5] = menuOptionsCopy.value[7]
      menuOptions.value[5].label = "A路设备：" + cabinetList.value[currentIndex].barIdA + '-' + (cabinetList.value[currentIndex].boxIndexA+1) + '-' + cabinetList.value[currentIndex].boxOutletIdA

      menuOptions.value[6] = menuOptionsCopy.value[8]
      menuOptions.value[6].label = "B路设备：" + cabinetList.value[currentIndex].barIdB + '-' + (cabinetList.value[currentIndex].boxIndexB+1) + '-' + cabinetList.value[currentIndex].boxOutletIdB

      let cabinetBoxes = cabinetList.value[currentIndex].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyA: cabinetBoxes.boxKeyA.split("-")[0] + "-" + cabinetBoxes.boxKeyA.split("-")[1],
      }
      menuOptions.value[4].value = {
        devKeyB: cabinetBoxes.boxKeyB.split("-")[0] + "-" + cabinetBoxes.boxKeyB.split("-")[1]
      }
      menuOptions.value[5].value = {
        devKey: cabinetBoxes.boxKeyA,
        boxId: cabinetList.value[currentIndex].keya?.id
      }
      menuOptions.value[6].value = {
        devKey: cabinetBoxes.boxKeyB,
        boxId: cabinetList.value[currentIndex].keyb?.id
      }
    } else if(cabinetList.value[currentIndex].cabinetBoxes && cabinetList.value[currentIndex].cabinetkeya && !cabinetList.value[currentIndex].cabinetkeyb) {
      menuOptions.value[3] = menuOptionsCopy.value[5]
      menuOptions.value[3].label = "A路母线：" + cabinetList.value[currentIndex].busIpA + '-' + cabinetList.value[currentIndex].barIdA

      menuOptions.value[4] = menuOptionsCopy.value[7]
      menuOptions.value[4].label = "A路设备：" + cabinetList.value[currentIndex].barIdA + '-' + (cabinetList.value[currentIndex].boxIndexA+1) + '-' + cabinetList.value[currentIndex].boxOutletIdA

      let cabinetBoxes = cabinetList.value[currentIndex].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyA: cabinetBoxes.boxKeyA.split("-")[0] + "-" + cabinetBoxes.boxKeyA.split("-")[1],
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyA,
        boxId: cabinetList.value[currentIndex].keya?.id
      }

    } else if(cabinetList.value[currentIndex].cabinetBoxes && cabinetList.value[currentIndex].cabinetkeyb && !cabinetList.value[currentIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[6]
      menuOptions.value[3].label = "B路母线：" + cabinetList.value[currentIndex].busIpB + '-' + cabinetList.value[currentIndex].barIdB

      menuOptions.value[4] = menuOptionsCopy.value[8]
      menuOptions.value[4].label = "B路设备：" + cabinetList.value[currentIndex].barIdB + '-' + (cabinetList.value[currentIndex].boxIndexB+1) + '-' + cabinetList.value[currentIndex].boxOutletIdB

      let cabinetBoxes = cabinetList.value[currentIndex].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyB: cabinetBoxes.boxKeyB.split("-")[0] + "-" + cabinetBoxes.boxKeyB.split("-")[1]
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyB,
        boxId: cabinetList.value[currentIndex].keyb?.id
      }
    }

    if(cabinetList.value[currentIndex].cabinetPdus && cabinetList.value[currentIndex].cabinetkeyb && cabinetList.value[currentIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[3]
      menuOptions.value[3].label = "A路设备：" + cabinetList.value[currentIndex].cabinetkeya

      menuOptions.value[4] = menuOptionsCopy.value[4]
      menuOptions.value[4].label = "B路设备：" + cabinetList.value[currentIndex].cabinetkeyb
      
      let cabinetBoxes = cabinetList.value[currentIndex].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyA,
        pduId: cabinetList.value[currentIndex].keya?.id
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.pduKeyB,
        pduId: cabinetList.value[currentIndex].keyb?.id
      }
    } else if(cabinetList.value[currentIndex].cabinetPdus && cabinetList.value[currentIndex].cabinetkeyb && !cabinetList.value[currentIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[3]
      menuOptions.value[3].label = "A路设备：" + cabinetList.value[currentIndex].cabinetkeya
      
      let cabinetBoxes = cabinetList.value[currentIndex].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyA,
        pduId: cabinetList.value[currentIndex].keya?.id
      }
    } else if(cabinetList.value[currentIndex].cabinetPdus && !cabinetList.value[currentIndex].cabinetkeyb && cabinetList.value[currentIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[4]
      menuOptions.value[3].label = "B路设备：" + cabinetList.value[currentIndex].cabinetkeyb
      
      let cabinetBoxes = cabinetList.value[currentIndex].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyB,
        pduId: cabinetList.value[currentIndex].keyb?.id
      }
    }

    if(cabinetList.value[currentIndex].id) {
      menuOptions.value[0]?.children?.splice(0,1)
    }
    if(!cabinetList.value[currentIndex].id) {
      menuOptions.value.splice(2,1)
      menuOptions.value[0]?.children?.splice(1,3)
      console.log(menuOptionsCopy.value[0])
    } else if(!cabinetList.value[currentIndex].cabinetBoxes && !cabinetList.value[currentIndex].cabinetPdus && !cabinetList.value[currentIndex].rackIndices) {
      menuOptions.value[0]?.children?.splice(1,1)
    }
  }

  menuOptions.value.splice(1,1)

  if(!editEnable.value) {
    menuOptions.value.splice(0,1)
  }

  operateMenu.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    add: isAdd,
    curIndex: currentIndex
  }
  console.log('operateMenu',e.target.className, operateMenu.value, cabinetList.value[currentIndex],menuOptions.value)
}
// 处理始端箱右击事件
const handleBarRightClick = (e) => {
  e.preventDefault()
  const container = e.currentTarget
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  operateMenuBox.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    curIndex: 0,
    type: 'bar'
  }
}
// 处理插接箱右击事件
const handlePluginRightClick = (e, type) => {
  e.preventDefault()
  const targetId = e.target.id || e.target.parentNode.id
  console.log('处理插接箱右击事件',type, e.target.className, e.target.parentNode, targetId, e.currentTarget)
  if (!targetId || !targetId.includes('box')) return
  const container = e.currentTarget
  const currentIndex = targetId.split('-')[1]
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  menuOptionsBox.value[0].value = {...machineColInfo[`bar${type}`].boxList[currentIndex],road: type}
  menuOptionsBox.value[1].value = machineColInfo[`bar${type}`].boxList[currentIndex]
  menuOptionsBox.value[1].label = "插接箱：" + machineColInfo[`bar${type}`].boxList[currentIndex].boxName
  
  if(!editEnable.value) {
    menuOptionsBox.value.splice(0,1)
  }
  
  operateMenuBox.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    curIndex: currentIndex,
    type
  }
}
const handleInitialDblick = (e, road) => {
  console.log('machineColInfo', machineColInfo)
  if(chosenBtn.value == 0) {
    push({path: '/bus/busmonitor/busmonitor/powerLoadDetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 1) {
    push({path: '/bus/busmonitor/busmonitor/buspowerdetail', query: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId}})
  } else if(chosenBtn.value == 2) {
    push({path: '/bus/busmonitor/busmonitor/buspowerdetail', query: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId}})
  } else if(chosenBtn.value == 3) {
    push({path: '/bus/busmonitor/busmonitor/buspowerfactor', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 4) {
    push({path: '/bus/busmonitor/busmonitor/buspowerdetail', query: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId}})
  } else if(chosenBtn.value == 7) {
    push({path: '/bus/busmonitor/busmonitor/busbalancedetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 8) {
    push({path: '/bus/busmonitor/busmonitor/bustem', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 10) {
    push({path: '/bus/busmonitor/busmonitor/busenergydetail', state: { devKey: machineColInfo[`bar${road}`].devIp + '-' + machineColInfo[`bar${road}`].barId,roomName: machineColInfo.roomName}})
  }
}
const handlePluginDblick = (e, road) => {
  const targetId = e.target.id || e.target.parentNode.id
  const index = targetId.split('-')[1]
  console.log('targetId', targetId, machineColInfo, index)
  if(chosenBtn.value == 0) {
    push({path: '/bus/busmonitor/boxmonitor/boxpowerLoadDetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 1) {
    push({path: '/bus/busmonitor/boxmonitor/boxpowerDetail', query: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 2) {
    push({path: '/bus/busmonitor/boxmonitor/boxpowerDetail', query: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 3) {
    push({path: '/bus/busmonitor/boxmonitor/boxpowerfactor', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 4) {
    push({path: '/bus/busmonitor/boxmonitor/boxpowerDetail', query: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 7) {
    push({path: '/bus/busmonitor/boxmonitor/boxbalancedetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 8) {
    push({path: '/bus/busmonitor/boxmonitor/boxtem', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  } else if(chosenBtn.value == 10) {
    push({path: '/bus/busmonitor/boxmonitor/boxenergydetail', state: { devKey: machineColInfo[`bar${road}`].boxList[index].boxKey, roomName: machineColInfo.roomName}})
  }
}
// 处理始端箱菜单点击事件
const handleBarOperate = async(type) => {
  const index = operateMenuBox.value.curIndex
  console.log('handleBarOperate', type, index, machineColInfo)
  if (type == 'edit') {
    handleConfig()
  }else{
    barChangeType.value = 'delete'
    machineColInfo.barA = null
    machineColInfo.barB = null
    cabinetList.value.forEach(item => {
      item.busIpA = null
      item.busIpB = null
      item.barIdA = null
      item.barIdB = null
      item.boxIndexA = null
      item.boxIndexB = null
      item.boxOutletIdA = null
      item.boxOutletIdB = null
      item.cabinetBoxes = null
    })
    console.log("machineColInfo.cabinetList",machineColInfo.cabinetList)
    instance?.deleteEveryConnection()
    // if(res != "1"){
    //    message.error('柜列母线删除失败!')
    // }
    operateMenuBox.value.show = false;
    // message.success('柜列母线删除成功!');
    // getMachineColInfo();
  }
}
// 处理插接箱/连接器菜单点击事件
const handleBoxOperate = async(type, road) => {
  const index = operateMenuBox.value.curIndex
  console.log('handleBoxOperate', type, index, machineColInfo)
  if (type == 'edit') {
    const data = machineColInfo[`bar${road}`].boxList[index]
    columnBoxForm.value.open(data)
  }else{
    const data = machineColInfo[`bar${road}`].boxList[index]
    const res = await MachineColumnApi.getDeleteAisleSingleBox({id: data.id})
    if(res != "1"){
       message.error('柜列插接箱/连接器单个删除失败!')
    }
    operateMenuBox.value.show = false;
    cabinetList.value.forEach(async item => {
      if(item[`boxIndex${road}`] == data.boxIndex) {
        item[`busIp${road}`] = null
        item[`barId${road}`] = null
        item[`boxIndex${road}`] = null
        item[`casId${road}`] = null
        item[`boxOutletId${road}`] = null
        item.cabinetBoxes[`boxIndex${road}`] = null
        item.cabinetBoxes[`boxKey${road}`] = null
        item.cabinetBoxes[`outletId${road}`] = null
        const resItem = await CabinetApi.saveCabinetInfo({
          ...item,
          pduBox: true
        })
        console.log("resCab",{...item},resItem)
      }
    })
    message.success('柜列插接箱/连接器单个删除成功!');
    setTimeout(() => { getMachineColInfo() } ,1000)
  }
}
// 跳转机柜
const handleJump = (data) => {
 let target = {} as any
  if (data) {
    target = data
  } else {
    target = cabinetList.value[operateMenu.value.curIndex]
  }
  console.log('target', target)
  if (!target.id) {
    message.error(`该机柜暂未保存绑定，无法跳转`)
    return
  }
  if(chosenBtn.value == 0) {
    push({path: '/cabinet/cab/cabinetPowerLoadDetail', query: {cabinet: target.id,roomId: target.roomId,roomName: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 1) {
    push({path: '/cabinet/cab/detail', state: {id: target.id,roomId: target.roomId,type: 'hour',location: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 2) {
    push({path: '/cabinet/cab/detail', state: {id: target.id,roomId: target.roomId,type: 'hour',location: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 3) {
    //弹窗
    openPFDetail(target)
  } else if(chosenBtn.value == 4) {
    push({path: '/cabinet/cab/detail', state: {id: target.id,roomId: target.roomId,type: 'hour',location: machineColInfo.roomName,cabinetName: target.cabinetName}})
  } else if(chosenBtn.value == 7) {
    cabinetBalanceDetail.value.open({
      id: target.id
    })
    // push({path: '/cabinet/cab/balance', query: {openDetailFlag: 1,id: target.id}})
  } else if(chosenBtn.value == 8) {
    push({path: '/cabinet/cab/cabinetenvdetail', query: { id: target.id }})
  } else if(chosenBtn.value == 9) {
    push({path: '/cabinet/cab/screen', state: { id: target.id,roomId: target.roomId }})
  } else if(chosenBtn.value == 10) {
    push({path: '/cabinet/cab/energyDetail', query: { cabinetId: target.id,cabinetroomId: target.roomId,roomName: machineColInfo.roomName,cabinetName: target.cabinetName }})
  }
}

const handleMenuBox = (value) => {
  operateMenuBox.value.show = false

  switch (value[1]) {
    case '插接箱编辑':
      handleBoxOperate('edit', value[0].road)
      break;
    
    case '插接箱删除':
      handleBoxOperate('delete', value[0].road)
      break;

    case '插接箱配电':
      push({ 
        path: '/bus/busmonitor/boxmonitor/boxpowerdetail', 
        query: { 
          devKey: value[0].boxKey,
          roomName: machineColInfo.roomName,
        } 
      });
      break;

    case '插接箱需量':
      boxRequirement.value.open({
        boxId: value[0].id,
        location: machineColInfo.roomName
      })
      break;

    case '插接箱温度':
      boxTemDetail.value.open({
        devKey: value[0].boxKey
      })
      break;

    case '插接箱供电平衡':
      boxBalanceDetail.value.open({
        devKey: value[0].boxKey,
        roomName: machineColInfo.roomName,
        boxId: value[0].id
      })
      break;

    case '插接箱设备管理':
      window.open('https://192.168.1.99/index.html', '_blank')
      break;
    
    default:
      console.warn('未知操作类型:', value[1]);
      break;
  }
}

const handleMenu = (value) => {
  console.log(value)
  operateMenu.value.show = false
  
  switch (value[1]) {
    case '柜列配电':
      push({ 
        path: '/aisle/aislemonitor/columnHome', 
        query: { id: value[0], roomId: machineColInfo.roomId } 
      });
      break;
    
    case '柜列用能':
      push({ 
        path: '/aisle/aislemonitor/aisleenergydetail', 
        query: { roomId: machineColInfo.roomId, id: value[0], location: machineColInfo.roomName } 
      });
      break;
    
    case '柜列需量':
      aisleRequirement.value.open({ 
        id: value[0], 
        location: `${machineColInfo.roomName}-${machineColInfo.aisleName}`
      })
      break;
    
    case '柜列供电平衡':
      aisleBalanceDetail.value.open({id: value[0]})
      break;

    case '柜列功率因数':
      aisleFactorDetail.value.open({ 
        id: value[0], 
        location: `${machineColInfo.roomName}-${machineColInfo.aisleName}`
      })
      break;

    case '机柜负荷':
      push({ 
        path: '/cabinet/cab/cabinetPowerLoadDetail', 
        query: { 
          cabinet: value[0].id, 
          roomId: value[0].roomId,
          roomName: machineColInfo.roomName,
          cabinetName: value[0].cabinetName
        } 
      });
      break;

    case '机柜配电':
      push({ 
        path: '/cabinet/cab/detail', 
        state: { 
          id: value[0].id, 
          roomId: value[0].roomId,
          type: 'hour',
          location: machineColInfo.roomName,
          cabinetName: value[0].cabinetName
        } 
      });
      break;

    case '机柜温度':
      push({ 
        path: '/cabinet/cab/cabinetenvdetail', 
        query: { 
          id: value[0].id
        } 
      });
      break;

    case '机柜用能':
      push({ 
        path: '/cabinet/cab/energyDetail', 
        query: { 
          cabinetId: value[0].id,
          cabinetroomId: value[0].roomId,
          roomName: machineColInfo.roomName,
          cabinetName: value[0].cabinetName
        } 
      });
      break;

    case '机柜供电平衡':
      cabinetBalanceDetail.value.open({
        id: value[0].id
      })
      break;

    case '机柜功率因数':
      openPFDetail({
        id: value[0].id,
        cabinetName: value[0].cabinetName
      })
      break;

    case 'A路母线负荷':
      push({ 
        path: '/bus/busmonitor/busmonitor/powerLoadDetail', 
        state: { 
          devKey: value[0].devKeyA
        } 
      });
      break;
      
    case 'B路母线负荷':
      push({ 
        path: '/bus/busmonitor/busmonitor/powerLoadDetail', 
        state: { 
          devKey: value[0].devKeyB
        } 
      });
      break;

    case 'A路母线配电':
      push({ 
        path: '/bus/busmonitor/busmonitor/buspowerdetail', 
        query: { 
          devKey: value[0].devKeyA
        } 
      });
      break;
      
    case 'B路母线配电':
      push({ 
        path: '/bus/busmonitor/busmonitor/buspowerdetail', 
        query: { 
          devKey: value[0].devKeyB
        } 
      });
      break;

    case 'A路母线温度':
      busTemDetail.value.open({
        devKey: value[0].devKeyA
      })
      break;
      
    case 'B路母线温度':
      busTemDetail.value.open({
        devKey: value[0].devKeyB
      })
      break;

    case 'A路配电':
    case 'B路配电':
      push({ 
        path: '/pdu/power/pdudisplayscreen', 
        query: { 
          devKey: value[0].devKey
        } 
      });
      break;

    case 'A路插接箱配电':
    case 'B路插接箱配电':
      push({ 
        path: '/bus/busmonitor/boxmonitor/boxpowerdetail', 
        query: { 
          devKey: value[0].devKey,
          roomName: machineColInfo.roomName,
        } 
      });
      break;

    case 'A路母线需量':
      busRequirement.value.open({
        devKey: value[0].devKeyA
      })
      break;

    case 'B路母线需量':
      console.log(value[0])
      busRequirement.value.open({
        devKey: value[0].devKeyB
      })
      break;

    case 'A路插接箱需量':
    case 'B路插接箱需量':
      boxRequirement.value.open({
        boxId: value[0].boxId,
        location: machineColInfo.roomName
      })
      break;

    case 'A路插接箱温度':
    case 'B路插接箱温度':
      boxTemDetail.value.open({
        devKey: value[0].devKey
      })
      break;

    case 'A路需量':
    case 'B路需量':
      pduRequirement.value.open({
        devKey: value[0].devKey,
        pduId: value[0].pduId,
        openDetailFlag: 1
      })
      break;

    case 'A路母线三相平衡':
      busBalanceDetail.value.open({
        devKey: value[0].devKeyA
      })
      break;

    case 'B路母线三相平衡':
      busBalanceDetail.value.open({
        devKey: value[0].devKeyB
      })
      break;

    case 'A路插接箱供电平衡':
    case 'B路插接箱供电平衡':
      boxBalanceDetail.value.open({
        devKey: value[0].devKey,
        roomName: machineColInfo.roomName,
        boxId: value[0].boxId
      })
      break;

    case 'A路供电平衡':
    case 'B路供电平衡':
      pduBalanceDetail.value.open({
        devKey: value[0].devKey,
        pduId: value[0].pduId
      })
      break;

    case 'A路设备管理':
    case 'B路设备管理':
    case 'A路插接箱设备管理':
    case 'B路插接箱设备管理':
      window.open('https://192.168.1.99/index.html', '_blank')
      break;

    case '新增机柜':
      handleOperate('add');
      break;
    
    case '机柜编辑':
      handleOperate('add')
      break;
    
    case '机柜删除':
      deleteCabinet(false);
      break;
    
    case '机柜解绑':
      deleteCabinet(true);
      break;
    
    case '柜列编辑':
      editMachine();
      break;
    
    case '柜列删除':
      deleteMachine();
      break;
    
    default:
      console.warn('未知操作类型:', value[1]);
      break;
  }
}

// 编辑柜列弹框
const editMachine = () => {
  machineForm.value.open('edit', {...machineColInfo}, operateMenu.value);
  operateMenu.value.show = false;
}

// 处理增加/编辑机柜
const handleChange = async(data) => {
  let messageAisleFlag = "修改成功！";
  let messageAisleFlagError = "修改失败！";
  if(data.type == 1){
    let asileObject = {id:data.id,
        roomId: data.roomId,
        aisleName:data.aisleName,
        aisleLength:data.length,
        xCoordinate:data.xCoordinate,
        yCoordinate:data.yCoordinate,
        direction:data.direction,
        powerCapacity:data.powerCapacity,
        eleAlarmDay:data.eleAlarmDay,
        eleAlarmMonth:data.eleAlarmMonth,
        eleLimitDay:data.eleLimitDay,
        eleLimitMonth:data.eleLimitMonth,
        pduBar: data.pduBar
    }

    const flagRes = await MachineRoomApi.findAddAisleVerify(asileObject)

    if(flagRes) {
      message.error(messageAisleFlagError + "可能原因如下：该柜列的位置的长度范围内有机柜或柜列,柜列同名,柜列超出机房长度范围")
      return
    }

    const aisleRes = await MachineRoomApi.saveRoomAisle(asileObject) 
    if(aisleRes != null || aisleRes != "") {
      getMachineColInfo()
      message.success(messageAisleFlag);
    }
  }
}

const deleteCabinet = (flag) => {
  const index = operateMenu.value.curIndex
  const cabItem = cabinetList.value[index]

  if(!flag && (cabItem.cabinetBoxes || cabItem.cabinetPdus || cabItem.rackIndices)) {
    message.warning(`该机柜已绑定，无法删除，请先删除绑定关系`)
    return
  }
  let messageTooltip = flag ? '确认解绑吗？' : '确认删除吗？'
  ElMessageBox.confirm(messageTooltip, '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    let cabinetRes = null
    if(cabItem.cabinetBoxes) {
      console.log("aaaaaa---cabItem",cabItem)
      cabinetRes = await CabinetApi.deleteCabinetInfo({
        id: cabItem.id,
        type: 2
      })
    } else if(cabItem.cabinetPdus) {
      cabinetRes = await CabinetApi.deleteCabinetInfo({
        id: cabItem.id,
        type: 1
      })
    } else if(cabItem.rackIndices) {
      cabinetRes = await CabinetApi.deleteCabinetInfo({
        id: cabItem.id,
        type: 3
      })
    } else if(!flag) {
      cabinetRes = await CabinetApi.deleteCabinetInfo({
        id: cabItem.id,
        type: 4
      })
    }
    if(cabinetRes) {
      message.success(flag ? '解绑成功' : '删除成功')
    } else {
      message.success(flag ? '解绑失败' : '删除失败')
    }
    getMachineColInfo()
  })
  operateMenu.value.show = false
}

// 处理菜单点击事件
const handleOperate = (type) => {
  operateMenu.value.show = false
  const index = operateMenu.value.curIndex
  if (type == 'add' || type == 'edit') {
    let info = {
      roomId: machineColInfo.roomId,
      roomName: machineColInfo.roomName,
      aisleId: machineColInfo.id,
      barA: false,
      index: Number(index)+1
    } as any
    if (machineColInfo.barA) {
      info = {
        roomId: machineColInfo.roomId,
        roomName: machineColInfo.roomName,
        aisleId: machineColInfo.id,
        index: Number(index)+1,
        barA: machineColInfo.barA.boxList,
        barB: machineColInfo.barB.boxList,
        barIdA: machineColInfo.barA.barId,
        busIpA: machineColInfo.barA.devIp,
        barIdB: machineColInfo.barB.barId,
        busIpB: machineColInfo.barB.devIp,
        boxAmount: machineColInfo.barA.boxList.length
      }
    }
    const data = cabinetList.value[index]
    console.log("cabinetList.value[index]",cabinetList.value[index])
    cabinetForm.value.open(type, data, null, machineColInfo)
  } else if (type == 'delete') {
    ElMessageBox.confirm('确认删除吗？', '提示', {
      confirmButtonText: '确 认',
      cancelButtonText: '取 消',
      type: 'warning'
    }).then(async () => {
      const cabItem = cabinetList.value[index]
      if(cabItem.cabinetBoxes) {
        console.log("aaaaaa---cabItem",cabItem)
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 2
        })
      } else if(cabItem.cabinetPdus) {
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 1
        })
      } else if(cabItem.rackIndices) {
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 3
        })
      } else {
        cabinetList.value.splice(index, 1, {})
        await CabinetApi.deleteCabinetInfo({
          id: cabItem.id,
          type: 4
        })
      }
      getMachineColInfo()
    })
  }
  console.log('handleOperate', machineColInfo)
}
// 处理编辑事件
const handleEditClick = () => {
  editEnable.value = true
  controlEndpointShow(true)
}
// 处理编辑取消事件
const handleCancel = () => {
  editEnable.value = false
  barChangeType.value = ''
  getMachineColInfo()
}
// 处理配置点击事件  打开配置弹窗
const handleConfig = () => {
  console.log('handleConfig', machineColInfo)
  let data = null as any | null
  if(machineColInfo.barA) {
    console.log('machineColInfo.barA', machineColInfo.barA)
    const boxList = machineColInfo.barA.boxList
    data = {
      barIdA: machineColInfo.barA.barId,
      ipA: machineColInfo.barA.devIp,
      barIdB: machineColInfo.barB.barId,
      directionA: machineColInfo.barA.direction,
      directionB: machineColInfo.barB.direction,
      casAddrA: machineColInfo.barA.casAddr,
      casAddrB: machineColInfo.barB.casAddr,
      ipB: machineColInfo.barB.devIp,
      cjxAmount: boxList.length,
    }
    console.log(data)
  }
  console.log('???',data)
  columnForm.value.open(data)
}
// 处理保存事件
const handleSubmit = () => {
  console.log('handleSubmit')
  saveMachineBus()
}
// 母线弹窗确认后的处理
const handleFormPlugin = async (data) => {
  console.log('handleFormSave', data)
  barChangeType.value = 'edit'
  let arrA = [] as any
  let arrB = [] as any
  const machineColInfoLength = (machineColInfo.barA && machineColInfo.barA.boxList) ? machineColInfo.barA.boxList.length : 0
  for(let i=0; i < data.cjxAmount; i++) {
    if(i < machineColInfoLength) {
      arrA.push(machineColInfo.barA.boxList[i])
      arrB.push(machineColInfo.barB.boxList[i])
    } else {
      arrA.push({
        casAddr: i+2,
        outletNum: 3,
        type: 0,
        boxName: 'box-' + i,
        boxIndex: i,
      })
      arrB.push({
        casAddr: i+2,
        outletNum: 3,
        type: 0,
        boxName: 'box-' + i,
        boxIndex: i,
      })
    }
  }
  const boxA = {
    barId: data.barIdA,
    casAddr: data.casAddrA,
    devIp: data.ipA,
    path: 'A',
    direction: data.directionA,
    boxList: arrA
  }
  const boxB = {
    barId: data.barIdB,
    casAddr: data.casAddrB,
    devIp: data.ipB,
    path: 'B',
    direction: data.directionB,
    boxList: arrB
  }
  // machineColInfo.barA = boxA
  // machineColInfo.barB = boxB
  // machineColInfo.pduBar = 1

  const res = await MachineColumnApi.saveAisleDetail({
    ...machineColInfo,
    barA: boxA,
    barB: boxB,
    pduBar: 1
  })
  if(res) {
    message.success('母线配置成功')
  } else {
    message.error('母线配置失败')
  }
  getMachineColInfoReal()
  // console.log('machineColInfo', machineColInfo)
  // toCreatConnect() // 因为添加插接箱需要添加瞄点 所以要创建
}
// 机柜弹窗确认后的处理
const handleFormCabinet = (data) => {
  // console.log('handleFormCabinet', data, operateMenu.value)
  data.index = +operateMenu.value.curIndex + 1
  cabinetList.value.splice(operateMenu.value.curIndex, 1, data)
  if (machineColInfo.barA && machineColInfo.pduBar) nextTick(() => {
    addCabinetAnchor(operateMenu.value.curIndex, data)
  })
  getMachineColInfo()
}
// 插接箱弹窗确认后处理
const handleFormBox = (data) => {
  const arr = [] as any
  const bar = `bar${operateMenuBox.value.type}`
  const index = operateMenuBox.value.curIndex
  const length = data.outletNum
  console.log(data)
  machineColInfo[bar].boxList.splice(index, 1, data)
  console.log(machineColInfo[bar].boxList)
  console.log(index)
  cabinetList.value.forEach(item => {
      console.log(item[`boxIndex${operateMenuBox.value.type}`],index)
    if(item[`boxIndex${operateMenuBox.value.type}`] == data.boxIndex && item[`boxOutletId${operateMenuBox.value.type}`] > data.outletNum) {
      item[`busIp${operateMenuBox.value.type}`] = null
      item[`barId${operateMenuBox.value.type}`] = null
      item[`boxIndex${operateMenuBox.value.type}`] = null
      item[`casId${operateMenuBox.value.type}`] = null
      item[`boxOutletId${operateMenuBox.value.type}`] = null
      item.cabinetBoxes[`boxIndex${operateMenuBox.value.type}`] = null
      item.cabinetBoxes[`boxKey${operateMenuBox.value.type}`] = null
      item.cabinetBoxes[`outletId${operateMenuBox.value.type}`] = null
      console.log(item)
    } else if(item[`boxIndex${operateMenuBox.value.type}`] == data.boxIndex && data.casAddr != '' && item[`casId${operateMenuBox.value.type}`] != Number(data.casAddr) ) {
      console.log(data)
      item[`casId${operateMenuBox.value.type}`] = Number(data.casAddr)
      console.log(item)
    }
  })
  instance?.deleteEveryConnection()
  showAllConnect()
  for (let i=1; i <= length; i++) {
    const boxElement = document.getElementById('plugin-' + index + '_' + operateMenuBox.value.type + '-' + i) as Element
    if (!boxElement) {
      arr.push(i)
    }
  }
  nextTick(() => {
    for (let i=1; i <= length; i++) {
      const boxElement = document.getElementById('plugin-' + index + '_' + operateMenuBox.value.type + '-' + i) as Element
      if (arr.includes(i)) {
        console.log('------i', i, boxElement)
        instance?.addEndpoint(boxElement, {
          source: true,
          target: true,
          endpoint: 'Dot',
          paintStyle: {
            strokeWidth: 1,
            stroke: operateMenuBox.value.type == 'A' ? '#43939c' : '#acd997',
            dashstyle: '5 5'
          }
        })
      }
      // 更新瞄点
      instance?.revalidate(boxElement)
    }
  })
}
// 接口获取柜列状态数据详情
const getDataDetail = async() => {
  const res = await MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})
  console.log('接口获取柜列状态数据详情', res)
  handleDataDetail(res)
}
// 处理柜列状态数据详情
const handleDataDetail = (res) => {
  console.log('machineColInfo', machineColInfo)
  let show = cabinetList.value
  console.log(show)
  res.cabinetList && res.cabinetList.forEach(cab => {
    cabinetList.value.forEach((item, index) => {
      if (item.id == cab.id) {
        const common = {
          type: 'bar',
          emphasis: {
            focus: 'series'
          },
          label: {
            show: true,
            formatter: '{c}', // 显示数据值
          },
        }
        let seriesA = [
          {
            name: 'L1',
            data: [(cab.lineCurA ? cab.lineCurA[0] : 0), (cab.lineCurB ? cab.lineCurB[0] : 0)],
            ...common
          }
        ] as any
        if (cab.lineCurA && cab.lineCurB && cab.lineCurA.length == 3 && cab.lineCurB.length == 3) {
          seriesA = [...seriesA, {
            name: 'L2',
            data: [(cab.lineCurA ? cab.lineCurA[1] : 0), (cab.lineCurB ? cab.lineCurB[1] : 0)],
            ...common
          }, {
            name: 'L3',
            data: [(cab.lineCurA ? cab.lineCurA[2] : 0), (cab.lineCurB ? cab.lineCurB[2] : 0)],
            ...common
          }]
        }
        cabinetList.value[index] = {
          ...cab,
          ...item,
          echartsOptionLoad: { // 负载
            xAxis: {
              type: 'category',
              data: ['负载率'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-30',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true,
            },
            yAxis: {
              type: 'value',
              max: 100,
              show: false,
            },
            series: [
              {
                name: 'load',
                data: [cab.loadRate ? cab.loadRate.toFixed(0) : 0],
                type: 'bar',
                barWidth: '100%',
                itemStyle: {
                  color: function (params) {
                      const value = params.value;
                      if (value < 50) {
                          return `rgba(22, 198, 12, ${(value+50)/100})`; 
                      } else if (value < 75) {
                          return `rgba(0, 120, 215, ${(150-value)/100})`;
                      } else if (value < 90) {
                          return `rgba(255, 225, 0, ${(value+10)/100})`;
                      } else {
                        return `rgba(240, 58, 23, ${value/100})`
                      }
                  }
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}%', // 显示数据值
                  color: 'white'
                },
              }
            ]
          },
          echartsOptionA1: { // 电流
            xAxis: {
              type: 'category',
              data: ['A路','B路'],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              },
              axisLine: {
                show: false
              }
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [{
              name: '电流',
              type: 'bar',
              itemStyle: {
                color: function (params) {
                    const index = params.dataIndex;
                    if (index == 0) {
                        return '#E5B849';
                    } else if (index == 1) {
                        return '#C8603A';
                    } else {
                      return '#fa3333'
                    }
                }
              },
              data: [cab.lineCurA ? cab.lineCurA[0] : 0,cab.lineCurB ? cab.lineCurB[0] : 0],
              label: {
                show: true, // 显示数值
                position: 'top', // 数值显示在柱形图顶部
                formatter: '{c}A',
                fontSize: 12, // 数值字体大小
                color: 'white'
              }
            }]
          },
          echartsOptionV1: { // 电压
            tooltip: {
              trigger: 'axis',
              axisPointer: {
                type: 'shadow'
              }
            },
            xAxis: {
              type: 'category',
              data: ['A路','B路'],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              },
              axisLine: {
                show: false
              }
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [{
              name: '电压',
              type: 'bar',
              itemStyle: {
                color: function (params) {
                    const index = params.dataIndex;
                    if (index == 0) {
                        return '#075F71';
                    } else if (index == 1) {
                        return '#119CB5';
                    } else {
                      return '#fa3333'
                    }
                }
              },
              data: [cab.lineVolA ? cab.lineVolA[0] : 0,cab.lineVolB ? cab.lineVolB[0] : 0],
              label: {
                show: true, // 显示数值
                position: 'top', // 数值显示在柱形图顶部
                formatter: '{c}V',
                fontSize: 12, // 数值字体大小
                color: 'white'
              }
            }]
          },
          echartsOptionFactor: { // 功率因数
            xAxis: {
              type: 'category',
              data: ['功率因数'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-26',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: 1, 
              show: false,
            },
            series: [
              {
                name: 'load',
                data: [cab.powerFactor],
                type: 'bar',
                barWidth: '100%',
                itemStyle: {
                  color: function (params) {
                      const value = params.value;
                      if (value < 0.75) {
                          return `rgba(240, 58, 23, ${1.3-value})`;
                      } else if (value < 0.85) {
                          return `rgba(255, 192, 0, ${1.75-value})`;
                      } else if (value < 0.9) {
                          return '#0078d7';
                      } else {
                        return '#16c60c'
                      }
                  }
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}', // 显示数据值
                  color: 'white'
                },
              }
            ]
          },
          echartsOptionApparent: { //功率
            xAxis: {
                type: 'category',
                data: ['功率'],
                axisTick: {
                  show: false
                },
                axisLine: {
                  show: false
                },
                axisLabel: {
                    show: false // 隐藏 ECharts 自带的标签
                }
            },
            yAxis: {
              type: 'value',
              show: false,
              max: cab.powCapacity*1.2
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '3.2%',
              top: '8%',
              containLabel: true
            },
            series: [
                {
                    name: 'Small Red Bar',
                    type: 'bar',
                    data: [cab.powActive ? cab.powActive.toFixed(1) : 0], // 左侧小柱形图的数据
                    itemStyle: {
                        color: '#5470c6'
                    },
                    stack: 'Ad',
                    barWidth: '100%',
                    label: {
                        show: true, // 显示数值
                        position: 'inside', // 数值显示在柱形图内部
                        formatter: function(params) {
                          return params.value === 0 ? '' : params.value+'kW';
                        },
                        color: 'white', // 数值颜色
                        fontSize: 12 // 数值字体大小
                    }
                },
                {
                    name: 'Small Blue Bar',
                    type: 'bar',
                    data: [cab.powReactive ? cab.powReactive.toFixed(1) : 0], // 右侧小柱形图的数据
                    itemStyle: {
                        color: '#FFC000'
                    },
                    stack: 'Ad',
                    barWidth: '100%',
                    label: {
                        show: true, // 显示数值
                        position: 'inside', // 数值显示在柱形图内部
                        formatter: '{c}kVar',
                        color: 'white', // 数值颜色
                        fontSize: 12 // 数值字体大小
                    }
                }
            ]
          },
          echartsOptionBalance: {
            xAxis: {
              type: 'category',
              data: ['A路', 'B路'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            yAxis: {
              type: 'value',
              show: false,
              max: cab.powCapacity
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            series: [
              {
                name: '有功功率',
                type: 'bar',
                data: [cab.powActiveA ? cab.powActiveA.toFixed(1) : 0,cab.powActiveB ? cab.powActiveB.toFixed(1) : 0], // 大柱形图的数据
                itemStyle: {
                  color: '#5470c6'
                },
                label: {
                  show: true, // 显示数值
                  position: 'top', // 数值显示在柱形图顶部
                  fontSize: 12, // 数值字体大小
                  color: 'white'
                }
              },
            ]
          },
          echartsOptionTemp: { // 温度
            xAxis: {
              type: 'category',
              data: ['冷通道温度','热通道温度'],
              axisTick: {
                show: false
              },
              axisLine: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-24',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              show: false,
            },
            series: [
              {
                data: [cab.temData ? cab.temData : 0,cab.temDataHot ? cab.temDataHot : 0],
                type: 'bar',
                itemStyle: {
                  color: function (params) {
                      const index = params.dataIndex;
                      const value = params.value;
                      if (index == 0) {
                        if (value <= 20) {
                          return `rgba(0, 120, 215, ${value+80})`;
                        } else if (value < 24) {
                          return `rgba(0, 128, 0, ${(value+76)/100})`;
                        } else if (value < 27) {
                          return `rgba(50, 205, 50, ${(124-value)/100})`;
                        } else if (value < 30) {
                          return `rgba(255, 192, 0, ${(value+70)/100})`;
                        } else if (value < 35) {
                          return `rgba(247, 99, 12, ${(value+65)/100})`;
                        } else {
                          return `rgba(232, 18, 36, ${value})`;
                        }
                      } else if (index == 1) {
                        if (value <= 30) {
                          return `rgba(0, 128, 0, ${(value+70)/100})`;
                        } else if (value < 35) {
                          return `rgba(50, 205, 50, ${(130-value)/100})`;
                        } else if (value < 40) {
                          return `rgba(255, 192, 0, ${(value+60)/100})`;
                        } else if (value < 45) {
                          return `rgba(247, 99, 12, ${(value+55)/100})`;
                        } else {
                          return `rgba(232, 18, 36, ${value})`;
                        }
                      } else {
                        return '#fa3333'
                      }
                  }
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}°C', // 显示数据值
                  color: 'white'
                },
              }
            ]
          },
          echartsOptionCapacity: { // 容量
            xAxis: {
              type: 'category',
              data: [`总容量:${item.cabinetHeight}`],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-22',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: item.cabinetHeight, 
              show: false,
            },
            series: [
              {
                data: [item.usedSpace],
                type: 'bar',
                barWidth: '100%',
                itemStyle: {
                  color: function (params) {
                      const value = params.value;
                      if (value/item.cabinetHeight < 0.5) {
                          return '#3bbb00';
                      } else if (value/item.cabinetHeight < 0.7) {
                          return '#ffc402';
                      } else {
                        return '#fa3333'
                      }
                  }
                },
                backgroundStyle: {
                  color: 'rgba(180, 180, 180, 0.2)'
                },
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '已用容量:\n{c}', // 显示数据值
                  color: 'white'
                },
              }
            ]
          },
          echartsOptionEq: { // 用能
            xAxis: {
              type: 'category',
              data: ['昨日用能'],
              axisTick: {
                show: false
              },
              axisLabel: {
                  show: false // 隐藏 ECharts 自带的标签
              }
            },
            grid: {
              left: '-30',
              right: '0',
              bottom: '4%',
              top: '8%',
              containLabel: true
            },
            yAxis: {
              type: 'value',
              max: item.powCapacity*24, 
              show: false,
            },
            series: [
              {
                name: 'load',
                data: [item.yesterdayEq ? item.yesterdayEq.toFixed(1) : 0],
                type: 'bar',
                barWidth: '100%',
                label: {
                  show: true,
                  position: 'top', // 顶部显示
                  formatter: '{c}kW·h', // 显示数据值
                  color: 'white'
                },
              }
            ]
          }
        }
        // console.log('ssssssss----------', cab, cab.id)
        return
      }
    })
  })
  emit('backData', [...cabinetList.value], scaleValue.value)
  echartsOptionCab.value = {
    title: {
      text: '机柜列实时统计'
    },
    grid: {
      left: '45',
      right: '80',
      bottom: '0',
      top: '50',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: cabinetList.value.map(item => item.cabinetName || '')
    },
    legend:{
      top: '0',
      right: '80',
      selectedMode: 'single'
    },
    yAxis: {
      type: 'value',
      // show: false
    },
    series: [
      {
        name:'有功功率',
        data: cabinetList.value.map(item => item.powActive == undefined ? null : item.powActive),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
      {
        name:'昨日用能',
        data: cabinetList.value.map(item => item.yesterdayEq == undefined ? null : item.yesterdayEq),
        type: 'bar',
        barWidth: '92%',
        label: {
          show: true,
          position: 'top', // 顶部显示
          formatter: '{c}', // 显示数据值
        },
      },
    ]
  }
  console.log('接口获取柜列状态数据详情end', cabinetList.value, machineColInfo)
}
const getCabinetColorAll = async () => {
  const res = await IndexApi.getCabinetColorAll()
  if (res != null) {
    tempList.value = res
    tempList.value.forEach((item,index) => {
      statusList[8].push({
        name: item.min + '°C ~ ' + item.max + '°C',
        selected: true,
        value: index+1,
        color: item.color,
        hotName: item.hotMin + '°C ~ ' + item.hotMax + '°C',
        hotColor: item.hotColor
      })
    })
  }
}
// 处理母线插接箱的初始化处理
// const handleBusInit = (data) => {
//   if(data.pduBar == 1 && data.barA && data.barA.boxList) {
//     data.barA.boxList.forEach((item,index) => {
//       machineColInfo.barA.boxList[index].boxName = 'plugin-' + index
//     })
//     data.barB.boxList.forEach((item,index) => {
//       machineColInfo.barB.boxList[index].boxName = 'plugin-' + index
//     })
//   }
//   console.log('machineColInfo', machineColInfo, data)
// }
// 接口获取柜列信息
const getMachineColInfo = async() => {
  const result = await MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})

  emit('sendList', result);
    //push({path: '/aisle/index', state: { roomDownVal: result.roomId}});
    Object.assign(machineColInfo, result);
    handleCabinetList(result); 
    // handleBusInit(result);
    console.log('getMachineColInfo', result);
}

// 接口获取柜列信息
const getMachineColInfoReal = async() => {
  const result = await MachineColumnApi.getAisleDetail({id: queryParams.cabinetColumnId})

  emit('sendList', result);

  Object.assign(machineColInfo, result);
  handleCabinetListReal(result); 
  
  console.log('getMachineColInfo', result);
}


const saveMachineBus = async() => {
  const filterCabinet = cabinetList.value.filter(item => item.cabinetName)
  console.log('filterCabinet', filterCabinet)
  console.log('machineColInfo', machineColInfo)
  // filterCabinet.forEach((cab, index) => {
  //   if (Number.isInteger(cab.boxIndexA)) {
  //     const target = machineColInfo.barA.boxList.find(box => box.boxIndex == cab.boxIndexA)
  //     if (target) cab.boxNameA = target.boxName
  //   }
  //   if (Number.isInteger(cab.boxIndexB)) {
  //     const target = machineColInfo.barB.boxList.find(box => box.boxIndex == cab.boxIndexB)
  //     if (target) cab.boxNameB = target.boxName
  //   }
  // })
  console.log('cabinetList', filterCabinet, {
    ...machineColInfo,
    length: cabinetList.value.length,
    cabinetList: filterCabinet,
  })
  const res = await MachineColumnApi.saveAisleDetail({
    ...machineColInfo,
    length: cabinetList.value.length,
    cabinetList: filterCabinet
  })
  if(filterCabinet.length && barChangeType.value != 'edit' && barChangeType.value != 'delete') {
    filterCabinet.forEach(async (cab, index) => {
      const resCab = await CabinetApi.saveCabinetInfo({
        ...cab,
        pduBox: true
      })
      console.log("resCab",{...cab},resCab)
    })
  }
  message.success('保存成功！')
  editEnable.value = false
  barChangeType.value = ''
  setTimeout(() => { getMachineColInfo() } ,1000)
  console.log('saveMachineBus', res)
}
// 处理机柜列表
const handleCabinetList = async(data) => {
  console.log('处理机柜列表', data)
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({
      roomId: data.roomId,
      aisleId: data.id,
      index: i+1
    })
  }
  // 给机柜要连接的插接箱 找到对应的下标
  data.cabinetList && data.cabinetList.forEach(item => {
    if(item.index > 0) {
      arr.splice(item.index - 1, 1, {...item,deviation: item.powActiveA && item.powActiveB ? (Math.abs(item.powActiveA-item.powActiveB)*100/Math.max(item.powActiveA,item.powActiveB))?.toFixed(0) : 0})
    }
  })
  console.log('arr', arr)
  cabinetList.value = arr
  handleDataDetail(data)
  handleCssScale()
  await toCreatConnect()
  controlEndpointShow(true)
}
// 实时处理机柜列表
const handleCabinetListReal = async(data) => {
  // data.cabinetList && data.cabinetList.forEach(item => {
  //   if(item.index > 0) {
  //     cabinetList.value[item.index - 1].runStatus = item.runStatus
  //   }
  // })
  console.log('处理机柜列表', data)
  const arr = [] as any
  for (let i=0; i < data.length; i++) {
    arr.push({
      roomId: data.roomId,
      aisleId: data.id,
      index: i+1
    })
  }
  // 给机柜要连接的插接箱 找到对应的下标
  data.cabinetList && data.cabinetList.forEach(item => {
    if(item.index > 0) {
      arr.splice(item.index - 1, 1, {...item,deviation: item.powActiveA && item.powActiveB ? (Math.abs(item.powActiveA-item.powActiveB)*100/Math.max(item.powActiveA,item.powActiveB))?.toFixed(0) : 0})
    }
  })
  console.log('arr', arr)
  cabinetList.value = arr
  handleDataDetail(data)
  updateCabinetConnect()
}
// 增加空机柜
const addMachine = async () => {
  console.log('addMachine')
  // cabinetList.value.push({})
  // updateCabinetConnect()
  let asileObject = {
    id:machineColInfo.id,
    roomId: machineColInfo.roomId,
    aisleName:machineColInfo.aisleName,
    aisleLength:machineColInfo.length+1,
    xCoordinate:machineColInfo.xCoordinate,
    yCoordinate:machineColInfo.yCoordinate,
    direction:machineColInfo.direction
  }
  const flagRes = await MachineRoomApi.findAddAisleVerify(asileObject)

  if(flagRes) {
    message.error("增加失败！可能原因如下：该柜列的位置的长度范围内有机柜或柜列,柜列同名,柜列超出机房长度范围")
    return
  }

  const aisleRes = await MachineRoomApi.saveRoomAisle(asileObject) 
  if(aisleRes) {
    message.success('增加成功！')
  } else {
    message.error('增加失败！')
  }
  getMachineColInfoReal()
}
// 删除空机柜
const deleteMachine = async () => {
  let minAmount = machineColInfo.cabinetList ? machineColInfo.cabinetList[machineColInfo.cabinetList.length-1].index : 0
  if(machineColInfo.length-1 < minAmount) {
    message.error("减少失败！当前柜列中机柜最大下标为" + minAmount + ",要减少的位置不能为空")
    return
  }
  // console.log('deleteMachine')
  // cabinetList.value.pop()
  // updateCabinetConnect()
  let asileObject = {
    id:machineColInfo.id,
    roomId: machineColInfo.roomId,
    aisleName:machineColInfo.aisleName,
    aisleLength:machineColInfo.length-1,
    xCoordinate:machineColInfo.xCoordinate,
    yCoordinate:machineColInfo.yCoordinate,
    direction:machineColInfo.direction
  }
  const flagRes = await MachineRoomApi.findAddAisleVerify(asileObject)

  if(flagRes) {
    message.error("减少失败！可能原因如下：该柜列的位置的长度范围内有机柜或柜列,柜列同名,柜列超出机房长度范围")
    return
  }

  const aisleRes = await MachineRoomApi.saveRoomAisle(asileObject) 
  if(aisleRes) {
    message.success('减少成功！')
  } else {
    message.error('减少失败！')
  }
  getMachineColInfoReal()
}
//
const switchBtn = (value) => {
  chosenBtn.value = value
  nextTick(()=>{updateCabinetConnect()})
  console.log('switchBtn', value, cabinetList.value,)
}
const switchBtnCabinet = (value) => {
  chosenBtnCabinet.value = value
  nextTick(()=>{updateCabinetConnect()})
  console.log('switchBtnCabinet', value, cabinetList.value,)
}

window.addEventListener('resize', function() {
  console.log('resize----')
  updateCabinetConnect()
})

// 计算出要缩放的比例
const handleCssScale = () => {
  scaleValue.value = 1
  // if (scaleValue.value != 1) scaleValue.value = 1/scaleValue.value
  isFromHome && nextTick(()=>{
    const targetMain = document.querySelector('.topologyContainer > .Container > .main') as Element
    const startBusWidth = machineColInfo.pduBar ? 132 : 0 // 两边始端箱宽度总和 
    const childWidth = targetMain.getBoundingClientRect().width + startBusWidth // Container元素的宽
    const childHeight = targetMain.getBoundingClientRect().height + 30 // Container元素的高 30是padding的长
    ContainerHeight.value = childHeight
    scaleValue.value = +(((containerInfo?.width - 30)/childWidth).toFixed(2))
    if (childHeight > 400) scaleValue.value = +(400/childHeight).toFixed(2)
    if (scaleValue.value > 1) scaleValue.value = 1
    console.log('containerInfo', childWidth, containerInfo?.width, scaleValue.value, childHeight, childHeight*scaleValue.value,machineColInfo)
  })
}

// 接口获取机房导航列表
const getNavList = async() => {
  const res = await MachineColumnApi.getAisleList({})
  console.log('接口获取机房导航列表*****', res)
  if (res && res.length) {
    roomList.value = res
    handleNavList(queryParams.cabinetroomId)
  }
}

const handleNavList = (cabinetroomId) => {
  let targetRoom = null as any
  if (!queryParams.cabinetroomId) {
    if(queryParams.roomDownValId == null || queryParams.roomDownValId  == ""){
        queryParams.cabinetroomId = roomList.value[0].roomId
    }else{
        queryParams.cabinetroomId = queryParams.roomDownValId 
    }
    targetRoom = roomList.value[0]
  } else {
    targetRoom = roomList.value.find(item => item.roomId == queryParams.cabinetroomId)
  }
  machineList.value = targetRoom.aisleList || []
  if (!queryParams.cabinetColumnId && machineList.value) {
    queryParams.cabinetColumnId = machineList.value[0].id
  }
  emit('idChange', queryParams.cabinetroomId)
  getMachineColInfo()
}

watch(() => queryParams.cabinetroomId, (val) => {
  if (val) {
    const targetRoom = roomList.value.find(item => item.roomId == val)
    console.log(targetRoom)
    machineList.value = targetRoom?.aisleList || []
    if (!machineList.value.find(item => item.id == queryParams.cabinetColumnId)) {
      queryParams.cabinetColumnId = machineList.value[0].id
    }
  }
})

watch(() => queryParams.cabinetColumnId,async (val) => {
  console.log('wwwwwwwwwww', val, machineList.value)
  emit('idChange', val)
  editEnable.value = false
  barChangeType.value = ''
  toCreatConnect(true)
  instance?.deleteEveryConnection()
  getMachineColInfo()
})

watch(() => containerInfo, (val) => {
  if (val) {
    queryParams.cabinetColumnId = containerInfo?.cabinetColumnId
    queryParams.cabinetroomId = containerInfo?.cabinetroomId
  }
},{immediate: true})

onMounted(() => {
  getNavList()
  initConnect()
  // getCabinetColorAll()
  flashListTimer.value = setInterval((getMachineColInfoReal), 5000);
  // intervalTimer = setInterval(() => {
  //   getDataDetail()
  // }, 10000)
  document.addEventListener('mousedown',(event) => {
    const element = event.target as HTMLElement
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item' && element.className != 'el-cascader-node__label') {
      operateMenu.value.show = false
    }
    if (event.button == 0 && operateMenuBox.value.show && element.className != 'menu_item' && element.className != 'el-cascader-node__label') {
      operateMenuBox.value.show = false
    }
  })
})

onBeforeUnmount(() => {
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})

onBeforeRouteLeave(()=>{
  if(flashListTimer.value){
    clearInterval(flashListTimer.value)
    flashListTimer.value = null;
  }
})
</script>

<style lang="scss" scoped>
.topologyContainer {
  transform-origin: center top;
  transform: scale(1, 1);
}
.CabEchart {
  width: 100%;
  height: 500px;
  margin-top: 50px;
}
.mask {
  width: 100%;
  height: 290px;
  position: absolute;
  top: 0;
  z-index: 99;
}
.btn-main {
  display: flex;
  align-items: center;
  height: 5vh;
}
.tip {
  display: flex;
  align-items: center;
  .color1 {
    width: 13px;
    height: 10px;
    border-radius: 3px;
    background-color: #95d475;
    margin-right: 5px;
  }
  .color2 {
    width: 13px;
    height: 10px;
    border-radius: 3px;
    background-color: #eebe77;
    margin-right: 5px;
  }
}
.btns {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  .statusRadius {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-left: 10px;
    margin-right: 5px;
  }
}
.topForm .line {
  display: inline-block;
  width: 8px;
  border-bottom: 1px solid #000;
  margin: 0px 8px 13px 8px;
}
:deep(.topForm .el-form-item) {
  margin-right: 0px
}
:deep(.el-card__body) {
  overflow-x: auto;
  overflow-y: hidden;
}
.topologyMain :deep(.el-card__body) {
  & > div {
    display: flex;
    justify-content: center;
  }
}
.custom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin-top:-50px;
}
.button-group {
  display: flex;
  gap: 10px;
}
.busContainer {
  display: flex;
  width: 100%;
}
.Container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  // overflow: hidden;
  // align-items: center;
  // padding-bottom: 20px;
  // min-height: calc(100vh - 270px);
  .Bus {
    position: relative;
    .startBus {
      position: relative;
      z-index: 100;
      font-size: 12px;
      color: #fcfcfce1;
      height: 76px;
      box-sizing: border-box;
      border: 1px solid #999;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      // box-shadow: 0 0 10px silver;
      margin-bottom: 50px;
    }
    .maskPoint1 {
      width: 10px;
      height: 6px;
      position: absolute;
      left: -5px;
      top: -6px;
      background-color: #fff;
    }
    .maskPoint2{
      width: 6px;
      height: 6px;
      position: absolute;
      left: -6px;
      top: 0;
      background-color: #fff;
    }
  }
  .main {
    // height: 100%;
    flex: 1;
    display: flex;
    flex-direction: column;
    .busListContainer {
      height: 80px;
      margin-bottom: 38px;
      position: relative;
      .bridge {
        height: 12px;
        background-color: rgb(155, 166, 190);
        margin-top: 27px;
      }
      .busList2 {
        box-sizing: border-box;
        position: absolute;
        width: 100%;
        height: 100%;
        left: -30px;
      }
      .busList1,.busList2 {
        padding: 0 19px;
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        .plugin-box {
          position: relative;
          height: fit-content;
          font-size: 12px;
          display: flex;
          flex-direction: column;
          border: 1px solid #979aa1;
          border-top: none;
          background-color: #fff;
          margin: 0 39px;
          z-index: 999;
          .line {
            width: 5px;
            height: 100px;
            background-color: #000;
          }
          .pointContainer {
            width: 100%;
            height: 5px;
            display: flex;
            justify-content: space-around;
            position: absolute;
            bottom: 0px;
            .point {
              border: 1px solid;
              width: 5px;
              height: 5px;
              border-radius: 50%;
              opacity: 0;
            }
          }
        }
        .connector {
          position: relative;
          height: 20px;
          width: 50px;
          border-radius: 2px;
          background-color: #E6E6E6;
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
          margin: 0 39px;
          .connector {
            margin: -16px auto 0;
          }
          .Tbox {
            width: 75px;
            position: absolute;
            top: 0;
            border: 1px solid #999;
            border-top: none;
            background-color: rgb(252, 252, 252);
            display: flex;
            flex-wrap: wrap;
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
}
.cabinetContainer {
  box-sizing: border-box;
  position: relative;
  // margin-top: 80px;
  .cabinetList {
    width: 100%;
    display: flex;
    justify-content: center;
    .cabinetBox {
      margin: 0 5px;
      .point {
        height: 5px;
        display: flex;
        justify-content: space-around;
        opacity: 0;
        .leftPoint {
          width: 5px;
          height: 5px;
          border: 1px solid;
          border-radius: 50%;
        }
        .rightPoint {
          width: 5px;
          height: 5px;
          border: 1px solid;
          border-radius: 50%;
        }
      }
      .cabinet {
        position: relative;
        width: 100px;
        height: 339px;
        box-sizing: border-box;
        border: 4px solid #888888;
      }
      .inner_empty {
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #979aa1;
        background-color: #f9f9f9;
      }
      .inner_fill {
        position: absolute;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        border: 5px solid #979aa1;
        z-index: 10;
      }
      .fill_box {
        position: absolute;
        z-index: 1;
        width: 100%;
        height: 100%;
        padding: 5px;
        box-sizing: border-box;
        background-color: #979aa1;
        color: white;
      }
      .status {
        font-size: 12px;
        line-height: 1;
        color: #000;
        text-align: center;
        margin-top: 10px;
      }
      .ti_xing {
        width:100%;
        border-bottom: 6px solid #4d4f53;
        margin: 0 5px;
        box-sizing: border-box;
        height: 0;
      }
    }
  }
  .operateBox {
    width: 20px;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    cursor: pointer;
    .operateIcon {
      width: 25px;
      height: 25px;
      display: flex;
      box-sizing: border-box;
      justify-content: center;
      align-items: center;
      font-size: 25px;
      border-radius: 50%;
      background-color: greenyellow;
      line-height: 1;
      padding-bottom: 3px;
    }
  }
}
.menu {
  box-sizing: border-box;
  position: absolute;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
  z-index: 999;
  .menu_item {
    width: 70px;
    height: 30px;
    padding: 0 10px;
    box-sizing: border-box;
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .menu_item:hover {
    background-color: rgb(231, 245, 255);
    color: rgb(82, 177, 255);
  }
}
.cabinetEchartsName {
  background-color:#484d55;
  position: absolute;
  bottom:3px;
  color:white;
  display:flex;
  width:90%;
  justify-content:center;
  font-size:10px;
}

:deep(.el-cascader-menu__wrap) {
  height: 250px;
}
</style>