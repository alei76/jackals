package jackals.lab.shares;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import jackals.downloader.HttpDownloader;
import jackals.downloader.ReqCfg;
import jackals.lab.FileUtil;
import jackals.model.PageObj;
import jackals.model.RequestOjb;
import jackals.utils.BlockExecutorPool;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.classification.*;
import smile.data.NumericAttribute;
import smile.math.distance.EuclideanDistance;
import smile.math.distance.ManhattanDistance;
import smile.math.kernel.GaussianKernel;
import smile.math.rbf.RadialBasisFunction;
import smile.util.SmileUtils;

import java.io.File;
import java.util.*;

//@Ignore
public class Shares {
    private Logger logger = LoggerFactory.getLogger(getClass());
    //    String training = "D:\\tmp\\calculate\\0_10.txt";
    String training = "D:\\tmp\\calculate\\0_50.txt";
    int checkData = 200;

    @Test
    public void runCode() throws Exception {
        File f2015 = new File("D:\\tmp\\calculate2015\\");
        for (File f : f2015.listFiles()) {
            try{
                runCode(f.getName().replaceAll("\\.txt", ""));

            }catch (LackDataException e){
                e.printStackTrace();
            }
        }
    }


    @Test
    public void find() throws Exception {
        //查找月均量比为正的股票
//        BlockExecutorPool executor = new BlockExecutorPool(10);
//        String[] txt = FileUtil.read(new File("D:\\tmp\\codes.txt")).split("\n");
//        for (final String s : txt) {
//            executor.execute(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        logger.info("download {}", s);
//                        downloadData(s, 2014, new File("D:\\tmp\\find2014\\" + s + ".txt"));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
        logger.info("download ======================================================");
        for (File code : new File("D:\\tmp\\find\\").listFiles()) {
            try {
                logger.info(code.getName());
                calculateFile(code, new File("D:\\tmp\\calculate2015\\" + code.getName()), "2015");
            } catch (LackDataException e) {
                logger.error("LackDataException", e);
            }
        }
        for (int x = 180; x > 0; x--) {
            int zl = 0;
            int jl = 0;

            String data = "";
            for (File code : new File("D:\\tmp\\calculate2008").listFiles()) {
                String[] arr = FileUtil.read(code).split("\n");
                if (arr.length < 180)
                    continue;
                String[] rowArr = arr[arr.length - x].split("\\s");
                double d1 = Double.valueOf(rowArr[23]);
                double liangbi = Double.valueOf(rowArr[11]);
                data = arr[arr.length - x].split("\\s")[0];
                int cnt = 0;
                if (d1 > 1) zl++;
                if (d1 < 1) jl++;
//            for (int i = arr.length - 10; i < arr.length; i++) {
//                double d = Double.valueOf(arr[i].split("\\s")[23]);
//                if (d < 1) {
//                    cnt++;
//                }
//            }
//            if (d1 >= 1 && cnt >= 3) {
//                System.out.println(code.getName());
//            }
            }
            System.out.println(data + "\t" + zl + "\t" + jl);
//            System.out.println(data+"\t"+zl+"\t"+jl);

        }

    }

    public void runCode(String code) throws Exception {
        File orig = new File("D:\\tmp\\runCode\\orig-" + code + ".txt");
        File calc = new File("D:\\tmp\\runCode\\calc-" + code + ".txt");
//        File orig = new File("D:\\tmp\\find\\" + code + ".txt");
//        File calc = new File("D:\\tmp\\calculate2015\\" + code + ".txt");
        if (!orig.exists())
//            return;
            downloadData(code, 2015, orig);
        if (!calc.exists())
            calculateFile(orig, calc, "2015");

//        dtree(orig, calc);
        runRBF(orig, calc);
//        runSVM(calc);
    }

    //////////////////////////////////
//    int k = smile.math.Math.max(label) + 1;
//    NeuralNetwork net = null;
//    if (k == 2) {
//        net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.LOGISTIC_SIGMOID, data[0].length, units, 1);
//    } else {
//        net = new NeuralNetwork(NeuralNetwork.ErrorFunction.CROSS_ENTROPY, NeuralNetwork.ActivationFunction.SOFTMAX, data[0].length, units, k);
//    }
//
//    for (int i = 0; i < epochs; i++) {
//        net.learn(data, label);
//    }


