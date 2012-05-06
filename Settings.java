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

import javax.xml.parsers.*;
import java.io.*;
import org.w3c.dom.*;
import java.lang.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

public class Settings
{
  static private Node rootNode;
  static private Document doc;
  
  static
  {
    try
    {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = factory.newDocumentBuilder();

      doc = db.parse( new File ( "Settings.xml" )); 
      rootNode = doc.getDocumentElement();
    } catch ( Exception e )
    {
      e.printStackTrace();
      System.exit(-1);
    }
  }
  
  synchronized static private Element getChildElementWithAttribute ( Node parent, String tagName, String attName, String attVal )
  {
    NodeList children = parent.getChildNodes();
    
    for(int i=0;i<children.getLength();i++)
    {
      Node child = children.item(i);
      if(child.getNodeType() == Node.ELEMENT_NODE)
      {
        Element e = (Element)child;
        if(e.getTagName().equals(tagName) &&
            e.hasAttribute(attName) &&
            e.getAttribute(attName).equals(attVal))
        {
          return e;
        }
      }
    }
    return null;
  }
  
  synchronized static private Element createChildElement ( Node parent, String tagName )
  {
	Element child = null;
    try
    {
      child = doc.createElement ( tagName );
      parent.appendChild ( child );
    } catch ( DOMException e )
    {
      e.printStackTrace();
      System.exit(-1);
    }
    return child;
  }
  
  // get a child element, creating it if it doesn't exist
  synchronized static private Element makeChildElementWithAttribute ( Node parent, String tagName, String attName, String attVal )
  {
    Element ch = getChildElementWithAttribute ( parent, tagName, attName, attVal );
    if(ch == null)
    {  
      ch = createChildElement ( parent, tagName );

      try
      {
        ch.setAttribute ( attName, attVal );
      } catch ( DOMException e )
      {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    return ch;
  }
  
  synchronized static public SettingValue get ( String keyPath )
  {
    String [] pathComponents = keyPath.split("\\.");

    Element el = (Element)rootNode;
    
    for(int i=0;i<pathComponents.length - 1;i++)
    {
      el = getChildElementWithAttribute( el, "section", "name", pathComponents[i] );
      if(el == null)
        throw new BadKeyPathException( keyPath );
    }
    
    String keyName = pathComponents[ pathComponents.length - 1 ];
    el = getChildElementWithAttribute( el, "setting", "key", keyName );
    if(el == null)
      throw new BadKeyPathException( keyPath );

    if(!el.hasAttribute("value"))
      throw new BadKeyPathException( keyPath );

    
    return new SettingValue(el.getAttribute("value"));
  }
 
  synchronized static public void put ( String keyPath, SettingValue value )
  {
    String [] pathComponents = keyPath.split("\\.");

    Element el = (Element)rootNode;
    
    for(int i=0;i<pathComponents.length - 1;i++)
    {
      el = makeChildElementWithAttribute( el, "section", "name", pathComponents[i] );
    }

    String keyName = pathComponents[ pathComponents.length - 1 ];
    el = makeChildElementWithAttribute ( el, "setting", "key", keyName );
    
    el.setAttribute ( "value", value.toString() );

    save();
  }
 
  // Try and get a value, but if the path does not exist, make the setting with
  // a given default value.
  synchronized static public SettingValue makeAndGet ( String keyPath, SettingValue defaultValue )
  {
    try
    {
      return get ( keyPath );
    } catch ( BadKeyPathException ex )
    {
      put ( keyPath, defaultValue );
      return defaultValue;
    }
  }
  
  // Code taken from
  synchronized static public void save ()
  {
    try
    {
      Source source = new DOMSource(doc);
      Result result = new StreamResult ( new File ( "Settings.xml" ));
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.transform(source, result);
    } catch ( TransformerConfigurationException e)
    {
      System.err.println("Warning, failed to write settings");
    } catch ( TransformerException e )
    {
      System.err.println("Warning, failed to write settings");
    }
  }
  
  synchronized static public void main ( String []args )
  {
    if(args.length < 1)
    {
      System.err.println("setting key path must be passed as an argument");
      return;
    }

    try
    {
      System.out.println ( get ( args[0] ) );
    } catch ( Exception e )
    {
      e.printStackTrace();
    }
  }
}

