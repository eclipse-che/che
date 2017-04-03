/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.Exec;
import org.eclipse.che.plugin.docker.client.LogMessage;
import org.eclipse.che.plugin.docker.client.MessageProcessor;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.json.Event;
import org.eclipse.che.plugin.docker.client.json.Filters;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkCreated;
import org.eclipse.che.plugin.docker.client.json.NetworkSettings;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.json.network.ContainerInNetwork;
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
import org.eclipse.che.plugin.docker.client.params.RemoveNetworkParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StartExecParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.DisconnectContainerFromNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.GetNetworksParams;
import org.eclipse.che.plugin.docker.client.params.network.InspectNetworkParams;
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

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
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
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.utils.InputStreamPumper;
import io.fabric8.openshift.api.model.Image;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

/**
 * Client for OpenShift API.
 *
 * @author Mario Loriedo (mloriedo@redhat.com)
 * @author Angel Misevski (amisevsk@redhat.com)
 * @author Ilya Buziuk (ibuziuk@redhat.com)
 */
@Singleton
public class OpenShiftConnector extends DockerConnector {
    private static final Logger LOG                                      = LoggerFactory.getLogger(OpenShiftConnector.class);
    private static final String CHE_CONTAINER_IDENTIFIER_LABEL_KEY       = "cheContainerIdentifier";
    private static final String CHE_DEFAULT_EXTERNAL_ADDRESS             = "172.17.0.1";
    private static final String CHE_OPENSHIFT_RESOURCES_PREFIX           = "che-ws-";
    private static final String CHE_WORKSPACE_ID_ENV_VAR                 = "CHE_WORKSPACE_ID";
    private static final int CHE_WORKSPACE_AGENT_PORT                    = 4401;
    private static final int CHE_TERMINAL_AGENT_PORT                     = 4411;
    private static final String DOCKER_PROTOCOL_PORT_DELIMITER           = "/";
    private static final int OPENSHIFT_WAIT_POD_DELAY                    = 1000;
    private static final int OPENSHIFT_WAIT_POD_TIMEOUT                  = 240;
    private static final int OPENSHIFT_IMAGESTREAM_WAIT_DELAY            = 2000;
    private static final int OPENSHIFT_IMAGESTREAM_MAX_WAIT_COUNT        = 30;
    private static final String OPENSHIFT_POD_STATUS_RUNNING             = "Running";
    private static final String OPENSHIFT_DEPLOYMENT_LABEL               = "deployment";
    private static final String OPENSHIFT_VOLUME_STORAGE_CLASS           = "volume.beta.kubernetes.io/storage-class";
    private static final String OPENSHIFT_VOLUME_STORAGE_CLASS_NAME      = "che-workspace";
    private static final String OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT = "IfNotPresent";

    private Map<String, KubernetesExecHolder> execMap = new HashMap<>();

    private final OpenShiftClient openShiftClient;
    private final String          openShiftCheProjectName;
    private final int             openShiftLivenessProbeDelay;
    private final int             openShiftLivenessProbeTimeout;
    private final String          workspacesPersistentVolumeClaim;
    private final String          workspacesPvcQuantity;
    private final String          cheWorkspaceStorage;
    private final String          cheWorkspaceProjectsStorage;
    private final String          cheServerExternalAddress;
    private final String          cheWorkspaceMemoryLimit;
    private final String          cheWorkspaceMemoryRequest;

    @Inject
    public OpenShiftConnector(DockerConnectorConfiguration connectorConfiguration,
                              DockerConnectionFactory connectionFactory,
                              DockerRegistryAuthResolver authResolver,
                              DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider,
                              @Nullable @Named("che.docker.ip.external") String cheServerExternalAddress,
                              @Named("che.openshift.project") String openShiftCheProjectName,
                              @Named("che.openshift.liveness.probe.delay") int openShiftLivenessProbeDelay,
                              @Named("che.openshift.liveness.probe.timeout") int openShiftLivenessProbeTimeout,
                              @Named("che.openshift.workspaces.pvc.name") String workspacesPersistentVolumeClaim,
                              @Named("che.openshift.workspaces.pvc.quantity") String workspacesPvcQuantity,
                              @Named("che.workspace.storage") String cheWorkspaceStorage,
                              @Named("che.workspace.projects.storage") String cheWorkspaceProjectsStorage,
                              @Nullable @Named("che.openshift.workspace.memory.request") String cheWorkspaceMemoryRequest,
                              @Nullable @Named("che.openshift.workspace.memory.override") String cheWorkspaceMemoryLimit) {


        super(connectorConfiguration, connectionFactory, authResolver, dockerApiVersionPathPrefixProvider);
        this.cheServerExternalAddress = cheServerExternalAddress;
        this.openShiftCheProjectName = openShiftCheProjectName;
        this.openShiftLivenessProbeDelay = openShiftLivenessProbeDelay;
        this.openShiftLivenessProbeTimeout = openShiftLivenessProbeTimeout;
        this.workspacesPersistentVolumeClaim = workspacesPersistentVolumeClaim;
        this.workspacesPvcQuantity = workspacesPvcQuantity;
        this.cheWorkspaceStorage = cheWorkspaceStorage;
        this.cheWorkspaceProjectsStorage = cheWorkspaceProjectsStorage;
        this.cheWorkspaceMemoryRequest = cheWorkspaceMemoryRequest;
        this.cheWorkspaceMemoryLimit = cheWorkspaceMemoryLimit;

        this.openShiftClient = new DefaultOpenShiftClient();
    }

