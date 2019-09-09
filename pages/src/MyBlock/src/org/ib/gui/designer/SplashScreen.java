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

package org.ib.gui.designer;

import net.miginfocom.swing.MigLayout;
import org.ib.gui.util.ImageHelper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/16/13
 */
public class SplashScreen extends JFrame implements ActionListener {
    private JButton newFile;
    private JButton openFile;

    public SplashScreen() throws HeadlessException {
        super("AgentSlang Designer");

        initComponents();
        initLayout();
        initActions();

        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setIconImage(ImageHelper.buildImage("../icons/logo.png", SplashScreen.class));
        setUndecorated(true);
        getContentPane().setBackground(Color.white);

        newFile = new JButton("New Project", ImageHelper.buildIcon("../icons/new_document.png", SplashScreen.class));
        newFile.setFont(newFile.getFont().deriveFont(24f));
        newFile.setBackground(Color.white);
        newFile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 10)));
        newFile.setFocusPainted(false);
        newFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        openFile = new JButton("Open Project", ImageHelper.buildIcon("../icons/open_document.png", SplashScreen.class));
        openFile.setFont(openFile.getFont().deriveFont(24f));
        openFile.setBackground(Color.white);
        openFile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.white), BorderFactory.createEmptyBorder(5, 5, 5, 10)));
        openFile.setFocusPainted(false);
        openFile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void initLayout() {
        setLayout(new BorderLayout(10, 10));
        add(new JLabel(ImageHelper.buildIcon("../icons/logo.jpg", SplashScreen.class)), BorderLayout.CENTER);

        JPanel actionsPanel = new JPanel(new MigLayout("center", "[]20[]"));
        actionsPanel.setBackground(Color.white);

        actionsPanel.add(newFile);
        actionsPanel.add(openFile);

        add(actionsPanel, BorderLayout.SOUTH);
        pack();
    }

    private void initActions() {
        newFile.setActionCommand("new");
        newFile.addActionListener(this);

        openFile.setActionCommand("open");
        openFile.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if ("open".equals(e.getActionCommand())) {
            final JFileChooser fc = new JFileChooser(new File(".")) {
                public void approveSelection() {
                    if (getSelectedFile() != null && getSelectedFile().isFile()) {
                        super.approveSelection();
                    }
                }
            };
            fc.setFileFilter(new FileNameExtensionFilter("AgentSlang configuration files", "xml"));
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setAcceptAllFileFilterUsed(false);

            int returnVal = fc.showOpenDialog(SplashScreen.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                DesignerMain designerMain = new DesignerMain();
                designerMain.setExtendedState(JFrame.MAXIMIZED_BOTH);
                if (designerMain.loadModel(file)) {
                    designerMain.setVisible(true);
                    this.setVisible(false);
                    this.dispose();
                } else {
                    designerMain.dispose();
                    JOptionPane.showMessageDialog(this, "Please check the selected project carefully !", "Invalid project template", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if ("new".equals(e.getActionCommand())) {
            try {
                DesignerMain designerMain = new DesignerMain();
                designerMain.setExtendedState(JFrame.MAXIMIZED_BOTH);
                designerMain.loadModel(new File(DesignerMain.class.getResource("defaultConfig.xml").toURI()));
                designerMain.setVisible(true);
                this.setVisible(false);
                this.dispose();
            } catch (URISyntaxException e1) {
                JOptionPane.showMessageDialog(this, "Please check the default settings for new projects !", "Invalid default template", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }
}
