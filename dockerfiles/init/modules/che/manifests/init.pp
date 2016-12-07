class che {

# creating che.env
  file { "/opt/che/config/che.env":
    ensure  => "present",
    content => template("che/che.env.erb"),
    mode    => "644",
  }
}
