<template>
  <div>
    <el-card>
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Export_Problem') }}</span>
        <div class="filter-row">
          <span>
            <el-button
              type="primary"
              size="small"
              @click="exportProblems"
              icon="el-icon-arrow-down"
              >{{ $t('m.Export') }}
            </el-button>
          </span>
          <span>
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              size="medium"
              @keyup.enter.native="filterByKeyword"
              @search-click="filterByKeyword"
            ></vxe-input>
          </span>
        </div>
      </div>
      <vxe-table
        :data="problems"
        stripe
        auto-resize
        ref="xTable"
        :loading="loadingProblems"
        :checkbox-config="{ labelField: '', highlight: true, range: true }"
        @checkbox-change="handleSelectionChange"
        @checkbox-all="handlechangeAll"
      >
        <vxe-table-column type="checkbox" width="60"> </vxe-table-column>
        <vxe-table-column title="ID" min-width="100" field="id">
        </vxe-table-column>
        <vxe-table-column min-width="150" :title="$t('m.Title')" field="title">
        </vxe-table-column>
        <vxe-table-column
          min-width="150"
          field="author"
          :title="$t('m.Author')"
        >
        </vxe-table-column>

        <vxe-table-column field="gmtCreate" :title="$t('m.Created_Time')">
          <template v-slot="{ row }">
            {{ row.gmtCreate | localtime }}
          </template>
        </vxe-table-column>
      </vxe-table>

      <div class="panel-options">
        <el-pagination
          class="page"
          layout="prev, pager, next, sizes"
          @current-change="getProblems"
          :page-size="limit"
          :page-sizes="[10, 50, 100, 500]"
           @size-change="handleSizeChange"
          :total="total"
        >
        </el-pagination>
      </div>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{ $t('m.Import_Problem') }}</span>
      </div>
      <el-upload
        ref="hoj"
        action="/api/file/import-problem"
        name="file"
        :file-list="fileList1"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile1Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
        :before-upload="beforeHojUpload"
      >
        <el-button
          size="small"
          :loading="loading.hoj"
          type="primary"
          slot="trigger"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('hoj')"
          :loading="loading.hoj"
          :disabled="!fileList1.length"
          icon="el-icon-upload"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Import_QDUOJ_Problem')
        }}</span>
      </div>
      <el-upload
        ref="qduoj"
        action="/api/file/import-qdoj-problem"
        name="file"
        :file-list="fileList2"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile2Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
        :before-upload="beforeQduojUpload"
      >
        <el-button
          size="small"
          type="primary"
          slot="trigger"
          :loading="loading.qduoj"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('qduoj')"
          :loading="loading.qduoj"
          icon="el-icon-upload"
          :disabled="!fileList2.length"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Import_FPS_Problem')
        }}</span>
      </div>
      <el-upload
        ref="fps"
        action="/api/file/import-fps-problem"
        name="file"
        :file-list="fileList3"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile3Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
        :before-upload="beforeFpsUpload"
      >
        <el-button
          size="small"
          type="primary"
          slot="trigger"
          :loading="loading.fps"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('fps')"
          :loading="loading.fps"
          icon="el-icon-upload"
          :disabled="!fileList3.length"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>

    <el-card style="margin-top:15px">
      <div slot="header">
        <span class="panel-title home-title">{{
          $t('m.Import_Hydro_Problem')
        }}</span>
      </div>
      <el-upload
        ref="hydro"
        action="/api/file/import-hydro-problem"
        name="file"
        :file-list="fileList4"
        :show-file-list="true"
        :with-credentials="true"
        :limit="3"
        :on-change="onFile4Change"
        :auto-upload="false"
        :on-success="uploadSucceeded"
        :on-error="uploadFailed"
        :before-upload="beforeHydroUpload"
      >
        <el-button
          size="small"
          type="primary"
          slot="trigger"
          :loading="loading.hydro"
          icon="el-icon-folder-opened"
          >{{ $t('m.Choose_File') }}</el-button
        >
        <el-button
          style="margin-left: 10px;"
          size="small"
          type="success"
          @click="submitUpload('hydro')"
          :loading="loading.hydro"
          icon="el-icon-upload"
          :disabled="!fileList4.length"
          >{{ $t('m.Upload') }}</el-button
        >
      </el-upload>
    </el-card>
  </div>
