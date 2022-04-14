/*
 * BaggingArchiveRuleFactory.java
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
import static myra.rule.Graph.START_INDEX;

import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Rule;
import myra.rule.Graph.Entry;
import myra.rule.Rule.Term;
import myra.rule.archive.ArchiveRuleFactory;
import myra.rule.archive.Graph;
import myra.rule.archive.Graph.Vertex;
import myra.rule.irl.RuleFactory;

/**
 * @author ahh209
 */
public class BaggingArchiveRuleFactory extends ArchiveRuleFactory implements RuleFactory {

    @Override
    public Rule create(int level,
                       myra.rule.Graph graph,
                       Entry[] heuristic,
                       Dataset dataset,
                       Instance[] instances) {

        if (!Graph.class.isInstance(graph)) {
            throw new IllegalArgumentException("Invalid graph class: "
                    + graph.getClass());
        }

        return this.create(level, (Graph) graph, heuristic, dataset, instances);
    }

    /**
    * Creates a classification rule. Note that this method does not determine
    * the consequent of the rule.
    * 
    * @param level
    *            the id (sequence) of the rule.
    * @param graph
    *            the construction graph.
    * @param heuristic
    *            the heuristic values.
    * @param dataset
    *            the current dataset.
    * @param instances
    *            the covered instances flag.
    * 
    * @return a classification rule.
    */
   public Rule create(int level,
                      Graph graph,
                      Entry[] heuristic,
                      Dataset dataset,
                      Instance[] instances) {
       // the rule being created (empty at the start)
       Rule rule = Rule.newInstance(graph.size() / 2);
       int previous = START_INDEX;

       double[] pheromone = new double[graph.size()];
       boolean[] incompatible = new boolean[graph.size()];
       incompatible[START_INDEX] = true;

       double[] baggingMask = dataset.getMask();

       while (true) {
           double total = 0.0;
           Entry[] neighbours = graph.matrix()[previous];

           // calculates the probability of visiting vertex i by
           // multiplying the pheromone and heuristic information (only
           // compatible vertices are considered)
           // checking if the attribute is allowed based on the bagging mask
           for (int i = 0; i < neighbours.length; i++) {
               	//relying on the lazy evaluation of the or statement to cope with attribute values of -1 which refer to the start/end nodes
               if (!incompatible[i] && neighbours[i] != null  && (graph.vertices()[i].attribute == -1 || baggingMask[graph.vertices()[i].attribute] == 1)) {
                   pheromone[i] =
                           neighbours[i].value(level) * heuristic[i].value(0);
                 
                   total += pheromone[i];
               } else {
                   pheromone[i] = 0.0;
               }
           }

           if (total == 0.0) {
               // there are no compatible vertices, the creation process is stopped
               break;
           }

           // prepares the roulette by accumulation the probabilities,
           // from 0 to 1
           double cumulative = 0.0;

           for (int i = 0; i < pheromone.length; i++) {
               if (pheromone[i] > 0) {
                   pheromone[i] = cumulative + (pheromone[i] / total);
                   cumulative = pheromone[i];
               }
           }

           for (int i = (pheromone.length - 1); i >= 0; i--) {
               if (pheromone[i] > 0) {
                   pheromone[i] = 1.0;
                   break;
               }
           }

           // roulette selection
           double slot = CONFIG.get(RANDOM_GENERATOR).nextDouble();
           int selected = Graph.END_INDEX;

           for (int i = 0; i < pheromone.length; i++) {
               if (slot < pheromone[i]) {
                   selected = i;
                   break;
               }
           }

           if (selected == Graph.END_INDEX) {
               break;
           }

           Vertex vertex = graph.vertices()[selected];
           Term term = new Term(selected, vertex.condition(level, dataset));
           rule.push(term);

           previous = selected;
           // make the vertex unavailable
           incompatible[selected] = true;
       }

       rule.compact();
       rule.apply(dataset, instances);
       return rule;
   }

   /**
    * Create a classification rules. Note that this method will use pheromone
    * values from the level <code>0</code> only.
    * 
    * @param graph
    *            the construction graph.
    * @param heuristic
    *            the heuristic values.
    * @param dataset
    *            the current dataset.
    * @param instances
    *            the covered instances flag.
    * 
    * @return a classification rule.
    */
   @Override
   public Rule create(myra.rule.Graph graph,
                      Entry[] heuristic,
                      Dataset dataset,
                      Instance[] instances) {
       return this.create(0, graph, heuristic, dataset, instances);
   }
}
