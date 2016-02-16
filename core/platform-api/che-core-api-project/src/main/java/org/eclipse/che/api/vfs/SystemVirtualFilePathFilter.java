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
package org.eclipse.che.api.vfs;

/**
 * A low-level filter that filters out virtual files by paths. This is a core filter that is applied globally at system
 * level, and be used to hide system files.
 *
 * @author Tareq Sharafy (tareq.sharafy@sap.com)
 */
public interface SystemVirtualFilePathFilter {

    boolean accept(String workspace, Path path);

}
