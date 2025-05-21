<template>
<!-- <div style="height:70%;"> -->
  <el-card shadow="never" :style="isFromHome ? 'flex: 1' : ''">
    <div class="toolbar">
      <div style="display: flex;align-items:center" v-if="!isFromHome">
        机房：
        <el-select :size="isFromHome ? 'small' : ''" v-model="roomId" placeholder="请选择" class="!w-200px" @change="handleChangeRoom">
          <el-option v-for="item in roomList" :key="item.id" :label="item.roomName" :value="item.id" />
        </el-select>
      </div>
      <!-- <div v-if="chosenBtn == 6" class="status" style="margin-left: 10px;height: 100%">
        温度条：
        <div class="temStatus" style="background: linear-gradient(to right, rgb(244,229,162), rgb(191,68,76))">
        </div>
      </div> -->
      <div class="status" :style="isFromHome ? 'font-size: 8px' : ''">
        <template v-for="item in statusInfo[chosenBtn]" :key="item.value">
          <div class="box" :style="{backgroundColor: item.color}"></div>{{item.name}}
        </template>
      </div>
      <div class="btns" :style="isFromHome ? 'flex: 1;display: flex;justify-content: flex-end;margin-right: 10px' : 'flex: 1;display: flex;justify-content: flex-end;margin-right: 10px'">
        <div style="display: flex;justify-content: flex-end;margin-right:3px;width: 100%;align-items: center;">
          <el-button size="small" @click="tableScaleValue = tableScaleValueStart;tableScaleWidth = tableScaleWidthStart;tableScaleHeight = tableScaleHeightStart" circle ><Icon icon="ep:refresh-right" /></el-button>
          <el-button size="small" @click="tableScale(false)" circle ><Icon icon="ep:minus" /></el-button>
          <el-button size="small" @click="tableScale(true)" circle ><Icon icon="ep:plus" /></el-button>
        </div>
        <template v-for="item in btns" :key="item.value">
          <el-button @click="switchBtn(item.value)" round color="#00778c" :size="isFromHome ? 'small' : ''" :plain="chosenBtn != item.value">{{item.name}}</el-button>
        </template>
      </div>
      <div style="display: flex;align-items: center;">
        <!-- <el-button @click="handleAdd" type="primary">新建机房</el-button> -->
        <el-button v-if="!editEnable" @click="handleEdit" :size="isFromHome ? 'small' : ''" type="primary" color="black">编辑</el-button>
        <!-- <el-button v-if="editEnable" @click="handleStopDelete" plain type="danger">已删除</el-button> -->
        <el-button v-if="editEnable" @click="handleCancel" plain type="primary" color="black">取消</el-button>
        <el-button v-if="editEnable" @click="openSetting" plain type="primary" color="black"><Icon :size="16" icon="ep:setting" style="margin-right: 5px" />配置</el-button>
        <el-button v-if="editEnable" @click="handleExportAisle" plain :loading="exportLoading" type="primary" color="black"><Icon :size="16" icon="ep:download" style="margin-right: 5px" />导出</el-button>
        <div style="margin-left: 12px">
          <el-upload
            v-if="editEnable"
            action=""
            :auto-upload="false"
            :on-change="handleFileChange"
            :show-file-list="false"
            accept=".xlsx,.xls"
          >
            <template #trigger>
              <el-button plain :loading="importLoading" type="primary" color="black"><Icon :size="16" icon="ep:upload" style="margin-right: 5px" />导入</el-button>
            </template>
          </el-upload>
        </div>
        <!-- <el-button v-if="editEnable" @click="handleSubmit" plain type="primary" color="black">保存</el-button> -->
        <!-- <el-button v-if="editEnable" @click="handleDelete" type="primary">删除机房</el-button> -->
        
      </div>
    </div>
  </el-card>
  <el-card shadow="never" :body-style="isFromHome ? 'padding: 0' : ''">
    <div ref="dragTableViewEle" @mousedown="onMouseDown" @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseLeave" @selectstart="onSelectStart" :style="{cursor: `${dragCursor}`}">
        <div class="dragContainer" 
          ref="tableContainer"
          v-loading="loading" 
          style="z-index: 100"
          @click.right="handleRightClick"
          :style="isFromHome ? `transform-origin: 0 0;height: 49vh;width:${tableScaleWidth}%;` : `height:calc(100vh - 180px);width:${tableScaleWidth}%;`">
          <!-- <div class="mask" v-if="!editEnable" @click.prevent=""></div> -->
          <el-table ref="dragTable" class="dragTable" v-if="tableData.length > 0" :style="{width: '100%',height: `${tableScaleHeight}%`,transform: `translateZ(0) scale(${tableScaleValue})`, transformOrigin: '0 0',transition: 'none'}" :data="tableData" border :row-style="{background: 'revert'}" :span-method="arraySpanMethod" row-class-name="dragRow">
            <el-table-column fixed type="index" min-width="31" align="center" :resizable="false" />
            <template v-for="(formItem, index) in formParam" :key="index">
              <el-table-column :label="formItem" min-width="31" align="center" :resizable="false">
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
                        <div v-if="chosenBtn == 0 && element.runStatus != 0 && element.runStatus != 4" :style="{backgroundColor: element.cabinetName && element.loadRate ? (element.loadRate>=89.5 ? `rgba(240, 58, 23, ${element.loadRate/100})` : (element.loadRate>=74.5 ? `rgba(255, 225, 0, ${(element.loadRate+15)/100})` : (element.loadRate>=49.5 ? `rgba(0, 120, 215, ${(141-element.loadRate)/100})` : `rgba(22, 198, 12, ${(element.loadRate+60)/100})`))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    前门露点温度: {{element.temludianfront ? element.temludianfront.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C<br/>
                                    后门露点温度: {{element.temludianblack ? element.temludianblack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="element.loadRate != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.loadRate ? element.loadRate.toFixed(0) : '0'}}<div style="font-size: 10px;margin-top: -15px">%</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 1 && element.runStatus == 1" :style="{backgroundColor: element.cabinetName ? `rgba(41, 132, 71, ${element.loadRate/100+0.5})` : '#f5f7fa',color: '#fff',height: '100%',width: '100%'}">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    前门露点温度: {{element.temludianfront ? element.temludianfront.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C<br/>
                                    后门露点温度: {{element.temludianblack ? element.temludianblack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="element.powActive != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powActive ? element.powActive.toFixed(0) : '0'}}<div style="font-size: 10px;margin-top: -15px">kW</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 2 && element.runStatus != 0 && element.runStatus != 4" :style="{backgroundColor: element.cabinetName && element.powerFactor ? (element.powerFactor>=0.945 ? `#16c60c` : (element.powerFactor>=0.845 ? `#0078d7` : (element.powerFactor>=0.745 ? `rgba(252, 225, 0, ${1.75-element.powerFactor})` : `rgba(240, 58, 23, ${1.3-element.powerFactor})`))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    前门露点温度: {{element.temludianfront ? element.temludianfront.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C<br/>
                                    后门露点温度: {{element.temludianblack ? element.temludianblack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="element.powerFactor != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}</div>
                            </el-tooltip>
                          </template>
                        </div>
                        <!-- <div v-else-if="chosenBtn == 6 && element.runStatus != 0 && element.runStatus != 4" :style="{background: `linear-gradient(to bottom, ${getColorFromGradient(element.temFront)} 50%, ${getColorFromGradient(element.temBlack)} 50%)`,color: '#fff',height: '100%',width: '100%'}">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%
                                  </div>
                                  <div style="width: 50%">
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="element.temFront != 0 || element.temBlack != 0" style="display: flex;flex-direction: column;justify-content: space-around;width: 100%">
                                <div>{{element.temFront ? element.temFront.toFixed(1) : '0.0'}}</div>
                                <div>{{element.temBlack ? element.temBlack.toFixed(1) : '0.0'}}</div>
                              </div>
                            </el-tooltip>
                          </template>
                        </div> -->
                        <div v-else-if="chosenBtn == 3 && element.runStatus != 0 && element.runStatus != 4" :style="{backgroundColor: element.cabinetName && element.temFront ? (element.temFront>=35 ? `rgba(232, 18, 36, ${element.temFront})` : (element.temFront>=30 ? `rgba(247, 99, 12, ${(element.temFront+65)/100})` : (element.temFront>=27 ? `rgba(252, 225, 0, ${(element.temFront+70)/100})` : (element.temFront>=24 ? `rgba(50, 205, 50, ${(124-element.temFront)/100})` : (element.temFront>20 ? `rgba(0, 128, 0, ${(element.temFront+76)/100})` : `rgba(0, 120, 215, ${element.temFront+80})`))))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
                          <template v-if="element.id > 0">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    前门露点温度: {{element.temludianfront ? element.temludianfront.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C<br/>
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门露点温度: {{element.temludianblack ? element.temludianblack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="element.temFront != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temFront ? element.temFront.toFixed(1) : '0.0'}}<div style="font-size: 10px;margin-top: -15px">°C</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else-if="chosenBtn == 4 && element.runStatus != 0 && element.runStatus != 4" :style="{backgroundColor: element.cabinetName && element.temBlack ? (element.temBlack>=45 ? `rgba(232, 18, 36, ${element.temBlack})` : (element.temBlack>=40 ? `rgba(247, 99, 12, ${(element.temBlack+55)/100})` : (element.temBlack>=35 ? `rgba(252, 225, 0, ${(element.temBlack+60)/100})` : (element.temBlack>30 ? `rgba(50, 205, 50, ${(130-element.temBlack)/100})` : `rgba(0, 128, 0, ${(element.temBlack+70)/100})`)))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    前门露点温度: {{element.temludianfront ? element.temludianfront.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C<br/>
                                    后门露点温度: {{element.temludianblack ? element.temludianblack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temBlack ? element.temBlack.toFixed(1) : '0.0'}}<div style="font-size: 10px;margin-top: -15px">°C</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                        <div v-else :style="{backgroundColor: element.cabinetName ? statusColor[element.runStatus].color : '#f5f7fa',color: '#fff',height: '100%',width: '100%'}">
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
                                    总功率因素：{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}<br/>
                                    总有功功率：{{element.powActive ? element.powActive.toFixed(3) : '0.000'}}kW<br/>
                                    总视在功率：{{element.powApparent ? element.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                    总无功功率：{{element.powReactive ? element.powReactive.toFixed(3) : '0.000'}}kVar
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    A路功率：{{element.powActivea ? element.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                    A路设备：{{element.cabinetkeya}}
                                  </div>
                                  <div style="width: 50%">
                                    B路功率：{{element.powActiveb ? element.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                    B路设备：{{element.cabinetkeyb}}
                                  </div>
                                </div>
                                <hr/>
                                <div class="flex justify-between" style="width: 20vw">
                                  <div style="width: 50%">
                                    前门温度：{{element.temFront ? element.temFront.toFixed(1) : ''}}°C<br/>
                                    前门湿度：{{element.temFront ? element.humFront.toFixed(0) : ''}}%<br/>
                                    前门露点温度: {{element.temludianfront ? element.temludianfront.toFixed(1) : ''}}°C
                                  </div>
                                  <div style="width: 50%">
                                    后门湿度：{{element.temBlack ? element.humBlack.toFixed(0) : ''}}%<br/>
                                    后门温度：{{element.temBlack ? element.temBlack.toFixed(1) : ''}}°C<br/>
                                    后门露点温度: {{element.temludianblack ? element.temludianblack.toFixed(1) : ''}}°C
                                  </div>
                                </div>
                                <div v-if="element.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                  <hr/>
                                  告警类型：{{alarmTypeDesc[element.alarmLogRecord?.alarmType]}}<br/>
                                  告警描述：{{element.alarmLogRecord?.alarmDesc}}
                                </div>
                              </template>
                              <div v-if="chosenBtn == 0 && element.runStatus != 0 && element.runStatus != 4 && element.loadRate != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.loadRate ? element.loadRate.toFixed(0) : '0'}}<div style="font-size: 10px;margin-top: -15px">%</div></div>
                              <div v-if="chosenBtn == 1 && element.runStatus != 0 && element.runStatus != 4 && element.powActive != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powActive ? element.powActive.toFixed(0) : '0'}}<div style="font-size: 10px;margin-top: -15px">kW</div></div>
                              <div v-if="chosenBtn == 2 && element.runStatus != 0 && element.runStatus != 4 && element.powerFactor != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.powerFactor ? element.powerFactor.toFixed(2) : '0.00'}}</div>
                              <div v-if="chosenBtn == 3 && element.runStatus != 0 && element.runStatus != 4 && element.temFront != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temFront ? element.temFront.toFixed(1) : '0.0'}}<div style="font-size: 10px;margin-top: -15px">°C</div></div>
                              <div v-if="chosenBtn == 4 && element.runStatus != 0 && element.runStatus != 4 && element.temBlack != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.temBlack ? element.temBlack.toFixed(1) : '0.0'}}<div style="font-size: 10px;margin-top: -15px">°C</div></div>
                              <div v-if="chosenBtn == 5 && element.runStatus != 0 && element.runStatus != 4 && element.yesterdayEq != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{element.yesterdayEq ? element.yesterdayEq.toFixed(0) : '0'}}<div style="font-size: 10px;margin-top: -15px">kWh</div></div>
                            </el-tooltip>
                          </template>
                        </div>
                      </div>
                      <div v-else-if="element.type == 1" :class="element.direction == '1' ? 'dragChild' : 'dragChildCol'"  @dblclick="handleJump(element)">
                        <template v-if="element.cabinetList.length > 0">
                          <div :class="item.cabinetName ? 'dragSon fill' : 'dragSon'" v-for="(item, i) in element.cabinetList" :key="i" :data-index="i">
                            <div v-if="chosenBtn == 0 && item.runStatus != 0 && item.runStatus != 4" :style="{backgroundColor: item.cabinetName && item.loadRate ? (item.loadRate>=89.5 ? `rgba(240, 58, 23, ${item.loadRate/100})` : (item.loadRate>=74.5 ? `rgba(255, 225, 0, ${(item.loadRate+15)/100})` : (item.loadRate>=49.5 ? `rgba(0, 120, 215, ${(141-item.loadRate)/100})` : `rgba(22, 198, 12, ${(item.loadRate+60)/100})`))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        前门露点温度: {{item.temludianfront ? item.temludianfront.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%<br/>
                                        后门露点温度: {{item.temludianblack ? item.temludianblack.toFixed(1) : ''}}°C
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="item.loadRate != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.loadRate ? item.loadRate.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">%</div><span v-else style="font-size: 10px;">%</span></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 1 && item.runStatus==1" :style="{backgroundColor: item.cabinetName ? `rgba(41, 132, 71, ${item.loadRate/100+0.5})` : '#f5f7fa',color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        前门露点温度: {{item.temludianfront ? item.temludianfront.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%<br/>
                                        后门露点温度: {{item.temludianblack ? item.temludianblack.toFixed(1) : ''}}°C
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="item.powActive != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powActive ? item.powActive.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">kW</div><span v-else style="font-size: 10px;">kW</span></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 2 && item.runStatus != 0 && item.runStatus != 4" :style="{backgroundColor: item.cabinetName && item.powerFactor ? (item.powerFactor>=0.895 ? `#16c60c` : (item.powerFactor>=0.845 ? `#0078d7` : (item.powerFactor>=0.745 ? `rgba(252, 225, 0, ${1.75-item.powerFactor})` : `rgba(240, 58, 23, ${1.3-item.powerFactor})`))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        前门露点温度: {{item.temludianfront ? item.temludianfront.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%<br/>
                                        后门露点温度: {{item.temludianblack ? item.temludianblack.toFixed(1) : ''}}°C
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="item.powerFactor != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}</div>
                                </el-tooltip>
                              </template>
                            </div>
                            <!-- <div v-else-if="chosenBtn == 6 && item.runStatus != 0 && item.runStatus != 4" :style="{background: element.direction == '1' ? `linear-gradient(to bottom, ${getColorFromGradient(item.temFront)} 50%, ${getColorFromGradient(item.temBlack)} 50%)` : `linear-gradient(to right, ${getColorFromGradient(item.temFront)} 50%, ${getColorFromGradient(item.temBlack)} 50%)`,color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="item.temFront != 0 || item.temBlack != 0" :style="element.direction == '1' ? 'display: flex;flex-direction: column;justify-content: space-around;width: 100%' : 'display: flex;justify-content: space-around;width: 100%'">
                                    <div>{{item.temFront ? item.temFront.toFixed(1) : '0.0'}}</div>
                                    <div>{{item.temBlack ? item.temBlack.toFixed(1) : '0.0'}}</div>
                                  </div>
                                  </el-tooltip>
                              </template>
                            </div> -->
                            <div v-else-if="chosenBtn == 3 && item.runStatus != 0 && item.runStatus != 4" :style="{backgroundColor: item.cabinetName && item.temFront ? (item.temFront>=35 ? `rgba(232, 18, 36, ${item.temFront})` : (item.temFront>=30 ? `rgba(247, 99, 12, ${(item.temFront+65)/100})` : (item.temFront>=27 ? `rgba(252, 225, 0, ${(item.temFront+70)/100})` : (item.temFront>=24 ? `rgba(50, 205, 50, ${(124-item.temFront)/100})` : (item.temFront>20 ? `rgba(0, 128, 0, ${(item.temFront+76)/100})` : `rgba(0, 120, 215, ${item.temFront+80})`))))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        前门露点温度: {{item.temludianfront ? item.temludianfront.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%<br/>
                                        后门露点温度: {{item.temludianblack ? item.temludianblack.toFixed(1) : ''}}°C
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="item.temFront != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temFront ? item.temFront.toFixed(1) : '0.0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">°C</div><span v-else style="font-size: 10px;">°C</span></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else-if="chosenBtn == 4 && item.runStatus != 0 && item.runStatus != 4" :style="{backgroundColor: item.cabinetName && item.temBlack ? (item.temBlack>=45 ? `rgba(232, 18, 36, ${item.temBlack})` : (item.temBlack>=40 ? `rgba(247, 99, 12, ${(item.temBlack+55)/100})` : (item.temBlack>=35 ? `rgba(252, 225, 0, ${(item.temBlack+60)/100})` : (item.temBlack>30 ? `rgba(50, 205, 50, ${(130-item.temBlack)/100})` : `rgba(0, 128, 0, ${(item.temBlack+70)/100})`)))) : '#eef4fc',color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        前门露点温度: {{item.temludianfront ? item.temludianfront.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%<br/>
                                        后门露点温度: {{item.temludianblack ? item.temludianblack.toFixed(1) : ''}}°C
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="item.temBlack != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temBlack ? item.temBlack.toFixed(1) : '0.0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">°C</div><span v-else style="font-size: 10px;">°C</span></div>
                                </el-tooltip>
                              </template>
                            </div>
                            <div v-else :style="{backgroundColor: item.cabinetName ? statusColor[item.runStatus].color : '#f5f7fa',color: '#fff',height: '100%',width: '100%'}">
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
                                        总功率因素：{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}<br/>
                                        总有功功率：{{item.powActive ? item.powActive.toFixed(3) : '0.000'}}kW<br/>
                                        总视在功率：{{item.powApparent ? item.powApparent.toFixed(3) : '0.000'}}kVA<br/>
                                        总无功功率：{{item.powReactive ? item.powReactive.toFixed(3) : '0.000'}}kVar
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        A路功率：{{item.powActivea ? item.powActivea.toFixed(3) : '0.000'}}kW<br/>
                                        A路设备：{{item.cabinetkeya}}
                                      </div>
                                      <div style="width: 50%">
                                        B路功率：{{item.powActiveb ? item.powActiveb.toFixed(3) : '0.000'}}kW<br/>
                                        B路设备：{{item.cabinetkeyb}}
                                      </div>
                                    </div>
                                    <hr/>
                                    <div class="flex justify-between" style="width: 20vw">
                                      <div style="width: 50%">
                                        前门温度：{{item.temFront ? item.temFront.toFixed(1) : ''}}°C<br/>
                                        前门湿度：{{item.temFront ? item.humFront.toFixed(0) : ''}}%<br/>
                                        前门露点温度: {{item.temludianfront ? item.temludianfront.toFixed(1) : ''}}°C
                                      </div>
                                      <div style="width: 50%">
                                        后门温度：{{item.temBlack ? item.temBlack.toFixed(1) : ''}}°C<br/>
                                        后门湿度：{{item.temBlack ? item.humBlack.toFixed(0) : ''}}%<br/>
                                        后门露点温度: {{item.temludianblack ? item.temludianblack.toFixed(1) : ''}}°C
                                      </div>
                                    </div>
                                    <div v-if="item.alarmLogRecord" style="width: 20vw;word-wrap: break-word;overflow-wrap: break-word;">
                                      <hr/>
                                      告警类型：{{alarmTypeDesc[item.alarmLogRecord?.alarmType]}}<br/>
                                      告警描述：{{item.alarmLogRecord?.alarmDesc}}
                                    </div>
                                  </template>
                                  <div v-if="chosenBtn == 0 && item.runStatus != 0 && item.runStatus != 4 && item.loadRate != 0" :style="!isFromHome ? 'font-size: 20px;' : ''">{{item.loadRate ? item.loadRate.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">%</div><span v-else style="font-size: 10px;">%</span></div>
                                  <div v-if="chosenBtn == 1 && item.runStatus != 0 && item.runStatus != 4 && item.powActive != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powActive ? item.powActive.toFixed(0) : '0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">kW</div><span v-else style="font-size: 10px;">kW</span></div>
                                  <div v-if="chosenBtn == 2 && item.runStatus != 0 && item.runStatus != 4 && item.powerFactor != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.powerFactor ? item.powerFactor.toFixed(2) : '0.00'}}</div>
                                  <div v-if="chosenBtn == 3 && item.runStatus != 0 && item.runStatus != 4 && item.temFront != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temFront ? item.temFront.toFixed(1) : '0.0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">°C</div><span v-else style="font-size: 10px;">°C</span></div>
                                  <div v-if="chosenBtn == 4 && item.runStatus != 0 && item.runStatus != 4 && item.temBlack != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.temBlack ? item.temBlack.toFixed(1) : '0.0'}}<div v-if="element.direction == '1'" style="font-size: 10px;margin-top: -15px">°C</div><span v-else style="font-size: 10px;">°C</span></div>
                                  <!-- <div v-if="chosenBtn == 5 && item.runStatus != 0 && item.runStatus != 4 && item.yesterdayEq != 0" :style="!isFromHome ? 'font-size: 20px' : ''">{{item.yesterdayEq ? item.yesterdayEq.toFixed(0) : '0'}}<br/><div style="font-size: 10px;margin-top: -15px">kWh</div></div> -->
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
          <div ref="menuViewEle" class="menu" v-if="operateMenu.show" :style="{left: `${operateMenu.left}`, top: `${operateMenu.top}`}">
            <div class="menu_item" v-if="showMenuAdd" @click="dragTableView">小抓手</div>
            <div class="menu_item" v-if="showMenuAdd && editEnable" @click="addAisle">新增柜列</div>
            <div class="menu_item" v-if="showMenuAdd && editEnable" @click="addCabinet('add')">新增机柜</div>
            <!-- <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="editMachine">编辑</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="handleJump(false)">查看</div>
            <div class="menu_item" v-if="!showMenuAdd && editEnable" @click="deleteMachine">删除</div> -->
            <el-cascader-panel ref="areaIdsCascader" class="menu_item_panel" v-if="(!showMenuAdd || !editEnable) && menuOptions.length" style="width: fit-content;" model-value="['编辑']" :options="menuOptions" :props="{expandTrigger: 'hover'}" @change="handleMenu" @expand-change="expandChange" />
          </div>
        </div>
    </div>
  </el-card>
  <layoutForm ref="machineForm" @success="handleChange" />
<el-dialog v-model="dialogVisible" title="机房配置" width="50%" :before-close="handleDialogCancel">
      <el-form>
        <div style="margin-bottom: 5px">
          <el-text>机房</el-text>
        </div>
        <div class="double-formitem">
          <div style="margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="addrFlag" fill="#00778c">
              <el-radio-button :label="false">不分楼层</el-radio-button>
              <el-radio-button :label="true">分楼层</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="名称" label-width="80">
            <el-input v-model="rowColInfo.roomName" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="楼层" prop="type" label-width="80">
            <el-select v-model="rowColInfo.addr" placeholder="请选择" :disabled="!addrFlag">
              <el-option v-for="(addr_item,addr_index) in addrList.slice(1)" :key="addr_index" :label="addr_item" :value="addr_item" />
            </el-select>
          </el-form-item>
        </div>
        <div style="margin-bottom: 5px">
          <el-text>拓扑</el-text>
        </div>
        <div v-if="!rowColInfo.areaFlag" class="double-formitem">
          <div style="margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="rowColInfo.areaFlag" fill="#00778c">
              <el-radio-button :label="true">面积</el-radio-button>
              <el-radio-button :label="false">地砖</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="行数" label-width="80">
            <el-input type="number" v-model="rowColInfo.row" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
          <el-form-item label="列数" label-width="80">
            <el-input type="number" v-model="rowColInfo.col" :min="1" :max="100" controls-position="right" placeholder="请输入" />
          </el-form-item>
        </div>
        <div v-else class="double-formitem">
          <div style="margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="rowColInfo.areaFlag" fill="#00778c">
              <el-radio-button :label="true">面积</el-radio-button>
              <el-radio-button :label="false">地砖</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="宽度" label-width="80">
            <el-input type="number" v-model="rowColInfo.width" :min="1" :max="60" placeholder="请输入">
              <template #append>m</template>
            </el-input>
          </el-form-item>
          <el-form-item label="长度" label-width="80">
            <el-input type="number" v-model="rowColInfo.length" :min="1" :max="60" placeholder="请输入">
              <template #append>m</template>
            </el-input>
          </el-form-item>
        </div>
        
        <div style="margin-bottom: 5px">
          <el-text>负载率</el-text>
        </div>
        <!-- <el-form-item label="非IT设备总额定功率" label-width="160">
          <el-input v-model="rowColInfo.airPower" placeholder="包括制冷系统（如空调、冷源设备、新风系统等）">
            <template #append>kVA</template>
          </el-input>
        </el-form-item> -->
        <div class="double-formitem">
          <div style="margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="rowColInfo.displayFlag" fill="#00778c">
              <el-radio-button :label="false">不显示</el-radio-button>
              <el-radio-button :label="true">显示</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="电力容量" label-width="80">
            <el-input type="number" v-model="rowColInfo.powerCapacity" placeholder="请输入">
              <template #append>kVA</template>
            </el-input>
          </el-form-item>
        </div>

        

        <div style="margin-bottom: 5px">
          <el-text>排序</el-text>
        </div>
        <div class="double-formitem">
          <div style="margin-left: 60px;margin-bottom: 18px">
            <el-radio-group v-model="sortFlag" fill="#00778c">
              <el-radio-button :label="false">不启用</el-radio-button>
              <el-radio-button :label="true">启用</el-radio-button>
            </el-radio-group>
          </div>
          <el-form-item label="排序序号" label-width="80">
            <el-input type="number" v-model="rowColInfo.sort" placeholder="请输入"  :disabled="!sortFlag"/>
          </el-form-item>
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
        <el-button @click="handleDialogCancel" color="black" plain>取 消</el-button>
        <el-button type="primary" @click="submitSetting" color="black">确 定</el-button>
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
  <aisleRequire ref="aisleRequirement" />
  <aisleBalance ref="aisleBalanceDetail" />
  <aisleFactor ref="aisleFactorDetail" />
  <cabinetBalance ref="cabinetBalanceDetail" />
  <busRequire ref="busRequirement" />
  <busBalance ref="busBalanceDetail" />
  <boxRequire ref="boxRequirement" />
  <boxBalance ref="boxBalanceDetail" />
  <pduRequire ref="pduRequirement" />
  <pduBalance ref="pduBalanceDetail" />
  <MachineForm ref="machineFormCabinet" @success="saveMachine" :roomList="roomList" :roomId="roomId" />
<!-- </div> -->
</template>

<script lang="ts" setup>
import draggable from "vuedraggable";
import layoutForm from './component/layoutForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MachineRoomApi } from '@/api/cabinet/room'
import { CabinetApi } from '@/api/cabinet/info'
import { AisleEnergyApi } from '@/api/aisle/aisleenergy'
import { IndexApi } from '@/api/cabinet/index'
import MachineForm from './component/MachineForm.vue';
import aisleRequire from './component/aisleRequire.vue';
import aisleBalance from './component/aisleBalance.vue';
import cabinetBalance from './component/cabinetBalance.vue';
import busRequire from './component/busRequire.vue';
import busBalance from './component/busBalance.vue';
import boxRequire from './component/boxRequire.vue';
import boxBalance from './component/boxBalance.vue';
import pduRequire from './component/pduRequire.vue';
import pduBalance from './component/pduBalance.vue';
import download from '@/utils/download'
import PFDetail from './component/PFDetail.vue'
import { debounce } from 'lodash-es'

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

const addrFlag = ref(false)
const sortFlag = ref(false)

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
const roomId = ref(Number(query.id)) // 房间id
const roomList = ref<any[]>([]) // 左侧导航栏树结构列表
const dragTable = ref() // 可移动编辑表格
const dragTableViewEle = ref()
const menuViewEle = ref()
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
  areaFlag: true, //新建类型 砖数 面积
  width: 8.4, //宽度
  length: 10.8, //长度
  powerCapacity:0, //电力容量
  airPower: null, //空调额定功率
  displayType: 0, //0负载率 1PUE
  displayFlag: 0, // 显示选择
  eleAlarmDay: 0, // 日用能告警
  eleLimitDay: 1000, // 日用能限制
  eleAlarmMonth: 0, // 月用能告警
  eleLimitMonth: 1000, // 月用能限制
  sort: null,
})
const emit = defineEmits(['backData', 'getroomid']) // 定义 backData 事件，用于操作成功后的回调
const tableData = ref<Record<string, any[]>[]>([]);
const alarmTypeDesc = ref(['','PDU离线','PDU告警','PDU预警','母线告警','母线离线','机柜容量','机柜电力容量告警','机柜每日电量限额告警'])
const statusInfo = ref([[
  {
    name: '0%~50%',
    color: '#16c60c',
    value: 0,
  },
  {
    name: '50%~75%',
    color: '#0078d7',
    value: 1,
  },
  {
    name: '75%~90%',
    color: '#fce100',
    value: 2,
  },
  {
    name: '90%~100%+',
    color: '#f03a17',
    value: 3,
  }
],[
  {
    name: '空机柜',
    color: '#f5f7fa',
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
    name: '0~0.75',
    color: '#f03a17',
    value: 0,
  },
  {
    name: '0.75~0.84',
    color: '#fce100',
    value: 1,
  },
  {
    name: '0.85~0.89',
    color: '#0078d7',
    value: 2,
  },
  {
    name: '0.90~1',
    color: '#16c60c',
    value: 3,
  }
],[
  {
    name: '<=20°C',
    color: '#0078d7',
    value: 0,
  },
  {
    name: '20°C~24°C',
    color: '#008000',
    value: 1,
  },
  {
    name: '24°C~27°C',
    color: '#32cd32',
    value: 2,
  },
  {
    name: '27°C~30°C',
    color: '#fff100',
    value: 3,
  },
  {
    name: '30°C~35°C',
    color: '#f7630c',
    value: 4,
  },
  {
    name: '>35°C',
    color: '#e81224',
    value: 5,
  }
],[
  {
    name: '<=30°C',
    color: '#008000',
    value: 0,
  },
  {
    name: '30°C~35°C',
    color: '#32cd32',
    value: 1,
  },
  {
    name: '35°C~40°C',
    color: '#fff100',
    value: 2,
  },
  {
    name: '40°C~45°C',
    color: '#f7630c',
    value: 3,
  },
  {
    name: '>45°C',
    color: '#e81224',
    value: 4,
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
    name: '空机柜',
    color: '#f5f7fa',
    value: 5,
  },
])
const btns = [
  {
    value: 1,
    name: '有功功率',
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
  // {
  //   value: 6,
  //   name: '温度',
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
    "pduBox": false,
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
const areaIdsCascader = ref()
const startX = ref(0);
const startY = ref(0);
const scrollLeft = ref(0);
const scrollTop = ref(0);

const aisleRequirement = ref()
const aisleBalanceDetail = ref()
const aisleFactorDetail = ref()
const cabinetBalanceDetail = ref()
const busRequirement = ref()
const busBalanceDetail = ref()
const boxRequirement = ref()
const boxBalanceDetail = ref()
const pduRequirement = ref()
const pduBalanceDetail = ref()

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
    // console.log(moveBox)
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
        console.log(X + moveBox.amount,rowColInfo)
        if (X + moveBox.amount > rowColInfo.col) return false
        for(let i = 0;i < moveBox.amount;i++) {
          if((tableData.value[Y][formParam.value[X+i]].length && tableData.value[Y][formParam.value[X+i]][0].id != moveBox.id) || (tableData.value[Y+1][formParam.value[X+i]].length && tableData.value[Y+1][formParam.value[X+i]][0].id != moveBox.id)) {
            console.log((tableData.value[Y][formParam.value[X+i]].length && tableData.value[Y][formParam.value[X+i]][0].id != moveBox.id) || (tableData.value[Y+1][formParam.value[X+i]].length && tableData.value[Y+1][formParam.value[X+i]][0].id != moveBox.id))
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
  },
  {
    value: '母线 ',
    label: '母线 ',
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
        value: 'A路母线三相平衡',
        label: 'A路母线三相平衡',
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
      areaFlag: res.areaFlag, //新建类型 砖数 面积
      width: res.areayLength, //宽度
      length: res.areaxLength, //长度
      airPower:res.airPower,
      addr: res.addr,
      displayType: res.displayType, //0负载率 1PUE
      displayFlag: res.displayFlag, // 显示选择
      eleAlarmDay: res.eleAlarmDay,
      eleLimitDay: res.eleLimitDay,
      eleAlarmMonth: res.eleAlarmMonth,
      eleLimitMonth: res.eleLimitMonth,
      sort: res.sort,
    })
    console.log(rowColInfo)
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
        arr.push({...emptyObject,index: j+1,aisleId: item.id})
      }
      item.cabinetList && item.cabinetList.forEach(ele => {
        if(ele.index > 0) {
          arr.splice(ele.index - 1, 1, ele)
          canDelete = false
        }
      })
      item.cabinetList = arr
      if(item.xCoordinate && item.yCoordinate)
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
    if(!roomList.value?.length) {
      getRoomList()
    }
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
        arr.push({...emptyObject,index: j+1,aisleId: item.id})
      }
      item.cabinetList && item.cabinetList.forEach(ele => {
        if(ele.index > 0) {
          arr.splice(ele.index - 1, 1, ele)
          canDelete = false
        }
      })
      item.cabinetList = arr
      if(item.xCoordinate && item.yCoordinate)
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

const getColorFromGradient = (value) => {
    // 对输入值进行归一化处理
    const normalizedValue = value / 100;
    const clampedValue = Math.min(Math.max(normalizedValue, 0), 1);

    const startColor = {r: 244,g: 229,b: 162};
    const endColor = {r: 191,g: 68,b: 76};

    const r = Math.round(startColor.r + (endColor.r - startColor.r) * clampedValue);
    const g = Math.round(startColor.g + (endColor.g - startColor.g) * clampedValue);
    const b = Math.round(startColor.b + (endColor.b - startColor.b) * clampedValue);

    return `rgb(${r}, ${g}, ${b})`;
}

const getRoomStatus = async(res) => {
  if (!res) {
    res = await MachineRoomApi.getRoomDataNewDetail({id: roomId.value})
    emit('backData', res)
  }
 // //console.log('//////////', tableData.value)
}

let lastTime = 0;
const throttleDelay = 32;
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
  tableScaleValueStart.value = Math.min(Math.floor(containerHeight/(rowColInfo.row*4))/10,Math.floor(containerWidth/(rowColInfo.col*4))/10)
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
  tableScaleValue.value = tableScaleValueStart.value
  tableScaleWidth.value = tableScaleWidth.value
  tableScaleHeight.value = tableScaleHeight.value
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
    areaFlag: true, //新建类型 砖数 面积
    width: 8.4, //宽度
    length: 10.8, //长度
    powerCapacity:0,
    airPower:null,
    displayType: 0, //0负载率 1PUE
    displayFlag: 0, // 显示选择
    eleAlarmDay: 0, // 日用能告警
    eleLimitDay: 1000, // 日用能限制
    eleAlarmMonth: 0, // 月用能告警
    eleLimitMonth: 1000, // 月用能限制
    sort: null,
  })
  radio.value = "负载率"
  addrFlag.value = false
  sortFlag.value = false
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
    push({path: '/room/roommonitor/topology', query: { id:roomId.value}})
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
    areaFlag: updateCfgInfo.value.areaFlag,
    width: updateCfgInfo.value.areayLength ? updateCfgInfo.value.areayLength : 8.4,
    length: updateCfgInfo.value.areaxLength ? updateCfgInfo.value.areaxLength : 10.8,
    powerCapacity:updateCfgInfo.value.powerCapacity,
    addr: updateCfgInfo.value.addr,
    airPower:updateCfgInfo.value.airPower ? updateCfgInfo.value.airPower : 0,
    displayType: updateCfgInfo.value.displayType, //0负载率 1PUE
    displayFlag: updateCfgInfo.value.displayFlag,
    eleAlarmDay: updateCfgInfo.value.eleAlarmDay,
    eleLimitDay: updateCfgInfo.value.eleLimitDay,
    eleAlarmMonth: updateCfgInfo.value.eleAlarmMonth,
    eleLimitMonth: updateCfgInfo.value.eleLimitMonth,
  })
  radio.value = updateCfgInfo.value.displayType ? "PUE" : "负载率"
  addrFlag.value = updateCfgInfo.value.addr == '未区分' ? false : true
  sortFlag.value = updateCfgInfo.value.sort ? true : false
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

const mouseX = ref(0)
const mouseY = ref(0)
const rect = ref()


// 右击弹出菜单
const handleRightClick = (e) => {
  e.preventDefault()
  const currentId = e.target.id ? e.target.id : (e.target.parentNode.id ? e.target.parentNode.id :  ((e.target.parentNode.parentNode.id ? e.target.parentNode.parentNode.id :  (e.target.parentNode.parentNode.parentNode.id ? e.target.parentNode.parentNode.parentNode.id :  (e.target.parentNode.parentNode.parentNode.parentNode.id ? e.target.parentNode.parentNode.parentNode.parentNode.id :  e.target.parentNode.parentNode.parentNode.parentNode.parentNode.id)))))
  const cabinetIndex = e.target.dataset.index ? e.target.dataset.index : (e.target.parentNode.dataset.index ? e.target.parentNode.dataset.index :  ((e.target.parentNode.parentNode.dataset.index ? e.target.parentNode.parentNode.dataset.index :  (e.target.parentNode.parentNode.parentNode.dataset.index ? e.target.parentNode.parentNode.parentNode.dataset.index :  (e.target.parentNode.parentNode.parentNode.parentNode.dataset.index ? e.target.parentNode.parentNode.parentNode.parentNode.dataset.index :  e.target.parentNode.parentNode.parentNode.parentNode.parentNode.parentNode.dataset.index)))))
  const lndexX = currentId.split('-')[1]
  const lndexY = currentId.split('-')[0]
  if (!currentId) return
  console.log(tableData.value,tableData.value[lndexY][formParam.value[lndexX]],lndexY,formParam.value[lndexX])
  
  menuOptions.value = []
  menuOptions.value[0] = JSON.parse(JSON.stringify(menuOptionsCopy.value[0]));

  if(cabinetIndex >= 0) {
    console.log(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList,cabinetIndex)
    menuOptions.value[0].value = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex]

    menuOptions.value[1] = menuOptionsCopy.value[1]
    menuOptions.value[1].value = tableData.value[lndexY][formParam.value[lndexX]][0].id
    menuOptions.value[1].label = "柜列：" + tableData.value[lndexY][formParam.value[lndexX]][0].name

    menuOptions.value[2] = menuOptionsCopy.value[2]
    menuOptions.value[2].value = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex]
    menuOptions.value[2].label = "机柜：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetName

    console.log(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex])
    if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb) {
      menuOptions.value[3] = menuOptionsCopy.value[5]

      menuOptions.value[4] = menuOptionsCopy.value[6]
      menuOptions.value[4].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya

      menuOptions.value[5] = menuOptionsCopy.value[7]
      menuOptions.value[5].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb

      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyA: cabinetBoxes.boxKeyA.split("-")[0] + "-" + cabinetBoxes.boxKeyA.split("-")[1],
        devKeyB: cabinetBoxes.boxKeyB.split("-")[0] + "-" + cabinetBoxes.boxKeyB.split("-")[1]
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyA,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keya?.id
      }
      menuOptions.value[5].value = {
        devKey: cabinetBoxes.boxKeyB,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keyb?.id
      }
    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb) {
      menuOptions.value[3] = JSON.parse(JSON.stringify(menuOptionsCopy.value[5]))
      menuOptions.value[3]?.children?.splice(7,1)
      menuOptions.value[3]?.children?.splice(3,3)

      menuOptions.value[4] = menuOptionsCopy.value[6]
      menuOptions.value[4].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya

      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyA: cabinetBoxes.boxKeyA.split("-")[0] + "-" + cabinetBoxes.boxKeyA.split("-")[1],
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyA,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keya?.id
      }

    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya) {
      menuOptions.value[3] = JSON.parse(JSON.stringify(menuOptionsCopy.value[5]))
      menuOptions.value[3]?.children?.splice(6,1)
      menuOptions.value[3]?.children?.splice(0,3)

      menuOptions.value[4] = menuOptionsCopy.value[7]
      menuOptions.value[4].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb

      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyB: cabinetBoxes.boxKeyB.split("-")[0] + "-" + cabinetBoxes.boxKeyB.split("-")[1]
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyB,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keyb?.id
      }
    }

    if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[3]
      menuOptions.value[3].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya

      menuOptions.value[4] = menuOptionsCopy.value[4]
      menuOptions.value[4].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb
      
      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyA,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keya?.id
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.pduKeyB,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keyb?.id
      }
    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[3]
      menuOptions.value[3].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya
      
      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyA,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keya?.id
      }
    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[4]
      menuOptions.value[3].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetkeyb
      
      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyB,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].keyb?.id
      }
    }

    if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].id) {
      menuOptions.value[0]?.children?.splice(0,1)
    }
    if(!tableData.value[lndexY][formParam.value[lndexX]][0].cabinetList[cabinetIndex].id) {
      menuOptions.value.splice(2,1)
      menuOptions.value[0]?.children?.splice(1,3)
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

    if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb) {
      menuOptions.value[3] = menuOptionsCopy.value[5]

      menuOptions.value[4] = menuOptionsCopy.value[6]
      menuOptions.value[4].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya

      menuOptions.value[5] = menuOptionsCopy.value[7]
      menuOptions.value[5].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb

      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyA: cabinetBoxes.boxKeyA.split("-")[0] + "-" + cabinetBoxes.boxKeyA.split("-")[1],
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyA,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].keya?.id
      }
      menuOptions.value[5].value = {
        devKey: cabinetBoxes.boxKeyB,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].keyb?.id
      }
    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb) {
      menuOptions.value[3] = JSON.parse(JSON.stringify(menuOptionsCopy.value[5]))
      menuOptions.value[3]?.children?.splice(7,1)
      menuOptions.value[3]?.children?.splice(3,3)

      menuOptions.value[4] = menuOptionsCopy.value[6]
      menuOptions.value[4].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya

      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyA: cabinetBoxes.boxKeyA.split("-")[0] + "-" + cabinetBoxes.boxKeyA.split("-")[1],
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyA,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].keya?.id
      }

    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya) {
      menuOptions.value[3] = JSON.parse(JSON.stringify(menuOptionsCopy.value[5]))
      menuOptions.value[3]?.children?.splice(6,1)
      menuOptions.value[3]?.children?.splice(0,3)

      menuOptions.value[4] = menuOptionsCopy.value[7]
      menuOptions.value[4].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb

      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetBoxes
      menuOptions.value[3].value = {
        devKeyB: cabinetBoxes.boxKeyB.split("-")[0] + "-" + cabinetBoxes.boxKeyB.split("-")[1]
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.boxKeyB,
        boxId: tableData.value[lndexY][formParam.value[lndexX]][0].keyb?.id
      }
    }

    if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[3]
      menuOptions.value[3].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya

      menuOptions.value[4] = menuOptionsCopy.value[4]
      menuOptions.value[4].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb
      
      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyA,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].keya?.id
      }
      menuOptions.value[4].value = {
        devKey: cabinetBoxes.pduKeyB,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].keyb?.id
      }
    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[3]
      menuOptions.value[3].label = "A路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya
      
      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyA,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].keya?.id
      }
    } else if(tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus && !tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb && tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeya) {
      menuOptions.value[3] = menuOptionsCopy.value[4]
      menuOptions.value[3].label = "B路设备：" + tableData.value[lndexY][formParam.value[lndexX]][0].cabinetkeyb
      
      let cabinetBoxes = tableData.value[lndexY][formParam.value[lndexX]][0].cabinetPdus
      menuOptions.value[3].value = {
        devKey: cabinetBoxes.pduKeyB,
        pduId: tableData.value[lndexY][formParam.value[lndexX]][0].keyb?.id
      }
    }

    if(tableData.value[lndexY][formParam.value[lndexX]][0].id) {
      menuOptions.value[0]?.children?.splice(0,1)
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
  // for(let i = 0;i < rowColInfo.row-(menuViewEle+lndexY);i++) {
  //   if(+lndexX+1 == rowColInfo.col || (!itemId && (tableData.value[+lndexY+i][formParam.value[+lndexX]].length || tableData.value[+lndexY+i][formParam.value[+lndexX+1]].length)) || (tableData.value[+lndexY+i][formParam.value[+lndexX]].length && tableData.value[+lndexY+i][formParam.value[+lndexX]][0].id != itemId) || (tableData.value[+lndexY+i][formParam.value[+lndexX+1]].length && tableData.value[+lndexY+i][formParam.value[+lndexX+1]][0].id != itemId)) {
  //     maxY = i
  //     break
  //   }
  // }
  // //console.log(maxX,maxY)
  showMenuAdd.value = !(tableData.value[lndexY][formParam.value[lndexX]].length > 0)
  operateMenu.value.show = true
    // const firstMenuItem = document.querySelector('.el-cascader-node') as HTMLElement;
    // console.log(firstMenuItem,areaIdsCascader.value)
    // if (firstMenuItem) {
    //   firstMenuItem.classList.add('in-active-path') // 触发点击展开子菜单
    //   firstMenuItem.ariaExpanded = 'true';
    // }
  nextTick(() => {
    // 获取鼠标位置
    mouseX.value = e.clientX;
    mouseY.value = e.clientY;

    // 获取视口尺寸
    const viewportWidth = dragTableViewEle.value?.clientWidth ? dragTableViewEle.value.clientWidth : 0;
    const viewportHeight = dragTableViewEle.value?.clientHeight ? dragTableViewEle.value.clientHeight : 0;

    // 获取菜单尺寸
    const menuWidth = menuViewEle.value?.clientWidth ? menuViewEle.value.clientWidth : 0;
    const menuHeight = menuViewEle.value?.clientHeight ? menuViewEle.value.clientHeight : 0;

    const container = e.currentTarget;
    rect.value = container.getBoundingClientRect()
    let offsetX = mouseX.value - Math.ceil(rect.value.left) + 1
    let offsetY = mouseY.value - Math.ceil(rect.value.top) + 1

    // 如果菜单会超出视口右侧，则向左调整
    if (offsetX + menuWidth > viewportWidth) {
        offsetX = Math.max(0, offsetX - menuWidth);
    }
    
    // 如果菜单会超出视口底部，则向上调整
    if (offsetY + menuHeight > viewportHeight) {
        offsetY = Math.max(0, offsetY - menuHeight);
    }
    
    console.log(mouseX.value,mouseY.value,viewportWidth,viewportHeight,menuWidth,menuHeight,offsetX,offsetY)

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
  })
    
    
    //console.log('editEnable.value', editEnable.value)
}

const expandChange = () => {
  nextTick(() => {
    // 获取视口尺寸
    const viewportWidth = dragTableViewEle.value?.clientWidth ? dragTableViewEle.value.clientWidth : 0;
    const viewportHeight = dragTableViewEle.value?.clientHeight ? dragTableViewEle.value.clientHeight : 0;

    // 获取菜单尺寸
    const menuWidth = menuViewEle.value?.clientWidth ? menuViewEle.value.clientWidth : 0;
    const menuHeight = menuViewEle.value?.clientHeight ? menuViewEle.value.clientHeight : 0;
    
    let offsetX = mouseX.value - Math.ceil(rect.value.left) + 1
    let offsetY = mouseY.value - Math.ceil(rect.value.top) + 1

    // 如果菜单会超出视口右侧，则向左调整
    if (offsetX + menuWidth > viewportWidth) {
        offsetX = Math.max(0, offsetX - menuWidth);
    }
    
    // 如果菜单会超出视口底部，则向上调整
    if (offsetY + menuHeight > viewportHeight) {
        offsetY = Math.max(0, offsetY - menuHeight);
    }
    
    console.log(mouseX.value,mouseY.value,viewportWidth,viewportHeight,menuWidth,menuHeight,offsetX,offsetY)

    operateMenu.value = {
      left: offsetX + 'px',
      top: offsetY + 'px',
      show: true,
      lndexX: operateMenu.value.lndexX, // 当前列
      lndexY: operateMenu.value.lndexY, // 当前行
      maxlndexX: 26,
      maxlndexY: 26,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row
    }
  })
}

// const toClickSecondCascaderCollect = (index, number?) => {

//   const el = document.querySelectorAll(`.el-cascader-menu`)[number].querySelectorAll(`.el-cascader-node`)

//   if (el && el[index]) {

//     return new Promise((resolve) => {

//       el[index].click()   // 触发点击事件，展开传过来的index对应的节点

//       time.value = setTimeout(() => {

//         resolve()     // 延迟1秒执行，防止执行过快，防止上一级没有展示出来就点击而导致找不到click报错

//       }, 1000)

//     })

//   }

//   return Promise.resolve()

// }

const handleMenu = (value) => {
  //console.log(value)
  operateMenu.value.show = false
  
  if(value[0] == '小抓手') {
    dragTableView()
    return
  }
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
      aisleRequirement.value.open({ 
        id: value[0], 
        location: `${rowColInfo.roomName}-${tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0].name}`
      })
      // openDetailAisleRequire({ id: value[0] })
      break;
    
    case '柜列供电平衡':
      aisleBalanceDetail.value.open({id: value[0]})
      // push({ 
      //   path: '/aisle/aislemonitor/aislebalance', 
      //   query: { openDetailFlag: 1, id: value[0] } 
      // });
      break;

    case '柜列功率因数':
      aisleFactorDetail.value.open({ 
        id: value[0], 
        location: `${rowColInfo.roomName}-${tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0].name}`
      })
      // push({ 
      //   path: '/aisle/aislemonitor/aislepowerfactor', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     id: value[0], 
      //     location: `${rowColInfo.roomName}-${tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0].name}`
      //   } 
      // });
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
      cabinetBalanceDetail.value.open({
        id: value[0].id
      })
      // push({ 
      //   path: '/cabinet/cab/balance', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     id: value[0].id
      //   } 
      // });
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
          roomName: rowColInfo.roomName,
        } 
      });
      break;

    case 'A路母线需量':
      busRequirement.value.open({
        devKey: value[0].devKeyA
      })
      // push({ 
      //   path: '/bus/busmonitor/busmonitor/busrequirement', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     devKey: value[0].devKeyA
      //   } 
      // });
      break;

    case 'B路母线需量':
      console.log(value[0])
      busRequirement.value.open({
        devKey: value[0].devKeyB
      })
      // push({ 
      //   path: '/bus/busmonitor/busmonitor/busrequirement', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     devKey: value[0].devKeyB
      //   } 
      // });
      break;

    case 'A路插接箱需量':
    case 'B路插接箱需量':
      boxRequirement.value.open({
        boxId: value[0].boxId,
        location: rowColInfo.roomName
      })
      // push({ 
      //   path: '/bus/busmonitor/boxmonitor/boxrequirement', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     boxId: value[0].boxId,
      //     roomName: rowColInfo.roomName,
      //   } 
      // });
      break;

    case 'A路需量':
    case 'B路需量':
      pduRequirement.value.open({
        devKey: value[0].devKey,
        pduId: value[0].pduId,
        openDetailFlag: 1
      })
      // push({ 
      //   path: '/pdu/power/pdurequirement', 
      //   query: { 
      //     devKey: value[0].devKey,
      //     pduId: value[0].pduId,
      //     openDetailFlag: 1
      //   } 
      // });
      break;

    case 'A路母线三相平衡':
      busBalanceDetail.value.open({
        devKey: value[0].devKeyA
      })
      // push({ 
      //   path: '/bus/busmonitor/busmonitor/busbalance', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     devKey: value[0].devKeyA
      //   } 
      // });
      break;

    case 'B路母线三相平衡':
      busBalanceDetail.value.open({
        devKey: value[0].devKeyB
      })
      // push({ 
      //   path: '/bus/busmonitor/busmonitor/busbalance', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     devKey: value[0].devKeyB
      //   } 
      // });
      break;

    case 'A路插接箱供电平衡':
    case 'B路插接箱供电平衡':
      boxBalanceDetail.value.open({
        devKey: value[0].devKey,
        roomName: rowColInfo.roomName,
        boxId: value[0].boxId
      })
      // push({ 
      //   path: '/bus/busmonitor/boxmonitor/boxbalance', 
      //   query: { 
      //     openDetailFlag: 1, 
      //     devKey: value[0].devKey,
      //     roomName: rowColInfo.roomName,
      //     boxId: value[0].boxId
      //   } 
      // });
      break;

    case 'A路供电平衡':
    case 'B路供电平衡':
      pduBalanceDetail.value.open({
        devKey: value[0].devKey,
        pduId: value[0].pduId
      })
      // push({ 
      //   path: '/pdu/power/curbalance/index', 
      //   query: { 
      //     devKey: value[0].devKey,
      //     pduId: value[0].pduId,
      //     openDetailFlag: 1
      //   } 
      // });
      break;

    case 'A路设备管理':
    case 'B路设备管理':
    case 'A路插接箱设备管理':
    case 'B路插接箱设备管理':
      window.open('https://172.16.1.9/index.html', '_blank')
      break;

    case '新增机柜':
      addCabinet('add', value[0]);
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
const showMenuAdd = ref(false)
// 拖拽开始的事件
const onStart = ({from}) => {
  const X = from.id.split('-')[1]
  const Y = from.id.split('-')[0]
  const target = tableData.value[Y][formParam.value[X]][0]
  movingInfo.value = target
  console.log('onStssasrt', target)
}
// 拖拽结束的事件
const onEnd = ({from, to}) => {
 //console.log('onsEnd',tableData.value, from, to, from.id, to.id)
  if (from.id != to.id) { // 发生移动才处理
    const X = +to.id.split('-')[1]
    const Y = +to.id.split('-')[0]
    // const X1 = +from.id.split('-')[1]
    // const Y1 = +from.id.split('-')[0]
    const targetTo = tableData.value[Y][formParam.value[X]][0]
    // console.log('value*******', targetTo,X+1,Y+1)
    
    // if (targetTo.type == 1) {
    //   if (targetTo.direction == 1) {
    //     tableData.value[Y1+1][formParam.value[X1]] = []
    //     tableData.value[Y+1][formParam.value[X]] = [{...targetTo,first: false}]
    //   } else {
    //     tableData.value[Y1][formParam.value[X1+1]] = []
    //     tableData.value[Y][formParam.value[X+1]] = [{...targetTo,first: false}]
    //   }
    //   for (let i=  1; i < targetTo.amount; i++) {
    //     if (targetTo.direction == 1) {
    //       tableData.value[Y1][formParam.value[X1+i]] = []
    //       tableData.value[Y1+1][formParam.value[X1+i]] = []
    //       tableData.value[Y][formParam.value[X+i]] = [{...targetTo,first: false}]
    //       tableData.value[Y+1][formParam.value[X+i]] = [{...targetTo,first: false}]
    //     } else {
    //       tableData.value[Y1+i][formParam.value[X1]] = []
    //       tableData.value[Y1+i][formParam.value[X1+1]] = []
    //       tableData.value[Y+i][formParam.value[X]] = [{...targetTo,first: false}]
    //       tableData.value[Y+i][formParam.value[X+1]] = [{...targetTo,first: false}]
    //     }
    //   }
    // } else if (targetTo.type == 2) {
    //   tableData.value[Y1+1][formParam.value[X1]] = []
    //   tableData.value[Y+1][formParam.value[X]] = [{...targetTo,first: false}]
    // }
    //console.log('tableData.value*******', tableData.value)
    moveMachine(targetTo,X+1,Y+1)
  }
}
const moveMachine = async (data,x,y) => {
  let res
  if(data.type == 1) {
    let asileObject = {
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
    }

    const flagRes = await MachineRoomApi.findAddAisleVerify(asileObject)

    if(flagRes) {
      message.error("移动失败,可能原因如下：该柜列的位置的长度范围内有机柜或柜列,柜列同名,柜列超出机房长度范围")
      getRoomInfoNoLoading()
      return
    }

    res = await MachineRoomApi.saveRoomAisle(asileObject) 
  } else {
    let cabinetObject = {
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
    }

    res = await MachineRoomApi.saveRoomCabinet(cabinetObject)
  }
  if(res) {
    message.success("移动成功")
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
  if (cabItem) {
    machineFormCabinet.value.open(type, cabItem,operateMenu.value,tableData.value[operateMenu.value.lndexY][formParam.value[operateMenu.value.lndexX]][0])
  } else if (type == 'add') {
    machineFormCabinet.value.open(type, null,operateMenu.value,null)
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
      const aisleRes = await AisleEnergyApi.deleteAisle({id: target.id})
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
          eleLimitMonth:data.eleLimitMonth,
          cabinetFirstVO: data.cabinetFirstVO,
          pduBar: data.pduBar
      }

      // console.log(data)

      // if(data.type == 2) {
      //   data.amount = 1
      // }
      // if (data.direction == 2) {
      //   const X = data.xCoordinate-1
      //   const Y = data.yCoordinate-1
      //   if (Y + data.amount > rowColInfo.row) return false
      //   for(let i = 0;i < data.amount;i++) {
      //     if((tableData.value[Y+i][formParam.value[X]].length && tableData.value[Y+i][formParam.value[X]][0].id != data.id) || (tableData.value[Y+i][formParam.value[X+1]].length && tableData.value[Y+i][formParam.value[X+1]][0].id != data.id)) {
      //       console.log(false)
      //       return
      //     }
      //   }
      // } else {
      //   const X = data.xCoordinate-1
      //   const Y = data.yCoordinate-1
      //   console.log(X + data.amount,rowColInfo)
      //   if (X + data.amount > rowColInfo.col) return false
      //   for(let i = 0;i < data.amount;i++) {
      //     if((tableData.value[Y][formParam.value[X+i]].length && tableData.value[Y][formParam.value[X+i]][0].id != data.id) || (tableData.value[Y+1][formParam.value[X+i]].length && tableData.value[Y+1][formParam.value[X+i]][0].id != data.id)) {
      //       console.log(false)
      //       return
      //     }
      //   }
      // }

      // console.log(true)

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
const importLoading = ref(false) // 导入的加载中
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

const fileRaw = ref(null);

// 文件选择回调
const handleFileChange = async (file) => {
  fileRaw.value = file.raw;  // 保存文件对象
  console.log(fileRaw.value)
  await message.exportConfirm("是否确认导入数数据项？");
  handleImportAisle()
};


const handleImportAisle = async () => {
  if (!fileRaw.value) {
    ElMessage.warning('请先选择excel文件');
    return;
  }

  const formData = new FormData();
  formData.append('file', fileRaw.value);  // 文件转为二进制流

  console.log(formData)

  try {
    importLoading.value = true
    const data = await MachineRoomApi.importAisleExcel(formData);
    ElMessage.success('导入成功');
  } catch (error) {
    ElMessage.error('导入失败');
  } finally {
    importLoading.value = false
  }
}

const handleExportAisle = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm();
    // 发起导出
    exportLoading.value = true;
    const axiosConfig = {
      timeout: 0 // 设置超时时间为0
    }
    const data = await MachineRoomApi.exportAisleExcel({roomId: roomId.value},axiosConfig);
    console.log("data",data);
    await download.excel(data, rowColInfo?.roomName + '的机柜绑定关系表.xlsx');
    ElMessage.success('导出成功');
  } catch (error) {
    ElMessage.success('导出失败');
    console.error('导出失败：', error);
  } finally {
    exportLoading.value = false;
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
  } else if(rowColInfo.powerCapacity === "") {
    message.error('机房电力容量不能为空,请输入!')
    return
  }

  if(roomFlag.value == 1){
    const resSelect = await MachineRoomApi.selectRoomByName({name: rowColInfo.roomName});
    if(resSelect != null){
      message.error('该机房名称已存在,请重新输入!');
      rowColInfo.roomName = '';
      return
    }
  }
  

  let roomFlagId:any = null;
  let messageRoomFlag = "保存成功！";
  if(roomFlag.value == 2){
    roomFlagId = roomId.value; 
    messageRoomFlag = "修改成功！";
  }

  if(radio.value === "PUE") {
  rowColInfo.displayType = 1
  } else {
    rowColInfo.displayType = 0
  }

  if(!sortFlag.value) {
    rowColInfo.sort = null
  }
  if(!addrFlag.value) {
    rowColInfo.addr = '未区分'
  }

  console.log(rowColInfo)
    if(rowColInfo.width <= 0 || rowColInfo.length <= 0 || rowColInfo.width > 60 || rowColInfo.length > 60) {
      message.error('机房面积有误或过大,请重新输入!')
      return
    }

    if(rowColInfo.areaFlag == false) {
      rowColInfo.width = Number((rowColInfo.row * 0.6).toFixed(1))
      rowColInfo.length = Number((rowColInfo.col * 0.6).toFixed(1))
    } else {
      rowColInfo.width = Number(rowColInfo.width)
      rowColInfo.length = Number(rowColInfo.length)
      rowColInfo.row = Math.ceil((rowColInfo.width * 10)/6)
      rowColInfo.col = Math.ceil((rowColInfo.length * 10)/6)
    }

    console.log(rowColInfo,roomFlagId)

  if(roomFlag.value == 2) {
    const resFind = await MachineRoomApi.findAreaById({xLength: rowColInfo.col,yLength: rowColInfo.row,id: roomFlagId});
    if(resFind) {
      message.error('减少的行数或列数中有机柜或柜列,请重新设置行数或列数!')
      return
    }
  }

   try {
    const res = await MachineRoomApi.saveRoomDetail({
      id: roomFlagId,
      roomName: rowColInfo.roomName,
      addr: rowColInfo.addr,
      xLength: rowColInfo.col,
      yLength: rowColInfo.row,
      areaFlag: rowColInfo.areaFlag, //新建类型 砖数 面积
      areayLength: rowColInfo.width, //宽度
      areaxLength: rowColInfo.length, //长度
      powerCapacity:rowColInfo.powerCapacity, 
      airPower:rowColInfo.airPower, 
      displayType: rowColInfo.displayType, 
      displayFlag: rowColInfo.displayFlag, 
      eleAlarmDay: rowColInfo.eleAlarmDay,
      eleAlarmMonth: rowColInfo.eleAlarmMonth,
      eleLimitDay: rowColInfo.eleLimitDay,
      eleLimitMonth: rowColInfo.eleLimitMonth,
      sort:rowColInfo.sort,
    })
    
    if(res != null || res != "")
    message.success(messageRoomFlag);
    dialogVisible.value = false;
    roomId.value = res;
   } catch (error) {
    console.log(error)
   }
   
   getRoomInfoNoLoading();
}

watch(() => rowColInfo.areaFlag, (val) => {
  if(val) {
    rowColInfo.width = Number((rowColInfo.row * 0.6).toFixed(1))
    rowColInfo.length = Number((rowColInfo.col * 0.6).toFixed(1))
  } else if(rowColInfo.width && rowColInfo.length) {
    rowColInfo.width = Number(rowColInfo.width)
    rowColInfo.length = Number(rowColInfo.length)
    rowColInfo.row = Math.ceil((rowColInfo.width * 10)/6)
    rowColInfo.col = Math.ceil((rowColInfo.length * 10)/6)
  }
})

watch(() => operateMenu.value.show, (val) => {
  const tableBodyWrapper = dragTable.value.$el.querySelector('.el-scrollbar__wrap')
  if (!tableBodyWrapper) {
    return
  }

  if(val) {
    tableBodyWrapper.addEventListener('scroll', handleScroll)
  } else {
    tableBodyWrapper.removeEventListener('scroll',handleScroll)
  }
})

const handleScroll = debounce(() => {
  operateMenu.value.show = false;
}, 30);

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
        sort:rowColInfo.sort,
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
.temStatus {
  width: 10vw;
  height: 100%;
}
.status {
  font-size: 12px;
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
      background-color: rgba(0, 119, 140, 0.1);
      color: rgba(0, 119, 140, 1);
      font-weight: 700;
      cursor: pointer;
    }
  }
  .dragTd {
    width: 100%;
    height: 100%;
    .dragChild {
      width: 100%;
      height: 100%;
      min-height: 29px;
      box-sizing: border-box;
      display: flex;
      border: 1px solid #000;
      &:hover {
        border: 1px solid #007BFF;
      }
      // align-items: center;
      .dragSon {
        min-height: 29px;
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        box-sizing: border-box;
        background-color: #f5f7fa;
        border-right: 1px solid #bed1ff;
        &>div {
          min-height: 29px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .fill {
        background-color: #f5f7fa;
      }
    }
    .dragChildCol {
      width: 100%;
      height: 100%;
      min-height: 29px;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      border: 1px solid #000;
      &:hover {
        border: 1px solid #007BFF;
      }
      .dragSon {
        flex: 1;
        min-height: 29px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #f5f7fa;
        border-bottom: 1px solid #bed1ff;
        &>div {
          min-height: 29px;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }
      .fill {
        background-color: #f5f7fa;
      }
    }
  }
  .warnDrag {
    min-height: 29px;
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    // height: 40px;
    background-color: rgb(255, 219, 12);
  }
  .normalDrag {
    min-height: 29px;
    height: 100%;
    width: 100%;
    // height: 40px;
    background-color: #f5f7fa;
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
      min-height: 29px;
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
:deep(.el-radio-button__inner) {
  width: 5vw;
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
  min-height: 29px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 30px;
  padding: 0;
  & > div {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

//:deep(.dragTable .el-table__header .el-table__cell) {
  // background-color: #ddd;
  // box-shadow: 0 1px 0px #ddd;
//}

:deep(.dragTable .el-table__body) {
  height: 100%;
  transform-origin: let top;
}

:deep(.dragTable .el-table__body .el-table__row .el-table__cell:nth-of-type(1)) {
  // background-color: #ddd;
  // box-shadow: 0 1px 0px #ddd;
}

:deep(.el-cascader-node:not(.is-disabled):focus, .el-cascader-node:not(.is-disabled):hover) {
  background: rgba(0, 119, 140, 0.1);
}

:deep(.el-cascader-node.in-active-path, .el-cascader-node.is-active, .el-cascader-node.is-selectable.in-checked-path) {
  color: rgba(0, 119, 140, 1);
}

.crosshair {
  cursor: crosshair; /* 当正在拖动时显示十字图标 */
}
.tagInDialog{
  position: absolute;
  left: 30%;
  top: 22px;
}
</style>
