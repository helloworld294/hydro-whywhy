<template>
  <div class="container">
    <el-row :gutter="20">
      <el-col :md="18" :xs="24" v-loading="loading">
        <div class="clipboard-header">
          <span style="padding: 16px; float: left">
            <el-breadcrumb separator-class="el-icon-arrow-right">
              <el-breadcrumb-item>{{ $t('m.Cloud_Clipboard') }} ( {{ total }} )</el-breadcrumb-item>
            </el-breadcrumb>
          </span>
          <span class="search">
            <vxe-input
              v-model="keyword"
              :placeholder="$t('m.Enter_keyword')"
              type="search"
              @keyup.enter.native="handleQueryChange"
              @search-click="handleQueryChange"
            ></vxe-input>
          </span>
        </div>
        <template v-if="clipboardList.length > 0">
          <div class="title-article" v-for="(item, index) in clipboardList" :key="index">
            <el-card shadow="hover" class="list-card">
              <h1 class="article-hlink">
                <a @click="toClipboardDetail(item.id)">{{ item.title || $t('m.Untitled') }}</a>
              </h1>
              <a @click="toClipboardDetail(item.id)" class="article-hlink2">
                <p>{{ getContentPreview(item.content) }}</p>
              </a>
              <div class="title-msg">
                <span class="pr pl hidden-xs-only">
                  <label class="fw"><i class="fa fa-clock-o"></i></label>
                  <span>
                    {{ $t('m.Release_Time') }}：
                    <el-tooltip :content="item.gmtModified | localtime" placement="top">
                      <span>{{ item.gmtModified | fromNow }}</span>
                    </el-tooltip>
                  </span>
                </span>
                <span class="pr hidden-sm-and-up">
                  <label class="fw"><i class="fa fa-clock-o"></i></label>
                  <span>{{ item.gmtModified | localtime }}</span>
                </span>

                <el-dropdown
                  style="float: right"
                  class="hidden-xs-only"
                  v-show="isAuthenticated"
                  @command="handleCommand"
                >
                  <span class="el-dropdown-link">
                    <i class="el-icon-more"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item
                      icon="el-icon-document-copy"
                      :command="'copy:' + index"
                    >{{ $t('m.Copy') }}</el-dropdown-item>
                    <el-dropdown-item
                      icon="el-icon-edit-outline"
                      :command="'edit:' + index"
                    >{{ $t('m.Edit') }}</el-dropdown-item>
                    <el-dropdown-item
                      icon="el-icon-delete"
                      :command="'delete:' + index"
                    >{{ $t('m.Delete') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>

                <div class="hidden-sm-and-up">
                  <el-dropdown
                    style="float: right; margin-top: 10px"
                    v-show="isAuthenticated"
                    @command="handleCommand"
                  >
                    <span class="el-dropdown-link">
                      <i class="el-icon-more"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        icon="el-icon-document-copy"
                        :command="'copy:' + index"
                      >{{ $t('m.Copy') }}</el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-edit-outline"
                        :command="'edit:' + index"
                      >{{ $t('m.Edit') }}</el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-delete"
                        :command="'delete:' + index"
                      >{{ $t('m.Delete') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </div>
              </div>
            </el-card>
          </div>
        </template>
        <template v-else>
          <el-empty :description="$t('m.No_Data')"></el-empty>
        </template>
        <Pagination
          :total="total"
          :page-size="limit"
          @on-change="getClipboardList"
          :current.sync="page"
        ></Pagination>
      </el-col>
      <el-col :md="6" :xs="24">
        <el-button
          class="btn"
          type="primary"
          @click="toCreateClipboard"
          style="width: 100%"
        >
          <i class="el-icon-plus"></i>
          {{ $t('m.Create_Clipboard') }}
        </el-button>
      </el-col>
    </el-row>

    <!-- 创建/编辑对话框 -->
    <el-dialog
      :title="editingItem ? $t('m.Edit') : $t('m.Create_Clipboard')"
      :visible.sync="showEditDialog"
      width="70%"
      @close="resetForm"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item :label="$t('m.Title')">
          <el-input v-model="form.title" :placeholder="$t('m.Title_Placeholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('m.Content')" required>
          <Editor :value.sync="form.content"></Editor>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showEditDialog = false">{{ $t('m.Cancel') }}</el-button>
        <el-button type="primary" @click="submitClipboard" :loading="saving">
          {{ $t('m.OK') }}
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import Avatar from 'vue-avatar';
import api from '@/common/api';
import myMessage from '@/common/message';
import { mapGetters } from 'vuex';
import 'element-ui/lib/theme-chalk/display.css';
import Pagination from '@/components/oj/common/Pagination';
const Editor = () => import('@/components/admin/Editor.vue');

export default {
  components: {
    Avatar,
    Editor,
    Pagination,
  },
  data() {
    return {
      loading: false,
      saving: false,
      page: 1,
      limit: 10,
      total: 0,
      keyword: '',
      clipboardList: [],
      showEditDialog: false,
      editingItem: null,
      form: {
        title: '',
        content: '',
      },
    };
  },
  mounted() {
    this.init();
  },
  watch: {
    $route(newVal, oldVal) {
      if (newVal != oldVal) {
        this.init();
      }
    },
  },
  computed: {
    ...mapGetters(['isAuthenticated', 'userInfo']),
  },
  methods: {
    init() {
      this.page = parseInt(this.$route.query.currentPage) || 1;
      this.limit = parseInt(this.$route.query.limit) || 10;
      this.keyword = this.$route.query.keyword || '';
      this.getClipboardList();
    },
    getClipboardList() {
      this.loading = true;
      api
        .getClipboardList(this.page, this.limit)
        .then((res) => {
          let list = res.data.data.records || [];
          // 如果有搜索关键词，前端过滤
          if (this.keyword && this.keyword.trim()) {
            const keyword = this.keyword.toLowerCase();
            list = list.filter(
              (item) =>
                (item.title && item.title.toLowerCase().includes(keyword)) ||
                (item.content && item.content.toLowerCase().includes(keyword))
            );
          }
          this.clipboardList = list;
          this.total = res.data.data.total || 0;
          this.loading = false;
        })
        .catch((err) => {
          this.loading = false;
          myMessage.error(err.data.msg || '获取失败');
        });
    },
    handleQueryChange() {
      this.page = 1;
      this.getClipboardList();
    },
    toClipboardDetail(id) {
      this.$router.push({
        name: 'ClipboardDetails',
        params: { clipboardID: id },
      });
    },
    toCreateClipboard() {
      if (!this.isAuthenticated) {
        myMessage.warning(this.$i18n.t('m.Please_login_first'));
        this.$store.dispatch('changeModalStatus', { visible: true });
      } else {
        this.editingItem = null;
        this.resetForm();
        this.showEditDialog = true;
      }
    },
    handleCommand(command) {
      const [action, index] = command.split(':');
      const item = this.clipboardList[parseInt(index)];

      if (action === 'copy') {
        this.copyToClipboard(item.content);
      } else if (action === 'edit') {
        this.editClipboard(item);
      } else if (action === 'delete') {
        this.deleteClipboard(item.id);
      }
    },
    editClipboard(item) {
      this.editingItem = item;
      this.form.title = item.title;
      this.form.content = item.content;
      this.showEditDialog = true;
    },
    submitClipboard() {
      if (!this.form.content || this.form.content.trim() === '') {
        myMessage.error(this.$i18n.t('m.Content_Required'));
        return;
      }

      this.saving = true;
      const data = {
        title: this.form.title,
        content: this.form.content,
      };

      const apiCall = this.editingItem
        ? api.updateClipboard(this.editingItem.id, data)
        : api.saveClipboard(data);

      apiCall
        .then((res) => {
          myMessage.success(
            this.editingItem ? this.$i18n.t('m.Update_Successfully') : this.$i18n.t('m.Save_Successfully')
          );
          this.showEditDialog = false;
          this.resetForm();
          this.getClipboardList();
          this.saving = false;
        })
        .catch((err) => {
          this.saving = false;
          myMessage.error(err.data.msg || '保存失败');
        });
    },
    deleteClipboard(id) {
      this.$confirm(this.$i18n.t('m.Delete_Tips'), 'Tips', {
        confirmButtonText: this.$i18n.t('m.OK'),
        cancelButtonText: this.$i18n.t('m.Cancel'),
        type: 'warning',
      })
        .then(() => {
          api
            .deleteClipboard(id)
            .then((res) => {
              myMessage.success(this.$i18n.t('m.Delete_Successfully'));
              this.getClipboardList();
            })
            .catch((err) => {
              myMessage.error(err.data.msg || '删除失败');
            });
        })
        .catch(() => {});
    },
    copyToClipboard(text) {
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(
          () => {
            myMessage.success(this.$i18n.t('m.Copy_Successfully'));
          },
          () => {
            this.fallbackCopyTextToClipboard(text);
          }
        );
      } else {
        this.fallbackCopyTextToClipboard(text);
      }
    },
    fallbackCopyTextToClipboard(text) {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      textArea.style.position = 'fixed';
      textArea.style.left = '-999999px';
      textArea.style.top = '-999999px';
      document.body.appendChild(textArea);
      textArea.focus();
      textArea.select();
      try {
        document.execCommand('copy');
        myMessage.success(this.$i18n.t('m.Copy_Successfully'));
      } catch (err) {
        myMessage.error('复制失败');
      }
      document.body.removeChild(textArea);
    },
    getContentPreview(content) {
      if (!content) return '';
      // 移除HTML标签，只保留文本
      const text = content.replace(/<[^>]*>/g, '').replace(/\n/g, ' ');
      return text.length > 150 ? text.substring(0, 150) + '...' : text;
    },
    resetForm() {
      this.form = {
        title: '',
        content: '',
      };
      this.editingItem = null;
    },
  },
};
</script>

<style scoped>
.clipboard-header {
  background-color: #fff;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 10px;
  height: 41px;
}
.clipboard-header .search {
  margin-top: 3px;
  margin-right: 6px;
  float: right;
}
.list-card {
  border-radius: 6px;
  margin-bottom: 10px;
  padding: 15px;
  text-align: left;
  position: relative;
}
.list-card p {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.list-card .article-hlink {
  overflow: hidden;
  display: block;
}
.article-hlink {
  margin: 0;
  padding: 0;
}
.article-hlink a {
  font-size: 16px;
  font-weight: 600;
  color: #34495e;
  margin-top: 5px;
}
a {
  color: #34495e;
  text-decoration: none;
}
.article-hlink2 p {
  margin-bottom: 10px;
  color: #888;
  font-size: 12px;
  margin: 0;
  padding: 0;
}
.title-article .title-msg {
  margin-top: 15px;
  font-size: 12px;
  color: #999 !important;
}
.title-article .title-msg a {
  color: #999;
  text-decoration: none;
}
.title-article .title-msg span {
  margin-right: 3px;
}
</style>
<style>
/deep/ .el-card__body {
  padding: 0 !important;
}
</style>

