#!/bin/bash

echo "🚀 Nexus 프로젝트 EC2 배포 시작..."

# 시스템 업데이트
echo "📦 시스템 패키지 업데이트 중..."
sudo apt update && sudo apt upgrade -y

# 도커 설치 (이미 설치되어 있지 않은 경우)
if ! command -v docker &> /dev/null; then
    echo "🐳 Docker 설치 중..."
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
    rm get-docker.sh
fi

# 도커 컴포즈 설치
if ! command -v docker-compose &> /dev/null; then
    echo "🐳 Docker Compose 설치 중..."
    sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
fi

# 기존 컨테이너 정리
echo "🧹 기존 컨테이너 정리 중..."
docker-compose down -v
docker system prune -f

# 애플리케이션 빌드 및 실행
echo "🔨 애플리케이션 빌드 및 실행 중..."
docker-compose up --build -d

# 상태 확인
echo "✅ 배포 완료! 컨테이너 상태 확인 중..."
docker-compose ps

echo "🌐 애플리케이션 접속 정보:"
echo "   - 메인 애플리케이션: http://$(curl -s ifconfig.me):8080"
echo "   - 알림 서버: http://$(curl -s ifconfig.me):3000"
echo "   - MySQL: localhost:3306"
echo "   - Redis: localhost:6379"

echo "📋 로그 확인 명령어:"
echo "   - 전체 로그: docker-compose logs -f"
echo "   - 특정 서비스: docker-compose logs -f nexus-app"
echo "   - 컨테이너 상태: docker-compose ps" 