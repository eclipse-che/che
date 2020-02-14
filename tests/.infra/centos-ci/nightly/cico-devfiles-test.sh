set -x

echo "========Starting nigtly test job $(date)========"
source tests/.infra/centos-ci/functional_tests_utils.sh
setupEnvs
installKVM
installStartDocker
installOC
installCheCtl
installJQ
installAndStartMinishift
loginToOpenshiftAndSetDevRole
deployCheIntoCluster
createTestUserAndObtainUserToken
runDevfileTestSuite 
echo "=========================== THIS IS POST TEST ACTIONS =============================="
archiveArtifacts "che-devfile-test"
echo '=======================FAILURE STATUS-----------------------:'$IS_TESTS_FAILED
if [[ "$IS_TESTS_FAILED" == "true" ]]; then exit 1; fi
