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

public interface IrcEventListener
{
  void onConnect ();
  void onDisconnect ();
  void onCommand ( IrcCommand c );
  void onPing ( String code );
  void onNoMotd();
  void onMotdEnd();
  void onJoin( IrcPrefix who, IrcString channel );
  void onPrivMsg ( IrcPrefix who, IrcString where, String msg );
  void onPart ( IrcPrefix who, IrcString where );
  void onNick ( IrcPrefix who, IrcString nick );
  void onQuit ( IrcPrefix who, String why );
  void onNamesList ( IrcString where, IrcString names );
  void onWelcome ( IrcString yourNick, String msg );
  void onMode ( IrcPrefix who, IrcString what, IrcString modeString, Vector args );
  void onTopicChange ( IrcPrefix who, IrcString where, String newTopic );
  void onTopic ( IrcString where, String newTopic );
  void onNoTopic ( IrcString where );
}

