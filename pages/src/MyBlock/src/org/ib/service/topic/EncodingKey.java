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

package org.ib.service.topic;

import java.util.Arrays;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/2/12
 */
public class EncodingKey {
    protected byte[] key;

    public EncodingKey(byte[] key) {
        this.key = key;
    }

    public EncodingKey createNewEncodingKey() {
        byte[] encoding = new byte[key.length];
        System.arraycopy(key, 0, encoding, 0, key.length);

        EncodingKey result = new EncodingKey(encoding);

        boolean ok = false;
        int i = 0;
        while (!ok && i < key.length) {
            if (result.key[i] == 255) {
                result.key[i] = 0;
                i++;
            } else {
                result.key[i]++;
                ok = true;
            }
        }
        return result;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncodingKey that = (EncodingKey) o;

        return Arrays.equals(key, that.key);
    }

    public int hashCode() {
        return Arrays.hashCode(key);
    }
}