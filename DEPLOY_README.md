# HOJ 本地部署脚本使用说明

## 脚本位置
`/root/hoj/deploy.sh`

## 功能
- 自动编译后端代码
- 自动编译前端代码
- 自动部署后端jar文件
- 自动部署前端dist文件
- 自动创建clipboard数据表
- 自动添加新用户角色（1009, 1010）
- 自动检测部署路径和代码路径
- 可选择是否重启服务

## 使用前准备

### 1. 确保环境已安装
- Docker 和 Docker Compose
- Maven（用于编译后端）
- Node.js 和 npm（用于编译前端）
- Git（如果需要clone代码）

### 2. 确保代码目录存在
脚本会自动检测代码路径，如果不存在可以手动指定或执行git clone

## 使用方法

在服务器上直接运行：

```bash
cd /root/hoj
./deploy.sh
```

## 脚本会询问的信息

1. **部署路径**: 自动检测，默认 `/root/HOJ-Deploy/standAlone`
2. **代码路径**: 自动检测，默认 `/root/hoj`
3. **MySQL容器名**: 默认 `hoj-mysql`
4. **MySQL root密码**: 默认 `hoj123456`
5. **是否执行git clone**: 如果代码目录不是Git仓库，可选择执行
6. **Git仓库地址**: 默认 `https://gitee.com/whysblog/hoj-why.git`
7. **是否重启服务**: 默认 `y` (是)

## 部署流程

### 1. 编译后端
- 自动执行: `mvn clean package -DskipTests`
- 生成文件: `hoj-backend-4.6.jar`

### 2. 编译前端
- 自动执行: `npm install && npm run build`
- 生成目录: `dist/`

### 3. 数据库操作
- **clipboard表**: 自动创建云剪贴板数据表
- **新用户角色**: 自动添加角色1009和1010
  - 角色1009: 禁止修改个人资料
  - 角色1010: 禁止修改个人资料&禁言

### 4. 更新文件
- 后端jar: 自动复制到部署目录
- 前端dist: 自动复制到部署目录

### 5. 重启服务（可选）
- 自动重启 `hoj-backend` 和 `hoj-frontend` 容器

## 注意事项

1. **Docker环境**: 服务器必须安装Docker和Docker Compose
2. **编译环境**: 确保Maven和Node.js已正确安装
3. **文件权限**: 确保有相应目录的写入权限
4. **MySQL容器**: 确保MySQL容器正在运行
5. **代码路径**: 脚本会自动检测，如果检测失败请手动指定

## 故障排查

### 编译失败
- 检查Maven和Node.js是否正确安装
- 检查代码是否完整
- 检查网络连接（下载依赖需要网络）

### 路径检测失败
- 手动指定部署路径和代码路径
- 检查目录是否存在

### 数据库表创建失败
- 检查MySQL容器是否运行
- 检查MySQL密码是否正确
- 检查表是否已存在（已存在会跳过）

### 服务重启失败
- 检查docker-compose.yml文件是否存在
- 检查容器名称是否正确
- 手动重启: `cd $REMOTE_PATH && docker-compose restart hoj-backend hoj-frontend`

## 手动部署步骤（如果脚本失败）

### 1. 编译后端
```bash
cd /root/hoj/hoj-springboot
mvn clean package -DskipTests
```

### 2. 编译前端
```bash
cd /root/hoj/hoj-vue
npm install
npm run build
```

### 3. 创建数据库表
```bash
# 创建clipboard表
docker exec -i hoj-mysql mysql -uroot -phoj123456 hoj < /root/hoj/create_clipboard_table.sql

# 添加新角色
docker exec -i hoj-mysql mysql -uroot -phoj123456 hoj < /root/hoj/add_new_roles.sql
```

### 4. 更新文件并重启
```bash
# 更新后端
cp /root/hoj/hoj-springboot/DataBackup/target/hoj-backend-4.6.jar /root/HOJ-Deploy/standAlone/backend/app.jar

# 更新前端
rm -rf /root/dist/*
cp -r /root/hoj/hoj-vue/dist/* /root/dist/

# 重启服务
cd /root/HOJ-Deploy/standAlone
docker-compose restart hoj-backend hoj-frontend
```

