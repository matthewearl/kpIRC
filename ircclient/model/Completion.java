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
import java.lang.*;

public class Completion implements Comparable
{
  // The text that should pop up in a drop down list
  // eg. the complete command (even the partially typed bit)
  final private String word;
  // The number of characters already typed
  final private int numTyped;

  Completion ( String word, int numTyped )
  {
    this.word = word;
    this.numTyped = numTyped;
  }
  public String getFull()
  {
    return word;
  }
  public int getNumTyped ()
  {
    return numTyped;
  }
  public String getTyped()
  {
    return word.substring(0,numTyped);
  }
  public String getUntyped()
  {
    return word.substring(numTyped);
  }
  public int compareTo ( Object o )
  {
    if(!(o instanceof Completion))
      throw new ClassCastException();

    Completion c = (Completion)o;
    if(word != c.word)
      return word.compareTo(c.word);
    return c.numTyped - numTyped;
  }
}

