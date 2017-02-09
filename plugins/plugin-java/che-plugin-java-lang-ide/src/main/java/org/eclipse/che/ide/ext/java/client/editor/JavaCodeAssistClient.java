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
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.WsAgentURLModifier;
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

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newCallback;
import static org.eclipse.che.api.promises.client.callback.PromiseHelper.newPromise;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaCodeAssistClient {
    private final static String CODE_ASSIST_URL_PREFIX = "/java/code-assist";

    private final DtoUnmarshallerFactory unmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final MessageLoader          loader;
    private final AppContext             appContext;
    private final WsAgentURLModifier     urlDecorator;

    @Inject
    public JavaCodeAssistClient(DtoUnmarshallerFactory unmarshallerFactory,
                                AppContext appContext,
                                LoaderFactory loaderFactory,
                                AsyncRequestFactory asyncRequestFactory,
                                WsAgentURLModifier urlDecorator) {
        this.appContext = appContext;
        this.unmarshallerFactory = unmarshallerFactory;
        this.loader = loaderFactory.newLoader();
        this.asyncRequestFactory = asyncRequestFactory;
        this.urlDecorator = urlDecorator;
    }

    public void computeProposals(String projectPath, String fqn, int offset, String contents, AsyncRequestCallback<Proposals> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CODE_ASSIST_URL_PREFIX + "/compute/completion" + "/?projectpath=" +
                     projectPath + "&fqn=" + fqn + "&offset=" + offset;
        asyncRequestFactory.createPostRequest(url, null).data(contents).send(callback);
    }

    public void computeAssistProposals(String projectPath, String fqn, int offset, List<Problem> problems,
                                       AsyncRequestCallback<Proposals> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CODE_ASSIST_URL_PREFIX + "/compute/assist" + "/?projectpath=" +
                     projectPath + "&fqn=" + fqn + "&offset=" + offset;
        List<Problem> prob = new ArrayList<>();
        prob.addAll(problems);
        asyncRequestFactory.createPostRequest(url, prob).send(callback);
    }


    public void applyProposal(String sessionId, int index, boolean insert, final AsyncCallback<ProposalApplyResult> callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CODE_ASSIST_URL_PREFIX + "/apply/completion/?sessionid=" +
                     sessionId + "&index=" + index + "&insert=" + insert;
        Unmarshallable<ProposalApplyResult> unmarshaller = unmarshallerFactory.newUnmarshaller(ProposalApplyResult.class);
        asyncRequestFactory.createGetRequest(url).send(new AsyncRequestCallback<ProposalApplyResult>(unmarshaller) {
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
        return urlDecorator.modify(appContext.getDevMachine().getWsAgentBaseUrl() +
                                   CODE_ASSIST_URL_PREFIX + "/compute/info?sessionid=" + sessionId +
                                   "&index=" + id);
    }

    /**
     * Creates edits that describe how to format the given string.
     * Returns the changes required to format source.
     * Note: Java code formatting is supported only.
     *
     * @param offset
     *         The given offset to start recording the edits (inclusive).
     * @param length
     *         the given length to stop recording the edits (exclusive).
     * @param content
     *         the content to format
     */
    public Promise<List<Change>> format(final int offset, final int length, final String content) {

        return newPromise(new AsyncPromiseHelper.RequestCall<List<Change>>() {
            @Override
            public void makeCall(AsyncCallback<List<Change>> callback) {
                String url =
                        appContext.getDevMachine().getWsAgentBaseUrl() + CODE_ASSIST_URL_PREFIX + "/format?offset=" + offset + "&length=" +
                        length;
                asyncRequestFactory.createPostRequest(url, null)
                                   .header(CONTENT_TYPE, MimeType.TEXT_PLAIN)
                                   .data(content)
                                   .send(newCallback(callback, unmarshallerFactory.newListUnmarshaller(Change.class)));
            }
        }).then(new Function<List<Change>, List<Change>>() {
            @Override
            public List<Change> apply(List<Change> arg) throws FunctionException {
                final List<Change> changes = new ArrayList<>();
                for (Change change : arg) {
                    changes.add(change);
                }
                return changes;
            }
        });
    }

    /**
     * Organizes the imports of a compilation unit.
     *
     * @param projectPath
     *         path to the project
     * @param fqn
     *         fully qualified name of the java file
     * @return list of imports which have conflicts
     */
    public Promise<OrganizeImportResult> organizeImports(String projectPath, String fqn) {
        String url =
                appContext.getDevMachine().getWsAgentBaseUrl() + CODE_ASSIST_URL_PREFIX + "/organize-imports?projectpath=" + projectPath +
                "&fqn=" + fqn;
        return asyncRequestFactory.createPostRequest(url, null)
                                  .loader(loader)
                                  .send(unmarshallerFactory.newUnmarshaller(OrganizeImportResult.class));
    }

    /**
     * Organizes the imports of a compilation unit.
     *
     * @param projectPath
     *         path to the project
     * @param fqn
     *         fully qualified name of the java file
     */
    public Promise<List<Change>> applyChosenImports(String projectPath, String fqn, ConflictImportDTO chosen) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + CODE_ASSIST_URL_PREFIX + "/apply-imports?projectpath=" + projectPath +
                     "&fqn=" + fqn;
        return asyncRequestFactory.createPostRequest(url, chosen)
                                  .loader(loader)
                                  .header(CONTENT_TYPE, MimeType.APPLICATION_JSON)
                                  .send(unmarshallerFactory.newListUnmarshaller(Change.class));
    }
}
