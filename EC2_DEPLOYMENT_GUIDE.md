# 🚀 EC2 도커 배포 가이드

## 1. EC2 인스턴스 생성
- **AMI**: Ubuntu 22.04 LTS
- **인스턴스 타입**: t3.medium 이상 (2GB RAM, 2 vCPU)
- **스토리지**: 20GB 이상

## 2. 보안 그룹 설정
다음 포트들을 열어주세요:

| 포트 | 프로토콜 | 설명 |
|------|----------|------|
| 22 | SSH | SSH 접속용 |
| 80 | HTTP | 웹 서버 (선택사항) |
| 8080 | HTTP | Spring Boot 애플리케이션 |
| 3000 | HTTP | 알림 서버 |
| 3306 | TCP | MySQL (선택사항, 외부 접근 필요시) |
| 6379 | TCP | Redis (선택사항, 외부 접근 필요시) |

## 3. 배포 실행

### EC2에 접속
```bash
ssh -i your-key.pem ubuntu@your-ec2-ip
```

### 프로젝트 업로드
```bash
# 로컬에서 프로젝트 압축
zip -r nexus-project.zip . -x "*.git*" "target/*" "node_modules/*"

# EC2로 업로드
scp -i your-key.pem nexus-project.zip ubuntu@your-ec2-ip:~/

# EC2에서 압축 해제
unzip nexus-project.zip
cd nexus
```

### 배포 스크립트 실행
```bash
chmod +x deploy-ec2.sh
./deploy-ec2.sh
```

## 4. 배포 확인

### 컨테이너 상태 확인
```bash
docker-compose ps
```

### 로그 확인
```bash
# 전체 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f nexus-app
docker-compose logs -f notification-server
```

### 애플리케이션 접속
- 메인 앱: `http://your-ec2-ip:8080`
- 알림 서버: `http://your-ec2-ip:3000`

## 5. 문제 해결

### 포트 충돌 시
```bash
# 포트 사용 확인
sudo netstat -tulpn | grep :8080

# 프로세스 종료
sudo kill -9 [PID]
```

### 도커 권한 문제
```bash
# 사용자를 docker 그룹에 추가
sudo usermod -aG docker $USER

# 재로그인 필요
exit
# 다시 SSH 접속
```

### 메모리 부족 시
```bash
# 스왑 메모리 추가
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## 6. 유용한 명령어

```bash
# 컨테이너 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart nexus-app

# 컨테이너 중지
docker-compose down

# 컨테이너와 볼륨 모두 삭제
docker-compose down -v

# 시스템 정리
docker system prune -f
```

## 7. 모니터링

### 리소스 사용량 확인
```bash
# 도커 리소스 사용량
docker stats

# 시스템 리소스
htop
```

### 로그 모니터링
```bash
# 실시간 로그 확인
docker-compose logs -f --tail=100
```

---

**🎉 배포 완료! 이제 `http://your-ec2-ip:8080`으로 접속하세요!** 