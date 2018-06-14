package com.sotfk;

public interface BattleEntity {
    public Runnable getCommand();
    public void setCommand(Runnable command);
    public int getStat(String stat);
    public void setStat(String stat, int amt);
    public boolean isActing();
    public void setActing(boolean acting);
    public void takeDmg(int dmgAmt);
    public int getCurHp();
    public void heal(int amt);
}
