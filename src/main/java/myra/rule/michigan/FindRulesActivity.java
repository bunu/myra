/*
 * FindRulesActivity.java
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
import static myra.Dataset.COVERED;
import static myra.Dataset.RULE_COVERED;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;
import static myra.rule.michigan.cAntMinerM.CLASSIFIER;

import java.util.Arrays;
import java.util.Comparator;

import myra.Archive;
import myra.Dataset;
import myra.Dataset.Instance;
import myra.IterativeActivity;
import myra.Config.ConfigKey;
import myra.rule.Graph;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.Graph.Entry;
import myra.rule.irl.PheromonePolicy;

/**
 * The <code>FineRulesActivity</code> is responsible for evolving a single rule
 * using an ACO-based procedure. Procedure also includes a niching process to
 * update the classifier after rule construction
 * 
 * @author James Brookhouse
 *
 */
public class FindRulesActivity extends IterativeActivity<Rule> {

	public final static ConfigKey<Double> UPDATE_THRESHOLD = new ConfigKey<>();

	/**
	 * Instance flag array indicating the instances to be used during the
	 * construction procedure.
	 */
	private Instance[] instances;

	/**
	 * The current dataset.
	 */
	private Dataset dataset;

	/**
	 * The construction graph.
	 */
	private Graph graph;

	/**
	 * The ACO pheromone policy.
	 */
	private PheromonePolicy policy;

	/**
	 * The heuristic values for the graph's vertices.
	 */
	private Entry[] heuristic;

	/**
	 * Creates a new <code>FindRulesActivity</code> object.
	 * 
	 * @param graph
	 *            the construction graph.
	 * @param instances
	 *            the instances to be used.
	 * @param training
	 *            the current dataset.
	 */
	public FindRulesActivity(Graph graph, Instance[] instances, Dataset training) {
		this.graph = graph;
		this.instances = instances;
		this.dataset = training;
	}

	@Override
	public Rule create() {
		// the instances array will be modified by the create and prune,
		// so we need to work on a copy to avoid concurrency problems
		Instance[] clone = Instance.copyOf(instances);

		Rule rule = CONFIG.get(DEFAULT_FACTORY).create(graph, heuristic, dataset, clone);
		CONFIG.get(DEFAULT_PRUNER).prune(dataset, rule, clone);
		rule.setQuality(CONFIG.get(DEFAULT_FUNCTION).evaluate(rule));

		return rule;
	}

	@Override
	public void initialise() {
		super.initialise();

		policy = CONFIG.get(DEFAULT_POLICY);
		policy.initialise(graph);

		// the heuristic procedure only takes into account
		// the instances covered by a rule, so we prepare an
		// instance array where each NOT_COVERED value is
		// replaced by a RULE_COVERED value

		Instance[] clone = Instance.copyOf(instances);
		Instance.mark(clone, NOT_COVERED, RULE_COVERED);

		heuristic = CONFIG.get(DEFAULT_HEURISTIC).compute(graph, dataset, clone);
	}

	@Override
	public boolean terminate() {
		return super.terminate() || stagnation > CONFIG.get(STAGNATION);
	}

	@Override
	public void update(Archive<Rule> archive) {
		super.update(archive);
		Rule[] rules = archive.topN(archive.size());
		for (Rule rule : rules) {
			if (rule.getQuality() >= CONFIG.get(UPDATE_THRESHOLD)) {
				policy.update(graph, rule);
			} else {
				// Its an ordered fitness list so when we drop below the
				// threshold we can just stop.
				break;
			}
		}
		policy.finaliseUpdate(graph);
		niching(archive);
	}

