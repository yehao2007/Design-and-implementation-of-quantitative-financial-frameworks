package com.quant.altdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * 应用程序入口类
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting AltDataFactorEngine");

        try {
            // 加载配置
            Properties config = loadConfig("config.properties");

            // 初始化引擎
            AltDataFactorEngine engine = new AltDataFactorEngine(config);
            logger.info("AltDataFactorEngine initialized successfully");

            // 示例：从新闻数据源获取数据
            LocalDateTime startDate = LocalDateTime.now().minusDays(30);
            LocalDateTime endDate = LocalDateTime.now();
            var factorData = engine.fetchData("news", "stock market", startDate, endDate);
            logger.info("Fetched {} factor data points", factorData.size());

            // 示例：训练模型
            engine.trainModel("ensemble", factorData, 50);

            // 示例：回测策略
            var backtestResult = engine.backtestStrategy("momentum", "ensemble", factorData);
            logger.info("Backtest completed. Sharpe Ratio: {}", backtestResult.getSharpeRatio());

            // 示例：可视化结果
            engine.visualizeResults(factorData, backtestResult);

        } catch (Exception e) {
            logger.error("Application failed: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * 加载配置文件
     * @param configFile 配置文件路径
     * @return 配置属性
     * @throws IOException 当配置文件加载失败时抛出
     */
    private static Properties loadConfig(String configFile) throws IOException {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);
            logger.info("Configuration loaded successfully from {}", configFile);
            return config;
        } catch (IOException e) {
            logger.error("Failed to load configuration from {}: {}", configFile, e.getMessage());
            throw e;
        }
    }
}