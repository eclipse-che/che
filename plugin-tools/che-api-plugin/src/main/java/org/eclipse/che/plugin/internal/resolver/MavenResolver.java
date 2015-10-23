/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.internal.resolver;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;
import org.eclipse.che.plugin.internal.api.PluginResolver;
import org.eclipse.che.plugin.internal.api.PluginResolverException;
import org.eclipse.che.plugin.internal.api.PluginResolverNotFoundException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * MavenResolver will resolve maven artifacts by using maven aether resolver.
 * @author Florent Benoit
 */
@Singleton
public class MavenResolver implements PluginResolver {

    /**
     * Plugin configuration.
     */
    private PluginConfiguration pluginConfiguration;


    /**
     * Defines a new resolver by using the specified configuration
     * @param pluginConfiguration expecting the Maven2 local repository
     */
    @Inject
    public MavenResolver(final PluginConfiguration pluginConfiguration) {
        this.pluginConfiguration = pluginConfiguration;
    }


    /**
     * Resolve provided artifact
     * @param pluginRef reference of the plugin
     * @return
     * @throws PluginResolverException
     */
    public Path download(@NotNull final String pluginRef) throws PluginResolverException, PluginResolverNotFoundException {

        String coords = pluginRef;
        if (coords.startsWith(getProtocol())) {
            coords = coords.substring(getProtocol().length());
        }

        // init the repository system
        RepositorySystem system = newRepositorySystem();

        // init the system session
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        // add local repository
        String localMavenRepo = pluginConfiguration.getLocalMavenRepository().toAbsolutePath().toString();

        Artifact artifact = new DefaultArtifact(coords);
        if ("CURRENT".equals(artifact.getVersion())) {
            artifact = artifact.setVersion(pluginConfiguration.getDefaultChePluginsVersion());
        }

        LocalRepository localRepo = new LocalRepository(localMavenRepo);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));


        ArtifactRequest artifactRequest = new ArtifactRequest();
        artifactRequest.setArtifact(artifact);
        artifactRequest.setRepositories(newRepositories());

        ArtifactResult artifactResult;
        try {
            artifactResult = system.resolveArtifact(session, artifactRequest);
        } catch (ArtifactResolutionException e) {
            throw new PluginResolverNotFoundException(String.format("Unable to download artifact %s", artifact.toString()), e);
        }

        artifact = artifactResult.getArtifact();

        return artifact.getFile().toPath();

    }

    protected RepositorySystem newRepositorySystem() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class);
    }

    protected List<RemoteRepository> newRepositories() {
        List<RemoteRepository> repositories = new ArrayList<>(3);
        repositories.add(new RemoteRepository.Builder("central", "default", "http://central.maven.org/maven2/").build());
        repositories.add(new RemoteRepository.Builder("codenvy-public", "default", "https://maven.codenvycorp.com/content/groups/public/").build());
        repositories.add(new RemoteRepository.Builder("codenvy-public", "default", "https://maven.codenvycorp.com/content/repositories/codenvy-public-snapshots/").build());
        return repositories;
    }


    @Override
    public String getProtocol() {
        return "mvn:";
    }


}
