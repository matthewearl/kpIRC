package kpl.gui;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class FontLabel
{
  private Label label;
  private FontData[] fontData;
  
  public FontLabel(Composite parent, int style)
  {
    label = new Label(parent, style);
    
    Font initialFont = label.getFont();
    fontData = initialFont.getFontData();
  }

  public void setText(String string)
  {
    label.setText(string);
  }

  private void updateFont()
  {
    Font newFont = new Font(label.getDisplay(), fontData);
    label.setFont(newFont);
  }
  
  public void setHeight(int size)
  {
    for (int i = 0; i < fontData.length; i++) {
      fontData[i].setHeight(size);
    }
    updateFont();
  }
  
  public void setStyle(int style)
  {
    for (int i = 0; i < fontData.length; i++) {
      fontData[i].setStyle(style);
    }
    updateFont();
  }

  public void setLayoutData(Object layoutData)
  {
    label.setLayoutData(layoutData);
  }
}
