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

package org.ib.gui.components;

import net.miginfocom.swing.MigLayout;
import org.ib.gui.components.delegate.EditableComponent;
import org.ib.gui.components.delegate.MutableEditableComponent;
import org.ib.gui.components.events.EditEvent;
import org.ib.gui.components.events.EditListener;
import org.ib.gui.data.DataProvider;
import org.ib.gui.util.ImageHelper;
import org.ib.gui.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/14/13
 */
public class EditPanel extends JPanel implements ActionListener {
    private static final String ACTION_NEW = "new";
    private static final String ACTION_COPY = "copy";
    private static final String ACTION_EDIT = "edit";
    private static final String ACTION_CUT = "cut";

    private static final String ACTION_CANCEL = "cancel";
    private static final String ACTION_OK = "ok";

    private JList listComponent;
    private String valueSeparator;
    private int valueCount;

    private JButton newBtn;
    private JButton copyBtn;
    private JButton editBtn;
    private JButton cutBtn;

    private EditorActions editPane;
    private String selectedAction;

    private final java.util.List<EditListener> listeners = new LinkedList<EditListener>();

    public EditPanel(JList listComponent, int valueCount, String valueSeparator) {
        this.listComponent = listComponent;
        this.valueSeparator = valueSeparator;
        this.valueCount = valueCount;

        initComponents();
        initLayout();
        initActions();
    }

    private void initComponents() {
        this.setOpaque(true);
        this.setBackground(Color.white);
        this.setBorder(BorderFactory.createLineBorder(Color.black, 1));

        newBtn = new JButton(ImageHelper.buildIcon("../icons/new.png", EditPanel.class));
        newBtn.setMargin(new Insets(0, 0, 0, 0));
        newBtn.setBackground(Color.white);
        newBtn.setBorder(BorderFactory.createEmptyBorder());

        copyBtn = new JButton(ImageHelper.buildIcon("../icons/copy.png", EditPanel.class));
        copyBtn.setMargin(new Insets(0, 0, 0, 0));
        copyBtn.setBackground(Color.white);
        copyBtn.setBorder(BorderFactory.createEmptyBorder());

        editBtn = new JButton(ImageHelper.buildIcon("../icons/edit.png", EditPanel.class));
        editBtn.setMargin(new Insets(0, 0, 0, 0));
        editBtn.setBackground(Color.white);
        editBtn.setBorder(BorderFactory.createEmptyBorder());

        cutBtn = new JButton(ImageHelper.buildIcon("../icons/cut.png", EditPanel.class));
        cutBtn.setMargin(new Insets(0, 0, 0, 0));
        cutBtn.setBackground(Color.white);
        cutBtn.setBorder(BorderFactory.createEmptyBorder());
    }

    private void initLayout() {
        setLayout(new MigLayout("insets 2"));

        add(newBtn, "wrap");
        add(copyBtn, "wrap");
        add(editBtn, "wrap");
        add(cutBtn);
    }

    private void initActions() {
        newBtn.setActionCommand(ACTION_NEW);
        newBtn.addActionListener(this);

        copyBtn.setActionCommand(ACTION_COPY);
        copyBtn.addActionListener(this);

        editBtn.setActionCommand(ACTION_EDIT);
        editBtn.addActionListener(this);

        cutBtn.setActionCommand(ACTION_CUT);
        cutBtn.addActionListener(this);

        listComponent.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    editSelectedItem();
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            public void componentMoved(ComponentEvent e) {
                if (editPane != null && editPane.isVisible()) {
                    Point point = UIUtils.getCenterLocationOnScreen(editPane.selectedComponent);
                    editPane.setLocation(point.x, point.y);
                }
            }
        });
    }

