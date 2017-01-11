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
package org.eclipse.che.ide.ext.git.client.compare;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.theme.ThemeAgent;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.orion.compare.CompareConfig;
import org.eclipse.che.ide.orion.compare.CompareFactory;
import org.eclipse.che.ide.orion.compare.CompareWidget;
import org.eclipse.che.ide.orion.compare.CompareWidget.ContentCallBack;
import org.eclipse.che.ide.orion.compare.FileOptions;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.window.Window;

/**
 * Implementation of {@link CompareView}.
 *
 * @author Igor Vinokur
 */
@Singleton
final class CompareViewImpl extends Window implements CompareView {

    interface PreviewViewImplUiBinder extends UiBinder<Widget, CompareViewImpl> {
    }

    private static final PreviewViewImplUiBinder UI_BINDER = GWT.create(PreviewViewImplUiBinder.class);

    @UiField
    DockLayoutPanel dockPanel;
    @UiField
    SimplePanel     comparePanel;
    @UiField
    Label           revision;

    @UiField(provided = true)
    final GitLocalizationConstant locale;

    private ActionDelegate delegate;
    private ThemeAgent     themeAgent;
    private CompareWidget  compare;

    private final CompareFactory compareFactory;
    private final LoaderFactory  loaderFactory;

    @Inject
    public CompareViewImpl(CompareFactory compareFactory,
                           GitLocalizationConstant locale,
                           LoaderFactory loaderFactory,
                           ThemeAgent themeAgent) {
        this.compareFactory = compareFactory;
        this.locale = locale;
        this.loaderFactory = loaderFactory;
        this.themeAgent = themeAgent;

        setWidget(UI_BINDER.createAndBindUi(this));

        Button closeButton = createButton(locale.buttonClose(), "git-compare-close-btn", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onClose();
            }
        });

        Button refreshButton = createButton(locale.buttonRefresh(), "git-compare-refresh-btn", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                compare.refresh();
            }
        });

        addButtonToFooter(closeButton);
        addButtonToFooter(refreshButton);

        comparePanel.getElement().setId(Document.get().createUniqueId());
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onClose() {
        compare.getContent(new ContentCallBack() {
            @Override
            public void onContentReceived(String content) {
                delegate.onClose(content);
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void show(String oldContent, String newContent, String revision, String file) {
        dockPanel.setSize(String.valueOf((com.google.gwt.user.client.Window.getClientWidth() / 100) * 95) + "px",
                          String.valueOf((com.google.gwt.user.client.Window.getClientHeight() / 100) * 90) + "px");

        super.show();

        this.revision.setText(revision + locale.compareReadOnlyTitle());

        FileOptions newFile = compareFactory.createFieOptions();
        newFile.setReadOnly(false);

        FileOptions oldFile = compareFactory.createFieOptions();
        oldFile.setReadOnly(true);

        newFile.setContent(newContent);
        newFile.setName(file);
        oldFile.setContent(oldContent);
        oldFile.setName(file);

        CompareConfig compareConfig = compareFactory.createCompareConfig();
        compareConfig.setNewFile(newFile);
        compareConfig.setOldFile(oldFile);
        compareConfig.setShowTitle(false);
        compareConfig.setShowLineStatus(false);

        compare = new CompareWidget(compareConfig, themeAgent.getCurrentThemeId(), loaderFactory);
        comparePanel.clear();
        comparePanel.add(compare);
    }

}
