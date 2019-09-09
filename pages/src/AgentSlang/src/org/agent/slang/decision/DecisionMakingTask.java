package org.agent.slang.decision;

import java.io.File;
import java.util.Iterator;

import org.agent.slang.data.annotation.GenericTextAnnotation;
import org.agent.slang.data.audio.PlayerEvent;
import org.agent.slang.data.simple.BmlData;
import org.agent.slang.dm.narrative.data.StateChangeData;
import org.agent.slang.dm.narrative.data.commands.CommandData;
import org.ib.data.StringData;

import actr.model.Model;
import actr.task.*;

/**
 * This class provides task related information of decision making process based on ACT-R cognitive architecture.
 * OS Compatibility: Windows, Linux version has not been tested
 * @author Sahba Zojaji, sahba.zojaji@insa-rouen.fr
 * @version 1, 20/05/2017
 */

public class DecisionMakingTask  extends Task{
	double actv = 0;
	
	public DecisionMakingTask ()  
	{
	}
	
	/**
	 * starts the task
	 */
    @Override
    public void start() {
    	System.out.println("start\n");   	
    }

    /**
     * updates the task based on predefined time.
     */
    @Override
    public void update(double time) {
                    
    }
    
    /**
     * creates and runs an ACT-R model
     * @param actrModel path of ACT-R model
     * @param moduleID module ID
     */
    public void create(File actrModel, int moduleID)
    {
    	System.out.println("Compiling...");
        Model model = Model.compile(actrModel, null);
        System.out.println("Running...");
        
        //Activate TextComponent
      	if (moduleID == 1)
      	{
      		model.runCommand("(goal-focus active-TextComponent-goal)");
      	}
      	//Activate SennaComponent
      	else if (moduleID == 2)
  		{
  			model.runCommand("(goal-focus active-SennaComponent-goal)");
  		}
  		//Activate StoryGraphComponent
      	else if (moduleID == 3) 
      	{
  			model.runCommand("(goal-focus active-StoryGraphComponent-goal)");
  		}
      	//Activate MarcBMLTranslationComponent
      	else if (moduleID == 4) 
      	{
  			model.runCommand("(goal-focus active-MarcBMLTranslationComponent-goal)");
  		}
      	//Activate PatternMatchingComponent
      	else if (moduleID == 5) 
      	{
  			model.runCommand("(goal-focus active-PatternMatchingComponent-goal)");
  		}
      	//Activate HandleCommandComponent
      	else if (moduleID == 6) 
      	{
  			model.runCommand("(goal-focus active-HandleCommandComponent-goal)");
  		}	      		
        
        model.run();
        
        System.out.println(String.format("Done."));
    }
    
    /**
     * binds some command on top an active model which is running
     */
    @Override
    public double bind (Iterator<String> it)
	{
    	try
		{		
			System.out.println("\nbind .  ");
			it.next(); // (
			String cmd = it.next();
			System.out.println("\ncommand .  "+cmd);
			if (cmd.equals ("activity")) 
				{
					actv = Double.valueOf (it.next());
					
					String message = "\nComponent Activeness:  "+actv;
					System.out.println(message);

					return actv;
				}
			else return 0;

		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
			return 0;
		}
	}
    
    /*
    @Override
    public boolean evalCondition (Iterator<String> it)
	{
		it.next(); 
		String cmd = it.next();
		boolean b = false;
		if (cmd.equals ("is-odd"))
		{
			double num = Double.valueOf (it.next());
			b = isodd(num);
			System.out.println("odd value:  "+num+"  "+b);
		}
		else if (cmd.equals ("is-even"))
		{
			double num = Double.valueOf (it.next());
			b = iseven(num);
			System.out.println("even value:  "+num+"  "+b);
		}
		return b;
	}
    
    boolean isodd (double n)
	{
		if (n==1 || n==3)
			return true;
		else
			return false;
	}
    
    boolean iseven (double n)
	{
		if (n==2 || n==4)
			return true;
		else
			return false;
	}
	*/
}
