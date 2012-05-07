package kpl.settings.model;

public class SettingsEditorParseException extends Exception
{
  private static final long serialVersionUID = 8319280022328674225L;
  
  public SettingsEditorParseException(Exception e)
  {
    super(e);
  }
  
  public SettingsEditorParseException(String s)
  {
    super(s);
  }
}
