/*
 * CostMeasure.java
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

package myra.classification.rule.function;

import static myra.Config.CONFIG;

import myra.Config.ConfigKey;
import myra.Cost.Maximise;
import myra.classification.rule.ClassificationRule;
import myra.datamining.Dataset;
import myra.datamining.Dataset.Instance;

/**
 * The <code>CostMeasure</code> class represents a rule quality function that
 * allows to directly trade off false positive and true positives.
 * 
 * @author Fernando Esteban Barril Otero
 */
public class CostMeasure extends ClassificationRuleFunction {
    /**
     * The config key for the c parameter.
     */
    public static final ConfigKey<Double> C = new ConfigKey<Double>();

    static {
        // default c value
        // see F. Janssen and J. Furnkranz, "On the quest for optimal rule
        // learning heuristics", Machine Learning 78, pp. 343-379, 2010.
        CONFIG.set(C, 0.437);
    }

    @Override
    public Maximise evaluate(Dataset dataset,
                             ClassificationRule rule,
                             Instance[] instances) {
        final double c = CONFIG.get(C);

        int[] frequency = rule.covered();
        int predicted = rule.getConsequent().value();
        int negative = 0;

        for (int i = 0; i < frequency.length; i++) {
            if (i != predicted) {
                negative += frequency[i];
            }
        }

        return new Maximise((c * frequency[predicted]) - ((1 - c) * negative));
    }
}