/*
 * EnsembleModel.java
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

package myra.ensemble.bagging;


import static myra.Config.CONFIG;

import java.util.ArrayList;

import myra.Config.ConfigKey;
import myra.classification.Accuracy;
import myra.classification.ClassificationModel;
import myra.classification.Label;
import myra.datamining.Dataset;
import myra.datamining.Model;
import myra.datamining.Prediction;


/**
 * Base model for an ensemble classifier
 *
 * @author Ayah Helal
 * @author James Brookhouse
 */
public class EnsembleModel implements Model {

    public static final ConfigKey<Integer> DEFAULT_COLONIES = new ConfigKey<>();

    /**
     * The wrapped (classification) model.
     */
    private ArrayList<Model> models;
    double[] qualities;

    /**
     * Default constructor for an EnsembleModel.
     */
    public EnsembleModel() {
		models = new ArrayList<>(CONFIG.get(DEFAULT_COLONIES));
		qualities = new double [(int)CONFIG.get(DEFAULT_COLONIES)];
    }

	/**
	 * Adds a model to the ensemble.
	 *
	 * @param model
	 * 			The model to be added.
	 */
	public void add(Model model) {
		models.add(model);
    }

	/**
	 * Uses the provided dataset to evaluate the models in the ensemble and assign a quality for each model.
	 *
	 * @param dataset
	 * 			The dataset to test on.
	 */
	public void setQuality(Dataset dataset) {
		for (int i = 0; i < models.size(); i++) {
			ClassificationModel rules =  new ClassificationModel(models.get(i));
			Accuracy measure = new Accuracy();
			double accuracy = measure.evaluate(dataset, rules).raw();
			qualities[i] = accuracy;
		}
    }

    /*
     * (non-Javadoc)
     * 
     * @see myra.datamining.Model#predict(myra.datamining.Dataset, int)
     */
    @Override
    public Prediction predict(Dataset dataset, int instance) {
		double[] frequency = new double[dataset.classLength()];
		for (int i = 0; i < models.size(); i++) {
			Label prediction = (Label) models.get(i).predict(dataset, instance);
			frequency[prediction.value()] += qualities[i];
		}
		double max = 0;
		int selectedindex = 0;
		for (int i = 0; i < frequency.length; i++) {
			if (frequency[i] > max) {
			selectedindex = i;
			max = frequency[i];
			}
		}
		return new Label(dataset.getAttribute(dataset.classIndex()), selectedindex);
    }

    /*
     * (non-Javadoc)
     * 
     * @see myra.datamining.Model#toString(myra.datamining.Dataset)
     */
    @Override
    public String toString(Dataset dataset) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < models.size(); i++) {
			builder.append("**** Model ");
			builder.append(i);
			builder.append(" ****\n");
			builder.append(models.get(i).toString(dataset)).append("\n");
			builder.append("******************************** \n");
		}
		return builder.toString();
	}

    /*
     * (non-Javadoc)
     * 
     * @see myra.datamining.Model#export(myra.datamining.Dataset)
     */
    @Override
    public String export(Dataset dataset) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < models.size(); i++) {
			builder.append("**** Model ");
			builder.append(i);
			builder.append(" ****\n");
			builder.append(models.get(i).export(dataset));
			builder.append("********************************\n");
		}
		return builder.toString();
    }
}
