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

package ircclient.gui;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.events.*;
import ircclient.model.*;
import java.util.*;
import java.lang.*;

/**
 * This class is displays IrcTextLines
 */
public class IrcText extends Canvas implements PaintListener, SelectionListener, ControlListener
{
  private IrcTextLineSource lineSource;

  // thumbSize is constantly 1. This is to avoid issues with having to calculate
  // varying line heights for all the lines in the buffer.
  //
  // minimum is constantly zero, and maximum is lineSource.size().
  // lineSource[selection] is the line drawn at the bottom of the control.
  private ScrollBar scrollBar;
  // Used purely in widgetSelected to determine how far we scrolled
  private int lastScrollPosition;
 
  private Vector wrappedLines;

  // Colors
  private final Color plainForegroundColor, plainBackgroundColor, 
          joinColor, alertColor, infoColor, leaveColor;

  public IrcText ( Composite parent, IrcTextLineSource lineSource )
  {
    super(parent, SWT.V_SCROLL);

    this.lineSource = lineSource;

    scrollBar = getVerticalBar();
    
    scrollBar.addSelectionListener(this);
    addPaintListener(this);
    addControlListener(this);
    
    scrollBar.setMinimum(0);
    scrollBar.setMaximum(1);
    scrollBar.setThumb(1);

    if(lineSource.size() > 0)
      scrollBar.setSelection(lineSource.size()-1);
    else
      scrollBar.setSelection(0);
    
    // an empty lineSource will mean an effective lastScrollPosition
    // of -1, (eg. there are no items to view), unfortunately we
    // cannot let the selection to -1, so we need to work around the
    // issue.
    lastScrollPosition = lineSource.size()-1;

    // Allocate colours now to speed stuff up later
    plainForegroundColor = new Color ( getDisplay(), 0, 0, 0 );
    plainBackgroundColor = new Color ( getDisplay(), 255, 255, 255 );
    alertColor = new Color ( getDisplay(), 255, 0, 0 );
    joinColor = new Color ( getDisplay(), 0, 128, 0 );
    leaveColor = new Color ( getDisplay(), 0x98, 0x40, 0 );
    infoColor = new Color ( getDisplay(), 0, 0, 128 );

    wrappedLines = null;
  }
  
  void initWrappedLines ()
  {
    wrappedLines = new Vector();

    int i = getLastVisibleLineIndex();

    GC gc = new GC(this);
    Rectangle c = gc.getClipping();
    
    int y = c.y + c.height;
    
    while( y > c.y && i >= 0)
    {
      IrcTextLine l = lineSource.getLine(i);
      WrappedLine wl = wrapIrcTextLine ( gc, l );
      wrappedLines.add(wl);
      y -= wl.size.y;
      i--;
    }  

    gc.dispose();
  }

  // Gets the index of the last line visible in the buffer
  int getLastVisibleLineIndex ()
  {
    if(lineSource.size() == 0)
      return -1;
    
    return scrollBar.getSelection();
  }
  
  // Should be called after a resize only
  void updateWrappedLines ()
  {
    int offset=0;
    GC gc = new GC(this);
    Rectangle c = gc.getClipping();
    
    int rightEdge = c.x + c.width;
    int y = c.y + c.height;
    int i = 0;
    while( y > c.y )
    {
      WrappedLine wl;
      
      int index = getLastVisibleLineIndex() - i;
      if(index < 0)
        break;
      IrcTextLine l = lineSource.getLine(index);
      
      if( i < wrappedLines.size() )
      {
        wl = (WrappedLine)wrappedLines.get(i);
        if(rightEdge <= wl.size.x || wl.isWrapped() )
          wl = wrapIrcTextLine( gc, l );
      } else
      {
        wl = wrapIrcTextLine(gc,l);
      }
      y -= wl.size.y;
      
      if(i >= wrappedLines.size())
        wrappedLines.add(wl);
      else
        wrappedLines.set(i,wl);

      i++;
    }

    // This will occur if lines wrapped pushing lines off the top
    // or if the control was shrunk vertically, removing lines from
    // the top.
    if(wrappedLines.size() > i)
      wrappedLines.setSize(i);
    
    gc.dispose();
  }
  
  // returns the height of a single (non wrapped) line of text
  public int getLineHeight ()
  {
    GC gc = new GC(this);
    int out = gc.getFontMetrics().getHeight();
    gc.dispose();
    return out;
  }

  public int getLineHeight ( GC gc )
  {
    return gc.getFontMetrics().getHeight();
  }
  
  // gets the number of non-wrapped lines the visible control can
  // contain at once
  public int getNumVisibleLines ()
  {
    GC gc = new GC(this);
    int out = 1+gc.getClipping().height/getLineHeight();
    gc.dispose();
    return out;
  }

  // up means the scroll bar selection decreased, eg. we are now looking
  // at older lines.
  private void scrollUp ( int n )
  {
    if(n >= wrappedLines.size())
    {
      // In this case there is no overlap between the two scroll positions
      // so we must completely rebuild wrappedLines, and redraw the whole
      // screen
      initWrappedLines();
      redraw();
      return;
    }
    
    GC gc = new GC(this);
    Rectangle c = gc.getClipping();
    gc.dispose();

    // Calculate how many pixels we have shifted by, and update
    // wrappedLines to the new position
    int offset = 0;
    for(int i=0;i<n;i++)
      offset += ((WrappedLine)wrappedLines.get(i)).size.y;
    for(int i=0;i<n;i++)
      wrappedLines.removeElementAt(0);
    updateWrappedLines();
   
    scroll(c.x, c.y+offset, c.x, c.y, c.width,c.height,false);
  }
  
