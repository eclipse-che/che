## Context

All commands are executed in the directory where this README is located.

## Templates and Parameters

Templates are provided with a set of predefined params which you can override with with `-p key=value`.
You can list all params before applying a template: `oc process --parameters -f <filename>`.
If you miss envs and parameters, you can add them to your template both as a parameters env variables.

Examples below reference `oc-new app` and `oc apply` commands.
`oc new-app` accepts parameters envs or env file which makes it possible to override default params and pass envs to chosen deployments (even if they are not in a template):

```
oc new-app -f example.yaml -p PARAM=VALUE -e ENV=VALUE --env-file=che.env
```
More info is available in [OpenShift documentation](https://docs.openshift.com/container-platform/3.7/dev_guide/application_lifecycle/new_app.html#specifying-a-template).

Env file has a simple format: `KEY=VALUE` per line.

You can also use `oc process` and then apply the resulted output `| oc apply -f -`, for example:

```
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io | oc apply -f -
```
In this case, however, it is not possible to pass envs, only params are available.

IMPORTANT! **ROUTING_SUFFIX** value in the below commands `$(minishift ip).nip.io` works for MiniShift only.
You MUST provide **own routing suffix** when deploying to OCP cluster.

## What is my ROUTING_SUFFIX?

By default `oc cluster up` command uses `nip.io` as wildcard DNS provider, so your routing suffix will be `$IP.nip.io`.
A routing suffix can be provided when a cluster starts. If you have existing routes in your OpenShift installation, you can extract ROUTING_SUFFIX.
A route has the following format: `${route}-${namespace}.${ROUTING_SUFFIX}`.
On Minishift your ROUTING_SUFFIX will be `$(minishiftp ip).nip.io`.

Here's a few simple commands to get your routing suffix:

```
oc create service clusterip test --tcp=80:80
oc expose service test
oc get route test -o=jsonpath='{.spec.host}{"\n"}'
```

Everything that comes after namespace in a route URL you have got is your routing suffix. Don't forget to delete test service and route.

## Why so many templates?

Since there are several Che flavors and configuration options, it is not possible to list all objects in one template, while duplicating templates makes it more difficult to maintain them.

## Che server logs persistence

All of the below commands create a PVC for Che server and Che deployment uses this PVC.

It is possible to avoid using PVC for Che server - just skip creating pvc `oc apply -f pvc/che-server-pvc.yaml` and `oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume` commands.
Without PVC Che deployment can use Rolling update strategy: `-p STRATEGY=Rolling` that causes zero downtime when updating Che server.
However, in this case, if you need to persist logs you need to use solutions like Kibana.

Single user Che stores H2 database in PVC.
Thus, to persist user settings, stacks, factories, workspaces and other objects, creating a PVC and mounting `/data` in Che server deployment is a must.

## Creating Workspace Objects

By default, Che SA is used to create workspace objects in the same namespace with Che server.
This is configurable - you may provide login and password or token, and set `CHE_INFRA_OPENSHIFT_PROJECT` to an empty string.

## Deploy single user Che (http)

```
oc new-project che

oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io; \
oc apply -f pvc/che-server-pvc.yaml; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume
```

## Deploy single user Che (https)

```
oc new-project che

oc apply -f pvc/che-server-pvc.yaml; \
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
		-p PROTOCOL=https \
		-p WS_PROTOCOL=wss \
		-p TLS=true; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https/che-route-tls.yaml
```

## Deploy multi user Che with bundled Keycloak and Postgres (http)

```
oc new-project che

oc new-app -f multi/postgres-template.yaml; \
oc new-app -f multi/keycloak-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io; \
oc apply -f pvc/che-server-pvc.yaml; \
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io -p CHE_MULTIUSER=true; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume
```

## Deploy multi user Che with bundled Keycloak and Postgres (https)

```
oc new-project che

oc new-app -f multi/postgres-template.yaml; \
oc new-app -f multi/keycloak-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io -p PROTOCOL=https; \
oc apply -f pvc/che-server-pvc.yaml; \
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true \
 	-p PROTOCOL=https \
	-p WS_PROTOCOL=wss \
	-p TLS=true; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```


## Deploy multi user Che with Postgres but connect to own Keycloak instance (http)

```
oc new-project che

oc new-app -f multi/postgres-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io; \
oc apply -f pvc/che-server-pvc.yaml; \
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true \
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume
```

## Deploy multi user Che alone (http)

If you already have own identity provider and a database ready to work with Che server, you may deploy Che server alone:

```
oc new-project che

oc apply -f pvc/che-server-pvc.yaml; \
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL
	-p CHE_JDBC_URL=$yourURL; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```

## Deploy multi user Che with Postgres but connect to own Keycloak instance (https)

```
oc new-project che

oc new-app -f multi/postgres-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io; \
oc apply -f pvc/che-server-pvc.yaml; \
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true \
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL \
	-p PROTOCOL=https \
	-p WS_PROTOCOL=wss \
	-p TLS=true; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```

## Deploy multi user Che alone (https)

If you already have own identity provider and a database ready to work with Che server, you may deploy Che server alone:

```
oc new-project che
oc new-app -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL
	-e CHE_JDBC_URL=$yourURL \
	-p PROTOCOL=https \
	-p WS_PROTOCOL=wss \
	-p TLS=true; \
oc apply -f https
```

## Update deployments

Updating deployments is easy using oc client:

```
oc set env dc/che KEY=VALUE
```

Some of envs that Che server respects are in che.env file, however, it references env variables that are used for Docker infra only. Usually, env names are self explaining.

Additionally, you may look at the property file and convert properties to envs using the following conversion rules:

* `.` becomes `_` (single underscore)
* `-` becomes `__` (double underscore)

So, property `che.env-name` becomes `CHE_ENV__NAME` environment variable that you can pass to Che deployment.

## Delete deployments

If you want to completely delete Che and its infrastructure components, deleting a project/namespace is the fastest way - all objects associated with this namespace will be deleted:

`oc delete project che`

If you need to delete particular deployments and associated objects, you can use selectors:

```bash
# remove all Che server related objects
oc delete all -l=app=che
# remove all Keycloak related objects
oc delete all -l=app=keycloak
# remove all Postgres related objects
oc delete all -l=app=postgres

```
PVCs, service accounts and role bindings should be deleted separately as `oc delete all` does not delete them:

```bash
# Delete Che server PVC, ServiceAccount and RoleBinding
oc delete sa -l=app=che
oc delete rolebinding -l=app=che

# Delete Keycloak and Postgres PVCs

oc delete pvc -l=app=keycloak
oc delete pvc -l=app=postgres
```

## Upgrade from http to https

IMPORTANT! Self-signed certificates aren't acceptable.

1. Update Che deployment with `PROTOCOL=https, WS_PROTOCOL=wss, TLS=true`
2. Manually edit or recreate routes for Che and Keycloak `oc apply -f https`
3. Once done, go to `https://keycloak-${NAMESPACE}.${ROUTING_SUFFIX}`, log in to admin console.
Default credentials are `admin:admin`.
Go to Clients, `che-public` client and edit **Valid Redirect URIs** and **Web Origins** URLs so that they use **https** protocol.
You do not need to do that if you initially deploy Che with https support.

## Creating workspace resources in personal OpenShift accounts

To allow creating workspace OpenShift resources in personal OpenShift accounts, you should:
- configure the Openshift identity provider in Keycloak as described in the Admin Guide
- install the Openshift console certificate in the Keycloak server (if it's self-signed) by:
    - retrieve the OpenShift console certificate into the `~/openshift.crt` file with this command:
    `minishift ssh docker exec origin /bin/cat ./openshift.local.config/master/ca.crt > ~/openshift.crt`
    - running the following command before all other commands:

```bash
    oc process -f multi/openshift-certificate-secret.yaml -p CERTIFICATE="$(cat ~/openshift.crt)" | oc apply -f -
```

- add the following parameters to the `oc new-app -f che-server-template.yaml` command:

```
-p CHE_INFRA_OPENSHIFT_PROJECT=NULL -p CHE_INFRA_OPENSHIFT_OAUTH__IDENTITY__PROVIDER=openshift-v3
```
