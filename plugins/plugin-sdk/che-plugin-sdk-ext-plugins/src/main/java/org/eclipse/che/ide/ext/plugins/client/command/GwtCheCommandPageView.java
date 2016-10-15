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
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link GwtCheCommandPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(GwtCheCommandPageViewImpl.class)
public interface GwtCheCommandPageView extends View<GwtCheCommandPageView.ActionDelegate> {

    /** Returns GWT module name. */
    String getGwtModule();

    /** Sets GWT module name. */
    void setGwtModule(String gwtModule);

    /** Returns GWT code server address. */
    String getCodeServerAddress();

    /** Sets GWT code server address. */
    void setCodeServerAddress(String codeServerAddress);

    /** Returns Che classpath. */
    String getClassPath();

    /** Sets Che classpath. */
    void setClassPath(String classPath);

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Called when GWT module has been changed. */
        void onGwtModuleChanged();

        /** Called when GWT code server address has been changed. */
        void onCodeServerAddressChanged();

        /** Called when classpath has been changed. */
        void onClassPathChanged();
    }
}
