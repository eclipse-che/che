#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   James Drummond - Initial Implementation
#
#
git clone https://github.com/JamesDrummond/che-doc-images.git /tmp/che-doc-images
cp -rf /tmp/che-doc-images/imgs /tmp/main/assets/
cp -rf /tmp/main/* /srv/jekyll/
chown -R jekyll:jekyll /srv/jekyll
jekyll build
rm -f /srv/jekyll/build.sh
rm -f /srv/jekyll/_site/feed.xml
rm -f /srv/jekyll/_site/feed.xslt.xml
rm -f /srv/jekyll/_site/sitemap.xml
