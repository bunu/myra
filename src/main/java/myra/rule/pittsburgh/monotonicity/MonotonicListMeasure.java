/*
 * MonotonicListFunction.java
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
import myra.rule.ListMeasure;
import myra.rule.PessimisticAccuracy;
import myra.rule.RuleList;

/**
 * @author James Brookhouse
 *
 */
public class MonotonicListMeasure implements ListMeasure 
{		
    /**
     * Pessimistic accuracy measure
     */
	private PessimisticAccuracy measure;
	
	/**
	 * Default MontonicListMeasure constructor
	 */
	public MonotonicListMeasure()
	{
		measure = new PessimisticAccuracy();
	}
	
    @Override
    public double evaluate(Dataset dataset, RuleList list) 
    {
		if (list.size() == 0) 
		{
		    return 0.0;
		}
		double accuracy = measure.evaluate(dataset, list);
		double mi = MonotonicFunctions.calculateMonotonicityIndex(list, dataset);
		
		return (1.0 - CONFIG.get(CONSTRAINT_WEIGHTING))*accuracy + CONFIG.get(CONSTRAINT_WEIGHTING)*mi;
    }
}
