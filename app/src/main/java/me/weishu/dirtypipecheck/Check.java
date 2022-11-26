package me.weishu.dirtypipecheck;

/**
 * @author weishu
 * @date 2022/11/26.
 */

public final class Check {

   static {
      System.loadLibrary("dirtypipecheck");
   }

   public static native boolean check(String path);

}
