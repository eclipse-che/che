node default {
  ##################################################################################################
  $che_ip = getValue("CHE_HOST", "localhost")
  $che_protocol = getValue("CHE_HOST_PROTOCOL","http")
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
  $che_server_url = getValue("CHE_SERVER_URL", "${che_protocol}://${che_ip}:${che_port}")
  $che_master_container_ram = getValue("CHE_MASTER_CONTAINER_RAM", "750m")

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

  ###############################
  # Single port configuration
  #
  $che_single_port = getValue("CHE_SINGLE_PORT","false")

  ###############################
  # Che multiuser
  #
  $che_multiuser = getValue("CHE_MULTIUSER","false")

  ################################
  # DNS resolver configuration
  $dns_resolvers = getValue("CHE_DNS_RESOLVERS","")

  ###############################
  # Workspace configuration

  $che_jmx_enabled = getValue("CHE_JMX_ENABLED", "false")
  $che_jmx_username = getValue("CHE_JMX_USERNAME", "admin")
  $che_jmx_password = getValue("CHE_JMX_PASSWORD", "Che")

  $che_pg_host = getValue("CHE_POSTGRES_HOST", "postgres")
  $che_pg_port = getValue("CHE_POSTGRES_PORT", "5432")
  $che_pg_username = getValue("CHE_POSTGRES_USERNAME", "pgche")
  $che_pg_password = getValue("CHE_POSTGRES_PASSWORD", "pgchepassword")
  $che_pg_database = getValue("CHE_POSTGRES_DATABASE", "dbche")

  $system_super_privileged_mode=getValue("SYSTEM_SUPER__PRIVILEGED__MODE", "false")

  $che_keycloak_admin_require_update_password=getValue("CHE_KEYCLOAK_ADMIN_REQUIRE_UPDATE_PASSWORD", "true")

  ###############################
  # Include base module
  include base
}
