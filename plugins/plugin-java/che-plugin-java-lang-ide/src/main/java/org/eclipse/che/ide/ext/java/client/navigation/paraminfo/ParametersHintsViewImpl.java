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
package org.eclipse.che.ide.ext.java.client.navigation.paraminfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ext.java.shared.dto.model.MethodParameters;

import java.util.List;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class ParametersHintsViewImpl extends PopupPanel implements ParametersHintsView {
    interface ParametersInfoViewImplUiBinder extends UiBinder<Widget, ParametersHintsViewImpl> {
    }

    private static ParametersInfoViewImplUiBinder UI_BINDER = GWT.create(ParametersInfoViewImplUiBinder.class);

    private final Provider<FlowPanel> panelsProvider;

    @UiField
    FlowPanel parametersPanel;

    @Inject
    public ParametersHintsViewImpl(Provider<FlowPanel> panelsProvider) {
        setWidget(UI_BINDER.createAndBindUi(this));

        this.panelsProvider = panelsProvider;

        setAutoHideEnabled(true);
    }

    @Override
    public void show(List<MethodParameters> parametersList, int x, int y) {
        parametersPanel.clear();

        for (MethodParameters parameters : parametersList) {
            FlowPanel widget = panelsProvider.get();

            String parametersLine = parameters.getParameters();
            if (parametersLine.isEmpty()) {
                parametersLine = "<no parameters>";
            }

            String result = parametersLine.replace("<", "&lt").replace(">", "&gt");

            Element element = widget.getElement();
            element.setInnerHTML(result);
            element.getStyle().setColor("yellow");

            parametersPanel.add(widget);
        }

        setPopupPosition(x, y);
        show();
    }

    /** The method used to hide popup with parameters when user press 'Escape' button. */
    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    hide();
                }
                break;
        }
    }
}