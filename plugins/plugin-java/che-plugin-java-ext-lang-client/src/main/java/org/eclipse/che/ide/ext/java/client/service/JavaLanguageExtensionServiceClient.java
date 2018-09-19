/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.client.service;

import static org.eclipse.che.api.promises.client.js.JsPromiseError.create;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.CLASS_PATH_TREE;
import static org.eclipse.che.ide.ext.java.shared.Constants.EFFECTIVE_POM;
import static org.eclipse.che.ide.ext.java.shared.Constants.EFFECTIVE_POM_REQUEST_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARIES;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARIES_CHILDREN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARY_CHILDREN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARY_ENTRY;
import static org.eclipse.che.ide.ext.java.shared.Constants.FILE_STRUCTURE;
import static org.eclipse.che.ide.ext.java.shared.Constants.GET_DESTINATIONS;
import static org.eclipse.che.ide.ext.java.shared.Constants.GET_JAVA_CORE_OPTIONS;
import static org.eclipse.che.ide.ext.java.shared.Constants.GET_LINKED_MODEL;
import static org.eclipse.che.ide.ext.java.shared.Constants.IMPLEMENTERS;
import static org.eclipse.che.ide.ext.java.shared.Constants.ORGANIZE_IMPORTS;
import static org.eclipse.che.ide.ext.java.shared.Constants.RECOMPUTE_POM_DIAGNOSTICS;
import static org.eclipse.che.ide.ext.java.shared.Constants.REFACTORING_GET_RENAME_TYPE;
import static org.eclipse.che.ide.ext.java.shared.Constants.REFACTORING_MOVE;
import static org.eclipse.che.ide.ext.java.shared.Constants.REFACTORING_RENAME;
import static org.eclipse.che.ide.ext.java.shared.Constants.REFACTORING_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.REIMPORT_MAVEN_PROJECTS;
import static org.eclipse.che.ide.ext.java.shared.Constants.REIMPORT_MAVEN_PROJECTS_REQUEST_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.REQUEST_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.UPDATE_JAVA_CORE_OPTIONS;
import static org.eclipse.che.ide.ext.java.shared.Constants.USAGES;
import static org.eclipse.che.ide.ext.java.shared.Constants.VALIDATE_MOVE_COMMAND;
import static org.eclipse.che.ide.ext.java.shared.Constants.VALIDATE_RENAMED_NAME;
import static org.eclipse.che.ide.ext.java.shared.Constants.VERIFY_DESTINATION;

import com.google.gwt.jsonp.client.TimeoutException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.workspace.WorkspaceReadyEvent;
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.CreateMoveParams;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.ImplementersResponse;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaCoreOptions;
import org.eclipse.che.jdt.ls.extension.api.dto.JavaProjectStructure;
import org.eclipse.che.jdt.ls.extension.api.dto.MoveSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportParams;
import org.eclipse.che.jdt.ls.extension.api.dto.OrganizeImportsResult;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringResult;
import org.eclipse.che.jdt.ls.extension.api.dto.RefactoringStatus;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSelectionParams;
import org.eclipse.che.jdt.ls.extension.api.dto.RenameSettings;
import org.eclipse.che.jdt.ls.extension.api.dto.RenamingElementInfo;
import org.eclipse.che.jdt.ls.extension.api.dto.UsagesResponse;
import org.eclipse.che.plugin.languageserver.ide.service.ServiceUtil;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.WorkspaceEdit;

@Singleton
public class JavaLanguageExtensionServiceClient {
  private static final String EVENT_JDT_PROJECT_UPDATED = "event:jdt:project-updated";
  private static final String EVENT_JDT_SUBSCRIBE = "event:jdt:subscribe";

  private final RequestTransmitter requestTransmitter;
  private final RequestHandlerConfigurator configurator;
  private final RequestTransmitter transmitter;
  private final AppContext appContext;

  @Inject
  public JavaLanguageExtensionServiceClient(
      RequestTransmitter requestTransmitter,
      RequestHandlerConfigurator configurator,
      RequestTransmitter transmitter,
      AppContext appContext,
      EventBus eventBus) {
    this.requestTransmitter = requestTransmitter;
    this.configurator = configurator;
    this.transmitter = transmitter;
    this.appContext = appContext;

    eventBus.addHandler(WorkspaceReadyEvent.getType(), e -> subscribe());
  }

