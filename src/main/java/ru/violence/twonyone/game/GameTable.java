package ru.violence.twonyone.game;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.coreapi.bukkit.api.util.BukkitHelper;
import ru.violence.coreapi.common.api.message.MessageKey;
import ru.violence.coreapi.common.api.user.NotEnoughCoinsException;
import ru.violence.coreapi.common.api.user.User;
import ru.violence.coreapi.common.api.util.Check;
import ru.violence.coreapi.common.user.transaction.TransactionCause;
import ru.violence.coreapi.common.user.transaction.TransactionSource;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.config.Config;
import ru.violence.twonyone.game.task.TurnExpireTask;
import ru.violence.twonyone.util.CardHoloHelper;
import ru.violence.twonyone.util.Holo;
import ru.violence.twonyone.util.LangHelper;
import ru.violence.twonyone.util.TaskHolder;
import ru.violence.twonyone.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class GameTable {
    public static final int WIN_POINTS = 21;

    private final @Getter @NotNull BlockFace direction;
    private final @Getter @NotNull GameChair chairOne;
    private final @Getter @NotNull GameChair chairTwo;
    private final @Getter @NotNull Holo waitingHolo1;
    private final @Getter @NotNull Holo waitingHolo2;
    private final @Getter @NotNull Holo waitingHolo3;
    private final @NotNull Location scoreHoloLoc;
    private final int broadcastNearbyRadius;

    private @Getter @NotNull State state = State.WAITING;
    private final List<BukkitTask> tasks = new ArrayList<>();
    private final List<Holo> holograms = new ArrayList<>();
    private Bet bet;
    private GameScore score;
    private GameChair currentTurnChair;
    private Holo scoreHoloPublicBet;
    private Holo scoreHoloPublic1;
    private Holo scoreHoloPublic2;
    private final TaskHolder turnExpireTask = new TaskHolder();
    private boolean lastWasKeep;

    public GameTable(@NotNull BlockFace direction, @NotNull List<Location> chairOneLocs, @NotNull List<Location> chairTwoLocs, @NotNull Location scoreHoloLoc, int broadcastNearbyRadius) {
        Check.isTrue(direction == BlockFace.NORTH
                || direction == BlockFace.EAST
                || direction == BlockFace.SOUTH
                || direction == BlockFace.WEST, "Illegal direction");
        this.direction = direction;
        this.chairOne = new GameChair(this, chairOneLocs);
        this.chairTwo = new GameChair(this, chairTwoLocs);
        this.waitingHolo1 = new Holo(scoreHoloLoc);
        this.waitingHolo2 = new Holo(scoreHoloLoc.clone().add(0, Holo.LINE_OFFSET, 0));
        this.waitingHolo3 = new Holo(scoreHoloLoc.clone().add(0, Holo.LINE_OFFSET * 2, 0));
        this.scoreHoloLoc = scoreHoloLoc;
        this.broadcastNearbyRadius = broadcastNearbyRadius;
        spawnWaitingHolo(false);
    }

    void addPlayer(@NotNull GamePlayer gamePlayer, @NotNull GameChair chair, @Nullable Bet bet) {
        Check.isTrue(state == State.WAITING, () -> "Wrong game state: " + state.name());
        Check.isTrue(!chair.isOccupied(), "Chair is already occupied");

        chair.sit(gamePlayer);

        if (bet != null) {
            Check.isTrue(!isBetSet(), "Bet is already set");
            this.bet = bet;
        }

        if (!getOppositeChair(chair).isOccupied()) {
            state = State.WAITING;
            LangHelper.sendTitle(gamePlayer.getPlayer(), LangKeys.TITLE_AWAITING_OPPONENT);
            spawnWaitingHolo(true);
            return;
        }

        startGame();
    }

    void removePlayer(@NotNull Player player, boolean triggerReset) {
        GameChair chair = Check.notNull(getChair(player), "Chair for " + player.getName() + " not found");

        if (state == State.WAITING) {
            if (triggerReset) reset();
            return;
        }

        if (state == State.PLAYING) {
            stop(EndReason.PLAYER_LEAVE, chair.getOpposite());
        }

        chair.stand();
    }

    void startGame() {
        Check.isTrue(state == State.WAITING, "Wrong game state: " + state.name());
        Check.notNull(bet, "Has no bet");
        Check.isTrue(getChairOne().isOccupied(), "Chair one is not occupied");
        Check.isTrue(getChairTwo().isOccupied(), "Chair two is not occupied");

        state = State.PLAYING;
        score = new GameScore(this);

        // Clear the title for player
        getHostChair().getPlayer().sendTitle("", "");

        try {
            User userHost = BukkitHelper.getUser(getHostChair().getPlayer()).get();
            User userGuest = BukkitHelper.getUser(getHostChair().getOpposite().getPlayer()).get();

            if (userHost.getDonateCoins() < bet.getAmount()) {
                throw new NotEnoughCoinsException(userHost, bet.getAmount());
            }
            if (userGuest.getDonateCoins() < bet.getAmount()) {
                throw new NotEnoughCoinsException(userGuest, bet.getAmount());
            }

            userHost.withdrawDonateCoins(bet.getAmount(), TransactionCause.PLUGIN, TransactionSource.PLUGIN, "TwonyOne bet (host)");
            userGuest.withdrawDonateCoins(bet.getAmount(), TransactionCause.PLUGIN, TransactionSource.PLUGIN, "TwonyOne bet (guest)");
        } catch (NotEnoughCoinsException e) {
            e.tellAboutIt();
            stop(EndReason.PAY_FAIL);
            return;
        }

        destroyWaitingHolo();
        broadcastSound(Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);

        // Roll first cards with nice animation, wow
        addTask(new BukkitRunnable() {
            int step = 0;

            @Override
            public void run() {
                switch (step) {
                    case 0: {
                        rollCard(chairOne, true);
                        break;
                    }
                    case 1: {
                        rollCard(chairTwo, true);
                        break;
                    }
                    case 2: {
                        rollCard(chairOne, false);
                        break;
                    }
                    case 3: {
                        rollCard(chairTwo, false);
                        Bukkit.getScheduler().runTaskLater(TwonyOnePlugin.getInstance(),
                                () -> tryPassTurnToNext(null), 20);
                        cancel();
                        return;
                    }
                }

                ++step;
            }
        }.runTaskTimer(TwonyOnePlugin.getInstance(), 20, 10));
    }

    public void stop(@NotNull EndReason reason) {
        stop(reason, null);
    }

    public void stop(@NotNull EndReason reason, @Nullable GameChair winner) {
        Check.isTrue(state == State.PLAYING);

        cancelTasks();
        turnExpireTask.cancel();
        state = State.ENDING;

        switch (reason) {
            case PAY_FAIL: {
                // Instantly reset
                reset();
                return;
            }
            case PLAYER_LEAVE: {
                // WTF? o_O
                if (winner == null) {
                    reset();
                    return;
                }

                Player winnerPlayer = winner.getPlayer();
                rewardWinner(winnerPlayer);

                broadcastMessageNearby(LangKeys.GAME_END_PLAYER_LEAVE.setArgs(winnerPlayer.getName(), winner.getOpposite().getPlayer().getName(), Utils.calculateAmountWithFee(bet.getAmount() * 2, Config.BET_FEE)), true);
                playWinSound(winner);

                updateScoreHolograms(true);
                CardHoloHelper.revealSecretCard(this);
                break;
            }
            case ALL_KEEPS: {
                winner = calcWinner();

                // Payout and broadcast
                if (winner != null) {
                    rewardWinner(winner.getPlayer());

                    broadcastMessageNearby(LangKeys.GAME_END_WIN.setArgs(winner.getPlayer().getName(), Utils.calculateAmountWithFee(bet.getAmount() * 2, Config.BET_FEE)), true);
                    playWinSound(winner);
                    playLoseSound(winner.getOpposite());
                } else {
                    refundTie();

                    broadcastMessageNearby(LangKeys.GAME_END_TIE, true);
                    playTieSound();
                }

                updateScoreHolograms(true);
                CardHoloHelper.revealSecretCard(this);
                break;
            }
            case PLUGIN_DISABLE: {
                // Payout without fee and instantly reset
                refundPluginDisable();
                reset(true);
                return;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + reason);
        }

        addTask(Bukkit.getScheduler().runTaskLater(TwonyOnePlugin.getInstance(), this::reset, 8 * 20));
    }

    private void playWinSound(@NotNull GameChair winner) {
        winner.getPlayer().playSound(getCenter(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f);
        broadcastSoundNearby(Sound.ENTITY_PLAYER_LEVELUP, 1f, 0.8f, false);
    }

    private void playLoseSound(@NotNull GameChair loser) {
        loser.getPlayer().playSound(getCenter(), Sound.ENTITY_EVOCATION_ILLAGER_PREPARE_WOLOLO, 1f, 1f);
    }

    private void playTieSound() {
        broadcastSoundNearby(Sound.ENTITY_EVOCATION_ILLAGER_AMBIENT, 1f, 1.1f);
    }

    private @Nullable GameChair calcWinner() {
        int scoreOne = getChairOne().getTotalScore();
        int scoreTwo = getChairTwo().getTotalScore();

        // Draw
        if (scoreOne == scoreTwo) return null;

        boolean isAllLose = scoreOne > WIN_POINTS && scoreTwo > WIN_POINTS;

        if (isAllLose) {
            // The one with fewer points has won
            return scoreOne < scoreTwo ? getChairOne() : getChairTwo();
        } else {
            // Someone has gone overboard
            if (scoreOne > WIN_POINTS) return getChairTwo();
            if (scoreTwo > WIN_POINTS) return getChairOne();
            // The one with more has won
            return scoreOne > scoreTwo ? getChairOne() : getChairTwo();
        }
    }

    public void reset() {
        reset(false);
    }

    public void reset(boolean onDisable) {
        cancelTasks();
        for (Player player : getPlayers()) {
            TwonyOnePlugin.getInstance().getGameManager().removeFromGame(player, false);
        }
        state = State.WAITING;
        for (Holo holo : holograms) holo.destroy();
        holograms.clear();
        chairOne.reset();
        chairTwo.reset();
        bet = null;
        score = null;
        currentTurnChair = null;
        if (scoreHoloPublicBet != null) {
            scoreHoloPublicBet.destroy();
            scoreHoloPublicBet = null;
        }
        if (scoreHoloPublic1 != null) {
            scoreHoloPublic1.destroy();
            scoreHoloPublic1 = null;
        }
        if (scoreHoloPublic2 != null) {
            scoreHoloPublic2.destroy();
            scoreHoloPublic2 = null;
        }
        turnExpireTask.cancel();
        lastWasKeep = false;
        if (!onDisable) spawnWaitingHolo(false);
    }

    private void spawnWaitingHolo(boolean waitingForOther) {
        if (waitingForOther) {
            waitingHolo1.setText(LangKeys.HOLO_WAITING_FOR_OTHER_1.setArgs(bet.getAmount()));
            waitingHolo2.setText(LangKeys.HOLO_WAITING_FOR_OTHER_2.setArgs(bet.getAmount()));
            waitingHolo3.setText(LangKeys.HOLO_WAITING_FOR_OTHER_3.setArgs(bet.getAmount()));
        } else {
            waitingHolo1.setText(LangKeys.HOLO_WAITING_1);
            waitingHolo2.setText(LangKeys.HOLO_WAITING_2);
            waitingHolo3.setText(LangKeys.HOLO_WAITING_3);
        }
    }

    private void destroyWaitingHolo() {
        waitingHolo1.destroy();
        waitingHolo2.destroy();
        waitingHolo3.destroy();
    }

    private void cancelTasks() {
        for (BukkitTask task : tasks) task.cancel();
        tasks.clear();
    }

    public void onTurnExpire() {
        onTurnKeep();
    }

    public void onTurnDraw() {
        if (currentTurnChair == null) return;

        if (getScore().getTotal(currentTurnChair) >= GameTable.WIN_POINTS) {
            LangHelper.sendTitle(currentTurnChair.getPlayer(), LangKeys.TITLE_CANT_DRAW_MORE);
            return;
        }

        lastWasKeep = false;

        Card rolled = rollCard(currentTurnChair, false);
        broadcastMessageNearby(LangKeys.GAME_PLAYER_DRAW.setArgs(currentTurnChair.getPlayer().getName(), rolled.getPoints()), true);
        LangHelper.sendTitle(currentTurnChair.getOpposite().getPlayer(), LangKeys.TITLE_OPPONENT_DRAW.setArgs(rolled.getPoints()));
        currentTurnChair.getPlayer().sendActionBar(" ");

        GameChair prevTurn = currentTurnChair;
        currentTurnChair.getGamePlayer().hideTurnItems();
        currentTurnChair = null;
        turnExpireTask.cancel();

        addTask(Bukkit.getScheduler().runTaskLater(TwonyOnePlugin.getInstance(), () -> tryPassTurnToNext(prevTurn), 20));
    }

    public void onTurnKeep() {
        if (currentTurnChair == null) return;

        boolean allKeeps = lastWasKeep;
        lastWasKeep = true;

        broadcastMessageNearby(LangKeys.GAME_PLAYER_KEEP.setArgs(currentTurnChair.getPlayer().getName()), true);
        LangHelper.sendTitle(currentTurnChair.getOpposite().getPlayer(), LangKeys.TITLE_OPPONENT_KEEP);
        currentTurnChair.getPlayer().sendActionBar(" ");
        broadcastSoundNearby(Sound.BLOCK_NOTE_XYLOPHONE, 1, 1);

        GameChair prevTurn = currentTurnChair;
        currentTurnChair.getGamePlayer().hideTurnItems();
        currentTurnChair = null;
        turnExpireTask.cancel();

        if (allKeeps) {
            broadcastMessage(LangKeys.GAME_ALL_KEEPS_PRE_END);
            broadcastSoundNearby(Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.75f, 1);
            addTask(Bukkit.getScheduler().runTaskLater(TwonyOnePlugin.getInstance(), () -> stop(EndReason.ALL_KEEPS), 5 * 20));
        } else {
            addTask(Bukkit.getScheduler().runTaskLater(TwonyOnePlugin.getInstance(), () -> tryPassTurnToNext(prevTurn), 20));
        }
    }

    public void updateScoreHolograms(boolean revealClosed) {
        for (GameChair chair : getChairs()) {
            chair.updateScoreText(revealClosed);
        }

        { // Public
            if (scoreHoloPublicBet == null) {
                scoreHoloPublicBet = new Holo(getScoreHoloLoc().add(0, -Holo.LINE_OFFSET, 0));
                scoreHoloPublicBet.setText(LangKeys.HOLO_SCORE_PUBLIC_BET.setArgs(bet.getAmount()));
            }
            if (scoreHoloPublic1 == null) {
                scoreHoloPublic1 = new Holo(getScoreHoloLoc());
                scoreHoloPublic1.setCanViewFilter(player -> !hasPlayer(player));
            }
            if (scoreHoloPublic2 == null) {
                scoreHoloPublic2 = new Holo(getScoreHoloLoc().add(0, Holo.LINE_OFFSET, 0));
                scoreHoloPublic2.setCanViewFilter(player -> !hasPlayer(player));
            }

            GameChair chairOne = getChairOne();
            GameChair chairTwo = getChairTwo();
            String chairOneName = chairOne.getPlayer() != null ? chairOne.getPlayer().getName() : "???";
            String chairTwoName = chairTwo.getPlayer() != null ? chairTwo.getPlayer().getName() : "???";

            if (revealClosed) {
                scoreHoloPublic1.setText(LangKeys.HOLO_SCORE_PUBLIC_1_REVEAL.setArgs(chairOneName, getScore().getTotal(chairOne), GameTable.WIN_POINTS));
                scoreHoloPublic2.setText(LangKeys.HOLO_SCORE_PUBLIC_2_REVEAL.setArgs(chairTwoName, getScore().getTotal(chairTwo), GameTable.WIN_POINTS));
            } else {
                scoreHoloPublic1.setText(LangKeys.HOLO_SCORE_PUBLIC_1.setArgs(chairOneName, getScore().getPublicTotal(chairOne), GameTable.WIN_POINTS));
                scoreHoloPublic2.setText(LangKeys.HOLO_SCORE_PUBLIC_2.setArgs(chairTwoName, getScore().getPublicTotal(chairTwo), GameTable.WIN_POINTS));
            }
        }
    }

    private void tryPassTurnToNext(@Nullable GameChair previous) {
        if (previous == null) {
            currentTurnChair = getHostChair();
        } else {
            currentTurnChair = previous.getOpposite();
        }

        currentTurnChair.getGamePlayer().giveTurnItems();
        currentTurnChair.getPlayer().playSound(currentTurnChair.getPlayer().getLocation(), Sound.UI_TOAST_IN, 1, 1);
        turnExpireTask.setTask(new TurnExpireTask(currentTurnChair, 30).runTaskTimer(TwonyOnePlugin.getInstance(), 0, 20));
    }

    private void broadcastMessage(@NotNull MessageKey message) {
        for (Player player : getPlayers()) {
            LangHelper.sendMessage(player, message);
        }
    }

    // TODO: Use this
    private void broadcastMessageNearby(@NotNull MessageKey message, boolean includeThisPlayers) {
        if (broadcastNearbyRadius <= 0) return;
        Location center = getCenter();

        for (Player player : center.getNearbyPlayers(broadcastNearbyRadius)) {
            // Don't disturb other players
            if ((includeThisPlayers && getPlayers().contains(player)) ||
                    !TwonyOnePlugin.getInstance().getGameManager().isInGame(player)) {
                LangHelper.sendMessage(player, message);
            }
        }
    }

    private void broadcastSound(@NotNull Sound sound, float volume, float pitch) {
        Location center = getCenter();

        for (Player player : getPlayers()) {
            player.playSound(center, sound, volume, pitch);
        }
    }

    private void broadcastSoundNearby(@NotNull Sound sound, float volume, float pitch) {
        broadcastSoundNearby(sound, volume, pitch, true);
    }

    private void broadcastSoundNearby(@NotNull Sound sound, float volume, float pitch, boolean includeThisPlayers) {
        Location center = getCenter();

        for (Player player : center.getNearbyPlayers(broadcastNearbyRadius)) {
            // Don't disturb other players
            if ((includeThisPlayers && getPlayers().contains(player)) ||
                    !TwonyOnePlugin.getInstance().getGameManager().isInGame(player)) {
                player.playSound(center, sound, volume, pitch);
            }
        }
    }

    private void playRollSound() {
        broadcastSoundNearby(Sound.BLOCK_ANVIL_BREAK, 1, 1);
    }

    public @NotNull Location getCenter() {
        Location center = scoreHoloLoc.clone();
        center.setY(chairOne.getSitBlock().getY() + 1); // Awkwardly align the center height
        return center;
    }

    private @NotNull Card rollCard(@NotNull GameChair chair, boolean secret) {
        if (secret) Check.isTrue(score.getCards(chair).isEmpty());

        Card card = score.addRandomCard(chair);

        if (secret) {
            CardHoloHelper.addClosed(chair, card);
        } else {
            CardHoloHelper.addOpen(chair, card);
        }

        playRollSound();
        chair.getTable().updateScoreHolograms(false);
        return card;
    }

    public void addTask(@NotNull BukkitTask task) {
        Check.isTrue(state != State.WAITING);
        tasks.add(task);
    }

    public void addHolo(@NotNull Holo holo) {
        Check.isTrue(state != State.WAITING);
        holograms.add(holo);
    }

    public @Nullable GameChair getHostChair() {
        return bet != null ? bet.getChair() : null;
    }

    public @Nullable Bet getBet() {
        return bet;
    }

    public boolean isBetSet() {
        return bet != null;
    }

    public @Nullable GamePlayer getGamePlayer(@NotNull Player player) {
        GamePlayer gpOne = chairOne.getGamePlayer();
        if (gpOne != null && gpOne.getPlayer().equals(player)) return gpOne;

        GamePlayer gpTwo = chairTwo.getGamePlayer();
        if (gpTwo != null && gpTwo.getPlayer().equals(player)) return gpTwo;

        return null;
    }

    public @NotNull List<GamePlayer> getGamePlayers() {
        List<GamePlayer> list = new ArrayList<>(2);

        GamePlayer playerOne = chairOne.getGamePlayer();
        GamePlayer playerTwo = chairTwo.getGamePlayer();

        if (playerOne != null) list.add(playerOne);
        if (playerTwo != null) list.add(playerTwo);

        return list;
    }

    public @NotNull List<Player> getPlayers() {
        List<Player> list = new ArrayList<>(2);

        Player playerOne = chairOne.getPlayer();
        Player playerTwo = chairTwo.getPlayer();

        if (playerOne != null) list.add(playerOne);
        if (playerTwo != null) list.add(playerTwo);

        return list;
    }

    public boolean hasPlayer(@NotNull Player player) {
        return getChairOne().getPlayer() == player || getChairTwo().getPlayer() == player;
    }

    public @Nullable GameChair getChair(@NotNull Player player) {
        if (player.equals(chairOne.getPlayer())) return chairOne;
        if (player.equals(chairTwo.getPlayer())) return chairTwo;
        return null;
    }

    public @Nullable GameChair getChair(@NotNull Block block) {
        if (chairOne.getBlocks().contains(block)) return chairOne;
        if (chairTwo.getBlocks().contains(block)) return chairTwo;
        return null;
    }

    public @NotNull GameChair @NotNull [] getChairs() {
        GameChair[] chairs = new GameChair[2];

        chairs[0] = chairOne;
        chairs[1] = chairTwo;

        return chairs;
    }

    public @NotNull GameChair getOppositeChair(@NotNull GameChair chair) throws IllegalArgumentException {
        if (chairOne.equals(chair)) return chairTwo;
        if (chairTwo.equals(chair)) return chairOne;
        throw new IllegalArgumentException("Unknown opposite chair");
    }

    public @NotNull GameScore getScore() {
        return Check.notNull(score);
    }

    public @NotNull Location getScoreHoloLoc() {
        return scoreHoloLoc.clone();
    }

    private void rewardWinner(@NotNull Player player) {
        Check.notNull(bet);
        BukkitHelper.getUser(player).get().depositDonateCoins(Utils.calculateAmountWithFee(bet.getAmount() * 2, Config.BET_FEE), TransactionCause.PLUGIN, TransactionSource.PLUGIN, "TwonyOne winning");
    }

    private void refundTie() {
        Check.notNull(bet);
        for (Player player : getPlayers()) {
            BukkitHelper.getUser(player).get().depositDonateCoins(bet.getAmount(), TransactionCause.PLUGIN, TransactionSource.PLUGIN, "TwonyOne tie");
        }
    }

    private void refundPluginDisable() {
        Check.notNull(bet);
        for (Player player : getPlayers()) {
            BukkitHelper.getUser(player).get().depositDonateCoins(bet.getAmount(), TransactionCause.PLUGIN, TransactionSource.PLUGIN, "TwonyOne plugin disable");
        }
    }
}
