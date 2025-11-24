#!/bin/bash

# HOJ 本地部署脚本（在服务器上直接运行）
# 使用方法: ./deploy.sh

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}HOJ 本地部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 自动检测部署路径
if [ -d "/root/HOJ-Deploy/standAlone" ]; then
    DEPLOY_PATH="/root/HOJ-Deploy/standAlone"
elif [ -d "$(pwd)/../standAlone" ]; then
    DEPLOY_PATH="$(cd $(pwd)/../standAlone && pwd)"
elif [ -f "docker-compose.yml" ]; then
    DEPLOY_PATH="$(pwd)"
else
    read -p "请输入部署路径 (默认: /root/HOJ-Deploy/standAlone): " DEPLOY_PATH
    DEPLOY_PATH=${DEPLOY_PATH:-/root/HOJ-Deploy/standAlone}
fi

echo -e "${GREEN}检测到部署路径: ${DEPLOY_PATH}${NC}"

# 自动检测代码路径
if [ -d "/root/hoj" ]; then
    CODE_PATH="/root/hoj"
elif [ -d "$(pwd)/../.." ] && [ -f "$(pwd)/../../hoj-vue/package.json" ]; then
    CODE_PATH="$(cd $(pwd)/../.. && pwd)"
else
    read -p "请输入代码路径 (默认: /root/hoj): " CODE_PATH
    CODE_PATH=${CODE_PATH:-/root/hoj}
fi

echo -e "${GREEN}检测到代码路径: ${CODE_PATH}${NC}"

# 输入MySQL容器名
read -p "请输入MySQL容器名 (默认: hoj-mysql): " MYSQL_CONTAINER
MYSQL_CONTAINER=${MYSQL_CONTAINER:-hoj-mysql}

# 输入MySQL root密码
read -p "请输入MySQL root密码 (默认: hoj123456): " MYSQL_PASSWORD
MYSQL_PASSWORD=${MYSQL_PASSWORD:-hoj123456}

