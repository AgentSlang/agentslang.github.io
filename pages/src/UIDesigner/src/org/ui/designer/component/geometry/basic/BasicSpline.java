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

package org.ui.designer.component.geometry.basic;

import java.util.Collection;
import java.util.Vector;

public abstract class BasicSpline {
    private Vector<Point3D> points = new Vector<Point3D>();

    protected void calcNaturalCubic(Point3D.Axis axis, Collection<Cubic> cubicCollection) {
        int num = points.size() - 1;

        float[] gamma = new float[num + 1];
        float[] delta = new float[num + 1];
        float[] D = new float[num + 1];

        int i;
      /*
           We solve the equation
          [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
          |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
          |  1 4 1   | | .  | = |      .         |
          |    ..... | | .  |   |      .         |
          |     1 4 1| | .  |   |3(x[n] - x[n-2])|
          [       1 2] [D[n]]   [3(x[n] - x[n-1])]

          by using row operations to convert the matrix to upper triangular
          and then back sustitution.  The D[i] are the derivatives at the knots.
      */
        gamma[0] = 1.0f / 2.0f;
        for (i = 1; i < num; i++) {
            gamma[i] = 1.0f / (4.0f - gamma[i - 1]);
        }
        gamma[num] = 1.0f / (2.0f - gamma[num - 1]);

        float p0 = points.get(0).getValue(axis);
        float p1 = points.get(1).getValue(axis);

        delta[0] = 3.0f * (p1 - p0) * gamma[0];
        for (i = 1; i < num; i++) {
            p0 = points.get(i - 1).getValue(axis);
            p1 = points.get(i + 1).getValue(axis);
            delta[i] = (3.0f * (p1 - p0) - delta[i - 1]) * gamma[i];
        }
        p0 = points.get(num - 1).getValue(axis);
        p1 = points.get(num).getValue(axis);

        delta[num] = (3.0f * (p1 - p0) - delta[num - 1]) * gamma[num];

        D[num] = delta[num];
        for (i = num - 1; i >= 0; i--) {
            D[i] = delta[i] - gamma[i] * D[i + 1];
        }

      /*
           now compute the coefficients of the cubics
      */
        cubicCollection.clear();

        for (i = 0; i < num; i++) {
            p0 = points.get(i).getValue(axis);
            p1 = points.get(i + 1).getValue(axis);

            cubicCollection.add(new Cubic(
                    p0,
                    D[i],
                    3 * (p1 - p0) - 2 * D[i] - D[i + 1],
                    2 * (p0 - p1) + D[i] + D[i + 1]
            )
            );
        }
    }

    public void addPoint(Point3D point) {
        this.points.add(point);
    }

    public Vector<Point3D> getPoints() {
        return points;
    }

    public abstract void calcSpline();

    public abstract Point3D getPoint(float position);
}
