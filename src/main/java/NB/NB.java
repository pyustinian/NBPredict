package NB;

import java.io.IOException;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class NB {
    public static class WorldCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            //把value对应的行数据按照指定的间隔符拆分开
            String[] words = value.toString().split("\t");
            //word[0]是评价（好评或者差评）
            //word[1]是评价的内容
            //过滤一下有些评价后面没有关键字
            if (words.length == 2){
                String[] pjs = words[1].split(" ");
                for (String pj : pjs) {
                    //如果含有非中文的就过滤掉
                    if (isAllChinese(pj)){
                        context.write(new Text(words[0] + "_" + pj), new IntWritable(1));
                    }
                }
                //统计好评差评数目
                context.write(new Text("统计_"+words[0]), new IntWritable(1));
            }
        }
        /**
         * 判断字符串是否全为中文
         */
        public boolean isAllChinese(String str) {

            if (str == null) {
                return false;
            }
            for (char c : str.toCharArray()) {
                if (!isChinese(c)) {
                    return false;
                }
            }
            return true;
        }
        /**
         * 判断单个字符是否为中文
         */
        public Boolean isChinese(char c) {
            return c >= 0x4E00 && c <= 0x9Fa5;
        }
    }

    public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int count = 0;
            Iterator<IntWritable> iterator = values.iterator();
            while (iterator.hasNext()){
                IntWritable value = iterator.next();
                count += value.get();
            }
            context.write(key, new IntWritable(count));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration configuration = new Configuration();
        //启动Job任务
        Job job = Job.getInstance(configuration, "FeelCountJob");
        job.setJarByClass(NB.class);

        //指定输入路径和读取方式
        job.setInputFormatClass(TextInputFormat.class);
        TextInputFormat.addInputPath(job, new Path("D:\\实验\\training.txt"));
        //map阶段
        job.setMapperClass(WorldCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        //reducer阶段
        job.setReducerClass(WordCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        //设置输出类型
        job.setOutputFormatClass(TextOutputFormat.class);
        //设置输出路径
        TextOutputFormat.setOutputPath(job, new Path("D:\\实验\\model.txt"));

        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
}
