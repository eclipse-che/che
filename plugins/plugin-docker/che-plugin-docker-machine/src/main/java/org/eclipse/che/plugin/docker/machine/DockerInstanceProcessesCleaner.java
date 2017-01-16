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
package org.eclipse.che.plugin.docker.machine;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.machine.server.spi.Instance;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent;
import org.eclipse.che.api.machine.shared.dto.event.MachineProcessEvent.EventType;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Removes process from {@link DockerInstance} on process destroying
 *
 * <p>Uses {@link EventService} to subscribe to processes events.<br>
 * Removes processes on {@code STOPPED} or {@code ERROR} process event.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class DockerInstanceProcessesCleaner implements EventSubscriber<MachineProcessEvent> {
    private final EventService                        eventService;
    private final ConcurrentHashMap<String, Instance> dockerMachines;

    @Inject
    public DockerInstanceProcessesCleaner(EventService eventService) {
        this.eventService = eventService;
        this.dockerMachines = new ConcurrentHashMap<>();
    }

    @Override
    public void onEvent(MachineProcessEvent event) {
        final Instance instance = dockerMachines.get(event.getMachineId());

        if (instance != null && (event.getEventType() == EventType.STOPPED ||
                                 event.getEventType() == EventType.ERROR)) {
            ((DockerInstance)instance).removeProcess(event.getProcessId());
        }
    }

    /**
     * Follows process events of provided instance
     */
    void trackProcesses(Instance instance) {
        dockerMachines.put(instance.getId(), instance);
    }

    /**
     * Stops following process events of instance with provided ID
     */
    void untrackProcesses(String instanceId) {
        dockerMachines.remove(instanceId);
    }

    @PostConstruct
    private void subscribe() {
        eventService.subscribe(this);
    }

    @PreDestroy
    private void unsubscribe() {
        eventService.unsubscribe(this);
    }
}
