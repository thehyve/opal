$jre = 'java-1.7.0-openjdk'

$packages = [ $jre ]

package { $packages:
	ensure => installed,
}

yumrepo { "opal":
  baseurl   => "https://repo.thehyve.nl/content/groups/public",
  descr     => "Opal Repository",
  enabled   => 1,
  gpgcheck  => 0
} -> package { 'opal-server':
  ensure    => latest,
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


