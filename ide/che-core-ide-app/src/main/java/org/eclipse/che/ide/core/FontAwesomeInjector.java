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
package org.eclipse.che.ide.core;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.component.Component;

/**
 * Font awesome style injector component.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class FontAwesomeInjector implements Component {

    @Override
    public void start(Callback<Component, Exception> callback) {
        LinkElement link = Document.get().createLinkElement();
        link.setRel("stylesheet");
        link.setHref(GWT.getModuleBaseForStaticFiles() + "font-awesome-4.5.0/css/font-awesome.min.css");

        Document.get().getHead().appendChild(link);

        callback.onSuccess(this);
    }

}
