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

import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_CONTENT_NODE_BY_FQN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_CONTENT_NODE_BY_PATH;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARIES;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARIES_CHILDREN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARY_CHILDREN;
import static org.eclipse.che.ide.ext.java.shared.Constants.EXTERNAL_LIBRARY_ENTRY;
import static org.eclipse.che.ide.ext.java.shared.Constants.FILE_STRUCTURE;

import com.google.gwt.jsonp.client.TimeoutException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.jdt.ls.extension.api.dto.ClassContent;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.che.jdt.ls.extension.api.dto.ExternalLibrariesParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.FileStructureCommandParameters;
import org.eclipse.che.jdt.ls.extension.api.dto.Jar;
import org.eclipse.che.jdt.ls.extension.api.dto.JarEntry;
import org.eclipse.che.plugin.languageserver.ide.service.ServiceUtil;

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
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(FILE_STRUCTURE)
                .paramsAsDto(params)
                .sendAndReceiveResultAsListOfDto(ExtendedSymbolInformation.class, 10000)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
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
                .sendAndReceiveResultAsListOfDto(Jar.class, 10000)
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
                .sendAndReceiveResultAsListOfDto(JarEntry.class, 10000)
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
                .sendAndReceiveResultAsListOfDto(JarEntry.class, 10000)
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
                .sendAndReceiveResultAsDto(JarEntry.class, 10000)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets content of the file from the library by file path.
   *
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return content {@link ClassContent}
   */
  public Promise<ClassContent> libraryNodeContentByPath(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_CONTENT_NODE_BY_PATH)
                .paramsAsDto(params)
                .sendAndReceiveResultAsDto(ClassContent.class, 10000)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  /**
   * Gets content of the file from the library by fqn.
   *
   * @param params external libraries parameters {@link ExternalLibrariesParameters}
   * @return content {@link ClassContent}
   */
  public Promise<ClassContent> libraryNodeContentByFqn(ExternalLibrariesParameters params) {
    return Promises.create(
        (resolve, reject) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(EXTERNAL_CONTENT_NODE_BY_FQN)
                .paramsAsDto(params)
                .sendAndReceiveResultAsDto(ClassContent.class, 10000)
                .onSuccess(resolve::apply)
                .onTimeout(() -> onTimeout(reject))
                .onFailure(error -> reject.apply(ServiceUtil.getPromiseError(error))));
  }

  private void onTimeout(RejectFunction reject) {
    reject.apply(
        new PromiseError() {
          TimeoutException t = new TimeoutException("Timeout");

          @Override
          public String getMessage() {
            return t.getMessage();
          }

          @Override
          public Throwable getCause() {
            return t;
          }
        });
  }
}
