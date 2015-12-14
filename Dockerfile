FROM ubuntu:14.04

RUN locale-gen en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

RUN apt-get update && apt-get -y install curl sudo procps wget unzip mc && \
    echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    useradd -u 1000 -G users,sudo -d /home/user --shell /bin/bash -m user && \
    echo "secret\nsecret" | passwd user && apt-get clean

USER user

RUN cd /home/user && curl -sSL https://get.docker.com/ | sh && \
    sudo usermod -aG docker user

# Define additional metadata for our image.

VOLUME /var/lib/docker

ENV JAVA_VERSION=8u45 \
    JAVA_VERSION_PREFIX=1.8.0_45 \
    CHE_LOCAL_CONF_DIR=/home/user/.che

ENV JAVA_HOME=/opt/jdk$JAVA_VERSION_PREFIX
ENV PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

# install Java

RUN sudo wget \
   --no-cookies \
   --no-check-certificate \
   --header "Cookie: oraclelicense=accept-securebackup-cookie" \
   -qO- \
   "http://download.oracle.com/otn-pub/java/jdk/$JAVA_VERSION-b14/jdk-$JAVA_VERSION-linux-x64.tar.gz" | sudo tar -zx -C /opt/ && \
    echo "export JAVA_HOME=$JAVA_HOME" >> ~/.bashrc

# expose 8080 port and a range of ports for runners

EXPOSE 8080 32823-49162

ENV CHE_LOCAL_CONF_DIR=/home/user/.che

ADD /assembly-sdk/target/assembly-sdk-*/assembly-sdk-* /home/user/che
RUN sudo chown -R user:user /home/user/che
CMD sudo service docker start && /home/user/che/bin/che.sh run
