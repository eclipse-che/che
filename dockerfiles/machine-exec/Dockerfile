FROM centos:7

ENV DEP_DOWNLOAD_URL https://github.com/golang/dep/releases/download/v0.5.0/dep-linux-amd64
ENV GOLANG_VERSION 1.10
ENV GOLANG_DOWNLOAD_URL https://golang.org/dl/go$GOLANG_VERSION.linux-amd64.tar.gz
ENV GOLANG_DOWNLOAD_SHA256 b5a64335f1490277b585832d1f6c7f8c6c11206cba5cd3f771dcb87b98ad1a33
# Go workspace path
ENV GOPATH /go
# Go installation path
ENV PATH ${GOPATH}/bin:/usr/local/go/bin:${PATH}
ENV MACHINE_EXEC_HASH b57b40eb2088271dc24f538f224f8c1ac474a777
ENV MACHINE_EXEC github.com/eclipse/che-theia-terminal-plugin/machine-exec-server

RUN yum -y update && yum install -y git && git --version && \
    # Install 'dep' - go dependency manager
    curl -fsSL ${DEP_DOWNLOAD_URL} -o /usr/bin/dep && \
    chmod +x /usr/bin/dep && \
    # Install Golang
    curl -fsSL "${GOLANG_DOWNLOAD_URL}" -o /tmp/golang.tar.gz \
    && echo "${GOLANG_DOWNLOAD_SHA256}  /tmp/golang.tar.gz" | sha256sum -c - \
    && tar -C /usr/local -xzf /tmp/golang.tar.gz \
    && rm /tmp/golang.tar.gz && \
    mkdir -p "${GOPATH}/src" "${GOPATH}/bin" && chmod -R 777 "${GOPATH}" && \
    # Build machine exec binary
    go get ${MACHINE_EXEC} && \
    go clean && \
    cd ${GOPATH}/src/${MACHINE_EXEC} && \
    git checkout ${MACHINE_EXEC_HASH} && \
    ./compile.sh && \
    cp -rf machine-exec-server /usr/local/bin && \
    # Clean up image to prevent overhaed image size
    go clean && \
    rm -rf /usr/local/go /usr/bin/dep ${GOPATH} && \
    yum remove -y git && \
    yum -y autoremove && \ 
    yum clean all && \
    rm -rf /var/cache/yum

CMD machine-exec-server
