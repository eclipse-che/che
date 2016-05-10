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
package org.eclipse.che.api.machine.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.exception.MachineException;
import org.eclipse.che.api.machine.server.model.impl.MachineImpl;
import org.eclipse.che.api.machine.server.spi.Instance;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Holds active machines
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class MachineRegistry {
    // TODO add locks per workspace or machine
    private final HashMap<String, Instance>    instances;
    private final HashMap<String, MachineImpl> machines;

    public MachineRegistry() {
        instances = new HashMap<>();
        machines = new HashMap<>();
    }

    //TODO return unmodifiable lists

    /**
     * Get all active machines
     *
     * @throws MachineException
     *         if any error occurs
     */
    public synchronized List<MachineImpl> getMachines() throws MachineException {
        final List<MachineImpl> list = new ArrayList<>(machines.size() + instances.size());
        list.addAll(machines.values());
        list.addAll(instances.values().stream().map(this::toMachine).collect(Collectors.toList()));
        return Collections.unmodifiableList(list);
    }

    /**
     * Get machine by ID, machine can be in running or not
     *
     * @param machineId
     *         id of machine
     * @throws NotFoundException
     *         if machine was not found
     * @throws MachineException
     *         if other error occurs
     */
    public synchronized MachineImpl getMachine(String machineId) throws NotFoundException, MachineException {
        MachineImpl machine = machines.get(machineId);
        if (machine == null) {
            final Instance instance = instances.get(machineId);
            if (instance == null) {
                throw new NotFoundException("Machine " + machineId + " is not found");
            }
            machine = toMachine(instance);
        }

        return machine;
    }

    /**
     * Return true if machine with unique {@code machineId} is exist, or false otherwise.
     *
     * @param machineId
     *         unique machine identifier
     */
    public synchronized boolean isExist(String machineId) {
        return machines.containsKey(machineId) || instances.containsKey(machineId);
    }

    /**
     * Get dev machine of specific workspace. Dev machine should be in RUNNING state
     *
     * @param workspaceId
     *         id of workspace
     * @throws NotFoundException
     *         if dev machine was not found or it is not in RUNNING
     * @throws MachineException
     *         if other error occurs
     */
    public synchronized MachineImpl getDevMachine(String workspaceId) throws NotFoundException, MachineException {
        for (Instance instance : instances.values()) {
            if (instance.getWorkspaceId().equals(workspaceId) && instance.getConfig().isDev()) {
                return toMachine(instance);
            }
        }

        throw new NotFoundException("Dev machine of workspace " + workspaceId + " is not running.");
    }

    /**
     * Get machine by id. It should be in RUNNING state
     *
     * @param machineId
     *         id of machine
     * @return machine with specified id
     * @throws NotFoundException
     *         if machine with specified id not found
     * @throws MachineException
     *         if other error occurs
     */
    public synchronized Instance getInstance(String machineId) throws NotFoundException, MachineException {
        final Instance instance = instances.get(machineId);
        if (instance == null) {
            throw new NotFoundException("Machine " + machineId + " is not found");
        } else {
            return instance;
        }
    }

    /**
     * Add not yet running machine
     *
     * @param machine
     *         machine
     * @throws ConflictException
     *         if machine with the same ID already exists
     * @throws MachineException
     *         if any other error occurs
     */
    public synchronized void addMachine(MachineImpl machine) throws MachineException, ConflictException {
        if (machines.containsKey(machine.getId())) {
            throw new ConflictException("Machine with id " + machine.getId() + " is already exist");
        }
        machines.put(machine.getId(), machine);
    }

    /**
     * Replace not running machine with instance of running machine
     *
     * @param instance
     *         running machine
     * @throws NotFoundException
     *         if not running machine is not found
     * @throws MachineException
     *         if any other error occurs
     */
    public synchronized void update(Instance instance) throws NotFoundException, MachineException {
        if (!instances.containsKey(instance.getId()) && !machines.containsKey(instance.getId())) {
            throw new NotFoundException("Machine " + instance.getId() + " not found");
        } else {
            instances.put(instance.getId(), instance);
            machines.remove(instance.getId());
        }
    }

    /**
     * Remove machine by id
     *
     * @param machineId
     *         id of machine that should be removed
     * @throws NotFoundException
     *         if machine with specified id not found
     */
    public synchronized void remove(String machineId) throws NotFoundException {
        final Instance instance = instances.remove(machineId);
        final MachineImpl machine = machines.remove(machineId);
        if (null == instance && null == machine) {
            throw new NotFoundException("Machine " + machineId + " is not found");
        }
    }

    private MachineImpl toMachine(Instance instance) {
        return new MachineImpl(instance.getConfig(),
                               instance.getId(),
                               instance.getWorkspaceId(),
                               instance.getEnvName(),
                               instance.getOwner(),
                               instance.getStatus(),
                               instance.getRuntime());
    }
}
