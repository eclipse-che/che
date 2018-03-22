## Context

All commands are executed in the directory where this README is located.

## Templates and Parameters

Templates are provided with a set of predefined params which you can override with with `-p key=value`. If you miss envs and parameters, you can add them to your template.

IMPORTANT! **ROUTING_SUFFIX** value in the below commands `$(minishift ip).nip.io` works for MiniShift only. You MUST provide **own routing suffix** when deploying to OCP cluster.

## Why many templates?

Since there are several Che flavors and configuration options, it is not possible to list all objects in one template, while duplicating templates makes it more difficult to maintain them.

## Che server logs persistence

All of the below commands create a PVC for Che server and Che deployment uses this PVC.

It is possible to avoid using PVC for Che server - just skip creating pvc `oc apply -f pvc/che-server-pvc.yaml` and `oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume` commands.
Without PVC Che deployment can use Rolling update strategy: `-p STRATEGY=Rolling` that causes zero downtime when updating Che server.
However, in this case, if you need to persist logs you need to use solutions like Kibana.

## Creating Workspace Objects

By default, che SA is used to create workspace objects in the same namespace with Che server. This is configurable - you may provide login and password or token, and set

## Upgrade from http to https

1. Update Che deployment with `PROTOCOL=https, WS_PROTOCOL=wss, TLS=true`
2. Manually edit or recreate routes for Che and Keycloak `oc apply -f https`
3. Once done, go to `https://keycloak.${NAMESPACE}.${ROUTING_SUFFIX}`, log in to admin console.
Default credentials are `admin:admin`.
Go to Clients, `che-public` client and edit **Valid Redirect URIs** and **Web Origins** URLs so that they use **https** protocol.
You do not need to do that if you initially deploy Che with 	https support.

## Deploy single user Che (http)

```
oc new-project che

oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io | oc apply -f -; \
oc apply -f pvc/che-server-pvc.yaml; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume
```

## Deploy single user Che (https)

```
oc new-project che

oc apply -f pvc/che-server-pvc.yaml; \
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
		-p PROTOCOL=https \
		-p WS_PROTOCOL=wss \
		-p TLS=true \
		| oc apply -f -; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```

## Deploy multi user Che with bundled Keycloak and Postgres (http)

```
oc new-project che

oc process -f multi/postgres-template.yaml | oc apply -f -; \
oc process -f multi/keycloak-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io | oc apply -f -; \
oc apply -f pvc/che-server-pvc.yaml; \
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io -p CHE_MULTIUSER=true | oc apply -f -; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume
```

## Deploy multi user Che with bundled Keycloak and Postgres (https)

```
oc process -f multi/postgres-template.yaml | oc apply -f -; \
oc process -f multi/keycloak-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
																					 -p PROTOCOL=https \
																					 | oc apply -f -; \
oc apply -f pvc/che-server-pvc.yaml; \
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true \
 	-p PROTOCOL=https \
	-p WS_PROTOCOL=wss \
	-p TLS=true  \
	| oc apply -f -; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```


## Deploy multi user Che with Postgres but connect to own Keycloak instance (http)

```
oc new-project che

oc process -f multi/postgres-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io | oc apply -f -; \
oc apply -f pvc/che-server-pvc.yaml; \
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true \
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL \
	| oc apply -f -; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume
```

## Deploy multi user Che alone (http)

If you already have own identity provider and a database ready to work with Che server, you may deploy Che server alone:

```
oc new-project che

oc apply -f pvc/che-server-pvc.yaml; \
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL
	-p CHE_JDBC_URL=$yourURL \
	| oc apply -f -; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```

## Deploy multi user Che with Postgres but connect to own Keycloak instance (https)

```
oc new-project che

oc process -f multi/postgres-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io | oc apply -f -; \
oc apply -f pvc/che-server-pvc.yaml; \
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true \
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL \
	-p PROTOCOL=https \
	-p WS_PROTOCOL=wss \
	-p TLS=true \
	| oc apply -f -; \
oc set volume dc/che --add -m /data --name=che-data-volume --claim-name=che-data-volume; \
oc apply -f https
```

## Deploy multi user Che alone (https)

If you already have own identity provider and a database ready to work with Che server, you may deploy Che server alone:

```
oc process -f che-server-template.yaml -p ROUTING_SUFFIX=$(minishift ip).nip.io \
	-p CHE_MULTIUSER=true
	-p CHE_KEYCLOAK_AUTH__SERVER__URL=$yourURL
	-p CHE_JDBC_URL=$yourURL \
	-p PROTOCOL=https \
	-p WS_PROTOCOL=wss \
	-p TLS=true \
	| oc apply -f -; \
oc apply -f https
```
