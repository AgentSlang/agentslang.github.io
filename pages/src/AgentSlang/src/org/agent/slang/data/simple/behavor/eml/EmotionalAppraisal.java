package org.agent.slang.data.simple.behavor.eml;

/**
 * This class will provide output emotional appraisal (based on Emotional Appraisal Theory) in BML command.
 * OS Compatibility: Windows and Linux
 */

public class EmotionalAppraisal 
{

public double expectedness=0.0;
public double unpleasantness=0.0;
public double goal_hindrance=0.0;
public double coping_control=0.0;
public double coping_power=0.0;
//public double external_causation=0;
 public EmotionalAppraisal(double expectedness,double unpleasantness, double goal_hindrance,double coping_control,double coping_power)
{
	this.expectedness=expectedness;
	this.unpleasantness=unpleasantness;
	this.goal_hindrance=goal_hindrance;
	this.coping_control=coping_control;
	this.coping_power=coping_power;
	//this.external_causation=external_causation;
}

 /**
  * Getting appraisal string
  * @return appraisal string
  */
 public String getAppraisalString()
 {
	 return "exp="+expectedness+";"+"unp="+unpleasantness+";"+"gh="+goal_hindrance+";"+"cc="+coping_control+";"+"cp="+coping_power;
 }
// **********  Novelty
 /**
  * Setting Expectedness
  * @param expectedness Expectedness
  */
public void	setExpectedness(double expectedness)
{
	this.expectedness=expectedness;
}
/**
 * Getting Expectedness
 * @return Expectedness
 */
public double	getExpectedness()
{
	return expectedness;
}

// ********** UnPleasantness
/**
 * Setting unpleasantness
 * @param unpleasantness unpleasantness
 */
public void	setUnpleasantness(double unpleasantness)
{
	this.unpleasantness=unpleasantness;
}

/**
 * Getting unpleasantness
 * @return unpleasantness
 */
public double getUnpleasantness()
{
	return this.unpleasantness;
}

// ********* 
/**
 * Getting hindrance goal
 * @return hindrance goal
 */
public double getGoal_hindrance()
{return goal_hindrance;}

//public double getExternal_causation()
//{return external_causation;}

/**
 * Getting coping power
 * @return coping power
 */
public double getCoping_power()
{return coping_power;}

/**
 * Getting Coping control
 * @return Coping control
 */
public double getCoping_control()
{return coping_control;}



}