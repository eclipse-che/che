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
package org.eclipse.che.ide.editor.preferences.editorproperties.property;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.TextBox;

import org.eclipse.che.commons.annotation.Nullable;

/**
 * The class provides methods to create and control property's widget which contains name and string value of property.
 *
 * @author Roman Nikitenko
 */
public class EditorStringPropertyWidget extends EditorPropertyBaseWidget implements KeyUpHandler {
    TextBox propertyValueBox;

    public EditorStringPropertyWidget(String name, String value) {
        propertyName.setText(name);

        propertyValueBox = new TextBox();
        propertyValueBox.setVisibleLength(5);
        propertyValueBox.setValue(value);
        propertyValueBox.addKeyUpHandler(this);

        valuePanel.add(propertyValueBox);
    }

    @Nullable
    @Override
    public JSONValue getValue() {
        String value = propertyValueBox.getValue();
        if (!value.isEmpty()) {
            return new JSONString(value);
        }
        return null;
    }

    @Override
    public void setValue(JSONValue value) {
        propertyValueBox.setValue(value.toString());
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
        delegate.onPropertyChanged();
    }
}
