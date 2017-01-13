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
package org.eclipse.che.ide.api.resources;

/**
 * Represents mechanism for getting and setting value of the content stamp. This value is changed when file is modified.
 *
 * @author Valeriy Svydenko
 */
public interface ModificationTracker {

    /**
     * Sets modification tracker value.
     *
     * @param  stamp encoded content
     */
    void setModificationStamp(String stamp);

    /**
     * Gets modification tracker value. Modification tracker is a value is changed by any modification of the content of the file.
     *
     * @return modification tracker value
     */
    String getModificationStamp();
}
