/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.languageserver;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.fixJdtUri;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.ide.ext.java.shared.Constants.CLASS_PATH_TREE;
import static org.eclipse.che.ide.ext.java.shared.Constants.EFFECTIVE_POM;
import static org.eclipse.che.ide.ext.java.shared.Constants.EFFECTIVE_POM_REQUEST_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARIES;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARIES_CHILDREN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARY_CHILDREN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARY_ENTRY;
import static org.eclipse.che.ide.ext.java.shared.Constants.FILE_STRUCTURE;
import static org.eclipse.che.ide.ext.java.shared.Constants.GET_JAVA_CORE_OPTIONS;
import static org.eclipse.che.ide.ext.java.shared.Constants.IMPLEMENTERS;
import static org.eclipse.che.ide.ext.java.shared.Constants.ORGANIZE_IMPORTS;
import static org.eclipse.che.ide.ext.java.shared.Constants.RECOMPUTE_POM_DIAGNOSTICS;
import static org.eclipse.che.ide.ext.java.shared.Constants.REIMPORT_MAVEN_PROJECTS;
import static org.eclipse.che.ide.ext.java.shared.Constants.REIMPORT_MAVEN_PROJECTS_REQUEST_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.UPDATE_JAVA_CORE_OPTIONS;
import static org.eclipse.che.ide.ext.java.shared.Constants.USAGES;
import static org.eclipse.che.jdt.ls.extension.api.Commands.CREATE_SIMPLE_PROJECT;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FILE_STRUCTURE_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FIND_IMPLEMENTERS_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FIND_TESTS_FROM_ENTRY_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FIND_TESTS_FROM_FOLDER_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FIND_TESTS_FROM_PROJECT_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FIND_TESTS_IN_FILE_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.FIND_TEST_BY_CURSOR_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_CLASS_PATH_TREE_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_EFFECTIVE_POM_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_EXTERNAL_LIBRARIES_CHILDREN_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_EXTERNAL_LIBRARIES_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_LIBRARY_CHILDREN_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_LIBRARY_ENTRY_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_OUTPUT_DIR_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.GET_SOURCE_FOLDERS;
import static org.eclipse.che.jdt.ls.extension.api.Commands.REIMPORT_MAVEN_PROJECTS_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.RESOLVE_CLASSPATH_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.TEST_DETECT_COMMAND;
import static org.eclipse.che.jdt.ls.extension.api.Commands.UPDATE_PROJECT_CLASSPATH;
import static org.eclipse.che.jdt.ls.extension.api.Commands.USAGES_COMMAND;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.InitializedLanguageServer;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectUpdatedEvent;
import org.eclipse.che.jdt.ls.extension.api.Commands;
import org.eclipse.che.jdt.ls.extension.api.Severity;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.ImplementersResponse;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaCoreOptions;
import org.eclipse.che.jdt.ls.extension.api.dto.JobResult;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportParams;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportsResult;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.ResourceLocation;
import org.eclipse.che.jdt.ls.extension.api.dto.TestFindParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.TestPosition;
import org.eclipse.che.jdt.ls.extension.api.dto.TestPositionParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateClasspathParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.ExtendedSymbolInformationDto;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.ImplementersResponseDto;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.TestPositionDto;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.json.adapters.CollectionTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EitherTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.json.adapters.EnumTypeAdapterFactory;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service makes custom commands in our jdt.ls extension available to clients.
 *
 * @author Thomas Mäder
 */
public class JavaLanguageServerExtensionService {
  private static final int TIMEOUT = 10;

  private final Gson gson;
  private final LanguageServerRegistry registry;

  private static final Logger LOG =
      LoggerFactory.getLogger(JavaLanguageServerExtensionService.class);
  private final RequestHandlerConfigurator requestHandler;
  private final ProjectManager projectManager;
  private final EventService eventService;