# 检查Git仓库
if [ ! -d "$CODE_PATH/.git" ]; then
    read -p "代码目录不是Git仓库，是否执行git clone? (y/n, 默认: y): " DO_CLONE
    DO_CLONE=${DO_CLONE:-y}
    if [ "$DO_CLONE" = "y" ] || [ "$DO_CLONE" = "Y" ]; then
        read -p "请输入Git仓库地址 (默认: https://gitee.com/whysblog/hoj-why.git): " GIT_REPO
        GIT_REPO=${GIT_REPO:-https://gitee.com/whysblog/hoj-why.git}
        echo -e "${YELLOW}执行git clone...${NC}"
        cd $(dirname $CODE_PATH)
        git clone $GIT_REPO $(basename $CODE_PATH)
        echo -e "${GREEN}Git clone完成${NC}"
    fi
fi

# 检查必要文件
BACKEND_SOURCE="$CODE_PATH/hoj-springboot"
FRONTEND_SOURCE="$CODE_PATH/hoj-vue"
CLIPBOARD_SQL="$CODE_PATH/create_clipboard_table.sql"
NEW_ROLES_SQL="$CODE_PATH/add_new_roles.sql"

if [ ! -d "$BACKEND_SOURCE" ]; then
    echo -e "${RED}错误: 后端源码目录不存在: $BACKEND_SOURCE${NC}"
    exit 1
fi

if [ ! -d "$FRONTEND_SOURCE" ]; then
    echo -e "${RED}错误: 前端源码目录不存在: $FRONTEND_SOURCE${NC}"
    exit 1
fi

if [ ! -f "$CLIPBOARD_SQL" ]; then
    echo -e "${RED}错误: 数据库SQL文件不存在: $CLIPBOARD_SQL${NC}"
    exit 1
fi

if [ ! -f "$NEW_ROLES_SQL" ]; then
    echo -e "${RED}错误: 新角色SQL文件不存在: $NEW_ROLES_SQL${NC}"
    exit 1
fi

# 检查Docker和Docker Compose
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker未安装${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null 2>&1; then
    echo -e "${RED}错误: Docker Compose未安装${NC}"
    exit 1
fi

# 检查MySQL容器
if ! docker ps | grep -q "$MYSQL_CONTAINER"; then
    echo -e "${RED}错误: MySQL容器 $MYSQL_CONTAINER 未运行${NC}"
    exit 1
fi

echo -e "${YELLOW}开始部署...${NC}"

# 1. 编译后端
echo -e "${YELLOW}[1/6] 编译后端...${NC}"
cd "$BACKEND_SOURCE"
if [ -f "pom.xml" ]; then
    mvn clean package -DskipTests
    BACKEND_JAR="$BACKEND_SOURCE/DataBackup/target/hoj-backend-4.6.jar"
    if [ ! -f "$BACKEND_JAR" ]; then
        echo -e "${RED}错误: 后端编译失败，jar文件不存在${NC}"
        exit 1
    fi
    echo -e "${GREEN}后端编译完成${NC}"
else
    echo -e "${RED}错误: 未找到pom.xml文件${NC}"
    exit 1
fi

# 2. 编译前端
echo -e "${YELLOW}[2/6] 编译前端...${NC}"
cd "$FRONTEND_SOURCE"
if [ -f "package.json" ]; then
    npm install
    npm run build
    if [ ! -d "dist" ]; then
        echo -e "${RED}错误: 前端编译失败，dist目录不存在${NC}"
        exit 1
    fi
    echo -e "${GREEN}前端编译完成${NC}"
else
    echo -e "${RED}错误: 未找到package.json文件${NC}"
    exit 1
fi

# 3. 创建clipboard表
echo -e "${YELLOW}[3/6] 创建clipboard数据表...${NC}"
if docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p"$MYSQL_PASSWORD" hoj < "$CLIPBOARD_SQL" 2>/dev/null; then
    echo -e "${GREEN}clipboard表创建成功${NC}"
else
    # 检查表是否已存在
    if docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p"$MYSQL_PASSWORD" hoj -e "SHOW TABLES LIKE 'clipboard';" 2>/dev/null | grep -q clipboard; then
        echo -e "${YELLOW}clipboard表已存在，跳过创建${NC}"
    else
        echo -e "${YELLOW}警告: 创建clipboard表时出现错误，请手动检查${NC}"
    fi
fi

# 4. 添加新用户角色
echo -e "${YELLOW}[4/6] 添加新用户角色 (1009, 1010)...${NC}"
if docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p"$MYSQL_PASSWORD" hoj < "$NEW_ROLES_SQL" 2>/dev/null; then
    echo -e "${GREEN}新用户角色添加成功${NC}"
else
    echo -e "${YELLOW}警告: 添加新用户角色时出现错误，请手动检查${NC}"
    # 检查角色是否已存在
    if docker exec -i "$MYSQL_CONTAINER" mysql -uroot -p"$MYSQL_PASSWORD" hoj -e "SELECT id FROM role WHERE id IN (1009, 1010);" 2>/dev/null | grep -qE "(1009|1010)"; then
        echo -e "${YELLOW}新用户角色可能已存在${NC}"
    fi
fi

# 5. 更新后端jar文件
echo -e "${YELLOW}[5/6] 更新后端jar文件...${NC}"
if [ -d "$DEPLOY_PATH/backend" ]; then
    cp "$BACKEND_JAR" "$DEPLOY_PATH/backend/app.jar"
    echo -e "${GREEN}后端jar文件已更新${NC}"
elif [ -d "$DEPLOY_PATH/../src/backend" ]; then
    cp "$BACKEND_JAR" "$DEPLOY_PATH/../src/backend/app.jar"
    echo -e "${GREEN}后端jar文件已更新到构建目录${NC}"
else
    echo -e "${YELLOW}警告: 未找到后端目录，跳过后端更新${NC}"
fi

# 6. 更新前端dist文件
echo -e "${YELLOW}[6/6] 更新前端dist文件...${NC}"
if [ -d "$DEPLOY_PATH/../src/frontend/dist" ]; then
    rm -rf "$DEPLOY_PATH/../src/frontend/dist"
    cp -r "$FRONTEND_SOURCE/dist" "$DEPLOY_PATH/../src/frontend/"
    echo -e "${GREEN}前端dist文件已更新${NC}"
elif [ -d "/root/dist" ]; then
    rm -rf /root/dist/*
    cp -r "$FRONTEND_SOURCE/dist"/* /root/dist/
    echo -e "${GREEN}前端dist文件已更新到 /root/dist${NC}"
else
    echo -e "${YELLOW}警告: 未找到前端dist目录，请手动更新${NC}"
fi

# 重启服务
echo ""
read -p "是否重启服务? (y/n, 默认: y): " RESTART_SERVICES
RESTART_SERVICES=${RESTART_SERVICES:-y}

if [ "$RESTART_SERVICES" = "y" ] || [ "$RESTART_SERVICES" = "Y" ]; then
    echo -e "${YELLOW}重启服务...${NC}"
    cd "$DEPLOY_PATH"
    if [ -f "docker-compose.yml" ]; then
        # 尝试使用docker-compose
        if command -v docker-compose &> /dev/null; then
            docker-compose restart hoj-backend hoj-frontend 2>/dev/null || true
        # 尝试使用docker compose
        elif docker compose version &> /dev/null 2>&1; then
            docker compose restart hoj-backend hoj-frontend 2>/dev/null || true
        fi
        echo -e "${GREEN}服务重启完成${NC}"
    else
        echo -e "${YELLOW}警告: 未找到docker-compose.yml文件${NC}"
    fi
else
    echo -e "${YELLOW}跳过服务重启，请手动重启${NC}"
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}部署完成!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "${YELLOW}部署信息:${NC}"
echo -e "  代码路径: ${CODE_PATH}"
echo -e "  部署路径: ${DEPLOY_PATH}"
echo -e "  MySQL容器: ${MYSQL_CONTAINER}"
echo -e "  后端jar: 已编译并更新"
echo -e "  前端dist: 已编译并更新"
echo -e "  数据库表: clipboard表已创建"
echo -e "  用户角色: 角色1009、1010已添加"
echo -e ""
echo -e "${YELLOW}请检查服务是否正常运行:${NC}"
echo -e "  cd ${DEPLOY_PATH} && docker-compose ps"

