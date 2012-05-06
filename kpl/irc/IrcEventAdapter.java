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
import java.io.*;
import java.util.*;

public abstract class IrcEventAdapter implements IrcEventListener
{
  protected IrcConnection conn;

  public IrcEventAdapter ( IrcConnection conn )
  {
    this.conn = conn;
  }
  
  public void onConnect () {}
  public void onDisconnect () {}
  public void onCommand ( IrcCommand c ) {}
  public void onPing ( String code ) {}
  public void onNoMotd() {}
  public void onMotdEnd() {}
  public void onJoin( IrcPrefix who, IrcString channel ) {}
  public void onPrivMsg ( IrcPrefix who, IrcString where, String msg ) {}
  public void onPart ( IrcPrefix who, IrcString where ) {}
  public void onNick ( IrcPrefix who, IrcString nick ) {}
  public void onQuit ( IrcPrefix who, String why ) {}
  public void onNamesList ( IrcString where, IrcString names ) {}
  public void onWelcome ( IrcString yourNick, String msg ) {}
  public void onMode ( IrcPrefix who, IrcString what, IrcString modeString, Vector args ) {}
  public void onTopicChange ( IrcPrefix who, IrcString where, String newTopic ) {}
  public void onTopic ( IrcString where, String newTopic ) {}
  public void onNoTopic ( IrcString where ) {}
}

