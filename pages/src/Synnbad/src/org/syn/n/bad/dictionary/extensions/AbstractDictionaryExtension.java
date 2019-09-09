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

package org.syn.n.bad.dictionary.extensions;

import org.syn.n.bad.dictionary.DictionaryException;
import org.syn.n.bad.dictionary.POS;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/24/13
 */
public abstract class AbstractDictionaryExtension {
    private String dictionaryPrefix;

    public AbstractDictionaryExtension(String dictionaryPrefix, File configFilePath) throws DictionaryException {
        this.dictionaryPrefix = dictionaryPrefix;
    }

    protected String generateID(POS pos, long offset) {
        if (pos == null) {
            return dictionaryPrefix + "#" + offset;
        } else {
            return dictionaryPrefix + "~" + pos.getPosLabel() + "#" + offset;
        }
    }

    protected String generateID(POS pos, String synsetID) {
        if (pos == null) {
            return dictionaryPrefix + "#" + synsetID;
        } else {
            return dictionaryPrefix + "~" + pos.getPosLabel() + "#" + synsetID;
        }
    }

    public abstract Set<String> getSynsetIDs(Locale language, String word, List<POS> restrictions) throws DictionaryException;
}
