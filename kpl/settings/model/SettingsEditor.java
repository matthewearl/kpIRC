package kpl.settings.model;
import java.util.*;

public abstract class SettingsEditor
{
  public class Input
  {
    private int type;
    private String description;
    private String setting;
    private String help;

    private Input(int type, String description, String setting, String help)
    {
      this.type = type;
      this.description = description;
      this.setting = setting;
      this.help = help;
    }

    public int getType()
    {
      return type;
    }

    public String getDescription()
    {
      return description;
    }

    public String getSetting()
    {
      return setting;
    }

    public String getHelp()
    {
      return help;
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

    public List getSubPages()
    {
      return subPages;
    }
  }
  
  private List pages;
  public final static int INPUT_TYPE_COLOUR = 0;
  public final static int INPUT_TYPE_TEXT = 1;
  
  public SettingsEditor()
  {
    pages = new Vector();
  }
  
  protected final Page addPage(String title, Page parent)
  {
    Page page = new Page(title);
    
    if (parent != null)
    {
      parent.subPages.add(page);
    } else
    {
      pages.add(page);
    }
    
    return page;
  }

  protected final Input addInput(Page page, int type, String description, String setting, String help)
  {
    Input input = new Input(type, description, setting, help);
    
    page.inputs.add(input);
    
    return input;
  }
  
  public final List getPages()
  {
    return pages;
  }
}
