#!/bin/bash

# STOMP → NestJS 마이그레이션 중간점검 테스트 스크립트
# 각 단계별로 테스트를 실행하여 기능이 정상 작동하는지 확인

echo "🚀 STOMP → NestJS 마이그레이션 중간점검 테스트"
echo "================================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 테스트 결과 카운터
PASSED=0
FAILED=0

# 테스트 실행 함수
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    echo -e "\n${BLUE}📋 실행 중: $test_name${NC}"
    echo "명령어: $test_command"
    
    if eval "$test_command"; then
        echo -e "${GREEN}✅ 성공: $test_name${NC}"
        ((PASSED++))
    else
        echo -e "${RED}❌ 실패: $test_name${NC}"
        ((FAILED++))
    fi
}

# 1단계: 현재 STOMP 기능 테스트
echo -e "\n${YELLOW}🔍 1단계: 현재 STOMP 기능 테스트${NC}"
echo "----------------------------------------"

run_test "ChatMessage 단위 테스트" "cd /c/SpringWork/nexus && ./mvnw test -Dtest=ChatMessageTest"
run_test "ChatController 단위 테스트" "cd /c/SpringWork/nexus && ./mvnw test -Dtest=ChatControllerTest"
run_test "채팅 통합 테스트" "cd /c/SpringWork/nexus && ./mvnw test -Dtest=ChatIntegrationTest"

# 2단계: NestJS 서버 상태 확인
echo -e "\n${YELLOW}🔍 2단계: NestJS 서버 상태 확인${NC}"
echo "----------------------------------------"

run_test "NestJS 서버 실행 상태 확인" "curl -f http://localhost:3000/api/notifications/count > /dev/null 2>&1"
run_test "NestJS 알림 서비스 테스트" "cd /c/SpringWork/nexus/notification-server && npm test -- --testPathPattern=notifications.service.spec.ts"

# 3단계: WebSocket 연결 테스트
echo -e "\n${YELLOW}🔍 3단계: WebSocket 연결 테스트${NC}"
echo "----------------------------------------"

run_test "Spring Boot WebSocket 연결 테스트" "curl -f -H 'Upgrade: websocket' -H 'Connection: Upgrade' http://localhost:8080/ws > /dev/null 2>&1"
run_test "NestJS WebSocket 연결 테스트" "curl -f -H 'Upgrade: websocket' -H 'Connection: Upgrade' http://localhost:3000 > /dev/null 2>&1"

# 4단계: 프론트엔드 JavaScript 테스트
echo -e "\n${YELLOW}🔍 4단계: 프론트엔드 JavaScript 테스트${NC}"
echo "----------------------------------------"

run_test "채팅 버튼 스크립트 문법 검사" "cd /c/SpringWork/nexus/src/main/resources/static/js && node -c chat-buttons.js"
run_test "Socket.IO 클라이언트 라이브러리 확인" "grep -q 'socket.io' /c/SpringWork/nexus/src/main/resources/templates/fragment/layout.html"

# 5단계: 데이터베이스 연결 테스트
echo -e "\n${YELLOW}🔍 5단계: 데이터베이스 연결 테스트${NC}"
echo "----------------------------------------"

run_test "Spring Boot DB 연결 테스트" "cd /c/SpringWork/nexus && ./mvnw test -Dtest=NexusApplicationTests"
run_test "알림 서비스 DB 연동 테스트" "cd /c/SpringWork/nexus && ./mvnw test -Dtest=NotificationService"

# 결과 요약
echo -e "\n${YELLOW}📊 테스트 결과 요약${NC}"
echo "================================================"
echo -e "${GREEN}✅ 통과: $PASSED${NC}"
echo -e "${RED}❌ 실패: $FAILED${NC}"
echo -e "총 테스트: $((PASSED + FAILED))"

if [ $FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 모든 테스트가 통과했습니다! 마이그레이션을 진행할 수 있습니다.${NC}"
    exit 0
else
    echo -e "\n${RED}⚠️  일부 테스트가 실패했습니다. 마이그레이션 전에 문제를 해결해주세요.${NC}"
    exit 1
fi 