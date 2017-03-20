class che {

  # creating che.env
  file { "/opt/che/config/che.env":
    ensure  => "present",
    content => template("che/che.env.erb"),
    mode    => "644",
  }

  if $che_dev_env == "on" {
    file { "/opt/che/che.sh":
      ensure  => "present",
      content => template("che/che.sh.erb"),
      mode    => "644",
    }
  }

  # JMX
  file { "/opt/che/config/jmxremote.access":
    ensure  => "present",
    content => "$che_jmx_username readwrite",
    mode    => "644",
  }

  file { "/opt/che/config/jmxremote.password":
    ensure  => "present",
    content => "$che_jmx_username $che_jmx_password",
    mode    => "644",
  }
}
