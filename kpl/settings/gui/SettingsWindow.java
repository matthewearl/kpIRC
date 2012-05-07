package kpl.settings.gui;

import java.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import kpl.gui.ColourPicker;
import kpl.gui.ColourPickerListener;
import kpl.gui.FontLabel;
import kpl.settings.model.*;
import kpl.settings.model.SettingsEditor.*;
import kpl.settings.SettingValue;
import kpl.settings.Settings;

public class SettingsWindow
{
  private Shell shell;
  private SettingsEditor settingsEditor;
  private Map itemsToPages;
  private Map itemsToPageControls;
  private Composite pageContainer;
  private StackLayout pageContainerLayout;
  private Set settingInputs;
  
  private abstract class SettingInput
  {
    private Input input;
    private SettingValue savedValue;
    private SettingValue currentValue;
    
    public SettingInput(Composite parent, Input input)
    {
      this.input = input;
      settingInputs.add(this);
      
      // Label on the left.
      FontLabel label = new FontLabel(parent, 0);
      label.setStyle(SWT.BOLD);
      label.setText(input.getDescription() + ":");
      GridData gridData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
      gridData.verticalSpan = 2;
      gridData.horizontalSpan = 1;
      label.setLayoutData(gridData);
      
      // Input control.
      savedValue = Settings.get(input.getSetting());
      currentValue = savedValue;
      Control innerInput = makeInnerInputControl(parent, savedValue);
      gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
      gridData.verticalSpan = 1;
      gridData.horizontalSpan = 1;
      innerInput.setLayoutData(gridData);
      
      // Help text.
      FontLabel helpLabel = new FontLabel(parent, 0);
      helpLabel.setStyle(SWT.ITALIC);
      helpLabel.setText(input.getHelp());
      gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
      gridData.verticalSpan = 1;
      gridData.horizontalSpan = 1;
      helpLabel.setLayoutData(gridData);
    }
    
    abstract protected Control makeInnerInputControl(Composite parent, SettingValue val);
    
    final protected void onSettingChange(SettingValue val)
    {
      currentValue = val;
    }
    
    private void apply()
    {
      if (!currentValue.equals(savedValue))
      {
        Settings.put(input.getSetting(), currentValue);
        savedValue = currentValue;
      }
    }
  }

  private class TextSettingInput extends SettingInput
  {
    public TextSettingInput(Composite parent, Input input)
    {
      super(parent, input);
    }

    protected Control makeInnerInputControl(Composite parent, SettingValue val)
    {
      final Text text = new Text(parent, SWT.SINGLE);
      text.setText(val.asString());
      text.addModifyListener(new ModifyListener()
      {
        public void modifyText(ModifyEvent e)
        {
          onSettingChange(new SettingValue(text.getText()));
        }
      });
      
      return text;
    }
  }

  private class ColourSettingInput extends SettingInput
  {
    public ColourSettingInput(Composite parent, Input input)
    {
      super(parent, input);
    }
    
    protected Control makeInnerInputControl(Composite parent, SettingValue val)
    {
      ColourPicker colourPicker = new ColourPicker(parent, val.asRGB());
      colourPicker.addListener(new ColourPickerListener() 
      {
        public void colourSelected(RGB colour)
        {
          onSettingChange(new SettingValue(colour));
        }
      });
      return colourPicker;
    }
  }
  
  private class PageControl extends Composite
  {
    PageControl(Page page)
    {
      super(pageContainer, 0);
      
      // Grid layout
      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      setLayout(gridLayout);
      
      // Title
      FontLabel title = new FontLabel(this, 0);
      title.setHeight(24);
      title.setStyle(SWT.ITALIC);
      title.setText(page.getTitle());
      GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
      gridData.horizontalSpan = gridLayout.numColumns;
      gridData.verticalSpan = 1;
      title.setLayoutData(gridData);
            
      // Inputs
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
      
      // Space filler.
      Control spaceFiller = new Label(this, 0);
      gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
      gridData.horizontalSpan = gridLayout.numColumns;
      spaceFiller.setLayoutData(gridData);
      
      this.pack();
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
  
  private void applyAllChanges()
  {
    Iterator it = settingInputs.iterator();
    
    while (it.hasNext())
    {
      SettingInput settingInput = (SettingInput)it.next();
      settingInput.apply();
    }
  }

  private Control makeButtons (Composite parent)
  {
    Composite buttonContainer = new Composite(shell, 0);
    GridLayout buttonGridLayout = new GridLayout();
    buttonGridLayout.numColumns = 3;
    buttonContainer.setLayout(buttonGridLayout);
    
    Button cancelButton = new Button(buttonContainer, 0);
    Button applyButton = new Button(buttonContainer, 0);
    Button okButton = new Button(buttonContainer, 0);
    
    cancelButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
    applyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
    okButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
    
    cancelButton.setText("Cancel");
    applyButton.setText("Apply");
    okButton.setText("Ok");
    
    cancelButton.addListener(SWT.Selection, new Listener()
    {
      public void handleEvent(Event event)
      {
        shell.close();
      }
    });
    
    applyButton.addListener(SWT.Selection, new Listener()
    {
      public void handleEvent(Event event)
      {
        applyAllChanges();
      }
    });
    
    okButton.addListener(SWT.Selection, new Listener()
    {
      public void handleEvent(Event event)
      {
        applyAllChanges();
        shell.close();
      }
    });
    
    return buttonContainer;
  }
  
  public SettingsWindow(Shell parent, SettingsEditor settingsEditor)
  {
    this.settingsEditor = settingsEditor;
    
    itemsToPages = new HashMap();
    itemsToPageControls = new HashMap();
    settingInputs = new HashSet();
    
    // Create shell
    shell = new Shell(parent);
    shell.setText("Settings");
    
    // Grid layout for the whole shell. Elements are a tree and page container.
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    shell.setLayout(gridLayout);
    
    // Tree
    Tree tree = new Tree(shell, SWT.BORDER);
    GridData gridData = new GridData(SWT.LEFT, SWT.FILL, false, true);
    gridData.horizontalSpan = 1;
    gridData.verticalSpan = 1;
    tree.setLayoutData(gridData);
    
    // Page container (a Composite for holding the pages in a StackLayout).
    pageContainer = new Composite(shell, 0);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
    gridData.horizontalSpan = 1;
    gridData.verticalSpan = 1;
    pageContainer.setLayoutData(gridData);
    pageContainerLayout = new StackLayout();
    pageContainer.setLayout(pageContainerLayout);
    
    // Cancel, Apply, Ok buttons
    Control buttons = makeButtons(shell);
    gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
    gridData.horizontalSpan = gridLayout.numColumns;
    buttons.setLayoutData(gridData);

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
    
    shell.pack();
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
