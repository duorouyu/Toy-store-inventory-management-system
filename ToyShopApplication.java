package com.toyshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

/**
 * 玩具店销售收银库存管理系统 —— 后端启动入口
 * <p>
 * 排除 Redis 自动装配：本地毕设演示使用进程内内存缓存（见 RedisConfig），
 * 无需安装 Redis 服务即可直接启动。
 * </p>
 */
@SpringBootApplication(exclude = { RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class })
@MapperScan("com.toyshop.mapper")
public class ToyShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToyShopApplication.class, args);
    }
}
