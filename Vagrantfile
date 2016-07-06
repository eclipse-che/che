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
$che_version   = ENV['CHE_VERSION'] || "latest"
$ip            = ENV['CHE_IP'] || "192.168.28.100"
$hostPort      = (ENV['CHE_PORT'] || 8080).to_i
$containerPort = (ENV['CHE_CONTAINER_PORT'] || ($hostPort == -1 ? 8080 : $hostPort)).to_i
$user_data     = ENV['CHE_DATA'] || "."

$provisionProgress = ENV['PROVISION_PROGRESS'] || "basic"

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

  config.vm.box = "boxcutter/centos72-docker"
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
    vb.name = "eclipse-che-vm"
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
      echo 'export HTTP_PROXY="'$HTTP_PROXY'"' >> /home/vagrant/.bashrc
      echo 'export HTTPS_PROXY="'$HTTPS_PROXY'"' >> /home/vagrant/.bashrc
      source /home/vagrant/.bashrc

      # Configuring the Che properties file - mounted into Che container when it starts
      echo 'http.proxy="'$HTTP_PROXY'"' >> /home/user/che/conf/che.properties
      echo 'https.proxy="'$HTTPS_PROXY'"' >> /home/user/che/conf/che.properties

      echo "HTTP PROXY set to: $HTTP_PROXY"
      echo "HTTPS PROXY set to: $HTTPS_PROXY"
    fi

    function perform
    {
      local progress=$1
      local command=$2
      shift 2
      
      local pid=""
      
      case "$progress" in
        extended)
          # simulate tty environment to get full output of progress bars and percentages
          printf "set timeout -1\nspawn %s\nexpect eof" "$command $*" | expect -f -
          ;;
        basic|*)
          $command "$@" &>/dev/null &
          pid=$!
          while kill -0 "$pid" >/dev/null 2>&1; do
            printf "#"
            sleep 10
          done
          wait $pid # return pid's exit code
          ;;
      esac
    }
    
    echo "------------------------------------"
    echo "ECLIPSE CHE: UPGRADING DOCKER ENGINE"
    echo "------------------------------------"
    if [ "$PROVISION_PROGRESS" = "extended" ]; then
       # we sacrifice a few seconds of additional install time for much better progress afterwards
       perform basic yum -y install expect
    fi
    perform $PROVISION_PROGRESS sudo yum -y update docker-engine

    echo $(docker --version)
 
    # Add the 'vagrant' user to the 'docker' group
    usermod -aG docker vagrant &>/dev/null

    # We need write access to this file to enable Che container to create other containers
    sudo chmod 777 /var/run/docker.sock &>/dev/null

    # Configure Docker daemon with the proxy
    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
        mkdir /etc/systemd/system/docker.service.d
    fi
    if [ -n "$HTTP_PROXY" ]; then
        printf "[Service]\nEnvironment=\"HTTP_PROXY=${HTTP_PROXY}\"" > /etc/systemd/system/docker.service.d/http-proxy.conf
        printf ""
    fi
    if [ -n "$HTTPS_PROXY" ]; then
        printf "[Service]\nEnvironment=\"HTTPS_PROXY=${HTTPS_PROXY}\"" > /etc/systemd/system/docker.service.d/https-proxy.conf
    fi
    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
        printf "[Service]\nEnvironment=\"NO_PROXY=${NO_PROXY}\"" > /etc/systemd/system/docker.service.d/no-proxy.conf
        systemctl daemon-reload
        systemctl restart docker
    fi

    echo "-------------------------------------------------"
    echo "ECLIPSE CHE: DOWNLOADING ECLIPSE CHE DOCKER IMAGE"
    echo "-------------------------------------------------"
    perform $PROVISION_PROGRESS docker pull codenvy/che:${CHE_VERSION}

    echo "--------------------------------"
    echo "ECLIPSE CHE: BOOTING ECLIPSE CHE"
    echo "--------------------------------"
    docker run --net=host --name=che --restart=always --detach `
              `-v /var/run/docker.sock:/var/run/docker.sock `
              `-v /home/user/che/lib:/home/user/che/lib-copy `
              `-v /home/user/che/workspaces:/home/user/che/workspaces `
              `-v /home/user/che/storage:/home/user/che/storage `
              `-v /home/user/che/conf:/container `
              `-e CHE_LOCAL_CONF_DIR=/container `
              `codenvy/che:${CHE_VERSION} --remote:${IP} --port:${PORT} run &>/dev/null
  SHELL

  config.vm.provision "shell" do |s| 
    s.inline = $script
    s.args = [$http_proxy, $https_proxy, $no_proxy, $che_version, $ip, $containerPort, $provisionProgress]
  end

  $script2 = <<-'SHELL'
    IP=$1
    PORT=$2
    MAPPED_PORT=$3

    if [ "${IP,,}" = "dhcp" ]; then
       DEV=$(grep -l "VAGRANT-BEGIN" /etc/sysconfig/network-scripts/ifcfg-*|xargs grep "DEVICE="|sort|tail -1|cut -d "=" -f 2)
       IP=$(ip addr show dev ${DEV} | sed -r -e '/inet [0-9]/!d;s/^[[:space:]]*inet ([^[:space:]/]+).*$/\1/')
    fi

    rm -f /home/user/che/.che_url
    rm -f /home/user/che/.che_host_port
    CHE_URL="http://${IP}:${PORT}"

    # Test the default dashboard page to see when it returns a non-error value.
    # Che is active once it returns success        
    while [ true ]; do
      printf "#"
      curl -v ${CHE_URL}/dashboard &>/dev/null
      exitcode=$?
      if [ $exitcode == "0" ]; then
        echo "${CHE_URL}" > /home/user/che/.che_url
        echo "${MAPPED_PORT}" > /home/user/che/.che_host_port
        echo "---------------------------------------"
        echo "ECLIPSE CHE: BOOTED AND REACHABLE"
        echo "ECLIPSE CHE: ${CHE_URL}      "
        echo "---------------------------------------"
        exit 0             
      fi 
      sleep 10
    done
  SHELL

  config.vm.provision "shell", run: "always" do |s|
    s.inline = $script2
    s.args = [$ip, $containerPort, $hostPort]
  end

end
