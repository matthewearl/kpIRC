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
import ircclient.model.*;
import java.lang.*;
import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

class IrcPage extends Composite implements PaneListener, KeyListener, TraverseListener
{
  protected IrcText outputText;
  protected Text inputText;
  protected Display display;
  protected CTabItem tabItem;
  protected Pane pane;
  protected CompletionPopup completionPopup;
  
  public IrcPage ( Composite parent, CTabItem tabItem, Pane pane, Display display )
  {
    super(parent, SWT.NONE);
    this.pane = pane;
    this.tabItem = tabItem;
    this.display = display;
    
    tabItem.setText(pane.getTitle());
  }
  
  protected void initCompletionPopup()
  {
    completionPopup = new CompletionPopup ( getShell(), inputText );
    completionPopup.addCompletionPopupListener ( new CompletionPopupListener()
        {
          public void completionSelected ( Completion c )
          {
            inputText.insert(c.getUntyped());
          }
        });
  }
  /*
  private Color mircCodeToColor ( int n )
  {
    RGB c;
    switch(n)
    {
      case 0: c = new RGB(0xFF,0xFF,0xFF); break;
      case 1: c = new RGB(0x00,0x00,0x00); break;
      case 2: c = new RGB(0x00,0x00,0x80); break;
      case 3: c = new RGB(0x00,0xFF,0x00); break;
      case 4: c = new RGB(0xFF,0x00,0x00); break;
      case 5: c = new RGB(0x98,0x40,0x00); break;
      case 6: c = new RGB(0x90,0x00,0x90); break;
      case 7: c = new RGB(0xFF,0x80,0x00); break;
      case 8: c = new RGB(0xFF,0xFF,0x00); break;
      case 9: c = new RGB(0x9B,0xFF,0x61); break;
      case 10: c = new RGB(0x00,0xFF,0xFF); break;
      case 11: c = new RGB(0x80,0xFF,0xFF); break;
      case 12: c = new RGB(0x80,0x80,0xFF); break;
      case 13: c = new RGB(0xFF,0x80,0xFF); break;
      case 14: c = new RGB(0x80,0x80,0x80); break;
      case 15: c = new RGB(0xC4,0xC4,0xC4); break;
      case 16: c = new RGB(0xFF,0xFF,0xFF); break;
      case 17: c = new RGB(0x00,0x00,0x00); break;
      case 18: c = new RGB(0x00,0x00,0x80); break;
      case 19: c = new RGB(0x00,0xA0,0x00); break;
      case 20: c = new RGB(0xFF,0x00,0x00); break;
      case 21: c = new RGB(0x98,0x40,0x00); break;
      case 22: c = new RGB(0x90,0x00,0x90); break;
      case 23: c = new RGB(0xFF,0x80,0x00); break;
      case 24: c = new RGB(0xFF,0xFF,0x00); break;
      case 25: c = new RGB(0x9B,0xFF,0x61); break;
      case 26: c = new RGB(0x00,0xFF,0xFF); break;
      case 27: c = new RGB(0x80,0xFF,0xFF); break;
      case 28: c = new RGB(0x80,0x80,0xFF); break;
      case 29: c = new RGB(0xFF,0x80,0xFF); break;
      case 30: c = new RGB(0x80,0x80,0x80); break;
      case 31: c = new RGB(0xC4,0xC4,0xC4); break;
      default: c = new RGB(0x00,0x00,0x00); break;
    }
    return new Color(display, c);
  }

  private class FormattedString
  {
    public StyleRange[] styles;
    public String str;
  }
  
  // Parses str as a mIRC colour coded string, strips its code
  // and returns the associated formatting as a StyleRange[]
  public FormattedString formatString ( String str )
  {
    final int STATE_READING_CHARACTERS = 0;
    final int STATE_READING_FOREGROUND = 1;
    final int STATE_READING_BACKGROUND = 2;
   
    final char MIRC_COLOUR_ESCAPE = '\003';
    final char MIRC_BOLD_CHAR = '\002';
    final char MIRC_UNDERLINE_CHAR = '\031';
    final char MIRC_REVERSE_CHAR = '\022';
    final char MIRC_NORMAL_CHAR = '\015';
    
    StyleRange[] stylesArr;
    StringBuffer outStr = new StringBuffer();
    StringBuffer digits = new StringBuffer();
    Color currentForeground = mircCodeToColor(1);
    Color currentBackground = mircCodeToColor(0);
    boolean currentBold = false;
    boolean currentUnderline = false;
    boolean currentReverse = false;
    int rangeStart = 0;
    
    // Vector of StyleRange, will be converted to an array at the end
    Vector styles = new Vector(); 
    
    int state = STATE_READING_CHARACTERS;
    int nextState;
    
    int j=0;
    for(int i=0;i<str.length();i++)
    {
      char c = str.charAt(i);
      
      nextState = state;
      if((c == MIRC_COLOUR_ESCAPE
          || c == MIRC_BOLD_CHAR
          || c == MIRC_UNDERLINE_CHAR
          || c == MIRC_REVERSE_CHAR
          || c == MIRC_NORMAL_CHAR) )
      {
        styles.add ( new StyleRange(rangeStart, j-rangeStart, currentForeground, 
              currentBackground, currentBold?SWT.BOLD:SWT.NORMAL) );

        switch(c)
        {
          case MIRC_COLOUR_ESCAPE:
            nextState = STATE_READING_FOREGROUND;
            digits = new StringBuffer();
            break;
          case MIRC_BOLD_CHAR: currentBold = !currentBold; break;
          case MIRC_REVERSE_CHAR: currentReverse = !currentReverse; break;
          case MIRC_UNDERLINE_CHAR: currentUnderline = !currentUnderline; break;
          case MIRC_NORMAL_CHAR:
            currentBold = currentUnderline = currentReverse = false;
            currentForeground = mircCodeToColor(1);
            currentBackground = mircCodeToColor(0);
            break;
        }
        
        rangeStart = j;
      }
      if(state == STATE_READING_FOREGROUND || state == STATE_READING_BACKGROUND)
      {
        if(Character.isDigit(c))
        {
          digits.append(c);
        } else
        {
          int num;
          if(digits.length() > 0)
          {
            try{num = Integer.parseInt(digits.toString());}
            catch(NumberFormatException ex){ System.err.println(ex); num=-1; }
          } else
          {
            num = -1;
          }
          if(state == STATE_READING_FOREGROUND)
          {
            if(num == -1)
              num = 1;
            currentForeground = mircCodeToColor(num);
          } else // state == STATE_READING_BACKGROUND
          {
            if(num == -1)
              num = 0;
            currentBackground = mircCodeToColor(num);
          }
          if(c == ',' && state==STATE_READING_FOREGROUND)
          {
            nextState = STATE_READING_BACKGROUND;
            digits = new StringBuffer();
          } else
          {
            nextState = STATE_READING_CHARACTERS;
          }
        }
      }
      
      if((state == STATE_READING_CHARACTERS || nextState == STATE_READING_CHARACTERS ) && 
          c != MIRC_BOLD_CHAR &&
          c != MIRC_REVERSE_CHAR &&
          c != MIRC_UNDERLINE_CHAR &&
          c != MIRC_NORMAL_CHAR)
      {
        outStr.append(c);
        j++;
      }

      state = nextState;
    }
    
    styles.add ( new StyleRange(rangeStart, j-rangeStart, currentForeground, 
          currentBackground, currentBold?SWT.BOLD:SWT.NORMAL) );
    
    FormattedString out = new FormattedString();
    out.str = new String( outStr );
    out.styles = (StyleRange[])styles.toArray(new StyleRange[styles.size()]);

    return out;
  }
  
  public void formatAndAppendText ( String s )
  {
    final FormattedString f = formatString(s);
    
    display.asyncExec( new Runnable()
        {
          public void run()
          {
            // Work out if the text box is scrolled to the bottom
            // so we can decide whether we want the text box to
            // continue to be at the bottom after text insertion.
            boolean atBottom = true;
            int start = outputText.getText().length();
            
            outputText.append(f.str+ "\n");

            for(int k=0;k<f.styles.length;k++)
              f.styles[k].start += start;
            
              outputText.replaceStyleRanges(start,f.str.length(),f.styles);

            if(atBottom)
              outputText.invokeAction(ST.TEXT_END);
          }
        });
  }
  
  public void newText ( String s )
  {
    formatAndAppendText(s);
  }
*/

