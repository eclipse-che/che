# Dashboard Configurability Proposal

## Motivation

Dashboard has [product.json](https://github.com/eclipse-che/che-dashboard#branding) configuration file which can be overridden on container build phase and which would some dashboard appearance stuff like Product Title, Product icon.

But apart from that there is some settings which an Admin would like to override on the CheCluster level without rebuilding container, like warning.

The potential user of this would be at least Hosted Che, which sometime needs to inform users about upcoming upgrade.

## Alternatives

There are multiple options to achieve the same goals:

### JSON vs YAML

According to the latest trends, every configuration file is in yaml format, since it's easier to read and write for admins. It may be a good time to move from JSON to YAML format. It may makes sense to do backward compatible solution where we support both (old JSON with new YAML) but maybe it's good enough to break the stuff and request dependencies to adapt to a new way.

### CheCluster vs ConfigMap

We're able to define it on CheCluster level in two way:

- define detailed model on CheCluster CRD level which would be backward compatible in terms of Kubernetes API and easier to discover/configure. But it would require changes on Che Operator side each time when Dashboard configuration is changed:

```yaml
spec:
  dashboard:
    productTitle: "Eclipse Che powered by ME"
    applications:
      - group: "Apps"
        title: "My App"
        url: "https://example-app.com
```

- define the ability to define inclined yaml file content on CheCluster CR level. It would not be 100% safe to use in terms of backward compatibility and letting users know about their mistakes, but it would allows define once on Che Operator side, expose link on documentation and then fully support on Dashboard/docs sides:

```yaml
spec:
  dashboard:
    config: |
        productTitle: "Eclipse Che powered by ME"
        applications:
        - group: "Apps"
            title: "My App"
            url: "https://example-app.com
```

- define an ability to configure dashboard with help of standalone configmap mounted with Che Operator mechanism:

```yaml
CHE_NAMESPACE="eclipse-che"
cat <<EOF | kubectl apply -f -
kind: ConfigMap
apiVersion: v1
metadata:
  name: che-dashboard-custom-config
  namespace: eclipse-che
  labels:
    app.kubernetes.io/component: che-dashboard-configmap
    app.kubernetes.io/part-of: che.eclipse.org
  annotations:
    che.eclipse.org/mount-as: file
    che.eclipse.org/mount-path: /config/product.yaml
data:
  config: |
    productTitle: "Eclipse Che powered by ME"
    applications:
    - group: "Apps"
        title: "My App"
        url: "https://example-app.com
EOF
```

TODOs:

- clearly describe pros and cons each of the way;
