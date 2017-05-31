/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.docker.machine;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.docker.client.json.ContainerInfo;
import org.eclipse.che.plugin.docker.client.json.PortBinding;
import org.stringtemplate.v4.ST;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a server evaluation strategy for the configuration where the strategy can be customized through template properties.
 *
 * @author Florent Benoit
 * @see ServerEvaluationStrategy
 */
public class CustomServerEvaluationStrategy extends DefaultServerEvaluationStrategy {

    /**
     * Regexp to extract port (under the form 22/tcp or 4401/tcp, etc.) from label references
     */
    public static final String LABEL_CHE_SERVER_REF_KEY = "^che:server:(.*):ref$";

    /**
     * Name of the property for getting the workspace ID.
     */
    public static final String CHE_WORKSPACE_ID_PROPERTY = "CHE_WORKSPACE_ID=";

    /**
     * Name of the property to get the machine name property
     */
    public static final String CHE_MACHINE_NAME_PROPERTY = "CHE_MACHINE_NAME=";

    /**
     * The current port of che.
     */
    private final String chePort;

    /**
     * Secured or not ? (for example https vs http)
     */
    private final String cheDockerCustomExternalProtocol;

    /**
     * Template for external addresses.
     */
    private String cheDockerCustomExternalTemplate;


    /**
     * Default constructor
     */
    @Inject
    public CustomServerEvaluationStrategy(@Nullable @Named("che.docker.ip") String cheDockerIp,
                                          @Nullable @Named("che.docker.ip.external") String cheDockerIpExternal,
                                          @Nullable @Named("che.docker.server_evaluation_strategy.custom.template") String cheDockerCustomExternalTemplate,
                                          @Nullable @Named("che.docker.server_evaluation_strategy.custom.external.protocol") String cheDockerCustomExternalProtocol,
                                          @Named("che.port") String chePort) {
        super(cheDockerIp, cheDockerIpExternal);
        this.chePort = chePort;
        this.cheDockerCustomExternalTemplate = cheDockerCustomExternalTemplate;
        this.cheDockerCustomExternalProtocol = cheDockerCustomExternalProtocol;
    }

    /**
     * Override the host for all ports by using the external template.
     */
    @Override
    protected Map<String, String> getExternalAddressesAndPorts(ContainerInfo containerInfo, String internalHost) {

        // create Rendering evaluation
        RenderingEvaluation renderingEvaluation = getOnlineRenderingEvaluation(containerInfo, internalHost);

        // get current ports
        Map<String, List<PortBinding>> ports = containerInfo.getNetworkSettings().getPorts();

        return ports.keySet().stream()
                    .collect(Collectors.toMap(portKey -> portKey,
                                              portKey -> renderingEvaluation.render(cheDockerCustomExternalTemplate, portKey)));
    }


    /**
     * Constructs a map of {@link ServerImpl} from provided parameters, using selected strategy
     * for evaluating addresses and ports.
     *
     * <p>Keys consist of port number and transport protocol (tcp or udp) separated by
     * a forward slash (e.g. 8080/tcp)
     *
     * @param containerInfo
     *         the {@link ContainerInfo} describing the container.
     * @param internalHost
     *         alternative hostname to use, if address cannot be obtained from containerInfo
     * @param serverConfMap
     *         additional Map of {@link ServerConfImpl}. Configurations here override those found
     *         in containerInfo.
     * @return a Map of the servers exposed by the container.
     */
    public Map<String, ServerImpl> getServers(ContainerInfo containerInfo,
                                              String internalHost,
                                              Map<String, ServerConfImpl> serverConfMap) {
            Map<String, ServerImpl> servers = super.getServers(containerInfo, internalHost, serverConfMap);
            return servers.entrySet().stream().collect(Collectors.toMap(map -> map.getKey(), map -> updateServer(map.getValue())));
    }