//    public void setup() {
//        SwingUtilities.getWindowAncestor(this).addComponentListener(new ComponentAdapter() {
//            public void componentMoved(ComponentEvent e) {
//                if (editPane != null && editPane.isVisible()) {
//                    Point point = UIUtils.getCenterLocationOnScreen(editPane.selectedComponent);
//                    editPane.setLocation(point.x, point.y);
//                }
//            }
//        });
//
//        SwingUtilities.getWindowAncestor(this).addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                if (editPane != null && editPane.isVisible() && !editPane.getBounds().contains(e.getX(), e.getY())) {
//                    hideEditPane();
//                }
//            }
//        });
//    }

    public void addEditListener(EditListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeEditListener(EditListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void fireDataUpdated(EditEvent event) {
        synchronized (listeners) {
            for (EditListener listener : listeners) {
                listener.dataUpdated(event);
            }
        }
    }

    private void showEditPane(String action, String value, final JComponent source) {
        Point point = UIUtils.getCenterLocationOnScreen(source);

        if (editPane == null) {
            editPane = new EditorActions(this);
        }
        editPane.setSelectedComponent(source);
        editPane.setLocation(point.x, point.y);
        editPane.pack();
        editPane.setEditorsValue(value);
        editPane.valueEditors[0].requestFocus();
        selectedAction = action;

        editPane.setVisible(true);
    }

    private void hideEditPane() {
        editPane.setVisible(false);
        editPane.dispose();
    }

    @SuppressWarnings("unchecked")
    private void commitChanges() {
        String value = editPane.joinValues();
        if (ACTION_NEW.equals(selectedAction)) {
            ((DefaultListModel) listComponent.getModel()).addElement(value);
            fireDataUpdated(new EditEvent(EditEvent.ACTION_NEW, value, value));
        } else if (ACTION_EDIT.equals(selectedAction)) {
            int index = listComponent.getSelectedIndex();
            String selected = (String) listComponent.getSelectedValue();
            ((DefaultListModel) listComponent.getModel()).set(index, value);
            fireDataUpdated(new EditEvent(EditEvent.ACTION_EDIT, value, selected));
        }
        hideEditPane();
    }


    private void editSelectedItem() {
        if (listComponent.getSelectedValue() != null) {
            showEditPane(ACTION_EDIT, listComponent.getSelectedValue().toString(), editBtn);
        }
    }


    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        if (ACTION_NEW.equals(e.getActionCommand())) {
            showEditPane(ACTION_NEW, "", (JComponent) e.getSource());
        } else if (ACTION_EDIT.equals(e.getActionCommand())) {
            editSelectedItem();
        } else if (ACTION_COPY.equals(e.getActionCommand())) {
            if (listComponent.getSelectedValue() != null) {
                String selected = (String) listComponent.getSelectedValue();
                ((DefaultListModel) listComponent.getModel()).addElement(listComponent.getSelectedValue());
                fireDataUpdated(new EditEvent(EditEvent.ACTION_NEW, selected, selected));
            }
        } else if (ACTION_CUT.equals(e.getActionCommand())) {
            if (listComponent.getSelectedValue() != null) {
                String selected = (String) listComponent.getSelectedValue();
                ((DefaultListModel) listComponent.getModel()).remove(listComponent.getSelectedIndex());
                fireDataUpdated(new EditEvent(EditEvent.ACTION_DELETE, selected, selected));
            }
        } else if (ACTION_CANCEL.equals(e.getActionCommand())) {
            hideEditPane();
        } else if (ACTION_OK.equals(e.getActionCommand())) {
            commitChanges();
        } else if ("valueEdit".equals(e.getActionCommand())) {
            if (editPane.allEditorsNonEmpty()) {
                commitChanges();
            }
        }
    }

    public void subscribeParamToDataProvider(int paramIndex, DataProvider provider) {
        if (editPane == null) {
            editPane = new EditorActions(this);
        }
        if (paramIndex >= 0 && paramIndex < editPane.valueEditors.length) {
            editPane.valueEditors[paramIndex].subscribeToDataProvider(provider);
        }
    }

    private class EditorActions extends JDialog {
        private EditableComponent[] valueEditors;

        private JButton okBtn;
        private JButton cancelBtn;

        private JComponent selectedComponent;

        private EditorActions(JComponent component) {
            super(SwingUtilities.getWindowAncestor(component));
            setModal(true);

            setUndecorated(true);
            getContentPane().setBackground(Color.white);
            ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.black, 1));

            valueEditors = new EditableComponent[valueCount];
            for (int i = 0; i < valueCount; i++) {
                valueEditors[i] = new MutableEditableComponent(15);
            }

            okBtn = new JButton(ImageHelper.buildIcon("../icons/button_check.png", EditPanel.class));
            okBtn.setMargin(new Insets(0, 0, 0, 0));
            okBtn.setBackground(Color.white);
            okBtn.setBorder(BorderFactory.createEmptyBorder());

            cancelBtn = new JButton(ImageHelper.buildIcon("../icons/button_delete.png", EditPanel.class));
            cancelBtn.setMargin(new Insets(0, 0, 0, 0));
            cancelBtn.setBackground(Color.white);
            cancelBtn.setBorder(BorderFactory.createEmptyBorder());

            setLayout(new BorderLayout(5, 5));

            JPanel valuePanel = new JPanel(new MigLayout("", "[fill,grow][pref!][fill,grow]"));
            valuePanel.setOpaque(false);
            for (int i = 0; i < valueEditors.length; i++) {
                if (i > 0) {
                    valuePanel.add(new JLabel(valueSeparator));
                }
                valuePanel.add(valueEditors[i]);
            }

            JPanel actionsPanel = new JPanel(new MigLayout("", "[fill,grow][pref!][pref!]"));
            actionsPanel.setOpaque(false);
            actionsPanel.add(okBtn, "skip");
            actionsPanel.add(cancelBtn);

            add(valuePanel, BorderLayout.CENTER);
            add(actionsPanel, BorderLayout.SOUTH);

            cancelBtn.setActionCommand(ACTION_CANCEL);
            cancelBtn.addActionListener(EditPanel.this);

            okBtn.setActionCommand(ACTION_OK);
            okBtn.addActionListener(EditPanel.this);

            for (EditableComponent valueEditor : valueEditors) {
                valueEditor.setActionCommand("valueEdit");
                valueEditor.addActionListener(EditPanel.this);
            }
        }

        public void setSelectedComponent(JComponent selectedComponent) {
            this.selectedComponent = selectedComponent;
        }

        private void setEditorsValue(String value) {
            String[] split = value.split(valueSeparator);
            int n = Math.min(split.length, valueEditors.length);

            for (int i = 0; i < n; i++) {
                valueEditors[i].setText(split[i]);
            }

            for (int i = n; i < valueEditors.length; i++) {
                valueEditors[i].setText("");
            }
        }

        private String joinValues() {
            StringBuilder sb = new StringBuilder();

            for (EditableComponent valueEditor : valueEditors) {
                if (sb.length() > 0) {
                    sb.append(valueSeparator);
                }
                sb.append(valueEditor.getText());
            }

            return sb.toString();
        }

        private boolean allEditorsNonEmpty() {
            for (EditableComponent valueEditor : valueEditors) {
                if (valueEditor.getText() == null || valueEditor.getText().trim().length() == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
