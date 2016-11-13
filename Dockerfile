FROM java

MAINTAINER Christopher A. Mosher <cmosher01@gmail.com>

# sdkman needs bash
SHELL [ "/bin/bash", "--login", "-c" ]

ENV HOME /root
WORKDIR $HOME

# install sdkman and latest gradle
RUN curl -s https://get.sdkman.io | bash
RUN sdk install gradle
RUN ln -s /root/.sdkman/candidates/gradle/current/bin/gradle /usr/local/bin/gradle
RUN echo "org.gradle.daemon=false" >gradle.properties

ENTRYPOINT [ "gradle" ]
CMD [ "run" ]

COPY static/ ./tomcat.8080/webapps/static/

COPY settings.gradle ./
COPY build.gradle ./
COPY src/ ./src/

RUN gradle build
