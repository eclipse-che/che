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
package org.eclipse.che.ide.command.editor.page.goal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.ui.listbox.CustomComboBox;

import java.util.Set;

/**
 * Implementation of {@link GoalPageView}.
 *
 * @author Artem Zatsarynnyi
 */
public class GoalPageViewImpl extends Composite implements GoalPageView {

    private static final GoalPageViewImplUiBinder UI_BINDER = GWT.create(GoalPageViewImplUiBinder.class);

    @UiField
    CustomComboBox goalComboBox;

    private ActionDelegate delegate;

    @Inject
    public GoalPageViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void setAvailableGoals(Set<CommandGoal> goals) {
        goalComboBox.clear();
        goals.forEach(g -> goalComboBox.addItem(g.getId()));
    }

    @Override
    public void setGoal(String goalId) {
        goalComboBox.setValue(goalId);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @UiHandler({"goalComboBox"})
    void onGoalKeyUp(KeyUpEvent event) {
        delegate.onGoalChanged(goalComboBox.getValue());
    }

    @UiHandler({"goalComboBox"})
    void onGoalChanged(ChangeEvent event) {
        delegate.onGoalChanged(goalComboBox.getValue());
    }

    interface GoalPageViewImplUiBinder extends UiBinder<Widget, GoalPageViewImpl> {
    }
}
