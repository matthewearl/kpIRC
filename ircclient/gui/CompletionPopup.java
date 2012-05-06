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

import java.util.*;
import java.lang.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import ircclient.model.*;

public class CompletionPopup
{
  private Shell parentShell, shell;
  private StyledText list;
  private Color grey,white;
  private Text textBox;
  private Vector completionPopupListeners;
  private Completion[] completions;
  
  CompletionPopup ( final Shell parentShell, final Text textBox )
  {
    this.parentShell=parentShell;
    this.textBox = textBox;
    
    shell = new Shell ( parentShell, SWT.NO_TRIM | SWT.ON_TOP );
    shell.setLayout(new FillLayout());
    list = new StyledText ( shell, SWT.READ_ONLY | SWT.MULTI );
    grey = new Color(parentShell.getDisplay(), 128,128,128);
    white = new Color(parentShell.getDisplay(), 255,255,255);
    completionPopupListeners = new Vector();
    
    list.addMouseListener ( new MouseAdapter()
        {
          public void mouseDown(MouseEvent e)
          {
            int offs;
            try
            {
              offs = list.getOffsetAtLocation( new Point(e.x,e.y) );
            } catch ( IllegalArgumentException ex )
            {
              // getOffsetAtLocation unhelpfully throws an exception if Point
              // is not over some text.
              return;
            }
            Completion c = completions[ list.getLineAtOffset(offs) ];
            for(int i=0;i<completionPopupListeners.size();i++)
            {
              CompletionPopupListener l  = (CompletionPopupListener)completionPopupListeners.elementAt(i);
              l.completionSelected( c );
            }
            close();
          }
        });
    shell.addShellListener ( new ShellAdapter()
        {
          public void shellActivated(ShellEvent e)
          {
            parentShell.forceActive();
          }
        });
    
    shell.setBounds ( new Rectangle ( 0, 0, 0, 0 ) );
    shell.open();
  }
  
  public void addCompletionPopupListener ( CompletionPopupListener l )
  {
    completionPopupListeners.add(l);
  }
  
  public void open ( Set completions )
  {
    int pos=0;
    
    this.completions = (Completion[])completions.toArray(new Completion[completions.size()]);
    
    list.setText("");
    Iterator it = completions.iterator();
    while(it.hasNext())
    {
      Completion c = (Completion)it.next();

      list.append(c.getFull() + "\n");
      list.setStyleRange ( new StyleRange ( pos, c.getNumTyped(), grey, white ));
      pos += c.getFull().length() + 1;
    }

    Point corner = textBox.toDisplay( 0, textBox.getLineHeight());
    
    shell.setBounds ( new Rectangle ( corner.x, corner.y, 
          100, textBox.getLineHeight()*completions.size()));
        
    shell.setVisible(true);
  }
 
  public void close ()
  {
    shell.setVisible(false);
  }
}

