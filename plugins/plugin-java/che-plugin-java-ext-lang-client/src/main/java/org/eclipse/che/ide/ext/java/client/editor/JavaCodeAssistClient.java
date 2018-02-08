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
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

/** @author Evgen Vidolob */
@Singleton
public class JavaCodeAssistClient {
  private static final String CODE_ASSIST_URL_PREFIX = "/java/code-assist";

  private final DtoUnmarshallerFactory unmarshallerFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final MessageLoader loader;
  private final AppContext appContext;

  @Inject
  public JavaCodeAssistClient(
      DtoUnmarshallerFactory unmarshallerFactory,
      AppContext appContext,
      LoaderFactory loaderFactory,
      AsyncRequestFactory asyncRequestFactory) {
    this.appContext = appContext;
    this.unmarshallerFactory = unmarshallerFactory;
    this.loader = loaderFactory.newLoader();
    this.asyncRequestFactory = asyncRequestFactory;
  }

  /**
   * Organizes the imports of a compilation unit.
   *
   * @param projectPath path to the project
   * @param fqn fully qualified name of the java file
   * @return list of imports which have conflicts
   */
  public Promise<OrganizeImportResult> organizeImports(String projectPath, String fqn) {
    String url =
        appContext.getWsAgentServerApiEndpoint()
            + CODE_ASSIST_URL_PREFIX
            + "/organize-imports?projectpath="
            + projectPath
            + "&fqn="
            + fqn;
    return asyncRequestFactory
        .createPostRequest(url, null)
        .loader(loader)
        .send(unmarshallerFactory.newUnmarshaller(OrganizeImportResult.class));
  }

  /**
   * Organizes the imports of a compilation unit.
   *
   * @param projectPath path to the project
   * @param fqn fully qualified name of the java file
   */
  public Promise<List<Change>> applyChosenImports(
      String projectPath, String fqn, ConflictImportDTO chosen) {
    String url =
        appContext.getWsAgentServerApiEndpoint()
            + CODE_ASSIST_URL_PREFIX
            + "/apply-imports?projectpath="
            + projectPath
            + "&fqn="
            + fqn;
    return asyncRequestFactory
        .createPostRequest(url, chosen)
        .loader(loader)
        .header(CONTENT_TYPE, MimeType.APPLICATION_JSON)
        .send(unmarshallerFactory.newListUnmarshaller(Change.class));
  }
}