  // We are looking at newer lines
  private void scrollDown ( int n )
  {
    int offset = 0;
    
    GC gc = new GC(this);
    Rectangle c = gc.getClipping();
   
    int y = c.y + c.height;
    int i = 0;
    
    while( y > c.y && i < n )
    {
      int index = getLastVisibleLineIndex() - i;
      IrcTextLine l = lineSource.getLine(index);
      WrappedLine wl = wrapIrcTextLine( gc, l );
      
      wrappedLines.add(i, wl);

      offset += wl.size.y;
      
      i++;
      y -= wl.size.y;
    }
    
    if( y <= c.y )
    {
      // We scrolled so far we can not retain any information
      // from wrappedLines
      wrappedLines.setSize(i);
      redraw();
      gc.dispose();
      return;
    }
    
    // otherwise continue getting lines so we can determine
    // how many lines are in view, and hence how long wrappedLines
    // should be
    while( y > c.y && i < wrappedLines.size() )
    {
      WrappedLine wl = (WrappedLine)wrappedLines.get(i);
      y -= wl.size.y;
      i++;
    }
    wrappedLines.setSize(i);
    
    scroll(c.x, c.y-offset, c.x, c.y, c.width,c.height,false);
    
    gc.dispose();
  }
  
  // from should be the value of scrollBar.getSelection() before
  // the scroll associated with this call.
  //
  // Also handles updating of wrappedLines 
  private void scrollText ( int from )
  {
    int to = scrollBar.getSelection();
    if(to < from)
      scrollUp(from - to);
    else if(from < to)
      scrollDown(to - from);
  }
  
  // n: the number of new lines
  public void newLines ( int n )
  {
    int oldMax = scrollBar.getMaximum();
    scrollBar.setMaximum(lineSource.size());
    
    // If scroll bar was at the bottom, keep it at the bottom
    if(getLastVisibleLineIndex() == oldMax - 1)
    {
      scrollBar.setSelection(lineSource.size() - 1);
      
      // Have the scrollbar event handler redraw the appropriate
      // area of the screen
      widgetSelected(null);
    }
  }
 
  private class ControlTooNarrowException extends Exception
  {
    ControlTooNarrowException()
    {
      super("Window too narrow to print correctly wrapped line");
    }
  }
  
  private class ColoursAndString
  {
    public Color fg, bg;
    public String s;
  }
  
  public ColoursAndString coloursAndStringForIrcTextLine ( IrcTextLine l )
  {
    ColoursAndString out = new ColoursAndString();

    out.fg = plainForegroundColor;
    out.bg = plainBackgroundColor;

    if(l.type == IrcTextLine.SPEECH)
    {
      out.s =  "<" + l.nick + "> " + l.text;;
    } else
    {
      switch(l.type)
      {
        case IrcTextLine.ALERT: out.fg = alertColor; break;
        case IrcTextLine.JOIN: out.fg = joinColor; break;
        case IrcTextLine.LEAVE: out.fg = leaveColor; break;
        case IrcTextLine.INFO: out.fg = infoColor; break;
      }
      out.s =  "*** " + l.text;;
    }
    return out;
  }
  
  public WrappedLine wrapIrcTextLine ( GC gc, IrcTextLine l )
  {
    ColoursAndString cas = coloursAndStringForIrcTextLine ( l );
    Rectangle c = gc.getClipping();
    return new WrappedLine ( getDisplay(), gc, cas.s, cas.fg, cas.bg, c.x, c.x, c.x+c.width );
  }
  
  public void paintBackground ( int x, int y, int width, int height )
  {
    GC gc = new GC(this);
    gc.setBackground ( new Color( getDisplay(), 255, 255, 255 ));
    gc.fillRectangle ( x, y, width, height );
    gc.dispose();
  }
  
  // PaintListener for Canvas
  // we draw from the bottom up so we stay aligned with
  // the bottom edge of the screen
  public void paintControl ( PaintEvent e )
  {
    if(wrappedLines == null)
      initWrappedLines();
    paintBackground( e.x, e.y, e.width, e.height );
    
    Rectangle c = e.gc.getClipping();
    
    int y = c.y + c.height;
    for(int i=0;i<wrappedLines.size();i++)
    {
      WrappedLine wl = (WrappedLine)wrappedLines.get(i);
      
      if( y < c.y )
        break;
      
      if(y - wl.size.y < c.y + c.height)
        wl.paint ( e.gc, y );
      y -= wl.size.y;
    }
  }
  
  // SelectionListener for Scrollbar
  public void widgetSelected(SelectionEvent e)
  {
    // Perform all outstanding paint events
    update();
    
    scrollText(lastScrollPosition);
    lastScrollPosition = scrollBar.getSelection();
  }
  
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  // ControlListener for Canvas
  public void controlResized(ControlEvent e)
  {
    if(wrappedLines == null)
      initWrappedLines();
    else
      updateWrappedLines();
  }

  public void controlMoved(ControlEvent e)
  {
  }
}

