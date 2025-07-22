# notification-server/Dockerfile

# 1. 빌드 단계
FROM node:20-alpine AS builder
WORKDIR /usr/src/app

# node_modules/.bin 경로를 PATH에 추가하여 npm 스크립트가 CLI 도구를 찾을 수 있도록 함
ENV PATH /usr/src/app/node_modules/.bin:$PATH

# 패키지 파일 복사 및 모든 의존성 설치
COPY package*.json ./
RUN npm install

# 소스 코드 복사
COPY . .

# nest CLI 권한 명시적 부여
RUN chmod +x ./node_modules/.bin/nest || true
RUN chmod +x ./node_modules/.bin/nest-cli || true

# TypeScript 빌드
RUN npm run build

# 2. 프로덕션 단계
FROM node:20-alpine
WORKDIR /usr/src/app

# 패키지 파일 복사 및 프로덕션 의존성만 설치
COPY package*.json ./
RUN npm ci --only=production && npm cache clean --force

# 빌드된 파일 복사
COPY --from=builder /usr/src/app/dist ./dist

# 포트 노출
EXPOSE 3000

# 프로덕션 모드로 실행
CMD ["node", "dist/main"] 