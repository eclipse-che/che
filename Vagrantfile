# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation

# Set to "<proto>://<user>:<pass>@<host>:<port>"
$http_proxy  = ""
$https_proxy = ""
$no_proxy    = "localhost,127.0.0.1"
$che_version = "nightly"
$ip          = "192.168.28.100"
$port        = 8080

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
  config.vm.network :private_network, ip: $ip
  config.vm.network "forwarded_port", guest: $port, host: $port
  config.vm.synced_folder ".", "/home/user/che"
  config.vm.define "che" do |che|
  end

  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.name = "eclipse-che-vm"
  end

  $script = <<-SHELL
    HTTP_PROXY=$1
    HTTPS_PROXY=$2
    NO_PROXY=$3
    CHE_VERSION=$4

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
      echo 'http.proxy="'$HTTP_PROXY'"' >> /home/user/che/che.properties
      echo 'https.proxy="'$HTTPS_PROXY'"' >> /home/user/che/che.properties

      echo "HTTP PROXY set to: $HTTP_PROXY"
      echo "HTTPS PROXY set to: $HTTPS_PROXY"
    fi

    # Add the user in the VM to the docker group
    echo "------------------------------------"
    echo "ECLIPSE CHE: UPGRADING DOCKER ENGINE"
    echo "------------------------------------"
    echo 'y' | sudo yum update docker-engine &>/dev/null &
    PROC_ID=$!
    while kill -0 "$PROC_ID" >/dev/null 2>&1; do
      printf "#"
      sleep 10
    done

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
    docker pull codenvy/che:${CHE_VERSION} &>/dev/null &
    PROC_ID=$!
 
    while kill -0 "$PROC_ID" >/dev/null 2>&1; do
      printf "#"
      sleep 10
    done
  SHELL

  config.vm.provision "shell" do |s| 
    s.inline = $script
    s.args = [$http_proxy, $https_proxy, $no_proxy, $che_version]
  end

  $script2 = <<-SHELL
    CHE_VERSION=$1
    IP=$2
    PORT=$3

    echo "--------------------------------"
    echo "ECLIPSE CHE: BOOTING ECLIPSE CHE"
    echo "--------------------------------"
    docker run --net=host --name=che --restart=always --detach `
              `-v /var/run/docker.sock:/var/run/docker.sock `
              `-v /home/user/che/lib:/home/user/che/lib-copy `
              `-v /home/user/che/workspaces:/home/user/che/workspaces `
              `-v /home/user/che/storage:/home/user/che/storage `
              `-v /home/user/che/che.properties:/container/che.properties `
              `-e CHE_LOCAL_CONF_DIR=/container `
              `codenvy/che:${CHE_VERSION} --remote:${IP} --port:${PORT} run &>/dev/null
    
    # Test the default dashboard page to see when it returns a non-error value.
    # Che is active once it returns success        
    while [ true ]; do
      printf "#"
      curl -v http://${IP}:${PORT}/dashboard &>/dev/null
      exitcode=$?
      if [ $exitcode == "0" ]; then
        echo "---------------------------------------"
        echo "ECLIPSE CHE: BOOTED AND REACHABLE"
        echo "ECLIPSE CHE: http://${IP}:${PORT}      "
        echo "---------------------------------------"
        exit 0             
      fi 
      sleep 10
    done
  SHELL

  config.vm.provision "shell", run: "always" do |s|
    s.inline = $script2
    s.args = [$che_version, $ip, $port]
  end

end
