package net.plasmere.utils;

public class InspectorUtils {
    private static boolean inspectorEnabled = false;
    public static void toggle() {
        InspectorUtils.inspectorEnabled = !InspectorUtils.inspectorEnabled;
    }
    public boolean isInspected(){
        return InspectorUtils.inspectorEnabled;
    }
}
