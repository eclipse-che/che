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
package org.eclipse.che.plugin.svn.ide.importer;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View interface for the Subversion project importer.
 *
 * @author vzhukovskii@codenvy.com
 */
public interface SubversionProjectImporterView extends View<SubversionProjectImporterView.ActionDelegate> {

    /** Action handler for the view actions/controls. */
    public interface ActionDelegate {
        /** Performs any actions appropriate in response to the user having changed the project's name. */
        void onProjectNameChanged();

        /** Performs any actions appropriate in response to the user having changed the project's URL. */
        void onProjectUrlChanged();

        /** Performs any actions appropriate in response to the user having changed the project's description. */
        void onProjectDescriptionChanged();

        /** Performs any actions appropriate in response to the user having changed the relative path in the project. */
        void onProjectRelativePathChanged();

        void onCredentialsChanged();
    }

    /** Set error marker. */
    void setNameErrorVisibility(boolean visible);

    /** Set project description. */
    void setProjectDescription(String text);

    /** Return project description. */
    String getProjectDescription();

    /** Set project source URL. */
    void setProjectUrl(final String url);

    /** Return project source URL. */
    String getProjectUrl();

    /** Return project name. */
    String getProjectName();

    /** Set project name. */
    void setProjectName(String name);

    /** Set active window focus to url text box. */
    void setUrlTextBoxFocused();

    /** Return custom relative project path. */
    String getProjectRelativePath();

    /** Return user name. */
    String getUserName();

    /** Return user password. */
    String getPassword();
}
