package com.drypted.spotlight.client.utils;

public class GeneralUtils
{
    public static final char EMPTY_CHAR = '\0';

    public static <T> boolean isInList(T[] list, T item)
    {
        for (T element : list)
        {
            if (element.equals(item))
            {
                return true;
            }
        }
        return false;
    }
}
