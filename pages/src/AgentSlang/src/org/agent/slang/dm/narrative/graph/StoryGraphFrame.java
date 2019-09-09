package org.agent.slang.dm.narrative.graph;

import javax.swing.*;
import java.io.File;

/**
 * Provides general information for story graph component
 * OS Compatibility: Windows and Linux
 * Created by Julien on 12/01/2016.
 */
public class StoryGraphFrame extends GraphFrame {

    private static String storyStateFilePath;
    private static String patternFolder;
    private static String regexForPatternFiles;

    public StoryGraphFrame(File modelDirectory, String storyStateFilePath, String patternFolder, String regexForPatternFiles) {
        super("Story Graph", modelDirectory, storyStateFilePath, patternFolder, regexForPatternFiles);
        this.storyStateFilePath = storyStateFilePath;
        this.patternFolder = patternFolder;
        this.regexForPatternFiles = regexForPatternFiles;
    }

    public static void main(String[] arguments) {
        SwingUtilities.invokeLater(new GraphThread("Story Graph", storyStateFilePath, patternFolder, regexForPatternFiles));
    }
}
