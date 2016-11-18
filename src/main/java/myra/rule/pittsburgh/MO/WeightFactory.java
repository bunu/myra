/*
 * WeightFactory.java
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
import static myra.Scheduler.COLONY_SIZE;

import myra.Config.ConfigKey;

/**
 * @author jb765
 *
 */
public class WeightFactory {
	
	private int currentAnt = 0;
	
	public static final ConfigKey<WeightFactory> WEIGHT_FACTORY = new ConfigKey<WeightFactory>();
	
	/**
	 * 
	 * @return
	 */
	public double[] getCurrentAntWeight() {
		int antNumber = CONFIG.get(COLONY_SIZE);
		double[] weights = new double[2];		
		weights[0] = currentAnt/antNumber;
		weights[1] = 1 - weights[0];
		return weights;
	}
	
	/**
	 * Generates the next set of function weights for the next ant
	 * @return set of function weights
	 */
	public double[] getNextAntWeights() {
		//HACK HACK HACK: only works for 2 functions, needs making generic when we have time
		int antNumber = CONFIG.get(COLONY_SIZE);
		double[] weights = new double[2];		
		weights[0] = currentAnt/antNumber;
		weights[1] = 1 - weights[0];		
		currentAnt++;
		return weights;
	}
	
	/**
	 * Resets the current ant to 0;
	 */
	public void resetAntWeights() {
		currentAnt = 0;
	}
}