    /**
     * Updates the protocol for the given server by using given protocol (like https) for http URLs.
     * @param server the server to update
     * @return updated server object
     */
    protected ServerImpl updateServer(ServerImpl server) {
        if (!Strings.isNullOrEmpty(cheDockerCustomExternalProtocol)) {
            if ("http".equals(server.getProtocol())) {
                server.setProtocol(cheDockerCustomExternalProtocol);
                String url = server.getUrl();
                int length = "http".length();
                server.setUrl(cheDockerCustomExternalProtocol.concat(url.substring(length)));
            }
        }
        return server;
    }


    /**
     * Allow to get the rendering outside of the evaluation strategies.
     * It is called online as in this case we have access to container info
     */
    public RenderingEvaluation getOnlineRenderingEvaluation(ContainerInfo containerInfo, String internalHost) {
        return new OnlineRenderingEvaluation(containerInfo).withInternalHost(internalHost);
    }

    /**
     * Allow to get the rendering outside of the evaluation strategies.
     * It is called offline as without container info, user need to provide merge of container and images data
     */
    public RenderingEvaluation getOfflineRenderingEvaluation(Map<String, String> labels, Set<String> exposedPorts, String[] env) {
        return new OfflineRenderingEvaluation(labels, exposedPorts, env);
    }

    /**
     * Simple interface for performing the rendering for a given portby using the given template
     *
     * @author Florent Benoit
     */
    public interface RenderingEvaluation {
        /**
         * Gets the template rendering for the given port and using the given template
         *
         * @param template
         *         which can include <propertyName></propertyName>
         * @param port
         *         the port for the mapping
         * @return the rendering of the template
         */
        String render(String template, String port);
    }

    /**
     * Online implementation (using the container info)
     */
    protected class OnlineRenderingEvaluation extends OfflineRenderingEvaluation implements RenderingEvaluation {

        private String gatewayAddressContainer;
        private String internalHost;

        protected OnlineRenderingEvaluation(ContainerInfo containerInfo) {
            super(containerInfo.getConfig().getLabels(), containerInfo.getConfig().getExposedPorts().keySet(),
                  containerInfo.getConfig().getEnv());
            this.gatewayAddressContainer = containerInfo.getNetworkSettings().getGateway();
        }

        protected OnlineRenderingEvaluation withInternalHost(String internalHost) {
            this.internalHost = internalHost;
            return this;
        }

        @Override
        protected String getExternalAddress() {
            return externalAddressProperty != null ?
                   externalAddressProperty :
                   !isNullOrEmpty(gatewayAddressContainer) ?
                   gatewayAddressContainer :
                   this.internalHost;
        }
    }

    /**
     * Offline implementation (container not yet created)
     */
    protected class OfflineRenderingEvaluation extends DefaultRenderingEvaluation implements RenderingEvaluation {

        public OfflineRenderingEvaluation(Map<String, String> labels, Set<String> exposedPorts, String[] env) {
            super(labels, exposedPorts, env);
        }
    }

    /**
     * Inner class used to perform the rendering
     */
    protected abstract class DefaultRenderingEvaluation implements RenderingEvaluation {

        /**
         * Labels
         */
        private Map<String, String> labels;

        /**
         * Ports
         */
        private Set<String> exposedPorts;

        /**
         * Environment variables
         */
        private final String[] env;

        /**
         * Map with properties for all ports
         */
        private Map<String, String> globalPropertiesMap = new HashMap<>();

        /**
         * Mapping between a port and the server ref name
         */
        private Map<String, String> portsToRefName;

        /**
         * Data initialized ?
         */
        private boolean initialized;

        /**
         * Default constructor.
         */
        protected DefaultRenderingEvaluation(Map<String, String> labels, Set<String> exposedPorts, String[] env) {
            this.labels = labels;
            this.exposedPorts = exposedPorts;
            this.env = env;
        }

        /**
         * Initialize data
         */
        protected void init() {
            this.initPortMapping();
            this.populateGlobalProperties();
        }

