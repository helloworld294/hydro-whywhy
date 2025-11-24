# HOJ 远程部署脚本使用说明

## 脚本位置
`/root/hoj/deploy_to_remote.sh`

## 功能
- 自动部署后端jar文件到远程服务器
- 自动部署前端dist文件到远程服务器
- 自动创建clipboard数据表
- 可选择是否重启服务

## 使用前准备

### 1. 确保本地文件已编译
```bash
# 编译后端
cd /root/hoj/hoj-springboot
mvn clean package -DskipTests

# 编译前端
cd /root/hoj/hoj-vue
npm run build
```

### 2. 确保SSH密钥已配置
```bash
# 生成SSH密钥（如果还没有）
ssh-keygen -t rsa

# 复制公钥到远程服务器
ssh-copy-id -p 22 root@远程服务器IP
```

## 使用方法

```bash
cd /root/hoj
./deploy_to_remote.sh
```

## 脚本会询问的信息

1. **部署服务器地址**: 远程服务器的IP地址或域名
2. **SSH用户名**: 默认 `root`
3. **SSH端口**: 默认 `22`
4. **远程部署路径**: 默认 `/root/HOJ-Deploy/standAlone`
5. **MySQL容器名**: 默认 `hoj-mysql`
6. **MySQL root密码**: 默认 `hoj123456`
7. **Git仓库地址**: 默认 `https://gitee.com/whysblog/hoj-why.git`
8. **远程代码目录**: 默认 `/root/hoj`
9. **是否重启服务**: 默认 `y` (是)

## 部署内容

### 1. 后端文件
- 文件: `hoj-backend-4.6.jar`
- 目标位置: `$REMOTE_PATH/backend/app.jar` 或 `$REMOTE_PATH/../src/backend/app.jar`

### 2. 前端文件
- 目录: `dist/`
- 目标位置: `$REMOTE_PATH/../src/frontend/dist` 或 `/root/dist`

### 3. 数据库表
- **clipboard表**: 云剪贴板功能所需的数据表
  - SQL文件: `create_clipboard_table.sql`
  - 自动在MySQL容器中执行创建
- **新用户角色**: 添加角色1009和1010
  - 角色1009: 禁止修改个人资料
  - 角色1010: 禁止修改个人资料&禁言
  - SQL文件: `add_new_roles.sql`
  - 自动在MySQL容器中执行

### 4. Git Clone
- 如果远程代码目录不存在，会自动执行 `git clone`
- 默认仓库: `https://gitee.com/whysblog/hoj-why.git`

## 注意事项

1. **SSH连接**: 确保本地可以SSH连接到远程服务器
2. **Docker环境**: 远程服务器必须安装Docker和Docker Compose
3. **文件权限**: 确保远程服务器有相应目录的写入权限
4. **MySQL容器**: 确保MySQL容器正在运行
5. **网络连接**: 确保网络连接稳定，文件传输可能需要一些时间

## 故障排查

### SSH连接失败
- 检查服务器地址是否正确
- 检查SSH服务是否运行
- 检查防火墙是否开放SSH端口
- 检查SSH密钥是否已配置

### 文件传输失败
- 检查网络连接
- 检查远程服务器磁盘空间
- 检查文件权限

### 数据库表创建失败
- 检查MySQL容器是否运行
- 检查MySQL密码是否正确
- 检查表是否已存在（已存在会跳过）

### 服务重启失败
- 检查docker-compose.yml文件是否存在
- 检查容器名称是否正确
- 手动重启: `cd $REMOTE_PATH && docker-compose restart hoj-backend hoj-frontend`

## 手动部署步骤（如果脚本失败）

### 1. 创建clipboard表
```bash
# 在远程服务器执行
docker exec -i hoj-mysql mysql -uroot -phoj123456 hoj < create_clipboard_table.sql
```

### 2. 更新后端
```bash
# 复制jar文件
scp hoj-backend-4.6.jar root@远程IP:/root/HOJ-Deploy/standAlone/backend/app.jar

# 重启后端
cd /root/HOJ-Deploy/standAlone
docker-compose restart hoj-backend
```

### 3. 更新前端
```bash
# 复制dist文件
scp -r dist/* root@远程IP:/root/dist/

# 重启前端
cd /root/HOJ-Deploy/standAlone
docker-compose restart hoj-frontend
```

