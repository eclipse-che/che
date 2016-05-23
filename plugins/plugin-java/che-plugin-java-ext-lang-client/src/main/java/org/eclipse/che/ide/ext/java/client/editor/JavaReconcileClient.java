/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.editor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.ReconcileResult;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class JavaReconcileClient {

    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final AppContext appContext;

    @Inject
    public JavaReconcileClient(DtoUnmarshallerFactory dtoUnmarshallerFactory,
                               AppContext appContext,
                               AsyncRequestFactory asyncRequestFactory) {
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.asyncRequestFactory = asyncRequestFactory;
    }

    public void reconcile(String projectPath, String fqn, final ReconcileCallback callback) {
        String url = appContext.getDevMachine().getWsAgentBaseUrl() + "/java/reconcile/?projectpath=" + projectPath + "&fqn=" + fqn;
        asyncRequestFactory.createGetRequest(url)
                           .send(new AsyncRequestCallback<ReconcileResult>(dtoUnmarshallerFactory.newUnmarshaller(ReconcileResult.class)) {
                               @Override
                               protected void onSuccess(ReconcileResult result) {
                                   callback.onReconcile(result);
                               }

                               @Override
                               protected void onFailure(Throwable exception) {
                                   Log.error(JavaReconcileClient.class, exception);
                               }
                           });
    }

    public interface ReconcileCallback {
        void onReconcile(ReconcileResult result);
    }
}
