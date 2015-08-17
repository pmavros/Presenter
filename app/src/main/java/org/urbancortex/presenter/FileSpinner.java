package org.urbancortex.presenter;

import java.io.File;

/**
 * Created by Panos on 17/08/15.
 */
public class FileSpinner{
    protected String fileName;
    protected File fileAddress;

    public FileSpinner(String name, File file)
    {
        fileName = name;
        fileAddress = file;
    }
}
