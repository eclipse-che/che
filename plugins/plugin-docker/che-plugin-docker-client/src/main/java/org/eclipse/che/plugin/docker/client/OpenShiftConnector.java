/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.client;

import com.google.common.collect.ImmutableMap;
import com.openshift.internal.restclient.ResourceFactory;
import com.openshift.internal.restclient.model.DeploymentConfig;
import com.openshift.internal.restclient.model.Pod;
import com.openshift.internal.restclient.model.Port;
import com.openshift.internal.restclient.model.Service;
import com.openshift.restclient.ClientBuilder;
import com.openshift.restclient.IClient;
import com.openshift.restclient.IResourceFactory;
import com.openshift.restclient.ResourceKind;
import com.openshift.restclient.images.DockerImageURI;
import com.openshift.restclient.model.IDeploymentConfig;
import com.openshift.restclient.model.IPod;
import com.openshift.restclient.model.IPort;
import com.openshift.restclient.model.IProject;
import com.openshift.restclient.model.IReplicationController;
import com.openshift.restclient.model.IService;
import com.openshift.restclient.model.IServicePort;
import com.openshift.restclient.model.deploy.DeploymentTriggerType;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.jboss.dmr.ModelNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

/**
 * Client for OpenShift API.
 *
 * @author ML
 */
@Singleton
public class OpenShiftConnector extends DockerConnector {
    private static final Logger LOG = LoggerFactory.getLogger(OpenShiftConnector.class);
    private static final String OPENSHIFT_API_VERSION = "v1";
    private static final String CHE_WORKSPACE_ID_ENV_VAR = "CHE_WORKSPACE_ID";
    private static final String CHE_OPENSHIFT_RESOURCES_PREFIX = "che-ws-";
    private static final String KUBERNETES_ANNOTATION_REGEX = "([A-Za-z0-9][-A-Za-z0-9_\\.]*)?[A-Za-z0-9]";

    /** Prefix used for che server labels */
    protected static final String CHE_SERVER_LABEL_PREFIX  = "che:server";
    /** Padding to use when converting server label to DNS name */
    protected static final String CHE_SERVER_LABEL_PADDING = "0%s0";
    /** Regex to use when matching converted labels -- should match {@link CHE_SERVER_LABEL_PADDING} */
    protected static final Pattern CHE_SERVER_LABEL_KEY    = Pattern.compile("^0(.*)0$");

    private static final String OPENSHIFT_SERVICE_TYPE_NODE_PORT = "NodePort";
    private static final int OPENSHIFT_LIVENESS_PROBE_DELAY = 120;
    private static final int OPENSHIFT_LIVENESS_PROBE_TIMEOUT = 1;
    private static final int OPENSHIFT_WAIT_POD_DELAY = 1000;
    private static final int OPENSHIFT_WAIT_POD_TIMEOUT = 120;
    private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";
    private static final String OPENSHIFT_DEPLOYMENT_CONFIG = "deploymentConfig";
    private static final String DOCKER_PREFIX = "docker://";
    public static final String DOCKER_PROTOCOL_PORT_DELIMITER = "/";
    public static final String IMAGE_PULL_POLICY_IFNOTPRESENT = "IfNotPresent";
    public static final String CHE_DEFAULT_EXTERNAL_ADDRESS = "172.17.0.1";
    public static final String CHE_SERVER_INTERNAL_IP_ADB = "172.17.0.5";
    public static final String CHE_CONTAINER_IDENTIFIER_LABEL_KEY = "cheContainerIdentifier";
    public static final String UID_ROOT = "0";
    public static final String UID_USER = "1000";
    public static final int CHE_WORKSPACE_AGENT_PORT = 4401;
    public static final int CHE_TERMINAL_AGENT_PORT = 4411;

    private final IClient            openShiftClient;
    private final IResourceFactory   openShiftFactory;
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