        /**
         * Compute port mapping with server ref name
         */
        protected void initPortMapping() {
            // ok, so now we have a map of labels and a map of exposed ports
            // need to extract the name of the ref (if defined in a label) or then pickup default name "Server-<port>-<protocol>"
            Pattern pattern = Pattern.compile(LABEL_CHE_SERVER_REF_KEY);
            Map<String, String> portsToKnownRefName = labels.entrySet().stream()
                                                            .filter(map -> pattern.matcher(map.getKey()).matches())
                                                            .collect(Collectors.toMap(p -> {
                                                                Matcher matcher = pattern.matcher(p.getKey());
                                                                matcher.matches();
                                                                String val = matcher.group(1);
                                                                return val.contains("/") ? val : val.concat("/tcp");
                                                            }, p -> p.getValue()));

            // add to this map only port without a known ref name
            Map<String, String> portsToUnkownRefName =
                    exposedPorts.stream().filter((port) -> !portsToKnownRefName.containsKey(port))
                                .collect(Collectors.toMap(p -> p, p -> "Server-" + p.replace('/', '-')));

            // list of all ports with refName (known/unknown)
            this.portsToRefName = new HashMap(portsToKnownRefName);
            portsToRefName.putAll(portsToUnkownRefName);
        }

        /**
         * Gets default external address.
         */
        protected String getExternalAddress() {
            return externalAddressProperty != null ?
                   externalAddressProperty : internalAddressProperty;
        }

        /**
         * Populate the template properties
         */
        protected void populateGlobalProperties() {
            String externalAddress = getExternalAddress();
            String externalIP = getExternalIp(externalAddress);
            globalPropertiesMap.put("internalIp", internalAddressProperty);
            globalPropertiesMap.put("externalAddress", externalAddress);
            globalPropertiesMap.put("externalIP", externalIP);
            globalPropertiesMap.put("workspaceId", getWorkspaceId());
            globalPropertiesMap.put("machineName", getMachineName());
            globalPropertiesMap.put("wildcardNipDomain", getWildcardNipDomain(externalAddress));
            globalPropertiesMap.put("wildcardXipDomain", getWildcardXipDomain(externalAddress));
            globalPropertiesMap.put("chePort", chePort);
        }

        /**
         * Rendering
         */
        @Override
        public String render(String template, String port) {
            if (!this.initialized) {
                init();
                this.initialized = true;
            }
            ST stringTemplate = new ST(template);
            globalPropertiesMap.forEach((key, value) -> stringTemplate.add(key, value));
            stringTemplate.add("serverName", portsToRefName.get(port));
            return stringTemplate.render();
        }

        /**
         * Gets the workspace ID from the config of the given container
         *
         * @return workspace ID
         */
        protected String getWorkspaceId() {
            return Arrays.stream(env).filter(env -> env.startsWith(CHE_WORKSPACE_ID_PROPERTY))
                         .map(s -> s.substring(CHE_WORKSPACE_ID_PROPERTY.length()))
                         .findFirst().get();
        }

        /**
         * Gets the workspace Machine Name from the config of the given container
         *
         * @return machine name of the workspace
         */
        protected String getMachineName() {
            return Arrays.stream(env).filter(env -> env.startsWith(CHE_MACHINE_NAME_PROPERTY))
                         .map(s -> s.substring(CHE_MACHINE_NAME_PROPERTY.length()))
                         .findFirst().get();
        }

        /**
         * Gets the IP address of the external address
         *
         * @return IP Address
         */
        protected String getExternalIp(String externalAddress) {
            try {
                return InetAddress.getByName(externalAddress).getHostAddress();
            } catch (UnknownHostException e) {
                throw new UnsupportedOperationException("Unable to find the IP for the address '" + externalAddress + "'", e);
            }
        }

        /**
         * Gets a Wildcard domain based on the ip using an external provider nip.io
         *
         * @return wildcard domain
         */
        protected String getWildcardNipDomain(String externalAddress) {
            return String.format("%s.%s", getExternalIp(externalAddress), "nip.io");
        }

        /**
         * Gets a Wildcard domain based on the ip using an external provider xip.io
         *
         * @return wildcard domain
         */
        protected String getWildcardXipDomain(String externalAddress) {
            return String.format("%s.%s", getExternalIp(externalAddress), "xip.io");
        }

    }

}
