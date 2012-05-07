package ircclient.model;

import kpl.settings.model.SettingsEditor;

public class IrcSettingsEditor extends SettingsEditor
{
  public IrcSettingsEditor ()
  {
    Page generalPage = addPage("General");
    Page ircPage = addPage("IRC", generalPage);
    Page coloursPage = addPage("Colours", generalPage);

    addInput(ircPage, INPUT_TYPE_TEXT, "Nick", "Irc.nick", "Name used to uniquely identify the client.");
    addInput(ircPage, INPUT_TYPE_TEXT, "User", "Irc.user", "Username of the client on the host.");
    addInput(ircPage, INPUT_TYPE_TEXT, "Real name", "Irc.name", "Real name of the client.");
    addInput(coloursPage, INPUT_TYPE_COLOUR, "Foreground colour", "Colours.defaultFG", "Default foreground colour for text.");
    addInput(coloursPage, INPUT_TYPE_COLOUR, "Background colour", "Colours.defaultBG", "Default background colour for text.");
    addInput(coloursPage, INPUT_TYPE_COLOUR, "Alert colour", "Colours.alertColour", "Colour of alert messages.");
    addInput(coloursPage, INPUT_TYPE_COLOUR, "Join colour", "Colours.joinColour", "Colour of join messages.");
    addInput(coloursPage, INPUT_TYPE_COLOUR, "Leave colour", "Colours.leaveColour", "Colour of leave messages.");
    addInput(coloursPage, INPUT_TYPE_COLOUR, "Info colour", "Colours.infoColour", "Colour of informational messages.");
  }

  private Page addPage(String string)
  {
    return addPage(string, null);
  }
}
