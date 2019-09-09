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

import javolution.xml.stream.XMLStreamException;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.EventListener;

interface FaceReaderConnectionListener extends EventListener {
	void onFaceReaderConnection(FaceReaderReceiver receiver, FaceReaderSender sender);
}

/**
 * @author Sami Boukortt, sami.boukortt@insa-rouen.fr
 * @version 1, 5/28/15
 */
public class GUI extends JFrame implements ClassificationListener, ResponseMessageListener {
	private final Object socketLock = new Object();
	private Socket socket;
	private FaceReaderReceiver receiver;
	private FaceReaderSender sender;
	private final JLabel statusMessage = new JLabel();
	private final JTextField addressField = new JTextField();
	private final JSpinner portSpinner = new JSpinner();
	private final JComboBox<String> stimulusList = new JComboBox<>();
	private final JComboBox<String> eventMarkerList = new JComboBox<>();
	private final JTextArea stateLog = new JTextArea();
	private final JTextArea detailedLog = new JTextArea();
	private final EventListenerList listeners = new EventListenerList();
	private final JCheckBox stateLogCheckBox = new JCheckBox("State log");
	private final JCheckBox detailedLogCheckBox = new JCheckBox("Detailed log");

	public void addFaceReaderConnectionListener(FaceReaderConnectionListener listener) {
		listeners.add(FaceReaderConnectionListener.class, listener);
	}

	public void removeFaceReaderConnectionListener(FaceReaderConnectionListener listener) {
		listeners.remove(FaceReaderConnectionListener.class, listener);
	}

