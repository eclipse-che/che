class traefik {
  file { "/opt/che/config/traefik":
    ensure => "directory",
    mode   => "755",
  } ->
  # creating traefik.toml
  file { "/opt/che/config/traefik/traefik.toml":
    ensure  => "present",
    content => template("traefik/traefik.toml.erb"),
    mode    => "644",
  }
}
