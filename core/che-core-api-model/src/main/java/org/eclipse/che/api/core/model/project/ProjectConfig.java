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

package org.eclipse.che.api.core.model.project;

import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 * @author Dmitry Shnurenko
 */
public interface ProjectConfig {
    String getName();

    String getPath();

    String getDescription();

    String getType();

    List<String> getMixins();

    Map<String, List<String>> getAttributes();

    SourceStorage getSource();

}
