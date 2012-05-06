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

package kpl.irc;
import java.lang.*;
import java.lang.reflect.*;

// For some reason, String is final
public class IrcString implements Comparable
{
  public String s;

  public IrcString ( String s )
  {
    this.s = s;
  }
  
  public IrcString ( StringBuffer b )
  {
    s = new String ( b );
  }
  
  public IrcString ( IrcString o )
  {
    this.s = o.s;
  }
  
  public int length ()
  {
    return s.length();
  }
  
  private String normalize ()
  {
    char arr[] = s.toLowerCase().toCharArray();

    for(int i=0;i<Array.getLength(arr);i++)
    {
      if(arr[i] == '[')
        arr[i] = '{';
      else if(arr[i] == ']')
        arr[i] = '}';
      else if(arr[i] == '\\')
        arr[i] = '|';
    }

    return new String ( arr );
  }
  
  public boolean equals ( Object o )
  {
    if(!(o instanceof IrcString) && !(o instanceof String))
    {
      return false;
    }

    String a = normalize();
    String b;
    if(o instanceof IrcString)
    {
      b = ((IrcString) o).normalize();
    } else
    {
      b = (new IrcString((String)o)).normalize();
    }

    return a.equals(b);
  }

  public int compareTo ( Object o )
  {
    if(!(o instanceof IrcString) && !(o instanceof String))
    {
      throw new ClassCastException();
    }

    String a = normalize();
    String b;
    if(o instanceof IrcString)
    {
      b = ((IrcString) o).normalize();
    } else
    {
      b = (new IrcString((String)o)).normalize();
    }
    return a.compareTo(b);
  }

  public String toString ()
  {
    return new String(s);
  }
}