    //    @Test
    public void runSVM(File code) throws Exception {
        Data train = loadData(training);
//        Data train = loadData("D:\\tmp\\calculate\\all.txt");
        Data test = loadData(code.getPath());

        SVM<double[]> svm = new SVM<double[]>(new GaussianKernel(0.5), 1.0, 3, SVM.Multiclass.ONE_VS_ALL);
        svm.learn(train.matrix, train.label);
        svm.finish();

        printAcc(code.getName(), train, svm);

        printAcc(code.getName(), test, svm);

    }

    private double printAcc(String name, Data train, Classifier classifier) {
        double error = 0;
        for (int i = 0; i < train.matrix.length; i++) {
//            System.out.println(tree.predict(train.matrix[i])+""+train.label[i]);
            if (classifier.predict(train.matrix[i]) != train.label[i]) {
                error++;
            }

        }
        System.out.println(name + " " + classifier.getClass() + " error:" + error);
        double acc = (train.matrix.length - error) / train.matrix.length * 100;
        System.out.println(name + " " + classifier.getClass() + "acc:" + acc);
        return acc;
    }

    RBFNetwork<double[]> rbf;

    public void runRBF(File orig, File calc) throws Exception {
        if (rbf == null) {
            train = loadData(training);
            double[][] centers = new double[30][];
            RadialBasisFunction basis = SmileUtils.learnGaussianRadialBasis(train.matrix, centers);
//        RBFNetwork<double[]> rbf = new RBFNetwork<double[]>(train.matrix, train.label, new ManhattanDistance(), basis, centers);
            rbf = new RBFNetwork<double[]>(train.matrix, train.label, new EuclideanDistance(), basis, centers);

        }
//        Data train = loadData("D:\\tmp\\calculate\\all.txt");
        Data test = loadData(calc.getPath());
        printAcc(calc.getName(), train, rbf);
        double acc = printAcc(calc.getName(), test, rbf);
        filter(calc, test, acc, rbf);

    }

    private void filter(File calc, Data test, double acc, Classifier classifier) {

        if (acc < 86)
            return;
        String str = FileUtil.read(calc);
        String[] arr = str.split("\n");
        String day1 = arr[arr.length - 3];
        String day2 = arr[arr.length - 2];
        String day3 = arr[arr.length - 1];
//        String day = FileUtil.readLastLine(calc);
        if (day1.startsWith("2015-11-17") && day2.startsWith("2015-11-18") && day3.startsWith("2015-11-19")) {
            int result1 = classifier.predict(loadDay(day1.split("\\s")));
            int result2 = classifier.predict(loadDay(day2.split("\\s")));
            int result3 = classifier.predict(loadDay(day3.split("\\s")));
            logger.info("{} {} {} {}", calc.getName(), result1, result2, result3);
            if ((result1 == 0 || result2 == 0) && result3 == 1)
                writeResult(test, classifier, new File("D:\\tmp\\buy\\" + calc.getName()));
        }


    }

    DecisionTree tree;
    Data train;

    //    @Test
    public void dtree(File orig, File calc) throws Exception {
        if (tree == null) {
            train = loadData(training);
            tree = new DecisionTree(train.matrix, train.label, 300, DecisionTree.SplitRule.ENTROPY);
        }
//        Data train = loadData("D:\\tmp\\calculate\\000063.txt");

//        Data train = loadData("D:\\tmp\\calculate\\0_700.txt");
//        Data test = loadData("D:\\tmp\\calculate\\100_10.txt");
//        Data test = loadData("D:\\tmp\\calculate\\500.txt");
        Data test = loadData(calc.getPath());
//        Data test = loadData("D:\\tmp\\calculate\\600426.txt");

        printAcc(calc.getName(), train, tree);
        double acc = printAcc(calc.getName(), test, tree);
        filter(calc, test, acc, tree);
//        writeResult(test, tree,new File("D:\\tmp\\calculate\\output.txt"));


//        System.out.println("error2:" + new DecimalFormat("###.######").format(error / test.matrix.length));

    }

