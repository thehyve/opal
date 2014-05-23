$jre = 'java-1.7.0-openjdk'

$packages = [ $jre ]

package { $packages:
	ensure => installed,
}

$java_sec = '/usr/lib/jvm/jre-1.7.0/lib/security/java.security'

Exec {
	path => '/bin:/usr/bin',
}

exec { 'disable-nss':
  command => "sed -i 's/^\\([^#].*\\/nss.cfg\\)$/#\\1/' '$java_sec'",
	unless  => "grep '^#security\\.provider\\.10' '$java_sec'",
	require => Package[$jre],
} #~> Service['opal']


file { "/etc/yum.repos.d/opal.repo":
  source  => "/vagrant/files/opal.repo",
} -> package { 'opal-server':
  ensure => latest,
}