/*
 * MORuleList.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2015 Fernando Esteban Barril Otero
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package myra.rule.pittsburgh.MO;

import myra.rule.RuleList;

/**
 * @author jb765
 *
 */
public class MORuleList {
	
	private double[] objectives;
	private RuleList list;
	
	/**
	 * Default MORuleList constructor
	 */
	public MORuleList(RuleList list) {
		objectives = new double[0];
		this.list = list;
	}
	
	/**
	 * 
	 * @return
	 */
	public double[] getObjectivesQuality() {
		return objectives;
	}
	
	/**
	 * 
	 * @param objectives
	 */
	public void setObjectivesQuality(double[] objectives) {
		this.objectives = objectives;
	}
	
	/**
	 * Returns the rule list encapsulated by this MORuleList
	 * @return
	 */
	public RuleList getRuleList() {
		return list;
	}
}
