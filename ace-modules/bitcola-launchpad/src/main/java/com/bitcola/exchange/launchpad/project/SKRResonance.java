package com.bitcola.exchange.launchpad.project;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author zkq
 * @create 2019-05-24 16:40
 **/
@Component
public class SKRResonance implements ResonanceProject {

    BigDecimal initPrice = new BigDecimal("0.0000147");
    BigDecimal riseRate = new BigDecimal("0.0032");
    BigDecimal roundPrice = new BigDecimal("50");
    BigDecimal totalNumber = new BigDecimal("1022674107");
    BigDecimal inviterReward = new BigDecimal("0.01");
    BigDecimal inviterUnlock = new BigDecimal("0.1");
    BigDecimal unlock = new BigDecimal("0.25");
    int totalRound = 1000;

    public static void main(String[] args) {
        SKRResonance  ss = new SKRResonance();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < 1000; i++) {
            BigDecimal number = ss.getNumberByRound(i + 1);
            System.out.println("第"+(i+1)+"轮 "+number+" --> "+ss.getPriceByRound(i+1)+"LTC");
            total = total.add(number);
        }
        System.out.println("总数量:"+total);
    }

    @Override
    public String coinCode() {
        return "SKR";
    }

    @Override
    public String symbol() {
        return "LTC";
    }

    @Override
    public int getCurrentRound(BigDecimal buyTotal) {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < getTotalRound(); i++) {
            BigDecimal number = getNumberByRound(i + 1);
            total = total.add(number);
            if (total.compareTo(buyTotal) > 0) return i+1;
        }
        return getTotalRound();
    }


    @Override
    public int getTotalRound() {
        return totalRound;
    }

    @Override
    public BigDecimal getPriceByRound(int round) {
        return initPrice.multiply(BigDecimal.ONE.add(riseRate).pow(round-1)).setScale(7,RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getNumberByRound(int round) {
        return roundPrice.divide(getPriceByRound(round),0, RoundingMode.DOWN);
    }

    @Override
    public BigDecimal getTotalNumber() {
        return totalNumber;
    }

    @Override
    public BigDecimal getCurrentRoundRemain(BigDecimal buyTotal) {
        for (int i = 0; i < getTotalRound(); i++) {
            BigDecimal number = getNumberByRound(i + 1);
            buyTotal = buyTotal.subtract(number);
            if (buyTotal.compareTo(BigDecimal.ZERO) < 0) return buyTotal.negate();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal inviterReward() {
        return inviterReward;
    }

    @Override
    public BigDecimal inviterUnlock() {
        return inviterUnlock;
    }

    @Override
    public BigDecimal unlock() {
        return unlock;
    }


}