    private void writeResult(Data test, Classifier tree, File output) {
        double error = 0;
        StringBuffer header = new StringBuffer();
        header.append("日期" + "\t");
        header.append("开盘价" + "\t");
        header.append("最高价" + "\t");
        header.append("收盘价" + "\t");
        header.append("最低价" + "\t");
        header.append("交易金额" + "\t");
        header.append("交易量" + "\t");
        header.append("均价变动" + "\t");
        header.append("最高变动" + "\t");
        header.append("最低变动" + "\t");
        header.append("高低差变动" + "\t");
        header.append("量比" + "\t");
        header.append("收盘5日涨幅" + "\t");
        header.append("收盘10日涨幅" + "\t");
        header.append("5日涨" + "\t");
        header.append("10日涨" + "\t");
        header.append("30日涨" + "\t");
        header.append("月均价" + "\t");
        header.append("月均价一日变动" + "\t");
        header.append("月均价未来10日变动" + "\t");
        header.append("月均量" + "\t");
        header.append("月均量一日变动" + "\t");
        header.append("月均量未来10日变动" + "\t");
        header.append("均量量比" + "\t");
        header.append("结果\n");
        error = 0;
//        File output = new File("D:\\tmp\\calculate\\output.txt");
        output.delete();
        FileUtil.write(output, header.toString(), true);
        for (int i = 0; i < test.matrix.length; i++) {
//            System.out.println(tree.predict(train.matrix[i])+""+train.label[i]);
            int result = tree.predict(test.matrix[i]);
            if (result != test.label[i]) {
                error++;
            }
            String line = test.rows[i] + "\t" + result + "\n";
            FileUtil.write(output, line, true);
//            System.out.println();
        }
    }

    private Data loadData(String file) {
        Data data = new Data();
        String strData = FileUtil.read(new File(file));
        String[] rows = strData.split("\n");

        List<double[]> dataList = new ArrayList<double[]>();
        List<Integer> typeList = new ArrayList<Integer>();
        for (int x = 0; x < rows.length; x++) {
            String[] colArr = rows[x].split("\\s");
            int type = Integer.valueOf(colArr[19]);
            if (type == -1) {
                continue;
            }
            dataList.add(loadDay(colArr));
            typeList.add(Integer.valueOf(colArr[19]));

        }
        data.matrix = new double[dataList.size()][];
        data.label = new int[dataList.size()];
        for (int x = 0; x < dataList.size(); x++) {
            data.matrix[x] = dataList.get(x);
            data.label[x] = typeList.get(x);//收盘价未来变动结果
//            data.label[x] = Integer.valueOf(colArr[22]);//交易量未来变动结果
        }
        data.rows = rows;
//        NumericAttribute[] attributes = new NumericAttribute[data.matrix[0].length];
//        attributes[0] = new NumericAttribute("c1", null, 1.0);
//        attributes[1] = new NumericAttribute("c2", null, 1.0);
//        attributes[2] = new NumericAttribute("c3", null, 1.0);
//        attributes[3] = new NumericAttribute("c4", null, 1.0);
//        attributes[4] = new NumericAttribute("c5", null, 1.0);
//        attributes[5] = new NumericAttribute("c6", null, 1.0);
//        attributes[6] = new NumericAttribute("c7", null, 1.0);
        return data;
    }

    private double[] loadDay(String[] colArr) {

//            System.out.println(colArr.length);
//            for (int y = 0; y < 7; y++) {
//                data.matrix[x][y] = Double.valueOf(colArr[y + 7]);
//            }
        ArrayList<Double> list = Lists.newArrayList(
                Double.valueOf(colArr[1]),
                Double.valueOf(colArr[2]),
                Double.valueOf(colArr[3]),
                Double.valueOf(colArr[4]),
                Double.valueOf(colArr[7]),
                Double.valueOf(colArr[8]),
                Double.valueOf(colArr[9]),
                Double.valueOf(colArr[10]),
                //                    Double.valueOf(colArr[11]),
                Double.valueOf(colArr[12]),
                Double.valueOf(colArr[13]),
                //            data.matrix[x][6] = Double.valueOf(colArr[1]);
                //            data.matrix[x][7] = Double.valueOf(colArr[2]);
                //            data.matrix[x][8] = Double.valueOf(colArr[3]);
                //            data.matrix[x][9] = Double.valueOf(colArr[4]);
                //            data.matrix[x][10] = Double.valueOf(colArr[5]);
                //            data.matrix[x][11] = Double.valueOf(colArr[6]);
                //                    Double.valueOf(colArr[17]),
                Double.valueOf(colArr[18]),
                //                     Double.valueOf(colArr[20]),
                Double.valueOf(colArr[21]),
                Double.valueOf(colArr[23])
                //            data.matrix[x][7] = Double.valueOf(colArr[22]);
                //            double c11;//17 //月均价
                //            double c12;//18//月均价一日变动
                //            double c13;//19//月均价未来10日变动
                //            double c14;//20//月均量
                //            double c15;//21//月均量一日变动
                //            double c16;//22//月均量未来10日变动
                //double c17;//23//均量量比
        );

        double[] day = new double[list.size()];

        for (int i = 0; i < list.size(); i++) {
            day[i] = list.get(i);
        }
        return day;
    }


