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
package org.eclipse.che.ide.extension.maven.client.command;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * The view of {@link MavenCommandPagePresenter}.
 *
 * @author Artem Zatsarynnyi
 */
@ImplementedBy(MavenCommandPageViewImpl.class)
public interface MavenCommandPageView extends View<MavenCommandPageView.ActionDelegate> {

    /** Returns working directory. */
    String getWorkingDirectory();

    /** Sets working directory. */
    void setWorkingDirectory(String workingDirectory);

    /** Returns command line. */
    String getCommandLine();

    /** Sets command line. */
    void setCommandLine(String commandLine);

    /** Action handler for the view actions/controls. */
    interface ActionDelegate {

        /** Called when working directory has been changed. */
        void onWorkingDirectoryChanged();

        /** Called when command line has been changed. */
        void onCommandLineChanged();
    }
}
