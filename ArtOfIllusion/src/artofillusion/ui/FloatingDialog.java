/* Copyright (C) 2001,2004 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.ui;

import buoy.event.*;
import buoy.widget.*;

/** This class is a dialog which "floats" above its parent window whenever it is set to be
    non-modal.  That is, it is drawn above its parent, but always makes sure that the
    parent has focus so that it will respond to key-presses. */

public class FloatingDialog extends BDialog
{
  public FloatingDialog(WindowWidget parent)
  {
    this(parent, null, false);
  }

  public FloatingDialog(WindowWidget parent, boolean modal)
  {
    this(parent, null, modal);
  }

  public FloatingDialog(WindowWidget parent, String title)
  {
    this(parent, title, false);
  }

  public FloatingDialog(WindowWidget parent, String title, boolean modal)
  {
    super(parent, title, modal);
    addEventLink(WindowActivatedEvent.class, this, "windowActivated");
  }
  
  private void windowActivated()
  {
    if (!isModal())
      ((WindowWidget) getParent()).toFront();
  }
}

