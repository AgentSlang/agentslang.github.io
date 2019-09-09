package org.agent.slang.data.simple.behavior.bml;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

//import com.srresearch.eyelink.Tracker; // LIMSI
//import testeyetracker.Tracker;

import org.agent.slang.data.simple.behavior.bml.Bml;
import org.agent.slang.data.simple.behavior.bml.BmlFork;
import org.agent.slang.data.simple.behavior.bml.Speech;

//import marc.environment.CreateSound;
//import marc.environment.StartSound;
/**
 * This class will provide system to manage playing speech element of BML commands.
 * OS Compatibility: Windows and Linux
 */
public class SpeechPlayer 
{
	/*public MarcCommunication marccommunication;
	private WaitFeedback waitfeedback;
	*/private Speech speech; 
	//private Tracker tracker=null;
/*	public SpeechPlayer(MarcCommunication marccommunication,WaitFeedback waitfeedback, Tracker tracker)
	{
		this.marccommunication=marccommunication;
		this.waitfeedback=waitfeedback;
		this.tracker=tracker;
	}
	public SpeechPlayer(MarcCommunication marccommunication,Tracker tracker)
	{
		this.marccommunication=marccommunication;
		this.tracker=tracker;
		waitfeedback=new WaitFeedback(this.marccommunication,tracker);
	}
*/
	
	public SpeechPlayer()
	{
	}

	/**
	 * Getting speech
	 * @return speech
	 */
	public Speech getSpeech()
	{
		return speech;
	}
	/**
	 * adding playing elements of speech to BML command 
	 * @param bml BML command
	 * @param speechTxt Text of Speech
	 * @param wavFilename name of wave file to play
	 * @param waitSec duration of speech in Seconds
	 * @param TTS_wav name of wave file provided by Text to Speech component
	 * @return BML command
	 */
	public Bml addPlaySpeech(Bml bml,String speechTxt, String wavFilename,double waitSec,String TTS_wav) 	{
		//try{
		bml.AddFork(new BmlFork("marc",0,(float)waitSec));
		switch(TTS_wav)
		{case "tts":// Text to Speech
			bml.AddSpeech(new Speech("marc","tts",speechTxt,wavFilename,"bml_TTS"));
			//LastItemStr="bml_TTS";
			break;
		 case "wav":// Speech from voice recorded in a wav file
			bml.AddSpeech(new Speech("marc","wav",speechTxt,wavFilename,"bml_Wav"));
			//LastItemStr="bml_Wav";
			break;// Speech from tts but sound from environment
		 case "tts_wav":
			 // load wav files during the initialization of MARC environment: file NarecaEnvironmentIni.bml  
			// // // bml.AddCreateSound(new CreateSound(wavFilename,wavFilename));
			speech=new Speech("marc","tts_wav",speechTxt,wavFilename,"bml_TTS_Wav");
			bml.AddSpeech(speech);
			// 08/03: there is always a delay between the lips movement and the sound play: try to introduce that delay  in bml stream
			bml.EndFork();
			bml.AddFork(new BmlFork("marc",0,(float)waitSec+0.1f));
			//bml.AddStartSound(new StartSound(wavFilename,"bml_StartSound"));
			//LastItemStr="bml_TTS_Wav";
			break;
		}
		bml.EndFork();
		//}//catch (IOException e){}
		//catch(UnsupportedAudioFileException e){}
		//catch( LineUnavailableException e){}
		return bml;
	}

}
