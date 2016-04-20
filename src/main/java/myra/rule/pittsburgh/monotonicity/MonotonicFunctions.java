/*
 * MonotonicFunctions.java
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

import java.util.Arrays;

import myra.Dataset;
import myra.Attribute.Condition;
import myra.rule.Rule;
import myra.rule.Rule.Term;
import myra.rule.RuleList;
import myra.rule.pittsburgh.monotonicity.Constraint.Direction;

/**
 * @author James Brookhouse
 *
 */
final class MonotonicFunctions {
	
	/**
	 * Calculates the monotonicity index of an data set (MI = 1 - NMI)
	 * 
	 * @param dataset - dataset to test
	 * @return MI index for the dataset
	 */
	public static double calculateDataMonotonicityIndex(Dataset dataset) {
		
		if(dataset.getConstraints().length > 0 && dataset.size() > 1) {
			int violations = 0;
			for(int i = 0; i < dataset.size(); i++) {
				for(int j = 0; j < dataset.size(); j++) {
					if(i != j) {
						if(checkNonMonotonicity(dataset.get(i),dataset.get(j), dataset, dataset.getConstraints()[0])) {
							violations++;		
						}
					}
				}
			}
			return 1.0 - (double) violations / (double)(Math.pow(dataset.size(), 2) - dataset.size());
		} else {
			return 1.0;
		}
	}
	
	/**
	 * Calculates the monotonicity index of a rule list (MI = 1- NMI)
	 * 
	 * @param list - list being tested
	 * @param dataset - dataset containing the constraints
	 * @return MI index for the list
	 */
	public static double calculateMonotonicityIndex(RuleList list, Dataset dataset) {
		
		// Check for monotonic violations in rule list
		if(dataset.getConstraints().length > 0 && list.size() > 1) {
			int violations = 0;
			for(int i = 0; i < list.size(); i++) {
				for(int j = 0; j < list.size(); j++) {
					if(i != j ) {
						if(checkNonMonotonicity(list.rules()[i], list.rules()[j], dataset, dataset.getConstraints()[0])){
							violations++;
						}
					}
				}
			}
			return 1.0 - (double) violations / (double)(Math.pow(list.size(), 2) - list.size());
		} else {
			return 1.0;
		}
	}
	
	/**
	 * Calculates the MI of a single rule, MI = 1-NMI
	 * @param list - partial list
	 * @param rule - rule to be tested
	 * @param dataset - data set containing the constraints
	 * @return MI index for the rule
	 */
	public static double calculateRuleMonotonicity(RuleList list, Rule rule, Dataset dataset) {
		if(dataset.getConstraints().length > 0 && list.size() > 0) {
			int violations = 0;
			for(int i = 0; i < list.size(); i++) {
				if(checkNonMonotonicity(rule,list.rules()[i], dataset, dataset.getConstraints()[0])) {
					violations++;
				}
			}
			return 1.0 - (double) violations / (double)(list.size());
		} else {
			return 0;
		}
	}
	
	/**
	 * Checks if two instances are monotonic
	 * @param i1 - first instance
	 * @param i2 - second instance
	 * @param dataset - data set being used
	 * @param constraint - constraint to check for violations
	 * @return true if there is a violation of the constraint
	 */
	private static boolean checkNonMonotonicity(double[] i1, double[] i2, Dataset dataset, Constraint constraint) {
		double c1 = i1[constraint.getAttributeIndex()];
		double c2 = i2[constraint.getAttributeIndex()];
		double r1 = i1[i1.length-1];
		double r2 = i2[i2.length-1];
		
		if(constraint.getConstraintDirection() == Direction.INCREASING)
		{
			if(c1 >= c2 && r1 >= r2)
			{
				return false;
			}
			
			if(c2 >= c1 && r2 >= r1)
			{
				return false;
			}
		}
		else if(constraint.getConstraintDirection() == Direction.DECREASING)
		{
			if(c1 <= c2 && r1 >= r2)
			{
				return false;
			}
			
			if(c2 <= c1 && r2 >= r1)
			{
				return false;
			}
		}  
		return true;
	}
	
    /**
     * Check the monotonicity of two rules
     * @param r1 - first rule
     * @param r2 - second rule
     * @param dataset - data set being used
     * @param constraint - constraint being checked for violations
     * @return true if the two rules violate a constraint
     */
    private static boolean checkNonMonotonicity(Rule r1, Rule r2, Dataset dataset, Constraint constraint)
    {	
    	Term[] t1 = r1.terms();
    	Term[] t2 = r2.terms();
    	int attributeN = dataset.attributes().length;
    	int ci = constraint.getAttributeIndex();
    	
    	int[] a1 = initaliseAttributeArray(t1, attributeN);
    	int[] a2 = initaliseAttributeArray(t2, attributeN);
    	
    	if(a1[ci] == -1 || a2[ci] == -1)
    	{
    		return false; // cannot constrain if either of the rules do not use the constraint
    	}
    	else
    	{
    		Condition c1 = t1[a1[ci]].condition();
    		Condition c2 = t2[a2[ci]].condition();
    		
    		if(constraint.getConstraintDirection() == Direction.INCREASING)
    		{
    			if(c1.value[0] >= c2.value[0] && r1.getConsequent() >= r2.getConsequent())
    			{
    				return false;
    			}
    			
    			if(c2.value[0] >= c1.value[0] && r2.getConsequent() >= r1.getConsequent())
    			{
    				return false;
    			}
    		}
    		else if(constraint.getConstraintDirection() == Direction.DECREASING)
    		{
    			if(c1.value[0] <= c2.value[0] && r1.getConsequent() >= r2.getConsequent())
    			{
    				return false;
    			}
    			
    			if(c2.value[0] <= c1.value[0] && r2.getConsequent() >= r1.getConsequent())
    			{
    				return false;
    			}
    		}   		
    		// If we get here then we are potentially violating if all other terms in the rule are the same
    		//TODO add checking for other violating terms 		
    		return true;
    	}
    }

    /**
     * Initialises an array to -1 where the length is the number of attributes in the data set
     * @param t -
     * @param attributeN - number of attributes
     * @return
     */
    private static int[] initaliseAttributeArray(Term[] t, int attributeN)
    {
    	int[] a = new int[attributeN];
    	Arrays.fill(a, -1);
    	for(int i = 0; i < t.length; i++)
    	{
    		a[t[i].condition().attribute] = i;
    	}
    	return a;
    }
}
