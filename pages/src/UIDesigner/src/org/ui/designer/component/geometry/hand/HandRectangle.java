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

package org.ui.designer.component.geometry.hand;

import org.ui.designer.component.geometry.basic.Point3D;

import java.awt.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 4/3/14
 */
public class HandRectangle extends HandShape {
    private Point3D a, b;
    private ShakyLine l1, l2, l3, l4;

    public HandRectangle(Point3D a, Point3D b) {
        this.a = a;
        this.b = b;
        calculateLines();
    }

    private void calculateLines() {
        l1 = new ShakyLine(a, new Point3D(b.getX(), a.getY()));
        l2 = new ShakyLine(new Point3D(b.getX(), a.getY()), b);
        l3 = new ShakyLine(b, new Point3D(a.getX(), b.getY()));
        l4 = new ShakyLine(new Point3D(a.getX(), b.getY()), a);
    }

    public void drawShape(Graphics2D graphics) {
        l1.drawShape(graphics);
        l2.drawShape(graphics);
        l3.drawShape(graphics);
        l4.drawShape(graphics);
    }
}
