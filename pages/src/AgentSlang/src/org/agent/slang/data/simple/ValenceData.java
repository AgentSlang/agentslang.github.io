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

package org.agent.slang.data.simple;

import org.ib.data.GenericData;
import org.ib.data.TypeIdentification;

/**
 * A data type to store Valence data characteristics.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/21/12
 */
@TypeIdentification(typeID = 7)
public class ValenceData implements GenericData {
    private long id;
    private double positive;
    private double negative;

    public ValenceData() {
    }

    public ValenceData(long id, double positive, double negative) {
        this.id = id;
        this.positive = positive;
        this.negative = negative;
    }

    public long getId() {
        return id;
    }

    public double getPositive() {
        return positive;
    }

    public double getNegative() {
        return negative;
    }

    public double getNeutral() {
        return 1 - positive - negative;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setPositive(double positive) {
        this.positive = positive;
    }

    public void setNegative(double negative) {
        this.negative = negative;
    }
}
