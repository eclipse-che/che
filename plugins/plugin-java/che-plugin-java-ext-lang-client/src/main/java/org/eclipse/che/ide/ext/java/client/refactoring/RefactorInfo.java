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
package org.eclipse.che.ide.ext.java.client.refactoring;

import org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;

import java.util.List;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class RefactorInfo {

    private final MoveType           moveType;
    private final RefactoredItemType refactoredItemType;
    private final List<?>            selectedItems;

    public static RefactorInfo of(MoveType moveType, RefactoredItemType refactoredItemType, List<?> selectedItems) {
        return new RefactorInfo(moveType, refactoredItemType, selectedItems);
    }

    public static RefactorInfo of(RefactoredItemType refactoredItemType, List<?> selectedItems) {
        return new RefactorInfo(refactoredItemType, selectedItems);
    }

    private RefactorInfo(MoveType moveType, RefactoredItemType refactoredItemType, List<?> selectedItems) {
        this.moveType = moveType;
        this.refactoredItemType = refactoredItemType;
        this.selectedItems = selectedItems;
    }

    private RefactorInfo(RefactoredItemType refactoredItemType, List<?> selectedItems) {
        moveType = null;
        this.refactoredItemType = refactoredItemType;
        this.selectedItems = selectedItems;
    }

    public List<?> getSelectedItems() {
        return selectedItems;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public RefactoredItemType getRefactoredItemType() {
        return refactoredItemType;
    }
}
