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

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/18/11
 */
public class ImageHelper {
    public static Icon buildIcon(final String relativePath, final Class loader) {
        URL resource = loader.getResource(relativePath);
        if (resource != null) {
            return new ImageIcon(resource);
        } else {
            System.err.println("[ImageHelper] Invalid resource: " + relativePath + " Loader: " + loader.getName());
            throw new IllegalArgumentException("Invalid resource");
        }
    }

    public static Image buildImage(final String relativePath, final Class loader) {
        URL resource = loader.getResource(relativePath);
        if (resource != null) {
            try {
                return ImageIO.read(resource);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid resource", e);
            }
        } else {
            System.err.println("[ImageHelper] Invalid resource: " + relativePath + " Loader: " + loader.getName());
            throw new IllegalArgumentException("Invalid resource");
        }
    }
}
