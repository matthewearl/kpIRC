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

// Performs only the tasks required to keep the connection alive,
// ie. sending USER/NICK, PING responses.

package kpl.irc;

public class FundamentalControl extends IrcEventAdapter
{
    FundamentalControl ( IrcConnection conn )
    {
      super(conn);
    }

    public void onConnect ()
    {
      conn.sendNick();
      conn.sendUser();
    }

    public void onPing ( String code )
    {
      conn.sendPong(code);
    }
}

