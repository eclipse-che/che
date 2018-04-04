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
package org.eclipse.che.dto.definitions;

import org.eclipse.che.dto.shared.DTO;

/**
 * Keeps DTO interfaces for hierarchy test
 *
 * @author Eugene Voevodin
 */
public final class DTOHierarchy {

  public interface Parent {

    String getParentField();
  }

  public interface Child extends Parent {

    String getChildField();
  }

  @DTO
  public interface ChildDto extends Child {

    String getDtoField();

    void setDtoField(String dtoField);

    ChildDto withDtoField(String dtoField);

    void setChildField(String childField);

    ChildDto withChildField(String childField);

    void setParentField(String parentField);

    ChildDto withParentField(String parentField);

    ChildDto getShadowedField();

    void setShadowedField(ChildDto v);
  }

  @DTO
  public interface GrandchildDto extends ChildDto {

    GrandchildDto getShadowedField();

    void setShadowedField(GrandchildDto v);
  }

  public interface Child2 extends Parent {
    String getChild2Field();
  }

  public interface GrandchildWithoutDto extends Child, Child2 {}
}
