/*
 * MORuleFunction.java
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


import myra.Dataset;
import myra.rule.Rule;
import myra.rule.RuleFunction;
import myra.rule.RuleList;
import myra.rule.pittsburgh.monotonicity.MIRuleFunction;

/**
 * @author jb765
 *
 */
public class MORuleFunction extends RuleFunction {

	private RuleFunction[] functionList;
	
	/**
	 * 
	 * @param functionList
	 */
	public MORuleFunction(RuleFunction[] functionList) {
		this.functionList = functionList;
	}
	
	/* (non-Javadoc)
	 * @see myra.rule.RuleFunction#evaluate(myra.rule.Rule)
	 */
	@Override
	public double evaluate(Rule rule) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double evaluate(Rule rule, double[] weightList) {
		if(functionList.length != weightList.length) {
			throw new IllegalArgumentException("MORuleFunction::evaluate - FunctionList length differs from WeightList length");
		}
		double quality = 0;
		for(int i = 0; i < functionList.length; i++) {
			quality += functionList[i].evaluate(rule)*weightList[i];
		}
		return quality;
	}
	
	public RuleFunction[] getFunctions() {
		return functionList;
	}
	
	public void setList(RuleList list) {
		for(int i = 0; i < functionList.length; i++) {
			if(functionList[i] instanceof MIRuleFunction ) {
				MIRuleFunction r = (MIRuleFunction) functionList[i];
				r.setList(list);
			}
		}
	}
	
	public void setDataset(Dataset dataset) {
		for(int i = 0; i < functionList.length; i++) {
			if(functionList[i] instanceof MIRuleFunction ) {
				MIRuleFunction r = (MIRuleFunction) functionList[i];
				r.setDataset(dataset);
			}
		}
	}
}
