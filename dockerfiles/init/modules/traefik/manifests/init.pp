class traefik {

  # creating traefik.toml
  file { "/opt/che/config/traefik.toml":
    ensure  => "present",
    content => template("traefik/traefik.toml.erb"),
    mode    => "644",
  }

}
