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
package org.eclipse.che.ide.ui.status;

import com.google.common.base.Predicate;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import javax.validation.constraints.NotNull;

/**
 * @author Vlad Zhukovskiy
 */
public class StatusText<T extends Widget> implements EmptyStatus<T> {

    public static final String DEFAULT_EMPTY_TEXT = "Nothing to show";

    private final VerticalPanel verticalPanel;

    private String myText = "";

    private T            widget;
    private Predicate<T> showPredicate;

    public StatusText() {

        setText(DEFAULT_EMPTY_TEXT);

        verticalPanel = new VerticalPanel();

        verticalPanel.setHeight("50px");
        verticalPanel.setWidth("100%");

        verticalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        verticalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    }


    @NotNull
    public String getText() {
        return myText;
    }

    public EmptyStatus setText(String text) {
        return clear().appendText(text);
    }

    public EmptyStatus appendText(String text) {
        myText += text;
        return this;
    }

    public StatusText clear() {
        myText = "";
        return this;
    }

    @Override
    public void paint() {
        verticalPanel.clear();

        if (showPredicate.apply(widget)) {
            verticalPanel.add(new Label(getText()));
            widget.getElement().appendChild(verticalPanel.getElement());
        }
    }

    @Override
    public void init(T widget, Predicate<T> showPredicate) {
        this.widget = widget;
        this.showPredicate = showPredicate;
    }
}
