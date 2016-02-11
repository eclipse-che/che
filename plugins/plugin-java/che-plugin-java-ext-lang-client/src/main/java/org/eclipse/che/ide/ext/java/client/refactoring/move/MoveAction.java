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
package org.eclipse.che.ide.ext.java.client.refactoring.move;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.project.node.JavaFileNode;
import org.eclipse.che.ide.ext.java.client.project.node.PackageNode;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.wizard.MovePresenter;

import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType.REFACTOR_MENU;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.PACKAGE;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class MoveAction extends Action {

    private final MovePresenter  movePresenter;
    private final SelectionAgent selectionAgent;

    private List<?>            selectedItems;
    private RefactoredItemType refactoredItemType;

    @Inject
    public MoveAction(JavaLocalizationConstant locale, SelectionAgent selectionAgent, MovePresenter movePresenter) {
        super(locale.moveActionName(), locale.moveActionDescription());

        this.movePresenter = movePresenter;
        this.selectionAgent = selectionAgent;
        this.selectedItems = new ArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent actionEvent) {
        actionEvent.getPresentation().setEnabled(isActionEnable());
    }

    private boolean isActionEnable() {
        Selection<?> selection = selectionAgent.getSelection();

        if (selection == null || selection.isEmpty()) {
            return false;
        }

        List<?> selectedItems = selection.getAllElements();

        this.selectedItems = selectedItems;

        for (Object selectedItem : selectedItems) {
            if (!(selectedItem instanceof HasStorablePath)) {
                return false;
            }

            HasStorablePath item = (HasStorablePath)selectedItem;

            boolean isSourceFileNode = item instanceof JavaFileNode;
            boolean isPackageNode = item instanceof PackageNode;

            if (isSourceFileNode) {
                refactoredItemType = COMPILATION_UNIT;
                return true;
            }

            if (isPackageNode) {
                refactoredItemType = PACKAGE;
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        RefactorInfo refactorInfo = RefactorInfo.of(REFACTOR_MENU, refactoredItemType, selectedItems);

        if (isActionEnable()) {
            movePresenter.show(refactorInfo);
        }
    }
}
