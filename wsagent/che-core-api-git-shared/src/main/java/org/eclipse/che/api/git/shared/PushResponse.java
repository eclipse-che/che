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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;
import java.util.List;
import java.util.Map;

/**
 * Info received from push response
 *
 * @author Igor Vinokur
 */
@DTO
public interface PushResponse {

    /** set output message */
    void setCommandOutput(String commandOutput);

    /** @return output message */
    String getCommandOutput();

    PushResponse withCommandOutput(String commandOutput);

    /** set list of push updates */
    void setUpdates(List<Map<String, String>> updates);

    /** @return list of push updates */
    List<Map<String, String>> getUpdates();

    PushResponse withUpdates(List<Map<String, String>> updates);
}
