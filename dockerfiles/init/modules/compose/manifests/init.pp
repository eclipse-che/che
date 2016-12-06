class compose {
  define generate_compose_file($compose_file_name = $name, $compose_file_for_containers = false) {
    file { "/opt/che/$compose_file_name":
      ensure  => "present",
      content => template("compose/docker-compose.yml.erb"),
      mode    => '644',
    }
  }

  compose::generate_compose_file { "docker-compose-container.yml" :
    compose_file_for_containers => true
  }

  compose::generate_compose_file { "docker-compose.yml" :
    compose_file_for_containers => false
  }
}
