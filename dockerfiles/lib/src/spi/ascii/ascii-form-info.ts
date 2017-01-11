/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
/**
 * Provides data about ascii forms.
 * @author Florent Benoit
 */
export interface AsciiFormInfo {

    /**
     * Size of the column for the title (key)
     */
    getTitleColumnSize() : number;

    /**
     * Provides size of the column for the values
     */
    getValueColumnSize() : number;

    /**
     * If true, name of the properties need to be uppercase.
     */
    isUppercasePropertyName() : boolean;

}
