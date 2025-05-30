<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="柜列报表">
    <template #NavInfo>
      <div >
        <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/aisle.png" /></div>
          <div class="name">柜列</div>
          <div>{{ location }}</div>
        </div>
        <div class="line"></div>
        <!-- <div class="status">
          <div class="box">
            <div class="top">
              <div class="tag"></div>&lt;15%
            </div>
            <div class="value"><span class="number">{{statusNumber.lessFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag empty"></div>小电流
            </div>
            <div class="value"><span class="number">{{statusNumber.smallCurrent}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag warn"></div>15%-30%
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterFifteen}}</span>个</div>
          </div>
          <div class="box">
            <div class="top">
              <div class="tag error"></div>&gt;30
            </div>
            <div class="value"><span class="number">{{statusNumber.greaterThirty}}</span>个</div>
          </div>
        </div> -->
        <div class="line"></div>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        label-width="120px"
      >
        <!-- <el-form-item label="网络地址" prop="devKey">
          <el-input
            v-model="queryParams.devKey"
            placeholder="请输入网络地址"
            clearable
            @keyup.enter="handleQuery"
            class="!w-240px"
          />
        </el-form-item> -->

        <!-- <el-form-item label="柜列Id" prop="ipAddr" >
          <el-autocomplete
            v-model="queryParams.id"
            :fetch-suggestions="querySearch"
            clearable
            class="!w-140px"
            placeholder="请输入id"
            @select="handleQuery"
          />
        </el-form-item> -->

        <el-form-item label="时间段" prop="createTime" label-width="100px">
          <el-button 
            @click="queryParams.timeType = 0;now = new Date();now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 0;handleDayPick();handleQuery()" 
            :type="switchValue == 0 ? 'primary' : ''"
          >
            日报
          </el-button>
          <el-button 
            @click="queryParams.timeType = 1;now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 1;handleMonthPick();handleQuery()" 
            :type="switchValue == 1 ? 'primary' : ''"
          >
            月报
          </el-button>
          <el-button 
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 2;" 
            :type="switchValue == 2 ? 'primary' : ''"
          >
            自定义
          </el-button>
          
          
        </el-form-item>
        <el-form-item style="position: relative; left: -27%;">
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
        
        <el-form-item style="position: relative;left: -55%;">
                 <el-select v-model="queryParams.dataType" placeholder="请选择" style="width: 100px">
            <el-option label="最大" :value="1" />
            <el-option label="平均" :value="0" />
            <el-option label="最小" :value="-1" />
          </el-select>
          <el-button @click="handleQuery"  ><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
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
              柜列基本信息
            </div>
            <br/>
            <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="CabinetTableData" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="基本信息" >
                      <el-table-column :show-header="false" prop="baseInfoName" />
                      <el-table-column :show-header="false" prop="baseInfoValue" />
                    </el-table-column>
                    
                      <!-- <template #default="scope">
                        <span v-if="scope.$index === 2">
                          <el-tag  v-if="scope.row.baseInfoValue == 0">正常</el-tag>
                          <el-tag type="warning" v-if="scope.row.baseInfoValue == 1">预警</el-tag>
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
                          <el-tag type="info" v-if="scope.row.baseInfoValue == 4">故障</el-tag>
                          <el-tag type="info" v-if="scope.row.baseInfoValue == 5">离线</el-tag>
                        </span>
                        <span v-else>{{ scope.row.baseInfoValue }}</span>
                      </template>
                    </el-table-column> -->
                    <el-table-column  align="center" label="当前功率" >
                      <el-table-column :show-header="false" prop="consumeName"  />
                      <el-table-column :show-header="false" prop="consumeValue" />
                    </el-table-column>
                    <el-table-column  align="center" label="AB路占比" >
                      <el-table-column :show-header="false" prop="percentageName"  />
                      <el-table-column :show-header="false" prop="percentageValue" >
                        <template #default="scope">
                          <span v-if="scope.$index === 0 && scope.row.percentageValue != null">
                            <div class="progressContainer">
                              <div class="progress">
                                <div class="left" :style="`flex: ${scope.row.percentageValue}`">{{scope.row.percentageValue}}%</div>
                                <div class="line"></div>
                                <div class="right" :style="`flex: ${100 - scope.row.percentageValue}`">{{100 - scope.row.percentageValue}}%</div>
                              </div>
                            </div>                            
                          </span>
                        </template>
                      </el-table-column>
                    </el-table-column>
                    
                  </el-table>
                </div>
              </el-col>
              <!-- <el-col :span="serChartContainerWidth">
                <div class="right-div" ref="serChartContainer" id="serChartContainer" style="width: 29vw; height: 25vh;"></div>
              </el-col> -->
            </el-row>
          </div>
             <div class="pageBox" >
            <div class="page-conTitle">
              机柜信息
            </div>
            <br/>
          </div>
       <el-table :data="tableData" style="width: 100%" :border="true">
      <el-table-column align="center" label="序号" type="index" prop="id" width="100px"/>
      <el-table-column prop="cabinetName" label="机柜名称" align="center"/>
      <el-table-column prop="runStatus" label="状态" align="center">
        <template #default="scope">
          <el-tag
            v-if="scope.row.runStatus == 1"
            type="default"
          >
            正常
          </el-tag>
          <el-tag
            v-if="scope.row.runStatus == 2"
            type="warning"
          >
            告警
          </el-tag>
          <el-popover
            placement="top-start"
            title="告警内容"
            :width="500"
            trigger="hover"
            :content="scope.row.pduAlarm"
            v-if="scope.row.runStatus == 2"
          >
            <template #reference>
              <el-tag type="danger">告警</el-tag>
            </template>
          </el-popover>
          <el-tag
            v-if="scope.row.runStatus == 0"
            type="info"
          >
            离线
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="powActive" label="总有功功率" align="center"/>
      <el-table-column prop="powReactive" label="总无功功率" align="center"/>
      <el-table-column prop="powApparent" label="总视在功率" align="center"/>
      <el-table-column prop="powerFactor" label="总功率因数" align="center"/>
      <el-table-column prop="proportion" label="AB路占比" align="center">
        <template #default="scope">
          <span v-if="scope.row.proportion != null">
            <div class="progressContainer">
              <div class="progress">
                <div class="left" :style="`flex: ${scope.row.proportion}; `">
                  {{ scope.row.proportion }}%
                </div>
                <div class="line"></div>
                <div class="right" :style="`flex: ${100 - scope.row.proportion}; `">
                  {{ 100 - scope.row.proportion }}%
                </div>
              </div>
            </div>
          </span>
          <span v-else>
            -
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button v-if="switchValue == 0" @click="generateDailyReport(scope.row.cabinetId)">日报</el-button>
          <el-button v-else-if="switchValue == 1" @click="generateMonthlyReport(scope.row.cabinetId)">月报</el-button>
        </template>
      </el-table-column>
    </el-table>


          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <p class="paragraph" v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p class="paragraph" v-if="visControll.isSameDay && eqData.firstEq">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p>
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
        <span class="power-value" v-if="pfLineData[`pName${index+1}`]!=null">峰值 <span class="highlight">{{ pfLineData[`fMax${index + 1}`].toFixed(2) }}</span>  <span class="time">记录于({{ pfLineData[`fMaxTime${index + 1}`] }})</span></span>
        <span class="power-value" v-if="pfLineData[`pName${index+1}`]!=null">谷值 <span class="highlight">{{ pfLineData[`fMin${index + 1}`].toFixed(2) }}</span>  <span class="time">记录于({{ pfLineData[`fMinTime${index + 1}`] }})</span></span>
        <span  class="separator" v-if="pfLineData[`pName${index+1}`]!=null">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="pfLineData[`pName${index+2}`]!=null">{{pfLineData[`pName${index+2}`]}}极值：</span>
        <span class="power-value" v-if="pfLineData[`pName${index+2}`]!=null">峰值 <span class="highlight">{{ pfLineData[`fMax${index + 2}`].toFixed(2) }}</span>  <span class="time">记录于({{ pfLineData[`fMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="pfLineData[`pName${index+2}`]!=null">谷值 <span class="highlight">{{ pfLineData[`fMin${index + 2}`].toFixed(2) }}</span>  <span class="time">记录于({{ pfLineData[`fMinTime${index + 2}`] }})</span></span>
      </div>
            </div>        
            <PFLine class="Container"  :width="computedWidth" height="58vh" :list="pfLineList"/>
          </div>

          <!-- <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              总平均功率曲线
            </div>
            <p class="paragraph">本周期内，最大视在功率{{powData.apparentPowMaxValue}}kVA， 发生时间{{powData.apparentPowMaxTime}}。最小视在功率{{powData.apparentPowMinValue}}kVA， 发生时间{{powData.apparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.activePowMaxValue}}kW， 发生时间{{powData.activePowMaxTime}}。最小有功功率{{powData.activePowMinValue}}kW， 发生时间{{powData.activePowMinTime}}</p>
            <Line class="Container"  width="70vw" height="58vh" :list="totalLineList"/>
          </div> -->
                <div class="pageBox"  v-if="visControll.powVis">
            <div class="page-conTitle">
              总功率趋势曲线
            </div>
             <div class="power-section single-line">
  <span class="power-title">视在功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.apparentPowMaxValue.toFixed(3)}}</span> kVA <span class="time">记录于({{factorData.apparentPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.apparentPowMinValue.toFixed(3)}}</span> kVA <span class="time">记录于({{factorData.apparentPowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">有功功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.activePowMaxValue.toFixed(3)}}</span> kW，<span class="time">记录于 ({{factorData.activePowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.activePowMinValue.toFixed(3)}}</span> kW，<span class="time">记录于 ({{factorData.activePowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">无功功率极值</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.reactivePowMaxValue.toFixed(3)}}</span> kVar，<span class="time">记录于 ({{factorData.reactivePowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.reactivePowMinValue.toFixed(3)}}</span> kVar，<span class="time">记录于 ({{factorData.reactivePowMinTime}})</span></span>
</div>
            <Line class="Container"  width="70vw" height="58vh" :list="totalLineList" :dataType="queryParams.dataType"/>
          </div>
         <div class="pageBox"  v-if="visControll.ApowVis">
            <div class="page-conTitle">
              A路功率趋势曲线
            </div>
                         <div class="power-section single-line">
  <span class="power-title">视在功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.apparentAPowMaxValue.toFixed(3)}}</span> kVA <span class="time">记录于({{factorData.apparentAPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.apparentAPowMinValue.toFixed(3)}}</span> kVA <span class="time">记录于({{factorData.apparentAPowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">有功功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.activeAPowMaxValue.toFixed(3)}}</span> kW，<span class="time">记录于 ({{factorData.activeAPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.activeAPowMinValue.toFixed(3)}}</span> kW，<span class="time">记录于 ({{factorData.activeAPowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">无功功率极值</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.reactiveAPowMaxValue.toFixed(3)}}</span> kVar，<span class="time">记录于 ({{factorData.reactiveAPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.reactiveAPowMinValue.toFixed(3)}}</span> kVar，<span class="time">记录于 ({{factorData.reactiveAPowMinTime}})</span></span>
</div>
            <Line class="Container" width="70vw" height="58vh" :list="aLineList" :dataType="queryParams.dataType"/>
          </div>

          <div class="pageBox"  v-if="visControll.BpowVis">
            <div class="page-conTitle">
              B路功率趋势曲线
            </div>
                         <div class="power-section single-line">
  <span class="power-title">视在功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.apparentBPowMaxValue.toFixed(3)}}</span> kVA <span class="time">记录于({{factorData.apparentBPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.apparentBPowMinValue.toFixed(3)}}</span> kVA <span class="time">记录于({{factorData.apparentBPowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">有功功率极值：</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.activeBPowMaxValue.toFixed(3)}}</span> kW，<span class="time">记录于 ({{factorData.activeBPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.activeBPowMinValue.toFixed(3)}}</span> kW，<span class="time">记录于 ({{factorData.activeBPowMinTime}})</span></span>
</div>

 
<div class="power-section single-line">
  <span class="power-title">无功功率极值</span>
  <span class="power-value">峰值 <span class="highlight">{{factorData.reactiveBPowMaxValue.toFixed(3)}}</span> kVar，<span class="time">记录于 ({{factorData.reactiveBPowMaxTime}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{factorData.reactiveBPowMinValue.toFixed(3)}}</span> kVar，<span class="time">记录于 ({{factorData.reactiveBPowMinTime}})</span></span>
</div>
            <Line class="Container" width="70vw" height="58vh" :list="bLineList" :dataType="queryParams.dataType"/>
          </div>

                    <div class="pageBox" v-if="visControll.lineACurVis">
            <div class="page-conTitle">
              相电流历史曲线趋势图
            </div>
                        
          <div v-for="(sensor, index) in lineCurList?.series" :key="index">
            <div class="power-section single-line" v-if="index %2 == 0">
              <span class="power-title">{{lineCurVolData[`curName${index + 1}`]}}</span>
  <span class="power-value">峰值 <span class="highlight">{{lineCurVolData[`curMaxValue${index + 1}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData[`curMaxTime${index + 1}`]}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{lineCurVolData[`curMinValue${index + 1}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData[`curMinTime${index + 1}`]}})</span></span>
          <span  class="separator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <span class="power-title" v-if="index+2<=lineCurList?.series.length "> {{lineCurVolData[`curName${index + 2}`]}}</span>
  <span class="power-value" v-if="index+2<=lineCurList?.series.length ">峰值 <span class="highlight">{{lineCurVolData[`curMaxValue${index + 2}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData[`curMaxTime${index + 2}`]}})</span></span>
  <span class="power-value" v-if="index+2<=lineCurList?.series.length ">谷值 <span class="highlight">{{lineCurVolData[`curMinValue${index + 2}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData[`curMinTime${index + 2}`]}})</span></span>
</div>
          </div>
            <CurLine :width="computedWidth" height="78vh" :list="lineCurList"  :dataType="queryParams.dataType"/>
          </div>
          <div class="pageBox" v-if="visControll.lineAVolVis">
            <div class="page-conTitle">
              相电压历史曲线趋势图
            </div>
                        <div v-for="(sensor, index) in lineVolList?.series" :key="index">
            <div class="power-section single-line" v-if="index %2 == 0">
              <span class="power-title">{{lineCurVolData[`volName${index + 1}`]}}</span>
  <span class="power-value">峰值 <span class="highlight">{{lineCurVolData[`volMaxValue${index + 1}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData[`volMaxTime${index + 1}`]}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{lineCurVolData[`volMinValue${index + 1}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData[`volMinTime${index + 1}`]}})</span></span>
          <span  class="separator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <span class="power-title" v-if="index+2<=lineVolList?.series.length ">{{lineCurVolData[`volName${index + 2}`]}}</span>
  <span class="power-value" v-if="index+2<=lineVolList?.series.length ">峰值 <span class="highlight">{{lineCurVolData[`volMaxValue${index + 2}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData[`volMaxTime${index + 2}`]}})</span></span>
  <span class="power-value" v-if="index+2<=lineVolList?.series.length ">谷值 <span class="highlight">{{lineCurVolData[`volMinValue${index + 2}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData[`volMinTime${index + 2}`]}})</span></span>
</div>

          </div>
            <VolLine :width="computedWidth" height="78vh" :list="lineVolList"  :dataType="queryParams.dataType"/>
          </div>

              <div class="pageBox" v-if="visControll.eqVis">
            <div class="page-conTitle" >
              机柜耗电量排名
            </div>
            <HorizontalBar :width="computedWidth" height="78vh" :list="cabinetEleList" />
          </div>

          <!-- <div class="pageBox"  v-if="visControll.ApowVis">
            <div class="page-conTitle">
              A路平均功率曲线
            </div>
            <p class="paragraph" >本周期内，最大视在功率{{powData.AapparentPowMaxValue}}kVA， 发生时间{{powData.AapparentPowMaxTime}}。最小视在功率{{powData.AapparentPowMinValue}}kVA， 发生时间{{powData.AapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.AactivePowMaxValue}}kW， 发生时间{{powData.AactivePowMaxTime}}。最小有功功率{{powData.AactivePowMinValue}}kW， 发生时间{{powData.AactivePowMinTime}}</p>
            <Line class="Container" width="70vw" height="58vh" :list="aLineList"/>
          </div> -->
          <!-- <div class="pageBox"  v-if="visControll.BpowVis">
            <div class="page-conTitle">
              B路平均功率曲线
            </div>
            <p class="paragraph" >本周期内，最大视在功率{{powData.BapparentPowMaxValue}}kVA， 发生时间{{powData.BapparentPowMaxTime}}。最小视在功率{{powData.BapparentPowMinValue}}kVA， 发生时间{{powData.BapparentPowMinTime}}</p>
            <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最大有功功率{{powData.BactivePowMaxValue}}kW， 发生时间{{powData.BactivePowMaxTime}}。最小有功功率{{powData.BactivePowMinValue}}kW， 发生时间{{powData.BactivePowMinTime}}</p>
            <Line class="Container" width="70vw" height="58vh" :list="bLineList"/>
          </div> -->
          <div class="pageBox"  v-if="warnList!=null">
            <div class="page-conTitle">
              告警信息
            </div>
                <el-table
                  ref="multipleTableRef"
                  :data="warnList.list"
                  highlight-current-row
                  style="width: 100%"
                  :stripe="true" 
                  :border="true"
                  @current-change="handleCurrentChange"
                >
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
          <!-- <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" >
              输出位电量排名
            </div>
            <div ref="outputRankChartContainer" id="outputRankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div> -->

        </div>
        
      </div>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/aisle/aisleindex'
import * as echarts from 'echarts';
import { ElTree } from 'element-plus'
import Line from './component/Line.vue'
import PFLine from './component/PFLine.vue'
import Bar from './component/Bar.vue'
import HorizontalBar from './component/HorizontalBar.vue';
import CurLine from './component/CurLine.vue';
import VolLine from './component/VolLine.vue';

/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })
const pfLineData = ref() as any;
const pfLineList = ref() as any;
const eleList = ref() as any;
const factorData = ref() as any;
const totalLineList = ref() as any;
const aLineList = ref() as any;
const bLineList = ref() as any;
const idList = ref() as any;
const cabinetEleData = ref() as any;
const cabinetEleList = ref() as any;
const now = ref()
const switchValue = ref(1);
const location = ref("");
const warnList=ref(null)
const lineCurVolData =ref() as any;
const lineCurList = ref() as any;
const lineVolList = ref() as any;
const tableData = ref([]);


const visControll = reactive({
  visAllReport : false,
  isSameDay : false,
  eqVis : false,
  powVis : false,
  outletVis : false,
  iceTemVis : false,
  hotTemVis : false,
  ApowVis :false,
  BpowVis : false,
  pfVis: false,
    lineACurVis : false,
  lineAVolVis : false,
})
const serChartContainerWidth = ref(0)

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

   // 格式化时间戳的方法
const formatTime = (timestamp) => {
    if(timestamp == null){
    return ''
  }
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' });
};

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

const areDatesEqual = (date1, date2) => {
  return (
    date1.getFullYear() === date2.getFullYear() &&
    date1.getMonth() === date2.getMonth() &&
    date1.getDate() === date2.getDate()
  );
}


// const activeNames = ref(["1","2","3","4","5"])

const CabinetTableData = ref([]) as any

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

watch( ()=>queryParams.dataType, async()=>{
  //  queryParams.devKey = queryParams.ipAddr;  
  getList()

})

const serverRoomArr =  ref([]) as any

const loadAll = async () => {
  var data = await IndexApi.idList();
  var objectArray = data.map((str) => {
    return { value: str };
  });

  return objectArray;
}

const querySearch = (queryString: string, cb: any) => {
  const results = queryString
    ? idList.value.filter(createFilter(queryString))
    : idList.value
  // call callback function to return suggestions
  cb(results)
}

const createFilter = (query: string | number) => {
  const queryStr = typeof query === 'string' ? query.toLowerCase() : query.toString();
  return (item: { value: string | number }) => {
    const itemValueStr = typeof item.value === 'string' ? item.value.toLowerCase() : item.value.toString();
    if (typeof query === 'string') {
      return itemValueStr.startsWith(queryStr);
    } else {
      return itemValueStr === queryStr;
    }
  };
};


//折叠功能


const getNavList = async() => {
  const res = await IndexApi.getAisleMenu()
  serverRoomArr.value = res
  if (res && res.length > 0) {
    const room = res[0]
    const keys = [] as string[]
    room.children.forEach(child => {
      if(child.children.length > 0) {
        child.children.forEach(son => {
          keys.push(son.id + '-' + son.type)
        })
      }
    })
  }
}

const handleClick = (row) => {
  if(row.type != null  && row.type == 2){

    queryParams.id = row.id
    handleQuery();
  }
}

const arraySpanMethod = ({
  rowIndex,
}) => {
  if (rowIndex === 1) {
  //这里为了是将第二列的表头隐藏，就形成了合并表头的效果
      return {display: 'none'}
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
})as any

interface PowData {
  apparentPowAvgValue: number[];
  activePowAvgValue: number[];
  AapparentPowAvgValue: number[];
  AactivePowAvgValue: number[];
  BapparentPowAvgValue: number[];
  BactivePowAvgValue: number[];
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
  AapparentPowMaxValue : number;
  AapparentPowMaxTime : string;
  AapparentPowMinValue : number;
  AapparentPowMinTime : string;
  AactivePowMaxValue : number;
  AactivePowMaxTime : string;
  AactivePowMinValue : number;
  AactivePowMinTime : string;
  BapparentPowMaxValue : number;
  BapparentPowMaxTime : string;
  BapparentPowMinValue : number;
  BapparentPowMinTime : string;
  BactivePowMaxValue : number;
  BactivePowMaxTime : string;
  BactivePowMinValue : number;
  BactivePowMinTime : string;
}
const powData = ref<PowData>({
  apparentPowAvgValue : [],
  activePowAvgValue: [],
  AapparentPowAvgValue : [],
  AactivePowAvgValue: [],
  BapparentPowAvgValue : [],
  BactivePowAvgValue: [],
  time:[],
  total_pow_apparent : 0,
  apparentPowMaxValue : 0,
  apparentPowMaxTime : "",
  apparentPowMinValue : 0,
  apparentPowMinTime : "",
  activePowMaxValue : 0,
  activePowMaxTime : "",
  activePowMinValue : 0,
  activePowMinTime : "",
  AapparentPowMaxValue : 0,
  AapparentPowMaxTime : "",
  AapparentPowMinValue : 0,
  AapparentPowMinTime : "",
  AactivePowMaxValue : 0,
  AactivePowMaxTime : "",
  AactivePowMinValue : 0,
  AactivePowMinTime : "",
  BapparentPowMaxValue : 0,
  BapparentPowMaxTime : "",
  BapparentPowMinValue : 0,
  BapparentPowMinTime : "",
  BactivePowMaxValue : 0,
  BactivePowMaxTime : "",
  BactivePowMinValue : 0,
  BactivePowMinTime : "",
})as any

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
const getList = async () => {
  loading.value = true
  
  await handleConsumeQuery();
  await handlePowQuery();
  await handleDetailQuery();
  await handlePFLineQuery();
  await handleWarn();
  await handleCVlineQuery();
  await handleCabinetQuery();

  visControll.visAllReport = true;
  loading.value = false

}
// 对数据进行处理，保留三位小数
// 辅助函数：处理数值并保留三位小数
function formatNumber(value) {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
        return 0; // 或者返回其他默认值
    }
    return Number(value.toFixed(3));
}

function formatNumber0(value) {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
        return null; // 或者返回其他默认值
    }
    return Number(value.toFixed(0));
}



const handleCabinetQuery = async () =>{
       const baseInfoList =  await IndexApi.getCabinetDetail({id : queryParams.id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime})
const processedData = baseInfoList.map(item => ({
    devKey: item.devKey,
    runStatus: item.status,
    cabinetName: item.cabinetName,
    powApparent: formatNumber(item.powApparent),
    powActive: formatNumber(item.powActive),
    powReactive: formatNumber(item.powReactive),
    powerFactor: formatNumber(item.powerFactor),
    proportion: formatNumber0(item.proportion),
    cabinetId: item.cabinetId,
}));

// 将处理后的数据存储在 tableData.value 中
tableData.value = processedData;

// 输出最终的 tableData.value
console.log('tableData.value', tableData.value);
}

const { push } = useRouter()

const now1 = ref()
const old1 = ref()
const new1 = ref()

const generateDailyReport = (devKey) => {
  now1.value = new Date();
  now1.value.setHours(0,0,0,0)
  old1.value = getFullTimeByDate(now1.value);
  new1.value = old1.value.split(" ")[0] + " " + "23:59:59";
  
      // 这里添加生成日报的逻辑，你可以根据 row 数据生成相应的日报报告
      console.log('生成日报报告', devKey);
      push('/report/cabinetreport?CabinetId='+devKey+'&timeType='+0+'&oldTime='+getFullTimeByDate(now1.value)+'&newTime='+new1.value+'&timeArr='+null+'&visAllReport='+false+'&switchValue='+0);
    };

    const generateMonthlyReport = (devKey) => {
      now1.value = new Date();
  now1.value.setDate(1)
  now1.value.setHours(0,0,0,0)
  old1.value = getFullTimeByDate(now1.value);
  new1.value = new Date(old1.value)
  new1.value.setMonth(new1.value.getMonth() + 1);
  new1.value.setDate(new1.value.getDate() - 1);
  new1.value.setHours(23,59,59)
  new1.value = getFullTimeByDate(new1.value);
      // 这里添加生成月报的逻辑，你可以根据 row 数据生成相应的月报报告
      console.log('生成月报报告', devKey);
      push('/report/cabinetreport?CabinetId='+devKey+'&timeType='+1+'&oldTime='+getFullTimeByDate(now1.value)+'&newTime='+new1.value+'&timeArr='+null+'&visAllReport='+false+'&switchValue='+1);
    };



const handleCVlineQuery = async () =>{
  lineCurVolData.value = await IndexApi.getAisleHdaLineData({id : queryParams.id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime,dataType: queryParams.dataType})
  lineCurList.value = lineCurVolData.value.curRes;
  lineVolList.value = lineCurVolData.value.volRes;
      if(lineCurVolData.value?.curRes?.time != null && lineCurVolData.value?.curRes?.time.length > 0){
    visControll.lineACurVis = true;
    visControll.lineAVolVis = true;
    }else{
    visControll.lineACurVis = false;
    visControll.lineAVolVis = false;
    }

}

const handlePFLineQuery = async () => {
  pfLineData.value = await IndexApi.getAislePFLineByType(queryParams);
  pfLineList.value = pfLineData.value.pfLineRes;
  
  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }
}


const handlePowQuery = async () => {
   factorData.value = await IndexApi.getPowDataByType(queryParams);
totalLineList.value = factorData.value.totalLineRes;
   aLineList.value = factorData.value.aLineRes;
   bLineList.value = factorData.value.bLineRes;
  if(totalLineList.value?.time != null && totalLineList.value?.time?.length > 0){

    visControll.powVis = true;
  }else{
    visControll.powVis = false;
  }
  
  if(aLineList.value?.time != null && aLineList.value?.time?.length > 0){

    visControll.ApowVis = true;
  }else{
    visControll.ApowVis = false;
  }

  if(bLineList.value?.time != null && bLineList.value?.time?.length > 0){

    visControll.BpowVis = true;
  }else{
    visControll.BpowVis = false;
  }

  // if(totalLineList.value?.time != null && totalLineList.value?.time?.length > 0){
  //   powData.value.apparentPowMaxValue = powData.value.apparentPowMaxValue?.toFixed(3);
  //   powData.value.apparentPowMinValue =  powData.value.apparentPowMinValue?.toFixed(3);
  //   powData.value.activePowMaxValue = powData.value.activePowMaxValue?.toFixed(3);
  //   powData.value.activePowMinValue = powData.value.activePowMinValue?.toFixed(3);
  //   visControll.powVis = true;
  // }else{
  //   visControll.powVis = false;
  // }
  
  // if(aLineList.value?.time != null && aLineList.value?.time?.length > 0){
  //   powData.value.AapparentPowMaxValue = powData.value.AapparentPowMaxValue?.toFixed(3);
  //   powData.value.AapparentPowMinValue =  powData.value.AapparentPowMinValue?.toFixed(3);
  //   powData.value.AactivePowMaxValue = powData.value.AactivePowMaxValue?.toFixed(3);
  //   powData.value.AactivePowMinValue = powData.value.AactivePowMinValue?.toFixed(3);
  //   visControll.ApowVis = true;
  // }else{
  //   visControll.ApowVis = false;
  // }

  // if(bLineList.value?.time != null && bLineList.value?.time?.length > 0){
  //   powData.value.BapparentPowMaxValue = powData.value.BapparentPowMaxValue?.toFixed(3);
  //   powData.value.BapparentPowMinValue =  powData.value.BapparentPowMinValue?.toFixed(3);
  //   powData.value.BactivePowMaxValue = powData.value.BactivePowMaxValue?.toFixed(3);
  //   powData.value.BactivePowMinValue = powData.value.BactivePowMinValue?.toFixed(3);
  //   visControll.BpowVis = true;
  // }else{
  //   visControll.BpowVis = false;
  // }
}

const handleConsumeQuery = async () => {
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
     cabinetEleData.value = await IndexApi.getCabinetEleByAis({id : queryParams.id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime})
     cabinetEleList.value = cabinetEleData.value.barRes;
}

const handleDetailQuery = async () => {
  var temp = [] as any;
  
  var data = await IndexApi.getAisBasicInformation({id : queryParams.id});
  var AisleInfo = data.list[0];
  location.value = AisleInfo?.location;
  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : AisleInfo?.location ,
    consumeName : "当前总视在功率",
    consumeValue : AisleInfo?.powApparentTotal != null ? AisleInfo?.powApparentTotal?.toFixed(3) + "kVA" : '/',
    percentageName: "当前AB路占比",
    percentageValue: AisleInfo.rateA != null ? AisleInfo.rateA.toFixed(0) : 50,
  })
  temp.push({ 
    baseInfoName : "电力容量",
    baseInfoValue :'--',
    consumeName : "当前总有功功率",
    consumeValue : AisleInfo?.powActiveTotal != null ? AisleInfo?.powActiveTotal?.toFixed(3) + "kW" : '/',
    percentageName: "A路有功功率",
    percentageValue: AisleInfo?.powActiveA != null ? AisleInfo?.powActiveA+'KW' : '--',
  })
  temp.push({
    baseInfoName : "负载率",
    baseInfoValue :  AisleInfo?.loadRate != null ? AisleInfo?.loadRate.toFixed(2)+'%' : '--',
    consumeName : "当前总无功功率",
    consumeValue : AisleInfo?.powReactiveTotal != null ? AisleInfo?.powReactiveTotal?.toFixed(3) + "kVar" : '/',
        percentageName: "B路有功功率",
    percentageValue: AisleInfo?.powActiveB != null ? AisleInfo?.powActiveB+'KW' :'--',
  })
   temp.push({
    baseInfoName : "耗电量",
    baseInfoValue : AisleInfo?.eleActive != null ? AisleInfo?.eleActive.toFixed(1)+'kWh' :'--',
    consumeName : "当前功率因素",
    consumeValue :  AisleInfo?.powFactorTotal != null ? AisleInfo.powFactorTotal.toFixed(2) : '--',
    percentageName: "偏差率",
    percentageValue: AisleInfo?.deviation != null ? (AisleInfo?.deviation * 100).toFixed(2)+'%' : '--',
  })
  CabinetTableData.value = temp;
}

watch(filterText, (val) => {
  treeRef.value!.filter(val)
})


// const message = useMessage() // 消息弹窗
// const { t } = useI18n() // 国际化

const loading = ref(false) // 列表的加载中

// const total = ref(0) // 列表的总页数
const queryFormRef = ref() // 搜索的表单
// const exportLoading = ref(false) // 导出的加载中

/** 搜索按钮操作 */
const handleQuery = async () => {

  if(queryParams.id){
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
      queryParams.devKey = null;
    }
  }
  
}

/** 重置按钮操作 */
// const resetQuery = () => {
//   queryFormRef.value.resetFields()
//   handleQuery()
// }

/** 添加/修改操作 */
// const formRef = ref()
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
const preStatus = ref([0])
/** 初始化 **/
onMounted( async () =>  {
  // getList();
  // initChart();
  getNavList();
  idList.value = await loadAll();
})
async function handleWarn(){
  warnList.value= await IndexApi.getRecordPageByType({
    pageNo: 1,
    pageSize: 100,
    devKey: queryParams.devKey,
    devType: 2,
    likeName: queryParams.id,
    alarmType: 10,
     pduStartTime: queryParams.oldTime,
    pduFinishTime: queryParams.newTime,
              alarmStatus: preStatus.value,
})
}
const handleCurrentChange = (val) => {
}
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
  margin-left: 30px;
  .progress {
    width: 400px;
    display: flex;
    align-items: center;
    font-size: 14px;
    color: #eee;
    box-sizing: border-box;
    position: relative;
    display: flex;
    justify-content: center;
    .line {
      width: 3px;
      height: 36px;
      background-color: #000;
    }
    .left {
      text-align: center;
      box-sizing: border-box;
      background-color: #3b8bf5;
    }
    .right {
      text-align: center;
      background-color:  #f86f13;
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

:deep .el-table thead tr th {
    // background: #01ada8 !important;
    // background: #7401ad !important;
    // color: #c01f1f;
        background: #f6f6f6 !important;
    color: rgb(0, 0, 0);
}
:deep(.master-left .el-card__body) {
  padding: 0;
}
:deep(.el-form) {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}
:deep(.el-form .el-form-item) {
  margin-right: 0;
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
