# Set to "<proto>://<user>:<pass>@<host>:<port>"
$http_proxy  = ""
$https_proxy = ""
$che_version = "latest"
$ip          = "192.168.28.28"

Vagrant.configure(2) do |config|
  config.vm.box = "centos71-docker-java-v1.0"
  config.vm.box_url = "https://install.codenvycorp.com/centos71-docker-java-v1.0.box"
  config.vm.box_download_insecure = true
  config.ssh.insert_key = false
  config.vm.network :private_network, ip: $ip
  config.vm.synced_folder ".", "/home/vagrant/.che"
  config.vm.define "artik" do |artik|
  end
  config.vm.provider "virtualbox" do |vb|
    vb.memory = "4096"
    vb.name = "artik-ide-vm"
    vb.customize ["modifyvm", :id, "--usb", "on"]
    vb.customize ["modifyvm", :id, "--usbehci", "on"]
    vb.customize ["usbfilter", "add", "0",
                  "--target", :id,
                  "--name", "Artik"]
  end

  $script = <<-SHELL
    HTTP_PROXY=$1
    HTTPS_PROXY=$2
    CHE_VERSION=$3
    IP=$4

    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
	    echo "-----------------------------------"
	    echo "."
	    echo "ARTIK IDE: CONFIGURING SYSTEM PROXY"
	    echo "."
	    echo "-----------------------------------"
	    echo 'export HTTP_PROXY="'$HTTP_PROXY'"' >> /home/vagrant/.bashrc
	    echo 'export HTTPS_PROXY="'$HTTPS_PROXY'"' >> /home/vagrant/.bashrc
	    source /home/vagrant/.bashrc
	    echo "HTTP PROXY set to: $HTTP_PROXY"
	    echo "HTTPS PROXY set to: $HTTPS_PROXY"
    fi

    # Add the user in the VM to the docker group
    usermod -aG docker vagrant &>/dev/null

    # Configure Docker daemon with the proxy
    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
        mkdir /etc/systemd/system/docker.service.d
    fi
    if [ -n "$HTTP_PROXY" ]; then
        printf "[Service]\nEnvironment=\"HTTP_PROXY=${HTTP_PROXY}\"" > /etc/systemd/system/docker.service.d/http-proxy.conf
    fi
    if [ -n "$HTTPS_PROXY" ]; then
        printf "[Service]\nEnvironment=\"HTTPS_PROXY=${HTTPS_PROXY}\"" > /etc/systemd/system/docker.service.d/https-proxy.conf
    fi
    if [ -n "$HTTP_PROXY" ] || [ -n "$HTTPS_PROXY" ]; then
        printf "[Service]\nEnvironment=\"NO_PROXY=localhost,127.0.0.1\"" > /etc/systemd/system/docker.service.d/no-proxy.conf
        systemctl daemon-reload
        systemctl restart docker
    fi

    echo "--------------------------------"
    echo "."
    echo "ARTIK IDE: DOWNLOADING ARTIK IDE"
    echo "."
    echo "--------------------------------"
    curl -O "https://install.codenvycorp.com/artik/samsung-artik-ide-${CHE_VERSION}.tar.gz"
    tar xvfz samsung-artik-ide-${CHE_VERSION}.tar.gz &>/dev/null
    sudo chown -R vagrant:vagrant * &>/dev/null
    export JAVA_HOME=/usr &>/dev/null

    # exporting CHE_LOCAL_CONF_DIR, reconfiguring Che to store workspaces, projects and prefs outside the Tomcat
    export CHE_LOCAL_CONF_DIR=/home/vagrant/.che &>/dev/null
    cp /home/vagrant/eclipse-che-*/conf/che.properties /home/vagrant/.che/
    sed -i 's|${catalina.base}/temp/local-storage|/home/vagrant/.che|' /home/vagrant/.che/che.properties
    sed -i 's|${che.home}/workspaces|/home/vagrant/.che|' /home/vagrant/.che/che.properties
    echo 'export CHE_LOCAL_CONF_DIR=/home/vagrant/.che' >> /home/vagrant/.bashrc

    echo "------------------------------------------"
    echo "."
    echo "ARTIK IDE: DOWNLOADING ARTIK RUNTIME IMAGE"
    echo "           950MB: SILENT OUTPUT           "
    echo "."
    echo "------------------------------------------"
    docker pull codenvy/artik &>/dev/null

    echo "--------------------------------------"
    echo "."
    echo "ARTIK IDE: DOWNLOADING DOCKER REGISTRY"
    echo "           50MB: SILENT OUTPUT        "
    echo "."
    echo "--------------------------------------"
    docker pull registry:2 &>/dev/null

    echo "-------------------------------"
    echo "."
    echo "ARTIK IDE: PREPPING SERVER ~10s"
    echo "."
    echo "-------------------------------"
    if [ -n "$HTTP_PROXY" ]; then
        sed -i "s|http.proxy=|http.proxy=${HTTP_PROXY}|" /home/vagrant/eclipse-che-*/conf/che.properties
    fi
    if [ -n "$HTTPS_PROXY" ]; then
        sed -i "s|https.proxy=|https.proxy=${HTTPS_PROXY}|"  /home/vagrant/eclipse-che-*/conf/che.properties
    fi
    echo vagrant | sudo -S -E -u vagrant /home/vagrant/eclipse-che-*/bin/che.sh --remote:${IP} --skip:client -g start &>/dev/null
  SHELL

  config.vm.provision "shell" do |s|
  	s.inline = $script
  	s.args = [$http_proxy, $https_proxy, $che_version, $ip]
  end

  $script2 = <<-SHELL
    IP=$1
    counter=0
    while [ true ]; do
      curl -v http://${IP}:8080/dashboard &>/dev/null
      exitcode=$?
      if [ $exitcode == "0" ]; then
        echo "----------------------------------------"
        echo "."
        echo "ECLIPSE CHE: SERVER BOOTED AND REACHABLE"
        echo "AVAILABLE: http://${IP}:8080  "
        echo "."
        echo "----------------------------------------"
        exit 0
      fi 
      # If we are not awake after 60 seconds, restart server
      if [ $counter == "11" ]; then
        echo "-----------------------------------------------"
        echo "."
        echo "ECLIPSE CHE: SERVER NOT RESPONSIVE -- REBOOTING"
        echo "."
        echo "-----------------------------------------------"
        export JAVA_HOME=/usr &>/dev/null
        echo vagrant | sudo -S -E -u vagrant /home/vagrant/eclipse-che-*/bin/che.sh --remote:${IP} --skip:client -g start &>/dev/null
      fi
      # If we are not awake after 180 seconds, exit with failure
      if [ $counter == "35" ]; then
        echo "---------------------------------------------"
        echo "."
        echo "ECLIPSE CHE: SERVER NOT RESPONSIVE -- EXITING"
        echo "           CONTACT SUPPORT FOR ASSISTANCE    "
        echo "."
        echo "---------------------------------------------"
        exit 0
      fi
      let counter=counter+1
      sleep 5
    done
  SHELL

  config.vm.provision "shell", run: "always" do |s|
    s.inline = $script2
    s.args = [$ip]
  end

end
