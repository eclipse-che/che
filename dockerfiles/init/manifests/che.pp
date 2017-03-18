node default {
  ##################################################################################################
  $che_ip = getValue("CHE_HOST", "localhost")
  $che_port = getValue("CHE_PORT", "8080")
  $che_instance = getValue("CHE_INSTANCE","/tmp/che")
  $che_config = getValue("CHE_CONFIG","/path/to/che/che/puppet/sources")
  $che_assembly = getValue("CHE_ASSEMBLY","/home/user/che")
  $che_env = getValue("CHE_ENVIRONMENT","production")
  $che_dev_env = getValue("CHE_REPO","off")
  $che_debug_port = getValue("CHE_DEBUG_PORT","8000")
  $che_debug_suspend = getValue("CHE_DEBUG_SUSPEND","false")
  $docker_ip = getValue("CHE_DOCKER_IP","172.17.0.1")
  $docker_host = getValue("DOCKER_HOST","tcp://localhost:2375")
  $che_user = getValue("CHE_USER","root")
  $che_server_xmx = getValue("CHE_SERVER_XMX","2048")

  ###############################
  # Http proxy configuration
  # leave those fields empty if no configuration needed
  #
  # http proxy for CHE
  $che_http_proxy = getValue("CHE_HTTP_PROXY","")
  $che_https_proxy = getValue("CHE_HTTPS_PROXY","")
  # provide dns which proxy should not be used for.
  # please leave this empty if you don't need no_proxy configuration
  $che_no_proxy = getValue("CHE_NO_PROXY","")
  #
  # http proxy for CHE workspaces
  $http_proxy_for_che_workspaces = getValue("CHE_WORKSPACE_HTTP__PROXY","")
  $https_proxy_for_che_workspaces = getValue("CHE_WORKSPACE_HTTPS__PROXY","")
  # provide dns which proxy should not be used for.
  # please leave this as it is if you don't need no_proxy configuration
  $no_proxy_for_che_workspaces = getValue("CHE_WORKSPACE_NO__PROXY","")

  ################################
  # DNS resolver configuration
  $dns_resolvers = getValue("CHE_DNS_RESOLVERS","")

  ###############################
  # Workspace configuration
  #
  $workspace_java_options = getValue("CHE_WORKSPACE_JAVA_OPTIONS", "-Xms256m -Xmx2048m -Djava.security.egd=file:/dev/./urandom")

  $che_jmx_enabled = getValue("CHE_JMX_ENABLED", "false")
  $che_jmx_username = getValue("CHE_JMX_USERNAME", "admin")
  $che_jmx_password = getValue("CHE_JMX_PASSWORD", "Che")
  ###############################
  # Include base module
  include base
}
