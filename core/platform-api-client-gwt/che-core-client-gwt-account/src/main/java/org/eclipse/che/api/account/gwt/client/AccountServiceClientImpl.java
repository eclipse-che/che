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
package org.eclipse.che.api.account.gwt.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.account.shared.dto.AccountDescriptor;
import org.eclipse.che.api.account.shared.dto.MemberDescriptor;
import org.eclipse.che.ide.MimeType;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.HTTPHeader;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Implementation of {@link AccountServiceClient} service.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class AccountServiceClientImpl implements AccountServiceClient {
    private final AsyncRequestFactory asyncRequestFactory;

    @Inject
    public AccountServiceClientImpl(AsyncRequestFactory asyncRequestFactory) {
        this.asyncRequestFactory = asyncRequestFactory;
    }

    /** {@inheritDoc} */
    @Override
    public void getMemberships(AsyncRequestCallback<List<MemberDescriptor>> callback) {
        final String requestUrl = "/api/account/";
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                           .send(callback);
    }

    /** {@inheritDoc} */
    @Override
    public void getAccountById(@NotNull String accountId, AsyncRequestCallback<AccountDescriptor> callback) {
        final String requestUrl = "/api/account/" + accountId;
        asyncRequestFactory.createGetRequest(requestUrl)
                           .header(HTTPHeader.ACCEPT, MimeType.APPLICATION_JSON)
                           .send(callback);
    }
}
