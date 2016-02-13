FROM ubuntu
RUN apt-get update && apt-get -y install curl sudo procps wget && \
    echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    useradd -u 1000 -G users,sudo -d /home/user --shell /bin/bash -m user && \
    echo "secret\nsecret" | passwd user && \
    curl -sSL https://get.docker.com/ | sh && \
    usermod -aG docker user && sudo apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*
    
USER user
ENV JAVA_VERSION=8u65 \
    JAVA_VERSION_PREFIX=1.8.0_65 \
    CHE_LOCAL_CONF_DIR=/home/user/.che
RUN mkdir /home/user/.che && \
    wget \
   --no-cookies \
   --no-check-certificate \
   --header "Cookie: oraclelicense=accept-securebackup-cookie" \
   -qO- \
   "http://download.oracle.com/otn-pub/java/jdk/$JAVA_VERSION-b17/jre-$JAVA_VERSION-linux-x64.tar.gz" | sudo tar -zx -C /opt/
ENV JAVA_HOME /opt/jre$JAVA_VERSION_PREFIX
ENV PATH $JAVA_HOME/bin:$PATH

EXPOSE 8080

ADD /assembly/assembly-main/target/eclipse-che-*/eclipse-che-* /home/user/che

CMD sudo rm -rf /home/user/che/lib-copy/* && \
    mkdir -p /home/user/che/lib-copy/ && \
    sudo chown -R user:user /home/user && \
    cp -rf /home/user/che/lib/* /home/user/che/lib-copy && \
    /home/user/che/bin/che.sh run
