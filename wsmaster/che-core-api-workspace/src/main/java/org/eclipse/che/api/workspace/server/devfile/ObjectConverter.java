/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ObjectConverter implements AttributeConverter<Serializable, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(Serializable attribute) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(attribute);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return baos.toByteArray();
  }

  @Override
  public Serializable convertToEntityAttribute(byte[] dbData) {
    try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dbData))) {
      return (Serializable) ois.readObject();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
