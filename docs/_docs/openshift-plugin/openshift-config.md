---
tags: [ "eclipse" , "che" ]
title: Openshift Install
excerpt: "How to install the OpenShift Plug-in."
layout: openshift
permalink: /:categories/config/
---
# Download  
### Binaries
[Get packaged zip file](http://maven.codenvycorp.com/content/repositories/codenvy-public-snapshots/org/eclipse/che/openshift-plugin-assembly-main/)

### Build From Source
```shell  
git clone https://github.com/codenvy/plugin-openshift
cd plugin-openshift
mvn clean install\
```

# Configure Che  

```shell  
# Create a directory to host conf files
mkdir -p /home/user/.che/plugin-conf

# Set CHE_LOCAL_CONF_DIR to be this new folder
echo "export CHE_LOCAL_CONF_DIR=/home/user/.che" >> ~/.bashrc
```
Create `~/.che/che.properties` and set OpenShift parameters.
```toml  
openshift.api.endpoint=https://your.openshift.instance.com/
oauth.openshift.authuri=https://your.openshift.instance.com/oauth/authorize
oauth.openshift.tokenuri=https://your.openshift.instance.com/oauth/token
oauth.openshift.clientid=yourID
oauth.openshift.clientsecret=yourSecret
oauth.openshift.redirecturis=http://localhost:${SERVER_PORT}/wsmaster/api/oauth/callback\
```
Create `~/.che/plugin-conf/che.properties` and set a single OpenShift parameter:
```toml  
openshift.api.endpoint=https://your.openshift.instance.com/\
```
Both configuration files are mandatory.
#### What is my API endpoint client ID and secret?
API endpoint is the URL of your OpenShift installation (can be a domain name or IP address). Client ID and secret should match those registered in a [custom oAuth client](#register-a-custom-oauth-client).  


# Register a Custom oAuth Client  
Che uses oAuth2 to get authentication tokens from OpenShift. You need to register a custom oAuth client in your OpenShift master node. Example, using `oc`:
```shell  
# Requires OpenShift admin privs
oc create -f <(echo '
{
  "kind": "OAuthClient\n  "apiVersion": "v1\n  "metadata": {
    "name": "che"
  },
  "secret": "yoursecret\n  "redirectURIs": [
    "http://localhost:8080/wsmaster/api/oauth/callback"
  ]
}')
```
