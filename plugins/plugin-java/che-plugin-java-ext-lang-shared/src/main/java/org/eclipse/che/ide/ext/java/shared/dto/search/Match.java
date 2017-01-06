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
package org.eclipse.che.ide.ext.java.shared.dto.search;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.ext.java.shared.dto.Region;

/**
 * A textual match in a given object.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface Match {

    /**
     * Match region in file.
     * @return the match region.
     */
    Region getFileMatchRegion();

    void setFileMatchRegion(Region region);

    /**
     * String content of matched line.
     * @return the line content
     */
    String getMatchedLine();

    void setMatchedLine(String matchedLine);

    /**
     * Match region in matched line.
     * Used for UI purpose, to highlight matched word in matched line.
     * @return the match region.
     */
    Region getMatchInLine();

    void setMatchInLine(Region region);

    /**
     * The line number of matched line.
     * @return the line number.
     */
    int getMatchLineNumber();

    void setMatchLineNumber(int lineNumber);

}
