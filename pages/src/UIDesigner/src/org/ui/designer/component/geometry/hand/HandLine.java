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

import org.ui.designer.component.geometry.basic.BasicSpline;
import org.ui.designer.component.geometry.basic.Point3D;
import org.ui.designer.component.geometry.basic.Spline2D;

import java.awt.*;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 4/3/14
 */
public class HandLine extends HandShape {
    private static final int MAX_ERROR = 3;
    private static final int resolution = 100;
    private Point3D a, b, m1, m2, m3;
    private BasicSpline spline;

    public HandLine(Point3D a, Point3D b) {
        this.a = a;
        this.b = b;
        calculateMedianValues(true);
        calculateSpline();
    }

    private void calculateMedianValues(boolean randomize) {
        float xt = (b.getX() - a.getX()) / 4;
        float x1 = a.getX() + xt;
        float x2 = a.getX() + 2 * xt;
        float x3 = a.getX() + 3 * xt;

        float y1 = (3 * a.getY() + b.getY()) / 4;
        float y2 = (a.getY() + b.getY()) / 2;
        float y3 = (a.getY() + 3 * b.getY()) / 4;

        if (randomize) {
            a.setX(a.getX() + getRandomError(MAX_ERROR));
            a.setY(a.getY() + getRandomError(MAX_ERROR));

            b.setX(b.getX() + getRandomError(MAX_ERROR));
            b.setY(b.getY() + getRandomError(MAX_ERROR));

            x1 += getRandomError(MAX_ERROR);
            y1 += getRandomError(MAX_ERROR);

            x2 += getRandomError(MAX_ERROR);
            y2 += getRandomError(MAX_ERROR);

            x3 += getRandomError(MAX_ERROR);
            y3 += getRandomError(MAX_ERROR);
        }

        m1 = new Point3D(x1, y1);
        m2 = new Point3D(x2, y2);
        m3 = new Point3D(x3, y3);
    }

    private void calculateSpline() {
        spline = new Spline2D();
        spline.addPoint(a);
        spline.addPoint(m1);
        spline.addPoint(m2);
        spline.addPoint(m3);
        spline.addPoint(b);
        spline.calcSpline();
    }

    public void drawShape(Graphics2D graphics) {
//        graphics.drawLine((int) a.getX(), (int) a.getY(), (int) m1.getX(), (int) m1.getY());
//        graphics.drawLine((int) m1.getX(), (int) m1.getY(), (int) m2.getX(), (int) m2.getY());
//        graphics.drawLine((int) m2.getX(), (int) m2.getY(), (int) m3.getX(), (int) m3.getY());
//        graphics.drawLine((int) m3.getX(), (int) m3.getY(), (int) b.getX(), (int) b.getY());

        Point3D lastPoint = null;
        for (int i = 0; i < resolution; i++) {
            float iterator = i;
            iterator /= resolution;
            Point3D point = spline.getPoint(iterator);
            if (lastPoint == null) {
                lastPoint = point;
            } else if (!lastPoint.equals(point)) {
                graphics.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(), (int) point.getX(), (int) point.getY());
                lastPoint = point;
            }
        }
    }
}
