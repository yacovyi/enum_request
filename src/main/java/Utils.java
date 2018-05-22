import java.util.List;

public class Utils {
    public static void printStatistics(List<Statistics> statisticsList){
        long avg = 0 , max = Long.MIN_VALUE , min = Long.MAX_VALUE, requestTimeSum = 0;
        String requestMethod = null;
        for(Statistics statistics : statisticsList){
            if (statistics.getRequestTimeMS() < min){
                min = statistics.getRequestTimeMS();
            }
            if (statistics.getRequestTimeMS() > max){
                max = statistics.getRequestTimeMS();
            }
            requestTimeSum += statistics.getRequestTimeMS();
            if (requestMethod == null)
                requestMethod = statistics.getHttpMethod();
        }
        avg = requestTimeSum/statisticsList.size();
        System.out.println(String.format("REQUEST %S TIME : \n AVG = %sms\n MAX = %sms\n MIN = %sms", requestMethod, avg , max, min));
    }
}