    /**
     * @param createContainerParams
     * @return
     * @throws IOException
     */
    @Override
    public ContainerCreated createContainer(CreateContainerParams createContainerParams) throws IOException {
        String containerName = KubernetesStringUtils.convertToContainerName(createContainerParams.getContainerName());
        String workspaceID = getCheWorkspaceId(createContainerParams);

        // Generate workspaceID if CHE_WORKSPACE_ID env var does not exist
        workspaceID = workspaceID.isEmpty() ? KubernetesStringUtils.generateWorkspaceID() : workspaceID;

        // imageForDocker is the docker version of the image repository. It's needed for other
        // OpenShiftConnector API methods, but is not acceptable as an OpenShift name
        String imageForDocker = createContainerParams.getContainerConfig().getImage();
        // imageStreamTagName is imageForDocker converted into a form that can be used
        // in OpenShift
        String imageStreamTagName = KubernetesStringUtils.convertPullSpecToTagName(imageForDocker);

        // imageStreamTagName is not enough to fill out a pull spec; it is only the tag, so we
        // have to get the ImageStreamTag from the tag, and then get the full ImageStreamTag name
        // from that tag. This works because the tags used in Che are unique.
        ImageStreamTag imageStreamTag = getImageStreamTagFromRepo(imageStreamTagName);
        String imageStreamTagPullSpec = imageStreamTag.getMetadata().getName();

        // Next we need to get the address of the registry where the ImageStreamTag is stored
        String imageStreamName = KubernetesStringUtils.getImageStreamNameFromPullSpec(imageStreamTagPullSpec);

        ImageStream imageStream = openShiftClient.imageStreams()
                                                 .inNamespace(openShiftCheProjectName)
                                                 .withName(imageStreamName)
                                                 .get();
        if (imageStream == null) {
            throw new OpenShiftException("ImageStream not found");
        }
        String registryAddress = imageStream.getStatus()
                                            .getDockerImageRepository()
                                            .split("/")[0];

        // The above needs to be combined to form a pull spec that will work when defining a container.
        String dockerPullSpec = String.format("%s/%s/%s", registryAddress,
                                                          openShiftCheProjectName,
                                                          imageStreamTagPullSpec);

        Set<String> containerExposedPorts = createContainerParams.getContainerConfig().getExposedPorts().keySet();
        Set<String> imageExposedPorts = inspectImage(InspectImageParams.create(imageForDocker))
                                                    .getConfig().getExposedPorts().keySet();
        Set<String> exposedPorts = getExposedPorts(containerExposedPorts, imageExposedPorts);

        String[] envVariables = createContainerParams.getContainerConfig().getEnv();
        String[] volumes = createContainerParams.getContainerConfig().getHostConfig().getBinds();

        Map<String, String> additionalLabels = createContainerParams.getContainerConfig().getLabels();

        Map<String, Quantity> resourceLimits = new HashMap<>();
        if (!isNullOrEmpty(cheWorkspaceMemoryLimit)) {
            LOG.info("Che property 'che.openshift.workspace.memory.override' "
                   + "used to override workspace memory limit to {}.", cheWorkspaceMemoryLimit);
            resourceLimits.put("memory", new Quantity(cheWorkspaceMemoryLimit));
        } else {
            long memoryLimitBytes = createContainerParams.getContainerConfig().getHostConfig().getMemory();
            String memoryLimit = Long.toString(memoryLimitBytes / 1048576) + "Mi";
            LOG.info("Creating workspace pod with memory limit of {}.", memoryLimit);
            resourceLimits.put("memory", new Quantity(cheWorkspaceMemoryLimit));
        }

        Map<String, Quantity> resourceRequests = new HashMap<>();
        if (!isNullOrEmpty(cheWorkspaceMemoryRequest)) {
            resourceRequests.put("memory", new Quantity(cheWorkspaceMemoryRequest));
        }

        String containerID;
        try {
            createOpenShiftService(workspaceID, exposedPorts, additionalLabels);
            String deploymentName = createOpenShiftDeployment(workspaceID,
                                                              dockerPullSpec,
                                                              containerName,
                                                              exposedPorts,
                                                              envVariables,
                                                              volumes,
                                                              resourceLimits,
                                                              resourceRequests);

            containerID = waitAndRetrieveContainerID(deploymentName);
            if (containerID == null) {
                throw new OpenShiftException("Failed to get the ID of the container running in the OpenShift pod");
            }
        } catch (IOException | KubernetesClientException e) {
            // Make sure we clean up deployment and service in case of an error -- otherwise Che can end up
            // in an inconsistent state.
            LOG.info("Error while creating Pod, removing deployment");
            LOG.info(e.getMessage());
            String deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
            cleanUpWorkspaceResources(deploymentName);
            openShiftClient.resource(imageStreamTag).delete();
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
        throw new UnsupportedOperationException("'getResource' is currently not supported by OpenShift");
    }

    @Override
    public void putResource(PutResourceParams params) throws IOException {
        throw new UnsupportedOperationException("'putResource' is currently not supported by OpenShift");
    }

    @Override
    public ContainerInfo inspectContainer(String containerId) throws IOException {

        Pod pod = getChePodByContainerId(containerId);
        if (pod == null ) {
            LOG.warn("No Pod found by container ID {}", containerId);
            return null;
        }

        List<Container> podContainers = pod.getSpec().getContainers();
        if (podContainers.size() > 1) {
            throw new OpenShiftException("Multiple Containers found in Pod.");
        } else if (podContainers.size() < 1 || isNullOrEmpty(podContainers.get(0).getImage())) {
            throw new OpenShiftException(String.format("Container %s not found", containerId));
        }
        String podPullSpec = podContainers.get(0).getImage();

        String tagName = KubernetesStringUtils.getTagNameFromPullSpec(podPullSpec);

        ImageStreamTag tag = getImageStreamTagFromRepo(tagName);
        ImageInfo imageInfo = getImageInfoFromTag(tag);

        String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
        if (deploymentName == null ) {
            LOG.warn("No label {} found for Pod {}", OPENSHIFT_DEPLOYMENT_LABEL, pod.getMetadata().getName());
            return null;
        }

        Service svc = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
        if (svc == null) {
            LOG.warn("No Service found by selector {}={}", OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
            return null;
        }

        return createContainerInfo(svc, imageInfo, pod, containerId);
    }

    @Override
    public void removeContainer(final RemoveContainerParams params) throws IOException {
        String containerId = params.getContainer();
        Pod pod = getChePodByContainerId(containerId);
        String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);
        cleanUpWorkspaceResources(deploymentName);
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
    public void disconnectContainerFromNetwork(DisconnectContainerFromNetworkParams params) throws IOException {
        // Not needed in OpenShift
    }

    @Override
    public Network inspectNetwork(String netId) throws IOException {
        return inspectNetwork(InspectNetworkParams.create(netId));
    }

    @Override
    public Network inspectNetwork(InspectNetworkParams params) throws IOException {
        String netId = params.getNetworkId();

        ServiceList services = openShiftClient.services()
                                              .inNamespace(this.openShiftCheProjectName)
                                              .list();
        Map<String, ContainerInNetwork> containers = new HashMap<>();
        for (Service svc : services.getItems()) {
            String selector = svc.getSpec().getSelector().get(OPENSHIFT_DEPLOYMENT_LABEL);
            if (selector == null || !selector.startsWith(CHE_OPENSHIFT_RESOURCES_PREFIX)) {
                continue;
            }

            PodList pods = openShiftClient.pods()
                                          .inNamespace(openShiftCheProjectName)
                                          .withLabel(OPENSHIFT_DEPLOYMENT_LABEL, selector)
                                          .list();

            for (Pod pod : pods.getItems()) {
                String podName = pod.getMetadata()
                                    .getName();
                ContainerInNetwork container = new ContainerInNetwork().withName(podName)
                                                                       .withIPv4Address(svc.getSpec()
                                                                                           .getClusterIP());
                String podId = KubernetesStringUtils.getLabelFromContainerID(pod.getMetadata()
                                                                                .getLabels()
                                                                                .get(CHE_CONTAINER_IDENTIFIER_LABEL_KEY));
                if (podId == null) {
                    continue;
                }
                containers.put(podId, container);
            }
        }

        List<IpamConfig> ipamConfig = new ArrayList<>();
        Ipam ipam = new Ipam().withDriver("bridge")
                              .withOptions(Collections.emptyMap())
                              .withConfig(ipamConfig);

        return new Network().withName("OpenShift")
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
     * In OpenShift, there is only one network in the Docker sense, and it is similar
     * to the default bridge network. Rather than implementing all of the filters
     * available in the Docker API, we only implement {@code type=["custom"|"builtin"]}.
     *
     * <p> If type is "custom", null is returned. Otherwise, the default network is returned,
     * and the result is effectively the same as {@link DockerConnector#inspectNetwork(String)}
     * where the network is "bridge".
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
     * repository, but creating the ImageStream is necessary as it is used to obtain
     * the address of the internal Docker registry later.
     *
     * @see DockerConnector#pull(PullParams, ProgressMonitor)
     */
    @Override
    public void pull(final PullParams params, final ProgressMonitor progressMonitor) throws IOException {

        String repo = params.getFullRepo();     // image to be pulled
        String tag = params.getTag();           // e.g. latest, usually

        String imageStreamName = KubernetesStringUtils.convertPullSpecToImageStreamName(repo);

        ImageStream existingImageStream = openShiftClient.imageStreams()
                                                         .inNamespace(openShiftCheProjectName)
                                                         .withName(imageStreamName)
                                                         .get();
        if (existingImageStream == null) {
            openShiftClient.imageStreams()
                           .inNamespace(openShiftCheProjectName)
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

            createdImageStream = openShiftClient.imageStreams()
                                                .inNamespace(openShiftCheProjectName)
                                                .withName(imageStreamName)
                                                .get();

            if (createdImageStream != null
                    && createdImageStream.getStatus().getDockerImageRepository() != null) {
                LOG.info(String.format("Created ImageStream %s.", imageStreamName));
                return;
            }
        }

        throw new OpenShiftException(String.format("Failed to create ImageStream %s.",
                                                   imageStreamName));
    }

    /**
     * Creates an ImageStreamTag that tracks a given image.
     *
     * <p> Docker tags are used extensively in Che: all workspaces run on tagged images
     * tracking built stacks. For new workspaces, or when snapshots are not used, the
     * tracked image is e.g. {@code eclipse/ubuntu_jdk8}, whereas for snapshotted workspaces,
     * the tracked image is the snapshot (e.g. {@code machine_snapshot-<identifier>}.
     *
     * <p> Since OpenShift does not support the same tagging functionality as Docker,
     * tags are implemented as ImageStreamTags, where the {@code From} field is always
     * the original image, and the ImageStreamTag name is derived from both the source
     * image and the target image. This replicates functionality for Che in Docker,
     * while working differently under the hood. The ImageStream name is derived from
     * the image that is being tracked (e.g. {@code eclipse/ubuntu_jdk8}), while the tag
     * name is derived from the target image (e.g. {@code eclipse-che/che_workspace<identifier>}).
     *
     * @see DockerConnector#tag(TagParams)
     */
    @Override
    public void tag(final TagParams params) throws IOException {
        // E.g. `docker tag sourceImage targetImage`
        String paramsSourceImage = params.getImage();  // e.g. eclipse/ubuntu_jdk8
        String targetImage = params.getRepository();   // e.g. eclipse-che/<identifier>
        String paramsTag = params.getTag();

        String sourceImage = KubernetesStringUtils.stripTagFromPullSpec(paramsSourceImage);
        String tag         = KubernetesStringUtils.getTagNameFromPullSpec(paramsSourceImage);
        if (isNullOrEmpty(tag)) {
            tag = !isNullOrEmpty(paramsTag) ? paramsTag : "latest";
        }

        String sourceImageWithTag;
        // Check if sourceImage matches existing imageStreamTag (e.g. when tagging a snapshot)
        try {
            String sourceImageTagName = KubernetesStringUtils.convertPullSpecToTagName(sourceImage);
            ImageStreamTag existingTag = getImageStreamTagFromRepo(sourceImageTagName);
            sourceImageWithTag = existingTag.getTag().getFrom().getName();
        } catch (IOException e) {
            // Image not found.
            sourceImageWithTag = String.format("%s:%s", sourceImage, tag);
        }

        String imageStreamTagName = KubernetesStringUtils.createImageStreamTagName(sourceImageWithTag,
                                                                                   targetImage);

        createImageStreamTag(sourceImageWithTag, imageStreamTagName);
    }

    @Override
    public ImageInfo inspectImage(InspectImageParams params) throws IOException {

        String image = KubernetesStringUtils.getImageStreamNameFromPullSpec(params.getImage());

        String imageStreamTagName = KubernetesStringUtils.convertPullSpecToTagName(image);
        ImageStreamTag imageStreamTag = getImageStreamTagFromRepo(imageStreamTagName);

        return getImageInfoFromTag(imageStreamTag);
    }

    @Override
    public void removeImage(final RemoveImageParams params) throws IOException {
        String image = KubernetesStringUtils.getImageStreamNameFromPullSpec(params.getImage());

        String imageStreamTagName = KubernetesStringUtils.convertPullSpecToTagName(image);
        ImageStreamTag imageStreamTag = getImageStreamTagFromRepo(imageStreamTagName);

        openShiftClient.resource(imageStreamTag).delete();
    }

    /**
     * OpenShift does not support taking image snapshots since the underlying assumption
     * is that Pods are largely immutable (and so any snapshot would be identical to the image
     * used to create the pod). Che uses docker commit to create machine snapshots, which are
     * used to restore workspaces. To emulate this functionality in OpenShift, commit
     * actually creates a new ImageStreamTag by calling {@link OpenShiftConnector#tag(TagParams)}
     * named for the snapshot that would be created.
     *
     * @see DockerConnector#commit(CommitParams)
     */
    @Override
    public String commit(final CommitParams params) throws IOException {
        String repo = params.getRepository();     // e.g. machine_snapshot_mdkfmksdfm
        String container = params.getContainer(); // container ID

        Pod pod = getChePodByContainerId(container);
        String image = pod.getSpec().getContainers().get(0).getImage();
        String imageStreamTagName = KubernetesStringUtils.getTagNameFromPullSpec(image);

        ImageStreamTag imageStreamTag = getImageStreamTagFromRepo(imageStreamTagName);
        String sourcePullSpec = imageStreamTag.getTag().getFrom().getName();
        String trackingRepo = KubernetesStringUtils.stripTagFromPullSpec(sourcePullSpec);
        String tag          = KubernetesStringUtils.getTagNameFromPullSpec(sourcePullSpec);

        tag(TagParams.create(trackingRepo, repo).withTag(tag));

        return repo; // Return value not used.
    }

    @Override
    public void getEvents(final GetEventsParams params, MessageProcessor<Event> messageProcessor) {
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
        openShiftClient.events()
                       .inNamespace(openShiftCheProjectName)
                       .watch(eventWatcher);
        try {
            waitForClose.await();
        } catch (InterruptedException e) {
            LOG.error("Thread interrupted while waiting for eventWatcher.");
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void getContainerLogs(final GetContainerLogsParams params, MessageProcessor<LogMessage> containerLogsProcessor)
            throws IOException {
        String container = params.getContainer(); // container ID
        Pod pod = getChePodByContainerId(container);
        if (pod != null) {
            String podName = pod.getMetadata().getName();
            boolean[] ret = new boolean[1];
            ret[0] = false;
            try (LogWatch watchLog = openShiftClient.pods().inNamespace(openShiftCheProjectName).withName(podName)
                    .watchLog()) {
                Watcher<Pod> watcher = new Watcher<Pod>() {

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
                openShiftClient.pods().inNamespace(openShiftCheProjectName).withName(podName).watch(watcher);
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
                // The kubernetes client throws an exception (Pipe not connected) when pod doesn't contain any logs.
                // We can ignore it.
            }
        }
    }

    @Override
    public Exec createExec(final CreateExecParams params) throws IOException {
        String[] command = params.getCmd();
        String containerId = params.getContainer();

        Pod pod = getChePodByContainerId(containerId);
        String podName = pod.getMetadata().getName();

        String execId = KubernetesStringUtils.generateWorkspaceID();
        KubernetesExecHolder execHolder = new KubernetesExecHolder().withCommand(command)
                                                                    .withPod(podName);
        execMap.put(execId, execHolder);

        return new Exec(command, execId);
    }

    @Override
    public void startExec(final StartExecParams params,
                          @Nullable MessageProcessor<LogMessage> execOutputProcessor) throws IOException {
        String execId = params.getExecId();

        KubernetesExecHolder exec = execMap.get(execId);

        String podName = exec.getPod();
        String[] command = exec.getCommand();
        for (int i = 0; i < command.length; i++) {
            command[i] = URLEncoder.encode(command[i], "UTF-8");
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try (ExecWatch watch = openShiftClient.pods()
                                              .inNamespace(openShiftCheProjectName)
                                              .withName(podName)
                                              .redirectingOutput()
                                              .redirectingError()
                                              .exec(command);
             InputStreamPumper outputPump = new InputStreamPumper(watch.getOutput(),
                                                                  new KubernetesOutputAdapter(LogMessage.Type.STDOUT,
                                                                                              execOutputProcessor));
             InputStreamPumper errorPump  = new InputStreamPumper(watch.getError(),
                                                                  new KubernetesOutputAdapter(LogMessage.Type.STDERR,
                                                                                              execOutputProcessor))
        ) {
            Future<?> outFuture = executor.submit(outputPump);
            Future<?> errFuture = executor.submit(errorPump);
            // Short-term worksaround; the Futures above seem to never finish.
            Thread.sleep(2500);
        } catch (KubernetesClientException e) {
            throw new OpenShiftException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            execMap.remove(execId);
            executor.shutdown();
        }
    }

    /**
     * Gets the ImageStreamTag corresponding to a given tag name (i.e. without the repository)
     * @param imageStreamTagName the tag name to search for
     * @return
     * @throws IOException if either no matching tag is found, or there are multiple matches.
     */
    private ImageStreamTag getImageStreamTagFromRepo(String imageStreamTagName) throws IOException {

        // Since repository + tag are limited to 63 chars, it's possible that the entire
        // tag name did not fit, so we have to match a substring.
        String imageTagTrimmed = imageStreamTagName.length() > 30 ? imageStreamTagName.substring(0, 30)
                                                                  : imageStreamTagName;

        // Note: ideally, ImageStreamTags could be identified with a label, but it seems like
        // ImageStreamTags do not support labels.
        List<ImageStreamTag> imageStreams = openShiftClient.imageStreamTags()
                                                           .inNamespace(openShiftCheProjectName)
                                                           .list()
                                                           .getItems();

        // We only get ImageStreamTag names here, since these ImageStreamTags do not include
        // Docker metadata, for some reason.
        List<String> imageStreamTags = imageStreams.stream()
                                                   .filter(e -> e.getMetadata()
                                                                 .getName()
                                                                 .contains(imageTagTrimmed))
                                                   .map(e -> e.getMetadata().getName())
                                                   .collect(Collectors.toList());

        if (imageStreamTags.size() < 1) {
            throw new OpenShiftException(String.format("ImageStreamTag %s not found!", imageStreamTagName));
        } else if (imageStreamTags.size() > 1) {
            throw new OpenShiftException(String.format("Multiple ImageStreamTags found for name %s",
                                                       imageStreamTagName));
        }

        String imageStreamTag = imageStreamTags.get(0);

        // Finally, get the ImageStreamTag, with Docker metadata.
        return openShiftClient.imageStreamTags()
                              .inNamespace(openShiftCheProjectName)
                              .withName(imageStreamTag)
                              .get();
    }

    private Service getCheServiceBySelector(String selectorKey, String selectorValue) {
        ServiceList svcs = openShiftClient.services()
                                .inNamespace(this.openShiftCheProjectName)
                                .list();

        Service svc = svcs.getItems().stream()
                .filter(s->s.getSpec().getSelector().containsKey(selectorKey))
                .filter(s->s.getSpec().getSelector().get(selectorKey).equals(selectorValue)).findAny().orElse(null);

        if (svc == null) {
            LOG.warn("No Service with selector {}={} could be found", selectorKey, selectorValue);
        }

        return svc;
    }

    private Deployment getDeploymentByName(String deploymentName) throws IOException {
        Deployment deployment = openShiftClient
                            .extensions().deployments()
                            .inNamespace(this.openShiftCheProjectName)
                            .withName(deploymentName)
                            .get();
        if (deployment == null) {
            LOG.warn("No Deployment with name {} could be found", deploymentName);
        }
        return deployment;
    }

    private List<Route> getRoutesByLabel(String labelKey, String labelValue) throws IOException {
        RouteList routeList = openShiftClient
                .routes()
                .inNamespace(this.openShiftCheProjectName)
                .withLabel(labelKey, labelValue)
                .list();

        List<Route> items = routeList.getItems();

        if (items.isEmpty()) {
            LOG.warn("No Route with label {}={} could be found", labelKey, labelValue);
            throw new IOException("No Route with label " + labelKey + "=" + labelValue + " could be found");
        }

        return items;
    }

    private Pod getChePodByContainerId(String containerId) throws IOException {
        PodList pods = openShiftClient.pods()
                                    .inNamespace(this.openShiftCheProjectName)
                                    .withLabel(CHE_CONTAINER_IDENTIFIER_LABEL_KEY,
                                               KubernetesStringUtils.getLabelFromContainerID(containerId))
                                    .list();

        List<Pod> items = pods.getItems();

        if (items.isEmpty()) {
            LOG.error("An OpenShift Pod with label {}={} could not be found", CHE_CONTAINER_IDENTIFIER_LABEL_KEY, containerId);
            throw new IOException("An OpenShift Pod with label " + CHE_CONTAINER_IDENTIFIER_LABEL_KEY + "=" + containerId +" could not be found");
        }

        if (items.size() > 1) {
            LOG.error("There are {} pod with label {}={} (just one was expected)", items.size(), CHE_CONTAINER_IDENTIFIER_LABEL_KEY, containerId );
            throw new IOException("There are " + items.size() + " pod with label " + CHE_CONTAINER_IDENTIFIER_LABEL_KEY + "=" + containerId + " (just one was expeced)");
        }

        return items.get(0);
    }

    /**
     * Extracts the ImageInfo stored in an ImageStreamTag. The returned object is the JSON
     * that would be returned by executing {@code docker inspect <image>}, except, due to a quirk
     * in OpenShift's handling of this data, fields except for {@code Config} and {@code ContainerConfig}
     * are null.
     * @param imageStreamTag
     * @return
     */
    private ImageInfo getImageInfoFromTag(ImageStreamTag imageStreamTag) {
        // The DockerImageConfig string here is the JSON that would be returned by a docker inspect image,
        // except that the capitalization is inconsistent, breaking deserialization. Top level elements
        // are lowercased with underscores, while nested elements conform to FieldNamingPolicy.UPPER_CAMEL_CASE.
        // We're only converting the config fields for brevity; this means that other fields are null.
        Image tagImage = imageStreamTag.getImage();
        String dockerImageConfig = tagImage.getDockerImageConfig();

        if (!isNullOrEmpty(dockerImageConfig)) {
            LOG.info("imageStreamTag dockerImageConfig is not empty. Using it to get image info");
            ImageInfo info = GSON.fromJson(dockerImageConfig.replaceFirst("config", "Config")
                                                            .replaceFirst("container_config", "ContainerConfig"),
                                           ImageInfo.class);
            return info;
        } else {
            LOG.info("imageStreamTag dockerImageConfig empty. Using dockerImageMetadata to get image info");
            String dockerImageMetadata = GSON.toJson(tagImage.getAdditionalProperties().get("dockerImageMetadata"));
            ImageInfo info = GSON.fromJson(dockerImageMetadata, ImageInfo.class);
            return info;
        }
    }

    protected String getCheWorkspaceId(CreateContainerParams createContainerParams) {
        Stream<String> env = Arrays.stream(createContainerParams.getContainerConfig().getEnv());
        String workspaceID = env.filter(v -> v.startsWith(CHE_WORKSPACE_ID_ENV_VAR) && v.contains("="))
                                .map(v -> v.split("=",2)[1])
                                .findFirst()
                                .orElse("");
        return workspaceID.replaceFirst("workspace","");
    }

    private void createOpenShiftService(String workspaceID,
                                        Set<String> exposedPorts,
                                        Map<String, String> additionalLabels) {

        String serviceName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;

        Map<String, String> selector = Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, serviceName);
        List<ServicePort> ports = KubernetesService.getServicePortsFrom(exposedPorts);

        Service service = openShiftClient
                .services()
                .inNamespace(this.openShiftCheProjectName)
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
            createOpenShiftRoute(serviceName, port.getName(), workspaceID);
        }
    }

    private void createOpenShiftRoute(String serviceName,
                                      String serverRef,
                                      String workspaceName) {

        String routeName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceName + "." + serverRef;
        if (cheServerExternalAddress == null) {
            throw new IllegalArgumentException("Property che.docker.ip.external must be set when using openshift.");
        }
        String serviceHost = serverRef + "." + workspaceName + "." + this.cheServerExternalAddress;

        Route route = openShiftClient
                .routes()
                .inNamespace(this.openShiftCheProjectName)
                .createNew()
                .withNewMetadata()
                .withName(routeName)
                .addToLabels(OPENSHIFT_DEPLOYMENT_LABEL,serviceName)
                .endMetadata()
                .withNewSpec()
                .withHost(serviceHost)
                .withNewTo()
                    .withKind("Service")
                    .withName(serviceName)
                .endTo()
                .withNewPort()
                    .withNewTargetPort()
                        .withStrVal(serverRef)
                    .endTargetPort()
                .endPort()
                .endSpec()
                .done();

        LOG.info("OpenShift route {} created", route.getMetadata().getName());
    }

    private String createOpenShiftDeployment(String workspaceID,
                                             String imageName,
                                             String sanitizedContainerName,
                                             Set<String> exposedPorts,
                                             String[] envVariables,
                                             String[] volumes,
                                             Map<String, Quantity> resourceLimits,
                                             Map<String, Quantity> resourceRequests) {

        String deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
        LOG.info("Creating OpenShift deployment {}", deploymentName);

        Map<String, String> selector = Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

        LOG.info("Adding container {} to OpenShift deployment {}", sanitizedContainerName, deploymentName);
        String[] command;
        String workspaceDir = getWorkspaceDir(volumes);
        if (workspaceDir != null) {
            command = new String[3];
            command[0] = "sh";
            command[1] = "-c";
            command[2] = "mkdir -p " + workspaceDir + ";tail -f /dev/null";
        } else {
            command = null;
        }
        if (command != null) {
            Deployment deployment = createOpenShiftDeploymentInternal(workspaceID,
                                                                      imageName,
                                                                      sanitizedContainerName,
                                                                      exposedPorts,
                                                                      envVariables,
                                                                      volumes,
                                                                      deploymentName,
                                                                      selector,
                                                                      command,
                                                                      false,
                                                                      resourceLimits,
                                                                      resourceRequests);
            try {
                waitAndRetrieveContainerID(deploymentName);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

            openShiftClient.extensions()
                           .deployments()
                           .inNamespace(this.openShiftCheProjectName)
                           .delete(deployment);
            if (!isDeleted(deploymentName)) {
                LOG.warn("OpenShift deployment {} hasn't been deleted", deploymentName);
            }
        }
        Deployment deployment = createOpenShiftDeploymentInternal(workspaceID,
                                                       imageName,
                                                       sanitizedContainerName,
                                                       exposedPorts,
                                                       envVariables,
                                                       volumes,
                                                       deploymentName,
                                                       selector,
                                                       null,
                                                       true,
                                                       resourceLimits,
                                                       resourceRequests);
        LOG.info("OpenShift deployment {} created", deploymentName);
        return deployment.getMetadata().getName();
    }

    private boolean isDeleted(String deploymentName) {
        for (int i = 0; i < OPENSHIFT_WAIT_POD_TIMEOUT; i++) {
            try {
                Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            List<Pod> pods = openShiftClient.pods()
                                .inNamespace(this.openShiftCheProjectName)
                                .withLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName)
                                .list()
                                .getItems();

            if (pods.size() < 1) {
                return true;
            }
        }
        return false;
    }

    private Deployment createOpenShiftDeploymentInternal(String workspaceID,
                                                         String imageName,
                                                         String sanitizedContainerName,
                                                         Set<String> exposedPorts,
                                                         String[] envVariables,
                                                         String[] volumes,
                                                         String deploymentName,
                                                         Map<String,
                                                         String> selector,
                                                         String[] command,
                                                         boolean withSubpath,
                                                         Map<String, Quantity> resourceLimits,
                                                         Map<String, Quantity> resourceRequests) {

        Container container = new ContainerBuilder()
                                    .withName(sanitizedContainerName)
                                    .withImage(imageName)
                                    .withEnv(KubernetesEnvVar.getEnvFrom(envVariables))
                                    .withPorts(KubernetesContainer.getContainerPortsFrom(exposedPorts))
                                    .withImagePullPolicy(OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT)
                                    .withNewSecurityContext()
                                        .withPrivileged(false)
                                    .endSecurityContext()
                                    .withLivenessProbe(getLivenessProbeFrom(exposedPorts))
                                    .withCommand(command)
                                    .withVolumeMounts(getVolumeMountsFrom(volumes, workspaceID, withSubpath))
                                    .withNewResources()
                                        .withLimits(resourceLimits)
                                        .withRequests(resourceRequests)
                                    .endResources()
                                    .build();

        PodSpec podSpec = new PodSpecBuilder()
                                 .withContainers(container)
                                 .withVolumes(getVolumesFrom(volumes, workspaceID))
                                 .build();

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(deploymentName)
                    .withNamespace(this.openShiftCheProjectName)
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

        deployment = openShiftClient.extensions()
                                    .deployments()
                                    .inNamespace(this.openShiftCheProjectName)
                                    .create(deployment);
        return deployment;
    }

    /**
     * Creates a new ImageStreamTag
     *
     * @param sourceImageWithTag the image that the ImageStreamTag will track
     * @param imageStreamTagName the name of the imageStream tag (e.g. {@code <ImageStream name>:<Tag name>})
     * @return the created ImageStreamTag
     * @throws IOException When {@code sourceImageWithTag} metadata cannot be found
     */
    private ImageStreamTag createImageStreamTag(String sourceImageWithTag,
                                                String imageStreamTagName) throws IOException {
        try {
            openShiftClient.imageStreamTags()
                           .inNamespace(openShiftCheProjectName)
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
                ImageStreamTag createdTag = openShiftClient.imageStreamTags()
                                                           .inNamespace(openShiftCheProjectName)
                                                           .withName(imageStreamTagName)
                                                           .get();
                if (createdTag != null) {
                    LOG.info(String.format("Created ImageStreamTag %s in namespace %s",
                                           createdTag.getMetadata().getName(),
                                           openShiftCheProjectName));
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
     * Collects the relevant information from a Service, an ImageInfo, and a Pod into
     * a docker ContainerInfo JSON object. The returned object is what would be returned
     * by executing {@code docker inspect <container>}, with fields filled as available.
     * @param svc
     * @param imageInfo
     * @param pod
     * @param containerId
     * @return
     */
    private ContainerInfo createContainerInfo(Service svc,
                                              ImageInfo imageInfo,
                                              Pod pod,
                                              String containerId) {

        // In Che on OpenShift, we only have one container per pod.
        Container container = pod.getSpec().getContainers().get(0);
        ContainerConfig imageContainerConfig = imageInfo.getContainerConfig();

        // HostConfig
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new String[0]);

        // Env vars
        List<String> imageEnv = Arrays.asList(imageContainerConfig.getEnv());
        List<String> containerEnv = container.getEnv()
                                             .stream()
                                             .map(e -> String.format("%s=%s", e.getName(), e.getValue()))
                                             .collect(Collectors.toList());
        String[] env = Stream.concat(imageEnv.stream(), containerEnv.stream())
                             .toArray(String[]::new);

        // Exposed Ports
        Map<String, List<PortBinding>> ports = getCheServicePorts(svc);
        Map<String, Map<String, String>> exposedPorts = new HashMap<>();
        for (String key : ports.keySet()) {
            exposedPorts.put(key, Collections.emptyMap());
        }

        // Labels
        Map<String, String> annotations = KubernetesLabelConverter.namesToLabels(svc.getMetadata().getAnnotations());
        Map<String, String> containerLabels = imageInfo.getConfig().getLabels();
        Map<String, String> labels = Stream.concat(annotations.entrySet().stream(), containerLabels.entrySet().stream())
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
        return info;
    }


    private void cleanUpWorkspaceResources(String deploymentName) throws IOException {

        Deployment deployment = getDeploymentByName(deploymentName);
        Service service = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
        List<Route> routes = getRoutesByLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

        if (routes != null) {
            for (Route route: routes) {
                LOG.info("Removing OpenShift Route {}", route.getMetadata().getName());
                openShiftClient.resource(route).delete();
            }
        }

        if (service != null) {
            LOG.info("Removing OpenShift Service {}", service.getMetadata().getName());
            openShiftClient.resource(service).delete();
        }

        if (deployment != null) {
            LOG.info("Removing OpenShift Deployment {}", deployment.getMetadata().getName());
            openShiftClient.resource(deployment).delete();
        }

        // Wait for all pods to terminate before returning.
        try {
            for (int waitCount = 0; waitCount < OPENSHIFT_WAIT_POD_TIMEOUT; waitCount++) {
                List<Pod> pods = openShiftClient.pods()
                                                .inNamespace(openShiftCheProjectName)
                                                .withLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName)
                                                .list()
                                                .getItems();
                if (pods.size() == 0) {
                    return;
                }
                Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("Thread interrupted while cleaning up workspace");
        }

        throw new OpenShiftException("Timeout while waiting for pods to terminate");
    }

    private String getWorkspaceDir(String[] volumes) {
        PersistentVolumeClaim pvc = getClaimCheWorkspace();
        if (pvc != null) {
            for (String volume : volumes) {
                String mountPath = volume.split(":",3)[1];
                if (cheWorkspaceProjectsStorage.equals(mountPath)) {
                    String hostPath = volume.split(":",3)[0];
                    String subPath = hostPath.replace(cheWorkspaceStorage, "");
                    if (subPath.startsWith("/")) {
                        subPath = subPath.substring(1);
                    }
                    return mountPath + "/" + subPath;
                }
            }
        }
        return null;
    }

    private List<VolumeMount> getVolumeMountsFrom(String[] volumes, String workspaceID, boolean withSubPath) {
        List<VolumeMount> vms = new ArrayList<>();
        PersistentVolumeClaim pvc = getClaimCheWorkspace();
        if (pvc != null) {
            for (String volume : volumes) {
                String mountPath = volume.split(":",3)[1];
                if (cheWorkspaceProjectsStorage.equals(mountPath)) {
                    String hostPath = volume.split(":",3)[0];
                    String subPath = null;
                    if (withSubPath) {
                        subPath = hostPath.replace(cheWorkspaceStorage, "");
                        if (subPath.startsWith("/")) {
                            subPath = subPath.substring(1);
                        }
                    }
                    VolumeMount vm = new VolumeMountBuilder()
                            .withMountPath(mountPath)
                            .withName(workspacesPersistentVolumeClaim)
                            .withSubPath(subPath)
                            .build();
                        vms.add(vm);
                }
            }
        } else {
            for (String volume : volumes) {
                String mountPath = volume.split(":",3)[1];
                String volumeName = getVolumeName(volume);
                VolumeMount vm = new VolumeMountBuilder()
                    .withMountPath(mountPath)
                    .withName("ws-" + workspaceID + "-" + volumeName)
                    .build();
                vms.add(vm);
            }
        }
        return vms;
    }

    private List<Volume> getVolumesFrom(String[] volumes, String workspaceID) {
        List<Volume> vs = new ArrayList<>();
        PersistentVolumeClaim pvc = getClaimCheWorkspace();
        if (pvc != null) {
            for (String volume : volumes) {
                String mountPath = volume.split(":",3)[1];
                if (cheWorkspaceProjectsStorage.equals(mountPath)) {
                    PersistentVolumeClaimVolumeSource pvcs = new PersistentVolumeClaimVolumeSourceBuilder()
                        .withClaimName(workspacesPersistentVolumeClaim)
                        .build();
                    Volume v = new VolumeBuilder()
                        .withPersistentVolumeClaim(pvcs)
                        .withName(workspacesPersistentVolumeClaim)
                        .build();
                    vs.add(v);
                }
            }
        } else {
            for (String volume : volumes) {
                String hostPath = volume.split(":",3)[0];
                String volumeName = getVolumeName(volume);

                Volume v = new VolumeBuilder()
                    .withNewHostPath(hostPath)
                    .withName("ws-" + workspaceID + "-" + volumeName)
                    .build();
                vs.add(v);
            }
        }
        return vs;
    }

    private PersistentVolumeClaim getClaimCheWorkspace() {
        PersistentVolumeClaimList pvcList = openShiftClient.persistentVolumeClaims().inNamespace(openShiftCheProjectName).list();
        for(PersistentVolumeClaim pvc: pvcList.getItems()) {
            if (workspacesPersistentVolumeClaim.equals(pvc.getMetadata().getName())) {
                return pvc;
            }
        }
        Map<String, Quantity> requests = new HashMap<>();
        requests.put("storage", new Quantity(workspacesPvcQuantity));
        Map<String, String> annotations = Collections.singletonMap(OPENSHIFT_VOLUME_STORAGE_CLASS, OPENSHIFT_VOLUME_STORAGE_CLASS_NAME);
        PersistentVolumeClaim pvc = new PersistentVolumeClaimBuilder()
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
        pvc = openShiftClient.persistentVolumeClaims().inNamespace(openShiftCheProjectName).create(pvc);
        LOG.info("Creating OpenShift PVC {}", pvc.getMetadata().getName());
        return pvc;
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

    private String waitAndRetrieveContainerID(String deploymentName) throws IOException {
        for (int i = 0; i < OPENSHIFT_WAIT_POD_TIMEOUT; i++) {
            try {
                Thread.sleep(OPENSHIFT_WAIT_POD_DELAY);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

            List<Pod> pods = openShiftClient.pods()
                                .inNamespace(this.openShiftCheProjectName)
                                .withLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName)
                                .list()
                                .getItems();

            if (pods.size() < 1) {
                throw new OpenShiftException(String.format("Pod with deployment name %s not found",
                                                           deploymentName));
            } else if (pods.size() > 1) {
                throw new OpenShiftException(String.format("Multiple pods with deployment name %s found",
                                                           deploymentName));
            }

            Pod pod = pods.get(0);
            String status = pod.getStatus().getPhase();
            if (OPENSHIFT_POD_STATUS_RUNNING.equals(status)) {
                String containerID = pod.getStatus().getContainerStatuses().get(0).getContainerID();
                String normalizedID = KubernetesStringUtils.normalizeContainerID(containerID);
                openShiftClient.pods()
                               .inNamespace(openShiftCheProjectName)
                               .withName(pod.getMetadata().getName())
                               .edit()
                               .editMetadata()
                                   .addToLabels(CHE_CONTAINER_IDENTIFIER_LABEL_KEY,
                                                KubernetesStringUtils.getLabelFromContainerID(normalizedID))
                               .endMetadata()
                               .done();
                return normalizedID;
            }
        }
        return null;
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
                            .withInitialDelaySeconds(openShiftLivenessProbeDelay)
                            .withTimeoutSeconds(openShiftLivenessProbeTimeout)
                            .build();
        }

        return null;
    }

    private Map<String, List<PortBinding>> getCheServicePorts(Service service) {
        Map<String, List<PortBinding>> networkSettingsPorts = new HashMap<>();
        List<ServicePort> servicePorts = service.getSpec().getPorts();
        LOG.info("Retrieving {} ports exposed by service {}",  servicePorts.size(), service.getMetadata().getName());
        for (ServicePort servicePort : servicePorts) {
            String protocol = servicePort.getProtocol();
            String targetPort = String.valueOf(servicePort.getTargetPort().getIntVal());
            String nodePort = String.valueOf(servicePort.getNodePort());
            String portName = servicePort.getName();

            LOG.info("Port: {}{}{} ({})", targetPort, DOCKER_PROTOCOL_PORT_DELIMITER, protocol, portName);

            networkSettingsPorts.put(targetPort + DOCKER_PROTOCOL_PORT_DELIMITER + protocol.toLowerCase(),
                    Collections.singletonList(
                            new PortBinding().withHostIp(CHE_DEFAULT_EXTERNAL_ADDRESS).withHostPort(nodePort)));
        }
        return networkSettingsPorts;
    }

    /**
     * @param containerExposedPorts
     * @param imageExposedPorts
     * @return ports exposed by both image and container
     */
    private Set<String> getExposedPorts(Set<String> containerExposedPorts, Set<String> imageExposedPorts) {
        Set<String> exposedPorts = new HashSet<>();
        exposedPorts.addAll(containerExposedPorts);
        exposedPorts.addAll(imageExposedPorts);
        return exposedPorts;
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
}
