# Opal Vagrant

This module provides a Vagrant/Puppet setup for hosting Opal.
The current Vagrant/Puppet setup and Opal packaging is targeting a .deb based Linux distribution (Debian or Ubuntu).
There is still no concrete decision on the OS to use, so these instructions will be slightly different if we turn to Red Hat/CentOS.
The Vagrant/Puppet setup will also require changes if we step away from the .deb packaging.

## Installation

1. Install Vagrant from

    http://www.vagrantup.com/downloads
2. Download the latest Opal Vagrant bundle from

    [Latest Opal Vagrant zip](http://repo.thehyve.nl/service/local/artifact/maven/redirect?r=snapshots&g=org.obiba.opal&a=opal-vagrant&e=zip&v=LATEST&c=vagrant)<br>
This is a very small file that only contains descriptors on how to prepare the guest VM and required dependencies and system settings<br>
3. Unzip this file to a folder in the host machine (where you want your Vagrant image to be located).<br>
The Vagrantfile defines some important VM settings, but most of them can be overriden locally.<br>
All overridable settings are described in the file 'config.yaml.sample', along with the defaults.<br>
If you need to customize something, create a 'config.yaml' copying from 'config.yaml.sample', edit the values you want and comment/remove all the others.<br>
Please kept all your customization in this config.yaml filee, and don't modify the Vagrantfile directly.<br>
This way we can update/enhance later on the Vagrantfile making sure your settings are kept.<br>
Please refer to section 'Guest VM customization' for concrete examples<br>
4. In the same folder of Vagrantfile, run

    vagrant up


## Guest VM customization

Examples of what you can modify in the config.yaml:
* the box (guest VM image) to use. The current setup requires it to be a recent Debian based Linux (Debian or Ubuntu)
* if you want box automatic update (better leave the defaults)
* guest VM to run in headless mode or not
* aspects of the 'hardware', like number of CPUs, memory, etc..
* ports forwarded from host to guest VM
* synchronized folders (existing in the host machine and mounted in the guest VM)

## After booting the VM

Opal is now up and running, and available in the host machine on the configured ports.
This Opal Vagrant VM provides:
* running Opal on the defaults ports (the actual host ports will depend on the Vagrantfile changes)
* running Opal-rserver
* running MySQL instance on the default ports

Please modify all the administration passwords, as they are all in the default values.

You can at any moment modify these settings and try others, then relaunch the VM by running

    vagrant reload

To login in the guest VM, run

    vagrant ssh

To shutdown the VM, run

    vagrant halt

And to boot it up again, run

    vagrant up

Please refer to Vagrant documentation for more information at
    http://docs.vagrantup.com/v2/getting-started/index.html

## Opal Post-Install Configuration

Before start using Opal, we need to create the necessary databases.
We need at least an identifiers database and data databases.
The easiest is to create the databases in the local running instance of MySQL, but probably for production,
using external MySQL instances will be better from a data warehousing/replication point of view.

Its also possible to install and use MongoDB in the VM (you have to do it yourselves),
or use an external MySQL or MongoDB (you have to configure the ports for that).
Its possible to create several databases (even use a combination of MySQL and MongoDB), depending on the needs and uses.

Remember that most of these tweaks can be accomplished just by modifying the Vagrantfile, and do a vagrant reload.

The very fist time you login in Opal, you will be presented with the 'Post-Install Configuration' screen.
Here you should register to the datasources created before.
There are 2 major types of datasources (identifiers and data), and you should register at least one of each.

## Opal service commands
Opal is registered as a service, so we can run in the guest VM commands like

    sudo service opal stop
    sudo service opal start
    sudo service opal restart

## Update opal (for Debian based box)

To update opal, we just need to run in the guest VM:

    sudo apt update
    sudo apt upgrade opal

A restart on the Opal service will be needed after an upgrade
