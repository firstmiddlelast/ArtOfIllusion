/* A ModellingTool corresponds to an item in the Modelling menu.  It will generally
 * be used either for creating new objects, or for editing existing ones. */

/* Copyright (C) 2001 by Peter Eastman
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion;

public interface ModellingTool
{
  
  /* Get the text that appear as the menu item.*/
  public String getName();
  
  /* This is called when the menu item for this tool is selected.  The single argument
   * is the LayoutWindow in which the command was chosen.  The Scene object can then be
   * obtained from the LayoutWindow's getScene() method. */
  public void commandSelected(LayoutWindow window);
}