  public void newLine ( IrcTextLine l )
  {
    // We must run sync as we do not know
    // whether or not the buffer will have
    // been appended by the time this
    // function is eventually called.
    display.syncExec ( new Runnable()
        {
          public void run()
          {
            outputText.newLines(1);
          }
        });
  }

  public void titleChanged ( String s )
  {
    final String title = s;
    display.asyncExec( new Runnable()
        {
          public void run()
          {
            tabItem.setText(title);
          }
        });
  }
  
  public void doCompletion ()
  {
    Completion c;
    Set completions = pane.completeLine( 
        inputText.getText(), inputText.getSelection().x);

    if(completions.size() == 0)
    {
      pane.printLine ( IrcTextLine.ALERT, "No completions");
    } else if(completions.size() == 1)
    {
      c = (Completion)completions.iterator().next();
      inputText.insert(c.getUntyped());
    } else
    {
      completionPopup.open(completions);
    }
  }
  
  public void keyPressed ( KeyEvent e )
  {
    switch(e.keyCode)
    {
      case 13: // New line
        String line = inputText.getText();
        pane.sendLine(line);
        pane.history.add(line);
        inputText.setText("");
        break;
      case SWT.ARROW_UP:
        inputText.setText( pane.history.previous( inputText.getText() ) );
        break;
      case SWT.ARROW_DOWN:
        inputText.setText( pane.history.next( inputText.getText() ) );
        break;
      case SWT.TAB: // Tab
        e.doit = false;
        doCompletion();
        break;
    }

    if(e.keyCode != SWT.TAB)
      completionPopup.close();
  }
  
