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
import java.lang.*;
import kpl.irc.*;
import kpl.settings.Settings;

// Server pane recieves events from the server, and routes them to
// the appropriate channel pane
public class ServerPane extends Pane implements IrcEventListener
{
  protected IrcConnection connection;
  protected String hostName;
  protected int port;
  protected IrcString currentNick; // Used for things such as determining if it
                            // is us who has left the channel, etc.
                            // should be what the server says our nick is.
                            //
                            // Some servers will seem to accept a given nick
                            // but will then carry on as if you had given a different
                            // (ie. truncated) nick
  protected TreeMap channelPanes;
  protected Map queryPanes;
  
  private ChannelPane ensureInChannel ( IrcString where )
  {
    if(channelPanes.containsKey(where))
    {
      ChannelPane cp = (ChannelPane)channelPanes.get(where);
      cp.ensureInChannel();
      return cp;
    }

    ChannelPane cp = model.makeNewChannelPane ( where, this );
    channelPanes.put(where, cp);
    return cp;
  }
  
  // Get the query pane for "who", making one if it does not exist
  private QueryPane getQueryPane ( IrcString who )
  {
    if(queryPanes.containsKey(who))
    {
      QueryPane qp = (QueryPane)queryPanes.get(who);
      return qp;
    }
    QueryPane qp = model.makeNewQueryPane ( who, this );
    queryPanes.put(who, qp);
    return qp;
  }
  
  public void onWelcome ( IrcString yourNick, String msg )
  {
    currentNick = yourNick;
  }
  
  public void onJoin ( IrcPrefix who, IrcString where )
  {
    ChannelPane cp = ensureInChannel( where );
    if(who.isServerName())
      throw new RuntimeException("Server sent JOIN");
    cp.onJoin(who);
  }
 
  public void onPrivMsg ( IrcPrefix who, IrcString where, String msg )
  {
    if(who.isServerName())
      throw new RuntimeException("Server sent PRIVMSG");
    if(where.equals(currentNick))
    {
      QueryPane qp = getQueryPane( who.getNick() );
      qp.onPrivMsg ( who, msg );
    } else
    {
      ChannelPane cp = ensureInChannel( where );
      cp.onPrivMsg ( who, msg );
    }
  }
  
  public void onPart ( IrcPrefix who, IrcString where )
  {
    ChannelPane cp = ensureInChannel( where );
    if(who.isServerName())
      throw new RuntimeException("Server sent PART");

    cp.onPart( who );
    
    if(who.getNick().equals(currentNick))
      cp.leaveChannel();
  }

  // Send message to a channel that MIGHT be of relevance,
  // and allow ChannelPane to determine to ignore it or not.
  public void onNick ( IrcPrefix who, IrcString newNick )
  {
    Iterator it = channelPanes.values().iterator();

    while(it.hasNext())
    {
      ChannelPane cp = (ChannelPane)it.next();
      cp.onNick(who, newNick);
    }

    if(who.getNick().equals(currentNick))
    {
      currentNick = newNick;
      printLine(IrcTextLine.INFO, "You are now known as " + currentNick);
    }
  }
 
  public void onQuit ( IrcPrefix who, String why )
  {
    Iterator it = channelPanes.values().iterator();

    while(it.hasNext())
    {
      ChannelPane cp = (ChannelPane)it.next();
      cp.onQuit(who, why);
    }
  }
 
  public void onNamesList ( IrcString where, IrcString names )
  {
    ChannelPane cp = ensureInChannel( where );
    cp.onNamesList( names );
  }
  
  public void onMode ( IrcPrefix who, IrcString what, IrcString modeString, Vector args )
  {
    // TODO: add support for MODE lines where "what" is a user.
    if(!channelPanes.containsKey(what))
      return;
    ChannelPane cp = (ChannelPane)channelPanes.get(what);
    cp.onMode( who, modeString, args );
  }
  
  public void onTopicChange ( IrcPrefix who, IrcString where, String newTopic )
  {
    if(!channelPanes.containsKey(where))
      return;
    ChannelPane cp = (ChannelPane)channelPanes.get(where);
    cp.onTopicChange(who, newTopic);
  }
  
  public void onTopic ( IrcString where, String newTopic )
  {
    if(!channelPanes.containsKey(where))
      return;
    ChannelPane cp = (ChannelPane)channelPanes.get(where);
    cp.onTopic(newTopic);
  }
  
  public void onNoTopic ( IrcString where )
  {
    if(!channelPanes.containsKey(where))
      return;
    ChannelPane cp = (ChannelPane)channelPanes.get(where);
    cp.onNoTopic();
  }
  
  protected void sendNonCommand ( String line ) throws UserInputException
  { 
    throw new UserInputException("Not in channel");
  }
  
