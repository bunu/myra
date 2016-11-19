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
import static myra.rule.irl.PheromonePolicy.DEFAULT_POLICY;
import static myra.rule.michigan.FindRulesActivity.UPDATE_THRESHOLD;

import java.util.ArrayList;
import java.util.Collection;

import myra.Dataset;
import myra.Model;
import myra.Option;
import myra.Config.ConfigKey;
import myra.Option.DoubleOption;
import myra.rule.RuleList;
import myra.rule.irl.cAntMiner;

/**
 * Default executable class file for the <code><i>c</i>Ant-Miner<sub>M</sub></code>
 * algorithm.
 * 
 * @author James Brookhouse
 *
 */
public class cAntMinerM extends cAntMiner {
	
	// HACK HACK HACK: dirty way to get the current CLASSIFIER to the rest of the
	// algorithm, not thread safe at all DO NOT attempt to use the parallel scheduler.
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
		options.add(new DoubleOption(UPDATE_THRESHOLD,
					      "ut",
					      "set the maximum %s of iterations",
					      "percentage"));
		return options;
    }
	
	@Override
    public String description() {
		return "cAnt-MinerM rule induction";
    }
	
	@Override
	public Model train(Dataset dataset) {
		CONFIG.set(CLASSIFIER, new RuleList());
		
		//TODO: finish training implementation
		
		return CONFIG.get(CLASSIFIER);
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
