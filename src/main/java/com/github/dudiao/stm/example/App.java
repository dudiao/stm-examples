package com.github.dudiao.stm.example;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.noear.solon.Solon;
import org.noear.solon.core.util.LogUtil;
import org.noear.solon.logging.utils.LogUtilToSlf4j;
import org.slf4j.LoggerFactory;

/**
 * @author songyinyin
 * @since 2023/4/30 16:09
 */
public class App {

  public static void main(String[] args) {
    // 设置日志级别
    setLogLevel(args);

    // 启动应用
    Solon.start(App.class, args);
  }

  private static void setLogLevel(String[] args) {
    for (String arg : args) {
      if (arg.contains("debug=1")) {
        return;
      }
    }
    LogUtil.globalSet(new LogUtilToSlf4j());
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger logger = loggerContext.getLogger("org.noear.solon.Solon");
    logger.setLevel(Level.valueOf("warn"));
  }
}
