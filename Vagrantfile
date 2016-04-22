# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
Vagrant.configure(2) do |config|
  config.vm.box = "boxcutter/centos71-docker"
  config.vm.box_download_insecure = true
  config.vm.network :private_network, ip: "192.168.28.30"
  config.vm.define "che" do |che|
  end
  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.name = "eclipse-che-vm"
  end

  config.vm.provision "shell", inline: <<-SHELL
    usermod -aG docker vagrant &>/dev/null
    echo "."
    echo "."
    echo "ECLIPSE CHE: INSTALLING JAVA"
    echo "."
    echo "."
    curl -H "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" -L -o jdk8-linux-x64.rpm "http://download.oracle.com/otn-pub/java/jdk/8u74-b02/jdk-8u74-linux-x64.rpm"
    yum localinstall -y jdk8-linux-x64.rpm &>/dev/null

    echo "."
    echo "."
    echo "ECLIPSE CHE: DOWNLOADING ECLIPSE CHE"
    echo "."
    echo "."
    curl -O "https://install.codenvycorp.com/che/eclipse-che-latest.tar.gz"
    tar xvfz eclipse-che-latest.tar.gz &>/dev/null
    sudo chown -R vagrant:vagrant * &>/dev/null
    export JAVA_HOME=/usr &>/dev/null

    echo "."
    echo "."
    echo "ECLIPSE CHE: PREPPING SERVER"
    echo "."
    echo "."

    echo vagrant | sudo -S -E -u vagrant /home/vagrant/eclipse-che-*/bin/che.sh --remote:192.168.28.28 --skip:client -g start

  SHELL

  config.vm.provision "shell", run: "always", inline: <<-SHELL
    echo "."
    echo "."
    echo "ECLIPSE CHE: SERVER BOOTING ~10s"
    echo "AVAILABLE: http://192.168.28.30:8080"
    echo "."
    echo "."

  SHELL
end
