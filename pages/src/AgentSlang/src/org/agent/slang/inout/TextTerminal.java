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

package org.agent.slang.inout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;

/**
 * This class provides fundamental requirements for TextComponent in order to send/receive text messages.
 * OS Compatibility: Windows and Linux
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 12/8/12
 */
public class TextTerminal extends JFrame {
    private JTextPane componentsLog;
    private JTextField inputLine;
    private JButton inputButton;

    private TextComponent textComponent;

    private static final int MAX_SIZE = 20;
    private LinkedList<String> previousText = new LinkedList<String>();
    private int index = -1;

    public TextTerminal(TextComponent textComponent) {
        this.textComponent = textComponent;
        initComponents();
        initLayout();
        initActions();
    }

    /**
     * Initialize text panel basic configuration.
     */
    private void initComponents() {
        setTitle("Text monitor");
        setSize(new Dimension(800, 600));
        setPreferredSize(new Dimension(800, 600));

        componentsLog = new JTextPane();
        componentsLog.setSize(new Dimension(400, 400));
        componentsLog.setPreferredSize(new Dimension(400, 400));
        componentsLog.setEditable(false);

        inputLine = new JTextField();

        inputButton = new JButton("Send");
    }

    /**
     * Initialize text panel layout.
     */
    private void initLayout() {
        setLayout(new BorderLayout(5, 5));

        JPanel toolPanel = new JPanel(new BorderLayout(5, 5));
        toolPanel.add(inputLine, BorderLayout.CENTER);
        toolPanel.add(inputButton, BorderLayout.EAST);

        add(new JScrollPane(componentsLog, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        add(toolPanel, BorderLayout.SOUTH);
    }

    /**
     * Initialize text panel actions.
     */
    private void initActions() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        inputButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(inputLine.getText());
                inputLine.setText("");
            }
        });

        inputLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(inputLine.getText());
                inputLine.setText("");
            }
        });

        inputLine.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (!previousText.isEmpty()) {
                        if (index == -1) {
                            index = previousText.size() - 1;
                        } else {
                            index = Math.min(previousText.size() - 1, Math.max(0, index + (e.getKeyCode() == KeyEvent.VK_UP ? -1 : +1)));
                        }

                        inputLine.setText(previousText.get(index));
                    }
                }
            }
        });
    }

    /**
     * Add received items in cache.
     * @param item received text item
     */
    private void addToCache(String item) {
        if (item != null && item.trim().length() > 0) {
            index = -1;
            if (previousText.isEmpty() || !item.equals(previousText.getLast())) {
                previousText.add(item);
            }
            while (previousText.size() > MAX_SIZE) {
                previousText.removeFirst();
            }
        }
    }

    /**
     * Sends text message.
     * @param message
     */
    protected void sendMessage(String message) {
        addToCache(message);
        addMessage("<<< " + message, formatMessage(false));
        if (textComponent != null) {
            textComponent.sendMessage(message);
        }
    }

    /**
     * Controls hot to show messages.
     * @param message text message
     */
    protected void handleMessage(String message) {
        addMessage(">>> " + message, formatMessage(true));
    }

    /**
     * Adds one message with a specific style into text panel.
     * @param message text message 
     * @param style message displaying style
     */
    private void addMessage(String message, Style style) {
        StyledDocument doc = componentsLog.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), message + "\n", style);
            componentsLog.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Defines incoming messages format.
     * @param incoming received message
     * @return message displaying style
     */
    private Style formatMessage(boolean incoming) {
        Style style = componentsLog.getStyle("none");
        if (style == null) {
            style = componentsLog.addStyle("none", null);
        }
        if (incoming) {
            StyleConstants.setForeground(style, Color.blue);
        } else {
            StyleConstants.setForeground(style, Color.black);
        }
        return style;
    }
}
