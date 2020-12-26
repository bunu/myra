/*
 * Heuristic.java
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

package myra.rule;

import myra.Config.ConfigKey;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;
import myra.rule.Graph.Entry;

/**
 * The <code>Heuristic</code> represents the background knowledge that can be
 * incorporated in the construction graph.
 */
public interface Heuristic {
    /**
     *
     * Config key for the default <code>Heuristic</code> instance.
     */
    public static final ConfigKey<Heuristic> DEFAULT_HEURISTIC =
            new ConfigKey<Heuristic>();

    /**
     * The config key to indicate if the heuristic value is recomputed at each
     * level of the tree.
     */
    public final static ConfigKey<Boolean> DYNAMIC_HEURISTIC =
            new ConfigKey<Boolean>();

    /**
     * Computes the heuristic information.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * 
     * @return an array with the heuristic values for each vertex of the graph.
     */
    public Entry[] compute(Graph graph, Dataset dataset, Instance[] instances);

    /**
     * Computes the heuristic information.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param used
     *            indicates the vertices already used.
     * 
     * @return an array with the heuristic values for each vertex of the graph.
     */
    public Entry[] compute(Graph graph,
                           Dataset dataset,
                           Instance[] instances,
                           boolean[] used);

    /**
     * Computes the heuristic information.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param target
     *            the target class value.
     * 
     * @return an array with the heuristic values for each vertex of the graph.
     */
    public Entry[] compute(Graph graph,
                           Dataset dataset,
                           Instance[] instances,
                           int target);

    /**
     * Computes the heuristic information.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     * @param instances
     *            the covered instances flags.
     * @param used
     *            indicates the vertices already used.
     * @param target
     *            the target class value.
     * 
     * @return an array with the heuristic values for each vertex of the graph.
     */
    public Entry[] compute(Graph graph,
                           Dataset dataset,
                           Instance[] instances,
                           boolean[] used,
                           int target);

    /**
     * No heuristic information implementation - e.g., heuristic value is set to
     * 1 for all unused attributes.
     * 
     * @author Fernando Esteban Barril Otero
     */
    public static class None implements Heuristic {
        @Override
        public Entry[] compute(Graph graph,
                               Dataset dataset,
                               Instance[] instances) {
            return compute(graph, dataset, instances, new boolean[0]);
        }

        @Override
        public Entry[] compute(Graph graph,
                               Dataset dataset,
                               Instance[] instances,
                               boolean[] used) {
            Entry[] heuristic = Entry.initialise(new Entry[graph.size()]);

            for (int i = 0; i < heuristic.length; i++) {
                double value = 1.0;

                heuristic[i].setInitial(value);
                heuristic[i].set(0, value);
            }

            return heuristic;
        }

        @Override
        public Entry[] compute(Graph graph,
                               Dataset dataset,
                               Instance[] instances,
                               int target) {
            return compute(graph, dataset, instances, new boolean[0], target);
        }

        @Override
        public Entry[] compute(Graph graph,
                               Dataset dataset,
                               Instance[] instances,
                               boolean[] used,
                               int target) {
            return compute(graph, dataset, instances, used);
        }
    }
}