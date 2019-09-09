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

package org.ib.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/1/12
 */
public class StringUtils {
    public static Collection<String> split(String item, String separator) {
        String[] items = item.split(separator);
        Collection<String> result = new LinkedList<String>();
        Collections.addAll(result, items);
        return result;
    }

    public static String join(Collection<String> items, String separator) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : items) {
            if (!first) {
                sb.append(separator);
            }
            sb.append(item);
            first = false;
        }

        return sb.toString();
    }

    public static String join(String[] items, String separator) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String item : items) {
            if (!first) {
                sb.append(separator);
            }
            sb.append(item);
            first = false;
        }

        return sb.toString();
    }

    public static byte[] byteWrap(String item, int length) {
        byte[] result = new byte[length];

        if (item.length() > 0) {
            for (int i = 0; i < length; i++) {
                result[i] = (byte) item.charAt(i);
            }
        }

        return result;
    }

    public static String byteUnwrap(byte[] items) {
        StringBuilder sb = new StringBuilder();
        for (byte item : items) {
            sb.append((char) item);
        }

        return sb.toString();
    }

}
