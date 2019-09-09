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

package org.agent.slang.dm.narrative.data;

import org.ib.data.GenericData;
import org.ib.data.TypeIdentification;

/**
 * A data type stores StateChangeData type characteristics.
 * OS Compatibility: Windows and Linux
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 7/13/15
 */

@TypeIdentification(typeID = 9)
public class StateChangeData implements GenericData {
    private int newStateNumber;
    private long id;

    public StateChangeData() {
    }

    public StateChangeData(long id, int data) {
        this.id = id;
        this.newStateNumber = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNewStateNumber() {
        return newStateNumber;
    }

    public void setNewStateNumber(int newStateNumber) {
        this.newStateNumber = newStateNumber;
    }

    public String toString() {
        return "" + newStateNumber;
    }
}
