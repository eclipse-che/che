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
package org.eclipse.che.ide.ui.loaders.initialization;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link LoaderView}.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class LoaderViewImpl implements LoaderView {

    private static final String LOADING = "LOADING:";

    @UiField(provided = true)
    Resources resources;
    @UiField
    FlowPanel iconPanel;
    @UiField
    FlowPanel expandHolder;
    @UiField
    FlowPanel operations;
    @UiField
    FlowPanel currentOperation;
    @UiField
    FlowPanel operationPanel;
    @UiField
    Label     status;

    DivElement progressBar;
    FlowPanel  rootElement;

    private List<FlowPanel> components;
    private ActionDelegate  delegate;
    private LoaderCss       styles;

    @Inject
    public LoaderViewImpl(LoaderViewImplUiBinder uiBinder,
                          Resources resources) {
        this.resources = resources;

        styles = resources.css();
        styles.ensureInjected();

        rootElement = uiBinder.createAndBindUi(this);

        progressBar = Document.get().createDivElement();
        operationPanel.getElement().appendChild(progressBar);
        operationPanel.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onExpanderClicked();
            }
        }, ClickEvent.getType());
        operations.setVisible(false);

        DivElement expander = Document.get().createDivElement();
        expander.appendChild(resources.expansionIcon().getSvg().getElement());
        expandHolder.getElement().appendChild(expander);
    }

    @Override
    public void setOperations(List<String> operations) {
        components = new ArrayList<>(operations.size());
        this.operations.clear();

        status.setText(LOADING);
        status.setStyleName(styles.inProgressStatusLabel());

        iconPanel.clear();
        iconPanel.getElement().appendChild((resources.loaderIcon().getSvg().getElement()));
        iconPanel.setStyleName(resources.css().iconPanel());

        progressBar.addClassName(styles.progressBarInProgressStatus());
        setProgressBarState(0);

        for (String operation : operations) {
            FlowPanel operationComponent = new FlowPanel();
            HTML label = new HTML(operation);
            operationComponent.setStyleName(styles.waitStatus());
            operationComponent.add(label);

            this.components.add(operationComponent);
            this.operations.add(operationComponent);
        }
    }

    @Override
    public void setCurrentOperation(String operation) {
        currentOperation.clear();
        currentOperation.add(new HTML(operation));
    }

    @Override
    public void setErrorStatus(int index, String operation) {
        iconPanel.clear();
        iconPanel.getElement().appendChild(resources.errorOperationIcon().getSvg().getElement());
        iconPanel.setStyleName(styles.iconPanelErrorStatus());

        progressBar.setClassName(styles.progressBarErrorStatus());
        status.setStyleName(styles.errorStatusLabel());
        setProgressBarState(100);

        FlowPanel operationComponent = components.get(index);
        operationComponent.getElement().removeAllChildren();

        operationComponent.getElement().appendChild((resources.errorOperationIcon().getSvg().getElement()));
        HTML label = new HTML(operation);
        operationComponent.add(label);
        operationComponent.setStyleName(styles.errorStatus());
    }

    @Override
    public void setSuccessStatus(int index, String operation) {
        FlowPanel operationComponent = components.get(index);
        operationComponent.getElement().removeAllChildren();

        operationComponent.getElement().appendChild((resources.completedOperationIcon().getSvg().getElement()));
        HTML label = new HTML(operation);
        operationComponent.add(label);
        operationComponent.setStyleName(styles.completedStatus());
    }

    @Override
    public void setInProgressStatus(int index, String operation) {
        FlowPanel operationComponent = components.get(index);
        operationComponent.getElement().removeAllChildren();

        operationComponent.getElement().appendChild((resources.currentOperationIcon().getSvg().getElement()));
        HTML label = new HTML(operation);
        operationComponent.add(label);
        operationComponent.setStyleName(styles.inProgressStatus());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void expandOperations() {
        operations.setVisible(true);
    }

    @Override
    public void collapseOperations() {
        operations.setVisible(false);
    }

    @Override
    public void setProgressBarState(int percent) {
        progressBar.getStyle().setProperty("width", percent + "%");
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    /** Styles for loader. */
    public interface LoaderCss extends CssResource {
        String errorStatusLabel();

        String inProgressStatusLabel();

        String progressBarErrorStatus();

        String progressBarInProgressStatus();

        String errorStatus();

        String completedStatus();

        String inProgressStatus();

        String waitStatus();

        String iconPanelErrorStatus();

        String statusPanel();

        String iconPanel();

        String operationPanel();

        String operations();

        String currentOperation();

        String expandedIcon();
    }

    /** Resources for loader. */
    public interface Resources extends ClientBundle {
        @Source({"Loader.css"})
        LoaderCss css();

        @Source("expansionIcon.svg")
        SVGResource expansionIcon();

        @Source("loaderIcon.svg")
        SVGResource loaderIcon();

        @Source("arrow.svg")
        SVGResource currentOperationIcon();

        @Source("done.svg")
        SVGResource completedOperationIcon();

        @Source("error.svg")
        SVGResource errorOperationIcon();
    }

    interface LoaderViewImplUiBinder extends UiBinder<FlowPanel, LoaderViewImpl> {
    }
}
