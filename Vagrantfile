# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation

# Set to "<proto>://<user>:<pass>@<host>:<port>"
$http_proxy    = ENV['HTTP_PROXY'] || ""
$https_proxy   = ENV['HTTPS_PROXY'] || ""
$no_proxy      = ENV['NO_PROXY'] || "localhost,127.0.0.1"
$che_version   = ENV['CHE_VERSION'] || "4.7.2"
$ip            = ENV['CHE_HOST_IP'] || "192.168.28.100"
$hostPort      = (ENV['CHE_PORT'] || 8080).to_i
$containerPort = (ENV['CHE_CONTAINER_PORT'] || ($hostPort == -1 ? 8080 : $hostPort)).to_i
$user_data     = ENV['CHE_DATA'] || "."
$vm_name       = ENV['CHE_VM_NAME'] || "eclipse-che-vm"
$provisionProgress = ENV['PROVISION_PROGRESS'] || "extended"

Vagrant.configure(2) do |config|
  puts ("ECLIPSE CHE: VAGRANT INSTALLER")
  puts ("ECLIPSE CHE: REQUIRED: VIRTUALBOX 5.x")
  puts ("ECLIPSE CHE: REQUIRED: VAGRANT 1.8.x")
  puts ("")
  if ($http_proxy.to_s != '' || $https_proxy.to_s != '') && !Vagrant.has_plugin?("vagrant-proxyconf")
    puts ("You configured a proxy, but Vagrant's proxy plugin not detected.")
    puts ("Install the plugin with: vagrant plugin install vagrant-proxyconf")
    Process.kill 9, Process.pid
  end

  if Vagrant.has_plugin?("vagrant-proxyconf")
    config.proxy.http = $http_proxy
    config.proxy.https = $https_proxy
    config.proxy.no_proxy = $no_proxy
  end

  config.vm.box = "boxcutter/centos72"
  config.vm.box_download_insecure = true
  config.ssh.insert_key = false
  if $ip.to_s.downcase == "dhcp"
    config.vm.network :private_network, type: "dhcp"
  else
    config.vm.network :private_network, ip: $ip
  end
  if $hostPort != -1
    config.vm.network "forwarded_port", guest: $containerPort, host: $hostPort
  end
  config.vm.synced_folder $user_data, "/home/user/che"
  config.vm.define "che" do |che|
  end

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.name = $vm_name
  end

  $script = <<-'SHELL'
    HTTP_PROXY=$1
    HTTPS_PROXY=$2
    NO_PROXY=$3
    CHE_VERSION=$4
    IP=$5
    PORT=$6
    PROVISION_PROGRESS=$7

    if [ "${IP,,}" = "dhcp" ]; then
       echo "----------------------------------------"
       echo "ECLIPSE CHE: CHECKING DYNAMIC IP ADDRESS"
       echo "----------------------------------------"
       DEV=$(grep -l "VAGRANT-BEGIN" /etc/sysconfig/network-scripts/ifcfg-*|xargs grep "DEVICE="|sort|tail -1|cut -d "=" -f 2)
       if [ -z "${DEV}" ]; then
          >&2 echo "Unable to find DHCP network device"
          exit 1
       fi
       IP=$(ip addr show dev ${DEV} | sed -r -e '/inet [0-9]/!d;s/^[[:space:]]*inet ([^[:space:]/]+).*$/\1/')
       if [ -z "${IP}" ]; then
          >&2 echo "Unable to find DHCP network ip"
          exit 1
       fi
       echo "IP: ${IP}"
       echo
    fi

    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
      echo "-------------------------------------"
      echo "."
      echo "ECLIPSE CHE: CONFIGURING SYSTEM PROXY"
      echo "."
      echo "-------------------------------------"
      echo "export http_proxy=$HTTP_PROXY" >> /etc/profile.d/vars.sh
      echo "export https_proxy=$HTTPS_PROXY" >> /etc/profile.d/vars.sh
      echo "export no_proxy=$NO_PROXY" >> /etc/profile.d/vars.sh
      source /etc/profile.d/vars.sh

      # Configuring the Che properties file - mounted into Che container when it starts
      echo "export CHE_PROPERTY_http_proxy=${HTTP_PROXY}" >> /etc/profile.d/vars.sh
      echo "export CHE_PROPERTY_https_proxy=${HTTP_PROXY}" >> /etc/profile.d/vars.sh

      echo "HTTP PROXY set to: $HTTP_PROXY"
      echo "HTTPS PROXY set to: $HTTPS_PROXY"
    fi

    echo "------------------------------"
    echo "ECLIPSE CHE: INSTALLING DOCKER"
    echo "------------------------------"
    sudo yum -y install expect

    # INstall docker
    sudo yum -y update
