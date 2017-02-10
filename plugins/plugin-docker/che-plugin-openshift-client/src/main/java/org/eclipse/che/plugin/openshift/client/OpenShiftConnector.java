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

import java.io.IOException;
import java.io.InputStream;
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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.DockerApiVersionPathPrefixProvider;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerRegistryAuthResolver;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.connection.DockerConnectionFactory;
import org.eclipse.che.plugin.docker.client.exception.ImageNotFoundException;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.ContainerListEntry;
import org.eclipse.che.plugin.docker.client.json.ImageConfig;
import org.eclipse.che.plugin.docker.client.json.ImageInfo;
import org.eclipse.che.plugin.docker.client.json.NetworkCreated;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.eclipse.che.plugin.docker.client.json.network.ContainerInNetwork;
import org.eclipse.che.plugin.docker.client.json.network.Ipam;
import org.eclipse.che.plugin.docker.client.json.network.IpamConfig;
import org.eclipse.che.plugin.docker.client.json.network.Network;
import org.eclipse.che.plugin.docker.client.params.CreateContainerParams;
import org.eclipse.che.plugin.docker.client.params.GetResourceParams;
import org.eclipse.che.plugin.docker.client.params.KillContainerParams;
import org.eclipse.che.plugin.docker.client.params.PutResourceParams;
import org.eclipse.che.plugin.docker.client.params.RemoveContainerParams;
import org.eclipse.che.plugin.docker.client.params.RemoveNetworkParams;
import org.eclipse.che.plugin.docker.client.params.StartContainerParams;
import org.eclipse.che.plugin.docker.client.params.StopContainerParams;
import org.eclipse.che.plugin.docker.client.params.InspectImageParams;
import org.eclipse.che.plugin.docker.client.params.PullParams;
import org.eclipse.che.plugin.docker.client.params.TagParams;
import org.eclipse.che.plugin.docker.client.params.network.ConnectContainerToNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.CreateNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.DisconnectContainerFromNetworkParams;
import org.eclipse.che.plugin.docker.client.params.network.InspectNetworkParams;
import org.eclipse.che.plugin.openshift.client.exception.OpenShiftException;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesContainer;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesEnvVar;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesLabelConverter;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesService;
import org.eclipse.che.plugin.openshift.client.kubernetes.KubernetesStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
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
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;

