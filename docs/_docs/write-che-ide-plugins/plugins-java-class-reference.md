---
tags: [ "eclipse" , "che" ]
title: Java Class Reference
excerpt: "JavaDoc for the Che & GWT classes available to developers"
layout: docs
permalink: /:categories/java-class-reference/
---
You can generate the JavaDoc for your installation from source.
```shell  
# You need the che-core library
git clone http://github.com/eclipse/che
git checkout {version-that-matches-your-install}
cd core/
mvn javadoc:aggregate

# JavaDoc available at:
/core/target/site/apidocs/index.html\
```
