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
package org.eclipse.che.api.machine.server.spi;

import java.util.Map;

/**
 * Describes set of keys that uniquely identifies snapshot of instance in implementation specific way.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 * @author Sergii Kabashniuk
 */
public interface InstanceKey {
    Map<String, String> getFields();
}
