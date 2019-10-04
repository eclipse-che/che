FROM quay.io/eclipse/che-java11-maven:nightly

USER root

RUN cd / && \
    git clone https://github.com/spring-projects/spring-petclinic && \
    cd /spring-petclinic && \
    mvn clean package

RUN mkdir -p /.m2 && \
    cp -r /root/.m2/repository/* /.m2 && \
    rm -rf spring-petclinic/ /root/.m2/repository/* 

USER 10001
