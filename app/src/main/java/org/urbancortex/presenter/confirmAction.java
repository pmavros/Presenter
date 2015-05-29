package org.urbancortex.presenter;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class confirmAction
{
    public confirmAction() {}

    public void confirm(Context paramContext, String paramString1, String paramString2)
    {
        new AlertDialog.Builder(paramContext).setTitle(paramString1).setMessage(paramString2).setIcon(0).setPositiveButton(17039379, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {}
        }).setNegativeButton(17039369, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {}
        }).show();
    }
}
