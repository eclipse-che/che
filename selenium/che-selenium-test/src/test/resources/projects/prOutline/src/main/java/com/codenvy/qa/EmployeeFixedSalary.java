/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.qa;

public class EmployeeFixedSalary extends Empl {

    private   double fixedPayment;
    protected long   longVar;
    public    int    intVar;
    private   byte   byteVar;
    protected short  shortVar;
    public    char   charVar;
    float            floatVar ;
    boolean          boolVar;



    public double getFixedPayment() {
        return fixedPayment;
    }

    public void setFixedPayment(double fixedPayment) {
        this.fixedPayment = fixedPayment;
    }

    public double getAvrSalary(){
        return fixedPayment;
    }

    public String toString(){
        return "\nEmployee with fixed salary\n"+getEmployeeNumber()
                +"\n"+getSurname()+"\n"+getDateOfBirth()+"\n"+getFixedPayment()
                +"\n"+getAvrSalary()+"\n";
    }
}
