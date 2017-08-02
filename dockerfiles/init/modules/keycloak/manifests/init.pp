class keycloak {
  file { "/opt/che/config/keycloak":
    ensure => "directory",
    mode   => "755",
  } ->
  file { "/opt/che/config/keycloak/che-realm.json":
    ensure  => "present",
    content => template("keycloak/che-realm.json.erb"),
    mode    => "644",
  } ->
  file { "/opt/che/config/keycloak/che-users-0.json":
    ensure  => "present",
    content => template("keycloak/che-users-0.json.erb"),
    mode    => "644",
  } ->
  file { "/opt/che/config/keycloak/master-realm.json":
    ensure  => "present",
    content => template("keycloak/master-realm.json.erb"),
    mode    => "644",
  } ->
  file { "/opt/che/config/keycloak/master-users-0.json":
    ensure  => "present",
    content => template("keycloak/master-users-0.json.erb"),
    mode    => "644",
  } ->
  file { 'keycloak theme custom login page':
   path => '/opt/che/config/keycloak/che',
   ensure  => "present",
   source => 'puppet:///modules/keycloak/che',
   recurse => true,
  }
}