    class Data {
        String[] rows;
        double[][] matrix;
        int[] label;
        NumericAttribute[] attributes;
    }

    public void calculateFile(File file, File result, String year) throws LackDataException {
        String[] txt = FileUtil.read(file).split("\n");
//        String[] txt = FileUtil.read(new File("D:\\tmp\\code_data\\600000.txt")).split("\n");
        Code[] codes = new Code[txt.length];
        for (int i = 0; i < txt.length; i++) {
            codes[i] = new Code(txt[i]);
        }
        if (codes.length < checkData)
            throw new LackDataException("数据不全");
        Avg(codes);
        for (int i = rage; i < codes.length; i++) {
            if (StringUtils.isNotEmpty(year) && !codes[i].date.startsWith(year))
                continue;
            calculateOne(i, codes);
//            if (codes[i].done)
//                    FileUtil.write(new File("D:\\tmp\\calculate\\600093.txt"), codes[i].calculateStr() + "\n", true);
            FileUtil.write(result, codes[i].calculateStr() + "\n", true);
        }
    }

    @Test
    public void calculateCodeTest() throws Exception {
        File file = new File("D:\\tmp\\calculate\\600708_2015.txt");
        calculateFile(file, new File("D:\\tmp\\calculate\\600708.txt"), null);
//        calculateFile(file, new File("D:\\tmp\\calculate\\600479.txt"), "2007");

    }

    @Test
    public void calculateAll() throws Exception {
        File root = new File("D:\\tmp\\code_data");


        int cnt = 0;
        int s = 0;
        int size = 50;
        File result = new File("D:\\tmp\\calculate\\" + s + "_" + size + ".txt");
        result.delete();
        for (File f : root.listFiles()) {
            System.out.println(cnt++ + "\t" + f.getName());
//            if (!f.getName().equals("600093.txt")) {
//                continue;
//            }
            if (cnt < s)
                continue;
            if (cnt > s + size)
                break;
            //FileUtil.write(new File("D:\\tmp\\calculate\\" + s + "_" + size + ".txt")
            calculateFile(f, result, null);
        }
    }

    int rage = 10;

    private void Avg(Code[] codes) {
        for (int i = rage; i < codes.length; i++) {
            Code code = codes[i];
            double priceMonth = 0;
            double quanMonth = 0;
            for (int x = i - rage; x < i; x++) {
                priceMonth += codes[x].priceAvg;
                quanMonth += codes[x].quantity;
            }
            code.c11 = priceMonth / rage;
            code.c14 = quanMonth / rage;
        }
    }

    /**
     * 使用20日线
     * 加入交易量参数
     *
     * @param i
     * @param codes
     */
    private void calculateOne(int i, Code[] codes) {

        try {
            Code code = codes[i];
            //均价变动
            code.c1 = (codes[i].priceAvg - codes[i - 1].priceAvg) / codes[i - 1].priceAvg * 100;
            //最高变动
            code.c2 = (codes[i].highest - codes[i - 1].highest) / codes[i - 1].highest * 100;
            //最低变动
            code.c3 = (codes[i].lowest - codes[i - 1].lowest) / codes[i - 1].lowest * 100;
            //高低差变动
            if (codes[i - 1].diff == 0) {
                code.c4 = 100;
            } else {
                code.c4 = (codes[i].diff - codes[i - 1].diff) / codes[i - 1].diff * 100;
            }
            //量比
            double quantitySum = 0;
            for (int x = i - 5; x < i; x++) {
                quantitySum += codes[x].quantity;
            }
            code.c5 = code.quantity / (quantitySum / 5);
            //收盘5日涨幅
            code.c6 = (codes[i].end - codes[i - 5].end) / codes[i - 5].end * 100;
            //收盘10日涨幅
            code.c7 = (codes[i].end - codes[i - 10].end) / codes[i - 10].end * 100;
            //5日涨
//            code.c8 = codes[i].end < codes[i + 5].end ? 1 : 0;
            //10日涨 30%
//            double rise = (codes[i + 10].end - codes[i].end) / codes[i].end;
            //0=涨幅>10%,
            // 1=跌幅>10%,
            // 2=-10%<区间<10%
//            if (rise > 0.3) {
//                code.c9 = 0;
//            } else if (rise > 0.2) {
//                code.c9 = 1;
//            } else if (rise > 0.1) {
//                code.c9 = 2;
//            } else if (rise > 0.0) {
//                code.c9 = 3;
//            } else if (rise > -0.1) {
//                code.c9 = 4;
//            } else if (rise > -0.2) {
//                code.c9 = 5;
//            } else {
//                code.c9 = 6;
//            }
//            code.c10 = codes[i].end < codes[i + 30].end ? 1 : 0;
//            if (codes[i - 1].c11 == 0 || i + 10 > codes.length - 1) {
//                return;
//            }
            code.c12 = code.c11 > codes[i - 5].c11 ? 1 : 0;
            code.c15 = code.c14 > codes[i - 5].c14 ? 1 : 0;
            if (i + 5 > codes.length - 1) {
                code.c13 = code.c16 = -1;
            } else {
                code.c13 = code.c11 < codes[i + 5].c11 ? 1 : 0;
                code.c16 = code.c14 < codes[i + 5].c14 ? 1 : 0;

            }
            //均量量比
            quantitySum = 0.00;
            int flag = 10;
            for (int x = i - flag; x < i; x++) {
                quantitySum += codes[x].c14;
            }

            code.c17 = code.c14 / (quantitySum == 0 ? code.c14 : quantitySum / flag);
//            code.c17 = code.c17 > 1 ? 1 : 0;
            code.done = true;
//            System.out.println(i);
        } catch (Throwable e) {
            logger.error("calculate", e);

        }


    }

