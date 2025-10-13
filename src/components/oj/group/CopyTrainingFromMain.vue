<template>
  <div>
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="query.keyword"
            :placeholder="$t('m.Enter_keyword')"
            @keyup.enter.native="getMainTrainingList"
            clearable
          >
            <el-button
              slot="append"
              icon="el-icon-search"
              @click="getMainTrainingList"
            ></el-button>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="query.auth"
            :placeholder="$t('m.Training_Auth')"
            @change="getMainTrainingList"
            clearable
          >
            <el-option
              :label="$t('m.All')"
              value=""
            ></el-option>
            <el-option
              :label="$t('m.Public_Training')"
              value="Public"
            ></el-option>
            <el-option
              :label="$t('m.Private_Training')"
              value="Private"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select
            v-model="query.categoryId"
            :placeholder="$t('m.Category')"
            @change="getMainTrainingList"
            clearable
          >
            <el-option
              :label="$t('m.All')"
              value=""
            ></el-option>
            <el-option
              v-for="category in categoryList"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            ></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button
            type="primary"
            @click="getMainTrainingList"
            :loading="loading"
          >
            {{ $t('m.Search') }}
          </el-button>
        </el-col>
      </el-row>
    </div>

    <vxe-table
      border="inner"
      stripe
      auto-resize
      :data="mainTrainingList"
      :loading="loading"
      align="center"
      ref="mainTrainingTable"
      @checkbox-change="onSelectionChange"
      @checkbox-all="onSelectionChange"
    >
      <vxe-table-column
        type="checkbox"
        width="60"
        align="center"
      ></vxe-table-column>
      
      <vxe-table-column
        field="rank"
        :title="$t('m.Number')"
        min-width="60"
        show-overflow
      ></vxe-table-column>
      
      <vxe-table-column
        field="title"
        :title="$t('m.Title')"
        min-width="200"
        align="center"
      >
        <template v-slot="{ row }">
          <el-link type="primary" @click="viewTrainingDetail(row.id)">
            {{ row.title }}
          </el-link>
        </template>
      </vxe-table-column>

      <vxe-table-column
        field="auth"
        :title="$t('m.Auth')"
        min-width="100"
        align="center"
      >
        <template v-slot="{ row }">
          <el-tag :type="TRAINING_TYPE[row.auth]['color']" effect="dark">
            {{ $t('m.Training_' + row.auth) }}
          </el-tag>
        </template>
      </vxe-table-column>
      
      <vxe-table-column
        field="categoryName"
        :title="$t('m.Category')"
        min-width="130"
        align="center"
      >
        <template v-slot="{ row }">
          <el-tag
            size="medium"
            :style="
              'background-color: #fff;color: ' +
                row.categoryColor +
                ';border-color: ' +
                row.categoryColor +
                ';'
            "
          >{{ row.categoryName }}</el-tag>
        </template>
      </vxe-table-column>

      <vxe-table-column
        field="problemCount"
        :title="$t('m.Problem_Number')"
        min-width="100"
        align="center"
      ></vxe-table-column>
      
      <vxe-table-column
        field="author"
        :title="$t('m.Author')"
        min-width="130"
        align="center"
        show-overflow
      >
        <template v-slot="{ row }">
          <el-link type="info" @click="goUserHome(row.author)">
            {{ row.author }}
          </el-link>
        </template>
      </vxe-table-column>
      
      <vxe-table-column
        field="gmtModified"
        :title="$t('m.Recent_Update')"
        min-width="120"
        align="center"
        show-overflow
      >
        <template v-slot="{ row }">
          <el-tooltip
            :content="row.gmtModified | localtime"
            placement="top"
          >
            <span>{{ row.gmtModified | fromNow }}</span>
          </el-tooltip>
        </template>
      </vxe-table-column>
    </vxe-table>

    <div class="pagination-container">
      <Pagination
        :total="total"
        :page-size="limit"
        @on-change="currentChange"
        :current.sync="currentPage"
        :layout="'prev, pager, next, sizes'"
        @on-page-size-change="onPageSizeChange"
      ></Pagination>
    </div>

    <div class="action-section" v-if="selectedTrainings.length > 0">
      <el-alert
        :title="$t('m.Selected_Trainings') + ': ' + selectedTrainings.length"
        type="info"
        :closable="false"
        show-icon
      ></el-alert>
      <div class="action-buttons">
        <el-button
          type="primary"
          @click="copySelectedTrainings"
          :loading="copyLoading"
        >
          {{ $t('m.Copy_Selected_Trainings') }}
        </el-button>
        <el-button @click="clearSelection">
          {{ $t('m.Clear_Selection') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import api from '@/common/api';
import { TRAINING_TYPE } from '@/common/constants';
import mMessage from '@/common/message';
import Pagination from '@/components/oj/common/Pagination';

export default {
  name: 'CopyTrainingFromMain',
  components: {
    Pagination,
  },
  props: {
    groupId: {
      type: [String, Number],
      required: true,
    },
  },
  data() {
    return {
      query: {
        keyword: '',
        auth: '',
        categoryId: '',
      },
      mainTrainingList: [],
      selectedTrainings: [],
      categoryList: [],
      TRAINING_TYPE: {},
      loading: false,
      copyLoading: false,
      currentPage: 1,
      limit: 10,
      total: 0,
    };
  },
  mounted() {
    this.TRAINING_TYPE = Object.assign({}, TRAINING_TYPE);
    this.getTrainingCategoryList();
    this.getMainTrainingList();
  },
  methods: {
    getMainTrainingList() {
      this.loading = true;
      const params = {
        currentPage: this.currentPage,
        limit: this.limit,
        ...this.query,
      };
      
      // 过滤空值
      Object.keys(params).forEach(key => {
        if (params[key] === '' || params[key] === null || params[key] === undefined) {
          delete params[key];
        }
      });

      api.getTrainingList(this.currentPage, this.limit, params).then(
        (res) => {
          this.mainTrainingList = res.data.data.records;
          this.total = res.data.data.total;
          this.loading = false;
        },
        (err) => {
          this.loading = false;
        }
      );
    },

    getTrainingCategoryList() {
      api.getTrainingCategoryList().then((res) => {
        this.categoryList = res.data.data;
      });
    },

    currentChange(page) {
      this.currentPage = page;
      this.getMainTrainingList();
    },

    onPageSizeChange(pageSize) {
      this.limit = pageSize;
      this.currentPage = 1;
      this.getMainTrainingList();
    },

    onSelectionChange({ records }) {
      this.selectedTrainings = records;
    },

    async copySelectedTrainings() {
      if (this.selectedTrainings.length === 0) {
        mMessage.warning(this.$t('m.Please_Select_Trainings'));
        return;
      }

      this.copyLoading = true;
      
      try {
        // 逐个复制选中的训练课程
        for (const training of this.selectedTrainings) {
          await this.copySingleTraining(training);
        }
        
        mMessage.success(this.$t('m.Copy_Trainings_Successfully'));
        this.clearSelection();
        this.$emit('copySuccess');
      } catch (error) {
        console.error('复制训练课程失败:', error);
        mMessage.error(this.$t('m.Copy_Trainings_Failed'));
      } finally {
        this.copyLoading = false;
      }
    },

    async copySingleTraining(sourceTraining) {
      try {
        // 1. 获取主训练的详细信息
        const trainingDetailRes = await api.getTraining(sourceTraining.id);
        const trainingDetail = trainingDetailRes.data.data;
        
        // 2. 获取主训练的题目列表
        const problemListRes = await api.getTrainingProblemList(sourceTraining.id);
        const problemList = problemListRes.data.data;
        
        // 3. 创建新的团队训练课程
        console.log('团队ID:', this.groupId);
        console.log('用户信息:', this.$store.getters.userInfo);
        console.log('训练详情:', trainingDetail);
        
        // 检查必要字段
        if (!this.groupId) {
          throw new Error('团队ID不能为空');
        }
        if (!this.$store.getters.userInfo?.username) {
          throw new Error('用户信息不能为空');
        }
        
        const newTrainingData = {
          training: {
            rank: sourceTraining.rank || 1000,
            title: sourceTraining.title + ' (副本)',
            description: trainingDetail.description || '',
            privatePwd: '',
            auth: 'Public', // 默认为公开
            gid: this.groupId, // 团队ID应该放在training对象内部
            author: this.$store.getters.userInfo.username, // 添加作者字段
          },
          trainingCategory: {
            id: trainingDetail.trainingCategory?.id || sourceTraining.categoryId,
          },
        };
        console.log('创建训练课程数据:', newTrainingData);
        
        const createRes = await api.addGroupTraining(newTrainingData);
        console.log('创建训练课程响应:', createRes);
        const newTrainingId = createRes.data.data.id;
        
        // 4. 复制题目到新创建的团队训练课程
        if (problemList && problemList.length > 0) {
          for (const problem of problemList) {
            try {
              // 使用现有的添加公共题目到训练课程的接口
              await api.addGroupTrainingProblemFromPublic({
                pid: problem.pid,
                tid: newTrainingId,
              });
            } catch (error) {
              console.warn(`复制题目 ${problem.displayId} 失败:`, error);
              // 继续复制其他题目，不中断整个过程
            }
          }
        }
        
      } catch (error) {
        console.error(`复制训练课程 ${sourceTraining.title} 失败:`, error);
        if (error.response) {
          console.error('错误响应:', error.response.data);
          console.error('错误状态码:', error.response.status);
        }
        throw error;
      }
    },

    clearSelection() {
      this.selectedTrainings = [];
      this.$refs.mainTrainingTable.clearCheckboxRow();
    },

    viewTrainingDetail(trainingId) {
      // 在新窗口打开训练详情
      const routeData = this.$router.resolve({
        name: 'TrainingDetails',
        params: { trainingID: trainingId },
      });
      window.open(routeData.href, '_blank');
    },

    goUserHome(username) {
      this.$router.push({
        path: '/user-home',
        query: { username },
      });
    },
  },
};
</script>

<style scoped>
.filter-section {
  margin-bottom: 20px;
}

.pagination-container {
  margin: 20px 0;
  text-align: center;
}

.action-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.action-buttons {
  margin-top: 10px;
  text-align: center;
}

.action-buttons .el-button {
  margin: 0 5px;
}
</style>
