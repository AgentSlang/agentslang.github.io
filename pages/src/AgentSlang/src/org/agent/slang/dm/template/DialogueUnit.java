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

package org.agent.slang.dm.template;

/**
 * A data type for displaying dialogues.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/5/13
 */
public class DialogueUnit {
    public static final String TAG_DIALOGUE_UNITS = "dialogue-units";
    public static final String TAG_DIALOGUE_UNIT = "du";

    public static final String TAG_DIALOGUE_GENERATORS = "dialogue-generators";
    public static final String TAG_DIALOGUE_GENERATOR = "dg";

    public static final String PROP_ID = "id";
    public static final String PROP_PATTERN = "pattern";
    public static final String PROP_REPLY_TEXT = "response";

    private String id;
    private String replyText;

    public DialogueUnit(String id, String replyText) {
        this.id = id;
        this.replyText = replyText;
    }

    /**
     * Gets dialogue id 
     * @return dialogue id 
     */
    public String getId() {
        return id;
    }

    /**
     * Gets machine response in terms of text to a child action 
     * @return response text
     */
    public String getReplyText() {
        return replyText;
    }

    /**
     * Checks if an object is a dialogue unit
     * @return boolean
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogueUnit that = (DialogueUnit) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    /**
     * gets hash code of the id
     */
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
