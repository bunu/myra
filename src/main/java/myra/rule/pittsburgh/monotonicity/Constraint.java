/*
 * Constraint.java
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

import myra.Config.ConfigKey;

/**
 * Defines a monotonic constraint
 * @author James Brookhouse
 *
 */
public class Constraint 
{
    /**
     * The config key for the constraint weighting.
     */
    public final static ConfigKey<Double> CONSTRAINT_WEIGHTING = new ConfigKey<>();
	
	/**
	 * Attribute index of the rule being constrained
	 */
	private int index;
	
	/**
	 * Direction of the rule, monotonically increasing or decreasing
	 */
	private Direction direction;
	
	/**
	 * Creates a constraint based on the attribute index and its direction
	 * @param index
	 * @param direction
	 */
	public Constraint(int index, Direction direction) 
	{
		this.index = index;
		this.direction = direction;
	}
	
	/**
	 * Gets the constrained attribute index
	 * @return
	 */
	public int getAttributeIndex() 
	{
		return index;
	}
	
	/**
	 * Gets the direction of the constraint
	 * @return
	 */
	public Direction getConstraintDirection() 
	{
		return direction;
	}
	
	/**
	 * Enum to specify the direction of a constraint, this can either be monotonically 
	 * increasing or monotonically decreasing
	 * @author jb765
	 *
	 */
	public enum Direction 
	{
		INCREASING,
		DECREASING
	}
}
