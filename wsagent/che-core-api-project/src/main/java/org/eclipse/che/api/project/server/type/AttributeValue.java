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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.model.project.type.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gazarenkov
 */
public class AttributeValue implements Value {

    private final List<String> values = new ArrayList<>();

    public AttributeValue(List<String> list) {
        if (list != null) {
            values.addAll(list);
        }
    }

    public AttributeValue(String str) {
        if (str != null) {
            values.add(str);
        }
    }

    @Override
    public String getString() {
        return values.isEmpty() ? null : values.get(0);
    }

    public void setString(String str) {
        values.clear();
        if (str != null) {
            values.add(str);
        }
    }

    @Override
    public List<String> getList() {
        return values;
    }

    public void setList(List<String> list) {
        values.clear();
        if (list != null) {
            values.addAll(list);
        }
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AttributeValue) {
            return this.values.equals(((AttributeValue)obj).getList());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return values != null ? values.hashCode() : 0;
    }
}
