package org.urbancortex.presenter;

class Event
{
    protected String[] Buttons;
    protected String Code;
    protected String Image;
    protected String Text;
    protected String Title;
    protected Boolean Alert;
    protected String Condition;


    public Event(String condition, String code, String title, String text, String[] buttons, String img, Boolean alert)
    {
        Code = code;
        Title = title;
        Text = text;
        Buttons = buttons;
        Image = img;
        Alert = alert;
        Condition = condition;
    }
}

