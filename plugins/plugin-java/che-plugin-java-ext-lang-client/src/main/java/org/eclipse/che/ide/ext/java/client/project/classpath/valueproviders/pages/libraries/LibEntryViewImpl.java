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
package org.eclipse.che.ide.ext.java.client.project.classpath.valueproviders.pages.libraries;

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
 * The implementation of {@link LibEntryView}.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class LibEntryViewImpl extends Composite implements LibEntryView {
    private static LibPropertyViewImplUiBinder ourUiBinder = GWT.create(LibPropertyViewImplUiBinder.class);

    @UiField
    FlowPanel buttonsPanel;
    @UiField
    FlowPanel libraryPanel;
    @UiField
    Button    addJarBtn;
    @UiField
    Button    addFolderBtn;

    private ActionDelegate delegate;

    @Inject
    public LibEntryViewImpl() {
        initWidget(ourUiBinder.createAndBindUi(this));

        addJarBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onAddJarClicked();
            }
        });

        addFolderBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onAddClassFolderClicked();
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addNode(NodeWidget addedNode) {
        libraryPanel.add(addedNode);
    }

    @Override
    public void removeNode(NodeWidget nodeWidget) {
        libraryPanel.remove(nodeWidget);
    }

    @Override
    public void setAddJarButtonState(boolean enabled) {
        addJarBtn.setEnabled(enabled);
    }

    @Override
    public void setAddClassFolderJarButtonState(boolean enabled) {
        addFolderBtn.setEnabled(enabled);
    }

    @Override
    public void clear() {
        libraryPanel.clear();
    }

    interface LibPropertyViewImplUiBinder
            extends UiBinder<Widget, LibEntryViewImpl> {
    }

}
