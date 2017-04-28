/*******************************************************************************
 * Copyright (c) 2017 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.debug.shared.model;

/**
 * Debug session state
 * 
 * @author Bartlomiej Laczkowski
 */
public interface DebugSessionState {
    
    /**
     * Returns debug session state.
     * 
     * @return debug session state
     */
    String getDebuggerType();
    
    /**
     * Returns debug session data.
     * 
     * @return debug session data
     */
    DebugSession getDebugSession();
    
    /**
     * Returns debug file location.
     * 
     * @return debug file location
     */
    Location getLocation();
    
}
