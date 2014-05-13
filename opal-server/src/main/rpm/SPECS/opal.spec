Name:       opal
Version:    2.2_SNAPSHOT_b20140512175607
Release:    1%{?dist}
Summary:    OBiBa Data integration Web application for biobanks
Group:      Applications/Engineering
License:    see /usr/share/doc/opal/copyright
URL:        http://www.obiba.org

Prefix:     /usr/local
BuildArchitectures: noarch

Requires:   daemon java-1.7.0-openjdk

%description
Opal is OBiBa?s core database application for biobanks. Participant data, once
collected from any data source, must be integrated and stored in a central
data repository under a uniform model. Opal is such a central repository. It
can import, process, validate, query, analyze, report, and export data. Opal
is typically used in a research center to analyze the data acquired at
assessment centres. Its ultimate purpose is to achieve seamless data-sharing
among biobanks.

OBiBa is an international software development project committed to building a
full suite of open source software for biobanks. It is comprised of several
independent and self-funded teams around the world, each of which is producing
stand-alone applications that support particular biobank activities. The
applications can be customized and integrated to create a complete biobank
information management system

#%prep

#Install/Erase-time Scripts
#argument passed to %pre %post %preun and %postun is the number of other versions installed for this package, so...
#When the first version of a package is installed, its %pre and %post scripts will be passed an argument equal to 1
#When the last version of a package is erased, its %preun and %postun scripts will be passed an argument equal to 0

#executes just before the package is to be installed. rarely used
%pre

#executes after the package has been installed. Examples of actions here: ldconfig, installing shells
%post

#executes just before the package is uninstalled
%preun

#executes after the package has been removed. Examples of actions here: remove shared libraries using ldconfig
%postun

%autosetup

#%build

%install
pwd

mkdir -p "$RPM_BUILD_ROOT"
cp -r * "$RPM_BUILD_ROOT"
mkdir -p "$RPM_BUILD_ROOT"/var/lib/opal
mkdir -p "$RPM_BUILD_ROOT"/var/log/opal
mkdir -p "$RPM_BUILD_ROOT"/tmp/opal
ls "$RPM_BUILD_ROOT"

%clean
echo "clean cwd is $(pwd)"
#rm -rf "$RPM_BUILD_ROOT"


%files
/etc/opal
/usr/share/@opal.dir@
%dir /var/lib/opal
%dir /var/log/opal
%dir /tmp/opal

%doc
/usr/share/doc/opal

%changelog