#    perform $PROVISION_PROGRESS sudo yum install docker-engine
    curl -fsSL https://get.docker.com/ | sh
    sudo service docker start 

    echo $(docker --version)

    # Add the 'vagrant' user to the 'docker' group
    usermod -aG docker vagrant &>/dev/null

    # We need write access to this file to enable Che container to create other containers
    sudo chmod 777 /var/run/docker.sock &>/dev/null

    # Setup the overlay storage driver to eliminate errors
    #sudo sed -i '/ExecStart=\/usr\/bin\/dockerd/c\ExecStart=\/usr\/bin\/dockerd --storage-driver=overlay' /lib/systemd/system/docker.service

    # Configure Docker daemon with the proxy
    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
        mkdir /etc/systemd/system/docker.service.d
    fi
    if [ -n "$HTTP_PROXY" ]; then
        printf "[Service]\nEnvironment=\"HTTP_PROXY=${1}\"" > /etc/systemd/system/docker.service.d/http-proxy.conf
        printf ""
    fi
    if [ -n "$HTTPS_PROXY" ]; then
        printf "[Service]\nEnvironment=\"HTTPS_PROXY=${2}\"" > /etc/systemd/system/docker.service.d/https-proxy.conf
    fi
    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
        printf "[Service]\nEnvironment=\"NO_PROXY=${3}\"" > /etc/systemd/system/docker.service.d/no-proxy.conf
    fi

    systemctl daemon-reload
    systemctl restart docker

    echo "--------------------------------------------------"
    echo "ECLIPSE CHE: DOWNLOADING ECLIPSE CHE DOCKER IMAGES"
    echo "--------------------------------------------------"
    docker pull alpine:latest
    docker pull codenvy/che-launcher:${4}
    docker pull codenvy/che-server:${4}

    curl -sL https://raw.githubusercontent.com/eclipse/che/master/che.sh | tr -d '\15\32' > /home/vagrant/che.sh
    chmod +x /home/vagrant/che.sh
    
    echo "export CHE_PORT=${6}" >> /etc/profile.d/vars.sh
    echo "export CHE_VERSION=${4}" >> /etc/profile.d/vars.sh
    echo "export CHE_HOST_IP=${5}" >> /etc/profile.d/vars.sh
    echo "export CHE_HOSTNAME=${5}" >> /etc/profile.d/vars.sh
    echo "export IS_INTERACTIVE=false" >> /etc/profile.d/vars.sh
    echo "export IS_PSEUDO_TTY=false" >> /etc/profile.d/vars.sh

  SHELL

  config.vm.provision "shell" do |s|
    s.inline = $script
    s.args = [$http_proxy, $https_proxy, $no_proxy, $che_version, $ip, $containerPort, $provisionProgress]
  end

  $script2 = <<-'SHELL'
    IP=$1
    PORT=$2
    MAPPED_PORT=$3

    echo "--------------------------------"
    echo "ECLIPSE CHE: BOOTING ECLIPSE CHE"
    echo "--------------------------------"

    docker run --rm -t -v /var/run/docker.sock:/var/run/docker.sock \
               -e "CHE_PORT=${2}" \
               -e "CHE_RESTART_POLICY=always" \
               -e "CHE_HOST_IP=${1}" \
               -e "CHE_HOSTNAME=${1}" \
               codenvy/che-launcher:${CHE_VERSION} start


    if [ "${IP,,}" = "dhcp" ]; then
       DEV=$(grep -l "VAGRANT-BEGIN" /etc/sysconfig/network-scripts/ifcfg-*|xargs grep "DEVICE="|sort|tail -1|cut -d "=" -f 2)
       IP=$(ip addr show dev ${DEV} | sed -r -e '/inet [0-9]/!d;s/^[[:space:]]*inet ([^[:space:]/]+).*$/\1/')
    fi

    rm -f /home/user/che/.che_url
    rm -f /home/user/che/.che_host_port
    CHE_URL="http://${IP}:${PORT}"

    echo "${CHE_URL}" > /home/user/che/.che_url
    echo "${MAPPED_PORT}" > /home/user/che/.che_host_port
    echo ""
    echo "ECLIPSE CHE READY AT: ${CHE_URL}"

  SHELL

  config.vm.provision "shell", run: "always" do |s|
    s.inline = $script2
    s.args = [$ip, $containerPort, $hostPort]
  end

end
