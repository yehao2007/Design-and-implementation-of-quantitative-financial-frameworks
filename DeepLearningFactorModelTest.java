package com.quant.altdata.factor.model;

import com.quant.altdata.data.model.FactorData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 深度学习因子模型测试类
 */
public class DeepLearningFactorModelTest {
    private DeepLearningFactorModel model;
    private List<FactorData> testData;

    @BeforeEach
    void setUp() {
        // 初始化模型
        model = new DeepLearningFactorModel(5);

        // 准备测试数据
        testData = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            double[] features = new double[5];
            for (int j = 0; j < 5; j++) {
                features[j] = Math.random();
            }
            double target = features[0] * 0.5 + features[1] * 0.3 + features[2] * 0.2;
            FactorData data = new FactorData(LocalDate.now().minusDays(i), features, target);
            data.setFactorValue(target);
            testData.add(data);
        }
    }

    @Test
    void testTrainAndPredict() {
        // 训练模型
        model.train(testData, 30);

        // 验证模型已训练
        assertTrue(model.isTrained());

        // 测试预测
        FactorData sample = testData.get(0);
        double prediction = model.predict(sample.getFeatures());
        assertNotNull(prediction);
        assertFalse(Double.isNaN(prediction));

        // 测试评估
        double mse = model.evaluate(testData);
        assertTrue(mse < 0.1, "MSE should be less than 0.1");
    }

    @Test
    void testPredictBeforeTraining() {
        // 测试未训练模型的预测
        FactorData sample = testData.get(0);
        assertThrows(IllegalStateException.class, () -> model.predict(sample.getFeatures()));
    }

    @Test
    void testFeatureSizeMismatch() {
        // 测试特征大小不匹配
        double[] wrongFeatures = new double[3];
        assertThrows(IllegalArgumentException.class, () -> model.predict(wrongFeatures));
    }
}