package org.eclipse.che.workspace.infrastructure.docker;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.URLRewriter;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.net.URI;

import static java.lang.String.format;

/**
 * {@link URLRewriter} needed for {@link DockerRuntimeInfrastructure} to handle specific cases of
 * running containers.
 * </br> For example, containers in MacOS, Windows, etc.
 *
 * @author Alexander Garagatyi
 */
public class URLRewriterImpl implements URLRewriter {
    static final String EXTERNAL_IP_PROPERTY = "che.docker.ip.external";

    private final String externalIpOfContainers;

    /**
     * `che.docker.ip.external` defines containers external IP in case it is needed
     * For example on Docker for Mac external IP of container is `localhost`.
     * On Windows it can be either `localhost` or hosts IP (but hosts IP may change
     * in case of moving from one network to another).
     */
    @Inject
    public URLRewriterImpl(@Named(EXTERNAL_IP_PROPERTY) String externalIpOfContainers) {
        if (externalIpOfContainers != null) {
            try {
                UriBuilder.fromUri("http://" + externalIpOfContainers).build();
            } catch (Exception e) {
                throw new RuntimeException(format("Illegal value '%s' of property '%s'. Error: %s",
                                                  externalIpOfContainers, EXTERNAL_IP_PROPERTY, e.getMessage()));
            }
        }
        this.externalIpOfContainers = externalIpOfContainers;
    }

    @Override
    public String rewriteURL(@Nullable RuntimeIdentity identity, @Nullable String name, String url)
            throws InfrastructureException {

        if (externalIpOfContainers != null) {
            try {
                URI uri = UriBuilder.fromUri(url).host(externalIpOfContainers).build();
                url = uri.toString();
            } catch (UriBuilderException | IllegalArgumentException e) {
                throw new InternalInfrastructureException(format("Rewriting of host '%s' in URL '%s' failed. Error: %s",
                                                                 externalIpOfContainers, url, e.getMessage()));
            }
        }
        return url;
    }
}
