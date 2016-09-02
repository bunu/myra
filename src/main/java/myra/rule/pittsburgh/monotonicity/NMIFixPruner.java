/*
 * NMIFixPruner.java
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
public class NMIFixPruner extends MonotonicPruner {

	/* (non-Javadoc)
	 * @see myra.rule.pittsburgh.monotonicity.MonotonicPruner#prune(myra.Dataset, myra.rule.RuleList)
	 */
	@Override
	public void prune(Dataset dataset, RuleList list) {
		
		int termsRemoved = 0;
		
		System.out.println("Pre-prune stats:\nRules: " + list.size());
				
		while(MonotonicFunctions.calculateMonotonicityIndex(list, dataset) < 1.0) {
			Rule[] rules = list.rules();
			double bestNMIReduction = 0;
			int termsToRemove = 0;
			int ruleIndex = -1;
			
			double nmiBefore = MonotonicFunctions.calculateMonotonicityIndex(list, dataset);
			
			for(int i = 0; i < rules.length; i++)
			{
				if(MonotonicFunctions.calculateRuleMonotonicity(list, rules[i], dataset) < 1.0) {
					int termNo = 0;
					do {
						termNo++;
						rules[i].terms()[rules[i].terms().length-termNo].setEnabeld(false);
					}
					while(MonotonicFunctions.calculateMonotonicityIndex(list, dataset) == nmiBefore);
				
					// If this reduction is greater or equal to the previous best reduction the we select it as 
					// the new candidate and save the number of terms we would need to remove
					double nmiReduction = nmiBefore - MonotonicFunctions.calculateMonotonicityIndex(list, dataset);
					if(nmiReduction >= bestNMIReduction) {
						bestNMIReduction = nmiReduction;
						termsToRemove = termNo;
						ruleIndex = i;
					}
					
					// Enable all the terms so we get back to the original rule list
					for(int j = 0; j >= rules[i].terms().length; j++) {
						rules[j].terms()[j].setEnabeld(true);
					}
				}
			}
			if(ruleIndex >= 0) {
				for(int i = 1; i <= termsToRemove; i++) {
					termsRemoved++;
					rules[ruleIndex].terms()[rules[ruleIndex].terms().length - i].setEnabeld(false);
					rules[ruleIndex].compact();
				}
				
				if(rules[ruleIndex].terms().length == 0) {
					// TODO
					// Not sure if this is the best thing to do, have two options remove the 
					// worstRule that now has 0 terms or remove all the rules below it and 
					// concatenate the list. Currently we do the former
					list.remove(ruleIndex);
				}
			}
			else {
				System.err.println("NMI of theory was non zero however no violating rule was found, exiting pruning");
				return ;
			}		
		}
		// Tidy up the rule after pruning by adding the default rule
		fixPrunedList(dataset, list);
		
		System.out.println("Post-prune stats:\nRules: " + list.size() + "\nTerms Removed: " + termsRemoved);
	}
}
