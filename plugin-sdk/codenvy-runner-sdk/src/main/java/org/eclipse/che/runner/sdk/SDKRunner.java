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
package org.eclipse.che.runner.sdk;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.CustomPortService;
import org.eclipse.che.api.project.server.ProjectEventService;
import org.eclipse.che.api.project.shared.dto.RunnerEnvironment;
import org.eclipse.che.api.runner.RunnerException;
import org.eclipse.che.api.runner.dto.RunRequest;
import org.eclipse.che.api.runner.internal.ApplicationProcess;
import org.eclipse.che.api.runner.internal.Constants;
import org.eclipse.che.api.runner.internal.DeploymentSources;
import org.eclipse.che.api.runner.internal.Disposer;
import org.eclipse.che.api.runner.internal.ResourceAllocators;
import org.eclipse.che.api.runner.internal.Runner;
import org.eclipse.che.api.runner.internal.RunnerConfiguration;
import org.eclipse.che.api.runner.internal.RunnerConfigurationFactory;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.commons.GwtXmlUtils;
import org.eclipse.che.ide.maven.tools.Dependency;
import org.eclipse.che.ide.maven.tools.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * Runner implementation to run Codenvy extensions by deploying it to application server.
 *
 * @author Artem Zatsarynnyy
 * @author Eugene Voevodin
 */
@Singleton
public class SDKRunner extends Runner {
    private static final Logger LOG = LoggerFactory.getLogger(SDKRunner.class);

    public static final String IDE_GWT_XML_FILE_NAME    = "IDEPlatform.gwt.xml";
    public static final String DEFAULT_SERVER_NAME      = "tomcat7";
    /** Rel for code server link. */
    public static final String LINK_REL_CODE_SERVER     = "code server";
    /** Name of configuration parameter that specifies the domain name or IP address of the code server. */
    public static final String CODE_SERVER_BIND_ADDRESS = "runner.sdk.code_server_bind_address";
    public static final String HOST_NAME                = "runner.sdk.host_name";

    private final Map<String, ApplicationServer> servers;
    private final String                         codeServerAddress;
    private final String                         hostName;
    private final CustomPortService              portService;
    private final CodeServer                     codeServer;
    private final ProjectEventService            projectEventService;

    @Inject
    public SDKRunner(@Named(Constants.DEPLOY_DIRECTORY) java.io.File deployDirectoryRoot,
                     @Named(Constants.APP_CLEANUP_TIME) int cleanupTime,
                     @Named(CODE_SERVER_BIND_ADDRESS) String codeServerAddress,
                     @Named(HOST_NAME) String hostName,
                     CustomPortService portService,
                     Set<ApplicationServer> appServers,
                     CodeServer codeServer,
                     ResourceAllocators allocators,
                     EventService eventService,
                     ProjectEventService projectEventService) {
        super(deployDirectoryRoot, cleanupTime, allocators, eventService);
        this.codeServerAddress = codeServerAddress;
        this.hostName = hostName;
        this.portService = portService;
        this.codeServer = codeServer;
        this.projectEventService = projectEventService;
        this.servers = new HashMap<>();
        //available application servers should be already injected
        for (ApplicationServer appServer : appServers) {
            servers.put(appServer.getName(), appServer);
        }
    }

    @Override
    public String getName() {
        return "sdk";
    }

    @Override
    public String getDescription() {
        return "Codenvy extensions runner";
    }

    @Override
    public List<RunnerEnvironment> getEnvironments() {
        final DtoFactory dtoFactory = DtoFactory.getInstance();
        final List<RunnerEnvironment> environments = new LinkedList<>();
        for (ApplicationServer server : servers.values()) {
            final RunnerEnvironment runnerEnvironment = dtoFactory.createDto(RunnerEnvironment.class)
                                                                  .withId(server.getName())
                                                                  .withDescription(server.getDescription());
            environments.add(runnerEnvironment);
        }
        return environments;
    }

    @Override
    public RunnerConfigurationFactory getRunnerConfigurationFactory() {
        return new RunnerConfigurationFactory() {
            @Override
            public RunnerConfiguration createRunnerConfiguration(RunRequest request) throws RunnerException {
                final String server = request.getEnvironmentId();
                final int httpPort = portService.acquire();
                final int codeServerPort = portService.acquire();
                final SDKRunnerConfiguration configuration =
                        new SDKRunnerConfiguration(server, request.getMemorySize(), httpPort, codeServerAddress, codeServerPort, request);
                configuration.getLinks().add(DtoFactory.getInstance().createDto(Link.class)
                                                       .withRel(Constants.LINK_REL_WEB_URL)
                                                       .withHref(String.format("http://%s:%d/%s", hostName, httpPort, "ws/default")));
                configuration.getLinks().add(DtoFactory.getInstance().createDto(Link.class)
                                                       .withRel(LINK_REL_CODE_SERVER)
                                                       .withHref(String.format("%s:%d", codeServerAddress, codeServerPort)));
                if (request.isInDebugMode()) {
                    configuration.setDebugHost(hostName);
                    configuration.setDebugPort(portService.acquire());
                }
                return configuration;
            }
        };
    }

