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

package org.agent.slang.in.proxy.socket;

import org.ib.component.base.Publisher;
import org.ib.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/24/13
 */
public abstract class DataSocket extends Thread {
    public interface DataListener {
        public void dataReceived(String data);
    }

    private final Collection<DataListener> listeners = new LinkedList<DataListener>();

    public void addDataListener(DataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeDataListener(DataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void fireDataReceived(String data) {
        synchronized (listeners) {
            for (DataListener listener : listeners) {
                listener.dataReceived(data);
            }
        }
    }

    protected BufferedReader reader = null;
    protected Publisher source;

    protected DataSocket(Publisher source) {
        this.source = source;
    }

    public void run() {
        if (reader != null) {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    fireDataReceived(line);
                    Logger.log(source, Logger.INFORM, "Line received:" + line);
                }
                reader.close();
            } catch (IOException e) {
                Logger.log(source, Logger.CRITICAL, "Data Input error", e);
            }
            close();
        }
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            //-- ignore
        }
    }
}
