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
import java.util.*;

public class IrcCommand
{
  IrcPrefix prefix;
  // A vector of IrcStrings
  Vector args;
 
  public IrcString getArg ( int i )
  {
    return (IrcString)args.elementAt(i);
  }
  
  public String toString()
  {
    StringBuffer out = new StringBuffer();
    
    if(prefix != null)
    {
      out.append(':');
      out.append(prefix.toString());
      out.append(' ');
    }

    for(int i=0;i<args.size();i++)
    {
      if(i == args.size()-1)
      {
        // Just in case...
        out.append(':');
      }
      out.append(args.elementAt(i).toString());

      if(i < args.size()-1)
      {
        out.append(' ');
      }
    }

    return out.toString();
  }
  
  private StringBuffer readNonSpaces( StringBuffer buf )
  {
    StringBuffer out = new StringBuffer();
    while(buf.length() > 0 && buf.charAt(0) != ' ')
    {
      out.append(buf.charAt(0));
      buf.deleteCharAt(0);
    }
    return out;
  }

  private void readSpaces ( StringBuffer buf )
  {
    while(buf.length() > 0 && buf.charAt(0) == ' ')
    {
      buf.deleteCharAt(0);
    }
  }
  
  IrcCommand ( String line ) throws MalformedCommandException
  {
    StringBuffer buf = new StringBuffer(line);
    int i=0;
    
    System.out.println("Got Line: \"" + line + "\"");
    if(buf.length() == 0)
    {
      throw new MalformedCommandException("Command has zero length");
    }
    
    if(buf.charAt(0) == ':')
    {
      buf = buf.deleteCharAt(0);
      prefix = new IrcPrefix(readNonSpaces(buf));
    } else
    {
      prefix = null;
    }
    
    args = new Vector();
    while(buf.length() > 0)
    {
      readSpaces(buf);
      IrcString arg;
      if(buf.length() > 0 && buf.charAt(0) == ':')
      {
        buf.deleteCharAt(0);
        arg = new IrcString(buf);
        buf = new StringBuffer();
      } else
      {
        arg = new IrcString(readNonSpaces(buf));
      }
      if(arg.length() > 0)
      {
        args.add(arg);
      }
    }

    if(args.size() == 0)
    {
      throw new MalformedCommandException("No args in command");
    }
  }
}

