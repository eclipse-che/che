filename=`ls assembly-sdk/target | grep codenvy`
SSH_KEY_NAME=idex
SSH_AS_USER_NAME=cl-server
AS_IP=172.19.11.69
home=/home/cl-server/tomcat-ide3

deleteFileIfExists() {
    if [ -f $1 ]; then
        echo $1
        rm -rf $1
    fi
}

    echo "upload new tomcat..."
    scp -i ~/.ssh/${SSH_KEY_NAME} assembly-sdk/target/${filename} ${SSH_AS_USER_NAME}@${AS_IP}:${home}
    echo "stoping tomcat"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/ide/bin/;if [ -f che.sh ]; then ./che.sh stop -force; fi"
    echo "clean up"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "rm -rf ${home}/ide/*"
    echo "unpack new tomcat..."
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "mv ${home}/${filename} ${home}/ide"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/ide && unzip ${filename}"
    echo "install deps..."
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/ide;./install.sh /home/cl-server/.m2/repository"
    echo "start new tomcat... on ${AS_IP}"
    ssh -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "cd ${home}/ide/bin;./che.sh start"

    AS_STATE='Starting'
    testfile=/tmp/catalina.out
    while [[ "${AS_STATE}" != "Started" ]]; do

    deleteFileIfExists ${testfile}

    scp -i ~/.ssh/${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP}:${home}/ide/logs/catalina.out ${testfile}

      if grep -Fq "Server startup" ${testfile}
        then
         echo "Tomcat of application server started"
         AS_STATE=Started
      fi

         echo "AS state = ${AS_STATE}  Attempt ${COUNTER}"
         sleep 5
         let COUNTER=COUNTER+1
         deleteFileIfExists ${testfile}
    done
