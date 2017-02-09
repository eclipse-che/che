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
package org.eclipse.che.plugin.svn.shared;

/**
 * Instructs Subversion to limit the scope of an operation to a particular tree depth. ARG is one of empty (only the target itself), files
 * (the target and any immediate file children thereof), immediates (the target and any immediate children thereof), or infinity (the target
 * and all of its descendants—full recursion).
 *
 * @author Vladyslav Zhukovskyi
 */
public enum Depth {
    DIRS_ONLY("empty", "Only this directory"),
    FILES_ONLY("files", "Only file children"),
    IMMEDIATE_CHILDREN("immediates", "Immediate children (files and directories)"),
    FULLY_RECURSIVE("infinity", "Fully recursive");

    private String value;
    private String description;

    Depth(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public static Depth from(String depth) {
        for (Depth v : Depth.values()) {
            if (v.value.equals(depth)) {
                return v;
            }
        }

        throw new IllegalArgumentException("Failed to resolve depth '" + depth + "'");
    }
}