    @Override
    protected ApplicationProcess newApplicationProcess(final DeploymentSources toDeploy,
                                                       final RunnerConfiguration configuration) throws RunnerException {
        // It always should be SDKRunnerConfiguration.
        final SDKRunnerConfiguration sdkRunnerCfg = (SDKRunnerConfiguration)configuration;

        final ApplicationServer server = servers.get(sdkRunnerCfg.getServer());
        if (server == null) {
            throw new RunnerException(String.format("Server %s not found", sdkRunnerCfg.getServer()));
        }

        final java.io.File appDir;
        final Path codeServerWorkDirPath;
        final Utils.ExtensionDescriptor extensionDescriptor;
        try {
            appDir = Files.createTempDirectory(getDeployDirectory().toPath(),
                                               (server.getName() + '_' + getName().replace("/", "."))).toFile();
            codeServerWorkDirPath = Files.createTempDirectory(getDeployDirectory().toPath(),
                                                              ("codeServer_" + getName().replace("/", ".")));
            extensionDescriptor = Utils.getExtensionFromJarFile(new ZipFile(toDeploy.getFile()));
        } catch (IOException | IllegalArgumentException e) {
            throw new RunnerException(e);
        }

        final CodeServer.CodeServerProcess codeServerProcess = codeServer.prepare(codeServerWorkDirPath,
                                                                                  sdkRunnerCfg,
                                                                                  extensionDescriptor,
                                                                                  getExecutor());
        final String workspace = sdkRunnerCfg.getRequest().getWorkspace();
        final String project = sdkRunnerCfg.getRequest().getProject();
        // Register an appropriate ProjectEventListener in order
        // to provide mirror of a remote project for GWT code server.
        projectEventService.addListener(workspace, project, codeServerProcess);

        final ZipFile warFile = buildCodenvyWebAppWithExtension(extensionDescriptor);

        final ApplicationProcess process =
                server.deploy(appDir, warFile, toDeploy.getFile(), sdkRunnerCfg, codeServerProcess,
                              new ApplicationProcess.Callback() {
                                  @Override
                                  public void started() {
                                  }

                                  @Override
                                  public void stopped() {
                                      // stop tracking changes in remote project since code server is stopped
                                      projectEventService.removeListener(workspace, project, codeServerProcess);
                                      portService.release(sdkRunnerCfg.getHttpPort());
                                      final int debugPort = sdkRunnerCfg.getDebugPort();
                                      if (debugPort > 0) {
                                          portService.release(debugPort);
                                      }
                                      final int codeServerPort = sdkRunnerCfg.getCodeServerPort();
                                      if (codeServerPort > 0) {
                                          portService.release(codeServerPort);
                                      }
                                  }
                              }
                             );

        registerDisposer(process, new Disposer() {
            @Override
            public void dispose() {
                if (!IoUtil.deleteRecursive(appDir)) {
                    LOG.error("Unable to remove app: {}", appDir);
                }

                if (!IoUtil.deleteRecursive(codeServerWorkDirPath.toFile(), false)) {
                    LOG.error("Unable to remove code server working directory: {}", codeServerWorkDirPath);
                }
            }
        });

        return process;
    }

    private ZipFile buildCodenvyWebAppWithExtension(Utils.ExtensionDescriptor extension) throws RunnerException {
        final ZipFile warPath;
        try {
            // prepare Codenvy Platform sources
            final Path workDirPath = Files.createTempDirectory(getDeployDirectory().toPath(), ("war_" + getName().replace("/", ".")));
            ZipUtils.unzip(Utils.getCodenvyPlatformBinaryDistribution().openStream(), workDirPath.toFile());

            // integrate extension to Codenvy Platform
            final File pom = workDirPath.resolve("pom.xml").toFile();
            final Model model = Model.readFrom(pom);
            model.dependencies()
                 .add(new Dependency(extension.groupId,
                                     extension.artifactId,
                                     extension.version));
            model.writeTo(pom);

            GwtXmlUtils.inheritGwtModule(IoUtil.findFile(SDKRunner.IDE_GWT_XML_FILE_NAME, workDirPath.toFile()).toPath(),
                                         extension.gwtModuleName);

            warPath = Utils.buildProjectFromSources(workDirPath, "*.war");
        } catch (Exception e) {
            throw new RunnerException(e);
        }
        return warPath;
    }

}