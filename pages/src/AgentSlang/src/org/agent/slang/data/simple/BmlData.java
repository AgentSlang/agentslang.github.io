package org.agent.slang.data.simple;

import org.ib.data.GenericData;
import org.ib.data.StringData;
import org.ib.data.TypeIdentification;

/**
 * A data type to store BML data characteristics.
 * OS Compatibility: Windows and Linux
 * @author Julien 
 * @version 14/01/2016.
 */
@TypeIdentification(typeID = 42)
public class BmlData implements GenericData{

    private long id;
    private String bmlType;
    private String data;
    private String gesture;
    private String slide;
    private String textData;
    private String emotionalAppraisal;
    private String audioFileName;
    private String intention;
    public BmlData() {
    }
    public BmlData(long id) {
    	this.id = id;
    	data = "";
    	gesture = "";
    	slide = "";
    	textData = "";
    	emotionalAppraisal = "";
    	audioFileName = "";
    	intention = "";
    }

    public BmlData(long id, String bmlType, String data) {
        this.id = id;
        this.bmlType = bmlType;
        this.data = data;
        this.textData = data;    
        gesture = "";
    	slide = "";
    	emotionalAppraisal = "";
    	audioFileName = "";
    	intention = "";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public String getBmlType() {
        return bmlType;
    }

    public void setBmlType(String bmlType) {
        this.bmlType = bmlType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
        this.textData = data;
    }
    public void setSlide(String slide) {
        this.slide = slide;
    }  
    public void setGesture(String gesture) {
        this.gesture = gesture;
    }
    public String getSlide() {
      return slide;
    }
    public String getGesture() {
        return gesture;
      }
    
    public void setEmotionalAppraisal(String appraisal) {
        this.emotionalAppraisal = appraisal;
    }
    public String getEmotionalAppraisal() {
      return emotionalAppraisal;
    }
    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }
    public String getAudioFileName() {
      return audioFileName;
    }
    public String getIntention() {
        return this.intention;
      }
      public void setIntention(String intention) {
          this.intention = intention;
        }
      }
