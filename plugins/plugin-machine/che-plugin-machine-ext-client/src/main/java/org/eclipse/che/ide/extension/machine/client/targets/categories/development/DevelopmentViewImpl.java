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
package org.eclipse.che.ide.extension.machine.client.targets.categories.development;


import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.zeroclipboard.ClipboardButtonBuilder;

/**
 * The implementation of {@link DevelopmentView}.
 *
 * @author Oleksii Orel
 */
@Singleton
public class DevelopmentViewImpl implements DevelopmentView {

    private static final DevelopmentViewImplUiBinder UI_BINDER = GWT.create(DevelopmentViewImplUiBinder.class);

    private final FlowPanel rootElement;

    private ActionDelegate delegate;

    @UiField
    TextBox targetName;

    @UiField
    TextBox owner;

    @UiField
    TextBox type;

    @UiField
    TextBox sourceType;

    @UiField
    TextBox source;

    @UiField
    TextArea sourceContent;


    @Inject
    public DevelopmentViewImpl(ClipboardButtonBuilder buttonBuilder) {
        this.rootElement = UI_BINDER.createAndBindUi(this);
        this.rootElement.setVisible(true);

        buttonBuilder.withResourceWidget(source).build();
        buttonBuilder.withResourceWidget(sourceContent).build();
    }


    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void setTargetName(String targetName) {
        this.targetName.setValue(targetName);
    }

    @Override
    public void setOwner(String owner) {
        this.owner.setValue(owner);
    }

    @Override
    public void setType(String type) {
        this.type.setValue(type);
    }

    @Override
    public void setSourceType(String sourceType) {
        this.sourceType.setValue(sourceType);
    }

    @Override
    public void setSource(String source) {
        this.source.setValue(source);
    }

    @Override
    public void setSourceContent(String sourceContent) {
        this.sourceContent.setValue(sourceContent);
    }

    public void updateTargetFields(DevelopmentMachineTarget target) {
        this.setTargetName(target.getName());
        this.setOwner(target.getOwner());
        this.setType(target.getType());
        this.setSourceType(target.getSourceType());

        final String source = target.getSource();

        if (source != null && source.length() > 0) {
            this.setSource(target.getSource());
            this.source.getParent().setVisible(true);
        } else {
            this.setSourceContent(target.getSourceContent());
            this.sourceContent.getParent().setVisible(true);
        }
    }

    @Override
    public boolean restoreTargetFields(DevelopmentMachineTarget target) {
        return this.delegate != null && this.delegate.onRestoreTargetFields(target);
    }


    interface DevelopmentViewImplUiBinder extends UiBinder<FlowPanel, DevelopmentViewImpl> {
    }
}
