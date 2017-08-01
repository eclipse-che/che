class base {
  $dirs = [
    "/opt/che",
    "/opt/che/data",
    "/opt/che/config",
    "/opt/che/logs",
    "/opt/che/templates" ]
  file { $dirs:
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/che/logs/keycloak":
    ensure  => "directory",
    owner   => "1000",
    group   => "1000",
    mode    => "755",
  } ->
  file { "/opt/che/data/keycloak":
    ensure  => "directory",
    owner   => "1000",
    group   => "1000",
    mode    => "755",
  }

  include che
  include compose
  include traefik
  include postgres
  include keycloak
}
