package org.agent.slang.Learning.Dialogue;

/**
 * It provides classification ability for agentslang. We classified transcribed speech text into some dialogue elements for pattern matching. An open-source naive-bayes classifier used and modified based on our dialogue elements and agentslang running plantform.
 * OS Compatibility: Windows. Linux version does not work properly and needs some modifications.
 * A Java class that implements a simple text classifier, based on WEKA.
 * To be used with MyFilteredLearner.java.
 * WEKA is available at: http://www.cs.waikato.ac.nz/ml/weka/
 * Copyright (C) 2013 Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 *
 * This program is free software: you can redistribute it and/or modify
 * it for any purpose.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Locale;

import org.agent.slang.data.audio.PlayerEvent;
import org.ib.component.annotations.ConfigureParams;
import org.ib.component.base.MixedComponent;
import org.ib.component.model.ComponentConfig;
import org.ib.data.GenericData;
import org.ib.data.LanguageUtils;
import org.ib.data.StringData;
import org.ib.utils.FileUtils;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/*
 * This class implements a simple text classifier in Java using WEKA. It loads a
 * file with the text to classify, and the model that has been learnt with
 * MyFilteredLearner.java.
 * 
 * @author Jose Maria Gomez Hidalgo - http://www.esp.uem.es/jmgomez
 */
@ConfigureParams(mandatoryConfigurationParams = "classifierPath",
outputChannels = {"text.data","classifierSignal.data"},
outputDataTypes = {StringData.class,StringData.class},
inputDataTypes = {StringData.class,PlayerEvent.class})
public class ClassifierComponent extends MixedComponent{
	
	private static final String TEXT_DATA = "text.data";
	private static final String CLASSIFIERSIGNAL_DATA = "classifierSignal.data";
	private static final String PROP_CLASS_PATH = "classifierPath";
	private File classifierFile;
	private long messageID;
	private boolean AgentSpeaking;

	/**
	 * String that stores the text to classify
	 */
	private String text;
	/**
	 * Object that stores the instance.
	 */
	private Instances instances;
	/**
	 * Object that stores the classifier.
	 */
	private FilteredClassifier classifier;	

