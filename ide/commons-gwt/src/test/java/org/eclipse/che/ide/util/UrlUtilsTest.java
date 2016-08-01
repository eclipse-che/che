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
package org.eclipse.che.ide.util;

import org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter;
import org.eclipse.che.ide.ui.loaders.initialization.LoaderView;
import org.eclipse.che.ide.ui.loaders.initialization.LoadingInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.eclipse.che.ide.ui.loaders.initialization.LoaderPresenter.State.WORKING;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UrlUtilsTest {

    @Test
    public void should_replaceHostName_when_hostname_is_an_IP_address() {
        // Given
        String hostnameInBrowser = "localhost";
        String inputUrl = "http://192.58.16.2:8080/api/";

        // When
        String fixedUrl = UrlUtils.replaceHostNameInUrl(inputUrl, hostnameInBrowser);

        // Then
        Assert.assertEquals("http://localhost:8080/api/", fixedUrl);
    }

    @Test
    public void should_replaceHostName_when_hostname_is_fqdn() {
        // Given
        String hostnameInBrowser = "localhost";
        String inputUrl = "http://myhost.example.com:8080/api/";

        // When
        String fixedUrl = UrlUtils.replaceHostNameInUrl(inputUrl, hostnameInBrowser);

        // Then
        Assert.assertEquals("http://localhost:8080/api/", fixedUrl);
    }

}
