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
package org.eclipse.che;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests of {@link RemotePreferenceDao}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class RemotePreferenceDaoTest {

    private static final String  API_ENDPOINT = "http://localhost:8000/api";
    private static final Subject TEST_SUBJECT = new SubjectImpl("name", "user123", "token", false);

    @Mock
    private HttpJsonRequestFactory requestFactory;
    @Mock
    private HttpJsonResponse       response;
    private HttpJsonRequest        request;
    private RemotePreferenceDao    preferenceDao;

    @BeforeMethod
    private void setUp() throws Exception {
        preferenceDao = new RemotePreferenceDao(API_ENDPOINT, requestFactory);
        request = mock(HttpJsonRequest.class, (Answer)invocation -> {
            if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                return invocation.getMock();
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(request.request()).thenReturn(response);
        when(requestFactory.fromUrl(anyString())).thenReturn(request);
        final EnvironmentContext context = new EnvironmentContext();
        context.setSubject(TEST_SUBJECT);
        EnvironmentContext.setCurrent(context);
    }

    @Test
    public void shouldGetPreferences() throws Exception {
        final Map<String, String> prefs = singletonMap("name", "value");
        when(response.asProperties()).thenReturn(prefs);

        final Map<String, String> result = preferenceDao.getPreferences(TEST_SUBJECT.getUserId());

        assertEquals(result, prefs);
    }

    @Test
    public void shouldGetPreferenceWithFilter() throws Exception {
        final Map<String, String> prefs = singletonMap("name", "value");
        when(response.asProperties()).thenReturn(prefs);

        final Map<String, String> result = preferenceDao.getPreferences(TEST_SUBJECT.getUserId(), "filter");

        assertEquals(result, prefs);
    }

    @Test
    public void shouldRemovePreferences() throws Exception {
        preferenceDao.remove(TEST_SUBJECT.getUserId());

        verify(request).request();
    }

    @Test
    public void shouldSetPreferences() throws Exception {
        final Map<String, String> prefs = singletonMap("name", "value");

        preferenceDao.setPreferences(TEST_SUBJECT.getUserId(), prefs);

        verify(request).setBody(prefs);
        verify(request).request();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenGettingPreferencesWithNullUserId() throws Exception {
        preferenceDao.getPreferences(null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenGettingPreferencesWithNullFilter() throws Exception {
        preferenceDao.getPreferences("user123", null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenSettingPreferencesWithNullUserId() throws Exception {
        preferenceDao.setPreferences(null, emptyMap());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenSettingPreferencesWithNullPreferences() throws Exception {
        preferenceDao.setPreferences("user123", null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenRemovingPreferencesWithNullUserId() throws Exception {
        preferenceDao.remove(null);
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenGettingPreferencesForUserDifferentFromCurrent() throws Exception {
        preferenceDao.getPreferences("fake");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenGettingPreferencesWithFilterForUserDifferentFromCurrent() throws Exception {
        preferenceDao.getPreferences("fake", "filter");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenSettingsForUserDifferentFromCurrent() throws Exception {
        preferenceDao.setPreferences("fake", emptyMap());
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionWhenRemovingPreferencesForUserDifferentFromCurrent() throws Exception {
        preferenceDao.getPreferences("fake");
    }

    @Test(expectedExceptions = ServerException.class, dataProvider = "exceptions")
    public void shouldRethrowExceptionAsServerExceptionWhenGettingPreferences(Exception ex) throws Exception {
        when(request.request()).thenThrow(ex);

        preferenceDao.getPreferences(TEST_SUBJECT.getUserId());
    }

    @Test(expectedExceptions = ServerException.class, dataProvider = "exceptions")
    public void shouldRethrowExceptionAsServerExceptionWhenGettingPreferencesWithFilter(Exception ex) throws Exception {
        when(request.request()).thenThrow(ex);

        preferenceDao.getPreferences(TEST_SUBJECT.getUserId(), "filter");
    }

    @Test(expectedExceptions = ServerException.class, dataProvider = "exceptions")
    public void shouldRethrowExceptionAsServerExceptionWhenSettingPreferences(Exception ex) throws Exception {
        when(request.request()).thenThrow(ex);

        preferenceDao.setPreferences(TEST_SUBJECT.getUserId(), emptyMap());
    }

    @Test(expectedExceptions = ServerException.class, dataProvider = "exceptions")
    public void shouldRethrowExceptionAsServerExceptionWhenRemovingPreferences(Exception ex) throws Exception {
        when(request.request()).thenThrow(ex);

        preferenceDao.remove(TEST_SUBJECT.getUserId());
    }

    @DataProvider(name = "exceptions")
    public Object[][] restExceptions() {
        return new Object[][] {
                {new BadRequestException("test bad request exception")},
                {new UnauthorizedException("test unauthorized exception")},
                {new ForbiddenException("test forbidden exception")},
                {new NotFoundException("test not found exception")},
                {new ConflictException("test conflict exception")},
                {new ServerException("test server exception")},
                {new IOException("text io exception")}
        };
    }
}