	/**
	 * This method loads the model to be used as classifier.
	 * 
	 * @param fileName
	 *            The name of the file that stores the text.
	 */
	public void loadModel(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					fileName));
			Object tmp = in.readObject();
			classifier = (FilteredClassifier) tmp;
			in.close();
			System.out.println("===== Loaded model: " + fileName + " =====");
		} catch (Exception e) {
			// Given the cast, a ClassNotFoundException must be caught along
			// with the IOException
			System.out.println("Problem found when reading: " + fileName);
		}
	}

	/**
	 * This method creates the instance to be classified (dialogue elements), from the text that has been read.
	 */
	public void makeInstance() {
		// Create the attributes, class and text
		FastVector fvNominalVal = new FastVector(27);
		fvNominalVal.addElement("ballposition");
		fvNominalVal.addElement("dontknow");
		fvNominalVal.addElement("retrieveball");
		fvNominalVal.addElement("reasonthrowboot");
		fvNominalVal.addElement("goodidea");
		fvNominalVal.addElement("opinionabouthead");
		fvNominalVal.addElement("opinionwhattheydo");
		fvNominalVal.addElement("mimikingbag");
		fvNominalVal.addElement("greeting");
		fvNominalVal.addElement("positiveopinion");
		fvNominalVal.addElement("negativeopinion");
		fvNominalVal.addElement("surprise");
		fvNominalVal.addElement("class");
		fvNominalVal.addElement("age");
		fvNominalVal.addElement("accept");
		fvNominalVal.addElement("reject");
		fvNominalVal.addElement("noidea");
		fvNominalVal.addElement("talkinginclass");
		fvNominalVal.addElement("givepen");
		fvNominalVal.addElement("outsideevent");
		fvNominalVal.addElement("opinionaboutteacher");
		fvNominalVal.addElement("outdoordescription");
		fvNominalVal.addElement("kissingshoes");
		fvNominalVal.addElement("metoo");
		fvNominalVal.addElement("like");
		fvNominalVal.addElement("thanks");
		fvNominalVal.addElement("thinkso");
		Attribute attribute1 = new Attribute("class", fvNominalVal);
		Attribute attribute2 = new Attribute("text", (FastVector) null);
		// Create list of instances with one element
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(attribute2);
		instances = new Instances("Test relation", fvWekaAttributes, 1);
		// Set class index
		instances.setClassIndex(0);
		// Create and add the instance

		Instance instance = new Instance(2);

		// DenseInstance instance = new DenseInstance(2);

		instance.setValue(attribute2, text);
		// Another way to do it:
		// instance.setValue((Attribute)fvWekaAttributes.elementAt(1), text);
		instances.add(instance);
		System.out
				.println("===== Instance created with reference dataset =====");
		System.out.println(instances);
	}

	/**
	 * This method performs the classification of the instance. 
	 */
	public void classify() {
		try {
			double pred = classifier.classifyInstance(instances.instance(0));
			System.out.println("===== Classified instance =====");
			System.out.println("Class predicted: "+ instances.classAttribute().value((int) pred));
			publishData(TEXT_DATA, new StringData(getMessageID(), instances.classAttribute().value((int) pred), LanguageUtils.getLanguageCodeByLocale(Locale.FRANCE)));
		} catch (Exception e) {
			System.out.println("Problem found when classifying the text");
		}
	}
	
	public ClassifierComponent(String outboundPort, ComponentConfig config) {
        super(outboundPort, config);
    }
	
	/**
     * Setting up the component based on input configuration.
     * @param config component configuration.
     */
	protected void setupComponent(ComponentConfig config) {
		classifierFile = config.getFileProperty(PROP_CLASS_PATH, "data", true);
        FileUtils.checkReadableFile(classifierFile, true);
        System.out.println("\nclassifierPath: "+classifierFile);
    }
	
	/**
     * Checking type of output data.
     */
	@Override
	public void definePublishedData() {
		addOutboundTypeChecker(TEXT_DATA, StringData.class);
		addOutboundTypeChecker(CLASSIFIERSIGNAL_DATA, StringData.class);
	}

	/**
     * Checking type of input data 
     */
	@Override
	public void defineReceivedData() {
		addInboundTypeChecker(StringData.class);
		addInboundTypeChecker(PlayerEvent.class);
	}

	/**
     * Managing input and output data in the class.
     * @param data input data
     */
	@Override
	protected void handleData(GenericData data) {
		System.out.println("ZJ Classsifier: GenericData: "+data);
		if (data instanceof StringData && !AgentSpeaking) {
		//if (data instanceof StringData) {
			System.out.println("Classification is Started!");
			publishData(CLASSIFIERSIGNAL_DATA, new StringData(getMessageID(), "ClassificationStarted", LanguageUtils.getLanguageCodeByLocale(Locale.FRANCE)));
			text =  "";
			text = text + " " + ((StringData) data).getData();
			text = text.toLowerCase();
			loadModel(classifierFile.getAbsolutePath());
			makeInstance();
			classify();
			publishData(CLASSIFIERSIGNAL_DATA, new StringData(getMessageID(), "ClassificationEnded", LanguageUtils.getLanguageCodeByLocale(Locale.FRANCE)));
			System.out.println("Classification is Ended!");
        }
		if (data instanceof PlayerEvent) {
            PlayerEvent event = (PlayerEvent) data;

            // stop classifying when MARC is speaking, and restart it when appropriate
            AgentSpeaking = event.getEvent() == PlayerEvent.EVENT_START;// ? "stop" : "start";
            System.out.println("ZJ Classsifier: AgentSpeaking: "+AgentSpeaking);
           // webSocketBroadcaster.broadcast(clients, message);
        }
	}
	
	/**
	 * Gets message ID
	 * @return message ID
	 */
	private long getMessageID() {
        messageID++;
        if (messageID > Long.MAX_VALUE - 2) {
            messageID = 0;
        }
        return messageID;
    }

}