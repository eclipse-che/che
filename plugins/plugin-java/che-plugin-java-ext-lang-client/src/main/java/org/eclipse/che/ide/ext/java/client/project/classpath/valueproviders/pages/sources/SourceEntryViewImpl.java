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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.sources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.node.NodeWidget;

/**
 * The implementation of {@link SourceEntryView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class SourceEntryViewImpl extends Composite implements SourceEntryView {
    private static SourceEntryViewImplUiBinder ourUiBinder = GWT.create(SourceEntryViewImplUiBinder.class);

    @UiField
    FlowPanel buttonsPanel;
    @UiField
    FlowPanel sourcePanel;
    @UiField
    Button    addSourceBtn;

    private ActionDelegate delegate;

    @Inject
    public SourceEntryViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        addSourceBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onAddSourceClicked();
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addNode(NodeWidget addedNode) {
        sourcePanel.add(addedNode);
    }

    @Override
    public void removeNode(NodeWidget nodeWidget) {
        sourcePanel.remove(nodeWidget);
    }

    @Override
    public void setAddSourceButtonState(boolean enabled) {
        addSourceBtn.setEnabled(enabled);
    }

    @Override
    public void clear() {
        sourcePanel.clear();
    }

    interface SourceEntryViewImplUiBinder
            extends UiBinder<Widget, SourceEntryViewImpl> {
    }

}
