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
package org.eclipse.che.datasource.ide.newDatasource.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.resources.client.CssResource;
import com.google.inject.Inject;

import org.eclipse.che.datasource.ide.newDatasource.presenter.NewDatasourceWizardPresenter;
import org.eclipse.che.ide.api.mvp.Presenter;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.loging.Log;

import java.util.HashMap;
import java.util.Map;


public class NewDatasourceWizardHeadViewImpl extends Window implements NewDatasourceWizardHeadView {

    private static NewDatasourceWizardHeadViewImplUiBinder ourUiBinder = GWT.create(NewDatasourceWizardHeadViewImplUiBinder.class);
    @UiField
    TextBox     datasourceName;
    @UiField
    SimplePanel datasourcesPanel;
    @UiField
    SimplePanel settingsPanel;
    @UiField
    Style       style;
    @UiField
    Button      saveButton;
    private ActionDelegate delegate;
    private Map<Presenter, Widget> pageCache = new HashMap<>();

    @Inject
    public NewDatasourceWizardHeadViewImpl(org.eclipse.che.ide.Resources resources) {
        super(true);
        setTitle("New Datasource");
        ensureDebugId("newdatasource");
        setWidget(ourUiBinder.createAndBindUi(this));
    }

    @Override
    public void showPage(Presenter presenter, String place) {
        switch (place) {
            case "categories":
                datasourcesPanel.clear();
                Log.info(NewDatasourceWizardPresenter.class, "catagories clear");
                break;
            case "settings":
                Log.info(NewDatasourceWizardPresenter.class, "Inside switch case");
                settingsPanel.clear();
                break;
            default:
                break;
        }
        if (pageCache.containsKey(presenter)) {
            switch (place) {
                case "categories":
                    datasourcesPanel.add(pageCache.get(presenter));
                    Log.info(NewDatasourceWizardPresenter.class, "catagories cashe");
                    break;
                case "settings":
                    Log.info(NewDatasourceWizardPresenter.class, "Inside page cache");
                    settingsPanel.add(pageCache.get(presenter));
                    break;
                default:
                    break;
            }
        } else {
            switch (place) {
                case "categories":
                    presenter.go(datasourcesPanel);
                    pageCache.put(presenter, datasourcesPanel.getWidget());
                    Log.info(NewDatasourceWizardPresenter.class, "catagories show");
                    break;
                case "settings":
                    Log.info(NewDatasourceWizardPresenter.class, "presenter.go");
                    presenter.go(settingsPanel);
                    pageCache.put(presenter, settingsPanel.getWidget());
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void cleanPage(String place) {
        switch (place) {
            case "categories":
                datasourcesPanel.clear();
                break;
            case "settings":
                settingsPanel.clear();
                break;
            default:
                break;
        }
    }

    @Override
    public void showDialog() {
        show();
    }

    @Override
    public void setEnabledAnimation(boolean enabled) {

    }

    @Override
    public void close() {
        hide();
        cleanPage("categories");
        cleanPage("settings");
        pageCache.clear();
    }

    @Override
    public void setFinishButtonEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    @Override
    public void reset() {
        datasourceName.setText("");
        changeEnabledState(true);
    }

    @Override
    public void enableInput() {
        changeEnabledState(true);
    }

    @Override
    public void disableInput() {
        changeEnabledState(false);
    }

    @Override
    public void setName(String name) {
        datasourceName.setValue(name, true);
    }

    @Override
    public void removeNameError() {
        datasourceName.removeStyleName(style.inputError());
    }

    @Override
    public void showNameError() {
        datasourceName.addStyleName(style.inputError());
    }

    private void changeEnabledState(boolean enabled) {
        datasourceName.setEnabled(enabled);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return null;
    }

    @Override
    protected void onClose() {
        pageCache.clear();
        delegate.onCancelClicked();
    }

    interface Style extends CssResource {

        String datasource();

        String datasourceNamePosition();

        String topPanel();

        String namePanel();

        String namePanelRight();

        String rootPanel();

        String tab();

        String blueButton();

        String disabled();

        String inputError();
    }

    interface NewDatasourceWizardHeadViewImplUiBinder
            extends UiBinder<FlowPanel, NewDatasourceWizardHeadViewImpl> {
    }

}
