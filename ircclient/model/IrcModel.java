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
import java.util.*;

public class IrcModel
{
  private LinkedList panes;
  private Vector listeners;
  // once shuttingDown has been set true, it can not be set false
  // when shuttingDown is true, no new connections should be made.
  private boolean shuttingDown;
  
  public IrcModel()
  {
    shuttingDown = false;
    panes = new LinkedList();
    panes.add( new ServerPane(this,0) );
    listeners = new Vector();
  }

  public void addListener ( IrcModelListener l )
  {
    listeners.add(l);

    // Inform this new listener of all the panes we have open already
    Iterator it = panes.iterator();
    while(it.hasNext())
    {
      Pane p = (Pane)it.next();
      l.newPane(p);
    }
  }
  
  public void notifyListenersOfNewPane ( Pane p )
  {
    for(int i=0;i<listeners.size();i++)
    {
      IrcModelListener listener = (IrcModelListener)listeners.elementAt(i);
      listener.newPane(p);
    }
  }
  
  // Returns the index where the new pane should go
  public int makeSpaceForNewPane ( ServerPane serverPane )
  {
    int index = serverPane.index + 1 + serverPane.channelPanes.size();
    if(index < panes.size())
    {
      // Shift along the index of all panes after the new tab,
      // if there are any
      ListIterator it = panes.listIterator(index);
      while(it.hasNext())
      {
        Pane p = (Pane)it.next();
        p.index++;
      }
    }
    return index;
  }
  
  public ChannelPane makeNewChannelPane ( IrcString chanName, ServerPane serverPane )
  {
    int index = makeSpaceForNewPane ( serverPane );
    ChannelPane cp = new ChannelPane( this, chanName, serverPane, index );
    panes.add(index,cp);
    notifyListenersOfNewPane( cp );
    return cp;
  }
  
  public QueryPane makeNewQueryPane ( IrcString who, ServerPane serverPane )
  {
    int index = makeSpaceForNewPane ( serverPane );
    QueryPane qp = new QueryPane( this, who, serverPane, index );
    panes.add(index,qp);
    notifyListenersOfNewPane( qp );
    return qp;
  }

  /** Makes a new empty server pane */
  public void newServerPane ()
  {
    ServerPane sp = new ServerPane ( this, panes.size() );
    panes.add(panes.size(),sp);
    for(int i=0;i<listeners.size();i++)
    {
      IrcModelListener listener = (IrcModelListener)listeners.elementAt(i);
      listener.newPane(sp);
    }
  }
  
  public void shutDown ()
  {
    if(shuttingDown)
      return;
    shuttingDown = true;
    Iterator it = panes.iterator();
    while(it.hasNext())
    {
      Pane p = (Pane)it.next();
      if(p instanceof ServerPane)
      {
        ServerPane sp = (ServerPane)p;
        sp.deactivate();
      }
    }
  }

  public boolean isShuttingDown ()
  {
    return shuttingDown;
  }
  
  public boolean hasShutDown ()
  {
    Iterator it = panes.iterator();
    while(it.hasNext())
    {
      Pane p = (Pane)it.next();
      if(p instanceof ServerPane)
      {
        ServerPane sp = (ServerPane)p;
        if(sp.isActive())
          return false;
      }
    }
    return true;
  }
  
  public void checkShutDown ()
  {
    if(hasShutDown() && shuttingDown)
    {
      // We have shut down... so notify all listeners
      for(int i=0;i<listeners.size();i++)
      {
        IrcModelListener listener = (IrcModelListener)listeners.elementAt(i);
        listener.hasShutDown();
      }
    }
  }

  public void removePane ( Pane p )
  {
    Iterator it = panes.listIterator();
    boolean deleted = false;
    
    while(it.hasNext())
    {
      Pane q = (Pane)it.next();
      if(!deleted)
      {
        if(p==q)
        {
          it.remove();
          deleted = true;
        }
      } else
      {
        q.index--;
      }
    }
  }
}