    public void downloadData(String code, int year, File output) throws Exception {
        List<Code> codes = new ArrayList<Code>();
        for (int y = year; y <= year; y++) {  //循环年
            for (int z = 1; z <= 4; z++) {                //循环季度
                String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code + ".phtml?year=" + y + "&jidu=" + z;
                codes.addAll(onePage(url));
            }
        }

        Collections.sort(codes, new Comparator<Code>() {
            @Override
            public int compare(Code o1, Code o2) {
                return o1.date.compareTo(o2.date);
            }
        });
        for (Code c : codes) {
            FileUtil.write(output, c.toString() + "\n", true);
        }

        if (codes.size() < checkData)
            throw new LackDataException("");
    }

    //    000063
    @Test
    public void downloadData1() throws Exception {
        downloadData("", 2015, new File("D:\\tmp\\calculate\\" + "" + "_" + 1 + ".txt"));
    }

    @Test
    public void downloadData() throws Exception {
//        Data train = onePage();
        //循环股票代码
        String[] txt = FileUtil.read(new File("D:\\tmp\\codes.txt")).split("\n");
        go1:
        for (int x = 0; x < txt.length; x++) {
            List<Code> codes = new ArrayList<Code>();
            String code = txt[x];
            for (int y = 2006; y <= 2009; y++) {  //循环年
                for (int z = 1; z <= 4; z++) {                //循环季度
                    String url = "http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/" + code + ".phtml?year=" + y + "&jidu=" + z;
                    try {
                        codes.addAll(onePage(url));
                    } catch (Throwable e) {
                        continue go1;
                    }

                }
            }
            Collections.sort(codes, new Comparator<Code>() {
                @Override
                public int compare(Code o1, Code o2) {
                    return o1.date.compareTo(o2.date);
                }
            });
            for (Code c : codes) {
                FileUtil.write(new File("D:\\tmp\\code_data\\" + code + ".txt"), c.toString() + "\n", true);
            }
        }

    }

    HttpDownloader downloader = new HttpDownloader(1);

    //http://money.finance.sina.com.cn/corp/go.php/vMS_MarketHistory/stockid/000063.phtml?year=2006&jidu=3
    public List<Code> onePage(String url) {
        List<Code> codes = new ArrayList<Code>();
        try {
            PageObj page = downloader.download(new RequestOjb(url),
                    ReqCfg.deft().setTimeOut(10000));
            Document doc = Jsoup.parse(page.getRawText());
            Elements elements = doc.select("table#FundHoldSharesTable>tbody>tr");
            elements.remove(0);
            for (Element e : elements) {
                Code code = new Code();
                code.date = e.child(0).text();//            日期
                code.start = Double.valueOf(e.child(1).text());//            开盘价
                code.highest = Double.valueOf(e.child(2).text());//            最高价
                code.end = Double.valueOf(e.child(3).text());//            收盘价
                code.lowest = Double.valueOf(e.child(4).text());//            最低价
                code.quantity = Double.valueOf(e.child(5).text());//            交易量(股)
                code.money = Double.valueOf(e.child(6).text());//            交易金额(元)
//                logger.info("{}", JSON.toJSONString(code));
                codes.add(code);
            }
        } catch (Throwable e) {
        }

        return codes;

    }


}