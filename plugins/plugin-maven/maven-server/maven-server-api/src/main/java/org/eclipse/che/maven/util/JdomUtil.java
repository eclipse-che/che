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
package org.eclipse.che.maven.util;

import java.util.List;
import java.util.Objects;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Text;

/**
 * Utility methods for JDOM classes.
 *
 * @author Evgen Vidolob
 */
public class JdomUtil {
  public static boolean isElementEquals(Element e1, Element e2) {
    if (e1 == null && e2 == null) {
      return true;
    }

    if (e1 == null || e2 == null) {
      return false;
    }

    return Objects.equals(e1.getName(), e2.getName())
        && isAttributesEqual(e1.getAttributes(), e2.getAttributes());
  }

  private static boolean isAttributesEqual(List l1, List l2) {
    if (l1.size() != l2.size()) {
      return false;
    }
    for (int i = 0; i < l1.size(); i++) {
      if (!isAttributeEqual((Attribute) l1.get(i), (Attribute) l2.get(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isAttributeEqual(Attribute a1, Attribute a2) {
    return a1.getName().equals(a2.getName()) && a1.getValue().equals(a2.getValue());
  }

  public static int getElementHash(Element element) {
    return getHash(0, element);
  }

  private static int getHash(int hash, Element element) {
    hash = sumHash(hash, element.getName());

    for (Object object : element.getAttributes()) {
      Attribute attribute = (Attribute) object;
      hash = sumHash(hash, attribute.getName());
      hash = sumHash(hash, attribute.getValue());
    }

    for (Object o : element.getContent()) {
      if (o instanceof Element) {
        hash = getHash(hash, (Element) o);
      } else if (o instanceof Text) {
        String text = ((Text) o).getText();
        if (!isNullOrWhitespace(text)) {
          hash = sumHash(hash, text);
        }
      }
    }

    return hash;
  }

  private static int sumHash(int hash, String element) {
    return hash * 31 + element.hashCode();
  }

  public static boolean isNullOrWhitespace(String s) {
    if (s == null) {
      return true;
    }
    return s.isEmpty() | s.trim().isEmpty();
  }
}
