/*
 * MonotonicBacktrackPruner.java
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
public class MonotonicBacktrackPruner extends MonotonicPruner {

	@Override
	public void prune(Dataset dataset, RuleList list) {		
		
		int termsRemoved = 0;
		
		System.out.println("Pre-prune stats:\nRules: " + list.size());
				
		while(MonotonicFunctions.calculateMonotonicityIndex(list, dataset) < 1.0) {
			
			if(list.rules().length > 0) {
				Rule r = list.rules()[list.rules().length -1];
				if(r.terms().length > 0) {
						r.terms()[r.terms().length-1].setEnabeld(false);
						r.compact();
						termsRemoved++;
				} else {
					list.remove(list.rules().length -1);
				}
			} else {
				break;
			}
		}				
		// Tidy up the rule after pruning by adding the default rule
		fixPrunedList(dataset, list);
		
		System.out.println("Post-prune stats:\nRules: " + list.size() + "\nTerms Removed: " + termsRemoved);
	}
}
