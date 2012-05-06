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
import java.util.*;
import kpl.irc.*;

public class ChannelPane extends Pane
{
  IrcString chanName;
  TreeMap users;
  String topic;
  ServerPane serverPane;
  Vector channelPaneListeners;
  ModeSet modes; // Note the following few variables only apply if the appropriate
                  // mode flag is set.
  int userLimit;
  String key;
  
  public boolean inChannel;
  
  ChannelPane ( IrcModel model, IrcString cn, ServerPane sp, int index )
  {
    super(model, cn.toString(), index);
    this.chanName = cn;
    this.serverPane = sp;
    
    inChannel = true;

    channelPaneListeners = new Vector();
    users = new TreeMap();
    modes = new ModeSet();
    topic = "";
    // Todo: Add to commandMap 
    commandMap.put("part", new PaneCommand(0) {
      public void run ( String [] args ) throws UserInputException
      {
        if(!inChannel)
          throw new UserInputException("Not in channel");
        serverPane.connection.sendPart( chanName );
      }
    });
  }

  // Defer unknown commands to the server pane so that 
  // server commands can be typed into channel windows
  protected void UnknownCommand ( String command, String line ) throws UserInputException
  {
    serverPane.sendLine(line);
  }
  
  public void addChannelPaneListener ( ChannelPaneListener l )
  {
    channelPaneListeners.add(l);

    l.onTopicChanged ( topic );
    l.onUserListChanged ( users.values () );
  }

  public void userListChanged ()
  {
    for(int i=0;i<channelPaneListeners.size();i++)
    {
      ChannelPaneListener l = (ChannelPaneListener)channelPaneListeners.elementAt(i);
      l.onUserListChanged ( users.values() );
    }
  }
 
  public void topicChanged ()
  {
    for(int i=0;i<channelPaneListeners.size();i++)
    {
      ChannelPaneListener l = (ChannelPaneListener)channelPaneListeners.elementAt(i);
      l.onTopicChanged ( topic );
    }
  }
  
  public void printPrivMsg ( IrcString nick, String msg )
  {
    printLine ( IrcTextLine.SPEECH, nick.toString(), msg );
  }
  
  public void leaveChannel ()
  {
    inChannel = false;
    changeTitle("(" + chanName.toString() + ")");
    users = new TreeMap();
    userListChanged();
  }
 
  public void ensureInChannel ()
  {
    if(inChannel)
      return;
    inChannel = true;
    changeTitle(chanName.toString());
  }
  
  public void onJoin ( IrcPrefix who )
  {
    if(users.containsKey(who.getNick()))
      return;
    users.put( who.getNick(), new IrcChannelUser( who.getNick() ));
    
    printLine( IrcTextLine.JOIN, "" + who.getNick() + " (" + who.getUser() + "@" + who.getHost() + ") has joined " + chanName);
    userListChanged();
  }
 
  public void onPrivMsg ( IrcPrefix who, String msg )
  {
    printPrivMsg( who.getNick(), msg );
  }
  
  public void onPart ( IrcPrefix who )
  {
    if(!users.containsKey(who.getNick()))
      return;
    users.remove ( who.getNick() );

    printLine(IrcTextLine.LEAVE, "" + who.getNick() + " has left " + chanName);
    userListChanged();
  }
  
  public void onNick ( IrcPrefix who, IrcString nick )
  {
    if(!users.containsKey(who.getNick()))
      return;
    
    IrcChannelUser u = (IrcChannelUser)users.get(who.getNick());
    u.nick = nick;
    
    users.remove(who.getNick());
    users.put(nick, u);
    
    printLine(IrcTextLine.INFO, "" + who.getNick() + " is now known as " + nick);
    userListChanged();
  }
 
  public void onQuit ( IrcPrefix who, String why )
  {
    if(!users.containsKey(who.getNick()))
      return;

    users.remove( who.getNick() );
    if(why.equals(""))
    {
      printLine(IrcTextLine.LEAVE, "" + who.getNick() + " quit");
    } else
    {
      printLine(IrcTextLine.LEAVE, "" + who.getNick() + " quit (" + why + ")");
    }
    userListChanged();
  }
  
  public void onNamesList ( IrcString namesList )
  {
    String []names = namesList.toString().split("( )+");

    for(int i=0;i<names.length;i++)
    {
      IrcString name = new IrcString(names[i]);
      IrcChannelUser u = new IrcChannelUser(name);
      users.put(u.nick, u);
    }
    userListChanged();
  }
  
