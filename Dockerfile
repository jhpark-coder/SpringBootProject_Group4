# Dockerfile

# Java 21 베이스 이미지 사용
FROM openjdk:21-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# Maven Wrapper와 pom.xml 복사
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Maven Wrapper에 실행 권한 부여
RUN chmod +x mvnw

# 의존성 다운로드
RUN ./mvnw dependency:go-offline -B

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./mvnw clean package -DskipTests

# JAR 파일을 app.jar로 이름 변경
RUN mv target/*.jar app.jar

# 포트 8080 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 