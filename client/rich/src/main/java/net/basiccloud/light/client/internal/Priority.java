package net.basiccloud.light.client.internal;

/**
 */
public class Priority {

    private static final int DEFAULT_PRIORITY = 1;

    public static int[] defaultPriorities(int instanceSize) {
        int[] priorities = new int[instanceSize];
        for (int i = 0; i < priorities.length; i++) {
            priorities[i] = DEFAULT_PRIORITY;
        }
        return priorities;
    }


    public static boolean hasAvailableInstance(int[] priorities) {
        for (int priority : priorities) {
            if (priority > 0)
                return true;
        }
        return false;
    }
}
