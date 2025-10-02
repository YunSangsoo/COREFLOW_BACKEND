FROM openjdk:17

ARG VERSION

#복사할 jar파일의 [현재위치] -> [복사될 위치]
COPY target/coreflow-0.0.1-SNAPSHOT.jar /app/coreflow.jar

LABEL maintainer="ses" \
      title="coreflow App" \
      version="$VERSION" \
      description="This image is coreflow service"

ENV APP_HOME /app
EXPOSE 8081
VOLUME /app/upload

#컨테이너 실행시 호출할 명령어
WORKDIR $APP_HOME
ENTRYPOINT ["java"]
CMD ["-jar", "coreflow.jar"]
