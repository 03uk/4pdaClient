package org.softeg.slartus.forpdaapi;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slinkin
 * Date: 06.02.13
 * Time: 9:59
 * To change this template use File | Settings | File Templates.
 */
public class QmsUserThemes extends ArrayList<QmsUserTheme> {
    public String Nick;

    public int getSelectedCount() {
        int count = 0;
        for (QmsUserTheme theme : this) {
            if (theme.isSelected())
                count++;
        }
        return count;
    }
}
