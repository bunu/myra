/*
 * MOActivity.java
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
import static myra.Dataset.NOT_COVERED;
import static myra.Dataset.RULE_COVERED;
import static myra.rule.Assignator.ASSIGNATOR;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.ListMeasure.DEFAULT_MEASURE;
import static myra.rule.Pruner.DEFAULT_PRUNER;
import static myra.rule.RuleFunction.DEFAULT_FUNCTION;
import static myra.rule.pittsburgh.MO.WeightFactory.WEIGHT_FACTORY;

import java.util.ArrayList;

import myra.Archive;
import myra.Dataset;
import myra.IterativeActivity;
import myra.Config.ConfigKey;
import myra.Dataset.Instance;
import myra.rule.Graph;
import myra.rule.Rule;
import myra.rule.RuleList;
import myra.rule.Graph.Entry;
import myra.rule.ListMeasure;

/**
 * @author jb765
 *
 */
public class FindMOActivity extends IterativeActivity<RuleList> {
	/**
     * The config key for the percentage of uncovered instances allowed.
     */
    public static final ConfigKey<Double> UNCOVERED = new ConfigKey<Double>();
    
    /**
     * Global best pareto non dominated set of rulelists.
     */
    public static final ConfigKey<MORuleList[]> GLOBALNONDOMSET = new ConfigKey<MORuleList[]>();

    /**
     * The current dataset.
     */
    private Dataset dataset;

    /**
     * The construction graph.
     */
    private Graph[] graphs;

    /**
     * The ACO pheromone policy.
     */
    private MOPheromonePolicy policy;

    /**
     * The convergence termination criteria counter.
     */
    private boolean reset;

    /**
     * The (initial) heuristic of the dataset. This value is not modified after
     * the {@link #initialise()} method.
     */
    private Entry[] INITIAL_HEURISTIC;

    /**
     * Creates a new <code>FindMOActivity</code> object.
     * 
     * @param graph
     *            the construction graph.
     * @param dataset
     *            the current dataset.
     */
    public FindMOActivity(Graph[] graphs, Dataset dataset) {	
    	this.graphs = graphs;
    	this.dataset = dataset;
    }

    @Override
    public RuleList create() {
    	double[] weights = CONFIG.get(WEIGHT_FACTORY).getNextAntWeights();
    	Instance[] instances = Instance.newArray(dataset.size());
    	Instance.markAll(instances, NOT_COVERED);
    	Entry[] heuristic = Entry.deepClone(INITIAL_HEURISTIC);

    	RuleList list = new RuleList();
    	list.setIteration(iteration);
    	MORuleFunction function = (MORuleFunction)CONFIG.get(DEFAULT_FUNCTION);
    	function.setDataset(dataset);
    	function.setList(list);

    	int available = dataset.size();
    	int uncovered = (int) ((dataset.size() * CONFIG.get(UNCOVERED)) + 0.5);

    	while (available >= uncovered) {
    		if (list.size() > 0) {
    			// the heuristic procedure only takes into account
    			// the instances covered by a rule, so we prepare an
    			// instance array where each NOT_COVERED value is
    			// replaced by a RULE_COVERED value

    			Instance.mark(instances, NOT_COVERED, RULE_COVERED);
    			heuristic = CONFIG.get(DEFAULT_HEURISTIC).compute(graphs[1], dataset, instances);
    			Instance.mark(instances, RULE_COVERED, NOT_COVERED);
    		}
    		// creates a rule for the current level
    		Rule rule = MORuleFactory.create(list.size(), graphs, weights, heuristic, dataset, instances);
	    
    		available = CONFIG.get(DEFAULT_PRUNER).prune(dataset, rule, instances);

    		list.add(rule);

    		if (rule.size() == 0) {
    			break;
    		}

    		// marks the instances covered by the current rule as
    		// COVERED, so they are not available for the next
    		// iterations
    		Dataset.markCovered(instances);
    	}

    	if (!list.hasDefault()) {
    		if (available == 0) {
    			Instance.markAll(instances, NOT_COVERED);
    		}

    		Rule rule = new Rule();
    		rule.apply(dataset, instances);
    		CONFIG.get(ASSIGNATOR).assign(rule);
    		list.add(rule);
    	}

    	list.setQuality(CONFIG.get(DEFAULT_MEASURE).evaluate(dataset, list));

    	return list;
    }

    @Override
    public void initialise() {
    	super.initialise();

    	policy = new MOPheromonePolicy();
    	policy.initialise(graphs);

    	reset = true;

    	// (initial) heuristic of the whole dataset

    	Instance[] instances = Instance.newArray(dataset.size());
    	Instance.markAll(instances, RULE_COVERED);

    	INITIAL_HEURISTIC = CONFIG.get(DEFAULT_HEURISTIC)
    			.compute(graphs[0], dataset, instances);
    }

    @Override
    public boolean terminate() {
    	if (stagnation > CONFIG.get(STAGNATION)) {
    		if (reset) {
    			policy.initialise(graphs);
    			stagnation = 0;
    			reset = false;
    		} else {
    			return true;
    		}
    	}
    	return super.terminate();
    }

