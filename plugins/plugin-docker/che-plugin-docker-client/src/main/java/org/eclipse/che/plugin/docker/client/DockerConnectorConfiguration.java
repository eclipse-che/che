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
package org.eclipse.che.plugin.docker.client;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.plugin.docker.client.helper.NetworkFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.io.File.separatorChar;

/**
 * @author Alexander Garagatyi
 * @author Florent Benoit
 */
public class DockerConnectorConfiguration {
    /**
     * Default Docker bridge name on Linux.
     */
    protected static final String BRIDGE_LINUX_INTERFACE_NAME = "docker0";

    /**
     * Default network interface name (Linux system).
     */
    protected static final String DEFAULT_LINUX_INTERFACE_NAME = "eth0";

    /**
     * Default ip of docker host (Linux system).
     */
    protected static final String DEFAULT_LINUX_DOCKER_HOST_IP = "172.17.42.1";

    /**
     * Default ip of docker host (Linux system).
     */
    protected static final String DEFAULT_DOCKER_MACHINE_DOCKER_HOST_IP = "192.168.99.1";

    /**
     * Docker host regexp to get the ip address.
     */
    protected static final Pattern HOST_REGEXP_PATTERN = Pattern.compile(".*?//(.*?):.*?");

    /**
     * Pattern allowing to get every IPv4 digit.
     */
    protected static final Pattern IPV4_ADDRESS_PATTERN = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");

    public static final String UNIX_SOCKET_SCHEME = "unix";
    public static final String UNIX_SOCKET_PATH   = "/var/run/docker.sock";
    public static final URI    UNIX_SOCKET_URI    = URI.create(UNIX_SOCKET_SCHEME + "://" + UNIX_SOCKET_PATH);

    /**
     * System variable used to define location of certificates.
     */
    public static final String DOCKER_CERT_PATH_PROPERTY = "DOCKER_CERT_PATH";

    /**
     * System variable used to define if TLS is used or not.
     */
    public static final String DOCKER_TLS_VERIFY_PROPERTY = "DOCKER_TLS_VERIFY";

    /**
     * System variable used to define host of docker.
     */
    public static final String DOCKER_HOST_PROPERTY = "DOCKER_HOST";

    /**
     * Default URL of docker when using Docker Machine.
     */
    public static final URI DEFAULT_DOCKER_MACHINE_URI = URI.create("https://192.168.99.100:2376");

    /**
     * Default of Docker Machine certificates (machine named default)
     */
    public static final String DEFAULT_DOCKER_MACHINE_CERTS_DIR = System.getProperty("user.home")
                                                                  + separatorChar + ".docker"
                                                                  + separatorChar + "machine"
                                                                  + separatorChar + "machines"
                                                                  + separatorChar + "default";


    private static final Logger LOG = LoggerFactory.getLogger(DockerConnectorConfiguration.class);

    @Inject(optional = true)
    @Named("docker.client.daemon_url")
    private URI dockerDaemonUri = dockerDaemonUri();

    @Inject(optional = true)
    @Named("docker.client.certificates_folder")
    private String dockerCertificatesDirectoryPath = dockerMachineCertsDirectoryPath();

    /**
     * Helper used to resolve ip address of the docker host ip from a docker container.
     */
    @Inject
    private NetworkFinder networkFinder;

    @Inject
    private InitialAuthConfig authConfigs;

    @Inject
    public DockerConnectorConfiguration(InitialAuthConfig initialAuthConfig, NetworkFinder networkFinder) {
        this.authConfigs = initialAuthConfig;
        this.networkFinder = networkFinder;
    }

    public DockerConnectorConfiguration(URI dockerDaemonUri,
                                        String dockerCertificatesDirectoryPath,
                                        InitialAuthConfig authConfigs,
                                        NetworkFinder networkFinder) {
        this.authConfigs = authConfigs;
        this.dockerDaemonUri = dockerDaemonUri;
        this.dockerCertificatesDirectoryPath = dockerCertificatesDirectoryPath;
        this.networkFinder = networkFinder;
    }

    /**
     * Tests URI to check if it is URI of UNIX socket
     *
     * @throws NullPointerException if argument is null
     */
    public static boolean isUnixSocketUri(@NotNull  URI uri) {
        Objects.requireNonNull(uri, "Required non-null uri");
        return UNIX_SOCKET_SCHEME.equals(uri.getScheme());
    }

    /**
     * Gets host (or IP) of server where docker is deployed
     *
     * <p>Helps build URI to ports exposed in containers
     */
    public String getDockerHost() {
        return isUnixSocketUri(dockerDaemonUri) ? "localhost" : dockerDaemonUri.getHost();
    }

    /**
     * Gets the Docker host ip address. This is host that can be reached from a docker container.
     *
     * @return docker host IP address
     * @see <a href="https://docs.docker.com/articles/networking/">Docker Networking</a>
     */
    public String getDockerHostIp() {
        return getDockerHostIp(SystemInfo.isLinux(), System.getenv());
    }

    /**
     * Gets URI of docker API used in client of Docker API
     *
     * @see DockerConnector
     */
    public URI getDockerDaemonUri() {
        return dockerDaemonUri;
    }

    /**
     * Gets docker registries authentication configuration set in Che configuration properties
     */
    public InitialAuthConfig getAuthConfigs() {
        return authConfigs;
    }

