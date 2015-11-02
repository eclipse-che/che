FROM codenvy/jdk7
RUN mkdir /home/user/app
WORKDIR /home/user/app
ADD $app$ /home/user/$app$
RUN unzip -q /home/user/$app$ -d /home/user/app && \
    rm /home/user/$app$
# expected to have all dependencies in lib directory and one single jar file application.jar in 'root' directory of archive.
CMD java -jar application.jar $args$
