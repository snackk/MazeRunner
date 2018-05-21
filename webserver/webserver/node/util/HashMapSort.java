package webserver.node.util;

import webserver.node.NodeInfo;

import java.util.*;

public class HashMapSort {

    public static boolean ASC = true;
    public static boolean DESC = false;

    public static Map<String, NodeInfo> sortByCpuUsage(Map<String, NodeInfo> unsortedMap, final boolean order) {

        List<Map.Entry<String, NodeInfo>> list = new LinkedList<Map.Entry<String, NodeInfo>>(unsortedMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, NodeInfo>>()
        {
            public int compare(Map.Entry<String, NodeInfo> o1, Map.Entry<String, NodeInfo> o2) {

                if (order)
                    return o1.getValue().getCpuLoad() > o2.getValue().getCpuLoad() ? 1 : 0;
                else
                    return o1.getValue().getCpuLoad() > o2.getValue().getCpuLoad() ? 0 : 1;
            }
        });

        Map<String, NodeInfo> sortedMap = new LinkedHashMap<String, NodeInfo>();
        for (Map.Entry<String, NodeInfo> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
