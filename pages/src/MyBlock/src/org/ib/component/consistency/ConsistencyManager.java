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

package org.ib.component.consistency;

import org.ib.data.GenericData;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/8/12
 */
public class ConsistencyManager {
    public static final String ANY = "*";

    private final Map<String, List<ConsistencyChecker>> checkers = new HashMap<String, List<ConsistencyChecker>>();

    public ConsistencyManager() {
    }

    public void addChecker(String group, ConsistencyChecker checker) {
        synchronized (checkers) {
            List<ConsistencyChecker> items = checkers.get(group);
            if (items == null) {
                items = new LinkedList<ConsistencyChecker>();
                checkers.put(group, items);
            }
            items.add(checker);
        }
    }

    public void removeChecker(String group, ConsistencyChecker checker) {
        synchronized (checkers) {
            List<ConsistencyChecker> items = checkers.get(group);
            if (items != null) {
                items.remove(checker);
                if (items.isEmpty()) {
                    checkers.remove(group);
                }
            }
        }
    }

    /*
     * !! Data is invalid by default
     */
    public boolean check(String group, GenericData data, boolean returnOnFirst) {
        synchronized (checkers) {
            if (!ANY.equals(group) && checkers.get(group) == null && checkers.get(ANY) == null) {
                return true;
            }

            boolean valid = false;

            List<ConsistencyChecker> items = checkers.get(group);
            if (items != null) {
                for (ConsistencyChecker checker : items) {
                    valid |= checker.check(data);
                    if (valid && returnOnFirst) {
                        return valid;
                    }
                }
            }

            if (ANY.equals(group)) {
                return valid;
            } else {
                return valid || check(ANY, data, returnOnFirst);
            }
        }
    }

    public Set<String> getDefinedGroups() {
        synchronized (checkers) {
            return Collections.unmodifiableSet(checkers.keySet());
        }
    }
}
