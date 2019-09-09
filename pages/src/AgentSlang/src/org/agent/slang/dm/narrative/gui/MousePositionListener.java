package org.agent.slang.dm.narrative.gui;

import java.util.EventListener;

/**
 * This interface provides listening to mouse position (x,y)
 * OS Compatibility: Windows and Linux
 */

public interface MousePositionListener extends EventListener {
    void onMouseMoved(int x, int y);
}
