import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.*;
import java.util.*;

// Do not change the signature of this class
public class TextAnalyzer extends Configured implements Tool {

    // Replace "?" with your own output key / value types
    // The four template data types are:
    //     <Input Key Type, Input Value Type, Output Key Type, Output Value Type>
    public static class TextMapper extends Mapper<LongWritable, Text, Text, Tuple> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
        private Tuple edge = new Tuple();

        public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you mapper function
            String line = value.toString().toLowerCase().replaceAll("[^a-zA-Z0-9]", " ");
            Set<String> wordSet = new HashSet<String>();
            StringTokenizer itr = new StringTokenizer(line);
            while (itr.hasMoreTokens()) {
                wordSet.add(itr.nextToken());
            }

            for (String s1 : wordSet) {
                for (String s2 : wordSet) {
                    if (!s1.equals(s2)) {
                        word.set(s1);
                        edge.set(new Text(s2), one);
                        context.write(word, edge);
                    }
                }
            }
        }
    }

    // Replace "?" with your own key / value types
    // NOTE: combiner's output key / value types have to be the same as those of mapper
    public static class TextCombiner extends Reducer<Text, Tuple, Text, Tuple> {
        private Tuple value = new Tuple();

        public void reduce(Text key, Iterable<Tuple> tuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you combiner function
            Map<String, Integer> edges = new HashMap<String, Integer>();
            for (Tuple t : tuples) {
                String neighbor = t.getValue().toString();
                int weight = t.getCount().get();
                if (edges.containsKey(neighbor)) {
                    int currentWeight = edges.get(neighbor);
                    edges.put(neighbor, currentWeight + weight);
                } else {
                    edges.put(neighbor, weight);
                }
            }

            for(String neighbor : edges.keySet()){
                int weight = edges.get(neighbor);
                value.set(new Text(neighbor), new IntWritable(weight));
                context.write(key, value);
            }
        }
    }

    // Replace "?" with your own input key / value types, i.e., the output
    // key / value types of your mapper function
    public static class TextReducer extends Reducer<Text, Tuple, Text, Text> {
        private final static Text emptyText = new Text("");

        public void reduce(Text key, Iterable<Tuple> queryTuples, Context context)
            throws IOException, InterruptedException
        {
            // Implementation of you reducer function
            Map<String, Integer> edges = new HashMap<String, Integer>();
            for (Tuple t : queryTuples) {
                String neighbor = t.getValue().toString();
                int weight = t.getCount().get();
                if (edges.containsKey(neighbor)) {
                    int currentWeight = edges.get(neighbor);
                    edges.put(neighbor, currentWeight + weight);
                } else {
                    edges.put(neighbor, weight);
                }
            }

            // Write out the results; you may change the following example
            // code to fit with your reducer function.
            //   Write out each edge and its weight
	        Text value = new Text();
            for(String neighbor : edges.keySet()){
                String weight = edges.get(neighbor).toString();
                value.set(" " + neighbor + " " + weight);
                context.write(key, value);
            }
            //   Empty line for ending the current context key
            context.write(emptyText, emptyText);
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = this.getConf();

        // Create job
        Job job = new Job(conf, "EID1_EID2"); // Replace with your EIDs
        job.setJarByClass(TextAnalyzer.class);

        // Setup MapReduce job
        job.setMapperClass(TextMapper.class);
        
	// set local combiner class
        job.setCombinerClass(TextCombiner.class);
	// set reducer class        
	job.setReducerClass(TextReducer.class);

        // Specify key / value types (Don't change them for the purpose of this assignment)
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        //   If your mapper and combiner's  output types are different from Text.class,
        //   then uncomment the following lines to specify the data types.
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Tuple.class);

        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);

        // Output
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.setOutputFormatClass(TextOutputFormat.class);

        // Execute job and return status
        return job.waitForCompletion(true) ? 0 : 1;
    }

    // Do not modify the main method
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new TextAnalyzer(), args);
        System.exit(res);
    }

    // You may define sub-classes here.
    public static class Tuple implements WritableComparable{

        private Text value;
        private IntWritable count;

        Tuple(){
            set(new Text(),new IntWritable());
        }

        Tuple(Text v,IntWritable c){
            set(v,c);
        }

        @Override
        public void write(DataOutput out) throws IOException{
            value.write(out);
            count.write(out);
        }

        public void set(Text t,IntWritable c){
            this.value=t;
            this.count=c;
        }

        @Override
        public void readFields(DataInput input) throws IOException {
            value.readFields(input);
            count.readFields(input);
        }
        
        @Override
        public int compareTo(Object o){
            Tuple that=(Tuple) o;
            return this.value.compareTo(that.getValue());
        }

        
        public int hashCode(){
            return value.hashCode()*100+count.hashCode();
        }

        public String toString(){
            return value+" "+count;
        }

        public Text getValue(){
            return this.value;
        }

        public IntWritable getCount(){
            return this.count;
        }
    }
}



