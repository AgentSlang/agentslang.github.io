package org.agent.slang.data.simple.behavior.bml;

/**
 * This class will provide system to manage BML forks.
 * OS Compatibility: Windows and Linux
 */
public class BmlFork 
{
private String agent="marc";
private long bml_id;
private int fork_id;
private String event="";
private float  duration;
private String WaitType="";

public BmlFork(String Agent,long BmlId)
{
	agent=Agent;
	bml_id=BmlId;
}
public BmlFork(String Agent,long BmlId,float DurationInSec)
{
	agent=Agent;
	bml_id=BmlId;
	// wait for few seconds
	duration=DurationInSec;
	WaitType="duration";
}

public BmlFork(String Agent,long BmlId,String SecondEventIdStr, String StardOrEnd)
{
	agent=Agent;
	bml_id=BmlId;
	// wait for the start/end of another event
	event=SecondEventIdStr+":"+StardOrEnd;
	//"bml_item_"+SecondEventId+":"+StardOrEnd;
	WaitType="event";
}
/**
 * Setting BML fork ID
 * @param IdFork BML fork ID
 */
public void SetId(int IdFork)
{
	fork_id	= IdFork;
}

/**
 * Getting wait item for a BML command
 * @return BML fork string
 */
public String GetWaitCommand()
{
	String BmlForkString="";
	if (!WaitType.isEmpty())
	{if (WaitType.equalsIgnoreCase("duration"))
	{BmlForkString="<wait" + " " + WaitType + "= \""+duration +"\" />"+"\n";	
	}
	else // if (WaitType.equalsIgnoreCase("event"))
	{
	BmlForkString="<wait" + " " + WaitType + "= \""+event +"\" />"+"\n";		
	}
	}
	return BmlForkString;
}
/**
 * Starting a BML fork command 
 * @return Start of BML fork command element
 */
public String StartBmlForkCommand()
{
	String bmlforkcommand="";
	bmlforkcommand="<"+agent +":fork id=\"" +"Track_"+bml_id+"_fork_"+fork_id+"\">"+"\n";
	return bmlforkcommand;
}
/**
 * Ending a BML fork command 
 * @return End of BML fork command element
 */
public String EndBmlForkCommand()
{
	String bmlforkcommand="";
	bmlforkcommand="</"+agent+":fork>"+"\n";
	return bmlforkcommand;
}
}

