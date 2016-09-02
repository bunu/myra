/*
 * NMIPruner.java
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

package myra.rule.pittsburgh.monotonicity;

import myra.Dataset;
import myra.rule.Rule;
import myra.rule.RuleList;

/**
 * @author James Brookhouse
 *
 */
public class NMIPruner extends MonotonicPruner 
{
	/* (non-Javadoc)
	 * @see myra.rule.pittsburgh.monotonicity.MonotonicPruner#prune(myra.Dataset, myra.rule.RuleList)
	 */
	@Override
	public void prune(Dataset dataset, RuleList list) 
	{
		System.out.println("Pre-prune stats:\nRules: " + list.size());
		int termsRemoved = 0;
		
		while(MonotonicFunctions.calculateMonotonicityIndex(list, dataset) < 1.0) {
			
			Rule[] rules = list.rules();
			Rule worstRule = null;
			double worstMI = 0;
			int worstIndex = -1;			
			
			for(int i = 0; i < rules.length; i++) {
				double MI = MonotonicFunctions.calculateRuleMonotonicity(list, rules[i], dataset);			
				if(MI <= worstMI) {
					worstRule = rules[i];
					worstMI = MI;
					worstIndex = i; 
				}
			}
			if(worstRule != null) {
				termsRemoved++;
				worstRule.terms()[worstRule.terms().length-1].setEnabeld(false);
				worstRule.compact();
				if(worstRule.terms().length == 0) {
					// TODO
					// Not sure if this is the best thing to do, have two options remove the 
					// worstRule that now has 0 terms or remove all the rules below it and 
					// concatenate the list. Currently we do the former
					list.remove(worstIndex);
				}
			}
			else {
				System.err.println("NMI of theory was non zero however no violating rule was found, exiting pruning");
				return;
			}		
		}		
		// Tidy up the rule after pruning by adding the default rule
		fixPrunedList(dataset,list);
		
		System.out.println("Post-prune stats:\nRules: " + list.size() + "\nTerms Removed: " + termsRemoved);
	}
}
