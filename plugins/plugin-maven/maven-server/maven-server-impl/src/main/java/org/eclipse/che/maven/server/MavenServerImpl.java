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
package org.eclipse.che.maven.server;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulationException;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.project.ProjectUtils;
import org.apache.maven.project.interpolation.AbstractStringBasedModelInterpolator;
import org.apache.maven.project.interpolation.ModelInterpolationException;
import org.apache.maven.project.path.DefaultPathTranslator;
import org.apache.maven.project.validation.ModelValidationResult;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.shared.dependency.tree.DependencyTreeResolutionListener;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.internal.impl.DefaultArtifactResolver;
import org.eclipse.aether.internal.impl.DefaultRepositorySystem;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.che.maven.CheArtifactResolver;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.che.maven.data.MavenArtifactKey;
import org.eclipse.che.maven.data.MavenKey;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.data.MavenProjectProblem;
import org.eclipse.che.maven.data.MavenRemoteRepository;
import org.eclipse.che.maven.data.MavenWorkspaceCache;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Evgen Vidolob
 */
public class MavenServerImpl extends MavenRmiObject implements MavenServer {
    private static final String[] CLI_METHODS = new String[] {"initialize", "cli", "logging", "properties", "container"};

    private final MavenServerTerminalLogger terminalLogger;
    private final File                      localRepository;

    private boolean                updateSnapshots;
    private DefaultPlexusContainer container;
    private Settings               settings;
    private Properties             properties;
    private ArtifactRepository     localRepo;

    private Date buildDate;

    private MavenWorkspaceCache workspaceCache;

    private MavenServerProgressNotifierImpl mavenProgressNotifier;


