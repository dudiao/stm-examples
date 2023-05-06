package com.github.dudiao.stm.example;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

/**
 * @author songyinyin
 * @since 2023/4/30 16:15
 */
@Slf4j
@Component
public class AppStartEvent implements EventListener<AppLoadEndEvent> {
  @Override
  public void onEvent(AppLoadEndEvent appLoadEndEvent) throws Throwable {

    log.info("app start!");

  }
}
