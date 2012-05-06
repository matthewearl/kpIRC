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

public class IrcTextLine
{
  public static final int SPEECH = 0;
  public static final int JOIN = 1;
  public static final int LEAVE = 2;
  public static final int INFO = 3;
  public static final int ALERT = 4;
  
  public final int type;
  
  // Only valid if type == SPEECH
  final public String nick;

  // Text can contain mIRC formatting codes, although
  // the IrcText class handles colouring text appropriate
  // for JOIN messages, etc.
  final public String text;

  public IrcTextLine ( int type, String text )
  {
    this.type = type;
    this.text = text;
    this.nick = null;
    if(type < JOIN || type > ALERT)
      throw new RuntimeException("Invalid type for constructor");
  }

  public IrcTextLine ( int type, String nick, String text )
  {
    this.type = type;
    this.nick = nick;
    this.text = text;

    if(type != SPEECH)
      throw new RuntimeException("Invalid type for constructor");
  }
}

