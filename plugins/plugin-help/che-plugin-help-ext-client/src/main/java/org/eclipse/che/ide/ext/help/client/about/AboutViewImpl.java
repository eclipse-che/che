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
package org.eclipse.che.ide.ext.help.client.about;

import org.eclipse.che.ide.api.ProductInfoDataProvider;
import org.eclipse.che.ide.ext.help.client.AboutResources;
import org.eclipse.che.ide.ext.help.client.HelpExtensionLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.vectomatic.dom.svg.ui.SVGImage;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * UI for {@link AboutView}.
 *
 * @author Ann Shumilova
 */
@Singleton
public class AboutViewImpl extends Window implements AboutView {
    interface AboutViewImplUiBinder extends UiBinder<Widget, AboutViewImpl> {
    }

    Button btnOk;
    @UiField
    Label                     version;
    @UiField
    Label                     revision;
    @UiField
    Label                     buildTime;
    @UiField(provided = true)
    AboutLocalizationConstant locale;
    @UiField
    FlowPanel                 logoPanel;

    private ActionDelegate delegate;


    @Inject
    public AboutViewImpl(ProductInfoDataProvider productInfoDataProvider,
                         AboutViewImplUiBinder uiBinder,
                         AboutLocalizationConstant locale,
                         HelpExtensionLocalizationConstant coreLocale,
                         AboutResources aboutResources) {
        this.locale = locale;

        aboutResources.aboutCss().ensureInjected();
        String title = locale.aboutControlTitle() + " " + productInfoDataProvider.getName();
        this.setTitle(title);
        this.setWidget(uiBinder.createAndBindUi(this));
        this.ensureDebugId("aboutView-window");

        btnOk = createButton(coreLocale.ok(), "help-about-ok", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                delegate.onOkClicked();
            }
        });
        addButtonToFooter(btnOk);

        logoPanel.add(new SVGImage(productInfoDataProvider.getLogo()));
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        this.hide();
    }

    /** {@inheritDoc} */
    @Override
    public void showDialog() {
        this.show(btnOk);
    }

    @Override
    protected void onEnterClicked() {
        delegate.onOkClicked();
    }

    /** {@inheritDoc} */
    @Override
    public void setVersion(String version) {
        this.version.setText(version);
    }

    /** {@inheritDoc} */
    @Override
    public void setRevision(String revision) {
        this.revision.setText(revision);
    }

    /** {@inheritDoc} */
    @Override
    public void setTime(String time) {
        this.buildTime.setText(time);
    }
}
