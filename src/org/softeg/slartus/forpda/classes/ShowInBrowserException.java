package org.softeg.slartus.forpda.classes;

import org.softeg.slartus.forpdaapi.NotReportException;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 15.10.12
 * Time: 9:22
 * To change this template use File | Settings | File Templates.
 */
public class ShowInBrowserException extends NotReportException {
    public String Url;

    public ShowInBrowserException(String message, String url) {
        super(message);
        Url = url;
    }
}
