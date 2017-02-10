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
package org.eclipse.che.plugin.svn.ide.log;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.ide.ui.window.Window;

/**
 * @author Vitaliy Guliy
 */
@Singleton
public class ShowLogsViewImpl extends Window implements ShowLogsView {

    interface ShowLogsViewImplUiBinder extends UiBinder<HTMLPanel, ShowLogsViewImpl> {
    }

    private static ShowLogsViewImplUiBinder uiBinder = GWT.create(ShowLogsViewImplUiBinder.class);

    private Delegate delegate;

    Button buttonShowLogs;

    Button buttonCancel;

    @UiField
    SpanElement revisionsCount;

    @UiField
    TextBox revisionField;

    @Inject
    public ShowLogsViewImpl(final SubversionExtensionResources svnResources,
                            final SubversionExtensionLocalizationConstants constants) {
        super(true);
        setWidget(uiBinder.createAndBindUi(this));
        setTitle("Show Log...");

        buttonShowLogs = createButton(constants.buttonLog(), "svn-showlogs-show", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.logClicked();
            }
        });
        buttonShowLogs.addStyleName(resources.windowCss().button());
        buttonShowLogs.getElement().getStyle().setMarginRight(8, Style.Unit.PX);
        addButtonToFooter(buttonShowLogs);

        buttonCancel = createButton(constants.buttonCancel(), "svn-showlogs-cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.cancelClicked();
            }
        });
        addButtonToFooter(buttonCancel);
    }

    @Override
    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void setRevisionCount(String revision) {
        revisionsCount.setInnerHTML(revision);
    }

    @Override
    public HasValue<String> rangeField() {
        return revisionField;
    }

}
