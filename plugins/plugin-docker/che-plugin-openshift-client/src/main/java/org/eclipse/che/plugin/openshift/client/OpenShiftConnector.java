/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.openshift.client;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerStateRunning;
import io.fabric8.kubernetes.api.model.ContainerStateTerminated;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.DoneableEndpoints;
import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeSystemInfo;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.ProbeBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.utils.InputStreamPumper;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.Image;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.api.model.RouteSpec;
import io.fabric8.openshift.api.model.RouteTargetReference;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.server.WorkspaceSubjectRegistry;
import org.eclipse.che.api.workspace.server.event.ServerIdleEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.WorkspacesRoutingSuffixProvider;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.json.ContainerProcesses;
import org.eclipse.che.plugin.docker.client.json.ContainerState;
import org.eclipse.che.plugin.docker.client.json.Event;
import org.eclipse.che.plugin.docker.client.json.Filters;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkCreated;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;
import org.eclipse.che.plugin.docker.client.json.Version;
import org.eclipse.che.plugin.docker.client.json.network.ContainerInNetwork;
import org.eclipse.che.plugin.docker.client.json.network.EndpointConfig;
import org.eclipse.che.plugin.docker.client.json.network.Ipam;
import org.eclipse.che.plugin.docker.client.json.network.IpamConfig;
import org.eclipse.che.plugin.docker.client.json.network.Network;
import org.eclipse.che.plugin.docker.client.params.CommitParams;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.CreateExecParams;
import org.eclipse.che.plugin.docker.client.params.GetContainerLogsParams;
import org.eclipse.che.plugin.docker.client.params.GetEventsParams;
import org.eclipse.che.plugin.docker.client.params.GetResourceParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.client.params.KillContainerParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.PutResourceParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveImageParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.TopParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.DisconnectContainerFromNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.GetNetworksParams;
import org.eclipse.che.plugin.docker.client.params.network.InspectNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.RemoveNetworkParams;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesContainer;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesEnvVar;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesExecHolder;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesLabelConverter;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesOutputAdapter;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesService;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client for OpenShift API.
 *
 * @author Mario Loriedo (mloriedo@redhat.com)
 * @author Angel Misevski (amisevsk@redhat.com)
 * @author Ilya Buziuk (ibuziuk@redhat.com)
 */
