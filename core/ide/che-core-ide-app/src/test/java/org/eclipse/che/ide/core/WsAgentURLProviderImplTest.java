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
package org.eclipse.che.ide.core;

import org.eclipse.che.ide.api.app.AppContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class WsAgentURLProviderImplTest {

    private static final String WS_AGENT_URL = "http://url";

    @Mock
    private AppContext appContext;

    @InjectMocks
    private WsAgentURLProviderImpl provider;

    @Test
    public void urlToWsAgentShouldBeProvided() {
        when(appContext.getWsAgentURL()).thenReturn(WS_AGENT_URL);

        String url = provider.get();

        assertThat(url, is(equalTo(WS_AGENT_URL)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void illegalArgumentExceptionShouldBeThrownWhenUrlIsNull() {
        when(appContext.getWsAgentURL()).thenReturn(null);

        provider.get();
    }
}
