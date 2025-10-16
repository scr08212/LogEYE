# ================= STAGE 1: Build the application =================
# Gradle과 JDK를 포함한 이미지에서 빌드를 수행합니다.
FROM gradle:8.5-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 빌드에 필요한 파일들만 먼저 복사하여 Docker 빌드 캐시를 활용합니다.
COPY build.gradle settings.gradle ./
COPY src ./src

# Gradle을 사용하여 애플리케이션을 빌드합니다. 테스트는 생략하여 빌드 속도를 높입니다.
RUN gradle build -x test --no-daemon

# ================= STAGE 2: Create the final image =================
# 실제 애플리케이션을 실행할 최소한의 환경만 포함하는 이미지입니다.
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사해옵니다.
# build/libs/*.jar 패턴 대신 정확한 파일명을 지정하는 것이 더 안정적이지만,
# build/libs 디렉토리 전체를 복사하는 것도 좋은 방법입니다.
COPY --from=builder /build/build/libs/*.jar app.jar

# 컨테이너가 시작될 때 이 명령어를 실행합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
