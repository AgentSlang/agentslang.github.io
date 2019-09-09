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

package org.agent.slang.dm.narrative.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This class provides requirements for agentslang slide show frame in order to displaying pictures, showing the name of story, current step and pointing to a special part of a picture.
 * OS Compatibility: Windows and Linux
 * @author William Boisseleau, william.boisseleau@insa-rouen.fr
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 07/23/13 - Initial commit, William Boisseleau
 *          2, 12/03/13 - AgentSlang 1.1 integration, Ovidiu Serban
 *          3, 07/16/15 - Publish the cursor position, Sami Boukortt
 */

public class IntegrationJPanel extends JPanel {

    private JLabel scenario;
    private JLabel slide;
    private JLabel step_i;

    public IntegrationJPanel(File modelPath) {
        initComponents(modelPath);
    }

    /**
     * Initializes agentslang slide frame panel and sets its characteristics like step number, slide, story name basic on input file data
     * @param modelPath input path file model of the story 
     */
    private void initComponents(File modelPath) {
        JPanel jPanelText = new JPanel();
        scenario = new JLabel();
        step_i = new JLabel();
        JPanel jPanelSlide = new JPanel();
        slide = new JLabel();

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image cursorImage = toolkit.getImage(getClass().getResource("cursor.png"));
        slide.setCursor(toolkit.createCustomCursor(cursorImage, new Point(11, 2), "hand"));

        setBackground(new java.awt.Color(252, 246, 170));
        setPreferredSize(new java.awt.Dimension(1200, 730));

        jPanelText.setOpaque(false);

        scenario.setText("Welcome. Your software is ready.");

        step_i.setText("");

        GroupLayout jPanelTextLayout = new GroupLayout(jPanelText);
        jPanelText.setLayout(jPanelTextLayout);
        jPanelTextLayout.setHorizontalGroup(
                jPanelTextLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelTextLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanelTextLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(scenario, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanelTextLayout.createSequentialGroup()
                                                .addComponent(step_i)
                                                .addGap(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        jPanelTextLayout.setVerticalGroup(
                jPanelTextLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelTextLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scenario)
                                .addGap(18, 18, 18)
                                .addComponent(step_i)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanelSlide.setOpaque(false);

        slide.setIcon(new ImageIcon(new File(modelPath, "slides/slide0.png").getAbsolutePath())); // NOI18N

        GroupLayout jPanelSlideLayout = new GroupLayout(jPanelSlide);
        jPanelSlide.setLayout(jPanelSlideLayout);
        jPanelSlideLayout.setHorizontalGroup(
                jPanelSlideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanelSlideLayout.createSequentialGroup()
                                .addComponent(slide)
                                .addGap(0, 72, Short.MAX_VALUE))
        );
        jPanelSlideLayout.setVerticalGroup(
                jPanelSlideLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(slide)
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanelText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanelSlide, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(jPanelSlide, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanelText, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    /**
     * Sets scenario name
     * @param str scenario name
     */
    public void setScenarioText(String str) {
        this.scenario.setText(str);
    }

    /**
     * sets mouse icon
     * @param str mouse icon file
     */
    public void setSlideIcon(File str) {
        this.slide.setIcon(new ImageIcon(str.getAbsolutePath()));
    }

    /**
     * sets step name
     * @param str step name
     */
    public void setStep_iText(String str) {
        this.step_i.setText(str);
    }
}
