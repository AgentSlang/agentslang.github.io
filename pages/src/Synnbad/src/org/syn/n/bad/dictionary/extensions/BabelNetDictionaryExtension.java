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

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetConfiguration;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.jlt.util.Language;
import org.syn.n.bad.dictionary.DictionaryException;
import org.syn.n.bad.dictionary.POS;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/24/13
 */
public class BabelNetDictionaryExtension extends AbstractDictionaryExtension {
    private static final Map<POS, edu.mit.jwi.item.POS> wnPOSMapping = new HashMap<POS, edu.mit.jwi.item.POS>();
    private static final Map<String, Language> localeLanguageMap = new HashMap<String, Language>();

    static {
        wnPOSMapping.put(POS.ADJECTIVE, edu.mit.jwi.item.POS.ADJECTIVE);
        wnPOSMapping.put(POS.ADVERB, edu.mit.jwi.item.POS.ADVERB);
        wnPOSMapping.put(POS.NOUN, edu.mit.jwi.item.POS.NOUN);
        wnPOSMapping.put(POS.VERB, edu.mit.jwi.item.POS.VERB);
        //--
        localeLanguageMap.put("en", Language.EN);
        localeLanguageMap.put("fr", Language.FR);
        localeLanguageMap.put("it", Language.IT);
        localeLanguageMap.put("es", Language.ES);
        localeLanguageMap.put("ro", Language.RO);
    }

    private BabelNet wnDictionary;

    public BabelNetDictionaryExtension(String dictionaryPrefix, File configFilePath) throws DictionaryException {
        super(dictionaryPrefix, configFilePath);
        BabelNetConfiguration.getInstance().setConfigurationFile(configFilePath);
        wnDictionary = BabelNet.getInstance();
    }

    private Language getLanguage(Locale locale) {
        if (localeLanguageMap.containsKey(locale.getLanguage())) {
            return localeLanguageMap.get(locale.getLanguage());
        } else {
            return Language.EN;
        }
    }

    public Set<String> getSynsetIDs(Locale language, String word, List<POS> restrictions) throws DictionaryException {
        Set<String> result = new HashSet<String>();
        if (wnDictionary != null) {
            for (POS posItem : restrictions) {
                List<BabelSynset> synsets = null;
                try {
                    synsets = wnDictionary.getSynsets(getLanguage(language), word, wnPOSMapping.get(posItem));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (synsets != null) {
                    for (BabelSynset synsetOffset : synsets) {
                        result.add(generateID(posItem, synsetOffset.getId()));
                    }
                }
            }
        }

        return result;
    }
}
