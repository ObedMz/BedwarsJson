package mine.lc.bedjson.controllers;

/**
 * <h1>Status</h1>
 * <br>
 * Class used to define the status of an arena,
 * OFF > The arena is unable to join.
 * PLAYING > The arena is currently in game.
 * WAITING > The arena is waiting for players to start.
 */
public enum Status {
    OFF,
    PLAYING,
    WAITING

}
