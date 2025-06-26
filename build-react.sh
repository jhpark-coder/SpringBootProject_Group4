#!/bin/bash

echo "=== React 빌드 시작 ==="

# frontend 디렉토리로 이동
cd src/main/frontend

# 의존성 설치 (필요한 경우)
echo "의존성 설치 중..."
npm install

# React 빌드
echo "React 빌드 중..."
npm run build

echo "=== React 빌드 완료 ==="
echo "빌드된 파일이 src/main/resources/static/editor/ 에 배치되었습니다."
echo "이제 Spring Boot를 실행하면 http://localhost:8080/editor 에서 React 앱을 확인할 수 있습니다." 