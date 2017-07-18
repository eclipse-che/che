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
package org.eclipse.che.plugin.maven.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Message for translating text message from <b>MavenServerNotifier</b>
 */
@DTO
public interface TextMessageDto extends MavenOutputEventDto {

    String getText();

    /** Returns text message of the output event. */
    void setText(String text);

    TextMessageDto withText(String text);
}