  @Inject
  public JavaLanguageServerExtensionService(
      LanguageServerRegistry registry,
      RequestHandlerConfigurator requestHandler,
      ProjectManager projectManager,
      EventService eventService) {
    this.registry = registry;
    this.requestHandler = requestHandler;
    this.projectManager = projectManager;
    this.eventService = eventService;
    this.gson =
        new GsonBuilder()
            .registerTypeAdapterFactory(new CollectionTypeAdapterFactory())
            .registerTypeAdapterFactory(new EitherTypeAdapterFactory())
            .registerTypeAdapterFactory(new EnumTypeAdapterFactory())
            .create();
  }

  @PostConstruct
  public void configureMethods() {
    requestHandler
        .newConfiguration()
        .methodName(FILE_STRUCTURE)
        .paramsAsDto(FileStructureCommandParameters.class)
        .resultAsListOfDto(ExtendedSymbolInformationDto.class)
        .withFunction(this::executeFileStructure);

    requestHandler
        .newConfiguration()
        .methodName(RECOMPUTE_POM_DIAGNOSTICS)
        .paramsAsString()
        .noResult()
        .withConsumer(this::reComputeDiagnostics);

    requestHandler
        .newConfiguration()
        .methodName(EFFECTIVE_POM)
        .paramsAsString()
        .resultAsString()
        .withFunction(this::getEffectivePom);

    requestHandler
        .newConfiguration()
        .methodName(REIMPORT_MAVEN_PROJECTS)
        .paramsAsDto(ReImportMavenProjectsCommandParameters.class)
        .resultAsListOfString()
        .withFunction(this::reImportMavenProjects);

    requestHandler
        .newConfiguration()
        .methodName(EXTERNAL_LIBRARIES)
        .paramsAsDto(ExternalLibrariesParameters.class)
        .resultAsListOfDto(Jar.class)
        .withFunction(this::getProjectExternalLibraries);

    requestHandler
        .newConfiguration()
        .methodName(EXTERNAL_LIBRARIES_CHILDREN)
        .paramsAsDto(ExternalLibrariesParameters.class)
        .resultAsListOfDto(JarEntry.class)
        .withFunction(this::getExternalLibrariesChildren);

    requestHandler
        .newConfiguration()
        .methodName(EXTERNAL_LIBRARY_CHILDREN)
        .paramsAsDto(ExternalLibrariesParameters.class)
        .resultAsListOfDto(JarEntry.class)
        .withFunction(this::getLibraryChildren);

    requestHandler
        .newConfiguration()
        .methodName(EXTERNAL_LIBRARY_ENTRY)
        .paramsAsString()
        .resultAsDto(JarEntry.class)
        .withFunction(this::getLibraryEntry);

    requestHandler
        .newConfiguration()
        .methodName(CLASS_PATH_TREE)
        .paramsAsString()
        .resultAsListOfDto(ClasspathEntry.class)
        .withFunction(this::getClasspathTree);

    requestHandler
        .newConfiguration()
        .methodName(ORGANIZE_IMPORTS)
        .paramsAsDto(OrganizeImportParams.class)
        .resultAsDto(OrganizeImportsResult.class)
        .withFunction(this::organizeImports);

    requestHandler
        .newConfiguration()
        .methodName(IMPLEMENTERS)
        .paramsAsDto(TextDocumentPositionParams.class)
        .resultAsDto(ImplementersResponseDto.class)
        .withFunction(this::findImplementers);

    requestHandler
        .newConfiguration()
        .methodName(USAGES)
        .paramsAsDto(TextDocumentPositionParams.class)
        .resultAsDto(UsagesResponse.class)
        .withFunction(this::usages);

    requestHandler
        .newConfiguration()
        .methodName(GET_JAVA_CORE_OPTIONS)
        .paramsAsListOfString()
        .resultAsDto(JavaCoreOptions.class)
        .withFunction(this::getJavaCoreOptions);

    requestHandler
        .newConfiguration()
        .methodName(UPDATE_JAVA_CORE_OPTIONS)
        .paramsAsDto(JavaCoreOptions.class)
        .resultAsBoolean()
        .withFunction(this::updateJavaCoreOptions);
  }

