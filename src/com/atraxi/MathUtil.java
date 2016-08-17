package com.atraxi;

/**
 * Created by Atraxi on 17/08/2016.
 */
public class MathUtil
{
    public static long gcd(long a, long b)
    {
//        while (b > 0)
//        {
//            long temp = b;
//            b = a % b; // % is remainder
//            a = temp;
//        }
//        return a;
        if (b==0) return a;
        return gcd(b,a%b);
    }

    public static long gcd(long[] input)
    {
        long result = input[0];
        for(int i = 1; i < input.length; i++) result = gcd(result, input[i]);
        return result;
    }

    public static long lcm(long a, long b)
    {
        return a * (b / gcd(a, b));
    }

    public static long lcm(long[] input)
    {
        long result = input[0];
        for(int i = 1; i < input.length; i++) result = lcm(result, input[i]);
        return result;
    }
}
