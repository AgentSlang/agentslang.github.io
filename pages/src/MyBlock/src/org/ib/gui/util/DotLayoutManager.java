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

package org.ib.gui.util;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/5/12
 */
public class DotLayoutManager {
    private static final String DOT_PATH = "dot";

    private static Boolean canProcess = null;

    public static boolean canProcess() {
        if (canProcess == null) {
            canProcess = canProcessDot();
        }
        return canProcess;
    }

    private static boolean canProcessDot() {
        try {
            Process process = Runtime.getRuntime().exec(DOT_PATH);
            process.destroy();
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    public static void computeNodePositions(Graph graph) {
        if (!canProcess()) {
            return;
        }

        resetConversion();
        Map<String, Point3> newPoints = computePositions(convertToDot(graph));

        for (Map.Entry<String, Point3> item : newPoints.entrySet()) {
            Node node = graph.getNode(item.getKey());
            node.setAttribute("xyz", item.getValue().x, item.getValue().y, item.getValue().z);
        }
    }

    private static Map<String, Point3> computePositions(String graphData) {
        Map<String, Point3> result = new HashMap<String, Point3>();
        try {
            Process process = Runtime.getRuntime().exec(DOT_PATH);

            PrintWriter pw = new PrintWriter(process.getOutputStream());
            pw.println(graphData);
            pw.flush();
            pw.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("\t") && !line.contains("->") && !line.contains("<-")) {
                    line = line.trim();
                    int index = line.indexOf(" ");

                    String id = line.substring(0, index);
                    String properties = line.substring(index + 2);
                    properties = properties.substring(0, properties.length() - 2);

                    for (String pair : properties.split(", ")) {
                        String[] propItem = pair.split("=");

                        if ("pos".equals(propItem[0])) {
                            String[] xy = propItem[1].substring(1, propItem[1].length() - 1).split(",");

                            result.put(DotToId(id), new Point3(Double.parseDouble(xy[0]), Double.parseDouble(xy[1]), 0));
                        }
                    }
                }
            }
            reader.close();

            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String convertToDot(Graph graph) {
        StringBuilder sb = new StringBuilder("digraph G {\n");
        for (Node node : graph.getNodeSet()) {
            for (Edge edge : node.getEachLeavingEdge()) {
                sb.append("\t").append(idToDot(edge.getNode0().getId())).append(" -> ").append(idToDot(edge.getNode1().getId())).append(";\n");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    private static int nodesID = 0;
    private static Map<String, String> conversionIdMap = new HashMap<String, String>();
    private static Map<String, String> conversionDotMap = new HashMap<String, String>();

    private static void resetConversion() {
        conversionIdMap.clear();
        conversionDotMap.clear();
        nodesID = 0;
    }

    private static String idToDot(String id) {
        String dotID = conversionIdMap.get(id);
        if (dotID == null) {
            dotID = "" + nodesID;
            nodesID++;
            conversionIdMap.put(id, dotID);
            conversionDotMap.put(dotID, id);
        }
        return dotID;
    }

    private static String DotToId(String dotId) {
        return conversionDotMap.get(dotId);
    }
}