@Singleton
public class OpenShiftConnector extends DockerConnector {
  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftConnector.class);
  public static final String CHE_OPENSHIFT_RESOURCES_PREFIX = "che-ws-";
  public static final String OPENSHIFT_DEPLOYMENT_LABEL = "deployment";
  public static final String CHE_MOUNTED_WORKSPACE_FOLDER = "/workspace-logs";
  public static final String WORKSPACE_LOGS_FOLDER_SUFFIX = "-logs";

  private static final String CHE_CONTAINER_IDENTIFIER_LABEL_KEY = "cheContainerIdentifier";
  private static final String CHE_DEFAULT_EXTERNAL_ADDRESS = "172.17.0.1";
  private static final String CHE_WORKSPACE_ID_ENV_VAR = "CHE_WORKSPACE_ID";
  private static final String CHE_IS_DEV_MACHINE_ENV_VAR = "CHE_IS_DEV_MACHINE";
  private static final int CHE_WORKSPACE_AGENT_PORT = 4401;
  private static final int CHE_TERMINAL_AGENT_PORT = 4411;
  private static final String DOCKER_PROTOCOL_PORT_DELIMITER = "/";
  private static final int OPENSHIFT_WAIT_POD_TIMEOUT = 240;
  private static final int OPENSHIFT_WAIT_POD_DELAY = 1000;
  private static final int OPENSHIFT_IMAGESTREAM_WAIT_DELAY = 2000;
  private static final int OPENSHIFT_IMAGESTREAM_MAX_WAIT_COUNT = 30;
  private static final long OPENSHIFT_POD_TERMINATION_GRACE_PERIOD = 0;

  private static final String OPENSHIFT_POD_STATUS_RUNNING = "Running";
  private static final String OPENSHIFT_VOLUME_STORAGE_CLASS =
      "volume.beta.kubernetes.io/storage-class";
  private static final String OPENSHIFT_VOLUME_STORAGE_CLASS_NAME = "che-workspace";
  private static final String OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT = "IfNotPresent";

  private static final String IDLING_ALPHA_OPENSHIFT_IO_IDLED_AT =
      "idling.alpha.openshift.io/idled-at";
  private static final String IDLING_ALPHA_OPENSHIFT_IO_PREVIOUS_SCALE =
      "idling.alpha.openshift.io/previous-scale";
  private static final String OPENSHIFT_CHE_SERVER_DEPLOYMENT_NAME = "che";
  private static final String OPENSHIFT_CHE_SERVER_SERVICE_NAME = "che-host";
  private static final String IDLING_ALPHA_OPENSHIFT_IO_UNIDLE_TARGETS =
      "idling.alpha.openshift.io/unidle-targets";
  private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";

  /** Regexp to extract port (under the form 22/tcp or 4401/tcp, etc.) from label references */
  public static final String LABEL_CHE_SERVER_REF_KEY = "^che:server:(.*):ref$";

  private static final String PS_COMMAND = "ps";
  private static final String TOP_REGEX_PATTERN = " +";

  private Map<String, KubernetesExecHolder> execMap = new HashMap<>();

  private final String openShiftCheProjectName;
  private final int openShiftLivenessProbeDelay;
  private final int openShiftLivenessProbeTimeout;
  private final String workspacesPersistentVolumeClaim;
  private final String workspacesPvcQuantity;
  private final String cheWorkspaceStorage;
  private final String cheWorkspaceProjectsStorage;
  private final String cheServerExternalAddress;
  private final String cheWorkspaceMemoryLimit;
  private final String cheWorkspaceMemoryRequest;
  private final boolean secureRoutes;
  private final boolean createWorkspaceDirs;
  private final OpenShiftPvcHelper openShiftPvcHelper;
  private final OpenShiftRouteCreator openShiftRouteCreator;
  private final OpenShiftDeploymentCleaner openShiftDeploymentCleaner;
  private final WorkspacesRoutingSuffixProvider cheWorkspacesRoutingSuffixProvider;
  private final OpenshiftWorkspaceEnvironmentProvider openshiftWorkspaceEnvironmentProvider;
  private String apiEndpoint;
  private Boolean apiEndpointRetrieved = false;
  private final WorkspaceSubjectRegistry workspaceSubjectRegistry;

  private Map<String, String> containerIdToWorkspaceId = new HashMap<>();
  private Map<String, String> imageIdToWorkspaceId = new HashMap<>();

  @Inject private OpenShiftClientFactory ocFactory;

  @Inject
  public OpenShiftConnector(
      OpenshiftWorkspaceEnvironmentProvider openshiftUserAccountProvider,
      DockerConnectorConfiguration connectorConfiguration,
      DockerConnectionFactory connectionFactory,
      DockerRegistryAuthResolver authResolver,
      DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider,
      OpenShiftPvcHelper openShiftPvcHelper,
      OpenShiftRouteCreator openShiftRouteCreator,
      OpenShiftDeploymentCleaner openShiftDeploymentCleaner,
      EventService eventService,
      WorkspaceSubjectRegistry workspaceSubjectRegistry,
      @Nullable @Named("che.docker.ip.external") String cheServerExternalAddress,
      WorkspacesRoutingSuffixProvider cheWorkspacesRoutingSuffixProvider,
      @Named("che.openshift.project") String openShiftCheProjectName,
      @Named("che.openshift.liveness.probe.delay") int openShiftLivenessProbeDelay,
      @Named("che.openshift.liveness.probe.timeout") int openShiftLivenessProbeTimeout,
      @Named("che.openshift.workspaces.pvc.name") String workspacesPersistentVolumeClaim,
      @Named("che.openshift.workspaces.pvc.quantity") String workspacesPvcQuantity,
      @Named("che.workspace.storage") String cheWorkspaceStorage,
      @Named("che.workspace.projects.storage") String cheWorkspaceProjectsStorage,
      @Nullable @Named("che.openshift.workspace.memory.request") String cheWorkspaceMemoryRequest,
      @Nullable @Named("che.openshift.workspace.memory.override") String cheWorkspaceMemoryLimit,
      @Named("che.openshift.secure.routes") boolean secureRoutes,
      @Named("che.openshift.precreate.workspace.dirs") boolean createWorkspaceDirs) {

    super(
        connectorConfiguration,
        connectionFactory,
        authResolver,
        dockerApiVersionPathPrefixProvider);
    this.openshiftWorkspaceEnvironmentProvider = openshiftUserAccountProvider;
    this.cheServerExternalAddress = cheServerExternalAddress;
    this.cheWorkspacesRoutingSuffixProvider = cheWorkspacesRoutingSuffixProvider;
    this.openShiftCheProjectName = openShiftCheProjectName;
    this.openShiftLivenessProbeDelay = openShiftLivenessProbeDelay;
    this.openShiftLivenessProbeTimeout = openShiftLivenessProbeTimeout;
    this.workspacesPersistentVolumeClaim = workspacesPersistentVolumeClaim;
    this.workspacesPvcQuantity = workspacesPvcQuantity;
    this.cheWorkspaceStorage = cheWorkspaceStorage;
    this.cheWorkspaceProjectsStorage = cheWorkspaceProjectsStorage;
    this.cheWorkspaceMemoryRequest = cheWorkspaceMemoryRequest;
    this.cheWorkspaceMemoryLimit = cheWorkspaceMemoryLimit;
    this.secureRoutes = secureRoutes;
    this.createWorkspaceDirs = createWorkspaceDirs;
    this.openShiftPvcHelper = openShiftPvcHelper;
    this.openShiftRouteCreator = openShiftRouteCreator;
    this.openShiftDeploymentCleaner = openShiftDeploymentCleaner;
    this.workspaceSubjectRegistry = workspaceSubjectRegistry;
    eventService.subscribe(
        new EventSubscriber<ServerIdleEvent>() {

          @Override
          public void onEvent(ServerIdleEvent event) {
            idleCheServer(event);
          }
        });
    LOG.info("openshiftWorkspaceEnvironmentProvider = {}", openshiftUserAccountProvider.getClass());
  }

  private String retrieveApiEndpoint() {
    OpenShiftClient oc = ocFactory.newOcClient();
    Service cheService =
        oc.services()
            .inNamespace(openShiftCheProjectName)
            .withName(OPENSHIFT_CHE_SERVER_SERVICE_NAME)
            .get();
    if (cheService != null) {
      if (openshiftWorkspaceEnvironmentProvider.areWorkspacesExternal()) {
        RouteList routes = oc.routes().inNamespace(openShiftCheProjectName).list();
        for (Route route : routes.getItems()) {
          RouteSpec spec = route.getSpec();
          RouteTargetReference target = spec.getTo();
          if (target != null
              && "Service".equalsIgnoreCase(target.getKind())
              && OPENSHIFT_CHE_SERVER_SERVICE_NAME.equals(target.getName())) {
            String host = spec.getHost();
            String protocol = spec.getTls() != null ? "https://" : "http://";
            return protocol + host + "/wsmaster/api";
          }
        }
      } else {
        ServiceSpec spec = cheService.getSpec();
        String host = spec.getClusterIP();
        String protocol = "http://";
        String port = "";
        List<ServicePort> ports = spec.getPorts();
        if (!ports.isEmpty()) {
          port = ":" + ports.get(0).getPort();
        }
        return protocol + host + port + "/wsmaster/api";
      }
    }
    return null;
  }

  /**
   * Gets the API endpoint URL to be used if the `che.workspace.che_server_endpoint` property is
   * null.
   *
   * @return .
   * @throws IOException when a problem occurs with docker api calls
   */
  @Override
  @Nullable
  public String getApiEndpoint() {
    synchronized (apiEndpointRetrieved) {
      if (!apiEndpointRetrieved) {
        apiEndpointRetrieved = true;
        apiEndpoint = retrieveApiEndpoint();
        LOG.debug("apiEndpoint = {}", apiEndpoint);
      }
    }

    return apiEndpoint;
  }

  @Override
  public Version getVersion() throws IOException {
    CheOpenshiftClient openShiftClient = ocFactory.newOcClient();
    String versionString = openShiftClient.getVersion();
    if (isNullOrEmpty(versionString)) {
      return null;
    }

    final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    OpenShiftVersion openShiftVersion = gson.fromJson(versionString, OpenShiftVersion.class);
    Version version = openShiftVersion.getVersion();
    version.setApiVersion(openShiftClient.getApiVersion());
    return version;
  }

  private void idleCheServer(ServerIdleEvent event) {
    DefaultOpenShiftClient openShiftClient = ocFactory.newOcClient();
    DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig>
        deploymentConfigResource =
            openShiftClient
                .deploymentConfigs()
                .inNamespace(openShiftCheProjectName)
                .withName(OPENSHIFT_CHE_SERVER_DEPLOYMENT_NAME);
    DeploymentConfig deploymentConfig = deploymentConfigResource.get();
    if (deploymentConfig == null) {
      LOG.warn(
          String.format("Deployment config %s not found", OPENSHIFT_CHE_SERVER_DEPLOYMENT_NAME));
      return;
    }
    Integer replicas = deploymentConfig.getSpec().getReplicas();
    if (replicas != null && replicas > 0) {
      Resource<Endpoints, DoneableEndpoints> endpointResource =
          openShiftClient
              .endpoints()
              .inNamespace(openShiftCheProjectName)
              .withName(OPENSHIFT_CHE_SERVER_SERVICE_NAME);
      Endpoints endpoint = endpointResource.get();
      if (endpoint == null) {
        LOG.warn(String.format("Endpoint %s not found", OPENSHIFT_CHE_SERVER_SERVICE_NAME));
        return;
      }
      Map<String, String> annotations = deploymentConfig.getMetadata().getAnnotations();
      if (annotations == null) {
        annotations = new HashMap<>();
        deploymentConfig.getMetadata().setAnnotations(annotations);
      }
      TimeZone tz = TimeZone.getTimeZone("UTC");
      DateFormat df = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
      df.setTimeZone(tz);
      String idle = df.format(new Date());
      annotations.put(IDLING_ALPHA_OPENSHIFT_IO_IDLED_AT, idle);
      annotations.put(IDLING_ALPHA_OPENSHIFT_IO_PREVIOUS_SCALE, "1");
      deploymentConfig.getSpec().setReplicas(0);
      deploymentConfigResource.patch(deploymentConfig);
      Map<String, String> endpointAnnotations = endpoint.getMetadata().getAnnotations();
      if (endpointAnnotations == null) {
        endpointAnnotations = new HashMap<>();
        endpoint.getMetadata().setAnnotations(endpointAnnotations);
      }
      endpointAnnotations.put(IDLING_ALPHA_OPENSHIFT_IO_IDLED_AT, idle);
      endpointAnnotations.put(
          IDLING_ALPHA_OPENSHIFT_IO_UNIDLE_TARGETS,
          "[{\"kind\":\"DeploymentConfig\",\"name\":\""
              + OPENSHIFT_CHE_SERVER_DEPLOYMENT_NAME
              + "\",\"replicas\":1}]");
      endpointResource.patch(endpoint);
      LOG.info("Che server has been idled");
    }
  }

  /**
   * Gets exposed ports for both container and image.
   *
   * @param containerConfig the configuration of the container
   * @param imageConfig the configuration of the image
   * @return all exposed ports
   */
  protected Set<String> getExposedPorts(ContainerConfig containerConfig, ImageConfig imageConfig) {

    Map<String, Map<String, String>> containerExposedPortsMap = containerConfig.getExposedPorts();
    if (containerExposedPortsMap == null) {
      containerExposedPortsMap = Collections.emptyMap();
    }
    Map<String, org.eclipse.che.plugin.docker.client.json.ExposedPort> imageExposedPortsMap =
        imageConfig.getExposedPorts();
    if (imageExposedPortsMap == null) {
      imageExposedPortsMap = Collections.emptyMap();
    }

    Set<String> containerExposedPorts = containerExposedPortsMap.keySet();
    Set<String> imageExposedPorts = imageExposedPortsMap.keySet();
    return ImmutableSet.<String>builder()
        .addAll(containerExposedPorts)
        .addAll(imageExposedPorts)
        .build();
  }

  /**
   * Gets labels for both container and image.
   *
   * @param containerConfig the configuration of the container
   * @param imageConfig the configuration of the image
   * @return all labels found
   */
  protected Map<String, String> getLabels(
      ContainerConfig containerConfig, ImageConfig imageConfig) {

    // first, get labels defined in the container configuration
    Map<String, String> containerLabels = containerConfig.getLabels();
    if (containerLabels == null) {
      containerLabels = Collections.emptyMap();
    }

    // Also, get labels from the image itself
    Map<String, String> imageLabels = imageConfig.getLabels();
    if (imageLabels == null) {
      imageLabels = Collections.emptyMap();
    }

    // Now merge all labels
    final Map<String, String> allLabels = new HashMap<>(containerLabels);
    allLabels.putAll(imageLabels);
    return allLabels;
  }

  /**
   * Gets the mapping between the port (with format 8080/tcp) and the associated label (if found)
   *
   * @param labels the mapping for known port labels
   * @param exposedPorts the ports that are exposed
   * @return a map that allow to get the service name for a given exposed port
   */
  protected Map<String, String> getPortsToRefName(
      Map<String, String> labels, Set<String> exposedPorts) {

    // Ports to known/unknown ref is map like : 8080/tcp <--> myCustomLabel
    Pattern pattern = Pattern.compile(LABEL_CHE_SERVER_REF_KEY);
    Map<String, String> portsToKnownRefName =
        labels
            .entrySet()
            .stream()
            .filter(map -> pattern.matcher(map.getKey()).matches())
            .collect(
                Collectors.toMap(
                    p -> {
                      Matcher matcher = pattern.matcher(p.getKey());
                      matcher.matches();
                      String val = matcher.group(1);
                      return val.contains("/") ? val : val.concat("/tcp");
                    },
                    p -> p.getValue()));

    // add to this map only port without a known ref name
    Map<String, String> portsToUnkownRefName =
        exposedPorts
            .stream()
            .filter((port) -> !portsToKnownRefName.containsKey(port))
            .collect(Collectors.toMap(p -> p, p -> "server-" + p.replace('/', '-')));

    // list of all ports with refName (known/unknown)
    Map<String, String> portsToRefName = new HashMap(portsToKnownRefName);
    portsToRefName.putAll(portsToUnkownRefName);

    return portsToRefName;
  }

  private Subject subjectForContainerId(String containerId) {
    String workspaceId = containerIdToWorkspaceId.get(containerId);
    if (workspaceId != null) {
      Subject subject = workspaceSubjectRegistry.getWorkspaceStarter(workspaceId);
      if (subject != null) {
        return subject;
      }
      Subject envSubject = EnvironmentContext.getCurrent().getSubject();
      LOG.info(
          "Didn't find the user that started workspace '{}' => using environment context subject: '{}'",
          workspaceId,
          envSubject.getUserName());
      return envSubject;
    }
    LOG.warn("Didn't find container '{}' in the map of created containers", containerId);
    return null;
  }

  private Subject subjectForImageId(String imageId) {
    String workspaceId = imageIdToWorkspaceId.get(imageId);
    if (workspaceId != null) {
      Subject subject = workspaceSubjectRegistry.getWorkspaceStarter(workspaceId);
      if (subject != null) {
        return subject;
      }
      Subject envSubject = EnvironmentContext.getCurrent().getSubject();
      LOG.info(
          "Didn't find the user that started workspace '{}' => using environment context subject: '{}'",
          workspaceId,
          envSubject.getUserName());
      return envSubject;
    }
    LOG.warn("Didn't find image '{}' in the map of created images", imageId);
    return null;
  }

  /**
   * @param createContainerParams
   * @return
   * @throws IOException
   */
  @Override
  public ContainerCreated createContainer(CreateContainerParams createContainerParams)
      throws IOException {
    String containerName =
        KubernetesStringUtils.convertToContainerName(createContainerParams.getContainerName());
    String workspaceID = getCheWorkspaceId(createContainerParams);

    // imageForDocker is the docker version of the image repository. It's needed for other
    // OpenShiftConnector API methods, but is not acceptable as an OpenShift name
    String imageForDocker = createContainerParams.getContainerConfig().getImage();
    imageIdToWorkspaceId.put(imageForDocker, getOriginalCheWorkspaceId(createContainerParams));

    // imageStreamTagName is imageForDocker converted into a form that can be used
    // in OpenShift
    String imageStreamTagName = KubernetesStringUtils.convertPullSpecToTagName(imageForDocker);

    // imageStreamTagName is not enough to fill out a pull spec; it is only the tag, so we
    // have to get the ImageStreamTag from the tag, and then get the full ImageStreamTag name
    // from that tag. This works because the tags used in Che are unique.
    ImageStreamTag imageStreamTag =
        getImageStreamTagFromRepo(
            imageStreamTagName,
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(),
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace());
    String imageStreamTagPullSpec = imageStreamTag.getMetadata().getName();

    // Next we need to get the address of the registry where the ImageStreamTag is stored
    String imageStreamName =
        KubernetesStringUtils.getImageStreamNameFromPullSpec(imageStreamTagPullSpec);

    ImageStream imageStream;
    OpenShiftClient openShiftClient =
        ocFactory.newOcClient(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    imageStream =
        openShiftClient
            .imageStreams()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .withName(imageStreamName)
            .get();
    if (imageStream == null) {
      throw new OpenShiftException("ImageStream not found");
    }
    String registryAddress = imageStream.getStatus().getDockerImageRepository().split("/")[0];

    // The above needs to be combined to form a pull spec that will work when defining a container.
    String dockerPullSpec =
        String.format(
            "%s/%s/%s",
            registryAddress,
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(),
            imageStreamTagPullSpec);

    ContainerConfig containerConfig = createContainerParams.getContainerConfig();
    ImageConfig imageConfig = inspectImage(InspectImageParams.create(imageForDocker)).getConfig();

    final Set<String> exposedPorts = getExposedPorts(containerConfig, imageConfig);
    final Map<String, String> labels = getLabels(containerConfig, imageConfig);
    Map<String, String> portsToRefName = getPortsToRefName(labels, exposedPorts);

    String[] envVariables = createContainerParams.getContainerConfig().getEnv();
    String[] volumes = createContainerParams.getContainerConfig().getHostConfig().getBinds();

    Map<String, String> additionalLabels = createContainerParams.getContainerConfig().getLabels();
    String networkName =
        createContainerParams.getContainerConfig().getHostConfig().getNetworkMode();
    EndpointConfig endpointConfig =
        createContainerParams
            .getContainerConfig()
            .getNetworkingConfig()
            .getEndpointsConfig()
            .get(networkName);
    String[] endpointAliases = endpointConfig != null ? endpointConfig.getAliases() : new String[0];

    Map<String, Quantity> resourceLimits = new HashMap<>();
    if (!isNullOrEmpty(cheWorkspaceMemoryLimit)) {
      LOG.info(
          "Che property 'che.openshift.workspace.memory.override' "
              + "used to override workspace memory limit to {}.",
          cheWorkspaceMemoryLimit);
      resourceLimits.put("memory", new Quantity(cheWorkspaceMemoryLimit));
    } else {
      long memoryLimitBytes =
          createContainerParams.getContainerConfig().getHostConfig().getMemory();
      String memoryLimit = Long.toString(memoryLimitBytes / 1048576) + "Mi";
      LOG.info("Creating workspace pod with memory limit of {}.", memoryLimit);
      resourceLimits.put("memory", new Quantity(cheWorkspaceMemoryLimit));
    }

    Map<String, Quantity> resourceRequests = new HashMap<>();
    if (!isNullOrEmpty(cheWorkspaceMemoryRequest)) {
      resourceRequests.put("memory", new Quantity(cheWorkspaceMemoryRequest));
    }

    String deploymentName;
    String serviceName;
    if (isDevMachine(createContainerParams)) {
      serviceName = deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
    } else {
      if (endpointAliases.length > 0) {
        serviceName = endpointAliases[0];
        deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + serviceName;
      } else {
        // Should never happen
        serviceName =
            deploymentName =
                CHE_OPENSHIFT_RESOURCES_PREFIX + KubernetesStringUtils.generateWorkspaceID();
      }
    }

    String containerID;
    try {
      createOpenShiftService(
          deploymentName,
          serviceName,
          exposedPorts,
          portsToRefName,
          additionalLabels,
          endpointAliases);
      createOpenShiftDeployment(
          deploymentName,
          dockerPullSpec,
          containerName,
          exposedPorts,
          portsToRefName,
          envVariables,
          volumes,
          resourceLimits,
          resourceRequests);

      containerID = waitAndRetrieveContainerID(deploymentName);
      if (containerID == null) {
        throw new OpenShiftException(
            "Failed to get the ID of the container running in the OpenShift pod");
      }
      containerIdToWorkspaceId.put(containerID, getOriginalCheWorkspaceId(createContainerParams));
    } catch (Exception e) {
      // Make sure we clean up deployment and service in case of an error -- otherwise Che can end
      // up
      // in an inconsistent state.
      LOG.info("Error while creating Pod, removing deployment");
      LOG.info(e.getMessage());
      imageIdToWorkspaceId.remove(imageForDocker);
      openShiftDeploymentCleaner.cleanDeploymentResources(deploymentName);
      try {
        openShiftClient =
            ocFactory.newOcClient(
                openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
        openShiftClient.resource(imageStreamTag).delete();
      } catch (Exception ee) {
        LOG.info("Error while cleaning image stream tag {}", imageStreamTag.toString());
        LOG.info(e.getMessage());
      }
      throw e;
    }

    return new ContainerCreated(containerID, null);
  }

  @Override
  public void startContainer(final StartContainerParams params) throws IOException {
    // Not used in OpenShift
  }

  @Override
  public void stopContainer(StopContainerParams params) throws IOException {
    // Not used in OpenShift
  }

  @Override
  public int waitContainer(String container) throws IOException {
    // Not used in OpenShift
    return 0;
  }

  @Override
  public void killContainer(KillContainerParams params) throws IOException {
    // Not used in OpenShift
  }

  @Override
  public List<ContainerListEntry> listContainers() throws IOException {
    // Implement once 'Service Provider Interface' is defined
    return Collections.emptyList();
  }

  @Override
  public InputStream getResource(GetResourceParams params) throws IOException {
    throw new UnsupportedOperationException(
        "'getResource' is currently not supported by OpenShift");
  }

  @Override
  public void putResource(PutResourceParams params) throws IOException {
    throw new UnsupportedOperationException(
        "'putResource' is currently not supported by OpenShift");
  }

  @Override
  public ContainerInfo inspectContainer(String containerId) throws IOException {
    Subject subject = subjectForContainerId(containerId);
    Pod pod = getChePodByContainerId(containerId);
    if (pod == null) {
      LOG.warn("No Pod found by container ID {}", containerId);
      return null;
    }

    String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
    if (deploymentName == null) {
      LOG.warn(
          "No label {} found for Pod {}", OPENSHIFT_DEPLOYMENT_LABEL, pod.getMetadata().getName());
      return null;
    }

    Deployment deployment;
    DefaultKubernetesClient client =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject));
    deployment = client.extensions().deployments().withName(deploymentName).get();
    if (deployment == null) {
      LOG.warn(
          "No deployment matching label {}={} found", OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
      return null;
    }

    List<Container> deploymentContainers =
        deployment.getSpec().getTemplate().getSpec().getContainers();
    if (deploymentContainers.size() > 1) {
      throw new OpenShiftException("Multiple Containers found in Pod.");
    } else if (deploymentContainers.size() < 1
        || isNullOrEmpty(deploymentContainers.get(0).getImage())) {
      throw new OpenShiftException(String.format("Container %s not found", containerId));
    }
    String podPullSpec = deploymentContainers.get(0).getImage();

    String tagName = KubernetesStringUtils.getTagNameFromPullSpec(podPullSpec);

    ImageStreamTag tag =
        getImageStreamTagFromRepo(
            tagName,
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject),
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject));
    ImageInfo imageInfo = getImageInfoFromTag(tag);

    Service svc = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName, subject);
    if (svc == null) {
      LOG.warn("No Service found by selector {}={}", OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
      return null;
    }

    return createContainerInfo(svc, imageInfo, pod, containerId);
  }

  @Override
  public void removeContainer(final RemoveContainerParams params) throws IOException {
    String containerId = params.getContainer();
    Subject subject = subjectForContainerId(containerId);
    String deploymentName = getDeploymentName(params);
    openShiftDeploymentCleaner.cleanDeploymentResources(deploymentName, subject);
    containerIdToWorkspaceId.remove(containerId);
  }

  @Override
  public NetworkCreated createNetwork(CreateNetworkParams params) throws IOException {
    // Not needed in OpenShift
    return new NetworkCreated().withId(params.getNetwork().getName());
  }

  @Override
  public void removeNetwork(String netId) throws IOException {
    // Not needed in OpenShift
  }

  @Override
  public void removeNetwork(RemoveNetworkParams params) throws IOException {
    // Not needed in OpenShift
  }

  @Override
  public void connectContainerToNetwork(String netId, String containerId) throws IOException {
    // Not needed in OpenShift
  }

  @Override
  public void connectContainerToNetwork(ConnectContainerToNetworkParams params) throws IOException {
    // Not used in OpenShift
  }

  @Override
  public void disconnectContainerFromNetwork(String netId, String containerId) throws IOException {
    // Not needed in OpenShift
  }

  @Override
  public void disconnectContainerFromNetwork(DisconnectContainerFromNetworkParams params)
      throws IOException {
    // Not needed in OpenShift
  }

  @Override
  public Network inspectNetwork(String netId) throws IOException {
    return inspectNetwork(InspectNetworkParams.create(netId));
  }

  @Override
  public Network inspectNetwork(InspectNetworkParams params) throws IOException {
    String netId = params.getNetworkId();
    ServiceList services;

    DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    services =
        kubeClient
            .services()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .list();

    Map<String, ContainerInNetwork> containers = new HashMap<>();
    for (Service svc : services.getItems()) {
      String selector = svc.getSpec().getSelector().get(OPENSHIFT_DEPLOYMENT_LABEL);
      if (selector == null || !selector.startsWith(CHE_OPENSHIFT_RESOURCES_PREFIX)) {
        continue;
      }

      PodList pods =
          kubeClient
              .pods()
              .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
              .withLabel(OPENSHIFT_DEPLOYMENT_LABEL, selector)
              .list();

      for (Pod pod : pods.getItems()) {
        String podName = pod.getMetadata().getName();
        ContainerInNetwork container =
            new ContainerInNetwork()
                .withName(podName)
                .withIPv4Address(svc.getSpec().getClusterIP());
        String podId =
            KubernetesStringUtils.getLabelFromContainerID(
                pod.getMetadata().getLabels().get(CHE_CONTAINER_IDENTIFIER_LABEL_KEY));
        if (podId == null) {
          continue;
        }
        containers.put(podId, container);
      }
    }

    List<IpamConfig> ipamConfig = new ArrayList<>();
    Ipam ipam =
        new Ipam().withDriver("bridge").withOptions(Collections.emptyMap()).withConfig(ipamConfig);

    return new Network()
        .withName("OpenShift")
        .withId(netId)
        .withContainers(containers)
        .withLabels(Collections.emptyMap())
        .withOptions(Collections.emptyMap())
        .withDriver("default")
        .withIPAM(ipam)
        .withScope("local")
        .withInternal(false)
        .withEnableIPv6(false);
  }

  /**
   * In OpenShift, there is only one network in the Docker sense, and it is similar to the default
   * bridge network. Rather than implementing all of the filters available in the Docker API, we
   * only implement {@code type=["custom"|"builtin"]}.
   *
   * <p>If type is "custom", null is returned. Otherwise, the default network is returned, and the
   * result is effectively the same as {@link DockerConnector#inspectNetwork(String)} where the
   * network is "bridge".
   *
   * @see DockerConnector#getNetworks()
   */
  @Override
  public List<Network> getNetworks(GetNetworksParams params) throws IOException {
    Filters filters = params.getFilters();
    List<Network> networks = new ArrayList<>();

    List<String> typeFilters = filters.getFilter("type");
    if (typeFilters == null || !typeFilters.contains("custom")) {
      Network network = inspectNetwork("openshift");
      networks.add(network);
    }
    return networks;
  }

  /**
   * Creates an ImageStream that tracks the repository.
   *
   * <p>Note: This method does not cause the relevant image to actually be pulled to the local
   * repository, but creating the ImageStream is necessary as it is used to obtain the address of
   * the internal Docker registry later.
   *
   * @see DockerConnector#pull(PullParams, ProgressMonitor)
   */
  @Override
  public void pull(final PullParams params, final ProgressMonitor progressMonitor)
      throws IOException {

    String repo = params.getFullRepo(); // image to be pulled
    String tag = params.getTag(); // e.g. latest, usually

    String imageStreamName = KubernetesStringUtils.convertPullSpecToImageStreamName(repo);
    ImageStream existingImageStream;

    OpenShiftClient openShiftClient =
        ocFactory.newOcClient(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    existingImageStream =
        openShiftClient
            .imageStreams()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .withName(imageStreamName)
            .get();

    if (existingImageStream == null) {
      openShiftClient
          .imageStreams()
          .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
          .createNew()
          .withNewMetadata()
          .withName(imageStreamName) // imagestream id
          .endMetadata()
          .withNewSpec()
          .addNewTag()
          .withName(tag)
          .endTag()
          .withDockerImageRepository(repo) // tracking repo
          .endSpec()
          .withNewStatus()
          .withDockerImageRepository("")
          .endStatus()
          .done();
    }

    // Wait for Image metadata to be obtained.
    ImageStream createdImageStream;
    for (int waitCount = 0; waitCount < OPENSHIFT_IMAGESTREAM_MAX_WAIT_COUNT; waitCount++) {
      try {
        Thread.sleep(OPENSHIFT_IMAGESTREAM_WAIT_DELAY);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      OpenShiftClient waitOpenShiftClient =
          ocFactory.newOcClient(
              openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
      createdImageStream =
          openShiftClient
              .imageStreams()
              .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
              .withName(imageStreamName)
              .get();

      if (createdImageStream != null
          && createdImageStream.getStatus().getDockerImageRepository() != null) {
        LOG.info(String.format("Created ImageStream %s.", imageStreamName));
        return;
      }
    }

    throw new OpenShiftException(
        String.format("Failed to create ImageStream %s.", imageStreamName));
  }

  /**
   * Creates an ImageStreamTag that tracks a given image.
   *
   * <p>Docker tags are used extensively in Che: all workspaces run on tagged images tracking built
   * stacks. For new workspaces, or when snapshots are not used, the tracked image is e.g. {@code
   * eclipse/ubuntu_jdk8}, whereas for snapshotted workspaces, the tracked image is the snapshot
   * (e.g. {@code machine_snapshot-<identifier>}.
   *
   * <p>Since OpenShift does not support the same tagging functionality as Docker, tags are
   * implemented as ImageStreamTags, where the {@code From} field is always the original image, and
   * the ImageStreamTag name is derived from both the source image and the target image. This
   * replicates functionality for Che in Docker, while working differently under the hood. The
   * ImageStream name is derived from the image that is being tracked (e.g. {@code
   * eclipse/ubuntu_jdk8}), while the tag name is derived from the target image (e.g. {@code
   * eclipse-che/che_workspace<identifier>}).
   *
   * @see DockerConnector#tag(TagParams)
   */
  @Override
  public void tag(final TagParams params) throws IOException {
    // E.g. `docker tag sourceImage targetImage`
    String paramsSourceImage = params.getImage(); // e.g. eclipse/ubuntu_jdk8
    String targetImage = params.getRepository(); // e.g. eclipse-che/<identifier>
    String paramsTag = params.getTag();

    String sourceImage = KubernetesStringUtils.stripTagFromPullSpec(paramsSourceImage);
    String tag = KubernetesStringUtils.getTagNameFromPullSpec(paramsSourceImage);
    if (isNullOrEmpty(tag)) {
      tag = !isNullOrEmpty(paramsTag) ? paramsTag : "latest";
    }

    String sourceImageWithTag;
    // Check if sourceImage matches existing imageStreamTag (e.g. when tagging a snapshot)
    try {
      String sourceImageTagName = KubernetesStringUtils.convertPullSpecToTagName(sourceImage);
      ImageStreamTag existingTag =
          getImageStreamTagFromRepo(
              sourceImageTagName,
              openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(),
              openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace());
      sourceImageWithTag = existingTag.getTag().getFrom().getName();
    } catch (IOException e) {
      // Image not found.
      sourceImageWithTag = String.format("%s:%s", sourceImage, tag);
    }

    String imageStreamTagName =
        KubernetesStringUtils.createImageStreamTagName(sourceImageWithTag, targetImage);

    createImageStreamTag(sourceImageWithTag, imageStreamTagName);
  }

  @Override
  public ImageInfo inspectImage(InspectImageParams params) throws IOException {
    Subject subject = subjectForImageId(params.getImage());
    if (subject == null) {
      subject = EnvironmentContext.getCurrent().getSubject();
    }
    final Config openshiftConfig =
        openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject);
    final String openshiftNamespace =
        openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject);

    String image = KubernetesStringUtils.getImageStreamNameFromPullSpec(params.getImage());

    String imageStreamTagName = KubernetesStringUtils.convertPullSpecToTagName(image);
    ImageStreamTag imageStreamTag =
        getImageStreamTagFromRepo(imageStreamTagName, openshiftConfig, openshiftNamespace);

    return getImageInfoFromTag(imageStreamTag);
  }

  @Override
  public void removeImage(final RemoveImageParams params) throws IOException {
    Subject subject = subjectForImageId(params.getImage());
    final Config openshiftConfig =
        openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject);
    final String openshiftNamespace =
        openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject);

    OpenShiftClient openShiftClient = ocFactory.newOcClient(openshiftConfig);
    String image = KubernetesStringUtils.getImageStreamNameFromPullSpec(params.getImage());
    String imageStreamTagName = KubernetesStringUtils.convertPullSpecToTagName(image);
    ImageStreamTag imageStreamTag =
        getImageStreamTagFromRepo(imageStreamTagName, openshiftConfig, openshiftNamespace);
    openShiftClient.resource(imageStreamTag).delete();
    imageIdToWorkspaceId.remove(params.getImage());
  }

  /**
   * OpenShift does not support taking image snapshots since the underlying assumption is that Pods
   * are largely immutable (and so any snapshot would be identical to the image used to create the
   * pod). Che uses docker commit to create machine snapshots, which are used to restore workspaces.
   * To emulate this functionality in OpenShift, commit actually creates a new ImageStreamTag by
   * calling {@link OpenShiftConnector#tag(TagParams)} named for the snapshot that would be created.
   *
   * @see DockerConnector#commit(CommitParams)
   */
  @Override
  public String commit(final CommitParams params) throws IOException {
    String repo = params.getRepository(); // e.g. machine_snapshot_mdkfmksdfm
    String container = params.getContainer(); // container ID

    Pod pod = getChePodByContainerId(container);
    String image = pod.getSpec().getContainers().get(0).getImage();
    String imageStreamTagName = KubernetesStringUtils.getTagNameFromPullSpec(image);

    ImageStreamTag imageStreamTag =
        getImageStreamTagFromRepo(
            imageStreamTagName,
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(),
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace());
    String sourcePullSpec = imageStreamTag.getTag().getFrom().getName();
    String trackingRepo = KubernetesStringUtils.stripTagFromPullSpec(sourcePullSpec);
    String tag = KubernetesStringUtils.getTagNameFromPullSpec(sourcePullSpec);

    tag(TagParams.create(trackingRepo, repo).withTag(tag));

    return repo; // Return value not used.
  }

  @Override
  public void getEvents(final GetEventsParams params, MessageProcessor<Event> messageProcessor)
      throws OpenShiftException {
    CountDownLatch waitForClose = new CountDownLatch(1);
    Watcher<io.fabric8.kubernetes.api.model.Event> eventWatcher =
        new Watcher<io.fabric8.kubernetes.api.model.Event>() {
          @Override
          public void eventReceived(Action action, io.fabric8.kubernetes.api.model.Event event) {
            // Do nothing;
          }

          @Override
          public void onClose(KubernetesClientException e) {
            if (e == null) {
              LOG.error("Eventwatch Closed");
            } else {
              LOG.error("Eventwatch Closed" + e.getMessage());
            }
            waitForClose.countDown();
          }
        };
    try (DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig())) {
      kubeClient
          .events()
          .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
          .watch(eventWatcher);
      try {
        waitForClose.await();
      } catch (InterruptedException e) {
        LOG.error("Thread interrupted while waiting for eventWatcher.");
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void getContainerLogs(
      final GetContainerLogsParams params, MessageProcessor<LogMessage> containerLogsProcessor)
      throws IOException {
    String container = params.getContainer(); // container ID
    Subject subject = subjectForContainerId(container);

    Pod pod = getChePodByContainerId(container);
    if (pod != null) {
      String podName = pod.getMetadata().getName();
      boolean[] ret = new boolean[1];
      ret[0] = false;
      DefaultKubernetesClient kubeClient =
          new DefaultKubernetesClient(
              openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject));
      try (LogWatch watchLog =
          kubeClient
              .pods()
              .inNamespace(
                  openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject))
              .withName(podName)
              .watchLog()) {
        Watcher<Pod> watcher =
            new Watcher<Pod>() {

              @Override
              public void eventReceived(Action action, Pod resource) {
                if (action == Action.DELETED) {
                  ret[0] = true;
                }
              }

              @Override
              public void onClose(KubernetesClientException cause) {
                ret[0] = true;
              }
            };
        kubeClient
            .pods()
            .inNamespace(
                openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject))
            .withName(podName)
            .watch(watcher);
        Thread.sleep(5000);
        InputStream is = watchLog.getOutput();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        while (!ret[0]) {
          String line = bufferedReader.readLine();
          containerLogsProcessor.process(new LogMessage(LogMessage.Type.DOCKER, line));
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      } catch (IOException e) {
        // The kubernetes client throws an exception (Pipe not connected) when pod doesn't contain
        // any logs.
        // We can ignore it.
      } finally {
        kubeClient.close();
      }
    }
  }

  @Override
  public ContainerProcesses top(final TopParams params) throws IOException {
    String containerId = params.getContainer();
    Subject subject = subjectForContainerId(containerId);
    Pod pod = getChePodByContainerId(containerId);
    String podName = pod.getMetadata().getName();
    String[] command;
    final String[] psArgs = params.getPsArgs();
    if (psArgs != null && psArgs.length != 0) {
      int length = psArgs.length + 1;
      command = new String[length];
      command[0] = PS_COMMAND;
      System.arraycopy(psArgs, 0, command, 1, psArgs.length);
    } else {
      command = new String[1];
      command[0] = PS_COMMAND;
    }
    ContainerProcesses processes = new ContainerProcesses();
    try (DefaultKubernetesClient kubeClient =
            new DefaultKubernetesClient(
                openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject));
        ExecWatch watch =
            kubeClient
                .pods()
                .inNamespace(
                    openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject))
                .withName(podName)
                .redirectingOutput()
                .redirectingError()
                .exec(command)) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(watch.getOutput()));
      boolean first = true;
      int limit = 0;
      try {
        List<String[]> procList = new ArrayList<>();
        while (reader.ready()) {
          String line = reader.readLine();
          if (line == null || line.isEmpty()) {
            continue;
          }
          if (line.startsWith("rpc error")) {
            throw new IOException(line);
          }
          line = line.trim();
          if (first) {
            String[] elements = line.split(TOP_REGEX_PATTERN);
            limit = elements.length;
            first = false;
            processes.setTitles(elements);
          } else {
            String[] elements = line.split(TOP_REGEX_PATTERN, limit);
            procList.add(elements);
          }
        }
        processes.setProcesses(procList.toArray(new String[0][0]));
      } catch (IOException e) {
        throw new OpenShiftException(e.getMessage());
      }
    } catch (KubernetesClientException e) {
      throw new OpenShiftException(e.getMessage());
    }
    return processes;
  }

  @Override
  public Exec createExec(final CreateExecParams params) throws IOException {
    String[] command = params.getCmd();
    String containerId = params.getContainer();

    Pod pod = getChePodByContainerId(containerId);
    String podName = pod.getMetadata().getName();

    String execId = KubernetesStringUtils.generateWorkspaceID();
    KubernetesExecHolder execHolder =
        new KubernetesExecHolder()
            .withCommand(command)
            .withPod(podName)
            .withContainerId(containerId);
    execMap.put(execId, execHolder);

    return new Exec(command, execId);
  }

  @Override
  public void startExec(
      final StartExecParams params, @Nullable MessageProcessor<LogMessage> execOutputProcessor)
      throws IOException {
    String execId = params.getExecId();

    KubernetesExecHolder exec = execMap.get(execId);
    Subject subject = subjectForContainerId(exec.getContainerId());

    String podName = exec.getPod();
    String[] command = exec.getCommand();
    for (int i = 0; i < command.length; i++) {
      command[i] = URLEncoder.encode(command[i], "UTF-8");
    }

    ExecutorService executor = Executors.newFixedThreadPool(2);
    try (DefaultKubernetesClient kubeClient =
        new DefaultKubernetesClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject))) {
      try (ExecWatch watch =
              kubeClient
                  .pods()
                  .inNamespace(
                      openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(
                          subject))
                  .withName(podName)
                  .redirectingOutput()
                  .redirectingError()
                  .exec(command);
          InputStreamPumper outputPump =
              new InputStreamPumper(
                  watch.getOutput(),
                  new KubernetesOutputAdapter(LogMessage.Type.STDOUT, execOutputProcessor));
          InputStreamPumper errorPump =
              new InputStreamPumper(
                  watch.getError(),
                  new KubernetesOutputAdapter(LogMessage.Type.STDERR, execOutputProcessor))) {
        Future<?> outFuture = executor.submit(outputPump);
        Future<?> errFuture = executor.submit(errorPump);
        // Short-term worksaround; the Futures above seem to never finish.
        Thread.sleep(2500);
      } catch (KubernetesClientException e) {
        throw new OpenShiftException(e.getMessage());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    } finally {
      execMap.remove(execId);
      executor.shutdown();
    }
  }

  @Override
  public SystemInfo getSystemInfo() throws IOException {
    DefaultKubernetesClient kubeClient = ocFactory.newKubeClient();
    PodList chePods = kubeClient.pods().inNamespace(this.openShiftCheProjectName).list();
    if (chePods.getItems().size() > 0) {
      Pod pod = chePods.getItems().get(0);
      Node node = kubeClient.nodes().withName(pod.getSpec().getNodeName()).get();
      NodeSystemInfo nodeInfo = node.getStatus().getNodeInfo();
      SystemInfo systemInfo = new SystemInfo();
      systemInfo.setKernelVersion(nodeInfo.getKernelVersion());
      systemInfo.setOperatingSystem(nodeInfo.getOperatingSystem());
      systemInfo.setID(node.getMetadata().getUid());
      int containers =
          kubeClient.pods().inNamespace(this.openShiftCheProjectName).list().getItems().size();
      int images = node.getStatus().getImages().size();
      systemInfo.setContainers(containers);
      systemInfo.setImages(images);
      systemInfo.setName(node.getMetadata().getName());
      String[] labels =
          node.getMetadata()
              .getLabels()
              .entrySet()
              .stream()
              .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
              .toArray(String[]::new);
      systemInfo.setLabels(labels);
      return systemInfo;
    } else {
      throw new OpenShiftException("No pod found");
    }
  }

  /**
   * Gets the ImageStreamTag corresponding to a given tag name (i.e. without the repository)
   *
   * @param imageStreamTagName the tag name to search for
   * @return
   * @throws IOException if either no matching tag is found, or there are multiple matches.
   */
  private ImageStreamTag getImageStreamTagFromRepo(
      String imageStreamTagName, Config openshiftConfig, String openshiftNamespace)
      throws IOException {

    // Note: ideally, ImageStreamTags could be identified with a label, but it seems like
    // ImageStreamTags do not support labels.
    List<ImageStreamTag> imageStreams;
    OpenShiftClient openShiftClient = ocFactory.newOcClient(openshiftConfig);
    imageStreams =
        openShiftClient.imageStreamTags().inNamespace(openshiftNamespace).list().getItems();

    // We only get ImageStreamTag names here, since these ImageStreamTags do not include
    // Docker metadata, for some reason.
    List<String> imageStreamTags =
        imageStreams
            .stream()
            .filter(
                e -> {
                  String tagName =
                      KubernetesStringUtils.getTagNameFromPullSpec(e.getMetadata().getName());
                  return imageStreamTagName.length() >= tagName.length()
                      ? imageStreamTagName.contains(tagName)
                      : tagName.contains(imageStreamTagName);
                })
            .map(e -> e.getMetadata().getName())
            .collect(Collectors.toList());

    if (imageStreamTags.size() < 1) {
      throw new OpenShiftException(
          String.format("ImageStreamTag %s not found!", imageStreamTagName));
    } else if (imageStreamTags.size() > 1) {
      throw new OpenShiftException(
          String.format("Multiple ImageStreamTags found for name %s", imageStreamTagName));
    }

    String imageStreamTag = imageStreamTags.get(0);

    // Finally, get the ImageStreamTag, with Docker metadata.
    return getImageStreamTag(imageStreamTag, openshiftConfig, openshiftNamespace);
  }

  private ImageStreamTag getImageStreamTag(
      final String imageStreamName, Config openshiftConfig, String openshiftNamespace)
      throws OpenShiftException {
    OpenShiftClient openShiftClient = ocFactory.newOcClient(openshiftConfig);
    return openShiftClient
        .imageStreamTags()
        .inNamespace(openshiftNamespace)
        .withName(imageStreamName)
        .get();
  }

  private Service getCheServiceBySelector(String selectorKey, String selectorValue, Subject subject)
      throws OpenShiftException {
    DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject));
    ServiceList svcs =
        kubeClient
            .services()
            .inNamespace(
                openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject))
            .list();

    Service svc =
        svcs.getItems()
            .stream()
            .filter(s -> s.getSpec().getSelector().containsKey(selectorKey))
            .filter(s -> s.getSpec().getSelector().get(selectorKey).equals(selectorValue))
            .findAny()
            .orElse(null);

    if (svc == null) {
      LOG.warn("No Service with selector {}={} could be found", selectorKey, selectorValue);
    }
    return svc;
  }

  private Pod getChePodByContainerId(String containerId) throws IOException {
    Subject subject = subjectForContainerId(containerId);
    DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig(subject));
    PodList pods =
        kubeClient
            .pods()
            .inNamespace(
                openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(subject))
            .withLabel(
                CHE_CONTAINER_IDENTIFIER_LABEL_KEY,
                KubernetesStringUtils.getLabelFromContainerID(containerId))
            .list();

    List<Pod> items = pods.getItems();

    if (items.isEmpty()) {
      LOG.error(
          "An OpenShift Pod with label {}={} could not be found",
          CHE_CONTAINER_IDENTIFIER_LABEL_KEY,
          containerId);
      throw new IOException(
          "An OpenShift Pod with label "
              + CHE_CONTAINER_IDENTIFIER_LABEL_KEY
              + "="
              + containerId
              + " could not be found");
    }

    if (items.size() > 1) {
      LOG.error(
          "There are {} pod with label {}={} (just one was expected)",
          items.size(),
          CHE_CONTAINER_IDENTIFIER_LABEL_KEY,
          containerId);
      throw new IOException(
          "There are "
              + items.size()
              + " pod with label "
              + CHE_CONTAINER_IDENTIFIER_LABEL_KEY
              + "="
              + containerId
              + " (just one was expeced)");
    }

    return items.get(0);
  }

  /**
   * Extracts the ImageInfo stored in an ImageStreamTag. The returned object is the JSON that would
   * be returned by executing {@code docker inspect <image>}, except, due to a quirk in OpenShift's
   * handling of this data, fields except for {@code Config} and {@code ContainerConfig} are null.
   *
   * @param imageStreamTag
   * @return
   */
  private ImageInfo getImageInfoFromTag(ImageStreamTag imageStreamTag) {
    // The DockerImageConfig string here is the JSON that would be returned by a docker inspect
    // image,
    // except that the capitalization is inconsistent, breaking deserialization. Top level elements
    // are lowercased with underscores, while nested elements conform to
    // FieldNamingPolicy.UPPER_CAMEL_CASE.
    // We're only converting the config fields for brevity; this means that other fields are null.
    Image tagImage = imageStreamTag.getImage();
    String dockerImageConfig = tagImage.getDockerImageConfig();

    if (!isNullOrEmpty(dockerImageConfig)) {
      LOG.info("imageStreamTag dockerImageConfig is not empty. Using it to get image info");
      ImageInfo info =
          GSON.fromJson(
              dockerImageConfig
                  .replaceFirst("config", "Config")
                  .replaceFirst("container_config", "ContainerConfig"),
              ImageInfo.class);
      return info;
    } else {
      LOG.info(
          "imageStreamTag dockerImageConfig empty. Using dockerImageMetadata to get image info");
      String dockerImageMetadata =
          GSON.toJson(tagImage.getAdditionalProperties().get("dockerImageMetadata"));
      ImageInfo info = GSON.fromJson(dockerImageMetadata, ImageInfo.class);
      return info;
    }
  }

  protected String getCheWorkspaceId(CreateContainerParams createContainerParams) {
    String workspaceID = getOriginalCheWorkspaceId(createContainerParams);
    return workspaceID.replaceFirst("workspace", "");
  }

  private String getOriginalCheWorkspaceId(CreateContainerParams createContainerParams) {
    Stream<String> env = Arrays.stream(createContainerParams.getContainerConfig().getEnv());
    String workspaceID =
        env.filter(v -> v.startsWith(CHE_WORKSPACE_ID_ENV_VAR) && v.contains("="))
            .map(v -> v.split("=", 2)[1])
            .findFirst()
            .orElse("");
    return workspaceID;
  }

  private boolean isDevMachine(CreateContainerParams createContainerParams) {
    Stream<String> env = Arrays.stream(createContainerParams.getContainerConfig().getEnv());
    return Boolean.parseBoolean(
        env.filter(v -> v.startsWith(CHE_IS_DEV_MACHINE_ENV_VAR) && v.contains("="))
            .map(v -> v.split("=", 2)[1])
            .findFirst()
            .orElse("false"));
  }

  private void createOpenShiftService(
      String deploymentName,
      String serviceName,
      Set<String> exposedPorts,
      Map<String, String> portsToRefName,
      Map<String, String> additionalLabels,
      String[] endpointAliases)
      throws OpenShiftException {
    Map<String, String> selector =
        Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
    List<ServicePort> ports = KubernetesService.getServicePortsFrom(exposedPorts, portsToRefName);

    DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    Service service =
        kubeClient
            .services()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .createNew()
            .withNewMetadata()
            .withName(serviceName)
            .withAnnotations(KubernetesLabelConverter.labelsToNames(additionalLabels))
            .endMetadata()
            .withNewSpec()
            .withSelector(selector)
            .withPorts(ports)
            .endSpec()
            .done();

    LOG.info("OpenShift service {} created", service.getMetadata().getName());

    for (ServicePort port : ports) {
      createOpenShiftRoute(serviceName, deploymentName, port.getName());
    }
  }

  private void createOpenShiftRoute(String serviceName, String deploymentName, String serverRef)
      throws OpenShiftException {
    String routeId = serviceName.replaceFirst(CHE_OPENSHIFT_RESOURCES_PREFIX, "");
    openShiftRouteCreator.createRoute(
        openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(),
        cheServerExternalAddress,
        cheWorkspacesRoutingSuffixProvider.get(),
        serverRef,
        serviceName,
        deploymentName,
        routeId,
        secureRoutes);
  }

  private void createOpenShiftDeployment(
      String deploymentName,
      String imageName,
      String sanitizedContainerName,
      Set<String> exposedPorts,
      Map<String, String> portsToRefName,
      String[] envVariables,
      String[] volumes,
      Map<String, Quantity> resourceLimits,
      Map<String, Quantity> resourceRequests)
      throws OpenShiftException {

    LOG.info("Creating OpenShift deployment {}", deploymentName);

    Map<String, String> selector =
        Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

    LOG.info(
        "Adding container {} to OpenShift deployment {}", sanitizedContainerName, deploymentName);

    if (createWorkspaceDirs) {
      createWorkspaceDir(volumes);
    }

    Container container =
        new ContainerBuilder()
            .withName(sanitizedContainerName)
            .withImage(imageName)
            .withEnv(KubernetesEnvVar.getEnvFrom(envVariables))
            .withPorts(KubernetesContainer.getContainerPortsFrom(exposedPorts, portsToRefName))
            .withImagePullPolicy(OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT)
            .withNewSecurityContext()
            .withPrivileged(false)
            .endSecurityContext()
            .withLivenessProbe(getLivenessProbeFrom(exposedPorts))
            .withVolumeMounts(getVolumeMountsFrom(volumes))
            .withNewResources()
            .withLimits(resourceLimits)
            .withRequests(resourceRequests)
            .endResources()
            .build();

    PodSpec podSpec =
        new PodSpecBuilder()
            .withContainers(container)
            .withVolumes(getVolumesFrom(volumes))
            .withTerminationGracePeriodSeconds(OPENSHIFT_POD_TERMINATION_GRACE_PERIOD)
            .build();

    Deployment deployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName(deploymentName)
            .withNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .endMetadata()
            .withNewSpec()
            .withReplicas(1)
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

    DefaultKubernetesClient openShiftClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    deployment =
        openShiftClient
            .extensions()
            .deployments()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .create(deployment);

    LOG.info("OpenShift deployment {} created", deploymentName);
  }

  /**
   * Creates a new ImageStreamTag
   *
   * @param sourceImageWithTag the image that the ImageStreamTag will track
   * @param imageStreamTagName the name of the imageStream tag (e.g. {@code <ImageStream name>:<Tag
   *     name>})
   * @return the created ImageStreamTag
   * @throws IOException When {@code sourceImageWithTag} metadata cannot be found
   */
  private ImageStreamTag createImageStreamTag(String sourceImageWithTag, String imageStreamTagName)
      throws IOException {

    try {
      OpenShiftClient openShiftClient =
          ocFactory.newOcClient(
              openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
      openShiftClient
          .imageStreamTags()
          .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
          .createOrReplaceWithNew()
          .withNewMetadata()
          .withName(imageStreamTagName)
          .endMetadata()
          .withNewTag()
          .withNewFrom()
          .withKind("DockerImage")
          .withName(sourceImageWithTag)
          .endFrom()
          .endTag()
          .done();

      // Wait for image metadata to be pulled
      for (int waitCount = 0; waitCount < OPENSHIFT_IMAGESTREAM_MAX_WAIT_COUNT; waitCount++) {
        Thread.sleep(OPENSHIFT_IMAGESTREAM_WAIT_DELAY);
        ImageStreamTag createdTag =
            openShiftClient
                .imageStreamTags()
                .inNamespace(
                    openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
                .withName(imageStreamTagName)
                .get();
        if (createdTag != null) {
          LOG.info(
              String.format(
                  "Created ImageStreamTag %s in namespace %s",
                  createdTag.getMetadata().getName(),
                  openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace()));
          return createdTag;
        }
      }

      throw new ImageNotFoundException(String.format("Image %s not found.", sourceImageWithTag));

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException(e.getLocalizedMessage(), e);
    }
  }

  /**
   * Collects the relevant information from a Service, an ImageInfo, and a Pod into a docker
   * ContainerInfo JSON object. The returned object is what would be returned by executing {@code
   * docker inspect <container>}, with fields filled as available.
   *
   * @param svc
   * @param imageInfo
   * @param pod
   * @param containerId
   * @return
   * @throws OpenShiftException
   */
  private ContainerInfo createContainerInfo(
      Service svc, ImageInfo imageInfo, Pod pod, String containerId) throws OpenShiftException {

    // In Che on OpenShift, we only have one container per pod.
    Container container = pod.getSpec().getContainers().get(0);
    ContainerConfig imageContainerConfig = imageInfo.getContainerConfig();

    // HostConfig
    HostConfig hostConfig = new HostConfig();
    hostConfig.setBinds(new String[0]);

    // Env vars
    List<String> imageEnv = Arrays.asList(imageContainerConfig.getEnv());
    List<String> containerEnv =
        container
            .getEnv()
            .stream()
            .map(e -> String.format("%s=%s", e.getName(), e.getValue()))
            .collect(Collectors.toList());
    String[] env = Stream.concat(imageEnv.stream(), containerEnv.stream()).toArray(String[]::new);

    // Exposed Ports
    Map<String, List<PortBinding>> ports = getCheServicePorts(svc);
    Map<String, Map<String, String>> exposedPorts = new HashMap<>();
    for (String key : ports.keySet()) {
      exposedPorts.put(key, Collections.emptyMap());
    }

    // Labels
    Map<String, String> annotations =
        KubernetesLabelConverter.namesToLabels(svc.getMetadata().getAnnotations());
    Map<String, String> containerLabels = imageInfo.getConfig().getLabels();
    Map<String, String> labels =
        Stream.concat(annotations.entrySet().stream(), containerLabels.entrySet().stream())
            .filter(e -> e.getKey().startsWith(KubernetesLabelConverter.getCheServerLabelPrefix()))
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

    // ContainerConfig
    ContainerConfig config = imageContainerConfig;
    config.setHostname(svc.getMetadata().getName());
    config.setEnv(env);
    config.setExposedPorts(exposedPorts);
    config.setLabels(labels);
    config.setImage(container.getImage());

    // NetworkSettings
    NetworkSettings networkSettings = new NetworkSettings();
    networkSettings.setIpAddress(svc.getSpec().getClusterIP());
    networkSettings.setGateway(svc.getSpec().getClusterIP());
    networkSettings.setPorts(ports);

    // Make final ContainerInfo
    ContainerInfo info = new ContainerInfo();
    info.setId(containerId);
    info.setConfig(config);
    info.setNetworkSettings(networkSettings);
    info.setHostConfig(hostConfig);
    info.setImage(imageInfo.getConfig().getImage());

    // In Che on OpenShift, we only have one container per pod.
    info.setState(getContainerStates(pod).get(0));
    return info;
  }

  private List<ContainerState> getContainerStates(final Pod pod) throws OpenShiftException {
    List<ContainerState> containerStates = new ArrayList<>();
    List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
    for (ContainerStatus status : containerStatuses) {
      io.fabric8.kubernetes.api.model.ContainerState state = status.getState();

      ContainerStateTerminated terminated = state.getTerminated();
      ContainerStateWaiting waiting = state.getWaiting();
      ContainerStateRunning running = state.getRunning();

      ContainerState containerState = new ContainerState();

      if (terminated != null) {
        containerState.setStatus("exited");
      } else if (waiting != null) {
        containerState.setStatus("paused");
      } else if (running != null) {
        containerState.setStatus("running");
      } else {
        throw new OpenShiftException(
            "Fail to detect the state of container with id " + status.getContainerID());
      }
      containerStates.add(containerState);
    }
    return containerStates;
  }

  private void createWorkspaceDir(String[] volumes) throws OpenShiftException {
    PersistentVolumeClaim pvc = getClaimCheWorkspace();
    String workspaceSubpath = getWorkspaceSubpath(volumes);
    if (pvc != null && !isNullOrEmpty(workspaceSubpath)) {
      LOG.info("Making sure directory exists for workspace {}", workspaceSubpath);
      boolean succeeded =
          openShiftPvcHelper.createJobPod(
              EnvironmentContext.getCurrent().getSubject(),
              workspacesPersistentVolumeClaim,
              openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace(),
              "create-",
              OpenShiftPvcHelper.Command.MAKE,
              workspaceSubpath);
      if (!succeeded) {
        LOG.error(
            "Failed to create workspace directory {} in PVC {}",
            workspaceSubpath,
            workspacesPersistentVolumeClaim);
        throw new OpenShiftException("Failed to create workspace directory in PVC");
      }
    }
  }

  /**
   * Gets the workspace subpath from an array of volumes. Since volumes provided are those used when
   * running Che in Docker, most of the volume spec is ignored; this method returns the subpath
   * within the hostpath that refers to the workspace.
   *
   * <p>E.g. for a volume {@code /data/workspaces/wksp-8z00:/projects:Z}, this method will return
   * "wksp-8z00".
   *
   * @param volumes
   * @return
   */
  private String getWorkspaceSubpath(String[] volumes) {
    String workspaceSubpath = null;
    for (String volume : volumes) {
      // Volumes are structured <hostpath>:<mountpath>:<options>.
      // We first check that <mountpath> matches the mount path for projects
      // and then extract the hostpath directory. The first part of the volume
      // String will be structured <cheWorkspaceStorage>/workspaceName.
      String mountPath = volume.split(":", 3)[1];
      if (cheWorkspaceProjectsStorage.equals(mountPath)) {
        workspaceSubpath = volume.split(":", 3)[0].replaceAll(cheWorkspaceStorage, "");
        if (workspaceSubpath.startsWith("/")) {
          workspaceSubpath = workspaceSubpath.substring(1);
        }
      }
    }
    return workspaceSubpath;
  }

  private List<VolumeMount> getVolumeMountsFrom(String[] volumes) throws OpenShiftException {
    List<VolumeMount> vms = new ArrayList<>();
    PersistentVolumeClaim pvc = getClaimCheWorkspace();
    if (pvc != null) {
      String subPath = getWorkspaceSubpath(volumes);
      if (subPath != null) {
        VolumeMount vm =
            new VolumeMountBuilder()
                .withMountPath(cheWorkspaceProjectsStorage)
                .withName(workspacesPersistentVolumeClaim)
                .withSubPath(subPath)
                .build();

        // add a mount from PVC for the logs
        VolumeMount logsVm =
            new VolumeMountBuilder()
                .withMountPath(CHE_MOUNTED_WORKSPACE_FOLDER)
                .withName(workspacesPersistentVolumeClaim)
                .withSubPath(subPath + WORKSPACE_LOGS_FOLDER_SUFFIX)
                .build();

        vms.add(vm);
        vms.add(logsVm);
      }
    }
    return vms;
  }

  private List<Volume> getVolumesFrom(String[] volumes) throws OpenShiftException {
    List<Volume> vs = new ArrayList<>();
    PersistentVolumeClaim pvc = getClaimCheWorkspace();
    if (pvc != null) {
      for (String volume : volumes) {
        String mountPath = volume.split(":", 3)[1];
        if (cheWorkspaceProjectsStorage.equals(mountPath)) {
          PersistentVolumeClaimVolumeSource pvcs =
              new PersistentVolumeClaimVolumeSourceBuilder()
                  .withClaimName(workspacesPersistentVolumeClaim)
                  .build();
          Volume v =
              new VolumeBuilder()
                  .withPersistentVolumeClaim(pvcs)
                  .withName(workspacesPersistentVolumeClaim)
                  .build();
          vs.add(v);
        }
      }
    }
    return vs;
  }

  private PersistentVolumeClaim getClaimCheWorkspace() throws OpenShiftException {
    DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    PersistentVolumeClaimList pvcList =
        kubeClient
            .persistentVolumeClaims()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .list();
    for (PersistentVolumeClaim pvc : pvcList.getItems()) {
      if (workspacesPersistentVolumeClaim.equals(pvc.getMetadata().getName())) {
        return pvc;
      }
    }
    Map<String, Quantity> requests = new HashMap<>();
    requests.put("storage", new Quantity(workspacesPvcQuantity));
    Map<String, String> annotations =
        Collections.singletonMap(
            OPENSHIFT_VOLUME_STORAGE_CLASS, OPENSHIFT_VOLUME_STORAGE_CLASS_NAME);
    PersistentVolumeClaim pvc =
        new PersistentVolumeClaimBuilder()
            .withNewMetadata()
            .withName(workspacesPersistentVolumeClaim)
            .withAnnotations(annotations)
            .endMetadata()
            .withNewSpec()
            .withAccessModes("ReadWriteOnce")
            .withNewResources()
            .withRequests(requests)
            .endResources()
            .endSpec()
            .build();
    pvc =
        kubeClient
            .persistentVolumeClaims()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .create(pvc);
    LOG.info("Creating OpenShift PVC {}", pvc.getMetadata().getName());
    return pvc;
  }

  private String waitAndRetrieveContainerID(String deploymentName) throws IOException {
    DefaultKubernetesClient kubeClient =
        ocFactory.newKubeClient(
            openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftConfig());
    for (int i = 0; i < OPENSHIFT_WAIT_POD_TIMEOUT; i++) {
      try {
        Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }

      List<Pod> pods =
          kubeClient
              .pods()
              .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
              .withLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName)
              .list()
              .getItems();

      if (pods.size() < 1) {
        throw new OpenShiftException(
            String.format("Pod with deployment name %s not found", deploymentName));
      } else if (pods.size() > 1) {
        throw new OpenShiftException(
            String.format("Multiple pods with deployment name %s found", deploymentName));
      }

      Pod pod = pods.get(0);
      String status = pod.getStatus().getPhase();
      if (OPENSHIFT_POD_STATUS_RUNNING.equals(status)) {
        String containerID = pod.getStatus().getContainerStatuses().get(0).getContainerID();
        String normalizedID = KubernetesStringUtils.normalizeContainerID(containerID);
        kubeClient
            .pods()
            .inNamespace(openshiftWorkspaceEnvironmentProvider.getWorkspacesOpenshiftNamespace())
            .withName(pod.getMetadata().getName())
            .edit()
            .editMetadata()
            .addToLabels(
                CHE_CONTAINER_IDENTIFIER_LABEL_KEY,
                KubernetesStringUtils.getLabelFromContainerID(normalizedID))
            .endMetadata()
            .done();
        return normalizedID;
      }
    }
    return null;
  }

  /**
   * Adds OpenShift liveness probe to the container. Liveness probe is configured via TCP Socket
   * Check - for dev machines by checking Workspace API agent port (4401), for non-dev by checking
   * Terminal port (4411)
   *
   * @param exposedPorts
   * @see <a href=
   *     "https://docs.openshift.com/enterprise/3.0/dev_guide/application_health.html">OpenShift
   *     Application Health</a>
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
          .withInitialDelaySeconds(openShiftLivenessProbeDelay)
          .withTimeoutSeconds(openShiftLivenessProbeTimeout)
          .build();
    }

    return null;
  }

  private Map<String, List<PortBinding>> getCheServicePorts(Service service) {
    Map<String, List<PortBinding>> networkSettingsPorts = new HashMap<>();
    List<ServicePort> servicePorts = service.getSpec().getPorts();
    LOG.info(
        "Retrieving {} ports exposed by service {}",
        servicePorts.size(),
        service.getMetadata().getName());
    for (ServicePort servicePort : servicePorts) {
      String protocol = servicePort.getProtocol();
      String targetPort = String.valueOf(servicePort.getTargetPort().getIntVal());
      String nodePort = String.valueOf(servicePort.getNodePort());
      String portName = servicePort.getName();

      LOG.info("Port: {}{}{} ({})", targetPort, DOCKER_PROTOCOL_PORT_DELIMITER, protocol, portName);

      networkSettingsPorts.put(
          targetPort + DOCKER_PROTOCOL_PORT_DELIMITER + protocol.toLowerCase(),
          Collections.singletonList(
              new PortBinding().withHostIp(CHE_DEFAULT_EXTERNAL_ADDRESS).withHostPort(nodePort)));
    }
    return networkSettingsPorts;
  }

  /**
   * @param exposedPorts
   * @return true if machine exposes 4411/tcp port used by Terminal agent, false otherwise
   */
  private boolean isTerminalAgentInjected(final Set<String> exposedPorts) {
    return exposedPorts.contains(CHE_TERMINAL_AGENT_PORT + "/tcp");
  }

  /**
   * @param exposedPorts
   * @return true if machine exposes 4401/tcp port used by Worspace API agent, false otherwise
   */
  private boolean isDevMachine(final Set<String> exposedPorts) {
    return exposedPorts.contains(CHE_WORKSPACE_AGENT_PORT + "/tcp");
  }

  private String getDeploymentName(final RemoveContainerParams params) throws IOException {
    String containerId = params.getContainer();
    Pod pod = getChePodByContainerId(containerId);
    String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
    return deploymentName;
  }
}
