FROM gradle:jdk12

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

EXPOSE 4567

RUN echo "org.gradle.daemon=false" >gradle.properties

USER root

RUN chown -R gradle: /usr/local

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN chown -R gradle: ./

USER gradle

RUN gradle build

RUN tar xf /home/gradle/build/distributions/*.tar --strip-components=1 -C /usr/local

CMD ["gedcom-web-view"]
