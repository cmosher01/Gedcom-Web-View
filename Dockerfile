FROM gradle:alpine

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

EXPOSE 4567
VOLUME /home/gradle/gedcom



USER root
RUN chmod -R a+w /usr/local

RUN echo "org.gradle.daemon=false" >gradle.properties

COPY docker-run.sh ./
RUN chmod a+x docker-run.sh
ENTRYPOINT ["/home/gradle/docker-run.sh"]

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN chown -R gradle: ./
USER gradle



RUN gradle build >build.log 2>&1
RUN tar xf build/distributions/*.tar --strip-components=1 -C /usr/local
