#!/bin/sh
set -e
set -u

for f in "/etc/passwd" "/etc/group"; do
    chgrp -R 0 ${f}
    chmod -R g+rwX ${f};
done
# Generate passwd.template
cat /etc/passwd | sed s#root:x.*#root:x:\${USER_ID}:\${GROUP_ID}::\${HOME}:/bin/bash#g > ${HOME}/passwd.template
# Generate group.template
cat /etc/group | sed s#root:x:0:#root:x:0:0,\${USER_ID}:#g > ${HOME}/group.template


# Install basic software used for checking github API rate limit
yum install -y epel-release
yum -y install curl jq expect

# define in env variable GITHUB_TOKEN only if it is defined
# else check if github rate limit is enough, else will abort requiring to set GITHUB_TOKEN value

if [ ! -z "${GITHUB_TOKEN-}" ]; then
    export GITHUB_TOKEN=$GITHUB_TOKEN;
    echo "Setting GITHUB_TOKEN value as provided";
else
    export GITHUB_LIMIT=$(curl -s 'https://api.github.com/rate_limit' | jq '.rate .remaining');
    echo "Current API rate limit https://api.github.com is ${GITHUB_LIMIT}";
    if [ "${GITHUB_LIMIT}" -lt 10 ]; then
        printf "\033[0;31m\n\n\nRate limit on https://api.github.com is reached so in order to build this image, ";
        printf "the build argument GITHUB_TOKEN needs to be provided so build will not fail.\n\n\n\033[0m";
        exit 1;
    else
        echo "GITHUB_TOKEN variable not set but https://api.github.com rate limit has enough slots";
    fi
fi

# Add yarn repo
curl -sL https://dl.yarnpkg.com/rpm/yarn.repo | tee /etc/yum.repos.d/yarn.repo
# Install nodejs/npm/yarn
curl --silent --location https://rpm.nodesource.com/setup_8.x | bash -
yum install -y nodejs yarn patch

echo "npm version:"
npm --version
echo "nodejs version:"
node --version

# Include opendjk for Java support
yum install -y gcc-c++ make python git supervisor java-1.8.0-openjdk-devel bzip2

# Clone specific tag of a Theia version
git clone --branch v${THEIA_VERSION} https://github.com/theia-ide/theia ${HOME}/theia-source-code

# Apply patches (if any)
if [ -d "${HOME}/patches/${THEIA_VERSION}" ]; then
    echo "Applying patches for Theia version ${THEIA_VERSION}";
    for file in $(find "${HOME}/patches/${THEIA_VERSION}" -name '*.patch'); do
        echo "Patching with ${file}";
        cd ${HOME}/theia-source-code && patch -p1 < ${file};
    done
fi

# Compile Theia
cd ${HOME}/theia-source-code && yarn

# add registry and start it
npm install -g verdaccio
mkdir ${HOME}/verdaccio
cd ${HOME}/verdaccio
verdaccio &
sleep 3

# Update registry URL to local one
cd ${HOME}
yarn config set registry http://localhost:4873
npm config set registry http://localhost:4873


# Create user for local registry
export USERNAME=theia
export PASSWORD=theia
export EMAIL=che-theia@eclipse.org

/usr/bin/expect <<EOD
spawn npm adduser --registry http://localhost:4873
expect {
  "Username:" {send "$USERNAME\r"; exp_continue}
  "Password:" {send "$PASSWORD\r"; exp_continue}
  "Email: (this IS public)" {send "$EMAIL\r"; exp_continue}
}
EOD

# Now go to source code of theia and publish it
cd ${HOME}/theia-source-code

# using 0.4 there to bump major version so we're sure to not download any 0.3.x dependencies
# Set the version of Theia
export THEIA_VERSION=0.4.1-che

./node_modules/.bin/lerna publish --registry=http://localhost:4873 --exact --repo-version=${THEIA_VERSION} --skip-git --force-publish --npm-tag=latest  --yes
cd ${HOME}

# Code has been published, let's delete it
rm -rf ${HOME}/theia-source-code

# Change version of Theia to specified in THEIA_VERSION
cd ${HOME} && ${HOME}/versions.sh 

# Apply resolution section to the Theia package.json to use strict versions for Theia dependencies
node ${HOME}/resolutions-provider.js ${HOME}/package.json

# avoid issue with checksum of electron
cd ${HOME} && npm install electron-packager -g

# Add default Theia extensions
cd ${HOME} && node ${HOME}/add-extensions.js

# Build Theia with all the extensions
cd ${HOME} && yarn && yarn theia build

# Reset config registry
npm config set registry https://registry.npmjs.org
yarn config set registry https://registry.npmjs.org

# install the latest theia generator of plug-in
npm install -g yo @theia/generator-plugin
mkdir -p ${HOME}/.config/insight-nodejs/
chmod -R 777 ${HOME}/.config/

# Change permissions to allow editing of files for openshift user
find ${HOME} -exec sh -c "chgrp 0 {}; chmod g+rwX {}" \;
# Grant permissions for modifying supervisor log file
touch /var/log/supervisord.log && chmod g+rwX /var/log/supervisord.log && chgrp 0 /var/log/supervisord.log

cd ${HOME}
yarn cache clean

# cleanup stuff installed temporary
yum erase -y jq expect
yum clean all
npm uninstall -g verdaccio
rm -rf ${HOME}/.config/verdaccio/

# remove lock file as well
rm ${HOME}/yarn.lock

# remove installed scripts
rm add-extensions.js resolutions-provider.js versions.sh setup.sh

# remove extensions file
rm extensions.json

# use typescript globally (to have tsc/typescript working)
npm install -g typescript@2.8.4
