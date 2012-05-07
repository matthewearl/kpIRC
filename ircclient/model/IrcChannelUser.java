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
import kpl.irc.*;

public class IrcChannelUser
{
  public IrcString nick;
  public ModeSet channelModes;

  IrcChannelUser ( IrcString user )
  {
    StringBuffer buf = new StringBuffer ( user.toString() );

    channelModes = new ModeSet();
    while(buf.length() > 0 && (buf.charAt(0) == '+' || buf.charAt(0) == '@'))
    {
      if(buf.charAt(0) == '+')
        channelModes.setModeString("+v");
      else
        channelModes.setModeString("+o");
     
      buf.deleteCharAt(0);
    }
    nick = new IrcString ( buf.toString() );
  }

  public String toString ()
  {
    String out = new String();
    if(channelModes.hasMode('o'))
      out = out + "@";
    if(channelModes.hasMode('v'))
      out = out + "+";
    out = out + nick;
    return out;
  }
}

