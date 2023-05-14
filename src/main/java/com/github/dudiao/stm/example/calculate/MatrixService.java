package com.github.dudiao.stm.example.calculate;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReadConfig;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.math4.legacy.linear.Array2DRowFieldMatrix;
import org.apache.commons.math4.legacy.linear.ArrayFieldVector;
import org.apache.commons.math4.legacy.linear.BigReal;
import org.apache.commons.math4.legacy.linear.FieldDecompositionSolver;
import org.apache.commons.math4.legacy.linear.FieldLUDecomposition;
import org.apache.commons.math4.legacy.linear.FieldVector;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 获取方程组
 *
 * @author songyinyin
 * @since 2023/5/13 18:19
 */
@Component
public class MatrixService implements LifecycleBean {

  @Inject(value = "${app.initMatrix}", required = false)
  File initFile;
  @Inject(value = "${app.resultFile}", required = false)
  File resultFile;
  @Inject("${app.scale}")
  Integer scale;
  @Inject("${app.total}")
  BigDecimal total;
  @Inject("${app.steps}")
  String stepsStr;
  @Inject("${app.property}")
  Map<String, String> proMap = new LinkedHashMap<>();

  BigDecimal[] steps;

  BigDecimal[][] property;

  String initMatrix;

  @Override
  public void start() throws Throwable {
    // initMatrix
    if (initFile == null) {
      File file = new File(System.getProperty("user.dir") + "/initMatrix.csv");
      if (file.exists()) {
        initMatrix = FileUtil.readString(file, StandardCharsets.UTF_8);
      }
      if (StrUtil.isBlank(initMatrix)) {
        initMatrix = ResourceUtil.readStr("initMatrix.csv", StandardCharsets.UTF_8);
      }
    } else {
      initMatrix = FileUtil.readUtf8String(initFile);
    }

    // resultFile
    if (resultFile == null) {
      String userDir = System.getProperty("user.dir");
      resultFile = new File(userDir + File.separator + "result.csv");
    }

    String[] split = stepsStr.split(",");
    steps = new BigDecimal[split.length];
    for (int i = 0; i < split.length; i++) {
      steps[i] = new BigDecimal(split[i]).setScale(scale, RoundingMode.HALF_UP);
    }

    total = total.setScale(scale, RoundingMode.HALF_UP);

    Collection<String> values = proMap.values();
    List<String> list = new ArrayList<>(values);
    int len = list.get(0).split(",").length;
    property = new BigDecimal[values.size()][len];
    for (int i = 0; i < list.size(); i++) {
      String[] split1 = list.get(i).split(",");
      for (int j = 0; j < split1.length; j++) {
        property[i][j] = new BigDecimal(split1[j]).setScale(scale, RoundingMode.HALF_UP);
      }
    }

  }