    @Override
    public void update(Archive<RuleList> archive) {
    	super.update(archive);
    	MORuleList[] nonDomSet = generateNonDomSet(updateObjectives(archive));
    	policy.update(graphs, extractRuleList(nonDomSet));
    	CONFIG.set(GLOBALNONDOMSET,mergeNonDomSets(nonDomSet,CONFIG.get(GLOBALNONDOMSET)));
    }
    
    private RuleList[] extractRuleList(MORuleList[] MOlists) {
    	RuleList[] lists = new RuleList[MOlists.length];
    	for(int i = 0; i < MOlists.length; i++){
    		lists[i] = MOlists[i].getRuleList();
    	}
    	return lists;
    }
    
    private MORuleList[] updateObjectives(Archive<RuleList> archive) {   	
    	ListMeasure[] measures = ((MOListMeasure)CONFIG.get(DEFAULT_MEASURE)).getListMeasures();
    	MORuleList[] morulelists = new MORuleList[archive.size()];
    	Object[] ol = archive.topN(archive.size());
    	RuleList[] rulelists = new RuleList[ol.length];
    	for(int i = 0; i < ol.length; i++) {
    		rulelists[i] = (RuleList)ol[i];
    	}
    	
    	for(int i = 0; i < rulelists.length; i++) {
    			MORuleList molist = new MORuleList(rulelists[i]);
    			double[] objectives = new double[measures.length];
    			for(int j = 0; j < measures.length; j++) {
    				objectives[j] = measures[j].evaluate(dataset, molist.getRuleList());
    			}
    			molist.setObjectivesQuality(objectives);
    			morulelists[i] = molist;
    	}
    	return morulelists;
    }
    
    /**
     * Merges to sets of <code>MORuleList</code>s. It is assumed that the first list is already a non-dominated set, 
     * if it is not then undefined behaviour will occur
     * @param globalNonDomSet - must be a correct non-dominated set
     * @param localNonDomSet
     * @return the new non-dominated set of <code>MORuleList</code>s.
     */
    private MORuleList[] mergeNonDomSets(MORuleList[] globalNonDomSet, MORuleList[] localNonDomSet) {
    	ArrayList<MORuleList> nonDomSet = new ArrayList<MORuleList>(globalNonDomSet.length*2);
    	for(MORuleList l : globalNonDomSet) {
    		nonDomSet.add(l);
    	}
    	
    	for(MORuleList llist : localNonDomSet) {
    		boolean nonDom = true;
    		ArrayList<MORuleList> removes = new ArrayList<MORuleList>(); 
    		for(MORuleList glist : nonDomSet) {
    			boolean[] checks = checkObjectives(glist,llist);
    			// If all the things are true then the item being tested is dominated by the item from the non 
    			// dominated set.
    			if(checks[0]) {
    				nonDom = false;
    				break;
    			}
    			// If all the things are false then the item being tested dominates the item from the non dominated 
    			// set so we should remove it from the non dominated set.
    			if(checks[1]) {
    				removes.add(glist);
    			}
    		}
    		nonDomSet.remove(removes);
    		if(nonDom) {
    			nonDomSet.add(llist);
    		}	
    	}
    	return (MORuleList[]) nonDomSet.toArray(new MORuleList[nonDomSet.size()]);
    }
    
    /**
     * Generates the Non dominated set of rule lists from the set of generated solutions
     * @param ruleListSet
     * @return the non dominated set of <code>MORuleList</code>s
     */
    private MORuleList[] generateNonDomSet(MORuleList[] ruleListSet) {
    	ArrayList<MORuleList> nonDomSet = new ArrayList<MORuleList>(ruleListSet.length);   	
    	for(MORuleList testList : ruleListSet) {
    		boolean nonDom = true;
    		ArrayList<MORuleList> removes = new ArrayList<MORuleList>();
    		for(MORuleList domList : nonDomSet) {
    			boolean[] checks = checkObjectives(domList,testList);		
    			// If all the things are true then the item being tested is dominated by the item from the non 
    			// dominated set.
    			if(checks[0]) {
    				nonDom = false;
    				break;
    			}    			
    			// If all the things are false then the item being tested dominates the item from the non dominated
    			// set so we should remove it from the non dominated set.
    			if(checks[1]) {
    				removes.add(domList);
    			}
    		}
    		for(MORuleList l : removes){
    			nonDomSet.remove(l);
    		}
    		nonDomSet.remove(removes);
    		if(nonDom) {
    			nonDomSet.add(testList);
    		}
    	}   	
    	return nonDomSet.toArray(new MORuleList[nonDomSet.size()]);
    }      
    
    /**
     * Checks if the two lists dominate each other, it is assumed that the first list is from the current non 
     * dominated set and the second is being tested for inclusion.
     * @param nonDomList
     * @param testList
     * @return boolean array where element 0 is all true status and element 1 is all false status
     */
    private boolean[] checkObjectives(MORuleList nonDomList, MORuleList testList) {
    	boolean[] checks = new boolean[]{true,true};    	
    	for(int i = 0; i < nonDomList.getObjectivesQuality().length; i++) {
			if(nonDomList.getObjectivesQuality()[i] > testList.getObjectivesQuality()[i]) {
				checks[1] = false;
			}
			else {
				checks[0] = false;
			}
		}    	
    	return checks;
    }
}