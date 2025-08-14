package com.quant.altdata.factor.model;

import com.quant.altdata.data.model.FactorData;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * 深度学习因子模型 - 使用LSTM和全连接层构建的神经网络模型
 */
public class DeepLearningFactorModel implements FactorModel {
    private static final Logger logger = LoggerFactory.getLogger(DeepLearningFactorModel.class);
    private MultiLayerNetwork model;
    private int inputSize;
    private boolean isTrained = false;

    /**
     * 构造函数
     * @param inputSize 输入特征数量
     */
    public DeepLearningFactorModel(int inputSize) {
        this.inputSize = inputSize;
        initializeModel();
    }

    /**
     * 初始化模型架构
     */
    private void initializeModel() {
        int lstmLayerSize = 128;
        int hiddenLayerSize = 64;
        int outputSize = 1;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .l2(0.0001)
                .list()
                .layer(0, new LSTM.Builder()
                        .nIn(inputSize)
                        .nOut(lstmLayerSize)
                        .activation(Activation.TANH)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(1, new DenseLayer.Builder()
                        .nIn(lstmLayerSize)
                        .nOut(hiddenLayerSize)
                        .activation(Activation.RELU)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .nIn(hiddenLayerSize)
                        .nOut(outputSize)
                        .activation(Activation.IDENTITY)
                        .build())
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        logger.info("DeepLearningFactorModel initialized with input size: {}", inputSize);
    }

    @Override
    public void train(List<FactorData> trainingData, int epochs) {
        if (trainingData == null || trainingData.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty");
        }

        logger.info("Training DeepLearningFactorModel with {} samples for {} epochs", trainingData.size(), epochs);

        // 准备训练数据
        INDArray features = Nd4j.create(trainingData.size(), inputSize);
        INDArray labels = Nd4j.create(trainingData.size(), 1);

        for (int i = 0; i < trainingData.size(); i++) {
            FactorData data = trainingData.get(i);
            double[] featureArray = data.getFeatures();
            if (featureArray.length != inputSize) {
                throw new IllegalArgumentException("Feature size mismatch: expected " + inputSize + ", got " + featureArray.length);
            }
            for (int j = 0; j < inputSize; j++) {
                features.putScalar(i, j, featureArray[j]);
            }
            labels.putScalar(i, 0, data.getTarget());
        }

        DataSet dataSet = new DataSet(features, labels);
        SplitTestAndTrain split = dataSet.splitTestAndTrain(0.8);
        DataSet trainData = split.getTrain();
        DataSet testData = split.getTest();

        // 训练模型
        for (int i = 0; i < epochs; i++) {
            model.fit(trainData);
            logger.debug("Epoch {} completed, score: {}", i, model.score());
        }

        // 评估模型
        double testScore = model.evaluate(testData).stats().get("MSE").getDouble(0);
        logger.info("Model training completed. Test MSE: {}", testScore);

        isTrained = true;
    }

    @Override
    public double predict(double[] features) {
        if (!isTrained) {
            throw new IllegalStateException("Model has not been trained yet");
        }

        if (features == null || features.length != inputSize) {
            throw new IllegalArgumentException("Feature size mismatch: expected " + inputSize + ", got " + (features == null ? 0 : features.length));
        }

        INDArray input = Nd4j.create(features);
        INDArray output = model.output(input);
        return output.getDouble(0);
    }

    @Override
    public double evaluate(List<FactorData> testData) {
        if (!isTrained) {
            throw new IllegalStateException("Model has not been trained yet");
        }

        if (testData == null || testData.isEmpty()) {
            throw new IllegalArgumentException("Test data cannot be null or empty");
        }

        INDArray features = Nd4j.create(testData.size(), inputSize);
        INDArray labels = Nd4j.create(testData.size(), 1);

        for (int i = 0; i < testData.size(); i++) {
            FactorData data = testData.get(i);
            for (int j = 0; j < inputSize; j++) {
                features.putScalar(i, j, data.getFeatures()[j]);
            }
            labels.putScalar(i, 0, data.getTarget());
        }

        DataSet testDataSet = new DataSet(features, labels);
        double mse = model.evaluate(testDataSet).stats().get("MSE").getDouble(0);
        logger.info("Model evaluation completed. Test MSE: {}", mse);
        return mse;
    }
}