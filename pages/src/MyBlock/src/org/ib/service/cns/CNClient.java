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

import org.ib.service.generic.AbstractClient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/3/12
 */
public class CNClient extends AbstractClient<String, String> {
    private static final String IPV4_REGEX = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
    /*
       Computer Name client commands:
       +<computer>@<ip>     - adds a new ip
       -<computer>          - removes a computer name
       ?<computer>          - returns the ip of a computer
    */

    private final Map<String, String> cache = new HashMap<String, String>();

    public CNClient(String host, String port) {
        super(host, port);
    }

    protected String convertData(byte[] data) {
        return new String(data);
    }

    protected byte[] convertData(String data) {
        return data.getBytes();
    }

    protected String resolveRemoteHost(String host) {
        if (isIP(host)) {
            return host;
        } else {
            throw new IllegalArgumentException("The host for the Computer Name Resolver needs to be an ip address ...");
        }
    }

    public void addComputerName(String computer, String ip) {
        request("+" + computer + "@" + ip);
    }

    public void removeComputerName(String computer) {
        request("-" + computer);
    }

    public String getIP(String computerName, boolean checkForIP) {
        if (checkForIP && isIP(computerName)) {
            return computerName;
        } else {
            String ip = fromCache(computerName);
            if (ip == null) {
                return cache(computerName, request("?" + computerName));
            } else {
                return ip;
            }
        }
    }

    public String resolveHost(String host) {
        String port = "";
        if (host.contains(":")) {
            String[] items = host.split(":");
            host = items[0];
            port = ":" + items[1];
        }

        return getIP(host, true) + port;
    }

    private boolean isIP(String ip) {
        return Pattern.compile(IPV4_REGEX).matcher(ip).matches();
    }

    private String cache(String item, String value) {
        cache.put(item, value);
        return value;
    }

    private String fromCache(String item) {
        return cache.get(item);
    }
}
