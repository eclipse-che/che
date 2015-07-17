// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include "cefclient/window_test.h"

#include <gtk/gtk.h>

namespace window_test {

namespace {

GtkWindow* GetWindow(CefWindowHandle handle) {
  return GTK_WINDOW(gtk_widget_get_toplevel(GTK_WIDGET(handle)));
}

bool IsMaximized(GtkWindow* window) {
  GdkWindow* gdk_window = gtk_widget_get_window(GTK_WIDGET(window));
  gint state = gdk_window_get_state(gdk_window);
  return (state & GDK_WINDOW_STATE_MAXIMIZED) ? true : false;
}

}  // namespace

void SetPos(CefWindowHandle handle, int x, int y, int width, int height) {
  GtkWindow* window = GetWindow(handle);
  GdkWindow* gdk_window = gtk_widget_get_window(GTK_WIDGET(window));

  // Make sure the window isn't minimized or maximized.
  if (IsMaximized(window))
    gtk_window_unmaximize(window);
  else
    gtk_window_present(window);

  // Retrieve information about the display that contains the window.
  GdkScreen* screen = gdk_screen_get_default();
  gint monitor = gdk_screen_get_monitor_at_window(screen, gdk_window);
  GdkRectangle rect;
  gdk_screen_get_monitor_geometry(screen, monitor, &rect);

  // Make sure the window is inside the display.
  CefRect display_rect(rect.x, rect.y, rect.width, rect.height);
  CefRect window_rect(x, y, width, height);
  ModifyBounds(display_rect, window_rect);

  gdk_window_move_resize(gdk_window, window_rect.x, window_rect.y,
                         window_rect.width, window_rect.height);
}

void Minimize(CefWindowHandle handle) {
  GtkWindow* window = GetWindow(handle);

  // Unmaximize the window before minimizing so restore behaves correctly.
  if (IsMaximized(window))
    gtk_window_unmaximize(window);

  gtk_window_iconify(window);
}

void Maximize(CefWindowHandle handle) {
  gtk_window_maximize(GetWindow(handle));
}

void Restore(CefWindowHandle handle) {
  GtkWindow* window = GetWindow(handle);
  if (IsMaximized(window))
    gtk_window_unmaximize(window);
  else
    gtk_window_present(window);
}

}  // namespace window_test
