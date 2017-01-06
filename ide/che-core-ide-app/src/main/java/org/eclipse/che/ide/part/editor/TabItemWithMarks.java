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
package org.eclipse.che.ide.part.editor;

import org.eclipse.che.ide.api.parts.PartStackView;

/**
 * Tab with error and warning marks
 *
 * @author Oleksii Orel
 */
public interface TabItemWithMarks extends PartStackView.TabItem {

    /** Add error mark for Tab title */
    void setErrorMark(boolean isVisible);

    /** Add warning mark for Tab title */
    void setWarningMark(boolean isVisible);

}
