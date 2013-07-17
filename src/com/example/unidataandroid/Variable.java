package com.example.unidataandroid;

public class Variable {
	private String name, description, units;
	
	public Variable(String name, String description, String units)
	{
		name = this.name;
		description = this.description;
		units = this.units;
	}
	
	public String getName()	{
		return name;
	}
	
	public void setName(String name){
		name = this.name;
	}
	
	public String getDescription()	{
		return description;
	}
	
	public void setDescription(String description){
		description = this.description;
	}
	
	public String getUnits()	{
		return units;
	}
	
	public void setUnits(String units){
		units = this.units;
	}
}
