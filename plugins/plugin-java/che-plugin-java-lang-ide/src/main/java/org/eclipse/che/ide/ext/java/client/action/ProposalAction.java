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
package org.eclipse.che.ide.ext.java.client.action;

/**
 * This interface is a general type for all completion proposal actions.
 * You must implement the interface when you add new completion action.
 *
 * @author Valeriy Svydenko
 */
public interface ProposalAction {
    /** Implement this method to provide proposal action handler */
    void performAsProposal();

    /** Returns action id */
    String getId();
}