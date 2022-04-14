/*
 * EnsembleMixedAttributeAntMiner.java
 * (this file is part of MYRA)
 *
 * Copyright 2008-2025
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

package myra.algorithm;

import static myra.Config.CONFIG;
import static myra.Scheduler.COLONY_SIZE;
import static myra.ensemble.bagging.EnsembleModel.DEFAULT_COLONIES;
import static myra.ensemble.bagging.FeatureBagging.DEFAULT_FEATURE_BAGGING;
import static myra.ensemble.bagging.InstanceBootstrapping.DEFAULT_INSTANCE_BOOTSTRAPPING;
import static myra.rule.Heuristic.DEFAULT_HEURISTIC;
import static myra.rule.Heuristic.DYNAMIC_HEURISTIC;
import static myra.rule.irl.RuleFactory.DEFAULT_FACTORY;

import java.util.ArrayList;
import java.util.Collection;

import myra.Option;

import myra.Config.ConfigKey;
import myra.Option.DoubleOption;
import myra.Option.IntegerOption;

import myra.classification.ClassificationModel;
import myra.datamining.Dataset;
import myra.ensemble.bagging.BaggingArchiveRuleFactory;
import myra.ensemble.bagging.EnsembleModel;
import myra.ensemble.bagging.FeatureBagging;
import myra.ensemble.bagging.InstanceBootstrapping;

import myra.rule.archive.Graph;
import myra.rule.irl.SequentialCovering;

/**
 * Executable class file for the <code><i>e</i>Ant-Miner<sub>HMA</sub></code>
 * algorithm.
 *
 * @author Ayah Helal
 * @author James Brookhouse
 */
public class EnsembleMixedAttributeAntMiner extends MixedAttributeAntMiner {

    public static final ConfigKey<Double> DEFAULT_VALIDATION = new ConfigKey<>();
    
    @Override
    protected void defaults() {
        super.defaults();

        // configuration not set via command line
        CONFIG.set(DEFAULT_FACTORY, new BaggingArchiveRuleFactory());

        // default configuration values
        CONFIG.set(DEFAULT_FEATURE_BAGGING, 0.4);
        CONFIG.set(DEFAULT_INSTANCE_BOOTSTRAPPING, 0.4);
        CONFIG.set(DEFAULT_VALIDATION, 0.2);
        CONFIG.set(DEFAULT_COLONIES, 10);
        CONFIG.set(COLONY_SIZE, 10);
    }

    @Override
    protected Collection<Option<?>> options() {
        ArrayList<Option<?>> options = new ArrayList<>();

        for (Option<?> option : super.options()) {
            if (option.getKey() != DEFAULT_HEURISTIC
                    && option.getKey() != DYNAMIC_HEURISTIC) {
                options.add(option);
            }
        }

        // percentage of bagged features
        options.add(new DoubleOption(DEFAULT_FEATURE_BAGGING,
                                     "fb",
                                     "specify the percentage of feature bagging %s",
                                     "value"));
        
        // percentage of bagged features
        options.add(new IntegerOption(DEFAULT_COLONIES,
                                     "ec",
                                     "specify the number of different colonies %s",
                                     "value"));
        
        // percentage of bagged instances
        options.add(new DoubleOption(DEFAULT_INSTANCE_BOOTSTRAPPING,
                                     "ib",
                                     "specify the percentage of instance bagging %s",
                                     "value"));
        
        // percentage of bagged instances
        options.add(new DoubleOption(DEFAULT_VALIDATION,
                                     "va",
                                     "specify the percentage of the dataset to be used for validation %s",
                                     "value"));

        return options;
    }

    @Override
    public ClassificationModel train(Dataset dataset) {
        int colonies = CONFIG.get(DEFAULT_COLONIES);
        EnsembleModel model = new EnsembleModel();

        for(int i=0 ; i < colonies;i++) {
            Dataset set = InstanceBootstrapping.bootstrapping(dataset,true);
            set.setMask(FeatureBagging.generateBaggingMask(dataset.attributes().length));
            SequentialCovering seco = new SequentialCovering();
            model.add(new ClassificationModel(seco.train(dataset, new Graph(dataset))));
        }
        model.setQuality(dataset);
        return new ClassificationModel(model);
    }

    @Override
    public String description() {
        return "Ensemble Mixed-Attribute Ant-Miner";
    }

    /**
     * <code>Ant-Miner<sub>HMA</sub></code> entry point.
     * 
     * @param args
     *            command-line arguments.
     * 
     * @throws Exception
     *             If an error occurs &mdash; e.g., I/O error.
     */
    public static void main(String[] args) throws Exception {
	EnsembleMixedAttributeAntMiner algorithm = new EnsembleMixedAttributeAntMiner();
        algorithm.run(args);
    }
}