	/**
	 * Used the archive populated in the last iteration and the rules in the
	 * classifier to construct a new classifier based on the niching mechanism
	 * defined in Olmo et al's GBAP
	 * 
	 * @param archive
	 *            the archive generated in the last generation
	 */
	private void niching(Archive<Rule> archive) {
		Rule[] rules = mergeRules(archive, CONFIG.get(CLASSIFIER).rules());
		sort(rules);

		// Now we have to allow the rules to claim the class tokens so we
		// initialise the a few arrays and then iterate over both the sorted
		// rule list and the classes in the dataset.
		int[] classdistribution = new int[dataset.classLength()];
		Instance[][] kinstances = new Instance[dataset.classLength()][];
		NicheScore[] scores = new NicheScore[rules.length];
		for (int i = 0; i < dataset.classLength(); i++) {
			classdistribution[i] = dataset.distribution(i);
			kinstances[i] = Instance.copyOf(instances);
		}

		for (int i = 0; i < rules.length; i++) {
			scores[i] = new NicheScore(rules[i], classdistribution);
			for (int j = 0; j < dataset.classLength(); j++) {
				scores[i].ruleCovers(dataset, kinstances[j], j);
			}
		}

		// Now construct the new classifier based on the niching results and
		// update the rule quality as the consequent may have been altered.
		//
		// TODO: the quality shouldn't be updated here need to find a better
		// place that doesn't involve updating it many times.
		RuleList newClassifier = new RuleList();
		for (NicheScore score : scores) {
			if (score.bestAdjustedfitness() != null) {
				Rule r = score.bestAdjustedfitness();
				r.setQuality(CONFIG.get(DEFAULT_FUNCTION).evaluate(r));
				newClassifier.add(r);
			}
		}
		newClassifier.apply(dataset);
		CONFIG.set(CLASSIFIER, newClassifier);
	}

	/**
	 * Merges the list generated by this iteration and the rules already present
	 * in the classifier ready for niching
	 * 
	 * @param archive
	 *            the archive of rules created in this iteration
	 * @param classifier
	 *            the current best classifier rule collection
	 * @return the merged list of rules
	 */
	private Rule[] mergeRules(Archive<Rule> archive, Rule[] classifier) {
		Rule[] rules = new Rule[archive.size() + classifier.length];
		int i = 0;
		for (Rule r : archive.topN(archive.size())) {
			rules[i] = r;
			i++;
		}
		for (Rule r : classifier) {
			rules[i] = r;
			i++;
		}
		return rules;
	}

	/**
	 * Sorts the list of rules given
	 * 
	 * @param rules
	 *            the rules to be sorted
	 */
	private void sort(Rule[] rules) {
		Arrays.sort(rules, new Comparator<Rule>() {
			public int compare(Rule r1, Rule r2) {
				return -r1.compareTo(r2);
			};
		});
	}

	/**
	 * 
	 * @author James Brookhouse
	 *
	 */
	public class NicheScore {

		private Rule rule;

		private int[] kvalues;

		private int[] classes;

		/**
		 * 
		 * @param rule
		 * @param classes
		 */
		public NicheScore(Rule rule, int[] classes) {
			this.rule = rule;
			this.classes = classes;
			this.kvalues = new int[classes.length];
		}

		/**
		 * Scans the dataset and checks for coverage of class k
		 * 
		 * @param dataset
		 *            the dataset
		 * @param instances
		 *            the instance array to track coverage
		 * @param k
		 *            the class to check coverage
		 */
		public void ruleCovers(Dataset dataset, Instance[] instances, int k) {
			int currentConsequent = rule.getConsequent();
			rule.setConsequent(k);
			for (int i = 0; i < dataset.size(); i++) {
				if (instances[i].flag == NOT_COVERED && dataset.get(i)[dataset.get(i).length - 1] == k
						&& rule.covers(dataset, i)) {
					kvalues[k]++;
					instances[i].flag = COVERED;
				}
			}
			rule.setConsequent(currentConsequent);
		}

		/**
		 * Gets the adjusted fitness for an attribute
		 * 
		 * @param k
		 *            the class required
		 * @return the adjusted fitness
		 */
		public double adjustedfitness(int k) {
			return rule.getQuality() * ((double) kvalues[k] / (double) classes[k]);
		}

		/**
		 * Gets the rule variant with the best adjusted fitness, if no rule has
		 * a valid adjusted fitness (one where the fitness is above 0) then null
		 * is returned instead.
		 * 
		 * @return the best rule
		 */
		public Rule bestAdjustedfitness() {
			Rule best = null;
			double bestscore = 0;
			for (int i = 0; 0 < classes.length; i++) {
				if (adjustedfitness(i) > bestscore) {
					best = rule;
					best.setConsequent(i);
					bestscore = adjustedfitness(i);
				}
			}
			return best;
		}

		/**
		 * Gets the rule associated with this Niche
		 * 
		 * @return the rule
		 */
		public Rule getRule() {
			return rule;
		}
	}
}