  @Inject
  protected void handleEvents() {
    configurator
        .newConfiguration()
        .methodName(EVENT_JDT_PROJECT_UPDATED)
        .paramsAsDto(String.class)
        .noResult()
        .withBiConsumer(
            (endpointId, projectPath) -> {
              onProjectUpdated(projectPath);
            });
  }

  private void subscribe() {
    transmitter
        .newRequest()
        .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
        .methodName(EVENT_JDT_SUBSCRIBE)
        .noParams()
        .sendAndSkipResult();
  }

  public Promise<List<ExtendedSymbolInformation>> fileStructure(
      FileStructureCommandParameters params) {
    return Promises.create(
        (resolve, reject) -> {
          requestTransmitter
              .newRequest()
              .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
              .methodName(FILE_STRUCTURE)
              .paramsAsDto(params)
              .sendAndReceiveResultAsListOfDto(ExtendedSymbolInformation.class, REQUEST_TIMEOUT)
              .onSuccess(resolve::apply)
              .onTimeout(() -> onTimeout(reject))
              .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error)));
        });
  }

  /**
   * Gets effective pom for maven project.
   *
   * @param pathToProject path to project relatively to projects root (e.g. /projects)
   * @return effective pom
   */
  public Promise<String> effectivePom(String pathToProject) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EFFECTIVE_POM)
                .paramsAsString(pathToProject)
                .sendAndReceiveResultAsString(EFFECTIVE_POM_REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(
                    () ->
                        reject.apply(
                            create(new TimeoutException("Timeout while getting effective pom."))))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Updates specified maven projects.
   *
   * @param params contains list of paths to projects which should be reimported
   * @return list of paths to updated projects
   */
  public Promise<List<String>> reImportMavenProjects(
      ReImportMavenProjectsCommandParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(REIMPORT_MAVEN_PROJECTS)
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfString(REIMPORT_MAVEN_PROJECTS_REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> reject.apply(create("Failed to update maven project.")))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets external libraries of the project.
   *
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return list of jars
   */
  public Promise<List<Jar>> externalLibraries(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_LIBRARIES)
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfDto(Jar.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  public Promise<Void> reComputePomDiagnostics(String pomPath) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(RECOMPUTE_POM_DIAGNOSTICS)
                .paramsAsString(pomPath)
                .sendAndSkipResult());
  }

  /**
   * Gets classpath structure.
   *
   * @param projectPath path to the project
   * @return classpath structure
   */
  public Promise<List<ClasspathEntry>> classpathTree(String projectPath) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(CLASS_PATH_TREE)
                .paramsAsString(projectPath)
                .sendAndReceiveResultAsListOfDto(ClasspathEntry.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets children of libraries.
   *
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return list of jars' entries.
   */
  public Promise<List<JarEntry>> externalLibrariesChildren(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_LIBRARIES_CHILDREN)
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfDto(JarEntry.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets children of the library.
   *
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return list of entries
   */
  public Promise<List<JarEntry>> libraryChildren(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_LIBRARY_CHILDREN)
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfDto(JarEntry.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets entry.
   *
   * @param resourceUri
   * @return entry {@link JarEntry}
   */
  public Promise<JarEntry> libraryEntry(String resourceUri) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_LIBRARY_ENTRY)
                .paramsAsString(resourceUri)
                .sendAndReceiveResultAsDto(JarEntry.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Rename refactoring. */
  public Promise<RefactoringResult> rename(RenameSettings renameSettings) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(REFACTORING_RENAME)
                .paramsAsDto(renameSettings)
                .sendAndReceiveResultAsDto(RefactoringResult.class, REFACTORING_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Returns type of Rename refactoring. */
  public Promise<RenamingElementInfo> getRenameType(RenameSelectionParams renameSelection) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(REFACTORING_GET_RENAME_TYPE)
                .paramsAsDto(renameSelection)
                .sendAndReceiveResultAsDto(RenamingElementInfo.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Validates new name. */
  public Promise<RefactoringStatus> validateRenamedName(RenameSelectionParams renameSelection) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(VALIDATE_RENAMED_NAME)
                .paramsAsDto(renameSelection)
                .sendAndReceiveResultAsDto(RefactoringStatus.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Returns linked model. */
  public Promise<List<Range>> getLinkedModeModel(TextDocumentPositionParams linkedModelParams) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(GET_LINKED_MODEL)
                .paramsAsDto(linkedModelParams)
                .sendAndReceiveResultAsListOfDto(Range.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Move refactoring. */
  public Promise<RefactoringResult> move(MoveSettings moveSettings) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(REFACTORING_MOVE)
                .paramsAsDto(moveSettings)
                .sendAndReceiveResultAsDto(RefactoringResult.class, REFACTORING_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Gets destinations for move refactoring. */
  public Promise<List<JavaProjectStructure>> getDestinations() {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(GET_DESTINATIONS)
                .noParams()
                .sendAndReceiveResultAsListOfDto(JavaProjectStructure.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Check if move operation is available for the selected element. */
  public Promise<Boolean> validateMoveCommand(CreateMoveParams moveParams) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(VALIDATE_MOVE_COMMAND)
                .paramsAsDto(moveParams)
                .sendAndReceiveResultAsBoolean(REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /** Check selected destination for move refactoring. */
  public Promise<RefactoringStatus> verifyDestination(MoveSettings moveSettings) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(VERIFY_DESTINATION)
                .paramsAsDto(moveSettings)
                .sendAndReceiveResultAsDto(RefactoringStatus.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Organize imports in a file or in all files in the specific directory.
   *
   * @param organizeImports parameters of the import operation
   * @return {@link WorkspaceEdit} changes to apply
   */
  public Promise<OrganizeImportsResult> organizeImports(OrganizeImportParams organizeImports) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(ORGANIZE_IMPORTS)
                .paramsAsDto(organizeImports)
                .sendAndReceiveResultAsDto(OrganizeImportsResult.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Returns JDT LS java core options.
   *
   * @param filter list of the specific options to return. If list is empty then all available
   *     options will be returned.
   */
  public Promise<JavaCoreOptions> getJavaCoreOptions(List<String> filter) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(GET_JAVA_CORE_OPTIONS)
                .paramsAsListOfString(filter)
                .sendAndReceiveResultAsDto(JavaCoreOptions.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  public Promise<Boolean> updateJavaCoreOptions(JavaCoreOptions javaCoreOptions) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(UPDATE_JAVA_CORE_OPTIONS)
                .paramsAsDto(javaCoreOptions)
                .sendAndReceiveResultAsBoolean()
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Get implementations of the selected element.
   *
   * @return descriptor of the implementations
   */
  public Promise<ImplementersResponse> findImplementations(TextDocumentPositionParams params) {
    return Promises.create(
        (resolve, reject) -> {
          requestTransmitter
              .newRequest()
              .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
              .methodName(IMPLEMENTERS)
              .paramsAsDto(params)
              .sendAndReceiveResultAsDto(ImplementersResponse.class, REQUEST_TIMEOUT)
              .onSuccess(resolve::apply)
              .onTimeout(() -> onTimeout(reject))
              .onFailure(
                  error -> {
                    reject.apply(ServiceUtil.getPromiseError(error));
                  });
        });
  }

  private void onTimeout(RejectFunction reject) {
    reject.apply(
        create(
            new TimeoutException(
                "Looks like the language server is taking to long to respond, please try again in sometime.")));
  }

  private void onProjectUpdated(String projectPath) {
    appContext
        .getWorkspaceRoot()
        .getContainer(projectPath)
        .then(
            container -> {
              if (container.isPresent()) {
                container.get().synchronize();
              }
            });
  }

  public Promise<UsagesResponse> usages(TextDocumentPositionParams params) {
    return Promises.create(
        (resolve, reject) -> {
          requestTransmitter
              .newRequest()
              .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
              .methodName(USAGES)
              .paramsAsDto(params)
              .sendAndReceiveResultAsDto(UsagesResponse.class, REQUEST_TIMEOUT)
              .onSuccess(resolve::apply)
              .onTimeout(() -> reject.apply(JsPromiseError.create(new TimeoutException("Timeout"))))
              .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error)));
        });
  }
}