        this.openShiftClient = new ClientBuilder(openShiftApiEndpoint)
                .withUserName(openShiftUserName)
                .withPassword(openShiftUserPassword)
                .build();
        this.openShiftFactory = new ResourceFactory(openShiftClient);
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
        String imageName = createContainerParams.getContainerConfig().getImage();//"mariolet/che-ws-agent";//"172.30.166.244:5000/eclipse-che/che-ws-agent:latest";//
        Set<String> containerExposedPorts = createContainerParams.getContainerConfig().getExposedPorts().keySet();
        Set<String> imageExposedPorts = inspectImage(imageName).getConfig().getExposedPorts().keySet();
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.addAll(containerExposedPorts);
        exposedPorts.addAll(imageExposedPorts);

        boolean runContainerAsRoot = runContainerAsRoot(imageName);

        String[] envVariables = createContainerParams.getContainerConfig().getEnv();
        String[] volumes = createContainerParams.getContainerConfig().getHostConfig().getBinds();

        IProject cheProject = getCheProject();

        Map<String, String> additionalLabels = createContainerParams.getContainerConfig().getLabels();
        createOpenShiftService(cheProject, workspaceID, exposedPorts, additionalLabels);
        String deploymentConfigName = createOpenShiftDeploymentConfig(cheProject,
                                                                      workspaceID,
                                                                      imageName,
                                                                      containerName,
                                                                      exposedPorts,
                                                                      envVariables,
                                                                      volumes,
                                                                      runContainerAsRoot);

        String containerID = waitAndRetrieveContainerID(cheProject, deploymentConfigName);
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

        IPod pod = getChePodByContainerId(info.getId());
        String deploymentConfig = pod.getLabels().get("deploymentConfig");
        IService svc = getCheServiceBySelector("deploymentConfig", deploymentConfig);
        Map<String, String> annotations = convertKubernetesNamesToLabels(svc.getAnnotations());
        Map<String, String> containerLabels = info.getConfig().getLabels();

        Map<String, String> labels = Stream.concat(annotations.entrySet().stream(), containerLabels.entrySet().stream())
                                           .filter(e -> e.getKey().startsWith(CHE_SERVER_LABEL_PREFIX))
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

        IPod pod = getChePodByContainerId(containerId);

        String deploymentConfig = pod.getLabels().get("deploymentConfig");
        String replicationController = pod.getLabels().get("deployment");

        IDeploymentConfig dc = getCheDeploymentDescriptor(deploymentConfig);
        IReplicationController rc = getCheReplicationController(replicationController);
        IService svc = getCheServiceBySelector("deploymentConfig", deploymentConfig);

        LOG.info("Removing OpenShift Service " + svc.getName());
        openShiftClient.delete(svc);

        LOG.info("Removing OpenShift DeploymentConfiguration " + dc.getName());
        openShiftClient.delete(dc);

        LOG.info("Removing OpenShift ReplicationController " + rc.getName());
        openShiftClient.delete(rc);

