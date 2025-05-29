<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="false"  :lazy="true" :load="loadNode" navTitle="插接箱报表">
    <template #NavInfo>
      <div >
         <br/>
      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="120px"
        style="float: left;"
      >      
        <!-- 1<el-form-item label="网络地址" prop="devKey">
          <el-input
            v-model="queryParams.devKey"
            placeholder="请输入网络地址"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item> -->

    <el-form-item label="时间段" prop="createTime" label-width="60px">
          <el-button :color="switchValue == 0 ? '#00778c' : ''"
            @click="queryParams.timeType = 0;now = new Date();now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;switchValue = 0;handleDayPick();handleQuery()" 
            :type="switchValue === 0 ? 'primary' : ''"
          >
            日报
          </el-button>
          <el-button :color="switchValue == 1 ? '#00778c' : ''"
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;switchValue = 1;handleMonthPick();handleQuery()" 
            :type="switchValue === 1 ? 'primary' : ''"
          >
            月报
          </el-button>
          <el-button :color="switchValue == 2 ? '#00778c' : ''"
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;switchValue = 2;" 
            :type="switchValue === 2 ? 'primary' : ''"
          >
            自定义
          </el-button>
          
          
        </el-form-item>
         <el-form-item>
          <el-date-picker
            v-if="switchValue == 0"
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="date"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-160px"
          />
          <el-date-picker
            v-if="switchValue == 1"
            v-model="queryParams.oldTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="month"
            :disabled-date="disabledDate"
            @change="handleMonthPick"
            class="!w-160px"
          />
          <el-date-picker
            v-if="switchValue == 2"
            v-model="queryParams.timeArr"
            value-format="YYYY-MM-DD HH:mm:ss"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :disabled-date="disabledDate"
            @change="handleDayPick"
            class="!w-200px"
          />
        </el-form-item>
