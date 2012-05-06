/*
 * Copyright (C) Matthew Earl
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package ircclient.model;
import java.util.*;

public class LineHistory
{
  private final int maxSize;
  private LinkedList lines;
  private ListIterator it;
  private String right;
  private String bottom;
 
  //    Lines v         v Bottom
  //  --------------    --
  //    |   |   |   |  |  |
  //  --------------    --
  //    ^
  //  
  //  right refers to the contents
  //  in the element to the right of the
  //  cursor. We want to return whenever
  //  either previous or next is called.
  
  public LineHistory ( int maxSize )
  {
    this.maxSize = maxSize;
    lines = new LinkedList();
    it = lines.listIterator(0);
    bottom = "";
    right = bottom;
  }

  public String previous ( String line )
  {
    if(!it.hasNext())
      bottom = line;
    if(it.hasPrevious())
      right = (String)it.previous();
    return right;
  }
  
  public String next( String line )
  {
    if(!it.hasNext())
      return line;
    it.next();
    if(!it.hasNext())
      return bottom;
    right = (String)it.next();
    it.previous();
    return right;
  }
  
  public void add ( String s )
  {
    lines.add(s);
    while(lines.size() > maxSize)
      lines.removeFirst();
    it = lines.listIterator(lines.size());
    right = "";
  }
}

