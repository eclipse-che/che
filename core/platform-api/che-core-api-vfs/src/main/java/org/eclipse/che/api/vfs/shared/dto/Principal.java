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
package org.eclipse.che.api.vfs.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/** @author <a href="mailto:aparfonov@codenvy.com">Andrey Parfonov</a> */
@DTO
public interface Principal {
    public enum Type {
        USER,
        GROUP
    }

    String getName();

    Principal withName(String name);

    void setName(String name);

    Type getType();

    Principal withType(Type type);

    void setType(Type type);
}
