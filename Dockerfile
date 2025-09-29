# 1. 가볍고 최적화된 Amazon Corretto 17 Alpine 이미지 사용
FROM amazoncorretto:17-alpine-jdk

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 빌드된 jar 파일을 app.jar 라는 이름으로 컨테이너에 복사
#    (jar 파일 이름이 다를 수 있으니 경로를 확인하세요)
COPY target/*.jar app.jar

# 4. 프리티어 EC2에 맞춰 메모리 제한을 걸어서 실행
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "app.jar"]