  ServerPane ( IrcModel model, int index )
  {
    super( model, "<Disconnected>", index );
    channelPanes = new TreeMap();
    queryPanes = new TreeMap();
    hostName = null;
    commandMap.put("connect", new PaneCommand(1) {
      public void run ( String [] args ) throws UserInputException
      {
        int p;
        if(args.length >= 2)
        {
          try
          {
            p = Integer.decode(args[1]).intValue();
          } catch(NumberFormatException e)
          {
            throw new UserInputException("Non numeric port passed");
          }
        } else
        {
          p = 6667;
        }
        cmdConnect( args[0], p );
      }
    });
    commandMap.put("reconnect", new PaneCommand(0) {
      public void run ( String [] args ) throws UserInputException
      {
        if(hostName == null)
        {
          throw new UserInputException("Not connected before");
        }
        cmdConnect( hostName, port );
      }
    });
    commandMap.put("quit", new PaneCommand(0) {
      public void run ( String [] args ) throws UserInputException
      {
        if(connection == null)
          throw new UserInputException("Not connected");
        String why;
        if(args.length >= 1)
          why = args[0];
        else
          why = "*** kpIRC ***";
        connection.sendQuit(why);
      }
    });
    commandMap.put("join", new PaneCommand(1) {
      public void run ( String [] args ) throws UserInputException
      {
        if(connection == null)
          throw new UserInputException("Not connected");
        connection.sendJoin(new IrcString(args[0]));
      }
    });
    commandMap.put("nick", new PaneCommand(1) {
      public void run ( String [] args ) throws UserInputException
      {
        if(connection == null)
          throw new UserInputException("Not connected");
        connection.sendNick(new IrcString(args[0]));
      }
    });
    
    commandMap.put("raw", new PaneCommand(1) {
      public void run ( String [] args ) throws UserInputException
      {
        if(connection == null)
          throw new UserInputException("Not connected");
        StringBuffer buf = new StringBuffer();
        for(int i=0;i<args.length;i++)
        {
          buf.append(args[i]);
          if(i < args.length-1)
            buf.append(' ');
        }
        try
        {
          connection.sendRawCommand(new String(buf));
        } catch ( MalformedCommandException e )
        {
          throw new UserInputException(""+e);
        }
      }
    });

    connection = null;
  }

  public void onConnect ()
  {
    printLine(IrcTextLine.INFO, "Connected");
    changeTitle(hostName);
  }
   
  public void onDisconnect ()
  {
    printLine(IrcTextLine.INFO, "Disconnected");
    changeTitle("(" + hostName + ")");
    connection = null;
    
    // Go through all the channel panes telling them to leave
    Iterator it = channelPanes.values().iterator();
    while(it.hasNext())
    {
      ChannelPane cp = (ChannelPane)it.next();
      cp.leaveChannel();
    } 

    // Check if we have shut down, so we can send window close events and such
    model.checkShutDown();
  }
  
  public void onCommand ( IrcCommand c )
  {
    printLine(IrcTextLine.INFO, "> " + c);
  }
  
  // Damn you, lack of MI!
  public void onPing ( String code ) {}
  public void onNoMotd() {}
  public void onMotdEnd() {}
 
  private void cmdConnect ( String host, int p ) throws UserInputException
  {
    if(model.isShuttingDown())
    {
      throw new UserInputException("Can not make new connection, client is shutting down");
    }
    
    if(p <=0 || p > 65534)
    {
      throw new UserInputException("Port number out of range");
    }
    if(connection != null)
    {
      throw new UserInputException("Already connected");
    }
    hostName = host;
    port = p;
    changeTitle("> " + hostName);
        
    connection = new IrcConnection(
    		Settings.get("Irc.nick").asString(),
    		Settings.get("Irc.user").asString(),
    		Settings.get("Irc.name").asString());
    currentNick = new IrcString("kpIRC-User");
    connection.addEventListener(this);
    connection.connect(hostName, port);
  }

  // If a connection is open, tell it to close
  public void deactivate ()
  {
    if(!isActive())
      return;
    connection.sendQuit("Closing kpIRC...");
  }

  public boolean isActive ()
  {
    return connection!=null;
  }

  // Can't close a server tab unless we have closed all the associated
  // client tabs first.
  public boolean readyToClose ()
  {
    return !isActive() && (channelPanes.size() == 0);
  }
  
  public void close ()
  {
    model.removePane(this);
  }

  public void removeChannelPane( IrcString where )
  {
    ChannelPane cp = (ChannelPane)channelPanes.get(where);
    model.removePane(cp);
    channelPanes.remove(where);
  }

  public void removeQueryPane ( IrcString who )
  {
    QueryPane qp = (QueryPane)queryPanes.get(who);
    model.removePane(qp);
    queryPanes.remove(who);
  }

  public Set completeChannelName ( String partialChannel )
  {
    return completeFromIrcStringTreeMap( partialChannel, channelPanes );
  }

  public Set completeLine ( String linePassed, int cursorPos )
  {
    Set out = super.completeLine(linePassed,cursorPos);

    // Only commands can be entered into the server pane
    // hence, only commands are completed
    if(linePassed.length() == 0 || linePassed.charAt(0) != '/')
      return out;
    
    String line = new String ( linePassed );
    line = line.substring(0,cursorPos);
  
    // Complete on channel name if we are typing a parameter
    if(line.indexOf(" ") != -1 && 
        (cursorPos == linePassed.length()||linePassed.charAt(cursorPos)==' '))
    {
      String []arr = line.split("( )+");
      String lastWord = arr[arr.length-1];

      if(lastWord.length() > 0 && lastWord.charAt(0) == '#')
        out.addAll(completeChannelName(lastWord));
    }

    return out;
  }
}


