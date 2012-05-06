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

import java.lang.*;
import org.eclipse.swt.graphics.RGB;

// TODO, Strings should be escaped by some means before being stored
// and unescaped before being read
public class SettingValue
{
  private String val;

  SettingValue ( String val )
  {
    this.val = val;
  }
  SettingValue ( int i )
  {
    this.val = String.valueOf(i);
  }
  SettingValue ( boolean b )
  {
    this.val = String.valueOf(b);
  }
  SettingValue ( RGB rgb )
  {
    int r = rgb.red;
    int g = rgb.green;
    int b = rgb.blue;

    StringBuffer rStr = new StringBuffer ( Integer.toHexString(r) );
    StringBuffer gStr = new StringBuffer ( Integer.toHexString(g) );
    StringBuffer bStr = new StringBuffer ( Integer.toHexString(b) );
    while(rStr.length() < 2)
      rStr.insert(0, '0');
    while(gStr.length() < 2)
      gStr.insert(0, '0');
    while(bStr.length() < 2)
      bStr.insert(0, '0');
    val = "#" + rStr + gStr + bStr;
    val = val.toUpperCase();
  }

  public String toString ()
  {
    return val;
  }

  public String asString ()
  {
    return val;
  }

  public int asInt ()
  {
    try
    {
      return Integer.parseInt ( val );
    } catch ( NumberFormatException ex )
    {
      throw new SettingConversionException ( "Setting is not readable as an integer" );
    }
  }
  
  public boolean asBoolean ()
  {
    if(val.equalsIgnoreCase("true"))
      return true;
    else if(val.equalsIgnoreCase("false"))
      return false;

    throw new SettingConversionException ( "Setting is not readable as a boolean" );
  }

  public RGB asRGB ()
  {
    if(val.length() != 7 || val.charAt(0) != '#')
      throw new SettingConversionException ( "Setting is not readable as an RGB" );
    try
    {
      return new RGB ( Integer.parseInt(val.substring(1,3), 16),
          Integer.parseInt(val.substring(3,5), 16),
          Integer.parseInt(val.substring(5,7), 16));
    } catch ( NumberFormatException ex )
    {
      throw new SettingConversionException ( "Setting is not readable as an RGB" );
    }
  }
}
