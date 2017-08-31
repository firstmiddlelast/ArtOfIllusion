/* This represents a link between an input port and an output port. */

/* Copyright (C) 2000,2003 by Peter Eastman

   This program is free software; you can redistribute it and/or modify it under the
   terms of the GNU General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful, but WITHOUT ANY 
   WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
   PARTICULAR PURPOSE.  See the GNU General Public License for more details. */

package artofillusion.procedural;

public class Link
{
  public IOPort from, to;
  
  public Link(IOPort from, IOPort to)
  {
    this.from = from;
    this.to = to;
  }
  
  /** Get the index (within its module) of the from port. */
  
  public int getFromPortIndex()
  {
    return from.getModule().getOutputIndex(from);
  }
  
  /** Get the index (within its module) of the to port. */
  
  public int getToPortIndex()
  {
    return to.getModule().getInputIndex(to);
  }
}

