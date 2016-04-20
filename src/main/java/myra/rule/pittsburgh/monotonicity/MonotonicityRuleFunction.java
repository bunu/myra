/*
 * MonotonicityFunction.java
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
import static myra.rule.pittsburgh.monotonicity.Constraint.CONSTRAINT_WEIGHTING;

import myra.Dataset;
import myra.rule.Rule;
import myra.rule.RuleFunction;
import myra.rule.RuleList;

/**
 * @author jb765
 *
 */
public class MonotonicityRuleFunction extends RuleFunction {

	private RuleList list;
	private Dataset dataset;
	
	public void setList(RuleList list) {
		this.list = list;
	}
	
	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public double evaluate(Rule rule) {
		BinaryConfusionMatrix m = fill(rule);
		
		double mi = MonotonicFunctions.calculateRuleMonotonicity(list, rule, dataset);

		double value = ((m.TP / (m.TP + m.FN)) * (m.TN / (m.TN + m.FP)))*(1-CONFIG.get(CONSTRAINT_WEIGHTING)) + mi*CONFIG.get(CONSTRAINT_WEIGHTING);

		return Double.isNaN(value) ? 0.0 : value;
	}

}
