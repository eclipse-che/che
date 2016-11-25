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
package org.eclipse.che.api.factory.server.model.impl;

import org.eclipse.che.api.core.model.factory.Button;
import org.eclipse.che.api.core.model.factory.ButtonAttributes;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

/**
 * Data object for {@link Button}.
 *
 * @author Anton Korneta
 */
@Entity(name = "Button")
@Table(name = "button")
public class ButtonImpl implements Button {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @Embedded
    private ButtonAttributesImpl attributes;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    public ButtonImpl(ButtonAttributes attributes,
                      Type type) {
        this.attributes = new ButtonAttributesImpl(attributes);
        this.type = type;
    }

    public ButtonImpl() {}

    public ButtonImpl(Button button) {
        this(button.getAttributes(), button.getType());
    }

    @Override
    public ButtonAttributesImpl getAttributes() {
        return attributes;
    }

    public void setAttributes(ButtonAttributesImpl attributes) {
        this.attributes = attributes;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ButtonImpl)) return false;
        final ButtonImpl other = (ButtonImpl)obj;
        return Objects.equals(attributes, other.attributes)
               && Objects.equals(type, other.type);
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + Objects.hashCode(attributes);
        result = 31 * result + Objects.hashCode(type);
        return result;
    }

    @Override
    public String toString() {
        return "ButtonImpl{" +
               "attributes=" + attributes +
               ", type=" + type +
               '}';
    }
}
