class postgres {
  file { "/opt/che/config/postgres":
    ensure => "directory",
    mode   => "755",
  } ->
  file { "/opt/che/config/postgres/docker-healthcheck.sh":
    content => template('postgres/docker-healthcheck.sh.erb'),
    ensure  => file,
    mode    => "755",
  }
}
