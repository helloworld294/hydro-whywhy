# 框架升级日志

**升级日期**: 2025-12-03  
**升级类型**: 框架和依赖包版本升级  
**升级策略**: 渐进式升级，保持 Java 8 和 Vue 2 兼容性

---

## 前端升级 (hoj-vue)

### 核心框架
- **Vue.js**: 2.6.11 → 2.7.16 (Vue 2 最后的稳定版本)
- **Vue CLI**: 4.5.0 → 5.0.8
- **Vue Router**: 3.2.0 → 3.6.5
- **Vuex**: 3.4.0 → 3.6.2
- **Vue Template Compiler**: 2.6.12 → 2.7.16

### UI 框架和组件库
- **Element UI**: 2.15.3 → 2.15.14
- **ECharts**: 4.9.0 → 5.4.3
- **Vue ECharts**: 5.0.0-beta.0 → 6.6.9
- **vxe-table**: 2.9.26 → 3.10.4
- **xe-utils**: 2.8.1 → 3.5.18

### 工具库
- **axios**: 0.21.0 → 1.6.2 (重要安全更新)
- **core-js**: 3.6.5 → 3.34.0
- **highlight.js**: 10.3.2 → 11.9.0
- **jQuery**: 3.5.1 → 3.7.1
- **moment**: 2.29.1 → 2.30.1
- **vue-i18n**: 8.24.4 → 8.28.2
- **vue-cropper**: 0.5.5 → 0.6.4
- **vue-dompurify-html**: 2.5.0 → 4.1.4
- **vue-clipboard2**: 0.3.1 → 0.3.3
- **vue-avatar**: 2.3.3 → 2.3.4
- **papaparse**: 5.3.0 → 5.4.1

### 构建工具
- **compression-webpack-plugin**: 5.0.1 → 10.0.0
- **webpack-bundle-analyzer**: 4.4.0 → 4.10.1
- 移除 **uglifyjs-webpack-plugin** (已被 Webpack 5 内置的 Terser 替代)

---

## 后端升级 (hoj-springboot)

### Spring 生态系统
- **Spring Boot**: 2.2.6.RELEASE → 2.7.18 (Spring Boot 2.x 最后的稳定版本)
- **Spring Cloud**: Hoxton.SR1 → 2021.0.8
- **Spring Cloud Alibaba**: 2.2.1.RELEASE → 2021.0.5.0

### 数据库相关
- **MySQL Connector**: 8.0.19 → 8.0.33
- **Druid**: 1.1.20 → 1.2.20
- **MyBatis Plus**: 3.2.0 → 3.5.5
- **MyBatis Spring Boot Starter**: 2.1.1 → 2.3.1

### 工具库
- **Hutool**: 5.8.8 → 5.8.24
- **Lombok**: 1.16.10 → 1.18.30
- **OSHI**: 5.6.1 → 6.4.8
- **JUnit**: 4.12 → 4.13.2
- **Logback**: 1.2.3 → 1.4.14
- **Shiro Redis**: 3.2.1 → 3.3.1

### Maven 插件
- **maven-compiler-plugin**: 3.10.1 → 3.11.0
- **maven-surefire-plugin**: 3.0.0-M6 → 3.2.2

---

## 升级原因

### 安全性
1. **axios 0.21.0** 存在已知的安全漏洞，升级到 1.6.2 修复了多个 CVE
2. **MySQL Connector 8.0.19** 存在安全问题，升级到 8.0.33
3. **Spring Boot 2.2.6** 已经停止维护，升级到 2.7.18 获得安全补丁

### 稳定性
1. Vue 2.7 是 Vue 2 的最后一个版本，包含了许多 Vue 3 的特性回移植
2. Spring Boot 2.7.18 是 Spring Boot 2.x 系列的最后一个维护版本
3. 所有依赖库都升级到各自的最新稳定版本

### 兼容性
1. 保持 Java 8 兼容性，无需升级 JDK
2. 保持 Vue 2 兼容性，无需重写前端代码
3. API 和功能保持向后兼容

---

## 注意事项

### 前端
1. **Vue 2.7** 引入了 Composition API 支持，但现有代码无需修改
2. **axios 1.x** 的 API 与 0.x 基本兼容，但建议测试所有 HTTP 请求
3. **ECharts 5.x** 相比 4.x 有较大变化，需要测试所有图表组件
4. **vxe-table 3.x** 相比 2.x 有 API 变化，需要检查表格组件

### 后端
1. **Spring Boot 2.7** 相比 2.2 有较多变化，需要测试所有功能模块
2. **MyBatis Plus 3.5** 相比 3.2 有 API 增强，现有代码兼容
3. **Spring Cloud 2021.0.x** 移除了部分过时的组件，需要检查依赖
4. **Druid 1.2** 相比 1.1 有性能优化，配置保持兼容

---

## 测试建议

### 前端测试清单
- [ ] 项目能否正常构建 (`npm run build`)
- [ ] 开发服务器能否正常启动 (`npm run serve`)
- [ ] 所有页面能否正常渲染
- [ ] 所有 HTTP 请求能否正常工作
- [ ] 所有图表组件能否正常显示
- [ ] 所有表格组件能否正常工作
- [ ] 国际化功能是否正常
- [ ] 代码高亮是否正常

### 后端测试清单
- [ ] 项目能否正常编译 (`mvn clean compile`)
- [ ] 所有模块能否正常打包 (`mvn clean package`)
- [ ] 应用能否正常启动
- [ ] 数据库连接是否正常
- [ ] 所有 API 接口是否正常响应
- [ ] Redis 缓存是否正常工作
- [ ] 认证授权功能是否正常
- [ ] 判题服务是否正常

---

## 回滚方案

如果升级后出现问题，可以通过以下方式回滚：

```bash
# 回滚到升级前的版本
git checkout main

# 或者切换到升级前的分支
git checkout <原分支名>
```

---

## 后续升级建议

### 短期（6个月内）
- 监控依赖包的安全更新
- 修复升级后发现的兼容性问题
- 优化性能和稳定性

### 中期（1-2年内）
- 考虑升级到 Vue 3 + Vite（需要重写前端）
- 考虑升级到 Spring Boot 3.x（需要 Java 17+）
- 逐步替换过时的依赖库

### 长期（2年以上）
- 采用微前端架构
- 采用云原生架构
- 引入更现代的技术栈
