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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.machine.shared.dto.MachineDto;

/**
 * The class which describes machine entity.
 *
 * @author Dmitry Shnurenko
 * @author Roman Nikitenko
 */
public class MachineEntityImpl extends org.eclipse.che.ide.api.machine.MachineEntityImpl {

    @Inject
    public MachineEntityImpl(@Assisted MachineDto descriptor) {
        super(descriptor);
    }

}