</template>
<script>
import api from '@/common/api';
import utils from '@/common/utils';
import myMessage from '@/common/message';
import JSZip from 'jszip';
export default {
  name: 'import_and_export',
  data() {
    return {
      fileList1: [],
      fileList2: [],
      fileList3: [],
      fileList4: [],
      page: 1,
      limit: 10,
      total: 0,
      loadingProblems: false,
      loadingImporting: false,
      keyword: '',
      problems: [],
      selected_problems: [],
      loading: {
        hoj: false,
        qduoj: false,
        fps: false,
        hydro:false,
      },
      uploadTimeout: null, // 上传超时ID
    };
  },
  mounted() {
    this.getProblems();
  },
  beforeDestroy() {
    // 组件销毁前清除超时
    if (this.uploadTimeout) {
      clearTimeout(this.uploadTimeout);
    }
  },
  methods: {
    // 题目表部分勾选 改变选中的内容
    handleSelectionChange({ records }) {
      this.selected_problems = records;
    },

    // 一键全部选中，改变选中的内容列表
    handlechangeAll() {
      this.selected_problems = this.$refs.xTable.getCheckboxRecords();
    },

    handleSizeChange(pageSize){
      this.limit = pageSize;
      this.getProblems();
    },

    getProblems(page = 1) {
      let params = {
        keyword: this.keyword,
        currentPage: page,
        limit: this.limit,
        oj: 'Mine',
      };
      this.loadingProblems = true;
      api.admin_getProblemList(params).then((res) => {
        this.problems = res.data.data.records;
        this.total = res.data.data.total;
        this.loadingProblems = false;
      });
    },
    exportProblems() {
      let params = [];
      if (this.selected_problems.length <= 0) {
        myMessage.error(this.$i18n.t('m.Export_Problem_NULL_Tips'));
        return;
      }
      for (let p of this.selected_problems) {
        params.push('pid=' + p.id);
      }
      let url = '/api/file/export-problem?' + params.join('&');
      utils.downloadFile(url);
    },
    submitUpload(ref) {
      this.loading[ref] = true;
      
      // 显示上传进度提示
      const loadingInstance = this.$loading({
        lock: true,
        text: '正在上传大文件，请耐心等待...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
      
      // 设置上传超时时间
      const uploadTimeout = setTimeout(() => {
        if (this.loading[ref]) {
          this.loading[ref] = false;
          loadingInstance.close();
          this.$message.error('文件上传超时，请检查网络连接或尝试较小的文件');
        }
      }, 600000); // 10分钟超时
      
      // 保存超时ID以便在成功或失败时清除
      this.uploadTimeout = uploadTimeout;
      
      this.$refs[ref].submit();
    },
    onFile1Change(file, fileList) {
      this.fileList1 = fileList.slice(-1);
    },
    onFile2Change(file, fileList) {
      this.fileList2 = fileList.slice(-1);
    },
    onFile3Change(file, fileList) {
      this.fileList3 = fileList.slice(-1);
    },
    onFile4Change(file, fileList) {
      this.fileList4 = fileList.slice(-1);
    },
    async beforeHojUpload(file) {
      // 提高HOJ导入文件大小限制到2GB
      const maxSize = 2 * 1024 * 1024 * 1024; // 2GB
      if (file.size > maxSize) {
        this.$message.error(`文件大小超过限制，最大支持2GB，当前文件大小：${(file.size / 1024 / 1024 / 1024).toFixed(2)}GB`);
        return false;
      }
      
      // 检查并补全config.yml
      try {
        const processedFile = await this.checkAndCompleteConfig(file, 'hoj');
        // 如果文件被修改了，需要更新上传组件的文件列表
        if (processedFile !== file) {
          // 延迟更新文件列表，确保上传组件使用新文件
          this.$nextTick(() => {
            this.fileList1 = [{
              name: processedFile.name,
              size: processedFile.size,
              raw: processedFile
            }];
          });
        }
        return true;
      } catch (error) {
        console.error('处理config.yml失败:', error);
        return true; // 即使失败也允许上传
      }
    },
    async beforeQduojUpload(file) {
      // 提高QDUOJ导入文件大小限制到2GB
      const maxSize = 2 * 1024 * 1024 * 1024; // 2GB
      if (file.size > maxSize) {
        this.$message.error(`文件大小超过限制，最大支持2GB，当前文件大小：${(file.size / 1024 / 1024 / 1024).toFixed(2)}GB`);
        return false;
      }
      
      // 检查并补全config.yml
      try {
        const processedFile = await this.checkAndCompleteConfig(file, 'qduoj');
        if (processedFile !== file) {
          this.$nextTick(() => {
            this.fileList2 = [{
              name: processedFile.name,
              size: processedFile.size,
              raw: processedFile
            }];
          });
        }
        return true;
      } catch (error) {
        console.error('处理config.yml失败:', error);
        return true;
      }
    },
    async beforeFpsUpload(file) {
      // 提高FPS导入文件大小限制到2GB
      const maxSize = 2 * 1024 * 1024 * 1024; // 2GB
      if (file.size > maxSize) {
        this.$message.error(`文件大小超过限制，最大支持2GB，当前文件大小：${(file.size / 1024 / 1024 / 1024).toFixed(2)}GB`);
        return false;
      }
      
      // 检查并补全config.yml
      try {
        const processedFile = await this.checkAndCompleteConfig(file, 'fps');
        if (processedFile !== file) {
          this.$nextTick(() => {
            this.fileList3 = [{
              name: processedFile.name,
              size: processedFile.size,
              raw: processedFile
            }];
          });
        }
        return true;
      } catch (error) {
        console.error('处理config.yml失败:', error);
        return true;
      }
    },
    async beforeHydroUpload(file) {
      // 提高hydro导入文件大小限制到2GB
      const maxSize = 2 * 1024 * 1024 * 1024; // 2GB
      if (file.size > maxSize) {
        this.$message.error(`文件大小超过限制，最大支持2GB，当前文件大小：${(file.size / 1024 / 1024 / 1024).toFixed(2)}GB`);
        return false;
      }
      
      // 检查并补全config.yml
      try {
        const processedFile = await this.checkAndCompleteConfig(file, 'hydro');
        if (processedFile !== file) {
          this.$nextTick(() => {
            this.fileList4 = [{
              name: processedFile.name,
              size: processedFile.size,
              raw: processedFile
            }];
          });
        }
        return true;
      } catch (error) {
        console.error('处理config.yml失败:', error);
        return true;
      }
    },
    uploadSucceeded(response, file, fileList) {
      // 清除所有加载状态
      this.loading.hoj = false;
      this.loading.qduoj = false;
      this.loading.fps = false;
      this.loading.hydro = false;
      
      // 清除上传超时
      if (this.uploadTimeout) {
        clearTimeout(this.uploadTimeout);
        this.uploadTimeout = null;
      }
      
      // 关闭加载提示
      this.$loading().close();
      
      if (response.status != 200) {
        myMessage.error(response.msg);
        this.$notify.error({
          title: this.$i18n.t('m.Error'),
          message: response.msg,
          dangerouslyUseHTMLString: true,
          duration: 8000
        });
      } else {
        myMessage.success(this.$i18n.t('m.Upload_Problem_Succeeded'));
        this.getProblems();
      }
    },
    uploadFailed() {
      // 清除所有加载状态
      this.loading.hoj = false;
      this.loading.qduoj = false;
      this.loading.fps = false;
      this.loading.hydro = false;
      
      // 清除上传超时
      if (this.uploadTimeout) {
        clearTimeout(this.uploadTimeout);
        this.uploadTimeout = null;
      }
      
      // 关闭加载提示
      this.$loading().close();
      
      myMessage.error(this.$i18n.t('m.Upload_Problem_Failed'));
    },
    filterByKeyword() {
      this.getProblems();
    },
    // 生成默认的config.yml内容
    generateDefaultConfig() {
      return `type: default
subtasks:
  - score: 100
    type: min
    cases: ['1']
  - score: 0
    type: min
    cases: ['2']
  - score: 0
    type: min
    cases: ['3']
  - score: 0
    type: min
    cases: ['4']
  - score: 0
    type: min
    cases: ['5']
  - score: 0
    type: min
    cases: ['6']
  - score: 0
    type: min
    cases: ['7']
  - score: 0
    type: min
    cases: ['8']
  - score: 0
    type: min
    cases: ['9']
  - score: 0
    type: min
    cases: ['10']
`;
    },
    // 检查并补全config.yml文件
    async checkAndCompleteConfig(file, uploadType) {
      try {
        // 只处理zip文件
        if (!file.name.toLowerCase().endsWith('.zip')) {
          return file;
        }

        const zip = new JSZip();
        const zipContent = await zip.loadAsync(file);
        
        // 检查是否已有config.yml或config.yaml
        const hasConfig = zipContent.files['config.yml'] || zipContent.files['config.yaml'];
        
        if (!hasConfig) {
          // 生成默认config.yml
          const defaultConfig = this.generateDefaultConfig();
          zip.file('config.yml', defaultConfig);
          
          // 重新生成zip文件
          const newZipBlob = await zip.generateAsync({ type: 'blob' });
          
          // 创建新的File对象
          const newFile = new File([newZipBlob], file.name, {
            type: 'application/zip',
            lastModified: Date.now()
          });
          
          this.$message.success('检测到缺少config.yml文件，已自动生成默认配置文件');
          return newFile;
        }
        
        return file;
      } catch (error) {
        console.warn('检查config.yml失败，使用原文件:', error);
        return file;
      }
    },
  },
};
</script>

<style scoped>
.filter-row {
  margin-top: 10px;
}
@media screen and (max-width: 768px) {
  .filter-row span {
    margin-right: 5px;
  }
}
@media screen and (min-width: 768px) {
  .filter-row span {
    margin-right: 20px;
  }
}
</style>
