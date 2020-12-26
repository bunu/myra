/*
 * MedianAssignatorTest.java
 * (this file is part of MYRA)
 * 
 * Copyright 2008-2016 Fernando Esteban Barril Otero
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

package myra.regression.rule;

import java.io.InputStreamReader;

import junit.framework.TestCase;
import myra.datamining.ARFFReader;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * <code>MedianAssignatorTest</code> class test.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class MedianAssignatorTest extends TestCase {
    public void testAssign() {
        try {
            ARFFReader reader = new ARFFReader();

            Dataset dataset = reader.read(new InputStreamReader(getClass()
                    .getResourceAsStream("/temperature.arff")));

            RegressionRule rule = new RegressionRule();
            Instance[] instances = Instance.newArray(dataset.size());
            rule.apply(dataset, instances);

            MedianAssignator assignator = new MedianAssignator();
            assignator.assign(dataset, rule, instances);

            assertEquals(72.0, rule.getConsequent().value());

        } catch (Exception e) {
            fail(e.toString());
        }
    }
}