FROM quay.io/openshift/origin-cli:latest

RUN yum install -y jq python3-pip

RUN pip3 install yq

RUN yum clean all && \
    rm -rf /var/cache/yum && \
    rm -rf /tmp/*

CMD ["/bin/bash"]
