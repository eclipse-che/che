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
package org.eclipse.che.dto.definitions;

/**
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public class Util {
    public static String addPrefix(DtoWithDelegate dto, String prefix) {
        return prefix + dto.getFirstName();
    }

    public static String getFullName(DtoWithDelegate dto) {
        return dto.getFirstName() + dto.getLastName();
    }
}
