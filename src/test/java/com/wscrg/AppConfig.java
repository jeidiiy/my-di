package com.wscrg;

@Configuration
@ComponentScan
public class AppConfig {

    public AppConfig() {
    }

    @Bean
    public DiscountPolicy getFixDiscountPolicy() {
        return new FixDiscountPolicy();
    }
}
