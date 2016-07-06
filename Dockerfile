FROM ubuntu
RUN apt-get update && apt-get -y install curl sudo procps wget && \
    echo "%sudo ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    useradd -u 1000 -G users,sudo -d /home/user --shell /bin/bash -m user && \
    echo "secret\nsecret" | passwd user && \
    curl -sSL https://get.docker.com/ | sh && \
    # Add user to docker group: 100 and 50 are this gIDs in boot2docker and Docker for Mac
    usermod -aG docker,100,50 user && \
    sudo apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

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
ENV CHE_HOME /home/user/che

ENTRYPOINT [ "/home/user/che/bin/che.sh", "-c" ]
CMD [ "run" ]
