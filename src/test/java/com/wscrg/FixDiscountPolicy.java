package com.wscrg;

public class FixDiscountPolicy implements DiscountPolicy {

    @Override
    public void discount() {
        System.out.println("This policy is a fix price policy.");
    }
}
