/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-10-26 16:24 创建
 */

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yiji.falcon.agent.util.JSONUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guqiu@yiji.com
 */
public class MongoDBTest {

    private static final String msg = "{\n" +
            "        \"host\" : \"ubuntuServer\",\n" +
            "        \"version\" : \"2.6.12\",\n" +
            "        \"process\" : \"mongod\",\n" +
            "        \"pid\" : NumberLong(7537),\n" +
            "        \"uptime\" : 11066,\n" +
            "        \"uptimeMillis\" : NumberLong(11066115),\n" +
            "        \"uptimeEstimate\" : 10008,\n" +
            "        \"localTime\" : ISODate(\"2016-10-25T10:26:27.213Z\"),\n" +
            "        \"asserts\" : {\n" +
            "                \"regular\" : 0,\n" +
            "                \"warning\" : 0,\n" +
            "                \"msg\" : 0,\n" +
            "                \"user\" : 0,\n" +
            "                \"rollovers\" : 0\n" +
            "        },\n" +
            "        \"backgroundFlushing\" : {\n" +
            "                \"flushes\" : 184,\n" +
            "                \"total_ms\" : 9,\n" +
            "                \"average_ms\" : 0.04891304347826087,\n" +
            "                \"last_ms\" : 0,\n" +
            "                \"last_finished\" : ISODate(\"2016-10-25T10:26:01.326Z\")\n" +
            "        },\n" +
            "        \"connections\" : {\n" +
            "                \"current\" : 1,\n" +
            "                \"available\" : 51199,\n" +
            "                \"totalCreated\" : NumberLong(7)\n" +
            "        },\n" +
            "        \"cursors\" : {\n" +
            "                \"note\" : \"deprecated, use server status metrics\",\n" +
            "                \"clientCursors_size\" : 0,\n" +
            "                \"totalOpen\" : 0,\n" +
            "                \"pinned\" : 0,\n" +
            "                \"totalNoTimeout\" : 0,\n" +
            "                \"timedOut\" : 0\n" +
            "        },\n" +
            "        \"dur\" : {\n" +
            "                \"commits\" : 28,\n" +
            "                \"journaledMB\" : 0,\n" +
            "                \"writeToDataFilesMB\" : 0,\n" +
            "                \"compression\" : 0,\n" +
            "                \"commitsInWriteLock\" : 0,\n" +
            "                \"earlyCommits\" : 0,\n" +
            "                \"timeMs\" : {\n" +
            "                        \"dt\" : 3004,\n" +
            "                        \"prepLogBuffer\" : 0,\n" +
            "                        \"writeToJournal\" : 0,\n" +
            "                        \"writeToDataFiles\" : 0,\n" +
            "                        \"remapPrivateView\" : 0\n" +
            "                }\n" +
            "        },\n" +
            "        \"extra_info\" : {\n" +
            "                \"note\" : \"fields vary by platform\",\n" +
            "                \"heap_usage_bytes\" : 62540104,\n" +
            "                \"page_faults\" : 0\n" +
            "        },\n" +
            "        \"globalLock\" : {\n" +
            "                \"totalTime\" : NumberLong(\"11066115000\"),\n" +
            "                \"lockTime\" : NumberLong(128753),\n" +
            "                \"currentQueue\" : {\n" +
            "                        \"total\" : 0,\n" +
            "                        \"readers\" : 0,\n" +
            "                        \"writers\" : 0\n" +
            "                },\n" +
            "                \"activeClients\" : {\n" +
            "                        \"total\" : 0,\n" +
            "                        \"readers\" : 0,\n" +
            "                        \"writers\" : 0\n" +
            "                }\n" +
            "        },\n" +
            "        \"indexCounters\" : {\n" +
            "                \"accesses\" : 2,\n" +
            "                \"hits\" : 2,\n" +
            "                \"misses\" : 0,\n" +
            "                \"resets\" : 0,\n" +
            "                \"missRatio\" : 0\n" +
            "        },\n" +
            "        \"locks\" : {\n" +
            "                \".\" : {\n" +
            "                        \"timeLockedMicros\" : {\n" +
            "                                \"R\" : NumberLong(298356),\n" +
            "                                \"W\" : NumberLong(128753)\n" +
            "                        },\n" +
            "                        \"timeAcquiringMicros\" : {\n" +
            "                                \"R\" : NumberLong(139322),\n" +
            "                                \"W\" : NumberLong(35990)\n" +
            "                        }\n" +
            "                },\n" +
            "                \"admin\" : {\n" +
            "                        \"timeLockedMicros\" : {\n" +
            "                                \"r\" : NumberLong(9513),\n" +
            "                                \"w\" : NumberLong(0)\n" +
            "                        },\n" +
            "                        \"timeAcquiringMicros\" : {\n" +
            "                                \"r\" : NumberLong(352),\n" +
            "                                \"w\" : NumberLong(0)\n" +
            "                        }\n" +
            "                },\n" +
            "                \"local\" : {\n" +
            "                        \"timeLockedMicros\" : {\n" +
            "                                \"r\" : NumberLong(82448),\n" +
            "                                \"w\" : NumberLong(27)\n" +
            "                        },\n" +
            "                        \"timeAcquiringMicros\" : {\n" +
            "                                \"r\" : NumberLong(27730),\n" +
            "                                \"w\" : NumberLong(1)\n" +
            "                        }\n" +
            "                }\n" +
            "        },\n" +
            "        \"network\" : {\n" +
            "                \"bytesIn\" : 2327,\n" +
            "                \"bytesOut\" : 15704,\n" +
            "                \"numRequests\" : 31\n" +
            "        },\n" +
            "        \"opcounters\" : {\n" +
            "                \"insert\" : 1,\n" +
            "                \"query\" : 369,\n" +
            "                \"update\" : 0,\n" +
            "                \"delete\" : 0,\n" +
            "                \"getmore\" : 0,\n" +
            "                \"command\" : 33\n" +
            "        },\n" +
            "        \"opcountersRepl\" : {\n" +
            "                \"insert\" : 0,\n" +
            "                \"query\" : 0,\n" +
            "                \"update\" : 0,\n" +
            "                \"delete\" : 0,\n" +
            "                \"getmore\" : 0,\n" +
            "                \"command\" : 0\n" +
            "        },\n" +
            "        \"recordStats\" : {\n" +
            "                \"accessesNotInMemory\" : 0,\n" +
            "                \"pageFaultExceptionsThrown\" : 0,\n" +
            "                \"admin\" : {\n" +
            "                        \"accessesNotInMemory\" : 0,\n" +
            "                        \"pageFaultExceptionsThrown\" : 0\n" +
            "                },\n" +
            "                \"local\" : {\n" +
            "                        \"accessesNotInMemory\" : 0,\n" +
            "                        \"pageFaultExceptionsThrown\" : 0\n" +
            "                }\n" +
            "        },\n" +
            "        \"writeBacksQueued\" : false,\n" +
            "        \"mem\" : {\n" +
            "                \"bits\" : 64,\n" +
            "                \"resident\" : 37,\n" +
            "                \"virtual\" : 342,\n" +
            "                \"supported\" : true,\n" +
            "                \"mapped\" : 80,\n" +
            "                \"mappedWithJournal\" : 160\n" +
            "        },\n" +
            "        \"metrics\" : {\n" +
            "                \"cursor\" : {\n" +
            "                        \"timedOut\" : NumberLong(0),\n" +
            "                        \"open\" : {\n" +
            "                                \"noTimeout\" : NumberLong(0),\n" +
            "                                \"pinned\" : NumberLong(0),\n" +
            "                                \"total\" : NumberLong(0)\n" +
            "                        }\n" +
            "                },\n" +
            "                \"document\" : {\n" +
            "                        \"deleted\" : NumberLong(0),\n" +
            "                        \"inserted\" : NumberLong(1),\n" +
            "                        \"returned\" : NumberLong(0),\n" +
            "                        \"updated\" : NumberLong(0)\n" +
            "                },\n" +
            "                \"getLastError\" : {\n" +
            "                        \"wtime\" : {\n" +
            "                                \"num\" : 0,\n" +
            "                                \"totalMillis\" : 0\n" +
            "                        },\n" +
            "                        \"wtimeouts\" : NumberLong(0)\n" +
            "                },\n" +
            "                \"operation\" : {\n" +
            "                        \"fastmod\" : NumberLong(0),\n" +
            "                        \"idhack\" : NumberLong(0),\n" +
            "                        \"scanAndOrder\" : NumberLong(0)\n" +
            "                },\n" +
            "                \"queryExecutor\" : {\n" +
            "                        \"scanned\" : NumberLong(0),\n" +
            "                        \"scannedObjects\" : NumberLong(0)\n" +
            "                },\n" +
            "                \"record\" : {\n" +
            "                        \"moves\" : NumberLong(0)\n" +
            "                },\n" +
            "                \"repl\" : {\n" +
            "                        \"apply\" : {\n" +
            "                                \"batches\" : {\n" +
            "                                        \"num\" : 0,\n" +
            "                                        \"totalMillis\" : 0\n" +
            "                                },\n" +
            "                                \"ops\" : NumberLong(0)\n" +
            "                        },\n" +
            "                        \"buffer\" : {\n" +
            "                                \"count\" : NumberLong(0),\n" +
            "                                \"maxSizeBytes\" : 268435456,\n" +
            "                                \"sizeBytes\" : NumberLong(0)\n" +
            "                        },\n" +
            "                        \"network\" : {\n" +
            "                                \"bytes\" : NumberLong(0),\n" +
            "                                \"getmores\" : {\n" +
            "                                        \"num\" : 0,\n" +
            "                                        \"totalMillis\" : 0\n" +
            "                                },\n" +
            "                                \"ops\" : NumberLong(0),\n" +
            "                                \"readersCreated\" : NumberLong(0)\n" +
            "                        },\n" +
            "                        \"preload\" : {\n" +
            "                                \"docs\" : {\n" +
            "                                        \"num\" : 0,\n" +
            "                                        \"totalMillis\" : 0\n" +
            "                                },\n" +
            "                                \"indexes\" : {\n" +
            "                                        \"num\" : 0,\n" +
            "                                        \"totalMillis\" : 0\n" +
            "                                }\n" +
            "                        }\n" +
            "                },\n" +
            "                \"storage\" : {\n" +
            "                        \"freelist\" : {\n" +
            "                                \"search\" : {\n" +
            "                                        \"bucketExhausted\" : NumberLong(0),\n" +
            "                                        \"requests\" : NumberLong(0),\n" +
            "                                        \"scanned\" : NumberLong(0)\n" +
            "                                }\n" +
            "                        }\n" +
            "                },\n" +
            "                \"ttl\" : {\n" +
            "                        \"deletedDocuments\" : NumberLong(0),\n" +
            "                        \"passes\" : NumberLong(184)\n" +
            "                }\n" +
            "        },\n" +
            "        \"ok\" : 1\n" +
            "}";

    @Test
    public void mongoTest(){
        JSONObject jsonObject = JSON.parseObject(transform(msg));
        Map<String,Object> map = new HashMap<>();
        JSONUtil.jsonToMap(map,jsonObject,null);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue() + " : " + entry.getValue().getClass());
        }
    }

    private String transform(String msg){
        return msg.replaceAll("\\w+\\(","")
                .replace(")","");
    }
}
