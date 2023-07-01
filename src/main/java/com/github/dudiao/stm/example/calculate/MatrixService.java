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

  @Inject(value = "${app.initMatrix4}", required = false)
  File initFile4;
  @Inject(value = "${app.initMatrix3}", required = false)
  File initFile3;
  @Inject(value = "${app.resultFile4}", required = false)
  File resultFile4;
  @Inject(value = "${app.resultFile3}", required = false)
  File resultFile3;

  @Inject("${app.scale}")
  Integer scale;
  @Inject("${app.total4}")
  BigDecimal total4;

  @Inject("${app.total3}")
  BigDecimal total3;
  @Inject("${app.steps4}")
  String steps4Str;
  @Inject("${app.steps3}")
  String steps3Str;
  @Inject("${app.property4}")
  Map<String, String> pro4Map = new LinkedHashMap<>();
  @Inject("${app.property3}")
  Map<String, String> pro3Map = new LinkedHashMap<>();

  BigDecimal[] steps4;
  BigDecimal[] steps3;

  BigDecimal[][] property4;
  BigDecimal[][] property3;

  String initMatrix4;

  String initMatrix3;

  @Override
  public void start() throws Throwable {
    // initMatrix
    if (initFile4 == null) {
      File file = new File(System.getProperty("user.dir") + "/initMatrix4.csv");
      if (file.exists()) {
        initMatrix4 = FileUtil.readString(file, StandardCharsets.UTF_8);
      }
      if (StrUtil.isBlank(initMatrix4)) {
        initMatrix4 = ResourceUtil.readStr("initMatrix4.csv", StandardCharsets.UTF_8);
      }
    } else {
      initMatrix4 = FileUtil.readUtf8String(initFile4);
    }
    if (initFile3 == null) {
      File file = new File(System.getProperty("user.dir") + "/initMatrix3.csv");
      if (file.exists()) {
        initMatrix3 = FileUtil.readString(file, StandardCharsets.UTF_8);
      }
      if (StrUtil.isBlank(initMatrix3)) {
        initMatrix3 = ResourceUtil.readStr("initMatrix3.csv", StandardCharsets.UTF_8);
      }
    } else {
      initMatrix3 = FileUtil.readUtf8String(initFile3);
    }

    // resultFile
    if (resultFile4 == null) {
      String userDir = System.getProperty("user.dir");
      resultFile4 = new File(userDir + File.separator + "result4.csv");
    }
    if (resultFile3 == null) {
      String userDir = System.getProperty("user.dir");
      resultFile3 = new File(userDir + File.separator + "result3.csv");
    }

    String[] split = steps4Str.split(",");
    steps4 = new BigDecimal[split.length];
    for (int i = 0; i < split.length; i++) {
      steps4[i] = new BigDecimal(split[i]).setScale(scale, RoundingMode.HALF_UP);
    }

    String[] split3 = steps3Str.split(",");
    steps3 = new BigDecimal[split3.length];
    for (int i = 0; i < split3.length; i++) {
      steps3[i] = new BigDecimal(split3[i]).setScale(scale, RoundingMode.HALF_UP);
    }

    total4 = total4.setScale(scale, RoundingMode.HALF_UP);

    Collection<String> values = pro4Map.values();
    List<String> list = new ArrayList<>(values);
    int len = list.get(0).split(",").length;
    property4 = new BigDecimal[values.size()][len];
    setProperty(list, property4);

    Collection<String> values3 = pro3Map.values();
    List<String> list3 = new ArrayList<>(values3);
    int len3 = list3.get(0).split(",").length;
    property3 = new BigDecimal[values3.size()][len3];
    setProperty(list3, property3);

  }

  private void setProperty(List<String> list, BigDecimal[][] property) {
    for (int i = 0; i < list.size(); i++) {
      String[] split1 = list.get(i).split(",");
      for (int j = 0; j < split1.length; j++) {
        property[i][j] = new BigDecimal(split1[j]).setScale(scale, RoundingMode.HALF_UP);
      }
    }
  }

  /**
   * 计算方程组，3 个性质
   */
  public void calculate3() {
    StopWatch stopWatch = new StopWatch("calculate3");
    stopWatch.start("init");
    CsvReadConfig csvReadConfig = CsvReadConfig.defaultConfig();
    csvReadConfig.setContainsHeader(true);
    CsvReader reader = CsvUtil.getReader(csvReadConfig);
    CsvData read = reader.readFromStr(initMatrix3);
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
    FileUtil.touch(resultFile3);
    CsvWriter writer = CsvUtil.getWriter(resultFile3, StandardCharsets.UTF_8);
    List<String> headerList = new ArrayList<>();
    headerList.add("right1");
    headerList.add("right2");
    headerList.add("right3");
    for (String s : header) {
      headerList.add("解_" + s);
    }
    headerList.addAll(pro3Map.keySet());

    writer.writeHeaderLine(headerList.toArray(new String[0]));
    stopWatch.stop();

    stopWatch.start("calculate");
    BigReal[][] matrixTmp = arraycopy(matrix);
    Array2DRowFieldMatrix<BigReal> coefficients = new Array2DRowFieldMatrix<>(matrixTmp, false);
    FieldDecompositionSolver<BigReal> solver = new FieldLUDecomposition<>(coefficients).getSolver();

    for (BigDecimal i = BigDecimal.ZERO; i.compareTo(total3) <= 0; i = i.add(steps3[0])) {
      BigDecimal tempI = total3.subtract(i);
      for (BigDecimal j = BigDecimal.ZERO; j.compareTo(tempI) <= 0; j = j.add(steps3[1])) {
        BigDecimal k = tempI.subtract(j);

        // 解方程
        ArrayFieldVector<BigReal> constants = new ArrayFieldVector<>(new BigReal[]{new BigReal(i), new BigReal(j), new BigReal(k)}, false);
        FieldVector<BigReal> solution = solver.solve(constants);
        List<BigDecimal> solutionList = new ArrayList<>();
        for (int i1 = 0; i1 < header.size(); i1++) {
          BigDecimal bigDecimal = solution.getEntry(i1).bigDecimalValue();
          solutionList.add(bigDecimal);
        }

        if (solutionList.stream().anyMatch(bigDecimal -> bigDecimal.compareTo(BigDecimal.ZERO) < 0)) {
          continue;
        }

        // 计算属性
        BigDecimal[] calculateProperty = calculateProperty(solutionList, property3);
        List<String> line = new ArrayList<>();
        line.add(toStr(i));
        line.add(toStr(j));
        line.add(toStr(k));
        line.addAll(solutionList.stream().map(this::toStr).toList());
        for (BigDecimal bigDecimal : calculateProperty) {
          line.add(toStr(bigDecimal));
        }
        writer.writeLine(line.toArray(new String[0]));
      }
    }
    writer.flush();

    stopWatch.stop();
    System.out.println(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
    System.out.println("生成的Excel3路径：" + resultFile3.getAbsolutePath());
  }


  /**
   * 计算方程组，4 个性质
   */
  public void calculate4() {
    StopWatch stopWatch = new StopWatch("calculate4");
    stopWatch.start("init");
    CsvReadConfig csvReadConfig = CsvReadConfig.defaultConfig();
    csvReadConfig.setContainsHeader(true);
    CsvReader reader = CsvUtil.getReader(csvReadConfig);
    CsvData read = reader.readFromStr(initMatrix4);
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
    FileUtil.touch(resultFile4);
    CsvWriter writer = CsvUtil.getWriter(resultFile4, StandardCharsets.UTF_8);
    List<String> headerList = new ArrayList<>();
    headerList.add("right1");
    headerList.add("right2");
    headerList.add("right3");
    headerList.add("right4");
    for (String s : header) {
      headerList.add("解_" + s);
    }
    headerList.addAll(pro4Map.keySet());

    writer.writeHeaderLine(headerList.toArray(new String[0]));
    stopWatch.stop();

    stopWatch.start("calculate");
    BigReal[][] matrixTmp = arraycopy(matrix);
    Array2DRowFieldMatrix<BigReal> coefficients = new Array2DRowFieldMatrix<>(matrixTmp, false);
    FieldDecompositionSolver<BigReal> solver = new FieldLUDecomposition<>(coefficients).getSolver();

    for (BigDecimal i = BigDecimal.ZERO; i.compareTo(total4) <= 0; i = i.add(steps4[0])) {
      BigDecimal tempI = total4.subtract(i);
      for (BigDecimal j = BigDecimal.ZERO; j.compareTo(tempI) <= 0; j = j.add(steps4[1])) {
        BigDecimal tempJ = tempI.subtract(j);
        for (BigDecimal k = BigDecimal.ZERO; k.compareTo(tempJ) <= 0; k = k.add(steps4[2])) {

          BigDecimal l = tempJ.subtract(k);

          // 解方程
          ArrayFieldVector<BigReal> constants = new ArrayFieldVector<>(new BigReal[]{new BigReal(i), new BigReal(j), new BigReal(k), new BigReal(l)}, false);
          FieldVector<BigReal> solution = solver.solve(constants);
          List<BigDecimal> solutionList = new ArrayList<>();
          for (int i1 = 0; i1 < header.size(); i1++) {
            BigDecimal bigDecimal = solution.getEntry(i1).bigDecimalValue();
            solutionList.add(bigDecimal);
          }

          if (solutionList.stream().anyMatch(bigDecimal -> bigDecimal.compareTo(BigDecimal.ZERO) < 0)) {
            continue;
          }

          // 计算属性
          BigDecimal[] calculateProperty = calculateProperty(solutionList, property4);
          List<String> line = new ArrayList<>();
          line.add(toStr(i));
          line.add(toStr(j));
          line.add(toStr(k));
          line.add(toStr(l));
          line.addAll(solutionList.stream().map(this::toStr).toList());
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
    System.out.println("生成的Excel4路径：" + resultFile4.getAbsolutePath());
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
  public BigDecimal[] calculateProperty(List<BigDecimal> result, BigDecimal[][] propertyMatrix) {

    BigDecimal[] resultPro = new BigDecimal[propertyMatrix.length];

    for (int i = 0; i < propertyMatrix.length; i++) {
      BigDecimal resultProperty = BigDecimal.ZERO;
      BigDecimal[] property = propertyMatrix[i];
      for (int j = 0; j < property.length; j++) {
        // 累加，计算密度
        resultProperty = resultProperty.add(result.get(j).multiply(property[j]));
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
