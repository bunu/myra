/*
 * MOListFunction.java
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

import static myra.Config.CONFIG;
import static myra.rule.pittsburgh.MO.WeightFactory.WEIGHT_FACTORY;

import myra.Dataset;
import myra.rule.ListMeasure;
import myra.rule.RuleList;

/**
 * @author jb765
 *
 */
public class MOListMeasure implements ListMeasure {

	private ListMeasure[] measures;
	
	/**
	 * 
	 * @param measures
	 */
	public MOListMeasure(ListMeasure[] measures) {
		this.measures = measures;
	}
	
	
	@Override
	public double evaluate(Dataset dataset, RuleList list) {
		double[] weights = CONFIG.get(WEIGHT_FACTORY).getCurrentAntWeight();
		double quality = 0;
		for(int i = 0; i < weights.length; i++) {
			quality += weights[i]*measures[i].evaluate(dataset, list);
		}
		return quality;
	}
	
	/**
	 * returns the list of measures being used by the <code>MOListMeasure </code>
	 * @return
	 */
	public ListMeasure[] getListMeasures() {
		return measures;
	}
}