  public void keyReleased ( KeyEvent e )
  {
  }

  public void keyTraversed ( TraverseEvent e )
  {
    e.doit = (e.detail != SWT.TRAVERSE_TAB_NEXT);
  }
  
  public boolean onCloseClicked ()
  {
    if(!pane.readyToClose())
    {
      pane.deactivate();
      return false;
    }
    pane.close();
    return true;
  }
}

class ServerPage extends IrcPage
{
  ServerPage( Composite parent, CTabItem tabItem, Pane pane, Display display)
  {
    super(parent, tabItem, pane, display);
    
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
      
    setLayout(gridLayout);
      
    outputText = new IrcText(this, pane.getIrcTextLineSource()) ;
    outputText.setBounds(10,10,200,100);
    GridData gd1 = new GridData(GridData.FILL_BOTH);
    gd1.widthHint = 640;
    gd1.heightHint = 480;
    outputText.setLayoutData(gd1);
    
    outputText.pack();
    
    inputText = new Text(this, SWT.BORDER);
    inputText.setBounds(10,220,200,20);

    GridData gd2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    inputText.setLayoutData(gd2);

    initCompletionPopup();
    
    inputText.addKeyListener(this);
    inputText.addTraverseListener(this);
    inputText.setTabs(0);
    // We have to add the PaneListener here rather than in the super constructor
    // because when the superconstructor is called no widgets have been made
    // yet by adding the PaneListener we are instructing the Pane to give us
    // data which needs to go into our widgets.
    pane.addPaneListener(this);
  }
}

class ChannelPage extends IrcPage implements ChannelPaneListener
{
  private ChannelPane channelPane;
  private org.eclipse.swt.widgets.List userList;
  private Text topicText;
  
  ChannelPage( Composite parent, CTabItem tabItem, ChannelPane channelPane, Display display)
  {
    super(parent, tabItem, channelPane, display);
    
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 1;
      
    setLayout(gridLayout);
   
    // topic box
    topicText = new Text(this, SWT.BORDER);
    topicText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
    
    // output/userlist composite
    SashForm comp = new SashForm (this, SWT.NONE);
    GridLayout gridLayout2 = new GridLayout();
    gridLayout2.numColumns = 2;
    comp.setLayout(gridLayout2);
    
    //    output text box
    outputText = new IrcText(comp, channelPane.getIrcTextLineSource()) ;
    
    //    user list
    userList = new org.eclipse.swt.widgets.List(comp, SWT.MULTI | SWT.V_SCROLL );
    
    int []weights = new int[2];
    weights[0] = 10; weights[1] = 2;
    comp.setWeights( weights );
    
    GridData gd1 = new GridData(GridData.FILL_BOTH);
    gd1.widthHint = 640;
    gd1.heightHint = 480;
    comp.setLayoutData(gd1);
    comp.pack();
    
    // input box
    inputText = new Text(this, SWT.BORDER);
    inputText.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

    initCompletionPopup();
    
    channelPane.addChannelPaneListener(this);
    inputText.addTraverseListener(this);
    inputText.addKeyListener(this);
    inputText.setTabs(0);
    // We have to add the PaneListener here rather than in the super constructor
    // because when the superconstructor is called no widgets have been made
    // yet by adding the PaneListener we are instructing the Pane to give us
    // data which needs to go into our widgets.
    pane.addPaneListener(this);
  }

