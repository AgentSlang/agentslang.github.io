package org.ib.data;

import java.util.Locale;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/5/13
 */
public interface LanguageDependentData extends GenericData {
    public int getLanguage();

    public void setLanguage(int language);

    public Locale getLocale();
}
