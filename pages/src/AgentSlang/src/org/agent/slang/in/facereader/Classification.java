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

import org.msgpack.annotation.Message;
import org.msgpack.annotation.OrdinalEnum;

import java.util.List;

/**
 * Contains classification data from FaceReader.
 *
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
@Message
public class Classification {
    @OrdinalEnum public enum LogType {StateLog, DetailedLog}
    @OrdinalEnum public enum AnalysisType {Video, Camera}

    /**
     * A single classification value.
     */
    @Message
    public static class ClassificationValue {
        @OrdinalEnum public enum Type {Value, State}

        private String label;
        private Type type;
        private List<Float> values;
        private List<String> states;

        public ClassificationValue() {}

        /** @return a descriptive label for the value */
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        /** @return whether the value contains numeric data or a set of states */
        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        /** @return the numeric values (if and only if <code>getType() == Value</code>) */
        public List<Float> getValues() {
            return values;
        }

        public void setValues(List<Float> values) {
            this.values = values;
        }

        /** @return the set of states (if and only if <code>getType() == State</code>) */
        public List<String> getStates() {
            return states;
        }

        public void setStates(List<String> states) {
            this.states = states;
        }

        @Override
        public String toString() {
            return "ClassificationValue{" +
                    "label='" + label + '\'' +
                    (type == Type.Value
                        ? ", values=" + values
                        : ", states=" + states) +
                    '}';
        }
    }

    private LogType logType;
    private AnalysisType analysisType;
    private int frameNumber;
    private String analysisStartTime;
    private int frameTimeTicks;
    private List<ClassificationValue> classificationValues;

    public Classification() {}

    /** @return whether this is a state log or a detailed log */
    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    /** @return whether the analysis is performed in real-time on a camera or on a video file */
    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    /** @return the number of the frame that is being analyzed */
    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    /** @return the absolute start time of the analysis */
    public String getAnalysisStartTime() {
        return analysisStartTime;
    }

    public void setAnalysisStartTime(String analysisStartTime) {
        this.analysisStartTime = analysisStartTime;
    }

    /** @return the time of the current frame time relative to the start of the analysis */
    public int getFrameTimeTicks() {
        return frameTimeTicks;
    }

    public void setFrameTimeTicks(int frameTimeTicks) {
        this.frameTimeTicks = frameTimeTicks;
    }

    /** @return the various values associated with the current frame */
    public List<ClassificationValue> getClassificationValues() {
        return classificationValues;
    }

    public void setClassificationValues(List<ClassificationValue> classificationValues) {
        this.classificationValues = classificationValues;
    }

    @Override
    public String toString() {
        return "Classification{" +
                "logType=" + logType +
                ", analysisType=" + analysisType +
                ", frameNumber=" + frameNumber +
                ", analysisStartTime='" + analysisStartTime + '\'' +
                ", frameTimeTicks=" + frameTimeTicks +
                ", classificationValues=" + classificationValues +
                '}';
    }
}
