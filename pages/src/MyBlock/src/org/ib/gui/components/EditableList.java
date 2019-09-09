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
import org.ib.gui.components.events.EditEvent;
import org.ib.gui.components.events.EditListener;
import org.ib.gui.components.events.ListEditEvent;
import org.ib.gui.data.DataProvider;
import org.ib.gui.util.UIUtils;

import javax.swing.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 1/16/13
 */
public class EditableList extends JPanel implements EditListener {
    private EditPanel editPanel;
    private JList managedList;

    private String separator;

    private final List<EditListener> listeners = new LinkedList<EditListener>();

    public EditableList(String title, int valueCount, String separator) {
        setName(title);
        this.separator = separator;

        initComponent(valueCount);
        initLayout(title);
        initActions();
    }

    @SuppressWarnings("unchecked")
    private void initComponent(int valueCount) {
        managedList = new JList(new DefaultListModel());
        editPanel = new EditPanel(managedList, valueCount, separator);
    }

    private void initLayout(String title) {
        setLayout(new MigLayout("", "[]2[grow,fill]", "[center][top,fill,grow]"));

        UIUtils.addSeparator(this, title);
        this.add(editPanel, "h 120!,align right");
        this.add(new JScrollPane(managedList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }

    private void initActions() {
        editPanel.addEditListener(this);
    }

    public void dataUpdated(EditEvent event) {
        fireDataUpdated(new ListEditEvent(event, this));
    }

    public void clean() {
        List<String> copy = new LinkedList<String>();
        for (int i = 0; i < managedList.getModel().getSize(); i++) {
            copy.add((String) managedList.getModel().getElementAt(i));
        }

        ((DefaultListModel) managedList.getModel()).clear();

        for (String item : copy) {
            fireDataUpdated(new ListEditEvent(EditEvent.ACTION_DELETE, item, item, this));
        }
    }

    @SuppressWarnings("unchecked")
    public void updateList(String value) {
        ((DefaultListModel) managedList.getModel()).addElement(value);
        fireDataUpdated(new ListEditEvent(EditEvent.ACTION_NEW, value, value, this));
    }

    public void updateList(Collection<String> values) {
        clean();
        for (String item : values) {
            updateList(item);
        }
    }

    public void subscribeParamToDataProvider(int paramIndex, DataProvider provider) {
        editPanel.subscribeParamToDataProvider(paramIndex, provider);
    }

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

    private void fireDataUpdated(ListEditEvent event) {
        synchronized (listeners) {
            for (EditListener listener : listeners) {
                listener.dataUpdated(event);
            }
        }
    }
}
