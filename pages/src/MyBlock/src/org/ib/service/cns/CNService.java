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

package org.ib.service.cns;

import org.ib.component.annotations.ConfigureParams;
import org.ib.component.model.ComponentConfig;
import org.ib.service.generic.AbstractService;
import org.ib.utils.SimpleXMLParser;
import org.ib.utils.XMLProperties;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/3/12
 */

@ConfigureParams(mandatoryConfigurationParams = "config")
public class CNService extends AbstractService<String, String> {
    public static final String P_MACHINE = "machine";
    public static final String P_CONFIG = "config";

    private final Map<String, String> machineNames = new HashMap<String, String>();

    /*
       Computer Name client commands:
       +<computer>@<ip>     - adds a new ip
       -<computer>          - removes a computer name
       ?<computer>          - returns the ip of a computer
    */

    public CNService(String port) {
        super(port);
    }

    protected String convertData(byte[] data) {
        return new String(data);
    }

    protected byte[] convertData(String data) {
        return data.getBytes();
    }

    protected String handleRequest(String data) {
        char command = data.charAt(0);
        switch (command) {
            case '+':
                data = data.substring(1);
                return addHost(data);
            case '-':
                data = data.substring(1);
                return removeHost(data);
            case '?':
                data = data.substring(1);
                return getIP(data);
        }
        return "";
    }

    private String addHost(String hostIP) {
        String[] pair = hostIP.trim().split("@");

        if (pair.length > 1) {
            machineNames.put(pair[0], pair[1]);
        }

        return "t";
    }

    private String removeHost(String machine) {
        machineNames.remove(machine);

        return "t";
    }

    private String getIP(String machine) {
        String ip = machineNames.get(machine);
        return ip == null ? "" : ip;
    }

    public void configure(XMLProperties properties) {
        File pathFileName = new File(properties.getProperty(ComponentConfig.PROPERTY_FILENAME)).getParentFile();
        URI url = pathFileName.toURI();
        XMLProperties machineProperties = SimpleXMLParser.parseDocument(new File(url.resolve(properties.getProperty(P_CONFIG))));
        for (String item : machineProperties.getPropertyList(P_MACHINE)) {
            addHost(item.trim());
        }
    }
}
