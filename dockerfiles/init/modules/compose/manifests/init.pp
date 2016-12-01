class compose {
  file { "/opt/che/docker-compose-container.yml":
    ensure  => "present",
    content => template("compose/docker-compose-container.yml.erb"),
    mode    => '644',
  }

  file { "/opt/che/docker-compose.yml":
    ensure  => "present",
    content => template("compose/docker-compose.yml.erb"),
    mode    => '644',
  }

}
