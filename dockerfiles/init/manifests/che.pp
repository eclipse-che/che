node default {
##################################################################################################
  $che_ip = getValue("CHE_HOST", "localhost")
  $che_port = getValue("CHE_PORT", "8080")
  $che_version = getValue("CHE_VERSION","nightly")
  $che_instance = getValue("CHE_INSTANCE","/tmp/che")
  $che_config = getValue("CHE_CONFIG","/path/to/che/che/puppet/sources")
  $che_assembly = getValue("CHE_ASSEMBLY","/home/user/che")
  $che_env = getValue("CHE_ENVIRONMENT","production")
  $che_debug_port = getValue("CHE_DEBUG_PORT","8000")
  $che_debug_suspend = getValue("CHE_DEBUG_SUSPEND","false")
  $docker_ip = getValue("CHE_DOCKER_IP","172.17.0.1")
  $docker_host = getValue("DOCKER_HOST","tcp://localhost:2375")

  
###############################
# oAuth configurations
  $google_client_id = getValue("CHE_GOOGLE_CLIENT_ID","your_google_client_id")
  $google_secret = getValue("CHE_GOOGLE_SECRET","your_google_secret")
  $github_client_id = getValue("CHE_GITHUB_CLIENT_ID","423531cf41d6c13e1b3b")
  $github_secret = getValue("CHE_GITHUB_SECRET","e708bfc28c541a8f25feac4466c93611d9018a3d")
  $bitbucket_client_id = getValue("CHE_BITBUCKET_CLIENT_ID","your_bitbucket_client_id")
  $bitbucket_secret = getValue("CHE_BITBUCKET_SECRET","your_bitbucket_secret")
  $wso2_client_id = getValue("CHE_WSO2_CLIENT_ID","your_wso2_client_id")
  $wso2_secret = getValue("CHE_WSO2_SECRET","your_wso2_secret")
  $projectlocker_client_id = getValue("CHE_PROJECTLOCKER_CLIENT_ID","your_projectlocker_client_id")
  $projectlocker_secret = getValue("CHE_PROJECTLOCKER_SECRET","your_projectlocker_secret")
  $microsoft_client_id = getValue("CHE_MICROSOFT_CLIENT_ID","your_microsoft_client_id")
  $microsoft_secret = getValue("CHE_MICROSOFT_SECRET","your_microsoft_secret")

###############################
# Include base module
  include base
}

