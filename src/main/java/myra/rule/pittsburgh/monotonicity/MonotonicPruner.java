/*
 * MonotonicityPruner.java
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


import static myra.Config.CONFIG;
import static myra.Dataset.NOT_COVERED;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import myra.Dataset;
import myra.Config.ConfigKey;
import myra.Dataset.Instance;
import myra.rule.Rule;
import myra.rule.RuleList;

/**
 * @author jb765
 *
 */
public abstract class MonotonicPruner {
	
	public final static ConfigKey<MonotonicPruner> MONOTONIC_PRUNER = new ConfigKey<>();
	
	public abstract void prune(Dataset dataset, RuleList list);
	
	/**
	 * Method tidies up a list after pruning ensuring the default rule is 
	 * correctly applied to the end of the list
	 * @param dataset
	 * @param list
	 */
	protected void fixPrunedList(Dataset dataset, RuleList list) {
		// Tidy up the rule after pruning by adding the default rule
		if (!list.hasDefault()) {
			Instance[] instances = Instance.newArray(dataset.size());
			for(Rule r : list.rules()) {
				r.apply(dataset, instances);
			}
			
			boolean available = false;
			
			for(Instance i : instances) {
				if(i.flag == NOT_COVERED) {
					available = true;
					break;
				}
			}
			
		    if (available == false) {
		    	Instance.markAll(instances, NOT_COVERED);
		    }

		    Rule rule = new Rule();
		    rule.apply(dataset, instances);
		    CONFIG.get(ASSIGNATOR).assign(rule);
		    list.add(rule);
		}
		list.setQuality(CONFIG.get(DEFAULT_MEASURE).evaluate(dataset, list));
	}
}
