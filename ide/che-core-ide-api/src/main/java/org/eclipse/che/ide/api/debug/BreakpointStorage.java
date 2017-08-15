/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.debug;

import org.eclipse.che.api.promises.client.Promise;

import java.util.List;

/**
 * Preserves and loads breakpoints for the active workspace.
 *
 * @author Anatolii Bazko
 */
public interface BreakpointStorage {

    /**
     * Preserves breakpoints into a storage.
     *
     * @param breakpoints
     *      the list of the breakpoints
     */
    void addAll(List<Breakpoint> breakpoints);

    /**
     * Preserve a single breakpoints into a storage.
     *
     * @param breakpoint
     *      the breakpoint
     */
    void add(Breakpoint breakpoint);

    /**
     * Removes breakpoints out of the storage.
     *
     * @param breakpoint
     *      the breakpoint
     */
    void delete(Breakpoint breakpoint);


    /**
     * Removes breakpoints out of the storage.
     *
     * @param breakpoints
     *      the list of the breakpoints
     */
    void deleteAll(List<Breakpoint> breakpoints);

    /**
     * Clears storage.
     */
    void clear();

    /**
     * Loads all breakpoints out of the storage.
     *
     * @return the list of the breakpoints
     */
    Promise<List<Breakpoint>> readAll();
}
