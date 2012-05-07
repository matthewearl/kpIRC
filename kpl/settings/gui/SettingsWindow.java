package kpl.settings.gui;

import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import kpl.settings.model.*;
import kpl.settings.model.SettingsEditor.*;
import kpl.settings.Settings;

public class SettingsWindow
{
  private Shell shell;
  private Shell parent;
  private SettingsEditor settingsEditor;
  private Map itemsToPages;
  private Map itemsToPageControls;
  private Composite pageContainer;
  private StackLayout pageContainerLayout;
  
  private class SettingInput extends Composite
  {
    public SettingInput(Composite parent, Input input)
    {
      super(parent, 0);
      setLayout(new FillLayout());
      
      Label label = new Label(this, 0);
      label.setText(input.getDescription() + ": " + input.getHelp());
    }
  }

  private class TextSettingInput extends SettingInput
  {
    public TextSettingInput(Composite parent, Input input)
    {
      super(parent, input);
    }
  }

  private class ColourSettingInput extends SettingInput
  {
    public ColourSettingInput(Composite parent, Input input)
    {
      super(parent, input);
    }
  }
  
  private class PageControl extends Composite
  {
    PageControl(Page page)
    {
      super(pageContainer, 0);
      
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 1;
      setLayout(gridLayout);
      
      Iterator it = page.getInputs().iterator();
      while (it.hasNext())
      {
        Input input = (Input)it.next();

        switch(input.getType())
        {
        case SettingsEditor.INPUT_TYPE_TEXT:
          new TextSettingInput(this, input);
          break;
        case SettingsEditor.INPUT_TYPE_COLOUR:
          new ColourSettingInput(this, input);
          break;
        default:
          throw new RuntimeException("Invalid input type: " + input.getType());
        }
      }
    }
  }
  
  /**
   * Associate page with item and add children to a tree item representing a Page.
   */
  private void completeTreeItem(TreeItem treeItem, final Page page)
  {
    treeItem.setText(page.getTitle());
    PageControl pageControl = new PageControl(page);
    itemsToPages.put(treeItem, page);
    itemsToPageControls.put(treeItem, pageControl);
    addSubPages(page, treeItem);
    
  }
  
  private void addSubPages(Page parentPage, TreeItem parentItem)
  {
    Iterator it = parentPage.getSubPages().iterator();
    while (it.hasNext())
    {
      Page page = (Page)it.next();
      TreeItem treeItem = new TreeItem(parentItem, 0);
      completeTreeItem(treeItem, page);
    }
  }
  
  public SettingsWindow(Shell parent, SettingsEditor settingsEditor)
  {
    this.parent = parent;
    this.settingsEditor = settingsEditor;
    itemsToPages = new HashMap();
    itemsToPageControls = new HashMap();
    
    // Create shell
    shell = new Shell(parent);
    shell.setText("Settings");
    
    // Grid layout for the whole shell. Elements are a tree and page container.
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 4;
    gridLayout.makeColumnsEqualWidth = true;
    shell.setLayout(gridLayout);
    
    // Tree
    Tree tree = new Tree(shell, 0);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 1;
    gridData.verticalSpan = 1;
    tree.setLayoutData(gridData);
    
    // Page container (a Composite for holding the pages in a StackLayout).
    pageContainer = new Composite(shell, 0);
    gridData = new GridData(GridData.FILL_BOTH);
    gridData.horizontalSpan = 3;
    gridData.verticalSpan = 1;
    pageContainer.setLayoutData(gridData);
    pageContainerLayout = new StackLayout();
    pageContainer.setLayout(pageContainerLayout);
    
    // Add TreeItems into the tree and corresponding PageControls into the page container.
    Iterator it = settingsEditor.getPages().iterator();
    while (it.hasNext())
    {
      final Page page = (Page)it.next();
      TreeItem treeItem = new TreeItem(tree, 0);
      completeTreeItem(treeItem, page);
    }
    
    // Make the correct page come to the top as the Tree is manipulated.
    tree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e)
      {
        TreeItem item = (TreeItem)e.item;
        Page page = (Page)itemsToPages.get(item);
        System.out.println("" + page.getTitle() + " selected");
        pageContainerLayout.topControl = (Control)itemsToPageControls.get(item);
        pageContainer.layout();
      }
    });
  }

  public void close()
  {
    shell.close();
  }

  public void dispose()
  {
    shell.dispose();
  }

  public void open()
  {
    shell.open();
  }
}
