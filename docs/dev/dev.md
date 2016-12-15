## Usage

- This script is intended to be run through codenvy using the default java stack. 
- Add "machine.server.extra.volume=/var/run/docker.sock:/var/run/docker.sock" to the codenvy.env config file.

```shell
sudo apt-get update
sudo apt-get install apt-transport-https ca-certificates
sudo apt-get update
sudo apt-key adv \
               --keyserver hkp://ha.pool.sks-keyservers.net:80 \
               --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" | sudo tee /etc/apt/sources.list.d/docker.list
sudo apt-get update
apt-cache policy docker-engine
cd /projects/<project name>/docs/dev
chmod +x jekyll.sh
./jekyll.sh <html bind port>
# ex. ./jekyll.sh 83 would be available at http://<host ip>:83/docs/