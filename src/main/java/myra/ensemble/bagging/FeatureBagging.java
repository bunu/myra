/*
 * FeatureBagging.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2025
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

package myra.ensemble.bagging;

import static myra.Config.CONFIG;
import static myra.datamining.Algorithm.RANDOM_GENERATOR;

import myra.Config.ConfigKey;


/**
 * Class that will create a feature bag. No underlying dataset will be modified as this is an expensive process,
 * instead a mask will be generated which will indicate if a feature has been turned on or off.
 *
 * @author Ayah Helal
 * @author James Brookhouse
 */
public class FeatureBagging {
    
    public static final ConfigKey<Double> DEFAULT_FEATURE_BAGGING = new ConfigKey<>();

	/**
	 * Generates a bagging mask, each feature is turned on or off probabilistically based on the value set by
	 * DEFAULT_FEATURE_BAGGING
	 * @param attributesSize The number of attributes/features in the dataset
	 * @return An array of doubles, where 1 indicates a feature is active and 0 inactive
	 */
	public static double[] generateBaggingMask(int attributesSize)
    {
		double probability = CONFIG.get(DEFAULT_FEATURE_BAGGING);
		if (probability > 1) {
			probability = Math.sqrt(attributesSize - 1) / (attributesSize - 1);
		}

		double[] mask = new double[attributesSize -1];

		for(int i=0;i<attributesSize -1 ;i++)
		{
			if(CONFIG.get(RANDOM_GENERATOR).nextDouble() <= probability) {
				mask[i] = 1.0;
			}
		}
		return mask;
    }
}
