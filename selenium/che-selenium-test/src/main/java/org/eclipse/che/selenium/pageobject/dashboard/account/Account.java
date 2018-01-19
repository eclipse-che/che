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
package org.eclipse.che.selenium.pageobject.dashboard.account;

public class Account {
  private String login;
  private String email;
  private String firstName;
  private String lastName;

  public Account() {
    this.login = "";
    this.email = "";
    this.firstName = "";
    this.lastName = "";
  }

  public Account(String login, String email, String firstName, String lastName) {
    this.login = login;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getLogin() {
    return this.login;
  }

  public String getEmail() {
    return this.email;
  }

  public String getFirstName() {
    return this.firstName;
  }

  public String getLastName() {
    return this.lastName;
  }

  public boolean isEquals(Account accountForCompare) {
    return this.login.equals(accountForCompare.getLogin())
        && this.email.equals(accountForCompare.getEmail())
        && this.firstName.equals(accountForCompare.getFirstName())
        && this.lastName.equals(accountForCompare.getLastName());
  }
}