        LOG.info("Removing OpenShift Pod " + pod.getName());
        openShiftClient.delete(pod);
    }

    @Override
    public void connectContainerToNetwork(ConnectContainerToNetworkParams params) throws IOException {
        // Not used in OpenShift
    }

    private IService getCheServiceBySelector(String selectorKey, String selectorValue) {
        List<IService> svcs = openShiftClient.list(ResourceKind.SERVICE, cheOpenShiftProjectName, Collections.emptyMap());
        IService svc = svcs.stream().filter(s -> s.getSelector().get(selectorKey).equals(selectorValue)).findAny().orElse(null);

        if (svc == null) {
            LOG.warn("No Service with selector " + selectorKey + "=" + selectorValue + " could be found.");
        }

        return svc;
    }

    private IReplicationController getCheReplicationController(String replicationController) throws IOException {
        IReplicationController rc = openShiftClient.get(ResourceKind.REPLICATION_CONTROLLER, replicationController, cheOpenShiftProjectName);
        if (rc == null) {
            LOG.warn("No ReplicationController with name " + replicationController + " could be found.");
        }
        return rc;
    }

    private IDeploymentConfig getCheDeploymentDescriptor(String deploymentConfig) throws IOException {
        IDeploymentConfig dc = openShiftClient.get(ResourceKind.DEPLOYMENT_CONFIG, deploymentConfig, cheOpenShiftProjectName);
        if (dc == null) {
            LOG.warn("DeploymentConfig with name " + deploymentConfig + " could be found");
        }
        return dc;
    }

    private IPod getChePodByContainerId(String containerId) throws IOException {
        Map<String, String> podLabels = new HashMap<>();
        String labelKey = CHE_CONTAINER_IDENTIFIER_LABEL_KEY;
        podLabels.put(labelKey, containerId.substring(0,12));

        List<IPod> pods = openShiftClient.list(ResourceKind.POD, cheOpenShiftProjectName, podLabels);

        if (pods.size() == 0 ) {
            LOG.error("An OpenShift Pod with label " + labelKey + "=" + containerId+" could not be found");
            throw new IOException("An OpenShift Pod with label " + labelKey + "=" + containerId+" could not be found");
        }

        if (pods.size() > 1 ) {
            LOG.error("There are " + pods.size() + " pod with label " + labelKey + "=" + containerId+" (just one was expeced)");
            throw  new  IOException("There are " + pods.size() + " pod with label " + labelKey + "=" + containerId+" (just one was expeced)");
        }

        return pods.get(0);
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

    private IProject getCheProject() throws IOException {
        List<IProject> list = openShiftClient.list(ResourceKind.PROJECT);
        IProject cheProject = list.stream()
                                   .filter(p -> p.getName().equals(cheOpenShiftProjectName))
                                   .findFirst().orElse(null);
        if (cheProject == null) {
            LOG.error("OpenShift project " + cheOpenShiftProjectName + " not found");
            throw new IOException("OpenShift project " + cheOpenShiftProjectName + " not found");
        }
        return cheProject;
    }

    private void createOpenShiftService(IProject cheProject,
                                        String workspaceID,
                                        Set<String> exposedPorts,
                                        Map<String, String> additionalLabels) {
        IService service = openShiftFactory.create(OPENSHIFT_API_VERSION, ResourceKind.SERVICE);
        ((Service) service).setNamespace(cheProject.getNamespace());
        ((Service) service).setName(CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID);
        service.setType(OPENSHIFT_SERVICE_TYPE_NODE_PORT);

        List<IServicePort> openShiftPorts = getServicePortsFrom(exposedPorts);
        service.setPorts(openShiftPorts);

        service.setSelector("deploymentConfig", (CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID));

        Map<String,String> sanitizedLabels = convertLabelsToKubernetesNames(additionalLabels);
        for (Map.Entry<String, String> entry : sanitizedLabels.entrySet()) {
            if (entry.getValue() != null) {
                service.setAnnotation(entry.getKey(), entry.getValue());
            }
        }
        openShiftClient.create(service);
        LOG.info(String.format("OpenShift service %s created", service.getName()));
    }

    private String createOpenShiftDeploymentConfig(IProject cheProject,
                                                   String workspaceID,
                                                   String imageName,
                                                   String sanitizedContaninerName,
                                                   Set<String> exposedPorts,
                                                   String[] envVariables,
                                                   String[] volumes,
                                                   boolean runContainerAsRoot) {
        String dcName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
        LOG.info(String.format("Creating OpenShift deploymentConfig %s", dcName));
        IDeploymentConfig dc = openShiftFactory.create(OPENSHIFT_API_VERSION, ResourceKind.DEPLOYMENT_CONFIG);
        ((DeploymentConfig) dc).setName(dcName);
        ((DeploymentConfig) dc).setNamespace(cheProject.getName());

        dc.setReplicas(1);
        dc.setReplicaSelector("deploymentConfig", dcName);
        dc.setServiceAccountName(this.cheOpenShiftServiceAccount);

        LOG.info(String.format("Adding container %s to OpenShift deploymentConfig %s", sanitizedContaninerName, dcName));
        addContainer(dc, workspaceID, imageName, sanitizedContaninerName, exposedPorts, envVariables, volumes, runContainerAsRoot);

        dc.addTrigger(DeploymentTriggerType.CONFIG_CHANGE);
        openShiftClient.create(dc);
        LOG.info(String.format("OpenShift deploymentConfig %s created", dcName));
        return dc.getName();
    }

    private void addContainer(IDeploymentConfig dc, String workspaceID, String imageName, String containerName, Set<String> exposedPorts, String[] envVariables, String[] volumes, boolean runContainerAsRoot) {
        Set<IPort> containerPorts = getContainerPortsFrom(exposedPorts);
        Map<String, String> containerEnv = getContainerEnvFrom(envVariables);
        dc.addContainer(containerName,
                new DockerImageURI(imageName),
                containerPorts,
                containerEnv,
                Collections.emptyList());
        dc.getContainer(containerName).setImagePullPolicy(IMAGE_PULL_POLICY_IFNOTPRESENT);

        ModelNode dcFirstContainer = ((DeploymentConfig)dc).getNode().get("spec").get("template").get("spec").get("containers").get(0);

        String UID = runContainerAsRoot ? UID_ROOT : UID_USER;

        dcFirstContainer.get("securityContext").get("privileged").set(true);
        dcFirstContainer.get("securityContext").get("runAsUser").set(UID);

        addLivenessProbe(dcFirstContainer, exposedPorts);

        // Add volumes
        for (String volume : volumes) {
            String hostPath = volume.split(":",3)[0];
            String mountPath = volume.split(":",3)[1];
            String volumName = getVolumeName(volume);
            addVolume((DeploymentConfig)dc, "ws-" + workspaceID + "-" + volumName, hostPath, mountPath);
        }
    }

    private String getVolumeName(String volume) {
        if ( volume.contains("ws-agent") ) {
            return "wsagent-lib";
        }

        if ( volume.contains("terminal") ) {
            return "terminal";
        }

        if ( volume.contains("workspaces") ) {
            return "project";
        }

        return "unknown-volume";
    }

    private void addVolume(DeploymentConfig dc, String name, String hostPath, String mountPath) {
        ModelNode dcVolume = dc.getNode().get("spec").get("template").get("spec").get("volumes").add();

        //ModelNode dcFirstVolume = dc.getNode().get("spec").get("template").get("spec").get("volumes").get(0);
        dcVolume.get("name").set(name);
        dcVolume.get("hostPath").get("path").set(hostPath);

        ModelNode dcFirstContainer = dc.getNode().get("spec").get("template").get("spec").get("containers").get(0);
        ModelNode volumeMount = dcFirstContainer.get("volumeMounts").add();
        //ModelNode firstVolumeMount = dcFirstContainer.get("volumeMounts").get(0);
        volumeMount.get("name").set(name);
        volumeMount.get("mountPath").set(mountPath);
    }

    private String waitAndRetrieveContainerID(IProject cheproject, String deploymentConfigName) {
        for (int i = 0; i < OPENSHIFT_WAIT_POD_TIMEOUT; i++) {
            try {
                Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            List<IPod> pods = openShiftClient.list(ResourceKind.POD, cheproject.getNamespace(), Collections.emptyMap());

            for (IPod p : pods) {
                String status = p.getStatus();
                String dc = p.getLabels().get(OPENSHIFT_DEPLOYMENT_CONFIG);
                if (OPENSHIFT_POD_STATUS_RUNNING.equals(status) && deploymentConfigName.equals(dc)) {
                    ModelNode containerID = ((Pod) p).getNode().get("status").get("containerStatuses").get(0).get("containerID");
                    String normalizedID = normalizeContainerID(containerID.toString());
                    p.addLabel(CHE_CONTAINER_IDENTIFIER_LABEL_KEY, getLabelFromContainerID(normalizedID));
                    openShiftClient.update(p);
                    return normalizedID;
                }
            }
        }
        return null;
    }

    public List<IServicePort> getServicePortsFrom(Set<String> exposedPorts) {
        List<IServicePort> servicePorts = new ArrayList<>();
        for (String exposedPort: exposedPorts){
            String[] portAndProtocol = exposedPort.split(DOCKER_PROTOCOL_PORT_DELIMITER,2);

            String port = portAndProtocol[0];
            String protocol = portAndProtocol[1];

            int portNumber = Integer.parseInt(port);
            String portName = isNullOrEmpty(servicePortNames.get(portNumber)) ?
                    exposedPort.replace("/","-") : servicePortNames.get(portNumber);
            int targetPortNumber = portNumber;

            IServicePort servicePort = OpenShiftPortFactory.createServicePort(
                    portName,
                    protocol,
                    portNumber,
                    targetPortNumber);
            servicePorts.add(servicePort);
        }
        return servicePorts;
    }

    public Set<IPort> getContainerPortsFrom(Set<String> exposedPorts) {
        Set<IPort> containerPorts = new HashSet<>();
        for (String exposedPort: exposedPorts){
            String[] portAndProtocol = exposedPort.split(DOCKER_PROTOCOL_PORT_DELIMITER,2);

            String port = portAndProtocol[0];
            String protocol = portAndProtocol[1].toUpperCase();

            int portNumber = Integer.parseInt(port);
            String portName = isNullOrEmpty(servicePortNames.get(portNumber)) ?
                    exposedPort.replace("/","-") : servicePortNames.get(portNumber);

            Port containerPort = new Port(new ModelNode());
            containerPort.setName(portName);
            containerPort.setProtocol(protocol);
            containerPort.setContainerPort(portNumber);
            containerPorts.add(containerPort);
        }
        return containerPorts;
    }

    public Map<String, String> getContainerEnvFrom(String[] envVariables){
        LOG.info("Container environment variables:");
        Map<String, String> env = new HashMap<>();
        for (String envVariable : envVariables) {
            String[] nameAndValue = envVariable.split("=",2);
            String varName = nameAndValue[0];
            String varValue = nameAndValue[1];
            env.put(varName, varValue);
            LOG.info("- " + varName + "=" + varValue);
        }
        return env;
    }

    private void replaceNetworkSettings(ContainerInfo info) throws IOException {

        if (info.getNetworkSettings() == null) {
            return;
        }

        IProject cheproject = getCheProject();

        IService service = getCheWorkspaceService(cheproject);
        Map<String, List<PortBinding>> networkSettingsPorts = getCheServicePorts((Service) service);

        info.getNetworkSettings().setPorts(networkSettingsPorts);
    }

    private IService getCheWorkspaceService(IProject cheproject) throws IOException {
        List<IService> services = openShiftClient.list(ResourceKind.SERVICE, cheproject.getNamespace(), Collections.emptyMap());
        // TODO: improve how the service is found (e.g. using a label with the workspaceid)
        IService service = services.stream().filter(s -> s.getName().startsWith(CHE_OPENSHIFT_RESOURCES_PREFIX)).findFirst().orElse(null);
        if (service == null) {
            LOG.error("No service with prefix " + CHE_OPENSHIFT_RESOURCES_PREFIX +" found");
            throw new IOException("No service with prefix " + CHE_OPENSHIFT_RESOURCES_PREFIX +" found");
        }
        return service;
    }

    private Map<String, List<PortBinding>> getCheServicePorts(Service service) {
        Map<String, List<PortBinding>> networkSettingsPorts = new HashMap<>();
        List<ModelNode> servicePorts = service.getNode().get("spec").get("ports").asList();
        LOG.info("Retrieving " + servicePorts.size() + " ports exposed by service " + service.getName());
        for (ModelNode servicePort : servicePorts) {
            String protocol = servicePort.get("protocol").asString();
            String targetPort = servicePort.get("targetPort").asString();
            String nodePort = servicePort.get("nodePort").asString();
            String portName = servicePort.get("name").asString();

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
     * @param container
     * @param exposedPorts
     * @see <a href=
     *      "https://docs.openshift.com/enterprise/3.0/dev_guide/application_health.html">OpenShift
     *      Application Health</a>
     *
     */
    private void addLivenessProbe(final ModelNode container, final Set<String> exposedPorts) {
        int port = 0;

        if (isDevMachine(exposedPorts)) {
            port = CHE_WORKSPACE_AGENT_PORT;
        } else if (isTerminalAgentInjected(exposedPorts)) {
            port = CHE_TERMINAL_AGENT_PORT;
        }

        if (port != 0) {
            container.get("livenessProbe").get("tcpSocket").get("port").set(port);
            container.get("livenessProbe").get("initialDelaySeconds").set(OPENSHIFT_LIVENESS_PROBE_DELAY);
            container.get("livenessProbe").get("timeoutSeconds").set(OPENSHIFT_LIVENESS_PROBE_TIMEOUT);
        }
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

    /**
     * Converts a map of labels to match Kubernetes annotation requirements. Annotations are limited
     * to alphanumeric characters, {@code '.'}, {@code '_'} and {@code '-'}, and must start and end
     * with an alphanumeric character, i.e. they must match the regex
     * {@code ([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9]}
     *
     * <p>Note that entry keys should begin with {@link OpenShiftConnector#CHE_SERVER_LABEL_PREFIX} and
     * entries should not contain {@code '.'} or {@code '_'} before conversion;
     * otherwise label will not be converted and included in output.
     *
     * <p>This implementation is relatively fragile -- changes to how Che generates labels may cause
     * this method to stop working. In general, it will only be possible to convert labels that are
     * alphanumeric plus up to 3 special characters (by converting the special characters to {@code '_'},
     * {@code '-'}, and {@code '.'} as necessary).
     *
     * @param labels Map of labels to convert
     * @return Map of labels converted to DNS Names
     * @see OpenShiftConnector#convertKubernetesNamesToLabels(Map)
     */
    protected Map<String, String> convertLabelsToKubernetesNames(Map<String, String> labels) {
        Map<String, String> names = new HashMap<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null) {
                continue;
            }
            // Check for potential problems with conversion
            if (!key.startsWith(CHE_SERVER_LABEL_PREFIX)) {
                LOG.warn("Expected CreateContainerParams label key " + entry.getKey() +
                         " to start with " + CHE_SERVER_LABEL_PREFIX);
            } else if (key.contains(".") || key.contains("_") || value.contains("_")) {
                LOG.error(String.format("Cannot convert label %s to DNS Name: "
                          + "'-' and '.' are used as escape characters", entry.toString()));
                continue;
            }

            // Convert keys: e.g. "che:server:4401/tcp:ref" -> "che.server.4401-tcp.ref"
            key = key.replaceAll(":", ".").replaceAll("/", "_");
            // Convert values: e.g. "/api" -> ".api" -- note values may include '-' e.g. "tomcat-debug"
            value = value.replaceAll("/", "_");

            // Add padding since DNS names must start and end with alphanumeric characters
            key   = String.format(CHE_SERVER_LABEL_PADDING, key);
            value = String.format(CHE_SERVER_LABEL_PADDING, value);

            if (!key.matches(KUBERNETES_ANNOTATION_REGEX) || !value.matches(KUBERNETES_ANNOTATION_REGEX)) {
                LOG.error(String.format("Could not convert label %s into Kubernetes annotation: "
                                        + "labels must be alphanumeric with ':' and '/'", entry.toString()));
                continue;
            }

            names.put(key, value);
        }
        return names;
    }

    /**
     * Undoes the label conversion done by {@link OpenShiftConnector#convertLabelsToKubernetesNames(Map)}
     *
     * @param labels Map of DNS names
     * @return Map of unconverted labels
     * @see OpenShiftConnector#convertLabelsToKubernetesNames(Map)
     */
    protected Map<String, String> convertKubernetesNamesToLabels(Map<String, String> names) {
        Map<String, String> labels = new HashMap<>();
        for (Map.Entry<String, String> entry: names.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();

            // Remove padding
            Matcher keyMatcher   = CHE_SERVER_LABEL_KEY.matcher(key);
            Matcher valueMatcher = CHE_SERVER_LABEL_KEY.matcher(value);
            if (!keyMatcher.matches() || !valueMatcher.matches()) {
                continue;
            }
            key = keyMatcher.group(1);
            value = valueMatcher.group(1);

            // Convert key: e.g. "che.server.4401_tcp.ref" -> "che:server:4401/tcp:ref"
            key = key.replaceAll("\\.", ":").replaceAll("_", "/");
            // Convert value: e.g. Convert values: e.g. "_api" -> "/api"
            value = value.replaceAll("_", "/");

            labels.put(key, value);
        }
        return labels;
    }
}
