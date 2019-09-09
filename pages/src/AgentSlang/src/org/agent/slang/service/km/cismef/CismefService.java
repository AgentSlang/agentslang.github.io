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

package org.agent.slang.service.km.cismef;

import org.agent.slang.service.km.cismef.data.Concept;
import org.ib.service.generic.AbstractService;
import org.ib.utils.XMLProperties;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/23/13
 */
public class CismefService extends AbstractService<String, String> {
    private static final String PROPERTY_CACHE = "cache";
    private static final String VALUE_CACHE = "cache-cismef";

    private ScrapperUtility scrapperUtility;

    public CismefService(String port) {
        super(port);
    }

    protected String convertData(byte[] data) {
        return new String(data);
    }

    protected byte[] convertData(String data) {
        return data.getBytes();
    }

    /*
        CISMeF Knowledge Management Service
       ?(concept) - retrieve all the information about the concept
     */

    protected String handleRequest(String data) {
        char command = data.charAt(0);
        switch (command) {
            case '?':
                data = data.substring(1);
                if (scrapperUtility != null) {
                    try {
                        return asString(scrapperUtility.scrapConcept(data));
                    } catch (IOException e) {
                        System.err.println(e.toString());
                        return "";
                    }
                }
        }
        return "";
    }

    public void configure(XMLProperties properties) {
        super.configure(properties);

        try {
            scrapperUtility = new ScrapperUtility(new File(properties.getProperty(PROPERTY_CACHE, VALUE_CACHE)));
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private String asString(List<Concept> concepts) {
        StringBuilder sb = new StringBuilder();

        for (Concept concept : concepts) {
            sb.append(concept.toString()).append("\n");
        }

        return sb.toString();
    }
}
