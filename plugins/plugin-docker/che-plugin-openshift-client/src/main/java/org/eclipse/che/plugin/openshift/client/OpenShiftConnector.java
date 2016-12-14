/*******************************************************************************
 * Copyright (c) 2012-2016 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.openshift.client;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSetList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * Client for OpenShift API.
 *
 * @author ML
 */
@Singleton
public class OpenShiftConnector extends DockerConnector {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftConnector.class);
    private static final String CHE_WORKSPACE_ID_ENV_VAR = "CHE_WORKSPACE_ID";
    private static final String CHE_OPENSHIFT_RESOURCES_PREFIX = "che-ws-";
    private static final String OPENSHIFT_SERVICE_TYPE_NODE_PORT = "NodePort";
    private static final int OPENSHIFT_LIVENESS_PROBE_DELAY = 120;
    private static final int OPENSHIFT_LIVENESS_PROBE_TIMEOUT = 1;
    private static final int OPENSHIFT_WAIT_POD_DELAY = 1000;
    private static final int OPENSHIFT_WAIT_POD_TIMEOUT = 120;
    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";
    private static final String OPENSHIFT_DEPLOYMENT_LABEL = "deployment";
    private static final String DOCKER_PREFIX = "docker://";
    public static final String DOCKER_PROTOCOL_PORT_DELIMITER = "/";
    public static final String IMAGE_PULL_POLICY_IFNOTPRESENT = "IfNotPresent";
    public static final String CHE_DEFAULT_EXTERNAL_ADDRESS = "172.17.0.1";
    public static final String CHE_SERVER_INTERNAL_IP_ADB = "172.17.0.5";
    public static final String CHE_CONTAINER_IDENTIFIER_LABEL_KEY = "cheContainerIdentifier";
    public static final Long UID_ROOT = Long.valueOf(0);
    public static final Long UID_USER = Long.valueOf(1000);
    public static final int CHE_WORKSPACE_AGENT_PORT = 4401;
    public static final int CHE_TERMINAL_AGENT_PORT = 4411;

    private final OpenShiftClient    openShiftClient;
    private final KubernetesLabelConverter kubernetesLabelConverter;
    private final String             cheOpenShiftProjectName;
    private final String             cheOpenShiftServiceAccount;

    public final Map<Integer, String> servicePortNames = ImmutableMap.<Integer, String>builder().
            put(22, "sshd").
            put(CHE_WORKSPACE_AGENT_PORT, "wsagent").
            put(4403, "wsagent-jpda").
            put(CHE_TERMINAL_AGENT_PORT, "terminal").
            put(8080, "tomcat").
            put(8000, "tomcat-jpda").
            put(9876, "codeserver").build();

    @Inject
    public OpenShiftConnector(DockerConnectorConfiguration connectorConfiguration,
                              DockerConnectionFactory connectionFactory,
                              DockerRegistryAuthResolver authResolver,
                              DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider,
                              @Named("che.openshift.endpoint") String openShiftApiEndpoint,
                              @Named("che.openshift.username") String openShiftUserName,
                              @Named("che.openshift.password") String openShiftUserPassword,
                              @Named("che.openshift.project") String cheOpenShiftProjectName,
                              @Named("che.openshift.serviceaccountname") String cheOpenShiftServiceAccount) {
        super(connectorConfiguration, connectionFactory, authResolver, dockerApiVersionPathPrefixProvider);
        this.cheOpenShiftProjectName = cheOpenShiftProjectName;
        this.cheOpenShiftServiceAccount = cheOpenShiftServiceAccount;

        Config config = new ConfigBuilder().withMasterUrl(openShiftApiEndpoint)
                .withUsername(openShiftUserName)
                .withPassword(openShiftUserPassword).build();
        this.openShiftClient = new DefaultOpenShiftClient(config);
        this.kubernetesLabelConverter = new KubernetesLabelConverter();
    }

    /**
     * @param createContainerParams
     * @return
     * @throws IOException
     */
    @Override
    public ContainerCreated createContainer(CreateContainerParams createContainerParams) throws IOException {

        String containerName = getNormalizedContainerName(createContainerParams);
        String workspaceID = getCheWorkspaceId(createContainerParams);
        // Generate workspaceID if CHE_WORKSPACE_ID env var does not exist
        workspaceID = workspaceID.isEmpty() ? generateWorkspaceID() : workspaceID;
        String imageName = createContainerParams.getContainerConfig().getImage();
        Set<String> containerExposedPorts = createContainerParams.getContainerConfig().getExposedPorts().keySet();
        Set<String> imageExposedPorts = inspectImage(imageName).getConfig().getExposedPorts().keySet();
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.addAll(containerExposedPorts);
        exposedPorts.addAll(imageExposedPorts);

        boolean runContainerAsRoot = runContainerAsRoot(imageName);

        String[] envVariables = createContainerParams.getContainerConfig().getEnv();
        String[] volumes = createContainerParams.getContainerConfig().getHostConfig().getBinds();

        Map<String, String> additionalLabels = createContainerParams.getContainerConfig().getLabels();
        createOpenShiftService(workspaceID, exposedPorts, additionalLabels);
        String deploymentName = createOpenShiftDeployment(workspaceID,
                                                          imageName,
                                                          containerName,
                                                          exposedPorts,
                                                          envVariables,
                                                          volumes,
                                                          runContainerAsRoot);

        String containerID = waitAndRetrieveContainerID(deploymentName);
        if (containerID == null) {
            throw new RuntimeException("Failed to get the ID of the container running in the OpenShift pod");
        }

        return new ContainerCreated(containerID, null);
    }

    @Override
    public void startContainer(final StartContainerParams params) throws IOException {
        // Not used in OpenShift
    }

    /**
     * @param docker
     * @param container
     * @return
     * @throws IOException
     */
    @Override
    public ContainerInfo inspectContainer(String container) throws IOException {
        // Proxy to DockerConnector
        ContainerInfo info = super.inspectContainer(container);
        if (info == null) {
            return null;
        }

        Pod pod = getChePodByContainerId(info.getId());
        if (pod == null ) {
            LOG.warn("No Pod found by container ID " + info.getId());
            return null;
        }

        String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
        if (deploymentName == null ) {
            LOG.warn("No label " + OPENSHIFT_DEPLOYMENT_LABEL + " found for Pod " + pod.getMetadata().getName());
            return null;
        }

        Service svc = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
        if (svc == null) {
            LOG.warn("No Service found by selector " + OPENSHIFT_DEPLOYMENT_LABEL + "=" + deploymentName);
            return null;
        }

        Map<String, String> annotations = kubernetesLabelConverter.namesToLabels(svc.getMetadata().getAnnotations());
        Map<String, String> containerLabels = info.getConfig().getLabels();

        Map<String, String> labels = Stream.concat(annotations.entrySet().stream(), containerLabels.entrySet().stream())
                                           .filter(e -> e.getKey().startsWith(kubernetesLabelConverter.getCheServerLabelPrefix()))
                                           .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        info.getConfig().setLabels(labels);

        LOG.info("Container labels:");
        info.getConfig().getLabels().entrySet()
                        .stream().forEach(e -> LOG.info("- " + e.getKey() + "=" + e.getValue()));

        // Ignore portMapping for now: info.getNetworkSettings().setPortMapping();
        // replacePortMapping(info)
        replaceNetworkSettings(info);

        return info;
    }

    @Override
    public void removeContainer(final RemoveContainerParams params) throws IOException {
        String containerId = params.getContainer();
        boolean useForce = params.isForce();
        boolean removeVolumes = params.isRemoveVolumes();

        Pod pod = getChePodByContainerId(containerId);

        String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);

        Deployment d = getDeploymentByName(deploymentName);
        ReplicaSet rs = getReplicaSetByLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
        Service svc = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

        if (svc != null) {
            LOG.info("Removing OpenShift Service " + svc.getMetadata().getName());
            openShiftClient.resource(svc).delete();
        }

        if (d != null) {
            LOG.info("Removing OpenShift Deployment " + d.getMetadata().getName());
            openShiftClient.resource(d).delete();
        }

        if (rs!= null) {
            LOG.info("Removing Replica Set " + rs.getMetadata().getName());
            openShiftClient.resource(rs).delete();
        }

        if (pod != null) {
            LOG.info("Removing OpenShift Pod " + pod.getMetadata().getName());
            openShiftClient.resource(pod).delete();
        }
    }

    @Override
    public void connectContainerToNetwork(ConnectContainerToNetworkParams params) throws IOException {
        // Not used in OpenShift
    }

    private Service getCheServiceBySelector(String selectorKey, String selectorValue) {
        ServiceList svcs = openShiftClient.services()
                                .inNamespace(this.cheOpenShiftProjectName)
                                .list();

        Service svc = svcs.getItems().stream()
                .filter(s->s.getSpec().getSelector().containsKey(selectorKey))
                .filter(s->s.getSpec().getSelector().get(selectorKey).equals(selectorValue)).findAny().orElse(null);

        if (svc == null) {
            LOG.warn("No Service with selector " + selectorKey + "=" + selectorValue + " could be found.");
        }

        return svc;
    }

    private Deployment getDeploymentByName(String deploymentName) throws IOException {
        Deployment dc = openShiftClient
                            .extensions().deployments()
                            .inNamespace(this.cheOpenShiftProjectName)
                            .withName(deploymentName)
                            .get();
        if (dc == null) {
            LOG.warn("No Deployment with name " + deploymentName + " could be found");
        }
        return dc;
    }

    private ReplicaSet getReplicaSetByLabel(String labelKey, String labelValue) throws IOException {
        ReplicaSetList rs = openShiftClient
                .extensions().replicaSets()
                .inNamespace(this.cheOpenShiftProjectName)
                .withLabel(labelKey, labelValue)
                .list();

        if (rs.getItems().size() == 0) {
            LOG.warn("No ReplicaSet with label " + labelKey + "=" + labelValue + " could be found");
            throw new IOException("No ReplicaSet with label " + labelKey + "=" + labelValue + " could be found");
        }

        if (rs.getItems().size() > 1) {
            LOG.warn("Found more than one ReplicaSet with label " + labelKey + "=" + labelValue);
            throw new IOException("Found more than one ReplicaSet with label " + labelValue + "=" + labelValue);
        }

        return rs.getItems().get(0);
    }
    private Pod getChePodByContainerId(String containerId) throws IOException {
        Map<String, String> podLabels = new HashMap<>();
        String labelKey = CHE_CONTAINER_IDENTIFIER_LABEL_KEY;
        podLabels.put(labelKey, containerId.substring(0,12));

        PodList pods = openShiftClient.pods()
                                    .inNamespace(this.cheOpenShiftProjectName)
                                    .withLabel(CHE_CONTAINER_IDENTIFIER_LABEL_KEY,containerId.substring(0,12))
                                    .list();

        if (pods.getItems().size() == 0) {
            LOG.error("An OpenShift Pod with label " + labelKey + "=" + containerId+" could not be found");
            throw new IOException("An OpenShift Pod with label " + labelKey + "=" + containerId+" could not be found");
        }

        if (pods.getItems().size() > 1) {
            LOG.error("There are " + pods.getItems().size() + " pod with label " + labelKey + "=" + containerId+" (just one was expeced)");
            throw  new  IOException("There are " + pods.getItems().size() + " pod with label " + labelKey + "=" + containerId+" (just one was expeced)");
        }

        return pods.getItems().get(0);
    }

    private String getNormalizedContainerName(CreateContainerParams createContainerParams) {
        String containerName = createContainerParams.getContainerName();
        // The name of a container in Kubernetes should be a
        // valid hostname as specified by RFC 1123 (i.e. max length
        // of 63 chars and no underscores)
        return containerName.substring(9).replace('_', '-');
    }

    protected String getCheWorkspaceId(CreateContainerParams createContainerParams) {
        Stream<String> env = Arrays.stream(createContainerParams.getContainerConfig().getEnv());
        String workspaceID = env.filter(v -> v.startsWith(CHE_WORKSPACE_ID_ENV_VAR) && v.contains("=")).
                                 map(v -> v.split("=",2)[1]).
                                 findFirst().
                                 orElse("");
        return workspaceID.replaceFirst("workspace","");
    }

    private void createOpenShiftService(String workspaceID,
                                        Set<String> exposedPorts,
										Map<String, String> additionalLabels) {

        Map<String, String> selector = new HashMap<>();
        selector.put(OPENSHIFT_DEPLOYMENT_LABEL, CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID);

        List<ServicePort> ports = getServicePortsFrom(exposedPorts);

        Service service = openShiftClient
                .services()
                .inNamespace(this.cheOpenShiftProjectName)
                .createNew()
                .withNewMetadata()
                    .withName(CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID)
                    .withAnnotations(kubernetesLabelConverter.labelsToNames(additionalLabels))
                .endMetadata()
                .withNewSpec()
                    .withType(OPENSHIFT_SERVICE_TYPE_NODE_PORT)
                    .withSelector(selector)
                    .withPorts(ports)
                .endSpec()
                .done();

        LOG.info(String.format("OpenShift service %s created", service.getMetadata().getName()));
    }

    private String createOpenShiftDeployment(String workspaceID,
                                             String imageName,
                                             String sanitizedContainerName,
                                             Set<String> exposedPorts,
                                             String[] envVariables,
                                             String[] volumes,
                                             boolean runContainerAsRoot) {

        String dName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
        LOG.info(String.format("Creating OpenShift deployment %s", dName));

        Map<String, String> selector = new HashMap<>();
        selector.put(OPENSHIFT_DEPLOYMENT_LABEL, CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID);

        LOG.info(String.format("Adding container %s to OpenShift deployment %s", sanitizedContainerName, dName));
        Long UID = runContainerAsRoot ? UID_ROOT : UID_USER;
        Container container = new ContainerBuilder()
                                    .withName(sanitizedContainerName)
                                    .withImage(imageName)
                                    .withEnv(getContainerEnvFrom(envVariables))
                                    .withPorts(getContainerPortsFrom(exposedPorts))
                                    .withImagePullPolicy(IMAGE_PULL_POLICY_IFNOTPRESENT)
                                    .withNewSecurityContext()
                                        .withRunAsUser(UID)
                                        .withPrivileged(true)
                                    .endSecurityContext()
                                    .withLivenessProbe(getLivenessProbeFrom(exposedPorts))
                                    .withVolumeMounts(getVolumeMountsFrom(volumes,workspaceID))
                                    .build();


        PodSpec podSpec = new PodSpecBuilder()
                                 .withContainers(container)
                                 .withVolumes(getVolumesFrom(volumes, workspaceID))
                                 .withServiceAccountName(this.cheOpenShiftServiceAccount)
                                 .build();


        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(dName)
                    .withNamespace(this.cheOpenShiftProjectName)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    //.withTrigger(DeploymentTriggerType.CONFIG_CHANGE) ?
                    .withNewSelector()
                        .withMatchLabels(selector)
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(selector)
                        .endMetadata()
                        .withSpec(podSpec)
                    .endTemplate()
                .endSpec()
                .build();

        deployment = openShiftClient.extensions()
                                    .deployments()
                                    .inNamespace(this.cheOpenShiftProjectName)
                                    .create(deployment);

        LOG.info(String.format("OpenShift deployment %s created", dName));
        return deployment.getMetadata().getName();
    }

    private List<VolumeMount> getVolumeMountsFrom(String[] volumes, String workspaceID) {

        List<VolumeMount> vms = new ArrayList<>();

        for (String volume : volumes) {
            String mountPath = volume.split(":",3)[1];
            String volumeName = getVolumeName(volume);

            VolumeMount vm = new VolumeMountBuilder()
                    .withMountPath(mountPath)
                    .withName("ws-" + workspaceID + "-" + volumeName)
                    .build();
            vms.add(vm);
        }

        return vms;
    }


    private List<Volume> getVolumesFrom(String[] volumes, String workspaceID) {

        List<Volume> vs = new ArrayList<>();

        for (String volume : volumes) {
            String hostPath = volume.split(":",3)[0];
            String volumeName = getVolumeName(volume);

            Volume v = new VolumeBuilder()
                    .withNewHostPath(hostPath)
                    .withName("ws-" + workspaceID + "-" + volumeName)
                    .build();
            vs.add(v);
        }

        return vs;
    }


    private String getVolumeName(String volume) {
        if (volume.contains("ws-agent")) {
            return "wsagent-lib";
        }

        if (volume.contains("terminal")) {
            return "terminal";
        }

        if (volume.contains("workspaces")) {
            return "project";
        }

        return "unknown-volume";
    }


    private String waitAndRetrieveContainerID(String deploymentName) {
        for (int i = 0; i < OPENSHIFT_WAIT_POD_TIMEOUT; i++) {
            try {
                Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            PodList pods = openShiftClient.pods()
                                .inNamespace(this.cheOpenShiftProjectName)
                                .list();

            for (Pod p : pods.getItems()) {
                String status = p.getStatus().getPhase();
                String dc = p.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
                if (OPENSHIFT_POD_STATUS_RUNNING.equals(status) && deploymentName.equals(dc)) {
                    String containerID = p.getStatus().getContainerStatuses().get(0).getContainerID();
                    String normalizedID = normalizeContainerID(containerID);
                    openShiftClient.pods()
                            .inNamespace(this.cheOpenShiftProjectName)
                            .withName(p.getMetadata().getName())
                            .edit()
                            .editMetadata()
                                .addToLabels(CHE_CONTAINER_IDENTIFIER_LABEL_KEY, getLabelFromContainerID(normalizedID))
                            .endMetadata()
                            .done();
                    return normalizedID;
                }
            }
        }
        return null;
    }

    public List<ServicePort> getServicePortsFrom(Set<String> exposedPorts) {
        List<ServicePort> servicePorts = new ArrayList<>();
        for (String exposedPort: exposedPorts){
            String[] portAndProtocol = exposedPort.split(DOCKER_PROTOCOL_PORT_DELIMITER,2);

            String port = portAndProtocol[0];
            String protocol = portAndProtocol[1];

            int portNumber = Integer.parseInt(port);
            String portName = isNullOrEmpty(servicePortNames.get(portNumber)) ?
                    exposedPort.replace("/","-") : servicePortNames.get(portNumber);
            int targetPortNumber = portNumber;

            ServicePort servicePort = new ServicePort();
            servicePort.setName(portName);
            servicePort.setProtocol(protocol);
            servicePort.setPort(portNumber);
            servicePort.setTargetPort(new IntOrString(targetPortNumber));
            servicePorts.add(servicePort);
        }
        return servicePorts;
    }

    public List<ContainerPort> getContainerPortsFrom(Set<String> exposedPorts) {
        List<ContainerPort> containerPorts = new ArrayList<>();
        for (String exposedPort: exposedPorts){
            String[] portAndProtocol = exposedPort.split(DOCKER_PROTOCOL_PORT_DELIMITER,2);

            String port = portAndProtocol[0];
            String protocol = portAndProtocol[1].toUpperCase();

            int portNumber = Integer.parseInt(port);
            String portName = isNullOrEmpty(servicePortNames.get(portNumber)) ?
                    exposedPort.replace("/","-") : servicePortNames.get(portNumber);

            ContainerPort containerPort = new ContainerPortBuilder()
                                                .withName(portName)
                                                .withProtocol(protocol)
                                                .withContainerPort(portNumber)
                                                .build();
            containerPorts.add(containerPort);
        }
        return containerPorts;
    }

    public List<EnvVar> getContainerEnvFrom(String[] envVariables){
        LOG.info("Container environment variables:");
        List<EnvVar> env = new ArrayList<>();
        for (String envVariable : envVariables) {
            String[] nameAndValue = envVariable.split("=",2);
            String varName = nameAndValue[0];
            String varValue = nameAndValue[1];
            EnvVar envVar = new EnvVarBuilder()
                    .withName(varName)
                    .withValue(varValue)
                    .build();
            env.add(envVar);
            LOG.info("- " + varName + "=" + varValue);
        }
        return env;
    }

    private void replaceNetworkSettings(ContainerInfo info) throws IOException {

        if (info.getNetworkSettings() == null) {
            return;
        }

        Service service = getCheWorkspaceService();
        Map<String, List<PortBinding>> networkSettingsPorts = getCheServicePorts(service);

        info.getNetworkSettings().setPorts(networkSettingsPorts);
    }

    private Service getCheWorkspaceService() throws IOException {
        ServiceList services = openShiftClient.services().inNamespace(this.cheOpenShiftProjectName).list();
        // TODO: improve how the service is found (e.g. using a label with the workspaceid)
        Service service = services.getItems().stream()
                .filter(s -> s.getMetadata().getName().startsWith(CHE_OPENSHIFT_RESOURCES_PREFIX))
                .findFirst().orElse(null);

        if (service == null) {
            LOG.error("No service with prefix " + CHE_OPENSHIFT_RESOURCES_PREFIX +" found");
            throw new IOException("No service with prefix " + CHE_OPENSHIFT_RESOURCES_PREFIX +" found");
        }

        return service;
    }

    private Map<String, List<PortBinding>> getCheServicePorts(Service service) {
        Map<String, List<PortBinding>> networkSettingsPorts = new HashMap<>();
        List<ServicePort> servicePorts = service.getSpec().getPorts();
        LOG.info("Retrieving " + servicePorts.size() + " ports exposed by service " + service.getMetadata().getName());
        for (ServicePort servicePort : servicePorts) {
            String protocol = servicePort.getProtocol();
            String targetPort = String.valueOf(servicePort.getTargetPort().getIntVal());
            String nodePort = String.valueOf(servicePort.getNodePort());
            String portName = servicePort.getName();

            LOG.info("Port: " + targetPort + DOCKER_PROTOCOL_PORT_DELIMITER + protocol + " (" + portName + ")");

            networkSettingsPorts.put(targetPort + DOCKER_PROTOCOL_PORT_DELIMITER + protocol.toLowerCase(),
                    Collections.singletonList(new PortBinding().withHostIp(CHE_DEFAULT_EXTERNAL_ADDRESS).withHostPort(nodePort)));
        }
        return networkSettingsPorts;
    }

    /**
     * Adds OpenShift liveness probe to the container. Liveness probe is configured
     * via TCP Socket Check - for dev machines by checking Workspace API agent port
     * (4401), for non-dev by checking Terminal port (4411)
     *
     * @param exposedPorts
     * @see <a href=
     *      "https://docs.openshift.com/enterprise/3.0/dev_guide/application_health.html">OpenShift
     *      Application Health</a>
     *
     */
    private Probe getLivenessProbeFrom(final Set<String> exposedPorts) {
        int port = 0;

        if (isDevMachine(exposedPorts)) {
            port = CHE_WORKSPACE_AGENT_PORT;
        } else if (isTerminalAgentInjected(exposedPorts)) {
            port = CHE_TERMINAL_AGENT_PORT;
        }

        if (port != 0) {
            return new ProbeBuilder()
                            .withNewTcpSocket()
                            .withNewPort(port)
                            .endTcpSocket()
                            .withInitialDelaySeconds(OPENSHIFT_LIVENESS_PROBE_DELAY)
                            .withTimeoutSeconds(OPENSHIFT_LIVENESS_PROBE_TIMEOUT)
                            .build();
        }

        return null;
    }

    /**
     * When container is expected to be run as root, user field from {@link ImageConfig} is empty.
     * For non-root user it contains "user" value
     *
     * @param dockerConnector
     * @param imageName
     * @return true if user property from Image config is empty string, false otherwise
     * @throws IOException
     */
    private boolean runContainerAsRoot(final String imageName) throws IOException {
        String user = inspectImage(imageName).getConfig().getUser();
        return user != null && user.isEmpty();
    }

    /**
     * @param exposedPorts
     * @return true if machine exposes 4411/tcp port used by Terminal agent,
     * false otherwise
     */
    private boolean isTerminalAgentInjected(final Set<String> exposedPorts) {
        return exposedPorts.contains(CHE_TERMINAL_AGENT_PORT + "/tcp");
    }

    /**
     * @param exposedPorts
     * @return true if machine exposes 4401/tcp port used by Worspace API agent,
     * false otherwise
     */
    private boolean isDevMachine(final Set<String> exposedPorts) {
        return exposedPorts.contains(CHE_WORKSPACE_AGENT_PORT + "/tcp");
    }

    /**
     * Che workspace id is used as OpenShift service / deployment config name
     * and must match the regex [a-z]([-a-z0-9]*[a-z0-9]) e.g. "q5iuhkwjvw1w9emg"
     *
     * @return randomly generated workspace id
     */
    private String generateWorkspaceID() {
        return RandomStringUtils.random(16, true, true).toLowerCase();
    }

    /**
     * @param containerID
     * @return label based on 'ContainerID' (first 12 chars of ID)
     */
    private String getLabelFromContainerID(final String containerID) {
        return StringUtils.substring(containerID, 0, 12);
    }

    /**
     * @param containerID
     * @return normalized version of 'ContainerID' without 'docker://' prefix and double quotes
     */
    private String normalizeContainerID(final String containerID) {
        return StringUtils.replaceOnce(containerID, DOCKER_PREFIX, "").replace("\"", "");
    }
}
