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
package org.eclipse.che.plugin.svn.ide.resolve;

public enum ConflictResolutionAction {

    POSTPONE("postpone"),
    BASE("base"),
    WORKING("working"),
    MINE_FULL("mine-full"),
    THEIRS_FULL("theirs-full"),
    MINE_CONFLICT("mine-conflict"),
    THEIRS_CONFLICT("theirs-conflict");

    private String text;

    ConflictResolutionAction(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static ConflictResolutionAction getEnum(String text) {
        switch (text) {
            case "postpone":
                return POSTPONE;
            case "base":
                return BASE;
            case "working":
                return WORKING;
            case "mine-full":
                return MINE_FULL;
            case "theirs-full":
                return THEIRS_FULL;
            case "mine-conflict":
                return MINE_CONFLICT;
            case "theirs-conflict":
                return THEIRS_CONFLICT;
            default:
                return null;
        }
    }

}
