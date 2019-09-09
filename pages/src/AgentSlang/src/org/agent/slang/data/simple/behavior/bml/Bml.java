package org.agent.slang.data.simple.behavior.bml;

import java.util.Vector;

//import marc.environment.CreateSound;
import org.agent.slang.data.simple.marc.enviornment.LoadImage;

//import marc.environment.StartSound;

/**
 * This class will provide output BML based on different XML items received from input.
 * OS Compatibility: Windows and Linux
 */

public class Bml 
{
public int id;
String bml_content="";

public int last_id_fork=0;
public int last_id_item=0;
public String last_id_item_str;
public Vector<String> all_id_item_str=new Vector ();
private BmlFork last_bmlfork;
public Bml(int Id)
{
	id=Id;
}

/**
 * Put the start element of a BML command 
 */
public void StartBmlWriter()
{
//	bml_content="<bml id=\"" + "Track"+ "\">"+"\n";
	bml_content="<bml id=\"" +"trackbml"+ "\">"+"\n";
}

/**
 * Add fork to a BML command.
 * @param bmlfork BML fork to be added
 */
public void AddFork(BmlFork bmlfork)
{
	bmlfork.SetId(last_id_fork+1);
	bml_content=bml_content+bmlfork.StartBmlForkCommand();
	bml_content=bml_content+bmlfork.GetWaitCommand();
	last_bmlfork=bmlfork;
	last_id_fork=last_id_fork+1;
}
/**
 * add a load image element to a BML command 
 * @param filename image file name
 * @param path image path
 */
public void AddLoadImage(String filename, String path)
{
	//System.out.println("\n\n BML IMAGE Directory: ############"+path);
	LoadImage loadImage=new LoadImage(filename, path);
	bml_content=bml_content+loadImage.loadImageWriter();
	last_id_item=last_id_item+1;
	all_id_item_str.add("bml_load_image");

}
/**
 * add a load image element to a BML command 
 * @param filename image file name
 */
public void AddLoadImage(String filename)
{
	LoadImage loadImage=new LoadImage(filename);
	bml_content=bml_content+loadImage.loadImageWriter();
	last_id_item=last_id_item+1;
	all_id_item_str.add("bml_load_image");

}
/**
 * add a speech element to a BML command 
 * @param speech speech content to be added
 */
public void AddSpeech(Speech speech)
{
	speech.SetId(last_id_item+1);
	bml_content=bml_content+speech.SpeechWriter();
	last_id_item=last_id_item+1;
	last_id_item_str=speech.getIdStr();
	all_id_item_str.add(speech.getIdStr());
	//System.out.println("+++++++++++++++++++++++++  "+last_id_item_str);
}
/**
 * add a posture element to a BML command 
 * @param posture posture content to be added
 */
public void addPosture(Posture posture)
{posture.setId(last_id_item+1);
	bml_content=bml_content+posture.postureWriter();
	last_id_item=last_id_item+1;
	last_id_item_str=posture.getIdStr();
	all_id_item_str.add(posture.getIdStr()+"_"+posture.getGestureName());
}

/**
 * reset the posture to a predefined posture in MARC toolkit 
 */
public void resetPosture() {
	// write bml content
	addPosture(new Posture("marc","Rest Pose","Rest Pose","bml_resetposture_item"));
		
	}

/**
 * add a face element to a BML command 
 * @param face face content to be added
 */
public void AddFace(Face face)
{
	face.SetId(last_id_item+1);
	bml_content=bml_content+face.FaceWriter();
	last_id_item=last_id_item+1;
	last_id_item_str=face.GetIdStr();
	all_id_item_str.add(face.GetIdStr());
	//System.out.println("**********************  "+ last_id_item_str);
}
/**
 * Ends a BML fork.
 */
public void EndFork()
{
	bml_content=bml_content+last_bmlfork.EndBmlForkCommand();
}
/**
 * Ends a BML command.
 */
public void EndBmlWriter()
{
	bml_content=bml_content+"</bml>";
}
/**
 * Getting the BML current content.
 * @return BML current content
 */
public String GetBmlContent()
{
	return bml_content;
}

/**
 * Appends a BML string the current BML content. 
 * @param bmlString BML string to be added
 */
public void AppendBmlContent(String bmlString)
{
	bml_content += bmlString;
}

}