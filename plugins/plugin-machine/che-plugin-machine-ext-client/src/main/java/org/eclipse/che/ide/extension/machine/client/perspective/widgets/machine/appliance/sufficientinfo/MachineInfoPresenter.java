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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.machine.appliance.sufficientinfo;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.user.UserProfileServiceClient;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * The class contains business logic which allow change view representation of machine.
 *
 * @author Dmitry Shnurenko
 */
public class MachineInfoPresenter implements TabPresenter {

    public final static String EMAIL_KEY      = "email";
    public final static String FIRST_NAME_KEY = "firstName";
    public final static String LAST_NAME_KEY  = "lastName";

    private final MachineInfoView          view;
    private final UserProfileServiceClient userProfile;
    private final WorkspaceServiceClient   wsService;
    private final DtoUnmarshallerFactory   unmarshallerFactory;

    @Inject
    public MachineInfoPresenter(MachineInfoView view,
                                UserProfileServiceClient userProfile,
                                WorkspaceServiceClient wsService,
                                DtoUnmarshallerFactory unmarshallerFactory) {
        this.view = view;
        this.userProfile = userProfile;
        this.wsService = wsService;
        this.unmarshallerFactory = unmarshallerFactory;
    }

    /**
     * Updates additional information about current machine.
     *
     * @param machine
     *         machine for which need update panel
     */
    public void update(@NotNull MachineEntity machine) {

        Unmarshallable<ProfileDto> profileUnMarshaller = unmarshallerFactory.newUnmarshaller(ProfileDto.class);

        userProfile.getCurrentProfile(new AsyncRequestCallback<ProfileDto>(profileUnMarshaller) {
            @Override
            protected void onSuccess(ProfileDto result) {
                Map<String, String> attributes = result.getAttributes();

                String firstName = attributes.get(FIRST_NAME_KEY);
                String lastName = attributes.get(LAST_NAME_KEY);

                String fullName = firstName + ' ' + lastName;

                String email = attributes.get(EMAIL_KEY);

                boolean isNameExist = !firstName.equals("undefined") && !lastName.equals("<none>");

                view.setOwner(isNameExist ? fullName : email);
            }

            @Override
            protected void onFailure(Throwable exception) {
                Log.error(getClass(), exception);
            }
        });

        wsService.getWorkspace(machine.getWorkspaceId())
                 .then(new Operation<WorkspaceDto>() {
                     @Override
                     public void apply(WorkspaceDto ws) throws OperationException {
                         view.setWorkspaceName(ws.getConfig().getName());
                     }
                 }).catchError(new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError err) throws OperationException {
                Log.error(getClass(), err.getCause());
            }
        });

        view.updateInfo(machine);
    }

    /** {@inheritDoc} */
    @Override
    public IsWidget getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
