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
import kpl.irc.*;

public class QueryPane extends Pane
{
  private IrcString who;
  private ServerPane serverPane;
  
  QueryPane ( IrcModel model, IrcString who, ServerPane serverPane, int index )
  {
    super( model, who.toString(), index );
    this.who = who;
    this.serverPane = serverPane;
  }
  
  protected void sendNonCommand ( String line ) throws UserInputException
  {
    // Should be non null as we would not be inChannel were we disconnected
    serverPane.connection.sendPrivMsg( who, line );
    printPrivMsg(serverPane.currentNick, line);
  }
  
  public void printPrivMsg ( IrcString nick, String msg )
  {
    printLine ( IrcTextLine.SPEECH, nick.toString(), msg );
  }
  
  public void onPrivMsg ( IrcPrefix who, String msg )
  {
    printPrivMsg( who.getNick(), msg );
  }
  
  // Query panes are always passive
  public void deactivate ()
  {
  }
  
  public boolean readyToClose()
  {
    return true;
  }

  public void close()
  {
    serverPane.removeQueryPane(who);
  }
  
  protected void UnknownCommand ( String command, String line ) throws UserInputException
  {
    serverPane.sendLine(line);
  }
}

