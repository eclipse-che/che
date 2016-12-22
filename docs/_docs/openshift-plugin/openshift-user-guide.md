---
tags: [ "eclipse" , "che" ]
title: OpenShift Use
excerpt: "How to use the OpenShift plug-in."
layout: openshift
permalink: /:categories/user-guide/
---
# Run Che  

```shell  
# if you downloaded binaries
/bin/che.sh run

# if you built Che from source

assembly/openshift-plugin-assembly-main/target/eclipse-che-${version}/eclipse-che-${version}/bin/che.sh run\
```
Che will be available at `http://localhost:8080`
# Connect Account  
Login to your account **OpenShift > Connect Account**. You will retrieve a token using authentication method set up for your OpenShift installation (login/password, Google oAuth etc).
# Create Application From Template  
Create a new app from a template at **OpenShift > Create Application From Template**. This will both create a new OpenShift project (namespace) and a set of configs, as well as import an app into Che.

Once an app is imported, OpenShift metadata is added to project attributes, thus a connection between Che and OpenShift resources is established.
#### Import a Module
}  


![module.png]({{ base }}/assets/imgs/module.png)

#### Development cycle
Since projects created from OpenShift templates use GitHub repositories that you cannot push to, use templates just for demo purposes. Alternatively, you may replace Git URL in *buildConfig* with the one you have push permissions at `OpenShift > Manage Configs`.  


# Import Existing OpenShift App  
When importing an existing OpenShift application, the plugin checks for all *buildConfigs* in all user namespaces, extracts Git URLs and shows them in the application list, grouped by a namespace.
# Deploy Existing App to OpenShift  
Deployment to OpenShift triggers two actions: marking Che project as OpenShift project type and creating a set of objects on the OpenShift side.

A user can choose/edit:

* namespace where all the objects will be created (new vs existing one)
* builder image, i.e. where the code will be compiled (if applicable) and deployed with an app server
* environment variables/labels and their values

You'll need to enter both project and application names.
# Link Che App With Existing OpenShift App / Unlink App  
Che projects can be linked with existing OpenShift applications at **OpenShift > Deploy > Link With Existing App**. When a Che project is linked to OpenShift application, OpenShift metadata is saved to Che project attributes (namespace and label).

It is possible to `'unlink'` Che project from OpenShift, which means clearing Che project attributes. Unlinking project does not delete OpenShift namespace and its associated resources.
# Delete OpenShift Project  
It is possible to delete an OpenShift project only when a related Che project is selected in a project tree. Deleting OpenShift project resets Che project (OpenShift attributes are removed) and deletes OpenShift namespace.

If an OpenShift namespace has multiple `buildConfigs` (apps), a user will be prompted to confirm namespace deletion. Warning message will contain the list of resources that will be deleted.
# Get Webhooks  
GitHub and generic Git webhooks make it possible to notify OpenShift that a Git repo has been updated. Git push will trigger a new build in OpenShift, and a `buildConfig` will clone updated source code. Webhooks are retrieved at `OpenShift > Manage Configs > Build`.
# Show Appication URL  
If a project has been deployed to OpenShift, linked with existing app or imported an as existing OpenShift application, it is possible to get preview URL of a running app (get route) at `OpenShift > Manage Configs > Route`.
# Trigger Build  
`OpenShift > Start Build` will initiate a new build, subscribe to Websocket channels to track build status and stream build logs.

Che will also listen to other build channels, and if a build has been triggered (not necessarily from Che), build logs will be streamed to a dedicated panel.
# Add A Database  
Databases can be added at `OpenShift > Services > Add Service`.

OpenShift Plugin will look for database templates in `openshift` namespace and all user namespaces, let a user edit/add environment variables (username, password, databasename) create objects listed in a template, and add database pod environment variables to application `deploymentConfig`.
#### Database Templates
The plugin filters templates by `database` tag. If you database templates do not have this tag, they will not be listed in the popup.  


# How to Connect to a Database Pod  
Pods use environment variables for communication. Some env variables are shared across all pods running in the same namespace, while others are added to application `deploymentConfig` when a database pod is created.

Having added a database, connect to it, using environment variables.

A few examples:
```java  
System.getenv("MYSQL_USER")
```

```php  
$servername = getenv("MYSQL_PORT_3306_TCP_ADDR");
```