  public void onMode ( IrcPrefix who, IrcString modeString, Vector args )
  {
    StringBuffer buf = new StringBuffer(modeString.toString());
    boolean addMode = true;
    char c;
    Iterator it = args.iterator();
    IrcString setter;

    if(who.isServerName())
      setter = who.getServerName();
    else
      setter = who.getNick();
    
    while(buf.length() > 0)
    {
      c = buf.charAt(0);
      buf.deleteCharAt(0);
      switch(c)
      {
        case '+': addMode = true; break;
        case '-': addMode = false; break;
        default:
          if(!addMode)
          {
            if((c == 'o' || c == 'v' || c == 'b') && !it.hasNext())
              break;
            switch(c)
            {
              case 'l': 
                printLine(IrcTextLine.INFO, "" + setter + " removes user limit");
                modes.unsetMode('l');
                break;
              case 'k':
                printLine(IrcTextLine.INFO, "" + setter + " removes key");
                modes.unsetMode('k');
                break;
              case 'o':
                IrcString n = (IrcString)it.next();
                if(users.containsKey(n))
                {
                  ((IrcChannelUser)users.get(n)).channelModes.unsetMode('o');
                  printLine(IrcTextLine.INFO, "" + setter + " takes ops from " + n);
                  userListChanged();
                }
                break;
              case 'v':
                IrcString ni = (IrcString)it.next();
                if(users.containsKey(ni))
                {
                  ((IrcChannelUser)users.get(ni)).channelModes.unsetMode('v');
                  printLine(IrcTextLine.INFO, "" + setter + " takes voice from " + ni);
                  userListChanged();
                }
                break;
              case 'b':
                printLine(IrcTextLine.INFO, "" + setter + " lifts ban on " + it.next());
                break;
              default:
                printLine(IrcTextLine.INFO, "" + setter + " sets mode -" + c);
                modes.unsetMode(c);
                break;
            }
          } else
          {
            if((c == 'l' || c == 'k' || c == 'o' || c == 'v' || c == 'b') && !it.hasNext())
              break;
            switch(c)
            {
              case 'l': 
                try
                {
                  userLimit = Integer.parseInt(((IrcString)it.next()).toString());
                  printLine(IrcTextLine.INFO, "" + setter + " sets user limit to " + userLimit);
                  modes.setMode('l');
                } catch ( NumberFormatException e ) {}
                break;
              case 'k':
                key = ((IrcString)it.next()).toString();
                printLine(IrcTextLine.INFO, "" + setter + " sets key to " + key);
                modes.setMode('k');
                break;
              case 'o':
                IrcString n = (IrcString)it.next();
                if(users.containsKey(n))
                {
                  ((IrcChannelUser)users.get(n)).channelModes.setMode('o');
                  printLine(IrcTextLine.INFO, "" + setter + " gives ops to " + n);
                }
                userListChanged();
                break;
              case 'v':
                IrcString ni = (IrcString)it.next();
                if(users.containsKey(ni))
                {
                  ((IrcChannelUser)users.get(ni)).channelModes.setMode('v');
                  printLine(IrcTextLine.INFO, "" + setter + " gives voice to " + ni);
                }
                userListChanged();
                break;
              case 'b':
                // We don't keep track of bans
                printLine(IrcTextLine.INFO, "" + setter + " sets ban on " + it.next());
                break;
              default:
                printLine(IrcTextLine.INFO, "" + setter + " sets mode +" + c);
                modes.setMode(c);
                break;
            }
          }
          break;
      }
    }
  }
  
  public void onTopicChange ( IrcPrefix who, String newTopic )
  {
    IrcString changer;
    
    if(who.isServerName())
      changer = who.getServerName();
    else
      changer = who.getNick();
    
    topic = newTopic;
    printLine ( IrcTextLine.INFO, "" + changer + " sets topic to \"" + newTopic + "\"");
    topicChanged();
  }
 
  public void onTopic ( String newTopic )
  {
    topic = newTopic;
    printLine ( IrcTextLine.INFO, "Topic is \"" + topic + "\"" );
    topicChanged();
  }
  
  public void onNoTopic ()
  {
    topic = "";
    printLine ( IrcTextLine.INFO, "No topic set" );
    topicChanged();
  }
  
  protected void sendNonCommand ( String line ) throws UserInputException
  {
    if(!inChannel)
      throw new UserInputException("Not in channel");

    // Should be non null as we would not be inChannel were we disconnected
    serverPane.connection.sendPrivMsg( chanName, line );
    printPrivMsg(serverPane.currentNick, line);
  }
  
  public void deactivate ()
  {
    if(inChannel)
        serverPane.connection.sendPart( chanName );
  }
  
  public boolean readyToClose()
  {
    return !inChannel;
  }

  public void close()
  {
    serverPane.removeChannelPane(chanName);
  }

  public Set completeLine ( String linePassed, int cursorPos )
  {
    Set out = super.completeLine(linePassed,cursorPos);
    out.addAll(serverPane.completeLine(linePassed,cursorPos));
    
    String line = new String ( linePassed );
    line = line.substring(0,cursorPos);
    
    if((line.length() == 0 || line.charAt(0) != '/' || line.indexOf(" ") != -1)
        && (cursorPos==linePassed.length()||linePassed.charAt(cursorPos)==' '))
    {
      String []arr = line.split("( )+");
      String lastWord = arr[arr.length-1];

      if(lastWord.length() > 0 && lastWord.charAt(0) == '#')
        out.addAll(serverPane.completeChannelName(lastWord));
      else
        out.addAll(completeFromIrcStringTreeMap(lastWord, users));
    }
    return out;
  }
}

