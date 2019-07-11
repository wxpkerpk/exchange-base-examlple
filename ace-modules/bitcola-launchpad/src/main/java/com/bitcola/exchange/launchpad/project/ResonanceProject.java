package com.bitcola.exchange.launchpad.project;

import java.math.BigDecimal;

public interface ResonanceProject {

    String coinCode();
    String symbol();
    int getCurrentRound(BigDecimal buyTotal);
    int getTotalRound();
    BigDecimal getPriceByRound(int round);
    BigDecimal getNumberByRound(int round);
    BigDecimal getTotalNumber();
    BigDecimal getCurrentRoundRemain(BigDecimal buyTotal);
    BigDecimal inviterReward();
    BigDecimal inviterUnlock();
    BigDecimal unlock();

}
