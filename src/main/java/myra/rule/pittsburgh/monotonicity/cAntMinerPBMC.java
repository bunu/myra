/*
 * cAntMinerPBMC.java
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
import static myra.IterativeActivity.MAX_ITERATIONS;
import static myra.IterativeActivity.STAGNATION;
import static myra.Scheduler.COLONY_SIZE;
import static myra.Scheduler.PARALLEL;
import static myra.interval.IntervalBuilder.DEFAULT_BUILDER;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.monotonicity.FindMonotonicRuleListActivity.UNCOVERED;
import static myra.rule.pittsburgh.LevelPheromonePolicy.EVAPORATION_FACTOR;
import static myra.rule.pittsburgh.LevelPheromonePolicy.P_BEST;
import static myra.rule.pittsburgh.monotonicity.Constraint.CONSTRAINT_WEIGHTING;
import static myra.rule.pittsburgh.monotonicity.MonotonicPruner.MONOTONIC_PRUNER;

import java.util.ArrayList;
import java.util.Collection;

import myra.Classifier;
import myra.Dataset;
import myra.Model;
import myra.Option;
import myra.Scheduler;
import myra.Option.BooleanOption;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;
import myra.interval.BoundarySplit;
import myra.interval.C45Split;
import myra.interval.IntervalBuilder;
import myra.interval.MDLSplit;
import myra.rule.BacktrackPruner;
import myra.rule.EntropyHeuristic;
import myra.rule.Graph;
import myra.rule.Heuristic;
import myra.rule.ListMeasure;
import myra.rule.MajorityAssignator;
import myra.rule.RuleList;

/**
 * @author James Brookhouse
 *
 */
public class cAntMinerPBMC extends Classifier{
	
	@Override
	protected void defaults() {
		super.defaults();
		
		// configuration not set via command line
		CONFIG.set(ASSIGNATOR, new MajorityAssignator());
		CONFIG.set(P_BEST, 0.05);
		CONFIG.set(IntervalBuilder.MAXIMUM_LIMIT, 25);
		CONFIG.set(DEFAULT_PRUNER, new BacktrackPruner());
		CONFIG.set(DEFAULT_FUNCTION, new MonotonicityRuleFunction());
		CONFIG.set(DEFAULT_MEASURE, new MonotonicListMeasure());

		// default configuration values
		CONFIG.set(COLONY_SIZE, 5);
		CONFIG.set(MAX_ITERATIONS, 500);
		CONFIG.set(IntervalBuilder.MINIMUM_CASES, 10);
		CONFIG.set(EVAPORATION_FACTOR, 0.9);
		CONFIG.set(UNCOVERED, 0.01);
		CONFIG.set(STAGNATION, 40);
		CONFIG.set(DEFAULT_HEURISTIC, new EntropyHeuristic());
		CONFIG.set(DYNAMIC_HEURISTIC, Boolean.FALSE);
		CONFIG.set(DEFAULT_BUILDER, new MDLSplit(new BoundarySplit()));
		CONFIG.set(CONSTRAINT_WEIGHTING, 0.5);
		CONFIG.set(MONOTONIC_PRUNER, new MonotonicBacktrackPruner());
	}
	
	@Override
    public String description() {
		return "Pittsburgh-based cAnt-Miner with monotonicity constraints";
    }

	@Override
	protected Model train(Dataset dataset) {
		FindMonotonicRuleListActivity activity =
				new FindMonotonicRuleListActivity(new Graph(dataset), dataset);

			Scheduler<RuleList> scheduler = Scheduler.newInstance(1);
			scheduler.setActivity(activity);
			scheduler.run();
			
			RuleList list = activity.getBest();
			CONFIG.get(MONOTONIC_PRUNER).prune(dataset, list);

			return list;
	}
	
    @Override
    protected Collection<Option<?>> options() {
		ArrayList<Option<?>> options = new ArrayList<Option<?>>();
		options.addAll(super.options());
	
		// colony size
		options.add(new IntegerOption(COLONY_SIZE,
					      "c",
					      "specify the %s of the colony",
					      "size"));
	
		// maximum number of iterations
		options.add(new IntegerOption(MAX_ITERATIONS,
					      "i",
					      "set the maximum %s of iterations",
					      "number"));
	
		// support to parallel execution
		options.add(new IntegerOption(PARALLEL,
					      "-parallel",
					      "enable parallel execution in multiple %s;"
						      + " if no cores are specified, use"
						      + " all available cores",
					      "cores") {
		    @Override
		    public void set(String value) {
			if (value == null) {
			    value = String
				    .format("%d",
					    Runtime.getRuntime().availableProcessors());
			}
	
			super.set(value);
		    }
		});

		// minimum number of covered examples
		options.add(new IntegerOption(IntervalBuilder.MINIMUM_CASES,
					      "m",
					      "set the minimum %s of covered examples per rule",
					      "number"));
	
		// number of uncovered examples
		options.add(new DoubleOption(UNCOVERED,
					     "u",
					     "set the %s of allowed uncovered examples",
					     "percentage"));
	
		// convergence test
		options.add(new IntegerOption(STAGNATION,
					      "x",
					      "set the number of %s for convergence test",
					      "iterations"));
	
		// evaporation factor
		options.add(new DoubleOption(EVAPORATION_FACTOR,
					     "e",
					     "set the MAX-MIN evaporation %s",
					     "factor"));
	
		// rule quality function
		Option<ListMeasure> measure = new Option<ListMeasure>(DEFAULT_MEASURE,
								      "l",
								      "specify the rule list quality %s",
								      true,
								      "function");
		measure.add("accuracy", new ListMeasure.Accuracy());
		measure.add("pessimistic", CONFIG.get(DEFAULT_MEASURE));
		options.add(measure);
	
		// heuristic information
		Option<Heuristic> heuristic = new Option<Heuristic>(DEFAULT_HEURISTIC,
								    "h",
								    "specify the heuristic %s",
								    true,
								    "method");
		heuristic.add("gain", CONFIG.get(DEFAULT_HEURISTIC));
		heuristic.add("none", new Heuristic.None());
		options.add(heuristic);
	
		// dynamic heuristic calculation
		BooleanOption dynamic =
			new BooleanOption(DYNAMIC_HEURISTIC,
					  "g",
					  "enables the dynamic heuristic computation");
		options.add(dynamic);
	
		// dynamic discretisation procedure
		Option<IntervalBuilder> builder =
			new Option<IntervalBuilder>(DEFAULT_BUILDER,
						    "d",
						    "specify the discretisation",
						    true,
						    "method");
		builder.add("c45", new C45Split());
		builder.add("mdl", CONFIG.get(DEFAULT_BUILDER));
		options.add(builder);
		
		// the constraint weighting ratio
		options.add(new DoubleOption(CONSTRAINT_WEIGHTING,
					     "cw",
					     "set the strength of the constraint ratio",
					     "ratio"));
	
	
		// monotonic pruner
		Option<MonotonicPruner> pruner =
			new Option<MonotonicPruner>(MONOTONIC_PRUNER,
						 "mp",
						 "specify the monotonic pruner",
						 true,
						 "pruner");
		pruner.add("most_monotonic", new MostMonotonicPruner());
		pruner.add("bactrack_monotonic", CONFIG.get(MONOTONIC_PRUNER));
		options.add(pruner);

		return options;
    }
	
    /**
     * <code><i>c</i>Ant-Miner<sub>PB</sub>MC</code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
    	cAntMinerPBMC algorithm = new cAntMinerPBMC();
    	algorithm.run(args);
    }
}