<el-form-item>
          <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        </el-form-item>
            <el-form-item>
          <el-select v-model="queryParams.dataType" placeholder="请选择" style="width: 100px">
            <el-option label="最大" :value="1" />
            <el-option label="平均" :value="0" />
            <el-option label="最小" :value="-1" />
          </el-select>
        </el-form-item>
        <el-form-item label="网络地址" prop="devKey">
          <el-autocomplete
            v-model="queryParams.devKey"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-200px"
            placeholder="请输入网络地址"
            @select="handleQuery"
          />
        </el-form-item>

    
       
        
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
  
    </template>
    <template #Content>
      <div v-show="visControll.visAllReport" class="page" >
        <div class="page-con">
          <div class="pageBox" >
            <div class="page-conTitle">
              插接箱基本信息
            </div>
            <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="PDUTableData" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="基本信息" >
                      <el-table-column  align="center" label="基本信息"  prop="baseInfoName" />
                      <el-table-column  prop="baseInfoValue" >
                        <template #default="scope">
                          <span v-if="scope.$index === 2">
                            <el-tag  v-if="scope.row.baseInfoValue == 1">正常</el-tag>
                            <el-tag type="warning" v-if="scope.row.baseInfoValue == 2">告警</el-tag>
                            <el-popover
                                placement="top-start"
                                title="告警内容"
                                :width="500"
                                trigger="hover"
                                :content="scope.row.pduAlarm"
                                v-if="scope.row.baseInfoValue == 2"
                              >
                                <template #reference>
                                  <el-tag type="danger">告警</el-tag>
                                </template>
                              </el-popover>
                            <el-tag type="info" v-if="scope.row.baseInfoValue == 0">离线</el-tag>
                          </span>
                          <span v-else>{{ scope.row.baseInfoValue }}</span>
                        </template>
                      </el-table-column>
                    </el-table-column>
                                <el-table-column align="center" label="当前状态">
            <el-table-column prop="statusInfoName" />
            <el-table-column prop="statusInfoValue">
              <template #default="scope">
                {{ scope.row.statusInfoValue || '--' }}
              </template>
            </el-table-column>
          </el-table-column> 
                     <el-table-column align="center" label="能耗">
            <el-table-column prop="consumeName" />
            <el-table-column prop="consumeValue">
              <template #default="scope">
                {{ scope.row.consumeValue || '--' }}
              </template>
            </el-table-column>
          </el-table-column> 
                                 <el-table-column align="center" label="不平衡度">
            <el-table-column prop="unbalanceName" />
            <el-table-column prop="unbalanceValue">
              <template #default="scope">
                {{ scope.row.unbalanceValue || '--' }}
              </template>
            </el-table-column>
          </el-table-column>  
                  </el-table>
                </div>
              </el-col>
              <el-col v-if="serChartContainerWidth == 10" :span="serChartContainerWidth">
                <!-- <Rad2ar width="29vw" height="25vh" :list="serverData" /> -->
                <div>
                 <div ref="serChartContainer" id="serChartContainer" style="width: 60vh; height: 25vh"></div>
               </div>
              </el-col>
            </el-row>
          </div>
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <p v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，{{eqData.maxEle == 0 ? '用电量' + eqData.maxEle : '最大单日用电量' + eqData.maxEle}}kWh， （发生时间{{eqData.maxEleTime}}）</p>
            <!-- <p v-if="visControll.isSameDay">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p> -->
            <p v-if="visControll.isSameDay">本周期内，共计使用电量{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh，最大用电量{{eqData.maxEle}}kWh， （发生时间{{eqData.maxEleTime}}）</p>

            <Bar class="Container" width="70vw" height="58vh" :list="eleList"/>
          </div>
          <!-- <div class="pageBox"  v-if="visControll.pfVis">
            <div class="page-conTitle">
              功率因素曲线
            </div>        
            <PFLine class="Container"  width="70vw" height="58vh" :list="pfLineList"/>
          </div> -->


                 <div class="pageBox"  v-if="visControll.pfVis">
            <div class="page-conTitle">
              功率因素曲线
            </div>
            <div v-for="(sensor, index) in pfLineList?.series" :key="index">
        <div class="power-section single-line" v-if="index %2 == 0">
        <span class="power-title" v-if="pfLineData[`pName${index+1}`]!=null">{{pfLineData[`pName${index+1}`]}}极值：</span>
        <span class="power-value" v-if="pfLineData[`pName${index+1}`]!=null">峰值 <span class="highlight">{{ pfLineData[`powFactorMax${index + 1}`] }}</span>  <span class="time">记录于({{ pfLineData[`powFactorMaxTime${index + 1}`] }})</span></span>
        <span class="power-value" v-if="pfLineData[`pName${index+1}`]!=null">谷值 <span class="highlight">{{ pfLineData[`powFactorMin${index + 1}`] }}</span>  <span class="time">记录于({{ pfLineData[`powFactorMinTime${index + 1}`] }})</span></span>
        <span  class="separator" v-if="pfLineData[`pName${index+1}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="pfLineData[`pName${index+2}`]!=null">{{pfLineData[`pName${index+2}`]}}极值：</span>
        <span class="power-value" v-if="pfLineData[`pName${index+2}`]!=null">峰值 <span class="highlight">{{ pfLineData[`powFactorMax${index + 2}`] }}</span>  <span class="time">记录于({{ pfLineData[`powFactorMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="pfLineData[`pName${index+2}`]!=null">谷值 <span class="highlight">{{ pfLineData[`powFactorMin${index + 2}`] }}</span>  <span class="time">记录于({{ pfLineData[`powFactorMinTime${index + 2}`] }})</span></span>
      </div>
            </div>        
            <PFLine class="Container"  :width="computedWidth" height="58vh" :list="pfLineList"/>
          </div>

          <!-- <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              平均功率曲线
            </div>
            <p>本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}KW， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kW， 发生时间{{powData.activePowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大无功功率{{powData.reactivePowMaxValue}}kVar， 发生时间{{powData.reactivePowMaxTime}}。最无功功率{{powData.reactivePowMinValue}}kVar， 发生时间{{powData.reactivePowMinTime}}</p>
            <Line class="Container"  width="70vw" height="58vh" :list="totalLineList"/>
          </div> -->
          <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              功率曲线
            </div>
            <div class="power-section single-line">
  <span class="power-title">视在功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{powData.apparentPowMaxValue}}</span> kVA <span class="time">记录于({{powData.apparentPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{powData.apparentPowMinValue}}</span> kVA <span class="time">记录于({{powData.apparentPowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">有功功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{powData.activePowMaxValue}}</span> kW，<span class="time">记录于 ({{powData.activePowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{powData.activePowMinValue}}</span> kW，<span class="time">记录于 ({{powData.activePowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">无功功率极值</span>
  <span class="power-value">峰值 <span class="highlight">{{powData.reactivePowMaxValue}}</span> kVar，<span class="time">记录于 ({{powData.reactivePowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{powData.reactivePowMinValue}}</span> kVar，<span class="time">记录于 ({{powData.reactivePowMinTime}})</span></span>
</div>
            <!-- <p>本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kW， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kW， 发生时间{{powData.activePowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大无功功率{{powData.reactivePowMaxValue}}kVar， 发生时间{{powData.reactivePowMaxTime}}。最小无功功率{{powData.reactivePowMinValue}}kVar， 发生时间{{powData.reactivePowMinTime}}</p> -->
            <Line class="Container"  :width="computedWidth" height="58vh" :list="totalLineList"/>
          </div>

        </div>
          <div class="pageBox" v-if="visControll.curVolVis">
            <div class="page-conTitle">
              相电流历史曲线趋势图
            </div>
            
          <div v-for="(sensor, index) in curList?.series" :key="index">
            <div class="power-section single-line" v-if="index %2 == 0">
              <span class="power-title">{{curVolData[`curName${index + 1}`]}}</span>
  <span class="power-value">峰值 <span class="highlight">{{curVolData[`curMaxValue${index + 1}`]}}</span> A <span class="time">记录于({{curVolData[`curMaxTime${index + 1}`]}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{curVolData[`curMinValue${index + 1}`]}}</span> A <span class="time">记录于({{curVolData[`curMinTime${index + 1}`]}})</span></span>
  <span class="power-title" v-if="index+2<=curList?.series.length-1 "> {{curVolData[`curName${index + 2}`]}}</span>
  <span class="power-value" v-if="index+2<=curList?.series.length-1 ">峰值 <span class="highlight">{{curVolData[`curMaxValue${index + 2}`]}}</span> A <span class="time">记录于({{curVolData[`curMaxTime${index + 2}`]}})</span></span>
  <span class="power-value" v-if="index+2<=curList?.series.length-1 ">谷值 <span class="highlight">{{curVolData[`curMinValue${index + 2}`]}}</span> A <span class="time">记录于({{curVolData[`curMinTime${index + 2}`]}})</span></span>
</div>
          </div>

            <CurLine :width="computedWidth" height="58vh" :list="curList" :dataType="queryParams.dataType"/>
          </div>

          <div class="pageBox" v-if="visControll.curVolVis">
            <div class="page-conTitle">
              相电压历史曲线趋势图
            </div>
            <div v-for="(sensor, index) in volList?.series" :key="index">
            <div class="power-section single-line" v-if="index %2 == 0">
              <span class="power-title">{{curVolData[`volName${index + 1}`]}}</span>
  <span class="power-value">峰值 <span class="highlight">{{curVolData[`volMaxValue${index + 1}`]}}</span> V <span class="time">记录于({{curVolData[`volMaxTime${index + 1}`]}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{curVolData[`volMinValue${index + 1}`]}}</span> V <span class="time">记录于({{curVolData[`volMinTime${index + 1}`]}})</span></span>
  <span class="power-title" v-if="index+2<=volList?.series.length-1 ">{{curVolData[`volName${index + 2}`]}}</span>
  <span class="power-value" v-if="index+2<=volList?.series.length-1 ">峰值 <span class="highlight">{{curVolData[`volMaxValue${index + 2}`]}}</span> V <span class="time">记录于({{curVolData[`volMaxTime${index + 2}`]}})</span></span>
  <span class="power-value" v-if="index+2<=volList?.series.length-1 ">谷值 <span class="highlight">{{curVolData[`volMinValue${index + 2}`]}}</span> V <span class="time">记录于({{curVolData[`volMinTime${index + 2}`]}})</span></span>
</div>

          </div>
            <!--<div ref="lineidChartContainerOne" id="lineidChartContainerOne" class="adaptiveStyle"></div>-->
            <VolLine :width="computedWidth" height="58vh" :list="volList" :dataType="queryParams.dataType"/>
          </div>


          <div class="pageBox" v-if="visControll.curLoopVis">
            <div class="page-conTitle">
              回路电流曲线
            </div>
                     <div v-for="(sensor, index) in curLoopList?.series" :key="index">
        <div class="power-section single-line" v-if="index %2 == 0">
        <span class="power-title" v-if="curvolLoopData[`CPName${index+1}`]!=null">{{curvolLoopData[`CPName${index+1}`]}}极值：</span>
        <span class="power-value" v-if="curvolLoopData[`CPName${index+1}`]!=null">峰值 <span class="highlight">{{ curvolLoopData[`loopCurMax${index + 1}`] }}</span> A <span class="time">记录于({{ curvolLoopData[`loopCurMaxTime${index + 1}`] }})</span></span>
        <span class="power-value" v-if="curvolLoopData[`CPName${index+1}`]!=null">谷值 <span class="highlight">{{ curvolLoopData[`loopCurMin${index + 1}`] }}</span> A <span class="time">记录于({{ curvolLoopData[`loopCurMinTime${index + 1}`] }})</span></span>
        <span  class="separator" v-if="curvolLoopData[`CPName${index+1}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="curvolLoopData[`CPName${index+2}`]!=null">{{curvolLoopData[`CPName${index+2}`]}}极值：</span>
        <span class="power-value" v-if="curvolLoopData[`CPName${index+2}`]!=null">峰值 <span class="highlight">{{ curvolLoopData[`loopCurMax${index + 2}`] }}</span> A <span class="time">记录于({{ curvolLoopData[`loopCurMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="curvolLoopData[`CPName${index+2}`]!=null">谷值 <span class="highlight">{{ curvolLoopData[`loopCurMin${index + 2}`] }}</span> A <span class="time">记录于({{ curvolLoopData[`loopCurMinTime${index + 2}`] }})</span></span>
      </div>
            </div>  
            <LoopCurLine :width="computedWidth" height="58vh" :list="curLoopList" :dataType="queryParams.dataType"/>
          </div>

          <div class="pageBox" v-if="visControll.volLoopVis">
            <div class="page-conTitle">
              回路电压曲线
            </div>
                                 <div v-for="(sensor, index) in volLoopList?.series" :key="index">
        <div class="power-section single-line" v-if="index %2 == 0">
        <span class="power-title" v-if="curvolLoopData[`VPName${index+1}`]!=null">{{curvolLoopData[`VPName${index+1}`]}}极值：</span>
        <span class="power-value" v-if="curvolLoopData[`VPName${index+1}`]!=null">峰值 <span class="highlight">{{ curvolLoopData[`loopVolMax${index + 1}`] }}</span> V <span class="time">记录于({{ curvolLoopData[`loopVolMaxTime${index + 1}`] }})</span></span>
        <span class="power-value" v-if="curvolLoopData[`VPName${index+1}`]!=null">谷值 <span class="highlight">{{ curvolLoopData[`loopVolMin${index + 1}`] }}</span> V <span class="time">记录于({{ curvolLoopData[`loopVolMinTime${index + 1}`] }})</span></span>
        <span  class="separator" v-if="curvolLoopData[`VPName${index+1}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="curvolLoopData[`VPName${index+2}`]!=null">{{curvolLoopData[`VPName${index+2}`]}}极值：</span>
        <span class="power-value" v-if="curvolLoopData[`CPName${index+2}`]!=null">峰值 <span class="highlight">{{ curvolLoopData[`loopVolMax${index + 2}`] }}</span> V <span class="time">记录于({{ curvolLoopData[`loopVolMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="curvolLoopData[`VPName${index+2}`]!=null">谷值 <span class="highlight">{{ curvolLoopData[`loopVolMin${index + 2}`] }}</span> V <span class="time">记录于({{ curvolLoopData[`loopVolMinTime${index + 2}`] }})</span></span>
      </div>
            </div>  
            <LoopVolLine :width="computedWidth" height="58vh" :list="volLoopList" :dataType="queryParams.dataType"/>
          </div>

          <div class="pageBox" v-if="visControll.temVis">
            <div class="page-conTitle">
              温度曲线
            </div>
            <div class="power-section single-line">
        <span class="power-title">A相温度极值：</span>
        <span class="power-value">峰值 <span class="highlight">{{ temData.temAMaxValue }}</span> °C <span class="time">记录于({{ temData.temAMaxTime }})</span></span>
        <span class="power-value">谷值 <span class="highlight">{{ temData.temAMinValue }}</span> °C <span class="time">记录于({{ temData.temAMinTime }})</span></span>
        
        <span class="power-title">B相温度极值：</span>
        <span class="power-value">峰值 <span class="highlight">{{ temData.temBMaxValue }}</span> °C <span class="time">记录于({{ temData.temBMaxTime }})</span></span>
        <span class="power-value">谷值 <span class="highlight">{{ temData.temBMinValue }}</span> °C <span class="time">记录于({{ temData.temBMinTime }})</span></span>
      </div>
      <div class="power-section single-line">
        <span class="power-title">C相温度极值：</span>
        <span class="power-value">峰值 <span class="highlight">{{ temData.temCMaxValue }}</span> °C <span class="time">记录于({{ temData.temCMaxTime }})</span></span>
        <span class="power-value">谷值 <span class="highlight">{{ temData.temCMinValue }}</span> °C <span class="time">记录于({{ temData.temCMinTime }})</span></span>
        
        <span class="power-title">D相温度极值：</span>
        <span class="power-value">峰值 <span class="highlight">{{ temData.temNMaxValue }}</span> °C <span class="time">记录于({{ temData.temNMaxTime }})</span></span>
        <span class="power-value">谷值 <span class="highlight">{{ temData.temNMinValue }}</span> °C <span class="time">记录于({{ temData.temNMinTime }})</span></span>
      </div>      
            <EnvTemLine  width="70vw" height="58vh" :list="temList"  :dataType="queryParams.dataType"/>
          </div>

          <div class="pageBox" v-if="visControll.outletEleVis">
            <div class="page-conTitle">
              输出位电量消耗
            </div>
                <div class="power-section single-line">
        <span class="power-title" v-if="outletList[`pName${1}`]!=null">{{outletList[`pName${1}`]}}：</span>
        <span class="power-value" v-if="outletList[`pName${1}`]!=null">峰值 <span class="highlight">{{ outletList[`outLetMax${1}`].toFixed(3) }}</span> kWh <span class="time">记录于({{ outletList[`outLetMaxTime${1}`] }})</span></span>
        <span class="power-value" v-if="outletList[`pName${1}`]!=null">谷值 <span class="highlight">{{ outletList[`outLetMin${1}`].toFixed(3)  }}</span> kWh <span class="time">记录于({{ outletList[`outLetMinTime${1}`] }})</span></span>
        <span  class="separator" v-if="outletList[`pName${2}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="outletList[`pName${2}`]!=null">{{outletList[`pName${2}`]}}：</span>
        <span class="power-value" v-if="outletList[`pName${2}`]!=null">峰值 <span class="highlight">{{ outletList[`outLetMax${2}`].toFixed(3)  }}</span> kWh <span class="time">记录于({{ outletList[`outLetMaxTime${2}`] }})</span></span>
        <span class="power-value" v-if="outletList[`pName${2}`]!=null">谷值 <span class="highlight">{{ outletList[`outLetMin${2}`].toFixed(3)  }}</span> kWh <span class="time">记录于({{ outletList[`outLetMinTime${2}`] }})</span></span>
      </div>
                <div class="power-section single-line">
        <span class="power-title" v-if="outletList[`pName${3}`]!=null">{{outletList[`pName${3}`]}}：</span>
        <span class="power-value" v-if="outletList[`pName${3}`]!=null">峰值 <span class="highlight">{{ outletList[`outLetMax${3}`].toFixed(3) }}</span>  <span class="time">记录于({{ outletList[`outLetMaxTime${3}`] }})</span></span>
        <span class="power-value" v-if="outletList[`pName${3}`]!=null">谷值 <span class="highlight">{{ outletList[`outLetMin${3}`].toFixed(3)  }}</span>  <span class="time">记录于({{ outletList[`outLetMinTime${3}`] }})</span></span>
        <span  class="separator" v-if="outletList[`pName${4}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="outletList[`pName${4}`]!=null">{{outletList[`pName${4}`]}}：</span>
        <span class="power-value" v-if="outletList[`pName${4}`]!=null">峰值 <span class="highlight">{{ outletList[`outLetMax${4}`].toFixed(3)  }}</span>  <span class="time">记录于({{ outletList[`outLetMaxTime${4}`] }})</span></span>
        <span class="power-value" v-if="outletList[`pName${4}`]!=null">谷值 <span class="highlight">{{ outletList[`outLetMin${24}`].toFixed(3)  }}</span>  <span class="time">记录于({{ outletList[`outLetMinTime${4}`] }})</span></span>
      </div>
            <HorizontalBar :width="computedWidth" height="58vh" :list="outletList" :dataType="queryParams.dataType"/>
          </div>

            <div class="pageBox" v-if="visControll.outLetActiveVis">
            <div class="page-conTitle">
              输出位有功功率
            </div>
                     <div v-for="(sensor, index) in outLetActiveList?.series" :key="index">
        <div class="power-section single-line" v-if="index %2 == 0">
        <span class="power-title" v-if="outletActiveData[`pName${index+1}`]!=null">{{outletActiveData[`pName${index+1}`]}}：</span>
        <span class="power-value" v-if="outletActiveData[`pName${index+1}`]!=null">峰值 <span class="highlight">{{ outletActiveData[`activePowMaxValue${index + 1}`] }}</span>  <span class="time">记录于({{ outletActiveData[`activePowMaxTime${index + 1}`] }})</span></span>
        <span class="power-value" v-if="outletActiveData[`pName${index+1}`]!=null">谷值 <span class="highlight">{{ outletActiveData[`activePowMinValue${index + 1}`] }}</span>  <span class="time">记录于({{ outletActiveData[`activePowMinTime${index + 1}`] }})</span></span>
        <span  class="separator" v-if="outletActiveData[`pName${index+1}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="outletActiveData[`pName${index+2}`]!=null">{{outletActiveData[`pName${index+2}`]}}：</span>
        <span class="power-value" v-if="outletActiveData[`pName${index+2}`]!=null">峰值 <span class="highlight">{{ outletActiveData[`activePowMaxValue${index + 2}`] }}</span>  <span class="time">记录于({{ outletActiveData[`activePowMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="outletActiveData[`pName${index+2}`]!=null">谷值 <span class="highlight">{{ outletActiveData[`activePowMinValue${index + 2}`] }}</span>  <span class="time">记录于({{ outletActiveData[`activePowMinTime${index + 2}`] }})</span></span>
      </div>
            </div>  
            <OutLetActiveLine width="70vw" height="58vh" :list="outLetActiveList" :dataType="queryParams.dataType"/>
          </div>
        </div>

        

        <div class="pageBox"  v-if="visControll.temVis">
            <div class="page-conTitle">
              告警信息
            </div>
                <el-table
                  ref="multipleTableRef"
                  :data="temp1"
                  highlight-current-row
                  style="width: 100%"
                  :stripe="true" 
                  :border="true"
                  @current-change="handleCurrentChange"
                >
                    <!-- <el-table-column type="selection" width="55" /> -->
                    <el-table-column type="index" width="80" label="序号" align="center" />
                    <el-table-column property="devPosition" label="区域" min-width="100" align="center" />
                    <el-table-column property="devName" label="设备" min-width="100" align="center" />
                    <el-table-column property="alarmLevelDesc" label="告警等级" min-width="100" align="center" />
                    <el-table-column property="alarmTypeDesc" label="告警类型" min-width="100" align="center" />
                    <el-table-column property="alarmDesc" label="描述" min-width="120" align="center">
                      <template #default="scope">
                        <el-tooltip  placement="right">
                          <div class="table-desc">{{scope.row.alarmDesc}}</div>
                          <template #content>
                            <div class="tooltip-width">{{scope.row.alarmDesc}}</div>
                          </template>
                        </el-tooltip>
                      </template>
                    </el-table-column>
    <el-table-column label="开始时间" min-width="160" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column label="结束时间" min-width="160" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.endTime) }}
        </template>
      </el-table-column>
                    <el-table-column property="finishReason" label="结束原因" min-width="100" align="center" />
                    <el-table-column property="confirmReason" label="确认原因" min-width="100" align="center" />
                </el-table>
          </div>
 

    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <PDUDeviceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import * as echarts from 'echarts';
import { ElTree } from 'element-plus';
import { IndexApi } from '@/api/bus/boxindex';
import type Node from 'element-plus/es/components/tree/src/model/node';
import Line from './component/Line.vue';
import PFLine from './component/PFLine.vue';
import CurLine from './component/CurLine.vue';
import VolLine from './component/VolLine.vue';
import Bar from './component/Bar.vue';
import EnvTemLine from './component/EnvTemLine.vue';
import LoopCurLine from './component/LoopCurLine.vue';
import LoopVolLine from './component/LoopVolLine.vue';
import HorizontalBar from './component/HorizontalBar.vue';
import OutLetActiveLine from './component/OutLetActiveLine.vue';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' });

const temp1 = ref([]) as any;
const preStatus = ref([0])
// const curvolList = ref() as any;
const curVolData = ref();
const volList = ref() as any
const curList = ref() as any
const curvolLoopData = ref() as any;
const curLoopList = ref() as any;
const volLoopList = ref() as any;
const outletList = ref() as any;
const outletActiveData = ref() as any;
const outLetActiveList = ref() as any;
const temList = ref() as any;
const eleList = ref() as any;
const totalLineList = ref() as any;
const pfLineData = ref() as any
const pfLineList = ref() as any;
const now = ref()
const switchValue = ref(1);
const visControll = reactive({
  visAllReport : false,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  temVis : false,
  pfVis: false,
  flag: false,
  curVolVis: false,
  curLoopVis : false,
  volLoopVis : false,
  outLetActiveVis : false,
  outletEleVis : false,
})
const serChartContainerWidth = ref(0)
const instance = getCurrentInstance();
let num=0

   // 格式化时间戳的方法
const formatTime = (timestamp) => {
    if(timestamp == null){
    return ''
  }
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' });
};

// 创建一个响应式引用来存储窗口宽度
const windowWidth = ref(window.innerWidth);

// 计算属性，根据窗口宽度返回不同的width值
const computedWidth = computed(() => {
  if (windowWidth.value >= 2400) {
    return '90vw';
  } else if (windowWidth.value >= 1600) {
    return '70vw';
  } else {
    return '80vw';
  }
});



// 监听窗口尺寸变化并更新windowWidth的值
const updateWindowWidth = () => {
  windowWidth.value = window.innerWidth;
};

interface RadarData{
  index: number;
  powApparent: number;
  powValue : number;
  curValue : number;
  name: string;
}

interface ServerData {
  nameAndMax: object[];
  curvalue: number[];
  powvalue: number[];
  powapparent: number[];
}

const result=ref<RadarData>({
  index : 0,
  powApparent : 0.000,
  powValue : 0.000,
  curValue : 0.00,
  name: ""
})

// 获得雷达视在功率数据
const serverData = ref<ServerData>({
  nameAndMax : [],
  curvalue: [],
  powvalue: [],
  powapparent: []
}) as any

const serverData1 = ref<ServerData>({
  nameAndMax : [],
  curvalue: [],
  powvalue: [],
  powapparent: []
}) as any

const serChartContainer = ref<HTMLElement | null>(null);
let serChart = null as echarts.ECharts | null; // 显式声明 serChart 的类型

const indicator =ref<ServerData>({
  nameAndMax: [],
  curvalue: [],
  powvalue: [],
  powapparent: [],
})
indicator.value.nameAndMax=serverData.value.nameAndMax

// 截取前10个元素
const truncateArrays = (data: ServerData): ServerData => {
  return {
    nameAndMax: data.nameAndMax.slice(0, 12),
    curvalue: data.curvalue.slice(0, 12),
    powvalue: data.powvalue.slice(0, 12),
    powapparent: data.powapparent.slice(0, 12)
  };
};

const initChart = async () => {
  if (serChartContainer.value && instance && serverData.value.nameAndMax && serverData.value.nameAndMax.length > 0) {
    
    serChart = echarts.init(serChartContainer.value);
    serChart.setOption({
      legend: {
                data: ['电流','视在功率', '有功功率'],
                selected: { // 默认选择状态
                  '电流': true, // 默认选中电能
                  '视在功率': false,
                  '有功功率': false
                           },
                bottom: 0,
                right: 0,
              },
      grid: {
                bottom: 0,
                top: 0,
              },
      radar: { indicator: indicator.value.nameAndMax},
      series: [

          { 
          name: 'PDU输出位电能', 
          type: 'radar', 
          label: { show: true, position: 'top' } ,
          data: 
          [ { value: serverData.value.curvalue, name: '电流' }, ] },
        { 
          name: 'PDU输出位视在功率', 
          type: 'radar', 
          label: { show: true, position: 'top' } ,
          data: [ { value: serverData.value.powapparent, name: '视在功率' }, ] },
          { 
          name: 'PDU输出位有功功率', 
          type: 'radar', 
          label: { show: true, position: 'top' } ,
          data: [ { value: serverData.value.powvalue, name: '有功功率' }, ] }
      ]
    });
    
    serChart.on('legendselectchanged', function (queryParams) {
      // console.log(indicator.value.nameAndMax)
      // console.log(serverData.value.nameAndMax)
      // console.log(serverData1.value.nameAndMax)
      const selected = queryParams.selected;
      console.log(selected)
      console.log(!selected['电流'])
      if(selected['电流']&&(selected['视在功率']||selected['有功功率'])){
        num++;
      }
      // if(!selected['电流']&&selected['视在功率']&&!selected['有功功率']){
      //   selected['电流'] = false;
      //   selected['视在功率'] = true;
      //   selected['有功功率'] = false;
      //   indicator.value.nameAndMax=serverData1.value.nameAndMax
      // }
      // else if(!selected['电流']&&!selected['视在功率']&&selected['有功功率']){
      //   selected['电流'] = false;
      //   selected['视在功率'] = false;
      //   selected['有功功率'] = true;
      //   indicator.value.nameAndMax=serverData1.value.nameAndMax
      // }
      if(selected['电流']&&!selected['视在功率']&&!selected['有功功率']){
        selected['电流'] = true;
        selected['视在功率'] = false;
        selected['有功功率'] = false;
        num=0;
        indicator.value.nameAndMax=serverData.value.nameAndMax
      }
      else if(num%2==0){
        selected['电流'] = true;
        selected['视在功率'] = false;
        selected['有功功率'] = false;
        indicator.value.nameAndMax=serverData.value.nameAndMax
      }
      else{
        selected['电流'] = false;
        indicator.value.nameAndMax=serverData1.value.nameAndMax

      }
      

      // 更新图表配置
      if(serChart){
        serChart.setOption({legend: {selected: selected},radar: { indicator: indicator.value.nameAndMax}});
      }
    });
        // serverData.value.nameAndMax=[]
        // serverData.value.curvalue=[]
        // serverData.value.powapparent=[]
        // serverData.value.powvalue=[]
        // serverData1.value.nameAndMax=[]
    // 将 serChart 绑定到组件实例，以便在销毁组件时能够正确释放资源
    // 使用截取后的数据
    serverData.value = truncateArrays(serverData.value);
    serverData1.value = truncateArrays(serverData1.value);
    instance.appContext.config.globalProperties.serChart = serChart;
  }

  visControll.visAllReport = true;
};

const disabledDate = (date) => {
  // 获取今天的日期
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // 设置date的时间为0时0分0秒，以便与today进行比较
  date.setHours(0, 0, 0, 0);

  // 如果date在今天之后，则禁用
  if(switchValue.value == 0){
    return date > today;
  }else {
    return date >= today;
  }
  
}

const handleDayPick = () => {
  if(queryParams?.oldTime && switchValue.value == 2){

    queryParams.oldTime = null;
    queryParams.newTime = null;
  }
  if(queryParams?.oldTime && switchValue.value == 0){
    
    queryParams.newTime = queryParams.oldTime.split(" ")[0] + " " + "23:59:59";
    visControll.isSameDay = true;

  } else if (queryParams.timeArr && switchValue.value == 2) {

    if(areDatesEqual(new Date(queryParams.timeArr[0]),new Date(queryParams.timeArr[1]))){
      visControll.isSameDay = true;
    }else{
      visControll.isSameDay = false;
    }
    queryParams.oldTime = queryParams.timeArr[0];
    queryParams.newTime = queryParams.timeArr[1].split(" ")[0]+ " " + "23:59:59";
    
  }
  
}

const handleMonthPick = () => {

  if(queryParams.oldTime){
    var newTime = new Date(queryParams.oldTime);
    newTime.setMonth(newTime.getMonth() + 1);
    newTime.setDate(newTime.getDate() - 1);
    newTime.setHours(23,59,59)
    queryParams.newTime = getFullTimeByDate(newTime);
    visControll.isSameDay = false;
  }else {
    queryParams.newTime = null;
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

const devKeyList = ref([])
const loadAll = async () => {
  var data = await IndexApi.devKeyList();
  var objectArray = data.map((str) => {
    return { value: str };
  });
  return objectArray;
}

const areDatesEqual = (date1, date2) => {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
}

const querySearch = (queryString: string, cb: any) => {

  const results = queryString
    ? devKeyList.value.filter(createFilter(queryString))
    : devKeyList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (queryString: string) => {
  return (devKeyList) => {
    return (
      devKeyList.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0
    )
  }
}

// const activeNames = ref(["1","2","3","4","5"])

const PDUTableData = ref([]) as any

const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  devKey : undefined,
  id: undefined,
  type: 'total',
  eqGranularity:"day",
  powGranularity : "hour",
  temGranularity : "hour",
  outputNumber : 10,
  ipAddr: undefined,
  createTime: undefined,
  timeArr:[],
  oldTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth(),1,0,0,0)),
  newTime : getFullTimeByDate(new Date(new Date().getFullYear(),new Date().getMonth() + 1,1,23,59,59)),
  timeType: 1,
  cascadeAddr : 0,
  dataType : 1
}) as any

const loadNode = async (node: Node, resolve: (data: Tree[]) => void) => {
  if (node.level === 0) {
    var temp = await IndexApi.getBoxMenu();
    return resolve(temp)
  }
  // if (node.level === 1){
  //   var temp = await CabinetApi.getRoomPDUList({id : node.data.id});
  //   return resolve(temp[0].children);
  // } 
  if (node.level >= 1){
    return resolve(node?.data?.children);
  } 
}


const handleClick = (row) => {
  if(row.type != null  && row.type == 7){
    queryParams.devKey = row.unique
    handleQuery();
  }
}

//折线图数据
interface EqData {
  eq: number[];
  time: string[];
  totalEle : number;
  maxEle : number;
  maxEleTime : string;
  firstEq : number;
  lastEq : number;
}
const eqData = ref<EqData>({
  eq : [],
  time : [],
  totalEle : 0,
  maxEle : 0,
  maxEleTime : "",
  firstEq : 0,
  lastEq : 0,
}) as any

interface PowData {
  apparentPowAvgValue: number[];
  activePowAvgValue: number[];
  time: string[];
  total_pow_apparent : number;
  apparentPowMaxValue : number;
  apparentPowMaxTime : string;
  apparentPowMinValue : number;
  apparentPowMinTime : string;
  activePowMaxValue : number;
  activePowMaxTime : string;
  activePowMinValue : number;
  activePowMinTime : string;
}
const powData = ref<PowData>({
  apparentPowAvgValue : [],
  activePowAvgValue: [],
  time:[],
  total_pow_apparent : 0,
  apparentPowMaxValue : 0,
  apparentPowMaxTime : "",
  apparentPowMinValue : 0,
  apparentPowMinTime : "",
  activePowMaxValue : 0,
  activePowMaxTime : "",
  activePowMinValue : 0,
  activePowMinTime : ""
}) as any

interface TemData {
  temAvgValue: any;
  time: string[];
  temMaxValue : number;
  temMaxTime : string;
  temMaxSensorId : number;
  temMinValue : number;
  temMinTime : string;
  temMinSensorId : number;
}
const temData = ref<TemData>({
  temAvgValue : [],
  time : [],
  temMaxValue : 0,
  temMaxTime : "",
  temMaxSensorId : 0,
  temMinValue : 0,
  temMinTime : "",
  temMinSensorId : 0,
}) as any

//树型控件
interface Tree {
  [key: string]: any
}

const filterText = ref('')
const treeRef = ref<InstanceType<typeof ElTree>>()
const itemStyle = ref({  
  emphasis: {  
    barBorderRadius: 3  
  },  
  normal: {  
    barBorderRadius: 3,  
    color: new echarts.graphic.LinearGradient(  
      0, 1, 0, 0, [  
        { offset: 0, color: '#3977E6' },  
        { offset: 1, color: '#37BBF8' }  
      ]  
    )  
  }  
});


watch(()=>queryParams.dataType, async()=>{
  //  queryParams.devKey = queryParams.ipAddr;  
  getList()

})

const getList = async () => {
  loading.value = true

  eqData.value = await IndexApi.getConsumeData(queryParams);
  if(eqData.value?.barRes?.series[0]){
    eqData.value.barRes.series[0].itemStyle = itemStyle.value;
  }
  eleList.value = eqData.value.barRes;
  if( eleList.value?.time != null && eleList.value?.time?.length > 0){
    eqData.value.maxEle = eqData.value.maxEle?.toFixed(1);
    eqData.value.totalEle = eqData.value.totalEle?.toFixed(1);
    if(eqData.value.firstEq){
      eqData.value.firstEq = eqData.value.firstEq?.toFixed(1);
    }
    if(eqData.value.lastEq){
      eqData.value.lastEq = eqData.value.lastEq?.toFixed(1);
    }    
    visControll.eqVis = true;
  } else{
    visControll.eqVis = false;
  }

  var temp = [] as any;
  var baseInfo = await IndexApi.getReportBasicInformationResVO(queryParams);

    var PDU = await IndexApi.getBoxRedisByDevKey(queryParams);
  console.log('PDU',PDU);
  PDU = JSON.parse(PDU)
  // temp.push({
  //   baseInfoName : "所属位置",
  //   baseInfoValue : baseInfo?.location !=null ? baseInfo?.location : "/",
  //   consumeName : "消耗电量",
  //   consumeValue : eqData.value?.barRes?.series && eqData.value?.barRes?.series.length > 0? visControll.isSameDay ? (eqData.value.lastEq - eqData.value.firstEq).toFixed(1) + "kWh" : eqData.value.totalEle + "kWh" : '/',
  // })
  // temp.push({
  //   baseInfoName : "网络地址",
  //   baseInfoValue : baseInfo?.devKey !=null ? baseInfo?.devKey : "/",
  //   consumeName : "当前视在功率",
  //   consumeValue : baseInfo?.powApparent !=null ? baseInfo?.powApparent.toFixed(3) + "kVA" : '/',
  // })
  // temp.push({
  //   baseInfoName : "设备状态",
  //   baseInfoValue : baseInfo?.runStatus !=null ? baseInfo?.runStatus : "/",
  //   consumeName : "当前功率因素",
  //   consumeValue : baseInfo?.powerFactor !=null ? baseInfo?.powerFactor.toFixed(2) : '/'
  // })
  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : baseInfo?.location !=null ? baseInfo?.location : "--",
    statusInfoName : "总视在功率",
    statusInfoValue : PDU?.box_data?.box_total_data != null ?PDU?.box_data?.box_total_data?.pow_apparent.toFixed(3)+ "kVA" : '--',
    consumeName : "起始电能",
    consumeValue : eqData.value.firstEq !=null ?eqData.value.firstEq+"kWh":'--',
    unbalanceName : "电流不平衡度",
    unbalanceValue : PDU?.bus_data?.bus_total_data != null ?PDU?.bus_data?.bus_total_data?.cur_unbalance.toFixed(0)+ "%" : '--',
  })
  temp.push({
    baseInfoName : "网络地址",
    baseInfoValue : baseInfo?.devKey !=null ? baseInfo?.devKey : "--",
    statusInfoName : "总有功功率",
    statusInfoValue :   PDU?.box_data?.box_total_data != null ?PDU?.box_data?.box_total_data?.pow_active.toFixed(3)+ "kW" : '--',
    consumeName : "结束电能",
    consumeValue :  eqData.value.lastEq != null ?eqData.value.lastEq+"kWh":'--',
    unbalanceName : "电压不平衡度",
    unbalanceValue : PDU?.bus_data?.bus_total_data != null ?PDU?.bus_data?.bus_total_data?.vol_unbalance.toFixed(0)+ "%" : '--',
  })
  temp.push({
    baseInfoName : "设备状态",
    baseInfoValue : baseInfo?.runStatus !=null ? baseInfo?.runStatus : "--",
    statusInfoName : "总无功功率",
    statusInfoValue : PDU?.box_data?.box_total_data != null ?PDU?.box_data?.box_total_data?.pow_reactive.toFixed(3)+ "kVar" : '--',
    consumeName : "电量消耗",
    consumeValue :  eqData.value?.barRes?.series && eqData.value?.barRes?.series.length > 0? visControll.isSameDay ? (eqData.value.lastEq - eqData.value.firstEq).toFixed(1) + "kWh" : eqData.value.totalEle + "kWh" : '--',
    // unbalanceName : "零线电流",
    // unbalanceValue : PDU?.bus_data?.bus_total_data != null ?PDU?.bus_data?.bus_total_data?.cur_zero_value.toFixed(2)+ "A" : '/',
  })
  temp.push({
    baseInfoName : "额定电流",
    baseInfoValue : '--',
    statusInfoName : "总功率因数",
    statusInfoValue :  PDU?.box_data?.box_total_data != null ?PDU?.box_data?.box_total_data?.power_factor.toFixed(2) : '--',
  })
  PDUTableData.value = temp;

  pfLineData.value  = await IndexApi.getBoxPFLine(queryParams);
  pfLineList.value = pfLineData.value.pfLineRes;
  console.log('pfLineListpfLineList',pfLineList.value);
  
  pfLineList.value.series.forEach(item => {
    item.data = item.data.map(value => parseFloat(value.toFixed(3)));
  });
  console.log('data.pfLineRes',pfLineData.pfLineRes)
  
  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
        pfLineData.value.pfLineRes.series.forEach((_, index) => {
          const maxKey = `powFactorMax${index + 1}`;
          const minKey = `powFactorMin${index + 1}`;

          if (pfLineData.value[maxKey] != null) {
            pfLineData.value[maxKey] = pfLineData.value[maxKey].toFixed(2);
          }
          if (pfLineData.value[minKey] != null) {
            pfLineData.value[minKey] = pfLineData.value[minKey].toFixed(2);
          }

          // const maxFKey = `powFactorMax${4}`;
          // const minFKey = `powFactorMin${4}`;
          // if(pfLineData.value[maxFKey] != null){
          // pfLineData.value[maxFKey] = pfLineData.value[maxFKey].toFixed(2);
          // }
          //  if (pfLineData.value[minFKey] != null) {
          //   pfLineData.value[minFKey] = pfLineData.value[minFKey].toFixed(2);
          // }

        });
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }

  powData.value = await IndexApi.getPowData(queryParams);
  totalLineList.value = powData.value.totalLineRes;
  if(totalLineList.value?.time != null && totalLineList.value?.time?.length > 0){
    powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
    powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
    powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
    powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);
    powData.value.reactivePowMaxValue = powData.value.reactivePowMaxValue?.toFixed(3);
    powData.value.reactivePowMinValue = powData.value.reactivePowMinValue?.toFixed(3);
    visControll.powVis = true;
  }else{
    visControll.powVis = false;
  }

  outletList.value = await IndexApi.getAvgBoxHdaOutletEleForm(queryParams);
  if(outletList.value.time != null && outletList.value.time.length > 0){
    visControll.outletEleVis = true;
  }else{
    visControll.outletEleVis = false;
  }
  console.log('outletRankData.value',outletList.value);
  
  curVolData.value = await IndexApi.getAvgBoxHdaLineForm(queryParams);
  curList.value = curVolData.value.curRes
  volList.value = curVolData.value.volRes

  if(curVolData.value?.dateTimes != null && curVolData.value?.dateTimes?.length > 0){
    // lineNumber = curVolData.value.lineNumber;
   
    curVolData.value.curRes.series.forEach((_, index) => {
          const maxKey = `curMaxValue${index + 1}`;
          const minKey = `curMinValue${index + 1}`;

          if (curVolData.value[maxKey] != null) {
            curVolData.value[maxKey] = curVolData.value[maxKey].toFixed(2);
          }
          if (curVolData.value[minKey] != null) {
            curVolData.value[minKey] = curVolData.value[minKey].toFixed(2);
          }
        });

        curVolData.value.volRes.series.forEach((_, index) => {
          const maxKey = `volMaxValue${index + 1}`;
          const minKey = `volMinValue${index + 1}`;

          if (curVolData.value[maxKey] != null) {
            curVolData.value[maxKey] = curVolData.value[maxKey].toFixed(1);
          }
          if (curVolData.value[minKey] != null) {
            curVolData.value[minKey] = curVolData.value[minKey].toFixed(1);
          }
        });
        visControll.curVolVis = true;
      }else{
      visControll.curVolVis = false;

  }

  outletActiveData.value =  await IndexApi.getAvgBoxHdaOutletForm(queryParams);
  outLetActiveList.value =   outletActiveData.value.totalLineRes;
    if(outLetActiveList.value?.time != null && outLetActiveList.value?.time?.length > 0){
        visControll.outLetActiveVis = true;
    }else{
      visControll.outLetActiveVis = false;
    }
  


  curvolLoopData.value = await IndexApi.getAvgBoxHdaLoopForm(queryParams);
  curLoopList.value = curvolLoopData.value.linCureRes
  volLoopList.value = curvolLoopData.value.linVoleRes

  if(curLoopList.value?.time != null && curLoopList.value?.time?.length > 0){
    curLoopList.value.series.forEach((_, index) => {
          const maxKey = `loopCurMax${index + 1}`;
          const minKey = `loopCurMin${index + 1}`;

          if (curvolLoopData.value[maxKey] != null) {
            curvolLoopData.value[maxKey] = curvolLoopData.value[maxKey].toFixed(2);
          }
          if (curvolLoopData.value[minKey] != null) {
            curvolLoopData.value[minKey] = curvolLoopData.value[minKey].toFixed(2);
          }
        }); 
        visControll.curLoopVis = true;
  }else{
    visControll.curLoopVis = false;
  }

  
  if(volLoopList.value?.time != null && volLoopList.value?.time?.length > 0){
    volLoopList.value.series.forEach((_, index) => {
          const maxKey = `loopVolMax${index + 1}`;
          const minKey = `loopVolMin${index + 1}`;

          if (curvolLoopData.value[maxKey] != null) {
            curvolLoopData.value[maxKey] = curvolLoopData.value[maxKey].toFixed(1);
          }
          if (curvolLoopData.value[minKey] != null) {
            curvolLoopData.value[minKey] = curvolLoopData.value[minKey].toFixed(1);
          }
        }); 
        visControll.volLoopVis = true;
  }else{
    visControll.volLoopVis = false;
  }




  
  

  temData.value = await IndexApi.getTemData(queryParams);
  temList.value = temData.value.lineRes;
  if(temList.value?.time != null && temList.value?.time?.length > 0){
        temData.value.temAMinValue = temData.value.temAMinValue?.toFixed(0);
    temData.value.temAMaxValue = temData.value.temAMaxValue?.toFixed(0);

    temData.value.temBMinValue = temData.value.temBMinValue?.toFixed(0);
    temData.value.temBMaxValue = temData.value.temBMaxValue?.toFixed(0);

    temData.value.temCMinValue = temData.value.temCMinValue?.toFixed(0);
    temData.value.temCMaxValue = temData.value.temCMaxValue?.toFixed(0);

    temData.value.temNMinValue = temData.value.temNMinValue?.toFixed(0);
    temData.value.temNMaxValue = temData.value.temNMaxValue?.toFixed(0);
    visControll.temVis = true;

  }else{
    visControll.temVis = false;
  }






  var temp = [] as any;
  var resultArray=[] as any;
  // 假设 PDU.pdu_data.output_item_list.pow_value 是一个 double 数组
  var powApparentValueArray = PDU?.bus_data?.line_item_list?.pow_apparent;
  var powValueArray = PDU?.bus_data?.line_item_list?.pow_value;
  var curValueArray = PDU?.bus_data?.line_item_list?.cur_value;
  
  // console.log(powValueArray)
  // 将值与下标保存到对象数组中
  if(powValueArray && powValueArray.length >= 0){
    for (var i = 0; i < powValueArray.length; i++) {
      result.value.index=i + 1
      result.value.name = "输出位" + (i + 1)
      result.value.powApparent = powApparentValueArray[i]
      result.value.curValue = curValueArray[i]
      result.value.powValue= powValueArray[i]
      resultArray.push({ ...result.value });
    }
    // 按值进行排序
    resultArray.sort((a, b) => b.curValue - a.curValue);
    // 只保留前十个元素
    resultArray = resultArray.slice(0, 12);

    for(var i=0;i<resultArray.length;i++){
      serverData.value.nameAndMax.push({
        name: resultArray[i].name,
        max: resultArray[i].curValue+0.001,
      })
      serverData1.value.nameAndMax.push({
        name: resultArray[i].name,
        max: resultArray[i].powApparent+0.001,
      })
      // console.log(serverData1.value.nameAndMax)
      serverData.value.curvalue.push(resultArray[i].curValue.toFixed(2))
      serverData.value.powvalue.push(resultArray[i].powValue)
      serverData.value.powapparent.push(resultArray[i].powApparent)
    }
    // 根据 resultArray 中的元素生成 nameAndMax 数组和 value 数组
    serChartContainerWidth.value = 10;
    // console.log(" serChartContainerWidth.value", serChartContainerWidth.value)
    if(resultArray.length==0){
      serChartContainerWidth.value = 0;
    }

  }else{
    serChartContainerWidth.value = 0;
  }
  console.log('雷达图的数据',serverData.value);
  // var Box = await IndexApi.getBoxRedisByDevKey(queryParams);
  // Box = JSON.parse(Box)
  visControll.visAllReport = true;
  // initChart();
  loading.value = false

    //清除temp1的缓存数据
  temp1.value=[]
  //获得告警信息
  const temp1Data = await IndexApi.getBoxRecordPage({
    pageNo: 1,
    pageSize: 100,
    devKey: queryParams.devKey,
        // devKey: '',
    devType: 7,
    alarmType: 4,
    likeName: queryParams.devKey,
     pduStartTime: queryParams.oldTime,
    pduFinishTime: queryParams.newTime,
          alarmStatus: preStatus.value,
        // likeName: '11111',
  })
  //处理告警信息数据
  // //debugger
  //处理时间信息
  
  console.log('表格的数据',temp1Data)
  temp1.value = temp1Data.list

}

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})

// 下拉框选项数组
// const deviceStatus = ref([])

// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化


const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

const arraySpanMethod = ({
  rowIndex,
}) => {
  if (rowIndex === 1) {
  //这里为了是将第二列的表头隐藏，就形成了合并表头的效果
      return {display: 'none'}
  }
}

/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.devKey){
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
      initChart();
    }
  }
  
}

/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
const formRef = ref()
// const openForm = (type: string, id?: number) => {
//   formRef.value.open(type, id)
// }

// /** 删除按钮操作 */
// const handleDelete = async (id: number) => {
//   try {
//     // 删除的二次确认
//     await message.delConfirm()
//     // 发起删除
//     await PDUDeviceApi.deletePDUDevice(id)
//     message.success(t('common.delSuccess'))
//     // 刷新列表
//     await getList()
//   } catch {}
// }

// /** 导出按钮操作 */
// const handleExport = async () => {
//   try {
//     // 导出的二次确认
//     await message.exportConfirm()
//     // 发起导出
//     exportLoading.value = true
//     const data = await PDUDeviceApi.exportPDUDevice(queryParams)
//     download.excel(data, 'PDU设备.xls')
//   } catch {
//   } finally {
//     exportLoading.value = false
//   }
// }


import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();
/** 初始化 **/
onMounted( async () =>  {
  // getList();
  initChart();
  devKeyList.value = await loadAll();

  // 使用可选链操作符避免访问 undefined 的属性
let devKey = route.query?.devKey as string | undefined;
let timeType = route.query?.timeType as string | undefined;
let oldTime = route.query?.oldTime as string | undefined;
let newTime = route.query?.newTime as string | undefined;
let timeArr = route.query?.timeArr as string | undefined;
let visAllReport = route.query?.visAllReport as string | undefined;
let switchValue1 = route.query?.switchValue as string | undefined;

if (devKey != undefined) {
  queryParams.devKey = devKey;
  queryParams.timeType = timeType;
  queryParams.oldTime = oldTime;
  queryParams.newTime = newTime;
  queryParams.timeArr = timeArr;
  queryParams.visAllReport = visAllReport;
  switchValue.value = switchValue1;
  getList();
  initChart();
}
  



})
</script>
<style scoped lang="scss">

.master {
  width: 100%;
  box-sizing: border-box;
  display: flex;
  .master-left {
    position: relative;
    overflow: hidden;
    box-sizing: border-box;
    margin-right: 20px;
    transition: all 0.2s linear;
    .openNavtree {
      box-sizing: border-box;
      text-align: right;
      cursor: pointer;
      position: absolute;
      right: 10px;
      top: 12px;
      font-size: 15px;
      display: flex;
      align-items: center;
    }
    .reduce {
      display: flex;
      align-items: center;
      position: absolute;
      right: 10px;
      top: 52px;
      color: #777777;
      cursor: pointer;
      font-size: 13px;
    }
    .expand {
      width: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
      color: #777;
      cursor: pointer;
      background-color: #eef4fc;
      padding: 10px 0;
    }
  }
  .master-right {
    flex: 1;
    overflow: hidden;
  }
}

.centered-div {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}
.right-div{
  display: flex;
  justify-content: right; /* 水平居右 */
  align-items: center; /* 垂直居中 */
  height: 100%; /* 使用父容器的高度 */
}

.navBar {
  box-sizing: border-box;
  width: 100%;
  height: 46px;
  line-height: 46px;
  padding-left: 10px;
  background-color: #d5ffc1;
  font-size: 14px;
}
.nav-left {
  width: 215px;
  height: 100%;
  .overview {
    padding: 0 20px;
    .count {
      height: 70px;
      margin-bottom: 15px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-left: 15px;
      padding-right: 10px;
      box-shadow: 0 3px 4px 1px rgba(0,0,0,.12);
      border-radius: 3px;
      border: 1px solid #eee;
      .info {
        height: 46px;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        justify-content: space-between;
        font-size: 13px;
        .value {
          font-size: 15px;
          font-weight: bold;
        }
      }
    }
  }
  .status {
    display: flex;
    flex-wrap: wrap;
    .box {
      height: 70px;
      width: 50%;
      box-sizing: border-box;
      display: flex;
      justify-content: center;
      align-items: center;
      flex-direction: column;
      .top {
        display: flex;
        align-items: center;
        .tag {
          width: 8px;
          height: 8px;
          background-color: #3bbb00;
          margin-right: 3px;
          margin-top: 2px;
        }
        .empty {
          background-color: #ccc;
        }
        .warn {
          background-color: #ffc402;
        }
        .error {
          background-color: #fa3333;
        }
      }
      .value {
        font-size: 14px;
        margin-top: 5px;
        color: #aaa;
        .number {
          font-size: 14px;
          font-weight: bold;
          margin-right: 5px;
          color: #000;
        }
      }
    }
  }
  .header {
    display: flex;
    flex-direction: column;
    align-items: center;
    font-size: 13px;
    padding-top: 28px;
    .header_img {
      width: 110px;
      height: 110px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      border: 1px solid #555;
      img {
        width: 75px;
        height: 75px;
      }
    }
    .name {
      font-size: 15px;
      margin: 15px 0;
    }
  }
  .line {
    height: 1px;
    margin-top: 28px;
    margin-bottom: 20px;
    background: linear-gradient(297deg, #fff, #dcdcdc 51%, #fff);
  }
}
.progressContainer {
  display: flex;
  justify-content: center;
  align-items: center;
  .progress {
    width: 100px;
    height: 18px;
    line-height: 18px;
    font-size: 12px;
    box-sizing: border-box;
    border-radius: 150px;
    overflow: hidden;
    position: relative;
    vertical-align: middle;
    background-color: #bfa;
    display: flex;
    justify-content: center;
    .left {
      overflow: hidden;
      box-sizing: border-box;
      background-color: var(--el-color-primary);

    }
    .right {
      overflow: hidden;
      background-color:  #f56c6c;
    }
  }
}


.page-conTitle {
    font-size: 18px;
    font-weight: 600;
    padding: 10px 0;
}

.paragraph {
    text-indent: 25px;
    font-size: 16px;
    font-weight: 530;
    line-height: 1.7;
}

.page-con {
    padding: 0 20px 0 20px;
}

.page {
    -webkit-box-shadow: 0 0px 0px rgba(0, 0, 0, .1);
    box-shadow: 0 0px 0px rgba(0, 0, 0, .1);
    background: #fff;
    color: rgba(0, 0, 0, .8);
}

.Container{

  left: 0px; 
  top: 0px; 
  user-select: none; 
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0); 
  padding: 0px; 
  margin: 0px; 
  border-width: 0px;
}

.el-table--border {
    border: 1px solid #e8e8e8 !important;
}

.el-table {
    color: #2c2c2c !important;
}

:deep(.master-left .el-card__body) {
  padding: 0;
}

:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

// :deep(.el-form .el-form-item) {
//   margin-right: 0;
// }

@media screen and (min-width:2048px) {
  .adaptiveStyle {
    width: 75vw;
    height: 42vh;
  }
}

@media screen and  (max-width:2048px) and (min-width:1600px) {
  .adaptiveStyle {
    width: 70vw;
    height: 42vh;
  }
}

@media screen and (max-width:1600px) {
  .adaptiveStyle {
    width: 85vw;
    height: 42vh;
  }
}

.power-section {
  margin-bottom: 15px;
  background: #f9f9f9;
  padding: 12px 15px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.power-title {
  font-weight: bold;
  color: #3498db;
  margin-bottom: 8px;
  font-size: 14px;
}

.power-value {
  margin: 5px 0;
  padding: 6px 10px;
  background: white;
  border-radius: 4px;
  font-size: 14px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.highlight {
  color: #e74c3c;
  font-weight: bold;
}

.time {
  color: rgb(55, 169, 173);
  font-size: 13px;
  margin-left: 5px;
}
</style>
