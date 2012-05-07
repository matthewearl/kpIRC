package kpl.settings.gui;

import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import kpl.settings.model.*;
import kpl.settings.model.SettingsEditor.Page;
import kpl.settings.Settings;

public class SettingsWindow
{
  private Shell shell;
  private Shell parent;
  private SettingsEditor settingsEditor;
  private Map itemsToPages;
  
  /**
   * Associate page with item and add children to a tree item representing a Page.
   */
  private void completeTreeItem(TreeItem treeItem, final Page page)
  {
    treeItem.setText(page.getTitle());
    itemsToPages.put(treeItem, page);
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
    
    shell = new Shell(parent);
    shell.setLayout(new FillLayout());
    Tree tree = new Tree(shell, 0);
    
    Iterator it = settingsEditor.getPages().iterator();
    while (it.hasNext())
    {
      final Page page = (Page)it.next();
      TreeItem treeItem = new TreeItem(tree, 0);
      completeTreeItem(treeItem, page);
    }
    
    tree.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e)
      {
        TreeItem item = (TreeItem)e.item;
        Page page = (Page)itemsToPages.get(item);
        System.out.println("" + page.getTitle() + " selected");
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
