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

package org.ib.bricks;

import org.ib.component.annotations.TestClass;
import org.ib.component.base.SourceComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.StringData;
import org.ib.data.LanguageUtils;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 11/21/12
 */

@TestClass

public class Test1 extends SourceComponent {
    private int i;

    public Test1(String port, ComponentConfig config) {
        super(port, config);
    }

    protected void setupComponent(ComponentConfig config) {
        i = 0;
    }

    public void definePublishedData() {
        addOutboundTypeChecker("test1", StringData.class);
        addOutboundTypeChecker("test2", StringData.class);
    }

    public boolean act() {

        publishData("test1", new StringData(i, "Hello-t1:" + i, LanguageUtils.IDX_NONE));
        i++;
        publishData("test2", new StringData(i, "Hello-t2:" + i, LanguageUtils.IDX_NONE));
        return true;
    }
}