import static com.google.common.base.Strings.isNullOrEmpty;

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
    private static final String OPENSHIFT_SERVICE_TYPE_NODE_PORT         = "NodePort";
    private static final int OPENSHIFT_WAIT_POD_DELAY                    = 1000;
    private static final int OPENSHIFT_WAIT_POD_TIMEOUT                  = 240;
    private static final int OPENSHIFT_IMAGESTREAM_MAX_WAIT              = 10; // seconds
    private static final String OPENSHIFT_POD_STATUS_RUNNING             = "Running";
    private static final String OPENSHIFT_DEPLOYMENT_LABEL               = "deployment";
    private static final String OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT = "IfNotPresent";
    private static final Long UID_ROOT                                   = Long.valueOf(0);
    private static final Long UID_USER                                   = Long.valueOf(1000);

    private final OpenShiftClient openShiftClient;
    private final String          openShiftCheProjectName;
    private final String          openShiftCheServiceAccount;
    private final int             openShiftLivenessProbeDelay;
    private final int             openShiftLivenessProbeTimeout;

    @Inject
    public OpenShiftConnector(ConfigBuilder configBuilder,
                              DockerConnectorConfiguration connectorConfiguration,
                              DockerConnectionFactory connectionFactory,
                              DockerRegistryAuthResolver authResolver,
                              DockerApiVersionPathPrefixProvider dockerApiVersionPathPrefixProvider,
                              @Named("che.openshift.endpoint") String openShiftApiEndpoint,
                              @Nullable @Named("che.openshift.token") String openShiftToken,
                              @Nullable @Named("che.openshift.username") String openShiftUserName,
                              @Nullable @Named("che.openshift.password") String openShiftUserPassword,
                              @Named("che.openshift.project") String openShiftCheProjectName,
                              @Named("che.openshift.serviceaccountname") String openShiftCheServiceAccount,
                              @Named("che.openshift.liveness.probe.delay") int openShiftLivenessProbeDelay,
                              @Named("che.openshift.liveness.probe.timeout") int openShiftLivenessProbeTimeout) {

        super(connectorConfiguration, connectionFactory, authResolver, dockerApiVersionPathPrefixProvider);
        this.openShiftCheProjectName = openShiftCheProjectName;
        this.openShiftCheServiceAccount = openShiftCheServiceAccount;
        this.openShiftLivenessProbeDelay = openShiftLivenessProbeDelay;
        this.openShiftLivenessProbeTimeout = openShiftLivenessProbeTimeout;

        Config config = getOpenShiftConfig(configBuilder,
                                           openShiftApiEndpoint,
                                           openShiftToken,
                                           openShiftUserName,
                                           openShiftUserPassword);
        this.openShiftClient = new DefaultOpenShiftClient(config);
    }

    private Config getOpenShiftConfig(ConfigBuilder configBuilder,
                                      String openShiftApiEndpoint,
                                      String openShiftToken,
                                      String openShiftUserName,
                                      String openShiftUserPassword) {
        if (isNullOrEmpty(openShiftToken)) {
            return configBuilder.withMasterUrl(openShiftApiEndpoint)
                    .withUsername(openShiftUserName)
                    .withPassword(openShiftUserPassword).build();
        } else {
            return configBuilder.withMasterUrl(openShiftApiEndpoint)
                    .withOauthToken(openShiftToken)
                    .build();
        }
    }

    /**
     * @param createContainerParams
     * @return
     * @throws IOException
     */
    @Override
    public ContainerCreated createContainer(CreateContainerParams createContainerParams) throws IOException {
        String containerName = KubernetesStringUtils.getContainerName(createContainerParams.getContainerName());
        String workspaceID = getCheWorkspaceId(createContainerParams);

        // Generate workspaceID if CHE_WORKSPACE_ID env var does not exist
        workspaceID = workspaceID.isEmpty() ? generateWorkspaceID() : workspaceID;

        // imageForDocker is the docker version of the image repository. It's needed for other
        // OpenShiftConnector API methods, but is not acceptable as an OpenShift name
        String imageForDocker = createContainerParams.getContainerConfig().getImage();
        // imageStreamTagName is imageForDocker converted into a form that can be used
        // in OpenShift
        String imageStreamTagName = KubernetesStringUtils.getTagNameFromRepoString(imageForDocker);

        // imageStreamTagName is not enough to fill out a pull spec; it is only the tag, so we
        // have to get the ImageStreamTag from the tag, and then get the full ImageStreamTag name
        // from that tag. This works because the tags used in Che are unique.
        ImageStreamTag imageStreamTag = getImageStreamTagFromRepo(imageStreamTagName);
        String imageStreamTagPullSpec = imageStreamTag.getMetadata().getName();

        // Next we need to get the address of the registry where the ImageStreamTag is stored
        String imageStreamName = imageStreamTagPullSpec.contains(":") ? imageStreamTagPullSpec.split(":")[0]
                                                                      : imageStreamTagPullSpec;
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
        Set<String> imageExposedPorts = inspectImage(imageForDocker).getConfig().getExposedPorts().keySet();
        Set<String> exposedPorts = getExposedPorts(containerExposedPorts, imageExposedPorts);

        boolean runContainerAsRoot = runContainerAsRoot(imageForDocker);

        String[] envVariables = createContainerParams.getContainerConfig().getEnv();
        String[] volumes = createContainerParams.getContainerConfig().getHostConfig().getBinds();

        Map<String, String> additionalLabels = createContainerParams.getContainerConfig().getLabels();
        createOpenShiftService(workspaceID, exposedPorts, additionalLabels);
        String deploymentName = createOpenShiftDeployment(workspaceID,
                                                          dockerPullSpec,
                                                          containerName,
                                                          exposedPorts,
                                                          envVariables,
                                                          volumes,
                                                          runContainerAsRoot);

        String containerID = waitAndRetrieveContainerID(deploymentName);
        if (containerID == null) {
            throw new OpenShiftException("Failed to get the ID of the container running in the OpenShift pod");
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

    /**
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
            LOG.warn("No Pod found by container ID {}", info.getId());
            return null;
        }

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

        Map<String, String> annotations = KubernetesLabelConverter.namesToLabels(svc.getMetadata().getAnnotations());
        Map<String, String> containerLabels = info.getConfig().getLabels();

        Map<String, String> labels = Stream.concat(annotations.entrySet().stream(), containerLabels.entrySet().stream())
                                           .filter(e -> e.getKey().startsWith(KubernetesLabelConverter.getCheServerLabelPrefix()))
                                           .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        info.getConfig().setLabels(labels);

        LOG.info("Container labels:");
        info.getConfig().getLabels().entrySet()
                        .stream().forEach(e -> LOG.info("- {}={}", e.getKey(), e.getValue()));

        replaceNetworkSettings(info);

        return info;
    }

    @Override
    public void removeContainer(final RemoveContainerParams params) throws IOException {
        String containerId = params.getContainer();
        Pod pod = getChePodByContainerId(containerId);

        String deploymentName = pod.getMetadata().getLabels().get(OPENSHIFT_DEPLOYMENT_LABEL);

        Deployment deployment = getDeploymentByName(deploymentName);
        ReplicaSet replicaSet = getReplicaSetByLabel(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);
        Service service = getCheServiceBySelector(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

        if (service != null) {
            LOG.info("Removing OpenShift Service {}", service.getMetadata().getName());
            openShiftClient.resource(service).delete();
        }

        if (deployment != null) {
            LOG.info("Removing OpenShift Deployment {}", deployment.getMetadata().getName());
            openShiftClient.resource(deployment).delete();
        }

        if (replicaSet != null) {
            LOG.info("Removing Replica Set {}", replicaSet.getMetadata().getName());
            openShiftClient.resource(replicaSet).delete();
        }

        LOG.info("Removing OpenShift Pod {}", pod.getMetadata().getName());
        openShiftClient.resource(pod).delete();
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

        String imageStreamName = KubernetesStringUtils.getImageStreamName(repo);

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

        // Wait up to 5 seconds for Image metadata to be obtained.
        ImageStream createdImageStream;
        for (int waitcount = 0; waitcount < OPENSHIFT_IMAGESTREAM_MAX_WAIT; waitcount++) {
            try {
                Thread.sleep(1000);
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
     * <p>
     * The target is usually specified e.g. eclipse-che/workspace<unique string>.
     * ImageStreamTags are labelled as {@code <registry>/<namespace>/<ImageStream>:<tag>}
     * The ImageStream name must be unique to the source image (e.g. eclipse_ubuntu_jdk8)
     * otherwise the sourceImage for this ImageStream will be overwritten when
     * another Stack is used. This leaves only the tag to hold the new image name.
     *
     * @see DockerConnector#tag(TagParams)
     */
    @Override
    public void tag(final TagParams params) throws ImageNotFoundException, IOException {
        // E.g. `docker tag oldImageRepo newImageRepo`
        String sourceImage = params.getImage();        // e.g. eclipse/ubuntu_jdk8
        String targetImage = params.getRepository();   // e.g. eclipse-che/<identifier>
        String tag = params.getTag();
        if (tag == null || tag.isEmpty()) {
            tag = "latest";
        }

        String sourceImageWithTag = String.format("%s:%s", sourceImage, tag);
        String newImageStreamName = KubernetesStringUtils.getImageStreamTagName(sourceImage, targetImage);

        try {
            ImageStreamTag imageStreamTag = openShiftClient.imageStreamTags()
                                                           .inNamespace(openShiftCheProjectName)
                                                           .createOrReplaceWithNew()
                                                           .withNewMetadata()
                                                               .withName(newImageStreamName)
                                                           .endMetadata()
                                                           .withNewTag()
                                                               .withNewFrom()
                                                                   .withKind("DockerImage")
                                                                   .withName(sourceImageWithTag)
                                                               .endFrom()
                                                           .endTag()
                                                           .done();

            // Wait up to 5 seconds for image metadata to be pulled
            for (int waitCount = 0; waitCount < OPENSHIFT_IMAGESTREAM_MAX_WAIT; waitCount++) {
                Thread.sleep(1000);

                ImageStreamTag createdTag = openShiftClient.imageStreamTags()
                                                           .inNamespace(openShiftCheProjectName)
                                                           .withName(newImageStreamName)
                                                           .get();
                if (createdTag != null) {
                    LOG.info(String.format("Created ImageStreamTag %s in namespace %s",
                                           imageStreamTag.getMetadata().getName(),
                                           openShiftCheProjectName));
                    return;
                }
            }

            throw new ImageNotFoundException(String.format("Image %s not found.", sourceImageWithTag));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public ImageInfo inspectImage(InspectImageParams params) throws IOException {

        String image = params.getImage();
        String imageStreamTagName = KubernetesStringUtils.getTagNameFromRepoString(image);
        if (imageStreamTagName.contains(":")) { // Image to inspect passed in with tag, which we don't support
            imageStreamTagName = imageStreamTagName.replaceAll(":.*", "");
        }

        ImageStreamTag imageStreamTag = getImageStreamTagFromRepo(imageStreamTagName);

        // The DockerImageConfig string here is the JSON that would be returned by a docker inspect,
        // except that the capitalization is inconsistent, breaking deserialization. Top level elements
        // are lowercased, while nested elements conform to FieldNamingPolicy.UPPER_CAMEL_CASE.
        // We're only converting the config field for brevity; this means that other fields are null.
        String dockerImageConfig = imageStreamTag.getImage().getDockerImageConfig();
        ImageInfo info = GSON.fromJson(dockerImageConfig.replaceFirst("config", "Config"), ImageInfo.class);

        return info;
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

    private ReplicaSet getReplicaSetByLabel(String labelKey, String labelValue) throws IOException {
        ReplicaSetList replicaSetList = openShiftClient
                .extensions().replicaSets()
                .inNamespace(this.openShiftCheProjectName)
                .withLabel(labelKey, labelValue)
                .list();

        List<ReplicaSet> items = replicaSetList.getItems();

        if (items.isEmpty()) {
            LOG.warn("No ReplicaSet with label {}={} could be found", labelKey, labelValue);
            throw new IOException("No ReplicaSet with label " + labelKey + "=" + labelValue + " could be found");
        }

        if (items.size() > 1) {
            LOG.warn("Found more than one ReplicaSet with label {}={}", labelKey, labelValue);
            throw new IOException("Found more than one ReplicaSet with label " + labelValue + "=" + labelValue);
        }

        return items.get(0);
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
            LOG.error("There are {} pod with label {}={} (just one was expeced)", items.size(), CHE_CONTAINER_IDENTIFIER_LABEL_KEY, containerId );
            throw new IOException("There are " + items.size() + " pod with label " + CHE_CONTAINER_IDENTIFIER_LABEL_KEY + "=" + containerId + " (just one was expeced)");
        }

        return items.get(0);
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

        Map<String, String> selector = Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID);
        List<ServicePort> ports = KubernetesService.getServicePortsFrom(exposedPorts);

        Service service = openShiftClient
                .services()
                .inNamespace(this.openShiftCheProjectName)
                .createNew()
                .withNewMetadata()
                    .withName(CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID)
                    .withAnnotations(KubernetesLabelConverter.labelsToNames(additionalLabels))
                .endMetadata()
                .withNewSpec()
                    .withType(OPENSHIFT_SERVICE_TYPE_NODE_PORT)
                    .withSelector(selector)
                    .withPorts(ports)
                .endSpec()
                .done();

        LOG.info("OpenShift service {} created", service.getMetadata().getName());
    }

    private String createOpenShiftDeployment(String workspaceID,
                                             String imageName,
                                             String sanitizedContainerName,
                                             Set<String> exposedPorts,
                                             String[] envVariables,
                                             String[] volumes,
                                             boolean runContainerAsRoot) {

        String deploymentName = CHE_OPENSHIFT_RESOURCES_PREFIX + workspaceID;
        LOG.info("Creating OpenShift deployment {}", deploymentName);

        Map<String, String> selector = Collections.singletonMap(OPENSHIFT_DEPLOYMENT_LABEL, deploymentName);

        LOG.info("Adding container {} to OpenShift deployment {}", sanitizedContainerName, deploymentName);
        Long UID = runContainerAsRoot ? UID_ROOT : UID_USER;
        Container container = new ContainerBuilder()
                                    .withName(sanitizedContainerName)
                                    .withImage(imageName)
                                    .withEnv(KubernetesEnvVar.getEnvFrom(envVariables))
                                    .withPorts(KubernetesContainer.getContainerPortsFrom(exposedPorts))
                                    .withImagePullPolicy(OPENSHIFT_IMAGE_PULL_POLICY_IFNOTPRESENT)
                                    .withNewSecurityContext()
                                        .withRunAsUser(UID)
                                        .withPrivileged(true)
                                    .endSecurityContext()
                                    .withLivenessProbe(getLivenessProbeFrom(exposedPorts))
                                    .withVolumeMounts(getVolumeMountsFrom(volumes, workspaceID))
                                    .build();

        PodSpec podSpec = new PodSpecBuilder()
                                 .withContainers(container)
                                 .withVolumes(getVolumesFrom(volumes, workspaceID))
                                 .withServiceAccountName(this.openShiftCheServiceAccount)
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

        LOG.info("OpenShift deployment {} created", deploymentName);
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

    private void replaceNetworkSettings(ContainerInfo info) throws IOException {
        if (info.getNetworkSettings() == null) {
            return;
        }

        Service service = getCheWorkspaceService();
        Map<String, List<PortBinding>> networkSettingsPorts = getCheServicePorts(service);
        info.getNetworkSettings().setPorts(networkSettingsPorts);
    }

    private Service getCheWorkspaceService() throws IOException {
        ServiceList services = openShiftClient.services().inNamespace(this.openShiftCheProjectName).list();
        // TODO: improve how the service is found (e.g. using a label with the workspaceid)
        Service service = services.getItems().stream()
                .filter(s -> s.getMetadata().getName().startsWith(CHE_OPENSHIFT_RESOURCES_PREFIX))
                .findFirst().orElse(null);

        if (service == null) {
            LOG.error("No service with prefix {} found", CHE_OPENSHIFT_RESOURCES_PREFIX);
            throw new IOException("No service with prefix " + CHE_OPENSHIFT_RESOURCES_PREFIX +" found");
        }

        return service;
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
     * When container is expected to be run as root, user field from {@link ImageConfig} is empty.
     * For non-root user it contains "user" value
     *
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
}
