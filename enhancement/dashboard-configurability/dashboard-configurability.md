# Dashboard Configurability Proposal

## Motivation

Dashboard has [product.json](https://github.com/eclipse-che/che-dashboard#branding) configuration file which can be overridden on container build phase and which would override some dashboard appearance stuff like Product Title, Product icon.

In addition from that, there are some settings which an Admin would like to override on the CheCluster level without rebuilding container, like warning.

The potential user of this would be at least Hosted Che, which sometime needs to inform users about upcoming upgrade.

## Alternatives

There are multiple options to achieve the same goals:

### JSON vs YAML

According to the latest trends, every configuration file is in yaml format, since it's easier to read and write for admins. It may be a good time to move from JSON to YAML format. It may makes sense to do backward compatible solution where we support both (old JSON with new YAML) but maybe it's good enough to break the stuff and request dependencies to adapt to a new way.

### CheCluster vs ConfigMap

#### 1. Part of CheCluster on model level

Define detailed model on CheCluster CRD level:

```yaml
spec:
  dashboard:
    productTitle: "Eclipse Che powered by ME"
    applications:
      - group: "Apps"
        title: "My App"
        url: "https://example-app.com
```

**Pros:**

- self-documenting on CheCluster CRD level;
- backward compatible in terms of K8s API;
- admin configuration is validated by K8s API;

**Cons:**

- each dashboard configuration change would require changes and build on CheOperator side;
- controversial: CheCluster CRD grows with component specific configuration;

#### 2. Part of CheCluster as field with inlined content

Define the ability to define inclined yaml file content on CheCluster CR level. 

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

**Pros:**

- is defined in Che Operator side once;
- controversial: CheCluster is kept for more deployment configuration than component-specific behavior;

**Cons:**

- ideally, this way needs a separate versioned piece of documentation;
- backward compatibility and content validation is done later on dashboard component side and it may be late lifecycle phase to report that;

#### 3. Separated ConfigMap

Define an ability to configure dashboard with help of standalone configmap mounted with Che Operator mechanism:

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

**Pros:**

- that's the cheapest way to get it working since nothing should be done on Dashboard side if we go with JSON, or only YAML to JSON conversion should be done;

**Cons:**

- not easy to discover such an ability;
- the format is described separately from configuration;
- no validation available;
