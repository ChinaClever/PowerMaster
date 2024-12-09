<template>
  <ContentWrap>
    <div>
      <el-upload
        ref="uploadRef"
        v-model:file-list="fileList"
        :action="uploadUrl"
        :auto-upload="false"
        :data="data"
        :disabled="formLoading"
        :limit="1"
        :on-change="handleFileChange"
        :on-error="submitFormError"
        :on-exceed="handleExceed"
        :on-success="submitFormSuccess"
        :http-request="httpRequest"
        accept=".doc, .docx"
        drag
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text"> 将文件拖到此处，或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip" style="color: red">
            提示：仅允许导入 doc、docx 格式文件！
          </div>
        </template>
      </el-upload>

      <el-button :disabled="formLoading" type="primary" @click="submitFileForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </div>
  </ContentWrap>
  <ContentWrap>
    <el-descriptions
      title="New Busbar始端箱产品技术规格说明书"
      :column="3"
      size="large"
      border
    >
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">设计编号 </div>
      </template>
      {{ resData.designNumber }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">编制日期</div>
      </template>
      {{ resData.designDate }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">设计单位</div>
      </template>
      {{ resData.architect }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">数量</div>
      </template>
      {{ resData.number }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">产品类别 </div>
      </template>
      {{ resData.productCategory }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">产品系列 </div>
      </template>
      {{ resData.productLine }}
    </el-descriptions-item>
    <el-descriptions-item :span="3">
      <template #label>
        <div class="cell-item">交货日期 </div>
      </template>
      {{ resData.deliveryDate }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">运输方式 </div>
      </template>
      {{ resData.modeOfShipping }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">部门 </div>
      </template>
      {{ resData.department }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">设计人 </div>
      </template>
      {{ resData.designer }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">客户编码</div>
      </template>
      {{ resData.customerCode }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">生产车间</div>
      </template>
      {{ resData.manufacturingShop }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">受订单号</div>
      </template>
      {{ resData.orderNumber }}
    </el-descriptions-item>
    <el-descriptions-item  :span="1">
      <template #label>
        <div class="cell-item">客户名称</div>
      </template>
      {{ resData.customerName }}
    </el-descriptions-item>
    <el-descriptions-item :span="2">
      <template #label>
        <div class="cell-item">客户型号</div>
      </template>
      {{ resData.customerModel }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">成品代码</div>
      </template>
      {{ resData.deviceCode }}
    </el-descriptions-item>
    <el-descriptions-item>
      <template #label>
        <div class="cell-item">规格型号</div>
      </template>
      {{ resData.deviceType }}
    </el-descriptions-item>
    </el-descriptions>
  </ContentWrap>
</template>

<script lang="ts" setup>
import { useUpload } from '@/components/UploadFile/src/useUpload'
const { httpRequest } = useUpload()
const { uploadUrl } = useUpload()
// const uploadUrl = ref('http://localhost:48088/admin-api/bus/file-upload') // 后端上传地址
const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const formLoading = ref(false) // 表单的加载中
const fileList = ref([]) // 文件列表
const data = ref({ path: '' })
const uploadRef = ref()
const resData = reactive({
  designNumber: '',
  designDate: '',
  architect: '',
  number: '',
  productCategory: '',
  productLine: '',
  deliveryDate: '',
  modeOfShipping: '',
  department: '',
  designer: '',
  customerCode: '',
  manufacturingShop: '',
  orderNumber: '',
  customerName: '',
  customerModel: '',
  deviceCode: '',
  deviceType: '',
  downloadUrl: ''
})

/** 处理上传的文件发生变化 */
const handleFileChange = (file) => {
  data.value.path = file.name
}

/** 提交表单 */
const submitFileForm = () => {
  if (fileList.value.length == 0) {
    message.error('请上传文件')
    return
  }
  unref(uploadRef)?.submit()
}

/** 文件上传成功处理 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitFormSuccess = (res) => {
  //先清空再赋值
  Object.keys(res.data).forEach(key => {
    resData[key] = '';
  });
  Object.keys(res.data).forEach(key => {
    resData[key] = res.data[key];
  });
  if (resData.downloadUrl != null){
    console.log(res)
    const link = document.createElement('a');
    link.href = resData.downloadUrl;
    link.download = resData.orderNumber + '+' + resData.deviceCode;  // 设置下载的文件名，可以根据需求动态设置
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }
  // 清理
  formLoading.value = false
  unref(uploadRef)?.clearFiles()
  // 提示成功，并刷新
  message.success(t('common.createSuccess'))
  emit('success')
}

/** 上传错误提示 */
const submitFormError = (): void => {
  message.error('上传失败，请您重新上传！')
  formLoading.value = false
}

/** 重置表单 */
const resetForm = () => {
  // 重置上传状态和文件
  formLoading.value = false
  uploadRef.value?.clearFiles()
}

/** 文件数超出提示 */
const handleExceed = (): void => {
  message.error('最多只能上传一个文件！')
}


</script>



<style scoped>
.cell-item {
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  width: auto;
}
</style>