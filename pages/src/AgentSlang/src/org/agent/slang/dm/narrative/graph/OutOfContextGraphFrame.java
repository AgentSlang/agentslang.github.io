package org.agent.slang.dm.narrative.graph;

import javax.swing.*;
import java.io.File;

/**
 * Provides general information for out of context component
 * OS Compatibility: Windows and Linux
 * Created by Julien
 */
public class OutOfContextGraphFrame extends GraphFrame {

    private static String storyStateFilePath;
    private static String patternFolder;
    private static String regexForPatternFiles;

    public OutOfContextGraphFrame(File modelDirectory, String storyStateFilePath, String patternFolder, String regexForPatternFiles) {
        super("Out Of Context", modelDirectory, storyStateFilePath, patternFolder, regexForPatternFiles);
        this.storyStateFilePath = storyStateFilePath;
        this.patternFolder = patternFolder;
        this.regexForPatternFiles = regexForPatternFiles;
    }

    public static void main(String[] arguments) {
        SwingUtilities.invokeLater(new GraphThread("Out Of Context", storyStateFilePath, patternFolder, regexForPatternFiles));
    }

}
