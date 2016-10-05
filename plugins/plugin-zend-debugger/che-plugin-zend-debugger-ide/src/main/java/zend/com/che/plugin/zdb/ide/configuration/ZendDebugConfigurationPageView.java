/*******************************************************************************
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend Technologies - initial API and implementation
 *******************************************************************************/
package zend.com.che.plugin.zdb.ide.configuration;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link ZendDebugConfigurationPagePresenter}.
 *
 * @author Bartlomiej Laczkowski
 */
public interface ZendDebugConfigurationPageView extends View<ZendDebugConfigurationPageView.ActionDelegate> {

    /** Returns client host/IP. */
    String getClientHostIP();

    /** Sets client host/IP. */
    void setClientHostIP(String host);

    /** Returns debug port. */
    int getDebugPort();

    /** Sets debug port. */
    void setDebugPort(int port);

    /** Returns broadcast port. */
    int getBroadcastPort();

    /** Sets broadcast port. */
    void setBroadcastPort(int port);

    /** Returns 'use ssl encryption' flag state. */
    boolean getUseSslEncryption();

    /** Sets 'use ssl encryption' flag state. */
    void setUseSslEncryption(boolean value);

    /** Action handler for the view's controls. */
    interface ActionDelegate {

        /** Called when 'Client host/IP' has been changed. */
        void onClientHostIPChanged();

        /** Called when 'Debug Port' has been changed. */
        void onDebugPortChanged();
        
        /** Called when 'Broadcast Port' has been changed. */
        void onBroadcastPortChanged();

        /** Called when 'Use SSL encryption' flag has been changed. */
        void onUseSslEncryptionChanged(boolean value);
    }
    
}
