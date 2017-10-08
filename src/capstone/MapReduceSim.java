/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capstone;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.util.Arrays;
import toolbox.random.Random;
import toolbox.util.MathUtil;
import static java.util.stream.Collectors.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author pabernathy
 */
public class MapReduceSim {

    public static void doSim1() {
        String file1 = "/Users/pabernathy/pworkspace/Capstone/join2_genchanA.txt";
        String file2 = "join2_gennumA.txt";
        file1 = "join2_channelsSample.txt";
        file2 = "join2_viewsSample.txt";
        
        try {
            List<String> combined = getInput(file1, file2);
            
            String currentLineShow = "";
            int currentShowViews = 0;
            HashMap<String, ArrayList<String>> allShowChannelMap = new HashMap<>();
            HashMap<String, Integer> allShowViewsMap = new HashMap<>();
            
            //Find the total viewers for each show and all channels each show was on.
            for(String line : combined) {
                if(line == null) {
                    continue;
                }
                String[] keyValue = line.split(",");
                currentLineShow = keyValue[0];
                boolean hasViews = false;
                try {
                    currentShowViews = Integer.parseInt(keyValue[1]);
                    hasViews = true;
                } catch(NumberFormatException e) {
                    //do nothing; this is a line where the second part is the channel, not views
                }
                if(hasViews) {
                    if(allShowViewsMap.get(currentLineShow) == null) {
                        allShowViewsMap.put(currentLineShow, currentShowViews);
                    }
                    else {
                        allShowViewsMap.put(currentLineShow, allShowViewsMap.get(currentLineShow) + currentShowViews);
                    }
                } else {
                    if(allShowChannelMap.get(currentLineShow) == null) {
                        ArrayList<String> channels = new ArrayList<>();
                        channels.add(keyValue[1]);
                        allShowChannelMap.put(currentLineShow, channels);
                    }
                    else if(!allShowChannelMap.get(currentLineShow).contains(keyValue[1])) {
                        allShowChannelMap.get(currentLineShow).add(keyValue[1]);
                    }
                }
            }
            //System.out.println(allShowViewsMap);
            //System.out.println(allShowChannelMap);
            
            //Now combined the two into one.
            HashMap<String, ArrayList<String>> newShowChannelMap = new HashMap<>();
            for(String key : allShowChannelMap.keySet()) {
                ArrayList<String> value = allShowChannelMap.get(key);
                //allShowChannelMap.remove(key);
                key += " " + allShowViewsMap.get(key);
                //allShowChannelMap.put(key, value);
                newShowChannelMap.put(key, value);
            }
            System.out.println(newShowChannelMap);
            List<String> showChannels = flatten(newShowChannelMap, " ");
            showChannels.forEach(System.out::println);
            //Filter on whatever channel you want.
            //showChannels.stream().filter(s -> s.contains("ABC")).forEach(System.out::println);
            
        } catch(IOException e) {
            System.err.println(e.getClass() + " " + e.getMessage());
        } catch(Exception e) {
            System.err.println(e.getClass() + " " + e.getMessage());
        }
    }
    
    public static List<String> mixRandomly(List<String> a, List<String> b) throws Exception {
        List<String> all = new ArrayList<>();
        all.addAll(a);
        all.addAll(b);
        //System.out.println();
        //all.forEach(System.out::println);
        int size = all.size();
        Integer[] indeces = MathUtil.seqInteger(0, size - 1);
        //Arrays.stream(indeces).forEach(System.out::println);
        List<Integer> rands = Random.sample(indeces, size, false);
        //rands.stream().forEach(System.out::println);
        /*System.out.println(MathUtil.summary(Arrays.asList(indeces)));
        System.out.println(MathUtil.summary(rands));
        System.out.println(size + " " + rands.size() + " " + rands.stream().distinct().count());
        int s1 = 0;
        int s2 = 0;
        for(int i = 0; i < size; i++) {
            s1 += indeces[i];
            s2 += rands.get(i);
        }
        System.out.println(s1 + " " + s2);
        rands.stream().sorted().forEach(System.out::println);*/
        List<String> randomAll = new ArrayList<>();
        for(Integer i : indeces) {
            randomAll.add(all.get(rands.get(i)));
            //System.out.println(i + " " + rands.get(i) + " " + all.get(rands.get(i)));
        }
        return randomAll;
    }
    
    public static List<String> sort(List<String> input) {
        return input.stream().sorted((a, b) -> a.split(",")[0].compareTo(b.split(",")[0])).collect(toList());
    }
    public static List<String> flatten(Map<String, ArrayList<String>> input, String separator) {
        List<String> output = new ArrayList<>();
        if(input == null) {
            return output;
        }
        for(String key : input.keySet()) {
            for(String currentValue : input.get(key)) {
                output.add(new StringBuilder().append(key).append(separator).append(currentValue).toString());
            }
        }
        return output;
    }
    
    public static List<String> getInput(String file1, String file2) throws IOException, Exception {
        List<String> file1Text = Capstone.readLinesFromFile(file1);
        List<String> file2Text = Capstone.readLinesFromFile(file2);
        //System.out.println(file1Text.size() + " " + file2Text.size());

        List<String> combined = mixRandomly(file1Text, file2Text);
        //System.out.println(combined);
        //I think the MapReduce framework will sort it by show in this case.
        combined = sort(combined);
        //System.out.println(combined);
        return combined;
    }
    
    public static void doSim2() {
        
    }
}
