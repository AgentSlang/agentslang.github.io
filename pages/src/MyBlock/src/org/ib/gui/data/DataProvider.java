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

package org.ib.gui.data;

import java.util.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/15/13
 */
public class DataProvider {
    public interface DataListener {
        public void dataUpdated(DataProvider provider);
    }

    private final List<DataListener> listeners = new LinkedList<DataListener>();
    private Collection<String> data;

    private String id;

    public DataProvider(String id, boolean uniqueValue) {
        this.id = id;
        if (uniqueValue) {
            data = new HashSet<String>();
        } else {
            data = new LinkedList<String>();
        }
    }

    public String getId() {
        return id;
    }

    public synchronized void addItems(Collection<String> items) {
        for (String item : items) {
            data.add(item);
        }

        fireDataUpdated();
    }

    public synchronized void addItem(String item) {
        data.add(item);

        fireDataUpdated();
    }

    public synchronized void removeItem(String item) {
        data.remove(item);
        fireDataUpdated();
    }

    public synchronized Collection<String> getItems() {
        return Collections.unmodifiableCollection(data);
    }

    public synchronized void addDataListener(DataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public synchronized void removeDataListener(DataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private synchronized void fireDataUpdated() {
        synchronized (listeners) {
            for (DataListener listener : listeners) {
                listener.dataUpdated(this);
            }
        }
    }

    public static DataProvider createStaticDataProvider(String id, Collection<String> values) {
        DataProvider provider = new DataProvider(id, true);
        provider.addItems(values);
        return provider;
    }
}
