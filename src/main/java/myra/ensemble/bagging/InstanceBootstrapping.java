/*
 * InstanceBootstrapping.java
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
import myra.datamining.Dataset;


/**
 * Class containing methods that will sample a dataset, based on the principles of bootstrapping.
 * Datasets can be sampled with and without replacements
 *
 * @author Ayah Helal
 * @author James Brookhouse
 */
public class InstanceBootstrapping {
    
    public static final ConfigKey<Double> DEFAULT_INSTANCE_BOOTSTRAPPING = new ConfigKey<>();

	/**
	 * Calls a bootstrapping method based on the value of the replacement boolean passed to it.
	 * @param data The dataset to sample.
	 * @param replacement If true, sampling will be done with replacement.
	 * @return The sampled data set.
	 */
    public static Dataset bootstrapping(Dataset data, boolean replacement) {
		if(replacement) {
			return bootstrappingWithRepeats(data);
		}
		return bootstrapping(data);
    }

	/**
	 * Creates a new dataset by sampling the given dataset. The new dataset is a percentage of the original
	 * stored by the variable DEFAULT_INSTANCE_BOOTSTRAPPING. The sampling is done with replacements, so some
	 * instances may feature more than once.
	 * @param data The dataset to sample.
	 * @return The sampled data set.
	 */
	public static Dataset bootstrappingWithRepeats(Dataset data) {
		Dataset newdata = new Dataset();
		newdata.setAttributes(data.attributes());
		int length = data.size();
		double percentage = CONFIG.get(DEFAULT_INSTANCE_BOOTSTRAPPING);
		int sampleSize = (int) Math.max(Math.ceil(length * percentage),1);
		for (int i = 0; i < sampleSize; i++) {
			newdata.add(data.get(CONFIG.get(RANDOM_GENERATOR).nextInt(length)));
		}
		return newdata;
    }

	/**
	 * Creates a dataset by randomly sampling the given data set up to the percentage
	 * size given in DEFAULT_INSTANCE_BOOTSTRAPPING
	 * @param data The dataset to sample.
	 * @return The sampled data set.
	 */
	public static Dataset bootstrapping(Dataset data) {
		Dataset newdata = new Dataset();
		newdata.setAttributes(data.attributes());
		boolean[] used = new boolean[data.size()];
		int length = data.size();
		double percentage = CONFIG.get(DEFAULT_INSTANCE_BOOTSTRAPPING);
		int sampleSize = (int) Math.max(Math.ceil(length * percentage),1);
		int random;
		for (int i = 0; i < sampleSize; i++) {
			do {
			random = CONFIG.get(RANDOM_GENERATOR).nextInt(length);
			} while (used[random]);

			newdata.add(data.get(random));
			used[random] = true;
		}
		return newdata;
    }
}
