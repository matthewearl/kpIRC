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
import java.lang.*;

public class IrcPrefix
{
  private IrcString serverName;
  private IrcString nick, user, host;

  public String toString()
  {
    if(isServerName())
    {
      return "" + serverName;
    }
    
    return "" + nick + "!" + user + "@" + host;
  }
  
  public boolean isServerName ()
  {
    return serverName != null;
  }
  
  public IrcString getServerName ()
  {
    return new IrcString(serverName);
  }

  public IrcString getNick ()
  {
    return new IrcString(nick);
  }

  public IrcString getUser ()
  {
    return new IrcString(user);
  }
 
  public IrcString getHost ()
  {
    return new IrcString(host);
  }
  
  IrcPrefix ( StringBuffer str )
  {
    StringBuffer n,u,h;

    n = new StringBuffer();
    u = h = null;

    StringBuffer curr = n;
    
    for(int i=0;i<str.length();i++)
    {
      if(str.charAt(i) == '!')
      {
        if(u != null)
        {
          throw new RuntimeException("Multiple ! signs in prefix" + str);
        }
        if(curr != n)
        {
          throw new RuntimeException("! in unexpected place in prefix" + str);
        }
        u = new StringBuffer();
        curr = u;
      } else if(str.charAt(i) == '@')
      {
        if(h != null)
        {
          throw new RuntimeException("Multiple @ signs in prefix" + str);
        }
        if(curr != u)
        {
          throw new RuntimeException("@ in unexpected place in prefix" + str);
        }
        h = new StringBuffer();
        curr = h;
      } else
      {
        curr.append(str.charAt(i));
      }
    }

    if(h == null)
    {
      if(u != null)
      {
        throw new RuntimeException("Partial prefix given" + str);
      }
      if(n.length() == 0)
      {
        throw new RuntimeException("Zero length prefix given");
      }
      serverName = new IrcString(n);
      nick = user = host = null;
    } else
    {
      if(h.length() == 0 || u.length() == 0 || n.length() == 0)
      {
        throw new RuntimeException("Zero length component(s) in prefix");
      }
      nick = new IrcString(n);
      user = new IrcString(u);
      host = new IrcString(h);
      serverName = null;
    }
  }
}

