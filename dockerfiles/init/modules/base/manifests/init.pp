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
  }
  
  include che
  include compose
  include traefik
}