  public void onUserListChanged ( final Collection users )
  {
    display.asyncExec( new Runnable()
    {
      public void run()
      {
        userList.setItems( new String[0] );
        
        Iterator it = users.iterator();
        while(it.hasNext())
        {
          IrcChannelUser u = (IrcChannelUser)it.next();
          userList.add( u.toString() );
        }
      }
    });
  }

  public void onTopicChanged ( final String newTopic )
  {
    display.asyncExec( new Runnable()
    {
      public void run()
      {
        topicText.setText(newTopic);
      }
    });
  }
}

public class IrcGui extends ShellAdapter implements IrcModelListener, CTabFolder2Listener 
{
  private Display display;
  private Shell shell;
  private CTabFolder tabFolder;
  private IrcModel model;
  private HashMap tabsToPanes;
  
  /* IrcModelListener events */
  public void newPane ( final Pane pane )
  {
    final Pane p = pane;

    display.syncExec ( new Runnable()
    {
      public void run()
      {
        // ServerPanes and QueryPanes both use the ServerPage widget
        // as they both have the same functionality
        if(p instanceof ServerPane || p instanceof QueryPane)
        {
          CTabItem ti = new CTabItem( tabFolder, SWT.NONE, pane.index );
          ServerPage page = new ServerPage ( tabFolder, ti, p, display ); 
          ti.setControl(page);
          
          if(p instanceof ServerPane)
            tabFolder.setInsertMark(pane.index, true);
          
          tabsToPanes.put(ti, page);
        } else if(p instanceof ChannelPane)
        {
          ChannelPane cp = (ChannelPane)p;

          CTabItem ti = new CTabItem( tabFolder, SWT.NONE, pane.index );
          ChannelPage page = new ChannelPage ( tabFolder, ti, cp, display ); 
          ti.setControl(page);
          
          tabsToPanes.put(ti, page);
        }
      }
    });
  }
  
  public void hasShutDown ()
  {
    display.asyncExec ( new Runnable()
    {
      public void run()
      {
        shell.close();
      }
    });
  }
  
  /* ShellAdapter events */
  public void shellClosed ( ShellEvent e )
  {
    e.doit = model.hasShutDown();

    if(!e.doit)
    {
      model.shutDown();
    }
  }

  /* CTabFolder2Listener events */
  public void close(CTabFolderEvent event)
  {
    IrcPage page = (IrcPage)tabsToPanes.get(event.item);
    event.doit = page.onCloseClicked();
  }
  public void minimize(CTabFolderEvent event) {}
  public void maximize(CTabFolderEvent event) {}
  public void showList(CTabFolderEvent event) {}
  public void restore(CTabFolderEvent event) {}
  
  /* -------- */
  
  public void run ()
  {
    tabsToPanes = new HashMap();
    display = new Display();
    shell = new Shell(display);
    shell.setText("kpIRC");
    
    shell.setLayout(new FillLayout());
    
    // Menu
    Menu mainMenu = new Menu(shell, SWT.BAR);
    shell.setMenuBar(mainMenu);
    MenuItem ircMenuItem = new MenuItem(mainMenu, SWT.CASCADE);
    ircMenuItem.setText("IRC");
    Menu ircMenu = new Menu(shell, SWT.DROP_DOWN);
    ircMenuItem.setMenu(ircMenu);
    
    MenuItem newServerTabItem = new MenuItem(ircMenu, SWT.NONE);
    newServerTabItem.setText("New Server Tab");
    newServerTabItem.setAccelerator(SWT.ALT | 'S');
    newServerTabItem.addSelectionListener( new SelectionAdapter()
        { public void widgetSelected(SelectionEvent e)
          { model.newServerPane();
          }
        });
    
    tabFolder = new CTabFolder(shell, SWT.CLOSE | SWT.BOTTOM);
    tabFolder.setSimple(false);
    tabFolder.addCTabFolder2Listener(this);
    // End Menu
    
    shell.addShellListener(this);
    
    model = new IrcModel();
    model.addListener( this );
    
    shell.pack();
    shell.open();
    shell.setSize(640,480);
    
    while(!shell.isDisposed())
    {
      if(!display.readAndDispatch())
       {
          display.sleep();
       }
    }
    display.dispose();

    model.shutDown();
  }
}

