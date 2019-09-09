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
package org.ib.gui.util.springbox;

/**
 * Edge representation.
 */
public class EdgeSpring {
    /**
     * The edge identifier.
     */
    public String id;

    /**
     * Source node.
     */
    public NodeParticle node0;

    /**
     * Target node.
     */
    public NodeParticle node1;

    /**
     * Edge weight.
     */
    public double weight = 1f;

    /**
     * Make this edge ignored by the layout algorithm ?.
     */
    public boolean ignored = false;

    /**
     * New edge between two given nodes.
     *
     * @param id The edge identifier.
     * @param n0 The first node.
     * @param n1 The second node.
     */
    public EdgeSpring(String id, NodeParticle n0, NodeParticle n1) {
        this.id = id;
        this.node0 = n0;
        this.node1 = n1;
    }

    /**
     * Considering the two nodes of the edge, return the one that was not given
     * as argument.
     *
     * @param node One of the nodes of the edge.
     * @return The other node.
     */
    public NodeParticle getOpposite(NodeParticle node) {
        if (node0 == node)
            return node1;

        return node0;
    }
}