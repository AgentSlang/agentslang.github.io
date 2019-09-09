/*
 * Copyright (c) Ovidiu Serban, ovidiu@roboslang.org
 *               web:http://ovidiu.roboslang.org/
 * All Rights Reserved. Use is subject to license terms.
 *
 * This file is part of AgentSlang Project (http://agent.roboslang.org/).
 *
 * AgentSlang is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License and CECILL-B.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * The CECILL-B license file should be a part of this project. If not,
 * it could be obtained at  <http://www.cecill.info/>.
 *
 * The usage of this project makes mandatory the authors citation in
 * any scientific publication or technical reports. For websites or
 * research projects the AgentSlang website and logo needs to be linked
 * in a visible area.
 */

package org.agent.slang.data.annotation;

import org.ib.data.IdentifiableData;
import org.ib.data.LanguageDependentData;
import org.ib.data.LanguageUtils;
import org.ib.data.TypeIdentification;
import org.syn.n.bad.annotation.Annotation;
import org.syn.n.bad.annotation.AnnotationToken;
import org.syn.n.bad.annotation.TextAnnotation;
import org.syn.n.bad.annotation.TextToken;

import java.util.Locale;

/**
 * It is a generic class for storing text annotation data type.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/24/13
 */

@TypeIdentification(typeID = 4, primitiveRegisters = {TextToken.class, AnnotationToken.class, Annotation.class})
public class GenericTextAnnotation extends TextAnnotation implements IdentifiableData, LanguageDependentData {
    private long id;
    private int language;

    public GenericTextAnnotation() {
    }

    public GenericTextAnnotation(long id) {
        this.id = id;
    }

    public GenericTextAnnotation(long id, int textLength) {
        super(textLength);
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Getting the Language of input text. 
     */
    public int getLanguage() {
        return language;
    }

    /**
     * Setting the language of input text.
     */
    public void setLanguage(int language) {
        this.language = language;
    }

    /**
     * Getting the local language of input text. 
     */
    public Locale getLocale() {
        return LanguageUtils.getLocaleByCode(language);
    }
}
