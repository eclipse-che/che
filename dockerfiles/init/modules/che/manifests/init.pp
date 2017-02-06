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
}