  /**
   * Compute output directory of the project.
   *
   * @param projectPath project path
   * @return output directory
   */
  public String getOutputDir(String projectPath) {
    checkLanguageServerInitialized();

    String projectUri = prefixURI(projectPath);
    Type type = new TypeToken<String>() {}.getType();
    return doGetOne(GET_OUTPUT_DIR_COMMAND, singletonList(projectUri), type);
  }

  /**
   * Compute output directory of the project.
   *
   * @param projectName project URI
   * @param sourceFolder name of source folder
   */
  public void createSimpleProject(String projectName, String sourceFolder) {
    CompletableFuture<Object> result =
        executeCommand(CREATE_SIMPLE_PROJECT, Arrays.asList(projectName, sourceFolder));
    Type targetClassType = new TypeToken<String>() {}.getType();
    try {
      gson.fromJson(gson.toJson(result.get(TIMEOUT, TimeUnit.SECONDS)), targetClassType);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  /**
   * Detects test method by cursor position.
   *
   * @param fileUri file URI
   * @param testAnnotation test method annotation
   * @param cursorOffset cursor position
   * @return test position {@link TestPosition}
   */
  public List<TestPositionDto> detectTest(String fileUri, String testAnnotation, int cursorOffset) {
    TestPositionParameters parameters =
        new TestPositionParameters(fileUri, testAnnotation, cursorOffset);
    CompletableFuture<Object> result =
        executeCommand(TEST_DETECT_COMMAND, singletonList(parameters));
    Type targetClassType = new TypeToken<ArrayList<TestPosition>>() {}.getType();
    try {
      List<TestPosition> positions =
          gson.fromJson(gson.toJson(result.get(TIMEOUT, TimeUnit.SECONDS)), targetClassType);
      return positions.stream().map(TestPositionDto::new).collect(Collectors.toList());
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  /**
   * Compute resolved classpath of the project.
   *
   * @param projectUri project URI
   * @return resolved classpath
   */
  public List<String> getResolvedClasspath(String projectUri) {
    CompletableFuture<Object> result =
        executeCommand(RESOLVE_CLASSPATH_COMMAND, singletonList(projectUri));
    Type targetClassType = new TypeToken<ArrayList<String>>() {}.getType();
    try {
      return gson.fromJson(gson.toJson(result.get(TIMEOUT, TimeUnit.SECONDS)), targetClassType);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  /**
   * Updates classpath of plain java project.
   *
   * @param projectUri project URI
   * @param entries classpath entries
   */
  public void updateClasspath(String projectUri, List<ClasspathEntry> entries) {
    UpdateClasspathParameters params = new UpdateClasspathParameters();
    params.setProjectUri(projectUri);
    params.setEntries(entries);
    executeCommand(UPDATE_PROJECT_CLASSPATH, singletonList(params));
  }

  /**
   * Gets source folders of plain java project.
   *
   * @param projectPath project path
   * @return source folders
   */
  public List<String> getSourceFolders(String projectPath) {
    checkLanguageServerInitialized();

    String projectUri = prefixURI(projectPath);
    Type type = new TypeToken<ArrayList<String>>() {}.getType();
    return doGetList(GET_SOURCE_FOLDERS, projectUri, type);
  }

  private void checkLanguageServerInitialized() {
    if (!findInitializedLanguageServer().isPresent()) {
      throw new IllegalStateException("Language server isn't initialized");
    }
  }

  /**
   * Finds tests in the class.
   *
   * @param fileUri file URI
   * @param methodAnnotation test method annotation
   * @param classAnnotation test class runner annotation
   * @return fqn of the class if it contains tests
   */
  public List<String> findTestsInFile(
      String fileUri, String methodAnnotation, String classAnnotation) {
    return executeFindTestsCommand(
        FIND_TESTS_IN_FILE_COMMAND, fileUri, methodAnnotation, classAnnotation, 0, emptyList());
  }

  /**
   * Finds tests in the project.
   *
   * @param projectUri project folder URI
   * @param methodAnnotation test method annotation
   * @param classAnnotation test class runner annotation
   * @return list of fqns of the classes if they contain tests
   */
  public List<String> findTestsFromProject(
      String projectUri, String methodAnnotation, String classAnnotation) {
    return executeFindTestsCommand(
        FIND_TESTS_FROM_PROJECT_COMMAND,
        projectUri,
        methodAnnotation,
        classAnnotation,
        0,
        emptyList());
  }

  /**
   * Finds tests in the folder.
   *
   * @param folderUri folder URI
   * @param methodAnnotation test method annotation
   * @param classAnnotation test class runner annotation
   * @return list of fqns of the classes if they contain tests
   */
  public List<String> findTestsFromFolder(
      String folderUri, String methodAnnotation, String classAnnotation) {
    return executeFindTestsCommand(
        FIND_TESTS_FROM_FOLDER_COMMAND,
        folderUri,
        methodAnnotation,
        classAnnotation,
        0,
        emptyList());
  }

  /**
   * Finds test by cursor position.
   *
   * @param fileUri URI of active file
   * @param methodAnnotation test method annotation
   * @param classAnnotation test class runner annotation
   * @param offset cursor offset
   * @return fqn of the classes if contains tests and cursor is outside of test method or returns
   *     fqn#methodName if a method is test and cursor is in this method otherwise returns empty
   *     list
   */
  public List<String> findTestsByCursorPosition(
      String fileUri, String methodAnnotation, String classAnnotation, int offset) {
    return executeFindTestsCommand(
        FIND_TEST_BY_CURSOR_COMMAND,
        fileUri,
        methodAnnotation,
        classAnnotation,
        offset,
        emptyList());
  }

  /**
   * Finds fqns of test classes.
   *
   * @param methodAnnotation test method annotation
   * @param classAnnotation test class runner annotation
   * @param entry list of URI of test classes
   * @return fqns of test classes
   */
  public List<String> findTestsFromSet(
      String methodAnnotation, String classAnnotation, List<String> entry) {
    return executeFindTestsCommand(
        FIND_TESTS_FROM_ENTRY_COMMAND, "", methodAnnotation, classAnnotation, 0, entry);
  }

  /**
   * Compute a file structure tree.
   *
   * @param params command parameters {@link FileStructureCommandParameters}
   * @return file structure tree
   */
  private List<ExtendedSymbolInformationDto> executeFileStructure(
      FileStructureCommandParameters params) {
    LOG.info("Requesting files structure for {}", params);
    params.setUri(prefixURI(params.getUri()));
    CompletableFuture<Object> result =
        executeCommand(FILE_STRUCTURE_COMMAND, singletonList(params));
    Type targetClassType = new TypeToken<ArrayList<ExtendedSymbolInformation>>() {}.getType();
    try {
      List<ExtendedSymbolInformation> symbols =
          gson.fromJson(gson.toJson(result.get(TIMEOUT, TimeUnit.SECONDS)), targetClassType);
      return symbols
          .stream()
          .map(
              symbol -> {
                fixLocation(symbol);
                return symbol;
              })
          .map(ExtendedSymbolInformationDto::new)
          .collect(Collectors.toList());
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  public ImplementersResponseDto findImplementers(TextDocumentPositionParams params) {
    params
        .getTextDocument()
        .setUri(LanguageServiceUtils.prefixURI(params.getTextDocument().getUri()));
    CompletableFuture<Object> result =
        executeCommand(FIND_IMPLEMENTERS_COMMAND, singletonList(params));

    Type targetClassType = new TypeToken<ImplementersResponse>() {}.getType();
    try {
      ImplementersResponse implementersResponse =
          gson.fromJson(gson.toJson(result.get(10, TimeUnit.SECONDS)), targetClassType);
      for (SymbolInformation symbolInformation : implementersResponse.getImplementers()) {
        symbolInformation.setLocation(
            LanguageServiceUtils.fixLocation(symbolInformation.getLocation()));
      }
      return new ImplementersResponseDto(implementersResponse);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  /**
   * Retrieves effective pom for specified project.
   *
   * @param projectPath path to project relatively to projects root (e.g. /projects)
   * @return effective pom for given project
   */
  public String getEffectivePom(String projectPath) {
    final String projectUri = prefixURI(projectPath);

    CompletableFuture<Object> result =
        executeCommand(GET_EFFECTIVE_POM_COMMAND, singletonList(projectUri));

    Type targetClassType = new TypeToken<String>() {}.getType();
    try {
      return gson.fromJson(
          gson.toJson(result.get(EFFECTIVE_POM_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)),
          targetClassType);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  /**
   * Updates given maven projects.
   *
   * @param parameters dto with list of paths to projects (relatively to projects root (e.g.
   *     /projects)) which should be re-imported.
   * @return list of paths (relatively to projects root) to projects which were updated.
   */
  public List<String> reImportMavenProjects(ReImportMavenProjectsCommandParameters parameters) {
    final List<String> projectsToReImport = parameters.getProjectsToUpdate();
    if (projectsToReImport.isEmpty()) {
      return emptyList();
    }

    ListIterator<String> iterator = projectsToReImport.listIterator();
    while (iterator.hasNext()) {
      iterator.set(prefixURI(iterator.next()));
    }

    CompletableFuture<Object> requestResult =
        executeCommand(REIMPORT_MAVEN_PROJECTS_COMMAND, singletonList(parameters));

    final List<String> result;
    Type targetClassType = new TypeToken<ArrayList<String>>() {}.getType();
    try {
      result =
          gson.fromJson(
              gson.toJson(
                  requestResult.get(
                      REIMPORT_MAVEN_PROJECTS_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)),
              targetClassType);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }

    iterator = result.listIterator();
    while (iterator.hasNext()) {
      iterator.set(removePrefixUri(iterator.next()));
    }
    return result;
  }

  private void reComputeDiagnostics(String pomPath) {
    String pomUri = prefixURI(pomPath);
    executeCommand(RECOMPUTE_POM_DIAGNOSTICS, singletonList(pomUri));
  }

  private List<Jar> getProjectExternalLibraries(ExternalLibrariesParameters params) {
    params.setProjectUri(prefixURI(params.getProjectUri()));
    Type type = new TypeToken<ArrayList<Jar>>() {}.getType();
    return doGetList(GET_EXTERNAL_LIBRARIES_COMMAND, params, type);
  }

  private List<JarEntry> getExternalLibrariesChildren(ExternalLibrariesParameters params) {
    params.setProjectUri(prefixURI(params.getProjectUri()));
    Type type = new TypeToken<ArrayList<JarEntry>>() {}.getType();
    return doGetList(GET_EXTERNAL_LIBRARIES_CHILDREN_COMMAND, params, type);
  }

  private List<JarEntry> getLibraryChildren(ExternalLibrariesParameters params) {
    params.setProjectUri(prefixURI(params.getProjectUri()));
    Type type = new TypeToken<ArrayList<JarEntry>>() {}.getType();
    return doGetList(GET_LIBRARY_CHILDREN_COMMAND, params, type);
  }

  private List<ClasspathEntry> getClasspathTree(String projectPath) {
    String projectUri = prefixURI(projectPath);
    Type type = new TypeToken<ArrayList<ClasspathEntry>>() {}.getType();
    return doGetList(GET_CLASS_PATH_TREE_COMMAND, projectUri, type);
  }

  private JarEntry getLibraryEntry(String resourceUri) {
    Type type = new TypeToken<JarEntry>() {}.getType();
    return doGetOne(GET_LIBRARY_ENTRY_COMMAND, singletonList(fixJdtUri(resourceUri)), type);
  }

  private List<String> executeFindTestsCommand(
      String commandId,
      String fileUri,
      String methodAnnotation,
      String projectAnnotation,
      int offset,
      List<String> classes) {
    TestFindParameters parameters =
        new TestFindParameters(fileUri, methodAnnotation, projectAnnotation, offset, classes);
    Type type = new TypeToken<ArrayList<String>>() {}.getType();
    return doGetList(commandId, parameters, type);
  }

  public String identifyFqnInResource(String filePath, int lineNumber) {
    Type type = new TypeToken<String>() {}.getType();
    ImmutableList<Object> params =
        ImmutableList.of(prefixURI(filePath), String.valueOf(lineNumber));

    return doGetOne(Commands.IDENTIFY_FQN_IN_RESOURCE, params, type);
  }

  public Location findResourcesByFqn(String fqn, int lineNumber) {
    Type type = new TypeToken<List<Either<String, ResourceLocation>>>() {}.getType();
    List<Either<String, ResourceLocation>> location =
        doGetList(
            Commands.FIND_RESOURCES_BY_FQN,
            ImmutableList.of(fqn, String.valueOf(lineNumber)),
            type);

    if (location.isEmpty()) {
      throw new RuntimeException("Type with fully qualified name: " + fqn + " was not found");
    }
    Either<String, ResourceLocation> l = location.get(0); // TODO let user choose sources

    if (l.isLeft()) {
      return new LocationImpl(removePrefixUri(l.getLeft()), lineNumber, null);
    } else {
      return new LocationImpl(
          l.getRight().getFqn(), lineNumber, true, l.getRight().getLibId(), null);
    }
  }

  /** Update jdt.ls workspace accordingly to added or removed projects. */
  public JobResult updateWorkspace(UpdateWorkspaceParameters updateWorkspaceParameters) {
    if (updateWorkspaceParameters.getAddedProjectsUri().isEmpty()) {
      if (!findInitializedLanguageServer().isPresent()) {
        return new JobResult(
            Severity.OK,
            0,
            "Skipped. Language server not initialized. Workspace updating is not required.");
      }
    }

    Type type = new TypeToken<JobResult>() {}.getType();
    List<Object> params = singletonList(updateWorkspaceParameters);

    try {
      return doGetOne(Commands.UPDATE_WORKSPACE, params, type, 1, TimeUnit.HOURS);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return new JobResult(Severity.ERROR, 1, e.getMessage());
    } finally {
      updateProjectsWithProblems(updateWorkspaceParameters.getAddedProjectsUri());
    }
  }

  private void updateProjectsWithProblems(List<String> addedProjectsUri) {
    for (String projectUri : addedProjectsUri) {
      final String projectPath = removePrefixUri(projectUri);

      projectManager
          .get(projectPath)
          .ifPresent(
              projectConfig -> {
                if (!projectConfig.getProblems().isEmpty()) {
                  try {
                    projectManager.update(projectConfig);
                    eventService.publish(new ProjectUpdatedEvent(projectPath));
                  } catch (Exception e) {
                    LOG.error(format("Failed to update project '%s' configuration", projectUri), e);
                  }
                }
              });
    }
  }

  /** Organizes imports in a file or in a directory. */
  public OrganizeImportsResult organizeImports(OrganizeImportParams organizeImports) {
    organizeImports.setResourceUri(prefixURI(organizeImports.getResourceUri()));

    Type type = new TypeToken<OrganizeImportsResult>() {}.getType();
    return doGetOne(Commands.ORGANIZE_IMPORTS, singletonList(organizeImports), type);
  }

  // configuration

  /**
   * Returns JDT LS java core options.
   *
   * @param filter list of the specific options to return.
   */
  public JavaCoreOptions getJavaCoreOptions(List<String> filter) {
    Type type = new TypeToken<JavaCoreOptions>() {}.getType();
    return doGetOne(Commands.GET_JAVA_CORE_OPTIONS_СOMMAND, new ArrayList<>(filter), type);
  }

  /** Updates JDT LS java core options. */
  public Boolean updateJavaCoreOptions(JavaCoreOptions javaCoreOptions) {
    Type type = new TypeToken<Boolean>() {}.getType();
    return doGetOne(
        Commands.UPDATE_JAVA_CORE_OPTIONS_СOMMAND, singletonList(javaCoreOptions), type);
  }

  /**
   * Returns all nested projects starting from the given path.
   *
   * @param rootPath the root project path
   */
  public List<String> getMavenProjects(String rootPath) {
    Type type = new TypeToken<ArrayList<String>>() {}.getType();
    List<String> projectsUri =
        doGetList(Commands.GET_MAVEN_PROJECTS_COMMAND, singletonList(prefixURI(rootPath)), type);
    return removePrefixUri(projectsUri);
  }

  private <T, P> List<T> doGetList(String command, P params, Type type) {
    return doGetList(command, singletonList(params), type);
  }

  private <T> List<T> doGetList(String command, List<Object> params, Type type) {
    CompletableFuture<Object> result = executeCommand(command, params);
    try {
      return gson.fromJson(gson.toJson(result.get(TIMEOUT, TimeUnit.SECONDS)), type);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private <T> T doGetOne(String command, List<Object> params, Type type) {
    return doGetOne(command, params, type, TIMEOUT, TimeUnit.SECONDS);
  }

  private <T> T doGetOne(
      String command, List<Object> params, Type type, long timeoutInSeconds, TimeUnit timeUnit) {
    CompletableFuture<Object> result = executeCommand(command, params);
    try {
      return gson.fromJson(gson.toJson(result.get(timeoutInSeconds, timeUnit)), type);
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private CompletableFuture<Object> executeCommand(String commandId, List<Object> parameters) {
    ExecuteCommandParams params = new ExecuteCommandParams(commandId, parameters);
    try {
      return getOrInitLanguageServer().getWorkspaceService().executeCommand(params);
    } catch (LanguageServerException e) {
      throw new IllegalStateException(e);
    }
  }

  private LanguageServer getOrInitLanguageServer() throws LanguageServerException {
    Optional<InitializedLanguageServer> languageServer = findInitializedLanguageServer();
    if (languageServer.isPresent()) {
      return languageServer.get().getServer();
    }

    // fake class is used, it is required to find and init language server by its extension
    registry.initialize(prefixURI("init.java"));

    languageServer = findInitializedLanguageServer();
    return languageServer
        .orElseThrow(
            () ->
                new LanguageServerException(
                    "Unexpected error. Language server not found after initialization."))
        .getServer();
  }

  private Optional<InitializedLanguageServer> findInitializedLanguageServer() {
    return registry.findServer(
        server -> (server.getLauncher() instanceof JavaLanguageServerLauncher));
  }

  private void fixLocation(ExtendedSymbolInformation symbol) {
    LanguageServiceUtils.fixLocation(symbol.getInfo().getLocation());
    for (ExtendedSymbolInformation child : symbol.getChildren()) {
      fixLocation(child);
    }
  }

  private UsagesResponse usages(TextDocumentPositionParams parameters) {
    String uri = LanguageServiceUtils.prefixURI(parameters.getUri());
    parameters.setUri(uri);
    parameters.getTextDocument().setUri(uri);
    try {
      Type targetClassType = new TypeToken<ArrayList<UsagesResponse>>() {}.getType();
      List<UsagesResponse> results = doGetList(USAGES_COMMAND, parameters, targetClassType);
      if (results.isEmpty()) {
        return null;
      }
      results
          .get(0)
          .getSearchResults()
          .forEach(
              result -> {
                iterate(
                    result,
                    r -> r.getChildren(),
                    r -> {
                      r.setUri(LanguageServiceUtils.fixUri(r.getUri()));
                    });
              });
      return new DtoServerImpls.UsagesResponseDto(results.get(0));
    } catch (JsonSyntaxException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }

  private <T> void iterate(
      T root, Function<T, List<T>> childrenAccessor, Consumer<T> elementHandler) {
    elementHandler.accept(root);
    for (T child : childrenAccessor.apply(root)) {
      iterate(child, childrenAccessor, elementHandler);
    }
  }
}
