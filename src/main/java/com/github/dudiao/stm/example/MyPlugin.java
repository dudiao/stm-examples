package com.github.dudiao.stm.example;

import com.dudiao.stm.plugin.StmPlugin;
import org.noear.solon.core.AopContext;

/**
 * @author songyinyin
 * @since 2023/4/23 11:46
 */
public class MyPlugin extends StmPlugin {

  @Override
  public void start(AopContext context) throws Throwable {
    System.out.println("MyPlugin start");
    super.start(context);
  }
}
