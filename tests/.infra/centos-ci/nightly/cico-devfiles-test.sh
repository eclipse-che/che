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
archiveArtifacts "che-devfile-test"
