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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.container;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.content.TabPresenter;
import org.eclipse.che.ide.extension.machine.client.perspective.widgets.tab.header.TabHeader;

import javax.validation.constraints.NotNull;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * The class allows represent tab container on view.
 *
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public class TabContainerViewImpl extends Composite implements TabContainerView, RequiresResize {

    interface TabContainerUiBinder extends UiBinder<Widget, TabContainerViewImpl> {
    }

    private final static TabContainerUiBinder UI_BINDER = GWT.create(TabContainerUiBinder.class);

    @UiField
    FlowPanel tabs;
    @UiField
    FlowPanel content;

    @Inject
    public TabContainerViewImpl() {
        initWidget(UI_BINDER.createAndBindUi(this));
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttach() {
        super.onAttach();
        Style style = getElement().getParentElement().getParentElement().getStyle();
        style.setHeight(100, PCT);
        style.setWidth(100, PCT);
    }

    /** {@inheritDoc} */
    @Override
    public void addHeader(@NotNull TabHeader tabHeader) {
        tabs.add(tabHeader);
    }

    /** {@inheritDoc} */
    @Override
    public void addContent(@NotNull TabPresenter tabPresenter) {
        content.add(tabPresenter.getView());
    }

    @Override
    public void onResize() {
        for (int i = 0; i < content.getWidgetCount(); i++) {
            Widget widget = content.getWidget(i);
            if(widget instanceof RequiresResize){
                ((RequiresResize)widget).onResize();
            }
        }
    }

}