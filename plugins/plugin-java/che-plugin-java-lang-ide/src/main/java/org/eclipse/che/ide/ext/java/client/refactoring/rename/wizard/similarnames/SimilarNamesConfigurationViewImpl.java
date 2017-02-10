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
package org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.similarnames;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RenameSettings.MachStrategy;
import org.eclipse.che.ide.ui.window.Window;

/**
 * @author Valeriy Svydenko
 */
@Singleton
final class SimilarNamesConfigurationViewImpl extends Window implements SimilarNamesConfigurationView {

    interface SimilarNamesConfigurationViewImplUiBinder extends UiBinder<Widget, SimilarNamesConfigurationViewImpl> {
    }

    private static SimilarNamesConfigurationViewImplUiBinder UI_BINDER = GWT.create(SimilarNamesConfigurationViewImplUiBinder.class);

    @UiField(provided = true)
    final JavaLocalizationConstant locale;
    @UiField
    Label       errorLabel;
    @UiField
    RadioButton findExactNames;
    @UiField
    RadioButton findEmbeddedNames;
    @UiField
    RadioButton findNameSuffixes;

    private ActionDelegate delegate;

    @Inject
    public SimilarNamesConfigurationViewImpl(JavaLocalizationConstant locale) {
        this.locale = locale;

        setTitle(locale.renameSimilarNamesConfigurationTitle());

        setWidget(UI_BINDER.createAndBindUi(this));

        createButtons(locale);
    }

    /** {@inheritDoc} */
    @Override
    public MachStrategy getMachStrategy() {
        if (findExactNames.getValue()) {
            return MachStrategy.EXACT;
        } else if(findEmbeddedNames.getValue()) {
            return MachStrategy.EMBEDDED;
        } else {
            return MachStrategy.SUFFIX;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void show() {
        findExactNames.setValue(true);
        findNameSuffixes.setValue(false);
        findEmbeddedNames.setValue(false);

        super.show();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    private void createButtons(JavaLocalizationConstant locale) {
        Button cancel = createButton(locale.moveDialogButtonCancel(), "similar-cancel-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                findExactNames.setValue(true);
                findNameSuffixes.setValue(false);
                findEmbeddedNames.setValue(false);

                hide();
            }
        });

        Button accept = createButton(locale.moveDialogButtonOk(), "similar-accept-button", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        addButtonToFooter(accept);
        addButtonToFooter(cancel);
    }

}
