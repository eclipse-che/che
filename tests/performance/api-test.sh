export TEST_DEVFILE_PATH="devfile-registry/devfiles/java11-maven-lombok__lombok-project-sample/devworkspace-che-code-latest.yaml"
export WORKSPACE_NAME="java-lombok"

export projectFilesPath="/projects/lombok-project-sample"
export expectedCommandOutput="BUILD SUCCESS"

oc login -u $OCP_USERNAME -p $OCP_PASSWORD --server=$OCP_SERVER_URL --insecure-skip-tls-verify

cd /tmp

curl --insecure ${BASE_URL}/${TEST_DEVFILE_PATH} -o devfile.yaml
echo "  routingClass: che" >> devfile.yaml
cat devfile.yaml
oc apply -f devfile.yaml

start=$(date +%s)
oc wait --for=condition=Ready dw ${WORKSPACE_NAME} --timeout=360s
end=$(date +%s)
echo "Workspace started in $(($end - $start)) seconds"

export WS_ID=$(oc get dw $WORKSPACE_NAME --template='{{.status.devworkspaceId}}')
export POD_NAME=$(oc get pods | grep $WS_ID | awk '{print $1}')

start=$(date +%s)
export LOG=$(oc exec $POD_NAME -c tools -- /bin/bash -c "cd '$projectFilesPath'; mvn clean install >> command_log.txt; grep '$expectedCommandOutput' ./command_log.txt;")
# export LOG=$(oc exec $POD_NAME -c tools -- /bin/bash -c "timeout 60 sh -c 'cd /projects/quarkus-quickstarts/getting-started && mvn compile quarkus:dev' >> command_log.txt; grep 'Listening for transport dt_socket at address: 5005' ./command_log.txt;")
end=$(date +%s)

if echo $LOG | grep -q "$expectedCommandOutput"; then
    echo "Build succeeded in $(($end - $start)) seconds"
    oc delete dw $WORKSPACE_NAME
else
    echo "Maven command failed."
    oc delete dw $WORKSPACE_NAME
    exit 1
fi
