<template>
  <el-dialog
    :title="$t('m.Security_Confirm')"
    :visible.sync="visible"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
  >
    <div class="security-confirm-content">
      <div class="warning-icon">
        <i class="el-icon-warning" style="color: #E6A23C; font-size: 48px;"></i>
      </div>
      <div class="warning-text">
        <h3>{{ $t('m.Security_Operation_Warning') }}</h3>
        <p>{{ operationDescription }}</p>
      </div>
      <div class="password-input">
        <el-input
          v-model="confirmPassword"
          type="password"
          :placeholder="$t('m.Enter_Security_Password')"
          show-password
          @keyup.enter.native="handleConfirm"
        ></el-input>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">{{ $t('m.Cancel') }}</el-button>
      <el-button 
        type="danger" 
        @click="handleConfirm"
        :loading="loading"
        :disabled="!confirmPassword"
      >
        {{ $t('m.Confirm') }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import api from '@/common/api'

export default {
  name: 'SecurityConfirm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    operationDescription: {
      type: String,
      default: ''
    },
    operationType: {
      type: String,
      default: ''
    },
    operationData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      confirmPassword: '',
      loading: false,
      SECURITY_PASSWORD: 'fdtfdt'
    }
  },
  methods: {
    async handleConfirm() {
      if (!this.confirmPassword) {
        this.$message.error(this.$i18n.t('m.Please_Enter_Password'))
        return
      }

      if (this.confirmPassword !== this.SECURITY_PASSWORD) {
        this.$message.error(this.$i18n.t('m.Incorrect_Security_Password'))
        this.confirmPassword = ''
        return
      }

      this.loading = true
      try {
        // 获取MAC地址
        const macAddress = await this.getMacAddress()
        
        // 记录安全操作日志
        await this.logSecurityOperation(macAddress)
        
        // 触发确认事件
        this.$emit('confirm', {
          operationType: this.operationType,
          operationData: this.operationData,
          macAddress: macAddress
        })
        
        this.handleCancel()
        this.$message.success(this.$i18n.t('m.Security_Operation_Confirmed'))
      } catch (error) {
        console.error('Security operation failed:', error)
        this.$message.error(this.$i18n.t('m.Security_Operation_Failed'))
      } finally {
        this.loading = false
      }
    },
    
    handleCancel() {
      this.confirmPassword = ''
      this.$emit('cancel')
    },
    
    async getMacAddress() {
      try {
        // 尝试获取网络接口信息
        const response = await fetch('/api/get-mac-address')
        if (response.ok) {
          const data = await response.json()
          return data.macAddress || 'unknown'
        }
      } catch (error) {
        console.warn('Failed to get MAC address:', error)
      }
      
      // 备用方案：使用用户代理和其他信息生成唯一标识
      const userAgent = navigator.userAgent
      const timestamp = Date.now()
      const random = Math.random().toString(36).substring(2)
      return `client_${timestamp}_${random}`
    },
    
    async logSecurityOperation(macAddress) {
      try {
        const logData = {
          operationType: this.operationType,
          operationData: this.operationData,
          macAddress: macAddress,
          userAgent: navigator.userAgent,
          timestamp: new Date().toISOString(),
          userId: this.$store.getters.userInfo.uid,
          username: this.$store.getters.userInfo.username
        }
        
        await api.logSecurityOperation(logData)
      } catch (error) {
        console.error('Failed to log security operation:', error)
        // 即使日志记录失败，也不阻止操作继续
      }
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.confirmPassword = ''
      }
    }
  }
}
</script>

<style scoped>
.security-confirm-content {
  text-align: center;
  padding: 20px 0;
}

.warning-icon {
  margin-bottom: 20px;
}

.warning-text h3 {
  color: #E6A23C;
  margin-bottom: 10px;
}

.warning-text p {
  color: #606266;
  margin-bottom: 20px;
  line-height: 1.5;
}

.password-input {
  margin-bottom: 20px;
}

.dialog-footer {
  text-align: center;
}
</style>
