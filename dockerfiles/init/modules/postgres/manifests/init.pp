class postgres {
  file { "/opt/che/data/postgres":
    ensure  => "directory",
    owner  => 26,
    group  => 0,
    mode    => "755",
  } ->
  file { "/opt/che/config/postgres":
    ensure  => "directory",
    mode    => "755",
  } ->
  file { "/opt/che/config/postgres/init-che-user.sh":
    content                 => template('postgres/init-che-user.sh.erb'),
    ensure                  => file,
    mode                    => "755",
  } ->
  file { "/opt/che/config/postgres/init-che-user-and-run.sh":
    content                 => template('postgres/init-che-user-and-run.sh.erb'),
    ensure                  => file,
    mode                    => "755",
  }
}
