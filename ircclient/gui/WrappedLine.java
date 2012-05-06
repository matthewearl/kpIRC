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
import org.eclipse.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

class LineSection
{
  public String str;
  public Color fg, bg;
  // position relative to the top left corner
  // of the WrappedLine
  public final Point pos;
  
  LineSection ( Color fg, Color bg, int x, int y )
  {
    this.fg = fg; this.bg = bg;
    str = new String();
    pos = new Point( x, y );
  }
}

class MircColourToken
{
  final public static int CHARACTER = 0;
  final public static int FOREGROUND = 1;
  final public static int BACKGROUND = 2;
  final public static int BOLD = 3;
  final public static int UNDERLINE = 4;
  final public static int REVERSE = 5;
  final public static int NORMAL = 6;
  final public static int EOS = 7;
  final public int type;
  
  final public char character;
  final public int colour;

  MircColourToken ( char c )
  {
    this.type = CHARACTER;
    this.character = c;
    this.colour = 0;
  }

  MircColourToken ( int type )
  {
    this.type = type;
    this.colour = 0;
    this.character = 'A';
  }

  MircColourToken ( int type, int colour )
  {
    this.type = type;
    this.colour = colour;
    this.character = 'A';
  }
    
  static public Color mircCodeToColor ( Display d, int n )
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
    return new Color(d, c);
  }

  void effectGC ( Display d, GC gc, Color defaultFG, Color defaultBG )
  {
    switch(type)
    {
      case EOS:
        throw new RuntimeException("Tried to effect GC with an End-of-Stream token");
      case CHARACTER:
        throw new RuntimeException("Tried to effect GC with a character token");
      case FOREGROUND:
        gc.setForeground(mircCodeToColor(d, colour));
        break;
      case BACKGROUND:
        gc.setBackground(mircCodeToColor(d, colour));
        break;
      case NORMAL:
        gc.setForeground(defaultFG);
        gc.setBackground(defaultBG);
        break;
      case REVERSE:
        gc.setForeground(defaultBG);
        gc.setBackground(defaultFG);
      case BOLD:
      case UNDERLINE:
        break;
    }
  }
}

class MircColourCodeReader
{
  private String s;
  private int position;

  private final int NORMAL = 0;
  private final int JUST_READ_FG = 1;

  private int state;
  
  MircColourCodeReader ( String s )
  {
    this.s = s;
    state = NORMAL;
  }
 
  // precondition: numberNext()
  int readNumber ( boolean foreground )
  {
    StringBuffer buf = new StringBuffer();

    while(position < s.length() && Character.isDigit(s.charAt(position)))
    {
      buf.append(s.charAt(position));
      position++;
    }

    if(foreground)
      state = JUST_READ_FG;
    else
      state = NORMAL;
    
    return Integer.parseInt(buf.toString());
  }
  
  boolean numberNext()
  {
    return (position+1)<s.length() && Character.isDigit(s.charAt(position));
  }
  
  MircColourToken readToken ()
  {
    if( position >= s.length() )
      return new MircColourToken ( MircColourToken.EOS );
    char c = s.charAt(position++);

    if(state == JUST_READ_FG && c == ',' && numberNext() )
    {
      return new MircColourToken( MircColourToken.BACKGROUND, readNumber(false) );
    }
    
    switch(c)
    {
      case 3:
        if(numberNext())
          return new MircColourToken( MircColourToken.FOREGROUND, readNumber(true));
        else
          return new MircColourToken ( MircColourToken.NORMAL );
      case 2: return new MircColourToken( MircColourToken.BOLD );
      case 31: return new MircColourToken( MircColourToken.UNDERLINE );
      case 22: return new MircColourToken( MircColourToken.REVERSE );
      case 0xf: return new MircColourToken( MircColourToken.NORMAL );
      default: return new MircColourToken( c );
    }
  }

  // Read until we get a character, effecting the given GC if it is non-null
  Character getCharacter ( Display d, GC gc, Color defaultFG, Color defaultBG )
  {
    while(0<1)
    {
      MircColourToken t = readToken();
      if(t.type == MircColourToken.CHARACTER)
        return new Character(t.character);
      if(t.type == MircColourToken.EOS)
        return null;
      if(gc != null)
        t.effectGC( d, gc, defaultFG, defaultBG );
    }
  }

  Character getCharacter ( Display d, GC gc )
  {
    return getCharacter(d, gc, new Color ( d, 0,0,0 ),
        new Color ( d, 255,255,255 ));
  }
  
  Character getCharacter ()
  {
    return getCharacter(null,null,null,null);
  }
}

public class WrappedLine
{
  private boolean wrapped;
  private Vector lineSections;
  public Point size;
  
  public int getLineHeight ( GC gc )
  {
    return gc.getFontMetrics().getHeight();
  }
  
  public WrappedLine ( Display d, GC gc, String s, Color defaultFG, Color defaultBG, int xStart, int xWrap, int xMax )
  {
    MircColourCodeReader reader = new MircColourCodeReader ( s );   
    MircColourToken tok;
    
    Color currentFG = defaultFG;
    Color currentBG = defaultBG;
    
    int nextX;
    int x = xStart;
    int y = 0;
    size = new Point(0,0);
    lineSections = new Vector();
    wrapped = false;
    
    LineSection ls = new LineSection( currentFG, currentBG, x, y);
    StringBuffer buf = new StringBuffer();
    
    while( (tok = reader.readToken()).type != MircColourToken.EOS )
    {
      if(tok.type == MircColourToken.CHARACTER)
      {
        nextX = x + gc.textExtent(new Character(tok.character).toString()).x;
          
        if(nextX > xMax)
        {
          nextX = xWrap;
          ls.str = buf.toString();
          if(ls.str.length() > 0)
            lineSections.add(ls);
          
          // Start a new line section upon changing line...
          y += getLineHeight(gc);
          ls = new LineSection( currentFG, currentBG, nextX, y );
          buf = new StringBuffer();

          wrapped = true;
        }
        if(nextX > size.x)
          size.x = nextX;
        x = nextX;
        buf.append(tok.character);
      } else
      {
        // Style has changed so start a new line section here
        ls.str = buf.toString();
        if(ls.str.length() > 0)
          lineSections.add(ls);

        switch(tok.type)
        {
          case MircColourToken.FOREGROUND:
            currentFG = MircColourToken.mircCodeToColor( d, tok.colour );
            break;
          case MircColourToken.BACKGROUND:
            currentBG = MircColourToken.mircCodeToColor( d, tok.colour );
            break;
          case MircColourToken.NORMAL:
            currentBG = defaultBG;
            currentFG = defaultFG;
            break;
          case MircColourToken.REVERSE:
            currentBG = defaultFG;
            currentFG = defaultBG;
            break;
        }
        
        ls = new LineSection( currentFG, currentBG, x, y );
        buf = new StringBuffer();
      }
    }
    ls.str = buf.toString();
    if(ls.str.length() > 0)
      lineSections.add(ls);
    
    size.y = y + getLineHeight(gc);
  }

  // the line is drawn from the yth pixel up
  public int paint ( GC gc, int y )
  {
    y -= size.y;
    
    for(int i=0;i<lineSections.size();i++)
    {
      LineSection ls = (LineSection)lineSections.get(i);
      
      gc.setForeground ( ls.fg );
      gc.setBackground ( ls.bg );
      
      gc.drawText ( ls.str, ls.pos.x, ls.pos.y + y );
    }

    return y;
  }

  public boolean isWrapped ()
  {
    return wrapped;
  }
}