	private Action createAction(String label, final ActionMessage message) {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent event) {
				sendMessage(message);
			}
		};
		action.putValue(Action.NAME, label);
		return action;
	}

	private void sendMessage(final ActionMessage message) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					synchronized (socketLock) {
						sender.sendActionMessage(message);
					}
				}
				catch (XMLStreamException | IOException e) {
					updateStatusMessage(String.format("%s: %s", e.getClass().getSimpleName(), e.getLocalizedMessage()));
				}
			}
		}).start();
	}

	public GUI() {
		setTitle("FaceReader");
		setLayout(new BorderLayout());

		//region the status bar
		JXStatusBar statusBar = new JXStatusBar();
		statusBar.add(statusMessage);
		add(statusBar, BorderLayout.PAGE_END);
		//endregion

		//region the action panel on the right
		{
			JPanel actionPanel = new JXTaskPaneContainer();
			actionPanel.setLayout(new VerticalLayout());

			{
				JXTaskPane connectionPane = new JXTaskPane();
				connectionPane.setTitle("Connection");

				connectionPane.add(new JLabel("Address: "));
				connectionPane.add(addressField);

				connectionPane.add(new JLabel("Port: "));
				connectionPane.add(portSpinner);

				JButton connectButton = new JButton("Connect");
				connectButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent event) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									connectTo(InetAddress.getByName(addressField.getText()),
									          (Integer) portSpinner.getValue());
								}
								catch (UnknownHostException e) {
									updateStatusMessage(String.format("Unknown host: %s", e.getLocalizedMessage()));
								}
							}
						}).start();
					}
				});
				connectionPane.add(connectButton);

				actionPanel.add(connectionPane);
			}

			{
				JXTaskPane analysisTasks = new JXTaskPane();
				analysisTasks.setTitle("Analysis");

				Action start = createAction("Start", ActionMessage.startAnalyzing()),
				       stop  = createAction("Stop",  ActionMessage.stopAnalyzing());

				analysisTasks.add(start);
				analysisTasks.add(stop);

				actionPanel.add(analysisTasks);
			}

			add(actionPanel, BorderLayout.EAST);
		}
		//endregion

		{
			JPanel centralPanel = new JPanel();
			centralPanel.setLayout(new VerticalLayout());

			//region the panel for selecting which logs to receive
			{
				JPanel receivePanel = new JPanel();
				receivePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
				receivePanel.setBorder(BorderFactory.createTitledBorder("Receive:"));

				stateLogCheckBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							sendMessage(ActionMessage.startStateLogSending());
						}
						else if (e.getStateChange() == ItemEvent.DESELECTED) {
							sendMessage(ActionMessage.stopStateLogSending());
						}
					}
				});

				detailedLogCheckBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {
							sendMessage(ActionMessage.startDetailedLogSending());
						}
						else if (e.getStateChange() == ItemEvent.DESELECTED) {
							sendMessage(ActionMessage.stopDetailedLogSending());
						}
					}
				});

				receivePanel.add(stateLogCheckBox);
				receivePanel.add(detailedLogCheckBox);

				centralPanel.add(receivePanel);
			}
			//endregion

			//region the panel for scoring stimuli and event markers
			{
				JPanel stimuliAndEventMarkers = new JPanel();
				stimuliAndEventMarkers.setLayout(new GridBagLayout());
				stimuliAndEventMarkers.setBorder(BorderFactory.createTitledBorder("Stimuli and Event Markers"));

				GridBagConstraints textConstraints = new GridBagConstraints(),
				                   comboBoxConstraints = new GridBagConstraints(),
				                   buttonConstraints = new GridBagConstraints();

				textConstraints.gridx = 0;
				textConstraints.gridy = 0;
				textConstraints.anchor = GridBagConstraints.LINE_END;

				comboBoxConstraints.gridx = 1;
				comboBoxConstraints.gridy = 0;
				comboBoxConstraints.weightx = 1;
				comboBoxConstraints.fill = GridBagConstraints.HORIZONTAL;

				buttonConstraints.gridx = 2;
				buttonConstraints.gridy = 0;

				stimuliAndEventMarkers.add(new JLabel("Stimuli: "), textConstraints);
				stimuliAndEventMarkers.add(stimulusList, comboBoxConstraints);
				JButton refreshStimuliButton = new JButton("Refresh list");
				refreshStimuliButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sendMessage(ActionMessage.getStimuli());
					}
				});
				stimuliAndEventMarkers.add(refreshStimuliButton, buttonConstraints);
				JButton scoreStimulusButton = new JButton("Score stimulus");
				scoreStimulusButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sendMessage(ActionMessage.scoreStimulus((String) stimulusList.getSelectedItem()));
					}
				});
				++buttonConstraints.gridx;
				stimuliAndEventMarkers.add(scoreStimulusButton);

				++textConstraints.gridy;
				++comboBoxConstraints.gridy;
				++buttonConstraints.gridy;
				buttonConstraints.gridx = 2;

				stimuliAndEventMarkers.add(new JLabel("Event markers: "), textConstraints);
				stimuliAndEventMarkers.add(eventMarkerList, comboBoxConstraints);
				JButton refreshEventMarkersButton = new JButton("Refresh list");
				refreshEventMarkersButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sendMessage(ActionMessage.getEventMarkers());
					}
				});
				stimuliAndEventMarkers.add(refreshEventMarkersButton, buttonConstraints);
				JButton scoreEventMarkerButton = new JButton("Score event marker");
				scoreEventMarkerButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						sendMessage(ActionMessage.scoreEventMarker((String) eventMarkerList.getSelectedItem()));
					}
				});
				++buttonConstraints.gridx;
				stimuliAndEventMarkers.add(scoreEventMarkerButton, buttonConstraints);

				centralPanel.add(stimuliAndEventMarkers);
			}
			//endregion

			//region the panel that displays the logs
			{
				JPanel logPanel = new JPanel();
				logPanel.setLayout(new VerticalLayout());

				JPanel stateLogPanel = new JPanel();
				stateLogPanel.setLayout(new VerticalLayout());
				stateLogPanel.setBorder(BorderFactory.createTitledBorder("State log"));
				stateLog.setEditable(false);
				stateLog.setLineWrap(true);
				stateLogPanel.add(stateLog);

				JPanel detailedLogPanel = new JPanel();
				detailedLogPanel.setLayout(new VerticalLayout());
				detailedLogPanel.setBorder(BorderFactory.createTitledBorder("Detailed log"));
				detailedLog.setEditable(false);
				detailedLog.setLineWrap(true);
				detailedLogPanel.add(detailedLog);

				logPanel.add(stateLogPanel);
				logPanel.add(detailedLogPanel);

				centralPanel.add(logPanel);
			}
			//endregion

			add(centralPanel, BorderLayout.CENTER);
		}

		pack();
		setMinimumSize(getPreferredSize());

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				try {
					if (socket != null) {
						socket.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void updateStatusMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				statusMessage.setText(message);
			}
		});
	}

	public void connectTo(final InetAddress address, final int port) {
		final GUI that = this;
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (socketLock) {
					if (socket != null) {
						try {
							socket.close();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							addressField.setText(address.getHostAddress());
							portSpinner.setModel(new SpinnerNumberModel(port, 0, 65535, 1));

							for (JCheckBox logCheckBox: new JCheckBox[] {stateLogCheckBox, detailedLogCheckBox}) {
								ItemListener[] listeners = logCheckBox.getItemListeners();
								for (ItemListener listener: listeners) {
									logCheckBox.removeItemListener(listener);
								}

								logCheckBox.setSelected(false);

								for (ItemListener listener: listeners) {
									logCheckBox.addItemListener(listener);
								}
							}
						}
					});

					try {
						socket = new Socket(address, port);
						receiver = new FaceReaderReceiver(socket.getInputStream());
						sender = new FaceReaderSender(socket.getOutputStream());
					}
					catch (IOException e) {
						updateStatusMessage(String.format("IOException: %s", e.getLocalizedMessage()));
						return;
					}

					receiver.addClassificationListener(that);
					receiver.addResponseMessageListener(that);

					new Thread(receiver).start();

					updateStatusMessage(String.format("Connected to FaceReader ([%s]:%d).", address, port));
				}

				for (FaceReaderConnectionListener listener: listeners.getListeners(FaceReaderConnectionListener.class)) {
					listener.onFaceReaderConnection(receiver, sender);
				}
			}
		}).start();
	}

	@Override
	public void onClassification(Classification classification) {
		switch (classification.getLogType()) {
			case StateLog:
				stateLog.setText(classification.toString());
				break;
			case DetailedLog:
				detailedLog.setText(classification.toString());
				break;
		}
	}

	@Override
	public void onResponseMessage(final ResponseMessage message) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				switch (message.type) {
					case FaceReader_Sends_Error:
						updateStatusMessage(String.format("Error: %s", message.information));
						break;

					case FaceReader_Sends_Success:
						updateStatusMessage(message.information.toString());
						break;

					case FaceReader_Sends_Stimuli:
						updateStatusMessage("");
						stimulusList.setModel(new DefaultComboBoxModel<>(message.information.toArray(new String[message.information.size()])));
						break;

					case FaceReader_Sends_EventMarkers:
						updateStatusMessage("");
						eventMarkerList.setModel(new DefaultComboBoxModel<>(message.information.toArray(new String[message.information.size()])));
						break;
				}
			}
		});
	}

	public static void main(String[] arguments) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException ignored) {}

				GUI gui = new GUI();
				gui.connectTo(InetAddress.getLoopbackAddress(), 9090);
				gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				gui.setVisible(true);
			}
		});
	}
}
