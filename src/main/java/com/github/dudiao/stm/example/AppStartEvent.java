package com.github.dudiao.stm.example;

import com.github.dudiao.stm.example.calculate.MatrixService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.core.event.EventListener;

/**
 * @author songyinyin
 * @since 2023/4/30 16:15
 */
@Component
public class AppStartEvent implements EventListener<AppLoadEndEvent> {

  @Inject
  MatrixService matrixService;

  @Override
  public void onEvent(AppLoadEndEvent appLoadEndEvent) throws Throwable {

    System.out.println("start calculate...");

    matrixService.calculate4();
    matrixService.calculate3();
  }
}
