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
import java.net.*;
import java.io.*;
import java.lang.*;
import java.util.*;

class IrcCommandWriter
{
  private PrintWriter pw;
  private OutputStream s;
  
  IrcCommandWriter ( OutputStream s )
  {
    this.s = s;
    pw = new PrintWriter(s,true);
  }
     
  synchronized public void writeCommand ( IrcCommand c )
  {
    System.setProperty("line.separator", "\r\n");
    pw.println(c.toString());
  }
}

class IrcCommandReader
{
  private BufferedReader in;

  IrcCommandReader ( InputStream s )
  {
    in = new BufferedReader( new InputStreamReader( s ));
  }

  // Block until an IrcCommand is read
  public IrcCommand readCommand () throws IOException, Exception
  {
    System.setProperty("line.separator", "\r\n");
    String line = in.readLine();
    if(line == null)
    {
      return null;
    }
    return new IrcCommand ( line );
  }
}

public class IrcConnection
{
  private Socket serverSocket;
  private LinkedList listeners;
  private IrcCommandReader in;
  private IrcCommandWriter out;
  private Thread thread;
  private IrcString nick, user, realName;
  private Map commandMap;
  private String hostName;
  
  public IrcConnection ( String nick, String user, String realName )
  {
    this.nick = new IrcString ( nick );
    this.user = new IrcString ( user );
    this.realName = new IrcString ( realName );

    listeners = new LinkedList();
   
    initCommandMap ();
    addEventListener ( new FundamentalControl(this) );
  }
  
  public void addEventListener ( IrcEventListener n )
  {
    listeners.add(n);
  }
 
  private void readLoop() throws Exception, IOException
  {
    IrcCommand c;

    c = in.readCommand();
    while(c != null)
    {
      onCommand(c);
      c = in.readCommand();
    }
  }
  
  abstract class commandDefinition
  {
    public final int minArgs;
    commandDefinition ( int minArgs )
    {
      this.minArgs = minArgs;
    }
    abstract public void action ( IrcCommand c, IrcEventListener l );
  }
  
