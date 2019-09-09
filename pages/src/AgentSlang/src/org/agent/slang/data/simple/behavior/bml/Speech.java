package org.agent.slang.data.simple.behavior.bml;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
/**
 * This class will provide system to manage Speech element of BML commands.
 * OS Compatibility: Windows and Linux
 */

public class Speech 
{ // two alternatives: 1) TTS (JSAPI), 2) from wav file
private String agent;
private String voice="Microsoft Julie Mobile"; // Microsoft Julie Mobile
private String synthesizer="JSAPI";
private float speed= 1.0f;
private float volume=1;
private double articulate= 1;
private String speechType;

private int id;
private String text="";
//private String wavFileDir="C:\\Program Files\\MARC-toolkit\\14.1.0\\data\\MARC\\Environments\\Meeting Room\\NarecaInitialization\\AudioNarration\\";
private String wavFileDir="";
private String wavFileName;
private String idString="";
public double durationWavSeconds=0.0;


public Speech(String CurrentAgent,String TTS_Wav,String text,String wavFileName, String idStr) {
	agent=CurrentAgent;
	speechType=TTS_Wav;
	switch (TTS_Wav)
	{
	case "tts":
		this.text=text;
		break;
	case "wav":
		this.wavFileName=wavFileName;
		this.articulate=0.7; 
		break;
	case "tts_wav":
		this.volume=1.0f;
		this.text=text;
		this.wavFileName=wavFileName;
		this.articulate=0.4; 
		
		// Get duration of audio data in seconds
	//	String wholeFileName=wavFileDir+wavFileName+".wav";
	//	File fileIn = new File(wholeFileName);
		File fileIn = new File(wavFileName);
	//	AudioInputStream audioStream = AudioSystem.getAudioInputStream(fileIn);
		//audioStream = AudioSystem.getAudioInputStream(getClass().getResource(wavFileDir+wavFileName+".wav"));
	//	 AudioFormat audioFormat = audioStream.getFormat();
	//     DataLine.Info info = new DataLine.Info(  Clip.class, audioStream.getFormat(), ((int) audioStream.getFrameLength() * audioFormat.getFrameSize()));
	     long audioFileLength = fileIn.length();
	  //   durationWavSeconds = audioFileLength / (audioFormat.getFrameSize() * audioFormat.getFrameRate());   // where frameSize -- Number of bytes in each, frameRate -- Number of frames per second frame 
	    // durationWavSeconds = audioStream.getFrameLength() / audioStream.getFormat().getFrameRate();
	    System.out.println(" Read file " +wavFileName+ "   duration = "  +durationWavSeconds);
		break;
	}
	this.idString=idStr;
}

/**
 * Reset the volume of speech to zero
 */
public void resetVolume()
{
	this.volume=0;
}
/**
 * getting the BML ID
 * @return BML item ID
 */
public String getIdStr()
{
	if (idString.isEmpty())
	{String bml_id="bml_item_" +id;
	return bml_id;}
	else
	{return idString;}
}

/**
 * Setting speech ID
 * @param IdSpeech Speech ID
 */
public void SetId(int IdSpeech)
{
	id	= IdSpeech;
}

/**
 * Generate final speech element of a BML command
 * @return BML speech command
 */
public String SpeechWriter()

{	String bml_id;
	if (idString.isEmpty())
	{bml_id="bml_item_" +id;}
	else
	{bml_id=idString;}
	
	String SpeechBml="<speech"+ "\n"+
	"id=\"" + bml_id + "\""+ "\n"+
	agent+":"+"volume=\"" + volume + "\""+ "\n"+
	agent+":"+"articulate=\"" + articulate + "\""+ "\n";
	//System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%% "+text + text.isEmpty());
	if(!speechType.equalsIgnoreCase("wav")) // if speech condition is TTS or TTS_Wav
	{
		SpeechBml=SpeechBml+ agent+":"+"voice=\"" + voice + "\""+ "\n"+
				agent+":"+"synthesizer=\"" + synthesizer + "\""+ "\n"+
				agent+":"+"speed=\"" + speed + "\""+ "\n"+
				"text=\"" + text + "\""+ "\n";	
	}
	else
	{
	SpeechBml=SpeechBml+ agent+":"+"file=\""+wavFileDir+wavFileName+".wav\"" + "\n";
	}
	SpeechBml=SpeechBml+"/>"+ "\n";
	
	
	
	return SpeechBml;
}

/**
 * Getting the voice of speech
 * @return voice of speech in string format
 */
public String GetVoice()
{
	return voice;
}

/**
 * Getting the Synthesizer of speech
 * @return Synthesizer of speech in String format
 */
public String GetSynthesizer()
{
	return synthesizer;
}

/**
 * Getting the Speed of speech
 * @return Speed of speech in String format
 */
public float GetSpeed()
{
	return speed;
}

/**
 * Getting the Volume of speech
 * @return Volume of speech in String format 
 */
public float GetVolume()
{
	return volume;
}

/**
 * Getting the articulatation of speech
 * @return articulatation of speech in String format
 */
public double GetArticulate()
{
	return articulate;
}

/**
 * Getting the ID of speech
 * @return ID of speech
 */
public int GetId()
{
	return id;
}

/**
 * Getting the text of speech
 * @return text of speech
 */
public String GetText()
{
	return text;
}




}
