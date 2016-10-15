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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.user.UserProfileServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Dmitry Shnurenko
 */
@RunWith(MockitoJUnitRunner.class)
public class MachineInfoPresenterTest {

    private static final String SOME_TEXT = "someText";

    //constructor mocks
    @Mock
    private MachineInfoView          view;
    @Mock
    private UserProfileServiceClient userProfile;
    @Mock
    private WorkspaceServiceClient   wsService;
    @Mock
    private DtoUnmarshallerFactory   unmarshallerFactory;

    //additional mocks
    @Mock
    private MachineEntity                machine;
    @Mock
    private AcceptsOneWidget             container;
    @Mock
    private Unmarshallable<ProfileDto>   profileUnmarshaller;
    @Mock
    private Unmarshallable<WorkspaceDto> wsUnmarshaller;
    @Mock
    private ProfileDto                   profileDescriptor;
    @Mock
    private WorkspaceDto                 wsDescriptor;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Promise<WorkspaceDto>        promise;

    @Captor
    private ArgumentCaptor<AsyncRequestCallback<ProfileDto>>   profileCaptor;
    @Captor
    private ArgumentCaptor<AsyncRequestCallback<WorkspaceDto>> wsCaptor;

    @InjectMocks
    private MachineInfoPresenter presenter;

    @Before
    public void setUp() {
        when(machine.getWorkspaceId()).thenReturn(SOME_TEXT);

        when(unmarshallerFactory.newUnmarshaller(ProfileDto.class)).thenReturn(profileUnmarshaller);
        when(unmarshallerFactory.newUnmarshaller(WorkspaceDto.class)).thenReturn(wsUnmarshaller);
    }

    @Test
    public void infoShouldBeUpdated() {
        when(wsService.getWorkspace(SOME_TEXT)).thenReturn(promise);

        presenter.update(machine);

        verify(unmarshallerFactory).newUnmarshaller(ProfileDto.class);

        verify(userProfile).getCurrentProfile(Matchers.<AsyncRequestCallback<ProfileDto>>anyObject());
        verify(machine).getWorkspaceId();
        verify(wsService).getWorkspace(eq(SOME_TEXT));

        verify(view).updateInfo(machine);
    }

    @Test
    public void ownerNameShouldBeSetWhenThereAreFirstAndLastNames() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(MachineInfoPresenter.FIRST_NAME_KEY, "firstName");
        attributes.put(MachineInfoPresenter.LAST_NAME_KEY, "lastName");

        when(profileDescriptor.getAttributes()).thenReturn(attributes);
        when(wsService.getWorkspace(SOME_TEXT)).thenReturn(promise);

        presenter.update(machine);

        verify(userProfile).getCurrentProfile(profileCaptor.capture());
        AsyncRequestCallback<ProfileDto> callback = profileCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method method = callback.getClass().getDeclaredMethod("onSuccess", Object.class);

        method.setAccessible(true);

        method.invoke(callback, profileDescriptor);

        verify(view).setOwner("firstName lastName");
    }

    @Test
    public void ownerEmailShouldBeSetWhenThereAreNotFirstOrLastName() throws Exception {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(MachineInfoPresenter.FIRST_NAME_KEY, "undefined");
        attributes.put(MachineInfoPresenter.LAST_NAME_KEY, "<none>");
        attributes.put(MachineInfoPresenter.EMAIL_KEY, "email");

        when(profileDescriptor.getAttributes()).thenReturn(attributes);
        when(wsService.getWorkspace(SOME_TEXT)).thenReturn(promise);

        presenter.update(machine);

        verify(userProfile).getCurrentProfile(profileCaptor.capture());
        AsyncRequestCallback<ProfileDto> callback = profileCaptor.getValue();

        //noinspection NonJREEmulationClassesInClientCode
        Method method = callback.getClass().getDeclaredMethod("onSuccess", Object.class);

        method.setAccessible(true);

        method.invoke(callback, profileDescriptor);

        verify(view).setOwner("email");
    }

    @Test
    //TODO fix test
    public void workspaceNameShouldBeSet() throws Exception {
        when(machine.getWorkspaceId()).thenReturn(SOME_TEXT);
        WorkspaceConfigDto wsConfigDto = mock(WorkspaceConfigDto.class);
        when(wsDescriptor.getConfig()).thenReturn(wsConfigDto);
        when(wsConfigDto.getName()).thenReturn(SOME_TEXT);
        when(wsService.getWorkspace(SOME_TEXT)).thenReturn(promise);

        presenter.update(machine);

        verify(wsService).getWorkspace(eq(SOME_TEXT));

        verify(machine).getWorkspaceId();
//        verify(wsDescriptor).getName();
//        verify(view).setWorkspaceName(SOME_TEXT);
    }

    @Test
    public void terminalShouldBeDisplayed() {
        presenter.go(container);

        verify(container).setWidget(view);
    }

    @Test
    public void terminalVisibilityShouldBeChanged() {
        presenter.setVisible(true);

        verify(view).setVisible(true);
    }
}
