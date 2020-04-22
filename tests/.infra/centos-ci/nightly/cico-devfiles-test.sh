set -x

echo "========Starting nigtly test job $(date)========"
source tests/.infra/centos-ci/functional_tests_utils.sh

function prepareCustomResourceFile() {
  cd /tmp
  wget https://raw.githubusercontent.com/eclipse/che-operator/master/deploy/crds/org_v1_che_cr.yaml -O custom-resource.yaml
  sed -i "s@tlsSupport: true@tlsSupport: false@g" /tmp/custom-resource.yaml
  cat /tmp/custom-resource.yaml
}

setupEnvs
installKVM
installStartDocker
installOC
installCheCtl
installJQ
installAndStartMinishift
loginToOpenshiftAndSetDevRole
prepareCustomResourceFile
deployCheIntoCluster --chenamespace=che --che-operator-cr-yaml=/tmp/custom-resource.yaml
createTestUserAndObtainUserToken
runDevfileTestSuite 
echo "=========================== THIS IS POST TEST ACTIONS =============================="
archiveArtifacts "che-devfile-test"
echo '=======================FAILURE STATUS-----------------------:'$IS_TESTS_FAILED
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
