class openshift {
  file { "/opt/che/config/openshift":
    ensure => "directory",
    mode   => "755",
  } ->
  file { 'Openshift scritps and descriptors':
   path => '/opt/che/config/openshift/scripts',
   ensure  => "present",
   source => 'puppet:///modules/openshift/scripts',
   mode   => "755",
   recurse => true,
  }
}