    public MavenServerImpl(MavenSettings settings) throws RemoteException {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(getLog4jLogLevel(settings.getLoggingLevel()));
        File mavenHome = settings.getMavenHome();
        if (mavenHome != null) {
            System.setProperty("maven.home", mavenHome.getPath());
        }

        terminalLogger = new MavenServerTerminalLogger();
        terminalLogger.setThreshold(settings.getLoggingLevel());
        ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
        MavenCli cli = new MavenCli(classWorld) {
            @Override
            protected void customizeContainer(PlexusContainer container) {
                ((DefaultPlexusContainer)container).setLoggerManager(new BaseLoggerManager() {
                    @Override
                    protected org.codehaus.plexus.logging.Logger createLogger(String s) {
                        return terminalLogger;
                    }
                });
            }
        };


        //maven 3.2.2 has org.apache.maven.cli.MavenCli$CliRequest class
        //but maven 3.3.3 has org.apache.maven.cli.CliRequest so try to support both classes
        Class<?> cliRequestClass;
        SettingsBuilder settingsBuilder = null;
        try {
            cliRequestClass = MavenCli.class.getClassLoader().loadClass("org.apache.maven.cli.CliRequest");
            System.setProperty("maven.multiModuleProjectDirectory", new File("").getPath());
            settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();
        } catch (ClassNotFoundException e) {
            try {
                cliRequestClass = MavenCli.class.getClassLoader().loadClass("org.apache.maven.cli.MavenCli$CliRequest");
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            }
        }


        Object request;
        List<String> commandLine = new ArrayList<>(settings.getUserProperties().size());
        commandLine.addAll(settings.getUserProperties().entrySet().stream().map(entry -> "-D" + entry.getKey() + "=" + entry.getValue())
                                   .collect(Collectors.toList()));

        if (settings.getLoggingLevel() == MavenTerminal.LEVEL_DEBUG) {
            commandLine.add("-X");
            commandLine.add("-e");
        }
        if (settings.getLoggingLevel() == MavenTerminal.LEVEL_DISABLED) {
            commandLine.add("-q");
        }
        if (commandLine.contains("-U") || commandLine.contains("--update-snapshots")) {
            updateSnapshots = true;
        }

        try {
            Constructor constructor = cliRequestClass.getDeclaredConstructor(String[].class, ClassWorld.class);
            constructor.setAccessible(true);
            request = constructor.newInstance(commandLine.toArray(new String[commandLine.size()]), classWorld);

            for (String method : CLI_METHODS) {
                Method m = MavenCli.class.getDeclaredMethod(method, cliRequestClass);
                m.setAccessible(true);
                m.invoke(cli, request);
            }

            Method containerMethod = MavenCli.class.getDeclaredMethod("container", cliRequestClass);
            containerMethod.setAccessible(true);
            container = (DefaultPlexusContainer)containerMethod.invoke(cli, request);
            container.getLoggerManager().setThreshold(settings.getLoggingLevel());
            Field systemProperties = cliRequestClass.getDeclaredField("systemProperties");
            systemProperties.setAccessible(true);

            properties = (Properties)systemProperties.get(request);
            Field userPropertiesField = cliRequestClass.getDeclaredField("userProperties");
            userPropertiesField.setAccessible(true);
            Properties userProperties = (Properties)userPropertiesField.get(request);
            this.settings = getSettings(settingsBuilder, settings, properties, userProperties);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new RuntimeException(e);
        }

        try {
            localRepository = new File(this.settings.getLocalRepository());
            localRepo = getMavenComponent(RepositorySystem.class).createLocalRepository(localRepository);
        } catch (InvalidRepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public static MavenModel interpolateModel(MavenModel model, File projectDir) throws RemoteException {
        Model result = MavenModelUtil.convertToMavenModel(model);
        result = internalInterpolate(result, projectDir);

//        PathTranslator pathTranslator = new DefaultPathTranslator();
//        pathTranslator.alignToBaseDirectory(result, projectDir);

        return MavenModelUtil.convertModel(result);
    }

    private static Model internalInterpolate(Model model, File projectDir) throws RemoteException {
        try {
            AbstractStringBasedModelInterpolator interpolator =
                    new org.apache.maven.project.interpolation.StringSearchModelInterpolator(new DefaultPathTranslator());
            interpolator.initialize();

            Properties props = new Properties(); //MavenServerUtil.collectSystemProperties();
            ProjectBuilderConfiguration config = new DefaultProjectBuilderConfiguration().setExecutionProperties(props);
            config.setBuildStartTime(new Date());

            model = interpolator.interpolate(model, projectDir, config, false);
        } catch (ModelInterpolationException e) {
            MavenServerContext.getLogger().warning(e);
        } catch (InitializationException e) {
            MavenServerContext.getLogger().error(e);
        }
        return model;
    }

    private Settings getSettings(SettingsBuilder builder, MavenSettings settings, Properties systemProperties, Properties userProperties)
            throws RemoteException {
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setGlobalSettingsFile(settings.getGlobalSettings());
        request.setUserSettingsFile(settings.getUserSettings());
        request.setSystemProperties(systemProperties);
        request.setUserProperties(userProperties);

        Settings result = new Settings();
        try {
            result = builder.build(request).getEffectiveSettings();
        } catch (SettingsBuildingException e) {
            MavenServerContext.getLogger().info(e);
        }

        result.setOffline(settings.isOffline());
        if (settings.getLocalRepository() != null) {
            result.setLocalRepository(settings.getLocalRepository().getPath());
        }
        if (result.getLocalRepository() == null) {
            result.setLocalRepository(new File(System.getProperty("user.home"), ".m2/repository").getPath());
        }
        return result;
    }

    private Level getLog4jLogLevel(int level) {
        switch (level) {
            case MavenTerminal.LEVEL_DEBUG:
                return Level.DEBUG;
            case MavenTerminal.LEVEL_DISABLED:
                return Level.OFF;
            case MavenTerminal.LEVEL_ERROR:
                return Level.ERROR;
            case MavenTerminal.LEVEL_FATAL:
                return Level.FATAL;
            case MavenTerminal.LEVEL_INFO:
                return Level.INFO;
            case MavenTerminal.LEVEL_WARN:
                return Level.WARN;
        }
        return Level.INFO;
    }

    public <T> T getMavenComponent(Class<T> clazz) {
        try {
            return container.lookup(clazz);
        } catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getMavenComponent(Class<T> clazz, String role) {
        try {
            return container.lookup(clazz, role);
        } catch (ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    private void logWarn(String message) {
        try {
            MavenServerContext.getLogger().warning(new RuntimeException(message));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * method from org.apache.maven.DefaultMaven#getLifecycleParticipants
     */
    private Collection<AbstractMavenLifecycleParticipant> getLifecycleParticipants(Collection<MavenProject> projects) {
        Collection<AbstractMavenLifecycleParticipant> lifecycleListeners = new LinkedHashSet<AbstractMavenLifecycleParticipant>();

        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            try {
                lifecycleListeners.addAll(container.lookupList(AbstractMavenLifecycleParticipant.class));
            } catch (ComponentLookupException e) {
                // this is just silly, lookupList should return an empty list!
                logWarn("Failed to lookup lifecycle participants: " + e.getMessage());
            }

            Collection<ClassLoader> scannedRealms = new HashSet<ClassLoader>();

            for (MavenProject project : projects) {
                ClassLoader projectRealm = project.getClassRealm();

                if (projectRealm != null && scannedRealms.add(projectRealm)) {
                    Thread.currentThread().setContextClassLoader(projectRealm);

                    try {
                        lifecycleListeners.addAll(container.lookupList(AbstractMavenLifecycleParticipant.class));
                    } catch (ComponentLookupException e) {
                        // this is just silly, lookupList should return an empty list!
                        logWarn("Failed to lookup lifecycle participants: " + e.getMessage());
                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }

        return lifecycleListeners;
    }

    @Override
    public void setComponents(MavenWorkspaceCache cache,
                              boolean failOnUnresolvedDependency,
                              MavenTerminal mavenTerminal,
                              MavenServerProgressNotifier notifier,
                              boolean alwaysUpdateSnapshot) throws RemoteException {

        container.addComponent(getMavenComponent(ArtifactResolver.class, "che"), ArtifactResolver.ROLE);
        ArtifactResolver artifactResolver = getMavenComponent(ArtifactResolver.class);
        if (artifactResolver instanceof CheArtifactResolver) {
            ((CheArtifactResolver)artifactResolver).setWorkspaceCache(cache, failOnUnresolvedDependency);
        }

        buildDate = new Date();
        workspaceCache = cache;
        updateSnapshots = updateSnapshots || alwaysUpdateSnapshot;
        terminalLogger.setTerminal(mavenTerminal);
        mavenProgressNotifier = new MavenServerProgressNotifierImpl(notifier);
    }

    @Override
    public String getEffectivePom(File pom, List<String> activeProfiles, List<String> inactiveProfiles)
            throws RemoteException {
        return EffectivePomWriter.getEffectivePom(this, pom, new ArrayList<>(activeProfiles), new ArrayList<>(inactiveProfiles));
    }

    @Override
    public MavenServerResult resolveProject(File pom, List<String> activeProfiles, List<String> inactiveProfiles) throws RemoteException {
        DependencyTreeResolutionListener listener = new DependencyTreeResolutionListener(terminalLogger);
        MavenResult mavenResult = internalResolveProject(pom, activeProfiles, inactiveProfiles, Collections.singletonList(listener));

        return createResult(pom, mavenResult);
    }

    @Override
    public MavenArtifact resolveArtifact(MavenArtifactKey artifactKey, List<MavenRemoteRepository> repositories) throws RemoteException {
        Artifact artifact = getMavenComponent(ArtifactFactory.class)
                .createArtifactWithClassifier(artifactKey.getGroupId(), artifactKey.getArtifactId(), artifactKey.getVersion(),
                                              artifactKey.getPackaging(), artifactKey.getClassifier());

        List<ArtifactRepository> repos = new ArrayList<>();
        ArtifactRepositoryFactory factory = getMavenComponent(ArtifactRepositoryFactory.class);
        for (MavenRemoteRepository repository : repositories) {
            try {
                ArtifactRepository artifactRepository =
                        ProjectUtils.buildArtifactRepository(MavenModelUtil.convertToMavenRepository(repository), factory, container);
                repos.add(artifactRepository);
            } catch (InvalidRepositoryException e) {
                MavenServerContext.getLogger().error(e);
            }

        }

        MavenExecutionRequest request = newMavenRequest(null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        repos.forEach(request::addRemoteRepository);

        DefaultMaven maven = (DefaultMaven)getMavenComponent(Maven.class);
        RepositorySystemSession repositorySystemSession = maven.newRepositorySession(request);

        org.eclipse.aether.impl.ArtifactResolver artifactResolver = getMavenComponent(org.eclipse.aether.impl.ArtifactResolver.class);
        InternalLoggerFactory loggerFactory = new InternalLoggerFactory();
        if (artifactResolver instanceof DefaultArtifactResolver) {
            ((DefaultArtifactResolver)artifactResolver).setLoggerFactory(loggerFactory);
        }

        org.eclipse.aether.RepositorySystem repositorySystem = getMavenComponent(org.eclipse.aether.RepositorySystem.class);
        if (repositorySystem instanceof DefaultRepositorySystem) {
            ((DefaultRepositorySystem)repositorySystem).setLoggerFactory(loggerFactory);
        }

        List<RemoteRepository> remoteRepositories = RepositoryUtils.toRepos(request.getRemoteRepositories());
        remoteRepositories = repositorySystem.newResolutionRepositories(repositorySystemSession, remoteRepositories);

        try {
            ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession,
                                                                             new ArtifactRequest(RepositoryUtils.toArtifact(artifact),
                                                                                                 remoteRepositories, null));
        return MavenModelUtil.convertArtifact(RepositoryUtils.toArtifact(artifactResult.getArtifact()), localRepository);
        } catch (ArtifactResolutionException e) {
            MavenServerContext.getLogger().info(e);
        }
        return MavenModelUtil.convertArtifact(artifact, localRepository);
    }

    @Override
    public void reset() throws RemoteException {
        terminalLogger.setTerminal(null);
        mavenProgressNotifier = null;
        ArtifactResolver artifactResolver = getMavenComponent(ArtifactResolver.class);
        if (artifactResolver instanceof CheArtifactResolver) {
            ((CheArtifactResolver)artifactResolver).reset();
        }
    }

    @Override
    public void dispose() throws RemoteException {
        container.dispose();
    }

    @Override
    public File getLocalRepository() {
        return localRepository;
    }

    private MavenServerResult createResult(File pom, MavenResult mavenResult) throws RemoteException {
        List<MavenProjectProblem> problems = new ArrayList<>();
        Set<MavenKey> unresolvedArtifacts = new HashSet<>();
        validate(pom, mavenResult.getExceptions(), problems);
        MavenProject project = mavenResult.getMavenProject();
        if (project == null) {
            return new MavenServerResult(null, problems, unresolvedArtifacts);
        }

        MavenModel model = null;
        try {
            DependencyResolutionResult resolutionResult = mavenResult.getDependencyResolutionResult();
            org.eclipse.aether.graph.DependencyNode dependencyNode = null;
            if (resolutionResult != null) {
                dependencyNode = resolutionResult.getDependencyGraph();
            }

            List<org.eclipse.aether.graph.DependencyNode> dependencyNodes = null;
            if (dependencyNode != null) {
                dependencyNodes = dependencyNode.getChildren();
            }

            model = MavenModelUtil.convertProjectToModel(project, dependencyNodes, new File(localRepo.getBasedir()));
        } catch (Exception e) {
            validate(project.getFile(), Collections.singletonList(e), problems);
        }

        List<String> activeProfiles = getActiveProfiles(project);
        MavenProjectInfo projectInfo = new MavenProjectInfo(model, null, activeProfiles);


        return new MavenServerResult(projectInfo, problems, unresolvedArtifacts);
    }

    private List<String> getActiveProfiles(MavenProject project) throws RemoteException {
        List<Profile> profiles = new ArrayList<>();

        try {
            while (project != null) {
                profiles.addAll(project.getActiveProfiles());
                project = project.getParent();
            }
        } catch (Exception e) {
            MavenServerContext.getLogger().info(e);
        }

        return profiles.stream().filter(p -> p.getId() != null).map(Profile::getId).collect(Collectors.toList());
    }

    private void validate(File pom, List<Exception> exceptions, List<MavenProjectProblem> problems)
            throws RemoteException {
        for (Throwable exception : exceptions) {

            if (exception instanceof IllegalStateException && exception.getCause() != null) {
                exception = exception.getCause();
            }

            if (exception instanceof InvalidProjectModelException) {
                ModelValidationResult validationResult = ((InvalidProjectModelException)exception).getValidationResult();
                if (validationResult != null) {
                    problems.addAll(validationResult.getMessages().stream()
                                                    .map(s -> MavenProjectProblem.newStructureProblem(pom.getPath(), s))
                                                    .collect(Collectors.toList()));
                } else {
                    problems.add(MavenProjectProblem.newStructureProblem(pom.getPath(), exception.getCause().getMessage()));
                }
            }
            if (exception instanceof ProjectBuildingException) {
                String message = exception.getCause() == null ? exception.getMessage() : exception.getCause().getMessage();
                problems.add(MavenProjectProblem.newStructureProblem(pom.getPath(), message));
            } else {
                MavenServerContext.getLogger().info(exception);
                problems.add(MavenProjectProblem.newStructureProblem(pom.getPath(), exception.getMessage()));
            }
        }
    }

    private MavenResult internalResolveProject(File pom, List<String> activeProfiles, List<String> inactiveProfiles,
                                               List<ResolutionListener> dependencyTreeResolutionListeners) {

        MavenExecutionRequest request = newMavenRequest(pom, activeProfiles, inactiveProfiles, Collections.emptyList());
        request.setUpdateSnapshots(updateSnapshots);

        AtomicReference<MavenResult> reference = new AtomicReference<>();
        runMavenRequest(request, () -> {
            try {
                ProjectBuilder builder = getMavenComponent(ProjectBuilder.class);

                List<ProjectBuildingResult> resultList =
                        builder.build(Collections.singletonList(pom), false, request.getProjectBuildingRequest());
                ProjectBuildingResult result = resultList.get(0);
                MavenProject mavenProject = result.getProject();
                RepositorySystemSession repositorySession = getMavenComponent(LegacySupport.class).getRepositorySession();
                if (repositorySession instanceof DefaultRepositorySystemSession) {
                    ((DefaultRepositorySystemSession)repositorySession)
                            .setTransferListener(new ArtifactTransferListener(mavenProgressNotifier));
                    if (workspaceCache != null) {
                        ((DefaultRepositorySystemSession)repositorySession).setWorkspaceReader(new MavenWorkspaceReader(workspaceCache));
                    }

                }

                List<Exception> exceptions = new ArrayList<>();

                loadExtensions(mavenProject, exceptions);
                mavenProject.setDependencyArtifacts(mavenProject.createArtifacts(getMavenComponent(ArtifactFactory.class), null, null));

                ArtifactResolutionRequest resolutionRequest = new ArtifactResolutionRequest();
                resolutionRequest.setArtifact(mavenProject.getArtifact());
                resolutionRequest.setRemoteRepositories(mavenProject.getRemoteArtifactRepositories());
                resolutionRequest.setArtifactDependencies(mavenProject.getDependencyArtifacts());
                resolutionRequest.setListeners(dependencyTreeResolutionListeners);
                resolutionRequest.setLocalRepository(localRepo);
                resolutionRequest.setManagedVersionMap(mavenProject.getManagedVersionMap());
                resolutionRequest.setResolveTransitively(true);
                resolutionRequest.setResolveRoot(false);
                ArtifactResolver resolver = getMavenComponent(ArtifactResolver.class);
                ArtifactResolutionResult resolve = resolver.resolve(resolutionRequest);
                mavenProject.setArtifacts(resolve.getArtifacts());
                reference.set(new MavenResult(mavenProject, exceptions));

            } catch (Exception e) {
                reference.set(new MavenResult(null, null, Collections.singletonList(e)));
            }
        });
        return reference.get();
    }

    private void loadExtensions(MavenProject project, List<Exception> exceptions) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        Collection<AbstractMavenLifecycleParticipant> participants = getLifecycleParticipants(Collections.singletonList(project));
        if (!participants.isEmpty()) {
            LegacySupport legacySupport = getMavenComponent(LegacySupport.class);
            MavenSession session = legacySupport.getSession();
            session.setCurrentProject(project);
            session.setProjects(Collections.singletonList(project));

            for (AbstractMavenLifecycleParticipant participant : participants) {
                Thread.currentThread().setContextClassLoader(participant.getClass().getClassLoader());
                try {
                    participant.afterProjectsRead(session);
                } catch (MavenExecutionException e) {
                    exceptions.add(e);
                } finally {
                    Thread.currentThread().setContextClassLoader(currentClassLoader);
                }
            }
        }
    }

    public void runMavenRequest(MavenExecutionRequest request, Runnable runnable) {
        DefaultMaven maven = (DefaultMaven)getMavenComponent(Maven.class);
        RepositorySystemSession repositorySystemSession = maven.newRepositorySession(request);
        request.getProjectBuildingRequest().setRepositorySession(repositorySystemSession);
        MavenSession mavenSession = new MavenSession(container, repositorySystemSession, request, new DefaultMavenExecutionResult());
        LegacySupport legacySupport = getMavenComponent(LegacySupport.class);
        MavenSession previousSession = legacySupport.getSession();
        legacySupport.setSession(mavenSession);
        try {
            for (AbstractMavenLifecycleParticipant participant : getLifecycleParticipants(Collections.emptyList())) {
                participant.afterSessionStart(mavenSession);
            }
            runnable.run();
        } catch (MavenExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            legacySupport.setSession(previousSession);
        }
    }

    public MavenExecutionRequest newMavenRequest(File pom, List<String> activeProfiles, List<String> inactiveProfiles, List<String> goals) {
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        try {
            getMavenComponent(MavenExecutionRequestPopulator.class).populateFromSettings(request, settings);
            request.setGoals(goals);
            request.setPom(pom);
            getMavenComponent(MavenExecutionRequestPopulator.class).populateDefaults(request);
            request.setSystemProperties(properties);
            request.setActiveProfiles(activeProfiles);
            request.setInactiveProfiles(inactiveProfiles);
            request.setStartTime(buildDate);
            return request;

        } catch (MavenExecutionRequestPopulationException e) {
            throw new RuntimeException(e);
        }
    }

    private class InternalLoggerFactory implements org.eclipse.aether.spi.log.LoggerFactory {

        @Override
        public org.eclipse.aether.spi.log.Logger getLogger(String s) {
            return new org.eclipse.aether.spi.log.Logger() {
                @Override
                public boolean isDebugEnabled() {
                    return terminalLogger.isDebugEnabled();
                }

                @Override
                public void debug(String s) {
                    terminalLogger.debug(s);
                }

                @Override
                public void debug(String s, Throwable throwable) {
                    terminalLogger.debug(s, throwable);
                }

                @Override
                public boolean isWarnEnabled() {
                    return terminalLogger.isWarnEnabled();
                }

                @Override
                public void warn(String s) {
                    terminalLogger.warn(s);
                }

                @Override
                public void warn(String s, Throwable throwable) {
                    terminalLogger.debug(s, throwable);
                }
            };
        }
    }
}
