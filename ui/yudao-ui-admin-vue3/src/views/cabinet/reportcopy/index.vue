<template>
  <CommonMenu :showCheckbox="false" @node-click="handleClick" :showSearch="true" :dataList="serverRoomArr" navTitle="机柜报表">
    <template #NavInfo >
      <div >
        <!-- <div class="header">
          <div class="header_img"><img alt="" src="@/assets/imgs/wmk.jpg" /></div>
          <div class="name">机柜</div>
          <div></div>
        </div> -->
        <!-- <div class="line"></div> -->
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
        <!-- <div class="line"></div> -->
         <br/>

      </div>
    </template>
    <template #ActionBar>
      <el-form
        class="-mb-15px"
        :model="queryParams"
        ref="queryFormRef"
        :inline="true"
        style="float: left;"

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
        <el-form-item label="时间段" prop="createTime" label-width="100px">
          <el-button :color="switchValue == 0 ? '#00778c' : ''"
            @click="queryParams.timeType = 0;dateTimeName='twentyfourHour';now = new Date();now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 0;handleDayPick();handleQuery()" 
            :type="switchValue == 0 ? 'primary' : ''"
          >
            日报
          </el-button>
          <el-button :color="switchValue == 1 ? '#00778c' : ''"
            @click="queryParams.timeType = 1;dateTimeName='seventytwoHour';now = new Date();now.setDate(1);now.setHours(0,0,0,0);queryParams.oldTime = getFullTimeByDate(now);queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 1;handleMonthPick();handleQuery()" 
            :type="switchValue == 1 ? 'primary' : ''"
          >
            月报
          </el-button>
          <el-button :color="switchValue == 2 ? '#00778c' : ''"
            @click="queryParams.timeType = 2;queryParams.oldTime = null;queryParams.newTime = null;queryParams.timeArr = null;visControll.visAllReport = false;switchValue = 2;" 
            :type="switchValue == 2 ? 'primary' : ''"
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
              <el-form-item>
          <el-select v-model="queryParams.dataType" placeholder="请选择" style="width: 100px">
            <el-option label="最大" :value="1" />
            <el-option label="平均" :value="0" />
            <el-option label="最小" :value="-1" />
          </el-select>
        </el-form-item>
          <!-- <el-button @click="handleExport"  ><Icon icon="ep:search" class="mr-5px" :loading="true" /> 导出</el-button> -->
        </el-form-item>
        <!-- <el-text size="large">
          报警次数：{{ pduInfo.alarm }}
        </el-text> -->
      </el-form>
    </template>
    <template #Content>
      <div id="pdfRef">
      <div v-show="visControll.visAllReport" class="page" >
        <div class="page-con">
          <div class="pageBox" >
            <div class="page-conTitle">
              机柜基本信息
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
            </el-row>
          </div>

                    <div class="pageBox" >
            <div class="page-conTitle">
              配电设备信息
            </div>
            <br/>
          </div>
              <el-table :data="tableData" style="width: 100%" :border="true">
                                    <el-table-column  align="center" label="序号" type="index" prop="id" width="100px"/>
      <el-table-column prop="devKey" label="网络地址" align="center"/>
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
      <el-table-column prop="eleActive" label="耗电量（kWh）" align="center"/>
      <el-table-column prop="volUnbalance" label="电压不平衡" align="center"/>
      <el-table-column prop="curUnbalance" label="电流不平衡" align="center"/>
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button v-if="switchValue == 0" @click="generateDailyReport(scope.row.devKey)">日报</el-button>
          <el-button v-else-if="switchValue == 1" @click="generateMonthlyReport(scope.row.devKey)">月报</el-button>
        </template>
      </el-table-column>
    </el-table>

          <!-- <div class="pageBox" >
            <div class="page-conTitle">
              机架基本信息
            </div>
            <br/>
            <el-row :gutter="24" >
              <el-col :span="24 - serChartContainerWidth">
                <div class="centered-div">
                  <el-table 
                    :data="rack" 
                    :header-cell-style="arraySpanMethod"
                    >
                    <el-table-column  align="center" label="序号" type="index" prop="id" width="100px"/>
                    <el-table-column  align="center" label="名称" prop="name"  />
                    <el-table-column  align="center" label="总功率(kW)" prop="totalPower" />
                    <el-table-column  align="center" label="A路电流(A)" prop="acurrent" />
                    <el-table-column  align="center" label="B路电流(A)" prop="bcurrent" />
                    <el-table-column label="操作" align="center">
                    <template #default="scope">
                    <el-button v-if="switchValue==0" @click="generateDailyReport(scope.row.id)">详情</el-button>
                    <el-button v-if="switchValue==1" @click="generateMonthlyReport(scope.row.id)">详情</el-button>
                    
                    </template>
      </el-table-column>
                  </el-table>
                </div>
              </el-col>
            </el-row>
          </div> -->
          <div class="pageBox" v-if="visControll.eqVis" >
            <div class="page-conTitle" >
              电量分布
            </div>
            <!-- <p class="paragraph" v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，最大用电量{{eqData.maxEle}}kWh， 最大负荷发生时间{{eqData.maxEleTime}}</p>
            <p class="paragraph" v-if="visControll.isSameDay && eqData.firstEq">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p> -->
              <p v-if="!visControll.isSameDay">本周期内，共计使用电量{{eqData.totalEle}}kWh，{{eqData.maxEle == 0 ? '用电量' + eqData.maxEle : '最大单日用电量' + eqData.maxEle}}kWh， （发生时间{{eqData.maxEleTime}}）</p>
            <!-- <p v-if="visControll.isSameDay">本周期内，开始时电能为{{eqData.firstEq}}kWh，结束时电能为{{eqData.lastEq}}kWh， 电能增长{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh</p> -->
            <p v-if="visControll.isSameDay">本周期内，共计使用电量{{(eqData.lastEq - eqData.firstEq).toFixed(1)}}kWh，最大用电量{{eqData.maxEle}}kWh， （发生时间{{eqData.maxEleTime}}）</p>

            <Bar class="Container" width="70vw" height="58vh" :list="eleList"/>
          </div>
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


          <div class="pageBox" v-if="visControll.lineACurVis">
            <div class="page-conTitle">
              相电流历史曲线趋势图
            </div>
                        
          <div v-for="(sensor, index) in lineCurList?.series" :key="index">
            <div class="power-section single-line" v-if="index %2 == 0">
              <span class="power-title">{{lineCurVolData.res[`curName${index + 1}`]}}</span>
  <span class="power-value">峰值 <span class="highlight">{{lineCurVolData.res[`curMaxValue${index + 1}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData.res[`curMaxTime${index + 1}`]}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{lineCurVolData.res[`curMinValue${index + 1}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData.res[`curMinTime${index + 1}`]}})</span></span>
          <span  class="separator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <span class="power-title" v-if="index+2<=lineCurList?.series.length "> {{lineCurVolData.res[`curName${index + 2}`]}}</span>
  <span class="power-value" v-if="index+2<=lineCurList?.series.length ">峰值 <span class="highlight">{{lineCurVolData.res[`curMaxValue${index + 2}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData.res[`curMaxTime${index + 2}`]}})</span></span>
  <span class="power-value" v-if="index+2<=lineCurList?.series.length ">谷值 <span class="highlight">{{lineCurVolData.res[`curMinValue${index + 2}`].toFixed(2)}}</span> A <span class="time">记录于({{lineCurVolData.res[`curMinTime${index + 2}`]}})</span></span>
</div>
          </div>
            <ACurLine class="adaptiveStyle" :list="lineCurList"  :dataType="queryParams.dataType"/>
          </div>
          <div class="pageBox" v-if="visControll.lineAVolVis">
            <div class="page-conTitle">
              相电压历史曲线趋势图
            </div>
                        <div v-for="(sensor, index) in lineVolList?.series" :key="index">
            <div class="power-section single-line" v-if="index %2 == 0">
              <span class="power-title">{{lineCurVolData.res[`volName${index + 1}`]}}</span>
  <span class="power-value">峰值 <span class="highlight">{{lineCurVolData.res[`volMaxValue${index + 1}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData.res[`volMaxTime${index + 1}`]}})</span></span>
  <span class="power-value">谷值 <span class="highlight">{{lineCurVolData.res[`volMinValue${index + 1}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData.res[`volMinTime${index + 1}`]}})</span></span>
          <span  class="separator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
  <span class="power-title" v-if="index+2<=lineVolList?.series.length ">{{lineCurVolData.res[`volName${index + 2}`]}}</span>
  <span class="power-value" v-if="index+2<=lineVolList?.series.length ">峰值 <span class="highlight">{{lineCurVolData.res[`volMaxValue${index + 2}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData.res[`volMaxTime${index + 2}`]}})</span></span>
  <span class="power-value" v-if="index+2<=lineVolList?.series.length ">谷值 <span class="highlight">{{lineCurVolData.res[`volMinValue${index + 2}`].toFixed(1)}}</span> V <span class="time">记录于({{lineCurVolData.res[`volMinTime${index + 2}`]}})</span></span>
</div>

          </div>
            <AVolLine class="adaptiveStyle" :list="lineVolList"  :dataType="queryParams.dataType"/>
          </div>
          <!-- <div class="pageBox" v-if="visControll.lineBCurVis">
            <div class="page-conTitle">
              B路相电流历史曲线趋势图
            </div>
            <BBCurLine class="adaptiveStyle" :list="lineBCurList"  :dataType="queryParams.dataType"/>
          </div> -->
          <!-- <div class="pageBox" v-if="visControll.lineBVolVis">
            <div class="page-conTitle">
              B路相电压历史曲线趋势图
            </div>
            <BVolLine class="adaptiveStyle" :list="lineBVolList"  :dataType="queryParams.dataType"/>
          </div> -->

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

 
<!--     
          <div class="pageBox" v-if="visControll.BpowVis">
            <div class="page-conTitle" >
              机架耗电电量排名（先写死，后续根据需要修改）
            </div>
            <HorizontalBar :width="computedWidth" height="58vh" />
          </div> -->
          <!-- <div class="pageBox" v-if="visControll.outletVis">
            <div class="page-conTitle" >
              输出位电量排名
            </div>
            <div ref="outputRankChartContainer" id="outputRankChartContainer" style="width: 70vw; height: 58vh;"></div>
          </div> -->
          <div class="pageBox" v-if="visControll.iceTemVis">
            <div class="page-conTitle">
             机柜温度曲线
            </div>
            <!-- <p class="paragraph" v-show="iceTemList.temMaxValue">本周期内，最高温度{{iceTemList.temMaxValue}}°C， 最高温度发生时间{{iceTemList.temMaxTime}}，由温度传感器{{iceTemList.temMaxSensorId}}采集得到</p>
            <p class="paragraph" v-show="iceTemList.temMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低温度{{iceTemList.temMinValue}}°C， 最高温度发生时间{{iceTemList.temMinTime}}，由温度传感器{{iceTemList.temMinSensorId}}采集得到</p> -->
          <div v-for="(sensor, index) in temList?.series" :key="index">
        <div class="power-section single-line" v-if="index %2==0">
        <span class="power-title">{{ allTemHumData[`temName${index + 1}`] }}极值：</span>
        <span class="power-value">峰值 <span class="highlight">{{ allTemHumData[`temMaxValue${index + 1}`] }}</span> °C <span class="time">记录于({{ allTemHumData[`temMaxTime${index + 1}`] }})</span></span>
        <span class="power-value">谷值 <span class="highlight">{{ allTemHumData[`temMinValue${index + 1}`] }}</span> °C <span class="time">记录于({{ allTemHumData[`temMinTime${index + 1}`] }})</span></span>
                <span  class="separator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="index+2<=temList?.series.length ">{{ allTemHumData[`temName${index + 1}`] }}极值：</span>
        <span class="power-value" v-if="index+2<=temList?.series.length ">峰值 <span class="highlight">{{ allTemHumData[`temMaxValue${index + 2}`] }}</span> °C <span class="time">记录于({{ allTemHumData[`temMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="index+2<=temList?.series.length ">谷值 <span class="highlight">{{ allTemHumData[`temMinValue${index + 2}`] }}</span> °C <span class="time">记录于({{ allTemHumData[`temMinTime${index + 2}`] }})</span></span>
      </div>
            </div>
            <EnvTemLine class="Container" width="70vw" height="58vh" :list="temList" :dataType="queryParams.dataType"/>
          </div>
          <!-- <div class="pageBox" v-if="visControll.hotTemVis">
            <div class="page-conTitle">
              热通道温度曲线
            </div>
            <p class="paragraph" v-show="hotTemList.temMaxValue">本周期内，最高温度{{hotTemList.temMaxValue}}°C， 最高温度发生时间{{hotTemList.temMaxTime}}，由温度传感器{{hotTemList.temMaxSensorId}}采集得到</p>
            <p class="paragraph" v-show="hotTemList.temMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低温度{{hotTemList.temMinValue}}°C， 最高温度发生时间{{hotTemList.temMinTime}}，由温度传感器{{hotTemList.temMinSensorId}}采集得到</p>
            <EnvTemLine class="Container" width="70vw" height="58vh" :list="hotTemList" />
          </div> -->
            <div class="pageBox" v-if="visControll.iceTemVis">
            <div class="page-conTitle">
             机柜湿度曲线
            </div>
            <!-- <p class="paragraph" v-show="iceTemList.humMaxValue">本周期内，最高湿度{{iceTemList.humMaxValue}}%RH， 最高湿度发生时间{{iceTemList.humMaxTime}}，由湿度传感器{{iceTemList.humMaxSensorId}}采集得到</p>
            <p class="paragraph" v-show="iceTemList.humMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低湿度{{iceTemList.humMinValue}}%RH， 最高湿度发生时间{{iceTemList.humMinTime}}，由湿度传感器{{iceTemList.humMinSensorId}}采集得到</p> -->
          <div v-for="(sensor, index) in humList?.series" :key="index">
        <div class="power-section single-line" v-if="index %2==0">
        <span class="power-title">{{ allTemHumData[`humName${index + 1}`] }}极值：</span>
        <span class="power-value">峰值 <span class="highlight">{{ allTemHumData[`humMaxValue${index + 1}`] }}</span> % <span class="time">由{{ allTemHumData[`humName${index + 1}`] }}+{{allTemHumData[`humMaxSensorId${index + 1}`]}}记录于({{ allTemHumData[`humMaxTime${index + 1}`] }})</span></span>
        <span class="power-value">谷值 <span class="highlight">{{ allTemHumData[`humMinValue${index + 1}`] }}</span> % <span class="time">由{{ allTemHumData[`humName${index + 1}`] }}+{{allTemHumData[`humMinSensorId${index + 1}`]}}记录于({{ allTemHumData[`humMinTime${index + 1}`] }})</span></span>
                <span  class="separator">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
        <span class="power-title" v-if="index+2<=humList?.series.length ">{{ allTemHumData[`humName${index + 1}`] }}极值：</span>
        <span class="power-value" v-if="index+2<=humList?.series.length ">峰值 <span class="highlight">{{ allTemHumData[`humMaxValue${index + 2}`] }}</span> % <span class="time">由{{ allTemHumData[`humName${index + 1}`] }}+{{allTemHumData[`humMaxSensorId${index + 2}`]}}记录于({{ allTemHumData[`humMaxTime${index + 2}`] }})</span></span>
        <span class="power-value" v-if="index+2<=humList?.series.length ">谷值 <span class="highlight">{{ allTemHumData[`humMinValue${index + 2}`] }}</span> % <span class="time">由{{ allTemHumData[`humName${index + 1}`] }}+{{allTemHumData[`humMinSensorId${index + 2}`]}}记录于({{ allTemHumData[`humMinTime${index + 2}`] }})</span></span>
      </div>
            </div>
            <EnvHumLine class="Container" width="70vw" height="58vh" :list="humList" :dataType="queryParams.dataType"/>
          </div>
        </div>
      
          <!-- <div class="pageBox" v-if="visControll.hotTemVis">
            <div class="page-conTitle">
              热通道湿度曲线
            </div>
            <p class="paragraph" v-show="hotTemList.humMaxValue">本周期内，最高湿度{{hotTemList.humMaxValue}}%RH， 最高湿度发生时间{{hotTemList.humMaxTime}}，由湿度传感器{{hotTemList.humMaxSensorId}}采集得到</p>
            <p class="paragraph" v-show="hotTemList.humMinValue">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;最低湿度{{hotTemList.humMinValue}}%RH， 最高湿度发生时间{{hotTemList.humMinTime}}，由湿度传感器{{hotTemList.humMinSensorId}}采集得到</p>
            <EnvHumLine class="Container" width="70vw" height="58vh" :list="hotTemList" />
        </div> -->

        <div class="pageBox">
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
      </div>
    </div>
    </template>
  </CommonMenu>

  <!-- 表单弹窗：添加/修改 -->
  <!-- <PDUDeviceForm ref="formRef" @success="getList" /> -->
</template>

<script setup lang="ts">
// import download from '@/utils/download'
import { IndexApi } from '@/api/cabinet/index'
import * as echarts from 'echarts';
import { CabinetApi } from '@/api/cabinet/info'
import { ElTree } from 'element-plus'
import Line from './component/Line.vue'
import PFLine from './component/PFLine.vue'
import Bar from './component/Bar.vue'
import EnvTemLine from './component/EnvTemLine.vue'
import EnvHumLine from './component/EnvHumLine.vue'
import { PDUDeviceApi } from '@/api/pdu/pdudevice'
import ACurLine from './component/AcurLine.vue'
import AVolLine from './component/AvolLine.vue'
import HorizontalBar from './component/HorizontalBar.vue'
// 引入方法
import { htmlPdf } from "@/utils/htmlToPDF.js"  
	// 导出成PDF
	const handleExport = (name) => {
	  var fileName= '机柜报表'
	  const fileList = document.getElementsByClassName('pdfRef')   // 很重要
	  htmlPdf(fileName, document.querySelector('#pdfRef'), fileList)
	}

   // 格式化时间戳的方法
const formatTime = (timestamp) => {
    if(timestamp == null){
    return ''
  }
  const date = new Date(timestamp);
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' });
};
/** PDU设备 列表 */
defineOptions({ name: 'PDUDevice' })
const temp1 = ref([]) as any;
const tableData = ref([]);
const AcurVolData = ref();
const BcurVolData = ref();
const dataLoaded = ref(false);
const pfLineData = ref() as any;
const pfLineList = ref() as any;
const iceTemList = ref([]) as any;
const hotTemList = ref([]) as any;
const allTemHumData = ref() as any;
const temList = ref() as any;
const humList = ref() as any;
const eleList = ref() as any;
const factorData = ref() as any;
const totalLineList = ref() as any;
const aLineList = ref() as any;
const bLineList = ref() as any;
const idList = ref() as any;
const now = ref()
const switchValue = ref(1);
const ele = ref();
const lineCurVolData = ref() as any;
const lineCurList = ref() as any;
const lineVolList = ref() as any;



const factor = ref();
let lineidChartA = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainerA = ref<HTMLElement | null>(null);
let lineidChartOneA = null as echarts.ECharts | null; // 显式声明 rankChart 的类型
const lineidChartContainerOneA = ref<HTMLElement | null>(null);
let lineidChartB = null as echarts.ECharts | null;
const lineidChartContainerB = ref<HTMLElement | null>(null);
let lineidChartOneB = null as echarts.ECharts | null;
const lineidChartContainerOneB = ref<HTMLElement | null>(null);
const dateTimeName = ref('seventytwoHour')


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
  flag: false,
  lineACurVis : false,
  lineAVolVis : false,
  lineBCurVis : false,
  lineBVolVis : false,
})
const serChartContainerWidth = ref(0)

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
      push('/report/pdureport?devKey='+devKey+'&timeType='+0+'&oldTime='+getFullTimeByDate(now1.value)+'&newTime='+new1.value+'&timeArr='+null+'&visAllReport='+false+'&switchValue='+0);
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
      push('/report/pdureport?devKey='+devKey+'&timeType='+1+'&oldTime='+getFullTimeByDate(now1.value)+'&newTime='+new1.value+'&timeArr='+null+'&visAllReport='+false+'&switchValue='+1);
    };


// const generateDailyReport = (id) => {
//       push('/u/rackreport?id='+id+'&Type='+0);
//     };

//     const generateMonthlyReport = (id) => {
//       push('/u/rackreport?id='+id+'&Type='+1);
//     };


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
    dataType : 1,
    pduKeyList : []
}) as any

const serverRoomArr =  ref([]) as any

watch( ()=>queryParams.dataType, async()=>{
  //  queryParams.devKey = queryParams.ipAddr;  
  getList()

})

//折叠功能

const getNavList = async() => {
  
  const res = await CabinetApi.getRoomMenuAll({})
  serverRoomArr.value = res
  // const res = await CabinetApi.getRoomMenuAll({})
  // serverRoomArr.value = res
  // if (res && res.length > 0) {
  //   const room = res[0]
  //   const keys = [] as string[]
  //   room.children.forEach(child => {
  //     if(child.children.length > 0) {
  //       child.children.forEach(son => {
  //         keys.push(son.id + '-' + son.type)
  //       })
  //     }
  //   })
  // }
}

const handleClick = (row) => {
  if(row.type != null  && row.type == 3){

    queryParams.Id = row.id
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
  await handleEleQuery();
  await handleConsumeQuery();
  await handlePowQuery();
  await handleIceQuery();
  await handleHotQuery();
  await handleDetailQuery();
  await handlePFLineQuery();
  
      //清除temp1的缓存数据
  temp1.value=[]
  console.log(queryParams.id)
  //获得告警信息
  const preStatus = ref([0])
  const temp1Data = await IndexApi.getBoxRecordPage({

    pageNo: 1,
    pageSize: 100,
    devType: 7,
    likeName: queryParams.Id,
    alarmType: 6,
    pduStartTime: queryParams.oldTime,
    pduFinishTime: queryParams.newTime,
              alarmStatus: preStatus.value,
    
  })
  //处理告警信息数据
  // //debugger
  //处理时间信息
  
  console.log('表格的数据',temp1Data)
  temp1.value = temp1Data.list


  visControll.visAllReport = true;
  loading.value = false

}

const handlePFLineQuery = async () => {
  // const data = await IndexApi.getCabinetPFLine(queryParams);
  pfLineData.value = await IndexApi.getCabinetPFLineByType(queryParams);
  pfLineList.value =  pfLineData.value.pfLineRes;
  //保留俩位小数
  
  
  if(pfLineList.value?.time != null && pfLineList.value?.time?.length > 0){
    visControll.pfVis = true;
  }else {
    visControll.pfVis = false;
  }
}

    


const handleEleQuery = async () => {
  const data = await IndexApi.getEleByCabinetId(queryParams);
  ele.value = data.ele;
  console.log('elekasjklasncklasnckasnk',ele.value);
}

const AlChartData = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[] //电流
});
const AllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});
const AlllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});

const BlChartData = ref({
  volValueList : [] as number[], //电压
  curValueList : [] as number[] //电流
});
const BllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});
const BlllChartData = ref({
  volValueList : [] as number[],
  curValueList : [] as number[]
});

const AlineidDateTimes = ref([] as string[])

  const BlineidDateTimes = ref([] as string[])

  const filterTimesFromDate = (dateTimeStrings, targetDate) => {
  const targetDateObj = new Date(targetDate);
  const targetDateString = targetDateObj.toISOString().split('T')[0]; // YYYY-MM-DD
  
  return dateTimeStrings.filter(dateTimeString => {
    const [datePart, _] = dateTimeString.split(' ');
    return datePart >= targetDateString;
  });
};

// 获取当前日期（或者您可以指定任意日期）
const currentDate = new Date().toISOString().split('T')[0];

const isPDU = ref(true);

const PDUHdaLineHisdata = async () => {
  try {
    // const result = await PDUDeviceApi.getPDUHdaLineHisdataByCabinet({ CabinetId: queryParams.Id, type: dateTimeName.value,oldTime:queryParams.oldTime,newTime:queryParams.newTime });
    lineCurVolData.value = await PDUDeviceApi.getPDUHdaLineHisdataByCabinetByType({ CabinetId: queryParams.Id, type: dateTimeName.value,oldTime:queryParams.oldTime,newTime:queryParams.newTime,dataType:queryParams.dataType });
    
    


    if(lineCurVolData.value?.res?.curRes?.time != null && lineCurVolData.value?.res?.curRes?.time.length > 0){
    lineCurList.value = lineCurVolData.value.res.curRes;
    visControll.lineACurVis = true;
    visControll.lineAVolVis = true;
        dataLoaded.value = true;
    }else{
    visControll.lineACurVis = false;
    visControll.lineAVolVis = false;
    }

       if(lineCurVolData.value?.res?.curRes?.time != null && lineCurVolData.value?.res?.curRes?.time.length > 0){
    lineVolList.value = lineCurVolData.value.res.volRes;
    visControll.lineBCurVis = true;
    visControll.lineBVolVis = true;
        dataLoaded.value = true;
    }else{
    visControll.lineBCurVis = false;
    visControll.lineBVolVis = false;
    }

    
    // AcurVolData.value = result.A;
    // BcurVolData.value = result.B;


  }catch (error) {
    isPDU.value = false;
  }
};
const rack = ref([] as any);
//获取机架信息
const getRackByCabinet = async () => {
  const result = await IndexApi.getRackByCabinet({id : queryParams.Id});
  
  // 保留俩位小数
  for (let i = 0; i < result.length; i++) {
    result[i].acurrent = result[i].acurrent?.toFixed(2);
    result[i].bcurrent = result[i].bcurrent?.toFixed(2);
    result[i].totalPower = result[i].totalPower?.toFixed(3);
  }
  rack.value = result;
  console.log("机架信息",result);
  console.log("机架信息",rack.value);
}

const handleIceQuery = async () => {
  if(queryParams.Id != null){ 
    const data = await IndexApi.getCabinetIceTemAndHumById({id : queryParams.Id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});
    allTemHumData.value =  await IndexApi.getCabinetIceAndHotTemAndHumByTypeById({id : queryParams.Id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime,dataType : queryParams.dataType});
    temList.value = allTemHumData.value.temResult;
    humList.value = allTemHumData.value.humResult;

    if(data?.temResult?.time != null && data?.temResult?.time?.length > 0){
      console.log(1)
      iceTemList.value = data;
      console.log("iceTemList.value ",iceTemList.value )
      if(iceTemList.value?.temMinValue != null){
        iceTemList.value.temMinValue = iceTemList.value?.temMinValue?.toFixed(2);
      }
      if(iceTemList.value?.temMaxValue != null){
        iceTemList.value.temMaxValue = iceTemList.value?.temMaxValue?.toFixed(2);
      }
      visControll.iceTemVis = true;
    }else{
      visControll.iceTemVis = false;
    }
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

const handleHotQuery = async () => {
  if(queryParams.Id != null){
    const data = await IndexApi.getCabinetHotTemAndHumById({id : queryParams.Id,timeType : queryParams.timeType, oldTime : queryParams.oldTime, newTime : queryParams.newTime});
    if(data?.temResult?.time != null && data?.temResult?.time?.length > 0){
      console.log(2)
      hotTemList.value = data;
      if(hotTemList.value?.temMinValue != null){
        hotTemList.value.temMinValue = hotTemList.value?.temMinValue?.toFixed(2);
      }
      if(hotTemList.value?.temMaxValue != null){
        hotTemList.value.temMaxValue = hotTemList.value?.temMaxValue?.toFixed(2);
      }
      visControll.hotTemVis = true;
    }else{
      visControll.hotTemVis = false;
    }
  }
}

const handlePowQuery = async () => {
  // powData.value = await IndexApi.getPowData(queryParams);
   factorData.value = await IndexApi.getPowDataByType(queryParams);
   totalLineList.value = factorData.value.totalLineRes;
   aLineList.value = factorData.value.aLineRes;
   bLineList.value = factorData.value.bLineRes;

  // totalLineList.value = powData.value.totalLineRes;
  // aLineList.value = powData.value.aLineRes;
  // bLineList.value = powData.value.bLineRes;

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
  
  // 设置 pduKeyList，确保只包含非空值
  const pduKeys = [eqData.value?.pduKeyA, eqData.value?.pduKeyB].filter(key => key != null);
  queryParams.pduKeyList = pduKeys;
  const baseInfoList = await IndexApi.getPduBasicInformation(queryParams);

const processedData = baseInfoList.map(item => ({
    devKey: item.ipAddress,
    runStatus: item.status,
    powApparent: formatNumber(item.powApparent),
    powActive: formatNumber(item.powActive),
    powReactive: formatNumber(item.powReactive),
    powerFactor: formatNumber(item.powerFactor),
    eleActive: formatNumber(item.eleActive),
    volUnbalance : formatNumber(item.volUnbalance)+'%',
    curUnbalance : formatNumber(item.curUnbalance)+'%',
}));

// 将处理后的数据存储在 tableData.value 中
tableData.value = processedData;

// 输出最终的 tableData.value
console.log('tableData.value', tableData.value);


}
// 对数据进行处理，保留三位小数
// 辅助函数：处理数值并保留三位小数
function formatNumber(value) {
    if (typeof value !== 'number' || !Number.isFinite(value)) {
        return 0; // 或者返回其他默认值
    }
    return Number(value.toFixed(3));
}

  



const handleDetailQuery = async () => {
  var temp = [] as any;
  
  var CabinetInfo1 = await CabinetApi.getCabinetDetail({id : queryParams.Id});
  var CabinetInfo = CabinetInfo1.redisData;
const aPowActive = CabinetInfo?.cabinet_power?.path_a?.pow_active;
const bPowActive = CabinetInfo?.cabinet_power?.path_b?.pow_active;

// 定义 result 变量，明确类型为 number | null
const result = ref<string | null>(null);

// 检查 aPowActive 和 bPowActive 是否都存在
const ab = aPowActive != null && bPowActive != null;

if (ab) {
    // 计算差的绝对值
    const difference = Math.abs(aPowActive - bPowActive);

    // 计算最大值
    const maxPowActive = Math.max(aPowActive, bPowActive);

    // 计算结果
    const numericResult = maxPowActive !== 0 ? (difference / maxPowActive) * 100 : 0;

    // 将结果转换为百分比格式
    result.value = numericResult.toFixed(2) + '%';
} else {
    // 如果 aPowActive 或 bPowActive 不存在，result 为 null
    result.value = null;
}
  

  var apow = CabinetInfo?.cabinet_power?.path_a?.pow_active;
  var bpow = CabinetInfo?.cabinet_power?.path_b?.pow_active;
  var percentageValue = 50 as any;
  if(apow == null && bpow == null){
    percentageValue = null;
  } else if (apow != null && bpow == null){
    percentageValue = 100;
  } else if (apow == null && bpow != null){
    percentageValue = 0;
  } else if (apow != 0 && bpow == 0){
    percentageValue = 100;
  } else if (apow == 0 && bpow != 0){
    percentageValue = 0;
  } else if (apow != 0 && bpow != 0) {
    percentageValue = apow / (apow + bpow);
    percentageValue *= 100;
  }
  
  temp.push({
    baseInfoName : "所属位置",
    baseInfoValue : CabinetInfo1?.name,
    consumeName : "当前总视在功率",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_apparent != null ? CabinetInfo?.cabinet_power?.total_data?.pow_apparent?.toFixed(3) + "kVA" : '--',
    percentageName: "当前AB路占比",
    percentageValue: percentageValue?.toFixed(0),
  })
  temp.push({
    baseInfoName : "电力容量",
    baseInfoValue : CabinetInfo?.pow_capacity != null ?  CabinetInfo?.pow_capacity+'KW' : '--',
    consumeName : "当前总有功功率",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_active != null ? CabinetInfo?.cabinet_power?.total_data?.pow_active?.toFixed(3) + "kW" : '/',
    percentageName: "A路有功功率",
    percentageValue: CabinetInfo?.cabinet_power?.path_a !=null ?CabinetInfo?.cabinet_power?.path_a.pow_active.toFixed(3)+'kW':'--',
  })
  temp.push({
    baseInfoName : "负载率",
    baseInfoValue : CabinetInfo?.load_factor != null ? CabinetInfo?.load_factor?.toFixed(2) + "%" : '--',
    consumeName : "当前总无功功率",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.pow_reactive != null ? CabinetInfo?.cabinet_power?.total_data?.pow_reactive?.toFixed(3) + "kVar" : '--',
    percentageName: "B路有功功率",
    percentageValue: CabinetInfo?.cabinet_power?.path_b !=null ?CabinetInfo?.cabinet_power?.path_b.pow_active.toFixed(3)+'kW':'--',
  })
  temp.push({
    baseInfoName : "耗电量",
    baseInfoValue : (ele.value || 0).toFixed(1) + "kWh",
    consumeName : "当前功率因素",
    consumeValue : CabinetInfo?.cabinet_power?.total_data?.power_factor != null ? CabinetInfo?.cabinet_power?.total_data?.power_factor?.toFixed(2) : '--',
    percentageName: "偏差率",
    percentageValue: result.value != null ? result : '--',
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

  if(queryParams.Id){
    if(queryParams.oldTime && queryParams.newTime){
      await getList();
      await PDUHdaLineHisdata();
      // await lineidFlashChartData();
      await getRackByCabinet();
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


import { useRoute, useRouter } from 'vue-router';
const route = useRoute();

/** 初始化 **/
onMounted( async () =>  {
    let id = route.query?.CabinetId as string | undefined;
  let timeType = route.query?.timeType as string | undefined;
  let oldTime = route.query?.oldTime as string | undefined;
  let newTime = route.query?.newTime as string | undefined;
  let visAllReport = route.query?.visAllReport as string | undefined;
  let switchValue1 = route.query?.switchValue as string | undefined;

  if (id != undefined) {
    queryParams.ipAddr = id;
    queryParams.Id = id;
    queryParams.timeType = timeType;
    queryParams.oldTime = oldTime;
    queryParams.newTime = newTime;
    queryParams.visAllReport = visAllReport;
      switchValue.value = switchValue1;
    getList();
    
  }
  getNavList()
  // getList();
  // initChart();
  idList.value = await loadAll();
  // window.addEventListener('resize', updateWindowWidth);
  // getList();
  // initChart();
  getNavList();
  idList.value = await loadAll();
  
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
    background: #f6f6f6 !important;
    color: black;
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
