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
package org.eclipse.che.ide.ext.java.client.editor;

import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.core.AgentURLModifier;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.ext.java.shared.dto.ConflictImportDTO;
import org.eclipse.che.ide.ext.java.shared.dto.OrganizeImportResult;
import org.eclipse.che.ide.ext.java.shared.dto.Problem;
import org.eclipse.che.ide.ext.java.shared.dto.ProposalApplyResult;
import org.eclipse.che.ide.ext.java.shared.dto.Proposals;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
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
  private final AgentURLModifier urlDecorator;

  @Inject
  public JavaCodeAssistClient(
      DtoUnmarshallerFactory unmarshallerFactory,
      AppContext appContext,
      LoaderFactory loaderFactory,
      AsyncRequestFactory asyncRequestFactory,
      AgentURLModifier urlDecorator) {
    this.appContext = appContext;
    this.unmarshallerFactory = unmarshallerFactory;
    this.loader = loaderFactory.newLoader();
    this.asyncRequestFactory = asyncRequestFactory;
    this.urlDecorator = urlDecorator;
  }

  public void computeProposals(
      String projectPath,
      String fqn,
      int offset,
      String contents,
      AsyncRequestCallback<Proposals> callback) {
    String url =
        appContext.getWsAgentServerApiEndpoint()
            + CODE_ASSIST_URL_PREFIX
            + "/compute/completion"
            + "/?projectpath="
            + projectPath
            + "&fqn="
            + fqn
            + "&offset="
            + offset;
    asyncRequestFactory.createPostRequest(url, null).data(contents).send(callback);
  }

  public void computeAssistProposals(
      String projectPath,
      String fqn,
      int offset,
      List<Problem> problems,
      AsyncRequestCallback<Proposals> callback) {
    String url =
        appContext.getWsAgentServerApiEndpoint()
            + CODE_ASSIST_URL_PREFIX
            + "/compute/assist"
            + "/?projectpath="
            + projectPath
            + "&fqn="
            + fqn
            + "&offset="
            + offset;
    List<Problem> prob = new ArrayList<>();
    prob.addAll(problems);
    asyncRequestFactory.createPostRequest(url, prob).send(callback);
  }

  public void applyProposal(
      String sessionId,
      int index,
      boolean insert,
      final AsyncCallback<ProposalApplyResult> callback) {
    String url =
        appContext.getWsAgentServerApiEndpoint()
            + CODE_ASSIST_URL_PREFIX
            + "/apply/completion/?sessionid="
            + sessionId
            + "&index="
            + index
            + "&insert="
            + insert;
    Unmarshallable<ProposalApplyResult> unmarshaller =
        unmarshallerFactory.newUnmarshaller(ProposalApplyResult.class);
    asyncRequestFactory
        .createGetRequest(url)
        .send(
            new AsyncRequestCallback<ProposalApplyResult>(unmarshaller) {
              @Override
              protected void onSuccess(ProposalApplyResult proposalApplyResult) {
                callback.onSuccess(proposalApplyResult);
              }

              @Override
              protected void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
              }
            });
  }

  public String getProposalDocUrl(int id, String sessionId) {
    return urlDecorator.modify(
        appContext.getWsAgentServerApiEndpoint()
            + CODE_ASSIST_URL_PREFIX
            + "/compute/info?sessionid="
            + sessionId
            + "&index="
            + id);
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
