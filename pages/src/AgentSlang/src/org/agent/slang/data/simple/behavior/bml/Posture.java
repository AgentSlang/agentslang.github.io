package org.agent.slang.data.simple.behavior.bml;

/**
 * This class will provide system to manage Posture element of BML commands.
 * OS Compatibility: Windows and Linux
 */

public class Posture 
{
	private String agent;
	private int id;
	private String idString="";
	private String define_as_rest_pose="false";
	private String transition;//="Rest Pose";
	private String part="WHOLE";// body parts: whole 
	private String stance;// gesture name
	private String facing="_CAMERA";
	private double turn_speed=0.1;//0.89;//0.1
	private boolean loop=false;
	private double blend_duration=1.0;//1.0
	
	public Posture(String agent, String gesture_name, String transition)
	{
		this.agent=agent;
		this.stance=gesture_name;
		this.transition=transition;
	}
	public Posture(String agent, String gesture_name,String transition,String idStr)
	{
		this.agent=agent;
		this.stance=gesture_name;
		this.idString=idStr;
		this.transition=transition;
	}
	/**
	 * Setting Posture ID of a BML command
	 * @param idPosture Posture ID
	 */
	public void setId(int idPosture)
	{
		id	= idPosture;
	}
	
	/**
	 * Getting gesture name of a BML command
	 * @return Gesture name
	 */
	public String getGestureName()
	{
		return stance;
	}
	
	/**
	 * Getting string ID of a BML command
	 * @return string BML ID
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
	 * Generate final posture element of a BML command
	 * @return BML posture command
	 */
	public String postureWriter()
	{
	String PostureBml="";
	String bml_id="bml_item_" +id;
	if (idString.isEmpty())
	{bml_id="bml_item_" +id;}
	else
	{bml_id=idString;}
	PostureBml="<posture"+ "\n"+
	"id=\"" + bml_id+"\""+ "\n"+
	//agent+":defined_as_rest_pose=\""+define_as_rest_pose+"\""+"\n"+
	agent+":transition=\""+transition+"\""+"\n"+
	"part=\""+part+"\""+"\n"+
	"stance=\""+stance+"\""+"\n"+
	"facing=\""+facing+"\""+"\n"+
	agent+":turn_speed=\""+turn_speed+"\""+"\n"+
	agent+":loop=\""+loop+"\""+"\n"+
	agent+":blend_duration=\""+blend_duration+"\""+"\n"+
	"/>"+ "\n";
	return PostureBml;
	}
}