    /**
     * Gets certificates for connection to encrypted docker API
     *
     * @return Docker certificates to create encrypted connection to docker API
     * or null if connection is not encrypted or certificates path wasn't configured properly
     */
    public DockerCertificates getDockerCertificates() {
        if (dockerCertificatesDirectoryPath == null || !getDockerDaemonUri().getScheme().equals("https")) {
            return null;
        }
        final File dockerCertificatesDirectory = new File(dockerCertificatesDirectoryPath);
        return dockerCertificatesDirectory.isDirectory() ? DockerCertificates.loadFromDirectory(dockerCertificatesDirectory) : null;
    }

    private static URI dockerDaemonUri() {
        return dockerDaemonUri(SystemInfo.isLinux(), System.getenv());
    }

    /**
     * Provides the URI that will be used to connect on docker.
     * It may use extra environment to help to build this URI
     *
     * @param isLinux
     *         if System is running on Linux
     * @param env
     *         should contain System environment
     * @return URI to connect to docker
     */
    protected static URI dockerDaemonUri(final boolean isLinux, @NotNull final Map<String, String> env) {
        // check if have docker variables
        String host = env.get(DOCKER_HOST_PROPERTY);
        if (host != null) {
            // user has defined some properties, use them
            URI userURI;
            try {
                userURI = new URI(host);
            } catch (URISyntaxException e) {
                LOG.error(String.format("Unable to parse environment variable %s with the following value - %s", DOCKER_HOST_PROPERTY, host), e);
                // unable to use given property, fallback to default URL
                return isLinux ? UNIX_SOCKET_URI : DEFAULT_DOCKER_MACHINE_URI;
            }

            // Secure connection ?
            String tls = env.get(DOCKER_TLS_VERIFY_PROPERTY);
            String protocol = "1".equals(tls) ? "https" : "http";

            // build URI
            try {
                return new URI(protocol, null, userURI.getHost(), userURI.getPort(), null, null, null);
            } catch (URISyntaxException e) {
                LOG.error(String.format("Unable to create URI from %s environment variable with value %s " +
                                        "and TLS environment variable %s with value %s",
                                        DOCKER_HOST_PROPERTY, host, DOCKER_TLS_VERIFY_PROPERTY, tls),
                          e);
                // unable to use given property, fallback to default URL
                return DEFAULT_DOCKER_MACHINE_URI;
            }
        }

        return isLinux ? UNIX_SOCKET_URI : DEFAULT_DOCKER_MACHINE_URI;
    }

    /**
     * Provides the IP address that is accessible from the docker container to reach the docker host
     *
     * @param isLinux
     *         if System is running on Linux
     * @param env
     *         should contain System environment
     * @return docker Ip address host to be used from docker container
     */
    protected String getDockerHostIp(final boolean isLinux, @NotNull final Map<String, String> env) {
        if (isLinux) {
            // search "docker0" bridge
            Optional<InetAddress> dockerBridgeInetAddress = networkFinder.getIPAddress(BRIDGE_LINUX_INTERFACE_NAME);
            if (dockerBridgeInetAddress.isPresent()) {
                return dockerBridgeInetAddress.get().getHostAddress();
            }
            // Che server is probably running in a Docker container: get its internal IP
            Optional<InetAddress> cheServerInetAddress = networkFinder.getIPAddress(DEFAULT_LINUX_INTERFACE_NAME);
            if (cheServerInetAddress.isPresent()) {
                return cheServerInetAddress.get().getHostAddress();
            }
            // return default Docker host ip address
            return DEFAULT_LINUX_DOCKER_HOST_IP;
        }

        // on other env, Windows/Mac, search the docker machine ip and then find the bridge used
        String host = env.get(DOCKER_HOST_PROPERTY);
        if (host != null) {
            Matcher matcher = HOST_REGEXP_PATTERN.matcher(host);
            if (matcher.matches()) {
                String dockerIpAddress = matcher.group(1);
                Matcher ipv4Matcher = IPV4_ADDRESS_PATTERN.matcher(dockerIpAddress);
                if (ipv4Matcher.matches()) {
                    String subnet = ipv4Matcher.group(1).concat(".").concat(ipv4Matcher.group(2)).concat(".").concat(ipv4Matcher.group(3));
                    // now try to find a network interface matching this
                    Optional<InetAddress> matchingIpAddress = networkFinder.getMatchingInetAddress(subnet);
                    // return the bridge that is matching the host
                    if (matchingIpAddress.isPresent()) {
                        return matchingIpAddress.get().getHostAddress();
                    }
                }
            }
        }
        return DEFAULT_DOCKER_MACHINE_DOCKER_HOST_IP;
    }

    private static String dockerMachineCertsDirectoryPath() {
        return dockerMachineCertsDirectoryPath(System.getenv());
    }

    /**
     * Provides the location of certificates used to connect on docker.
     * It may use extra environment to help to build this path
     *
     * @param env
     *         should contain System environment
     * @return local path of the docker certificates
     * @throws NullPointerException if argument is null
     */
    protected static String dockerMachineCertsDirectoryPath(@NotNull Map<String, String> env) {
        Objects.requireNonNull(env, "Required non-null env variables");
        // check if have boot2docker variables
        String certPath = env.get(DOCKER_CERT_PATH_PROPERTY);

        // return it only if it exists
        if (certPath != null) {
            if (new File(certPath).exists()) {
                return certPath;
            } else {
                LOG.error(String.format("The directory provided by property %s doesn't exists. Returning default value", certPath));
            }
        }
        // return default value
        return DEFAULT_DOCKER_MACHINE_CERTS_DIR;
    }
}
