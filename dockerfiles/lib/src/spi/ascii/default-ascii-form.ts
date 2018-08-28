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
import {AsciiForm} from "./ascii-form";
import {FormatterMode} from "./formatter-mode";
import {AsciiFormatter} from "./ascii-formatter";
import {CSVFormatter} from "./csv-formatter";
import {AsciiFormInfo} from "./ascii-form-info";
import {AsciiFormEntry} from "./ascii-form-entry";
import {ModernFormatter} from "./modern-formatter";

/**
 * Default implementation of {@link AsciiForm}
 * @author Florent Benoit
 */
export class DefaultAsciiForm implements AsciiForm {

    private entries:Array<AsciiFormEntry>;

    private  alphabeticalSortValue:boolean = false;

    private  uppercasePropertyName:boolean = false;

    /**
     * Formatters.
     */
    private formatters:Map<FormatterMode, AsciiFormatter>;

    /**
     * Formatter
     */
    private  formatterMode:FormatterMode;

    /**
     * Default constructor
     */
    public constructor() {
        this.entries = new Array<AsciiFormEntry>();
        this.formatters = new Map<FormatterMode, AsciiFormatter>();
        this.formatters.set('MODERN', new ModernFormatter());
        this.formatters.set('CSV', new CSVFormatter());

        this.formatterMode = 'MODERN';
    }


    /**
     * Adds a new entry in the form
     *
     * @param propertyName
     *         the name of the property
     * @param propertyValue
     *         the value of the property
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */

    public  withEntry(propertyName:string, propertyValue:string):AsciiForm {
        this.entries.push(new AsciiFormEntry(propertyName, propertyValue));
        return this;
    }

    /**
     * Order all properties by using alphabetical order.
     *
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */
    public alphabeticalSort():AsciiForm {
        this.alphabeticalSortValue = true;
        return this;
    }

    /**
     * Use uppercase for the property name
     *
     * @return {@link com.codenvy.cli.command.builtin.util.ascii.AsciiForm}
     */

    public  withUppercasePropertyName():AsciiForm {
        this.uppercasePropertyName = true;
        return this;
    }


    public  withFormatter(formatterMode:FormatterMode):AsciiForm {
        this.formatterMode = formatterMode;
        return this;
    }

    /**
     * Transform the given form into an ascii form
     *
     * @return stringified table of the form
     */

    public toAscii():string {
        // compute each line

        // sort entries if alphabetical sort
        if (this.alphabeticalSort) {
            //Collections.sort(entries);
        }

        let output:string = "";

        this.entries.forEach((entry) => {
            // first get title
            let title:string = this.getFormatterMode().formatFormTitle(entry.getName(), new MyAsciiFormInfo(this));
            let value:string = this.getFormatterMode().formatFormValue(entry.getValue(), new MyAsciiFormInfo(this));
            output += title + value + "\n";
        });

        if (output.length > 0 && output.slice(-1) === '\n') {
            // remove it
            output = output.slice(0, -1);
        }

        return output;

    }

    /**
     * @return formatter
     */
    protected  getFormatterMode():AsciiFormatter {
        return this.formatters.get(this.formatterMode);
    }


    public  getTitleColumnSize():number {
        let length:number = 0;
        this.entries.forEach((entry) => {
            // length is without ansi
            length = Math.max(length, entry.getName().length);
        });
        return length;
    }

    public getValueColumnSize():number {
        let length:number = 0;
        this.entries.forEach((entry) => {
            // length is without ansi
            length = Math.max(length, entry.getValue().length);
        });

        return length;
    }

    public isUppercasePropertyName():boolean {
        return this.uppercasePropertyName;
    }

}
export class MyAsciiFormInfo implements AsciiFormInfo {
    private   form:DefaultAsciiForm;

    public constructor(form:DefaultAsciiForm) {
        this.form = form;
    }


    public getTitleColumnSize():number {
        return this.form.getTitleColumnSize();
    }

    public  getValueColumnSize():number {
        return this.form.getValueColumnSize();
    }

    public  isUppercasePropertyName():boolean {
        return this.form.isUppercasePropertyName();
    }
}