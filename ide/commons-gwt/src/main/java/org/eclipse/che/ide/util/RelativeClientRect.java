// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util;

import elemental.html.ClientRect;

/** A {@link ClientRect} which is relative to a given point. */
public class RelativeClientRect implements ClientRect {

  public static ClientRect relativeToRect(ClientRect relativeParent, ClientRect rect) {
    return new RelativeClientRect(
        (int) relativeParent.getLeft(), (int) relativeParent.getTop(), rect);
  }

  private final ClientRect rect;

  private final int offsetLeft;

  private final int offsetTop;

  public RelativeClientRect(int offsetLeft, int offsetTop, ClientRect rect) {
    this.offsetLeft = offsetLeft;
    this.offsetTop = offsetTop;
    this.rect = rect;
  }

  @Override
  public float getBottom() {
    return rect.getBottom() - offsetTop;
  }

  @Override
  public float getHeight() {
    return rect.getHeight();
  }

  @Override
  public float getLeft() {
    return rect.getLeft() - offsetLeft;
  }

  @Override
  public float getRight() {
    return rect.getRight() - offsetLeft;
  }

  @Override
  public float getTop() {
    return rect.getTop() - offsetTop;
  }

  @Override
  public float getWidth() {
    return rect.getWidth();
  }
}
