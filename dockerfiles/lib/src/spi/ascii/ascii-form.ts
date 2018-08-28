/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
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
