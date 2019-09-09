package org.agent.slang.data.simple.behavior.bml;
/**
 * This class will provide system to manage face element of BML commands based on FACS.
 * OS Compatibility: Windows and Linux
 */
public class Face 
{
	private String agent;
	private int id;
	private String idString="";
	private String au;
	private String type="FACS"; 
	private String side="BOTH";
	private float amount;
	private float interpolate=1;
	private String interpolation_type="linear";
	
	public Face(String agent, String activeAU,float amount)
	{
		// ActiveAU = int from 1 to .., or "All"
		this.agent=agent;
		au=activeAU;
		this.amount=amount;
	}
	public Face(String agent, String activeAU,float amount,float interpolate, String side)
	{
		// ActiveAU = int from 1 to .., or "All"
		this.agent=agent;
		au=activeAU;
		this.amount=amount;
		this.interpolate=interpolate;
		this.side=side;
	}
	public Face(String agent, String activeAU,String idStr,float amount)
	{
		// ActiveAU = int from 1 to .., or "All"
		this.agent=agent;
		au=activeAU;
		this.amount=amount;
		this.idString=idStr;
	}
	
	/**
	 * Setting Face ID of a BML command
	 * @param idFace Face ID
	 */
	public void SetId(int idFace)
	{
		id	= idFace;
	}
	
	/**
	 * Getting string ID of a BML command
	 * @return string BML ID
	 */
	public String GetIdStr()
	{
		String bml_id="bml_item_" +id +"_"+ "au"+au;
		if (au.equalsIgnoreCase("All"))
		{	if (!idString.isEmpty())
			{bml_id=idString;}
		else
			{bml_id="bml_item_" +id;}
		}
		return bml_id;
	}
	/**
	 * Generate final face element of a BML command
	 * @return BML face command
	 */
	public String FaceWriter()
	{
	String FaceBml="";
	String bml_au=au;
	String bml_amount=Float.toString(amount);
	String bml_id="bml_item_" +id +"_"+ "au"+au;
	String bml_interpolation_type=agent+":"+"interpolation_type=\"" + interpolation_type + "\""+ "\n";
	if (au.equalsIgnoreCase("All"))
	{
		bml_au=agent+":"+au;
		bml_id="bml_item_" +id;
		if (!idString.isEmpty())
		{bml_id=idString;}
		bml_interpolation_type="";
		bml_amount="0";
	}
	FaceBml="<face"+ "\n"+
	"id=\"" + bml_id+"\""+ "\n"+
	"type=\"" + type + "\""+ "\n"+
	"side=\"" + side + "\""+ "\n"+
	"amount=\"" + bml_amount + "\""+ "\n"+
	"au=\"" + bml_au + "\""+ "\n"+
	agent+":"+"interpolate=\"" + interpolate + "\""+ "\n"+
	//bml_interpolation_type+
	"/>"+ "\n";
		return FaceBml;
	}
}
