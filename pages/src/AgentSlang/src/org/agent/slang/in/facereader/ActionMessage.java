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

package org.agent.slang.in.facereader;

import java.util.Collections;
import java.util.List;

/**
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
final class ActionMessage {
    enum Type {
        FaceReader_Start_Analyzing,
        FaceReader_Start_StateLogSending,
        FaceReader_Start_DetailedLogSending,

        FaceReader_Stop_StateLogSending,
        FaceReader_Stop_DetailedLogSending,
        FaceReader_Stop_Analyzing,

        FaceReader_Get_Stimuli,
        FaceReader_Get_EventMarkers,

        FaceReader_Score_Stimulus,
        FaceReader_Score_EventMarker,
    }

    final Type type;
    final String id;
    final List<String> information;

    private ActionMessage(Type type, String id, List<String> information) {
        this.type = type;
        this.id = id;
        this.information = information;
    }

    public static ActionMessage startAnalyzing() {
        return new ActionMessage(Type.FaceReader_Start_Analyzing, null, null);
    }
    public static ActionMessage startAnalyzing(String id) {
        return new ActionMessage(Type.FaceReader_Start_Analyzing, id, null);
    }

    public static ActionMessage startStateLogSending() {
        return new ActionMessage(Type.FaceReader_Start_StateLogSending, null, null);
    }
    public static ActionMessage startStateLogSending(String id) {
        return new ActionMessage(Type.FaceReader_Start_StateLogSending, id, null);
    }

    public static ActionMessage startDetailedLogSending() {
        return new ActionMessage(Type.FaceReader_Start_DetailedLogSending, null, null);
    }
    public static ActionMessage startDetailedLogSending(String id) {
        return new ActionMessage(Type.FaceReader_Start_DetailedLogSending, id, null);
    }

    public static ActionMessage stopAnalyzing() {
        return new ActionMessage(Type.FaceReader_Stop_Analyzing, null, null);
    }
    public static ActionMessage stopAnalyzing(String id) {
        return new ActionMessage(Type.FaceReader_Stop_Analyzing, id, null);
    }

    public static ActionMessage stopStateLogSending() {
        return new ActionMessage(Type.FaceReader_Stop_StateLogSending, null, null);
    }
    public static ActionMessage stopStateLogSending(String id) {
        return new ActionMessage(Type.FaceReader_Stop_StateLogSending, id, null);
    }

    public static ActionMessage stopDetailedLogSending() {
        return new ActionMessage(Type.FaceReader_Stop_DetailedLogSending, null, null);
    }
    public static ActionMessage stopDetailedLogSending(String id) {
        return new ActionMessage(Type.FaceReader_Stop_DetailedLogSending, id, null);
    }

    public static ActionMessage getStimuli() {
        return new ActionMessage(Type.FaceReader_Get_Stimuli, null, null);
    }
    public static ActionMessage getStimuli(String id) {
        return new ActionMessage(Type.FaceReader_Get_Stimuli, id, null);
    }

    public static ActionMessage getEventMarkers() {
        return new ActionMessage(Type.FaceReader_Get_EventMarkers, null, null);
    }
    public static ActionMessage getEventMarkers(String id) {
        return new ActionMessage(Type.FaceReader_Get_EventMarkers, id, null);
    }

    public static ActionMessage scoreStimulus(String stimulus) {
        return new ActionMessage(Type.FaceReader_Score_Stimulus, null, Collections.singletonList(stimulus));
    }
    public static ActionMessage scoreStimulus(String id, String stimulus) {
        return new ActionMessage(Type.FaceReader_Score_Stimulus, id, Collections.singletonList(stimulus));
    }

    public static ActionMessage scoreEventMarker(String eventMarker) {
        return new ActionMessage(Type.FaceReader_Score_EventMarker, null, Collections.singletonList(eventMarker));
    }
    public static ActionMessage scoreEventMarker(String id, String eventMarker) {
        return new ActionMessage(Type.FaceReader_Score_EventMarker, id, Collections.singletonList(eventMarker));
    }
}
