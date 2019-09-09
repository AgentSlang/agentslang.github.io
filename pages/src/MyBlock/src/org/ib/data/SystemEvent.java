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

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/19/13
 */

@TypeIdentification(typeID = 12)
public class SystemEvent implements GenericData {
    public static final int SYSTEM_WAKE = 1;
    public static final int SYSTEM_SHUTDOWN = 2;

    private long id;
    private int event;
    private String sourceName;

    public SystemEvent() {
    }

    public SystemEvent(long id, int event) {
        this(id, event, null);
    }

    public SystemEvent(long id, int event, String sourceName) {
        this.id = id;
        this.event = event;
        this.sourceName = sourceName;
    }

    public long getId() {
        return id;
    }

    public int getEvent() {
        return event;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}
