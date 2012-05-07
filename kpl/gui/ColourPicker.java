package kpl.gui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import kpl.settings.SettingConversionException;
import kpl.settings.SettingValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class ColourPicker extends Composite
{
  private Set colourPickerListeners;
  private Button button;
  private Text text;
  
  private void update(RGB rgb)
  {
    SettingValue val = new SettingValue(rgb); 
    text.setText(val.asString());
    button.setBackground(new Color(button.getShell().getDisplay(), rgb));
  }
  
  public ColourPicker (final Composite parent, RGB colour)
  {
    super(parent, 0);

    colourPickerListeners = new HashSet();
    
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    setLayout(gridLayout);
    
    text = new Text(this, 0);
    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    
    button = new Button(this, 0);
    GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, true);
    gridData.widthHint = 40;
    button.setLayoutData(gridData);
    update(colour);
    final ColorDialog colorDialog = new ColorDialog(parent.getShell());
    colorDialog.setRGB(colour);
    
    button.addSelectionListener(new SelectionAdapter()
    {
      public void widgetSelected(SelectionEvent e)
      {
        RGB colour = colorDialog.open();
       
        text.setFocus();
        if (colour != null)
        {
          update(colour);
          notifyListeners(colour);
        }
      }
    });
    
    text.addModifyListener(new ModifyListener()
    {
      public void modifyText(ModifyEvent e)
      {
        try
        {
          RGB colour = new SettingValue(text.getText()).asRGB();
          if (!colorDialog.getRGB().equals(colour))
          {
            colorDialog.setRGB(colour);
            update(colour);
            notifyListeners(colour);
          }
        } catch (SettingConversionException ex)
        {
        }
      }
    });
    
  }

  public void addListener(ColourPickerListener l)
  {
    colourPickerListeners.add(l);
  }
  
  private void notifyListeners(RGB colour)
  {
    Iterator it = colourPickerListeners.iterator();
    
    while (it.hasNext())
    {
      ColourPickerListener l = (ColourPickerListener)it.next();
      l.colourSelected(colour);
    }
  }
}