  void initCommandMap ()
  {
    commandMap = new TreeMap();
    
    commandMap.put(new IrcString("PING"), 
        new commandDefinition(2)
        { public void action ( IrcCommand c, IrcEventListener l )
          { l.onPing( c.getArg(1).toString());
          }
        });
    commandMap.put(new IrcString("422"), 
        new commandDefinition(1)
        { public void action ( IrcCommand c, IrcEventListener l )
          { l.onNoMotd();
          }
        });
    commandMap.put(new IrcString("376"), 
        new commandDefinition(1)
        { public void action ( IrcCommand c, IrcEventListener l )
          { l.onMotdEnd();
          }
        });
    commandMap.put(new IrcString("JOIN"),
        new commandDefinition(2)
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix != null)
              l.onJoin(c.prefix, c.getArg(1));
          }
        });
    commandMap.put(new IrcString("PRIVMSG"),
        new commandDefinition(3)
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix != null)
              l.onPrivMsg(c.prefix, c.getArg(1), c.getArg(2).toString());
          }
        });
    commandMap.put(new IrcString("PART"),
        new commandDefinition(2)
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix != null)
              l.onPart(c.prefix, c.getArg(1));
          }
        });
    commandMap.put(new IrcString("NICK"),
        new commandDefinition(2)
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix != null)
              l.onNick(c.prefix, c.getArg(1));
          }
        });
    commandMap.put(new IrcString("QUIT"),
        new commandDefinition(1)
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix == null)
              return;
            if(c.args.size() > 1)
              l.onQuit(c.prefix, c.getArg(1).toString());
            else
              l.onQuit(c.prefix, "");
          }
        });
    commandMap.put(new IrcString("353"),  /* eg. :irc.phobos 353 kingping = #q3mods :kingping */
        new commandDefinition(5)          /*                  0     1     2    3        4     */
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix != null)
              l.onNamesList(c.getArg(3), c.getArg(4));
          }
        });
    commandMap.put(new IrcString("001"), /* Documented in RFC 2812 */
        new commandDefinition(3)
        { public void action ( IrcCommand c, IrcEventListener l )
          { l.onWelcome( c.getArg(1), c.getArg(2).toString() );
          }
        });
    commandMap.put(new IrcString("MODE"),
        new commandDefinition(3)
        { public void action ( IrcCommand c, IrcEventListener l )
          { Vector args = new Vector ( c.args );
            // Take off the command, channel/target, and modes
            args.remove(0);
            args.remove(0);
            args.remove(0);
            l.onMode( c.prefix, c.getArg(1), c.getArg(2), args );
          }
        });
    commandMap.put(new IrcString("TOPIC"),
        new commandDefinition(2)
        { public void action ( IrcCommand c, IrcEventListener l )
          { if(c.prefix != null)
            { String topic;
              if(c.args.size() >= 3)
                topic = c.getArg(2).toString();
              else
                topic = "";
              l.onTopicChange( c.prefix, c.getArg(1), topic );
            }
          }
        });
    commandMap.put(new IrcString("332"),
        new commandDefinition(4)
        { public void action ( IrcCommand c, IrcEventListener l )
          { l.onTopic( c.getArg(2), c.getArg(3).toString() );
          }
        });
    commandMap.put(new IrcString("331"),
        new commandDefinition(3)
        { public void action ( IrcCommand c, IrcEventListener l )
          { l.onNoTopic( c.getArg(2) );
          }
        });
  }

  public void onCommand ( IrcCommand c )
  {
    IrcString cmdStr = c.getArg(0);
    Iterator it;

    it = listeners.iterator();
    while(it.hasNext())
    {
      IrcEventListener listener = (IrcEventListener)it.next();
      listener.onCommand(c);
    }
    
    if(!commandMap.containsKey(cmdStr))
    {
      return;
    }
    
    commandDefinition def = (commandDefinition)commandMap.get(cmdStr);
    if(c.args.size() < def.minArgs)
    {
      throw new RuntimeException("Not enough args to " + cmdStr);
    }
    
    it = listeners.iterator();
    while(it.hasNext())
    { 
      IrcEventListener listener = (IrcEventListener)it.next();
      def.action(c, listener);
    }
  }
  
  private void onConnect ()
  {
    Iterator it = listeners.iterator();
    while(it.hasNext())
    {
      IrcEventListener listener = (IrcEventListener)it.next();
      listener.onConnect();
    }
  }
  
  public void connect ( String hn, int p )
  {
    final String hostname = hn;
    final int port = p;
    final IrcConnection conn = this;
   
    this.hostName = hn;
    
    thread = new Thread()
      {
        public void run ()
        {
          try
          {
            serverSocket = new Socket(hostname, port);
    
            in = new IrcCommandReader(serverSocket.getInputStream());
            out = new IrcCommandWriter(serverSocket.getOutputStream());
            
            conn.onConnect();
            conn.readLoop();
           
            // tell everyone we have disconnected and the thread is about to die
            Iterator it = listeners.iterator();
            while(it.hasNext())
            {
              IrcEventListener listener = (IrcEventListener)it.next();
              listener.onDisconnect();
            }
          } catch (Exception e)
          {
            e.printStackTrace();
            System.err.println(e);
            System.exit(-1);
          }
        }
      };
    thread.start();
  }

  public boolean hasFinished()
  {
    return thread==null || !thread.isAlive();
  }
  
  public void waitFinished() throws InterruptedException
  {
    if(!hasFinished())
      thread.join();
  }
  
  // There has to be a good reason for converting to an IrcCommand
  // and then immediately back to a String again.
  private void sendCommand( String cmd )
  {
    try
    {
      out.writeCommand(new IrcCommand(cmd));
    } catch ( MalformedCommandException e )
    {
      System.out.println(e);
    }

  }
  public void sendNick()
  {
    sendCommand("NICK " + nick);
  }
  public void changeNick( IrcString newNick )
  {
    nick = newNick;
    sendNick();
  }
  public void sendUser()
  {
    String localHostName;

    localHostName = serverSocket.getLocalAddress().getHostName();
    sendCommand("USER " + user + " " + localHostName + " " + hostName + " :" + realName);
  }
  public void sendPong( String code )
  {
    sendCommand("PONG :" + code);
  }
  public void sendQuit( String why )
  {
    sendCommand("QUIT :" + why);
  }
  public void sendPrivMsg ( IrcString chan, String line )
  {
    sendCommand("PRIVMSG " + chan + " :" + line);
  }
  public void sendPart ( IrcString chan )
  {
    sendCommand("PART " + chan);
  }
  public void sendJoin ( IrcString chan )
  {
    sendCommand("JOIN " + chan);
  }
  public void sendNick ( IrcString nick )
  {
    sendCommand("NICK " + nick);
  }
  public void sendRawCommand ( String cmd ) throws MalformedCommandException
  {
    out.writeCommand(new IrcCommand(cmd));
  }
  public String getHostName()
  {
    return new String(hostName);
  }
};


