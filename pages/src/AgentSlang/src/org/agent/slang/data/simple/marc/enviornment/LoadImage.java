package org.agent.slang.data.simple.marc.enviornment;

/**
 * This class is used in order to put an image as a slide inside MARC Toolkit environment.
 * OS Compatibility: Windows and Linux
 */
public class LoadImage 
{
	String ImageFileName;
	String fileDir="C:\\Program Files\\MARC-toolkit\\14.1.0\\data\\MARC\\Environments\\Meeting Room\\NarecaInitialization\\ImagesNarration\\";
	String name="nareca_screen";// Graphical object in the environment. The image will be loaded according to this graphical object
	String square_adjust="true";
	String emission="1;1;1;1" ;
	String diffuse="0;0;0;1" ; 
	String ambiant="0;0;0;1"; 

	/**
	 * loads an image as a slide in the MARC environment 
	 * @param filename image file name
	 */
	public LoadImage(String filename)
	{
		this.ImageFileName=filename;
	}
	
	/**
	 * loads an image as a slide in the MARC environment 
	 * @param filename image file name
	 * @param path image path
	 */
	public LoadImage(String filename,String path)
	{
		this.ImageFileName=filename;
		 fileDir=path;
	}

	/**
	 * Generate final BML command in order to put the image inside MARC environment
	 * @return BML command
	 */
	public String loadImageWriter()
	{
		this.ImageFileName = "slide"+this.ImageFileName.trim()+".png";
		String loadImageBml="<marc:environment>"  +"\n"+
	    "<object_set_material"+ " "+
		"name=\"" + name + "\""+ "  "+
		"emission=\"" + emission + "\""+ " "+
		"diffuse=\"" + diffuse + "\""+ " "+
		"ambiant=\"" + ambiant + "\""+ " "+
		"/>" +"\n"+
		"<object_set_texture" +" "+
		"name=\"" + name + "\""+ "  "+
		"square_adjust=\"" + square_adjust +"\""+ "  "+
		"texture=\"" + fileDir + ImageFileName + "\""+ "  "+
		"/>" +"</marc:environment>" +"\n";
			return loadImageBml;
		
	}
	
	/**
	 * Generate final BML command in order to put the image inside MARC environment
	 * @param filename image file name
	 * @return BML command
	 */
	public String loadImageWriter(String filename)
	{  
		this.ImageFileName = "slide"+filename.trim()+".png";;

		String loadImageBml="<marc:environment>"  +"\n"+
		
	    "<object_set_material"+ " "+
		"name=\"" + name + "\""+ "  "+
		"emission=\"" + emission + "\""+ " "+
		"diffuse=\"" + diffuse + "\""+ " "+
		"ambiant=\"" + ambiant + "\""+ " "+
		"/>" +"\n"+
		"<object_set_texture" +" "+
		"name=\"" + name + "\""+ "  "+
		"square_adjust=\"" + square_adjust +"\""+ "  "+
		"texture=\"" + fileDir + ImageFileName + "\""+ "  "+
		"/>" +"</marc:environment>" +"\n";
		return loadImageBml;
		
	}
}
