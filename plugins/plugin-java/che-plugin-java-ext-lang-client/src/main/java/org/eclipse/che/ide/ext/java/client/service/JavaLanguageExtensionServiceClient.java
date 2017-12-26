/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_NODE_CONTENT;
import static org.eclipse.che.ide.ext.java.shared.Constants.FILE_STRUCTURE;
import static org.eclipse.che.ide.ext.java.shared.Constants.ORGANIZE_IMPORTS;
import static org.eclipse.che.ide.ext.java.shared.Constants.REIMPORT_MAVEN_PROJECTS;
import static org.eclipse.che.ide.ext.java.shared.Constants.REIMPORT_MAVEN_PROJECTS_REQUEST_TIMEOUT;
import static org.eclipse.che.ide.ext.java.shared.Constants.REQUEST_TIMEOUT;

import com.google.gwt.jsonp.client.TimeoutException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.jdt.ls.extension.api.dto.ReImportMavenProjectsCommandParameters;
import org.eclipse.che.plugin.languageserver.ide.service.ServiceUtil;
import org.eclipse.lsp4j.WorkspaceEdit;

@Singleton
public class JavaLanguageExtensionServiceClient {
  private final RequestTransmitter requestTransmitter;

  @Inject
  public JavaLanguageExtensionServiceClient(RequestTransmitter requestTransmitter) {
    this.requestTransmitter = requestTransmitter;
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

  /**
   * Gets classpath structure.
   *
   * @param projectPath path to the project
   * @return classpath structure
   */
  public Promise<List<ClasspathEntryDto>> classpathTree(String projectPath) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(CLASS_PATH_TREE)
                .paramsAsString(projectPath)
                .sendAndReceiveResultAsListOfDto(ClasspathEntryDto.class, REQUEST_TIMEOUT)
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
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return entry {@link JarEntry}
   */
  public Promise<JarEntry> libraryEntry(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_LIBRARY_ENTRY)
                .paramsAsDto(params)
                .sendAndReceiveResultAsDto(JarEntry.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets content of the file from the library by file path.
   *
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return content of the file
   */
  public Promise<String> libraryNodeContentByPath(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_NODE_CONTENT)
                .paramsAsDto(params)
                .sendAndReceiveResultAsString(REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Organize imports in a file or in all files in the specific directory.
   *
   * @param path the path to the file or to the directory
   * @return {@link WorkspaceEdit} changes to apply
   */
  public Promise<WorkspaceEdit> organizeImports(String path) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(ORGANIZE_IMPORTS)
                .paramsAsString(path)
                .sendAndReceiveResultAsDto(WorkspaceEdit.class, REQUEST_TIMEOUT)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  private void onTimeout(RejectFunction reject) {
    reject.apply(
        create(
            new TimeoutException(
                "Looks like the language server is taking to long to respond, please try again in sometime.")));
  }
}
