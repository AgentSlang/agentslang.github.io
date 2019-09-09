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

package org.agent.slang.dm.narrative.data.gui;

import org.ib.data.GenericData;
import org.ib.data.TypeIdentification;

/**
 * A data type stores MousePositionData type characteristics.
 * OS Compatibility: Windows and Linux
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 7/15/15
 */

@TypeIdentification(typeID = 22)
public class MousePositionData implements GenericData {
    private int x, y;
    private long id;

    public MousePositionData() {
    }

    public MousePositionData(long id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * converts mouse data position into text
     * @return string form of mouse data position
     */
    @Override
    public String toString() {
        return "MousePositionData{" +
                "id=" + id +
                ", y=" + y +
                ", x=" + x +
                '}';
    }
}
