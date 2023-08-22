package ru.violence.twonyone;

import lombok.experimental.UtilityClass;
import ru.violence.coreapi.common.message.MessageKey;

@UtilityClass
public class LangKeys {
    public final MessageKey CHAIR_OCCUPIED = of("chair-occupied");
    public final MessageKey OPPONENT_LEAVED = of("opponent-leaved");
    public final MessageKey TABLE_GOT_OCCUPIED = of("table-got-occupied");

    public final MessageKey TITLE_AWAITING_OPPONENT = of("title.awaiting-opponent");
    public final MessageKey TITLE_OPPONENT_DRAW = of("title.opponent-draw");
    public final MessageKey TITLE_OPPONENT_KEEP = of("title.opponent-keep");
    public final MessageKey TITLE_CANT_DRAW_MORE = of("title.cant-draw-more");

    public final MessageKey HOLO_WAITING_1 = of("holo.waiting-1");
    public final MessageKey HOLO_WAITING_2 = of("holo.waiting-2");
    public final MessageKey HOLO_WAITING_3 = of("holo.waiting-3");
    public final MessageKey HOLO_SCORE_YOU = of("holo.score-you");
    public final MessageKey HOLO_SCORE_OPPONENT = of("holo.score-opponent");
    public final MessageKey HOLO_SCORE_OPPONENT_REVEAL = of("holo.score-opponent-reveal");
    public final MessageKey HOLO_SCORE_PUBLIC_1 = of("holo.score-public-1");
    public final MessageKey HOLO_SCORE_PUBLIC_2 = of("holo.score-public-2");
    public final MessageKey HOLO_SCORE_PUBLIC_1_REVEAL = of("holo.score-public-1-reveal");
    public final MessageKey HOLO_SCORE_PUBLIC_2_REVEAL = of("holo.score-public-2-reveal");

    public final MessageKey MENU_GAME_HELP = of("menu.game-help");

    public final MessageKey MENU_BET_ADJUST_TITLE = of("menu.bet-adjust.title");
    public final MessageKey MENU_BET_ADJUST_INFO = of("menu.bet-adjust.info");
    public final MessageKey MENU_BET_ADJUST_REDUCE = of("menu.bet-adjust.reduce");
    public final MessageKey MENU_BET_ADJUST_INCREASE = of("menu.bet-adjust.increase");
    public final MessageKey MENU_BET_ADJUST_CONFIRM = of("menu.bet-adjust.confirm");
    public final MessageKey MENU_BET_ADJUST_CANCEL = of("menu.bet-adjust.cancel");

    public final MessageKey MENU_BET_CONFIRM_TITLE = of("menu.bet-confirm.title");
    public final MessageKey MENU_BET_CONFIRM_INFO = of("menu.bet-confirm.info");
    public final MessageKey MENU_BET_CONFIRM_CONFIRM = of("menu.bet-confirm.confirm");
    public final MessageKey MENU_BET_CONFIRM_CANCEL = of("menu.bet-confirm.cancel");

    public final MessageKey MENU_QUIT_TITLE = of("menu.quit.title");
    public final MessageKey MENU_QUIT_CONFIRM = of("menu.quit.confirm");
    public final MessageKey MENU_QUIT_CANCEL = of("menu.quit.cancel");

    public final MessageKey GAME_TIME_LEFT_TO_THINK = of("game.time-left-to-think");
    public final MessageKey GAME_PLAYER_DRAW = of("game.player-draw");
    public final MessageKey GAME_PLAYER_KEEP = of("game.player-keep");
    public final MessageKey GAME_ITEM_LEAVE = of("game.item.leave");
    public final MessageKey GAME_ITEM_DRAW = of("game.item.draw");
    public final MessageKey GAME_ITEM_KEEP = of("game.item.keep");
    public final MessageKey GAME_ALL_KEEPS_PRE_END = of("game.all-keeps-pre-end");
    public final MessageKey GAME_END_PLAYER_LEAVE = of("game.end.player-leave");
    public final MessageKey GAME_END_WIN = of("game.end.win");
    public final MessageKey GAME_END_TIE = of("game.end.tie");

    private MessageKey of(String key) {
        return MessageKey.of("twonyone." + key);
    }
}
