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
package org.eclipse.che.ide.api.factory;

import com.google.gwt.event.shared.GwtEvent;

import org.eclipse.che.api.factory.shared.dto.FactoryDto;


/**
 * Event then indicate that factory accepted. Means that projects are imported, actions applied
 *
 * @author Vitalii Parfonov
 */
public class FactoryAcceptedEvent extends GwtEvent<FactoryAcceptedHandler> {

    private FactoryDto factory;

    public FactoryAcceptedEvent(FactoryDto factory) {
        this.factory = factory;
    }

    public static Type<FactoryAcceptedHandler> TYPE = new Type<>();

    @Override
    public Type<FactoryAcceptedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FactoryAcceptedHandler handler) {
        handler.onFactoryAccepted(this);

    }

    public FactoryDto getFactory() {
        return factory;
    }
}
