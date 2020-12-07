package net.plasmere.utils;

public enum LoggedEventType {
    /*
    Note to future self and other devs:
    Convention was broken here for a reason - 
    to fix another bug as I was too lazy to make a workaround. I plan on changing this eventually.
    */
    broken,
    placed,
    added,
    removed,
    spawned,
    killed,
    used
}
