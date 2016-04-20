/*
 * NMICalc.java
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

import myra.Dataset;
import myra.rule.pittsburgh.monotonicity.Constraint.Direction;
import myra.util.ARFFReader;

/**
 * @author jb765
 *
 */
public class NMICalc {
	public static void main(String[] args) {
		if(args.length != 1) {
			System.out.println("Usage: <input file path:String> ");
			System.exit(1);
		}
		else {
			calculateNMI(args[0]);
		}
	}
	
	public static void calculateNMI(String filename) {
	try {
		ARFFReader reader = new ARFFReader();
		Dataset data = reader.read(filename);		
		Constraint[] constraints = data.getConstraints();
		System.out.println("All Constraints: " + MonotonicFunctions.calculateDataMonotonicityIndex(data));
		for(int i = 0; i < constraints.length; i++) {
			Constraint[] tempc = new Constraint[1];
			data.setConstraints(tempc);
			
			tempc[0] = new Constraint(constraints[i].getAttributeIndex(),Direction.INCREASING);
			System.out.println(data.getAttribute(tempc[0].getAttributeIndex()).getName() + " " + tempc[0].getConstraintDirection() + ": " + MonotonicFunctions.calculateDataMonotonicityIndex(data));
		
			tempc[0] = new Constraint(constraints[i].getAttributeIndex(),Direction.DECREASING);
			System.out.println(data.getAttribute(tempc[0].getAttributeIndex()).getName() + " " + tempc[0].getConstraintDirection() + ": " + MonotonicFunctions.calculateDataMonotonicityIndex(data));
		}
	} catch (Exception e) {
		e.printStackTrace();
	}	
	}
}
