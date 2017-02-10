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
package org.eclipse.che.ide.ext.java.shared.dto.refactoring;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * A <code>RefactoringResult</code> object represents the result of a
 * refactoring operation. It manages a list of <code>ChangeInfo</code> objects.
 * Each <code>ChangeInfo</code> object describes one change that was applied.
 *
 * @author Valeriy Svydenko
 */
@DTO
public interface RefactoringResult extends RefactoringStatus {
    /** @return list of the changes which were applied. */
    List<ChangeInfo> getChanges();

    void setChanges(List<ChangeInfo> changes);
}
