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
package org.eclipse.che.ide.extension.machine.client.targets.categories.docker;

import org.eclipse.che.ide.api.mvp.View;

/**
 * View to manage docker machine targets.
 *
 * @author Oleksii Orel
 */
public interface DockerView extends View<DockerView.ActionDelegate> {

    /**
     * Sets target name.
     *
     * @param targetName
     */
    void setTargetName(String targetName);

    /**
     * Sets docker machine owner  value.
     *
     * @param owner
     */
    void setOwner(String owner);

    /**
     * Sets docker machine type value.
     *
     * @param type
     */
    void setType(String type);

    /**
     * Sets machine sourceType value.
     *
     * @param sourceType
     */
    void setSourceType(String sourceType);

    /**
     * Sets machine source value.
     *
     * @param source
     */
    void setSource(String source);

    /**
     * Sets machine sourceContent value.
     *
     * @param sourceContent
     */
    void setSourceContent(String sourceContent);

    /**
     * Update target fields on DockerView.
     *
     * @param target
     *          select target
     */
    void updateTargetFields(DockerMachineTarget target);

    /**
     * Restore fields from machine config.
     *
     * @param target
     */
    boolean restoreTargetFields(DockerMachineTarget target);


    interface ActionDelegate {
        boolean onRestoreTargetFields(DockerMachineTarget target);
    }
}
