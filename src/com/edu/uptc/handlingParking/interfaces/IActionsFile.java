package com.edu.uptc.handlingParking.interfaces;


import com.edu.uptc.handlingParking.enums.*;

public interface IActionsFile {
	
	public void loadFile(ETypeFileEnum eTypeFileEnum);
    public void dumpFile(ETypeFileEnum eTypeFileEnum);

}
