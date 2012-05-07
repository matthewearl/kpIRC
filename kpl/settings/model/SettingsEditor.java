package kpl.settings.model;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

public class SettingsEditor
{
  public final static int INPUT_TYPE_COLOUR = 0;
  public final static int INPUT_TYPE_TEXT = 1;
  
  public class Input
  {
    private int type;
    private String description;
    private String setting;
    private String help;

    private Input(int type, String description, String setting, String help)
    {
      super();
      this.type = type;
      this.description = description;
      this.setting = setting;
      this.help = help;
    }
  }
  
  public class Page
  {
    private String title;
    private List subPages;
    private List inputs;
    
    private Page(String title)
    {
      this.title = title;
      this.subPages = new Vector();
      this.inputs = new Vector();
    }
    
    public String getTitle()
    {
      return title;
    }
  }
  
  private List pages;
  
  static private int stringToInputType(String s) throws SettingsEditorParseException
  {
    if (s.equals("text"))
    {
      return INPUT_TYPE_TEXT;
    } else if (s.equals("colour"))
    {
      return INPUT_TYPE_COLOUR;
    }
    
    throw new SettingsEditorParseException("Invalid input type " + s);
  }
    
  private Input parseInput(Element e) throws SettingsEditorParseException
  {
    String attrs[] = {"type", "description", "setting"};
    
    for (String attr : attrs)
    {
      if(!e.hasAttribute(attr))
      {
        throw new SettingsEditorParseException("Input does not have " + attr + " attribute");
      }
    }
    
    return new Input(stringToInputType(e.getAttribute("type")),
                     e.getAttribute("description"),
                     e.getAttribute("setting"),
                     e.getTextContent());
  }
  
  private void parsePages(Node node, List pages, List inputs) throws SettingsEditorParseException
  {
    NodeList childNodes = node.getChildNodes();
    
    for (int i = 0; i < childNodes.getLength(); i++)
    {
      Node childNode = childNodes.item(i);      
      if(childNode.getNodeType() == Node.ELEMENT_NODE)
      {
        Element e = (Element)childNode;
        
        if (e.getTagName().equals("page")) {
          if (!e.hasAttribute("title"))
          {
            throw new SettingsEditorParseException("Page does not have title attribute;");
          }
          Page page = new Page(e.getAttribute("title"));
          pages.add(page);
          parsePages(e, page.subPages, page.inputs);
        } else if (e.getTagName().equals("input")) {
          if (inputs != null)
          {
            inputs.add(parseInput(e));
          } else
          {
            throw new SettingsEditorParseException("input tag must be inside a page");
          }
        } else {
          throw new SettingsEditorParseException("Invalid tag " + e.getTagName());
        }
      }
    }
  }
  
  private void parseDoc(Document doc) throws SettingsEditorParseException
  {
    parsePages(doc.getDocumentElement(), this.pages, null);
  }
  
  public SettingsEditor(InputStream xmlInputStream) throws IOException, SettingsEditorParseException
  {
    pages = new Vector();
    
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = factory.newDocumentBuilder();
      Document doc;
      
      parseDoc(db.parse(xmlInputStream)); 
    } catch(ParserConfigurationException e)
    {
      throw new SettingsEditorParseException(e);
    } catch(SAXException e) 
    {
      throw new SettingsEditorParseException(e);
    }
  }
}
