package com.nutech.priceloader.DBConfiguration;

import org.springframework.util.Assert;

public class DBContextHolder {
    private static ThreadLocal<DBTypeEnum> CONTEXT
    = new ThreadLocal<>();

  public static void set(DBTypeEnum clientDatabase) {
      Assert.notNull(clientDatabase, "clientDatabase cannot be null");
      CONTEXT.set(clientDatabase);
  }

  public static DBTypeEnum getClientDatabase() {
      return CONTEXT.get();
  }

  public static void clear() {
      CONTEXT.remove();
  }
}