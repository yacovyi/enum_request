import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class test_enum {

   private static final String language = "ES";
   private final int threads = 1;
   private int numOfRetry = 10;
   private ObjectMapper mapper = new ObjectMapper();
   private HttpClient httpClient = HttpClientBuilder.create().build();
   private static final String base_url = "http://10.42.128.166:12007/enums/handle/";
   //private static final String base_url = "http://10.42.128.166:12007/enums/handle/";
   private ExecutorService executor = Executors.newFixedThreadPool(threads);
   private CopyOnWriteArrayList<Statistics> statistics = new CopyOnWriteArrayList<Statistics>();
   private static  TransportClient client;
   @BeforeAll
   public static void beforeAll() throws UnknownHostException {
      // on startup
      Settings settings = Settings.builder()
              .put("cluster.name", "wscns").build();
      client = new PreBuiltTransportClient(settings)
              .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));


   }
   @AfterAll
   public static void afterAll(){
      client.close();
   }

   @Test
   public void test_all() throws InterruptedException {
      test(null);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.DAYS);
      Utils.printStatistics(statistics);
   }

   @Test
   public void test_get() throws InterruptedException {
      test(true);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.DAYS);
      Utils.printStatistics(statistics);
   }
   @Test
   public void test_post() throws InterruptedException {
      test(false);
      executor.shutdown();
      executor.awaitTermination(1, TimeUnit.DAYS);
      Utils.printStatistics(statistics);
   }

   public void test(Boolean newAPI) throws InterruptedException {
      for(int i=0;i<threads;i++){
         final int threadNumber = i;
         executor.execute(new Runnable() {
            public void run() {
               try {
                  if (newAPI!=null){
                     post_get_all_enums(threadNumber, newAPI);
                  }else{
                     post_get_all_enums(threadNumber, true);
                     post_get_all_enums(threadNumber, false);
                  }

               } catch (IOException e) {
                  throw new RuntimeException(e);
               }
            }
         });
      }


   }


   public void post_get_all_enums(int threadNumber,final boolean newAPI) throws IOException {
      List<String> enums = get_enum_list();
      //System.out.println("Thread " + threadNumber + " Started") ;
      for(String enumListName : enums){
         //String enumListName = "VEHICLE_MODEL";
         for (int i=0;i<numOfRetry;i++){
            Statistics requestStatistic = new Statistics();

            requestStatistic.setStartRequestTime(ZonedDateTime.now());
            requestStatistic.setEnumListName(enumListName);
            requestStatistic.setHttpMethod(newAPI ? "GET" : "POST");
            requestStatistic.setLanguage(language);
            requestStatistic.setThreadName("Thread " + threadNumber);

            List<IcdEnumItemReply> enumItems = getEnumList(enumListName, language, newAPI);
            Assertions.assertNotNull(enumItems);
            //System.out.println("Thread " + threadNumber + " Fetch " + enumListName) ;

            requestStatistic.setEndRequestTime(ZonedDateTime.now());
            statistics.add(requestStatistic);

            String jsonRequestStatistic = mapper.writeValueAsString(requestStatistic);

            IndexResponse response = client.prepareIndex("statistics_index", "statistics")
                    .setSource(jsonRequestStatistic)
                    .get();
         }
         break;
      }
   }
   public List<IcdEnumItemReply> getEnumList(String enumListName,String language, boolean newAPI) throws IOException {
      if (newAPI){
         return getEnumListNewAPI(enumListName, language);
      }else{
         return getEnumListOldAPI(enumListName, language);
      }
   }
   public List<IcdEnumItemReply> getEnumListOldAPI(String enumListName,String language) throws IOException {

      ObjectNode objectNode1 = mapper.createObjectNode();
      objectNode1.put("listName", enumListName);
      objectNode1.put("language", language);

      HttpPost postRequest = new HttpPost(
              base_url + "GetEnumRequest");
      StringEntity input = new StringEntity(objectNode1.toString());
      input.setContentType("application/json");
      postRequest.setEntity(input);

      //set headers
      addHeaders(postRequest);

      HttpResponse response = httpClient.execute(postRequest);
      Assertions.assertTrue(response.getStatusLine().getStatusCode() == 200);

      Map<String, List<IcdEnumItemReply>> jsonMap = mapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, List<IcdEnumItemReply>>>() {} );
      Assertions.assertNotNull(jsonMap);
      Assertions.assertTrue(jsonMap.containsKey("itemList"));
      Assertions.assertTrue(jsonMap.get("itemList") instanceof List);
      Assertions.assertTrue(((List)jsonMap.get("itemList")).size() > 0);
      return (( List<IcdEnumItemReply>)jsonMap.get("itemList"));
   }

   public List<IcdEnumItemReply> getEnumListNewAPI(String enumListName,String language) throws IOException {

      String url = String.format("%s%s/%s?language=%s",base_url, "GetEnumRequest", enumListName, language);
      HttpGet getRequest = new HttpGet(url);

      //set headers
      addHeaders(getRequest);

      HttpResponse response = httpClient.execute(getRequest);
      Assertions.assertTrue(response.getStatusLine().getStatusCode() == 200);

      Map<String, List<IcdEnumItemReply>> jsonMap = mapper.readValue(response.getEntity().getContent(), new TypeReference<Map<String, List<IcdEnumItemReply>>>() {} );
      Assertions.assertNotNull(jsonMap);
      Assertions.assertTrue(jsonMap.containsKey("itemList"));
      Assertions.assertTrue(jsonMap.get("itemList") instanceof List);
      Assertions.assertTrue(((List)jsonMap.get("itemList")).size() > 0);
      return (( List<IcdEnumItemReply>)jsonMap.get("itemList"));
   }


   public List<String> get_enum_list() throws IOException {
      //Assertions.assertTrue(false);
      String listNames = "listNames";

      HttpGet request = new HttpGet(
              base_url + "GetAllListNamesRequest");

      //set headers
      addHeaders(request);

      HttpResponse response = httpClient.execute(request);
      Assertions.assertTrue(response.getStatusLine().getStatusCode() == 200);

      Map<String, ArrayList<String>> jsonMap = mapper.readValue(response.getEntity().getContent(), Map.class);
      Assertions.assertNotNull(jsonMap);
      Assertions.assertTrue(jsonMap.containsKey(listNames));
      Assertions.assertTrue(jsonMap.get(listNames) instanceof List);
      List<String> enums = jsonMap.get(listNames);

      return enums;
   }

   private void addHeaders(org.apache.http.client.methods.HttpRequestBase requestBase){
      requestBase.addHeader("WS-USER-INFO.NAME", "wstu11");
      requestBase.addHeader("WS-USER-INFO.DIVISION-ID", "POS3");
      requestBase.addHeader("WS-USER-INFO.POSITION-ID", "POS11");
      requestBase.addHeader("WS-USER-INFO.CLASSIFICATION-VALUE", "100");
      requestBase.addHeader("WS-USER-INFO.ROLES", "system_role_analyst");
   }

}
