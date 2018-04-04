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
package org.eclipse.che.inject;

import com.google.inject.AbstractModule;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeConverter;
import java.net.URI;
import java.net.URISyntaxException;

/** @author andrew00x */
public class URIConverter extends AbstractModule implements TypeConverter {
  @Override
  public Object convert(String value, TypeLiteral<?> toType) {
    try {
      return new URI(value);
    } catch (URISyntaxException e) {
      throw new ProvisionException(String.format("Invalid URI '%s'", value), e);
    }
  }

  @Override
  protected void configure() {
    convertToTypes(Matchers.only(TypeLiteral.get(URI.class)), this);
  }
}
