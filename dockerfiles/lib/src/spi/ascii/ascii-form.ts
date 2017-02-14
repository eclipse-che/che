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
import {FormatterMode} from "./formatter-mode";

/**
 * AsciiForm allow to build forms (key, value) with all values being left aligned  in a simple way.
 * No need for each command to build its own logic.
 * There are CSV, Modern formatters, etc.
 * @author Florent Benoit
 */

export  interface AsciiForm {

    /**
     * Adds a new entry in the form
     * @param propertyName the name of the property
     * @param propertyValue the value of the property
     * @return {@link AsciiForm}
     */
    withEntry(propertyName:string, propertyValue:string) : AsciiForm;

    /**
     * Order all properties by using alphabetical order.
     * @return {@link AsciiForm}
     */
    alphabeticalSort() : AsciiForm;

    /**
     * Use uppercase for the property name
     * @return {@link AsciiForm}
     */
    withUppercasePropertyName() : AsciiForm;

    /**
     * Allow to pickup formatter
     * @param formatterMode
     */
    withFormatter(formatterMode:FormatterMode) : AsciiForm;

    /**
     * Transform the given form into an ascii form
     *
     * @return stringified table of the form
     */
    toAscii() : string;

}
