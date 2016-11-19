/*
 * MichiganPheromonePolicy.java
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

package myra.rule.michigan;

import static myra.rule.Graph.START_INDEX;

import myra.rule.Graph;
import myra.rule.Rule;
import myra.rule.Graph.Entry;
import myra.rule.Rule.Term;
import myra.rule.irl.EdgePheromonePolicy;

/**
 * This class is responsible for maintaining the pheromone values of the
 * construction graph. Pheromones are stored in each edge.
 * 
 * <p>
 * Evaporation is simulated by normalising pheromone values during the
 * <code>finaliseUpdate </code> mechanism. Values that do not increase during
 * the update will decrease as a result of the normalisation.
 * </p>
 * 
 * @author James Brookhouse
 */
public class MichiganPheromonePolicy extends EdgePheromonePolicy {
	
	/**
	 * Updates the pheromone values, increasing the pheromone according to the
	 * <code>rule</code> quality. Evaporation is not performed at this point
	 * allowing for multi-rule updates the <code>finaliseUpdate</code> method is
	 * used to perform evaporation.
	 * 
	 * @param graph
	 *            the construction graph.
	 * @param rule
	 *            the rule to guide the update.
	 */
	public void update(Graph graph, Rule rule) {
		Term[] terms = rule.terms();
		Entry[][] matrix = graph.matrix();

		final double q = rule.getQuality();
		int from = START_INDEX;

		for (int i = 0; i < terms.length; i++) {
			Entry entry = matrix[from][terms[i].index()];
			double value = entry.value(0);
			entry.set(0, value + (value * q));

			from = terms[i].index();
		}
	}
}
