$mysql_pkg = 'mysql-community-server'

package { 'mysql-community-release':
  ensure    => present,
  provider  => rpm,
  source    => '/vagrant/files/mysql-community-release-el6-5.noarch.rpm',

} -> yumrepo { "mysql56-community":
  enabled   => 0,

} -> yumrepo { "mysql55-community":
  enabled   => 1,

} -> package { $mysql_pkg:
  ensure    => installed,
}

service { 'mysqld':
  ensure  => running,
  require => Package[$mysql_pkg]

} -> mysql_database { 'opal_ids':
  ensure    => present,
  charset   => 'utf8',

} -> mysql_database { 'opal_data':
  ensure    => present,
  charset   => 'utf8',
}
