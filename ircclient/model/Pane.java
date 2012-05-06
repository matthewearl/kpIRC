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

abstract public class Pane
{
  protected String title;
  protected VectorLineSource buffer;
  protected IrcModel model;
  protected Vector paneListeners;
  protected TreeMap commandMap;
  // Position of this pane relative to the other panes.
  // First pane is zero.
  public int index;
  public LineHistory history;
  
  Pane ( IrcModel model, String title, int index )
  {
    this.index = index;
    this.model = model;
    this.title = title;
    buffer = new VectorLineSource();
    paneListeners = new Vector();
    commandMap = new TreeMap(String.CASE_INSENSITIVE_ORDER);
    history = new LineHistory(128);
  }
  
  public String getTitle ()
  {
    return new String(title);
  }
  
  public void changeTitle ( String s )
  {
    title = s;

    for(int i=0;i<paneListeners.size();i++)
    {
      ((PaneListener)paneListeners.elementAt(i)).titleChanged ( s );
    }
  }
  
  public void addPaneListener ( PaneListener l )
  {
    paneListeners.add(l);
  }
  
  public void printLine ( IrcTextLine l )
  {
    buffer.addLine(l);
    for(int i=0;i<paneListeners.size();i++)
    {
      ((PaneListener)paneListeners.elementAt(i)).newLine ( l );
    }
  }
  
  public void printLine ( int type, String text )
  {
    printLine ( new IrcTextLine ( type, text ) );
  }
  
  public void printLine ( int type, String nick, String text )
  {
    printLine ( new IrcTextLine ( type, nick, text ) );
  }
  
  protected abstract class PaneCommand
  {
    public int minArgs;
    protected PaneCommand ( int minArgs )
    {
      this.minArgs = minArgs;
    }
    abstract public void run ( String [] args ) throws UserInputException;
  }
 
  abstract protected void sendNonCommand ( String line ) throws UserInputException;
  
  public void sendLine ( String line )
  {
    try
    {
      StringBuffer buf = new StringBuffer(line);
      
      if(buf.length() == 0)
      {
        throw new UserInputException("No text entered");
      }
      if(!textIsCommand(buf))
      {
        sendNonCommand ( line );
        return;
      }

      // parse the command 
      String cmdStr = new String(extractCommand ( buf ));
      String[] args = buf.toString().split("( )+");

      // For some reason, given an empty string, split will return an array of one
      // empty string, rather than an empty array.
      if(args.length == 1 && args[0].equals(""))
        {
        args = new String[0];
      }

      // Allow partially entered commands by performing a command lookup.
      Set choices = getCommandsWithPrefix(cmdStr);

      if(choices.size() == 1)
      {
        cmdStr = (String)choices.iterator().next();
        PaneCommand cmd = (PaneCommand)commandMap.get(cmdStr);
        if(args.length < cmd.minArgs)
        {
          throw new UserInputException("Not enough arguments to " + cmdStr);
        }
        cmd.run(args);
      } else if(choices.size() == 0)
      {
        UnknownCommand(cmdStr, line);
      } else {
        AmbiguousCommand(cmdStr, choices, line);
      }
    } catch( UserInputException e )
    {
      printLine(IrcTextLine.ALERT, e.toString());
    }
  }
  
  // 
  protected Set completeFromIrcStringTreeMap ( String partial, TreeMap map )
  {
    TreeSet out = new TreeSet();
    String partialLower = partial.toLowerCase();
    
    Iterator it = map.tailMap(new IrcString(partial)).keySet().iterator();
    while(it.hasNext())
    {
      String key = ((IrcString)it.next()).toString();
      String keyLower = key.toLowerCase();
      if(!keyLower.startsWith(partialLower))
        break;
      out.add(new Completion(key,partial.length()));
    }

    return out;
  }
  
  // returns a set of Strings
  protected Set getCommandsWithPrefix ( String prefix )
  {
    prefix = prefix.toLowerCase();

    SortedMap subMap = commandMap.tailMap(prefix);
    Set out = new TreeSet();

    Iterator it = subMap.keySet().iterator();
    while(it.hasNext())
    {
      String cmd = (String)it.next();
      if(!cmd.startsWith(prefix))
        break;

      out.add(cmd);
    }

    return out;
  }
  
  // returns a set of Completions
  public Set completeLine ( String linePassed, int cursorPos )
  {
    Set out = new TreeSet();
    String line = new String( linePassed );
    
    line = line.substring(0,cursorPos);
    
    // We only complete commands in this method
    if(line.length() == 0 || line.charAt(0) != '/')
      return out;

    // We will only complete the command name
    if(line.indexOf(" ") != -1)
      return out;

    // Only complete if we are typing at the end of a word or line
    if(cursorPos < linePassed.length() && linePassed.charAt(cursorPos) == ' ')
      return out;
    
    String partialCmd = line.substring(1);
    Iterator it = getCommandsWithPrefix(partialCmd).iterator();
    while(it.hasNext())
    {
      String cmd = (String)it.next();
      out.add( new Completion(cmd+" ", partialCmd.length()));
    }
    return out;
  }
  
  protected void AmbiguousCommand( String command, Set choices, String line ) throws UserInputException
  {
    String errStr = "Ambiguous command " + command + ": ";
    Iterator it = choices.iterator();

    while(it.hasNext())
    {
      String choice = (String)it.next();

      errStr += " " + choice;
      if(it.hasNext()) {
        choice += ", ";
      }
    }

    throw new UserInputException(errStr);
  }
  
  protected void UnknownCommand( String command, String line ) throws UserInputException
  {
    throw new UserInputException("Unknown command " + command);
  }
  
  protected boolean textIsCommand ( StringBuffer buf )
  {
    return buf.charAt(0) == '/';
  }

  protected StringBuffer extractCommand ( StringBuffer buf ) throws UserInputException
  {
    StringBuffer cmdStr = new StringBuffer();
    
    buf.deleteCharAt(0);
    
    while(buf.length() > 0 && buf.charAt(0) != ' ')
    {
      cmdStr.append(buf.charAt(0));
      buf.deleteCharAt(0);
    }
    
    while(buf.length() > 0 && buf.charAt(0) == ' ')
    {
      buf.deleteCharAt(0);
    }

    if(cmdStr.length() == 0)
    {
      throw new UserInputException("Zero length command given");
    }

    return cmdStr;
  }

  /* Return true iff this Pane is doing nothing */
  abstract public boolean readyToClose ();

  /* Close requires readyToClose() */
  /* close is always called by the listening GUI module */
  abstract public void close ();

  /* This function should do something towards making the pane
   * readyToClose, eg. quitting, or parting */
  /* Called when the close button is hit but readyToClose() is
   * false */
  abstract public void deactivate ();
  
  public IrcTextLineSource getIrcTextLineSource () 
  {
    return buffer;
  }
}

