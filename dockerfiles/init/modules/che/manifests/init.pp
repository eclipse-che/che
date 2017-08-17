class che {
  file { "/opt/che/config/che":
    ensure => "directory",
    mode   => "755",
  } ->
  # creating che.env
  file { "/opt/che/config/che/che.env":
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
  file { "/opt/che/config/che/jmxremote.access":
    ensure  => "present",
    content => "$che_jmx_username readwrite",
    mode    => "644",
  }

  file { "/opt/che/config/che/jmxremote.password":
    ensure  => "present",
    content => "$che_jmx_username $che_jmx_password",
    mode    => "644",
  }
}
