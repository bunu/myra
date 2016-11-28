/*
 * cAntMinerM.java
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

import static myra.Config.CONFIG;
import static myra.Dataset.NOT_COVERED;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.michigan.FindRulesActivity.UPDATE_THRESHOLD;
import static myra.rule.michigan.FindRulesActivity.ITERATION_RULELIST;

import java.util.ArrayList;
import java.util.Collection;

import myra.Dataset;
import myra.Model;
import myra.Option;
import myra.Scheduler;
import myra.Config.ConfigKey;
import myra.Dataset.Instance;
import myra.Option.DoubleOption;
import myra.rule.Graph;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.irl.cAntMiner;

/**
 * Default executable class file for the
 * <code><i>c</i>Ant-Miner<sub>M</sub></code> algorithm.
 * 
 * @author James Brookhouse
 *
 */
public class cAntMinerM extends cAntMiner {

	// HACK HACK HACK: dirty way to get the current CLASSIFIER to the rest of
	// the algorithm, not thread safe at all DO NOT attempt to use the parallel
	// scheduler until checking how much will break.
	public final static ConfigKey<RuleList> CLASSIFIER = new ConfigKey<>();

	@Override
	protected void defaults() {
		super.defaults();

		// configuration not set via command line
		CONFIG.set(DEFAULT_POLICY, new MichiganPheromonePolicy());

		// default configuration values
		CONFIG.set(UPDATE_THRESHOLD, 0.5);
	}

	@Override
	protected Collection<Option<?>> options() {
		ArrayList<Option<?>> options = new ArrayList<Option<?>>();
		options.addAll(super.options());

		// maximum number of iterations
		options.add(new DoubleOption(UPDATE_THRESHOLD, "ut", "set the maximum %s of iterations", "percentage"));
		return options;
	}

	@Override
	public String description() {
		return "cAnt-MinerM rule induction";
	}

	@Override
	public Model train(Dataset dataset) {
		CONFIG.set(CLASSIFIER, new RuleList());
		CONFIG.set(ITERATION_RULELIST, new RuleList());

		Graph graph = new Graph(dataset);
		Scheduler<Rule> scheduler = Scheduler.newInstance(1);
		FindRulesActivity activity = new FindRulesActivity(graph, Instance.newArray(dataset.size()), dataset);
		scheduler.setActivity(activity);
		scheduler.run();

		RuleList list = CONFIG.get(CLASSIFIER);
		addDefault(list, dataset);
		return list;
	}

	/**
	 * Adds the default rule to the list if it does not contain one.
	 * 
	 * @param list
	 *            the <code>RuleList</code> to add default rule to.
	 * @param dataset
	 *            the current Dataset.
	 */
	private void addDefault(RuleList list, Dataset dataset) {

		if (!list.hasDefault()) {
			Instance[] instances = Instance.newArray(dataset.size());
			Instance.markAll(instances, NOT_COVERED);

			for (int i = 0; i < list.rules().length; i++) {
				list.rules()[i].apply(dataset, instances);
				Dataset.markCovered(instances);
			}

			int available = 0;

			for (Instance i : instances) {
				if (i.flag == NOT_COVERED) {
					available++;
				}
			}

			if (available == 0) {
				Instance.markAll(instances, NOT_COVERED);
			}

			Rule rule = new Rule();
			rule.apply(dataset, instances);
			CONFIG.get(ASSIGNATOR).assign(rule);
			list.add(rule);
		}
	}

	/**
	 * <code><i>c</i>Ant-Miner<sub>M</sub></code> entry point.
	 * 
	 * @param args
	 *            command-line arguments.
	 * 
	 * @throws Exception
	 *             If an error occurs &mdash; e.g., I/O error.
	 */
	public static void main(String[] args) throws Exception {
		cAntMinerM algorithm = new cAntMinerM();
		algorithm.run(args);
	}
}
