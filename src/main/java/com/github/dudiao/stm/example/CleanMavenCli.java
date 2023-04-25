package com.github.dudiao.stm.example;

import com.dudiao.stm.plugin.StmPluginCli;
import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

/**
 * 一个示例的命令行工具，仅打印参数
 *
 * @author songyinyin
 * @since 2023/4/23 10:02
 */
@Component
@CommandLine.Command(name = "mvnClean", description = "清理Maven相关目录")
public class CleanMavenCli implements StmPluginCli {

  private final Logger log = LoggerFactory.getLogger(CleanMavenCli.class);

  @CommandLine.Option(names = {"-l", "--local"}, description = "是否只列出本地的工具")
  private boolean local;

  @CommandLine.Option(names = {"-n"}, description = "名称")
  private String name;

  @Override
  public Integer call() throws Exception {
    log.info("1 local: {}, name: {}", local, name);
    return 0;
  }
}
