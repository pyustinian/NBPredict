package predict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.io.PrintStream;

public class predictTest {
    private final static String modelFilePath="D:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.3\\work\\NB_2018082027_2018082027_2018082032\\model_predict\\model.txt";
    private final static String testFilePath="D:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.3\\work\\NB_2018082027_2018082027_2018082032\\model_predict\\test.txt";
    public static HashMap<String, Integer> parameters = null; // 情感标签集
    public static double Nd = 0.;// 文件中的总记录数
    public static HashMap<String, Integer> allFeatures = null;// 整个训练样本的键值对
    public static HashMap<String, Double> labelFeatures = null;// 某一类别下所有词出现的总数
    public static HashSet<String> V = null;// 总训练样本的不重复单词

    //d对训练数据进行二次处理，得到多项式模型
    public static void loadModel(String modelFile) throws Exception {
        if (parameters != null && allFeatures != null) {
            return;
        }
        parameters = new HashMap<String, Integer>();/* 情感标签集 */
        allFeatures = new HashMap<String,Integer>();// 全部属性对
        labelFeatures = new HashMap<String, Double>();// 某一类别下所有词出现的总数
        V = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader(modelFile));
        String line;
        while ((line = br.readLine()) != null) {
            String feature = line.substring(0, line.indexOf("\t"));
            Integer count = Integer.parseInt(line.substring(line.indexOf("\t") + 1));
            if (feature.contains("_")) {
                allFeatures.put(feature, count);
                String label = feature.substring(0, feature.indexOf("_"));
                if (labelFeatures.containsKey(label)) {
                    labelFeatures.put(label, labelFeatures.get(label) + count);
                } else {
                    labelFeatures.put(label, (double) count);
                }
                String word = feature.substring(feature.indexOf("_") + 1);
                V.add(word);
            } else {
                parameters.put(feature, count);
                Nd += count;
            }
        }
        br.close();
    }

    //计算条件概率
    public static String predict(ArrayList<String> sentence, String modelFile) throws Exception {
        loadModel(modelFile);
        String predLabel = null;
        double maxValue = Double.NEGATIVE_INFINITY;// 最大类概率（默认值为负无穷小）
        Set<String> labelSet = parameters.keySet(); // 获得标签集
        for (String label : labelSet) {
            double tempValue = Math.log(parameters.get(label) / Nd);// 先验概率
            //先验概率P(c)= 类c下单词总数/整个训练样本的单词总数 parameters .get(label):类别c对应的文档在训练数据集中的计数
            //Nd:整个训练样本的单词总数
            for (int i=0;i<sentence.size();i++) {
                String word=sentence.get(i);
                String lf = label + "_" + word;
                // 计算最大似然概率
                if (allFeatures.containsKey(lf)) {
                    tempValue += Math.log((double) (allFeatures.get(lf) + 1) / (labelFeatures.get(label) + V.size()));
                    //多项式原理 类条件概率P(tk|c)=(类c下单词tk在各个文档中出现过的次数之和+1)/(类c下单词总数+|V|)
                    //allFeatures.get(lf)：类别c与词语 w共同出现的次数 labelFeatures.get(label) +
                    //V.size()：类别c下属性总数+该训练文本中词语总数 Laplace Smoothing处理未出现在训练集中的数据 +1
                } else {
                    tempValue += Math.log( 1.0 / (labelFeatures.get(label) + V.size()));
                }
            }
            if (tempValue > maxValue) {
                maxValue = tempValue;
                predLabel = label;
            }
        }
        return predLabel;
    }

    public static void main(String[] args) throws Exception {
        ArrayList<ArrayList<String>> testSet=new ArrayList<ArrayList<String>>();
        File file=new File(testFilePath);
        BufferedReader reader=new BufferedReader(new FileReader(file));
        String str1;
        //读入测试数据，保存到List
        while((str1=reader.readLine())!=null){
            str1=str1.replaceAll("\t"," ");
            StringTokenizer tokenizer=new StringTokenizer(str1);
            ArrayList<String> s=new ArrayList<String>();
            while(tokenizer.hasMoreTokens()){
                s.add(tokenizer.nextToken());
            }
            testSet.add(s);
        }
        ArrayList<String> res=new ArrayList<String>();
        System.out.println(testSet.size());
        for (ArrayList<String> s1 : testSet) {
            String s2 = predict(s1, modelFilePath);
            res.add(s2);
        }
        //记录好评数量
        int count=0;
        //预测结果输出到TXT文件中
        PrintStream ps=new PrintStream("D:\\Program Files\\JetBrains\\IntelliJ IDEA 2020.3\\work\\NB_2018082027_2018082027_2018082032\\model_predict\\2018082027_预测结果.txt");
        System.setOut(ps);
        //预测结果的输出
        for(int i=0;i<testSet.size();i++){
            System.out.println(i+"\t"+testSet.get(i).get(0));
            String value=testSet.get(i).get(0);
            if(res.get(i).equals(value)){
                count++;
            }
        }
        double wc=(double)count/testSet.size();
        System.out.println("正确率："+wc);
    }
}
