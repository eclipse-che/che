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
package org.eclipse.che.ide.command.toolbar.processes;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.api.machine.shared.dto.execagent.GetProcessesResponseDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.ExecAgentCommandManager;
import org.eclipse.che.ide.api.machine.events.WsAgentStateEvent;
import org.eclipse.che.ide.api.machine.events.WsAgentStateHandler;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.dto.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Singleton
public class ProcessesListPresenter implements Presenter {

    private final ProcessesListView       view;

    @Inject
    public ProcessesListPresenter(final ProcessesListView view,
                                  EventBus eventBus,
                                  final ExecAgentCommandManager execAgentCommandManager,
                                  final AppContext appContext,
                                  final DtoFactory dtoFactory) {
        this.view = view;

        eventBus.addHandler(WsAgentStateEvent.TYPE, new WsAgentStateHandler() {
            @Override
            public void onWsAgentStarted(WsAgentStateEvent event) {
                for (Machine machine : appContext.getWorkspace().getRuntime().getMachines()) {
                    execAgentCommandManager.getProcesses(machine.getId(), true).then(new Operation<List<GetProcessesResponseDto>>() {
                        @Override
                        public void apply(List<GetProcessesResponseDto> arg) throws OperationException {
                            List<GetProcessesResponseDto> arg1 = new ArrayList<>();
                            arg1.add(dtoFactory.createDto(GetProcessesResponseDto.class));
                            arg1.add(dtoFactory.createDto(GetProcessesResponseDto.class));
                            arg1.add(dtoFactory.createDto(GetProcessesResponseDto.class));
                            arg1.add(dtoFactory.createDto(GetProcessesResponseDto.class));

                            view.setProcesses(arg1);
                        }
                    });
                }
            }

            @Override
            public void onWsAgentStopped(WsAgentStateEvent event) {

            }
        });
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }
}
