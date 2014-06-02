package com.github.marschal.svndiffstat;


class TickConfiguration {

  private final int tickLower;
  private final int tickUpper;
  
  TickConfiguration(int tickLower, int tickUpper) {
    this.tickLower = tickLower;
    this.tickUpper = tickUpper;
  }
  
  int getTickLower() {
    return this.tickLower;
  }

  int getTickUpper() {
    return this.tickUpper;
  }
  
}
