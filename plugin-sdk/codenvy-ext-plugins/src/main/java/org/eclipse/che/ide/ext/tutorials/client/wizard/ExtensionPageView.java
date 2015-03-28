/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.tutorials.client.wizard;

import org.eclipse.che.ide.api.mvp.View;
import com.google.inject.ImplementedBy;

/**
 * @author Evgen Vidolob
 */
@ImplementedBy(ExtensionPageViewImpl.class)
public interface ExtensionPageView extends View<ExtensionPageView.ActionDelegate> {
    void reset();

    String getGroupId();

    void setGroupId(String group);

    String getArtifactId();

    void setArtifactId(String artifact);

    String getVersion();

    void setVersion(String value);

    void showArtifactIdMissingIndicator(boolean show);

    void showGroupIdMissingIndicator(boolean show);

    void showVersionMissingIndicator(boolean show);

    public interface ActionDelegate {
        void onTextsChange();
    }
}
