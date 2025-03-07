package cardgame.service.battle;

public class BattleResult {
    private final boolean matchFound;
    private final String battleLog;

    public BattleResult(boolean matchFound, String battleLog) {
        this.matchFound = matchFound;
        this.battleLog = battleLog;
    }

    public boolean isMatchFound() {
        return matchFound;
    }

    public String getBattleLog() {
        return battleLog;
    }
}
