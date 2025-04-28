<template>
<!-- <div style="height:calc(100vh - 120px);"> -->
  <el-card shadow="never">
    <div class="toolbar">
      <div style="display: flex;align-items:center" v-if="!isFromHome">
        机房：
        <el-select :size="isFromHome ? 'small' : ''" v-model="roomId" placeholder="请选择" class="!w-200px" @change="handleChangeRoom">
          <el-option v-for="item in roomList" :key="item.id" :label="item.roomName" :value="item.id" />
        </el-select>
      </div>
      <div class="status">
        <template v-for="item in statusInfo[chosenBtn]" :key="item.value">
          <div class="box" :style="{backgroundColor: item.color}"></div>{{item.name}}
        </template>
      </div>
      <div class="btns" :style="isFromHome ? 'flex: 1;display: flex;justify-content: flex-end;margin-right: 10px' : 'flex: 1;display: flex;justify-content: flex-end;margin-right: 10px'">
        <div style="display: flex;justify-content: flex-end;margin-right:3px;width: 100%;align-items: center;">
          <el-button v-if="isFromHome" size="small" @click="tableScaleValue = tableScaleValueStart;tableScaleWidth = tableScaleWidthStart;tableScaleHeight = tableScaleHeightStart" circle ><Icon icon="ep:refresh-right" /></el-button>
          <el-button v-else size="small" @click="tableScaleValue = 1;tableScaleWidth = 100;tableScaleHeight = 100" circle ><Icon icon="ep:refresh-right" /></el-button>
          <el-button size="small" @click="tableScale(false)" circle ><Icon icon="ep:minus" /></el-button>
          <el-button size="small" @click="tableScale(true)" circle ><Icon icon="ep:plus" /></el-button>
        </div>
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" round color="#00778c" :size="isFromHome ? 'small' : ''" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div style="display: flex;align-items: center">
        <!-- <el-button @click="handleAdd" type="primary">新建机房</el-button> -->
        <el-button v-if="!editEnable" @click="handleEdit" :size="isFromHome ? 'small' : ''" type="primary">编辑</el-button>
        <!-- <el-button v-if="editEnable" @click="handleStopDelete" plain type="danger">已删除</el-button> -->
        <el-button v-if="editEnable" @click="handleCancel" plain type="primary">取消</el-button>
        <el-button v-if="editEnable" @click="openSetting" plain type="primary"><Icon :size="16" icon="ep:setting" style="margin-right: 5px" />配置</el-button>
        <el-button v-if="editEnable" @click="handleSubmit" plain type="primary">保存</el-button>
        <!-- <el-button v-if="editEnable" @click="handleDelete" type="primary">删除机房</el-button> -->
        
      </div>
    </div>
  </el-card>
  <el-card shadow="never" :style="isFromHome ? 'flex: 1;' : ''">
    <div ref="dragTableViewEle" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseLeave" @selectstart="onSelectStart" :style="{cursor: `${dragCursor}`}">
        <div class="dragContainer" 
          ref="tableContainer"
          v-loading="loading" 
          style="z-index: 100"
          @click.right="handleRightClick"
          :style="isFromHome ? `transform-origin: 0 0;height: 49vh;width:${tableScaleWidth}%;` : `height:calc(100vh - 230px);width:${tableScaleWidth}%;`">
          <!-- <div class="mask" v-if="!editEnable" @click.prevent=""></div> -->
          <el-table ref="dragTable" class="dragTable" v-if="tableData.length > 0" :style="{width: '100%',height: `${tableScaleHeight}%`,transform: `translateZ(0) scale(${tableScaleValue})`, transformOrigin: '0 0',transition: 'none'}" :data="tableData" border :row-style="{background: 'revert'}" :span-method="arraySpanMethod" row-class-name="dragRow">
            <el-table-column fixed type="index" align="center" :resizable="false" />
            <template v-for="(formItem, index) in formParam" :key="index">
              <el-table-column :label="formItem" min-width="54" align="center" :resizable="false">
                <template #default="scope">
                  <draggable
                    :id="`${scope.$index}-${index}`"
                    class="dragTd"
                    :list="scope.row[formItem]"
                    :itemKey="item => item.id"
                    tag="div"
                    :group="scope.row[formItem].length > 0 ? groupMachineFill : groupMachineBlank"
                    animation="100"
                    @start="onStart"
                    @end.prevent="onEnd"
                  >
                    <template #item="{ element }">
                      <div v-if="element && element.type == 2" class="normalDrag" @dblclick="handleJump(element)">
                        <div v-if="chosenBtn == 0 && element.runStatus != 0 && element.runStatus != 4 && element.loadRate != 0" :style="{backgroundColor: element.cabinetName ? (element.loadRate>90 ? `rgba(212, 32, 35, ${element.loadRate/100})` : (element.loadRate>=60 ? `rgba(229, 184, 73, ${(element.loadRate+40)/100})` : `rgba(124, 255, 178, ${(element.loadRate+10)/100})`)) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.name">
                            <el-tooltip effect="light">
                              <template #content>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    机柜状态：{{statusColor[element.runStatus].name}} <br/>
                                    机柜名称：{{element.cabinetName}} <br/>
                                    机柜负荷：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                                    昨日用能：{{element.yesterdayEq ? element.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                  </div>
                                  <div style="width: 50%">
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路设备：{{element.cabinetkeya}}<br/>
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW
                                  </div>
                                  <div style="width: 50%">
                                    B路设备：{{element.cabinetkeyb}}<br/>
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div :style="!isFromHome ? 'font-size: 20px' : ''">{{element.loadRate ? element.loadRate.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">%</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 1 && element.runStatus == 1" :style="{backgroundColor: element.cabinetName ? (element.loadRate>90 ? `rgba(212, 32, 35, ${element.loadRate/100})` : (element.loadRate>=60 ? `rgba(229, 184, 73, ${(element.loadRate+40)/100})` : `rgba(124, 255, 178, ${(element.loadRate+10)/100})`)) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.name">
                            <el-tooltip effect="light">
                              <template #content>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    机柜状态：{{statusColor[element.runStatus].name}} <br/>
                                    机柜名称：{{element.cabinetName}} <br/>
                                    机柜负荷：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                                    昨日用能：{{element.yesterdayEq ? element.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                  </div>
                                  <div style="width: 50%">
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路设备：{{element.cabinetkeya}}<br/>
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW
                                  </div>
                                  <div style="width: 50%">
                                    B路设备：{{element.cabinetkeyb}}<br/>
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powApparent ? element.powApparent.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">kVA</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 2 && element.runStatus != 0 && element.runStatus != 4 && element.loadRate != 0" :style="{backgroundColor: element.cabinetName ? (element.powerFactor>=0.75 ? `rgba(124, 255, 178, ${element.powerFactor})` : (element.powerFactor>=0.5 ? `rgba(88, 217, 249, ${element.powerFactor+0.25})` : (element.powerFactor>=0.25 ? `rgba(253, 221, 96, ${element.powerFactor+0.5})` : `rgba(255, 110, 118, ${element.powerFactor+0.75})`))) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.name">
                            <el-tooltip effect="light">
                              <template #content>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    机柜状态：{{statusColor[element.runStatus].name}} <br/>
                                    机柜名称：{{element.cabinetName}} <br/>
                                    机柜负荷：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                                    昨日用能：{{element.yesterdayEq ? element.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                  </div>
                                  <div style="width: 50%">
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路设备：{{element.cabinetkeya}}<br/>
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW
                                  </div>
                                  <div style="width: 50%">
                                    B路设备：{{element.cabinetkeyb}}<br/>
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}</div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 3 && element.runStatus==1" :style="{backgroundColor: element.cabinetName ? (element.temFront>=tempList[2]?.min ? tempList[2]?.color : (element.temFront>=tempList[1]?.min ? tempList[1]?.color : (element.temFront>=tempList[0]?.min ? tempList[0]?.color : 'red'))) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.name">
                            <el-tooltip effect="light">
                              <template #content>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    机柜状态：{{statusColor[element.runStatus].name}} <br/>
                                    机柜名称：{{element.cabinetName}} <br/>
                                    机柜负荷：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                                    昨日用能：{{element.yesterdayEq ? element.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                  </div>
                                  <div style="width: 50%">
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路设备：{{element.cabinetkeya}}<br/>
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW
                                  </div>
                                  <div style="width: 50%">
                                    B路设备：{{element.cabinetkeyb}}<br/>
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temFront ? element.temFront.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 4 && element.runStatus==1" :style="{backgroundColor: element.cabinetName ? (element.temBlack>=tempList[2]?.hotMin ? tempList[2]?.hotColor : (element.temBlack>=tempList[1]?.hotMin ? tempList[1]?.hotColor : (element.temBlack>=tempList[0]?.hotMin ? tempList[0]?.hotColor : 'red'))) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.name">
                            <el-tooltip effect="light">
                              <template #content>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    机柜状态：{{statusColor[element.runStatus].name}} <br/>
                                    机柜名称：{{element.cabinetName}} <br/>
                                    机柜负荷：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                                    昨日用能：{{element.yesterdayEq ? element.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                  </div>
                                  <div style="width: 50%">
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路设备：{{element.cabinetkeya}}<br/>
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW
                                  </div>
                                  <div style="width: 50%">
                                    B路设备：{{element.cabinetkeyb}}<br/>
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temBlack ? element.temBlack.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else :style="{backgroundColor: element.cabinetName ? statusColor[element.runStatus].color : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.name">
                            <el-tooltip effect="light">
                              <template #content>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    机柜状态：{{statusColor[element.runStatus].name}} <br/>
                                    机柜名称：{{element.cabinetName}} <br/>
                                    机柜负荷：{{element.loadRate ? element.loadRate.toFixed(1) : '0.0'}}%<br/>
                                    昨日用能：{{element.yesterdayEq ? element.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                  </div>
                                  <div style="width: 50%">
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路设备：{{element.cabinetkeya}}<br/>
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW
                                  </div>
                                  <div style="width: 50%">
                                    B路设备：{{element.cabinetkeyb}}<br/>
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="chosenBtn == 0 && element.runStatus != 0 && element.runStatus != 4 && element.loadRate != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.loadRate ? element.loadRate.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">%</div></div>
                              <div v-if="chosenBtn == 1 && element.runStatus != 0 && element.runStatus != 4 && element.powApparent != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powApparent ? element.powApparent.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">kVA</div></div>
                              <div v-if="chosenBtn == 2 && element.runStatus != 0 && element.runStatus != 4 && element.powerFactor != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}</div>
                              <div v-if="chosenBtn == 3 && element.runStatus != 0 && element.runStatus != 4 && element.temFront != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temFront ? element.temFront.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                              <div v-if="chosenBtn == 4 && element.runStatus != 0 && element.runStatus != 4 && element.temBlack != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temBlack ? element.temBlack.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                              <div v-if="chosenBtn == 5 && element.runStatus != 0 && element.runStatus != 4 && element.yesterdayEq != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.yesterdayEq ? element.yesterdayEq.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">kWh</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                      </div>
                      <div v-else-if="element.type == 1" :class="element.direction == '1' ? 'dragChild' : 'dragChildCol'"  @dblclick="handleJump(element)">
                        <template v-if="element.cabinetList.length > 0">
                          <div :class="item.cabinetName ? 'dragSon fill' : 'dragSon'" v-for="(item, i) in element.cabinetList" :key="i" :data-index="i">
                            <div v-if="chosenBtn == 0 && item.runStatus != 0 && item.runStatus != 4 && item.loadRate != 0" :style="{backgroundColor: item.cabinetName ? (item.loadRate>90 ? `rgba(212, 32, 35, ${item.loadRate/100})` : (item.loadRate>=60 ? `rgba(229, 184, 73, ${(item.loadRate+40)/100})` : `rgba(124, 255, 178, ${(item.loadRate+10)/100})`)) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                              <template v-if="item.id > 0">
                                <el-tooltip effect="light">
                                  <template #content>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        机柜状态：{{statusColor[item.runStatus].name}} <br/>
                                        机柜名称：{{item.cabinetName}} <br/>
                                        机柜负荷：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                        昨日用能：{{item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                      </div>
                                      <div style="width: 50%">
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路设备：{{item.cabinetkeya}}<br/>
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW
                                      </div>
                                      <div style="width: 50%">
                                        B路设备：{{item.cabinetkeyb}}<br/>
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div :style="!isFromHome ? 'font-size: 20px' : ''">{{item.loadRate ? item.loadRate.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -5px">%</div><span v-else style="font-size: 10px;">%</span></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 1 && item.runStatus==1" :style="{backgroundColor: item.cabinetName ? (item.loadRate>90 ? `rgba(212, 32, 35, ${item.loadRate/100})` : (item.loadRate>=60 ? `rgba(229, 184, 73, ${(item.loadRate+40)/100})` : `rgba(124, 255, 178, ${(item.loadRate+10)/100})`)) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                              <template v-if="item.id > 0">
                                <el-tooltip effect="light">
                                  <template #content>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        机柜状态：{{statusColor[item.runStatus].name}} <br/>
                                        机柜名称：{{item.cabinetName}} <br/>
                                        机柜负荷：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                        昨日用能：{{item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                      </div>
                                      <div style="width: 50%">
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路设备：{{item.cabinetkeya}}<br/>
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW
                                      </div>
                                      <div style="width: 50%">
                                        B路设备：{{item.cabinetkeyb}}<br/>
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powApparent ? item.powApparent.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">kVA</div></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 2 && item.runStatus != 0 && item.runStatus != 4 && item.powerFactor != 0" :style="{backgroundColor: item.cabinetName ? (item.powerFactor>=0.75 ? `rgba(124, 255, 178, ${item.powerFactor})` : (item.powerFactor>=0.5 ? `rgba(88, 217, 249, ${item.powerFactor+0.25})` : (item.powerFactor>=0.25 ? `rgba(253, 221, 96, ${item.powerFactor+0.5})` : `rgba(255, 110, 118, ${item.powerFactor+0.75})`))) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                              <template v-if="item.id > 0">
                                <el-tooltip effect="light">
                                  <template #content>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        机柜状态：{{statusColor[item.runStatus].name}} <br/>
                                        机柜名称：{{item.cabinetName}} <br/>
                                        机柜负荷：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                        昨日用能：{{item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                      </div>
                                      <div style="width: 50%">
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路设备：{{item.cabinetkeya}}<br/>
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW
                                      </div>
                                      <div style="width: 50%">
                                        B路设备：{{item.cabinetkeyb}}<br/>
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}</div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 3 && item.runStatus==1" :style="{backgroundColor: item.cabinetName ? (item.temFront>=tempList[2]?.min ? tempList[2]?.color : (item.temFront>=tempList[1]?.min ? tempList[1]?.color : (item.temFront>=tempList[0]?.min ? tempList[0]?.color : 'red'))) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                              <template v-if="item.id > 0">
                                <el-tooltip effect="light">
                                  <template #content>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        机柜状态：{{statusColor[item.runStatus].name}} <br/>
                                        机柜名称：{{item.cabinetName}} <br/>
                                        机柜负荷：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                        昨日用能：{{item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                      </div>
                                      <div style="width: 50%">
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路设备：{{item.cabinetkeya}}<br/>
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW
                                      </div>
                                      <div style="width: 50%">
                                        B路设备：{{item.cabinetkeyb}}<br/>
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temFront ? item.temFront.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 4 && item.runStatus==1" :style="{backgroundColor: item.cabinetName ? (item.temBlack>=tempList[2]?.hotMin ? tempList[2]?.hotColor : (item.temBlack>=tempList[1]?.hotMin ? tempList[1]?.hotColor : (item.temBlack>=tempList[0]?.hotMin ? tempList[0]?.hotColor : 'red'))) : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                              <template v-if="item.id > 0">
                                <el-tooltip effect="light">
                                  <template #content>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        机柜状态：{{statusColor[item.runStatus].name}} <br/>
                                        机柜名称：{{item.cabinetName}} <br/>
                                        机柜负荷：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                        昨日用能：{{item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                      </div>
                                      <div style="width: 50%">
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路设备：{{item.cabinetkeya}}<br/>
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW
                                      </div>
                                      <div style="width: 50%">
                                        B路设备：{{item.cabinetkeyb}}<br/>
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temBlack ? item.temBlack.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else :style="{backgroundColor: item.cabinetName ? statusColor[item.runStatus].color : '#effaff',color: '#fff',height: '100%',width: '100%'}">
                              <template v-if="item.id > 0">
                                <el-tooltip effect="light">
                                  <template #content>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        机柜状态：{{statusColor[item.runStatus].name}} <br/>
                                        机柜名称：{{item.cabinetName}} <br/>
                                        机柜负荷：{{item.loadRate ? item.loadRate.toFixed(1) : '0.0'}}%<br/>
                                        昨日用能：{{item.yesterdayEq ? item.yesterdayEq.toFixed(1) : '0.0'}}kW·h
                                      </div>
                                      <div style="width: 50%">
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar<br/>
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路设备：{{item.cabinetkeya}}<br/>
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW
                                      </div>
                                      <div style="width: 50%">
                                        B路设备：{{item.cabinetkeyb}}<br/>
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="chosenBtn == 0 && item.runStatus != 0 && item.runStatus != 4 && item.loadRate != 0" :style="!isFromHome ? 'font-size: 20px;' : ''">{{item.loadRate ? item.loadRate.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -5px">%</div><span v-else style="font-size: 10px;">%</span></div>
                                  <div v-if="chosenBtn == 1 && item.runStatus != 0 && item.runStatus != 4 && item.powApparent != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powApparent ? item.powApparent.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -5px">kVA</div><span v-else style="font-size: 10px;">kVA</span></div>
                                  <div v-if="chosenBtn == 2 && item.runStatus != 0 && item.runStatus != 4 && item.powerFactor != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}</div>
                                  <div v-if="chosenBtn == 3 && item.runStatus != 0 && item.runStatus != 4 && item.temFront != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temFront ? item.temFront.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                                  <div v-if="chosenBtn == 4 && item.runStatus != 0 && item.runStatus != 4 && item.temBlack != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temBlack ? item.temBlack.toFixed(1) : '0.0'}}<br/><div style="font-size: 10px;margin-top: -5px">°C</div></div>
                                  <!-- <div v-if="chosenBtn == 5 && item.runStatus != 0 && item.runStatus != 4 && item.yesterdayEq != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.yesterdayEq ? item.yesterdayEq.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -5px">kWh</div></div> -->
                                </el-tooltip>
                              </template>
                            </div>
                          </div>
                        </template>
                        <template v-else>
                          <div class="dragSon" v-for="item in element.amount" :key="item">{{item}}</div>
                        </template>
                      </div>
                    </template>
                  </draggable>
                </template>
              </el-table-column>
            </template>
          </el-table>
          
        <!--  <el-empty v-if="loading == false && tableData.length == 0" style="height: calc(100vh - 220px)" description="机房暂未配置，请先编辑配置" /> -->
          <div class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
            <!-- <div class="menu_item" v-if="!editEnable" @click="dragTableView">拖拽</div> -->
            <div class="menu_item" v-if="showMenuAdd && editEnable" @click="addAisle">新增柜列</div>
            <div class="menu_item" v-if="showMenuAdd && editEnable" @click="addCabinet('add')">新增机柜</div>
            <!-- <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="editMachine">编辑</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="handleJump(false)">查看</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="deleteMachine">删除</div> -->
            <el-cascader-panel class="menu_item_panel" v-if="(!showMenuAdd || !editEnable) && menuOptions.length" style="width: fit-content" :options="menuOptions" :props="{expandTrigger: 'hover'}" @change="handleMenu" />
          </div>
        </div>
    </div>
  </el-card>
  <layoutForm ref="machineForm" @success="handleChange" />
  <el-dialog v-model="dialogVisible" title="机房配置" width="30%" :before-close="handleDialogCancel">
      <el-form>
        <div style="margin-bottom: 5px">
          <el-text>机房</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label="名称" label-width="90">
            <el-input v-model="rowColInfo.roomName" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="楼层" prop="type" label-width="90">
            <el-select v-model="rowColInfo.addr" placeholder="请选择">
              <el-option v-for="(addr_item,addr_index) in addrList" :key="addr_index" :label="addr_item" :value="addr_item" />
            </el-select>
          </el-form-item>
        </div>
        <div style="margin-bottom: 10px">
          <el-text>地砖（地砖按60CM*60CM）</el-text>
        </div>
        <div class="double-formitem">
          <el-form-item label="行数" label-width="90">
            <el-input-number v-model="rowColInfo.row" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="列数" label-width="90">
            <el-input-number v-model="rowColInfo.col" :min="1" :max="70" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        
        <div style="margin-bottom: 5px">
          <el-text>容量</el-text>
        </div>
        <el-form-item label="机房总电力容量" label-width="160">
          <el-input v-model="rowColInfo.powerCapacity" placeholder="请输入">
            <template #append>kVA</template>
          </el-input>
        </el-form-item>
        <!-- <el-form-item label="非IT设备总额定功率" label-width="160">
          <el-input v-model="rowColInfo.airPower" placeholder="包括制冷系统（如空调、冷源设备、新风系统等）">
            <template #append>kVA</template>
          </el-input>
        </el-form-item> -->

        <div class="double-formitem">
          <el-form-item label="显示选择" label-width="90" style="padding-top: 15px">
            <el-switch v-model="rowColInfo.displayFlag" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-radio-group v-model="radio" size="large" style="margin-left: 15px;">
            <el-radio-button label="负载率" value="负载率"/>
            <!-- <el-radio-button label="PUE" value="PUE"/> -->
          </el-radio-group>
        </div>

        <!-- <div class="double-formitem">
          <el-form-item label="日用能告警" label-width="90">
            <el-switch v-model="rowColInfo.eleAlarmDay" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="日用能限制" label-width="90">
            <el-input-number v-model="rowColInfo.eleLimitDay" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        <div class="double-formitem">
          <el-form-item label="月用能告警" label-width="90">
            <el-switch v-model="rowColInfo.eleAlarmMonth" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="月用能限制" label-width="90">
            <el-input-number v-model="rowColInfo.eleLimitMonth" :min="0" :max="9999" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div> -->
      </el-form>
      <template #footer>
        <el-button @click="handleDialogCancel">取 消</el-button>
        <el-button type="primary" @click="submitSetting">确 定</el-button>
      </template>
    </el-dialog>

  
  <el-dialog v-model="dialogStopDelete" title="恢复机房"  :before-close="handleDialogStopDelete">
       <el-table v-loading="loading" :data="deletedList" :stripe="true" :show-overflow-tooltip="true"  :border=true>
         <el-table-column label="编号" min-width="110" align="center">
            <template #default="scope">
               <div>{{scope.row.id}}</div>
            </template>
        </el-table-column>
        <el-table-column label="机房名称" min-width="110" align="center">
            <template #default="scope">
               <div>{{scope.row.roomName}}</div>
            </template>
        </el-table-column>
        
       <el-table-column label="删除日期" min-width="110" align="center">
           <template #default="scope">
               {{ (new Date(scope.row.updateTime)).toISOString().slice(0, 10) }}
            </template>
        </el-table-column>
        
        
       <el-table-column label="操作" align="center">
          <template #default="scope">
            <el-button
              link
              type="danger"
              @click="handleRestore(scope.row.id)"
            >
              恢复机房
            </el-button>
          </template>
        </el-table-column>
     </el-table>
     <Pagination
        :total="queryParams.pageTotal"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="handleStopDelete"
      />
      <div style="height:30px"></div>
  </el-dialog>
  <el-dialog v-model="detailVis" style="width: 80%;height: 80%;margin-top: 100px;">
    <div class="custom-row" style="display: flex; align-items: center;">
      <!-- 位置标签 -->
      <div class="location-tag el-col">
        <span style="margin-right:10px;font-size:18px;font-weight:bold;">功率因素详情</span>
        <span>所在位置：{{ rowColInfo.roomName?rowColInfo.roomName:'未绑定' }}</span>
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
      <el-table-column label="总功率因素" align="center" prop="factor_total_avg_value" />
      <el-table-column label="A路功率因素" align="center" prop="factor_a_avg_value" />
      <el-table-column label="B路功率因素" align="center" prop="factor_b_avg_value" />
    </el-table>
    </div>
  </el-dialog>
  <MachineForm ref="machineFormCabinet" @success="saveMachine" :roomList="roomList" :roomId="roomId" />
<!-- </div> -->
</template>

<script lang="ts" setup>
import draggable from "vuedraggable";
import layoutForm from './component/layoutForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MachineRoomApi } from '@/api/cabinet/room'
import { CabinetApi } from '@/api/cabinet/info'
import { IndexApi } from '@/api/cabinet/index'
import { Console } from "console";
import MachineForm from './component/MachineForm.vue';
import download from '@/utils/download'
import PFDetail from './component/PFDetail.vue'

const { push } = useRouter() // 路由跳转
const message = useMessage() // 消息弹窗

const route = useRoute();
const query = route.query;

const queryParams = reactive({
  company: undefined,
  showCol: [1, 2, 12, 13, 15, 16] as number[],
  pageNo: 1,
  pageSize: 24,
  pageTotal: 0,
})

const {containerInfo, isFromHome} = defineProps({
  containerInfo: {
    type: Object,
  },
  isFromHome: {
    type: Boolean,
    default: false,
  }
})
let timer = null as any // 定时器
const switchChartOrTable = ref(0)
const isAddRoom = ref(false) // 是否为添加机房模式 
const roomId = ref(0) // 房间id
const roomList = ref<any[]>([]) // 左侧导航栏树结构列表
const dragTable = ref() // 可移动编辑表格
const dragTableViewEle = ref()
const tableContainer = ref()
const scaleValue = ref(1) // 缩放比例

const tableScaleValue = ref(1)
const tableScaleWidth = ref(100)
const tableScaleHeight = ref(100)
const tableScaleValueStart = ref(1)
const tableScaleWidthStart = ref(100)
const tableScaleHeightStart = ref(100)

const deletedList = ref<any>([]) //已删除的
const chosenBtn = ref(1)
const ContainerHeight = ref(100)
const loading = ref(false)
const movingInfo = ref<any>({})
const roomFlag =ref();
const aisleFlag = ref();
const roomDownValId = ref();
const updateCfgInfo = ref();
const roomsId = reactive({
  roomDownValIds: Number(query.id),
})
const tempList:any = ref([{},{},{}])

const addrList = ref([
  '未区分',
  '一楼',
  '二楼',
  '三楼',
  '四楼',
  '五楼',
  '六楼',
  '七楼',
  '八楼',
  '九楼',
  '十楼',
]) // 楼层

const radio = ref("负载率")
const rowColInfo = reactive({
  roomName: '', // 机房名
  addr: '未区分', //楼层
  row: 14, // 行
  col: 18, // 列
  powerCapacity:0, //电力容量
  airPower: null, //空调额定功率
  displayType: 0, //0负载率 1PUE
  displayFlag: 0, // 显示选择
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
})
const emit = defineEmits(['backData', 'getroomid']) // 定义 backData 事件，用于操作成功后的回调
const tableData = ref<Record<string, any[]>[]>([]);
const alarmTypeDesc = ref(['','PDU离线','PDU告警','PDU预警','母线告警','母线离线','机柜容量'])
const statusInfo = ref([[
  {
    name: '0%<负载率<60%',
    color: '#7cffb2',
    value: 0,
  },
  {
    name: '60%<=负载率<=90%',
    color: '#e5b849',
    value: 1,
  },
  {
    name: '90%<负载率',
    color: '#d42023',
    value: 2,
  }
],[
  {
    name: '空载',
    color: '#effaff',
    value: 5,
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
    name: '未绑定',
    color: '#3e62c7',
    value: 0,
  },
  {
    name: '离线',
    color: '#757c8a',
    value: 4,
  },
],[
  {
    name: '功数<0.25',
    color: '#ff6e76',
    value: 0,
  },
  {
    name: '0.25<=功数<0.50',
    color: '#fddd60',
    value: 1,
  },
  {
    name: '0.50<=功数<0.75',
    color: '#58d9f9',
    value: 2,
  },
  {
    name: '0.75<=功数',
    color: '#7cffb2',
    value: 3,
  }
]])
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
    name: '空载',
    color: '#effaff',
    value: 5,
  },
])
const btns = [
  {
    value: 1,
    name: '视在功率',
  },
  {
    value: 0,
    name: '机柜负荷',
  },
  {
    value: 2,
    name: '功率因素',
  },
  // {
  //   value: 5,
  //   name: '昨日用能',
  // },
  {
    value: 3,
    name: '前门温度',
  },
  {
    value: 4,
    name: '后门温度',
  },
  // {
  //   value: 4,
  //   name: '容量',
  // },
]

const flashListTimer = ref();

const emptyObject = {
    "id": 0,
    "cabinetName": null,
    "roomId": 0,
    "roomName": null,
    "aisleId": 0,
    "powCapacity": null,
    "cabinetType": null,
    "pduBox": null,
    "isDisabled": null,
    "isDeleted": null,
    "cabinetHeight": 0,
    "cabinetUseHeight": 0,
    "type": null,
    "company": null,
    "pduIpA": null,
    "casIdA": 0,
    "pduIpB": null,
    "casIdB": 0,
    "runStatus": 0,
    "sensorList": null,
    "rackIndexList": null,
    "outletA": null,
    "outletB": null,
    "usedSpace": 0,
    "rackNum": 0,
    "freeSpace": 0,
    "busIpA": null,
    "busNameA": null,
    "boxNameA": null,
    "barIdA": null,
    "addrA": null,
    "barIdB": null,
    "addrB": null,
    "boxOutletIdA": null,
    "busIpB": null,
    "busNameB": null,
    "boxNameB": null,
    "boxOutletIdB": null,
    "index": 4,
    "yesterdayEq": null,
    "boxIndexA": null,
    "boxIndexB": null,
    "eleAlarmDay": null,
    "eleAlarmMonth": null,
    "eleLimitDay": null,
    "eleLimitMonth": null,
    "loadRate": 0,
    "lineCurA": null,
    "lineCurB": null,
    "lineVolA": null,
    "lineVolB": null,
    "powerFactor": 0,
    "powerFactorA": 0,
    "powerFactorB": 0,
    "powActive": 0,
    "powActiveA": 0,
    "powActiveB": 0,
    "powReactive": 0,
    "powReactiveA": 0,
    "powReactiveB": 0,
    "powApparent": 0,
    "powApparentA": 0,
    "powApparentB": 0,
    "temData": null,
    "temDataHot": null,
    "cabinetBoxes": null,
    "cabinetPdus": null,
    "rackIndices": null,
    "xCoordinate": 0,
    "yCoordinate": 0
}

const dialogVisible = ref(false);
const dialogStopDelete = ref(false);
const editEnable = ref(false);
const dragCursor = ref();
const machineForm = ref();
const machineFormCabinet = ref();
const startX = ref(0);
const startY = ref(0);
const scrollLeft = ref(0);
const scrollTop = ref(0);

const groupMachineFill = {
  name: 'MachineFill',
  pull: () => {
    return editEnable.value
  }, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: false // 拖入
}
const groupMachineBlank = {
  name: 'MachineBlank',
  pull: false, //允许拖出,如果设置 字符串'clone' 表示该组拖出的元素会被克隆
  put: (event) => {
   // //console.log('event', event.el.id)
    const moveBox = movingInfo.value
    //console.log(moveBox)
    if (editEnable.value) {
      if(moveBox.type == 2) {
        moveBox.amount = 1
      }
      if (moveBox.direction == 2) {
        const X = +event.el.id.split('-')[1]
        const Y = +event.el.id.split('-')[0]
        if (Y + moveBox.amount > rowColInfo.row) return false
        for(let i = 0;i < moveBox.amount;i++) {
          if((tableData.value[Y+i][formParam.value[X]].length && tableData.value[Y+i][formParam.value[X]][0].id != moveBox.id) || (tableData.value[Y+i][formParam.value[X+1]].length && tableData.value[Y+i][formParam.value[X+1]][0].id != moveBox.id)) {
            return false
          }
        }
      } else {
        const X = +event.el.id.split('-')[1]
        const Y = +event.el.id.split('-')[0]
        if (X + moveBox.amount > rowColInfo.col) return false
        for(let i = 0;i < moveBox.amount;i++) {
          if((tableData.value[Y][formParam.value[X+i]].length && tableData.value[Y][formParam.value[X+i]][0].id != moveBox.id) || (tableData.value[Y+1][formParam.value[X+i]].length && tableData.value[Y+1][formParam.value[X+i]][0].id != moveBox.id)) {
            return false
          }
        }
      }
    }
    return editEnable.value
  }, // 拖入
}

const operateMenu = ref({
  left: '0px',
  top: '0px',
  show: false,
  lndexX: 0,
  lndexY: 0,
  maxlndexX: 0,
  maxlndexY: 0,
  xLength: 0,
  yLength: 0
})

// 接口获取机房导航列表
const getRoomList = async() => {
  const res = await MachineRoomApi.getRoomList({})
  ////console.log('接口获取机房导航列表*****', res, roomId.value)
  if (res && res.length) {
    roomList.value = res
    const find = res.find(item => item.id == roomId.value)
    if (!find) {
      if(roomsId.roomDownValIds == null){
          roomId.value = res[0].id
      }else{
          roomId.value = roomsId.roomDownValIds;
      }
    }
    emit('getroomid', roomId.value);
    getRoomInfo();
  }
  //console.log(typeof(roomId.value))
}

const menuOptions = ref([
  {
    value: '拖拽视图',
    label: '拖拽视图',
    children: [{}]
  }
])

const menuOptionsCopy = ref([
  {
    value: '编辑',
    label: '编辑',
    children: [
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
      },
      {
        value: '柜列移动',
        label: '柜列移动',
      },
      {
        value: '柜列编辑',
        label: '柜列编辑',
      },
      {
        value: '柜列删除',
        label: '柜列删除'
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
  }
])

const getRoomInfo = async() => {
  resetForm();
  loading.value = true;
  try {
    const result = await MachineRoomApi.getRoomDataNewDetail({id: roomId.value});
    const res = result;
    if(!res) {
      return
    }
    // const result1 = MachineRoomApi.getRoomDetail({id: roomId.value});
    // const result2 = MachineRoomApi.getRoomDataDetail({id: roomId.value})
    // const results = await Promise.all([result1, result2])
    // const res = results[0];
    //console.log("res",res)
    res.aisleList = res.aisleList==null ? [] : res.aisleList
    res.cabinetList = res.cabinetList==null ? [] : res.cabinetList
    updateCfgInfo.value=res;
    roomDownValId.value = res.id;
    const data: Record<string, any[]>[] = [];
    Object.assign(rowColInfo, {
      roomName: res.roomName,
      row: res.y_length,
      col: res.x_length,
      powerCapacity:res.powerCapacity,
      airPower:res.airPower,
      addr: res.addr,
      displayType: res.displayType, //0负载率 1PUE
      displayFlag: res.displayFlag, // 显示选择
      eleAlarmDay: res.eleAlarmDay,
      eleLimitDay: res.eleLimitDay,
      eleAlarmMonth: res.eleAlarmMonth,
      eleLimitMonth: res.eleLimitMonth,
    })
    emit('backData', res)
    
    for (let row = 0; row < res.y_length; row++) {
      const rowData: Record<string, any[]> = {};
      for (let col = 0; col < res.x_length; col++) {
        const colKey = getTableColCharCode(col);
        rowData[colKey] = [];
      }
      data.push(rowData);
    }
    res.aisleList.forEach(item => {
      if(item.cabinetList == null) {
        item.cabinetList = []
      }
      const arr = [] as any
      let canDelete = true
      for(let j=0;j < item.length;j++) {
        arr.push(emptyObject)
      }
      item.cabinetList && item.cabinetList.forEach(ele => {
        if(ele.index > 0) {
          arr.splice(ele.index - 1, 1, ele)
          canDelete = false
        }
      })
      item.cabinetList = arr
      for(let i=0; i < item.length; i++) {
        const dataItem =  {
          id: item.id,
          name: item.aisleName,
          direction: item.direction == 'x' ? 1 : 2,
          barA: item.barA,
          barB: item.barB,
          type: 1,
          amount: item.cabinetList.length,
          cabinetList: item.cabinetList,
          first: false,
          canDelete,
          originAmount: item.cabinetList.length,
          originDirection: item.direction == 'x' ? 1 : 2,
          powerCapacity:item.powerCapacity,
          eleAlarmDay: item.eleAlarmDay,
          eleLimitDay: item.eleLimitDay,
          eleAlarmMonth: item.eleAlarmMonth,
          eleLimitMonth: item.eleLimitMonth,
        }
        if (i == 0) dataItem.first = true
        if (dataItem.direction == 1) {
          //console.log('----dataItem1', dataItem )
          data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, dataItem)
          data[item.yCoordinate][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, {...dataItem,first: false})
        } else {
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, dataItem)
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate)].splice(0, 1, {...dataItem,first: false})
        }
      }
    })
    res.cabinetList.forEach(item => {
      if (item.xCoordinate > 0 && item.yCoordinate > 0) {
        data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, {...item, name: item.cabinetName, type: 2,first: true})
        data[item.yCoordinate][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, {...item, name: item.cabinetName, type: 2,first: false})
      }
    })
    tableData.value = data;
    //console.log("tableData.value",tableData.value)
    getRoomStatus(result)
    if(isFromHome)
    updateScale()
    // handleCssScale()
  } finally {
    loading.value = false
    // updateScale()
  }
}

const getRoomInfoNoLoading = async() => {
  try {
    const result = await MachineRoomApi.getRoomDataNewDetail({id: roomId.value});
    const res = result;
    // const result1 = MachineRoomApi.getRoomDetail({id: roomId.value});
    // const result2 = MachineRoomApi.getRoomDataDetail({id: roomId.value})
    // const results = await Promise.all([result1, result2])
    // const res = results[0];
    //console.log("res",res)
    if(!res) {
      return
    }
    res.aisleList = res.aisleList==null ? [] : res.aisleList
    res.cabinetList = res.cabinetList==null ? [] : res.cabinetList
    updateCfgInfo.value=res;
    roomDownValId.value = res.id;
    const data: Record<string, any[]>[] = [];
    Object.assign(rowColInfo, {
      roomName: res.roomName,
      row: res.y_length,
      col: res.x_length,
      powerCapacity:res.powerCapacity,
      airPower:res.airPower,
      addr: res.addr,
      displayType: res.displayType, //0负载率 1PUE
      displayFlag: res.displayFlag, // 显示选择
      eleAlarmDay: res.eleAlarmDay,
      eleLimitDay: res.eleLimitDay,
      eleAlarmMonth: res.eleAlarmMonth,
      eleLimitMonth: res.eleLimitMonth,
    })
    emit('backData', res)
    
    for (let row = 0; row < res.y_length; row++) {
      const rowData: Record<string, any[]> = {};
      for (let col = 0; col < res.x_length; col++) {
        const colKey = getTableColCharCode(col);
        rowData[colKey] = [];
      }
      data.push(rowData);
    }
    res.aisleList.forEach(item => {
      if(item.cabinetList == null) {
        item.cabinetList = []
      }
      const arr = [] as any
      let canDelete = true
      for(let j=0;j < item.length;j++) {
        arr.push(emptyObject)
      }
      item.cabinetList && item.cabinetList.forEach(ele => {
        if(ele.index > 0) {
          arr.splice(ele.index - 1, 1, ele)
          canDelete = false
        }
      })
      item.cabinetList = arr
      for(let i=0; i < item.length; i++) {
        const dataItem =  {
          id: item.id,
          name: item.aisleName,
          direction: item.direction == 'x' ? 1 : 2,
          barA: item.barA,
          barB: item.barB,
          type: 1,
          amount: item.cabinetList.length,
          cabinetList: item.cabinetList,
          first: false,
          canDelete,
          originAmount: item.cabinetList.length,
          originDirection: item.direction == 'x' ? 1 : 2,
          powerCapacity:item.powerCapacity,
          eleAlarmDay: item.eleAlarmDay,
          eleLimitDay: item.eleLimitDay,
          eleAlarmMonth: item.eleAlarmMonth,
          eleLimitMonth: item.eleLimitMonth,
        }
        if (i == 0) dataItem.first = true
        if (dataItem.direction == 1) {
          //console.log('----dataItem1', dataItem )
          data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, dataItem)
          data[item.yCoordinate][getTableColCharCode(item.xCoordinate - 1 + i)].splice(0, 1, {...dataItem,first: false})
        } else {
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, dataItem)
          data[item.yCoordinate - 1 + i][getTableColCharCode(item.xCoordinate)].splice(0, 1, {...dataItem,first: false})
        }
      }
    })
    res.cabinetList.forEach(item => {
      if (item.xCoordinate > 0 && item.yCoordinate > 0) {
        data[item.yCoordinate - 1][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, {...item, name: item.cabinetName, type: 2,first: true})
        data[item.yCoordinate][getTableColCharCode(item.xCoordinate - 1)].splice(0, 1, {...item, name: item.cabinetName, type: 2,first: false})
      }
    })
    tableData.value = data;
    //console.log("tableData.value",tableData.value)
    getRoomStatus(result)
    // handleCssScale()
  } finally {
  }
}

const getRoomStatus = async(res) => {
  if (!res) {
    res = await MachineRoomApi.getRoomDataNewDetail({id: roomId.value})
    emit('backData', res)
  }
 // //console.log('//////////', tableData.value)
}

let lastTime = 0;
const throttleDelay = 100;
const tableScale = (flag) => {
  // if(tableScaleValue.value == 0.4 && !flag) {
  //   return
  // }
  const now = Date.now();

  // 如果距离上次执行的时间小于 throttleDelay，则跳过
  if (now - lastTime < throttleDelay) {
    return
  } 
  //console.log(now)
  lastTime = now;
  if(flag) {
    tableScaleValue.value += 0.2
  } else {
    tableScaleValue.value -= 0.2
  }
  tableScaleValue.value = Math.max(0.1, tableScaleValue.value);
  tableScaleWidth.value = 1/tableScaleValue.value*100
  tableScaleHeight.value = 1/tableScaleValue.value*100
}

const updateScale = () => {
  console.log(dragTableViewEle.value,dragTable.value)
  if (!dragTableViewEle.value) return;

  const containerWidth = dragTableViewEle.value.clientWidth;
  const containerHeight = dragTableViewEle.value.clientHeight;

  // 计算缩放比例（取宽高中较小的比例，确保完整显示）
  tableScaleValueStart.value = Math.min(Math.floor(containerHeight/(rowColInfo.row*2.4))/10,Math.floor(containerWidth/(rowColInfo.col*5.4))/10)
  tableScaleWidthStart.value = 1/tableScaleValueStart.value*100
  tableScaleHeightStart.value = 1/tableScaleValueStart.value*100
  
  tableScaleValue.value = tableScaleValueStart.value
  tableScaleWidth.value = tableScaleWidthStart.value
  tableScaleHeight.value = tableScaleHeightStart.value

  console.log(tableScaleValueStart.value,tableScaleWidthStart.value,tableScaleHeightStart.value,containerWidth,containerHeight,rowColInfo)
};

// 计算出要缩放的比例
const handleCssScale = () => {
  isFromHome && nextTick(() => {
    const timer = setTimeout(() => {
      const targetBody = dragTable.value.$el.querySelector('.el-table__body ') as HTMLElement
      // const targetContainer = document.querySelector('.dragContainer ') as HTMLElement
      const tableWidth = +targetBody.getBoundingClientRect().width.toFixed() + 30 // Container元素的宽
      const tableHeight = +targetBody.getBoundingClientRect().height.toFixed() // Container元素的高
      scaleValue.value = +((containerInfo?.width/tableWidth).toFixed(2))
      ContainerHeight.value = scaleValue.value * tableHeight
      clearTimeout(timer)
    //  //console.log('containerInfotargetBody',scaleValue.value, containerInfo?.width, targetBody, targetBody.getBoundingClientRect(), tableWidth, tableHeight)
    }, 10)
  })
}

// 处理修改机房的事件
const handleChangeRoom = (val) => {
  roomId.value = val
  tableScaleValue.value = 1
  tableScaleWidth.value = 100
  tableScaleHeight.value = 100
  getRoomInfo()
}
//取消
const handleCancel = () => {
  editEnable.value = false;
  getRoomInfo();
}
//已删除
const handleStopDelete = async() =>{
  dialogStopDelete.value =true;
  const res = await MachineRoomApi.deletedRoomInfo({
    pageNo: queryParams.pageNo,
    pageSize: queryParams.pageSize,
  })
  deletedList.value = res.list;
  queryParams.pageTotal = res.total;
}

//恢复机房
const handleRestore = async (flagRoomid) => {
  ElMessageBox.confirm('确认恢复机房吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await MachineRoomApi.restoreRoomInfo({id: flagRoomid});
    if(res != null || res != "")
    dialogStopDelete.value =false;
    message.success('恢复成功')
    getRoomList()
  })
}



const handleDialogStopDelete =() =>{
   dialogStopDelete.value =false;
}


// 处理弹窗取消事件
const handleDialogCancel = () => {
  dialogVisible.value = false;
  isAddRoom.value = false;
}
// 处理点击添加机房事件
const handleAdd = () => {
  roomFlag.value = 1;
  dialogVisible.value = true;
  resetForm();
}
// 重置表单
const resetForm = () => {
  Object.assign(rowColInfo, {
    roomName: '', // 机房名
    addr: '未区分', //楼层
    row: 14, // 行
    col: 18, // 列,
    powerCapacity:0,
    airPower:null,
    displayType: 0, //0负载率 1PUE
    displayFlag: 0, // 显示选择
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
  })
  radio.value = "负载率"
}
// 处理点击删除机房事件
const handleDelete = () => {
  ElMessageBox.confirm('确认删除机房吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
    const res = await MachineRoomApi.deleteRoom({id: roomId.value})
    //console.log('handleDelete', res)
    editEnable.value = false
    message.success('删除成功')
    getRoomList()
  })
}
// 处理点击编辑事件
const handleEdit = () => {
  if (isFromHome) {
    push({path: '/room/roommonitor/topology', query: { id:roomDownValId.value}})
    return
  }
  editEnable.value = true
}

const openSetting = () => {
  roomFlag.value = 2;
  Object.assign(rowColInfo, {
    roomName: updateCfgInfo.value.roomName,
    row: updateCfgInfo.value.y_length,
    col: updateCfgInfo.value.x_length,
    powerCapacity:updateCfgInfo.value.powerCapacity,
    addr: updateCfgInfo.value.addr,
    airPower:updateCfgInfo.value.airPower ? updateCfgInfo.value.airPower : 0,
    displayType: updateCfgInfo.value.displayType ? 1 : 0, //0负载率 1PUE
    displayFlag: updateCfgInfo.value.displayFlag ? 1 : 0,
    eleAlarmDay: updateCfgInfo.value.eleAlarmDay,
    eleLimitDay: updateCfgInfo.value.eleLimitDay,
    eleAlarmMonth: updateCfgInfo.value.eleAlarmMonth,
    eleLimitMonth: updateCfgInfo.value.eleLimitMonth,
  })
  radio.value = updateCfgInfo.value.displayType ? "PUE" : "负载率"
  dialogVisible.value = true;
}

const switchBtn = (value) => {
  chosenBtn.value = value
}

const arraySpanMethod = ({
  row,
  columnIndex,
}) => {
  if (columnIndex > 0) {
    const num = 1
    const td = row[getTableColCharCode(columnIndex - num)]
    const tdData = td[0]
    if (tdData && tdData.type && tdData.type == 1) { // 如果是柜列
      if (tdData.first) { // 如果是柜列中开头第一个  合并单元格
        if (tdData.direction == 1) { // 横向
          return [2, tdData.amount]
        } else { // 纵向
          return [tdData.amount, 2]
        }
      } else { // 如果不是柜列中开头第一个 该单元格不显示
        return [0, 0]
      }
    } 
    if (tdData && tdData.type && tdData.type == 2) { // 如果是机柜
      if (tdData.first) { // 如果是柜列中开头第一个  合并单元格
        return [2, 1]
      } else { // 如果不是柜列中开头第一个 该单元格不显示
        return [0, 0]
      }
    } 
  }
  return [1, 1]
}

// 右击弹出菜单
const handleRightClick = (e) => {
  e.preventDefault()
  const container = e.currentTarget;
  const rect = container.getBoundingClientRect()
  const offsetX = e.clientX - Math.ceil(rect.left) + 1
  const offsetY = e.clientY - Math.ceil(rect.top) + 1
  const currentId = e.target.id ? e.target.id : (e.target.parentNode.id ? e.target.parentNode.id :  ((e.target.parentNode.parentNode.id ? e.target.parentNode.parentNode.id :  (e.target.parentNode.parentNode.parentNode.id ? e.target.parentNode.parentNode.parentNode.id :  (e.target.parentNode.parentNode.parentNode.parentNode.id ? e.target.parentNode.parentNode.parentNode.parentNode.id :  e.target.parentNode.parentNode.parentNode.parentNode.parentNode.id)))))
  console.log('handleRightClick', e.target,e.target.classList,e.target.parentNode.parentNode.parentNode.parentNode.parentNode,e.target.dataset.index, currentId, offsetX, offsetY)
  const cabinetIndex = e.target.dataset.index ? e.target.dataset.index : (e.target.parentNode.dataset.index ? e.target.parentNode.dataset.index :  ((e.target.parentNode.parentNode.dataset.index ? e.target.parentNode.parentNode.dataset.index :  (e.target.parentNode.parentNode.parentNode.dataset.index ? e.target.parentNode.parentNode.parentNode.dataset.index :  (e.target.parentNode.parentNode.parentNode.parentNode.dataset.index ? e.target.parentNode.parentNode.parentNode.parentNode.dataset.index :  e.target.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.dataset.index)))))
  const lndexX = currentId.split('-')[1]
  const lndexY = currentId.split('-')[0]
  if (!currentId) return
  console.log(tableData.value,tableData.value[lndexY][formParam.value[lndexX]],lndexY,formParam.value[lndexX])
  
  menuOptions.value[0] = JSON.parse(JSON.stringify(menuOptionsCopy.value[0]));

  if(cabinetIndex >= 0) {
    menuOptions.value[0].value = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex]

    menuOptions.value[1] = menuOptionsCopy.value[1]
    menuOptions.value[1].value = tableData.value[lndexY][formParam.value[lndexX]][0].id
    menuOptions.value[1].label = "柜列：" + tableData.value[lndexY][formParam.value[lndexX]][0].name

    menuOptions.value[2] = menuOptionsCopy.value[2]
    menuOptions.value[2].value = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex]
    menuOptions.value[2].label = "机柜：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetName

    menuOptions.value[3] = menuOptionsCopy.value[3]
    menuOptions.value[3].value = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya
    menuOptions.value[3].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya

    menuOptions.value[4] = menuOptionsCopy.value[4]
    menuOptions.value[4].value = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb
    menuOptions.value[4].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb
      
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb) {
      menuOptions.value.splice(4,1)
    }
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya) {
      menuOptions.value.splice(3,1)
    }
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].id) {
      menuOptions.value.splice(2,1)
      menuOptions.value[0]?.children?.splice(0,3)
      console.log(menuOptionsCopy.value[0])
    } else if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].rackIndices) {
      menuOptions.value[0]?.children?.splice(1,1)
    }
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].id) {
      menuOptions.value.splice(1,1)
    }
  } else if(tableData.value[lndexY][formParam.value[lndexX]][0]?.type == 2) {
    menuOptions.value[0].value = tableData.value[lndexY][formParam.value[lndexX]][0]

    menuOptions.value[2] = menuOptionsCopy.value[2]
    menuOptions.value[2].value = tableData.value[lndexY][formParam.value[lndexX]][0]
    menuOptions.value[2].label = "机柜：" + tableData.value[lndexY][formParam.value[lndexX]][0].name

    menuOptions.value[3] = menuOptionsCopy.value[3]
    menuOptions.value[3].value = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya
    menuOptions.value[3].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya

    menuOptions.value[4] = menuOptionsCopy.value[4]
    menuOptions.value[4].value = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb
    menuOptions.value[4].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb
      
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb) {
      menuOptions.value.splice(4,1)
    }
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya) {
      menuOptions.value.splice(3,1)
    }
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].id) {
      menuOptions.value.splice(2,1)
      console.log(menuOptionsCopy.value[0])
    }
    menuOptions.value.splice(1,1)
    menuOptions.value[0]?.children?.splice(3,3)
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus && !tableData.value[lndexY][formParam.value[lndexX]][0].rackIndices) {
      menuOptions.value[0]?.children?.splice(1,1)
    }
  } else {
    menuOptions.value.splice(1,4)
  }
  console.log(editEnable.value)
  if(!editEnable.value) {
    console.log(111111111)
    menuOptions.value.splice(0,1)
  }
  // let itemId = tableData.value[lndexY][formParam.value[lndexX]]?.[0]?.id
  // let maxX = 26
  // let maxY = 26
  // for(let i = 0;i < rowColInfo.col-(+lndexX);i++) {
  //   if(+lndexY+1 == rowColInfo.row || (!itemId && (tableData.value[+lndexY][formParam.value[+lndexX+i]].length || tableData.value[+lndexY+1][formParam.value[+lndexX+i]].length)) || (tableData.value[+lndexY][formParam.value[+lndexX+i]].length && tableData.value[+lndexY][formParam.value[+lndexX+i]][0].id != itemId) || (tableData.value[+lndexY+1][formParam.value[+lndexX+i]].length && tableData.value[+lndexY+1][formParam.value[+lndexX+i]][0].id != itemId)) {
  //     maxX = i
  //     break
  //   }
  // }
  // for(let i = 0;i < rowColInfo.row-(+lndexY);i++) {
  //   if(+lndexX+1 == rowColInfo.col || (!itemId && (tableData.value[+lndexY+i][formParam.value[+lndexX]].length || tableData.value[+lndexY+i][formParam.value[+lndexX+1]].length)) || (tableData.value[+lndexY+i][formParam.value[+lndexX]].length && tableData.value[+lndexY+i][formParam.value[+lndexX]][0].id != itemId) || (tableData.value[+lndexY+i][formParam.value[+lndexX+1]].length && tableData.value[+lndexY+i][formParam.value[+lndexX+1]][0].id != itemId)) {
  //     maxY = i
  //     break
  //   }
  // }
  // //console.log(maxX,maxY)
  operateMenu.value = {
    left: offsetX + 'px',
    top: offsetY + 'px',
    show: true,
    lndexX, // 当前列
    lndexY, // 当前行
    maxlndexX: 26,
    maxlndexY: 26,
    xLength: rowColInfo.col,
    yLength: rowColInfo.row
  }
  //console.log('editEnable.value', editEnable.value)
}

const handleMenu = (value) => {
  //console.log(value)
  operateMenu.value.show = false
  switch (value[1]) {
    case '柜列配电':
      push({ 
        path: '/aisle/aislemonitor/columnHome', 
        query: { id: value[0], roomId: roomId.value } 
      });
      break;
    
    case '柜列用能':
      push({ 
        path: '/aisle/aislemonitor/aisleenergydetail', 
        query: { roomId: roomId.value, id: value[0], location: rowColInfo.roomName } 
      });
      break;
    
    case '柜列需量':
      push({ 
        path: '/aisle/aislemonitor/aislerequirement', 
        query: { 
          openDetailFlag: 1, 
          id: value[0], 
          location: `${rowColInfo.roomName}-${tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0].name}`
        } 
      });
      break;
    
    case '柜列供电平衡':
      push({ 
        path: '/aisle/aislemonitor/aislebalance', 
        query: { openDetailFlag: 1, id: value[0] } 
      });
      break;

    case '柜列功率因数':
      push({ 
        path: '/aisle/aislemonitor/aislepowerfactor', 
        query: { 
          openDetailFlag: 1, 
          id: value[0], 
          location: `${rowColInfo.roomName}-${tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0].name}`
        } 
      });
      break;

    case '机柜负荷':
      push({ 
        path: '/cabinet/cab/cabinetPowerLoadDetail', 
        query: { 
          cabinet: value[0].id, 
          roomId: value[0].roomId,
          roomName: rowColInfo.roomName,
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
          location: rowColInfo.roomName,
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
          roomName: rowColInfo.roomName,
          cabinetName: value[0].cabinetName
        } 
      });
      break;

    case '机柜供电平衡':
      push({ 
        path: '/cabinet/cab/balance', 
        query: { 
          openDetailFlag: 1, 
          id: value[0].id
        } 
      });
      break;

    case '机柜功率因数':
      openPFDetail({
        id: value[0].id,
        cabinetName: value[0].cabinetName
      })
      break;
    
    case '拖拽视图':
      dragTableView();
      break;
    
    case '机柜编辑':
      addCabinet('edit', value[0]);
      break;
    
    case '机柜删除':
      deleteCabinet(value[0], false);
      break;
    
    case '机柜解绑':
      deleteCabinet(value[0], true);
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

const deleteCabinet = (cabItem,flag) => {
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
    getRoomInfoNoLoading()
  })
}

// 判断是否展示添加菜单项
const showMenuAdd = computed(() => {
  const lndexX = operateMenu.value.lndexX
  const lndexY = operateMenu.value.lndexY
  //console.log('tableData.value[lndexY][formParam.value[lndexX]]', tableData.value[lndexY][formParam.value[lndexX]], lndexX, lndexY)
  return !(tableData.value[lndexY][formParam.value[lndexX]].length > 0)
})
// 拖拽开始的事件
const onStart = ({from}) => {
  const X = from.id.split('-')[1]
  const Y = from.id.split('-')[0]
  const target = tableData.value[Y][formParam.value[X]][0]
  movingInfo.value = target
  ////console.log('onStssasrt', target)
}
// 拖拽结束的事件
const onEnd = ({from, to}) => {
 //console.log('onsEnd',tableData.value, from, to, from.id, to.id)
  if (from.id != to.id) { // 发生移动才处理
    const X = +to.id.split('-')[1]
    const Y = +to.id.split('-')[0]
    const X1 = +from.id.split('-')[1]
    const Y1 = +from.id.split('-')[0]
    const targetTo = tableData.value[Y][formParam.value[X]][0]
    //console.log('value*******', targetTo,X+1,Y+1)
    
    if (targetTo.type == 1) {
      if (targetTo.direction == 1) {
        tableData.value[Y1+1][formParam.value[X1]] = []
        tableData.value[Y+1][formParam.value[X]] = [{...targetTo,first: false}]
      } else {
        tableData.value[Y1][formParam.value[X1+1]] = []
        tableData.value[Y][formParam.value[X+1]] = [{...targetTo,first: false}]
      }
      for (let i=  1; i < targetTo.amount; i++) {
        if (targetTo.direction == 1) {
          tableData.value[Y1][formParam.value[X1+i]] = []
          tableData.value[Y1+1][formParam.value[X1+i]] = []
          tableData.value[Y][formParam.value[X+i]] = [{...targetTo,first: false}]
          tableData.value[Y+1][formParam.value[X+i]] = [{...targetTo,first: false}]
        } else {
          tableData.value[Y1+i][formParam.value[X1]] = []
          tableData.value[Y1+i][formParam.value[X1+1]] = []
          tableData.value[Y+i][formParam.value[X]] = [{...targetTo,first: false}]
          tableData.value[Y+i][formParam.value[X+1]] = [{...targetTo,first: false}]
        }
      }
    } else if (targetTo.type == 2) {
      tableData.value[Y1+1][formParam.value[X1]] = []
      tableData.value[Y+1][formParam.value[X]] = [{...targetTo,first: false}]
    }
    //console.log('tableData.value*******', tableData.value)
    moveMachine(targetTo,X+1,Y+1)
  }
}
const moveMachine = async (data,x,y) => {
  let res
  if(data.type == 1) {
    res = await MachineRoomApi.saveRoomAisle({
        id:data.id,
        roomId: roomId.value,
        aisleName:data.name,
        aisleLength:data.amount,
        xCoordinate:x,
        yCoordinate:y,
        direction:data.direction == 1 ? 'x' : 'y',
        powerCapacity:data.powerCapacity,
        eleAlarmDay:data.eleAlarmDay,
        eleAlarmMonth:data.eleAlarmMonth,
        eleLimitDay:data.eleLimitDay,
        eleLimitMonth:data.eleLimitMonth
    }) 
  } else {
    res = await MachineRoomApi.saveRoomCabinet({
      id: data.id,
      roomId: roomId.value,
      cabinetName: data.name,
      cabinetHeight: data.cabinetHeight,
      xCoordinate:x,
      yCoordinate:y,
      powerCapacity:data.powerCapacity,
      eleAlarmDay: data.eleAlarmDay,
      eleLimitDay: data.eleLimitDay,
      eleAlarmMonth: data.eleAlarmMonth,
      eleLimitMonth: data.eleLimitMonth
    })
  }
  if(res) {
    message.success("移动成功")
  } else {
    message.success("移动失败")
  }
  getRoomInfoNoLoading()
}
//移动表格视图
const dragTableView = () => {
  dragCursor.value = 'grab';
  operateMenu.value.show = false
}
const onMouseDown = (e) => {

  if (dragCursor.value == 'grab') {
    dragCursor.value = 'grabbing';
    startX.value = e.pageX;
    startY.value = e.pageY;
    // 获取表格滚动区域元素
    const tableScrollbarWrap = dragTable.value.$el.querySelector('.el-scrollbar__wrap');
    // 获取滚动位置
    scrollTop.value = tableScrollbarWrap.scrollTop;
    scrollLeft.value = tableScrollbarWrap.scrollLeft;
    return false
  }
}

const onMouseMove = (e) => {
  const now = Date.now();

  // 如果距离上次执行的时间小于 throttleDelay，则跳过
  if (now - lastTime < throttleDelay) {
    return
  } 

  lastTime = now;
  if (dragCursor.value == 'grabbing') {
    const dx = e.pageX - startX.value;
    const dy = e.pageY - startY.value;
    dragTable.value.setScrollLeft(scrollLeft.value-dx)
    dragTable.value.setScrollTop(scrollTop.value-dy)
  }
};
const onMouseUp = () => {
  if (dragCursor.value == 'grabbing') {
    dragCursor.value = 'grab'
  }
}

const onMouseLeave = () => {
  dragCursor.value = ''
}

const onSelectStart = (e) => {
  if(dragCursor.value == 'grabbing') {
    e.preventDefault();
  }
}

// 增加/编辑机柜弹框
const addCabinet = async (type: string, cabItem?: object) => {
  if (type == 'add') {
    machineFormCabinet.value.open(type, null,operateMenu.value,null)
  } else if (type == 'edit' && cabItem) {
    try {
      console.log(cabItem)
      machineFormCabinet.value.open(type, cabItem,operateMenu.value,tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0])
    } finally {
    }
  }
  operateMenu.value.show = false
}

// 增加柜列弹框
const addAisle = () => {
  if(!operateMenu.value.maxlndexX && !operateMenu.value.maxlndexY) {
    message.warning("当前位置空间不足，请选择其他位置")
    operateMenu.value.show = false
    return
  }
  aisleFlag.value = 1;
  machineForm.value.open('add', null, operateMenu.value)
  operateMenu.value.show = false
}
// 编辑柜列弹框
const editMachine = () => {
  aisleFlag.value = 2;
  const Y = operateMenu.value.lndexY;
  const X = formParam.value[operateMenu.value.lndexX];
  //console.log("machineForm",machineForm,{...tableData.value[Y][X][0]})
  machineForm.value.open('edit', {...tableData.value[Y][X][0]}, operateMenu.value);
  operateMenu.value.show = false;
}
// 跳转机柜/柜列
const handleJump = (data) => {
  let target = {} as any;
  if (data) {
    target = data;
  } else {
    const Y = operateMenu.value.lndexY;
    const X = formParam.value[operateMenu.value.lndexX];
    target = tableData.value[Y][X][0];
  }
 // //console.log('target', target)
  if (!target.id) {
    message.error(`该${target.type == 1 ? '柜列' : '机柜'}暂未保存绑定，无法跳转`)
    return
  }
  if (target.type == 1) {
    push({path: '/aisle/aislemonitor/columnHome', query: { id: target.id, roomId: roomId.value }})
  } else {
    if(target.runStatus == 0){
     message.error('未绑定设备无法查看详情!')
     return;
    }
    push({path: '/cabinet/cab/detail', state: {id: target.id, roomId: target.roomId,type: 'hour',location: rowColInfo.roomName,cabinetName: target.cabinetName}})
  }
}
// 删除机柜
const deleteMachine = () => {
  // //console.log("tableData.value",tableData.value)
   const Y = operateMenu.value.lndexY
    const X = formParam.value[operateMenu.value.lndexX]
    const target = JSON.parse(JSON.stringify(tableData.value[Y][X][0])) // 要删除的目标
 //console.log('删除机柜',tableData.value[Y][X], target,Y,X)
    //console.log(target)
  if (target.type && target.type == 1 && !target.canDelete) {
    message.warning(`该柜列有机柜，无法删除，请先删除机柜`)
    return
  } else if(target.type && target.type == 2 && (target.cabinetkeya || target.cabinetkeyb)) {
    message.warning(`该机柜已绑定，无法删除，请先删除绑定关系`)
    return
  }
  ElMessageBox.confirm('确认要删除吗？', '提示', {
    confirmButtonText: '确 认',
    cancelButtonText: '取 消',
    type: 'warning'
  }).then(async () => {
   
    if (target.type && target.type == 1) {
      const aisleRes = await MachineRoomApi.deletedRoomAisleInfo({id: target.id})
      if(aisleRes != null || aisleRes != "") {
        // getRoomInfoNoLoading()
        for (let i = 0; i < target.originAmount; i++) {
          if (target.direction == 1) {
            // const charCode = X.charCodeAt(0) + i
        //    //console.log('String.fromCharCode(charCode)', operateMenu.value.lndexX, operateMenu.value.lndexX+i)
            tableData.value[Y][formParam.value[+operateMenu.value.lndexX + i]].splice(0, 1)
            tableData.value[+Y + 1][formParam.value[+operateMenu.value.lndexX + i]].splice(0, 1)
          } else {
            tableData.value[+Y + i][X].splice(0, 1)
            tableData.value[+Y + i][formParam.value[+operateMenu.value.lndexX + 1]].splice(0, 1)
          }
        }
        message.success('删除成功')
      }
      // //console.log("tableData.value",tableData.value)
    } else if(target.type && target.type == 2) {
      const cabinetRes = await CabinetApi.deleteCabinetInfo({
        id: target.id,
        type: 4
      })
      //console.log("tableData.value",tableData.value)
      if(cabinetRes != null || cabinetRes != "") {
        // getRoomInfoNoLoading()
        tableData.value[Y][X].splice(0, 1)
        tableData.value[+Y + 1][X].splice(0, 1)
        message.success('删除成功')
      }
      //console.log("tableData.value",tableData.value)
    }
  })
  operateMenu.value.show = false
}
// 处理增加/编辑机柜
const handleChange = async(data) => {
  let aisleFlagId:any = null;
  let messageAisleFlag = "保存成功！";
  let messageAisleFlagError = "保存失败！";
  if(aisleFlag.value == 2){
      aisleFlagId = data.id; 
      messageAisleFlag = "修改成功！";
      messageAisleFlagError = "修改失败！"
  }
  if(data.type == 1){
      let asileObject = {id:aisleFlagId,
          roomId: roomId.value,
          aisleName:data.name,
          aisleLength:data.amount,
          xCoordinate:data.xCoordinate,
          yCoordinate:data.yCoordinate,
          direction:data.direction == 1 ? 'x' : 'y',
          powerCapacity:data.powerCapacity,
          eleAlarmDay:data.eleAlarmDay,
          eleAlarmMonth:data.eleAlarmMonth,
          eleLimitDay:data.eleLimitDay,
          eleLimitMonth:data.eleLimitMonth
      }

      const flagRes = await MachineRoomApi.findAddAisleVerify(asileObject)

      if(flagRes) {
        message.error(messageAisleFlagError + "可能原因如下：该柜列的位置的长度范围内有机柜或柜列,柜列同名,柜列超出机房长度范围")
        return
      }

      const aisleRes = await MachineRoomApi.saveRoomAisle(asileObject) 
      if(aisleRes != null || aisleRes != "") {
        // if(tableData.value[+Y][formParam.value[X]].length) {
        //   let lastMount = tableData.value[+Y][formParam.value[X]][0].amount
        //   for(let i=  0; i < lastMount; i++) {
        //     if (data.direction == 1) {
        //       tableData.value[+Y][formParam.value[X+i]] = []
        //       tableData.value[+Y+1][formParam.value[X+i]] = []
        //     } else {
        //       tableData.value[+Y+i][formParam.value[X]] = []
        //       tableData.value[+Y+i][formParam.value[X+1]] = []
        //     }
        //   }
        // }
        
        // if (data.direction == 1) {
        //   tableData.value[+Y][formParam.value[X]] = [{...data,first: true}]
        //   tableData.value[+Y+1][formParam.value[X]] = [{...data,first: false}]
        // } else {
        //   tableData.value[+Y][formParam.value[X]] = [{...data,first: true}]
        //   tableData.value[+Y][formParam.value[X+1]] = [{...data,first: false}]
        // }
        // for (let i=  1; i < data.amount; i++) {
        //   if (data.direction == 1) {
        //     tableData.value[+Y][formParam.value[X+i]] = [{...data,first: false}]
        //     tableData.value[+Y+1][formParam.value[X+i]] = [{...data,first: false}]
        //   } else {
        //     tableData.value[+Y+i][formParam.value[X]] = [{...data,first: false}]
        //     tableData.value[+Y+i][formParam.value[X+1]] = [{...data,first: false}]
        //   }
        // }
        getRoomInfoNoLoading()
        message.success(messageAisleFlag);
      }
  }
  // else{
      // const cabinetRes = await MachineRoomApi.saveRoomCabinet({
      //     id:aisleFlagId
      //     roomId: roomId.value,
      //     cabinetName: data.name,
      //     cabinetHeight: data.cabinetHeight,
      //     xCoordinate:X+1,
      //     yCoordinate:+Y+1,
      //     powerCapacity:data.powerCapacity,
      //     eleAlarmDay: data.eleAlarmDay,
      //     eleLimitDay: data.eleLimitDay,
      //     eleAlarmMonth: data.eleAlarmMonth,
      //     eleLimitMonth: data.eleLimitMonth
      // })
      // if(cabinetRes != null || cabinetRes != "") {
      //   message.success(messageAisleFlag);
      // }
  // }
}

const queryParamsPF = reactive({
  pageNo: 1,
  pageSize: 24,
  cabinetIds : [],
})as any
const pfESList = ref({}) as any
const pfTableList = ref([]) as any
const exportLoading = ref(false) // 导出的加载中
const detailVis = ref(false)

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

const getCabinetColorAll = async () => {
  const res = await IndexApi.getCabinetColorAll()
  if (res != null) {
    tempList.value = res
  }
}

const getColumnCharCodeToNumber = (columnId: string): number => {
  let result = 0;
  const base = 26; // 因为英文字母有 26 个
  // 从字符串末尾开始遍历，因为 Excel 列标识符是从左到右递增的 26 进制数
  for (let i = 0; i < columnId.length; i++) {
    const charCode = columnId.charCodeAt(i) - 'A'.charCodeAt(0) + 1; // 将字符转换为 1-26 的数字
    const power = Math.pow(base, columnId.length - 1 - i); // 计算当前位的权重（26 的幂）
    result += charCode * power; // 将当前位的值加到结果中
  }
  return result - 1; // 因为我们的计算是从 1 开始的（A=1），而通常我们希望索引从 0 开始
};

// 处理设置提交
const submitSetting = async() => {
   if(rowColInfo.roomName == '') {
      message.error('机房名称不能为空,请输入!')
      return
   }

   let roomFlagId:any = null;
   let messageRoomFlag = "保存成功！";
   if(roomFlag.value == 2){
      roomFlagId = roomId.value; 
      messageRoomFlag = "修改成功！";
   }
   if(radio.value === "PUE") {
    rowColInfo.displayType = 1
   }
   else {
    rowColInfo.displayType = 0
   }
   try {
    const res = await MachineRoomApi.saveRoomDetail({
      id: roomFlagId,
      roomName: rowColInfo.roomName,
      addr: rowColInfo.addr,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
      powerCapacity:rowColInfo.powerCapacity, 
      airPower:rowColInfo.airPower, 
      displayType: rowColInfo.displayType, 
      displayFlag: rowColInfo.displayFlag, 
      eleAlarmDay: rowColInfo.eleAlarmDay,
      eleAlarmMonth: rowColInfo.eleAlarmMonth,
      eleLimitDay: rowColInfo.eleLimitDay,
      eleLimitMonth: rowColInfo.eleLimitMonth,
    })
    
    if(res != null || res != "")
    message.success(messageRoomFlag);
    dialogVisible.value = false;
    roomId.value = res;
   } catch (error) {
    //console.log(error)
   }
   
   getRoomList();
}


// 获取表格列label字符
const getTableColCharCode = (num: number): string => {
  const baseCharCode = 65; // A 的 ASCII 码
  if (num < 26) {
    return String.fromCharCode(baseCharCode + num);
  } else if (num < 52) {
    return `A ${String.fromCharCode(baseCharCode + (num - 26))}`;
  } else {
    return `B ${String.fromCharCode(baseCharCode + (num - 52))}`;
  }
};
// 处理提交保存事件
const handleSubmit = async() => {
  const aisleList = [] as any
  const cabinetList = [] as any
  if (!isAddRoom.value)
  //console.log(rowColInfo.row)
  //console.log(rowColInfo.row)
  for(let i = 0; i < rowColInfo.row; i++) {
    for(let j = 0; j < rowColInfo.col; j++) {
    //  //console.log('处理提交保存事件', tableData.value, i, getTableColCharCode(j))
      const target = tableData.value[i][getTableColCharCode(j)][0]
      if (target && target.type == 1 && target.first) {
        //console.log('target.......', target)
        aisleList.push({
          id: target.id,
          aisleName: target.name,
          xCoordinate: j + 1,
          yCoordinate: i + 1,
          direction: target.direction == 1 ? 'x' : 'y',
          length: target.amount,
          eleAlarmDay: target.eleAlarmDay,
          eleLimitDay: target.eleLimitDay,
          eleAlarmMonth: target.eleAlarmMonth,
          eleLimitMonth: target.eleLimitMonth,
        })
      } else if (target && target.type == 2 && target.first) {
        cabinetList.push({
          id: target.id,
          cabinetName: target.name,
          cabinetHeight: target.cabinetHeight,
          xCoordinate: j + 1,
          yCoordinate: i + 1,
          eleAlarmDay: target.eleAlarmDay,
          eleLimitDay: target.eleLimitDay,
          eleAlarmMonth: target.eleAlarmMonth,
          eleLimitMonth: target.eleLimitMonth,
        })
      }
    }
  }
  try {
    //console.log('aisleListend.......', aisleList)
    //console.log('cabinetList.......', cabinetList)
    loading.value = true
    const res = await MachineRoomApi.saveRoomDetail({
        id: isAddRoom.value ? '' : roomId.value,
        roomName: rowColInfo.roomName,
        addr: rowColInfo.addr,
        xLength: rowColInfo.col,
        yLength: rowColInfo.row,
        powerCapacity:rowColInfo.powerCapacity, 
        airPower:rowColInfo.airPower, 
        displayType: rowColInfo.displayType, 
        displayFlag: rowColInfo.displayFlag, 
        eleAlarmDay: rowColInfo.eleAlarmDay,
        eleAlarmMonth: rowColInfo.eleAlarmMonth,
        eleLimitDay: rowColInfo.eleLimitDay,
        eleLimitMonth: rowColInfo.eleLimitMonth,
        aisleList,
        cabinetList
    })
    if (isAddRoom.value) {
          roomId.value = res;
          getRoomList();
          message.success('新建成功！');
          dialogVisible.value = false;
          editEnable.value = false;
          isAddRoom.value = false;
          return;
    }
    editEnable.value = false;
    message.success('保存成功！');
  } finally {
    loading.value = false;
  }
}

// const formParam = Object.keys(tableData[0])
const formParam = computed(() => {
  return Object.keys(tableData.value[0] || {});
})

getRoomList()

watch(() => containerInfo, (val) => {
  if (val) {
    roomId.value = containerInfo?.roomId
  }
},{immediate: true})

onMounted(() => {
  getCabinetColorAll()
  roomsId.roomDownValIds = Number(query.id)
  flashListTimer.value = setInterval((getRoomInfoNoLoading), 5000);
  document.addEventListener('mousedown', (event) => {
    const element = event.target as HTMLElement
    // //console.log(event)
    if (event.button == 0 && operateMenu.value.show && element.className != 'menu_item' && element.className != 'el-cascader-node__label') {
      operateMenu.value.show = false
    }
  })
  // timer = setInterval(() => {
  //   getRoomStatus(false)
  // }, 5000)
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

onUnmounted(() => {
  window.onresize = null
  clearInterval(timer)
  timer = null
})
</script>

<style lang="scss" scoped>
.btns {
  margin-left: 25px;
}
.status {
  font-size: 14px;
  display: flex;
  align-items: center;
  .box {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-left: 10px;
    margin-right: 5px;
  }
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items:center;
  height: 3vh
}
.dragContainer {
  // transform-origin: left right;
  position: relative;
  .dragTable {
    transition: transform 0.3s ease; /* 添加平滑过渡效果 */
  }
  .mask {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    height: 50%;
    z-index: 999;
  }
  .menu {
    box-sizing: border-box;
    position: absolute;
    background-color: #fff;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    z-index: 999;
    .menu_item {
      width: 140px;
      height: 30px;
      padding: 20px 10px;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 14px;
      color: #606266;
    }
    .menu_item:hover {
      background-color: rgb(231, 245, 255);
      color: rgb(82, 177, 255);
      cursor: pointer;
    }
  }
  .dragTd {
    width: 100%;
    height: 100%;
    .dragChild {
      width: 100%;
      height: 100%;
      min-height: 20px;
      box-sizing: border-box;
      display: flex;
      border: 1px solid #000;
      &:hover {
        border: 1px solid #007BFF;
      }
      // align-items: center;
      .dragSon {
        min-height: 20px;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        background-color: #effaff;
        border-right: 1px solid #bed1ff;
        &>div {
          min-height: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .fill {
        background-color: #effaff;
      }
    }
    .dragChildCol {
      width: 100%;
      height: 100%;
      min-height: 20px;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      border: 1px solid #000;
      &:hover {
        border: 1px solid #007BFF;
      }
      .dragSon {
        flex: 1;
        min-height: 20px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #effaff;
        border-bottom: 1px solid #bed1ff;
        &>div {
          min-height: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .fill {
        background-color: #effaff;
      }
    }
  }
  .warnDrag {
    min-height: 20px;
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    // height: 40px;
    background-color: rgb(255, 219, 12);
  }
  .normalDrag {
    min-height: 20px;
    height: 100%;
    width: 100%;
    // height: 40px;
    background-color: #effaff;
    box-sizing: border-box;
    border: 1px solid #000;
      &:hover {
        border: 1px solid #007BFF;
      }
    &>div {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;
      min-height: 20px;
    }
  }
}
.double-formitem {
  display: flex;
  & > div {
    flex: 1;
  }
}
.custom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: nowrap;
  margin-top:-50px;
}

:deep(.el-card__body) {
  padding: 15px;
}

:deep(.el-card) {
  margin-top: -5px;
  margin-bottom: 10px;
}
:deep(.el-input-number) {
  width: 100%;
}
:deep(.dragTable .hover-row .el-table__cell td) {
  background-color:unset!important;
}
:deep(.dragTable .el-table__cell) {
  padding: 0;
}
:deep(.dragTable .el-table__cell .cell) {
  width: 100%;
  height: 100%;
  min-height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  & > div {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

:deep(.dragTable .el-table__header .el-table__cell) {
  // background-color: #ddd;
  // box-shadow: 0 1px 0px #ddd;
}

:deep(.dragTable .el-table__body) {
  height: 100%;
  transform-origin: let top;
}

:deep(.dragTable .el-table__body .el-table__row .el-table__cell:nth-of-type(1)) {
  // background-color: #ddd;
  // box-shadow: 0 1px 0px #ddd;
}

:deep(.el-cascader-menu__wrap.el-scrollbar__wrap) {
  height: auto;
}

.crosshair {
  cursor: crosshair; /* 当正在拖动时显示十字图标 */
}
</style>