  /**
   * 获取方程组
   */
  public void calculate() {
    StopWatch stopWatch = new StopWatch("calculate");
    stopWatch.start("init");
    CsvReadConfig csvReadConfig = CsvReadConfig.defaultConfig();
    csvReadConfig.setContainsHeader(true);
    CsvReader reader = CsvUtil.getReader(csvReadConfig);
    CsvData read = reader.readFromStr(initMatrix);
    List<String> header = read.getHeader();
    int size = header.size();
    BigDecimal[][] matrix = new BigDecimal[size][size];
    for (int i = 0; i < size; i++) {
      List<String> rawList = read.getRow(i).getRawList();
      for (int j = 0; j < size; j++) {
        String raw = rawList.get(j);
        if (StrUtil.isBlank(raw)) {
          matrix[i][j] = BigDecimal.ZERO;
        } else {
          matrix[i][j] = new BigDecimal(raw).setScale(scale, RoundingMode.HALF_UP);
        }
      }
    }
    FileUtil.touch(resultFile);
    CsvWriter writer = CsvUtil.getWriter(resultFile, StandardCharsets.UTF_8);
    List<String> headerList = new ArrayList<>();
    headerList.add("right1");
    headerList.add("right2");
    headerList.add("right3");
    headerList.add("right4");
    headerList.add("解_w");
    headerList.add("解_x");
    headerList.add("解_y");
    headerList.add("解_z");
    headerList.addAll(proMap.keySet());

    writer.writeHeaderLine(headerList.toArray(new String[0]));
    stopWatch.stop();

    stopWatch.start("calculate");
    BigReal[][] matrixTmp = arraycopy(matrix);
    Array2DRowFieldMatrix<BigReal> coefficients = new Array2DRowFieldMatrix<>(matrixTmp, false);
    FieldDecompositionSolver<BigReal> solver = new FieldLUDecomposition<>(coefficients).getSolver();

    for (BigDecimal i = BigDecimal.ZERO; i.compareTo(total) <= 0; i = i.add(steps[0])) {
      BigDecimal tempI = total.subtract(i);
      for (BigDecimal j = BigDecimal.ZERO; j.compareTo(tempI) <= 0; j = j.add(steps[1])) {
        BigDecimal tempJ = tempI.subtract(j);
        for (BigDecimal k = BigDecimal.ZERO; k.compareTo(tempJ) <= 0; k = k.add(steps[2])) {

          BigDecimal l = tempJ.subtract(k);

          // 解方程
          ArrayFieldVector<BigReal> constants = new ArrayFieldVector<>(new BigReal[]{new BigReal(i), new BigReal(j), new BigReal(k), new BigReal(l)}, false);
          FieldVector<BigReal> solution = solver.solve(constants);
          BigDecimal w = solution.getEntry(0).bigDecimalValue();
          BigDecimal x = solution.getEntry(1).bigDecimalValue();
          BigDecimal y = solution.getEntry(2).bigDecimalValue();
          BigDecimal z = solution.getEntry(3).bigDecimalValue();
          if (w.compareTo(BigDecimal.ZERO) < 0 || x.compareTo(BigDecimal.ZERO) < 0 || y.compareTo(BigDecimal.ZERO) < 0 || z.compareTo(BigDecimal.ZERO) < 0) {
            continue;
          }
          BigDecimal[] result = new BigDecimal[]{w, x, y, z};

          // 计算属性
          BigDecimal[] calculateProperty = calculateProperty(result, property);
          List<String> line = new ArrayList<>();
          line.add(toStr(i));
          line.add(toStr(j));
          line.add(toStr(k));
          line.add(toStr(l));
          line.add(toStr(result[0]));
          line.add(toStr(result[1]));
          line.add(toStr(result[2]));
          line.add(toStr(result[3]));
          for (BigDecimal bigDecimal : calculateProperty) {
            line.add(toStr(bigDecimal));
          }
          writer.writeLine(line.toArray(new String[0]));
        }
      }
    }
    writer.flush();

    stopWatch.stop();
    System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    System.out.println("生成的Excel路径：" + resultFile.getAbsolutePath());
  }

  public static BigReal[][] arraycopy(BigDecimal[][] array) {
    BigReal[][] resultArray = new BigReal[array.length][array[0].length];
    for (int i = 0; i < array.length; i++) {
      BigDecimal[] decimals = array[i];
      for (int j = 0; j < decimals.length; j++) {
        resultArray[i][j] = new BigReal(decimals[j]);
      }
    }
    return resultArray;
  }

  /**
   * 计算属性
   *
   * @param result         方程的解
   * @param propertyMatrix 属性矩阵
   */
  public BigDecimal[] calculateProperty(BigDecimal[] result, BigDecimal[][] propertyMatrix) {

    BigDecimal[] resultPro = new BigDecimal[propertyMatrix.length];

    for (int i = 0; i < propertyMatrix.length; i++) {
      BigDecimal resultProperty = BigDecimal.ZERO;
      BigDecimal[] property = propertyMatrix[i];
      for (int j = 0; j < property.length; j++) {
        // 累加，计算密度
        resultProperty = resultProperty.add(result[j].multiply(property[j]));
      }
      resultPro[i] = resultProperty;
    }
    return resultPro;
  }

  private String toStr(BigDecimal bigDecimal) {
    if (bigDecimal == null) {
      return "";
    }
    return bigDecimal.setScale(scale, RoundingMode.HALF_UP).toString();
  }

}
