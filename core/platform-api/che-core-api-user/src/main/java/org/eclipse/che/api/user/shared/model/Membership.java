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
package org.eclipse.che.api.user.shared.model;

import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 */
public interface Membership {

    String getScope();

    List<String> getRoles();

    String getUserId();

    String getUserName();

    String getSubjectId();

    Map<String, String> getSubjectProperties();
}
