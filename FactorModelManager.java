package com.quant.altdata.factor.model;

import com.quant.altdata.data.model.FactorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 因子模型管理器 - 管理和维护各种因子模型
 */
public class FactorModelManager {
    private static final Logger logger = LoggerFactory.getLogger(FactorModelManager.class);
    private final Map<String, FactorModel> models;
    private final Properties config;

    /**
     * 构造函数
     * @param config 配置属性
     */
    public FactorModelManager(Properties config) {
        this.config = config;
        this.models = new HashMap<>();
        initializeDefaultModels();
    }

    /**
     * 初始化默认模型
     */
    private void initializeDefaultModels() {
        // 从配置中读取特征数量
        int featureCount = Integer.parseInt(config.getProperty("model.feature.count", "10"));

        // 创建默认模型
        models.put("linear", new LinearRegressionModel(featureCount));
        models.put("deeplearning", new DeepLearningFactorModel(featureCount));

        // 初始化集成模型
        EnsembleModel ensembleModel = new EnsembleModel();
        ensembleModel.addModel(models.get("linear"), 0.3);
        ensembleModel.addModel(models.get("deeplearning"), 0.7);
        models.put("ensemble", ensembleModel);

        logger.info("Default models initialized: {}", models.keySet());
    }

    /**
     * 训练指定模型
     * @param modelName 模型名称
     * @param trainingData 训练数据
     * @param epochs 训练轮数
     */
    public void trainModel(String modelName, List<FactorData> trainingData, int epochs) {
        if (!models.containsKey(modelName)) {
            throw new IllegalArgumentException("Model not found: " + modelName);
        }

        try {
            FactorModel model = models.get(modelName);
            model.train(trainingData, epochs);
            logger.info("Model {} trained successfully", modelName);
        } catch (Exception e) {
            logger.error("Failed to train model {}: {}", modelName, e.getMessage(), e);
            throw new RuntimeException("Model training failed", e);
        }
    }

    /**
     * 获取指定模型
     * @param modelName 模型名称
     * @return 模型实例
     */
    public FactorModel getModel(String modelName) {
        if (!models.containsKey(modelName)) {
            throw new IllegalArgumentException("Model not found: " + modelName);
        }
        return models.get(modelName);
    }

    /**
     * 注册新模型
     * @param name 模型名称
     * @param model 模型实例
     */
    public void registerModel(String name, FactorModel model) {
        if (models.containsKey(name)) {
            logger.warn("Overriding existing model: {}", name);
        }
        models.put(name, model);
        logger.info("Model registered: {}", name);
    }
}