FROM openjdk:8-jdk

ARG MAVEN_VERSION=3.3.9
ARG USER_HOME_DIR="/root"

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL http://apache.osuosl.org/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
    | tar -xzC /usr/share/maven --strip-components=1 \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

WORKDIR /tmp
RUN wget -O che.zip https://github.com/eclipse/che/archive/master.zip&&\
    unzip che.zip > /dev/null && cd che-master/assembly/assembly-main/ &&\
    mvn package

ADD . /usr/src/che/
WORKDIR  /usr/src/che/assembly/assembly-main

RUN mvn clean install && mv target/eclipse-che-* target/eclipse-che
