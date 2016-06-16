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
package org.eclipse.che;

import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.user.server.UserProfileService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests that {@link RemotePreferenceDao} uses correct methods of {@link UserProfileService}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners({MockitoTestNGListener.class, EverrestJetty.class})
public class RemotePreferenceDaoCompatibilityTest {

    @SuppressWarnings("unused") // used by EverrestJetty
    private static final EnvironmentFilter ENVIRONMENT_FILTER = new EnvironmentFilter();
    private static final Subject           TEST_SUBJECT       = new SubjectImpl("name", "id", "token", false);

    @Mock
    private PreferenceDao preferenceDaoMock;

    @Mock
    private UserDao userDao;

    @Mock
    private UserProfileDao userProfileDao;

    @InjectMocks
    private UserProfileService profileService;

    @BeforeMethod
    private void setUp() {
        final EnvironmentContext context = new EnvironmentContext();
        context.setSubject(TEST_SUBJECT);
        EnvironmentContext.setCurrent(context);
    }

    @Test
    public void testGetPreferences(ITestContext ctx) throws Exception {
        final RemotePreferenceDao remoteDao = new RemotePreferenceDao(getUrl(ctx), new DefaultHttpJsonRequestFactory());

        remoteDao.getPreferences(TEST_SUBJECT.getUserId());

        verify(preferenceDaoMock).getPreferences(TEST_SUBJECT.getUserId());
    }

    @Test
    public void testGetPreferencesWithFilter(ITestContext ctx) throws Exception {
        final RemotePreferenceDao remoteDao = new RemotePreferenceDao(getUrl(ctx), new DefaultHttpJsonRequestFactory());

        remoteDao.getPreferences(TEST_SUBJECT.getUserId(), "filter");

        verify(preferenceDaoMock).getPreferences(TEST_SUBJECT.getUserId(), "filter");
    }

    @Test
    public void testSetPreferences(ITestContext ctx) throws Exception {
        final RemotePreferenceDao remoteDao = new RemotePreferenceDao(getUrl(ctx), new DefaultHttpJsonRequestFactory());
        final Map<String, String> prefs = Collections.singletonMap("pref1", "value1");

        remoteDao.setPreferences(TEST_SUBJECT.getUserId(), prefs);

        verify(preferenceDaoMock).setPreferences(TEST_SUBJECT.getUserId(), prefs);
    }

    @Test
    public void testRemovePreferences(ITestContext ctx) throws Exception {
        final RemotePreferenceDao remoteDao = new RemotePreferenceDao(getUrl(ctx), new DefaultHttpJsonRequestFactory());

        remoteDao.remove(TEST_SUBJECT.getUserId());

        verify(preferenceDaoMock).remove(TEST_SUBJECT.getUserId());
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {

        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(TEST_SUBJECT);
            // hacking security context
            try {
                final SecurityContext securityContext = mock(SecurityContext.class, RETURNS_MOCKS);
                final Field scField = request.getClass().getSuperclass().getDeclaredField("securityContext");
                scField.setAccessible(true);
                scField.set(request, securityContext);
            } catch (IllegalAccessException | NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private String getUrl(ITestContext ctx) {
        return "http://localhost:" + ctx.getAttribute(EverrestJetty.JETTY_PORT) + "/rest";
    }
}
