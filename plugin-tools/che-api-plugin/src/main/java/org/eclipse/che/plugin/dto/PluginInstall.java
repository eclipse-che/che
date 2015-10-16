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
package org.eclipse.che.plugin.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Florent Benoit
 */
@DTO
public interface PluginInstall {

    long getId();
    void setId(long id);
    PluginInstall withId(long id);


    PluginInstallStatus getStatus();
    void setStatus(PluginInstallStatus status);
    PluginInstall withStatus(PluginInstallStatus status);


    String getLog();
    void setLog(String log);
    PluginInstall withLog(String log);



}
